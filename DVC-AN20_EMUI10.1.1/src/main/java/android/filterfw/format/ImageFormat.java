package android.filterfw.format;

import android.annotation.UnsupportedAppUsage;
import android.filterfw.core.MutableFrameFormat;
import android.graphics.Bitmap;

public class ImageFormat {
    public static final int COLORSPACE_GRAY = 1;
    public static final String COLORSPACE_KEY = "colorspace";
    public static final int COLORSPACE_RGB = 2;
    public static final int COLORSPACE_RGBA = 3;
    public static final int COLORSPACE_YUV = 4;

    public static MutableFrameFormat create(int width, int height, int colorspace, int bytesPerSample, int target) {
        MutableFrameFormat result = new MutableFrameFormat(2, target);
        result.setDimensions(width, height);
        result.setBytesPerSample(bytesPerSample);
        result.setMetaValue(COLORSPACE_KEY, Integer.valueOf(colorspace));
        if (target == 1) {
            result.setObjectClass(Bitmap.class);
        }
        return result;
    }

    @UnsupportedAppUsage
    public static MutableFrameFormat create(int width, int height, int colorspace, int target) {
        return create(width, height, colorspace, bytesPerSampleForColorspace(colorspace), target);
    }

    @UnsupportedAppUsage
    public static MutableFrameFormat create(int colorspace, int target) {
        return create(0, 0, colorspace, bytesPerSampleForColorspace(colorspace), target);
    }

    @UnsupportedAppUsage
    public static MutableFrameFormat create(int colorspace) {
        return create(0, 0, colorspace, bytesPerSampleForColorspace(colorspace), 0);
    }

    public static int bytesPerSampleForColorspace(int colorspace) {
        if (colorspace == 1) {
            return 1;
        }
        if (colorspace == 2) {
            return 3;
        }
        if (colorspace == 3) {
            return 4;
        }
        if (colorspace == 4) {
            return 3;
        }
        throw new RuntimeException("Unknown colorspace id " + colorspace + "!");
    }
}
