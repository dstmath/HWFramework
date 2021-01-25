package ohos.ai.engine.bigreport;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class InterfaceInfo implements Sequenceable {
    private CallAppInfo callAppInfo = new CallAppInfo();
    private String detailResult;
    private String interfaceName;
    private String result;
    private long runTime;

    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.interfaceName);
        parcel.writeLong(this.runTime);
        parcel.writeString(this.result);
        parcel.writeString(this.detailResult);
        parcel.writeSequenceable(this.callAppInfo);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.interfaceName = parcel.readString();
        this.runTime = parcel.readLong();
        this.result = parcel.readString();
        this.detailResult = parcel.readString();
        parcel.readSequenceable(this.callAppInfo);
        return true;
    }

    public String getInterfaceName() {
        return this.interfaceName;
    }

    public void setInterfaceName(String str) {
        this.interfaceName = str;
    }

    public long getRunTime() {
        return this.runTime;
    }

    public void setRunTime(long j) {
        this.runTime = j;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String str) {
        this.result = str;
    }

    public String getDetailResult() {
        return this.detailResult;
    }

    public void setDetailResult(String str) {
        this.detailResult = str;
    }

    public int getCallingUid() {
        return this.callAppInfo.getCallingUid();
    }

    public void setCallingUid(int i) {
        this.callAppInfo.setCallingUid(i);
    }

    public int getCallingPid() {
        return this.callAppInfo.getCallingPid();
    }

    public void setCallingPid(int i) {
        this.callAppInfo.setCallingPid(i);
    }

    public String getPkgName() {
        return this.callAppInfo.getCallName();
    }

    public void setPkgName(String str) {
        this.callAppInfo.setCallName(str);
    }

    public String getCallVersion() {
        return this.callAppInfo.getCallVersion();
    }

    public void setCallVersion(String str) {
        this.callAppInfo.setCallVersion(str);
    }

    public int getCallState() {
        return this.callAppInfo.getCallState();
    }

    public void setCallState(int i) {
        this.callAppInfo.setCallState(i);
    }
}
