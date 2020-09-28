package android.hardware.camera2.params;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.hardware.camera2.utils.SurfaceUtils;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.renderscript.Allocation;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.telephony.IccCardConstants;
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
    private static final int HAL_DATASPACE_DYNAMIC_DEPTH = 4098;
    private static final int HAL_DATASPACE_HEIF = 4099;
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
    private final StreamConfiguration[] mDynamicDepthConfigurations;
    private final StreamConfigurationDuration[] mDynamicDepthMinFrameDurations;
    private final SparseIntArray mDynamicDepthOutputFormats;
    private final StreamConfigurationDuration[] mDynamicDepthStallDurations;
    private final StreamConfiguration[] mHeicConfigurations;
    private final StreamConfigurationDuration[] mHeicMinFrameDurations;
    private final SparseIntArray mHeicOutputFormats;
    private final StreamConfigurationDuration[] mHeicStallDurations;
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

    public StreamConfigurationMap(StreamConfiguration[] configurations, StreamConfigurationDuration[] minFrameDurations, StreamConfigurationDuration[] stallDurations, StreamConfiguration[] depthConfigurations, StreamConfigurationDuration[] depthMinFrameDurations, StreamConfigurationDuration[] depthStallDurations, StreamConfiguration[] dynamicDepthConfigurations, StreamConfigurationDuration[] dynamicDepthMinFrameDurations, StreamConfigurationDuration[] dynamicDepthStallDurations, StreamConfiguration[] heicConfigurations, StreamConfigurationDuration[] heicMinFrameDurations, StreamConfigurationDuration[] heicStallDurations, HighSpeedVideoConfiguration[] highSpeedVideoConfigurations, ReprocessFormatsMap inputOutputFormatsMap, boolean listHighResolution) {
        this(configurations, minFrameDurations, stallDurations, depthConfigurations, depthMinFrameDurations, depthStallDurations, dynamicDepthConfigurations, dynamicDepthMinFrameDurations, dynamicDepthStallDurations, heicConfigurations, heicMinFrameDurations, heicStallDurations, highSpeedVideoConfigurations, inputOutputFormatsMap, listHighResolution, true);
    }

    public StreamConfigurationMap(StreamConfiguration[] configurations, StreamConfigurationDuration[] minFrameDurations, StreamConfigurationDuration[] stallDurations, StreamConfiguration[] depthConfigurations, StreamConfigurationDuration[] depthMinFrameDurations, StreamConfigurationDuration[] depthStallDurations, StreamConfiguration[] dynamicDepthConfigurations, StreamConfigurationDuration[] dynamicDepthMinFrameDurations, StreamConfigurationDuration[] dynamicDepthStallDurations, StreamConfiguration[] heicConfigurations, StreamConfigurationDuration[] heicMinFrameDurations, StreamConfigurationDuration[] heicStallDurations, HighSpeedVideoConfiguration[] highSpeedVideoConfigurations, ReprocessFormatsMap inputOutputFormatsMap, boolean listHighResolution, boolean enforceImplementationDefined) {
        int i;
        StreamConfiguration[] streamConfigurationArr;
        int i2;
        StreamConfiguration[] streamConfigurationArr2;
        int i3;
        StreamConfiguration[] streamConfigurationArr3;
        SparseIntArray map;
        int i4;
        this.mOutputFormats = new SparseIntArray();
        this.mHighResOutputFormats = new SparseIntArray();
        this.mAllOutputFormats = new SparseIntArray();
        this.mInputFormats = new SparseIntArray();
        this.mDepthOutputFormats = new SparseIntArray();
        this.mDynamicDepthOutputFormats = new SparseIntArray();
        this.mHeicOutputFormats = new SparseIntArray();
        this.mHighSpeedVideoSizeMap = new HashMap<>();
        this.mHighSpeedVideoFpsRangeMap = new HashMap<>();
        if (configurations == null && depthConfigurations == null && heicConfigurations == null) {
            throw new NullPointerException("At least one of color/depth/heic configurations must not be null");
        }
        if (configurations == null) {
            this.mConfigurations = new StreamConfiguration[0];
            this.mMinFrameDurations = new StreamConfigurationDuration[0];
            this.mStallDurations = new StreamConfigurationDuration[0];
        } else {
            this.mConfigurations = (StreamConfiguration[]) Preconditions.checkArrayElementsNotNull(configurations, "configurations");
            this.mMinFrameDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(minFrameDurations, "minFrameDurations");
            this.mStallDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(stallDurations, "stallDurations");
        }
        this.mListHighResolution = listHighResolution;
        if (depthConfigurations == null) {
            this.mDepthConfigurations = new StreamConfiguration[0];
            this.mDepthMinFrameDurations = new StreamConfigurationDuration[0];
            this.mDepthStallDurations = new StreamConfigurationDuration[0];
        } else {
            this.mDepthConfigurations = (StreamConfiguration[]) Preconditions.checkArrayElementsNotNull(depthConfigurations, "depthConfigurations");
            this.mDepthMinFrameDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(depthMinFrameDurations, "depthMinFrameDurations");
            this.mDepthStallDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(depthStallDurations, "depthStallDurations");
        }
        if (dynamicDepthConfigurations == null) {
            this.mDynamicDepthConfigurations = new StreamConfiguration[0];
            this.mDynamicDepthMinFrameDurations = new StreamConfigurationDuration[0];
            this.mDynamicDepthStallDurations = new StreamConfigurationDuration[0];
        } else {
            this.mDynamicDepthConfigurations = (StreamConfiguration[]) Preconditions.checkArrayElementsNotNull(dynamicDepthConfigurations, "dynamicDepthConfigurations");
            this.mDynamicDepthMinFrameDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(dynamicDepthMinFrameDurations, "dynamicDepthMinFrameDurations");
            this.mDynamicDepthStallDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(dynamicDepthStallDurations, "dynamicDepthStallDurations");
        }
        if (heicConfigurations == null) {
            this.mHeicConfigurations = new StreamConfiguration[0];
            this.mHeicMinFrameDurations = new StreamConfigurationDuration[0];
            this.mHeicStallDurations = new StreamConfigurationDuration[0];
        } else {
            this.mHeicConfigurations = (StreamConfiguration[]) Preconditions.checkArrayElementsNotNull(heicConfigurations, "heicConfigurations");
            this.mHeicMinFrameDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(heicMinFrameDurations, "heicMinFrameDurations");
            this.mHeicStallDurations = (StreamConfigurationDuration[]) Preconditions.checkArrayElementsNotNull(heicStallDurations, "heicStallDurations");
        }
        if (highSpeedVideoConfigurations == null) {
            this.mHighSpeedVideoConfigurations = new HighSpeedVideoConfiguration[0];
        } else {
            this.mHighSpeedVideoConfigurations = (HighSpeedVideoConfiguration[]) Preconditions.checkArrayElementsNotNull(highSpeedVideoConfigurations, "highSpeedVideoConfigurations");
        }
        StreamConfiguration[] streamConfigurationArr4 = this.mConfigurations;
        int length = streamConfigurationArr4.length;
        int i5 = 0;
        while (i5 < length) {
            StreamConfiguration config = streamConfigurationArr4[i5];
            int fmt = config.getFormat();
            if (config.isOutput()) {
                SparseIntArray sparseIntArray = this.mAllOutputFormats;
                sparseIntArray.put(fmt, sparseIntArray.get(fmt) + 1);
                long duration = 0;
                if (this.mListHighResolution) {
                    StreamConfigurationDuration[] streamConfigurationDurationArr = this.mMinFrameDurations;
                    int length2 = streamConfigurationDurationArr.length;
                    int i6 = 0;
                    while (true) {
                        if (i6 >= length2) {
                            break;
                        }
                        StreamConfigurationDuration configurationDuration = streamConfigurationDurationArr[i6];
                        if (configurationDuration.getFormat() == fmt) {
                            i4 = length2;
                            if (configurationDuration.getWidth() == config.getSize().getWidth() && configurationDuration.getHeight() == config.getSize().getHeight()) {
                                duration = configurationDuration.getDuration();
                                break;
                            }
                        } else {
                            i4 = length2;
                        }
                        i6++;
                        streamConfigurationDurationArr = streamConfigurationDurationArr;
                        length2 = i4;
                    }
                }
                map = duration <= DURATION_20FPS_NS ? this.mOutputFormats : this.mHighResOutputFormats;
            } else {
                map = this.mInputFormats;
            }
            map.put(fmt, map.get(fmt) + 1);
            i5++;
            streamConfigurationArr4 = streamConfigurationArr4;
        }
        StreamConfiguration[] streamConfigurationArr5 = this.mDepthConfigurations;
        int length3 = streamConfigurationArr5.length;
        int i7 = 0;
        while (i7 < length3) {
            StreamConfiguration config2 = streamConfigurationArr5[i7];
            if (!config2.isOutput()) {
                streamConfigurationArr3 = streamConfigurationArr5;
                i3 = length3;
            } else {
                streamConfigurationArr3 = streamConfigurationArr5;
                i3 = length3;
                this.mDepthOutputFormats.put(config2.getFormat(), this.mDepthOutputFormats.get(config2.getFormat()) + 1);
            }
            i7++;
            streamConfigurationArr5 = streamConfigurationArr3;
            length3 = i3;
        }
        StreamConfiguration[] streamConfigurationArr6 = this.mDynamicDepthConfigurations;
        int length4 = streamConfigurationArr6.length;
        int i8 = 0;
        while (i8 < length4) {
            StreamConfiguration config3 = streamConfigurationArr6[i8];
            if (!config3.isOutput()) {
                streamConfigurationArr2 = streamConfigurationArr6;
                i2 = length4;
            } else {
                streamConfigurationArr2 = streamConfigurationArr6;
                i2 = length4;
                this.mDynamicDepthOutputFormats.put(config3.getFormat(), this.mDynamicDepthOutputFormats.get(config3.getFormat()) + 1);
            }
            i8++;
            streamConfigurationArr6 = streamConfigurationArr2;
            length4 = i2;
        }
        StreamConfiguration[] streamConfigurationArr7 = this.mHeicConfigurations;
        int length5 = streamConfigurationArr7.length;
        int i9 = 0;
        while (i9 < length5) {
            StreamConfiguration config4 = streamConfigurationArr7[i9];
            if (!config4.isOutput()) {
                streamConfigurationArr = streamConfigurationArr7;
                i = length5;
            } else {
                streamConfigurationArr = streamConfigurationArr7;
                i = length5;
                this.mHeicOutputFormats.put(config4.getFormat(), this.mHeicOutputFormats.get(config4.getFormat()) + 1);
            }
            i9++;
            streamConfigurationArr7 = streamConfigurationArr;
            length5 = i;
        }
        if (configurations == null || !enforceImplementationDefined || this.mOutputFormats.indexOfKey(34) >= 0) {
            HighSpeedVideoConfiguration[] highSpeedVideoConfigurationArr = this.mHighSpeedVideoConfigurations;
            int length6 = highSpeedVideoConfigurationArr.length;
            int i10 = 0;
            while (i10 < length6) {
                HighSpeedVideoConfiguration config5 = highSpeedVideoConfigurationArr[i10];
                Size size = config5.getSize();
                Range<Integer> fpsRange = config5.getFpsRange();
                Integer fpsRangeCount = this.mHighSpeedVideoSizeMap.get(size);
                this.mHighSpeedVideoSizeMap.put(size, Integer.valueOf((fpsRangeCount == null ? 0 : fpsRangeCount).intValue() + 1));
                Integer sizeCount = this.mHighSpeedVideoFpsRangeMap.get(fpsRange);
                if (sizeCount == null) {
                    sizeCount = 0;
                }
                this.mHighSpeedVideoFpsRangeMap.put(fpsRange, Integer.valueOf(sizeCount.intValue() + 1));
                i10++;
                highSpeedVideoConfigurationArr = highSpeedVideoConfigurationArr;
            }
            this.mInputOutputFormatsMap = inputOutputFormatsMap;
            return;
        }
        throw new AssertionError("At least one stream configuration for IMPLEMENTATION_DEFINED must exist");
    }

    public int[] getOutputFormats() {
        return getPublicFormats(true);
    }

    public int[] getValidOutputFormatsForInput(int inputFormat) {
        ReprocessFormatsMap reprocessFormatsMap = this.mInputOutputFormatsMap;
        if (reprocessFormatsMap == null) {
            return new int[0];
        }
        int[] outputs = reprocessFormatsMap.getOutputs(inputFormat);
        if (this.mHeicOutputFormats.size() <= 0) {
            return outputs;
        }
        int[] outputsWithHeic = Arrays.copyOf(outputs, outputs.length + 1);
        outputsWithHeic[outputs.length] = 1212500294;
        return outputsWithHeic;
    }

    public int[] getInputFormats() {
        return getPublicFormats(false);
    }

    public Size[] getInputSizes(int format) {
        return getPublicFormatSizes(format, false, false);
    }

    public boolean isOutputSupportedFor(int format) {
        checkArgumentFormat(format);
        int internalFormat = imageFormatToInternal(format);
        int dataspace = imageFormatToDataspace(format);
        return dataspace == 4096 ? this.mDepthOutputFormats.indexOfKey(internalFormat) >= 0 : dataspace == 4098 ? this.mDynamicDepthOutputFormats.indexOfKey(internalFormat) >= 0 : dataspace == 4099 ? this.mHeicOutputFormats.indexOfKey(internalFormat) >= 0 : getFormatsMap(true).indexOfKey(internalFormat) >= 0;
    }

    public static <T> boolean isOutputSupportedFor(Class<T> klass) {
        Preconditions.checkNotNull(klass, "klass must not be null");
        if (klass == ImageReader.class || klass == MediaRecorder.class || klass == MediaCodec.class || klass == Allocation.class || klass == SurfaceHolder.class || klass == SurfaceTexture.class) {
            return true;
        }
        return false;
    }

    public boolean isOutputSupportedFor(Surface surface) {
        StreamConfiguration[] configs;
        Preconditions.checkNotNull(surface, "surface must not be null");
        Size surfaceSize = SurfaceUtils.getSurfaceSize(surface);
        int surfaceFormat = SurfaceUtils.getSurfaceFormat(surface);
        int surfaceDataspace = SurfaceUtils.getSurfaceDataspace(surface);
        boolean isFlexible = SurfaceUtils.isFlexibleConsumer(surface);
        if (surfaceDataspace == 4096) {
            configs = this.mDepthConfigurations;
        } else if (surfaceDataspace == 4098) {
            configs = this.mDynamicDepthConfigurations;
        } else if (surfaceDataspace == 4099) {
            configs = this.mHeicConfigurations;
        } else {
            configs = this.mConfigurations;
        }
        for (StreamConfiguration config : configs) {
            if (config.getFormat() == surfaceFormat && config.isOutput()) {
                if (config.getSize().equals(surfaceSize)) {
                    return true;
                }
                if (isFlexible && config.getSize().getWidth() <= 1920) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isOutputSupportedFor(Size size, int format) {
        StreamConfiguration[] configs;
        int internalFormat = imageFormatToInternal(format);
        int dataspace = imageFormatToDataspace(format);
        if (dataspace == 4096) {
            configs = this.mDepthConfigurations;
        } else if (dataspace == 4098) {
            configs = this.mDynamicDepthConfigurations;
        } else if (dataspace == 4099) {
            configs = this.mHeicConfigurations;
        } else {
            configs = this.mConfigurations;
        }
        for (StreamConfiguration config : configs) {
            if (config.getFormat() == internalFormat && config.isOutput() && config.getSize().equals(size)) {
                return true;
            }
        }
        return false;
    }

    public <T> Size[] getOutputSizes(Class<T> klass) {
        if (!isOutputSupportedFor(klass)) {
            return null;
        }
        return getInternalFormatSizes(34, 0, true, false);
    }

    public Size[] getOutputSizes(int format) {
        return getPublicFormatSizes(format, true, false);
    }

    public Size[] getHighSpeedVideoSizes() {
        Set<Size> keySet = this.mHighSpeedVideoSizeMap.keySet();
        return (Size[]) keySet.toArray(new Size[keySet.size()]);
    }

    public Range<Integer>[] getHighSpeedVideoFpsRangesFor(Size size) {
        Integer fpsRangeCount = this.mHighSpeedVideoSizeMap.get(size);
        if (fpsRangeCount == null || fpsRangeCount.intValue() == 0) {
            throw new IllegalArgumentException(String.format("Size %s does not support high speed video recording", size));
        }
        Range<Integer>[] fpsRanges = new Range[fpsRangeCount.intValue()];
        int i = 0;
        HighSpeedVideoConfiguration[] highSpeedVideoConfigurationArr = this.mHighSpeedVideoConfigurations;
        for (HighSpeedVideoConfiguration config : highSpeedVideoConfigurationArr) {
            if (size.equals(config.getSize())) {
                fpsRanges[i] = config.getFpsRange();
                i++;
            }
        }
        return fpsRanges;
    }

    public Range<Integer>[] getHighSpeedVideoFpsRanges() {
        Set<Range<Integer>> keySet = this.mHighSpeedVideoFpsRangeMap.keySet();
        return (Range[]) keySet.toArray(new Range[keySet.size()]);
    }

    public Size[] getHighSpeedVideoSizesFor(Range<Integer> fpsRange) {
        Integer sizeCount = this.mHighSpeedVideoFpsRangeMap.get(fpsRange);
        if (sizeCount == null || sizeCount.intValue() == 0) {
            throw new IllegalArgumentException(String.format("FpsRange %s does not support high speed video recording", fpsRange));
        }
        Size[] sizes = new Size[sizeCount.intValue()];
        int i = 0;
        HighSpeedVideoConfiguration[] highSpeedVideoConfigurationArr = this.mHighSpeedVideoConfigurations;
        for (HighSpeedVideoConfiguration config : highSpeedVideoConfigurationArr) {
            if (fpsRange.equals(config.getFpsRange())) {
                sizes[i] = config.getSize();
                i++;
            }
        }
        return sizes;
    }

    public Size[] getHighResolutionOutputSizes(int format) {
        if (!this.mListHighResolution) {
            return null;
        }
        return getPublicFormatSizes(format, true, true);
    }

    public long getOutputMinFrameDuration(int format, Size size) {
        Preconditions.checkNotNull(size, "size must not be null");
        checkArgumentFormatSupported(format, true);
        return getInternalFormatDuration(imageFormatToInternal(format), imageFormatToDataspace(format), size, 0);
    }

    public <T> long getOutputMinFrameDuration(Class<T> klass, Size size) {
        if (isOutputSupportedFor(klass)) {
            return getInternalFormatDuration(34, 0, size, 0);
        }
        throw new IllegalArgumentException("klass was not supported");
    }

    public long getOutputStallDuration(int format, Size size) {
        checkArgumentFormatSupported(format, true);
        return getInternalFormatDuration(imageFormatToInternal(format), imageFormatToDataspace(format), size, 1);
    }

    public <T> long getOutputStallDuration(Class<T> klass, Size size) {
        if (isOutputSupportedFor(klass)) {
            return getInternalFormatDuration(34, 0, size, 1);
        }
        throw new IllegalArgumentException("klass was not supported");
    }

    public boolean equals(Object obj) {
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
        if (!Arrays.equals(this.mConfigurations, other.mConfigurations) || !Arrays.equals(this.mMinFrameDurations, other.mMinFrameDurations) || !Arrays.equals(this.mStallDurations, other.mStallDurations) || !Arrays.equals(this.mDepthConfigurations, other.mDepthConfigurations) || !Arrays.equals(this.mDepthMinFrameDurations, other.mDepthMinFrameDurations) || !Arrays.equals(this.mDepthStallDurations, other.mDepthStallDurations) || !Arrays.equals(this.mDynamicDepthConfigurations, other.mDynamicDepthConfigurations) || !Arrays.equals(this.mDynamicDepthMinFrameDurations, other.mDynamicDepthMinFrameDurations) || !Arrays.equals(this.mDynamicDepthStallDurations, other.mDynamicDepthStallDurations) || !Arrays.equals(this.mHeicConfigurations, other.mHeicConfigurations) || !Arrays.equals(this.mHeicMinFrameDurations, other.mHeicMinFrameDurations) || !Arrays.equals(this.mHeicStallDurations, other.mHeicStallDurations) || !Arrays.equals(this.mHighSpeedVideoConfigurations, other.mHighSpeedVideoConfigurations)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCodeGeneric(this.mConfigurations, this.mMinFrameDurations, this.mStallDurations, this.mDepthConfigurations, this.mDepthMinFrameDurations, this.mDepthStallDurations, this.mDynamicDepthConfigurations, this.mDynamicDepthMinFrameDurations, this.mDynamicDepthStallDurations, this.mHeicConfigurations, this.mHeicMinFrameDurations, this.mHeicStallDurations, this.mHighSpeedVideoConfigurations);
    }

    private int checkArgumentFormatSupported(int format, boolean output) {
        checkArgumentFormat(format);
        int internalFormat = imageFormatToInternal(format);
        int internalDataspace = imageFormatToDataspace(format);
        if (output) {
            if (internalDataspace == 4096) {
                if (this.mDepthOutputFormats.indexOfKey(internalFormat) >= 0) {
                    return format;
                }
            } else if (internalDataspace == 4098) {
                if (this.mDynamicDepthOutputFormats.indexOfKey(internalFormat) >= 0) {
                    return format;
                }
            } else if (internalDataspace == 4099) {
                if (this.mHeicOutputFormats.indexOfKey(internalFormat) >= 0) {
                    return format;
                }
            } else if (this.mAllOutputFormats.indexOfKey(internalFormat) >= 0) {
                return format;
            }
        } else if (this.mInputFormats.indexOfKey(internalFormat) >= 0) {
            return format;
        }
        throw new IllegalArgumentException(String.format("format %x is not supported by this stream configuration map", Integer.valueOf(format)));
    }

    static int checkArgumentFormatInternal(int format) {
        if (!(format == 33 || format == 34 || format == 36)) {
            if (format != 256) {
                if (format != 540422489) {
                    if (format != 1212500294) {
                        return checkArgumentFormat(format);
                    }
                }
            }
            throw new IllegalArgumentException("An unknown internal format: " + format);
        }
        return format;
    }

    static int checkArgumentFormat(int format) {
        if (ImageFormat.isPublicFormat(format) || PixelFormat.isPublicFormat(format)) {
            return format;
        }
        throw new IllegalArgumentException(String.format("format 0x%x was not defined in either ImageFormat or PixelFormat", Integer.valueOf(format)));
    }

    public static int imageFormatToPublic(int format) {
        if (format == 33) {
            return 256;
        }
        if (format != 256) {
            return format;
        }
        throw new IllegalArgumentException("ImageFormat.JPEG is an unknown internal format");
    }

    public static int depthFormatToPublic(int format) {
        if (format == 256) {
            throw new IllegalArgumentException("ImageFormat.JPEG is an unknown internal format");
        } else if (format == 540422489) {
            return ImageFormat.DEPTH16;
        } else {
            switch (format) {
                case 32:
                    return 4098;
                case 33:
                    return 257;
                case 34:
                    throw new IllegalArgumentException("IMPLEMENTATION_DEFINED must not leak to public API");
                default:
                    throw new IllegalArgumentException("Unknown DATASPACE_DEPTH format " + format);
            }
        }
    }

    static int[] imageFormatToPublic(int[] formats) {
        if (formats == null) {
            return null;
        }
        for (int i = 0; i < formats.length; i++) {
            formats[i] = imageFormatToPublic(formats[i]);
        }
        return formats;
    }

    static int imageFormatToInternal(int format) {
        if (format == 256 || format == 257) {
            return 33;
        }
        if (format == 4098) {
            return 32;
        }
        if (format == 1144402265) {
            return 540422489;
        }
        if (format == 1212500294 || format == 1768253795) {
            return 33;
        }
        return format;
    }

    static int imageFormatToDataspace(int format) {
        if (format == 256) {
            return HAL_DATASPACE_V0_JFIF;
        }
        if (format == 257 || format == 4098 || format == 1144402265) {
            return 4096;
        }
        if (format != 1212500294) {
            return format != 1768253795 ? 0 : 4098;
        }
        return 4099;
    }

    public static int[] imageFormatToInternal(int[] formats) {
        if (formats == null) {
            return null;
        }
        for (int i = 0; i < formats.length; i++) {
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
        SparseIntArray formatsMap;
        StreamConfiguration[] configurations;
        StreamConfigurationDuration[] minFrameDurations;
        char c;
        StreamConfigurationMap streamConfigurationMap = this;
        int i = format;
        char c2 = 4096;
        if (dataspace == 4096 && highRes) {
            return new Size[0];
        }
        if (!output) {
            formatsMap = streamConfigurationMap.mInputFormats;
        } else if (dataspace == 4096) {
            formatsMap = streamConfigurationMap.mDepthOutputFormats;
        } else if (dataspace == 4098) {
            formatsMap = streamConfigurationMap.mDynamicDepthOutputFormats;
        } else if (dataspace == 4099) {
            formatsMap = streamConfigurationMap.mHeicOutputFormats;
        } else if (highRes) {
            formatsMap = streamConfigurationMap.mHighResOutputFormats;
        } else {
            formatsMap = streamConfigurationMap.mOutputFormats;
        }
        int sizesCount = formatsMap.get(i);
        if ((!output || dataspace == 4096 || dataspace == 4098 || dataspace == 4099) && sizesCount == 0) {
            return null;
        }
        if (output && dataspace != 4096 && dataspace != 4098 && dataspace != 4099 && streamConfigurationMap.mAllOutputFormats.get(i) == 0) {
            return null;
        }
        Size[] sizes = new Size[sizesCount];
        if (dataspace == 4096) {
            configurations = streamConfigurationMap.mDepthConfigurations;
        } else if (dataspace == 4098) {
            configurations = streamConfigurationMap.mDynamicDepthConfigurations;
        } else if (dataspace == 4099) {
            configurations = streamConfigurationMap.mHeicConfigurations;
        } else {
            configurations = streamConfigurationMap.mConfigurations;
        }
        if (dataspace == 4096) {
            minFrameDurations = streamConfigurationMap.mDepthMinFrameDurations;
        } else if (dataspace == 4098) {
            minFrameDurations = streamConfigurationMap.mDynamicDepthMinFrameDurations;
        } else if (dataspace == 4099) {
            minFrameDurations = streamConfigurationMap.mHeicMinFrameDurations;
        } else {
            minFrameDurations = streamConfigurationMap.mMinFrameDurations;
        }
        int length = configurations.length;
        int sizeIndex = 0;
        int sizeIndex2 = 0;
        while (sizeIndex < length) {
            StreamConfiguration config = configurations[sizeIndex];
            int fmt = config.getFormat();
            if (fmt == i && config.isOutput() == output) {
                if (!output || !streamConfigurationMap.mListHighResolution) {
                    c = c2;
                } else {
                    long duration = 0;
                    int i2 = 0;
                    while (true) {
                        if (i2 >= minFrameDurations.length) {
                            break;
                        }
                        StreamConfigurationDuration d = minFrameDurations[i2];
                        if (d.getFormat() == fmt && d.getWidth() == config.getSize().getWidth() && d.getHeight() == config.getSize().getHeight()) {
                            duration = d.getDuration();
                            break;
                        }
                        i2++;
                    }
                    c = 4096;
                    if (dataspace != 4096) {
                        if (highRes != (duration > DURATION_20FPS_NS)) {
                        }
                    }
                }
                sizes[sizeIndex2] = config.getSize();
                sizeIndex2++;
            } else {
                c = c2;
            }
            sizeIndex++;
            i = format;
            c2 = c;
            streamConfigurationMap = this;
        }
        if (sizeIndex2 == sizesCount || !(dataspace == 4098 || dataspace == 4099)) {
            if (sizeIndex2 == sizesCount) {
                return sizes;
            }
            throw new AssertionError("Too few sizes (expected " + sizesCount + ", actual " + sizeIndex2 + ")");
        } else if (sizeIndex2 > sizesCount) {
            throw new AssertionError("Too many dynamic depth sizes (expected " + sizesCount + ", actual " + sizeIndex2 + ")");
        } else if (sizeIndex2 <= 0) {
            return new Size[0];
        } else {
            return (Size[]) Arrays.copyOf(sizes, sizeIndex2);
        }
    }

    private int[] getPublicFormats(boolean output) {
        int[] formats = new int[getPublicFormatCount(output)];
        int i = 0;
        SparseIntArray map = getFormatsMap(output);
        int j = 0;
        while (j < map.size()) {
            formats[i] = imageFormatToPublic(map.keyAt(j));
            j++;
            i++;
        }
        if (output) {
            int j2 = 0;
            while (j2 < this.mDepthOutputFormats.size()) {
                formats[i] = depthFormatToPublic(this.mDepthOutputFormats.keyAt(j2));
                j2++;
                i++;
            }
            if (this.mDynamicDepthOutputFormats.size() > 0) {
                formats[i] = 1768253795;
                i++;
            }
            if (this.mHeicOutputFormats.size() > 0) {
                formats[i] = 1212500294;
                i++;
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
            for (StreamConfigurationDuration configurationDuration : durations) {
                if (configurationDuration.getFormat() == format && configurationDuration.getWidth() == size.getWidth() && configurationDuration.getHeight() == size.getHeight()) {
                    return configurationDuration.getDuration();
                }
            }
            return 0;
        }
        throw new IllegalArgumentException("size was not supported");
    }

    private StreamConfigurationDuration[] getDurations(int duration, int dataspace) {
        if (duration != 0) {
            if (duration != 1) {
                throw new IllegalArgumentException("duration was invalid");
            } else if (dataspace == 4096) {
                return this.mDepthStallDurations;
            } else {
                if (dataspace == 4098) {
                    return this.mDynamicDepthStallDurations;
                }
                if (dataspace == 4099) {
                    return this.mHeicStallDurations;
                }
                return this.mStallDurations;
            }
        } else if (dataspace == 4096) {
            return this.mDepthMinFrameDurations;
        } else {
            if (dataspace == 4098) {
                return this.mDynamicDepthMinFrameDurations;
            }
            if (dataspace == 4099) {
                return this.mHeicMinFrameDurations;
            }
            return this.mMinFrameDurations;
        }
    }

    private int getPublicFormatCount(boolean output) {
        int size = getFormatsMap(output).size();
        if (output) {
            return size + this.mDepthOutputFormats.size() + this.mDynamicDepthOutputFormats.size() + this.mHeicOutputFormats.size();
        }
        return size;
    }

    private static <T> boolean arrayContains(T[] array, T element) {
        if (array == null) {
            return false;
        }
        for (T el : array) {
            if (Objects.equals(el, element)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSupportedInternalConfiguration(int format, int dataspace, Size size) {
        StreamConfiguration[] configurations;
        if (dataspace == 4096) {
            configurations = this.mDepthConfigurations;
        } else if (dataspace == 4098) {
            configurations = this.mDynamicDepthConfigurations;
        } else if (dataspace == 4099) {
            configurations = this.mHeicConfigurations;
        } else {
            configurations = this.mConfigurations;
        }
        for (int i = 0; i < configurations.length; i++) {
            if (configurations[i].getFormat() == format && configurations[i].getSize().equals(size)) {
                return true;
            }
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
        int i = 0;
        int i2 = 0;
        while (i2 < length) {
            int format = formats[i2];
            Size[] sizes = getOutputSizes(format);
            int length2 = sizes.length;
            int i3 = i;
            while (i3 < length2) {
                Size size = sizes[i3];
                long minFrameDuration = getOutputMinFrameDuration(format, size);
                long stallDuration = getOutputStallDuration(format, size);
                Object[] objArr = new Object[6];
                objArr[i] = Integer.valueOf(size.getWidth());
                objArr[1] = Integer.valueOf(size.getHeight());
                objArr[2] = formatToString(format);
                objArr[3] = Integer.valueOf(format);
                objArr[4] = Long.valueOf(minFrameDuration);
                objArr[5] = Long.valueOf(stallDuration);
                sb.append(String.format("[w:%d, h:%d, format:%s(%d), min_duration:%d, stall:%d], ", objArr));
                i3++;
                i = 0;
            }
            i2++;
            i = 0;
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
        int i = 0;
        int i2 = 0;
        while (i2 < length) {
            int format = formats[i2];
            Size[] sizes = getHighResolutionOutputSizes(format);
            if (sizes != null) {
                int length2 = sizes.length;
                int i3 = i;
                while (i3 < length2) {
                    Size size = sizes[i3];
                    long minFrameDuration = getOutputMinFrameDuration(format, size);
                    long stallDuration = getOutputStallDuration(format, size);
                    Object[] objArr = new Object[6];
                    objArr[i] = Integer.valueOf(size.getWidth());
                    objArr[1] = Integer.valueOf(size.getHeight());
                    objArr[2] = formatToString(format);
                    objArr[3] = Integer.valueOf(format);
                    objArr[4] = Long.valueOf(minFrameDuration);
                    objArr[5] = Long.valueOf(stallDuration);
                    sb.append(String.format("[w:%d, h:%d, format:%s(%d), min_duration:%d, stall:%d], ", objArr));
                    i3++;
                    i = 0;
                }
            }
            i2++;
            i = 0;
        }
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(")");
    }

    private void appendInputsString(StringBuilder sb) {
        sb.append("Inputs(");
        int[] formats = getInputFormats();
        for (int format : formats) {
            Size[] sizes = getInputSizes(format);
            for (Size size : sizes) {
                sb.append(String.format("[w:%d, h:%d, format:%s(%d)], ", Integer.valueOf(size.getWidth()), Integer.valueOf(size.getHeight()), formatToString(format), Integer.valueOf(format)));
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
        for (int inputFormat : inputFormats) {
            sb.append(String.format("[in:%s(%d), out:", formatToString(inputFormat), Integer.valueOf(inputFormat)));
            int[] outputFormats = getValidOutputFormatsForInput(inputFormat);
            for (int i = 0; i < outputFormats.length; i++) {
                sb.append(String.format("%s(%d)", formatToString(outputFormats[i]), Integer.valueOf(outputFormats[i])));
                if (i < outputFormats.length - 1) {
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
        for (Size size : sizes) {
            Range<Integer>[] ranges = getHighSpeedVideoFpsRangesFor(size);
            for (Range<Integer> range : ranges) {
                sb.append(String.format("[w:%d, h:%d, min_fps:%d, max_fps:%d], ", Integer.valueOf(size.getWidth()), Integer.valueOf(size.getHeight()), range.getLower(), range.getUpper()));
            }
        }
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(")");
    }

    private String formatToString(int format) {
        if (format == 1) {
            return "RGBA_8888";
        }
        if (format == 2) {
            return "RGBX_8888";
        }
        if (format == 3) {
            return "RGB_888";
        }
        if (format == 4) {
            return "RGB_565";
        }
        if (format == 16) {
            return "NV16";
        }
        if (format == 17) {
            return "NV21";
        }
        if (format == 256) {
            return "JPEG";
        }
        if (format == 257) {
            return "DEPTH_POINT_CLOUD";
        }
        switch (format) {
            case 20:
                return "YUY2";
            case 32:
                return "RAW_SENSOR";
            case 4098:
                return "RAW_DEPTH";
            case ImageFormat.Y8:
                return "Y8";
            case 540422489:
                return "Y16";
            case ImageFormat.YV12:
                return "YV12";
            case ImageFormat.DEPTH16:
                return "DEPTH16";
            case ImageFormat.HEIC:
                return "HEIC";
            case ImageFormat.DEPTH_JPEG:
                return "DEPTH_JPEG";
            default:
                switch (format) {
                    case 34:
                        return "PRIVATE";
                    case 35:
                        return "YUV_420_888";
                    case 36:
                        return "RAW_PRIVATE";
                    case 37:
                        return "RAW10";
                    default:
                        return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
                }
        }
    }
}
