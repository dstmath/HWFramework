package android.hardware.camera2.params;

import android.annotation.SystemApi;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.hardware.camera2.utils.SurfaceUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class OutputConfiguration implements Parcelable {
    private static final String CHECK_SURFACE_MESSAGE = "Surface must not be null";
    public static final Parcelable.Creator<OutputConfiguration> CREATOR = new Parcelable.Creator<OutputConfiguration>() {
        /* class android.hardware.camera2.params.OutputConfiguration.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OutputConfiguration createFromParcel(Parcel source) {
            try {
                return new OutputConfiguration(source);
            } catch (Exception e) {
                Log.e(OutputConfiguration.TAG, "Exception creating OutputConfiguration from parcel", e);
                return null;
            }
        }

        @Override // android.os.Parcelable.Creator
        public OutputConfiguration[] newArray(int size) {
            return new OutputConfiguration[size];
        }
    };
    private static final int DEFAULT_LIST_CAPACITY = 10;
    private static final int HEX_HIGH_MASK = -65536;
    private static final int HEX_LOW_MASK = 65535;
    private static final int MAX_SURFACES_COUNT = 4;
    @SystemApi
    public static final int ROTATION_0 = 0;
    @SystemApi
    public static final int ROTATION_180 = 2;
    @SystemApi
    public static final int ROTATION_270 = 3;
    @SystemApi
    public static final int ROTATION_90 = 1;
    private static final String ROTATION_CONSTANT = "Rotation constant";
    private static final int SHIFT_OFFSET_HEX = 16;
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
    private String mPhysicalCameraId;
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

    @SystemApi
    public OutputConfiguration(Surface surface, int rotation) {
        this(-1, surface, rotation);
    }

    @SystemApi
    public OutputConfiguration(int surfaceGroupId, Surface surface, int rotation) {
        this.SURFACE_TYPE_UNKNOWN = -1;
        this.SURFACE_TYPE_SURFACE_VIEW = 0;
        this.SURFACE_TYPE_SURFACE_TEXTURE = 1;
        Preconditions.checkNotNull(surface, CHECK_SURFACE_MESSAGE);
        Preconditions.checkArgumentInRange(rotation, 0, 3, ROTATION_CONSTANT);
        this.mSurfaceGroupId = surfaceGroupId;
        this.mSurfaceType = -1;
        this.mSurfaces = new ArrayList<>();
        this.mSurfaces.add(surface);
        this.mRotation = rotation;
        this.mConfiguredSize = SurfaceUtils.getSurfaceSize(surface);
        this.mConfiguredFormat = SurfaceUtils.getSurfaceFormat(surface);
        this.mConfiguredDataspace = SurfaceUtils.getSurfaceDataspace(surface);
        this.mConfiguredGenerationId = surface.getGenerationId();
        this.mIsDeferredConfig = false;
        this.mIsShared = false;
        this.mPhysicalCameraId = null;
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
        this.mSurfaces = new ArrayList<>();
        this.mRotation = 0;
        this.mConfiguredSize = surfaceSize;
        this.mConfiguredFormat = StreamConfigurationMap.imageFormatToInternal(34);
        this.mConfiguredDataspace = StreamConfigurationMap.imageFormatToDataspace(34);
        this.mConfiguredGenerationId = 0;
        this.mIsDeferredConfig = true;
        this.mIsShared = false;
        this.mPhysicalCameraId = null;
    }

    public OutputConfiguration(Surface surface, int rotation, int dualMode) {
        this.SURFACE_TYPE_UNKNOWN = -1;
        this.SURFACE_TYPE_SURFACE_VIEW = 0;
        this.SURFACE_TYPE_SURFACE_TEXTURE = 1;
        Preconditions.checkNotNull(surface, CHECK_SURFACE_MESSAGE);
        Preconditions.checkArgumentInRange(rotation, 0, 3, ROTATION_CONSTANT);
        this.mIsDeferredConfig = false;
        this.mSurfaceGroupId = -1;
        this.mSurfaceType = -1;
        this.mSurfaces = new ArrayList<>(10);
        this.mSurfaces.add(surface);
        this.mRotation = (65535 & rotation) | ((dualMode << 16) & -65536);
        this.mConfiguredSize = SurfaceUtils.getSurfaceSize(surface);
        this.mConfiguredFormat = SurfaceUtils.getSurfaceFormat(surface);
        this.mConfiguredDataspace = SurfaceUtils.getSurfaceDataspace(surface);
        this.mConfiguredGenerationId = surface.getGenerationId();
    }

    public void enableSurfaceSharing() {
        this.mIsShared = true;
    }

    public void setPhysicalCameraId(String physicalCameraId) {
        this.mPhysicalCameraId = physicalCameraId;
    }

    public boolean isForPhysicalCamera() {
        return this.mPhysicalCameraId != null;
    }

    public boolean isDeferredConfiguration() {
        return this.mIsDeferredConfig;
    }

    public void addSurface(Surface surface) {
        Preconditions.checkNotNull(surface, CHECK_SURFACE_MESSAGE);
        if (this.mSurfaces.contains(surface)) {
            throw new IllegalStateException("Surface is already added!");
        } else if (this.mSurfaces.size() == 1 && !this.mIsShared) {
            throw new IllegalStateException("Cannot have 2 surfaces for a non-sharing configuration");
        } else if (this.mSurfaces.size() + 1 <= 4) {
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
        } else {
            throw new IllegalArgumentException("Exceeds maximum number of surfaces");
        }
    }

    public void removeSurface(Surface surface) {
        if (getSurface() == surface) {
            throw new IllegalArgumentException("Cannot remove surface associated with this output configuration");
        } else if (!this.mSurfaces.remove(surface)) {
            throw new IllegalArgumentException("Surface is not part of this output configuration");
        }
    }

    public OutputConfiguration(OutputConfiguration other) {
        this.SURFACE_TYPE_UNKNOWN = -1;
        this.SURFACE_TYPE_SURFACE_VIEW = 0;
        this.SURFACE_TYPE_SURFACE_TEXTURE = 1;
        if (other != null) {
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
            this.mPhysicalCameraId = other.mPhysicalCameraId;
            return;
        }
        throw new IllegalArgumentException("OutputConfiguration shouldn't be null");
    }

    private OutputConfiguration(Parcel source) {
        this.SURFACE_TYPE_UNKNOWN = -1;
        this.SURFACE_TYPE_SURFACE_VIEW = 0;
        boolean isShared = true;
        this.SURFACE_TYPE_SURFACE_TEXTURE = 1;
        int rotation = source.readInt();
        int surfaceSetId = source.readInt();
        int surfaceType = source.readInt();
        int width = source.readInt();
        int height = source.readInt();
        boolean isDeferred = source.readInt() == 1;
        isShared = source.readInt() != 1 ? false : isShared;
        ArrayList<Surface> surfaces = new ArrayList<>();
        source.readTypedList(surfaces, Surface.CREATOR);
        String physicalCameraId = source.readString();
        Preconditions.checkArgumentInRange(rotation, 0, 3, ROTATION_CONSTANT);
        this.mSurfaceGroupId = surfaceSetId;
        this.mRotation = rotation;
        this.mSurfaces = surfaces;
        this.mConfiguredSize = new Size(width, height);
        this.mIsDeferredConfig = isDeferred;
        this.mIsShared = isShared;
        this.mSurfaces = surfaces;
        if (this.mSurfaces.size() > 0) {
            this.mSurfaceType = -1;
            this.mConfiguredFormat = SurfaceUtils.getSurfaceFormat(this.mSurfaces.get(0));
            this.mConfiguredDataspace = SurfaceUtils.getSurfaceDataspace(this.mSurfaces.get(0));
            this.mConfiguredGenerationId = this.mSurfaces.get(0).getGenerationId();
        } else {
            this.mSurfaceType = surfaceType;
            this.mConfiguredFormat = StreamConfigurationMap.imageFormatToInternal(34);
            this.mConfiguredDataspace = StreamConfigurationMap.imageFormatToDataspace(34);
            this.mConfiguredGenerationId = 0;
        }
        this.mPhysicalCameraId = physicalCameraId;
    }

    public int getMaxSharedSurfaceCount() {
        return 4;
    }

    public Surface getSurface() {
        if (this.mSurfaces.size() == 0) {
            return null;
        }
        return this.mSurfaces.get(0);
    }

    public List<Surface> getSurfaces() {
        return Collections.unmodifiableList(this.mSurfaces);
    }

    @SystemApi
    public int getRotation() {
        return this.mRotation;
    }

    public int getSurfaceGroupId() {
        return this.mSurfaceGroupId;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (dest != null) {
            dest.writeInt(this.mRotation);
            dest.writeInt(this.mSurfaceGroupId);
            dest.writeInt(this.mSurfaceType);
            dest.writeInt(this.mConfiguredSize.getWidth());
            dest.writeInt(this.mConfiguredSize.getHeight());
            dest.writeInt(this.mIsDeferredConfig ? 1 : 0);
            dest.writeInt(this.mIsShared ? 1 : 0);
            dest.writeTypedList(this.mSurfaces);
            dest.writeString(this.mPhysicalCameraId);
            return;
        }
        throw new IllegalArgumentException("dest must not be null");
    }

    public boolean equals(Object obj) {
        int i;
        int i2;
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
        if (!(this.mRotation == other.mRotation && this.mConfiguredSize.equals(other.mConfiguredSize) && (i = this.mConfiguredFormat) == (i2 = other.mConfiguredFormat) && this.mSurfaceGroupId == other.mSurfaceGroupId && this.mSurfaceType == other.mSurfaceType && this.mIsDeferredConfig == other.mIsDeferredConfig && this.mIsShared == other.mIsShared && i == i2 && this.mConfiguredDataspace == other.mConfiguredDataspace && this.mConfiguredGenerationId == other.mConfiguredGenerationId && Objects.equals(this.mPhysicalCameraId, other.mPhysicalCameraId))) {
            return false;
        }
        int minLen = Math.min(this.mSurfaces.size(), other.mSurfaces.size());
        for (int i3 = 0; i3 < minLen; i3++) {
            if (this.mSurfaces.get(i3) != other.mSurfaces.get(i3)) {
                return false;
            }
        }
        return true;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0039: APUT  (r0v4 int[]), (7 ??[int, float, short, byte, char]), (r9v3 int) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0075: APUT  (r0v2 int[]), (8 ??[int, float, short, byte, char]), (r9v1 int) */
    public int hashCode() {
        int i = 0;
        if (this.mIsDeferredConfig) {
            int[] iArr = new int[8];
            iArr[0] = this.mRotation;
            iArr[1] = this.mConfiguredSize.hashCode();
            iArr[2] = this.mConfiguredFormat;
            iArr[3] = this.mConfiguredDataspace;
            iArr[4] = this.mSurfaceGroupId;
            iArr[5] = this.mSurfaceType;
            iArr[6] = this.mIsShared ? 1 : 0;
            String str = this.mPhysicalCameraId;
            if (str != null) {
                i = str.hashCode();
            }
            iArr[7] = i;
            return HashCodeHelpers.hashCode(iArr);
        }
        int[] iArr2 = new int[9];
        iArr2[0] = this.mRotation;
        iArr2[1] = this.mSurfaces.hashCode();
        iArr2[2] = this.mConfiguredGenerationId;
        iArr2[3] = this.mConfiguredSize.hashCode();
        iArr2[4] = this.mConfiguredFormat;
        iArr2[5] = this.mConfiguredDataspace;
        iArr2[6] = this.mSurfaceGroupId;
        iArr2[7] = this.mIsShared ? 1 : 0;
        String str2 = this.mPhysicalCameraId;
        if (str2 != null) {
            i = str2.hashCode();
        }
        iArr2[8] = i;
        return HashCodeHelpers.hashCode(iArr2);
    }
}
