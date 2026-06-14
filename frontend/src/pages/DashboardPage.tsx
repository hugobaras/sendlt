import { FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiFetch, ApiClientError } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import type {
  GradePyramidResponse,
  GymResponse,
  SessionParticipantResponse,
  VolumeStatsResponse,
} from '../types/api';

export function DashboardPage() {
  const { token, user } = useAuth();
  const navigate = useNavigate();
  const [volume, setVolume] = useState<VolumeStatsResponse | null>(null);
  const [pyramid, setPyramid] = useState<GradePyramidResponse | null>(null);
  const [gyms, setGyms] = useState<GymResponse[]>([]);
  const [gymId, setGymId] = useState('');
  const [roomCode, setRoomCode] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!token) return;
    Promise.all([
      apiFetch<VolumeStatsResponse>('/api/stats/me/volume', { token }),
      apiFetch<GradePyramidResponse>('/api/stats/me/grade-pyramid', { token }),
      apiFetch<GymResponse[]>('/api/gyms', { token }),
    ])
      .then(([vol, pyr, gymList]) => {
        setVolume(vol);
        setPyramid(pyr);
        setGyms(gymList);
        if (gymList.length > 0) setGymId(gymList[0].id);
      })
      .catch((err) => setError(err instanceof ApiClientError ? err.message : 'Erreur de chargement'));
  }, [token]);

  async function createSession(e: FormEvent) {
    e.preventDefault();
    if (!token || !gymId) return;
    setLoading(true);
    setError(null);
    try {
      const session = await apiFetch<{ id: string }>('/api/sessions', {
        method: 'POST',
        token,
        body: JSON.stringify({ gymId }),
      });
      navigate(`/session/${session.id}`);
    } catch (err) {
      setError(err instanceof ApiClientError ? err.message : 'Impossible de créer la session');
    } finally {
      setLoading(false);
    }
  }

  async function joinSession(e: FormEvent) {
    e.preventDefault();
    if (!token || !roomCode) return;
    setLoading(true);
    setError(null);
    try {
      const participant = await apiFetch<SessionParticipantResponse>('/api/sessions/join', {
        method: 'POST',
        token,
        body: JSON.stringify({ roomCode: roomCode.toUpperCase() }),
      });
      navigate(`/session/${participant.sessionId}`);
    } catch (err) {
      setError(err instanceof ApiClientError ? err.message : 'Code salle invalide');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="dashboard">
      <section className="hero-panel">
        <div>
          <h1>Bonjour, {user?.displayName}</h1>
        </div>
        <div className="hero-actions">
          <a className="btn btn-ghost" href="/swagger-ui.html" target="_blank" rel="noreferrer">
            API Swagger
          </a>
        </div>
      </section>

      {error && <p className="error-banner">{error}</p>}

      <div className="grid-2">
        <section className="card">
          <h2>Logbook</h2>
          <div className="stat-row">
            <div className="stat">
              <span className="stat-value">{volume?.successfulBoulders ?? '—'}</span>
              <span className="stat-label">Blocs réussis</span>
            </div>
          </div>
          <h3>Pyramide Font</h3>
          {pyramid && pyramid.entries.length > 0 ? (
            <ul className="pyramid-list">
              {pyramid.entries.map((entry) => (
                <li key={entry.grade}>
                  <span>{entry.grade}</span>
                  <span className="pyramid-bar" style={{ width: `${entry.count * 24}px` }} />
                  <span>{entry.count}</span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="muted">Aucune réussite enregistrée pour l&apos;instant.</p>
          )}
        </section>

        <section className="card stack">
          <h2>Session live</h2>
          <form onSubmit={createSession} className="stack">
            <label>
              Salle
              <select value={gymId} onChange={(e) => setGymId(e.target.value)} required>
                {gyms.length === 0 && <option value="">Aucune salle — demande à un admin</option>}
                {gyms.map((gym) => (
                  <option key={gym.id} value={gym.id}>
                    {gym.name} {gym.city ? `(${gym.city})` : ''}
                  </option>
                ))}
              </select>
            </label>
            <button type="submit" className="btn btn-primary" disabled={loading || !gymId}>
              Créer une session
            </button>
          </form>

          <hr />

          <form onSubmit={joinSession} className="stack">
            <label>
              Code salle (6 caractères)
              <input
                value={roomCode}
                onChange={(e) => setRoomCode(e.target.value.toUpperCase())}
                pattern="[A-Z0-9]{6}"
                maxLength={6}
                placeholder="ABC123"
                required
              />
            </label>
            <button type="submit" className="btn btn-secondary" disabled={loading}>
              Rejoindre avec le code
            </button>
          </form>
        </section>
      </div>
    </div>
  );
}
