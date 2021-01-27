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
        /* class android.telecom.Logging.$$Lambda$SessionManager$VyH2gT1EjIvzDy_C9JfTT60CISM */

        @Override // java.lang.Runnable
        public final void run() {
            SessionManager.this.lambda$new$0$SessionManager();
        }
    };
    private Context mContext;
    @VisibleForTesting
    public ICurrentThreadId mCurrentThreadId = $$Lambda$L5F_SL2jOCUETYvgdB36aGwY50E.INSTANCE;
    private Handler mSessionCleanupHandler = new Handler(Looper.getMainLooper());
    private ISessionCleanupTimeoutMs mSessionCleanupTimeoutMs = new ISessionCleanupTimeoutMs() {
        /* class android.telecom.Logging.$$Lambda$SessionManager$hhtZwTEbvOfLNlAvB6Do9_2gW4 */

        @Override // android.telecom.Logging.SessionManager.ISessionCleanupTimeoutMs
        public final long get() {
            return SessionManager.this.lambda$new$1$SessionManager();
        }
    };
    private List<ISessionListener> mSessionListeners = new ArrayList();
    @VisibleForTesting
    public ConcurrentHashMap<Integer, Session> mSessionMapper = new ConcurrentHashMap<>(100);
    private int sCodeEntryCounter = 0;

    public interface ICurrentThreadId {
        int get();
    }

    /* access modifiers changed from: private */
    public interface ISessionCleanupTimeoutMs {
        long get();
    }

    public interface ISessionIdQueryHandler {
        String getSessionId();
    }

    public interface ISessionListener {
        void sessionComplete(String str, long j);
    }

    public /* synthetic */ void lambda$new$0$SessionManager() {
        cleanupStaleSessions(getSessionCleanupTimeoutMs());
    }

    public /* synthetic */ long lambda$new$1$SessionManager() {
        Context context = this.mContext;
        if (context == null) {
            return 30000;
        }
        return getCleanupTimeout(context);
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
            startSession(shortMethodName, callerIdentification);
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
        this.mSessionMapper.put(Integer.valueOf(threadId), new Session(getNextSessionID(), shortMethodName, System.currentTimeMillis(), false, callerIdentification));
    }

    public synchronized void startExternalSession(Session.Info sessionInfo, String shortMethodName) {
        if (sessionInfo != null) {
            int threadId = getCallingThreadId();
            if (this.mSessionMapper.get(Integer.valueOf(threadId)) != null) {
                Log.w(LOGGING_TAG, "trying to start an external session with a session already active.", new Object[0]);
                return;
            }
            Log.d(LOGGING_TAG, Session.START_EXTERNAL_SESSION, new Object[0]);
            Session externalSession = new Session(Session.EXTERNAL_INDICATOR + sessionInfo.sessionId, sessionInfo.methodPath, System.currentTimeMillis(), false, null);
            externalSession.setIsExternal(true);
            externalSession.markSessionCompleted(-1);
            this.mSessionMapper.put(Integer.valueOf(threadId), externalSession);
            continueSession(createSubsession(), shortMethodName);
        }
    }

    public Session createSubsession() {
        return createSubsession(false);
    }

    private synchronized Session createSubsession(boolean isStartedFromActiveSession) {
        Session threadSession = this.mSessionMapper.get(Integer.valueOf(getCallingThreadId()));
        if (threadSession == null) {
            Log.d(LOGGING_TAG, "Log.createSubsession was called with no session active.", new Object[0]);
            return null;
        }
        Session newSubsession = new Session(threadSession.getNextChildId(), threadSession.getShortMethodName(), System.currentTimeMillis(), isStartedFromActiveSession, null);
        threadSession.addChild(newSubsession);
        newSubsession.setParentSession(threadSession);
        if (!isStartedFromActiveSession) {
            Log.v(LOGGING_TAG, "CREATE_SUBSESSION " + newSubsession.toString(), new Object[0]);
        } else {
            Log.v(LOGGING_TAG, "CREATE_SUBSESSION (Invisible subsession)", new Object[0]);
        }
        return newSubsession;
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
                return;
            }
            long fullSessionTimeMs = System.currentTimeMillis() - subsession.getExecutionStartTimeMilliseconds();
            Log.d(LOGGING_TAG, "END_SESSION (dur: " + fullSessionTimeMs + " ms): " + subsession.toString(), new Object[0]);
            if (!subsession.isExternal()) {
                notifySessionCompleteListeners(subsession.getShortMethodName(), fullSessionTimeMs);
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

    private void breakCycleWhenRemove(Session session) {
        ArrayList<Session> childs = session.getChildSessions();
        if (childs != null) {
            for (int i = 0; i < childs.size(); i++) {
                Session child = childs.get(i);
                if (child != null) {
                    breakCycleWhenRemove(child);
                    child.setParentSession(null);
                }
            }
        }
    }

    @VisibleForTesting
    public synchronized void cleanupStaleSessions(long timeoutMs) {
        String logMessage = "Stale Sessions Cleaned:\n";
        boolean isSessionsStale = false;
        long currentTimeMs = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, Session>> it = this.mSessionMapper.entrySet().iterator();
        while (it.hasNext()) {
            Session session = it.next().getValue();
            if (currentTimeMs - session.getExecutionStartTimeMilliseconds() > timeoutMs) {
                it.remove();
                logMessage = logMessage + session.printFullSessionTree() + "\n";
                isSessionsStale = true;
                Session topNode = session;
                while (topNode.getParentSession() != null) {
                    topNode = topNode.getParentSession();
                }
                breakCycleWhenRemove(topNode);
            }
        }
        if (isSessionsStale) {
            Log.w(LOGGING_TAG, logMessage, new Object[0]);
        } else {
            Log.v(LOGGING_TAG, "No stale logging sessions needed to be cleaned...", new Object[0]);
        }
    }

    private long getCleanupTimeout(Context context) {
        return Settings.Secure.getLong(context.getContentResolver(), "telecom.stale_session_cleanup_timeout_millis", 30000);
    }
}
