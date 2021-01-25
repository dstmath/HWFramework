package ohos.distributedschedule.scenario;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;

public final class ScenarioInfo {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109952, TAG);
    private static final String TAG = "ScenarioInfo";
    private String bundleName;
    private boolean direction;
    private int scenarioType;

    public ScenarioInfo() {
        this(0, false, null);
    }

    public ScenarioInfo(int i, boolean z, String str) {
        this.scenarioType = i;
        this.direction = z;
        this.bundleName = str;
    }

    ScenarioInfo(ScenarioInfo scenarioInfo) {
        if (scenarioInfo != null) {
            this.scenarioType = scenarioInfo.scenarioType;
            this.direction = scenarioInfo.direction;
            this.bundleName = scenarioInfo.bundleName;
        }
    }

    public void setScenarioType(int i) {
        this.scenarioType = i;
    }

    public int getScenarioType() {
        return this.scenarioType;
    }

    public void setDirection(boolean z) {
        this.direction = z;
    }

    public boolean getDirection() {
        return this.direction;
    }

    public void setBundleName(String str) {
        if (str != null) {
            this.bundleName = str;
        }
    }

    public String getBundleName() {
        return this.bundleName;
    }

    /* access modifiers changed from: package-private */
    public boolean marshalling(Parcel parcel) {
        if (!parcel.writeInt(this.scenarioType)) {
            HiLog.warn(LABEL, "write scenarioType failed.", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.direction)) {
            HiLog.warn(LABEL, "write direction failed.", new Object[0]);
            return false;
        } else if (parcel.writeString(this.bundleName)) {
            return true;
        } else {
            HiLog.warn(LABEL, "write bundleName failed.", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean unmarshalling(Parcel parcel) {
        this.scenarioType = parcel.readInt();
        this.direction = parcel.readBoolean();
        this.bundleName = parcel.readString();
        String str = this.bundleName;
        if (str != null && !str.isEmpty()) {
            return true;
        }
        HiLog.warn(LABEL, "read bundleName failed.", new Object[0]);
        return false;
    }
}
