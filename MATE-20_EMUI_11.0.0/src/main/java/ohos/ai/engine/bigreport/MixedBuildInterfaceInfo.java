package ohos.ai.engine.bigreport;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class MixedBuildInterfaceInfo implements Sequenceable {
    private CallAppInfo mCallAppInfo = new CallAppInfo();
    private int mDetailResult;
    private int mInterfaceId;
    private int mResult;
    private long mRunTime;

    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.mInterfaceId);
        parcel.writeLong(this.mRunTime);
        parcel.writeInt(this.mResult);
        parcel.writeInt(this.mDetailResult);
        parcel.writeSequenceable(this.mCallAppInfo);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.mInterfaceId = parcel.readInt();
        this.mRunTime = parcel.readLong();
        this.mResult = parcel.readInt();
        this.mDetailResult = parcel.readInt();
        parcel.readSequenceable(this.mCallAppInfo);
        return true;
    }

    public int getInterfaceId() {
        return this.mInterfaceId;
    }

    public void setInterfaceId(int i) {
        this.mInterfaceId = i;
    }

    public long getRunTime() {
        return this.mRunTime;
    }

    public void setRunTime(long j) {
        this.mRunTime = j;
    }

    public int getResult() {
        return this.mResult;
    }

    public void setResult(int i) {
        this.mResult = i;
    }

    public int getDetailResult() {
        return this.mDetailResult;
    }

    public void setDetailResult(int i) {
        this.mDetailResult = i;
    }

    public int getCallingUid() {
        return this.mCallAppInfo.getCallingUid();
    }

    public void setCallingUid(int i) {
        this.mCallAppInfo.setCallingUid(i);
    }

    public int getCallingPid() {
        return this.mCallAppInfo.getCallingPid();
    }

    public void setCallingPid(int i) {
        this.mCallAppInfo.setCallingPid(i);
    }

    public int getCallState() {
        return this.mCallAppInfo.getCallState();
    }

    public void setCallState(int i) {
        this.mCallAppInfo.setCallState(i);
    }
}
