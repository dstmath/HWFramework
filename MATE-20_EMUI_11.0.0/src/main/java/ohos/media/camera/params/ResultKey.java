package ohos.media.camera.params;

import java.util.Locale;
import java.util.Objects;

public final class ResultKey {
    public static final Key<Integer> PRO_EXPOSURE_HINT_RESULT = new Key<>("ohos.camera.proExposureHintResult", Integer.class);
    public static final Key<Boolean> VIDEO_STABILIZATION_STATE = new Key<>("ohos.camera.videoStabilizationState", Boolean.class);

    private ResultKey() {
    }

    public static final class Key<T> {
        private final String name;
        private final Class<T> type;

        public Key(String str, Class<T> cls) {
            this.name = str;
            this.type = cls;
        }

        public String getName() {
            return this.name;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            Key key = (Key) obj;
            if (!this.name.equals(key.name) || !this.type.equals(key.type)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.name, this.type);
        }

        public String toString() {
            return String.format(Locale.ENGLISH, "camera result key:%s, type:%s", this.name, this.type);
        }
    }
}
