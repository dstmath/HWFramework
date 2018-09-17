package android.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Pools.SynchronizedPool;
import android.view.WindowManager.LayoutParams;

public class MagnificationSpec implements Parcelable {
    public static final Creator<MagnificationSpec> CREATOR = null;
    private static final int MAX_POOL_SIZE = 20;
    private static final SynchronizedPool<MagnificationSpec> sPool = null;
    public float offsetX;
    public float offsetY;
    public float scale;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.MagnificationSpec.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.MagnificationSpec.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.MagnificationSpec.<clinit>():void");
    }

    private MagnificationSpec() {
        this.scale = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
    }

    public void initialize(float scale, float offsetX, float offsetY) {
        if (scale < LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            throw new IllegalArgumentException("Scale must be greater than or equal to one!");
        }
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public boolean isNop() {
        return this.scale == LayoutParams.BRIGHTNESS_OVERRIDE_FULL && this.offsetX == 0.0f && this.offsetY == 0.0f;
    }

    public static MagnificationSpec obtain(MagnificationSpec other) {
        MagnificationSpec info = obtain();
        info.scale = other.scale;
        info.offsetX = other.offsetX;
        info.offsetY = other.offsetY;
        return info;
    }

    public static MagnificationSpec obtain() {
        MagnificationSpec spec = (MagnificationSpec) sPool.acquire();
        return spec != null ? spec : new MagnificationSpec();
    }

    public void recycle() {
        clear();
        sPool.release(this);
    }

    public void clear() {
        this.scale = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        this.offsetX = 0.0f;
        this.offsetY = 0.0f;
    }

    public void setTo(MagnificationSpec other) {
        this.scale = other.scale;
        this.offsetX = other.offsetX;
        this.offsetY = other.offsetY;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeFloat(this.scale);
        parcel.writeFloat(this.offsetX);
        parcel.writeFloat(this.offsetY);
        recycle();
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        MagnificationSpec s = (MagnificationSpec) other;
        if (!(this.scale == s.scale && this.offsetX == s.offsetX && this.offsetY == s.offsetY)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int result;
        int floatToIntBits;
        int i = 0;
        if (this.scale != 0.0f) {
            result = Float.floatToIntBits(this.scale);
        } else {
            result = 0;
        }
        int i2 = result * 31;
        if (this.offsetX != 0.0f) {
            floatToIntBits = Float.floatToIntBits(this.offsetX);
        } else {
            floatToIntBits = 0;
        }
        floatToIntBits = (i2 + floatToIntBits) * 31;
        if (this.offsetY != 0.0f) {
            i = Float.floatToIntBits(this.offsetY);
        }
        return floatToIntBits + i;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<scale:");
        builder.append(Float.toString(this.scale));
        builder.append(",offsetX:");
        builder.append(Float.toString(this.offsetX));
        builder.append(",offsetY:");
        builder.append(Float.toString(this.offsetY));
        builder.append(">");
        return builder.toString();
    }

    private void initFromParcel(Parcel parcel) {
        this.scale = parcel.readFloat();
        this.offsetX = parcel.readFloat();
        this.offsetY = parcel.readFloat();
    }
}
