package android.hardware.camera2.params;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.legacy.LegacyCameraDevice;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.hardware.camera2.utils.SurfaceUtils;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.net.wifi.AnqpInformationElement;
import android.renderscript.Allocation;
import android.renderscript.Mesh.TriangleMeshBuilder;
import android.rms.HwSysResource;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.util.Preconditions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public final class StreamConfigurationMap {
    private static final long DURATION_20FPS_NS = 50000000;
    private static final int DURATION_MIN_FRAME = 0;
    private static final int DURATION_STALL = 1;
    private static final int HAL_DATASPACE_DEPTH = 4096;
    private static final int HAL_DATASPACE_RANGE_SHIFT = 27;
    private static final int HAL_DATASPACE_STANDARD_SHIFT = 16;
    private static final int HAL_DATASPACE_TRANSFER_SHIFT = 22;
    private static final int HAL_DATASPACE_UNKNOWN = 0;
    private static final int HAL_DATASPACE_V0_JFIF = 146931712;
    private static final int HAL_PIXEL_FORMAT_BLOB = 33;
    private static final int HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED = 34;
    private static final int HAL_PIXEL_FORMAT_RAW10 = 37;
    private static final int HAL_PIXEL_FORMAT_RAW12 = 38;
    private static final int HAL_PIXEL_FORMAT_RAW16 = 32;
    private static final int HAL_PIXEL_FORMAT_RAW_OPAQUE = 36;
    private static final int HAL_PIXEL_FORMAT_Y16 = 540422489;
    private static final int HAL_PIXEL_FORMAT_YCbCr_420_888 = 35;
    private static final String TAG = "StreamConfigurationMap";
    private final SparseIntArray mAllOutputFormats;
    private final StreamConfiguration[] mConfigurations;
    private final StreamConfiguration[] mDepthConfigurations;
    private final StreamConfigurationDuration[] mDepthMinFrameDurations;
    private final SparseIntArray mDepthOutputFormats;
    private final StreamConfigurationDuration[] mDepthStallDurations;
    private final SparseIntArray mHighResOutputFormats;
    private final HighSpeedVideoConfiguration[] mHighSpeedVideoConfigurations;
    private final HashMap<Range<Integer>, Integer> mHighSpeedVideoFpsRangeMap;
    private final HashMap<Size, Integer> mHighSpeedVideoSizeMap;
    private final SparseIntArray mInputFormats;
    private final ReprocessFormatsMap mInputOutputFormatsMap;
    private final boolean mListHighResolution;
    private final StreamConfigurationDuration[] mMinFrameDurations;
    private final SparseIntArray mOutputFormats;
    private final StreamConfigurationDuration[] mStallDurations;

    public StreamConfigurationMap(StreamConfiguration[] configurations, StreamConfigurationDuration[] minFrameDurations, StreamConfigurationDuration[] stallDurations, StreamConfiguration[] depthConfigurations, StreamConfigurationDuration[] depthMinFrameDurations, StreamConfigurationDuration[] depthStallDurations, HighSpeedVideoConfiguration[] highSpeedVideoConfigurations, ReprocessFormatsMap inputOutputFormatsMap, boolean listHighResolution) {
        this.mOutputFormats = new SparseIntArray();
        this.mHighResOutputFormats = new SparseIntArray();
        this.mAllOutputFormats = new SparseIntArray();
        this.mInputFormats = new SparseIntArray();
        this.mDepthOutputFormats = new SparseIntArray();
        this.mHighSpeedVideoSizeMap = new HashMap();
        this.mHighSpeedVideoFpsRangeMap = new HashMap();
        if (configurations == null) {
            Preconditions.checkArrayElementsNotNull(depthConfigurations, "depthConfigurations");
            this.mConfigurations = new StreamConfiguration[HAL_DATASPACE_UNKNOWN];
            this.mMinFrameDurations = new StreamConfigurationDuration[HAL_DATASPACE_UNKNOWN];
            this.mStallDurations = new StreamConfigurationDuration[HAL_DATASPACE_UNKNOWN];
        } else {
            this.mConfigurations = (StreamConfiguration[]) Preconditions.checkArrayElementsNotNull(configurations, "configurations");
            this.mMinFrameDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(minFrameDurations, "minFrameDurations");
            this.mStallDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(stallDurations, "stallDurations");
        }
        this.mListHighResolution = listHighResolution;
        if (depthConfigurations == null) {
            this.mDepthConfigurations = new StreamConfiguration[HAL_DATASPACE_UNKNOWN];
            this.mDepthMinFrameDurations = new StreamConfigurationDuration[HAL_DATASPACE_UNKNOWN];
            this.mDepthStallDurations = new StreamConfigurationDuration[HAL_DATASPACE_UNKNOWN];
        } else {
            this.mDepthConfigurations = (StreamConfiguration[]) Preconditions.checkArrayElementsNotNull(depthConfigurations, "depthConfigurations");
            this.mDepthMinFrameDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(depthMinFrameDurations, "depthMinFrameDurations");
            this.mDepthStallDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(depthStallDurations, "depthStallDurations");
        }
        if (highSpeedVideoConfigurations == null) {
            this.mHighSpeedVideoConfigurations = new HighSpeedVideoConfiguration[HAL_DATASPACE_UNKNOWN];
        } else {
            this.mHighSpeedVideoConfigurations = (HighSpeedVideoConfiguration[]) Preconditions.checkArrayElementsNotNull(highSpeedVideoConfigurations, "highSpeedVideoConfigurations");
        }
        StreamConfiguration[] streamConfigurationArr = this.mConfigurations;
        int length = streamConfigurationArr.length;
        for (int i = HAL_DATASPACE_UNKNOWN; i < length; i += DURATION_STALL) {
            int i2;
            SparseIntArray map;
            StreamConfiguration config = streamConfigurationArr[i];
            int fmt = config.getFormat();
            if (config.isOutput()) {
                this.mAllOutputFormats.put(fmt, this.mAllOutputFormats.get(fmt) + DURATION_STALL);
                long duration = 0;
                if (this.mListHighResolution) {
                    StreamConfigurationDuration[] streamConfigurationDurationArr = this.mMinFrameDurations;
                    int length2 = streamConfigurationDurationArr.length;
                    for (i2 = HAL_DATASPACE_UNKNOWN; i2 < length2; i2 += DURATION_STALL) {
                        StreamConfigurationDuration configurationDuration = streamConfigurationDurationArr[i2];
                        if (configurationDuration.getFormat() == fmt && configurationDuration.getWidth() == config.getSize().getWidth() && configurationDuration.getHeight() == config.getSize().getHeight()) {
                            duration = configurationDuration.getDuration();
                            break;
                        }
                    }
                }
                map = duration <= DURATION_20FPS_NS ? this.mOutputFormats : this.mHighResOutputFormats;
            } else {
                map = this.mInputFormats;
            }
            map.put(fmt, map.get(fmt) + DURATION_STALL);
        }
        StreamConfiguration[] streamConfigurationArr2 = this.mDepthConfigurations;
        int length3 = streamConfigurationArr2.length;
        for (i2 = HAL_DATASPACE_UNKNOWN; i2 < length3; i2 += DURATION_STALL) {
            config = streamConfigurationArr2[i2];
            if (config.isOutput()) {
                this.mDepthOutputFormats.put(config.getFormat(), this.mDepthOutputFormats.get(config.getFormat()) + DURATION_STALL);
            }
        }
        if (configurations == null || this.mOutputFormats.indexOfKey(HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED) >= 0) {
            HighSpeedVideoConfiguration[] highSpeedVideoConfigurationArr = this.mHighSpeedVideoConfigurations;
            length3 = highSpeedVideoConfigurationArr.length;
            for (i2 = HAL_DATASPACE_UNKNOWN; i2 < length3; i2 += DURATION_STALL) {
                HighSpeedVideoConfiguration config2 = highSpeedVideoConfigurationArr[i2];
                Size size = config2.getSize();
                Range<Integer> fpsRange = config2.getFpsRange();
                Integer fpsRangeCount = (Integer) this.mHighSpeedVideoSizeMap.get(size);
                if (fpsRangeCount == null) {
                    fpsRangeCount = Integer.valueOf(HAL_DATASPACE_UNKNOWN);
                }
                this.mHighSpeedVideoSizeMap.put(size, Integer.valueOf(fpsRangeCount.intValue() + DURATION_STALL));
                Integer sizeCount = (Integer) this.mHighSpeedVideoFpsRangeMap.get(fpsRange);
                if (sizeCount == null) {
                    sizeCount = Integer.valueOf(HAL_DATASPACE_UNKNOWN);
                }
                this.mHighSpeedVideoFpsRangeMap.put(fpsRange, Integer.valueOf(sizeCount.intValue() + DURATION_STALL));
            }
            this.mInputOutputFormatsMap = inputOutputFormatsMap;
            return;
        }
        throw new AssertionError("At least one stream configuration for IMPLEMENTATION_DEFINED must exist");
    }

    public final int[] getOutputFormats() {
        return getPublicFormats(true);
    }

    public final int[] getValidOutputFormatsForInput(int inputFormat) {
        if (this.mInputOutputFormatsMap == null) {
            return new int[HAL_DATASPACE_UNKNOWN];
        }
        return this.mInputOutputFormatsMap.getOutputs(inputFormat);
    }

    public final int[] getInputFormats() {
        return getPublicFormats(false);
    }

    public Size[] getInputSizes(int format) {
        return getPublicFormatSizes(format, false, false);
    }

    public boolean isOutputSupportedFor(int format) {
        boolean z = true;
        checkArgumentFormat(format);
        int internalFormat = imageFormatToInternal(format);
        if (imageFormatToDataspace(format) == HAL_DATASPACE_DEPTH) {
            if (this.mDepthOutputFormats.indexOfKey(internalFormat) < 0) {
                z = false;
            }
            return z;
        }
        if (getFormatsMap(true).indexOfKey(internalFormat) < 0) {
            z = false;
        }
        return z;
    }

    public static <T> boolean isOutputSupportedFor(Class<T> klass) {
        Preconditions.checkNotNull(klass, "klass must not be null");
        if (klass == ImageReader.class || klass == MediaRecorder.class || klass == MediaCodec.class || klass == Allocation.class || klass == SurfaceHolder.class || klass == SurfaceTexture.class) {
            return true;
        }
        return false;
    }

    public boolean isOutputSupportedFor(Surface surface) {
        Preconditions.checkNotNull(surface, "surface must not be null");
        Size surfaceSize = SurfaceUtils.getSurfaceSize(surface);
        int surfaceFormat = SurfaceUtils.getSurfaceFormat(surface);
        int surfaceDataspace = SurfaceUtils.getSurfaceDataspace(surface);
        boolean isFlexible = SurfaceUtils.isFlexibleConsumer(surface);
        if (surfaceFormat >= DURATION_STALL && surfaceFormat <= 5) {
            surfaceFormat = HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED;
        }
        StreamConfiguration[] configs = surfaceDataspace != HAL_DATASPACE_DEPTH ? this.mConfigurations : this.mDepthConfigurations;
        int length = configs.length;
        for (int i = HAL_DATASPACE_UNKNOWN; i < length; i += DURATION_STALL) {
            StreamConfiguration config = configs[i];
            if (config.getFormat() == surfaceFormat && config.isOutput()) {
                if (config.getSize().equals(surfaceSize)) {
                    return true;
                }
                if (isFlexible && config.getSize().getWidth() <= LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING) {
                    return true;
                }
            }
        }
        return false;
    }

    public <T> Size[] getOutputSizes(Class<T> klass) {
        if (isOutputSupportedFor((Class) klass)) {
            return getInternalFormatSizes(HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED, HAL_DATASPACE_UNKNOWN, true, false);
        }
        return null;
    }

    public Size[] getOutputSizes(int format) {
        return getPublicFormatSizes(format, true, false);
    }

    public Size[] getHighSpeedVideoSizes() {
        Set<Size> keySet = this.mHighSpeedVideoSizeMap.keySet();
        return (Size[]) keySet.toArray(new Size[keySet.size()]);
    }

    public Range<Integer>[] getHighSpeedVideoFpsRangesFor(Size size) {
        int i = HAL_DATASPACE_UNKNOWN;
        Integer fpsRangeCount = (Integer) this.mHighSpeedVideoSizeMap.get(size);
        if (fpsRangeCount == null || fpsRangeCount.intValue() == 0) {
            Object[] objArr = new Object[DURATION_STALL];
            objArr[HAL_DATASPACE_UNKNOWN] = size;
            throw new IllegalArgumentException(String.format("Size %s does not support high speed video recording", objArr));
        }
        Range<Integer>[] fpsRanges = new Range[fpsRangeCount.intValue()];
        HighSpeedVideoConfiguration[] highSpeedVideoConfigurationArr = this.mHighSpeedVideoConfigurations;
        int length = highSpeedVideoConfigurationArr.length;
        int i2 = HAL_DATASPACE_UNKNOWN;
        while (i < length) {
            int i3;
            HighSpeedVideoConfiguration config = highSpeedVideoConfigurationArr[i];
            if (size.equals(config.getSize())) {
                i3 = i2 + DURATION_STALL;
                fpsRanges[i2] = config.getFpsRange();
            } else {
                i3 = i2;
            }
            i += DURATION_STALL;
            i2 = i3;
        }
        return fpsRanges;
    }

    public Range<Integer>[] getHighSpeedVideoFpsRanges() {
        Set<Range<Integer>> keySet = this.mHighSpeedVideoFpsRangeMap.keySet();
        return (Range[]) keySet.toArray(new Range[keySet.size()]);
    }

    public Size[] getHighSpeedVideoSizesFor(Range<Integer> fpsRange) {
        int i = HAL_DATASPACE_UNKNOWN;
        Integer sizeCount = (Integer) this.mHighSpeedVideoFpsRangeMap.get(fpsRange);
        if (sizeCount == null || sizeCount.intValue() == 0) {
            Object[] objArr = new Object[DURATION_STALL];
            objArr[HAL_DATASPACE_UNKNOWN] = fpsRange;
            throw new IllegalArgumentException(String.format("FpsRange %s does not support high speed video recording", objArr));
        }
        Size[] sizes = new Size[sizeCount.intValue()];
        HighSpeedVideoConfiguration[] highSpeedVideoConfigurationArr = this.mHighSpeedVideoConfigurations;
        int length = highSpeedVideoConfigurationArr.length;
        int i2 = HAL_DATASPACE_UNKNOWN;
        while (i < length) {
            int i3;
            HighSpeedVideoConfiguration config = highSpeedVideoConfigurationArr[i];
            if (fpsRange.equals(config.getFpsRange())) {
                i3 = i2 + DURATION_STALL;
                sizes[i2] = config.getSize();
            } else {
                i3 = i2;
            }
            i += DURATION_STALL;
            i2 = i3;
        }
        return sizes;
    }

    public Size[] getHighResolutionOutputSizes(int format) {
        if (this.mListHighResolution) {
            return getPublicFormatSizes(format, true, true);
        }
        return null;
    }

    public long getOutputMinFrameDuration(int format, Size size) {
        Preconditions.checkNotNull(size, "size must not be null");
        checkArgumentFormatSupported(format, true);
        return getInternalFormatDuration(imageFormatToInternal(format), imageFormatToDataspace(format), size, HAL_DATASPACE_UNKNOWN);
    }

    public <T> long getOutputMinFrameDuration(Class<T> klass, Size size) {
        if (isOutputSupportedFor((Class) klass)) {
            return getInternalFormatDuration(HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED, HAL_DATASPACE_UNKNOWN, size, HAL_DATASPACE_UNKNOWN);
        }
        throw new IllegalArgumentException("klass was not supported");
    }

    public long getOutputStallDuration(int format, Size size) {
        checkArgumentFormatSupported(format, true);
        return getInternalFormatDuration(imageFormatToInternal(format), imageFormatToDataspace(format), size, DURATION_STALL);
    }

    public <T> long getOutputStallDuration(Class<T> klass, Size size) {
        if (isOutputSupportedFor((Class) klass)) {
            return getInternalFormatDuration(HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED, HAL_DATASPACE_UNKNOWN, size, DURATION_STALL);
        }
        throw new IllegalArgumentException("klass was not supported");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StreamConfigurationMap)) {
            return false;
        }
        StreamConfigurationMap other = (StreamConfigurationMap) obj;
        if (Arrays.equals(this.mConfigurations, other.mConfigurations) && Arrays.equals(this.mMinFrameDurations, other.mMinFrameDurations) && Arrays.equals(this.mStallDurations, other.mStallDurations) && Arrays.equals(this.mDepthConfigurations, other.mDepthConfigurations)) {
            z = Arrays.equals(this.mHighSpeedVideoConfigurations, other.mHighSpeedVideoConfigurations);
        }
        return z;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCodeGeneric(this.mConfigurations, this.mMinFrameDurations, this.mStallDurations, this.mDepthConfigurations, this.mHighSpeedVideoConfigurations);
    }

    private int checkArgumentFormatSupported(int format, boolean output) {
        checkArgumentFormat(format);
        int internalFormat = imageFormatToInternal(format);
        int internalDataspace = imageFormatToDataspace(format);
        if (output) {
            if (internalDataspace == HAL_DATASPACE_DEPTH) {
                if (this.mDepthOutputFormats.indexOfKey(internalFormat) >= 0) {
                    return format;
                }
            } else if (this.mAllOutputFormats.indexOfKey(internalFormat) >= 0) {
                return format;
            }
        } else if (this.mInputFormats.indexOfKey(internalFormat) >= 0) {
            return format;
        }
        Object[] objArr = new Object[DURATION_STALL];
        objArr[HAL_DATASPACE_UNKNOWN] = Integer.valueOf(format);
        throw new IllegalArgumentException(String.format("format %x is not supported by this stream configuration map", objArr));
    }

    static int checkArgumentFormatInternal(int format) {
        switch (format) {
            case HAL_PIXEL_FORMAT_BLOB /*33*/:
            case HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED /*34*/:
            case HAL_PIXEL_FORMAT_RAW_OPAQUE /*36*/:
            case HAL_PIXEL_FORMAT_Y16 /*540422489*/:
                return format;
            case TriangleMeshBuilder.TEXTURE_0 /*256*/:
                throw new IllegalArgumentException("ImageFormat.JPEG is an unknown internal format");
            default:
                return checkArgumentFormat(format);
        }
    }

    static int checkArgumentFormat(int format) {
        if (ImageFormat.isPublicFormat(format) || PixelFormat.isPublicFormat(format)) {
            return format;
        }
        Object[] objArr = new Object[DURATION_STALL];
        objArr[HAL_DATASPACE_UNKNOWN] = Integer.valueOf(format);
        throw new IllegalArgumentException(String.format("format 0x%x was not defined in either ImageFormat or PixelFormat", objArr));
    }

    static int imageFormatToPublic(int format) {
        switch (format) {
            case HAL_PIXEL_FORMAT_BLOB /*33*/:
                return TriangleMeshBuilder.TEXTURE_0;
            case TriangleMeshBuilder.TEXTURE_0 /*256*/:
                throw new IllegalArgumentException("ImageFormat.JPEG is an unknown internal format");
            default:
                return format;
        }
    }

    static int depthFormatToPublic(int format) {
        switch (format) {
            case HAL_PIXEL_FORMAT_BLOB /*33*/:
                return AnqpInformationElement.ANQP_CAPABILITY_LIST;
            case HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED /*34*/:
                throw new IllegalArgumentException("IMPLEMENTATION_DEFINED must not leak to public API");
            case TriangleMeshBuilder.TEXTURE_0 /*256*/:
                throw new IllegalArgumentException("ImageFormat.JPEG is an unknown internal format");
            case HAL_PIXEL_FORMAT_Y16 /*540422489*/:
                return ImageFormat.DEPTH16;
            default:
                throw new IllegalArgumentException("Unknown DATASPACE_DEPTH format " + format);
        }
    }

    static int[] imageFormatToPublic(int[] formats) {
        if (formats == null) {
            return null;
        }
        for (int i = HAL_DATASPACE_UNKNOWN; i < formats.length; i += DURATION_STALL) {
            formats[i] = imageFormatToPublic(formats[i]);
        }
        return formats;
    }

    static int imageFormatToInternal(int format) {
        switch (format) {
            case TriangleMeshBuilder.TEXTURE_0 /*256*/:
            case AnqpInformationElement.ANQP_CAPABILITY_LIST /*257*/:
                return HAL_PIXEL_FORMAT_BLOB;
            case ImageFormat.DEPTH16 /*1144402265*/:
                return HAL_PIXEL_FORMAT_Y16;
            default:
                return format;
        }
    }

    static int imageFormatToDataspace(int format) {
        switch (format) {
            case TriangleMeshBuilder.TEXTURE_0 /*256*/:
                return HAL_DATASPACE_V0_JFIF;
            case AnqpInformationElement.ANQP_CAPABILITY_LIST /*257*/:
            case ImageFormat.DEPTH16 /*1144402265*/:
                return HAL_DATASPACE_DEPTH;
            default:
                return HAL_DATASPACE_UNKNOWN;
        }
    }

    public static int[] imageFormatToInternal(int[] formats) {
        if (formats == null) {
            return null;
        }
        for (int i = HAL_DATASPACE_UNKNOWN; i < formats.length; i += DURATION_STALL) {
            formats[i] = imageFormatToInternal(formats[i]);
        }
        return formats;
    }

    private Size[] getPublicFormatSizes(int format, boolean output, boolean highRes) {
        try {
            checkArgumentFormatSupported(format, output);
            return getInternalFormatSizes(imageFormatToInternal(format), imageFormatToDataspace(format), output, highRes);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Size[] getInternalFormatSizes(int format, int dataspace, boolean output, boolean highRes) {
        if (dataspace == HAL_DATASPACE_DEPTH && highRes) {
            return new Size[HAL_DATASPACE_UNKNOWN];
        }
        SparseIntArray formatsMap;
        if (!output) {
            formatsMap = this.mInputFormats;
        } else if (dataspace == HAL_DATASPACE_DEPTH) {
            formatsMap = this.mDepthOutputFormats;
        } else if (highRes) {
            formatsMap = this.mHighResOutputFormats;
        } else {
            formatsMap = this.mOutputFormats;
        }
        int sizesCount = formatsMap.get(format);
        if (((!output || dataspace == HAL_DATASPACE_DEPTH) && sizesCount == 0) || (output && dataspace != HAL_DATASPACE_DEPTH && this.mAllOutputFormats.get(format) == 0)) {
            throw new IllegalArgumentException("format not available");
        }
        Size[] sizes = new Size[sizesCount];
        StreamConfiguration[] configurations = dataspace == HAL_DATASPACE_DEPTH ? this.mDepthConfigurations : this.mConfigurations;
        StreamConfigurationDuration[] minFrameDurations = dataspace == HAL_DATASPACE_DEPTH ? this.mDepthMinFrameDurations : this.mMinFrameDurations;
        int length = configurations.length;
        int i = HAL_DATASPACE_UNKNOWN;
        int sizeIndex = HAL_DATASPACE_UNKNOWN;
        while (i < length) {
            int sizeIndex2;
            StreamConfiguration config = configurations[i];
            int fmt = config.getFormat();
            if (fmt == format && config.isOutput() == output) {
                if (output && this.mListHighResolution) {
                    long duration = 0;
                    for (int i2 = HAL_DATASPACE_UNKNOWN; i2 < minFrameDurations.length; i2 += DURATION_STALL) {
                        StreamConfigurationDuration d = minFrameDurations[i2];
                        if (d.getFormat() == fmt && d.getWidth() == config.getSize().getWidth() && d.getHeight() == config.getSize().getHeight()) {
                            duration = d.getDuration();
                            break;
                        }
                    }
                    if (dataspace != HAL_DATASPACE_DEPTH) {
                        boolean z;
                        if (duration > DURATION_20FPS_NS) {
                            z = true;
                        } else {
                            z = false;
                        }
                        if (highRes != z) {
                            sizeIndex2 = sizeIndex;
                        }
                    }
                }
                sizeIndex2 = sizeIndex + DURATION_STALL;
                sizes[sizeIndex] = config.getSize();
            } else {
                sizeIndex2 = sizeIndex;
            }
            i += DURATION_STALL;
            sizeIndex = sizeIndex2;
        }
        if (sizeIndex == sizesCount) {
            return sizes;
        }
        throw new AssertionError("Too few sizes (expected " + sizesCount + ", actual " + sizeIndex + ")");
    }

    private int[] getPublicFormats(boolean output) {
        int[] formats = new int[getPublicFormatCount(output)];
        int i = HAL_DATASPACE_UNKNOWN;
        SparseIntArray map = getFormatsMap(output);
        int j = HAL_DATASPACE_UNKNOWN;
        while (j < map.size()) {
            int i2 = i + DURATION_STALL;
            formats[i] = imageFormatToPublic(map.keyAt(j));
            j += DURATION_STALL;
            i = i2;
        }
        if (output) {
            j = HAL_DATASPACE_UNKNOWN;
            while (j < this.mDepthOutputFormats.size()) {
                i2 = i + DURATION_STALL;
                formats[i] = depthFormatToPublic(this.mDepthOutputFormats.keyAt(j));
                j += DURATION_STALL;
                i = i2;
            }
        }
        if (formats.length == i) {
            return formats;
        }
        throw new AssertionError("Too few formats " + i + ", expected " + formats.length);
    }

    private SparseIntArray getFormatsMap(boolean output) {
        return output ? this.mAllOutputFormats : this.mInputFormats;
    }

    private long getInternalFormatDuration(int format, int dataspace, Size size, int duration) {
        if (isSupportedInternalConfiguration(format, dataspace, size)) {
            StreamConfigurationDuration[] durations = getDurations(duration, dataspace);
            int length = durations.length;
            for (int i = HAL_DATASPACE_UNKNOWN; i < length; i += DURATION_STALL) {
                StreamConfigurationDuration configurationDuration = durations[i];
                if (configurationDuration.getFormat() == format && configurationDuration.getWidth() == size.getWidth() && configurationDuration.getHeight() == size.getHeight()) {
                    return configurationDuration.getDuration();
                }
            }
            return 0;
        }
        throw new IllegalArgumentException("size was not supported");
    }

    private StreamConfigurationDuration[] getDurations(int duration, int dataspace) {
        switch (duration) {
            case HAL_DATASPACE_UNKNOWN /*0*/:
                return dataspace == HAL_DATASPACE_DEPTH ? this.mDepthMinFrameDurations : this.mMinFrameDurations;
            case DURATION_STALL /*1*/:
                return dataspace == HAL_DATASPACE_DEPTH ? this.mDepthStallDurations : this.mStallDurations;
            default:
                throw new IllegalArgumentException("duration was invalid");
        }
    }

    private int getPublicFormatCount(boolean output) {
        int size = getFormatsMap(output).size();
        if (output) {
            return size + this.mDepthOutputFormats.size();
        }
        return size;
    }

    private static <T> boolean arrayContains(T[] array, T element) {
        if (array == null) {
            return false;
        }
        int length = array.length;
        for (int i = HAL_DATASPACE_UNKNOWN; i < length; i += DURATION_STALL) {
            if (Objects.equals(array[i], element)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSupportedInternalConfiguration(int format, int dataspace, Size size) {
        StreamConfiguration[] configurations = dataspace == HAL_DATASPACE_DEPTH ? this.mDepthConfigurations : this.mConfigurations;
        int i = HAL_DATASPACE_UNKNOWN;
        while (i < configurations.length) {
            if (configurations[i].getFormat() == format && configurations[i].getSize().equals(size)) {
                return true;
            }
            i += DURATION_STALL;
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("StreamConfiguration(");
        appendOutputsString(sb);
        sb.append(", ");
        appendHighResOutputsString(sb);
        sb.append(", ");
        appendInputsString(sb);
        sb.append(", ");
        appendValidOutputFormatsForInputString(sb);
        sb.append(", ");
        appendHighSpeedVideoConfigurationsString(sb);
        sb.append(")");
        return sb.toString();
    }

    private void appendOutputsString(StringBuilder sb) {
        sb.append("Outputs(");
        int[] formats = getOutputFormats();
        int length = formats.length;
        for (int i = HAL_DATASPACE_UNKNOWN; i < length; i += DURATION_STALL) {
            int format = formats[i];
            Size[] sizes = getOutputSizes(format);
            int length2 = sizes.length;
            for (int i2 = HAL_DATASPACE_UNKNOWN; i2 < length2; i2 += DURATION_STALL) {
                Size size = sizes[i2];
                long minFrameDuration = getOutputMinFrameDuration(format, size);
                long stallDuration = getOutputStallDuration(format, size);
                StringBuilder stringBuilder = sb;
                stringBuilder.append(String.format("[w:%d, h:%d, format:%s(%d), min_duration:%d, stall:%d], ", new Object[]{Integer.valueOf(size.getWidth()), Integer.valueOf(size.getHeight()), formatToString(format), Integer.valueOf(format), Long.valueOf(minFrameDuration), Long.valueOf(stallDuration)}));
            }
        }
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(")");
    }

    private void appendHighResOutputsString(StringBuilder sb) {
        sb.append("HighResolutionOutputs(");
        int[] formats = getOutputFormats();
        int length = formats.length;
        for (int i = HAL_DATASPACE_UNKNOWN; i < length; i += DURATION_STALL) {
            int format = formats[i];
            Size[] sizes = getHighResolutionOutputSizes(format);
            if (sizes != null) {
                int length2 = sizes.length;
                for (int i2 = HAL_DATASPACE_UNKNOWN; i2 < length2; i2 += DURATION_STALL) {
                    Size size = sizes[i2];
                    long minFrameDuration = getOutputMinFrameDuration(format, size);
                    long stallDuration = getOutputStallDuration(format, size);
                    StringBuilder stringBuilder = sb;
                    stringBuilder.append(String.format("[w:%d, h:%d, format:%s(%d), min_duration:%d, stall:%d], ", new Object[]{Integer.valueOf(size.getWidth()), Integer.valueOf(size.getHeight()), formatToString(format), Integer.valueOf(format), Long.valueOf(minFrameDuration), Long.valueOf(stallDuration)}));
                }
            }
        }
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(")");
    }

    private void appendInputsString(StringBuilder sb) {
        sb.append("Inputs(");
        int[] formats = getInputFormats();
        int length = formats.length;
        for (int i = HAL_DATASPACE_UNKNOWN; i < length; i += DURATION_STALL) {
            Size[] sizes = getInputSizes(formats[i]);
            int length2 = sizes.length;
            for (int i2 = HAL_DATASPACE_UNKNOWN; i2 < length2; i2 += DURATION_STALL) {
                Size size = sizes[i2];
                sb.append(String.format("[w:%d, h:%d, format:%s(%d)], ", new Object[]{Integer.valueOf(size.getWidth()), Integer.valueOf(size.getHeight()), formatToString(format), Integer.valueOf(format)}));
            }
        }
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(")");
    }

    private void appendValidOutputFormatsForInputString(StringBuilder sb) {
        sb.append("ValidOutputFormatsForInput(");
        int[] inputFormats = getInputFormats();
        int length = inputFormats.length;
        for (int i = HAL_DATASPACE_UNKNOWN; i < length; i += DURATION_STALL) {
            sb.append(String.format("[in:%s(%d), out:", new Object[]{formatToString(inputFormat), Integer.valueOf(inputFormats[i])}));
            int[] outputFormats = getValidOutputFormatsForInput(inputFormat);
            for (int i2 = HAL_DATASPACE_UNKNOWN; i2 < outputFormats.length; i2 += DURATION_STALL) {
                sb.append(String.format("%s(%d)", new Object[]{formatToString(outputFormats[i2]), Integer.valueOf(outputFormats[i2])}));
                if (i2 < outputFormats.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("], ");
        }
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(")");
    }

    private void appendHighSpeedVideoConfigurationsString(StringBuilder sb) {
        sb.append("HighSpeedVideoConfigurations(");
        Size[] sizes = getHighSpeedVideoSizes();
        int length = sizes.length;
        for (int i = HAL_DATASPACE_UNKNOWN; i < length; i += DURATION_STALL) {
            Range<Integer>[] ranges = getHighSpeedVideoFpsRangesFor(sizes[i]);
            int length2 = ranges.length;
            for (int i2 = HAL_DATASPACE_UNKNOWN; i2 < length2; i2 += DURATION_STALL) {
                Range<Integer> range = ranges[i2];
                sb.append(String.format("[w:%d, h:%d, min_fps:%d, max_fps:%d], ", new Object[]{Integer.valueOf(size.getWidth()), Integer.valueOf(size.getHeight()), range.getLower(), range.getUpper()}));
            }
        }
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(")");
    }

    private String formatToString(int format) {
        switch (format) {
            case DURATION_STALL /*1*/:
                return "RGBA_8888";
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                return "RGBX_8888";
            case Engine.DEFAULT_STREAM /*3*/:
                return "RGB_888";
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                return "RGB_565";
            case HAL_DATASPACE_STANDARD_SHIFT /*16*/:
                return "NV16";
            case HwSysResource.CURSOR /*17*/:
                return "NV21";
            case HwSysResource.MEMORY /*20*/:
                return "YUY2";
            case HAL_PIXEL_FORMAT_RAW16 /*32*/:
                return "RAW_SENSOR";
            case HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED /*34*/:
                return "PRIVATE";
            case HAL_PIXEL_FORMAT_YCbCr_420_888 /*35*/:
                return "YUV_420_888";
            case HAL_PIXEL_FORMAT_RAW_OPAQUE /*36*/:
                return "RAW_PRIVATE";
            case HAL_PIXEL_FORMAT_RAW10 /*37*/:
                return "RAW10";
            case TriangleMeshBuilder.TEXTURE_0 /*256*/:
                return "JPEG";
            case AnqpInformationElement.ANQP_CAPABILITY_LIST /*257*/:
                return "DEPTH_POINT_CLOUD";
            case ImageFormat.Y8 /*538982489*/:
                return "Y8";
            case HAL_PIXEL_FORMAT_Y16 /*540422489*/:
                return "Y16";
            case ImageFormat.YV12 /*842094169*/:
                return "YV12";
            case ImageFormat.DEPTH16 /*1144402265*/:
                return "DEPTH16";
            default:
                return "UNKNOWN";
        }
    }
}
