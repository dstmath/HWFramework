package android.hardware.camera2;

import android.graphics.Rect;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.PublicKey;
import android.hardware.camera2.impl.SyntheticKey;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.TonemapCurve;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.hardware.camera2.utils.TypeReference;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class CaptureRequest extends CameraMetadata<Key<?>> implements Parcelable {
    @PublicKey
    public static final Key<Boolean> BLACK_LEVEL_LOCK = null;
    @PublicKey
    public static final Key<Integer> COLOR_CORRECTION_ABERRATION_MODE = null;
    @PublicKey
    public static final Key<RggbChannelVector> COLOR_CORRECTION_GAINS = null;
    @PublicKey
    public static final Key<Integer> COLOR_CORRECTION_MODE = null;
    @PublicKey
    public static final Key<ColorSpaceTransform> COLOR_CORRECTION_TRANSFORM = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AE_ANTIBANDING_MODE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AE_EXPOSURE_COMPENSATION = null;
    @PublicKey
    public static final Key<Boolean> CONTROL_AE_LOCK = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AE_MODE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AE_PRECAPTURE_TRIGGER = null;
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AE_REGIONS = null;
    @PublicKey
    public static final Key<Range<Integer>> CONTROL_AE_TARGET_FPS_RANGE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AF_MODE = null;
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AF_REGIONS = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AF_TRIGGER = null;
    @PublicKey
    public static final Key<Boolean> CONTROL_AWB_LOCK = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AWB_MODE = null;
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AWB_REGIONS = null;
    @PublicKey
    public static final Key<Integer> CONTROL_CAPTURE_INTENT = null;
    @PublicKey
    public static final Key<Integer> CONTROL_EFFECT_MODE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_MODE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_POST_RAW_SENSITIVITY_BOOST = null;
    @PublicKey
    public static final Key<Integer> CONTROL_SCENE_MODE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_VIDEO_STABILIZATION_MODE = null;
    public static final Creator<CaptureRequest> CREATOR = null;
    @PublicKey
    public static final Key<Integer> EDGE_MODE = null;
    @PublicKey
    public static final Key<Integer> FLASH_MODE = null;
    @PublicKey
    public static final Key<Integer> HOT_PIXEL_MODE = null;
    public static final Key<double[]> JPEG_GPS_COORDINATES = null;
    @PublicKey
    @SyntheticKey
    public static final Key<Location> JPEG_GPS_LOCATION = null;
    public static final Key<String> JPEG_GPS_PROCESSING_METHOD = null;
    public static final Key<Long> JPEG_GPS_TIMESTAMP = null;
    @PublicKey
    public static final Key<Integer> JPEG_ORIENTATION = null;
    @PublicKey
    public static final Key<Byte> JPEG_QUALITY = null;
    @PublicKey
    public static final Key<Byte> JPEG_THUMBNAIL_QUALITY = null;
    @PublicKey
    public static final Key<Size> JPEG_THUMBNAIL_SIZE = null;
    public static final Key<Boolean> LED_TRANSMIT = null;
    @PublicKey
    public static final Key<Float> LENS_APERTURE = null;
    @PublicKey
    public static final Key<Float> LENS_FILTER_DENSITY = null;
    @PublicKey
    public static final Key<Float> LENS_FOCAL_LENGTH = null;
    @PublicKey
    public static final Key<Float> LENS_FOCUS_DISTANCE = null;
    @PublicKey
    public static final Key<Integer> LENS_OPTICAL_STABILIZATION_MODE = null;
    @PublicKey
    public static final Key<Integer> NOISE_REDUCTION_MODE = null;
    @PublicKey
    public static final Key<Float> REPROCESS_EFFECTIVE_EXPOSURE_FACTOR = null;
    public static final Key<Integer> REQUEST_ID = null;
    @PublicKey
    public static final Key<Rect> SCALER_CROP_REGION = null;
    @PublicKey
    public static final Key<Long> SENSOR_EXPOSURE_TIME = null;
    @PublicKey
    public static final Key<Long> SENSOR_FRAME_DURATION = null;
    @PublicKey
    public static final Key<Integer> SENSOR_SENSITIVITY = null;
    @PublicKey
    public static final Key<int[]> SENSOR_TEST_PATTERN_DATA = null;
    @PublicKey
    public static final Key<Integer> SENSOR_TEST_PATTERN_MODE = null;
    @PublicKey
    public static final Key<Integer> SHADING_MODE = null;
    @PublicKey
    public static final Key<Integer> STATISTICS_FACE_DETECT_MODE = null;
    @PublicKey
    public static final Key<Boolean> STATISTICS_HOT_PIXEL_MAP_MODE = null;
    @PublicKey
    public static final Key<Integer> STATISTICS_LENS_SHADING_MAP_MODE = null;
    @PublicKey
    @SyntheticKey
    public static final Key<TonemapCurve> TONEMAP_CURVE = null;
    public static final Key<float[]> TONEMAP_CURVE_BLUE = null;
    public static final Key<float[]> TONEMAP_CURVE_GREEN = null;
    public static final Key<float[]> TONEMAP_CURVE_RED = null;
    @PublicKey
    public static final Key<Float> TONEMAP_GAMMA = null;
    @PublicKey
    public static final Key<Integer> TONEMAP_MODE = null;
    @PublicKey
    public static final Key<Integer> TONEMAP_PRESET_CURVE = null;
    private boolean mIsPartOfCHSRequestList;
    private boolean mIsReprocess;
    private int mReprocessableSessionId;
    private final CameraMetadataNative mSettings;
    private final HashSet<Surface> mSurfaceSet;
    private Object mUserTag;

    public static final class Builder {
        private final CaptureRequest mRequest;

        public Builder(CameraMetadataNative template, boolean reprocess, int reprocessableSessionId) {
            this.mRequest = new CaptureRequest(reprocess, reprocessableSessionId, null);
        }

        public void addTarget(Surface outputTarget) {
            this.mRequest.mSurfaceSet.add(outputTarget);
        }

        public void removeTarget(Surface outputTarget) {
            this.mRequest.mSurfaceSet.remove(outputTarget);
        }

        public <T> void set(Key<T> key, T value) {
            this.mRequest.mSettings.set((Key) key, (Object) value);
        }

        public <T> T get(Key<T> key) {
            return this.mRequest.mSettings.get((Key) key);
        }

        public void setTag(Object tag) {
            this.mRequest.mUserTag = tag;
        }

        public void setPartOfCHSRequestList(boolean partOfCHSList) {
            this.mRequest.mIsPartOfCHSRequestList = partOfCHSList;
        }

        public CaptureRequest build() {
            return new CaptureRequest(null, null);
        }

        public boolean isEmpty() {
            return this.mRequest.mSettings.isEmpty();
        }
    }

    public static final class Key<T> {
        private final android.hardware.camera2.impl.CameraMetadataNative.Key<T> mKey;

        public Key(String name, Class<T> type) {
            this.mKey = new android.hardware.camera2.impl.CameraMetadataNative.Key(name, (Class) type);
        }

        public Key(String name, TypeReference<T> typeReference) {
            this.mKey = new android.hardware.camera2.impl.CameraMetadataNative.Key(name, (TypeReference) typeReference);
        }

        public String getName() {
            return this.mKey.getName();
        }

        public final int hashCode() {
            return this.mKey.hashCode();
        }

        public final boolean equals(Object o) {
            return o instanceof Key ? ((Key) o).mKey.equals(this.mKey) : false;
        }

        public String toString() {
            return String.format("CaptureRequest.Key(%s)", new Object[]{this.mKey.getName()});
        }

        public android.hardware.camera2.impl.CameraMetadataNative.Key<T> getNativeKey() {
            return this.mKey;
        }

        Key(android.hardware.camera2.impl.CameraMetadataNative.Key<?> nativeKey) {
            this.mKey = nativeKey;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.camera2.CaptureRequest.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.camera2.CaptureRequest.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.CaptureRequest.<clinit>():void");
    }

    private CaptureRequest() {
        this.mIsPartOfCHSRequestList = false;
        this.mSettings = new CameraMetadataNative();
        this.mSurfaceSet = new HashSet();
        this.mIsReprocess = false;
        this.mReprocessableSessionId = -1;
    }

    private CaptureRequest(CaptureRequest source) {
        this.mIsPartOfCHSRequestList = false;
        this.mSettings = new CameraMetadataNative(source.mSettings);
        this.mSurfaceSet = (HashSet) source.mSurfaceSet.clone();
        this.mIsReprocess = source.mIsReprocess;
        this.mIsPartOfCHSRequestList = source.mIsPartOfCHSRequestList;
        this.mReprocessableSessionId = source.mReprocessableSessionId;
        this.mUserTag = source.mUserTag;
    }

    private CaptureRequest(CameraMetadataNative settings, boolean isReprocess, int reprocessableSessionId) {
        this.mIsPartOfCHSRequestList = false;
        this.mSettings = CameraMetadataNative.move(settings);
        this.mSurfaceSet = new HashSet();
        this.mIsReprocess = isReprocess;
        if (!isReprocess) {
            this.mReprocessableSessionId = -1;
        } else if (reprocessableSessionId == -1) {
            throw new IllegalArgumentException("Create a reprocess capture request with an invalid session ID: " + reprocessableSessionId);
        } else {
            this.mReprocessableSessionId = reprocessableSessionId;
        }
    }

    public <T> T get(Key<T> key) {
        return this.mSettings.get((Key) key);
    }

    protected <T> T getProtected(Key<?> key) {
        return this.mSettings.get((Key) key);
    }

    protected Class<Key<?>> getKeyClass() {
        return Key.class;
    }

    public List<Key<?>> getKeys() {
        return super.getKeys();
    }

    public Object getTag() {
        return this.mUserTag;
    }

    public boolean isReprocess() {
        return this.mIsReprocess;
    }

    public boolean isPartOfCRequestList() {
        return this.mIsPartOfCHSRequestList;
    }

    public CameraMetadataNative getNativeCopy() {
        return new CameraMetadataNative(this.mSettings);
    }

    public int getReprocessableSessionId() {
        if (this.mIsReprocess && this.mReprocessableSessionId != -1) {
            return this.mReprocessableSessionId;
        }
        throw new IllegalStateException("Getting the reprocessable session ID for a non-reprocess capture request is illegal.");
    }

    public boolean equals(Object other) {
        if (other instanceof CaptureRequest) {
            return equals((CaptureRequest) other);
        }
        return false;
    }

    private boolean equals(CaptureRequest other) {
        return other != null && Objects.equals(this.mUserTag, other.mUserTag) && this.mSurfaceSet.equals(other.mSurfaceSet) && this.mSettings.equals(other.mSettings) && this.mIsReprocess == other.mIsReprocess && this.mReprocessableSessionId == other.mReprocessableSessionId;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCodeGeneric(this.mSettings, this.mSurfaceSet, this.mUserTag);
    }

    private void readFromParcel(Parcel in) {
        boolean z = false;
        this.mSettings.readFromParcel(in);
        this.mSurfaceSet.clear();
        Parcelable[] parcelableArray = in.readParcelableArray(Surface.class.getClassLoader());
        if (parcelableArray != null) {
            for (Parcelable s : parcelableArray) {
                this.mSurfaceSet.add((Surface) s);
            }
            if (in.readInt() != 0) {
                z = true;
            }
            this.mIsReprocess = z;
            this.mReprocessableSessionId = -1;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        this.mSettings.writeToParcel(dest, flags);
        dest.writeParcelableArray((Surface[]) this.mSurfaceSet.toArray(new Surface[this.mSurfaceSet.size()]), flags);
        dest.writeInt(this.mIsReprocess ? 1 : 0);
    }

    public boolean containsTarget(Surface surface) {
        return this.mSurfaceSet.contains(surface);
    }

    public Collection<Surface> getTargets() {
        return Collections.unmodifiableCollection(this.mSurfaceSet);
    }
}
