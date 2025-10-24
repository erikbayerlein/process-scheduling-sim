'use client';

import { useEffect, useRef, useState, useCallback } from 'react';
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs';
import type {
  StatusUpdateEvent,
  ProcessCompleteEvent,
  SimulationCompletedEvent,
  SimulationConfigMessage,
} from '@/lib/types';

interface UseStompWebSocketOptions {
  url: string;
  onStatusUpdate?: (event: StatusUpdateEvent) => void;
  onProcessComplete?: (event: ProcessCompleteEvent) => void;
  onSimulationComplete?: (event: SimulationCompletedEvent) => void;
  onError?: (error: string) => void;
  onOpen?: () => void;
  onClose?: () => void;
}

export function useStompWebSocket({
  url,
  onStatusUpdate,
  onProcessComplete,
  onSimulationComplete,
  onError,
  onOpen,
  onClose,
}: UseStompWebSocketOptions) {
  const [isConnected, setIsConnected] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);
  const clientRef = useRef<Client | null>(null);
  const subscriptionsRef = useRef<StompSubscription[]>([]);

  const connect = useCallback(() => {
    if (clientRef.current?.connected || isConnecting) {
      return;
    }

    setIsConnecting(true);

    const client = new Client({
      brokerURL: url,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      setIsConnected(true);
      setIsConnecting(false);
      onOpen?.();

      // Inscrever nos tópicos
      const subscriptions: StompSubscription[] = [];

      // 1. Atualizações de status da simulação
      const statusSub = client.subscribe('/process-scheduler/simulation/update', (message: IMessage) => {
        try {
          const event: StatusUpdateEvent = JSON.parse(message.body);
          onStatusUpdate?.(event);
        } catch (error) {}
      });
      subscriptions.push(statusSub);

      // 2. Processos concluídos
      const completeSub = client.subscribe('/process-scheduler/process/completed', (message: IMessage) => {
        try {
          const event: ProcessCompleteEvent = JSON.parse(message.body);
          onProcessComplete?.(event);
        } catch (error) {}
      });
      subscriptions.push(completeSub);

      // 3. Simulação concluída
      const simCompleteSub = client.subscribe('/process-scheduler/simulation/completed', (message: IMessage) => {
        try {
          const event: SimulationCompletedEvent = JSON.parse(message.body);
          onSimulationComplete?.(event);
        } catch (error) {}
      });
      subscriptions.push(simCompleteSub);

      // 4. Erros
      const errorSub = client.subscribe('/process-scheduler/errors', (message: IMessage) => {
        onError?.(message.body);
      });
      subscriptions.push(errorSub);

      subscriptionsRef.current = subscriptions;
    };

    client.onStompError = frame => {
      setIsConnected(false);
      setIsConnecting(false);
      onError?.(frame.headers['message'] || 'Erro desconhecido');
    };

    client.onWebSocketClose = () => {
      setIsConnected(false);
      setIsConnecting(false);
      onClose?.();
    };

    client.onWebSocketError = event => {
      setIsConnecting(false);
      onError?.('Erro de conexão WebSocket');
    };

    client.activate();
    clientRef.current = client;
  }, [url, onStatusUpdate, onProcessComplete, onSimulationComplete, onError, onOpen, onClose, isConnecting]);

  const disconnect = useCallback(() => {
    // Cancelar inscrições
    subscriptionsRef.current.forEach(sub => sub.unsubscribe());
    subscriptionsRef.current = [];

    // Desativar cliente
    if (clientRef.current) {
      clientRef.current.deactivate();
      clientRef.current = null;
    }

    setIsConnected(false);
    setIsConnecting(false);
  }, []);

  const startSimulation = useCallback((config: SimulationConfigMessage) => {
    if (!clientRef.current?.connected) {
      return false;
    }

    try {
      clientRef.current.publish({
        destination: '/app/start',
        body: JSON.stringify(config),
        headers: {
          'content-type': 'application/json',
        },
      });
      return true;
    } catch (error) {
      return false;
    }
  }, []);

  useEffect(() => {
    return () => {
      disconnect();
    };
  }, [disconnect]);

  return {
    isConnected,
    isConnecting,
    connect,
    disconnect,
    startSimulation,
  };
}
