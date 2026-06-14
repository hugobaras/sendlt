import { FormEvent, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch, ApiClientError } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { useSessionStomp } from '../hooks/useSessionStomp';
import type {
  AttemptResponse,
  AttemptType,
  BoulderResponse,
  SessionResponse,
} from '../types/api';

export function SessionPage() {
  const { sessionId } = useParams<{ sessionId: string }>();
  const { token } = useAuth();
  const navigate = useNavigate();
  const { connected, events } = useSessionStomp(token, sessionId);

  const [session, setSession] = useState<SessionResponse | null>(null);
  const [boulders, setBoulders] = useState<BoulderResponse[]>([]);
  const [boulderId, setBoulderId] = useState('');
  const [attemptType, setAttemptType] = useState<AttemptType>('FLASH');
  const [triesCount, setTriesCount] = useState(1);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!token || !sessionId) return;
    apiFetch<SessionResponse>(`/api/sessions/${sessionId}`, { token })
      .then(async (s) => {
        setSession(s);
        const sectors = await apiFetch<{ id: string }[]>(`/api/sectors?gymId=${s.gymId}`, {
          token,
        });
        const lists = await Promise.all(
          sectors.map((sector) =>
            apiFetch<BoulderResponse[]>(`/api/boulders?sectorId=${sector.id}`, { token }),
          ),
        );
        const all = lists.flat();
        setBoulders(all);
        if (all.length > 0) setBoulderId(all[0].id);
      })
      .catch((err) =>
        setError(err instanceof ApiClientError ? err.message : 'Session introuvable'),
      );
  }, [token, sessionId]);

  async function submitAttempt(e: FormEvent) {
    e.preventDefault();
    if (!token || !sessionId || !boulderId) return;
    setError(null);
    setSuccess(null);
    try {
      await apiFetch<AttemptResponse>(`/api/sessions/${sessionId}/attempts`, {
        method: 'POST',
        token,
        body: JSON.stringify({ boulderId, type: attemptType, triesCount }),
      });
      setSuccess('Enregistré');
    } catch (err) {
      setError(err instanceof ApiClientError ? err.message : 'Erreur lors de la tentative');
    }
  }

  async function closeSession() {
    if (!token || !sessionId) return;
    await apiFetch<SessionResponse>(`/api/sessions/${sessionId}/close`, {
      method: 'POST',
      token,
    });
    navigate('/');
  }

  if (!session) {
    return <p className="muted">Chargement de la session…</p>;
  }

  return (
    <div className="session-page">
      <section className="card session-header">
        <div>
          <h1>Session live</h1>
          <p className="muted">
            Code salle : <strong className="mono">{session.roomCode}</strong> ·{' '}
            <span className={`status-pill status-${session.status.toLowerCase()}`}>
              {session.status}
            </span>
          </p>
        </div>
        <div className="session-header-actions">
          <span className={`live-dot ${connected ? 'on' : ''}`}>
            {connected ? 'STOMP connecté' : 'STOMP déconnecté'}
          </span>
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/')}>
            Retour
          </button>
          {session.status === 'ACTIVE' && (
            <button type="button" className="btn btn-secondary" onClick={closeSession}>
              Clôturer
            </button>
          )}
        </div>
      </section>

      <div className="grid-2">
        <section className="card stack">
          <h2>Nouvelle tentative</h2>
          <form onSubmit={submitAttempt} className="stack">
            <label>
              Bloc
              <select
                value={boulderId}
                onChange={(e) => setBoulderId(e.target.value)}
                required
                disabled={session.status !== 'ACTIVE'}
              >
                {boulders.length === 0 && <option value="">Aucun bloc dans cette salle</option>}
                {boulders.map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.color} · {b.gradeFont ?? b.gradeVScale ?? '?'}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Type
              <select
                value={attemptType}
                onChange={(e) => {
                  const t = e.target.value as AttemptType;
                  setAttemptType(t);
                  if (t === 'FLASH') setTriesCount(1);
                }}
                disabled={session.status !== 'ACTIVE'}
              >
                <option value="FLASH">FLASH</option>
                <option value="TOP_AFTER_TRIES">TOP après essais</option>
                <option value="FAIL">Échec</option>
              </select>
            </label>
            <label>
              Nombre d&apos;essais
              <input
                type="number"
                min={1}
                value={triesCount}
                onChange={(e) => setTriesCount(Number(e.target.value))}
                disabled={session.status !== 'ACTIVE' || attemptType === 'FLASH'}
              />
            </label>
            {error && <p className="error-banner">{error}</p>}
            {success && <p className="success-banner">{success}</p>}
            <button
              type="submit"
              className="btn btn-primary"
              disabled={session.status !== 'ACTIVE' || !boulderId}
            >
              Enregistrer
            </button>
          </form>
        </section>

        <section className="card">
          <h2>Activité</h2>
          <div className="event-feed">
            {events.length === 0 && (
              <p className="muted">En attente d&apos;activité…</p>
            )}
            {events.map((evt, idx) => (
              <article key={`${evt.timestamp}-${idx}`} className="event-item">
                <span className="event-type">{evt.eventType}</span>
                {evt.attempt && (
                  <p>
                    <strong>{evt.attempt.userDisplayName}</strong> — {evt.attempt.type} sur{' '}
                    {evt.attempt.gradeFont ?? evt.attempt.boulderColor} ({evt.attempt.triesCount}{' '}
                    essai{evt.attempt.triesCount > 1 ? 's' : ''})
                  </p>
                )}
                {evt.participant && (
                  <p>
                    <strong>{evt.participant.userDisplayName}</strong> a rejoint la session
                  </p>
                )}
                <time className="muted small">{new Date(evt.timestamp).toLocaleTimeString()}</time>
              </article>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
