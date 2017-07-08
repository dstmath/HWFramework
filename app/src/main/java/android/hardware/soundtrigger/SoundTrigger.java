package android.hardware.soundtrigger;

import android.media.AudioFormat;
import android.media.AudioFormat.Builder;
import android.net.ProxyInfo;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

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
    public static final int STATUS_BAD_VALUE = 0;
    public static final int STATUS_DEAD_OBJECT = 0;
    public static final int STATUS_ERROR = Integer.MIN_VALUE;
    public static final int STATUS_INVALID_OPERATION = 0;
    public static final int STATUS_NO_INIT = 0;
    public static final int STATUS_OK = 0;
    public static final int STATUS_PERMISSION_DENIED = 0;

    public static class ConfidenceLevel implements Parcelable {
        public static final Creator<ConfidenceLevel> CREATOR = null;
        public final int confidenceLevel;
        public final int userId;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.soundtrigger.SoundTrigger.ConfidenceLevel.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.soundtrigger.SoundTrigger.ConfidenceLevel.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.soundtrigger.SoundTrigger.ConfidenceLevel.<clinit>():void");
        }

        public ConfidenceLevel(int userId, int confidenceLevel) {
            this.userId = userId;
            this.confidenceLevel = confidenceLevel;
        }

        private static ConfidenceLevel fromParcel(Parcel in) {
            return new ConfidenceLevel(in.readInt(), in.readInt());
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.userId);
            dest.writeInt(this.confidenceLevel);
        }

        public int describeContents() {
            return SoundTrigger.STATUS_DEAD_OBJECT;
        }

        public int hashCode() {
            return ((this.confidenceLevel + 31) * 31) + this.userId;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ConfidenceLevel other = (ConfidenceLevel) obj;
            return this.confidenceLevel == other.confidenceLevel && this.userId == other.userId;
        }

        public String toString() {
            return "ConfidenceLevel [userId=" + this.userId + ", confidenceLevel=" + this.confidenceLevel + "]";
        }
    }

    public static class RecognitionEvent implements Parcelable {
        public static final Creator<RecognitionEvent> CREATOR = null;
        public final boolean captureAvailable;
        public final int captureDelayMs;
        public AudioFormat captureFormat;
        public final int capturePreambleMs;
        public final int captureSession;
        public final byte[] data;
        public final int soundModelHandle;
        public final int status;
        public final boolean triggerInData;

        /* renamed from: android.hardware.soundtrigger.SoundTrigger.RecognitionEvent.1 */
        static class AnonymousClass1 implements Creator<RecognitionEvent> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m73createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public RecognitionEvent createFromParcel(Parcel in) {
                return RecognitionEvent.fromParcel(in);
            }

            public /* bridge */ /* synthetic */ Object[] m74newArray(int size) {
                return newArray(size);
            }

            public RecognitionEvent[] newArray(int size) {
                return new RecognitionEvent[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.soundtrigger.SoundTrigger.RecognitionEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.soundtrigger.SoundTrigger.RecognitionEvent.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.soundtrigger.SoundTrigger.RecognitionEvent.<clinit>():void");
        }

        public RecognitionEvent(int status, int soundModelHandle, boolean captureAvailable, int captureSession, int captureDelayMs, int capturePreambleMs, boolean triggerInData, AudioFormat captureFormat, byte[] data) {
            this.status = status;
            this.soundModelHandle = soundModelHandle;
            this.captureAvailable = captureAvailable;
            this.captureSession = captureSession;
            this.captureDelayMs = captureDelayMs;
            this.capturePreambleMs = capturePreambleMs;
            this.triggerInData = triggerInData;
            this.captureFormat = captureFormat;
            this.data = data;
        }

        protected static RecognitionEvent fromParcel(Parcel in) {
            int status = in.readInt();
            int soundModelHandle = in.readInt();
            boolean captureAvailable = in.readByte() == (byte) 1;
            int captureSession = in.readInt();
            int captureDelayMs = in.readInt();
            int capturePreambleMs = in.readInt();
            boolean triggerInData = in.readByte() == (byte) 1;
            AudioFormat captureFormat = null;
            if (in.readByte() == (byte) 1) {
                captureFormat = new Builder().setChannelMask(in.readInt()).setEncoding(in.readInt()).setSampleRate(in.readInt()).build();
            }
            return new RecognitionEvent(status, soundModelHandle, captureAvailable, captureSession, captureDelayMs, capturePreambleMs, triggerInData, captureFormat, in.readBlob());
        }

        public int describeContents() {
            return SoundTrigger.STATUS_DEAD_OBJECT;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            dest.writeInt(this.status);
            dest.writeInt(this.soundModelHandle);
            if (this.captureAvailable) {
                i = SoundTrigger.SERVICE_STATE_DISABLED;
            } else {
                i = SoundTrigger.STATUS_DEAD_OBJECT;
            }
            dest.writeByte((byte) i);
            dest.writeInt(this.captureSession);
            dest.writeInt(this.captureDelayMs);
            dest.writeInt(this.capturePreambleMs);
            if (this.triggerInData) {
                i = SoundTrigger.SERVICE_STATE_DISABLED;
            } else {
                i = SoundTrigger.STATUS_DEAD_OBJECT;
            }
            dest.writeByte((byte) i);
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
            int i;
            int i2 = 1231;
            if (this.captureAvailable) {
                i = 1231;
            } else {
                i = 1237;
            }
            i = (((((((i + 31) * 31) + this.captureDelayMs) * 31) + this.capturePreambleMs) * 31) + this.captureSession) * 31;
            if (!this.triggerInData) {
                i2 = 1237;
            }
            int result = i + i2;
            if (this.captureFormat != null) {
                result = (((((result * 31) + this.captureFormat.getSampleRate()) * 31) + this.captureFormat.getEncoding()) * 31) + this.captureFormat.getChannelMask();
            }
            return (((((result * 31) + Arrays.hashCode(this.data)) * 31) + this.soundModelHandle) * 31) + this.status;
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
            if (this.captureFormat != null) {
                return other.captureFormat != null && this.captureFormat.getSampleRate() == other.captureFormat.getSampleRate() && this.captureFormat.getEncoding() == other.captureFormat.getEncoding() && this.captureFormat.getChannelMask() == other.captureFormat.getChannelMask();
            } else {
                if (other.captureFormat != null) {
                    return false;
                }
            }
        }

        public String toString() {
            String str;
            int i;
            StringBuilder append = new StringBuilder().append("RecognitionEvent [status=").append(this.status).append(", soundModelHandle=").append(this.soundModelHandle).append(", captureAvailable=").append(this.captureAvailable).append(", captureSession=").append(this.captureSession).append(", captureDelayMs=").append(this.captureDelayMs).append(", capturePreambleMs=").append(this.capturePreambleMs).append(", triggerInData=").append(this.triggerInData);
            if (this.captureFormat == null) {
                str = ProxyInfo.LOCAL_EXCL_LIST;
            } else {
                str = ", sampleRate=" + this.captureFormat.getSampleRate();
            }
            append = append.append(str);
            if (this.captureFormat == null) {
                str = ProxyInfo.LOCAL_EXCL_LIST;
            } else {
                str = ", encoding=" + this.captureFormat.getEncoding();
            }
            append = append.append(str);
            if (this.captureFormat == null) {
                str = ProxyInfo.LOCAL_EXCL_LIST;
            } else {
                str = ", channelMask=" + this.captureFormat.getChannelMask();
            }
            append = append.append(str).append(", data=");
            if (this.data == null) {
                i = SoundTrigger.STATUS_DEAD_OBJECT;
            } else {
                i = this.data.length;
            }
            return append.append(i).append("]").toString();
        }
    }

    public static class GenericRecognitionEvent extends RecognitionEvent {
        public static final Creator<GenericRecognitionEvent> CREATOR = null;

        /* renamed from: android.hardware.soundtrigger.SoundTrigger.GenericRecognitionEvent.1 */
        static class AnonymousClass1 implements Creator<GenericRecognitionEvent> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m75createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public GenericRecognitionEvent createFromParcel(Parcel in) {
                return GenericRecognitionEvent.fromParcelForGeneric(in);
            }

            public /* bridge */ /* synthetic */ Object[] m76newArray(int size) {
                return newArray(size);
            }

            public GenericRecognitionEvent[] newArray(int size) {
                return new GenericRecognitionEvent[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.soundtrigger.SoundTrigger.GenericRecognitionEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.soundtrigger.SoundTrigger.GenericRecognitionEvent.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.soundtrigger.SoundTrigger.GenericRecognitionEvent.<clinit>():void");
        }

        public GenericRecognitionEvent(int status, int soundModelHandle, boolean captureAvailable, int captureSession, int captureDelayMs, int capturePreambleMs, boolean triggerInData, AudioFormat captureFormat, byte[] data) {
            super(status, soundModelHandle, captureAvailable, captureSession, captureDelayMs, capturePreambleMs, triggerInData, captureFormat, data);
        }

        private static GenericRecognitionEvent fromParcelForGeneric(Parcel in) {
            RecognitionEvent event = RecognitionEvent.fromParcel(in);
            return new GenericRecognitionEvent(event.status, event.soundModelHandle, event.captureAvailable, event.captureSession, event.captureDelayMs, event.capturePreambleMs, event.triggerInData, event.captureFormat, event.data);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            RecognitionEvent other = (RecognitionEvent) obj;
            return super.equals(obj);
        }

        public String toString() {
            return "GenericRecognitionEvent ::" + super.toString();
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

        public SoundModel(UUID uuid, UUID vendorUuid, int type, byte[] data) {
            this.uuid = uuid;
            this.vendorUuid = vendorUuid;
            this.type = type;
            this.data = data;
        }

        public int hashCode() {
            int i = TYPE_KEYPHRASE;
            int hashCode = (((((Arrays.hashCode(this.data) + 31) * 31) + this.type) * 31) + (this.uuid == null ? TYPE_KEYPHRASE : this.uuid.hashCode())) * 31;
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

    public static class GenericSoundModel extends SoundModel implements Parcelable {
        public static final Creator<GenericSoundModel> CREATOR = null;

        /* renamed from: android.hardware.soundtrigger.SoundTrigger.GenericSoundModel.1 */
        static class AnonymousClass1 implements Creator<GenericSoundModel> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m77createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public GenericSoundModel createFromParcel(Parcel in) {
                return GenericSoundModel.fromParcel(in);
            }

            public /* bridge */ /* synthetic */ Object[] m78newArray(int size) {
                return newArray(size);
            }

            public GenericSoundModel[] newArray(int size) {
                return new GenericSoundModel[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.soundtrigger.SoundTrigger.GenericSoundModel.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.soundtrigger.SoundTrigger.GenericSoundModel.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.soundtrigger.SoundTrigger.GenericSoundModel.<clinit>():void");
        }

        public GenericSoundModel(UUID uuid, UUID vendorUuid, byte[] data) {
            super(uuid, vendorUuid, SoundTrigger.SERVICE_STATE_DISABLED, data);
        }

        public int describeContents() {
            return SoundTrigger.STATUS_DEAD_OBJECT;
        }

        private static GenericSoundModel fromParcel(Parcel in) {
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
            return "GenericSoundModel [uuid=" + this.uuid + ", vendorUuid=" + this.vendorUuid + ", type=" + this.type + ", data=" + (this.data == null ? SoundTrigger.STATUS_DEAD_OBJECT : this.data.length) + "]";
        }
    }

    public static class Keyphrase implements Parcelable {
        public static final Creator<Keyphrase> CREATOR = null;
        public final int id;
        public final String locale;
        public final int recognitionModes;
        public final String text;
        public final int[] users;

        /* renamed from: android.hardware.soundtrigger.SoundTrigger.Keyphrase.1 */
        static class AnonymousClass1 implements Creator<Keyphrase> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m79createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public Keyphrase createFromParcel(Parcel in) {
                return Keyphrase.fromParcel(in);
            }

            public /* bridge */ /* synthetic */ Object[] m80newArray(int size) {
                return newArray(size);
            }

            public Keyphrase[] newArray(int size) {
                return new Keyphrase[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.soundtrigger.SoundTrigger.Keyphrase.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.soundtrigger.SoundTrigger.Keyphrase.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.soundtrigger.SoundTrigger.Keyphrase.<clinit>():void");
        }

        public Keyphrase(int id, int recognitionModes, String locale, String text, int[] users) {
            this.id = id;
            this.recognitionModes = recognitionModes;
            this.locale = locale;
            this.text = text;
            this.users = users;
        }

        private static Keyphrase fromParcel(Parcel in) {
            int id = in.readInt();
            int recognitionModes = in.readInt();
            String locale = in.readString();
            String text = in.readString();
            int[] iArr = null;
            int numUsers = in.readInt();
            if (numUsers >= 0) {
                iArr = new int[numUsers];
                in.readIntArray(iArr);
            }
            return new Keyphrase(id, recognitionModes, locale, text, iArr);
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
            return SoundTrigger.STATUS_DEAD_OBJECT;
        }

        public int hashCode() {
            int i = SoundTrigger.STATUS_DEAD_OBJECT;
            int hashCode = ((((this.text == null ? SoundTrigger.STATUS_DEAD_OBJECT : this.text.hashCode()) + 31) * 31) + this.id) * 31;
            if (this.locale != null) {
                i = this.locale.hashCode();
            }
            return ((((hashCode + i) * 31) + this.recognitionModes) * 31) + Arrays.hashCode(this.users);
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
            return this.recognitionModes == other.recognitionModes && Arrays.equals(this.users, other.users);
        }

        public String toString() {
            return "Keyphrase [id=" + this.id + ", recognitionModes=" + this.recognitionModes + ", locale=" + this.locale + ", text=" + this.text + ", users=" + Arrays.toString(this.users) + "]";
        }
    }

    public static class KeyphraseRecognitionEvent extends RecognitionEvent {
        public static final Creator<KeyphraseRecognitionEvent> CREATOR = null;
        public final KeyphraseRecognitionExtra[] keyphraseExtras;

        /* renamed from: android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionEvent.1 */
        static class AnonymousClass1 implements Creator<KeyphraseRecognitionEvent> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m81createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public KeyphraseRecognitionEvent createFromParcel(Parcel in) {
                return KeyphraseRecognitionEvent.fromParcelForKeyphrase(in);
            }

            public /* bridge */ /* synthetic */ Object[] m82newArray(int size) {
                return newArray(size);
            }

            public KeyphraseRecognitionEvent[] newArray(int size) {
                return new KeyphraseRecognitionEvent[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionEvent.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionEvent.<clinit>():void");
        }

        public KeyphraseRecognitionEvent(int status, int soundModelHandle, boolean captureAvailable, int captureSession, int captureDelayMs, int capturePreambleMs, boolean triggerInData, AudioFormat captureFormat, byte[] data, KeyphraseRecognitionExtra[] keyphraseExtras) {
            super(status, soundModelHandle, captureAvailable, captureSession, captureDelayMs, capturePreambleMs, triggerInData, captureFormat, data);
            this.keyphraseExtras = keyphraseExtras;
        }

        private static KeyphraseRecognitionEvent fromParcelForKeyphrase(Parcel in) {
            int status = in.readInt();
            int soundModelHandle = in.readInt();
            boolean captureAvailable = in.readByte() == (byte) 1;
            int captureSession = in.readInt();
            int captureDelayMs = in.readInt();
            int capturePreambleMs = in.readInt();
            boolean triggerInData = in.readByte() == (byte) 1;
            AudioFormat captureFormat = null;
            if (in.readByte() == (byte) 1) {
                captureFormat = new Builder().setChannelMask(in.readInt()).setEncoding(in.readInt()).setSampleRate(in.readInt()).build();
            }
            return new KeyphraseRecognitionEvent(status, soundModelHandle, captureAvailable, captureSession, captureDelayMs, capturePreambleMs, triggerInData, captureFormat, in.readBlob(), (KeyphraseRecognitionExtra[]) in.createTypedArray(KeyphraseRecognitionExtra.CREATOR));
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            dest.writeInt(this.status);
            dest.writeInt(this.soundModelHandle);
            if (this.captureAvailable) {
                i = SoundTrigger.SERVICE_STATE_DISABLED;
            } else {
                i = SoundTrigger.STATUS_DEAD_OBJECT;
            }
            dest.writeByte((byte) i);
            dest.writeInt(this.captureSession);
            dest.writeInt(this.captureDelayMs);
            dest.writeInt(this.capturePreambleMs);
            if (this.triggerInData) {
                i = SoundTrigger.SERVICE_STATE_DISABLED;
            } else {
                i = SoundTrigger.STATUS_DEAD_OBJECT;
            }
            dest.writeByte((byte) i);
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
            return SoundTrigger.STATUS_DEAD_OBJECT;
        }

        public int hashCode() {
            return (super.hashCode() * 31) + Arrays.hashCode(this.keyphraseExtras);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj) || getClass() != obj.getClass()) {
                return false;
            }
            return Arrays.equals(this.keyphraseExtras, ((KeyphraseRecognitionEvent) obj).keyphraseExtras);
        }

        public String toString() {
            String str;
            int i;
            StringBuilder append = new StringBuilder().append("KeyphraseRecognitionEvent [keyphraseExtras=").append(Arrays.toString(this.keyphraseExtras)).append(", status=").append(this.status).append(", soundModelHandle=").append(this.soundModelHandle).append(", captureAvailable=").append(this.captureAvailable).append(", captureSession=").append(this.captureSession).append(", captureDelayMs=").append(this.captureDelayMs).append(", capturePreambleMs=").append(this.capturePreambleMs).append(", triggerInData=").append(this.triggerInData);
            if (this.captureFormat == null) {
                str = ProxyInfo.LOCAL_EXCL_LIST;
            } else {
                str = ", sampleRate=" + this.captureFormat.getSampleRate();
            }
            append = append.append(str);
            if (this.captureFormat == null) {
                str = ProxyInfo.LOCAL_EXCL_LIST;
            } else {
                str = ", encoding=" + this.captureFormat.getEncoding();
            }
            append = append.append(str);
            if (this.captureFormat == null) {
                str = ProxyInfo.LOCAL_EXCL_LIST;
            } else {
                str = ", channelMask=" + this.captureFormat.getChannelMask();
            }
            append = append.append(str).append(", data=");
            if (this.data == null) {
                i = SoundTrigger.STATUS_DEAD_OBJECT;
            } else {
                i = this.data.length;
            }
            return append.append(i).append("]").toString();
        }
    }

    public static class KeyphraseRecognitionExtra implements Parcelable {
        public static final Creator<KeyphraseRecognitionExtra> CREATOR = null;
        public final int coarseConfidenceLevel;
        public final ConfidenceLevel[] confidenceLevels;
        public final int id;
        public final int recognitionModes;

        /* renamed from: android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionExtra.1 */
        static class AnonymousClass1 implements Creator<KeyphraseRecognitionExtra> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m83createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public KeyphraseRecognitionExtra createFromParcel(Parcel in) {
                return KeyphraseRecognitionExtra.fromParcel(in);
            }

            public /* bridge */ /* synthetic */ Object[] m84newArray(int size) {
                return newArray(size);
            }

            public KeyphraseRecognitionExtra[] newArray(int size) {
                return new KeyphraseRecognitionExtra[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionExtra.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionExtra.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionExtra.<clinit>():void");
        }

        public KeyphraseRecognitionExtra(int id, int recognitionModes, int coarseConfidenceLevel, ConfidenceLevel[] confidenceLevels) {
            this.id = id;
            this.recognitionModes = recognitionModes;
            this.coarseConfidenceLevel = coarseConfidenceLevel;
            this.confidenceLevels = confidenceLevels;
        }

        private static KeyphraseRecognitionExtra fromParcel(Parcel in) {
            return new KeyphraseRecognitionExtra(in.readInt(), in.readInt(), in.readInt(), (ConfidenceLevel[]) in.createTypedArray(ConfidenceLevel.CREATOR));
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeInt(this.recognitionModes);
            dest.writeInt(this.coarseConfidenceLevel);
            dest.writeTypedArray(this.confidenceLevels, flags);
        }

        public int describeContents() {
            return SoundTrigger.STATUS_DEAD_OBJECT;
        }

        public int hashCode() {
            return ((((((Arrays.hashCode(this.confidenceLevels) + 31) * 31) + this.id) * 31) + this.recognitionModes) * 31) + this.coarseConfidenceLevel;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            KeyphraseRecognitionExtra other = (KeyphraseRecognitionExtra) obj;
            return Arrays.equals(this.confidenceLevels, other.confidenceLevels) && this.id == other.id && this.recognitionModes == other.recognitionModes && this.coarseConfidenceLevel == other.coarseConfidenceLevel;
        }

        public String toString() {
            return "KeyphraseRecognitionExtra [id=" + this.id + ", recognitionModes=" + this.recognitionModes + ", coarseConfidenceLevel=" + this.coarseConfidenceLevel + ", confidenceLevels=" + Arrays.toString(this.confidenceLevels) + "]";
        }
    }

    public static class KeyphraseSoundModel extends SoundModel implements Parcelable {
        public static final Creator<KeyphraseSoundModel> CREATOR = null;
        public final Keyphrase[] keyphrases;

        /* renamed from: android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel.1 */
        static class AnonymousClass1 implements Creator<KeyphraseSoundModel> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m85createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public KeyphraseSoundModel createFromParcel(Parcel in) {
                return KeyphraseSoundModel.fromParcel(in);
            }

            public /* bridge */ /* synthetic */ Object[] m86newArray(int size) {
                return newArray(size);
            }

            public KeyphraseSoundModel[] newArray(int size) {
                return new KeyphraseSoundModel[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel.<clinit>():void");
        }

        public KeyphraseSoundModel(UUID uuid, UUID vendorUuid, byte[] data, Keyphrase[] keyphrases) {
            super(uuid, vendorUuid, SoundTrigger.STATUS_DEAD_OBJECT, data);
            this.keyphrases = keyphrases;
        }

        private static KeyphraseSoundModel fromParcel(Parcel in) {
            UUID uuid = UUID.fromString(in.readString());
            UUID vendorUuid = null;
            if (in.readInt() >= 0) {
                vendorUuid = UUID.fromString(in.readString());
            }
            return new KeyphraseSoundModel(uuid, vendorUuid, in.readBlob(), (Keyphrase[]) in.createTypedArray(Keyphrase.CREATOR));
        }

        public int describeContents() {
            return SoundTrigger.STATUS_DEAD_OBJECT;
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
            return "KeyphraseSoundModel [keyphrases=" + Arrays.toString(this.keyphrases) + ", uuid=" + this.uuid + ", vendorUuid=" + this.vendorUuid + ", type=" + this.type + ", data=" + (this.data == null ? SoundTrigger.STATUS_DEAD_OBJECT : this.data.length) + "]";
        }

        public int hashCode() {
            return (super.hashCode() * 31) + Arrays.hashCode(this.keyphrases);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj) || !(obj instanceof KeyphraseSoundModel)) {
                return false;
            }
            return Arrays.equals(this.keyphrases, ((KeyphraseSoundModel) obj).keyphrases);
        }
    }

    public static class ModuleProperties implements Parcelable {
        public static final Creator<ModuleProperties> CREATOR = null;
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

        /* renamed from: android.hardware.soundtrigger.SoundTrigger.ModuleProperties.1 */
        static class AnonymousClass1 implements Creator<ModuleProperties> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m87createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public ModuleProperties createFromParcel(Parcel in) {
                return ModuleProperties.fromParcel(in);
            }

            public /* bridge */ /* synthetic */ Object[] m88newArray(int size) {
                return newArray(size);
            }

            public ModuleProperties[] newArray(int size) {
                return new ModuleProperties[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.soundtrigger.SoundTrigger.ModuleProperties.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.soundtrigger.SoundTrigger.ModuleProperties.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.soundtrigger.SoundTrigger.ModuleProperties.<clinit>():void");
        }

        ModuleProperties(int id, String implementor, String description, String uuid, int version, int maxSoundModels, int maxKeyphrases, int maxUsers, int recognitionModes, boolean supportsCaptureTransition, int maxBufferMs, boolean supportsConcurrentCapture, int powerConsumptionMw, boolean returnsTriggerInEvent) {
            this.id = id;
            this.implementor = implementor;
            this.description = description;
            this.uuid = UUID.fromString(uuid);
            this.version = version;
            this.maxSoundModels = maxSoundModels;
            this.maxKeyphrases = maxKeyphrases;
            this.maxUsers = maxUsers;
            this.recognitionModes = recognitionModes;
            this.supportsCaptureTransition = supportsCaptureTransition;
            this.maxBufferMs = maxBufferMs;
            this.supportsConcurrentCapture = supportsConcurrentCapture;
            this.powerConsumptionMw = powerConsumptionMw;
            this.returnsTriggerInEvent = returnsTriggerInEvent;
        }

        private static ModuleProperties fromParcel(Parcel in) {
            return new ModuleProperties(in.readInt(), in.readString(), in.readString(), in.readString(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readByte() == SoundTrigger.SERVICE_STATE_DISABLED, in.readInt(), in.readByte() == SoundTrigger.SERVICE_STATE_DISABLED, in.readInt(), in.readByte() == SoundTrigger.SERVICE_STATE_DISABLED);
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = SoundTrigger.SERVICE_STATE_DISABLED;
            dest.writeInt(this.id);
            dest.writeString(this.implementor);
            dest.writeString(this.description);
            dest.writeString(this.uuid.toString());
            dest.writeInt(this.version);
            dest.writeInt(this.maxSoundModels);
            dest.writeInt(this.maxKeyphrases);
            dest.writeInt(this.maxUsers);
            dest.writeInt(this.recognitionModes);
            if (this.supportsCaptureTransition) {
                i = SoundTrigger.SERVICE_STATE_DISABLED;
            } else {
                i = SoundTrigger.STATUS_DEAD_OBJECT;
            }
            dest.writeByte((byte) i);
            dest.writeInt(this.maxBufferMs);
            if (this.supportsConcurrentCapture) {
                i = SoundTrigger.SERVICE_STATE_DISABLED;
            } else {
                i = SoundTrigger.STATUS_DEAD_OBJECT;
            }
            dest.writeByte((byte) i);
            dest.writeInt(this.powerConsumptionMw);
            if (!this.returnsTriggerInEvent) {
                i2 = SoundTrigger.STATUS_DEAD_OBJECT;
            }
            dest.writeByte((byte) i2);
        }

        public int describeContents() {
            return SoundTrigger.STATUS_DEAD_OBJECT;
        }

        public String toString() {
            return "ModuleProperties [id=" + this.id + ", implementor=" + this.implementor + ", description=" + this.description + ", uuid=" + this.uuid + ", version=" + this.version + ", maxSoundModels=" + this.maxSoundModels + ", maxKeyphrases=" + this.maxKeyphrases + ", maxUsers=" + this.maxUsers + ", recognitionModes=" + this.recognitionModes + ", supportsCaptureTransition=" + this.supportsCaptureTransition + ", maxBufferMs=" + this.maxBufferMs + ", supportsConcurrentCapture=" + this.supportsConcurrentCapture + ", powerConsumptionMw=" + this.powerConsumptionMw + ", returnsTriggerInEvent=" + this.returnsTriggerInEvent + "]";
        }
    }

    public static class RecognitionConfig implements Parcelable {
        public static final Creator<RecognitionConfig> CREATOR = null;
        public final boolean allowMultipleTriggers;
        public final boolean captureRequested;
        public final byte[] data;
        public final KeyphraseRecognitionExtra[] keyphrases;

        /* renamed from: android.hardware.soundtrigger.SoundTrigger.RecognitionConfig.1 */
        static class AnonymousClass1 implements Creator<RecognitionConfig> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m89createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public RecognitionConfig createFromParcel(Parcel in) {
                return RecognitionConfig.fromParcel(in);
            }

            public /* bridge */ /* synthetic */ Object[] m90newArray(int size) {
                return newArray(size);
            }

            public RecognitionConfig[] newArray(int size) {
                return new RecognitionConfig[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.soundtrigger.SoundTrigger.RecognitionConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.soundtrigger.SoundTrigger.RecognitionConfig.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.soundtrigger.SoundTrigger.RecognitionConfig.<clinit>():void");
        }

        public RecognitionConfig(boolean captureRequested, boolean allowMultipleTriggers, KeyphraseRecognitionExtra[] keyphrases, byte[] data) {
            this.captureRequested = captureRequested;
            this.allowMultipleTriggers = allowMultipleTriggers;
            this.keyphrases = keyphrases;
            this.data = data;
        }

        private static RecognitionConfig fromParcel(Parcel in) {
            return new RecognitionConfig(in.readByte() == (byte) 1, in.readByte() == (byte) 1, (KeyphraseRecognitionExtra[]) in.createTypedArray(KeyphraseRecognitionExtra.CREATOR), in.readBlob());
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = SoundTrigger.SERVICE_STATE_DISABLED;
            if (this.captureRequested) {
                i = SoundTrigger.SERVICE_STATE_DISABLED;
            } else {
                i = SoundTrigger.STATUS_DEAD_OBJECT;
            }
            dest.writeByte((byte) i);
            if (!this.allowMultipleTriggers) {
                i2 = SoundTrigger.STATUS_DEAD_OBJECT;
            }
            dest.writeByte((byte) i2);
            dest.writeTypedArray(this.keyphrases, flags);
            dest.writeBlob(this.data);
        }

        public int describeContents() {
            return SoundTrigger.STATUS_DEAD_OBJECT;
        }

        public String toString() {
            return "RecognitionConfig [captureRequested=" + this.captureRequested + ", allowMultipleTriggers=" + this.allowMultipleTriggers + ", keyphrases=" + Arrays.toString(this.keyphrases) + ", data=" + Arrays.toString(this.data) + "]";
        }
    }

    public static class SoundModelEvent implements Parcelable {
        public static final Creator<SoundModelEvent> CREATOR = null;
        public final byte[] data;
        public final int soundModelHandle;
        public final int status;

        /* renamed from: android.hardware.soundtrigger.SoundTrigger.SoundModelEvent.1 */
        static class AnonymousClass1 implements Creator<SoundModelEvent> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m91createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public SoundModelEvent createFromParcel(Parcel in) {
                return SoundModelEvent.fromParcel(in);
            }

            public /* bridge */ /* synthetic */ Object[] m92newArray(int size) {
                return newArray(size);
            }

            public SoundModelEvent[] newArray(int size) {
                return new SoundModelEvent[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.soundtrigger.SoundTrigger.SoundModelEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.soundtrigger.SoundTrigger.SoundModelEvent.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.soundtrigger.SoundTrigger.SoundModelEvent.<clinit>():void");
        }

        SoundModelEvent(int status, int soundModelHandle, byte[] data) {
            this.status = status;
            this.soundModelHandle = soundModelHandle;
            this.data = data;
        }

        private static SoundModelEvent fromParcel(Parcel in) {
            return new SoundModelEvent(in.readInt(), in.readInt(), in.readBlob());
        }

        public int describeContents() {
            return SoundTrigger.STATUS_DEAD_OBJECT;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.status);
            dest.writeInt(this.soundModelHandle);
            dest.writeBlob(this.data);
        }

        public int hashCode() {
            return ((((Arrays.hashCode(this.data) + 31) * 31) + this.soundModelHandle) * 31) + this.status;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SoundModelEvent other = (SoundModelEvent) obj;
            return Arrays.equals(this.data, other.data) && this.soundModelHandle == other.soundModelHandle && this.status == other.status;
        }

        public String toString() {
            return "SoundModelEvent [status=" + this.status + ", soundModelHandle=" + this.soundModelHandle + ", data=" + (this.data == null ? SoundTrigger.STATUS_DEAD_OBJECT : this.data.length) + "]";
        }
    }

    public interface StatusListener {
        void onRecognition(RecognitionEvent recognitionEvent);

        void onServiceDied();

        void onServiceStateChange(int i);

        void onSoundModelUpdate(SoundModelEvent soundModelEvent);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.soundtrigger.SoundTrigger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.soundtrigger.SoundTrigger.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.soundtrigger.SoundTrigger.<clinit>():void");
    }

    public static native int listModules(ArrayList<ModuleProperties> arrayList);

    public SoundTrigger() {
    }

    public static SoundTriggerModule attachModule(int moduleId, StatusListener listener, Handler handler) {
        if (listener == null) {
            return null;
        }
        return new SoundTriggerModule(moduleId, listener, handler);
    }
}
