package ohos.media.camera.params;

import java.util.Locale;
import java.util.Objects;

public final class PropertyKey {
    public static final Key<Integer> PARTIAL_RESULT_COUNT = new Key<>("ohos.camera.partialResultCount", Integer.class);
    public static final Key<Integer> SENSOR_ORIENTATION = new Key<>("ohos.camera.sensorOrientation", Integer.class);
    public static final Key<Boolean> VIDEO_STABILIZATION_SUPPORT = new Key<>("ohos.camera.videoStabilizationSupport", Boolean.class);

    private PropertyKey() {
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

        public boolean checkType(Class<?> cls) {
            return this.type.equals(cls);
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
            return String.format(Locale.ENGLISH, "camera property key:%s, type:%s", this.name, this.type);
        }
    }
}
