package ohos.aafwk.ability;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ProcessErrorInfo implements Sequenceable {
    public static final int CRASHED = 1;
    public static final int NOT_RESPONDING = 2;
    public static final int NO_ERROR = 0;
    public static final Sequenceable.Producer<ProcessErrorInfo> PRODUCER = $$Lambda$ProcessErrorInfo$NjxgwFCG_NXKBWxVadCImraKUPc.INSTANCE;
    private int condition;
    private String longMsg;
    private int pid;
    private String processName;
    private String shortMsg;
    private String stackTrace;
    private String tag;
    private int uid;

    static /* synthetic */ ProcessErrorInfo lambda$static$0(Parcel parcel) {
        ProcessErrorInfo processErrorInfo = new ProcessErrorInfo();
        processErrorInfo.unmarshalling(parcel);
        return processErrorInfo;
    }

    public int getCondition() {
        return this.condition;
    }

    public void setCondition(int i) {
        this.condition = i;
    }

    public String getProcessName() {
        return this.processName;
    }

    public void setProcessName(String str) {
        this.processName = str;
    }

    public int getPid() {
        return this.pid;
    }

    public void setPid(int i) {
        this.pid = i;
    }

    public int getUid() {
        return this.uid;
    }

    public void setUid(int i) {
        this.uid = i;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String str) {
        this.tag = str;
    }

    public String getShortMsg() {
        return this.shortMsg;
    }

    public void setShortMsg(String str) {
        this.shortMsg = str;
    }

    public String getLongMsg() {
        return this.longMsg;
    }

    public void setLongMsg(String str) {
        this.longMsg = str;
    }

    public String getStackTrace() {
        return this.stackTrace;
    }

    public void setStackTrace(String str) {
        this.stackTrace = str;
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.condition);
        parcel.writeString(this.processName);
        parcel.writeInt(this.pid);
        parcel.writeInt(this.uid);
        parcel.writeString(this.tag);
        parcel.writeString(this.shortMsg);
        parcel.writeString(this.longMsg);
        parcel.writeString(this.stackTrace);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.condition = parcel.readInt();
        this.processName = parcel.readString();
        this.pid = parcel.readInt();
        this.uid = parcel.readInt();
        this.tag = parcel.readString();
        this.shortMsg = parcel.readString();
        this.longMsg = parcel.readString();
        this.stackTrace = parcel.readString();
        return true;
    }

    public String toString() {
        return "condition = " + this.condition + "; processName = " + this.processName + "; uid = " + this.uid + "; pid = " + this.pid + "; tag = " + this.tag + "; shortMsg = " + this.shortMsg + "; longMsg = " + this.longMsg + "; stackTrace" + this.stackTrace + ".";
    }
}
