package ohos.media.camera.params;

import java.util.Locale;
import java.util.Objects;
import ohos.agp.utils.Rect;
import ohos.location.Location;

public final class ParameterKey {
    public static final Key<Byte> AI_MOVIE = new Key<>("ohos.camera.aiMovie", Byte.class);
    public static final Key<Float> BOKEH_APERTURE = new Key<>("ohos.camera.BokehAperture", Float.class);
    public static final Key<Float> EXPOSURE_COMPENSATION = new Key<>("ohos.camera.exposureCompensation", Float.class);
    public static final Key<Byte> FILTER_EFFECT = new Key<>("ohos.camera.filterEffect", Byte.class);
    public static final Key<Integer> FILTER_LEVEL = new Key<>("ohos.camera.filterLevel", Integer.class);
    public static final Key<Byte> IMAGE_COMPRESSION_QUALITY = new Key<>("ohos.camera.imageCompressionQuality", Byte.class);
    public static final Key<Boolean> IMAGE_MIRROR = new Key<>("ohos.camera.imageMirror", Boolean.class);
    public static final Key<Byte> PORTRAIT_FAIRLIGHT = new Key<>("ohos.camera.portraitFairlight", Byte.class);
    public static final Key<Byte> PORTRAIT_SPOTS_BOKEH = new Key<>("ohos.camera.portraitSpotsBokeh", Byte.class);
    public static final Key<Integer> PRO_AWB_TYPE = new Key<>("ohos.camera.proAwbType", Integer.class);
    public static final Key<Boolean> PRO_EXPOSURE_HINT = new Key<>("ohos.camera.proExposureHint", Boolean.class);
    public static final Key<Float> PRO_FOCUS_DISTANCE = new Key<>("ohos.camera.proFocusDistance", Float.class);
    public static final Key<Integer> PRO_MANUAL_WB = new Key<>("ohos.camera.proManualWb", Integer.class);
    public static final Key<Byte> PRO_METERING = new Key<>("ohos.camera.proMetering", Byte.class);
    public static final Key<Integer> PRO_SENSOR_EXPOSURE_TIME = new Key<>("ohos.camera.proSensorExposureTime", Integer.class);
    public static final Key<Integer> PRO_SENSOR_ISO = new Key<>("ohos.camera.proSensorIso", Integer.class);
    public static final Key<Boolean> SCENE_EFFECT_ENABLE = new Key<>("ohos.camera.sceneEffectEnable", Boolean.class);
    public static final Key<Boolean> SENSOR_HDR = new Key<>("ohos.camera.sensorHdr", Boolean.class);
    public static final Key<Long> SUPER_NIGHT_EXPOSURE = new Key<>("ohos.camera.superNightExposure", Long.class);
    public static final Key<Long> SUPER_NIGHT_ISO = new Key<>("ohos.camera.superNightIso", Long.class);
    public static final Key<Rect> SUPER_SLOW_CHECK_AREA = new Key<>("ohos.camera.superSlowCheckArea", Rect.class);
    public static final Key<Boolean> VIDEO_STABILIZATION = new Key<>("ohos.camera.videoStabilization", Boolean.class);

    private ParameterKey() {
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
            return String.format(Locale.ENGLISH, "camera parameter key:%s, type:%s", this.name, this.type);
        }

        public Object cloneValue(Object obj) {
            if (obj == null) {
                return null;
            }
            if (!checkType(obj.getClass())) {
                throw new IllegalArgumentException("Invalid class type: key: " + this.name + ",type: " + this.type + ",value type:" + obj.getClass());
            } else if (this.type.equals(Byte.class)) {
                return obj;
            } else {
                if (this.type.equals(Byte[].class)) {
                    Byte[] bArr = (Byte[]) obj;
                    int length = bArr.length;
                    Byte[] bArr2 = new Byte[length];
                    System.arraycopy(bArr, 0, bArr2, 0, length);
                    return bArr2;
                } else if (this.type.equals(byte[].class)) {
                    byte[] bArr3 = (byte[]) obj;
                    int length2 = bArr3.length;
                    byte[] bArr4 = new byte[length2];
                    System.arraycopy(bArr3, 0, bArr4, 0, length2);
                    return bArr4;
                } else if (this.type.equals(Integer.class)) {
                    return obj;
                } else {
                    if (this.type.equals(Integer[].class)) {
                        Integer[] numArr = (Integer[]) obj;
                        int length3 = numArr.length;
                        Integer[] numArr2 = new Integer[length3];
                        System.arraycopy(numArr, 0, numArr2, 0, length3);
                        return numArr2;
                    } else if (this.type.equals(int[].class)) {
                        int[] iArr = (int[]) obj;
                        int length4 = iArr.length;
                        int[] iArr2 = new int[length4];
                        System.arraycopy(iArr, 0, iArr2, 0, length4);
                        return iArr2;
                    } else if (this.type.equals(Boolean.class)) {
                        return obj;
                    } else {
                        if (this.type.equals(Boolean[].class)) {
                            Boolean[] boolArr = (Boolean[]) obj;
                            int length5 = boolArr.length;
                            Boolean[] boolArr2 = new Boolean[length5];
                            System.arraycopy(boolArr, 0, boolArr2, 0, length5);
                            return boolArr2;
                        } else if (this.type.equals(boolean[].class)) {
                            boolean[] zArr = (boolean[]) obj;
                            int length6 = zArr.length;
                            boolean[] zArr2 = new boolean[length6];
                            System.arraycopy(zArr, 0, zArr2, 0, length6);
                            return zArr2;
                        } else if (this.type.equals(Float.class)) {
                            return obj;
                        } else {
                            if (this.type.equals(Float[].class)) {
                                Float[] fArr = (Float[]) obj;
                                int length7 = fArr.length;
                                Float[] fArr2 = new Float[length7];
                                System.arraycopy(fArr, 0, fArr2, 0, length7);
                                return fArr2;
                            } else if (this.type.equals(float[].class)) {
                                float[] fArr3 = (float[]) obj;
                                int length8 = fArr3.length;
                                float[] fArr4 = new float[length8];
                                System.arraycopy(fArr3, 0, fArr4, 0, length8);
                                return fArr4;
                            } else if (this.type.equals(Location.class)) {
                                return new Location((Location) obj);
                            } else {
                                if (this.type.equals(Rect.class)) {
                                    Rect rect = (Rect) obj;
                                    return new Rect(rect.left, rect.top, rect.right, rect.bottom);
                                }
                                throw new IllegalArgumentException("Unsupported class type:" + this.type + " for key " + this.name);
                            }
                        }
                    }
                }
            }
        }
    }
}
