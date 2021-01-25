package ohos.media.camera.mode.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class StringUtil {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(StringUtil.class);

    private StringUtil() {
    }

    public static boolean isEmptyString(String str) {
        return str == null || str.trim().equals("");
    }

    public static List<String> split(String str, String str2) {
        if (str == null) {
            LOGGER.warn("str is null", new Object[0]);
            return Collections.emptyList();
        }
        StringTokenizer stringTokenizer = new StringTokenizer(str, str2);
        ArrayList arrayList = new ArrayList();
        while (stringTokenizer.hasMoreElements()) {
            arrayList.add(stringTokenizer.nextToken());
        }
        return arrayList;
    }

    public static String removeWhitespace(String str) {
        if (str != null) {
            return str.replaceAll("\\s", "");
        }
        LOGGER.warn("str is null", new Object[0]);
        return null;
    }

    public static double convertRatioStringToRatio(String str) {
        if (str == null) {
            LOGGER.warn("ratioString is null", new Object[0]);
            return 0.0d;
        }
        List<String> split = split(str, ":");
        try {
            return Double.parseDouble(split.get(0)) / Double.parseDouble(split.get(1));
        } catch (NumberFormatException unused) {
            LOGGER.error("NumberFormatException ratioString: %{public}s", str);
            return 0.0d;
        }
    }
}
