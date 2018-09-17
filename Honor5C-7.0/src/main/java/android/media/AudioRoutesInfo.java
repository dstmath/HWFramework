package android.media;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

public class AudioRoutesInfo implements Parcelable {
    public static final Creator<AudioRoutesInfo> CREATOR = null;
    public static final int MAIN_DOCK_SPEAKERS = 4;
    public static final int MAIN_HDMI = 8;
    public static final int MAIN_HEADPHONES = 2;
    public static final int MAIN_HEADSET = 1;
    public static final int MAIN_SPEAKER = 0;
    public static final int MAIN_USB = 16;
    public CharSequence bluetoothName;
    public int mainType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.AudioRoutesInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.AudioRoutesInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.AudioRoutesInfo.<clinit>():void");
    }

    public AudioRoutesInfo() {
        this.mainType = MAIN_SPEAKER;
    }

    public AudioRoutesInfo(AudioRoutesInfo o) {
        this.mainType = MAIN_SPEAKER;
        this.bluetoothName = o.bluetoothName;
        this.mainType = o.mainType;
    }

    AudioRoutesInfo(Parcel src) {
        this.mainType = MAIN_SPEAKER;
        this.bluetoothName = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(src);
        this.mainType = src.readInt();
    }

    public int describeContents() {
        return MAIN_SPEAKER;
    }

    public String toString() {
        return getClass().getSimpleName() + "{ type=" + typeToString(this.mainType) + (TextUtils.isEmpty(this.bluetoothName) ? ProxyInfo.LOCAL_EXCL_LIST : ", bluetoothName=" + this.bluetoothName) + " }";
    }

    private static String typeToString(int type) {
        if (type == 0) {
            return "SPEAKER";
        }
        if ((type & MAIN_HEADSET) != 0) {
            return "HEADSET";
        }
        if ((type & MAIN_HEADPHONES) != 0) {
            return "HEADPHONES";
        }
        if ((type & MAIN_DOCK_SPEAKERS) != 0) {
            return "DOCK_SPEAKERS";
        }
        if ((type & MAIN_HDMI) != 0) {
            return "HDMI";
        }
        if ((type & MAIN_USB) != 0) {
            return "USB";
        }
        return Integer.toHexString(type);
    }

    public void writeToParcel(Parcel dest, int flags) {
        TextUtils.writeToParcel(this.bluetoothName, dest, flags);
        dest.writeInt(this.mainType);
    }
}
