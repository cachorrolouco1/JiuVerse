import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';
import { useAuthStore } from '../store/useAuthStore';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../navigation/AppNavigator';

type NavigationProp = NativeStackNavigationProp<RootStackParamList, 'Login'>;

export default function LoginScreen() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login, isLoading, error } = useAuthStore();
  const navigation = useNavigation<NavigationProp>();

  const handleLogin = async () => {
    if (email && password) {
      await login(email, password);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.brandingBox}>
        <View style={styles.accentIndicator} />
        <Text style={styles.title}>JIUVERSE</Text>
        <Text style={styles.subtitle}>MMORPG SOCIAL DE ARTES MARCIAIS</Text>
      </View>

      <View style={styles.formCard}>
        {error && <Text style={styles.errorText}>{error}</Text>}

        <Text style={styles.label}>E-MAIL DO LUTADOR</Text>
        <TextInput
          style={styles.input}
          placeholder="exemplo@jiuverse.com"
          placeholderTextColor="#64748B"
          value={email}
          onChangeText={setEmail}
          autoCapitalize="none"
          keyboardType="email-address"
        />

        <Text style={styles.label}>SENHA DO DOJO</Text>
        <TextInput
          style={styles.input}
          placeholder="••••••••"
          placeholderTextColor="#64748B"
          secureTextEntry
          value={password}
          onChangeText={setPassword}
          autoCapitalize="none"
        />

        <TouchableOpacity 
          style={styles.button} 
          onPress={handleLogin}
          disabled={isLoading}
        >
          {isLoading ? (
            <ActivityIndicator color="#030712" />
          ) : (
            <Text style={styles.buttonText}>ESTABELECER CONEXÃO</Text>
          )}
        </TouchableOpacity>

        <TouchableOpacity 
          style={styles.secondaryLink}
          onPress={() => navigation.navigate('Register')}
        >
          <Text style={styles.linkText}>Não tem conta? Inscreva-se na Academia</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#090D16',
    justifyContent: 'center',
    padding: 20,
  },
  brandingBox: {
    alignItems: 'center',
    marginBottom: 40,
  },
  accentIndicator: {
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: '#06B6D4',
    marginBottom: 10,
  },
  title: {
    fontSize: 32,
    fontWeight: '900',
    color: '#F8FAFC',
    letterSpacing: 2,
  },
  subtitle: {
    fontSize: 10,
    fontWeight: 'bold',
    color: '#06B6D4',
    marginTop: 4,
    fontFamily: 'System',
  },
  formCard: {
    backgroundColor: '#0F172A',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#1E293B',
    padding: 20,
  },
  errorText: {
    color: '#EF4444',
    fontSize: 12,
    fontWeight: 'bold',
    marginBottom: 12,
    textAlign: 'center',
  },
  label: {
    fontSize: 10,
    fontWeight: 'bold',
    color: '#06B6D4',
    marginBottom: 6,
    letterSpacing: 1,
  },
  input: {
    backgroundColor: '#090D16',
    borderWidth: 1,
    borderColor: '#334155',
    borderRadius: 8,
    padding: 12,
    color: '#F8FAFC',
    fontSize: 14,
    marginBottom: 16,
  },
  button: {
    backgroundColor: '#14B8A6',
    padding: 14,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 8,
  },
  buttonText: {
    color: '#030712',
    fontWeight: 'bold',
    fontSize: 14,
    letterSpacing: 0.5,
  },
  secondaryLink: {
    marginTop: 16,
    alignItems: 'center',
  },
  linkText: {
    color: '#94A3B8',
    fontSize: 12,
    textDecorationLine: 'underline',
  },
});
