package android.provider;

import android.media.ExifInterface;
import android.os.Bundle;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MetadataReader {
    private static final String[] DEFAULT_EXIF_TAGS = {"FNumber", ExifInterface.TAG_COPYRIGHT, ExifInterface.TAG_DATETIME, ExifInterface.TAG_EXPOSURE_TIME, ExifInterface.TAG_FOCAL_LENGTH, "FNumber", ExifInterface.TAG_GPS_LATITUDE, ExifInterface.TAG_GPS_LATITUDE_REF, ExifInterface.TAG_GPS_LONGITUDE, ExifInterface.TAG_GPS_LONGITUDE_REF, ExifInterface.TAG_IMAGE_LENGTH, ExifInterface.TAG_IMAGE_WIDTH, "ISOSpeedRatings", ExifInterface.TAG_MAKE, ExifInterface.TAG_MODEL, ExifInterface.TAG_ORIENTATION, ExifInterface.TAG_SHUTTER_SPEED_VALUE};
    private static final String JPEG_MIME_TYPE = "image/jpeg";
    private static final String JPG_MIME_TYPE = "image/jpg";
    private static final int TYPE_DOUBLE = 1;
    private static final int TYPE_INT = 0;
    private static final Map<String, Integer> TYPE_MAPPING = new HashMap();
    private static final int TYPE_STRING = 2;

    private MetadataReader() {
    }

    static {
        TYPE_MAPPING.put(ExifInterface.TAG_ARTIST, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_BITS_PER_SAMPLE, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_COMPRESSION, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_COPYRIGHT, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_DATETIME, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_IMAGE_DESCRIPTION, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_IMAGE_LENGTH, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_IMAGE_WIDTH, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_MAKE, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_MODEL, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_ORIENTATION, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_PLANAR_CONFIGURATION, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_PRIMARY_CHROMATICITIES, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_REFERENCE_BLACK_WHITE, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_RESOLUTION_UNIT, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_ROWS_PER_STRIP, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_SAMPLES_PER_PIXEL, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_SOFTWARE, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_STRIP_BYTE_COUNTS, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_STRIP_OFFSETS, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_TRANSFER_FUNCTION, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_WHITE_POINT, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_X_RESOLUTION, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_Y_CB_CR_COEFFICIENTS, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_Y_CB_CR_POSITIONING, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_Y_RESOLUTION, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_APERTURE_VALUE, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_BRIGHTNESS_VALUE, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_CFA_PATTERN, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_COLOR_SPACE, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_COMPONENTS_CONFIGURATION, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_CONTRAST, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_CUSTOM_RENDERED, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_DATETIME_DIGITIZED, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_DATETIME_ORIGINAL, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_DIGITAL_ZOOM_RATIO, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_EXIF_VERSION, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_EXPOSURE_BIAS_VALUE, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_EXPOSURE_INDEX, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_EXPOSURE_MODE, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_EXPOSURE_PROGRAM, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_EXPOSURE_TIME, 1);
        TYPE_MAPPING.put("FNumber", 1);
        TYPE_MAPPING.put(ExifInterface.TAG_FILE_SOURCE, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_FLASH, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_FLASH_ENERGY, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_FLASHPIX_VERSION, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_FOCAL_LENGTH, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_GAIN_CONTROL, 0);
        TYPE_MAPPING.put("ISOSpeedRatings", 0);
        TYPE_MAPPING.put(ExifInterface.TAG_IMAGE_UNIQUE_ID, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_LIGHT_SOURCE, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_MAKER_NOTE, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_MAX_APERTURE_VALUE, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_METERING_MODE, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_NEW_SUBFILE_TYPE, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_OECF, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_PIXEL_X_DIMENSION, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_PIXEL_Y_DIMENSION, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_RELATED_SOUND_FILE, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_SATURATION, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_SCENE_CAPTURE_TYPE, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_SCENE_TYPE, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_SENSING_METHOD, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_SHARPNESS, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_SHUTTER_SPEED_VALUE, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_SPECTRAL_SENSITIVITY, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_SUBFILE_TYPE, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_SUBSEC_TIME, 2);
        TYPE_MAPPING.put("SubSecTimeDigitized", 2);
        TYPE_MAPPING.put("SubSecTimeOriginal", 2);
        TYPE_MAPPING.put(ExifInterface.TAG_SUBJECT_AREA, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_SUBJECT_DISTANCE, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_SUBJECT_DISTANCE_RANGE, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_SUBJECT_LOCATION, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_USER_COMMENT, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_WHITE_BALANCE, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_ALTITUDE, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_ALTITUDE_REF, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_AREA_INFORMATION, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_DOP, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_DATESTAMP, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_DEST_BEARING, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_DEST_BEARING_REF, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_DEST_DISTANCE, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_DEST_DISTANCE_REF, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_DEST_LATITUDE, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_DEST_LATITUDE_REF, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_DEST_LONGITUDE, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_DEST_LONGITUDE_REF, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_DIFFERENTIAL, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_IMG_DIRECTION, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_IMG_DIRECTION_REF, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_LATITUDE, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_LATITUDE_REF, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_LONGITUDE, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_LONGITUDE_REF, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_MAP_DATUM, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_MEASURE_MODE, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_PROCESSING_METHOD, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_SATELLITES, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_SPEED, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_SPEED_REF, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_STATUS, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_TIMESTAMP, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_TRACK, 1);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_TRACK_REF, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_GPS_VERSION_ID, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_INTEROPERABILITY_INDEX, 2);
        TYPE_MAPPING.put(ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_DNG_VERSION, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_DEFAULT_CROP_SIZE, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_ORF_PREVIEW_IMAGE_START, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_ORF_PREVIEW_IMAGE_LENGTH, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_ORF_ASPECT_FRAME, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_RW2_SENSOR_BOTTOM_BORDER, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_RW2_SENSOR_LEFT_BORDER, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_RW2_SENSOR_RIGHT_BORDER, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_RW2_SENSOR_TOP_BORDER, 0);
        TYPE_MAPPING.put(ExifInterface.TAG_RW2_ISO, 0);
    }

    public static boolean isSupportedMimeType(String mimeType) {
        return JPG_MIME_TYPE.equals(mimeType) || JPEG_MIME_TYPE.equals(mimeType);
    }

    public static void getMetadata(Bundle metadata, InputStream stream, String mimeType, String[] tags) throws IOException {
        List<String> metadataTypes = new ArrayList<>();
        if (isSupportedMimeType(mimeType)) {
            Bundle exifData = getExifData(stream, tags);
            if (exifData.size() > 0) {
                metadata.putBundle(DocumentsContract.METADATA_EXIF, exifData);
                metadataTypes.add(DocumentsContract.METADATA_EXIF);
            }
        }
        metadata.putStringArray(DocumentsContract.METADATA_TYPES, (String[]) metadataTypes.toArray(new String[metadataTypes.size()]));
    }

    private static Bundle getExifData(InputStream stream, String[] tags) throws IOException {
        String data;
        if (tags == null) {
            tags = DEFAULT_EXIF_TAGS;
        }
        ExifInterface exifInterface = new ExifInterface(stream);
        Bundle exif = new Bundle();
        for (String tag : tags) {
            if (TYPE_MAPPING.get(tag).equals(0)) {
                int data2 = exifInterface.getAttributeInt(tag, Integer.MIN_VALUE);
                if (data2 != Integer.MIN_VALUE) {
                    exif.putInt(tag, data2);
                }
            } else if (TYPE_MAPPING.get(tag).equals(1)) {
                double data3 = exifInterface.getAttributeDouble(tag, Double.MIN_VALUE);
                if (data3 != Double.MIN_VALUE) {
                    exif.putDouble(tag, data3);
                }
            } else if (TYPE_MAPPING.get(tag).equals(2) && (data = exifInterface.getAttribute(tag)) != null) {
                exif.putString(tag, data);
            }
        }
        return exif;
    }
}
