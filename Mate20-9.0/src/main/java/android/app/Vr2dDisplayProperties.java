package android.app;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.PrintWriter;

public final class Vr2dDisplayProperties implements Parcelable {
    public static final Parcelable.Creator<Vr2dDisplayProperties> CREATOR = new Parcelable.Creator<Vr2dDisplayProperties>() {
        public Vr2dDisplayProperties createFromParcel(Parcel source) {
            return new Vr2dDisplayProperties(source);
        }

        public Vr2dDisplayProperties[] newArray(int size) {
            return new Vr2dDisplayProperties[size];
        }
    };
    public static final int FLAG_VIRTUAL_DISPLAY_ENABLED = 1;
    private final int mAddedFlags;
    private final int mDpi;
    private final int mHeight;
    private final int mRemovedFlags;
    private final int mWidth;

    public static class Builder {
        private int mAddedFlags = 0;
        private int mDpi = -1;
        private int mHeight = -1;
        private int mRemovedFlags = 0;
        private int mWidth = -1;

        public Builder setDimensions(int width, int height, int dpi) {
            this.mWidth = width;
            this.mHeight = height;
            this.mDpi = dpi;
            return this;
        }

        public Builder setEnabled(boolean enabled) {
            if (enabled) {
                addFlags(1);
            } else {
                removeFlags(1);
            }
            return this;
        }

        public Builder addFlags(int flags) {
            this.mAddedFlags |= flags;
            this.mRemovedFlags &= ~flags;
            return this;
        }

        public Builder removeFlags(int flags) {
            this.mRemovedFlags |= flags;
            this.mAddedFlags &= ~flags;
            return this;
        }

        public Vr2dDisplayProperties build() {
            Vr2dDisplayProperties vr2dDisplayProperties = new Vr2dDisplayProperties(this.mWidth, this.mHeight, this.mDpi, this.mAddedFlags, this.mRemovedFlags);
            return vr2dDisplayProperties;
        }
    }

    public Vr2dDisplayProperties(int width, int height, int dpi) {
        this(width, height, dpi, 0, 0);
    }

    private Vr2dDisplayProperties(int width, int height, int dpi, int flags, int removedFlags) {
        this.mWidth = width;
        this.mHeight = height;
        this.mDpi = dpi;
        this.mAddedFlags = flags;
        this.mRemovedFlags = removedFlags;
    }

    public int hashCode() {
        return (31 * ((31 * getWidth()) + getHeight())) + getDpi();
    }

    public String toString() {
        return "Vr2dDisplayProperties{mWidth=" + this.mWidth + ", mHeight=" + this.mHeight + ", mDpi=" + this.mDpi + ", flags=" + toReadableFlags(this.mAddedFlags) + ", removed_flags=" + toReadableFlags(this.mRemovedFlags) + "}";
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vr2dDisplayProperties that = (Vr2dDisplayProperties) o;
        if (getFlags() != that.getFlags() || getRemovedFlags() != that.getRemovedFlags() || getWidth() != that.getWidth() || getHeight() != that.getHeight()) {
            return false;
        }
        if (getDpi() != that.getDpi()) {
            z = false;
        }
        return z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mWidth);
        dest.writeInt(this.mHeight);
        dest.writeInt(this.mDpi);
        dest.writeInt(this.mAddedFlags);
        dest.writeInt(this.mRemovedFlags);
    }

    private Vr2dDisplayProperties(Parcel source) {
        this.mWidth = source.readInt();
        this.mHeight = source.readInt();
        this.mDpi = source.readInt();
        this.mAddedFlags = source.readInt();
        this.mRemovedFlags = source.readInt();
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + toString());
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getDpi() {
        return this.mDpi;
    }

    public int getFlags() {
        return this.mAddedFlags;
    }

    public int getRemovedFlags() {
        return this.mRemovedFlags;
    }

    private static String toReadableFlags(int flags) {
        String retval = "{";
        if ((flags & 1) == 1) {
            retval = retval + "enabled";
        }
        return retval + "}";
    }
}
