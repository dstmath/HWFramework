package android.hardware.soundtrigger;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.media.AudioFormat;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.system.OsConstants;
import com.android.internal.logging.nano.MetricsProto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@SystemApi
public class SoundTrigger {
    public static final int RECOGNITION_MODE_USER_AUTHENTICATION = 4;
    public static final int RECOGNITION_MODE_USER_IDENTIFICATION = 2;
    public static final int RECOGNITION_MODE_VOICE_TRIGGER = 1;
    public static final int RECOGNITION_STATUS_ABORT = 1;
    public static final int RECOGNITION_STATUS_FAILURE = 2;
    public static final int RECOGNITION_STATUS_GET_STATE_RESPONSE = 3;
    public static final int RECOGNITION_STATUS_SUCCESS = 0;
    public static final int SERVICE_STATE_DISABLED = 1;
    public static final int SERVICE_STATE_ENABLED = 0;
    public static final int SOUNDMODEL_STATUS_UPDATED = 0;
    public static final int STATUS_BAD_VALUE = (-OsConstants.EINVAL);
    public static final int STATUS_DEAD_OBJECT = (-OsConstants.EPIPE);
    public static final int STATUS_ERROR = Integer.MIN_VALUE;
    public static final int STATUS_INVALID_OPERATION = (-OsConstants.ENOSYS);
    public static final int STATUS_NO_INIT = (-OsConstants.ENODEV);
    public static final int STATUS_OK = 0;
    public static final int STATUS_PERMISSION_DENIED = (-OsConstants.EPERM);

    public interface StatusListener {
        void onRecognition(RecognitionEvent recognitionEvent);

        void onServiceDied();

        void onServiceStateChange(int i);

        void onSoundModelUpdate(SoundModelEvent soundModelEvent);
    }

    private static native int listModules(String str, ArrayList<ModuleProperties> arrayList);

    private SoundTrigger() {
    }

