import { create } from 'zustand';

export interface InventoryItem {
  id: string;
  name: string;
  type: 'WEARABLE' | 'FURNITURE' | 'BADGE';
  rarity: 'COMUM' | 'RARO' | 'EPICO' | 'LENDARIO';
  equipped: boolean;
  price: number;
}

export interface RankUser {
  rank: number;
  nickname: string;
  belt: 'Branca' | 'Azul' | 'Roxa' | 'Marrom' | 'Preta';
  xp: number;
  coins: number;
}

export interface Friend {
  id: string;
  nickname: string;
  belt: string;
  status: 'ONLINE' | 'TREINANDO' | 'OFFLINE';
}

export interface Message {
  id: string;
  sender: string;
  senderName: string;
  content: string;
  timestamp: string;
}

interface GameState {
  inventory: InventoryItem[];
  ranking: RankUser[];
  friends: Friend[];
  messages: Message[];
  activeDojoPosition: { x: number; y: number };
  
  // Actions
  equipItem: (id: string) => void;
  addFriend: (nickname: string) => void;
  removeFriend: (id: string) => void;
  sendMessage: (content: string, senderName: string) => void;
  receiveMessage: (msg: Message) => void;
  updatePosition: (x: number, y: number) => void;
}

export const useGameStore = create<GameState>((set) => ({
  inventory: [
    { id: 'item_1', name: 'Kimono Azul Gracie', type: 'WEARABLE', rarity: 'COMUM', equipped: true, price: 150 },
    { id: 'item_2', name: 'Faixa Preta Lendária', type: 'WEARABLE', rarity: 'LENDARIO', equipped: false, price: 5000 },
    { id: 'item_3', name: 'Muralha Tatame Pro', type: 'FURNITURE', rarity: 'RARO', equipped: false, price: 400 },
    { id: 'item_4', name: 'Boneco de Pancadas (Uke)', type: 'FURNITURE', rarity: 'EPICO', equipped: false, price: 1200 },
    { id: 'item_5', name: 'Proteção Bucal Premium', type: 'WEARABLE', rarity: 'COMUM', equipped: false, price: 100 },
  ],
  ranking: [
    { rank: 1, nickname: 'MestreGracie4', belt: 'Preta', xp: 95400, coins: 250000 },
    { rank: 2, nickname: 'ChaveDeBraco', belt: 'Marrom', xp: 52100, coins: 43000 },
    { rank: 3, nickname: 'PassadorJusto', belt: 'Roxa', xp: 32000, coins: 12000 },
    { rank: 4, nickname: 'GuardaAranha', belt: 'Azul', xp: 19500, coins: 8500 },
    { rank: 5, nickname: 'DojoMasterX', belt: 'Preta', xp: 18100, coins: 1400 },
  ],
  friends: [
    { id: 'f_1', nickname: 'GuardaAberta99', belt: 'Azul', status: 'ONLINE' },
    { id: 'f_2', nickname: 'PatinhoDoTatame', belt: 'Branca', status: 'TREINANDO' },
    { id: 'f_3', nickname: 'InicianteDoOss', belt: 'Branca', status: 'OFFLINE' },
  ],
  messages: [
    { id: 'm_1', sender: 'ai_coach', senderName: 'Mestre Cícero', content: 'Bem-vindo ao Tatame Central do JiuVerse, campeão!', timestamp: '15:20' },
    { id: 'm_2', sender: 'system', senderName: 'SISTEMA', content: 'Você entrou no Dojo Principal da Aliança.', timestamp: '15:21' },
  ],
  activeDojoPosition: { x: 3, y: 3 },

  equipItem: (id) => set((state) => ({
    inventory: state.inventory.map((item) => {
      if (item.id === id) {
        return { ...item, equipped: !item.equipped };
      }
      // If we are equipping a WEARABLE belt or kimono, maybe unequip others?
      // For general items, we just toggle.
      return item;
    })
  })),

  addFriend: (nickname) => set((state) => ({
    friends: [
      ...state.friends,
      {
        id: 'f_' + Date.now(),
        nickname,
        belt: 'Branca',
        status: 'ONLINE'
      }
    ]
  })),

  removeFriend: (id) => set((state) => ({
    friends: state.friends.filter((friend) => friend.id !== id)
  })),

  sendMessage: (content, senderName) => set((state) => {
    const formattedTime = new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    const newMsg: Message = {
      id: 'm_' + Date.now(),
      sender: 'user',
      senderName,
      content,
      timestamp: formattedTime
    };
    return {
      messages: [...state.messages, newMsg]
    };
  }),

  receiveMessage: (msg) => set((state) => ({
    messages: [...state.messages, msg]
  })),

  updatePosition: (x, y) => set(() => ({
    activeDojoPosition: { x, y }
  })),
}));
