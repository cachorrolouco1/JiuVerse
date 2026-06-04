import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';
import { useAuthStore } from '../store/useAuthStore';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../navigation/AppNavigator';

type NavigationProp = NativeStackNavigationProp<RootStackParamList, 'Register'>;

export default function RegisterScreen() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const { register, isLoading, error } = useAuthStore();
  const navigation = useNavigation<NavigationProp>();

  const handleRegister = async () => {
    if (email && password && nickname) {
      await register(email, password, nickname);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.brandingBox}>
        <View style={styles.accentIndicator} />
        <Text style={styles.title}>JIUVERSE</Text>
        <Text style={styles.subtitle}>REGISTRE SEU AVATAR COMBATENTE</Text>
      </View>

      <View style={styles.formCard}>
        {error && <Text style={styles.errorText}>{error}</Text>}

        <Text style={styles.label}>NICKNAME EXIBIDO (ÚNICO)</Text>
        <TextInput
          style={styles.input}
          placeholder="Ex: RyronGracie"
          placeholderTextColor="#64748B"
          value={nickname}
          onChangeText={setNickname}
          autoCapitalize="words"
        />

        <Text style={styles.label}>E-MAIL DA CONTA</Text>
        <TextInput
          style={styles.input}
          placeholder="lutador@dojo.com"
          placeholderTextColor="#64748B"
          value={email}
          onChangeText={setEmail}
          autoCapitalize="none"
          keyboardType="email-address"
        />

        <Text style={styles.label}>SENHA AUTORITATIVA</Text>
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
          onPress={handleRegister}
          disabled={isLoading}
        >
          {isLoading ? (
            <ActivityIndicator color="#030712" />
          ) : (
            <Text style={styles.buttonText}>GRADUAR E CADASTRAR</Text>
          )}
        </TouchableOpacity>

        <TouchableOpacity 
          style={styles.secondaryLink}
          onPress={() => navigation.navigate('Login')}
        >
          <Text style={styles.linkText}>Já possui cadastro? Clique aqui para Login</Text>
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
    fontSize: 28,
    fontWeight: '900',
    color: '#F8FAFC',
    letterSpacing: 2,
  },
  subtitle: {
    fontSize: 10,
    fontWeight: 'bold',
    color: '#06B6D4',
    marginTop: 4,
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
    backgroundColor: '#06B6D4',
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
