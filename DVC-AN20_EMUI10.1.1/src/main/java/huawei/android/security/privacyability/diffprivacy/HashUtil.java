package huawei.android.security.privacyability.diffprivacy;

import com.huawei.security.keystore.HwKeyProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* access modifiers changed from: package-private */
public class HashUtil {
    private static final int AND_SIZE = 255;
    private static final int BKDR_HASH_SEED = 131;
    private static final int BYTE_SIZE = 2;
    private static final int HASH_NUM = 8;
    private static final Object INSTANCE_SYNC = new Object();
    private static final int INT_BYTES = 4;
    private static final int MAX_HASHSIZE = 65535;
    private static final int MULTIPLE_SIZE = 256;
    final MessageDigest md5 = MessageDigest.getInstance(HwKeyProperties.DIGEST_MD5);

    HashUtil() throws NoSuchAlgorithmException {
    }

    private int getBKDRHash(int hashIndex, int cohort, String str, int hashSize) {
        byte[] digest;
        long hash = 0;
        byte[] stringInUtf8 = str.getBytes(StandardCharsets.UTF_8);
        byte[] message = ByteBuffer.allocate(stringInUtf8.length + 4 + 4).putInt(cohort).put(stringInUtf8).putInt(hashIndex).array();
        synchronized (INSTANCE_SYNC) {
            this.md5.reset();
            digest = this.md5.digest(message);
        }
        int length = digest.length;
        int i = 0;
        while (i < length) {
            byte item = digest[i];
            if (hash < (Long.MAX_VALUE - ((long) item)) / ((long) BKDR_HASH_SEED)) {
                hash = (((long) BKDR_HASH_SEED) * hash) + ((long) item);
            }
            i++;
            digest = digest;
        }
        return Math.abs(((int) hash) % hashSize);
    }

    public int fastHash(int hashNum, int hashIndex, int cohort, String str, int hashSize) {
        byte[] digest;
        byte[] stringInUtf8 = str.getBytes(StandardCharsets.UTF_8);
        if (hashNum >= 8 || hashSize > 65535) {
            return getBKDRHash(hashIndex, cohort, str, hashSize);
        }
        byte[] message = ByteBuffer.allocate(stringInUtf8.length + 4).putInt(cohort).put(stringInUtf8).array();
        synchronized (INSTANCE_SYNC) {
            this.md5.reset();
            digest = this.md5.digest(message);
        }
        return (((digest[hashIndex * 2] & 255) * 256) + (digest[(hashIndex * 2) + 1] & 255)) % hashSize;
    }
}
