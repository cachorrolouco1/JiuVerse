/**
 * JIUVERSE PROXIMITY VOICE CHAT BLUEPRINT - NESTJS GATEWAY & WEBRTC SIGNALING
 * Specialty: Spatial Audio Attenuation, STUN/TURN Signaling, Moderation Streams & Client Hooks
 */

import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  OnGatewayConnection,
  OnGatewayDisconnect,
  MessageBody,
  ConnectedSocket,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { Injectable, Logger } from '@nestjs/common';

// Architectural configurations for ICE Servers (STUN/TURN)
export const ICE_SERVERS_CONFIG = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' }, 
    {
      urls: 'turn:turn.jiuverse.com:3478?transport=udp',
      username: 'jiuverse_fighter_voice',
      credential: 'OSS_SECRET_TOKEN_2026_PRODUCTION'
    }
  ]
};

// Distance parameters for proximity audio logic
const MAX_HEARING_DISTANCE = 15.0; // Meters
const MIN_HEARING_DISTANCE = 2.0;  // Volume at maximum (1.0) under this threshold

interface VoiceUser {
  socketId: string;
  charId: string;
  nickname: string;
  x: number;
  y: number;
  muted: boolean;
  blockedUsers: Set<string>; // characterIds blocked by this user
  isModerationMuted: boolean; // Automoderation punishment state
}

@Injectable()
@WebSocketGateway({
  cors: { origin: '*' },
  namespace: 'voice-proximity',
})
export class VoiceGateway implements OnGatewayConnection, OnGatewayDisconnect {
  private readonly logger = new Logger(VoiceGateway.name);
  
  @WebSocketServer()
  server: Server;

  // Active players registry mapping SocketId -> Proximity Voice session
  private activeUsers = new Map<string, VoiceUser>();

  handleConnection(client: Socket) {
    this.logger.log(`📱 Conexão de áudio estabelecida no Gateway: ${client.id}`);
  }

  handleDisconnect(client: Socket) {
    const user = this.activeUsers.get(client.id);
    if (user) {
      this.logger.log(`📱 ${user.nickname} desconectou da sessão de voz.`);
      this.activeUsers.delete(client.id);
      
      // Notify other clients to teardown WebRTC peer connections
      client.broadcast.emit('voice_peer_disconnected', { socketId: client.id });
    }
  }

  /**
   * 1. Register User and initialize Voice Room session
   */
  @SubscribeMessage('voice_register')
  handleRegister(
    @ConnectedSocket() client: Socket,
    @MessageBody() payload: { charId: string; nickname: string; x: number; y: number }
  ) {
    const newUser: VoiceUser = {
      socketId: client.id,
      charId: payload.charId,
      nickname: payload.nickname,
      x: payload.x || 0,
      y: payload.y || 0,
      muted: false,
      blockedUsers: new Set<string>(),
      isModerationMuted: false,
    };

    this.activeUsers.set(client.id, newUser);
    this.logger.log(`🎙️ Registrado: ${payload.nickname} (${payload.charId}) na posição (${newUser.x}, ${newUser.y})`);

    // Sends the TURN/STUN servers to RN client right after registration
    client.emit('voice_config', { iceServers: ICE_SERVERS_CONFIG });
    
    // Check who is close and trigger RTC negotiated handshakes
    this.updateSpatialConnections(client, newUser);
  }

  /**
   * 2. Spatial Update handler: updates coordinate matrices and computes real-time volumes
   */
  @SubscribeMessage('voice_position_sync')
  handlePositionSync(
    @ConnectedSocket() client: Socket,
    @MessageBody() payload: { x: number; y: number }
  ) {
    const user = this.activeUsers.get(client.id);
    if (!user) return;

    user.x = payload.x;
    user.y = payload.y;

    // Recalculate peer audio scaling and update the client
    this.recalculateProximityMatrix(client, user);
  }

  /**
   * Recalculates volume factors based on 2.5D Isometric distance.
   * Send the volume multiplier matrices directly to React Native client runtime.
   */
  private recalculateProximityMatrix(client: Socket, user: VoiceUser) {
    const volumeControls: Array<{ targetSocketId: string; volume: number; withinRange: boolean }> = [];

    this.activeUsers.forEach((otherUser, otherSocketId) => {
      if (otherSocketId === client.id) return;

      // Checking blocking relationships in both directions
      const hasBlocked = user.blockedUsers.has(otherUser.charId);
      const isBlockedByOther = otherUser.blockedUsers.has(user.charId);

      if (hasBlocked || isBlockedByOther || user.muted || otherUser.muted || user.isModerationMuted || otherUser.isModerationMuted) {
        // Zero-volume if muted/blocked
        volumeControls.push({
          targetSocketId: otherSocketId,
          volume: 0.0,
          withinRange: false
        });
        return;
      }

      // Calculate Euclidean distance in isometric space grid
      const dx = user.x - otherUser.x;
      const dy = user.y - otherUser.y;
      const distance = Math.sqrt(dx * dx + dy * dy);

      let volume = 0.0;
      let withinRange = false;

      if (distance <= MAX_HEARING_DISTANCE) {
        withinRange = true;
        if (distance <= MIN_HEARING_DISTANCE) {
          volume = 1.0; // Max volume
        } else {
          // Logarithmic distance model for organic sound attenuation
          const alpha = (distance - MIN_HEARING_DISTANCE) / (MAX_HEARING_DISTANCE - MIN_HEARING_DISTANCE);
          volume = Math.max(0.0, Math.min(1.0, 1.0 - Math.log10(1.0 + 9.0 * alpha)));
        }
      }

      volumeControls.push({
        targetSocketId: otherSocketId,
        volume: parseFloat(volume.toFixed(3)),
        withinRange
      });
    });

    // Send calculations to the owner's app to calibrate local low-level WebRTC/AudioTrack gains
    client.emit('proximity_volumes_gain', { controls: volumeControls });
  }

