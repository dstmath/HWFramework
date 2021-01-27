package ohos.ivicommon.drivingsafety.model;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.MessageParcel;

public class AtomicAbilityInfo {
    private static final HiLogLabel TAG = new HiLogLabel(3, DrivingSafetyConst.IVI_DRIVING, "AtomicAbilityInfo");
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

    public boolean marshalling(MessageParcel messageParcel) {
        if (!messageParcel.writeString(this.abilityName)) {
            HiLog.error(TAG, "write abilityName failed", new Object[0]);
            return false;
        } else if (!messageParcel.writeBoolean(this.supportDriveMode)) {
            HiLog.error(TAG, "write supportDriveMode failed", new Object[0]);
            return false;
        } else if (!messageParcel.writeString(this.controlItem)) {
            HiLog.error(TAG, "write controlItem failed", new Object[0]);
            return false;
        } else if (!messageParcel.writeInt(this.xPos)) {
            HiLog.error(TAG, "write xPos failed", new Object[0]);
            return false;
        } else if (messageParcel.writeInt(this.yPos)) {
            return true;
        } else {
            HiLog.error(TAG, "write yPos failed", new Object[0]);
            return false;
        }
    }

    public boolean unmarshalling(MessageParcel messageParcel) {
        this.abilityName = messageParcel.readString();
        this.supportDriveMode = messageParcel.readBoolean();
        this.controlItem = messageParcel.readString();
        this.xPos = messageParcel.readInt();
        this.yPos = messageParcel.readInt();
        return true;
    }

    public String toString() {
        return "AtomicAbilityInfo [abilityName=" + this.abilityName + ", supportDriveMode=" + this.supportDriveMode + ", controlItem=" + this.controlItem + ", x=" + this.xPos + ", y=" + this.yPos + "]";
    }
}