    public static class ModuleProperties implements Parcelable {
        public static final Parcelable.Creator<ModuleProperties> CREATOR = new Parcelable.Creator<ModuleProperties>() {
            /* class android.hardware.soundtrigger.SoundTrigger.ModuleProperties.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ModuleProperties createFromParcel(Parcel in) {
                return ModuleProperties.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public ModuleProperties[] newArray(int size) {
                return new ModuleProperties[size];
            }
        };
        public final String description;
        @UnsupportedAppUsage
        public final int id;
        public final String implementor;
        public final int maxBufferMs;
        public final int maxKeyphrases;
        @UnsupportedAppUsage
        public final int maxSoundModels;
        public final int maxUsers;
        public final int powerConsumptionMw;
        public final int recognitionModes;
        public final boolean returnsTriggerInEvent;
        public final boolean supportsCaptureTransition;
        public final boolean supportsConcurrentCapture;
        @UnsupportedAppUsage
        public final UUID uuid;
        public final int version;

        @UnsupportedAppUsage
        ModuleProperties(int id2, String implementor2, String description2, String uuid2, int version2, int maxSoundModels2, int maxKeyphrases2, int maxUsers2, int recognitionModes2, boolean supportsCaptureTransition2, int maxBufferMs2, boolean supportsConcurrentCapture2, int powerConsumptionMw2, boolean returnsTriggerInEvent2) {
            this.id = id2;
            this.implementor = implementor2;
            this.description = description2;
            this.uuid = UUID.fromString(uuid2);
            this.version = version2;
            this.maxSoundModels = maxSoundModels2;
            this.maxKeyphrases = maxKeyphrases2;
            this.maxUsers = maxUsers2;
            this.recognitionModes = recognitionModes2;
            this.supportsCaptureTransition = supportsCaptureTransition2;
            this.maxBufferMs = maxBufferMs2;
            this.supportsConcurrentCapture = supportsConcurrentCapture2;
            this.powerConsumptionMw = powerConsumptionMw2;
            this.returnsTriggerInEvent = returnsTriggerInEvent2;
        }

        /* access modifiers changed from: private */
        public static ModuleProperties fromParcel(Parcel in) {
            return new ModuleProperties(in.readInt(), in.readString(), in.readString(), in.readString(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readByte() == 1, in.readInt(), in.readByte() == 1, in.readInt(), in.readByte() == 1);
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeString(this.implementor);
            dest.writeString(this.description);
            dest.writeString(this.uuid.toString());
            dest.writeInt(this.version);
            dest.writeInt(this.maxSoundModels);
            dest.writeInt(this.maxKeyphrases);
            dest.writeInt(this.maxUsers);
            dest.writeInt(this.recognitionModes);
            dest.writeByte(this.supportsCaptureTransition ? (byte) 1 : 0);
            dest.writeInt(this.maxBufferMs);
            dest.writeByte(this.supportsConcurrentCapture ? (byte) 1 : 0);
            dest.writeInt(this.powerConsumptionMw);
            dest.writeByte(this.returnsTriggerInEvent ? (byte) 1 : 0);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "ModuleProperties [id=" + this.id + ", implementor=" + this.implementor + ", description=" + this.description + ", uuid=" + this.uuid + ", version=" + this.version + ", maxSoundModels=" + this.maxSoundModels + ", maxKeyphrases=" + this.maxKeyphrases + ", maxUsers=" + this.maxUsers + ", recognitionModes=" + this.recognitionModes + ", supportsCaptureTransition=" + this.supportsCaptureTransition + ", maxBufferMs=" + this.maxBufferMs + ", supportsConcurrentCapture=" + this.supportsConcurrentCapture + ", powerConsumptionMw=" + this.powerConsumptionMw + ", returnsTriggerInEvent=" + this.returnsTriggerInEvent + "]";
        }
    }

    public static class SoundModel {
        public static final int TYPE_GENERIC_SOUND = 1;
        public static final int TYPE_KEYPHRASE = 0;
        public static final int TYPE_UNKNOWN = -1;
        @UnsupportedAppUsage
        public final byte[] data;
        public final int type;
        @UnsupportedAppUsage
        public final UUID uuid;
        @UnsupportedAppUsage
        public final UUID vendorUuid;

        public SoundModel(UUID uuid2, UUID vendorUuid2, int type2, byte[] data2) {
            this.uuid = uuid2;
            this.vendorUuid = vendorUuid2;
            this.type = type2;
            this.data = data2;
        }

        public int hashCode() {
            int result = ((((1 * 31) + Arrays.hashCode(this.data)) * 31) + this.type) * 31;
            UUID uuid2 = this.uuid;
            int i = 0;
            int result2 = (result + (uuid2 == null ? 0 : uuid2.hashCode())) * 31;
            UUID uuid3 = this.vendorUuid;
            if (uuid3 != null) {
                i = uuid3.hashCode();
            }
            return result2 + i;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof SoundModel)) {
                return false;
            }
            SoundModel other = (SoundModel) obj;
            if (!Arrays.equals(this.data, other.data) || this.type != other.type) {
                return false;
            }
            UUID uuid2 = this.uuid;
            if (uuid2 == null) {
                if (other.uuid != null) {
                    return false;
                }
            } else if (!uuid2.equals(other.uuid)) {
                return false;
            }
            UUID uuid3 = this.vendorUuid;
            if (uuid3 == null) {
                if (other.vendorUuid != null) {
                    return false;
                }
            } else if (!uuid3.equals(other.vendorUuid)) {
                return false;
            }
            return true;
        }
    }

