package android.telecom.Logging;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telecom.Log;
import android.telecom.Logging.Session;
import android.util.Base64;
import com.android.internal.annotations.VisibleForTesting;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final long DEFAULT_SESSION_TIMEOUT_MS = 30000;
    private static final String LOGGING_TAG = "Logging";
    private static final long SESSION_ID_ROLLOVER_THRESHOLD = 262144;
    private static final String TIMEOUTS_PREFIX = "telecom.";
    @VisibleForTesting
    public Runnable mCleanStaleSessions = new Runnable() {
        public final void run() {
            SessionManager.this.cleanupStaleSessions(SessionManager.this.getSessionCleanupTimeoutMs());
        }
    };
    private Context mContext;
    @VisibleForTesting
    public ICurrentThreadId mCurrentThreadId = $$Lambda$L5F_SL2jOCUETYvgdB36aGwY50E.INSTANCE;
    private Handler mSessionCleanupHandler = new Handler(Looper.getMainLooper());
    private ISessionCleanupTimeoutMs mSessionCleanupTimeoutMs = new ISessionCleanupTimeoutMs() {
        public final long get() {
            return SessionManager.lambda$new$1(SessionManager.this);
        }
    };
    private List<ISessionListener> mSessionListeners = new ArrayList();
    @VisibleForTesting
    public ConcurrentHashMap<Integer, Session> mSessionMapper = new ConcurrentHashMap<>(100);
    private int sCodeEntryCounter = 0;

    public interface ICurrentThreadId {
        int get();
    }

    private interface ISessionCleanupTimeoutMs {
        long get();
    }

    public interface ISessionIdQueryHandler {
        String getSessionId();
    }

    public interface ISessionListener {
        void sessionComplete(String str, long j);
    }

    public static /* synthetic */ long lambda$new$1(SessionManager sessionManager) {
        if (sessionManager.mContext == null) {
            return DEFAULT_SESSION_TIMEOUT_MS;
        }
        return sessionManager.getCleanupTimeout(sessionManager.mContext);
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    private long getSessionCleanupTimeoutMs() {
        return this.mSessionCleanupTimeoutMs.get();
    }

    private synchronized void resetStaleSessionTimer() {
        this.mSessionCleanupHandler.removeCallbacksAndMessages(null);
        if (this.mCleanStaleSessions != null) {
            this.mSessionCleanupHandler.postDelayed(this.mCleanStaleSessions, getSessionCleanupTimeoutMs());
        }
    }

    public synchronized void startSession(Session.Info info, String shortMethodName, String callerIdentification) {
        if (info == null) {
            try {
                startSession(shortMethodName, callerIdentification);
            } catch (Throwable th) {
                throw th;
            }
        } else {
            startExternalSession(info, shortMethodName);
        }
    }

    public synchronized void startSession(String shortMethodName, String callerIdentification) {
        resetStaleSessionTimer();
        int threadId = getCallingThreadId();
        if (this.mSessionMapper.get(Integer.valueOf(threadId)) != null) {
            continueSession(createSubsession(true), shortMethodName);
            return;
        }
        Log.d(LOGGING_TAG, Session.START_SESSION, new Object[0]);
        Session session = new Session(getNextSessionID(), shortMethodName, System.currentTimeMillis(), false, callerIdentification);
        this.mSessionMapper.put(Integer.valueOf(threadId), session);
    }

    public synchronized void startExternalSession(Session.Info sessionInfo, String shortMethodName) {
        if (sessionInfo != null) {
            int threadId = getCallingThreadId();
            if (this.mSessionMapper.get(Integer.valueOf(threadId)) != null) {
                Log.w(LOGGING_TAG, "trying to start an external session with a session already active.", new Object[0]);
                return;
            }
            Log.d(LOGGING_TAG, Session.START_EXTERNAL_SESSION, new Object[0]);
            Session session = new Session(Session.EXTERNAL_INDICATOR + sessionInfo.sessionId, sessionInfo.methodPath, System.currentTimeMillis(), false, null);
            session.setIsExternal(true);
            session.markSessionCompleted(-1);
            this.mSessionMapper.put(Integer.valueOf(threadId), session);
            continueSession(createSubsession(), shortMethodName);
        }
    }

    public Session createSubsession() {
        return createSubsession(false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0064, code lost:
        return r3;
     */
    private synchronized Session createSubsession(boolean isStartedFromActiveSession) {
        Session threadSession = this.mSessionMapper.get(Integer.valueOf(getCallingThreadId()));
        if (threadSession == null) {
            Log.d(LOGGING_TAG, "Log.createSubsession was called with no session active.", new Object[0]);
            return null;
        }
        Session session = new Session(threadSession.getNextChildId(), threadSession.getShortMethodName(), System.currentTimeMillis(), isStartedFromActiveSession, null);
        Session newSubsession = session;
        threadSession.addChild(newSubsession);
        newSubsession.setParentSession(threadSession);
        if (!isStartedFromActiveSession) {
            Log.v(LOGGING_TAG, "CREATE_SUBSESSION " + newSubsession.toString(), new Object[0]);
        } else {
            Log.v(LOGGING_TAG, "CREATE_SUBSESSION (Invisible subsession)", new Object[0]);
        }
    }

    public synchronized Session.Info getExternalSession() {
        Session threadSession = this.mSessionMapper.get(Integer.valueOf(getCallingThreadId()));
        if (threadSession == null) {
            Log.d(LOGGING_TAG, "Log.getExternalSession was called with no session active.", new Object[0]);
            return null;
        }
        return threadSession.getInfo();
    }

    public synchronized void cancelSubsession(Session subsession) {
        if (subsession != null) {
            subsession.markSessionCompleted(-1);
            endParentSessions(subsession);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0069, code lost:
        return;
     */
    public synchronized void continueSession(Session subsession, String shortMethodName) {
        if (subsession != null) {
            resetStaleSessionTimer();
            subsession.setShortMethodName(shortMethodName);
            subsession.setExecutionStartTimeMs(System.currentTimeMillis());
            if (subsession.getParentSession() == null) {
                Log.i(LOGGING_TAG, "Log.continueSession was called with no session active for method " + shortMethodName, new Object[0]);
                return;
            }
            this.mSessionMapper.put(Integer.valueOf(getCallingThreadId()), subsession);
            if (!subsession.isStartedFromActiveSession()) {
                Log.v(LOGGING_TAG, Session.CONTINUE_SUBSESSION, new Object[0]);
            } else {
                Log.v(LOGGING_TAG, "CONTINUE_SUBSESSION (Invisible Subsession) with Method " + shortMethodName, new Object[0]);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0097, code lost:
        return;
     */
    public synchronized void endSession() {
        int threadId = getCallingThreadId();
        Session completedSession = this.mSessionMapper.get(Integer.valueOf(threadId));
        if (completedSession == null) {
            Log.w(LOGGING_TAG, "Log.endSession was called with no session active.", new Object[0]);
            return;
        }
        completedSession.markSessionCompleted(System.currentTimeMillis());
        if (!completedSession.isStartedFromActiveSession()) {
            Log.v(LOGGING_TAG, "END_SUBSESSION (dur: " + completedSession.getLocalExecutionTime() + " mS)", new Object[0]);
        } else {
            Log.v(LOGGING_TAG, "END_SUBSESSION (Invisible Subsession) (dur: " + completedSession.getLocalExecutionTime() + " ms)", new Object[0]);
        }
        Session parentSession = completedSession.getParentSession();
        this.mSessionMapper.remove(Integer.valueOf(threadId));
        endParentSessions(completedSession);
        if (parentSession != null && !parentSession.isSessionCompleted() && completedSession.isStartedFromActiveSession()) {
            this.mSessionMapper.put(Integer.valueOf(threadId), parentSession);
        }
    }

    private void endParentSessions(Session subsession) {
        if (subsession.isSessionCompleted() && subsession.getChildSessions().size() == 0) {
            Session parentSession = subsession.getParentSession();
            if (parentSession != null) {
                subsession.setParentSession(null);
                parentSession.removeChild(subsession);
                if (parentSession.isExternal()) {
                    notifySessionCompleteListeners(subsession.getShortMethodName(), System.currentTimeMillis() - subsession.getExecutionStartTimeMilliseconds());
                }
                endParentSessions(parentSession);
            } else {
                long fullSessionTimeMs = System.currentTimeMillis() - subsession.getExecutionStartTimeMilliseconds();
                Log.d(LOGGING_TAG, "END_SESSION (dur: " + fullSessionTimeMs + " ms): " + subsession.toString(), new Object[0]);
                if (!subsession.isExternal()) {
                    notifySessionCompleteListeners(subsession.getShortMethodName(), fullSessionTimeMs);
                }
            }
        }
    }

    private void notifySessionCompleteListeners(String methodName, long sessionTimeMs) {
        for (ISessionListener l : this.mSessionListeners) {
            l.sessionComplete(methodName, sessionTimeMs);
        }
    }

    public String getSessionId() {
        Session currentSession = this.mSessionMapper.get(Integer.valueOf(getCallingThreadId()));
        return currentSession != null ? currentSession.toString() : "";
    }

    /* Debug info: failed to restart local var, previous not found, register: 1 */
    public synchronized void registerSessionListener(ISessionListener l) {
        if (l != null) {
            this.mSessionListeners.add(l);
        }
    }

    private synchronized String getNextSessionID() {
        Integer nextId;
        int i = this.sCodeEntryCounter;
        this.sCodeEntryCounter = i + 1;
        nextId = Integer.valueOf(i);
        if (((long) nextId.intValue()) >= 262144) {
            restartSessionCounter();
            int i2 = this.sCodeEntryCounter;
            this.sCodeEntryCounter = i2 + 1;
            nextId = Integer.valueOf(i2);
        }
        return getBase64Encoding(nextId.intValue());
    }

    private synchronized void restartSessionCounter() {
        this.sCodeEntryCounter = 0;
    }

    private String getBase64Encoding(int number) {
        return Base64.encodeToString(Arrays.copyOfRange(ByteBuffer.allocate(4).putInt(number).array(), 2, 4), 3);
    }

    private int getCallingThreadId() {
        return this.mCurrentThreadId.get();
    }

    @VisibleForTesting
    public synchronized void cleanupStaleSessions(long timeoutMs) {
        String logMessage = "Stale Sessions Cleaned:\n";
        boolean isSessionsStale = false;
        long currentTimeMs = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, Session>> it = this.mSessionMapper.entrySet().iterator();
        while (it.hasNext()) {
            if (currentTimeMs - it.next().getValue().getExecutionStartTimeMilliseconds() > timeoutMs) {
                it.remove();
                logMessage = logMessage + session.printFullSessionTree() + "\n";
                isSessionsStale = true;
            }
        }
        if (isSessionsStale) {
            Log.w(LOGGING_TAG, logMessage, new Object[0]);
        } else {
            Log.v(LOGGING_TAG, "No stale logging sessions needed to be cleaned...", new Object[0]);
        }
    }

    private long getCleanupTimeout(Context context) {
        return Settings.Secure.getLong(context.getContentResolver(), "telecom.stale_session_cleanup_timeout_millis", DEFAULT_SESSION_TIMEOUT_MS);
    }
}
