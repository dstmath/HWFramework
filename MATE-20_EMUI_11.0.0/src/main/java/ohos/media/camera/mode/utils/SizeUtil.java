package ohos.media.camera.mode.utils;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class SizeUtil {
    private static final int DEFAULT_SIZE_LIST_STR_LEN = 256;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(SizeUtil.class);
    private static final int MIN_SIZE_LENGTH = 2;
    private static final double OFFSET = 1.0E-6d;
    private static final float PICTURE_SIZE_INTEGER = 4.5f;
    private static final int RATIO_MULTIPLE = 10;
    private static final int RATIO_SIZE_UNIT = 1000;
    private static final String SEPARATOR = "_";

    private SizeUtil() {
    }

    public static double convertSizeToRatio(Size size) {
        if (size == null) {
            return 0.0d;
        }
        return ((double) size.width) / ((double) size.height);
    }

    public static boolean isFullResolutionSupported(CameraAbilityImpl cameraAbilityImpl, FullResolutionMode fullResolutionMode) {
        Integer num;
        if (fullResolutionMode == null) {
            LOGGER.error("isFullResolutionSupported:mode is null", new Object[0]);
            return false;
        } else if (cameraAbilityImpl == null || (num = (Integer) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.FULL_RESOLUTION_SUPPORT_FEATUREE)) == null || (num.intValue() & (1 << fullResolutionMode.getModeValue())) == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static String convertSizeListToString(List<Size> list) {
        StringBuilder sb = new StringBuilder(256);
        if (CollectionUtil.isEmptyCollection(list)) {
            LOGGER.error("sizes is null or sizes.size() is zero", new Object[0]);
            return sb.toString();
        }
        for (Size size : list) {
            sb.append(convertSizeToString(size));
            sb.append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String convertSizeToString(Size size) {
        if (size == null) {
            LOGGER.error("convertSizeToString:size is null!", new Object[0]);
            return null;
        }
        return size.width + "x" + size.height;
    }

    public static Optional<Size> convertSizeStringToSize(String str) {
        Object[] objArr;
        String str2;
        if (str == null) {
            return Optional.empty();
        }
        try {
            if (str.contains(SEPARATOR)) {
                objArr = new MessageFormat("{0}x{1}_{2}").parse(str);
            } else {
                objArr = new MessageFormat("{0}x{1}").parse(str);
            }
            Object obj = objArr[0];
            Object obj2 = objArr[1];
            String str3 = "";
            if (!(obj instanceof String) || !(obj2 instanceof String)) {
                str2 = str3;
            } else {
                str3 = (String) obj;
                str2 = (String) obj2;
            }
            return Optional.ofNullable(new Size(Integer.valueOf(str3).intValue(), Integer.valueOf(str2).intValue()));
        } catch (ParseException unused) {
            LOGGER.error("Parse failed, resString=%{public}s", str);
            return Optional.empty();
        } catch (NumberFormatException unused2) {
            LOGGER.error("NumberFormatException resString: %{public}s", str);
            return Optional.empty();
        }
    }

    public static boolean isSizeMatched(Size size, String str) {
        try {
            return Math.abs(convertSizeToStorage(size) - ((double) Float.parseFloat(str))) < OFFSET;
        } catch (NumberFormatException unused) {
            LOGGER.error("NumberFormatException sizeString: %{public}s", str);
            return false;
        }
    }

    public static double convertSizeToStorage(Size size) {
        if (size == null) {
            return 0.0d;
        }
        try {
            return Double.parseDouble(getShowingPictureSize(((((float) size.width) * ((float) size.height)) / 1000.0f) / 1000.0f));
        } catch (NumberFormatException unused) {
            LOGGER.error("NumberFormatException size: %{public}s", size);
            return 0.0d;
        }
    }

    public static String getShowingPictureSize(float f) {
        float f2 = Float.compare(f, PICTURE_SIZE_INTEGER) >= 0 ? (float) ((int) (f + 0.5f)) : ((float) ((int) ((f * 10.0f) + 0.5f))) / 10.0f;
        if (((double) Math.abs(f2 - ((float) Math.round(f2)))) < OFFSET) {
            return String.valueOf((int) f2);
        }
        return String.valueOf(f2);
    }

    public enum FullResolutionMode {
        MODE_NULL(0),
        MODE_APERATURE(1),
        MODE_BEAUTY_BACK(2),
        MODE_SUPER_NIGHT(3),
        MODE_BEAUTY_FRONT(4),
        MODE_LIGHT_PAINTING(5),
        MODE_PHOTO_AI(6);
        
        private final int modeValue;

        private FullResolutionMode(int i) {
            this.modeValue = i;
        }

        public int getModeValue() {
            return this.modeValue;
        }
    }
}
