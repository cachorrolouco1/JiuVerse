import React, { useState, useRef } from 'react';
import { View, Text, StyleSheet, FlatList, TextInput, TouchableOpacity, KeyboardAvoidingView, Platform } from 'react-native';
import { useGameStore } from '../store/useGameStore';
import { useAuthStore } from '../store/useAuthStore';
import { useSocket } from '../hooks/useSocket';

export default function MessagesScreen() {
  const messages = useGameStore((state) => state.messages);
  const sendMessage = useGameStore((state) => state.sendMessage);
  const user = useAuthStore((state) => state.user);
  const token = useAuthStore((state) => state.token);
  
  const [typedMessage, setTypedMessage] = useState('');
  const flatListRef = useRef<FlatList>(null);

  // Bind to tatame_central chat sockets
  const { emitChat } = useSocket('tatame_central', token);

  const handleSend = () => {
    if (typedMessage.trim() && user) {
      // Local addition and socket emission
      sendMessage(typedMessage.trim(), user.nickname);
      emitChat(typedMessage.trim(), user.nickname);
      setTypedMessage('');
      
      // Auto scroll to latest
      setTimeout(() => {
        flatListRef.current?.scrollToEnd({ animated: true });
      }, 100);
    }
  };

  const renderMessageItem = ({ item }: { item: any }) => {
    const isMe = item.sender === 'user' || item.senderName === user?.nickname;
    return (
      <View style={[styles.messageBubbleContainer, isMe ? styles.alignRight : styles.alignLeft]}>
        <View style={[styles.bubble, isMe ? styles.myBubble : styles.otherBubble]}>
          <View style={styles.bubbleHeader}>
            <Text style={[styles.senderName, isMe ? styles.mySenderName : styles.otherSenderName]}>
              {item.senderName}
            </Text>
            <Text style={styles.timestamp}>{item.timestamp}</Text>
          </View>
          <Text style={styles.content}>{item.content}</Text>
        </View>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <FlatList
        ref={flatListRef}
        data={messages}
        keyExtractor={(item) => item.id}
        renderItem={renderMessageItem}
        contentContainerStyle={styles.listContainer}
        onContentSizeChange={() => flatListRef.current?.scrollToEnd({ animated: true })}
      />

      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        keyboardVerticalOffset={Platform.OS === 'ios' ? 90 : 0}
      >
        <View style={styles.inputBar}>
          <TextInput
            style={styles.input}
            placeholder="Grite oss ou digite sua mensagem no Tatame..."
            placeholderTextColor="#64748B"
            value={typedMessage}
            onChangeText={setTypedMessage}
            multiline={false}
          />
          <TouchableOpacity style={styles.sendBigButton} onPress={handleSend}>
            <Text style={styles.sendText}>ENVIAR</Text>
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#090D16',
    justifyContent: 'space-between',
  },
  listContainer: {
    padding: 14,
    paddingBottom: 24,
  },
  messageBubbleContainer: {
    flexDirection: 'row',
    marginBottom: 10,
    width: '100%',
  },
  alignLeft: {
    justifyContent: 'flex-start',
  },
  alignRight: {
    justifyContent: 'flex-end',
  },
  bubble: {
    maxWidth: '85%',
    borderRadius: 8,
    borderWidth: 1,
    padding: 10,
  },
  myBubble: {
    backgroundColor: '#115E59',
    borderColor: '#14B8A6',
  },
  otherBubble: {
    backgroundColor: '#1E293B',
    borderColor: '#334155',
  },
  bubbleHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  senderName: {
    fontSize: 10,
    fontWeight: 'black',
  },
  mySenderName: {
    color: '#2DD4BF',
  },
  otherSenderName: {
    color: '#06B6D4',
  },
  timestamp: {
    color: '#94A3B8',
    fontSize: 8,
    marginLeft: 10,
  },
  content: {
    color: '#F8FAFC',
    fontSize: 12,
    lineHeight: 16,
  },
  inputBar: {
    flexDirection: 'row',
    backgroundColor: '#1F2937',
    borderTopColor: '#374151',
    borderTopWidth: 1,
    paddingHorizontal: 12,
    paddingVertical: 10,
    alignItems: 'center',
  },
  input: {
    flex: 1,
    backgroundColor: '#0B0F19',
    borderColor: '#4B5563',
    borderWidth: 1,
    borderRadius: 8,
    color: '#F8FAFC',
    paddingHorizontal: 12,
    paddingVertical: 8,
    fontSize: 12,
    marginRight: 8,
  },
  sendBigButton: {
    backgroundColor: '#14B8A6',
    borderRadius: 8,
    paddingHorizontal: 14,
    paddingVertical: 10,
    justifyContent: 'center',
    alignItems: 'center',
  },
  sendText: {
    color: '#030712',
    fontWeight: 'black',
    fontSize: 11,
  },
});
