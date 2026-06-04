import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image } from 'react-native';
import { useAuthStore } from '../store/useAuthStore';

export default function ProfileScreen() {
  const { user, logout } = useAuthStore();

  if (!user) return null;

  return (
    <View style={styles.container}>
      <View style={styles.avatarCard}>
        <View style={styles.avatarDecoration}>
          <Text style={styles.avatarEmoji}>🥋</Text>
        </View>
        <Text style={styles.nickname}>{user.nickname}</Text>
        <View style={styles.beltBadge}>
          <Text style={styles.beltBadgeText}>FAIXA AZUL • GRAU 2</Text>
        </View>
        <Text style={styles.email}>{user.email}</Text>
      </View>

      <View style={styles.statsCard}>
        <Text style={styles.statsTitle}>STATUS DO LUTADOR</Text>
        
        <View style={styles.statRow}>
          <Text style={styles.statLabel}>Exp. (XP)</Text>
          <Text style={styles.statValue}>{user.xp} / 1000</Text>
        </View>
        <View style={styles.progressBarBg}>
          <View style={[styles.progressBarFilled, { width: '25%' }]} />
        </View>

        <View style={styles.statRow}>
          <Text style={styles.statLabel}>Carteira (JiuCoins)</Text>
          <Text style={styles.coinsValue}>$ {user.coins} JC</Text>
        </View>

        <View style={styles.statRow}>
          <Text style={styles.statLabel}>Pontos de Treino (TP)</Text>
          <Text style={styles.statValue}>18 TP</Text>
        </View>

        <View style={styles.statRow}>
          <Text style={styles.statLabel}>Vitórias em Combate</Text>
          <Text style={styles.winText}>42 WT</Text>
        </View>
      </View>

      <TouchableOpacity style={styles.logoutButton} onPress={logout}>
        <Text style={styles.logoutButtonText}>SAIR DO APLICATIVO (LOGOUT)</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#090D16',
    padding: 14,
  },
  avatarCard: {
    backgroundColor: '#0F172A',
    borderColor: '#1E293B',
    borderWidth: 1,
    borderRadius: 12,
    alignItems: 'center',
    padding: 20,
    marginBottom: 16,
  },
  avatarDecoration: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: '#1E293B',
    borderColor: '#06B6D4',
    borderWidth: 1.5,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
  },
  avatarEmoji: {
    fontSize: 28,
  },
  nickname: {
    color: '#F8FAFC',
    fontSize: 20,
    fontWeight: 'bold',
  },
  beltBadge: {
    backgroundColor: '#1E293B',
    borderColor: '#3B82F6',
    borderWidth: 1,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 14,
    marginTop: 6,
  },
  beltBadgeText: {
    color: '#F8FAFC',
    fontSize: 10,
    fontWeight: 'bold',
  },
  email: {
    color: '#94A3B8',
    fontSize: 11,
    marginTop: 8,
  },
  statsCard: {
    backgroundColor: '#0F172A',
    borderColor: '#1E293B',
    borderWidth: 1,
    borderRadius: 12,
    padding: 16,
    marginBottom: 20,
  },
  statsTitle: {
    fontSize: 10,
    fontWeight: 'bold',
    color: '#06B6D4',
    letterSpacing: 1,
    marginBottom: 12,
  },
  statRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginVertical: 4,
  },
  statLabel: {
    color: '#94A3B8',
    fontSize: 12,
  },
  statValue: {
    color: '#F8FAFC',
    fontSize: 12,
    fontWeight: 'bold',
  },
  progressBarBg: {
    height: 6,
    backgroundColor: '#1E293B',
    borderRadius: 3,
    marginTop: 4,
    marginBottom: 12,
  },
  progressBarFilled: {
    height: 6,
    backgroundColor: '#06B6D4',
    borderRadius: 3,
  },
  coinsValue: {
    color: '#EAB308',
    fontSize: 12,
    fontWeight: 'bold',
  },
  winText: {
    color: '#10B981',
    fontSize: 12,
    fontWeight: 'bold',
  },
  logoutButton: {
    borderColor: '#EF4444',
    borderWidth: 1,
    backgroundColor: '#450A0A',
    padding: 14,
    borderRadius: 8,
    alignItems: 'center',
  },
  logoutButtonText: {
    color: '#F8FAFC',
    fontWeight: 'bold',
    fontSize: 12,
  },
});
