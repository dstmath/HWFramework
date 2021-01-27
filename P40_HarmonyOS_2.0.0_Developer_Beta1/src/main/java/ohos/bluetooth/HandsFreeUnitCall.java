package ohos.bluetooth;

import java.util.UUID;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class HandsFreeUnitCall implements Sequenceable {
    public static final int CALL_STATE_ACTIVE = 0;
    public static final int CALL_STATE_ALERTING = 3;
    public static final int CALL_STATE_DIALING = 2;
    public static final int CALL_STATE_HELD_BY_RESPONSE_AND_HOLD = 6;
    public static final int CALL_STATE_HOLD = 1;
    public static final int CALL_STATE_INCOMING = 4;
    public static final int CALL_STATE_TERMINATED = 7;
    public static final int CALL_STATE_WAITING = 5;
    private static final int NOT_VALID_ID = -1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "HandsFreeUnitCall");
    private int mId;
    private boolean mInBandRing;
    private boolean mIsMeeting;
    private String mNumber;
    private boolean mOutCalling;
    private BluetoothRemoteDevice mRemoteDevice;
    private int mState;
    private UUID mUuid;

    public boolean unmarshalling(Parcel parcel) {
        return true;
    }

    HandsFreeUnitCall() {
        this.mId = 0;
        this.mUuid = null;
        this.mState = 0;
        this.mNumber = null;
        this.mIsMeeting = false;
        this.mOutCalling = false;
        this.mInBandRing = false;
        this.mRemoteDevice = null;
        this.mId = -1;
        this.mUuid = null;
        this.mState = 0;
        this.mNumber = null;
        this.mIsMeeting = false;
        this.mOutCalling = false;
        this.mInBandRing = false;
        this.mRemoteDevice = new BluetoothRemoteDevice();
    }

    HandsFreeUnitCall(int i, UUID uuid, int i2, String str, boolean z, boolean z2, boolean z3, BluetoothRemoteDevice bluetoothRemoteDevice) {
        this.mId = 0;
        this.mUuid = null;
        this.mState = 0;
        this.mNumber = null;
        this.mIsMeeting = false;
        this.mOutCalling = false;
        this.mInBandRing = false;
        this.mRemoteDevice = null;
        this.mId = i;
        this.mUuid = uuid;
        this.mState = i2;
        this.mNumber = str;
        this.mIsMeeting = z;
        this.mOutCalling = z2;
        this.mInBandRing = z3;
        this.mRemoteDevice = bluetoothRemoteDevice;
    }

    public int getCallId() {
        return this.mId;
    }

    public UUID getCallUuid() {
        return this.mUuid;
    }

    public int getCallState() {
        return this.mState;
    }

    public String getCallNumber() {
        return this.mNumber;
    }

    public boolean isMeeting() {
        return this.mIsMeeting;
    }

    public boolean isOutCalling() {
        return this.mOutCalling;
    }

    public boolean isInBandRing() {
        return this.mInBandRing;
    }

    public BluetoothRemoteDevice getRemoteDevice() {
        return this.mRemoteDevice;
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeSequenceable(this.mRemoteDevice);
        parcel.writeInt(this.mId);
        parcel.writeString(this.mUuid.toString());
        parcel.writeInt(this.mState);
        parcel.writeString(this.mNumber);
        parcel.writeBoolean(this.mIsMeeting);
        parcel.writeBoolean(this.mOutCalling);
        parcel.writeBoolean(this.mInBandRing);
        return true;
    }

    public void unmarshllingFromSa(Parcel parcel) {
        try {
            this.mRemoteDevice = new BluetoothRemoteDevice(parcel.readString());
        } catch (IllegalArgumentException unused) {
            HiLog.error(TAG, "wrong device addr in call info", new Object[0]);
            this.mRemoteDevice = new BluetoothRemoteDevice();
        }
        this.mId = parcel.readInt();
        try {
            this.mUuid = UUID.fromString(parcel.readString());
        } catch (IllegalArgumentException unused2) {
            HiLog.error(TAG, "wrong uuid in call info", new Object[0]);
            this.mUuid = null;
        }
        this.mState = parcel.readInt();
        this.mNumber = parcel.readString();
        this.mIsMeeting = parcel.readBoolean();
        this.mOutCalling = parcel.readBoolean();
        this.mInBandRing = parcel.readBoolean();
    }
}
