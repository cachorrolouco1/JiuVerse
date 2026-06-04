import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';

// Standard Enterprise Axios client configurations with interceptors
export const api = axios.create({
  baseURL: 'https://api.jiuverse.com/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  async (config) => {
    const token = await AsyncStorage.getItem('@jiuverse_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Clear token to trigger login screen redirect automatically
      await AsyncStorage.removeItem('@jiuverse_token');
    }
    return Promise.reject(error);
  }
);

// --- React Query Fetch Helpers ---
export const fetchInventory = async () => {
  const response = await api.get('/character/inventory');
  return response.data;
};

export const fetchRanking = async () => {
  const response = await api.get('/ranking/global');
  return response.data;
};

export const fetchDojosList = async () => {
  const response = await api.get('/dojos/public');
  return response.data;
};
