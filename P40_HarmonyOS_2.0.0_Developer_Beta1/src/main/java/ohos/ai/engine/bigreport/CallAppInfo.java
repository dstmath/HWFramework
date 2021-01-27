package ohos.ai.engine.bigreport;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class CallAppInfo implements Sequenceable {
    private String callName;
    private int callState = 0;
    private String callVersion;
    private int callingPid = -1;
    private int callingUid = -1;

    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.callName);
        parcel.writeString(this.callVersion);
        parcel.writeInt(this.callState);
        parcel.writeInt(this.callingUid);
        parcel.writeInt(this.callingPid);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.callName = parcel.readString();
        this.callVersion = parcel.readString();
        this.callState = parcel.readInt();
        this.callingUid = parcel.readInt();
        this.callingPid = parcel.readInt();
        return true;
    }

    public String getCallName() {
        return this.callName;
    }

    public void setCallName(String str) {
        this.callName = str;
    }

    public String getCallVersion() {
        return this.callVersion;
    }

    public void setCallVersion(String str) {
        this.callVersion = str;
    }

    public int getCallState() {
        return this.callState;
    }

    public void setCallState(int i) {
        this.callState = i;
    }

    public int getCallingUid() {
        return this.callingUid;
    }

    public void setCallingUid(int i) {
        this.callingUid = i;
    }

    public int getCallingPid() {
        return this.callingPid;
    }

    public void setCallingPid(int i) {
        this.callingPid = i;
    }
}
