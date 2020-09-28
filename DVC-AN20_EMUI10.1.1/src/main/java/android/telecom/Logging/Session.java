package android.telecom.Logging;

import android.os.Parcel;
import android.os.Parcelable;
import android.telecom.Log;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Iterator;

public class Session {
    public static final String CONTINUE_SUBSESSION = "CONTINUE_SUBSESSION";
    public static final String CREATE_SUBSESSION = "CREATE_SUBSESSION";
    public static final String END_SESSION = "END_SESSION";
    public static final String END_SUBSESSION = "END_SUBSESSION";
    public static final String EXTERNAL_INDICATOR = "E-";
    public static final String SESSION_SEPARATION_CHAR_CHILD = "_";
    public static final String START_EXTERNAL_SESSION = "START_EXTERNAL_SESSION";
    public static final String START_SESSION = "START_SESSION";
    public static final String SUBSESSION_SEPARATION_CHAR = "->";
    public static final String TRUNCATE_STRING = "...";
    public static final int UNDEFINED = -1;
    private int mChildCounter = 0;
    private ArrayList<Session> mChildSessions;
    private long mExecutionEndTimeMs = -1;
    private long mExecutionStartTimeMs;
    private String mFullMethodPathCache;
    private boolean mIsCompleted = false;
    private boolean mIsExternal = false;
    private boolean mIsStartedFromActiveSession = false;
    private String mOwnerInfo;
    private Session mParentSession;
    private String mSessionId;
    private String mShortMethodName;

