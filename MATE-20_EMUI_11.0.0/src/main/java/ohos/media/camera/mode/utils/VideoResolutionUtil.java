package ohos.media.camera.mode.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.adapter.utils.CustomConfigurationUtil;
import ohos.media.camera.mode.adapter.utils.ScreenUtils;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.codec.CodecDescriptionList;
import ohos.media.common.Format;
import ohos.media.image.common.Size;
import ohos.media.recorder.Recorder;
import ohos.media.recorder.RecorderProfile;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class VideoResolutionUtil {
    private static final String CAMERA_BACK_NAME = CameraManagerHelper.getBackCameraName();
    private static final String CAMERA_FRONT_NAME = CameraManagerHelper.getFrontCameraName();
    private static final int DEFAULT_MAX_HEIGHT = 1088;
    private static final float FLOAT_COMPARISON_TOLERANCE = 0.01f;
    private static final int FRAME_RATE = 30;
    private static final int[] FULL_VIDEO_QUALITY_ARRAYS = {8, 15, 16, 10, 12, 6, 14, 13, 11, 5, 4, 100, 8, 9, 7, 2};
    private static final int INIT_CAPACITY = 16;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(VideoResolutionUtil.class);
    private static final int MAX_RESOLUTION_VALUE = 65536;
    private static final int MIN_SIDE_LENGTH_OF_720P = 720;
    private static final int QUALITY_FWVGA = 8;
    private static final int QUALITY_QHD = 100;
    private static final int QUALITY_WVGA = 9;
    private static final float RATIO_19_2_TO_9 = 2.1333334f;
    private static final float RATIO_19_2_TO_9_THOUSANDTHS = 0.1f;
    private static final float RATIO_19_3_TO_9 = 2.1444445f;
    private static final float RATIO_19_3_TO_9_THOUSANDTHS = 0.05f;
    private static final float RATIO_19_5_TO_9 = 2.1666667f;
    private static final double RATIO_TOLERANCE = 0.05d;
    private static final int SIZE_ELEMENTS = 2;
    private static final String STRING_X = "x";
    private static final Size VIDEO_1080P_1920_880_19_3_TO_9_SIZE = new Size(1920, 880);
    private static final Size VIDEO_1080P_1920_896_19_2_TO_9_SIZE = new Size(1920, 896);
    private static final Size VIDEO_1080P_2304_1080_19_2TO9_SIZE = new Size(2304, 1080);
    private static final String[] VIDEO_18TO9_SIZE_ARRAY_WIDTHS = {"1440", "1080", "720"};
    private static final Size VIDEO_2304_1064_19_5TO9_SIZE = new Size(2304, 1064);
    private static final Size VIDEO_2336_1080_19_5TO9_SIZE = new Size(2336, 1080);
    private static final Size VIDEO_3264_TO_1504_SIZE = new Size(3264, 1504);
    private static final Size VIDEO_3648_TO_1680_SIZE = new Size(3648, 1680);
    private static final Size VIDEO_3840_TO_2160_SIZE = new Size(3840, 2160);
    private static final Size VIDEO_720P_18TO9_SIZE = new Size(1440, MIN_SIDE_LENGTH_OF_720P);
    private static final Size VIDEO_920P_18TO9_SIZE = new Size(1920, 920);
    private static boolean has19Dot3To9Resolution = false;
    private static boolean has19Dot5To9Resolution = false;
    private static boolean has19dDot2To9Resolution = false;
    private static Map<String, List<Size>> recordSupportsCache = new HashMap();
    private static Size video18To9And1080pSize = null;
    private static Size video19Dot2To9Size = null;
    private static Size video19Dot3To9Size = null;
    private static Size video19dot5To9Size = null;
    private static Size video21To9And1080pSize = null;
    private static Size video21To9And720pSize = null;

    private VideoResolutionUtil() {
    }

    public static List<List<Size>> getPreviewSupports(CameraAbilityImpl cameraAbilityImpl, List<Class<?>> list) {
        Logger logger = LOGGER;
        logger.begin("getPreviewSupports " + list);
        if (cameraAbilityImpl == null || list == null) {
            LOGGER.error("getPreviewSupports error, cameraAbility or classList is null", new Object[0]);
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        HashMap hashMap = new HashMap();
        List<Size> recordSupports = getRecordSupports(cameraAbilityImpl);
        for (Class<?> cls : list) {
            if (cls.equals(Recorder.class)) {
                arrayList.add(recordSupports);
            } else {
                List<Size> supportedSizes = cameraAbilityImpl.getSupportedSizes(cls);
                if (supportedSizes == null) {
                    LOGGER.error("previewSupports is null", new Object[0]);
                    arrayList.add(Collections.emptyList());
                } else {
                    String arrays = Arrays.toString(supportedSizes.toArray());
                    LOGGER.info("previewSupports is %{public}s", arrays);
                    if (!hashMap.containsKey(arrays)) {
                        LOGGER.debug("Cache not hit", new Object[0]);
                        ArrayList arrayList2 = new ArrayList();
                        for (Size size : recordSupports) {
                            Optional<Size> optimalVideoPreviewSize = getOptimalVideoPreviewSize(size, supportedSizes);
                            if (optimalVideoPreviewSize.isPresent()) {
                                arrayList2.add(optimalVideoPreviewSize.get());
                            }
                        }
                        hashMap.put(arrays, (List) arrayList2.stream().distinct().collect(Collectors.toList()));
                    } else {
                        LOGGER.debug("Cache hit", new Object[0]);
                    }
                    arrayList.add((List) hashMap.get(arrays));
                    LOGGER.debug("getPreviewSupports, final preview supports is %{public}s", hashMap.get(arrays));
                }
            }
        }
        Logger logger2 = LOGGER;
        logger2.end("getPreviewSupports " + list);
        return arrayList;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00b8, code lost:
        r5.add(ohos.media.camera.mode.utils.CameraUtil.convertSizeToString(r6.width, r6.height));
     */
    public static List<Size> getRecordSupports(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("getRecordSupports");
        if (cameraAbilityImpl == null) {
            LOGGER.error("getRecordSupports error, cameraAbility is null", new Object[0]);
            return Collections.emptyList();
        } else if (recordSupportsCache.containsKey(cameraAbilityImpl.getCameraId())) {
            return recordSupportsCache.get(cameraAbilityImpl.getCameraId());
        } else {
            List<Size> supportedSizes = cameraAbilityImpl.getSupportedSizes(Recorder.class);
            if (supportedSizes == null) {
                LOGGER.error("deviceSupports is null", new Object[0]);
                return Collections.emptyList();
            }
            LOGGER.info("deviceSupports is %{public}s", Arrays.toString(supportedSizes.toArray()));
            video18To9And1080pSize = null;
            video19Dot2To9Size = null;
            video19Dot3To9Size = null;
            video19dot5To9Size = null;
            video21To9And1080pSize = null;
            video21To9And720pSize = null;
            setSpecificVideoSize(supportedSizes, cameraAbilityImpl);
            LinkedHashMap linkedHashMap = new LinkedHashMap(supportedSizes.size());
            initializeProfileSupports(cameraAbilityImpl, linkedHashMap);
            ArrayList<String> arrayList = new ArrayList(16);
            for (Size size : linkedHashMap.keySet()) {
                Iterator<Size> it = supportedSizes.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    } else if (it.next().equals(size) || (size.width == VIDEO_3840_TO_2160_SIZE.width && size.height == VIDEO_3840_TO_2160_SIZE.height)) {
                        break;
                    }
                }
            }
            reduceResolutionIfNeeded(arrayList);
            ArrayList arrayList2 = new ArrayList(arrayList.size());
            for (String str : arrayList) {
                SizeUtil.convertSizeStringToSize(str).ifPresent(new Consumer(arrayList2) {
                    /* class ohos.media.camera.mode.utils.$$Lambda$VideoResolutionUtil$R19XmbqNfJWUEHXud8vLh9eRY */
                    private final /* synthetic */ List f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        this.f$0.add((Size) obj);
                    }
                });
            }
            LOGGER.info("getRecordSupports, recordSupports: %{public}s", arrayList2);
            LOGGER.end("getRecordSupports");
            recordSupportsCache.put(cameraAbilityImpl.getCameraId(), arrayList2);
            return (List) arrayList2.stream().distinct().collect(Collectors.toList());
        }
    }

    public static List<Size> getCaptureSupports(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.error("getCaptureSupports error, cameraAbility is null", new Object[0]);
            return Collections.emptyList();
        }
        List<Size> supportedSizes = cameraAbilityImpl.getSupportedSizes(3);
        if (supportedSizes == null) {
            LOGGER.error("getCaptureSupports error, cameraCaptureSupports is null", new Object[0]);
            return Collections.emptyList();
        }
        List<Size> recordSupports = getRecordSupports(cameraAbilityImpl);
        boolean isFrontCamera = CameraUtil.isFrontCamera(cameraAbilityImpl);
        ArrayList arrayList = new ArrayList();
        for (Size size : recordSupports) {
            arrayList.add(getSnapshotSize(size, supportedSizes, isFrontCamera).get());
        }
        LOGGER.debug("getCaptureSupports, final capture supports is %{public}s", arrayList);
        return (List) arrayList.stream().distinct().collect(Collectors.toList());
    }

    private static void setSpecificVideoSize(List<Size> list, CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("setSpecificVideoSize");
        setCurrent18to9VideoSize(list, cameraAbilityImpl);
        setCurrent19Dot2To9VideoSize(list, cameraAbilityImpl);
        setCurrent19Dot3To9VideoSize(list, cameraAbilityImpl);
        setCurrent19to9VideoSize(list, cameraAbilityImpl);
        if (!CameraUtil.isFrontCamera(cameraAbilityImpl) && CustomConfigurationUtil.isAiMovieEnabled()) {
            setCurrent21to9VideoSize(list, cameraAbilityImpl);
        }
        LOGGER.end("setSpecificVideoSize");
    }

    private static void setCurrent18to9VideoSize(List<Size> list, CameraAbilityImpl cameraAbilityImpl) {
        Size size = null;
        int i = 0;
        for (Size size2 : list) {
            int i2 = size2.height;
            int i3 = size2.width;
            if (i3 > i2) {
                i3 = i2;
                i2 = i3;
            }
            if ((Arrays.asList(ConstantValue.VIDEO_SIZE_RATIO_2160_1064, ConstantValue.VIDEO_SIZE_RATIO_2240_1080, ConstantValue.VIDEO_SIZE_RATIO_1920_920).contains(i2 + STRING_X + i3) || (((double) Math.abs((((float) i2) / ((float) i3)) - 2.0f)) < 1.0E-5d && Arrays.asList(VIDEO_18TO9_SIZE_ARRAY_WIDTHS).contains(String.valueOf(i3)))) && i < i2 && isEncoderSupported(cameraAbilityImpl, size2, 6)) {
                size = size2;
                i = i2;
            }
        }
        if (size != null) {
            video18To9And1080pSize = size;
            LOGGER.debug("setCurrent18to9VideoSize %{public}s", video18To9And1080pSize);
        }
    }

    private static boolean isEncoderSupported(CameraAbilityImpl cameraAbilityImpl, Size size, int i) {
        Format format = new Format();
        format.putStringValue(Format.MIME, Format.VIDEO_AVC);
        format.putIntValue(Format.WIDTH, size.width);
        format.putIntValue(Format.HEIGHT, size.height);
        format.putIntValue(Format.FRAME_RATE, 30);
        format.putIntValue(Format.FRAME_INTERVAL, 1);
        RecorderProfile encoderProfile = getEncoderProfile(CameraUtil.isFrontCamera(cameraAbilityImpl) ? CAMERA_FRONT_NAME : CAMERA_BACK_NAME, i);
        if (encoderProfile != null) {
            format.putIntValue(Format.BIT_RATE, encoderProfile.vBitRate);
        }
        LOGGER.begin("new MediaCodecList");
        CodecDescriptionList codecDescriptionList = new CodecDescriptionList(1);
        LOGGER.end("new MediaCodecList");
        boolean z = codecDescriptionList.getEncoderByFormat(format) != null;
        LOGGER.info("isEncoderSupported : size=%{public}s, recorderProfile=%{public}d, isSupported=%{public}b", size, Integer.valueOf(i), Boolean.valueOf(z));
        return z;
    }

    private static RecorderProfile getEncoderProfile(String str, int i) {
        return RecorderProfile.getParameter(str, i);
    }

    private static void setCurrent19Dot2To9VideoSize(List<Size> list, CameraAbilityImpl cameraAbilityImpl) {
        Size size = null;
        int i = 0;
        for (Size size2 : list) {
            int i2 = size2.height;
            int i3 = size2.width;
            if (i3 > i2) {
                i3 = i2;
                i2 = i3;
            }
            if (Math.abs((((float) i2) / ((float) i3)) - RATIO_19_2_TO_9) < RATIO_19_2_TO_9_THOUSANDTHS && i < i2 && isEncoderSupported(cameraAbilityImpl, size2, 6)) {
                size = size2;
                i = i2;
            }
        }
        if (size == null) {
            return;
        }
        if (size.equals(VIDEO_1080P_1920_896_19_2_TO_9_SIZE) || size.equals(VIDEO_1080P_2304_1080_19_2TO9_SIZE)) {
            video19Dot2To9Size = size;
            LOGGER.debug("setCurrent19_2to9VideoSize %{public}s", video19Dot2To9Size);
        }
    }

    private static void setCurrent19Dot3To9VideoSize(List<Size> list, CameraAbilityImpl cameraAbilityImpl) {
        Size size = null;
        int i = 0;
        for (Size size2 : list) {
            Size swapWidthAndHeight = swapWidthAndHeight(size2);
            if (Math.abs((((float) swapWidthAndHeight.height) / ((float) swapWidthAndHeight.width)) - RATIO_19_3_TO_9) < RATIO_19_3_TO_9_THOUSANDTHS && i < swapWidthAndHeight.height && isEncoderSupported(cameraAbilityImpl, size2, 6)) {
                i = swapWidthAndHeight.height;
                size = size2;
            }
        }
        if (size != null && size.equals(VIDEO_1080P_1920_880_19_3_TO_9_SIZE)) {
            video19Dot3To9Size = size;
            LOGGER.debug("setCurrent19Dot3To9VideoSize %{public}s", video19Dot3To9Size);
        }
    }

    private static Size swapWidthAndHeight(Size size) {
        int i = size.height;
        int i2 = size.width;
        return i2 > i ? new Size(i, i2) : size;
    }

    private static void setCurrent19to9VideoSize(List<Size> list, CameraAbilityImpl cameraAbilityImpl) {
        Size size = null;
        int i = 0;
        for (Size size2 : list) {
            int i2 = size2.height;
            int i3 = size2.width;
            if (i3 > i2) {
                i3 = i2;
                i2 = i3;
            }
            if (Math.abs((((float) i2) / ((float) i3)) - RATIO_19_5_TO_9) < FLOAT_COMPARISON_TOLERANCE && i < i2 && isEncoderSupported(cameraAbilityImpl, size2, 6)) {
                if (!((size2.width == VIDEO_3648_TO_1680_SIZE.width && size2.height == VIDEO_3648_TO_1680_SIZE.height) || (size2.width == VIDEO_3264_TO_1504_SIZE.width && size2.height == VIDEO_3264_TO_1504_SIZE.height))) {
                    size = size2;
                    i = i2;
                }
            }
        }
        if (size != null) {
            video19dot5To9Size = size;
            LOGGER.debug("setCurrent19to9VideoSize %{public}s", video19dot5To9Size);
        }
    }

    private static void setCurrent21to9VideoSize(List<Size> list, CameraAbilityImpl cameraAbilityImpl) {
        for (Size size : list) {
            int i = size.height;
            int i2 = size.width;
            if (i2 > i) {
                i2 = i;
                i = i2;
            }
            if (ConstantValue.VIDEO_SIZE_RATIO_1680_720.equals(i + STRING_X + i2) && isEncoderSupported(cameraAbilityImpl, size, 5)) {
                video21To9And720pSize = size;
                LOGGER.info("video21to9_720PSize:%{public}s", size);
            }
            if (ConstantValue.VIDEO_SIZE_RATIO_2560_1080.equals(i + STRING_X + i2) && isEncoderSupported(cameraAbilityImpl, size, 6)) {
                video21To9And1080pSize = size;
                LOGGER.info("video21to9_1080PSize:%{public}s", size);
            }
        }
    }

    private static void initializeProfileSupports(CameraAbilityImpl cameraAbilityImpl, Map<Size, RecorderProfile> map) {
        LOGGER.begin("initializeProfileSupports");
        if (cameraAbilityImpl != null) {
            map.clear();
            has19Dot3To9Resolution = false;
            has19dDot2To9Resolution = false;
            has19Dot5To9Resolution = false;
            String str = CameraUtil.isFrontCamera(cameraAbilityImpl) ? CAMERA_FRONT_NAME : CAMERA_BACK_NAME;
            try {
                int parseInt = Integer.parseInt(str);
                int[] iArr = FULL_VIDEO_QUALITY_ARRAYS;
                for (int i : iArr) {
                    if (RecorderProfile.isProfile(parseInt, i)) {
                        RecorderProfile encoderProfile = getEncoderProfile(str, i);
                        if (encoderProfile != null) {
                            map.put(new Size(encoderProfile.vFrameWidth, encoderProfile.vFrameHeight), encoderProfile);
                        }
                    } else {
                        handleNonStandardQuality(i, parseInt, map);
                    }
                }
            } catch (NumberFormatException unused) {
                LOGGER.error("NumberFormatException cameraId: %{public}s", str);
            }
            LOGGER.debug("profile supports: %{public}s", map);
            LOGGER.end("initializeProfileSupports");
        }
    }

    private static void reduceResolutionIfNeeded(List<String> list) {
        ArrayList arrayList = new ArrayList(list.size());
        for (String str : list) {
            if (!isResolutionBelow720P(str)) {
                arrayList.add(str);
            }
        }
        list.clear();
        list.addAll(arrayList);
    }

    private static boolean isResolutionBelow720P(String str) {
        if (str == null) {
            return false;
        }
        String[] split = str.split(STRING_X);
        if (split.length != 2) {
            return false;
        }
        try {
            if (Math.min(Integer.parseInt(split[0]), Integer.parseInt(split[1])) < MIN_SIDE_LENGTH_OF_720P) {
                return true;
            }
            return false;
        } catch (NumberFormatException unused) {
            LOGGER.warn("NumberFormatException size: %{public}s", str);
            return false;
        }
    }

    private static void handleNonStandardQuality(int i, int i2, Map<Size, RecorderProfile> map) {
        Size size = video21To9And720pSize;
        if (size == null || i != 13) {
            Size size2 = video21To9And1080pSize;
            if (size2 == null || i != 14) {
                Size size3 = video19dot5To9Size;
                if (size3 == null || i != 15 || (!size3.equals(VIDEO_2336_1080_19_5TO9_SIZE) && !video19dot5To9Size.equals(VIDEO_2304_1064_19_5TO9_SIZE))) {
                    Size size4 = video19Dot3To9Size;
                    if (size4 == null || i != 18) {
                        Size size5 = video19Dot2To9Size;
                        if (size5 != null && i == 16) {
                            addModifiedProfileToProfileSupports(i2, 6, size5, map);
                            has19dDot2To9Resolution = true;
                        } else if (isNeedShow18To9()) {
                            handleNonStandardQuality18To9(i, i2, map);
                        }
                    } else {
                        addModifiedProfileToProfileSupports(i2, 6, size4, map);
                        has19Dot3To9Resolution = true;
                    }
                } else {
                    addModifiedProfileToProfileSupports(i2, 6, video19dot5To9Size, map);
                    has19Dot5To9Resolution = true;
                }
            } else {
                addModifiedProfileToProfileSupports(i2, 6, size2, map);
            }
        } else {
            addModifiedProfileToProfileSupports(i2, 5, size, map);
        }
    }

    private static void handleNonStandardQuality18To9(int i, int i2, Map<Size, RecorderProfile> map) {
        if (VIDEO_720P_18TO9_SIZE.equals(video18To9And1080pSize) && i == 11) {
            addModifiedProfileToProfileSupports(i2, 5, video18To9And1080pSize, map);
        } else if (!VIDEO_920P_18TO9_SIZE.equals(video18To9And1080pSize) || i != 11) {
            Size size = video18To9And1080pSize;
            if (size != null && i == 10) {
                addModifiedProfileToProfileSupports(i2, 6, size, map);
            }
        } else {
            addModifiedProfileToProfileSupports(i2, 6, video18To9And1080pSize, map);
        }
    }

    private static boolean isNeedShow18To9() {
        return !isAspectRatioTotlerance() && !has19dDot2To9Resolution && !has19Dot3To9Resolution && !has19Dot5To9Resolution;
    }

    private static boolean isAspectRatioTotlerance() {
        int[] realNotchSize = ScreenUtils.getRealNotchSize();
        Size screenSize = DeviceUtil.getScreenSize();
        if (realNotchSize == null || screenSize == null) {
            return false;
        }
        int i = screenSize.height;
        int i2 = screenSize.width;
        if (realNotchSize.length == 2 && realNotchSize[1] > 0) {
            i -= realNotchSize[1];
        }
        if (Math.abs((((double) i) / ((double) i2)) - 2.0d) >= 0.1d) {
            return true;
        }
        return false;
    }

    private static void addModifiedProfileToProfileSupports(int i, int i2, Size size, Map<Size, RecorderProfile> map) {
        RecorderProfile encoderProfile = getEncoderProfile(String.valueOf(i), i2);
        if (encoderProfile != null) {
            encoderProfile.vFrameWidth = size.width;
            encoderProfile.vFrameHeight = size.height;
            map.put(size, encoderProfile);
        }
    }

    private static Optional<Size> getSnapshotSize(Size size, List<Size> list, boolean z) {
        if (list == null || size == null) {
            return Optional.empty();
        }
        float f = ((float) size.height) / ((float) size.width);
        Size size2 = null;
        Size size3 = new Size(0, 0);
        for (Size size4 : list) {
            if (size4.height <= size.height && size4.width <= size.width) {
                if (size4.width != VIDEO_3840_TO_2160_SIZE.width || size4.height != VIDEO_3840_TO_2160_SIZE.height) {
                    if (Math.abs((((float) size4.height) / ((float) size4.width)) - f) < FLOAT_COMPARISON_TOLERANCE && (size2 == null || size2.width < size4.width)) {
                        size2 = size4;
                    }
                    if (isSupportMaxSize(size2, size3, size4)) {
                        size3 = size4;
                    }
                }
            }
        }
        if (size2 == null) {
            size2 = size3;
        }
        if (!(size2.width == 0 || size2.height == 0)) {
            size = size2;
        }
        LOGGER.info("optimal video capture size = %s", size);
        return Optional.ofNullable(size);
    }

    private static boolean isLimitToVideoSize(boolean z) {
        return (z && CustomConfigurationUtil.isFrontVideoSnapshotSizeLimitToVideoSize()) || (!z && CustomConfigurationUtil.isBackVideoSnapshotSizeLimitToVideoSize());
    }

    private static boolean isVideoSizeOptimal(Size size, List<Size> list) {
        return size.width == VIDEO_3840_TO_2160_SIZE.width && size.height == VIDEO_3840_TO_2160_SIZE.height && list.contains(size);
    }

    private static boolean isSupportMaxSize(Size size, Size size2, Size size3) {
        return size == null && (size3.width > size2.width || size3.height > size2.height);
    }

    private static Optional<Size> getOptimalVideoPreviewSize(Size size, List<Size> list) {
        if (list == null || size == null) {
            return Optional.empty();
        }
        Size size2 = null;
        double convertSizeToRatio = SizeUtil.convertSizeToRatio(size);
        for (Size size3 : list) {
            if (size3.height <= DEFAULT_MAX_HEIGHT && Math.abs(SizeUtil.convertSizeToRatio(size3) - convertSizeToRatio) <= RATIO_TOLERANCE) {
                if (size2 == null || (Math.abs(size3.height - size.height) <= Math.abs(size2.height - size.height) && Math.abs(size3.width - size.width) <= Math.abs(size2.width - size.width))) {
                    size2 = size3;
                }
            }
        }
        LOGGER.info("optimal video preview size = %{public}s", size2);
        return Optional.ofNullable(size2);
    }

    public static List<Size> getVideoStabilizeSupports(CameraAbilityImpl cameraAbilityImpl, int i) {
        if (cameraAbilityImpl == null) {
            LOGGER.warn("getVideoStabilizeSupports cameraAbility == null", new Object[0]);
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList();
        int[] iArr = (int[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.AVAILABLE_VIDEO_STABILIZATION_CONFIGURATIONS);
        if (iArr != null && iArr.length > 0 && iArr.length % 3 == 0) {
            int length = iArr.length / 3;
            for (int i2 = 0; i2 < length; i2++) {
                int i3 = i2 * 3;
                if (iArr[i3 + 2] == i) {
                    arrayList.add(new Size(iArr[i3], iArr[i3 + 1]));
                }
            }
            LOGGER.debug("videoStabilizeSupports=%{public}s", Arrays.toString(arrayList.toArray()));
        }
        return arrayList;
    }
}
