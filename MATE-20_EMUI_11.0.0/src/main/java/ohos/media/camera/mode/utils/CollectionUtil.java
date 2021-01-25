package ohos.media.camera.mode.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CollectionUtil {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CollectionUtil.class);

    private CollectionUtil() {
    }

    public static <T extends Collection<?>> boolean isEmptyCollection(T t) {
        return t == null || t.isEmpty();
    }

    public static <T> boolean isEmptyCollection(T[] tArr) {
        return tArr == null || tArr.length == 0;
    }

    public static boolean isEmptyCollection(int[] iArr) {
        return iArr == null || iArr.length == 0;
    }

    public static boolean isEmptyCollection(byte[] bArr) {
        return bArr == null || bArr.length == 0;
    }

    public static <T extends Collection<?>> boolean contains(T t, Object obj) {
        return t != null && t.contains(obj);
    }

    public static boolean contains(int[] iArr, int i) {
        if (iArr == null) {
            return false;
        }
        for (int i2 : iArr) {
            if (i2 == i) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(byte[] bArr, int i) {
        if (bArr == null) {
            return false;
        }
        for (byte b : bArr) {
            if (b == i) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(String[] strArr, String str) {
        if (isEmptyCollection(strArr)) {
            LOGGER.error("contains: collection is null or empty", new Object[0]);
            return false;
        } else if (StringUtil.isEmptyString(str)) {
            LOGGER.error("contains: value is null", new Object[0]);
            return false;
        } else {
            for (String str2 : strArr) {
                if (str2.equals(str)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean contains(Byte[] bArr, Byte b) {
        if (bArr == null) {
            return false;
        }
        for (Byte b2 : bArr) {
            if (b2.equals(b)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(List<Size> list, Size size) {
        if (list == null) {
            return false;
        }
        for (Size size2 : list) {
            if (size2.equals(size)) {
                return true;
            }
        }
        return false;
    }

    public static <T> List<T> arrayToList(T[] tArr) {
        ArrayList arrayList = new ArrayList(tArr.length);
        if (isEmptyCollection(tArr)) {
            return arrayList;
        }
        arrayList.addAll(Arrays.asList(tArr));
        return arrayList;
    }
}