    public static class Keyphrase implements Parcelable {
        public static final Parcelable.Creator<Keyphrase> CREATOR = new Parcelable.Creator<Keyphrase>() {
            /* class android.hardware.soundtrigger.SoundTrigger.Keyphrase.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Keyphrase createFromParcel(Parcel in) {
                return Keyphrase.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public Keyphrase[] newArray(int size) {
                return new Keyphrase[size];
            }
        };
        @UnsupportedAppUsage
        public final int id;
        @UnsupportedAppUsage
        public final String locale;
        @UnsupportedAppUsage
        public final int recognitionModes;
        @UnsupportedAppUsage
        public final String text;
        @UnsupportedAppUsage
        public final int[] users;

        @UnsupportedAppUsage
        public Keyphrase(int id2, int recognitionModes2, String locale2, String text2, int[] users2) {
            this.id = id2;
            this.recognitionModes = recognitionModes2;
            this.locale = locale2;
            this.text = text2;
            this.users = users2;
        }

        /* access modifiers changed from: private */
        public static Keyphrase fromParcel(Parcel in) {
            int[] users2;
            int id2 = in.readInt();
            int recognitionModes2 = in.readInt();
            String locale2 = in.readString();
            String text2 = in.readString();
            int numUsers = in.readInt();
            if (numUsers >= 0) {
                int[] users3 = new int[numUsers];
                in.readIntArray(users3);
                users2 = users3;
            } else {
                users2 = null;
            }
            return new Keyphrase(id2, recognitionModes2, locale2, text2, users2);
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeInt(this.recognitionModes);
            dest.writeString(this.locale);
            dest.writeString(this.text);
            int[] iArr = this.users;
            if (iArr != null) {
                dest.writeInt(iArr.length);
                dest.writeIntArray(this.users);
                return;
            }
            dest.writeInt(-1);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public int hashCode() {
            int i = 1 * 31;
            String str = this.text;
            int i2 = 0;
            int result = (((i + (str == null ? 0 : str.hashCode())) * 31) + this.id) * 31;
            String str2 = this.locale;
            if (str2 != null) {
                i2 = str2.hashCode();
            }
            return ((((result + i2) * 31) + this.recognitionModes) * 31) + Arrays.hashCode(this.users);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Keyphrase other = (Keyphrase) obj;
            String str = this.text;
            if (str == null) {
                if (other.text != null) {
                    return false;
                }
            } else if (!str.equals(other.text)) {
                return false;
            }
            if (this.id != other.id) {
                return false;
            }
            String str2 = this.locale;
            if (str2 == null) {
                if (other.locale != null) {
                    return false;
                }
            } else if (!str2.equals(other.locale)) {
                return false;
            }
            if (this.recognitionModes == other.recognitionModes && Arrays.equals(this.users, other.users)) {
                return true;
            }
            return false;
        }

        public String toString() {
            return "Keyphrase [id=" + this.id + ", recognitionModes=" + this.recognitionModes + ", locale=" + this.locale + ", text=" + this.text + ", users=" + Arrays.toString(this.users) + "]";
        }
    }

    public static class KeyphraseSoundModel extends SoundModel implements Parcelable {
        public static final Parcelable.Creator<KeyphraseSoundModel> CREATOR = new Parcelable.Creator<KeyphraseSoundModel>() {
            /* class android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public KeyphraseSoundModel createFromParcel(Parcel in) {
                return KeyphraseSoundModel.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public KeyphraseSoundModel[] newArray(int size) {
                return new KeyphraseSoundModel[size];
            }
        };
        @UnsupportedAppUsage
        public final Keyphrase[] keyphrases;

        @UnsupportedAppUsage
        public KeyphraseSoundModel(UUID uuid, UUID vendorUuid, byte[] data, Keyphrase[] keyphrases2) {
            super(uuid, vendorUuid, 0, data);
            this.keyphrases = keyphrases2;
        }

        /* access modifiers changed from: private */
        public static KeyphraseSoundModel fromParcel(Parcel in) {
            UUID uuid = UUID.fromString(in.readString());
            UUID vendorUuid = null;
            if (in.readInt() >= 0) {
                vendorUuid = UUID.fromString(in.readString());
            }
            return new KeyphraseSoundModel(uuid, vendorUuid, in.readBlob(), (Keyphrase[]) in.createTypedArray(Keyphrase.CREATOR));
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.uuid.toString());
            if (this.vendorUuid == null) {
                dest.writeInt(-1);
            } else {
                dest.writeInt(this.vendorUuid.toString().length());
                dest.writeString(this.vendorUuid.toString());
            }
            dest.writeBlob(this.data);
            dest.writeTypedArray(this.keyphrases, flags);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("KeyphraseSoundModel [keyphrases=");
            sb.append(Arrays.toString(this.keyphrases));
            sb.append(", uuid=");
            sb.append(this.uuid);
            sb.append(", vendorUuid=");
            sb.append(this.vendorUuid);
            sb.append(", type=");
            sb.append(this.type);
            sb.append(", data=");
            sb.append(this.data == null ? 0 : this.data.length);
            sb.append("]");
            return sb.toString();
        }

        @Override // android.hardware.soundtrigger.SoundTrigger.SoundModel
        public int hashCode() {
            return (super.hashCode() * 31) + Arrays.hashCode(this.keyphrases);
        }

        @Override // android.hardware.soundtrigger.SoundTrigger.SoundModel
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (super.equals(obj) && (obj instanceof KeyphraseSoundModel) && Arrays.equals(this.keyphrases, ((KeyphraseSoundModel) obj).keyphrases)) {
                return true;
            }
            return false;
        }
    }

    public static class GenericSoundModel extends SoundModel implements Parcelable {
        public static final Parcelable.Creator<GenericSoundModel> CREATOR = new Parcelable.Creator<GenericSoundModel>() {
            /* class android.hardware.soundtrigger.SoundTrigger.GenericSoundModel.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public GenericSoundModel createFromParcel(Parcel in) {
                return GenericSoundModel.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public GenericSoundModel[] newArray(int size) {
                return new GenericSoundModel[size];
            }
        };

        @UnsupportedAppUsage
        public GenericSoundModel(UUID uuid, UUID vendorUuid, byte[] data) {
            super(uuid, vendorUuid, 1, data);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        /* access modifiers changed from: private */
        public static GenericSoundModel fromParcel(Parcel in) {
            UUID uuid = UUID.fromString(in.readString());
            UUID vendorUuid = null;
            if (in.readInt() >= 0) {
                vendorUuid = UUID.fromString(in.readString());
            }
            return new GenericSoundModel(uuid, vendorUuid, in.readBlob());
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.uuid.toString());
            if (this.vendorUuid == null) {
                dest.writeInt(-1);
            } else {
                dest.writeInt(this.vendorUuid.toString().length());
                dest.writeString(this.vendorUuid.toString());
            }
            dest.writeBlob(this.data);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("GenericSoundModel [uuid=");
            sb.append(this.uuid);
            sb.append(", vendorUuid=");
            sb.append(this.vendorUuid);
            sb.append(", type=");
            sb.append(this.type);
            sb.append(", data=");
            sb.append(this.data == null ? 0 : this.data.length);
            sb.append("]");
            return sb.toString();
        }
    }

