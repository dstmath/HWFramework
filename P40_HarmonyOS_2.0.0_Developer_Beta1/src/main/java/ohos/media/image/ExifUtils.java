package ohos.media.image;

import ohos.media.image.common.PropertyKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.Pair;

public class ExifUtils {
    private static final int DEFAULT_VALUE = -1;
    private static final Logger LOGGER = LoggerFactory.getImageLogger(ExifUtils.class);
    private static final double MINUTES = 60.0d;
    private static final String RATIONAL_SEPARATOR = "/";
    private static final double SECONDS = 3600.0d;

    public static Pair<Float, Float> getLatLong(ImageSource imageSource) {
        if (imageSource != null) {
            String imagePropertyString = imageSource.getImagePropertyString(PropertyKey.Exif.GPS_LATITUDE);
            String imagePropertyString2 = imageSource.getImagePropertyString(PropertyKey.Exif.GPS_LATITUDE_REF);
            String imagePropertyString3 = imageSource.getImagePropertyString(PropertyKey.Exif.GPS_LONGITUDE);
            String imagePropertyString4 = imageSource.getImagePropertyString(PropertyKey.Exif.GPS_LONGITUDE_REF);
            if (!(imagePropertyString == null || imagePropertyString2 == null || imagePropertyString3 == null || imagePropertyString4 == null)) {
                try {
                    return Pair.create(Float.valueOf(convertRationalToFloat(imagePropertyString, imagePropertyString2)), Float.valueOf(convertRationalToFloat(imagePropertyString3, imagePropertyString4)));
                } catch (IllegalArgumentException unused) {
                }
            }
            return null;
        }
        throw new IllegalArgumentException("imageSource is null");
    }

    public static double getAltitude(ImageSource imageSource, double d) {
        if (imageSource != null) {
            double imagePropertyDouble = imageSource.getImagePropertyDouble(PropertyKey.Exif.GPS_ALTITUDE, -1.0d);
            int imagePropertyInt = imageSource.getImagePropertyInt(PropertyKey.Exif.GPS_ALTITUDE_REF, -1);
            if (imagePropertyDouble < 0.0d || imagePropertyInt < 0) {
                return d;
            }
            int i = 1;
            if (imagePropertyInt == 1) {
                i = -1;
            }
            return imagePropertyDouble * ((double) i);
        }
        throw new IllegalArgumentException("imageSource is null");
    }

    private static float convertRationalToFloat(String str, String str2) {
        try {
            String[] split = str.split(",");
            if (split.length >= 3) {
                String[] split2 = split[0].split(RATIONAL_SEPARATOR);
                double parseDouble = Double.parseDouble(split2[0].trim()) / Double.parseDouble(split2[1].trim());
                String[] split3 = split[1].split(RATIONAL_SEPARATOR);
                double parseDouble2 = Double.parseDouble(split3[0].trim()) / Double.parseDouble(split3[1].trim());
                String[] split4 = split[2].split(RATIONAL_SEPARATOR);
                double parseDouble3 = parseDouble + (parseDouble2 / MINUTES) + ((Double.parseDouble(split4[0].trim()) / Double.parseDouble(split4[1].trim())) / SECONDS);
                if (!"S".equals(str2)) {
                    if (!"W".equals(str2)) {
                        return (float) parseDouble3;
                    }
                }
                return (float) (-parseDouble3);
            }
            throw new IllegalArgumentException("parameter is invalid");
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException unused) {
            throw new IllegalArgumentException();
        }
    }
}
