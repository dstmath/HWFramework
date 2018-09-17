package android.telecom.Logging;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telecom.Log;
import android.text.TextUtils;
import android.util.LogException;
import java.util.ArrayList;

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
        public static final Creator<Info> CREATOR = new Creator<Info>() {
            public Info createFromParcel(Parcel source) {
                return new Info(source.readString(), source.readString(), null);
            }

            public Info[] newArray(int size) {
                return new Info[size];
            }
        };
        public final String methodPath;
        public final String sessionId;

        /* synthetic */ Info(String id, String path, Info -this2) {
            this(id, path);
        }

        private Info(String id, String path) {
            this.sessionId = id;
            this.methodPath = path;
        }

        public static Info getInfo(Session s) {
            return new Info(s.getFullSessionId(), s.getFullMethodPath(!Log.DEBUG ? s.isSessionExternal() : false));
        }

        public int describeContents() {
            return 0;
        }

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
        this.mChildSessions = new ArrayList(5);
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
            shortMethodName = LogException.NO_VALUE;
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

    public String getSessionId() {
        return this.mSessionId;
    }

    public void markSessionCompleted(long executionEndTimeMs) {
        this.mExecutionEndTimeMs = executionEndTimeMs;
        this.mIsCompleted = true;
    }

    public long getLocalExecutionTime() {
        if (this.mExecutionEndTimeMs == -1) {
            return -1;
        }
        return this.mExecutionEndTimeMs - this.mExecutionStartTimeMs;
    }

    public synchronized String getNextChildId() {
        int i;
        i = this.mChildCounter;
        this.mChildCounter = i + 1;
        return String.valueOf(i);
    }

    private String getFullSessionId() {
        Session parentSession = this.mParentSession;
        if (parentSession == null) {
            return this.mSessionId;
        }
        if (Log.VERBOSE) {
            return parentSession.getFullSessionId() + SESSION_SEPARATION_CHAR_CHILD + this.mSessionId;
        }
        return parentSession.getFullSessionId();
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
        for (Session child : this.mChildSessions) {
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

    /* JADX WARNING: Missing block: B:22:0x0049, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void getFullMethodPath(StringBuilder sb, boolean truncatePath) {
        if (TextUtils.isEmpty(this.mFullMethodPathCache) || (truncatePath ^ 1) == 0) {
            Session parentSession = getParentSession();
            boolean isSessionStarted = false;
            if (parentSession != null) {
                isSessionStarted = this.mShortMethodName.equals(parentSession.mShortMethodName) ^ 1;
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
            if (isSessionStarted && (truncatePath ^ 1) != 0) {
                this.mFullMethodPathCache = sb.toString();
            }
        } else {
            sb.append(this.mFullMethodPathCache);
        }
    }

    private boolean isSessionExternal() {
        if (getParentSession() == null) {
            return isExternal();
        }
        return getParentSession().isSessionExternal();
    }

    public int hashCode() {
        int hashCode;
        int i = 1;
        int i2 = 0;
        int hashCode2 = (this.mSessionId != null ? this.mSessionId.hashCode() : 0) * 31;
        if (this.mShortMethodName != null) {
            hashCode = this.mShortMethodName.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (((((hashCode2 + hashCode) * 31) + ((int) (this.mExecutionStartTimeMs ^ (this.mExecutionStartTimeMs >>> 32)))) * 31) + ((int) (this.mExecutionEndTimeMs ^ (this.mExecutionEndTimeMs >>> 32)))) * 31;
        if (this.mParentSession != null) {
            hashCode = this.mParentSession.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode2 + hashCode) * 31;
        if (this.mChildSessions != null) {
            hashCode = this.mChildSessions.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode2 + hashCode) * 31;
        if (this.mIsCompleted) {
            hashCode = 1;
        } else {
            hashCode = 0;
        }
        hashCode = (((hashCode2 + hashCode) * 31) + this.mChildCounter) * 31;
        if (!this.mIsStartedFromActiveSession) {
            i = 0;
        }
        hashCode = (hashCode + i) * 31;
        if (this.mOwnerInfo != null) {
            i2 = this.mOwnerInfo.hashCode();
        }
        return hashCode + i2;
    }

    public boolean equals(Object o) {
        boolean z = true;
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
        if (this.mSessionId == null ? session.mSessionId != null : (this.mSessionId.equals(session.mSessionId) ^ 1) != 0) {
            return false;
        }
        if (this.mShortMethodName == null ? session.mShortMethodName != null : (this.mShortMethodName.equals(session.mShortMethodName) ^ 1) != 0) {
            return false;
        }
        if (this.mParentSession == null ? session.mParentSession != null : (this.mParentSession.equals(session.mParentSession) ^ 1) != 0) {
            return false;
        }
        if (this.mChildSessions == null ? session.mChildSessions != null : (this.mChildSessions.equals(session.mChildSessions) ^ 1) != 0) {
            return false;
        }
        if (this.mOwnerInfo != null) {
            z = this.mOwnerInfo.equals(session.mOwnerInfo);
        } else if (session.mOwnerInfo != null) {
            z = false;
        }
        return z;
    }

    public String toString() {
        if (this.mParentSession != null && this.mIsStartedFromActiveSession) {
            return this.mParentSession.toString();
        }
        StringBuilder methodName = new StringBuilder();
        methodName.append(getFullMethodPath(false));
        if (!(this.mOwnerInfo == null || (this.mOwnerInfo.isEmpty() ^ 1) == 0)) {
            methodName.append("(InCall package: ");
            methodName.append(this.mOwnerInfo);
            methodName.append(")");
        }
        return methodName.toString() + "@" + getFullSessionId();
    }
}
