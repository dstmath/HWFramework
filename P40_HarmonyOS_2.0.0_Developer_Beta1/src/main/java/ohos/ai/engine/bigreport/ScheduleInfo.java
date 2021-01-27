package ohos.ai.engine.bigreport;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ScheduleInfo implements Sequenceable {
    private String apiName;
    private int callTimes;
    private String packageName;
    private long runTime;

    public boolean marshalling(Parcel parcel) {
        parcel.writeLong(this.runTime);
        parcel.writeString(this.packageName);
        parcel.writeString(this.apiName);
        parcel.writeInt(this.callTimes);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.runTime = parcel.readLong();
        this.packageName = parcel.readString();
        this.apiName = parcel.readString();
        this.callTimes = parcel.readInt();
        return true;
    }

    public long getRunTime() {
        return this.runTime;
    }

    public void setRunTime(long j) {
        this.runTime = j;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public String getApiName() {
        return this.apiName;
    }

    public void setApiName(String str) {
        this.apiName = str;
    }

    public int getCallTimes() {
        return this.callTimes;
    }

    public void setCallTimes(int i) {
        this.callTimes = i;
    }
}
