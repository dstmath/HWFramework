package android.hardware.soundtrigger;

import android.annotation.SystemApi;
import android.media.AudioFormat;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.system.OsConstants;
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

    public static class ConfidenceLevel implements Parcelable {
        public static final Parcelable.Creator<ConfidenceLevel> CREATOR = new Parcelable.Creator<ConfidenceLevel>() {
            public ConfidenceLevel createFromParcel(Parcel in) {
                return ConfidenceLevel.fromParcel(in);
            }

            public ConfidenceLevel[] newArray(int size) {
                return new ConfidenceLevel[size];
            }
        };
        public final int confidenceLevel;
        public final int userId;

        public ConfidenceLevel(int userId2, int confidenceLevel2) {
            this.userId = userId2;
            this.confidenceLevel = confidenceLevel2;
        }

        /* access modifiers changed from: private */
        public static ConfidenceLevel fromParcel(Parcel in) {
            return new ConfidenceLevel(in.readInt(), in.readInt());
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.userId);
            dest.writeInt(this.confidenceLevel);
        }

        public int describeContents() {
            return 0;
        }

        public int hashCode() {
            return (31 * ((31 * 1) + this.confidenceLevel)) + this.userId;
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

    public static class GenericRecognitionEvent extends RecognitionEvent implements Parcelable {
        public static final Parcelable.Creator<GenericRecognitionEvent> CREATOR = new Parcelable.Creator<GenericRecognitionEvent>() {
            public GenericRecognitionEvent createFromParcel(Parcel in) {
                return GenericRecognitionEvent.fromParcelForGeneric(in);
            }

            public GenericRecognitionEvent[] newArray(int size) {
                return new GenericRecognitionEvent[size];
            }
        };

        public GenericRecognitionEvent(int status, int soundModelHandle, boolean captureAvailable, int captureSession, int captureDelayMs, int capturePreambleMs, boolean triggerInData, AudioFormat captureFormat, byte[] data) {
            super(status, soundModelHandle, captureAvailable, captureSession, captureDelayMs, capturePreambleMs, triggerInData, captureFormat, data);
        }

        /* access modifiers changed from: private */
        public static GenericRecognitionEvent fromParcelForGeneric(Parcel in) {
            RecognitionEvent event = RecognitionEvent.fromParcel(in);
            GenericRecognitionEvent genericRecognitionEvent = new GenericRecognitionEvent(event.status, event.soundModelHandle, event.captureAvailable, event.captureSession, event.captureDelayMs, event.capturePreambleMs, event.triggerInData, event.captureFormat, event.data);
            return genericRecognitionEvent;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj != null && getClass() == obj.getClass()) {
                return super.equals(obj);
            }
            return false;
        }

        public String toString() {
            return "GenericRecognitionEvent ::" + super.toString();
        }
    }

    public static class GenericSoundModel extends SoundModel implements Parcelable {
        public static final Parcelable.Creator<GenericSoundModel> CREATOR = new Parcelable.Creator<GenericSoundModel>() {
            public GenericSoundModel createFromParcel(Parcel in) {
                return GenericSoundModel.fromParcel(in);
            }

            public GenericSoundModel[] newArray(int size) {
                return new GenericSoundModel[size];
            }
        };

        public GenericSoundModel(UUID uuid, UUID vendorUuid, byte[] data) {
            super(uuid, vendorUuid, 1, data);
        }

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

    public static class Keyphrase implements Parcelable {
        public static final Parcelable.Creator<Keyphrase> CREATOR = new Parcelable.Creator<Keyphrase>() {
            public Keyphrase createFromParcel(Parcel in) {
                return Keyphrase.fromParcel(in);
            }

            public Keyphrase[] newArray(int size) {
                return new Keyphrase[size];
            }
        };
        public final int id;
        public final String locale;
        public final int recognitionModes;
        public final String text;
        public final int[] users;

        public Keyphrase(int id2, int recognitionModes2, String locale2, String text2, int[] users2) {
            this.id = id2;
            this.recognitionModes = recognitionModes2;
            this.locale = locale2;
            this.text = text2;
            this.users = users2;
        }

        /* access modifiers changed from: private */
        public static Keyphrase fromParcel(Parcel in) {
            int id2 = in.readInt();
            int recognitionModes2 = in.readInt();
            String locale2 = in.readString();
            String text2 = in.readString();
            int[] users2 = null;
            int numUsers = in.readInt();
            if (numUsers >= 0) {
                users2 = new int[numUsers];
                in.readIntArray(users2);
            }
            Keyphrase keyphrase = new Keyphrase(id2, recognitionModes2, locale2, text2, users2);
            return keyphrase;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeInt(this.recognitionModes);
            dest.writeString(this.locale);
            dest.writeString(this.text);
            if (this.users != null) {
                dest.writeInt(this.users.length);
                dest.writeIntArray(this.users);
                return;
            }
            dest.writeInt(-1);
        }

        public int describeContents() {
            return 0;
        }

        public int hashCode() {
            int i = 0;
            int result = 31 * ((31 * ((31 * 1) + (this.text == null ? 0 : this.text.hashCode()))) + this.id);
            if (this.locale != null) {
                i = this.locale.hashCode();
            }
            return (31 * ((31 * (result + i)) + this.recognitionModes)) + Arrays.hashCode(this.users);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Keyphrase other = (Keyphrase) obj;
            if (this.text == null) {
                if (other.text != null) {
                    return false;
                }
            } else if (!this.text.equals(other.text)) {
                return false;
            }
            if (this.id != other.id) {
                return false;
            }
            if (this.locale == null) {
                if (other.locale != null) {
                    return false;
                }
            } else if (!this.locale.equals(other.locale)) {
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

    public static class KeyphraseRecognitionEvent extends RecognitionEvent implements Parcelable {
        public static final Parcelable.Creator<KeyphraseRecognitionEvent> CREATOR = new Parcelable.Creator<KeyphraseRecognitionEvent>() {
            public KeyphraseRecognitionEvent createFromParcel(Parcel in) {
                return KeyphraseRecognitionEvent.fromParcelForKeyphrase(in);
            }

            public KeyphraseRecognitionEvent[] newArray(int size) {
                return new KeyphraseRecognitionEvent[size];
            }
        };
        public final KeyphraseRecognitionExtra[] keyphraseExtras;

        public KeyphraseRecognitionEvent(int status, int soundModelHandle, boolean captureAvailable, int captureSession, int captureDelayMs, int capturePreambleMs, boolean triggerInData, AudioFormat captureFormat, byte[] data, KeyphraseRecognitionExtra[] keyphraseExtras2) {
            super(status, soundModelHandle, captureAvailable, captureSession, captureDelayMs, capturePreambleMs, triggerInData, captureFormat, data);
            this.keyphraseExtras = keyphraseExtras2;
        }

        /* access modifiers changed from: private */
        public static KeyphraseRecognitionEvent fromParcelForKeyphrase(Parcel in) {
            int status = in.readInt();
            int soundModelHandle = in.readInt();
            boolean captureAvailable = in.readByte() == 1;
            int captureSession = in.readInt();
            int captureDelayMs = in.readInt();
            int capturePreambleMs = in.readInt();
            boolean triggerInData = in.readByte() == 1;
            AudioFormat captureFormat = null;
            if (in.readByte() == 1) {
                captureFormat = new AudioFormat.Builder().setChannelMask(in.readInt()).setEncoding(in.readInt()).setSampleRate(in.readInt()).build();
            }
            KeyphraseRecognitionEvent keyphraseRecognitionEvent = new KeyphraseRecognitionEvent(status, soundModelHandle, captureAvailable, captureSession, captureDelayMs, capturePreambleMs, triggerInData, captureFormat, in.readBlob(), (KeyphraseRecognitionExtra[]) in.createTypedArray(KeyphraseRecognitionExtra.CREATOR));
            return keyphraseRecognitionEvent;
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
            dest.writeTypedArray(this.keyphraseExtras, flags);
        }

        public int describeContents() {
            return 0;
        }

        public int hashCode() {
            return (31 * super.hashCode()) + Arrays.hashCode(this.keyphraseExtras);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (super.equals(obj) && getClass() == obj.getClass() && Arrays.equals(this.keyphraseExtras, ((KeyphraseRecognitionEvent) obj).keyphraseExtras)) {
                return true;
            }
            return false;
        }

        public String toString() {
            String str;
            String str2;
            String str3;
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
            if (this.captureFormat == null) {
                str = "";
            } else {
                str = ", sampleRate=" + this.captureFormat.getSampleRate();
            }
            sb.append(str);
            if (this.captureFormat == null) {
                str2 = "";
            } else {
                str2 = ", encoding=" + this.captureFormat.getEncoding();
            }
            sb.append(str2);
            if (this.captureFormat == null) {
                str3 = "";
            } else {
                str3 = ", channelMask=" + this.captureFormat.getChannelMask();
            }
            sb.append(str3);
            sb.append(", data=");
            sb.append(this.data == null ? 0 : this.data.length);
            sb.append("]");
            return sb.toString();
        }
    }

    public static class KeyphraseRecognitionExtra implements Parcelable {
        public static final Parcelable.Creator<KeyphraseRecognitionExtra> CREATOR = new Parcelable.Creator<KeyphraseRecognitionExtra>() {
            public KeyphraseRecognitionExtra createFromParcel(Parcel in) {
                return KeyphraseRecognitionExtra.fromParcel(in);
            }

            public KeyphraseRecognitionExtra[] newArray(int size) {
                return new KeyphraseRecognitionExtra[size];
            }
        };
        public final int coarseConfidenceLevel;
        public final ConfidenceLevel[] confidenceLevels;
        public final int id;
        public final int recognitionModes;

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

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeInt(this.recognitionModes);
            dest.writeInt(this.coarseConfidenceLevel);
            dest.writeTypedArray(this.confidenceLevels, flags);
        }

        public int describeContents() {
            return 0;
        }

        public int hashCode() {
            return (31 * ((31 * ((31 * ((31 * 1) + Arrays.hashCode(this.confidenceLevels))) + this.id)) + this.recognitionModes)) + this.coarseConfidenceLevel;
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

    public static class KeyphraseSoundModel extends SoundModel implements Parcelable {
        public static final Parcelable.Creator<KeyphraseSoundModel> CREATOR = new Parcelable.Creator<KeyphraseSoundModel>() {
            public KeyphraseSoundModel createFromParcel(Parcel in) {
                return KeyphraseSoundModel.fromParcel(in);
            }

            public KeyphraseSoundModel[] newArray(int size) {
                return new KeyphraseSoundModel[size];
            }
        };
        public final Keyphrase[] keyphrases;

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

        public int describeContents() {
            return 0;
        }

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

        public int hashCode() {
            return (31 * super.hashCode()) + Arrays.hashCode(this.keyphrases);
        }

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

    public static class ModuleProperties implements Parcelable {
        public static final Parcelable.Creator<ModuleProperties> CREATOR = new Parcelable.Creator<ModuleProperties>() {
            public ModuleProperties createFromParcel(Parcel in) {
                return ModuleProperties.fromParcel(in);
            }

            public ModuleProperties[] newArray(int size) {
                return new ModuleProperties[size];
            }
        };
        public final String description;
        public final int id;
        public final String implementor;
        public final int maxBufferMs;
        public final int maxKeyphrases;
        public final int maxSoundModels;
        public final int maxUsers;
        public final int powerConsumptionMw;
        public final int recognitionModes;
        public final boolean returnsTriggerInEvent;
        public final boolean supportsCaptureTransition;
        public final boolean supportsConcurrentCapture;
        public final UUID uuid;
        public final int version;

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
            ModuleProperties moduleProperties = new ModuleProperties(in.readInt(), in.readString(), in.readString(), in.readString(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readByte() == 1, in.readInt(), in.readByte() == 1, in.readInt(), in.readByte() == 1);
            return moduleProperties;
        }

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

        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "ModuleProperties [id=" + this.id + ", implementor=" + this.implementor + ", description=" + this.description + ", uuid=" + this.uuid + ", version=" + this.version + ", maxSoundModels=" + this.maxSoundModels + ", maxKeyphrases=" + this.maxKeyphrases + ", maxUsers=" + this.maxUsers + ", recognitionModes=" + this.recognitionModes + ", supportsCaptureTransition=" + this.supportsCaptureTransition + ", maxBufferMs=" + this.maxBufferMs + ", supportsConcurrentCapture=" + this.supportsConcurrentCapture + ", powerConsumptionMw=" + this.powerConsumptionMw + ", returnsTriggerInEvent=" + this.returnsTriggerInEvent + "]";
        }
    }

    public static class RecognitionConfig implements Parcelable {
        public static final Parcelable.Creator<RecognitionConfig> CREATOR = new Parcelable.Creator<RecognitionConfig>() {
            public RecognitionConfig createFromParcel(Parcel in) {
                return RecognitionConfig.fromParcel(in);
            }

            public RecognitionConfig[] newArray(int size) {
                return new RecognitionConfig[size];
            }
        };
        public final boolean allowMultipleTriggers;
        public final boolean captureRequested;
        public final byte[] data;
        public final KeyphraseRecognitionExtra[] keyphrases;

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

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte(this.captureRequested ? (byte) 1 : 0);
            dest.writeByte(this.allowMultipleTriggers ? (byte) 1 : 0);
            dest.writeTypedArray(this.keyphrases, flags);
            dest.writeBlob(this.data);
        }

        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "RecognitionConfig [captureRequested=" + this.captureRequested + ", allowMultipleTriggers=" + this.allowMultipleTriggers + ", keyphrases=" + Arrays.toString(this.keyphrases) + ", data=" + Arrays.toString(this.data) + "]";
        }
    }

    public static class RecognitionEvent {
        public static final Parcelable.Creator<RecognitionEvent> CREATOR = new Parcelable.Creator<RecognitionEvent>() {
            public RecognitionEvent createFromParcel(Parcel in) {
                return RecognitionEvent.fromParcel(in);
            }

            public RecognitionEvent[] newArray(int size) {
                return new RecognitionEvent[size];
            }
        };
        public final boolean captureAvailable;
        public final int captureDelayMs;
        public final AudioFormat captureFormat;
        public final int capturePreambleMs;
        public final int captureSession;
        public final byte[] data;
        public final int soundModelHandle;
        public final int status;
        public final boolean triggerInData;

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
            int status2 = in.readInt();
            int soundModelHandle2 = in.readInt();
            boolean captureAvailable2 = in.readByte() == 1;
            int captureSession2 = in.readInt();
            int captureDelayMs2 = in.readInt();
            int capturePreambleMs2 = in.readInt();
            boolean triggerInData2 = in.readByte() == 1;
            AudioFormat captureFormat2 = null;
            if (in.readByte() == 1) {
                captureFormat2 = new AudioFormat.Builder().setChannelMask(in.readInt()).setEncoding(in.readInt()).setSampleRate(in.readInt()).build();
            }
            RecognitionEvent recognitionEvent = new RecognitionEvent(status2, soundModelHandle2, captureAvailable2, captureSession2, captureDelayMs2, capturePreambleMs2, triggerInData2, captureFormat2, in.readBlob());
            return recognitionEvent;
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
            int i = 1237;
            int result = 31 * ((31 * ((31 * ((31 * ((31 * 1) + (this.captureAvailable ? 1231 : 1237))) + this.captureDelayMs)) + this.capturePreambleMs)) + this.captureSession);
            if (this.triggerInData) {
                i = 1231;
            }
            int result2 = result + i;
            if (this.captureFormat != null) {
                result2 = (31 * ((31 * ((31 * result2) + this.captureFormat.getSampleRate())) + this.captureFormat.getEncoding())) + this.captureFormat.getChannelMask();
            }
            return (31 * ((31 * ((31 * result2) + Arrays.hashCode(this.data))) + this.soundModelHandle)) + this.status;
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
            if (this.captureFormat == null) {
                if (other.captureFormat != null) {
                    return false;
                }
            } else if (other.captureFormat != null && this.captureFormat.getSampleRate() == other.captureFormat.getSampleRate() && this.captureFormat.getEncoding() == other.captureFormat.getEncoding() && this.captureFormat.getChannelMask() == other.captureFormat.getChannelMask()) {
                return true;
            } else {
                return false;
            }
            return true;
        }

        public String toString() {
            String str;
            String str2;
            String str3;
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
            if (this.captureFormat == null) {
                str = "";
            } else {
                str = ", sampleRate=" + this.captureFormat.getSampleRate();
            }
            sb.append(str);
            if (this.captureFormat == null) {
                str2 = "";
            } else {
                str2 = ", encoding=" + this.captureFormat.getEncoding();
            }
            sb.append(str2);
            if (this.captureFormat == null) {
                str3 = "";
            } else {
                str3 = ", channelMask=" + this.captureFormat.getChannelMask();
            }
            sb.append(str3);
            sb.append(", data=");
            sb.append(this.data == null ? 0 : this.data.length);
            sb.append("]");
            return sb.toString();
        }
    }

    public static class SoundModel {
        public static final int TYPE_GENERIC_SOUND = 1;
        public static final int TYPE_KEYPHRASE = 0;
        public static final int TYPE_UNKNOWN = -1;
        public final byte[] data;
        public final int type;
        public final UUID uuid;
        public final UUID vendorUuid;

        public SoundModel(UUID uuid2, UUID vendorUuid2, int type2, byte[] data2) {
            this.uuid = uuid2;
            this.vendorUuid = vendorUuid2;
            this.type = type2;
            this.data = data2;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = 31 * ((31 * ((31 * ((31 * 1) + Arrays.hashCode(this.data))) + this.type)) + (this.uuid == null ? 0 : this.uuid.hashCode()));
            if (this.vendorUuid != null) {
                i = this.vendorUuid.hashCode();
            }
            return hashCode + i;
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
            if (this.uuid == null) {
                if (other.uuid != null) {
                    return false;
                }
            } else if (!this.uuid.equals(other.uuid)) {
                return false;
            }
            if (this.vendorUuid == null) {
                if (other.vendorUuid != null) {
                    return false;
                }
            } else if (!this.vendorUuid.equals(other.vendorUuid)) {
                return false;
            }
            return true;
        }
    }

    public static class SoundModelEvent implements Parcelable {
        public static final Parcelable.Creator<SoundModelEvent> CREATOR = new Parcelable.Creator<SoundModelEvent>() {
            public SoundModelEvent createFromParcel(Parcel in) {
                return SoundModelEvent.fromParcel(in);
            }

            public SoundModelEvent[] newArray(int size) {
                return new SoundModelEvent[size];
            }
        };
        public final byte[] data;
        public final int soundModelHandle;
        public final int status;

        SoundModelEvent(int status2, int soundModelHandle2, byte[] data2) {
            this.status = status2;
            this.soundModelHandle = soundModelHandle2;
            this.data = data2;
        }

        /* access modifiers changed from: private */
        public static SoundModelEvent fromParcel(Parcel in) {
            return new SoundModelEvent(in.readInt(), in.readInt(), in.readBlob());
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.status);
            dest.writeInt(this.soundModelHandle);
            dest.writeBlob(this.data);
        }

        public int hashCode() {
            return (31 * ((31 * ((31 * 1) + Arrays.hashCode(this.data))) + this.soundModelHandle)) + this.status;
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
            sb.append(this.data == null ? 0 : this.data.length);
            sb.append("]");
            return sb.toString();
        }
    }

    public interface StatusListener {
        void onRecognition(RecognitionEvent recognitionEvent);

        void onServiceDied();

        void onServiceStateChange(int i);

        void onSoundModelUpdate(SoundModelEvent soundModelEvent);
    }

    public static native int listModules(ArrayList<ModuleProperties> arrayList);

    private SoundTrigger() {
    }

    public static SoundTriggerModule attachModule(int moduleId, StatusListener listener, Handler handler) {
        if (listener == null) {
            return null;
        }
        return new SoundTriggerModule(moduleId, listener, handler);
    }
}
