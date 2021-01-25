package ohos.data.distributed.common;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

public class TextUtils {
    private static String deviceId;

    public static boolean byte2Boolean(byte b) {
        return b == 1;
    }

    private TextUtils() {
    }

    public static boolean isEmpty(String str) {
        return Objects.isNull(str) || str.trim().length() == 0;
    }

    public static boolean lenLessEqualThan(String str, int i) {
        if (Objects.isNull(str)) {
            return false;
        }
        try {
            if (str.getBytes("UTF-8").length <= i) {
                return true;
            }
            return false;
        } catch (UnsupportedEncodingException e) {
            throw new KvStoreException(KvStoreErrorCode.UTF_8_NOT_SUPPORT, e.getMessage());
        }
    }

    public static byte[] getKeyBytes(String str) {
        if (!isEmpty(str)) {
            return getKeyBytesMayEmpty(str);
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "key is empty.");
    }

    public static byte[] getKeyBytesMayEmpty(String str) {
        if (!Objects.isNull(str)) {
            try {
                return str.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new KvStoreException(KvStoreErrorCode.UTF_8_NOT_SUPPORT, e.getMessage());
            }
        } else {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "key is null.");
        }
    }

    public static byte[] boolean2byte(boolean z) {
        byte b = z ? (byte) 1 : 0;
        ByteBuffer allocate = ByteBuffer.allocate(1);
        allocate.put(b);
        return allocate.array();
    }

    public static boolean isListEmpty(List<?> list) {
        return Objects.isNull(list) || list.isEmpty();
    }

    public static void assertKey(String str) throws KvStoreException {
        assertKey(str, "key is empty or over maximum size.");
    }

    public static void assertDeviceKey(String str) throws KvStoreException {
        if (isEmpty(str) || !lenLessEqualThan(str, KvStore.MAX_KEY_LENGTH_DEVICE)) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "key is invalid");
        }
    }

    public static void assertKey(String str, String str2) throws KvStoreException {
        if (isEmpty(str) || !lenLessEqualThan(str, 1024)) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, str2);
        }
    }
}
