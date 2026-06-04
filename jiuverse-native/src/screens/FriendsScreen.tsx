import React, { useState } from 'react';
import { View, Text, StyleSheet, FlatList, TextInput, TouchableOpacity } from 'react-native';
import { useGameStore, Friend } from '../store/useGameStore';

export default function FriendsScreen() {
  const { friends, addFriend, removeFriend } = useGameStore();
  const [newFriendName, setNewFriendName] = useState('');

  const submitAddFriend = () => {
    if (newFriendName.trim()) {
      addFriend(newFriendName.trim());
      setNewFriendName('');
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ONLINE': return '#10B981';
      case 'TREINANDO': return '#06B6D4';
      default: return '#64748B';
    }
  };

  const renderFriend = ({ item }: { item: Friend }) => {
    return (
      <View style={styles.friendRow}>
        <View style={styles.left}>
          <View style={[styles.statusDot, { backgroundColor: getStatusColor(item.status) }]} />
          <View style={styles.info}>
            <Text style={styles.nickname}>{item.nickname}</Text>
            <Text style={styles.subtext}>Faixa {item.belt} • {item.status}</Text>
          </View>
        </View>

        <TouchableOpacity style={styles.removeBtn} onPress={() => removeFriend(item.id)}>
          <Text style={styles.removeText}>REMOVER</Text>
        </TouchableOpacity>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      {/* Adding friends section */}
      <View style={styles.addSection}>
        <Text style={styles.addSectionTitle}>PROCURAR LUTADOR NO CLUBE</Text>
        <View style={styles.inputRow}>
          <TextInput
            style={styles.input}
            placeholder="Nome do avatar do Jiu-Jitsu..."
            placeholderTextColor="#64748B"
            value={newFriendName}
            onChangeText={setNewFriendName}
          />
          <TouchableOpacity style={styles.addBtn} onPress={submitAddFriend}>
            <Text style={styles.addBtnText}>CONVIDAR</Text>
          </TouchableOpacity>
        </View>
      </View>

      <Text style={styles.listTitle}>SEUS COLEGAS DE TREINO ({friends.length})</Text>

      <FlatList
        data={friends}
        keyExtractor={(item) => item.id}
        renderItem={renderFriend}
        contentContainerStyle={styles.listContainer}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyText}>Nenhum companheiro de tatame adicionado ainda.</Text>
          </View>
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#090D16',
    padding: 14,
  },
  addSection: {
    backgroundColor: '#0F172A',
    borderColor: '#1E293B',
    borderWidth: 1,
    borderRadius: 8,
    padding: 12,
    marginBottom: 20,
  },
  addSectionTitle: {
    fontSize: 9,
    fontWeight: 'bold',
    color: '#06B6D4',
    letterSpacing: 1,
    marginBottom: 8,
  },
  inputRow: {
    flexDirection: 'row',
  },
  input: {
    flex: 1,
    backgroundColor: '#090D16',
    borderWidth: 1,
    borderColor: '#334155',
    borderRadius: 6,
    paddingHorizontal: 10,
    paddingVertical: 8,
    color: '#F8FAFC',
    fontSize: 12,
    marginRight: 8,
  },
  addBtn: {
    backgroundColor: '#14B8A6',
    borderRadius: 6,
    paddingHorizontal: 14,
    justifyContent: 'center',
    alignItems: 'center',
  },
  addBtnText: {
    color: '#030712',
    fontSize: 11,
    fontWeight: 'black',
  },
  listTitle: {
    fontSize: 10,
    fontWeight: 'bold',
    color: '#94A3B8',
    marginBottom: 8,
  },
  listContainer: {
    paddingBottom: 10,
  },
  friendRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#0F172A',
    borderColor: '#1E293B',
    borderWidth: 1,
    borderRadius: 8,
    padding: 12,
    marginBottom: 8,
  },
  left: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 10,
  },
  info: {
    flexDirection: 'column',
  },
  nickname: {
    color: '#F8FAFC',
    fontSize: 13,
    fontWeight: 'bold',
  },
  subtext: {
    color: '#94A3B8',
    fontSize: 10,
    marginTop: 2,
  },
  removeBtn: {
    borderColor: '#EF4444',
    borderWidth: 1,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 4,
  },
  removeText: {
    color: '#EF4444',
    fontSize: 8,
    fontWeight: 'bold',
  },
  emptyContainer: {
    padding: 30,
    alignItems: 'center',
  },
  emptyText: {
    color: '#64748B',
    fontSize: 12,
    textAlign: 'center',
  },
});
