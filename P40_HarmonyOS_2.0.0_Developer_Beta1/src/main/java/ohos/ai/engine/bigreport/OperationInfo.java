package ohos.ai.engine.bigreport;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class OperationInfo implements Sequenceable {
    private int callingPid = -1;
    private int callingUid = -1;
    private String operationName;
    private long operationTime;
    private String result;

    public boolean marshalling(Parcel parcel) {
        parcel.writeLong(this.operationTime);
        parcel.writeString(this.operationName);
        parcel.writeString(this.result);
        parcel.writeInt(this.callingUid);
        parcel.writeInt(this.callingPid);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.operationTime = parcel.readLong();
        this.operationName = parcel.readString();
        this.result = parcel.readString();
        this.callingUid = parcel.readInt();
        this.callingPid = parcel.readInt();
        return true;
    }

    public long getOperationTime() {
        return this.operationTime;
    }

    public void setOperationTime(long j) {
        this.operationTime = j;
    }

    public String getOperationName() {
        return this.operationName;
    }

    public void setOperationName(String str) {
        this.operationName = str;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String str) {
        this.result = str;
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
