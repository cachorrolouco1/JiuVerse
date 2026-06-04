import React from 'react';
import { View, Text, StyleSheet, FlatList } from 'react-native';
import { useGameStore, RankUser } from '../store/useGameStore';

export default function RankingScreen() {
  const ranking = useGameStore((state) => state.ranking);

  const getBeltColor = (belt: string) => {
    switch (belt) {
      case 'Preta': return '#F1F5F9';
      case 'Marrom': return '#78350F';
      case 'Roxa': return '#7C3AED';
      case 'Azul': return '#2563EB';
      default: return '#94A3B8';
    }
  };

  const renderRankItem = ({ item }: { item: RankUser }) => {
    const beltColor = getBeltColor(item.belt);
    return (
      <View style={styles.rankRow}>
        <View style={styles.leftSection}>
          <Text style={[styles.posText, item.rank <= 3 ? styles.podiumText : null]}>
            {item.rank === 1 ? '🥇' : item.rank === 2 ? '🥈' : item.rank === 3 ? '🥉' : `${item.rank}`}
          </Text>
          <View style={styles.nameBadgeCol}>
            <Text style={styles.nickname}>{item.nickname}</Text>
            <View style={[styles.beltIndicator, { backgroundColor: beltColor }]}>
              <Text style={[styles.beltText, item.belt === 'Preta' ? styles.blackBeltText : null]}>
                FAIXA {item.belt.toUpperCase()}
              </Text>
            </View>
          </View>
        </View>
        <View style={styles.rightSection}>
          <Text style={styles.xpText}>{item.xp} XP</Text>
          <Text style={styles.coinsText}>$ {item.coins} JC</Text>
        </View>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.headerBox}>
        <Text style={styles.title}>QUADRO DE DUQUES (GLOBAL RANK)</Text>
        <Text style={styles.subtitle}>Os avatares mais ativos e condecorados do JiuVerse.</Text>
      </View>

      <FlatList
        data={ranking}
        keyExtractor={(item) => item.nickname}
        renderItem={renderRankItem}
        contentContainerStyle={styles.listContainer}
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
  headerBox: {
    backgroundColor: '#0F172A',
    borderColor: '#1E293B',
    borderWidth: 1,
    borderRadius: 8,
    padding: 12,
    marginBottom: 16,
  },
  title: {
    color: '#06B6D4',
    fontSize: 12,
    fontWeight: 'bold',
  },
  subtitle: {
    color: '#94A3B8',
    fontSize: 10,
    marginTop: 4,
  },
  listContainer: {
    paddingBottom: 10,
  },
  rankRow: {
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
  leftSection: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  posText: {
    color: '#F8FAFC',
    fontSize: 14,
    fontWeight: 'bold',
    width: 32,
    textAlign: 'center',
  },
  podiumText: {
    fontSize: 18,
  },
  nameBadgeCol: {
    marginLeft: 12,
  },
  nickname: {
    color: '#F8FAFC',
    fontSize: 13,
    fontWeight: 'bold',
  },
  beltIndicator: {
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
    marginTop: 4,
    alignSelf: 'flex-start',
  },
  beltText: {
    color: '#F8FAFC',
    fontSize: 8,
    fontWeight: 'black',
  },
  blackBeltText: {
    color: '#090D16',
  },
  rightSection: {
    alignItems: 'flex-end',
  },
  xpText: {
    color: '#06B6D4',
    fontSize: 11,
    fontWeight: 'bold',
  },
  coinsText: {
    color: '#EAB308',
    fontSize: 9,
    marginTop: 2,
  },
});
