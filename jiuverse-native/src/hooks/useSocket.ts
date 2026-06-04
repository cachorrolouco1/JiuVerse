import { useEffect, useRef, useState } from 'react';
import { io, Socket } from 'socket.io-client';
import { useGameStore, Message } from '../store/useGameStore';

export const useSocket = (roomId: string, token: string | null) => {
  const socketRef = useRef<Socket | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const receiveMessage = useGameStore((state) => state.receiveMessage);
  const updatePosition = useGameStore((state) => state.updatePosition);

  useEffect(() => {
    if (!token) return;

    // Connects to the load-balanced NestJS WebSockets pool
    const socket = io('https://api.jiuverse.com/gameserver', {
      auth: { token },
      transports: ['websocket'],
      query: { roomId },
      reconnectionAttempts: 5,
      reconnectionDelay: 1000,
    });

    socketRef.current = socket;

    socket.on('connect', () => {
      setIsConnected(true);
      console.log('Socket.IO conectado ao pool do JiuVerse!');
    });

    socket.on('disconnect', () => {
      setIsConnected(false);
    });

    socket.on('messageBroadcast', (msg: Message) => {
      // Receive chat messages from Redis multi-instance synchronization
      receiveMessage(msg);
    });

    socket.on('playerMoved', (data: { id: string; x: number; y: number }) => {
      // Track positions of multiplayer avatares
      console.log(`Jogador moved: ${data.id} - (${data.x}, ${data.y})`);
    });

    return () => {
      socket.disconnect();
    };
  }, [roomId, token, receiveMessage]);

  const emitMovement = (x: number, y: number) => {
    if (socketRef.current && isConnected) {
      // Send coordinate updates at standard MMORPG refresh rate (10Hz)
      socketRef.current.emit('move', { x, y });
      updatePosition(x, y);
    }
  };

  const emitChat = (content: string, senderName: string) => {
    if (socketRef.current && isConnected) {
      socketRef.current.emit('chatMessage', { content, senderName });
    }
  };

  return {
    isConnected,
    emitMovement,
    emitChat,
  };
};
