package ohos.media.camera.mode.tags.hisi.utils;

import java.util.ArrayList;
import java.util.Arrays;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.adapter.utils.CustomConfigurationUtil;
import ohos.media.camera.mode.adapter.utils.constant.BeautyColors;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.media.camera.mode.tags.CaptureParameters;
import ohos.media.camera.mode.tags.HuaweiTags;
import ohos.media.camera.mode.utils.CameraUtil;
import ohos.media.camera.mode.utils.CollectionUtil;
import ohos.media.camera.mode.utils.StringUtil;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class HisiBeautyCapabilityUtil {
    public static final int BODY_SHAPING = 4;
    private static final int[] BODY_SHAPING_DEFAULT;
    private static final int DEFAULT_BEAUTY_PARAMS_LENGTH = 4;
    private static final int[] DEFAULT_RANGE = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private static final int DEFAULT_SKIN_SMOOTH_INDEX = 2;
    private static final int[] FACE_BEAUTY_DEFAULT;
    public static final int FACE_SLENDER = 2;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(HisiBeautyCapabilityUtil.class);
    private static final int MAX_SKIN_TONES = 8;
    public static final int SKIN_SMOOTH = 1;
    public static final int SKIN_TONE = 3;
    private static final int SKIN_TONE_VALUE_INDEX = 3;

    static {
        int[] iArr = DEFAULT_RANGE;
        FACE_BEAUTY_DEFAULT = iArr;
        BODY_SHAPING_DEFAULT = iArr;
    }

    private HisiBeautyCapabilityUtil() {
    }

    private static boolean isSkinSmoothAvailable(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            return false;
        }
        boolean z = HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.BEAUTY_SETTING_SUPPORTED) && HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.BEAUTY_SETTING_SKIN_SMOOTH);
        LOGGER.debug("isSkinSmoothAvailable %{public}b", Boolean.valueOf(z));
        return z;
    }

    private static int[] getSkinSmoothRange(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("getSkinSmoothRange");
        if (cameraAbilityImpl == null) {
            return new int[0];
        }
        int[] tagSupportValues = HuaweiTags.getTagSupportValues(cameraAbilityImpl, InnerPropertyKey.BEAUTY_SETTING_SKIN_SMOOTH_VALUES);
        Logger logger = LOGGER;
        logger.end("getSkinSmoothRange " + Arrays.toString(tagSupportValues));
        return tagSupportValues;
    }

    private static boolean isFaceBeautyAvailable(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            return false;
        }
        boolean isSupported = HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.FACE_BEAUTY_SUPPORTED);
        LOGGER.debug("isFaceBeautyAvailable %{public}b", Boolean.valueOf(isSupported));
        return isSupported;
    }

    private static int[] getFaceBeautyRange(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("getFaceBeautyRange");
        if (cameraAbilityImpl == null) {
            return new int[0];
        }
        if (!isFaceBeautyAvailable(cameraAbilityImpl)) {
            return new int[0];
        }
        LOGGER.end("getFaceBeautyRange");
        return FACE_BEAUTY_DEFAULT;
    }

    public static boolean isUnifySkinSmoothAvailable(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            return false;
        }
        boolean z = isSkinSmoothAvailable(cameraAbilityImpl) || isFaceBeautyAvailable(cameraAbilityImpl);
        LOGGER.debug("isUnifySkinSmoothAvailable %{public}b", Boolean.valueOf(z));
        return z;
    }

    public static int[] getUnifySkinSmoothRange(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("getUnifySkinSmoothRange");
        if (cameraAbilityImpl == null) {
            return new int[0];
        }
        if (!isBeautySettingAvailable(cameraAbilityImpl)) {
            LOGGER.end("getUnifySkinSmoothRange get face beauty");
            return getFaceBeautyRange(cameraAbilityImpl);
        } else if (CameraUtil.isFrontCamera(cameraAbilityImpl)) {
            LOGGER.end("getUnifySkinSmoothRange get skin smooth");
            return getSkinSmoothRange(cameraAbilityImpl);
        } else if (isBodyShapingAvailable(cameraAbilityImpl)) {
            LOGGER.end("getUnifySkinSmoothRange get face beauty");
            return getFaceBeautyRange(cameraAbilityImpl);
        } else {
            LOGGER.end("getUnifySkinSmoothRange get skin smooth");
            return getSkinSmoothRange(cameraAbilityImpl);
        }
    }

    public static boolean isUnifySkinSmoothValueValid(int i, CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("isUnifySkinSmoothValueValid " + i);
        int[] unifySkinSmoothRange = getUnifySkinSmoothRange(cameraAbilityImpl);
        boolean z = false;
        if (unifySkinSmoothRange == null) {
            return false;
        }
        int length = unifySkinSmoothRange.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            } else if (unifySkinSmoothRange[i2] == i) {
                z = true;
                break;
            } else {
                i2++;
            }
        }
        LOGGER.end("isUnifySkinSmoothValueValid " + z);
        return z;
    }

    public static int getUnifySkinSmoothDefaultLevel(CameraAbilityImpl cameraAbilityImpl, String str) {
        LOGGER.begin("getUnifySkinSmoothDefaultLevel");
        int[] beautySettingDefaultValues = getBeautySettingDefaultValues(cameraAbilityImpl, str);
        int i = 0;
        if (CollectionUtil.isEmptyCollection(beautySettingDefaultValues)) {
            i = beautySettingDefaultValues[0];
        }
        Logger logger = LOGGER;
        logger.end("getUnifySkinSmoothDefaultLevel " + i);
        return i;
    }

    public static boolean isSkinToneAvailable(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("isSkinToneAvailable");
        boolean z = false;
        if (cameraAbilityImpl == null) {
            return false;
        }
        int[] skinToneRange = getSkinToneRange(cameraAbilityImpl);
        if (CameraUtil.isFrontCamera(cameraAbilityImpl) && HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.BEAUTY_SETTING_SUPPORTED) && !CollectionUtil.isEmptyCollection(skinToneRange)) {
            z = true;
        }
        Logger logger = LOGGER;
        logger.end("isSkinToneAvailable " + z);
        return z;
    }

    public static int[] getSkinToneRange(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("getSkinToneRange");
        if (cameraAbilityImpl == null) {
            return new int[0];
        }
        int[] tagSupportValues = HuaweiTags.getTagSupportValues(cameraAbilityImpl, InnerPropertyKey.BEAUTY_SETTING_FRONT_SKIN_TONE);
        if (CollectionUtil.isEmptyCollection(tagSupportValues)) {
            return new int[0];
        }
        ArrayList arrayList = new ArrayList();
        int min = Math.min(tagSupportValues.length, 8);
        for (int i = 0; i < min; i++) {
            if (tagSupportValues[i] != 0) {
                arrayList.add(Integer.valueOf(i));
            }
        }
        int size = arrayList.size();
        int[] iArr = new int[(size + 1)];
        for (int i2 = 0; i2 < size; i2++) {
            iArr[i2] = ((Integer) arrayList.get(i2)).intValue();
        }
        iArr[size] = BeautyColors.COLORS_NONE.getBeautyColorValue();
        LOGGER.end("getSkinToneRange " + Arrays.toString(iArr));
        return iArr;
    }

    public static boolean isFaceColorValueValid(int i, CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("isFaceColorValueValid " + i);
        int[] skinToneRange = getSkinToneRange(cameraAbilityImpl);
        int length = skinToneRange.length;
        boolean z = false;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            } else if (skinToneRange[i2] == i) {
                z = true;
                break;
            } else {
                i2++;
            }
        }
        LOGGER.end("isFaceColorValueValid " + z);
        return z;
    }

    public static int getSkinToneDefaultLevel(CameraAbilityImpl cameraAbilityImpl, String str) {
        LOGGER.begin("getSkinToneDefaultLevel");
        int[] beautySettingDefaultValues = getBeautySettingDefaultValues(cameraAbilityImpl, str);
        int i = beautySettingDefaultValues.length > 1 ? beautySettingDefaultValues[1] : 0;
        Logger logger = LOGGER;
        logger.end("getSkinToneDefaultLevel " + i);
        return i;
    }

    private static boolean isFaceSlenderAvailable(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("isFaceSlenderAvailable");
        boolean z = false;
        if (cameraAbilityImpl == null) {
            return false;
        }
        if (CameraUtil.isFrontCamera(cameraAbilityImpl) && HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.BEAUTY_SETTING_SUPPORTED) && HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.BEAUTY_SETTING_FRONT_FACE_SLENDER)) {
            z = true;
        }
        Logger logger = LOGGER;
        logger.end("isFaceSlenderAvailable " + z);
        return z;
    }

    private static int[] getFaceSlenderRange(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("getFaceSlenderRange");
        if (cameraAbilityImpl == null) {
            return new int[0];
        }
        int[] tagSupportValues = HuaweiTags.getTagSupportValues(cameraAbilityImpl, InnerPropertyKey.BEAUTY_SETTING_FACE_SLENDER_VALUES);
        Logger logger = LOGGER;
        logger.end("getFaceSlenderRange " + Arrays.toString(tagSupportValues));
        return tagSupportValues;
    }

    private static boolean isAiShapingAvailable(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("isAiShapingAvailable");
        boolean z = false;
        if (cameraAbilityImpl == null) {
            return false;
        }
        if (CameraUtil.isFrontCamera(cameraAbilityImpl) && HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.BEAUTY_SETTING_SUPPORTED) && HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.AI_SHAPING_SUPPORT)) {
            z = true;
        }
        Logger logger = LOGGER;
        logger.end("isAiShapingAvailable " + z);
        return z;
    }

    private static int[] getAiShapingRange(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("getAiShapingRange");
        if (cameraAbilityImpl == null) {
            return new int[0];
        }
        int[] tagSupportValues = HuaweiTags.getTagSupportValues(cameraAbilityImpl, InnerPropertyKey.AI_SHAPING_VALUES);
        Logger logger = LOGGER;
        logger.end("getAiShapingRange " + Arrays.toString(tagSupportValues));
        return tagSupportValues;
    }

    public static boolean isUnifyFaceSlenderAvailable(CameraAbilityImpl cameraAbilityImpl) {
        return isFaceSlenderAvailable(cameraAbilityImpl) || isAiShapingAvailable(cameraAbilityImpl);
    }

    public static boolean isUnifyFaceSlenderValueValid(int i, CameraAbilityImpl cameraAbilityImpl) {
        int[] iArr;
        LOGGER.begin("isUnifyFaceSlenderValueValid " + i);
        if (isFaceSlenderAvailable(cameraAbilityImpl)) {
            iArr = getFaceSlenderRange(cameraAbilityImpl);
        } else {
            iArr = getAiShapingRange(cameraAbilityImpl);
        }
        int length = iArr.length;
        boolean z = false;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            } else if (iArr[i2] == i) {
                z = true;
                break;
            } else {
                i2++;
            }
        }
        LOGGER.end("isUnifyFaceSlenderValueValid " + z);
        return z;
    }

    public static int[] getUnifyFaceSlenderRange(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.debug("getUnifyFaceSlenderRange", new Object[0]);
        if (getFaceSlenderRange(cameraAbilityImpl).length != 0) {
            return getFaceSlenderRange(cameraAbilityImpl);
        }
        return getAiShapingRange(cameraAbilityImpl);
    }

    public static int getUnifyFaceSlenderDefaultLevel(CameraAbilityImpl cameraAbilityImpl, String str) {
        LOGGER.begin("getSkinSmoothDefaultLevel");
        int[] beautySettingDefaultValues = getBeautySettingDefaultValues(cameraAbilityImpl, str);
        int i = beautySettingDefaultValues.length > 2 ? beautySettingDefaultValues[2] : 0;
        Logger logger = LOGGER;
        logger.end("getSkinSmoothDefaultLevel " + i);
        return i;
    }

    public static boolean isBodyShapingAvailable(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("isBodyShapingAvailable");
        boolean z = false;
        if (cameraAbilityImpl == null) {
            return false;
        }
        if (!CameraUtil.isFrontCamera(cameraAbilityImpl) && HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.BEAUTY_SETTING_SUPPORTED) && HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.BODYSHAPING_MODE_SUPPORTED)) {
            z = true;
        }
        Logger logger = LOGGER;
        logger.end("isBodyShapingAvailable " + z);
        return z;
    }

    public static int[] getBodyShapingRange(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("getBodyShapingRange");
        if (cameraAbilityImpl == null) {
            return new int[0];
        }
        if (isBodyShapingAvailable(cameraAbilityImpl)) {
            LOGGER.end("getBodyShapingRange true");
            return BODY_SHAPING_DEFAULT;
        }
        LOGGER.end("getBodyShapingRange false");
        return new int[0];
    }

    public static boolean isBodyShapingValueValid(int i, CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("isFaceSlenderValueValid " + i);
        int[] bodyShapingRange = getBodyShapingRange(cameraAbilityImpl);
        boolean z = false;
        if (bodyShapingRange == null) {
            return false;
        }
        int length = bodyShapingRange.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            } else if (bodyShapingRange[i2] == i) {
                z = true;
                break;
            } else {
                i2++;
            }
        }
        LOGGER.end("isFaceSlenderValueValid " + z);
        return z;
    }

    public static int getBodyShapingDefaultLevel(CameraAbilityImpl cameraAbilityImpl, String str) {
        LOGGER.begin("getBodyShapingDefaultLevel");
        int[] beautySettingDefaultValues = getBeautySettingDefaultValues(cameraAbilityImpl, str);
        int i = beautySettingDefaultValues.length > 3 ? beautySettingDefaultValues[3] : 0;
        Logger logger = LOGGER;
        logger.end("getBodyShapingDefaultLevel " + i);
        return i;
    }

    public static boolean isVideoBeautyAvailable(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("isVideoBeautyAvailable");
        boolean z = false;
        if (cameraAbilityImpl == null) {
            return false;
        }
        if (CustomConfigurationUtil.isBeautyVideoSupported() && HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.VIDEO_BEAUTY_SUPPORTED)) {
            z = true;
        }
        Logger logger = LOGGER;
        logger.end("isVideoBeautyAvailable " + z);
        return z;
    }

    public static int[] getVideoBeautyRange(CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("getVideoBeautyRange");
        if (cameraAbilityImpl == null) {
            return new int[0];
        }
        if (isVideoBeautyAvailable(cameraAbilityImpl)) {
            LOGGER.end("getVideoBeautyRange true");
            return FACE_BEAUTY_DEFAULT;
        }
        LOGGER.end("getVideoBeautyRange false");
        return new int[0];
    }

    public static boolean isVideoBeautyValueValid(int i, CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("isVideoBeautyValueValid " + i);
        int[] videoBeautyRange = getVideoBeautyRange(cameraAbilityImpl);
        boolean z = false;
        if (videoBeautyRange == null) {
            return false;
        }
        int length = videoBeautyRange.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            } else if (videoBeautyRange[i2] == i) {
                z = true;
                break;
            } else {
                i2++;
            }
        }
        LOGGER.end("isVideoBeautyValueValid " + z);
        return z;
    }

    private static int[] getBeautySettingDefaultValues(CameraAbilityImpl cameraAbilityImpl, String str) {
        LOGGER.begin("getBeautySettingDefaultValues");
        if (cameraAbilityImpl == null) {
            return new int[0];
        }
        if (StringUtil.isEmptyString(str)) {
            LOGGER.warn("modeType is null", new Object[0]);
            return new int[0];
        }
        int[] tagSupportValues = HuaweiTags.getTagSupportValues(cameraAbilityImpl, InnerPropertyKey.BEAUTY_SETTING_DEFAULT_PARA);
        if (tagSupportValues.length < 4) {
            tagSupportValues = (!ConstantValue.MODE_NAME_NORMAL_VIDEO.equals(str) || CameraUtil.isFrontCamera(cameraAbilityImpl)) ? new int[]{CustomConfigurationUtil.getDefaultBeautyLevel(), 0, 0, 0} : new int[]{0, 0, 0, 0};
            Logger logger = LOGGER;
            logger.end("getBeautySettingDefaultValues " + Arrays.toString(tagSupportValues));
        }
        return tagSupportValues;
    }

    private static boolean isBeautySettingAvailable(CameraAbilityImpl cameraAbilityImpl) {
        return HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.BEAUTY_SETTING_SUPPORTED);
    }

    public static void enableVideoSkinSmooth(CameraAbilityImpl cameraAbilityImpl, CaptureParameters captureParameters, int i) {
        LOGGER.begin("enableVideoSkinSmooth");
        if (!isValid(cameraAbilityImpl, captureParameters)) {
            LOGGER.warn("input value is null", new Object[0]);
            return;
        }
        captureParameters.addParameter(InnerParameterKey.FACE_BEAUTY_MODE, (byte) 0);
        if (isVideoBeautyAvailable(cameraAbilityImpl)) {
            captureParameters.addParameter(InnerParameterKey.FACE_BEAUTY_LEVEL, Integer.valueOf(i));
            if (i == 0) {
                captureParameters.addParameter(InnerParameterKey.FACE_BEAUTY_MODE, (byte) 0);
            } else {
                captureParameters.addParameter(InnerParameterKey.FACE_BEAUTY_MODE, (byte) 1);
            }
        }
        LOGGER.end("enableVideoSkinSmooth");
    }

    public static void enableBodyShaping(CameraAbilityImpl cameraAbilityImpl, CaptureParameters captureParameters, int i) {
        LOGGER.begin("enableBodyShaping");
        if (!isValid(cameraAbilityImpl, captureParameters)) {
            LOGGER.warn("input value is null", new Object[0]);
            return;
        }
        if (isFaceBeautyAvailable(cameraAbilityImpl)) {
            captureParameters.addParameter(InnerParameterKey.FACE_BEAUTY_MODE, (byte) 1);
        }
        if (isBodyShapingAvailable(cameraAbilityImpl)) {
            captureParameters.addParameter(InnerParameterKey.BODY_SHAPING_LEVEL, Byte.valueOf((byte) i));
            if (isBeautySettingAvailable(cameraAbilityImpl)) {
                captureParameters.addParameter(InnerParameterKey.BEAUTY_MULTI_SETTING_MODE, (byte) 1);
            }
        }
        LOGGER.end("enableBodyShaping");
    }

    public static boolean isValid(CameraAbilityImpl cameraAbilityImpl, CaptureParameters captureParameters) {
        if (cameraAbilityImpl == null) {
            LOGGER.warn("the cameraAbility value cameraAbility is null", new Object[0]);
            return false;
        } else if (captureParameters != null) {
            return true;
        } else {
            LOGGER.warn("the parameters value parameters is null", new Object[0]);
            return false;
        }
    }
}
