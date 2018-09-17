package android.media.midi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class MidiDeviceStatus implements Parcelable {
    public static final Creator<MidiDeviceStatus> CREATOR = null;
    private static final String TAG = "MidiDeviceStatus";
    private final MidiDeviceInfo mDeviceInfo;
    private final boolean[] mInputPortOpen;
    private final int[] mOutputPortOpenCount;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.midi.MidiDeviceStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.midi.MidiDeviceStatus.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.midi.MidiDeviceStatus.<clinit>():void");
    }

    public MidiDeviceStatus(MidiDeviceInfo deviceInfo, boolean[] inputPortOpen, int[] outputPortOpenCount) {
        this.mDeviceInfo = deviceInfo;
        this.mInputPortOpen = new boolean[inputPortOpen.length];
        System.arraycopy(inputPortOpen, 0, this.mInputPortOpen, 0, inputPortOpen.length);
        this.mOutputPortOpenCount = new int[outputPortOpenCount.length];
        System.arraycopy(outputPortOpenCount, 0, this.mOutputPortOpenCount, 0, outputPortOpenCount.length);
    }

    public MidiDeviceStatus(MidiDeviceInfo deviceInfo) {
        this.mDeviceInfo = deviceInfo;
        this.mInputPortOpen = new boolean[deviceInfo.getInputPortCount()];
        this.mOutputPortOpenCount = new int[deviceInfo.getOutputPortCount()];
    }

    public MidiDeviceInfo getDeviceInfo() {
        return this.mDeviceInfo;
    }

    public boolean isInputPortOpen(int portNumber) {
        return this.mInputPortOpen[portNumber];
    }

    public int getOutputPortOpenCount(int portNumber) {
        return this.mOutputPortOpenCount[portNumber];
    }

    public String toString() {
        int i;
        int inputPortCount = this.mDeviceInfo.getInputPortCount();
        int outputPortCount = this.mDeviceInfo.getOutputPortCount();
        StringBuilder builder = new StringBuilder("mInputPortOpen=[");
        for (i = 0; i < inputPortCount; i++) {
            builder.append(this.mInputPortOpen[i]);
            if (i < inputPortCount - 1) {
                builder.append(",");
            }
        }
        builder.append("] mOutputPortOpenCount=[");
        for (i = 0; i < outputPortCount; i++) {
            builder.append(this.mOutputPortOpenCount[i]);
            if (i < outputPortCount - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this.mDeviceInfo, flags);
        parcel.writeBooleanArray(this.mInputPortOpen);
        parcel.writeIntArray(this.mOutputPortOpenCount);
    }
}
