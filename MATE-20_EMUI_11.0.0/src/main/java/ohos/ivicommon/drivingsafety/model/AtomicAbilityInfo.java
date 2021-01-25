package ohos.ivicommon.drivingsafety.model;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class AtomicAbilityInfo implements Sequenceable {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "AtomicAbilityInfo");
    private String abilityName;
    private String controlItem;
    private boolean supportDriveMode;
    private int xPos;
    private int yPos;

    public String getAbilityName() {
        return this.abilityName;
    }

    public void setAbilityName(String str) {
        this.abilityName = str;
    }

    public boolean isDriveModeEnabled() {
        return this.supportDriveMode;
    }

    public void enableDriveMode(boolean z) {
        this.supportDriveMode = z;
    }

    public String getControlItem() {
        return this.controlItem;
    }

    public void setControlItem(String str) {
        this.controlItem = str;
    }

    public void setX(int i) {
        this.xPos = i;
    }

    public int getX() {
        return this.xPos;
    }

    public void setY(int i) {
        this.yPos = i;
    }

    public int getY() {
        return this.yPos;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (!parcel.writeString(this.abilityName)) {
            HiLog.error(TAG, "write abilityName failed", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.supportDriveMode)) {
            HiLog.error(TAG, "write supportDriveMode failed", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.controlItem)) {
            HiLog.error(TAG, "write controlItem failed", new Object[0]);
            return false;
        } else if (!parcel.writeInt(this.xPos)) {
            HiLog.error(TAG, "write xPos failed", new Object[0]);
            return false;
        } else if (parcel.writeInt(this.yPos)) {
            return true;
        } else {
            HiLog.error(TAG, "write yPos failed", new Object[0]);
            return false;
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.abilityName = parcel.readString();
        this.supportDriveMode = parcel.readBoolean();
        this.controlItem = parcel.readString();
        this.xPos = parcel.readInt();
        this.yPos = parcel.readInt();
        return true;
    }

    public String toString() {
        return "AtomicAbilityInfo [abilityName=" + this.abilityName + ", supportDriveMode=" + this.supportDriveMode + ", controlItem=" + this.controlItem + ", x=" + this.xPos + ", y=" + this.yPos + "]";
    }
}
