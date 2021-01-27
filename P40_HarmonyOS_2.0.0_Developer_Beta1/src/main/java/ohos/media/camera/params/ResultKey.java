package ohos.media.camera.params;

import java.util.Locale;
import java.util.Objects;
import ohos.utils.Scope;

public final class ResultKey {
    public static final Key<Byte> AUTO_ZOOM_STATE = new Key<>("ohos.camera.autoZoomState", Byte.class);
    public static final Key<Scope<Integer>> EXPOSURE_FPS_RANGE_RESULT = new Key<>("ohos.camera.exposureFpsRangeResult", castIntegerRangeClass());
    public static final Key<Byte> FACE_AE_STATE = new Key<>("ohos.camera.faceAEState", Byte.class);
    public static final Key<byte[]> VENDOR_CUSTOM_RESULT = new Key<>("ohos.camera.vendorCustomResult", byte[].class);
    public static final Key<Boolean> VIDEO_STABILIZATION_STATE = new Key<>("ohos.camera.videoStabilizationState", Boolean.class);

    private ResultKey() {
    }

    /* JADX DEBUG: Type inference failed for r0v1. Raw type applied. Possible types: java.lang.Class<?>, java.lang.Class<ohos.utils.Scope<java.lang.Integer>> */
    private static Class<Scope<Integer>> castIntegerRangeClass() {
        return new Scope(0, 0).getClass();
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
