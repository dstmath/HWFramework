package ohos.app;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ProcessInfo implements Sequenceable {
    public static final Sequenceable.Producer<ProcessInfo> PRODUCER = $$Lambda$ProcessInfo$xSzp3_eQzPceWvQ0m2McaUGriF0.INSTANCE;
    private int pid;
    private String processName;

    static /* synthetic */ ProcessInfo lambda$static$0(Parcel parcel) {
        ProcessInfo processInfo = new ProcessInfo();
        processInfo.unmarshalling(parcel);
        return processInfo;
    }

    public ProcessInfo() {
    }

    public ProcessInfo(String str, int i) {
        this.processName = str;
        this.pid = i;
    }

    public String getProcessName() {
        return this.processName;
    }

    public int getPid() {
        return this.pid;
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel.writeString(this.processName) && parcel.writeInt(this.pid)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.processName = parcel.readString();
        this.pid = parcel.readInt();
        return true;
    }
}