    public static class RecognitionEvent {
        public static final Parcelable.Creator<RecognitionEvent> CREATOR = new Parcelable.Creator<RecognitionEvent>() {
            /* class android.hardware.soundtrigger.SoundTrigger.RecognitionEvent.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public RecognitionEvent createFromParcel(Parcel in) {
                return RecognitionEvent.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public RecognitionEvent[] newArray(int size) {
                return new RecognitionEvent[size];
            }
        };
        @UnsupportedAppUsage
        public final boolean captureAvailable;
        public final int captureDelayMs;
        public final AudioFormat captureFormat;
        public final int capturePreambleMs;
        @UnsupportedAppUsage
        public final int captureSession;
        @UnsupportedAppUsage
        public final byte[] data;
        @UnsupportedAppUsage
        public final int soundModelHandle;
        @UnsupportedAppUsage
        public final int status;
        public final boolean triggerInData;

        @UnsupportedAppUsage
        public RecognitionEvent(int status2, int soundModelHandle2, boolean captureAvailable2, int captureSession2, int captureDelayMs2, int capturePreambleMs2, boolean triggerInData2, AudioFormat captureFormat2, byte[] data2) {
            this.status = status2;
            this.soundModelHandle = soundModelHandle2;
            this.captureAvailable = captureAvailable2;
            this.captureSession = captureSession2;
            this.captureDelayMs = captureDelayMs2;
            this.capturePreambleMs = capturePreambleMs2;
            this.triggerInData = triggerInData2;
            this.captureFormat = captureFormat2;
            this.data = data2;
        }

        public boolean isCaptureAvailable() {
            return this.captureAvailable;
        }

        public AudioFormat getCaptureFormat() {
            return this.captureFormat;
        }

        public int getCaptureSession() {
            return this.captureSession;
        }

        public byte[] getData() {
            return this.data;
        }

        protected static RecognitionEvent fromParcel(Parcel in) {
            AudioFormat captureFormat2;
            int status2 = in.readInt();
            int soundModelHandle2 = in.readInt();
            boolean captureAvailable2 = in.readByte() == 1;
            int captureSession2 = in.readInt();
            int captureDelayMs2 = in.readInt();
            int capturePreambleMs2 = in.readInt();
            boolean triggerInData2 = in.readByte() == 1;
            if (in.readByte() == 1) {
                captureFormat2 = new AudioFormat.Builder().setChannelMask(in.readInt()).setEncoding(in.readInt()).setSampleRate(in.readInt()).build();
            } else {
                captureFormat2 = null;
            }
            return new RecognitionEvent(status2, soundModelHandle2, captureAvailable2, captureSession2, captureDelayMs2, capturePreambleMs2, triggerInData2, captureFormat2, in.readBlob());
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.status);
            dest.writeInt(this.soundModelHandle);
            dest.writeByte(this.captureAvailable ? (byte) 1 : 0);
            dest.writeInt(this.captureSession);
            dest.writeInt(this.captureDelayMs);
            dest.writeInt(this.capturePreambleMs);
            dest.writeByte(this.triggerInData ? (byte) 1 : 0);
            if (this.captureFormat != null) {
                dest.writeByte((byte) 1);
                dest.writeInt(this.captureFormat.getSampleRate());
                dest.writeInt(this.captureFormat.getEncoding());
                dest.writeInt(this.captureFormat.getChannelMask());
            } else {
                dest.writeByte((byte) 0);
            }
            dest.writeBlob(this.data);
        }

        public int hashCode() {
            int i = 1 * 31;
            boolean z = this.captureAvailable;
            int i2 = MetricsProto.MetricsEvent.AUTOFILL_SERVICE_DISABLED_APP;
            int result = (((((((i + (z ? 1231 : 1237)) * 31) + this.captureDelayMs) * 31) + this.capturePreambleMs) * 31) + this.captureSession) * 31;
            if (!this.triggerInData) {
                i2 = 1237;
            }
            int result2 = result + i2;
            AudioFormat audioFormat = this.captureFormat;
            if (audioFormat != null) {
                result2 = (((((result2 * 31) + audioFormat.getSampleRate()) * 31) + this.captureFormat.getEncoding()) * 31) + this.captureFormat.getChannelMask();
            }
            return (((((result2 * 31) + Arrays.hashCode(this.data)) * 31) + this.soundModelHandle) * 31) + this.status;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            RecognitionEvent other = (RecognitionEvent) obj;
            if (this.captureAvailable != other.captureAvailable || this.captureDelayMs != other.captureDelayMs || this.capturePreambleMs != other.capturePreambleMs || this.captureSession != other.captureSession || !Arrays.equals(this.data, other.data) || this.soundModelHandle != other.soundModelHandle || this.status != other.status || this.triggerInData != other.triggerInData) {
                return false;
            }
            AudioFormat audioFormat = this.captureFormat;
            if (audioFormat == null) {
                if (other.captureFormat != null) {
                    return false;
                }
            } else if (other.captureFormat != null && audioFormat.getSampleRate() == other.captureFormat.getSampleRate() && this.captureFormat.getEncoding() == other.captureFormat.getEncoding() && this.captureFormat.getChannelMask() == other.captureFormat.getChannelMask()) {
                return true;
            } else {
                return false;
            }
            return true;
        }

        public String toString() {
            String str;
            String str2;
            StringBuilder sb = new StringBuilder();
            sb.append("RecognitionEvent [status=");
            sb.append(this.status);
            sb.append(", soundModelHandle=");
            sb.append(this.soundModelHandle);
            sb.append(", captureAvailable=");
            sb.append(this.captureAvailable);
            sb.append(", captureSession=");
            sb.append(this.captureSession);
            sb.append(", captureDelayMs=");
            sb.append(this.captureDelayMs);
            sb.append(", capturePreambleMs=");
            sb.append(this.capturePreambleMs);
            sb.append(", triggerInData=");
            sb.append(this.triggerInData);
            String str3 = "";
            if (this.captureFormat == null) {
                str = str3;
            } else {
                str = ", sampleRate=" + this.captureFormat.getSampleRate();
            }
            sb.append(str);
            if (this.captureFormat == null) {
                str2 = str3;
            } else {
                str2 = ", encoding=" + this.captureFormat.getEncoding();
            }
            sb.append(str2);
            if (this.captureFormat != null) {
                str3 = ", channelMask=" + this.captureFormat.getChannelMask();
            }
            sb.append(str3);
            sb.append(", data=");
            byte[] bArr = this.data;
            sb.append(bArr == null ? 0 : bArr.length);
            sb.append("]");
            return sb.toString();
        }
    }

    public static class RecognitionConfig implements Parcelable {
        public static final Parcelable.Creator<RecognitionConfig> CREATOR = new Parcelable.Creator<RecognitionConfig>() {
            /* class android.hardware.soundtrigger.SoundTrigger.RecognitionConfig.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public RecognitionConfig createFromParcel(Parcel in) {
                return RecognitionConfig.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public RecognitionConfig[] newArray(int size) {
                return new RecognitionConfig[size];
            }
        };
        public final boolean allowMultipleTriggers;
        @UnsupportedAppUsage
        public final boolean captureRequested;
        @UnsupportedAppUsage
        public final byte[] data;
        @UnsupportedAppUsage
        public final KeyphraseRecognitionExtra[] keyphrases;

        @UnsupportedAppUsage
        public RecognitionConfig(boolean captureRequested2, boolean allowMultipleTriggers2, KeyphraseRecognitionExtra[] keyphrases2, byte[] data2) {
            this.captureRequested = captureRequested2;
            this.allowMultipleTriggers = allowMultipleTriggers2;
            this.keyphrases = keyphrases2;
            this.data = data2;
        }

        /* access modifiers changed from: private */
        public static RecognitionConfig fromParcel(Parcel in) {
            boolean allowMultipleTriggers2 = false;
            boolean captureRequested2 = in.readByte() == 1;
            if (in.readByte() == 1) {
                allowMultipleTriggers2 = true;
            }
            return new RecognitionConfig(captureRequested2, allowMultipleTriggers2, (KeyphraseRecognitionExtra[]) in.createTypedArray(KeyphraseRecognitionExtra.CREATOR), in.readBlob());
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte(this.captureRequested ? (byte) 1 : 0);
            dest.writeByte(this.allowMultipleTriggers ? (byte) 1 : 0);
            dest.writeTypedArray(this.keyphrases, flags);
            dest.writeBlob(this.data);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "RecognitionConfig [captureRequested=" + this.captureRequested + ", allowMultipleTriggers=" + this.allowMultipleTriggers + ", keyphrases=" + Arrays.toString(this.keyphrases) + ", data=" + Arrays.toString(this.data) + "]";
        }
    }

