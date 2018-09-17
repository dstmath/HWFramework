package android.os.health;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;

public class HealthKeys {
    public static final int BASE_PACKAGE = 40000;
    public static final int BASE_PID = 20000;
    public static final int BASE_PROCESS = 30000;
    public static final int BASE_SERVICE = 50000;
    public static final int BASE_UID = 10000;
    public static final int TYPE_COUNT = 5;
    public static final int TYPE_MEASUREMENT = 1;
    public static final int TYPE_MEASUREMENTS = 4;
    public static final int TYPE_STATS = 2;
    public static final int TYPE_TIMER = 0;
    public static final int TYPE_TIMERS = 3;
    public static final int UNKNOWN_KEY = 0;

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Constant {
        int type();
    }

    public static class Constants {
        private final String mDataType;
        private final int[][] mKeys = new int[5][];

        public Constants(Class clazz) {
            int i;
            this.mDataType = clazz.getSimpleName();
            Class<Constant> annotationClass = Constant.class;
            SortedIntArray[] keys = new SortedIntArray[this.mKeys.length];
            for (i = 0; i < keys.length; i++) {
                keys[i] = new SortedIntArray(N);
            }
            for (Field field : clazz.getDeclaredFields()) {
                Constant constant = (Constant) field.getAnnotation(annotationClass);
                if (constant != null) {
                    int type = constant.type();
                    if (type >= keys.length) {
                        throw new RuntimeException("Unknown Constant type " + type + " on " + field);
                    }
                    try {
                        keys[type].addValue(field.getInt(null));
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException("Can't read constant value type=" + type + " field=" + field, ex);
                    }
                }
            }
            for (i = 0; i < keys.length; i++) {
                this.mKeys[i] = keys[i].getArray();
            }
        }

        public String getDataType() {
            return this.mDataType;
        }

        public int getSize(int type) {
            return this.mKeys[type].length;
        }

        public int getIndex(int type, int key) {
            int index = Arrays.binarySearch(this.mKeys[type], key);
            if (index >= 0) {
                return index;
            }
            throw new RuntimeException("Unknown Constant " + key + " (of type " + type + " )");
        }

        public int[] getKeys(int type) {
            return this.mKeys[type];
        }
    }

    private static class SortedIntArray {
        int[] mArray;
        int mCount;

        SortedIntArray(int maxCount) {
            this.mArray = new int[maxCount];
        }

        void addValue(int value) {
            int[] iArr = this.mArray;
            int i = this.mCount;
            this.mCount = i + 1;
            iArr[i] = value;
        }

        int[] getArray() {
            if (this.mCount == this.mArray.length) {
                Arrays.sort(this.mArray);
                return this.mArray;
            }
            int[] result = new int[this.mCount];
            System.arraycopy(this.mArray, 0, result, 0, this.mCount);
            Arrays.sort(result);
            return result;
        }
    }
}
