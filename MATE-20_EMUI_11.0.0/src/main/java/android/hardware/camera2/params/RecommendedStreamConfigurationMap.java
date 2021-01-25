package android.hardware.camera2.params;

import android.util.ArraySet;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public final class RecommendedStreamConfigurationMap {
    public static final int MAX_USECASE_COUNT = 32;
    private static final String TAG = "RecommendedStreamConfigurationMap";
    public static final int USECASE_LOW_LATENCY_SNAPSHOT = 6;
    public static final int USECASE_PREVIEW = 0;
    public static final int USECASE_RAW = 5;
    public static final int USECASE_RECORD = 1;
    public static final int USECASE_SNAPSHOT = 3;
    public static final int USECASE_VENDOR_START = 24;
    public static final int USECASE_VIDEO_SNAPSHOT = 2;
    public static final int USECASE_ZSL = 4;
    private StreamConfigurationMap mRecommendedMap;
    private boolean mSupportsPrivate;
    private int mUsecase;

    @Retention(RetentionPolicy.SOURCE)
    public @interface RecommendedUsecase {
    }

    public RecommendedStreamConfigurationMap(StreamConfigurationMap recommendedMap, int usecase, boolean supportsPrivate) {
        this.mRecommendedMap = recommendedMap;
        this.mUsecase = usecase;
        this.mSupportsPrivate = supportsPrivate;
    }

    public int getRecommendedUseCase() {
        return this.mUsecase;
    }

    private Set<Integer> getUnmodifiableIntegerSet(int[] intArray) {
        if (intArray == null || intArray.length <= 0) {
            return null;
        }
        ArraySet<Integer> integerSet = new ArraySet<>();
        integerSet.ensureCapacity(intArray.length);
        for (int intEntry : intArray) {
            integerSet.add(Integer.valueOf(intEntry));
        }
        return Collections.unmodifiableSet(integerSet);
    }

    public Set<Integer> getOutputFormats() {
        return getUnmodifiableIntegerSet(this.mRecommendedMap.getOutputFormats());
    }

    public Set<Integer> getValidOutputFormatsForInput(int inputFormat) {
        return getUnmodifiableIntegerSet(this.mRecommendedMap.getValidOutputFormatsForInput(inputFormat));
    }

    public Set<Integer> getInputFormats() {
        return getUnmodifiableIntegerSet(this.mRecommendedMap.getInputFormats());
    }

    private Set<Size> getUnmodifiableSizeSet(Size[] sizeArray) {
        if (sizeArray == null || sizeArray.length <= 0) {
            return null;
        }
        ArraySet<Size> sizeSet = new ArraySet<>();
        sizeSet.addAll(Arrays.asList(sizeArray));
        return Collections.unmodifiableSet(sizeSet);
    }

    public Set<Size> getInputSizes(int format) {
        return getUnmodifiableSizeSet(this.mRecommendedMap.getInputSizes(format));
    }

    public boolean isOutputSupportedFor(int format) {
        return this.mRecommendedMap.isOutputSupportedFor(format);
    }

    public Set<Size> getOutputSizes(int format) {
        return getUnmodifiableSizeSet(this.mRecommendedMap.getOutputSizes(format));
    }

    public Set<Size> getHighSpeedVideoSizes() {
        return getUnmodifiableSizeSet(this.mRecommendedMap.getHighSpeedVideoSizes());
    }

    private Set<Range<Integer>> getUnmodifiableRangeSet(Range<Integer>[] rangeArray) {
        if (rangeArray == null || rangeArray.length <= 0) {
            return null;
        }
        ArraySet<Range<Integer>> rangeSet = new ArraySet<>();
        rangeSet.addAll(Arrays.asList(rangeArray));
        return Collections.unmodifiableSet(rangeSet);
    }

    public Set<Range<Integer>> getHighSpeedVideoFpsRangesFor(Size size) {
        return getUnmodifiableRangeSet(this.mRecommendedMap.getHighSpeedVideoFpsRangesFor(size));
    }

    public Set<Range<Integer>> getHighSpeedVideoFpsRanges() {
        return getUnmodifiableRangeSet(this.mRecommendedMap.getHighSpeedVideoFpsRanges());
    }

    public Set<Size> getHighSpeedVideoSizesFor(Range<Integer> fpsRange) {
        return getUnmodifiableSizeSet(this.mRecommendedMap.getHighSpeedVideoSizesFor(fpsRange));
    }

    public Set<Size> getHighResolutionOutputSizes(int format) {
        return getUnmodifiableSizeSet(this.mRecommendedMap.getHighResolutionOutputSizes(format));
    }

    public long getOutputMinFrameDuration(int format, Size size) {
        return this.mRecommendedMap.getOutputMinFrameDuration(format, size);
    }

    public long getOutputStallDuration(int format, Size size) {
        return this.mRecommendedMap.getOutputStallDuration(format, size);
    }

    public <T> Set<Size> getOutputSizes(Class<T> klass) {
        if (this.mSupportsPrivate) {
            return getUnmodifiableSizeSet(this.mRecommendedMap.getOutputSizes(klass));
        }
        return null;
    }

    public <T> long getOutputMinFrameDuration(Class<T> klass, Size size) {
        if (this.mSupportsPrivate) {
            return this.mRecommendedMap.getOutputMinFrameDuration(klass, size);
        }
        return 0;
    }

    public <T> long getOutputStallDuration(Class<T> klass, Size size) {
        if (this.mSupportsPrivate) {
            return this.mRecommendedMap.getOutputStallDuration(klass, size);
        }
        return 0;
    }

    public boolean isOutputSupportedFor(Surface surface) {
        return this.mRecommendedMap.isOutputSupportedFor(surface);
    }
}