    public static class ConfidenceLevel implements Parcelable {
        public static final Parcelable.Creator<ConfidenceLevel> CREATOR = new Parcelable.Creator<ConfidenceLevel>() {
            /* class android.hardware.soundtrigger.SoundTrigger.ConfidenceLevel.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ConfidenceLevel createFromParcel(Parcel in) {
                return ConfidenceLevel.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public ConfidenceLevel[] newArray(int size) {
                return new ConfidenceLevel[size];
            }
        };
        @UnsupportedAppUsage
        public final int confidenceLevel;
        @UnsupportedAppUsage
        public final int userId;

        @UnsupportedAppUsage
        public ConfidenceLevel(int userId2, int confidenceLevel2) {
            this.userId = userId2;
            this.confidenceLevel = confidenceLevel2;
        }

        /* access modifiers changed from: private */
        public static ConfidenceLevel fromParcel(Parcel in) {
            return new ConfidenceLevel(in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.userId);
            dest.writeInt(this.confidenceLevel);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public int hashCode() {
            return (((1 * 31) + this.confidenceLevel) * 31) + this.userId;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ConfidenceLevel other = (ConfidenceLevel) obj;
            if (this.confidenceLevel == other.confidenceLevel && this.userId == other.userId) {
                return true;
            }
            return false;
        }

        public String toString() {
            return "ConfidenceLevel [userId=" + this.userId + ", confidenceLevel=" + this.confidenceLevel + "]";
        }
    }

    public static class KeyphraseRecognitionExtra implements Parcelable {
        public static final Parcelable.Creator<KeyphraseRecognitionExtra> CREATOR = new Parcelable.Creator<KeyphraseRecognitionExtra>() {
            /* class android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionExtra.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public KeyphraseRecognitionExtra createFromParcel(Parcel in) {
                return KeyphraseRecognitionExtra.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public KeyphraseRecognitionExtra[] newArray(int size) {
                return new KeyphraseRecognitionExtra[size];
            }
        };
        @UnsupportedAppUsage
        public final int coarseConfidenceLevel;
        @UnsupportedAppUsage
        public final ConfidenceLevel[] confidenceLevels;
        @UnsupportedAppUsage
        public final int id;
        @UnsupportedAppUsage
        public final int recognitionModes;

        @UnsupportedAppUsage
        public KeyphraseRecognitionExtra(int id2, int recognitionModes2, int coarseConfidenceLevel2, ConfidenceLevel[] confidenceLevels2) {
            this.id = id2;
            this.recognitionModes = recognitionModes2;
            this.coarseConfidenceLevel = coarseConfidenceLevel2;
            this.confidenceLevels = confidenceLevels2;
        }

        /* access modifiers changed from: private */
        public static KeyphraseRecognitionExtra fromParcel(Parcel in) {
            return new KeyphraseRecognitionExtra(in.readInt(), in.readInt(), in.readInt(), (ConfidenceLevel[]) in.createTypedArray(ConfidenceLevel.CREATOR));
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeInt(this.recognitionModes);
            dest.writeInt(this.coarseConfidenceLevel);
            dest.writeTypedArray(this.confidenceLevels, flags);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public int hashCode() {
            return (((((((1 * 31) + Arrays.hashCode(this.confidenceLevels)) * 31) + this.id) * 31) + this.recognitionModes) * 31) + this.coarseConfidenceLevel;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            KeyphraseRecognitionExtra other = (KeyphraseRecognitionExtra) obj;
            if (Arrays.equals(this.confidenceLevels, other.confidenceLevels) && this.id == other.id && this.recognitionModes == other.recognitionModes && this.coarseConfidenceLevel == other.coarseConfidenceLevel) {
                return true;
            }
            return false;
        }

        public String toString() {
            return "KeyphraseRecognitionExtra [id=" + this.id + ", recognitionModes=" + this.recognitionModes + ", coarseConfidenceLevel=" + this.coarseConfidenceLevel + ", confidenceLevels=" + Arrays.toString(this.confidenceLevels) + "]";
        }
    }

    public static class KeyphraseRecognitionEvent extends RecognitionEvent implements Parcelable {
        public static final Parcelable.Creator<KeyphraseRecognitionEvent> CREATOR = new Parcelable.Creator<KeyphraseRecognitionEvent>() {
            /* class android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionEvent.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public KeyphraseRecognitionEvent createFromParcel(Parcel in) {
                return KeyphraseRecognitionEvent.fromParcelForKeyphrase(in);
            }

            @Override // android.os.Parcelable.Creator
            public KeyphraseRecognitionEvent[] newArray(int size) {
                return new KeyphraseRecognitionEvent[size];
            }
        };
        @UnsupportedAppUsage
        public final KeyphraseRecognitionExtra[] keyphraseExtras;

        @UnsupportedAppUsage
        public KeyphraseRecognitionEvent(int status, int soundModelHandle, boolean captureAvailable, int captureSession, int captureDelayMs, int capturePreambleMs, boolean triggerInData, AudioFormat captureFormat, byte[] data, KeyphraseRecognitionExtra[] keyphraseExtras2) {
            super(status, soundModelHandle, captureAvailable, captureSession, captureDelayMs, capturePreambleMs, triggerInData, captureFormat, data);
            this.keyphraseExtras = keyphraseExtras2;
        }

        /* access modifiers changed from: private */
        public static KeyphraseRecognitionEvent fromParcelForKeyphrase(Parcel in) {
            AudioFormat captureFormat;
            int status = in.readInt();
            int soundModelHandle = in.readInt();
            boolean captureAvailable = in.readByte() == 1;
            int captureSession = in.readInt();
            int captureDelayMs = in.readInt();
            int capturePreambleMs = in.readInt();
            boolean triggerInData = in.readByte() == 1;
            if (in.readByte() == 1) {
                captureFormat = new AudioFormat.Builder().setChannelMask(in.readInt()).setEncoding(in.readInt()).setSampleRate(in.readInt()).build();
            } else {
                captureFormat = null;
            }
            return new KeyphraseRecognitionEvent(status, soundModelHandle, captureAvailable, captureSession, captureDelayMs, capturePreambleMs, triggerInData, captureFormat, in.readBlob(), (KeyphraseRecognitionExtra[]) in.createTypedArray(KeyphraseRecognitionExtra.CREATOR));
        }

        @Override // android.os.Parcelable, android.hardware.soundtrigger.SoundTrigger.RecognitionEvent
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.status);
            dest.writeInt(this.soundModelHandle);
            dest.writeByte(this.captureAvailable ? (byte) 1 : 0);
            dest.writeInt(this.captureSession);
            dest.writeInt(this.captureDelayMs);
            dest.writeInt(this.capturePreambleMs);
            dest.writeByte(this.triggerInData ? (byte) 1 : 0);
            if (this.captureFormat != null) {
                dest.writeByte((byte) 1);
                dest.writeInt(this.captureFormat.getSampleRate());
                dest.writeInt(this.captureFormat.getEncoding());
                dest.writeInt(this.captureFormat.getChannelMask());
            } else {
                dest.writeByte((byte) 0);
            }
            dest.writeBlob(this.data);
            dest.writeTypedArray(this.keyphraseExtras, flags);
        }

        @Override // android.os.Parcelable, android.hardware.soundtrigger.SoundTrigger.RecognitionEvent
        public int describeContents() {
            return 0;
        }

        @Override // android.hardware.soundtrigger.SoundTrigger.RecognitionEvent
        public int hashCode() {
            return (super.hashCode() * 31) + Arrays.hashCode(this.keyphraseExtras);
        }

        @Override // android.hardware.soundtrigger.SoundTrigger.RecognitionEvent
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (super.equals(obj) && getClass() == obj.getClass() && Arrays.equals(this.keyphraseExtras, ((KeyphraseRecognitionEvent) obj).keyphraseExtras)) {
                return true;
            }
            return false;
        }

        @Override // android.hardware.soundtrigger.SoundTrigger.RecognitionEvent
        public String toString() {
            String str;
            String str2;
            StringBuilder sb = new StringBuilder();
            sb.append("KeyphraseRecognitionEvent [keyphraseExtras=");
            sb.append(Arrays.toString(this.keyphraseExtras));
            sb.append(", status=");
            sb.append(this.status);
            sb.append(", soundModelHandle=");
            sb.append(this.soundModelHandle);
            sb.append(", captureAvailable=");
            sb.append(this.captureAvailable);
            sb.append(", captureSession=");
            sb.append(this.captureSession);
            sb.append(", captureDelayMs=");
            sb.append(this.captureDelayMs);
            sb.append(", capturePreambleMs=");
            sb.append(this.capturePreambleMs);
            sb.append(", triggerInData=");
            sb.append(this.triggerInData);
            String str3 = "";
            if (this.captureFormat == null) {
                str = str3;
            } else {
                str = ", sampleRate=" + this.captureFormat.getSampleRate();
            }
            sb.append(str);
            if (this.captureFormat == null) {
                str2 = str3;
            } else {
                str2 = ", encoding=" + this.captureFormat.getEncoding();
            }
            sb.append(str2);
            if (this.captureFormat != null) {
                str3 = ", channelMask=" + this.captureFormat.getChannelMask();
            }
            sb.append(str3);
            sb.append(", data=");
            sb.append(this.data == null ? 0 : this.data.length);
            sb.append("]");
            return sb.toString();
        }
    }

    public static class GenericRecognitionEvent extends RecognitionEvent implements Parcelable {
        public static final Parcelable.Creator<GenericRecognitionEvent> CREATOR = new Parcelable.Creator<GenericRecognitionEvent>() {
            /* class android.hardware.soundtrigger.SoundTrigger.GenericRecognitionEvent.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public GenericRecognitionEvent createFromParcel(Parcel in) {
                return GenericRecognitionEvent.fromParcelForGeneric(in);
            }

            @Override // android.os.Parcelable.Creator
            public GenericRecognitionEvent[] newArray(int size) {
                return new GenericRecognitionEvent[size];
            }
        };

        @UnsupportedAppUsage
        public GenericRecognitionEvent(int status, int soundModelHandle, boolean captureAvailable, int captureSession, int captureDelayMs, int capturePreambleMs, boolean triggerInData, AudioFormat captureFormat, byte[] data) {
            super(status, soundModelHandle, captureAvailable, captureSession, captureDelayMs, capturePreambleMs, triggerInData, captureFormat, data);
        }

        /* access modifiers changed from: private */
        public static GenericRecognitionEvent fromParcelForGeneric(Parcel in) {
            RecognitionEvent event = RecognitionEvent.fromParcel(in);
            return new GenericRecognitionEvent(event.status, event.soundModelHandle, event.captureAvailable, event.captureSession, event.captureDelayMs, event.capturePreambleMs, event.triggerInData, event.captureFormat, event.data);
        }

        @Override // android.hardware.soundtrigger.SoundTrigger.RecognitionEvent
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            RecognitionEvent recognitionEvent = (RecognitionEvent) obj;
            return super.equals(obj);
        }

        @Override // android.hardware.soundtrigger.SoundTrigger.RecognitionEvent
        public String toString() {
            return "GenericRecognitionEvent ::" + super.toString();
        }
    }

    public static class SoundModelEvent implements Parcelable {
        public static final Parcelable.Creator<SoundModelEvent> CREATOR = new Parcelable.Creator<SoundModelEvent>() {
            /* class android.hardware.soundtrigger.SoundTrigger.SoundModelEvent.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SoundModelEvent createFromParcel(Parcel in) {
                return SoundModelEvent.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public SoundModelEvent[] newArray(int size) {
                return new SoundModelEvent[size];
            }
        };
        public final byte[] data;
        public final int soundModelHandle;
        public final int status;

        @UnsupportedAppUsage
        SoundModelEvent(int status2, int soundModelHandle2, byte[] data2) {
            this.status = status2;
            this.soundModelHandle = soundModelHandle2;
            this.data = data2;
        }

        /* access modifiers changed from: private */
        public static SoundModelEvent fromParcel(Parcel in) {
            return new SoundModelEvent(in.readInt(), in.readInt(), in.readBlob());
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.status);
            dest.writeInt(this.soundModelHandle);
            dest.writeBlob(this.data);
        }

        public int hashCode() {
            return (((((1 * 31) + Arrays.hashCode(this.data)) * 31) + this.soundModelHandle) * 31) + this.status;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SoundModelEvent other = (SoundModelEvent) obj;
            if (Arrays.equals(this.data, other.data) && this.soundModelHandle == other.soundModelHandle && this.status == other.status) {
                return true;
            }
            return false;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("SoundModelEvent [status=");
            sb.append(this.status);
            sb.append(", soundModelHandle=");
            sb.append(this.soundModelHandle);
            sb.append(", data=");
            byte[] bArr = this.data;
            sb.append(bArr == null ? 0 : bArr.length);
            sb.append("]");
            return sb.toString();
        }
    }

    static String getCurrentOpPackageName() {
        String packageName = ActivityThread.currentOpPackageName();
        if (packageName == null) {
            return "";
        }
        return packageName;
    }

    @UnsupportedAppUsage
    public static int listModules(ArrayList<ModuleProperties> modules) {
        return listModules(getCurrentOpPackageName(), modules);
    }

    @UnsupportedAppUsage
    public static SoundTriggerModule attachModule(int moduleId, StatusListener listener, Handler handler) {
        if (listener == null) {
            return null;
        }
        return new SoundTriggerModule(moduleId, listener, handler);
    }
}