    public static class Info implements Parcelable {
        public static final Parcelable.Creator<Info> CREATOR = new Parcelable.Creator<Info>() {
            /* class android.telecom.Logging.Session.Info.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Info createFromParcel(Parcel source) {
                return new Info(source.readString(), source.readString());
            }

            @Override // android.os.Parcelable.Creator
            public Info[] newArray(int size) {
                return new Info[size];
            }
        };
        public final String methodPath;
        public final String sessionId;

        private Info(String id, String path) {
            this.sessionId = id;
            this.methodPath = path;
        }

        public static Info getInfo(Session s) {
            return new Info(s.getFullSessionId(), s.getFullMethodPath(!Log.DEBUG && s.isSessionExternal()));
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel destination, int flags) {
            destination.writeString(this.sessionId);
            destination.writeString(this.methodPath);
        }
    }

    public Session(String sessionId, String shortMethodName, long startTimeMs, boolean isStartedFromActiveSession, String ownerInfo) {
        setSessionId(sessionId);
        setShortMethodName(shortMethodName);
        this.mExecutionStartTimeMs = startTimeMs;
        this.mParentSession = null;
        this.mChildSessions = new ArrayList<>(5);
        this.mIsStartedFromActiveSession = isStartedFromActiveSession;
        this.mOwnerInfo = ownerInfo;
    }

    public void setSessionId(String sessionId) {
        if (sessionId == null) {
            this.mSessionId = "?";
        }
        this.mSessionId = sessionId;
    }

    public String getShortMethodName() {
        return this.mShortMethodName;
    }

    public void setShortMethodName(String shortMethodName) {
        if (shortMethodName == null) {
            shortMethodName = "";
        }
        this.mShortMethodName = shortMethodName;
    }

    public void setIsExternal(boolean isExternal) {
        this.mIsExternal = isExternal;
    }

    public boolean isExternal() {
        return this.mIsExternal;
    }

    public void setParentSession(Session parentSession) {
        this.mParentSession = parentSession;
    }

    public void addChild(Session childSession) {
        if (childSession != null) {
            this.mChildSessions.add(childSession);
        }
    }

    public void removeChild(Session child) {
        if (child != null) {
            this.mChildSessions.remove(child);
        }
    }

    public long getExecutionStartTimeMilliseconds() {
        return this.mExecutionStartTimeMs;
    }

    public void setExecutionStartTimeMs(long startTimeMs) {
        this.mExecutionStartTimeMs = startTimeMs;
    }

    public Session getParentSession() {
        return this.mParentSession;
    }

    public ArrayList<Session> getChildSessions() {
        return this.mChildSessions;
    }

    public boolean isSessionCompleted() {
        return this.mIsCompleted;
    }

    public boolean isStartedFromActiveSession() {
        return this.mIsStartedFromActiveSession;
    }

    public Info getInfo() {
        return Info.getInfo(this);
    }

    @VisibleForTesting
    public String getSessionId() {
        return this.mSessionId;
    }

    public void markSessionCompleted(long executionEndTimeMs) {
        this.mExecutionEndTimeMs = executionEndTimeMs;
        this.mIsCompleted = true;
    }

    public long getLocalExecutionTime() {
        long j = this.mExecutionEndTimeMs;
        if (j == -1) {
            return -1;
        }
        return j - this.mExecutionStartTimeMs;
    }

    public synchronized String getNextChildId() {
        int i;
        i = this.mChildCounter;
        this.mChildCounter = i + 1;
        return String.valueOf(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getFullSessionId() {
        Session parentSession = this.mParentSession;
        if (parentSession == null) {
            return this.mSessionId;
        }
        if (!Log.VERBOSE) {
            return parentSession.getFullSessionId();
        }
        return parentSession.getFullSessionId() + SESSION_SEPARATION_CHAR_CHILD + this.mSessionId;
    }

    public String printFullSessionTree() {
        Session topNode = this;
        while (topNode.getParentSession() != null) {
            topNode = topNode.getParentSession();
        }
        return topNode.printSessionTree();
    }

    public String printSessionTree() {
        StringBuilder sb = new StringBuilder();
        printSessionTree(0, sb);
        return sb.toString();
    }

    private void printSessionTree(int tabI, StringBuilder sb) {
        sb.append(toString());
        Iterator<Session> it = this.mChildSessions.iterator();
        while (it.hasNext()) {
            Session child = it.next();
            sb.append("\n");
            for (int i = 0; i <= tabI; i++) {
                sb.append("\t");
            }
            child.printSessionTree(tabI + 1, sb);
        }
    }

    public String getFullMethodPath(boolean truncatePath) {
        StringBuilder sb = new StringBuilder();
        getFullMethodPath(sb, truncatePath);
        return sb.toString();
    }

    private synchronized void getFullMethodPath(StringBuilder sb, boolean truncatePath) {
        if (TextUtils.isEmpty(this.mFullMethodPathCache) || truncatePath) {
            Session parentSession = getParentSession();
            boolean isSessionStarted = false;
            if (parentSession != null) {
                isSessionStarted = !this.mShortMethodName.equals(parentSession.mShortMethodName);
                parentSession.getFullMethodPath(sb, truncatePath);
                sb.append(SUBSESSION_SEPARATION_CHAR);
            }
            if (!isExternal()) {
                sb.append(this.mShortMethodName);
            } else if (truncatePath) {
                sb.append(TRUNCATE_STRING);
            } else {
                sb.append("(");
                sb.append(this.mShortMethodName);
                sb.append(")");
            }
            if (isSessionStarted && !truncatePath) {
                this.mFullMethodPathCache = sb.toString();
            }
            return;
        }
        sb.append(this.mFullMethodPathCache);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSessionExternal() {
        if (getParentSession() == null) {
            return isExternal();
        }
        return getParentSession().isSessionExternal();
    }

    public int hashCode() {
        String str = this.mSessionId;
        int i = 0;
        int hashCode = (str != null ? str.hashCode() : 0) * 31;
        String str2 = this.mShortMethodName;
        int hashCode2 = str2 != null ? str2.hashCode() : 0;
        long j = this.mExecutionStartTimeMs;
        long j2 = this.mExecutionEndTimeMs;
        int result = (((((hashCode + hashCode2) * 31) + ((int) (j ^ (j >>> 32)))) * 31) + ((int) (j2 ^ (j2 >>> 32)))) * 31;
        Session session = this.mParentSession;
        int result2 = (result + (session != null ? session.hashCode() : 0)) * 31;
        ArrayList<Session> arrayList = this.mChildSessions;
        int result3 = (((((((result2 + (arrayList != null ? arrayList.hashCode() : 0)) * 31) + (this.mIsCompleted ? 1 : 0)) * 31) + this.mChildCounter) * 31) + (this.mIsStartedFromActiveSession ? 1 : 0)) * 31;
        String str3 = this.mOwnerInfo;
        if (str3 != null) {
            i = str3.hashCode();
        }
        return result3 + i;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Session session = (Session) o;
        if (this.mExecutionStartTimeMs != session.mExecutionStartTimeMs || this.mExecutionEndTimeMs != session.mExecutionEndTimeMs || this.mIsCompleted != session.mIsCompleted || this.mChildCounter != session.mChildCounter || this.mIsStartedFromActiveSession != session.mIsStartedFromActiveSession) {
            return false;
        }
        String str = this.mSessionId;
        if (str == null ? session.mSessionId != null : !str.equals(session.mSessionId)) {
            return false;
        }
        String str2 = this.mShortMethodName;
        if (str2 == null ? session.mShortMethodName != null : !str2.equals(session.mShortMethodName)) {
            return false;
        }
        Session session2 = this.mParentSession;
        if (session2 == null ? session.mParentSession != null : !session2.equals(session.mParentSession)) {
            return false;
        }
        ArrayList<Session> arrayList = this.mChildSessions;
        if (arrayList == null ? session.mChildSessions != null : !arrayList.equals(session.mChildSessions)) {
            return false;
        }
        String str3 = this.mOwnerInfo;
        if (str3 != null) {
            return str3.equals(session.mOwnerInfo);
        }
        if (session.mOwnerInfo == null) {
            return true;
        }
        return false;
    }

    public String toString() {
        Session session = this.mParentSession;
        if (session != null && this.mIsStartedFromActiveSession) {
            return session.toString();
        }
        StringBuilder methodName = new StringBuilder();
        methodName.append(getFullMethodPath(false));
        String str = this.mOwnerInfo;
        if (str != null && !str.isEmpty()) {
            methodName.append("(InCall package: ");
            methodName.append(this.mOwnerInfo);
            methodName.append(")");
        }
        return methodName.toString() + "@" + getFullSessionId();
    }
}
