package android.media;

import java.util.Arrays;
import java.util.UUID;

public abstract class DrmInitData {

    public static final class SchemeInitData {
        public final byte[] data;
        public final String mimeType;

        public SchemeInitData(String mimeType, byte[] data) {
            this.mimeType = mimeType;
            this.data = data;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof SchemeInitData)) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            SchemeInitData other = (SchemeInitData) obj;
            if (this.mimeType.equals(other.mimeType)) {
                z = Arrays.equals(this.data, other.data);
            }
            return z;
        }

        public int hashCode() {
            return this.mimeType.hashCode() + (Arrays.hashCode(this.data) * 31);
        }
    }

    public abstract SchemeInitData get(UUID uuid);

    DrmInitData() {
    }
}
