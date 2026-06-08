import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getApiBaseUrl } from '../config';

export function useRideSocket(topics, onMessage) {
  const handlerRef = useRef(onMessage);
  handlerRef.current = onMessage;

  const topicsKey = topics.filter(Boolean).join('|');

  useEffect(() => {
    if (!topicsKey) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${getApiBaseUrl()}/ws`),
      reconnectDelay: 3000,
      onConnect: () => {
        topics.filter(Boolean).forEach((topic) => {
          client.subscribe(topic, (message) => {
            try {
              handlerRef.current(JSON.parse(message.body));
            } catch {
              /* ignore malformed payloads */
            }
          });
        });
      },
    });

    client.activate();
    return () => client.deactivate();
  }, [topicsKey]);
}

export function useCountdown(deadlineMs) {
  const [secondsLeft, setSecondsLeft] = useState(() =>
    deadlineMs ? Math.max(0, Math.ceil((deadlineMs - Date.now()) / 1000)) : 0
  );

  useEffect(() => {
    if (!deadlineMs) {
      setSecondsLeft(0);
      return;
    }

    const tick = () => {
      setSecondsLeft(Math.max(0, Math.ceil((deadlineMs - Date.now()) / 1000)));
    };

    tick();
    const id = setInterval(tick, 100);
    return () => clearInterval(id);
  }, [deadlineMs]);

  return secondsLeft;
}

export function buildDeadlineFromDuty(duty) {
  if (duty.acceptDeadlineEpochMs) return duty.acceptDeadlineEpochMs;
  if (duty.createdAt) {
    const created = new Date(duty.createdAt).getTime();
    return created + 30000;
  }
  return Date.now() + 30000;
}
