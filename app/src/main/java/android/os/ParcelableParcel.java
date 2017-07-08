package android.os;

import android.os.Parcelable.ClassLoaderCreator;
import android.util.MathUtils;

public class ParcelableParcel implements Parcelable {
    public static final ClassLoaderCreator<ParcelableParcel> CREATOR = null;
    final ClassLoader mClassLoader;
    final Parcel mParcel;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.ParcelableParcel.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.ParcelableParcel.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.ParcelableParcel.<clinit>():void");
    }

    public ParcelableParcel(ClassLoader loader) {
        this.mParcel = Parcel.obtain();
        this.mClassLoader = loader;
    }

    public ParcelableParcel(Parcel src, ClassLoader loader) {
        this.mParcel = Parcel.obtain();
        this.mClassLoader = loader;
        int size = src.readInt();
        if (size < 0) {
            throw new IllegalArgumentException("Negative size read from parcel");
        }
        int pos = src.dataPosition();
        src.setDataPosition(MathUtils.addOrThrow(pos, size));
        this.mParcel.appendFrom(src, pos, size);
    }

    public Parcel getParcel() {
        this.mParcel.setDataPosition(0);
        return this.mParcel;
    }

    public ClassLoader getClassLoader() {
        return this.mClassLoader;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mParcel.dataSize());
        dest.appendFrom(this.mParcel, 0, this.mParcel.dataSize());
    }
}
