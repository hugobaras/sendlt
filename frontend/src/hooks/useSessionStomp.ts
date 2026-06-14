import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { SessionActivityMessage } from '../types/api';

export function useSessionStomp(token: string | null, sessionId: string | undefined) {
  const [connected, setConnected] = useState(false);
  const [events, setEvents] = useState<SessionActivityMessage[]>([]);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!token || !sessionId) {
      setConnected(false);
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      onConnect: () => {
        setConnected(true);
        client.subscribe(`/topic/sessions/${sessionId}/activity`, (message) => {
          const payload = JSON.parse(message.body) as SessionActivityMessage;
          setEvents((prev) => [payload, ...prev].slice(0, 50));
        });
      },
      onDisconnect: () => setConnected(false),
      onStompError: () => setConnected(false),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
      setConnected(false);
    };
  }, [token, sessionId]);

  return { connected, events };
}
