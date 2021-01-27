package ohos.dcall;

import java.util.Objects;
import ohos.ai.engine.pluginlabel.PluginLabelConstants;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public final class CallAudioDevice implements Sequenceable {
    public static final Sequenceable.Producer<CallAudioDevice> CREATOR = new Sequenceable.Producer<CallAudioDevice>() {
        /* class ohos.dcall.CallAudioDevice.AnonymousClass1 */

        public CallAudioDevice createFromParcel(Parcel parcel) {
            return new CallAudioDevice(parcel.readByte() != 0, parcel.readInt(), parcel.readInt());
        }
    };
    public static final int MASK_BLUETOOTH = 2;
    public static final int MASK_EARPIECE = 1;
    public static final int MASK_SPEAKER = 8;
    public static final int MASK_WIRED_HEADSET = 4;
    private int mAudioDevice;
    private boolean mIsMuted;
    private int mSupportedAudioDevice;

    public CallAudioDevice(boolean z, int i, int i2) {
        this.mIsMuted = z;
        this.mAudioDevice = i;
        this.mSupportedAudioDevice = i2;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CallAudioDevice)) {
            return false;
        }
        CallAudioDevice callAudioDevice = (CallAudioDevice) obj;
        if (isMuted() == callAudioDevice.isMuted() && getAudioDevice() == callAudioDevice.getAudioDevice() && getSupportedAudioDevice() == callAudioDevice.getSupportedAudioDevice()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Boolean.valueOf(this.mIsMuted), Integer.valueOf(this.mAudioDevice), Integer.valueOf(this.mSupportedAudioDevice));
    }

    public String toString() {
        return "AudioDevice isMuted: " + this.mIsMuted + ", audioDevice: " + audioDeviceToString(this.mAudioDevice) + ", supportedAudioDevice: " + audioDeviceToString(this.mSupportedAudioDevice);
    }

    public static String audioDeviceToString(int i) {
        if (i == 0) {
            return PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
        }
        StringBuffer stringBuffer = new StringBuffer();
        if ((i & 1) == 1) {
            listAppend(stringBuffer, "EARPIECE");
        }
        if ((i & 2) == 2) {
            listAppend(stringBuffer, "BLUETOOTH");
        }
        if ((i & 4) == 4) {
            listAppend(stringBuffer, "WIRED_HEADSET");
        }
        if ((i & 8) == 8) {
            listAppend(stringBuffer, "SPEAKER");
        }
        return stringBuffer.toString();
    }

    private static void listAppend(StringBuffer stringBuffer, String str) {
        if (stringBuffer.length() > 0) {
            stringBuffer.append(", ");
        }
        stringBuffer.append(str);
    }

    public boolean isMuted() {
        return this.mIsMuted;
    }

    public int getAudioDevice() {
        return this.mAudioDevice;
    }

    public int getSupportedAudioDevice() {
        return this.mSupportedAudioDevice;
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        parcel.writeByte(this.mIsMuted ? (byte) 1 : 0);
        parcel.writeInt(this.mAudioDevice);
        parcel.writeInt(this.mSupportedAudioDevice);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        boolean z = false;
        if (parcel == null) {
            return false;
        }
        if (parcel.readByte() != 0) {
            z = true;
        }
        this.mIsMuted = z;
        this.mAudioDevice = parcel.readInt();
        this.mSupportedAudioDevice = parcel.readInt();
        return true;
    }
}