  private updateSpatialConnections(client: Socket, user: VoiceUser) {
    this.activeUsers.forEach((otherUser, otherSocketId) => {
      if (otherSocketId === client.id) return;

      const dx = user.x - otherUser.x;
      const dy = user.y - otherUser.y;
      const distance = Math.sqrt(dx * dx + dy * dy);

      if (distance <= MAX_HEARING_DISTANCE) {
        // Initiate SDP handshake requests! Client-side WebRTC begins.
        client.emit('initiate_webrtc_offer', {
          targetSocketId: otherSocketId,
          targetNickname: otherUser.nickname,
        });
      }
    });
  }

  /**
   * 3. WebRTC Signaling pass-through methods
   */
  @SubscribeMessage('webrtc_offer')
  handleOffer(@ConnectedSocket() client: Socket, @MessageBody() payload: { targetSocketId: string; sdp: any }) {
    this.server.to(payload.targetSocketId).emit('webrtc_offer_received', {
      senderSocketId: client.id,
      sdp: payload.sdp
    });
  }

  @SubscribeMessage('webrtc_answer')
  handleAnswer(@ConnectedSocket() client: Socket, @MessageBody() payload: { targetSocketId: string; sdp: any }) {
    this.server.to(payload.targetSocketId).emit('webrtc_answer_received', {
      senderSocketId: client.id,
      sdp: payload.sdp
    });
  }

  @SubscribeMessage('webrtc_ice_candidate')
  handleIceCandidate(@ConnectedSocket() client: Socket, @MessageBody() payload: { targetSocketId: string; candidate: any }) {
    this.server.to(payload.targetSocketId).emit('webrtc_ice_candidate_received', {
      senderSocketId: client.id,
      candidate: payload.candidate
    });
  }

  /**
   * 4. Social Controls & Moderation (Mute, Block, Report & Automatic AI Moderation)
   */
  @SubscribeMessage('social_mute_self')
  handleMuteSelf(@ConnectedSocket() client: Socket, @MessageBody() payload: { muted: boolean }) {
    const user = this.activeUsers.get(client.id);
    if (user) {
      user.muted = payload.muted;
      this.logger.log(`🎙️ Mute Alterado: ${user.nickname} está agora ${user.muted ? 'Mutado' : 'Desmutado'}`);
      this.recalculateProximityMatrix(client, user);
    }
  }

  @SubscribeMessage('social_block_user')
  handleBlockUser(@ConnectedSocket() client: Socket, @MessageBody() payload: { targetCharId: string }) {
    const user = this.activeUsers.get(client.id);
    if (user) {
      user.blockedUsers.add(payload.targetCharId);
      this.logger.log(`🚫 ${user.nickname} bloqueou o id de personagem: ${payload.targetCharId}`);
      this.recalculateProximityMatrix(client, user);
    }
  }

  @SubscribeMessage('social_report_user')
  handleReportUser(
    @ConnectedSocket() client: Socket,
    @MessageBody() payload: { targetCharId: string; targetSocketId: string; reason: string }
  ) {
    const user = this.activeUsers.get(client.id);
    const target = this.activeUsers.get(payload.targetSocketId);
    
    if (user && target) {
      this.logger.log(`⚠️ DENÚNCIA: ${user.nickname} denunciou ${target.nickname} por: "${payload.reason}"`);
      
      // Auto moderation trigger simulation (e.g. if reports exceed 3, or speech-to-text safety checks detect toxicity)
      this.simulateSpeechModeration(target);
    }
  }

  /**
   * Simulates automatic AI moderation analyzing audio peaks and background speech
   */
  private simulateSpeechModeration(user: VoiceUser) {
    // Punish highly offensive/toxic reports with direct moderation state mute for 5 mins
    user.isModerationMuted = true;
    this.logger.warn(`🚨 AUTO_MODERAÇÃO ATIVADA: Silenciando temporariamente ${user.nickname} devido a comportamento prejudicial.`);
    
    const targetSocket = this.server.sockets.sockets.get(user.socketId);
    if (targetSocket) {
      targetSocket.emit('moderation_notice', {
        reason: 'Violência verbal ou áudio impróprio detectado pelo sistema de moderação inteligente do JiuVerse.',
        durationSeconds: 300
      });
      this.recalculateProximityMatrix(targetSocket, user);
    }
  }
}


