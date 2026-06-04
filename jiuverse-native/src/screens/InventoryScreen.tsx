import React from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ScrollView } from 'react-native';
import { useGameStore, InventoryItem } from '../store/useGameStore';

export default function InventoryScreen() {
  const { inventory, equipItem } = useGameStore();

  const getRarityColor = (rarity: string) => {
    switch (rarity) {
      case 'LENDARIO': return '#EAB308';
      case 'EPICO': return '#A855F7';
      case 'RARO': return '#3B82F6';
      default: return '#94A3B8';
    }
  };

  const renderItem = ({ item }: { item: InventoryItem }) => {
    return (
      <View style={styles.itemCard}>
        <View style={styles.itemHeader}>
          <Text style={[styles.rarityLabel, { color: getRarityColor(item.rarity) }]}>{item.rarity}</Text>
          <Text style={styles.itemType}>{item.type}</Text>
        </View>
        <Text style={styles.itemName}>{item.name}</Text>
        <Text style={styles.itemPrice}>Preço: $ {item.price} JC</Text>
        
        <TouchableOpacity 
          style={[styles.actionButton, item.equipped ? styles.equippedButton : styles.equipButton]}
          onPress={() => equipItem(item.id)}
        >
          <Text style={styles.actionButtonText}>
            {item.equipped ? 'DESEQUIPAR✖' : 'EQUIPAR🥋'}
          </Text>
        </TouchableOpacity>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.introHeader}>
        <Text style={styles.introTitle}>MOCHILA DO LUTADOR</Text>
        <Text style={styles.introDesc}>Equipe roupas raras de kimono ou decore sua própria academia com tatames decoráveis de forma síncrona.</Text>
      </View>

      <FlatList
        data={inventory}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={styles.listContainer}
        numColumns={2}
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
  introHeader: {
    backgroundColor: '#0F172A',
    borderColor: '#1E293B',
    borderWidth: 1,
    borderRadius: 8,
    padding: 12,
    marginBottom: 16,
  },
  introTitle: {
    color: '#06B6D4',
    fontSize: 12,
    fontWeight: 'bold',
  },
  introDesc: {
    color: '#94A3B8',
    fontSize: 10,
    marginTop: 4,
    lineHeight: 14,
  },
  listContainer: {
    paddingBottom: 20,
  },
  itemCard: {
    flex: 0.5,
    backgroundColor: '#0F172A',
    borderColor: '#1E293B',
    borderWidth: 1,
    borderRadius: 8,
    padding: 12,
    margin: 4,
    justifyContent: 'space-between',
  },
  itemHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  rarityLabel: {
    fontSize: 8,
    fontWeight: 'black',
  },
  itemType: {
    color: '#94A3B8',
    fontSize: 8,
    fontWeight: 'bold',
  },
  itemName: {
    color: '#F8FAFC',
    fontSize: 12,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  itemPrice: {
    color: '#EAB308',
    fontSize: 9,
    fontFamily: 'System',
    marginBottom: 10,
  },
  actionButton: {
    borderRadius: 4,
    paddingVertical: 8,
    alignItems: 'center',
  },
  equipButton: {
    backgroundColor: '#1E293B',
    borderColor: '#334155',
    borderWidth: 1,
  },
  equippedButton: {
    backgroundColor: '#134E5E',
    borderColor: '#06B6D4',
    borderWidth: 1,
  },
  actionButtonText: {
    color: '#F8FAFC',
    fontSize: 10,
    fontWeight: 'black',
  },
});
