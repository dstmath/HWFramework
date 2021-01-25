package ohos.media.image.common;

import java.util.HashMap;
import java.util.Map;

public final class ImageFormat {
    private static final int BITS_10 = 10;
    private static final int BITS_12 = 12;
    private static final int BITS_16 = 16;
    private static final int BITS_SHIFT = 8;
    private static final int COMPONENT_1 = 1;
    private static final int COMPONENT_2 = 2;
    private static final int COMPONENT_3 = 3;
    private static final Map<Integer, Integer> FORMAT_MAPPING = new HashMap();
    public static final int JPEG = 3;
    public static final int NV21 = 1;
    public static final int RAW10 = 4;
    public static final int RAW16 = 5;
    private static final int SHIFT_MASK = 255;
    public static final int UNKNOWN = 0;
    public static final int YUV420_888 = 2;

    public enum ComponentType {
        YUV_Y(1),
        YUV_U(2),
        YUV_V(3),
        JPEG(4),
        RAW10(5),
        RAW16(6);
        
        private final int value;

        private ComponentType(int i) {
            this.value = i;
        }

        public static ComponentType valueOf(int i) {
            ComponentType[] values = values();
            for (ComponentType componentType : values) {
                if (componentType.value == i) {
                    return componentType;
                }
            }
            return null;
        }
    }

    static {
        FORMAT_MAPPING.put(1, 3075);
        FORMAT_MAPPING.put(2, 3075);
        FORMAT_MAPPING.put(4, 2561);
        FORMAT_MAPPING.put(5, 4097);
        FORMAT_MAPPING.put(3, 1);
    }

    private ImageFormat() {
    }

    public static int getBitsNumberPerPixel(int i) {
        return (FORMAT_MAPPING.getOrDefault(Integer.valueOf(i), 0).intValue() >> 8) & 255;
    }

    public static int getComponentNumber(int i) {
        return FORMAT_MAPPING.getOrDefault(Integer.valueOf(i), 0).intValue() & 255;
    }
}