/**
 * REACT NATIVE WEBRTC PROXIMITY CLIENT HOOK (BLUEPRINT)
 * 
 * Implementação no aplicativo Android/iOS usando react-native-webrtc + Web Audio API.
 */
export const REACT_NATIVE_VOICE_CLIENT_BLUEPRINT = `
import React, { useEffect, useRef, useState } from 'react';
import { StyleSheet, Text, View, Switch, TouchableOpacity } from 'react-native';
import { RTCPeerConnection, RTCIceCandidate, RTCSessionDescription, mediaDevices } from 'react-native-webrtc';
import io from 'socket.io-client';

export function ProximityVoiceClient({ charId, nickname }) {
  const [localStream, setLocalStream] = useState(null);
  const [isMuted, setIsMuted] = useState(false);
  const [activePeers, setActivePeers] = useState({});
  const socketRef = useRef(null);
  const peerConnections = useRef({});

  useEffect(() => {
    // 1. Initialize local microphone media stream
    mediaDevices.getUserMedia({ audio: true, video: false })
      .then(stream => {
        setLocalStream(stream);
        setupSocket(stream);
      })
      .catch(err => console.error("Falha ao abrir microfone:", err));

    return () => {
      socketRef.current?.disconnect();
      Object.values(peerConnections.current).forEach(pc => pc.close());
    };
  }, []);

  const setupSocket = (stream) => {
    socketRef.current = io('https://voice.jiuverse.com/voice-proximity');

    socketRef.current.on('connect', () => {
      socketRef.current.emit('voice_register', {
        charId,
        nickname,
        x: 5, // Spawn default coordinate
        y: 5
      });
    });

    // 2. Dynamic high-volume / low-volume mixer adjustments
    socketRef.current.on('proximity_volumes_gain', ({ controls }) => {
      controls.forEach(ctrl => {
        const peer = peerConnections.current[ctrl.targetSocketId];
        if (peer) {
          // Adjust volume on the HTMLAudioElement or native component reference
          // In React Native WebRTC, volume is controlled by applying remote description volume or via an AudioTrack gain node.
          console.log(\`Ajustando ganho para \${ctrl.targetSocketId} para: \${ctrl.volume}\`);
          setPeerVolume(ctrl.targetSocketId, ctrl.volume);
        }
      });
    });

    // 3. Negotiate connection on range entry
    socketRef.current.on('initiate_webrtc_offer', async ({ targetSocketId }) => {
      const pc = createPeerConnection(targetSocketId, stream);
      const offer = await pc.createOffer();
      await pc.setLocalDescription(offer);
      socketRef.current.emit('webrtc_offer', { targetSocketId, sdp: offer });
    });

    socketRef.current.on('webrtc_offer_received', async ({ senderSocketId, sdp }) => {
      const pc = createPeerConnection(senderSocketId, stream);
      await pc.setRemoteDescription(new RTCSessionDescription(sdp));
      const answer = await pc.createAnswer();
      await pc.setLocalDescription(answer);
      socketRef.current.emit('webrtc_answer', { targetSocketId: senderSocketId, sdp: answer });
    });

    socketRef.current.on('webrtc_answer_received', async ({ senderSocketId, sdp }) => {
      const pc = peerConnections.current[senderSocketId];
      if (pc) {
        await pc.setRemoteDescription(new RTCSessionDescription(sdp));
      }
    });

    socketRef.current.on('webrtc_ice_candidate_received', async ({ senderSocketId, candidate }) => {
      const pc = peerConnections.current[senderSocketId];
      if (pc) {
        await pc.addIceCandidate(new RTCIceCandidate(candidate));
      }
    });
  };

  const createPeerConnection = (targetSocketId, stream) => {
    const pc = new RTCPeerConnection({
      iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
    });

    peerConnections.current[targetSocketId] = pc;
    stream.getTracks().forEach(track => pc.addTrack(track, stream));

    pc.onicecandidate = (event) => {
      if (event.candidate) {
        socketRef.current.emit('webrtc_ice_candidate', {
          targetSocketId,
          candidate: event.candidate
        });
      }
    };

    pc.onaddstream = (event) => {
      console.log("Recebeu trilha de voz remota!");
      // Render or add to active peer streams state
    };

    return pc;
  };

  const setPeerVolume = (socketId, volume) => {
    // Interface helper to call native WebRTC modules to attenuate gain
  };

  const toggleMute = () => {
    const nextState = !isMuted;
    setIsMuted(nextState);
    localStream?.getAudioTracks().forEach(track => { track.enabled = !nextState; });
    socketRef.current?.emit('social_mute_self', { muted: nextState });
  };

  return (
    <View style={styles.card}>
      <Text style={styles.title}>🎙️ Voz por Proximidade WebRTC</Text>
      <Switch value={!isMuted} onValueChange={toggleMute} />
      <Text>{isMuted ? "Canal Mutado" : "Microfone Transmitindo OOSS!"}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { padding: 12, backgroundColor: '#1E293B', borderRadius: 8, marginVertical: 10 },
  title: { fontSize: 14, color: '#06B6D4', fontWeight: 'bold' }
});
`;
