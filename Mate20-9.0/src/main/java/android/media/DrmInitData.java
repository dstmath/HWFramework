package android.media;

import java.util.Arrays;
import java.util.UUID;

public abstract class DrmInitData {

    public static final class SchemeInitData {
        public final byte[] data;
        public final String mimeType;

        public SchemeInitData(String mimeType2, byte[] data2) {
            this.mimeType = mimeType2;
            this.data = data2;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof SchemeInitData)) {
                return false;
            }
            boolean z = true;
            if (obj == this) {
                return true;
            }
            SchemeInitData other = (SchemeInitData) obj;
            if (!this.mimeType.equals(other.mimeType) || !Arrays.equals(this.data, other.data)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return this.mimeType.hashCode() + (31 * Arrays.hashCode(this.data));
        }
    }

    public abstract SchemeInitData get(UUID uuid);

    DrmInitData() {
    }
}
