package android.hardware.location;

import android.os.Parcel;
import android.os.Parcelable.Creator;

public class NanoApp {
    public static final Creator<NanoApp> CREATOR = null;
    private final String TAG;
    private final String UNKNOWN;
    private byte[] mAppBinary;
    private int mAppId;
    private boolean mAppIdSet;
    private int mAppVersion;
    private String mName;
    private int mNeededExecMemBytes;
    private int mNeededReadMemBytes;
    private int[] mNeededSensors;
    private int mNeededWriteMemBytes;
    private int[] mOutputEvents;
    private String mPublisher;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.location.NanoApp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.location.NanoApp.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.NanoApp.<clinit>():void");
    }

    public NanoApp() {
        this(0, null);
        this.mAppIdSet = false;
    }

    public NanoApp(int appId, byte[] appBinary) {
        this.TAG = "NanoApp";
        this.UNKNOWN = "Unknown";
        this.mPublisher = "Unknown";
        this.mName = "Unknown";
        this.mAppId = appId;
        this.mAppIdSet = true;
        this.mAppVersion = 0;
        this.mNeededReadMemBytes = 0;
        this.mNeededWriteMemBytes = 0;
        this.mNeededExecMemBytes = 0;
        this.mNeededSensors = new int[0];
        this.mOutputEvents = new int[0];
        this.mAppBinary = appBinary;
    }

    public void setPublisher(String publisher) {
        this.mPublisher = publisher;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setAppId(int appId) {
        this.mAppId = appId;
        this.mAppIdSet = true;
    }

    public void setAppVersion(int appVersion) {
        this.mAppVersion = appVersion;
    }

    public void setNeededReadMemBytes(int neededReadMemBytes) {
        this.mNeededReadMemBytes = neededReadMemBytes;
    }

    public void setNeededWriteMemBytes(int neededWriteMemBytes) {
        this.mNeededWriteMemBytes = neededWriteMemBytes;
    }

    public void setNeededExecMemBytes(int neededExecMemBytes) {
        this.mNeededExecMemBytes = neededExecMemBytes;
    }

    public void setNeededSensors(int[] neededSensors) {
        this.mNeededSensors = neededSensors;
    }

    public void setOutputEvents(int[] outputEvents) {
        this.mOutputEvents = outputEvents;
    }

    public void setAppBinary(byte[] appBinary) {
        this.mAppBinary = appBinary;
    }

    public String getPublisher() {
        return this.mPublisher;
    }

    public String getName() {
        return this.mName;
    }

    public int getAppId() {
        return this.mAppId;
    }

    public int getAppVersion() {
        return this.mAppVersion;
    }

    public int getNeededReadMemBytes() {
        return this.mNeededReadMemBytes;
    }

    public int getNeededWriteMemBytes() {
        return this.mNeededWriteMemBytes;
    }

    public int getNeededExecMemBytes() {
        return this.mNeededExecMemBytes;
    }

    public int[] getNeededSensors() {
        return this.mNeededSensors;
    }

    public int[] getOutputEvents() {
        return this.mOutputEvents;
    }

    public byte[] getAppBinary() {
        return this.mAppBinary;
    }

    private NanoApp(Parcel in) {
        this.TAG = "NanoApp";
        this.UNKNOWN = "Unknown";
        this.mPublisher = in.readString();
        this.mName = in.readString();
        this.mAppId = in.readInt();
        this.mAppVersion = in.readInt();
        this.mNeededReadMemBytes = in.readInt();
        this.mNeededWriteMemBytes = in.readInt();
        this.mNeededExecMemBytes = in.readInt();
        this.mNeededSensors = new int[in.readInt()];
        in.readIntArray(this.mNeededSensors);
        this.mOutputEvents = new int[in.readInt()];
        in.readIntArray(this.mOutputEvents);
        this.mAppBinary = new byte[in.readInt()];
        in.readByteArray(this.mAppBinary);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.mAppBinary == null) {
            throw new IllegalStateException("Must set non-null AppBinary for nanoapp " + this.mName);
        } else if (this.mAppIdSet) {
            out.writeString(this.mPublisher);
            out.writeString(this.mName);
            out.writeInt(this.mAppId);
            out.writeInt(this.mAppVersion);
            out.writeInt(this.mNeededReadMemBytes);
            out.writeInt(this.mNeededWriteMemBytes);
            out.writeInt(this.mNeededExecMemBytes);
            out.writeInt(this.mNeededSensors.length);
            out.writeIntArray(this.mNeededSensors);
            out.writeInt(this.mOutputEvents.length);
            out.writeIntArray(this.mOutputEvents);
            out.writeInt(this.mAppBinary.length);
            out.writeByteArray(this.mAppBinary);
        } else {
            throw new IllegalStateException("Must set AppId for nanoapp " + this.mName);
        }
    }

    public String toString() {
        return ((("Id : " + this.mAppId) + ", Version : " + this.mAppVersion) + ", Name : " + this.mName) + ", Publisher : " + this.mPublisher;
    }
}
