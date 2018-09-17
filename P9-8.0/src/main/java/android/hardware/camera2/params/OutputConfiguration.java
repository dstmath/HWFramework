package android.hardware.camera2.params;

import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.hardware.camera2.utils.SurfaceUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OutputConfiguration implements Parcelable {
    public static final Creator<OutputConfiguration> CREATOR = new Creator<OutputConfiguration>() {
        public OutputConfiguration createFromParcel(Parcel source) {
            try {
                return new OutputConfiguration(source, null);
            } catch (Exception e) {
                Log.e(OutputConfiguration.TAG, "Exception creating OutputConfiguration from parcel", e);
                return null;
            }
        }

        public OutputConfiguration[] newArray(int size) {
            return new OutputConfiguration[size];
        }
    };
    private static final int MAX_SURFACES_COUNT = 2;
    public static final int ROTATION_0 = 0;
    public static final int ROTATION_180 = 2;
    public static final int ROTATION_270 = 3;
    public static final int ROTATION_90 = 1;
    public static final int SURFACE_GROUP_ID_NONE = -1;
    private static final String TAG = "OutputConfiguration";
    private final int SURFACE_TYPE_SURFACE_TEXTURE;
    private final int SURFACE_TYPE_SURFACE_VIEW;
    private final int SURFACE_TYPE_UNKNOWN;
    private final int mConfiguredDataspace;
    private final int mConfiguredFormat;
    private final int mConfiguredGenerationId;
    private final Size mConfiguredSize;
    private final boolean mIsDeferredConfig;
    private boolean mIsShared;
    private final int mRotation;
    private final int mSurfaceGroupId;
    private final int mSurfaceType;
    private ArrayList<Surface> mSurfaces;

    public OutputConfiguration(Surface surface) {
        this(-1, surface, 0);
    }

    public OutputConfiguration(int surfaceGroupId, Surface surface) {
        this(surfaceGroupId, surface, 0);
    }

    public OutputConfiguration(Surface surface, int rotation) {
        this(-1, surface, rotation);
    }

    public OutputConfiguration(int surfaceGroupId, Surface surface, int rotation) {
        this.SURFACE_TYPE_UNKNOWN = -1;
        this.SURFACE_TYPE_SURFACE_VIEW = 0;
        this.SURFACE_TYPE_SURFACE_TEXTURE = 1;
        Preconditions.checkNotNull(surface, "Surface must not be null");
        Preconditions.checkArgumentInRange(rotation, 0, 3, "Rotation constant");
        this.mSurfaceGroupId = surfaceGroupId;
        this.mSurfaceType = -1;
        this.mSurfaces = new ArrayList();
        this.mSurfaces.add(surface);
        this.mRotation = rotation;
        this.mConfiguredSize = SurfaceUtils.getSurfaceSize(surface);
        this.mConfiguredFormat = SurfaceUtils.getSurfaceFormat(surface);
        this.mConfiguredDataspace = SurfaceUtils.getSurfaceDataspace(surface);
        this.mConfiguredGenerationId = surface.getGenerationId();
        this.mIsDeferredConfig = false;
        this.mIsShared = false;
    }

    public <T> OutputConfiguration(Size surfaceSize, Class<T> klass) {
        this.SURFACE_TYPE_UNKNOWN = -1;
        this.SURFACE_TYPE_SURFACE_VIEW = 0;
        this.SURFACE_TYPE_SURFACE_TEXTURE = 1;
        Preconditions.checkNotNull(klass, "surfaceSize must not be null");
        Preconditions.checkNotNull(klass, "klass must not be null");
        if (klass == SurfaceHolder.class) {
            this.mSurfaceType = 0;
        } else if (klass == SurfaceTexture.class) {
            this.mSurfaceType = 1;
        } else {
            this.mSurfaceType = -1;
            throw new IllegalArgumentException("Unknow surface source class type");
        }
        if (surfaceSize.getWidth() == 0 || surfaceSize.getHeight() == 0) {
            throw new IllegalArgumentException("Surface size needs to be non-zero");
        }
        this.mSurfaceGroupId = -1;
        this.mSurfaces = new ArrayList();
        this.mRotation = 0;
        this.mConfiguredSize = surfaceSize;
        this.mConfiguredFormat = StreamConfigurationMap.imageFormatToInternal(34);
        this.mConfiguredDataspace = StreamConfigurationMap.imageFormatToDataspace(34);
        this.mConfiguredGenerationId = 0;
        this.mIsDeferredConfig = true;
        this.mIsShared = false;
    }

    public void enableSurfaceSharing() {
        this.mIsShared = true;
    }

    public boolean isDeferredConfiguration() {
        return this.mIsDeferredConfig;
    }

    public void addSurface(Surface surface) {
        Preconditions.checkNotNull(surface, "Surface must not be null");
        if (this.mSurfaces.contains(surface)) {
            throw new IllegalStateException("Surface is already added!");
        } else if (this.mSurfaces.size() == 1 && (this.mIsShared ^ 1) != 0) {
            throw new IllegalStateException("Cannot have 2 surfaces for a non-sharing configuration");
        } else if (this.mSurfaces.size() + 1 > 2) {
            throw new IllegalArgumentException("Exceeds maximum number of surfaces");
        } else {
            Size surfaceSize = SurfaceUtils.getSurfaceSize(surface);
            if (!surfaceSize.equals(this.mConfiguredSize)) {
                Log.w(TAG, "Added surface size " + surfaceSize + " is different than pre-configured size " + this.mConfiguredSize + ", the pre-configured size will be used.");
            }
            if (this.mConfiguredFormat != SurfaceUtils.getSurfaceFormat(surface)) {
                throw new IllegalArgumentException("The format of added surface format doesn't match");
            } else if (this.mConfiguredFormat == 34 || this.mConfiguredDataspace == SurfaceUtils.getSurfaceDataspace(surface)) {
                this.mSurfaces.add(surface);
            } else {
                throw new IllegalArgumentException("The dataspace of added surface doesn't match");
            }
        }
    }

    public OutputConfiguration(OutputConfiguration other) {
        this.SURFACE_TYPE_UNKNOWN = -1;
        this.SURFACE_TYPE_SURFACE_VIEW = 0;
        this.SURFACE_TYPE_SURFACE_TEXTURE = 1;
        if (other == null) {
            throw new IllegalArgumentException("OutputConfiguration shouldn't be null");
        }
        this.mSurfaces = other.mSurfaces;
        this.mRotation = other.mRotation;
        this.mSurfaceGroupId = other.mSurfaceGroupId;
        this.mSurfaceType = other.mSurfaceType;
        this.mConfiguredDataspace = other.mConfiguredDataspace;
        this.mConfiguredFormat = other.mConfiguredFormat;
        this.mConfiguredSize = other.mConfiguredSize;
        this.mConfiguredGenerationId = other.mConfiguredGenerationId;
        this.mIsDeferredConfig = other.mIsDeferredConfig;
        this.mIsShared = other.mIsShared;
    }

    public OutputConfiguration(Surface surface, int rotation, int dualMode) {
        this.SURFACE_TYPE_UNKNOWN = -1;
        this.SURFACE_TYPE_SURFACE_VIEW = 0;
        this.SURFACE_TYPE_SURFACE_TEXTURE = 1;
        Preconditions.checkNotNull(surface, "Surface must not be null");
        Preconditions.checkArgumentInRange(rotation, 0, 3, "Rotation constant");
        this.mIsDeferredConfig = false;
        this.mSurfaceGroupId = -1;
        this.mSurfaceType = -1;
        this.mSurfaces = new ArrayList();
        this.mSurfaces.add(surface);
        this.mRotation = (65535 & rotation) | ((dualMode << 16) & Color.RED);
        this.mConfiguredSize = SurfaceUtils.getSurfaceSize(surface);
        this.mConfiguredFormat = SurfaceUtils.getSurfaceFormat(surface);
        this.mConfiguredDataspace = SurfaceUtils.getSurfaceDataspace(surface);
        this.mConfiguredGenerationId = surface.getGenerationId();
    }

    private OutputConfiguration(Parcel source) {
        this.SURFACE_TYPE_UNKNOWN = -1;
        this.SURFACE_TYPE_SURFACE_VIEW = 0;
        this.SURFACE_TYPE_SURFACE_TEXTURE = 1;
        int rotation = source.readInt();
        int surfaceSetId = source.readInt();
        int surfaceType = source.readInt();
        int width = source.readInt();
        int height = source.readInt();
        boolean isDeferred = source.readInt() == 1;
        boolean isShared = source.readInt() == 1;
        ArrayList<Surface> surfaces = new ArrayList();
        source.readTypedList(surfaces, Surface.CREATOR);
        Preconditions.checkArgumentInRange(rotation, 0, 3, "Rotation constant");
        this.mSurfaceGroupId = surfaceSetId;
        this.mRotation = rotation;
        this.mSurfaces = surfaces;
        this.mConfiguredSize = new Size(width, height);
        this.mIsDeferredConfig = isDeferred;
        this.mIsShared = isShared;
        this.mSurfaces = surfaces;
        if (this.mSurfaces.size() > 0) {
            this.mSurfaceType = -1;
            this.mConfiguredFormat = SurfaceUtils.getSurfaceFormat((Surface) this.mSurfaces.get(0));
            this.mConfiguredDataspace = SurfaceUtils.getSurfaceDataspace((Surface) this.mSurfaces.get(0));
            this.mConfiguredGenerationId = ((Surface) this.mSurfaces.get(0)).getGenerationId();
            return;
        }
        this.mSurfaceType = surfaceType;
        this.mConfiguredFormat = StreamConfigurationMap.imageFormatToInternal(34);
        this.mConfiguredDataspace = StreamConfigurationMap.imageFormatToDataspace(34);
        this.mConfiguredGenerationId = 0;
    }

    public Surface getSurface() {
        if (this.mSurfaces.size() == 0) {
            return null;
        }
        return (Surface) this.mSurfaces.get(0);
    }

    public List<Surface> getSurfaces() {
        return Collections.unmodifiableList(this.mSurfaces);
    }

    public int getRotation() {
        return this.mRotation;
    }

    public int getSurfaceGroupId() {
        return this.mSurfaceGroupId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        if (dest == null) {
            throw new IllegalArgumentException("dest must not be null");
        }
        int i2;
        dest.writeInt(this.mRotation);
        dest.writeInt(this.mSurfaceGroupId);
        dest.writeInt(this.mSurfaceType);
        dest.writeInt(this.mConfiguredSize.getWidth());
        dest.writeInt(this.mConfiguredSize.getHeight());
        if (this.mIsDeferredConfig) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        dest.writeInt(i2);
        if (!this.mIsShared) {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeTypedList(this.mSurfaces);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OutputConfiguration)) {
            return false;
        }
        OutputConfiguration other = (OutputConfiguration) obj;
        if (this.mRotation != other.mRotation || (this.mConfiguredSize.equals(other.mConfiguredSize) ^ 1) != 0 || this.mConfiguredFormat != other.mConfiguredFormat || this.mSurfaceGroupId != other.mSurfaceGroupId || this.mSurfaceType != other.mSurfaceType || this.mIsDeferredConfig != other.mIsDeferredConfig || this.mIsShared != other.mIsShared || this.mConfiguredFormat != other.mConfiguredFormat || this.mConfiguredDataspace != other.mConfiguredDataspace || this.mConfiguredGenerationId != other.mConfiguredGenerationId) {
            return false;
        }
        int minLen = Math.min(this.mSurfaces.size(), other.mSurfaces.size());
        for (int i = 0; i < minLen; i++) {
            if (this.mSurfaces.get(i) != other.mSurfaces.get(i)) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int i = 1;
        int[] iArr;
        if (this.mIsDeferredConfig) {
            iArr = new int[7];
            iArr[0] = this.mRotation;
            iArr[1] = this.mConfiguredSize.hashCode();
            iArr[2] = this.mConfiguredFormat;
            iArr[3] = this.mConfiguredDataspace;
            iArr[4] = this.mSurfaceGroupId;
            iArr[5] = this.mSurfaceType;
            if (!this.mIsShared) {
                i = 0;
            }
            iArr[6] = i;
            return HashCodeHelpers.hashCode(iArr);
        }
        iArr = new int[8];
        iArr[0] = this.mRotation;
        iArr[1] = this.mSurfaces.hashCode();
        iArr[2] = this.mConfiguredGenerationId;
        iArr[3] = this.mConfiguredSize.hashCode();
        iArr[4] = this.mConfiguredFormat;
        iArr[5] = this.mConfiguredDataspace;
        iArr[6] = this.mSurfaceGroupId;
        if (!this.mIsShared) {
            i = 0;
        }
        iArr[7] = i;
        return HashCodeHelpers.hashCode(iArr);
    }
}
