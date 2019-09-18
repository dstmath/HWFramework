package android.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pools;

public class MagnificationSpec implements Parcelable {
    public static final Parcelable.Creator<MagnificationSpec> CREATOR = new Parcelable.Creator<MagnificationSpec>() {
        public MagnificationSpec[] newArray(int size) {
            return new MagnificationSpec[size];
        }

        public MagnificationSpec createFromParcel(Parcel parcel) {
            MagnificationSpec spec = MagnificationSpec.obtain();
            spec.initFromParcel(parcel);
            return spec;
        }
    };
    private static final int MAX_POOL_SIZE = 20;
    private static final Pools.SynchronizedPool<MagnificationSpec> sPool = new Pools.SynchronizedPool<>(20);
    public float offsetX;
    public float offsetY;
    public float scale = 1.0f;

    private MagnificationSpec() {
    }

    public void initialize(float scale2, float offsetX2, float offsetY2) {
        if (scale2 >= 1.0f) {
            this.scale = scale2;
            this.offsetX = offsetX2;
            this.offsetY = offsetY2;
            return;
        }
        throw new IllegalArgumentException("Scale must be greater than or equal to one!");
    }

    public boolean isNop() {
        return this.scale == 1.0f && this.offsetX == 0.0f && this.offsetY == 0.0f;
    }

    public static MagnificationSpec obtain(MagnificationSpec other) {
        MagnificationSpec info = obtain();
        info.scale = other.scale;
        info.offsetX = other.offsetX;
        info.offsetY = other.offsetY;
        return info;
    }

    public static MagnificationSpec obtain() {
        MagnificationSpec spec = sPool.acquire();
        return spec != null ? spec : new MagnificationSpec();
    }

    public void recycle() {
        clear();
        sPool.release(this);
    }

    public void clear() {
        this.scale = 1.0f;
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
        int i = 0;
        int floatToIntBits = 31 * ((31 * (this.scale != 0.0f ? Float.floatToIntBits(this.scale) : 0)) + (this.offsetX != 0.0f ? Float.floatToIntBits(this.offsetX) : 0));
        if (this.offsetY != 0.0f) {
            i = Float.floatToIntBits(this.offsetY);
        }
        return floatToIntBits + i;
    }

    public String toString() {
        return "<scale:" + Float.toString(this.scale) + ",offsetX:" + Float.toString(this.offsetX) + ",offsetY:" + Float.toString(this.offsetY) + ">";
    }

    /* access modifiers changed from: private */
    public void initFromParcel(Parcel parcel) {
        this.scale = parcel.readFloat();
        this.offsetX = parcel.readFloat();
        this.offsetY = parcel.readFloat();
    }
}
