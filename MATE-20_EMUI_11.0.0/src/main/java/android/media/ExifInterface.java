package android.media;

import android.annotation.UnsupportedAppUsage;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiScanner;
import android.opengl.GLES30;
import android.security.keymaster.KeymasterDefs;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.Log;
import android.util.Pair;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.gsm.SmsCbConstants;
import com.android.internal.util.ArrayUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.io.IoUtils;
import libcore.io.Streams;

public class ExifInterface {
    private static final Charset ASCII = Charset.forName("US-ASCII");
    private static final int[] BITS_PER_SAMPLE_GREYSCALE_1 = {4};
    private static final int[] BITS_PER_SAMPLE_GREYSCALE_2 = {8};
    private static final int[] BITS_PER_SAMPLE_RGB = {8, 8, 8};
    private static final short BYTE_ALIGN_II = 18761;
    private static final short BYTE_ALIGN_MM = 19789;
    private static final int DATA_DEFLATE_ZIP = 8;
    private static final int DATA_HUFFMAN_COMPRESSED = 2;
    private static final int DATA_JPEG = 6;
    private static final int DATA_JPEG_COMPRESSED = 7;
    private static final int DATA_LOSSY_JPEG = 34892;
    private static final int DATA_PACK_BITS_COMPRESSED = 32773;
    private static final int DATA_UNCOMPRESSED = 1;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final byte[] EXIF_ASCII_PREFIX = {65, 83, 67, 73, 73, 0, 0, 0};
    private static final int EXIF_HEAD_LENGTH = 6;
    private static final ExifTag[] EXIF_POINTER_TAGS = {new ExifTag(TAG_SUB_IFD_POINTER, 330, 4), new ExifTag(TAG_EXIF_IFD_POINTER, 34665, 4), new ExifTag(TAG_GPS_INFO_IFD_POINTER, (int) GLES30.GL_DRAW_BUFFER0, 4), new ExifTag(TAG_INTEROPERABILITY_IFD_POINTER, 40965, 4), new ExifTag(TAG_ORF_CAMERA_SETTINGS_IFD_POINTER, 8224, 1), new ExifTag(TAG_ORF_IMAGE_PROCESSING_IFD_POINTER, 8256, 1)};
    private static final ExifTag[][] EXIF_TAGS;
    private static final byte[] HEIF_BRAND_HEIC = {104, 101, 105, 99};
    private static final byte[] HEIF_BRAND_MIF1 = {109, 105, 102, 49};
    private static final byte[] HEIF_TYPE_FTYP = {102, 116, 121, 112};
    private static final String HW_MAKER_NOTE = "HwMakerNote";
    private static final String HW_MNOTE_ISO = "ISO-8859-1";
    private static final byte[] IDENTIFIER_EXIF_APP1 = "Exif\u0000\u0000".getBytes(ASCII);
    private static final byte[] IDENTIFIER_XMP_APP1 = "http://ns.adobe.com/xap/1.0/\u0000".getBytes(ASCII);
    private static final ExifTag[] IFD_EXIF_TAGS = {new ExifTag(TAG_EXPOSURE_TIME, 33434, 5), new ExifTag("FNumber", 33437, 5), new ExifTag(TAG_EXPOSURE_PROGRAM, 34850, 3), new ExifTag(TAG_SPECTRAL_SENSITIVITY, (int) GLES30.GL_MAX_DRAW_BUFFERS, 2), new ExifTag("ISOSpeedRatings", (int) GLES30.GL_DRAW_BUFFER2, 3), new ExifTag(TAG_OECF, (int) GLES30.GL_DRAW_BUFFER3, 7), new ExifTag(TAG_EXIF_VERSION, 36864, 2), new ExifTag(TAG_DATETIME_ORIGINAL, 36867, 2), new ExifTag(TAG_DATETIME_DIGITIZED, 36868, 2), new ExifTag(TAG_OFFSET_TIME, 36880, 2), new ExifTag(TAG_OFFSET_TIME_ORIGINAL, 36881, 2), new ExifTag(TAG_OFFSET_TIME_DIGITIZED, 36882, 2), new ExifTag(TAG_COMPONENTS_CONFIGURATION, 37121, 7), new ExifTag(TAG_COMPRESSED_BITS_PER_PIXEL, 37122, 5), new ExifTag(TAG_SHUTTER_SPEED_VALUE, 37377, 10), new ExifTag(TAG_APERTURE_VALUE, 37378, 5), new ExifTag(TAG_BRIGHTNESS_VALUE, 37379, 10), new ExifTag(TAG_EXPOSURE_BIAS_VALUE, 37380, 10), new ExifTag(TAG_MAX_APERTURE_VALUE, 37381, 5), new ExifTag(TAG_SUBJECT_DISTANCE, 37382, 5), new ExifTag(TAG_METERING_MODE, 37383, 3), new ExifTag(TAG_LIGHT_SOURCE, 37384, 3), new ExifTag(TAG_FLASH, 37385, 3), new ExifTag(TAG_FOCAL_LENGTH, 37386, 5), new ExifTag(TAG_SUBJECT_AREA, 37396, 3), new ExifTag(TAG_MAKER_NOTE, 37500, 7), new ExifTag(TAG_USER_COMMENT, 37510, 7), new ExifTag(TAG_SUBSEC_TIME, 37520, 2), new ExifTag("SubSecTimeOriginal", 37521, 2), new ExifTag("SubSecTimeDigitized", 37522, 2), new ExifTag(TAG_FLASHPIX_VERSION, 40960, 7), new ExifTag(TAG_COLOR_SPACE, 40961, 3), new ExifTag(TAG_PIXEL_X_DIMENSION, 40962, 3, 4), new ExifTag(TAG_PIXEL_Y_DIMENSION, 40963, 3, 4), new ExifTag(TAG_RELATED_SOUND_FILE, 40964, 2), new ExifTag(TAG_INTEROPERABILITY_IFD_POINTER, 40965, 4), new ExifTag(TAG_FLASH_ENERGY, 41483, 5), new ExifTag(TAG_SPATIAL_FREQUENCY_RESPONSE, 41484, 7), new ExifTag(TAG_FOCAL_PLANE_X_RESOLUTION, 41486, 5), new ExifTag(TAG_FOCAL_PLANE_Y_RESOLUTION, 41487, 5), new ExifTag(TAG_FOCAL_PLANE_RESOLUTION_UNIT, 41488, 3), new ExifTag(TAG_SUBJECT_LOCATION, 41492, 3), new ExifTag(TAG_EXPOSURE_INDEX, 41493, 5), new ExifTag(TAG_SENSING_METHOD, 41495, 3), new ExifTag(TAG_FILE_SOURCE, 41728, 7), new ExifTag(TAG_SCENE_TYPE, 41729, 7), new ExifTag(TAG_CFA_PATTERN, 41730, 7), new ExifTag(TAG_CUSTOM_RENDERED, 41985, 3), new ExifTag(TAG_EXPOSURE_MODE, 41986, 3), new ExifTag(TAG_WHITE_BALANCE, 41987, 3), new ExifTag(TAG_DIGITAL_ZOOM_RATIO, 41988, 5), new ExifTag(TAG_FOCAL_LENGTH_IN_35MM_FILM, 41989, 3), new ExifTag(TAG_SCENE_CAPTURE_TYPE, 41990, 3), new ExifTag(TAG_GAIN_CONTROL, 41991, 3), new ExifTag(TAG_CONTRAST, 41992, 3), new ExifTag(TAG_SATURATION, 41993, 3), new ExifTag(TAG_SHARPNESS, 41994, 3), new ExifTag(TAG_DEVICE_SETTING_DESCRIPTION, 41995, 7), new ExifTag(TAG_SUBJECT_DISTANCE_RANGE, 41996, 3), new ExifTag(TAG_IMAGE_UNIQUE_ID, 42016, 2), new ExifTag(TAG_DNG_VERSION, 50706, 1), new ExifTag(TAG_DEFAULT_CROP_SIZE, 50720, 3, 4)};
    private static final int IFD_FORMAT_BYTE = 1;
    private static final int[] IFD_FORMAT_BYTES_PER_FORMAT = {0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8, 1};
    private static final int IFD_FORMAT_DOUBLE = 12;
    private static final int IFD_FORMAT_IFD = 13;
    private static final String[] IFD_FORMAT_NAMES = {"", "BYTE", "STRING", "USHORT", "ULONG", "URATIONAL", "SBYTE", HuaweiTelephonyConfigs.VALUE_CHIP_PLATFORM_UNDEFINED, "SSHORT", "SLONG", "SRATIONAL", "SINGLE", "DOUBLE"};
    private static final int IFD_FORMAT_SBYTE = 6;
    private static final int IFD_FORMAT_SINGLE = 11;
    private static final int IFD_FORMAT_SLONG = 9;
    private static final int IFD_FORMAT_SRATIONAL = 10;
    private static final int IFD_FORMAT_SSHORT = 8;
    private static final int IFD_FORMAT_STRING = 2;
    private static final int IFD_FORMAT_ULONG = 4;
    private static final int IFD_FORMAT_UNDEFINED = 7;
    private static final int IFD_FORMAT_URATIONAL = 5;
    private static final int IFD_FORMAT_USHORT = 3;
    private static final ExifTag[] IFD_GPS_TAGS = {new ExifTag(TAG_GPS_VERSION_ID, 0, 1), new ExifTag(TAG_GPS_LATITUDE_REF, 1, 2), new ExifTag(TAG_GPS_LATITUDE, 2, 5), new ExifTag(TAG_GPS_LONGITUDE_REF, 3, 2), new ExifTag(TAG_GPS_LONGITUDE, 4, 5), new ExifTag(TAG_GPS_ALTITUDE_REF, 5, 1), new ExifTag(TAG_GPS_ALTITUDE, 6, 5), new ExifTag(TAG_GPS_TIMESTAMP, 7, 5), new ExifTag(TAG_GPS_SATELLITES, 8, 2), new ExifTag(TAG_GPS_STATUS, 9, 2), new ExifTag(TAG_GPS_MEASURE_MODE, 10, 2), new ExifTag(TAG_GPS_DOP, 11, 5), new ExifTag(TAG_GPS_SPEED_REF, 12, 2), new ExifTag(TAG_GPS_SPEED, 13, 5), new ExifTag(TAG_GPS_TRACK_REF, 14, 2), new ExifTag(TAG_GPS_TRACK, 15, 5), new ExifTag(TAG_GPS_IMG_DIRECTION_REF, 16, 2), new ExifTag(TAG_GPS_IMG_DIRECTION, 17, 5), new ExifTag(TAG_GPS_MAP_DATUM, 18, 2), new ExifTag(TAG_GPS_DEST_LATITUDE_REF, 19, 2), new ExifTag(TAG_GPS_DEST_LATITUDE, 20, 5), new ExifTag(TAG_GPS_DEST_LONGITUDE_REF, 21, 2), new ExifTag(TAG_GPS_DEST_LONGITUDE, 22, 5), new ExifTag(TAG_GPS_DEST_BEARING_REF, 23, 2), new ExifTag(TAG_GPS_DEST_BEARING, 24, 5), new ExifTag(TAG_GPS_DEST_DISTANCE_REF, 25, 2), new ExifTag(TAG_GPS_DEST_DISTANCE, 26, 5), new ExifTag(TAG_GPS_PROCESSING_METHOD, 27, 7), new ExifTag(TAG_GPS_AREA_INFORMATION, 28, 7), new ExifTag(TAG_GPS_DATESTAMP, 29, 2), new ExifTag(TAG_GPS_DIFFERENTIAL, 30, 3)};
    private static final ExifTag[] IFD_INTEROPERABILITY_TAGS = {new ExifTag(TAG_INTEROPERABILITY_INDEX, 1, 2)};
    private static final int IFD_OFFSET = 8;
    private static final ExifTag[] IFD_THUMBNAIL_TAGS = {new ExifTag(TAG_NEW_SUBFILE_TYPE, 254, 4), new ExifTag(TAG_SUBFILE_TYPE, 255, 4), new ExifTag(TAG_THUMBNAIL_IMAGE_WIDTH, 256, 3, 4), new ExifTag(TAG_THUMBNAIL_IMAGE_LENGTH, 257, 3, 4), new ExifTag(TAG_BITS_PER_SAMPLE, 258, 3), new ExifTag(TAG_COMPRESSION, 259, 3), new ExifTag(TAG_PHOTOMETRIC_INTERPRETATION, 262, 3), new ExifTag(TAG_IMAGE_DESCRIPTION, 270, 2), new ExifTag(TAG_MAKE, 271, 2), new ExifTag(TAG_MODEL, 272, 2), new ExifTag(TAG_STRIP_OFFSETS, 273, 3, 4), new ExifTag(TAG_ORIENTATION, 274, 3), new ExifTag(TAG_SAMPLES_PER_PIXEL, 277, 3), new ExifTag(TAG_ROWS_PER_STRIP, 278, 3, 4), new ExifTag(TAG_STRIP_BYTE_COUNTS, 279, 3, 4), new ExifTag(TAG_X_RESOLUTION, 282, 5), new ExifTag(TAG_Y_RESOLUTION, 283, 5), new ExifTag(TAG_PLANAR_CONFIGURATION, 284, 3), new ExifTag(TAG_RESOLUTION_UNIT, 296, 3), new ExifTag(TAG_TRANSFER_FUNCTION, 301, 3), new ExifTag(TAG_SOFTWARE, 305, 2), new ExifTag(TAG_DATETIME, 306, 2), new ExifTag(TAG_ARTIST, 315, 2), new ExifTag(TAG_WHITE_POINT, 318, 5), new ExifTag(TAG_PRIMARY_CHROMATICITIES, 319, 5), new ExifTag(TAG_SUB_IFD_POINTER, 330, 4), new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT, 513, 4), new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, 514, 4), new ExifTag(TAG_Y_CB_CR_COEFFICIENTS, 529, 5), new ExifTag(TAG_Y_CB_CR_SUB_SAMPLING, 530, 3), new ExifTag(TAG_Y_CB_CR_POSITIONING, 531, 3), new ExifTag(TAG_REFERENCE_BLACK_WHITE, 532, 5), new ExifTag(TAG_COPYRIGHT, 33432, 2), new ExifTag(TAG_EXIF_IFD_POINTER, 34665, 4), new ExifTag(TAG_GPS_INFO_IFD_POINTER, (int) GLES30.GL_DRAW_BUFFER0, 4), new ExifTag(TAG_DNG_VERSION, 50706, 1), new ExifTag(TAG_DEFAULT_CROP_SIZE, 50720, 3, 4)};
    private static final ExifTag[] IFD_TIFF_TAGS = {new ExifTag(TAG_NEW_SUBFILE_TYPE, 254, 4), new ExifTag(TAG_SUBFILE_TYPE, 255, 4), new ExifTag(TAG_IMAGE_WIDTH, 256, 3, 4), new ExifTag(TAG_IMAGE_LENGTH, 257, 3, 4), new ExifTag(TAG_BITS_PER_SAMPLE, 258, 3), new ExifTag(TAG_COMPRESSION, 259, 3), new ExifTag(TAG_PHOTOMETRIC_INTERPRETATION, 262, 3), new ExifTag(TAG_IMAGE_DESCRIPTION, 270, 2), new ExifTag(TAG_MAKE, 271, 2), new ExifTag(TAG_MODEL, 272, 2), new ExifTag(TAG_STRIP_OFFSETS, 273, 3, 4), new ExifTag(TAG_ORIENTATION, 274, 3), new ExifTag(TAG_SAMPLES_PER_PIXEL, 277, 3), new ExifTag(TAG_ROWS_PER_STRIP, 278, 3, 4), new ExifTag(TAG_STRIP_BYTE_COUNTS, 279, 3, 4), new ExifTag(TAG_X_RESOLUTION, 282, 5), new ExifTag(TAG_Y_RESOLUTION, 283, 5), new ExifTag(TAG_PLANAR_CONFIGURATION, 284, 3), new ExifTag(TAG_RESOLUTION_UNIT, 296, 3), new ExifTag(TAG_TRANSFER_FUNCTION, 301, 3), new ExifTag(TAG_SOFTWARE, 305, 2), new ExifTag(TAG_DATETIME, 306, 2), new ExifTag(TAG_ARTIST, 315, 2), new ExifTag(TAG_WHITE_POINT, 318, 5), new ExifTag(TAG_PRIMARY_CHROMATICITIES, 319, 5), new ExifTag(TAG_SUB_IFD_POINTER, 330, 4), new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT, 513, 4), new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, 514, 4), new ExifTag(TAG_Y_CB_CR_COEFFICIENTS, 529, 5), new ExifTag(TAG_Y_CB_CR_SUB_SAMPLING, 530, 3), new ExifTag(TAG_Y_CB_CR_POSITIONING, 531, 3), new ExifTag(TAG_REFERENCE_BLACK_WHITE, 532, 5), new ExifTag(TAG_COPYRIGHT, 33432, 2), new ExifTag(TAG_EXIF_IFD_POINTER, 34665, 4), new ExifTag(TAG_GPS_INFO_IFD_POINTER, (int) GLES30.GL_DRAW_BUFFER0, 4), new ExifTag(TAG_RW2_SENSOR_TOP_BORDER, 4, 4), new ExifTag(TAG_RW2_SENSOR_LEFT_BORDER, 5, 4), new ExifTag(TAG_RW2_SENSOR_BOTTOM_BORDER, 6, 4), new ExifTag(TAG_RW2_SENSOR_RIGHT_BORDER, 7, 4), new ExifTag(TAG_RW2_ISO, 23, 3), new ExifTag(TAG_RW2_JPG_FROM_RAW, 46, 7), new ExifTag(TAG_XMP, 700, 1)};
    private static final int IFD_TYPE_EXIF = 1;
    private static final int IFD_TYPE_GPS = 2;
    private static final int IFD_TYPE_INTEROPERABILITY = 3;
    private static final int IFD_TYPE_ORF_CAMERA_SETTINGS = 7;
    private static final int IFD_TYPE_ORF_IMAGE_PROCESSING = 8;
    private static final int IFD_TYPE_ORF_MAKER_NOTE = 6;
    private static final int IFD_TYPE_PEF = 9;
    private static final int IFD_TYPE_PREVIEW = 5;
    private static final int IFD_TYPE_PRIMARY = 0;
    private static final int IFD_TYPE_THUMBNAIL = 4;
    private static final int IMAGE_TYPE_ARW = 1;
    private static final int IMAGE_TYPE_CR2 = 2;
    private static final int IMAGE_TYPE_DNG = 3;
    private static final int IMAGE_TYPE_HEIF = 12;
    private static final int IMAGE_TYPE_JPEG = 4;
    private static final int IMAGE_TYPE_NEF = 5;
    private static final int IMAGE_TYPE_NRW = 6;
    private static final int IMAGE_TYPE_ORF = 7;
    private static final int IMAGE_TYPE_PEF = 8;
    private static final int IMAGE_TYPE_RAF = 9;
    private static final int IMAGE_TYPE_RW2 = 10;
    private static final int IMAGE_TYPE_SRW = 11;
    private static final int IMAGE_TYPE_UNKNOWN = 0;
    private static final ExifTag JPEG_INTERCHANGE_FORMAT_LENGTH_TAG = new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, 514, 4);
    private static final ExifTag JPEG_INTERCHANGE_FORMAT_TAG = new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT, 513, 4);
    private static final byte[] JPEG_SIGNATURE = {-1, MARKER_SOI, -1};
    private static final Object LOCK = new Object();
    private static final byte MARKER = -1;
    private static final byte MARKER_APP1 = -31;
    private static final byte MARKER_COM = -2;
    private static final byte MARKER_EOI = -39;
    private static final byte MARKER_SOF0 = -64;
    private static final byte MARKER_SOF1 = -63;
    private static final byte MARKER_SOF10 = -54;
    private static final byte MARKER_SOF11 = -53;
    private static final byte MARKER_SOF13 = -51;
    private static final byte MARKER_SOF14 = -50;
    private static final byte MARKER_SOF15 = -49;
    private static final byte MARKER_SOF2 = -62;
    private static final byte MARKER_SOF3 = -61;
    private static final byte MARKER_SOF5 = -59;
    private static final byte MARKER_SOF6 = -58;
    private static final byte MARKER_SOF7 = -57;
    private static final byte MARKER_SOF9 = -55;
    private static final byte MARKER_SOI = -40;
    private static final byte MARKER_SOS = -38;
    private static final int MAX_THUMBNAIL_SIZE = 512;
    private static final byte[] MNOTE_HW_HEADER = {72, 85, 65, 87, 69, 73, 0, 0};
    private static final ExifTag[] ORF_CAMERA_SETTINGS_TAGS = {new ExifTag(TAG_ORF_PREVIEW_IMAGE_START, 257, 4), new ExifTag(TAG_ORF_PREVIEW_IMAGE_LENGTH, 258, 4)};
    private static final ExifTag[] ORF_IMAGE_PROCESSING_TAGS = {new ExifTag(TAG_ORF_ASPECT_FRAME, (int) SmsCbConstants.MESSAGE_ID_CMAS_ALERT_EXTREME_IMMEDIATE_OBSERVED, 3)};
    private static final byte[] ORF_MAKER_NOTE_HEADER_1 = {79, 76, 89, 77, 80, 0};
    private static final int ORF_MAKER_NOTE_HEADER_1_SIZE = 8;
    private static final byte[] ORF_MAKER_NOTE_HEADER_2 = {79, 76, 89, 77, 80, 85, 83, 0, 73, 73};
    private static final int ORF_MAKER_NOTE_HEADER_2_SIZE = 12;
    private static final ExifTag[] ORF_MAKER_NOTE_TAGS = {new ExifTag(TAG_ORF_THUMBNAIL_IMAGE, 256, 7), new ExifTag(TAG_ORF_CAMERA_SETTINGS_IFD_POINTER, 8224, 4), new ExifTag(TAG_ORF_IMAGE_PROCESSING_IFD_POINTER, 8256, 4)};
    private static final short ORF_SIGNATURE_1 = 20306;
    private static final short ORF_SIGNATURE_2 = 21330;
    public static final int ORIENTATION_FLIP_HORIZONTAL = 2;
    public static final int ORIENTATION_FLIP_VERTICAL = 4;
    public static final int ORIENTATION_NORMAL = 1;
    public static final int ORIENTATION_ROTATE_180 = 3;
    public static final int ORIENTATION_ROTATE_270 = 8;
    public static final int ORIENTATION_ROTATE_90 = 6;
    public static final int ORIENTATION_TRANSPOSE = 5;
    public static final int ORIENTATION_TRANSVERSE = 7;
    public static final int ORIENTATION_UNDEFINED = 0;
    private static final int ORIGINAL_RESOLUTION_IMAGE = 0;
    private static final String[] PATTERNS = {"yyyy:MM:dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy.MM.dd HH:mm:ss"};
    private static final int PEF_MAKER_NOTE_SKIP_SIZE = 6;
    private static final String PEF_SIGNATURE = "PENTAX";
    private static final ExifTag[] PEF_TAGS = {new ExifTag(TAG_COLOR_SPACE, 55, 3)};
    private static final int PHOTOMETRIC_INTERPRETATION_BLACK_IS_ZERO = 1;
    private static final int PHOTOMETRIC_INTERPRETATION_RGB = 2;
    private static final int PHOTOMETRIC_INTERPRETATION_WHITE_IS_ZERO = 0;
    private static final int PHOTOMETRIC_INTERPRETATION_YCBCR = 6;
    private static final int RAF_INFO_SIZE = 160;
    private static final int RAF_JPEG_LENGTH_VALUE_SIZE = 4;
    private static final int RAF_OFFSET_TO_JPEG_IMAGE_OFFSET = 84;
    private static final String RAF_SIGNATURE = "FUJIFILMCCD-RAW";
    private static final int REDUCED_RESOLUTION_IMAGE = 1;
    private static final short RW2_SIGNATURE = 85;
    private static final int SIGNATURE_CHECK_SIZE = 5000;
    private static final int SOI_LENGTH = 2;
    private static final byte START_CODE = 42;
    private static final String TAG = "ExifInterface";
    @Deprecated
    public static final String TAG_APERTURE = "FNumber";
    public static final String TAG_APERTURE_VALUE = "ApertureValue";
    public static final String TAG_ARTIST = "Artist";
    public static final String TAG_BITS_PER_SAMPLE = "BitsPerSample";
    public static final String TAG_BRIGHTNESS_VALUE = "BrightnessValue";
    public static final String TAG_CFA_PATTERN = "CFAPattern";
    public static final String TAG_COLOR_SPACE = "ColorSpace";
    public static final String TAG_COMPONENTS_CONFIGURATION = "ComponentsConfiguration";
    public static final String TAG_COMPRESSED_BITS_PER_PIXEL = "CompressedBitsPerPixel";
    public static final String TAG_COMPRESSION = "Compression";
    public static final String TAG_CONTRAST = "Contrast";
    public static final String TAG_COPYRIGHT = "Copyright";
    public static final String TAG_CUSTOM_RENDERED = "CustomRendered";
    public static final String TAG_DATETIME = "DateTime";
    public static final String TAG_DATETIME_DIGITIZED = "DateTimeDigitized";
    public static final String TAG_DATETIME_ORIGINAL = "DateTimeOriginal";
    public static final String TAG_DEFAULT_CROP_SIZE = "DefaultCropSize";
    public static final String TAG_DEVICE_SETTING_DESCRIPTION = "DeviceSettingDescription";
    public static final String TAG_DIGITAL_ZOOM_RATIO = "DigitalZoomRatio";
    public static final String TAG_DNG_VERSION = "DNGVersion";
    private static final String TAG_EXIF_IFD_POINTER = "ExifIFDPointer";
    public static final String TAG_EXIF_VERSION = "ExifVersion";
    public static final String TAG_EXPOSURE_BIAS_VALUE = "ExposureBiasValue";
    public static final String TAG_EXPOSURE_INDEX = "ExposureIndex";
    public static final String TAG_EXPOSURE_MODE = "ExposureMode";
    public static final String TAG_EXPOSURE_PROGRAM = "ExposureProgram";
    public static final String TAG_EXPOSURE_TIME = "ExposureTime";
    public static final String TAG_FILE_SOURCE = "FileSource";
    public static final String TAG_FLASH = "Flash";
    public static final String TAG_FLASHPIX_VERSION = "FlashpixVersion";
    public static final String TAG_FLASH_ENERGY = "FlashEnergy";
    public static final String TAG_FOCAL_LENGTH = "FocalLength";
    public static final String TAG_FOCAL_LENGTH_IN_35MM_FILM = "FocalLengthIn35mmFilm";
    public static final String TAG_FOCAL_PLANE_RESOLUTION_UNIT = "FocalPlaneResolutionUnit";
    public static final String TAG_FOCAL_PLANE_X_RESOLUTION = "FocalPlaneXResolution";
    public static final String TAG_FOCAL_PLANE_Y_RESOLUTION = "FocalPlaneYResolution";
    public static final String TAG_F_NUMBER = "FNumber";
    public static final String TAG_GAIN_CONTROL = "GainControl";
    public static final String TAG_GPS_ALTITUDE = "GPSAltitude";
    public static final String TAG_GPS_ALTITUDE_REF = "GPSAltitudeRef";
    public static final String TAG_GPS_AREA_INFORMATION = "GPSAreaInformation";
    public static final String TAG_GPS_DATESTAMP = "GPSDateStamp";
    public static final String TAG_GPS_DEST_BEARING = "GPSDestBearing";
    public static final String TAG_GPS_DEST_BEARING_REF = "GPSDestBearingRef";
    public static final String TAG_GPS_DEST_DISTANCE = "GPSDestDistance";
    public static final String TAG_GPS_DEST_DISTANCE_REF = "GPSDestDistanceRef";
    public static final String TAG_GPS_DEST_LATITUDE = "GPSDestLatitude";
    public static final String TAG_GPS_DEST_LATITUDE_REF = "GPSDestLatitudeRef";
    public static final String TAG_GPS_DEST_LONGITUDE = "GPSDestLongitude";
    public static final String TAG_GPS_DEST_LONGITUDE_REF = "GPSDestLongitudeRef";
    public static final String TAG_GPS_DIFFERENTIAL = "GPSDifferential";
    public static final String TAG_GPS_DOP = "GPSDOP";
    public static final String TAG_GPS_IMG_DIRECTION = "GPSImgDirection";
    public static final String TAG_GPS_IMG_DIRECTION_REF = "GPSImgDirectionRef";
    private static final String TAG_GPS_INFO_IFD_POINTER = "GPSInfoIFDPointer";
    public static final String TAG_GPS_LATITUDE = "GPSLatitude";
    public static final String TAG_GPS_LATITUDE_REF = "GPSLatitudeRef";
    public static final String TAG_GPS_LONGITUDE = "GPSLongitude";
    public static final String TAG_GPS_LONGITUDE_REF = "GPSLongitudeRef";
    public static final String TAG_GPS_MAP_DATUM = "GPSMapDatum";
    public static final String TAG_GPS_MEASURE_MODE = "GPSMeasureMode";
    public static final String TAG_GPS_PROCESSING_METHOD = "GPSProcessingMethod";
    public static final String TAG_GPS_SATELLITES = "GPSSatellites";
    public static final String TAG_GPS_SPEED = "GPSSpeed";
    public static final String TAG_GPS_SPEED_REF = "GPSSpeedRef";
    public static final String TAG_GPS_STATUS = "GPSStatus";
    public static final String TAG_GPS_TIMESTAMP = "GPSTimeStamp";
    public static final String TAG_GPS_TRACK = "GPSTrack";
    public static final String TAG_GPS_TRACK_REF = "GPSTrackRef";
    public static final String TAG_GPS_VERSION_ID = "GPSVersionID";
    private static final String TAG_HAS_THUMBNAIL = "HasThumbnail";
    public static final String TAG_IMAGE_DESCRIPTION = "ImageDescription";
    public static final String TAG_IMAGE_LENGTH = "ImageLength";
    public static final String TAG_IMAGE_UNIQUE_ID = "ImageUniqueID";
    public static final String TAG_IMAGE_WIDTH = "ImageWidth";
    private static final String TAG_INTEROPERABILITY_IFD_POINTER = "InteroperabilityIFDPointer";
    public static final String TAG_INTEROPERABILITY_INDEX = "InteroperabilityIndex";
    @Deprecated
    public static final String TAG_ISO = "ISOSpeedRatings";
    public static final String TAG_ISO_SPEED_RATINGS = "ISOSpeedRatings";
    public static final String TAG_JPEG_INTERCHANGE_FORMAT = "JPEGInterchangeFormat";
    public static final String TAG_JPEG_INTERCHANGE_FORMAT_LENGTH = "JPEGInterchangeFormatLength";
    public static final String TAG_LIGHT_SOURCE = "LightSource";
    public static final String TAG_MAKE = "Make";
    public static final String TAG_MAKER_NOTE = "MakerNote";
    public static final String TAG_MAX_APERTURE_VALUE = "MaxApertureValue";
    public static final String TAG_METERING_MODE = "MeteringMode";
    public static final String TAG_MODEL = "Model";
    public static final String TAG_NEW_SUBFILE_TYPE = "NewSubfileType";
    public static final String TAG_OECF = "OECF";
    public static final String TAG_OFFSET_TIME = "OffsetTime";
    public static final String TAG_OFFSET_TIME_DIGITIZED = "OffsetTimeDigitized";
    public static final String TAG_OFFSET_TIME_ORIGINAL = "OffsetTimeOriginal";
    public static final String TAG_ORF_ASPECT_FRAME = "AspectFrame";
    private static final String TAG_ORF_CAMERA_SETTINGS_IFD_POINTER = "CameraSettingsIFDPointer";
    private static final String TAG_ORF_IMAGE_PROCESSING_IFD_POINTER = "ImageProcessingIFDPointer";
    public static final String TAG_ORF_PREVIEW_IMAGE_LENGTH = "PreviewImageLength";
    public static final String TAG_ORF_PREVIEW_IMAGE_START = "PreviewImageStart";
    public static final String TAG_ORF_THUMBNAIL_IMAGE = "ThumbnailImage";
    public static final String TAG_ORIENTATION = "Orientation";
    public static final String TAG_PHOTOMETRIC_INTERPRETATION = "PhotometricInterpretation";
    public static final String TAG_PIXEL_X_DIMENSION = "PixelXDimension";
    public static final String TAG_PIXEL_Y_DIMENSION = "PixelYDimension";
    public static final String TAG_PLANAR_CONFIGURATION = "PlanarConfiguration";
    public static final String TAG_PRIMARY_CHROMATICITIES = "PrimaryChromaticities";
    private static final ExifTag TAG_RAF_IMAGE_SIZE = new ExifTag(TAG_STRIP_OFFSETS, 273, 3);
    public static final String TAG_REFERENCE_BLACK_WHITE = "ReferenceBlackWhite";
    public static final String TAG_RELATED_SOUND_FILE = "RelatedSoundFile";
    public static final String TAG_RESOLUTION_UNIT = "ResolutionUnit";
    public static final String TAG_ROWS_PER_STRIP = "RowsPerStrip";
    public static final String TAG_RW2_ISO = "ISO";
    public static final String TAG_RW2_JPG_FROM_RAW = "JpgFromRaw";
    public static final String TAG_RW2_SENSOR_BOTTOM_BORDER = "SensorBottomBorder";
    public static final String TAG_RW2_SENSOR_LEFT_BORDER = "SensorLeftBorder";
    public static final String TAG_RW2_SENSOR_RIGHT_BORDER = "SensorRightBorder";
    public static final String TAG_RW2_SENSOR_TOP_BORDER = "SensorTopBorder";
    public static final String TAG_SAMPLES_PER_PIXEL = "SamplesPerPixel";
    public static final String TAG_SATURATION = "Saturation";
    public static final String TAG_SCENE_CAPTURE_TYPE = "SceneCaptureType";
    public static final String TAG_SCENE_TYPE = "SceneType";
    public static final String TAG_SENSING_METHOD = "SensingMethod";
    public static final String TAG_SHARPNESS = "Sharpness";
    public static final String TAG_SHUTTER_SPEED_VALUE = "ShutterSpeedValue";
    public static final String TAG_SOFTWARE = "Software";
    public static final String TAG_SPATIAL_FREQUENCY_RESPONSE = "SpatialFrequencyResponse";
    public static final String TAG_SPECTRAL_SENSITIVITY = "SpectralSensitivity";
    public static final String TAG_STRIP_BYTE_COUNTS = "StripByteCounts";
    public static final String TAG_STRIP_OFFSETS = "StripOffsets";
    public static final String TAG_SUBFILE_TYPE = "SubfileType";
    public static final String TAG_SUBJECT_AREA = "SubjectArea";
    public static final String TAG_SUBJECT_DISTANCE = "SubjectDistance";
    public static final String TAG_SUBJECT_DISTANCE_RANGE = "SubjectDistanceRange";
    public static final String TAG_SUBJECT_LOCATION = "SubjectLocation";
    public static final String TAG_SUBSEC_TIME = "SubSecTime";
    public static final String TAG_SUBSEC_TIME_DIG = "SubSecTimeDigitized";
    public static final String TAG_SUBSEC_TIME_DIGITIZED = "SubSecTimeDigitized";
    public static final String TAG_SUBSEC_TIME_ORIG = "SubSecTimeOriginal";
    public static final String TAG_SUBSEC_TIME_ORIGINAL = "SubSecTimeOriginal";
    private static final String TAG_SUB_IFD_POINTER = "SubIFDPointer";
    private static final String TAG_THUMBNAIL_DATA = "ThumbnailData";
    public static final String TAG_THUMBNAIL_IMAGE_LENGTH = "ThumbnailImageLength";
    public static final String TAG_THUMBNAIL_IMAGE_WIDTH = "ThumbnailImageWidth";
    private static final String TAG_THUMBNAIL_LENGTH = "ThumbnailLength";
    private static final String TAG_THUMBNAIL_OFFSET = "ThumbnailOffset";
    public static final String TAG_TRANSFER_FUNCTION = "TransferFunction";
    public static final String TAG_USER_COMMENT = "UserComment";
    public static final String TAG_WHITE_BALANCE = "WhiteBalance";
    public static final String TAG_WHITE_POINT = "WhitePoint";
    public static final String TAG_XMP = "Xmp";
    public static final String TAG_X_RESOLUTION = "XResolution";
    public static final String TAG_Y_CB_CR_COEFFICIENTS = "YCbCrCoefficients";
    public static final String TAG_Y_CB_CR_POSITIONING = "YCbCrPositioning";
    public static final String TAG_Y_CB_CR_SUB_SAMPLING = "YCbCrSubSampling";
    public static final String TAG_Y_RESOLUTION = "YResolution";
    public static final int WHITEBALANCE_AUTO = 0;
    public static final int WHITEBALANCE_MANUAL = 1;
    private static final HashMap<Integer, Integer> sExifPointerTagMap = new HashMap<>();
    private static final HashMap[] sExifTagMapsForReading;
    private static final HashMap[] sExifTagMapsForWriting;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private static SimpleDateFormat sFormatter;
    private static SimpleDateFormat sFormatterTz = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss XXX");
    private static SimpleDateFormat[] sFormatters;
    private static SimpleDateFormat[] sFormattersUTC;
    private static final Pattern sGpsTimestampPattern = Pattern.compile("^([0-9][0-9]):([0-9][0-9]):([0-9][0-9])$");
    private static final Pattern sNonZeroTimePattern = Pattern.compile(".*[1-9].*");
    private static final HashSet<String> sTagSetForCompatibility = new HashSet<>(Arrays.asList("FNumber", TAG_DIGITAL_ZOOM_RATIO, TAG_EXPOSURE_TIME, TAG_SUBJECT_DISTANCE, TAG_GPS_TIMESTAMP));
    private boolean mAreThumbnailStripsConsecutive;
    private AssetManager.AssetInputStream mAssetInputStream;
    @UnsupportedAppUsage
    private final HashMap[] mAttributes;
    private ByteOrder mExifByteOrder = ByteOrder.BIG_ENDIAN;
    private int mExifOffset;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private String mFilename;
    private Set<Integer> mHandledIfdOffsets;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private boolean mHasThumbnail;
    private boolean mHasThumbnailStrips;
    private boolean mIsInputStream;
    private boolean mIsSupportedFile;
    private int mMimeType;
    private boolean mModified;
    private int mOrfMakerNoteOffset;
    private int mOrfThumbnailLength;
    private int mOrfThumbnailOffset;
    private int mRw2JpgFromRawOffset;
    private FileDescriptor mSeekableFileDescriptor;
    private byte[] mThumbnailBytes;
    private int mThumbnailCompression;
    private int mThumbnailLength;
    private int mThumbnailOffset;

    @Retention(RetentionPolicy.SOURCE)
    public @interface IfdType {
    }

    static {
        String[] strArr = PATTERNS;
        sFormatters = new SimpleDateFormat[strArr.length];
        sFormattersUTC = new SimpleDateFormat[strArr.length];
        ExifTag[] exifTagArr = IFD_TIFF_TAGS;
        EXIF_TAGS = new ExifTag[][]{exifTagArr, IFD_EXIF_TAGS, IFD_GPS_TAGS, IFD_INTEROPERABILITY_TAGS, IFD_THUMBNAIL_TAGS, exifTagArr, ORF_MAKER_NOTE_TAGS, ORF_CAMERA_SETTINGS_TAGS, ORF_IMAGE_PROCESSING_TAGS, PEF_TAGS};
        ExifTag[][] exifTagArr2 = EXIF_TAGS;
        sExifTagMapsForReading = new HashMap[exifTagArr2.length];
        sExifTagMapsForWriting = new HashMap[exifTagArr2.length];
        int i = 0;
        while (true) {
            String[] strArr2 = PATTERNS;
            if (i >= strArr2.length) {
                break;
            }
            sFormatters[i] = new SimpleDateFormat(strArr2[i]);
            sFormattersUTC[i] = new SimpleDateFormat(PATTERNS[i]);
            sFormattersUTC[i].setTimeZone(TimeZone.getTimeZone(Time.TIMEZONE_UTC));
            i++;
        }
        sFormatterTz.setTimeZone(TimeZone.getTimeZone(Time.TIMEZONE_UTC));
        for (int ifdType = 0; ifdType < EXIF_TAGS.length; ifdType++) {
            sExifTagMapsForReading[ifdType] = new HashMap();
            sExifTagMapsForWriting[ifdType] = new HashMap();
            ExifTag[] exifTagArr3 = EXIF_TAGS[ifdType];
            for (ExifTag tag : exifTagArr3) {
                sExifTagMapsForReading[ifdType].put(Integer.valueOf(tag.number), tag);
                sExifTagMapsForWriting[ifdType].put(tag.name, tag);
            }
        }
        sExifPointerTagMap.put(Integer.valueOf(EXIF_POINTER_TAGS[0].number), 5);
        sExifPointerTagMap.put(Integer.valueOf(EXIF_POINTER_TAGS[1].number), 1);
        sExifPointerTagMap.put(Integer.valueOf(EXIF_POINTER_TAGS[2].number), 2);
        sExifPointerTagMap.put(Integer.valueOf(EXIF_POINTER_TAGS[3].number), 3);
        sExifPointerTagMap.put(Integer.valueOf(EXIF_POINTER_TAGS[4].number), 7);
        sExifPointerTagMap.put(Integer.valueOf(EXIF_POINTER_TAGS[5].number), 8);
    }

    /* access modifiers changed from: private */
    public static class Rational {
        public final long denominator;
        public final long numerator;

        private Rational(long numerator2, long denominator2) {
            if (denominator2 == 0) {
                this.numerator = 0;
                this.denominator = 1;
                return;
            }
            this.numerator = numerator2;
            this.denominator = denominator2;
        }

        public String toString() {
            return this.numerator + "/" + this.denominator;
        }

        public double calculate() {
            return ((double) this.numerator) / ((double) this.denominator);
        }
    }

    /* access modifiers changed from: private */
    public static class ExifAttribute {
        public static final long BYTES_OFFSET_UNKNOWN = -1;
        public final byte[] bytes;
        public final long bytesOffset;
        public final int format;
        public final int numberOfComponents;

        private ExifAttribute(int format2, int numberOfComponents2, byte[] bytes2) {
            this(format2, numberOfComponents2, -1, bytes2);
        }

        private ExifAttribute(int format2, int numberOfComponents2, long bytesOffset2, byte[] bytes2) {
            this.format = format2;
            this.numberOfComponents = numberOfComponents2;
            this.bytesOffset = bytesOffset2;
            this.bytes = bytes2;
        }

        public static ExifAttribute createUShort(int[] values, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[(ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[3] * values.length)]);
            buffer.order(byteOrder);
            for (int value : values) {
                buffer.putShort((short) value);
            }
            return new ExifAttribute(3, values.length, buffer.array());
        }

        public static ExifAttribute createUShort(int value, ByteOrder byteOrder) {
            return createUShort(new int[]{value}, byteOrder);
        }

        public static ExifAttribute createULong(long[] values, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[(ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[4] * values.length)]);
            buffer.order(byteOrder);
            for (long value : values) {
                buffer.putInt((int) value);
            }
            return new ExifAttribute(4, values.length, buffer.array());
        }

        public static ExifAttribute createULong(long value, ByteOrder byteOrder) {
            return createULong(new long[]{value}, byteOrder);
        }

        public static ExifAttribute createSLong(int[] values, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[(ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[9] * values.length)]);
            buffer.order(byteOrder);
            for (int value : values) {
                buffer.putInt(value);
            }
            return new ExifAttribute(9, values.length, buffer.array());
        }

        public static ExifAttribute createSLong(int value, ByteOrder byteOrder) {
            return createSLong(new int[]{value}, byteOrder);
        }

        public static ExifAttribute createByte(String value) {
            if (value.length() != 1 || value.charAt(0) < '0' || value.charAt(0) > '1') {
                byte[] ascii = value.getBytes(ExifInterface.ASCII);
                return new ExifAttribute(1, ascii.length, ascii);
            }
            byte[] bytes2 = {(byte) (value.charAt(0) - '0')};
            return new ExifAttribute(1, bytes2.length, bytes2);
        }

        public static ExifAttribute createString(String value) {
            byte[] ascii = (value + (char) 0).getBytes(ExifInterface.ASCII);
            return new ExifAttribute(2, ascii.length, ascii);
        }

        public static ExifAttribute createURational(Rational[] values, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[(ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[5] * values.length)]);
            buffer.order(byteOrder);
            for (Rational value : values) {
                buffer.putInt((int) value.numerator);
                buffer.putInt((int) value.denominator);
            }
            return new ExifAttribute(5, values.length, buffer.array());
        }

        public static ExifAttribute createURational(Rational value, ByteOrder byteOrder) {
            return createURational(new Rational[]{value}, byteOrder);
        }

        public static ExifAttribute createSRational(Rational[] values, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[(ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[10] * values.length)]);
            buffer.order(byteOrder);
            for (Rational value : values) {
                buffer.putInt((int) value.numerator);
                buffer.putInt((int) value.denominator);
            }
            return new ExifAttribute(10, values.length, buffer.array());
        }

        public static ExifAttribute createSRational(Rational value, ByteOrder byteOrder) {
            return createSRational(new Rational[]{value}, byteOrder);
        }

        public static ExifAttribute createDouble(double[] values, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[(ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[12] * values.length)]);
            buffer.order(byteOrder);
            for (double value : values) {
                buffer.putDouble(value);
            }
            return new ExifAttribute(12, values.length, buffer.array());
        }

        public static ExifAttribute createDouble(double value, ByteOrder byteOrder) {
            return createDouble(new double[]{value}, byteOrder);
        }

        public String toString() {
            return "(" + ExifInterface.IFD_FORMAT_NAMES[this.format] + ", data length:" + this.bytes.length + ")";
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Object getValue(ByteOrder byteOrder) {
            IOException e;
            byte b;
            try {
                ByteOrderedDataInputStream inputStream = new ByteOrderedDataInputStream(this.bytes);
                try {
                    inputStream.setByteOrder(byteOrder);
                    int i = 0;
                    switch (this.format) {
                        case 1:
                        case 6:
                            if (this.bytes.length != 1 || this.bytes[0] < 0 || this.bytes[0] > 1) {
                                return new String(this.bytes, ExifInterface.ASCII);
                            }
                            return new String(new char[]{(char) (this.bytes[0] + 48)});
                        case 2:
                        case 7:
                            int index = 0;
                            if (this.numberOfComponents >= ExifInterface.EXIF_ASCII_PREFIX.length) {
                                boolean same = true;
                                while (true) {
                                    if (i < ExifInterface.EXIF_ASCII_PREFIX.length) {
                                        if (this.bytes[i] != ExifInterface.EXIF_ASCII_PREFIX[i]) {
                                            same = false;
                                        } else {
                                            i++;
                                        }
                                    }
                                }
                                if (same) {
                                    index = ExifInterface.EXIF_ASCII_PREFIX.length;
                                }
                            }
                            StringBuilder stringBuilder = new StringBuilder();
                            while (index < this.numberOfComponents && (b = this.bytes[index]) != 0) {
                                if (b >= 32) {
                                    stringBuilder.append((char) b);
                                } else {
                                    stringBuilder.append('?');
                                }
                                index++;
                            }
                            return stringBuilder.toString();
                        case 3:
                            int[] values = new int[this.numberOfComponents];
                            while (i < this.numberOfComponents) {
                                values[i] = inputStream.readUnsignedShort();
                                i++;
                            }
                            return values;
                        case 4:
                            try {
                                long[] values2 = new long[this.numberOfComponents];
                                while (i < this.numberOfComponents) {
                                    values2[i] = inputStream.readUnsignedInt();
                                    i++;
                                }
                                return values2;
                            } catch (OutOfMemoryError e2) {
                                Log.e(ExifInterface.TAG, "no enough memory:" + this.format + ", " + this.numberOfComponents);
                                return null;
                            }
                        case 5:
                            Rational[] values3 = new Rational[this.numberOfComponents];
                            while (i < this.numberOfComponents) {
                                values3[i] = new Rational(inputStream.readUnsignedInt(), inputStream.readUnsignedInt());
                                i++;
                            }
                            return values3;
                        case 8:
                            int[] values4 = new int[this.numberOfComponents];
                            while (i < this.numberOfComponents) {
                                values4[i] = inputStream.readShort();
                                i++;
                            }
                            return values4;
                        case 9:
                            int[] values5 = new int[this.numberOfComponents];
                            while (i < this.numberOfComponents) {
                                values5[i] = inputStream.readInt();
                                i++;
                            }
                            return values5;
                        case 10:
                            Rational[] values6 = new Rational[this.numberOfComponents];
                            while (i < this.numberOfComponents) {
                                values6[i] = new Rational((long) inputStream.readInt(), (long) inputStream.readInt());
                                i++;
                            }
                            return values6;
                        case 11:
                            double[] values7 = new double[this.numberOfComponents];
                            while (i < this.numberOfComponents) {
                                values7[i] = (double) inputStream.readFloat();
                                i++;
                            }
                            return values7;
                        case 12:
                            double[] values8 = new double[this.numberOfComponents];
                            while (i < this.numberOfComponents) {
                                values8[i] = inputStream.readDouble();
                                i++;
                            }
                            return values8;
                        default:
                            return null;
                    }
                } catch (IOException e3) {
                    e = e3;
                    Log.w(ExifInterface.TAG, "IOException occurred during reading a value", e);
                    return null;
                }
            } catch (IOException e4) {
                e = e4;
                Log.w(ExifInterface.TAG, "IOException occurred during reading a value", e);
                return null;
            }
        }

        public double getDoubleValue(ByteOrder byteOrder) {
            Object value = getValue(byteOrder);
            if (value == null) {
                throw new NumberFormatException("NULL can't be converted to a double value");
            } else if (value instanceof String) {
                return Double.parseDouble((String) value);
            } else {
                if (value instanceof long[]) {
                    long[] array = (long[]) value;
                    if (array.length == 1) {
                        return (double) array[0];
                    }
                    throw new NumberFormatException("There are more than one component");
                } else if (value instanceof int[]) {
                    int[] array2 = (int[]) value;
                    if (array2.length == 1) {
                        return (double) array2[0];
                    }
                    throw new NumberFormatException("There are more than one component");
                } else if (value instanceof double[]) {
                    double[] array3 = (double[]) value;
                    if (array3.length == 1) {
                        return array3[0];
                    }
                    throw new NumberFormatException("There are more than one component");
                } else if (value instanceof Rational[]) {
                    Rational[] array4 = (Rational[]) value;
                    if (array4.length == 1) {
                        return array4[0].calculate();
                    }
                    throw new NumberFormatException("There are more than one component");
                } else {
                    throw new NumberFormatException("Couldn't find a double value");
                }
            }
        }

        public int getIntValue(ByteOrder byteOrder) {
            Object value = getValue(byteOrder);
            if (value == null) {
                throw new NumberFormatException("NULL can't be converted to a integer value");
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            } else {
                if (value instanceof long[]) {
                    long[] array = (long[]) value;
                    if (array.length == 1) {
                        return (int) array[0];
                    }
                    throw new NumberFormatException("There are more than one component");
                } else if (value instanceof int[]) {
                    int[] array2 = (int[]) value;
                    if (array2.length == 1) {
                        return array2[0];
                    }
                    throw new NumberFormatException("There are more than one component");
                } else {
                    throw new NumberFormatException("Couldn't find a integer value");
                }
            }
        }

        public String getStringValue(ByteOrder byteOrder) {
            Object value = getValue(byteOrder);
            if (value == null) {
                return null;
            }
            if (value instanceof String) {
                return (String) value;
            }
            StringBuilder stringBuilder = new StringBuilder();
            if (value instanceof long[]) {
                long[] array = (long[]) value;
                for (int i = 0; i < array.length; i++) {
                    stringBuilder.append(array[i]);
                    if (i + 1 != array.length) {
                        stringBuilder.append(SmsManager.REGEX_PREFIX_DELIMITER);
                    }
                }
                return stringBuilder.toString();
            } else if (value instanceof int[]) {
                int[] array2 = (int[]) value;
                for (int i2 = 0; i2 < array2.length; i2++) {
                    stringBuilder.append(array2[i2]);
                    if (i2 + 1 != array2.length) {
                        stringBuilder.append(SmsManager.REGEX_PREFIX_DELIMITER);
                    }
                }
                return stringBuilder.toString();
            } else if (value instanceof double[]) {
                double[] array3 = (double[]) value;
                for (int i3 = 0; i3 < array3.length; i3++) {
                    stringBuilder.append(array3[i3]);
                    if (i3 + 1 != array3.length) {
                        stringBuilder.append(SmsManager.REGEX_PREFIX_DELIMITER);
                    }
                }
                return stringBuilder.toString();
            } else if (!(value instanceof Rational[])) {
                return null;
            } else {
                Rational[] array4 = (Rational[]) value;
                for (int i4 = 0; i4 < array4.length; i4++) {
                    stringBuilder.append(array4[i4].numerator);
                    stringBuilder.append('/');
                    stringBuilder.append(array4[i4].denominator);
                    if (i4 + 1 != array4.length) {
                        stringBuilder.append(SmsManager.REGEX_PREFIX_DELIMITER);
                    }
                }
                return stringBuilder.toString();
            }
        }

        public int size() {
            return ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[this.format] * this.numberOfComponents;
        }
    }

    /* access modifiers changed from: private */
    public static class ExifTag {
        public final String name;
        public final int number;
        public final int primaryFormat;
        public final int secondaryFormat;

        private ExifTag(String name2, int number2, int format) {
            this.name = name2;
            this.number = number2;
            this.primaryFormat = format;
            this.secondaryFormat = -1;
        }

        private ExifTag(String name2, int number2, int primaryFormat2, int secondaryFormat2) {
            this.name = name2;
            this.number = number2;
            this.primaryFormat = primaryFormat2;
            this.secondaryFormat = secondaryFormat2;
        }
    }

    public ExifInterface(File file) throws IOException {
        ExifTag[][] exifTagArr = EXIF_TAGS;
        this.mAttributes = new HashMap[exifTagArr.length];
        this.mHandledIfdOffsets = new HashSet(exifTagArr.length);
        if (file != null) {
            initForFilename(file.getAbsolutePath());
            return;
        }
        throw new NullPointerException("file cannot be null");
    }

    public ExifInterface(String filename) throws IOException {
        ExifTag[][] exifTagArr = EXIF_TAGS;
        this.mAttributes = new HashMap[exifTagArr.length];
        this.mHandledIfdOffsets = new HashSet(exifTagArr.length);
        if (filename != null) {
            initForFilename(filename);
            return;
        }
        throw new NullPointerException("filename cannot be null");
    }

    public ExifInterface(FileDescriptor fileDescriptor) throws IOException {
        ExifTag[][] exifTagArr = EXIF_TAGS;
        this.mAttributes = new HashMap[exifTagArr.length];
        this.mHandledIfdOffsets = new HashSet(exifTagArr.length);
        if (fileDescriptor != null) {
            this.mAssetInputStream = null;
            this.mFilename = null;
            boolean isFdOwner = false;
            if (isSeekableFD(fileDescriptor)) {
                this.mSeekableFileDescriptor = fileDescriptor;
                try {
                    fileDescriptor = Os.dup(fileDescriptor);
                    isFdOwner = true;
                } catch (ErrnoException e) {
                    throw e.rethrowAsIOException();
                }
            } else {
                this.mSeekableFileDescriptor = null;
            }
            this.mIsInputStream = false;
            FileInputStream in = null;
            try {
                in = new FileInputStream(fileDescriptor, isFdOwner);
                loadAttributes(in);
            } finally {
                IoUtils.closeQuietly(in);
            }
        } else {
            throw new NullPointerException("fileDescriptor cannot be null");
        }
    }

    public ExifInterface(InputStream inputStream) throws IOException {
        ExifTag[][] exifTagArr = EXIF_TAGS;
        this.mAttributes = new HashMap[exifTagArr.length];
        this.mHandledIfdOffsets = new HashSet(exifTagArr.length);
        if (inputStream != null) {
            this.mFilename = null;
            if (inputStream instanceof AssetManager.AssetInputStream) {
                this.mAssetInputStream = (AssetManager.AssetInputStream) inputStream;
                this.mSeekableFileDescriptor = null;
            } else if (!(inputStream instanceof FileInputStream) || !isSeekableFD(((FileInputStream) inputStream).getFD())) {
                this.mAssetInputStream = null;
                this.mSeekableFileDescriptor = null;
            } else {
                this.mAssetInputStream = null;
                this.mSeekableFileDescriptor = ((FileInputStream) inputStream).getFD();
            }
            this.mIsInputStream = true;
            loadAttributes(inputStream);
            return;
        }
        throw new NullPointerException("inputStream cannot be null");
    }

    private ExifAttribute getExifAttribute(String tag) {
        if (tag != null) {
            for (int i = 0; i < EXIF_TAGS.length; i++) {
                Object value = this.mAttributes[i].get(tag);
                if (value != null) {
                    return (ExifAttribute) value;
                }
            }
            return null;
        }
        throw new NullPointerException("tag shouldn't be null");
    }

    public String getAttribute(String tag) {
        if (tag != null) {
            ExifAttribute attribute = getExifAttribute(tag);
            if (attribute == null) {
                return null;
            }
            if (tag.equals(HW_MAKER_NOTE)) {
                Log.d(TAG, "get hw makernote");
                return new String(attribute.bytes, Charset.forName(HW_MNOTE_ISO));
            } else if (!sTagSetForCompatibility.contains(tag)) {
                return attribute.getStringValue(this.mExifByteOrder);
            } else {
                if (!tag.equals(TAG_GPS_TIMESTAMP)) {
                    try {
                        return Double.toString(attribute.getDoubleValue(this.mExifByteOrder));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (attribute.format != 5 && attribute.format != 10) {
                    return null;
                } else {
                    Rational[] array = (Rational[]) attribute.getValue(this.mExifByteOrder);
                    if (array.length != 3) {
                        return null;
                    }
                    return String.format("%02d:%02d:%02d", Integer.valueOf((int) (((float) array[0].numerator) / ((float) array[0].denominator))), Integer.valueOf((int) (((float) array[1].numerator) / ((float) array[1].denominator))), Integer.valueOf((int) (((float) array[2].numerator) / ((float) array[2].denominator))));
                }
            }
        } else {
            throw new NullPointerException("tag shouldn't be null");
        }
    }

    public int getAttributeInt(String tag, int defaultValue) {
        if (tag != null) {
            ExifAttribute exifAttribute = getExifAttribute(tag);
            if (exifAttribute == null) {
                return defaultValue;
            }
            try {
                return exifAttribute.getIntValue(this.mExifByteOrder);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            throw new NullPointerException("tag shouldn't be null");
        }
    }

    public double getAttributeDouble(String tag, double defaultValue) {
        if (tag != null) {
            ExifAttribute exifAttribute = getExifAttribute(tag);
            if (exifAttribute == null) {
                return defaultValue;
            }
            try {
                return exifAttribute.getDoubleValue(this.mExifByteOrder);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            throw new NullPointerException("tag shouldn't be null");
        }
    }

    public void setAttribute(String tagName, String value) {
        boolean isHwMnote;
        String tag;
        int i;
        int dataFormat;
        String value2 = value;
        if (tagName == null) {
            Log.e(TAG, "setAttribute tagName is null");
            return;
        }
        if (tagName.equals(HW_MAKER_NOTE)) {
            isHwMnote = true;
            tag = TAG_MAKER_NOTE;
        } else {
            isHwMnote = false;
            tag = tagName;
        }
        int i2 = 2;
        int i3 = 1;
        if (value2 != null && sTagSetForCompatibility.contains(tag)) {
            if (tag.equals(TAG_GPS_TIMESTAMP)) {
                Matcher m = sGpsTimestampPattern.matcher(value2);
                if (!m.find()) {
                    Log.w(TAG, "Invalid value for " + tag + " : " + value2);
                    return;
                }
                value2 = Integer.parseInt(m.group(1)) + "/1," + Integer.parseInt(m.group(2)) + "/1," + Integer.parseInt(m.group(3)) + "/1";
            } else {
                try {
                    double doubleValue = Double.parseDouble(value);
                    value2 = ((long) (10000.0d * doubleValue)) + "/10000";
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid value for " + tag + " : " + value2);
                    return;
                }
            }
        }
        int i4 = 0;
        while (i4 < EXIF_TAGS.length) {
            if (i4 != 4 || this.mHasThumbnail) {
                Object obj = sExifTagMapsForWriting[i4].get(tag);
                if (obj == null) {
                    i = i3;
                } else if (value2 == null) {
                    this.mAttributes[i4].remove(tag);
                    i = i3;
                } else {
                    ExifTag exifTag = (ExifTag) obj;
                    Pair<Integer, Integer> guess = guessDataFormat(value2);
                    if (exifTag.primaryFormat == guess.first.intValue() || exifTag.primaryFormat == guess.second.intValue()) {
                        dataFormat = exifTag.primaryFormat;
                    } else if (exifTag.secondaryFormat != -1 && (exifTag.secondaryFormat == guess.first.intValue() || exifTag.secondaryFormat == guess.second.intValue())) {
                        dataFormat = exifTag.secondaryFormat;
                    } else if (exifTag.primaryFormat == i3 || exifTag.primaryFormat == 7 || exifTag.primaryFormat == i2) {
                        dataFormat = exifTag.primaryFormat;
                    } else if (DEBUG) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Given tag (");
                        sb.append(tag);
                        sb.append(") value didn't match with one of expected formats: ");
                        sb.append(IFD_FORMAT_NAMES[exifTag.primaryFormat]);
                        String str = "";
                        sb.append(exifTag.secondaryFormat == -1 ? str : ", " + IFD_FORMAT_NAMES[exifTag.secondaryFormat]);
                        sb.append(" (guess: ");
                        sb.append(IFD_FORMAT_NAMES[guess.first.intValue()]);
                        if (guess.second.intValue() != -1) {
                            str = ", " + IFD_FORMAT_NAMES[guess.second.intValue()];
                        }
                        sb.append(str);
                        sb.append(")");
                        Log.d(TAG, sb.toString());
                        i = 1;
                    } else {
                        i = 1;
                    }
                    switch (dataFormat) {
                        case 1:
                            i = 1;
                            this.mAttributes[i4].put(tag, ExifAttribute.createByte(value2));
                            continue;
                        case 2:
                        case 7:
                            i = 1;
                            if (!isHwMnote) {
                                this.mAttributes[i4].put(tag, ExifAttribute.createString(value2));
                                break;
                            } else {
                                byte[] bytes = value2.getBytes(Charset.forName(HW_MNOTE_ISO));
                                this.mAttributes[i4].put(HW_MAKER_NOTE, new ExifAttribute(7, bytes.length, bytes));
                                continue;
                            }
                        case 3:
                            i = 1;
                            String[] values = value2.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            int[] intArray = new int[values.length];
                            for (int j = 0; j < values.length; j++) {
                                intArray[j] = Integer.parseInt(values[j]);
                            }
                            this.mAttributes[i4].put(tag, ExifAttribute.createUShort(intArray, this.mExifByteOrder));
                            continue;
                        case 4:
                            i = 1;
                            String[] values2 = value2.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            long[] longArray = new long[values2.length];
                            for (int j2 = 0; j2 < values2.length; j2++) {
                                longArray[j2] = Long.parseLong(values2[j2]);
                            }
                            this.mAttributes[i4].put(tag, ExifAttribute.createULong(longArray, this.mExifByteOrder));
                            continue;
                        case 5:
                            String[] values3 = value2.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            Rational[] rationalArray = new Rational[values3.length];
                            for (int j3 = 0; j3 < values3.length; j3++) {
                                String[] numbers = values3[j3].split("/");
                                rationalArray[j3] = new Rational((long) Double.parseDouble(numbers[0]), (long) Double.parseDouble(numbers[1]));
                            }
                            i = 1;
                            this.mAttributes[i4].put(tag, ExifAttribute.createURational(rationalArray, this.mExifByteOrder));
                            continue;
                        case 6:
                        case 8:
                        case 11:
                        default:
                            i = 1;
                            if (!DEBUG) {
                                break;
                            } else {
                                Log.d(TAG, "Data format isn't one of expected formats: " + dataFormat);
                                continue;
                            }
                        case 9:
                            String[] values4 = value2.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            int[] intArray2 = new int[values4.length];
                            for (int j4 = 0; j4 < values4.length; j4++) {
                                intArray2[j4] = Integer.parseInt(values4[j4]);
                            }
                            this.mAttributes[i4].put(tag, ExifAttribute.createSLong(intArray2, this.mExifByteOrder));
                            i = 1;
                            continue;
                        case 10:
                            String[] values5 = value2.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            Rational[] rationalArray2 = new Rational[values5.length];
                            int j5 = 0;
                            while (j5 < values5.length) {
                                String[] numbers2 = values5[j5].split("/");
                                rationalArray2[j5] = new Rational((long) Double.parseDouble(numbers2[0]), (long) Double.parseDouble(numbers2[1]));
                                j5++;
                                obj = obj;
                                exifTag = exifTag;
                                guess = guess;
                                dataFormat = dataFormat;
                            }
                            this.mAttributes[i4].put(tag, ExifAttribute.createSRational(rationalArray2, this.mExifByteOrder));
                            i = 1;
                            continue;
                        case 12:
                            String[] values6 = value2.split(SmsManager.REGEX_PREFIX_DELIMITER);
                            double[] doubleArray = new double[values6.length];
                            for (int j6 = 0; j6 < values6.length; j6++) {
                                doubleArray[j6] = Double.parseDouble(values6[j6]);
                            }
                            this.mAttributes[i4].put(tag, ExifAttribute.createDouble(doubleArray, this.mExifByteOrder));
                            i = 1;
                            continue;
                    }
                }
            } else {
                i = i3;
            }
            i4++;
            i3 = i;
            i2 = 2;
        }
    }

    private boolean updateAttribute(String tag, ExifAttribute value) {
        boolean updated = false;
        for (int i = 0; i < EXIF_TAGS.length; i++) {
            if (this.mAttributes[i].containsKey(tag)) {
                this.mAttributes[i].put(tag, value);
                updated = true;
            }
        }
        return updated;
    }

    private void removeAttribute(String tag) {
        for (int i = 0; i < EXIF_TAGS.length; i++) {
            this.mAttributes[i].remove(tag);
        }
    }

    private void loadAttributes(InputStream in) throws IOException {
        if (in != null) {
            for (int i = 0; i < EXIF_TAGS.length; i++) {
                try {
                    this.mAttributes[i] = new HashMap();
                } catch (IOException e) {
                    this.mIsSupportedFile = false;
                    Log.w(TAG, "Invalid image: ExifInterface got an unsupported image format file(ExifInterface supports JPEG and some RAW image formats only) or a corrupted JPEG file to ExifInterface.");
                    addDefaultValuesForCompatibility();
                    if (!DEBUG) {
                        return;
                    }
                } catch (OutOfMemoryError e2) {
                    Log.w(TAG, "OutOfMemoryError in loadAttributes");
                    addDefaultValuesForCompatibility();
                    if (!DEBUG) {
                        return;
                    }
                } catch (Throwable th) {
                    addDefaultValuesForCompatibility();
                    if (DEBUG) {
                        printAttributes();
                    }
                    throw th;
                }
            }
            InputStream in2 = new BufferedInputStream(in, 5000);
            this.mMimeType = getMimeType((BufferedInputStream) in2);
            ByteOrderedDataInputStream inputStream = new ByteOrderedDataInputStream(in2);
            switch (this.mMimeType) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 5:
                case 6:
                case 8:
                case 11:
                    getRawAttributes(inputStream);
                    break;
                case 4:
                    getJpegAttributes(inputStream, 0, 0);
                    break;
                case 7:
                    getOrfAttributes(inputStream);
                    break;
                case 9:
                    getRafAttributes(inputStream);
                    break;
                case 10:
                    getRw2Attributes(inputStream);
                    break;
                case 12:
                    getHeifAttributes(inputStream);
                    break;
            }
            setThumbnailData(inputStream);
            this.mIsSupportedFile = true;
            addDefaultValuesForCompatibility();
            if (!DEBUG) {
                return;
            }
            printAttributes();
            return;
        }
        throw new NullPointerException("inputstream shouldn't be null");
    }

    private static boolean isSeekableFD(FileDescriptor fd) throws IOException {
        try {
            Os.lseek(fd, 0, OsConstants.SEEK_CUR);
            return true;
        } catch (ErrnoException e) {
            return false;
        }
    }

    private void printAttributes() {
        for (int i = 0; i < this.mAttributes.length; i++) {
            Log.d(TAG, "The size of tag group[" + i + "]: " + this.mAttributes[i].size());
            for (Map.Entry entry : this.mAttributes[i].entrySet()) {
                ExifAttribute tagValue = (ExifAttribute) entry.getValue();
                Log.d(TAG, "tagName: " + entry.getKey() + ", tagType: " + tagValue.toString() + ", tagValue: '" + tagValue.getStringValue(this.mExifByteOrder) + "'");
            }
        }
    }

    public void saveAttributes() throws IOException {
        if (!this.mIsSupportedFile || this.mMimeType != 4) {
            throw new IOException("ExifInterface only supports saving attributes on JPEG formats.");
        } else if (this.mIsInputStream || (this.mSeekableFileDescriptor == null && this.mFilename == null)) {
            throw new IOException("ExifInterface does not support saving attributes for the current input.");
        } else {
            this.mModified = true;
            this.mThumbnailBytes = getThumbnail();
            FileInputStream in = null;
            FileOutputStream out = null;
            File tempFile = null;
            try {
                if (this.mFilename != null) {
                    tempFile = new File(this.mFilename + ".tmp");
                    if (!new File(this.mFilename).renameTo(tempFile)) {
                        throw new IOException("Could'nt rename to " + tempFile.getAbsolutePath());
                    }
                } else if (this.mSeekableFileDescriptor != null) {
                    tempFile = File.createTempFile("temp", "jpg");
                    Os.lseek(this.mSeekableFileDescriptor, 0, OsConstants.SEEK_SET);
                    in = new FileInputStream(this.mSeekableFileDescriptor);
                    out = new FileOutputStream(tempFile);
                    Streams.copy(in, out);
                }
                IoUtils.closeQuietly(in);
                IoUtils.closeQuietly(out);
                FileOutputStream out2 = null;
                try {
                    FileInputStream in2 = new FileInputStream(tempFile);
                    if (this.mFilename != null) {
                        out2 = new FileOutputStream(this.mFilename);
                    } else if (this.mSeekableFileDescriptor != null) {
                        Os.lseek(this.mSeekableFileDescriptor, 0, OsConstants.SEEK_SET);
                        out2 = new FileOutputStream(this.mSeekableFileDescriptor);
                    }
                    saveJpegAttributes(in2, out2);
                    IoUtils.closeQuietly(in2);
                    IoUtils.closeQuietly(out2);
                    tempFile.delete();
                    this.mThumbnailBytes = null;
                } catch (ErrnoException e) {
                    throw e.rethrowAsIOException();
                } catch (Throwable th) {
                    IoUtils.closeQuietly((AutoCloseable) null);
                    IoUtils.closeQuietly((AutoCloseable) null);
                    tempFile.delete();
                    throw th;
                }
            } catch (ErrnoException e2) {
                throw e2.rethrowAsIOException();
            } catch (Throwable th2) {
                IoUtils.closeQuietly((AutoCloseable) null);
                IoUtils.closeQuietly((AutoCloseable) null);
                throw th2;
            }
        }
    }

    public boolean hasThumbnail() {
        return this.mHasThumbnail;
    }

    public boolean hasAttribute(String tag) {
        return getExifAttribute(tag) != null;
    }

    public byte[] getThumbnail() {
        int i = this.mThumbnailCompression;
        if (i == 6 || i == 7) {
            return getThumbnailBytes();
        }
        return null;
    }

    public byte[] getThumbnailBytes() {
        if (!this.mHasThumbnail) {
            return null;
        }
        byte[] bArr = this.mThumbnailBytes;
        if (bArr != null) {
            return bArr;
        }
        InputStream in = null;
        if (this.mAssetInputStream != null) {
            in = this.mAssetInputStream;
            if (in.markSupported()) {
                in.reset();
            } else {
                Log.d(TAG, "Cannot read thumbnail from inputstream without mark/reset support");
                IoUtils.closeQuietly(in);
                return null;
            }
        } else {
            try {
                if (this.mFilename != null) {
                    in = new FileInputStream(this.mFilename);
                } else if (this.mSeekableFileDescriptor != null) {
                    FileDescriptor fileDescriptor = Os.dup(this.mSeekableFileDescriptor);
                    Os.lseek(fileDescriptor, 0, OsConstants.SEEK_SET);
                    in = new FileInputStream(fileDescriptor, true);
                }
            } catch (ErrnoException | IOException e) {
                Log.d(TAG, "Encountered exception while getting thumbnail", e);
                IoUtils.closeQuietly((AutoCloseable) null);
                return null;
            } catch (Throwable th) {
                IoUtils.closeQuietly((AutoCloseable) null);
                throw th;
            }
        }
        if (in != null) {
            if (in.skip((long) this.mThumbnailOffset) == ((long) this.mThumbnailOffset)) {
                byte[] buffer = new byte[this.mThumbnailLength];
                if (in.read(buffer) == this.mThumbnailLength) {
                    this.mThumbnailBytes = buffer;
                    IoUtils.closeQuietly(in);
                    return buffer;
                }
                throw new IOException("Corrupted image");
            }
            throw new IOException("Corrupted image");
        }
        throw new FileNotFoundException();
    }

    public Bitmap getThumbnailBitmap() {
        if (!this.mHasThumbnail) {
            return null;
        }
        if (this.mThumbnailBytes == null) {
            this.mThumbnailBytes = getThumbnailBytes();
        }
        int i = this.mThumbnailCompression;
        if (i == 6 || i == 7) {
            return BitmapFactory.decodeByteArray(this.mThumbnailBytes, 0, this.mThumbnailLength);
        }
        if (i == 1) {
            int[] rgbValues = new int[(this.mThumbnailBytes.length / 3)];
            for (int i2 = 0; i2 < rgbValues.length; i2++) {
                byte[] bArr = this.mThumbnailBytes;
                rgbValues[i2] = (bArr[i2 * 3] << WifiScanner.PnoSettings.PnoNetwork.FLAG_SAME_NETWORK) + 0 + (bArr[(i2 * 3) + 1] << 8) + bArr[(i2 * 3) + 2];
            }
            ExifAttribute imageLengthAttribute = (ExifAttribute) this.mAttributes[4].get(TAG_IMAGE_LENGTH);
            ExifAttribute imageWidthAttribute = (ExifAttribute) this.mAttributes[4].get(TAG_IMAGE_WIDTH);
            if (!(imageLengthAttribute == null || imageWidthAttribute == null)) {
                return Bitmap.createBitmap(rgbValues, imageWidthAttribute.getIntValue(this.mExifByteOrder), imageLengthAttribute.getIntValue(this.mExifByteOrder), Bitmap.Config.ARGB_8888);
            }
        }
        return null;
    }

    public boolean isThumbnailCompressed() {
        if (!this.mHasThumbnail) {
            return false;
        }
        int i = this.mThumbnailCompression;
        if (i == 6 || i == 7) {
            return true;
        }
        return false;
    }

    public long[] getThumbnailRange() {
        if (this.mModified) {
            throw new IllegalStateException("The underlying file has been modified since being parsed");
        } else if (!this.mHasThumbnail) {
            return null;
        } else {
            if (!this.mHasThumbnailStrips || this.mAreThumbnailStripsConsecutive) {
                return new long[]{(long) this.mThumbnailOffset, (long) this.mThumbnailLength};
            }
            return null;
        }
    }

    public long[] getAttributeRange(String tag) {
        if (tag == null) {
            throw new NullPointerException("tag shouldn't be null");
        } else if (!this.mModified) {
            ExifAttribute attribute = getExifAttribute(tag);
            if (attribute != null) {
                return new long[]{attribute.bytesOffset, (long) attribute.bytes.length};
            }
            return null;
        } else {
            throw new IllegalStateException("The underlying file has been modified since being parsed");
        }
    }

    public byte[] getAttributeBytes(String tag) {
        if (tag != null) {
            ExifAttribute attribute = getExifAttribute(tag);
            if (attribute != null) {
                return attribute.bytes;
            }
            return null;
        }
        throw new NullPointerException("tag shouldn't be null");
    }

    public boolean getLatLong(float[] output) {
        String latValue = getAttribute(TAG_GPS_LATITUDE);
        String latRef = getAttribute(TAG_GPS_LATITUDE_REF);
        String lngValue = getAttribute(TAG_GPS_LONGITUDE);
        String lngRef = getAttribute(TAG_GPS_LONGITUDE_REF);
        if (!(latValue == null || latRef == null || lngValue == null || lngRef == null)) {
            try {
                output[0] = convertRationalLatLonToFloat(latValue, latRef);
                output[1] = convertRationalLatLonToFloat(lngValue, lngRef);
                return true;
            } catch (IllegalArgumentException e) {
            }
        }
        return false;
    }

    public double getAltitude(double defaultValue) {
        double altitude = getAttributeDouble(TAG_GPS_ALTITUDE, -1.0d);
        int i = -1;
        int ref = getAttributeInt(TAG_GPS_ALTITUDE_REF, -1);
        if (altitude < 0.0d || ref < 0) {
            return defaultValue;
        }
        if (ref != 1) {
            i = 1;
        }
        return ((double) i) * altitude;
    }

    @UnsupportedAppUsage
    public long getDateTime() {
        return parseDateTime(getAttribute(TAG_DATETIME), getAttribute(TAG_SUBSEC_TIME), getAttribute(TAG_OFFSET_TIME));
    }

    public long getDateTimeDigitized() {
        return parseDateTime(getAttribute(TAG_DATETIME_DIGITIZED), getAttribute("SubSecTimeDigitized"), getAttribute(TAG_OFFSET_TIME_DIGITIZED));
    }

    @UnsupportedAppUsage
    public long getDateTimeOriginal() {
        return parseDateTime(getAttribute(TAG_DATETIME_ORIGINAL), getAttribute("SubSecTimeOriginal"), getAttribute(TAG_OFFSET_TIME_ORIGINAL));
    }

    private static long parseDateTime(String dateTimeString, String subSecs, String offsetString) {
        Throwable th;
        Date datetime;
        if (dateTimeString == null || !sNonZeroTimePattern.matcher(dateTimeString).matches()) {
            return -1;
        }
        ParsePosition pos = new ParsePosition(0);
        try {
            synchronized (LOCK) {
                try {
                    SimpleDateFormat[] simpleDateFormatArr = sFormatters;
                    int length = simpleDateFormatArr.length;
                    Date datetime2 = null;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            datetime = datetime2;
                            break;
                        }
                        try {
                            SimpleDateFormat formatter = simpleDateFormatArr[i];
                            formatter.setTimeZone(TimeZone.getDefault());
                            datetime2 = formatter.parse(dateTimeString, pos);
                            if (datetime2 != null) {
                                datetime = datetime2;
                                break;
                            }
                            i++;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
            if (offsetString != null) {
                datetime = sFormatterTz.parse(dateTimeString + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + offsetString, new ParsePosition(0));
            }
            if (datetime == null) {
                return -1;
            }
            long msecs = datetime.getTime();
            if (subSecs == null) {
                return msecs;
            }
            try {
                long sub = Long.parseLong(subSecs);
                while (sub > 1000) {
                    sub /= 10;
                }
                return msecs + sub;
            } catch (NumberFormatException e) {
                return msecs;
            }
        } catch (IllegalArgumentException e2) {
            return -1;
        }
    }

    @UnsupportedAppUsage
    public long getGpsDateTime() {
        String date = getAttribute(TAG_GPS_DATESTAMP);
        String time = getAttribute(TAG_GPS_TIMESTAMP);
        if (date == null || time == null || (!sNonZeroTimePattern.matcher(date).matches() && !sNonZeroTimePattern.matcher(time).matches())) {
            return -1;
        }
        String dateTimeString = date + ' ' + time;
        ParsePosition pos = new ParsePosition(0);
        try {
            synchronized (LOCK) {
                for (SimpleDateFormat formatter : sFormattersUTC) {
                    Date datetime = formatter.parse(dateTimeString, pos);
                    if (datetime != null) {
                        return datetime.getTime();
                    }
                }
                return -1;
            }
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static float convertRationalLatLonToFloat(String rationalString, String ref) {
        try {
            String[] parts = rationalString.split(SmsManager.REGEX_PREFIX_DELIMITER);
            String[] pair = parts[0].split("/");
            double degrees = Double.parseDouble(pair[0].trim()) / Double.parseDouble(pair[1].trim());
            String[] pair2 = parts[1].split("/");
            double minutes = Double.parseDouble(pair2[0].trim()) / Double.parseDouble(pair2[1].trim());
            String[] pair3 = parts[2].split("/");
            double result = (minutes / 60.0d) + degrees + ((Double.parseDouble(pair3[0].trim()) / Double.parseDouble(pair3[1].trim())) / 3600.0d);
            if (ref.equals("S") || ref.equals("W")) {
                return (float) (-result);
            }
            return (float) result;
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new IllegalArgumentException();
        }
    }

    private void initForFilename(String filename) throws IOException {
        FileInputStream in = null;
        this.mAssetInputStream = null;
        this.mFilename = filename;
        this.mIsInputStream = false;
        try {
            in = new FileInputStream(filename);
            if (isSeekableFD(in.getFD())) {
                this.mSeekableFileDescriptor = in.getFD();
            } else {
                this.mSeekableFileDescriptor = null;
            }
            loadAttributes(in);
        } finally {
            IoUtils.closeQuietly(in);
        }
    }

    private int getMimeType(BufferedInputStream in) throws IOException {
        in.mark(5000);
        byte[] signatureCheckBytes = new byte[5000];
        in.read(signatureCheckBytes);
        in.reset();
        if (isJpegFormat(signatureCheckBytes)) {
            return 4;
        }
        if (isRafFormat(signatureCheckBytes)) {
            return 9;
        }
        if (isHeifFormat(signatureCheckBytes)) {
            return 12;
        }
        if (isOrfFormat(signatureCheckBytes)) {
            return 7;
        }
        if (isRw2Format(signatureCheckBytes)) {
            return 10;
        }
        return 0;
    }

    private static boolean isJpegFormat(byte[] signatureCheckBytes) throws IOException {
        int i = 0;
        while (true) {
            byte[] bArr = JPEG_SIGNATURE;
            if (i >= bArr.length) {
                return true;
            }
            if (signatureCheckBytes[i] != bArr[i]) {
                return false;
            }
            i++;
        }
    }

    private boolean isRafFormat(byte[] signatureCheckBytes) throws IOException {
        byte[] rafSignatureBytes = RAF_SIGNATURE.getBytes();
        for (int i = 0; i < rafSignatureBytes.length; i++) {
            if (signatureCheckBytes[i] != rafSignatureBytes[i]) {
                return false;
            }
        }
        return true;
    }

    /* JADX INFO: Multiple debug info for r0v8 byte[]: [D('signatureInputStream' android.media.ExifInterface$ByteOrderedDataInputStream), D('brand' byte[])] */
    private boolean isHeifFormat(byte[] signatureCheckBytes) throws IOException {
        ByteOrderedDataInputStream signatureInputStream = null;
        try {
            signatureInputStream = new ByteOrderedDataInputStream(signatureCheckBytes);
            signatureInputStream.setByteOrder(ByteOrder.BIG_ENDIAN);
            long chunkSize = (long) signatureInputStream.readInt();
            byte[] chunkType = new byte[4];
            signatureInputStream.read(chunkType);
            if (!Arrays.equals(chunkType, HEIF_TYPE_FTYP)) {
                signatureInputStream.close();
                return false;
            }
            long chunkDataOffset = 8;
            if (chunkSize == 1) {
                chunkSize = signatureInputStream.readLong();
                if (chunkSize < 16) {
                    signatureInputStream.close();
                    return false;
                }
                chunkDataOffset = 8 + 8;
            }
            if (chunkSize > ((long) signatureCheckBytes.length)) {
                chunkSize = (long) signatureCheckBytes.length;
            }
            long chunkDataSize = chunkSize - chunkDataOffset;
            if (chunkDataSize < 8) {
                signatureInputStream.close();
                return false;
            }
            byte[] brand = new byte[4];
            boolean isMif1 = false;
            boolean isHeic = false;
            for (long i = 0; i < chunkDataSize / 4; i++) {
                if (signatureInputStream.read(brand) != brand.length) {
                    signatureInputStream.close();
                    return false;
                }
                if (i != 1) {
                    if (Arrays.equals(brand, HEIF_BRAND_MIF1)) {
                        isMif1 = true;
                    } else if (Arrays.equals(brand, HEIF_BRAND_HEIC)) {
                        isHeic = true;
                    }
                    if (isMif1 && isHeic) {
                        signatureInputStream.close();
                        return true;
                    }
                }
            }
            signatureInputStream.close();
            return false;
        } catch (Exception e) {
            if (DEBUG) {
                Log.d(TAG, "Exception parsing HEIF file type box.", e);
            }
            if (signatureInputStream == null) {
            }
        } catch (Throwable th) {
            if (signatureInputStream != null) {
                signatureInputStream.close();
            }
            throw th;
        }
    }

    private boolean isOrfFormat(byte[] signatureCheckBytes) throws IOException {
        ByteOrderedDataInputStream signatureInputStream = new ByteOrderedDataInputStream(signatureCheckBytes);
        this.mExifByteOrder = readByteOrder(signatureInputStream);
        signatureInputStream.setByteOrder(this.mExifByteOrder);
        short orfSignature = signatureInputStream.readShort();
        if (orfSignature == 20306 || orfSignature == 21330) {
            return true;
        }
        return false;
    }

    private boolean isRw2Format(byte[] signatureCheckBytes) throws IOException {
        ByteOrderedDataInputStream signatureInputStream = new ByteOrderedDataInputStream(signatureCheckBytes);
        this.mExifByteOrder = readByteOrder(signatureInputStream);
        signatureInputStream.setByteOrder(this.mExifByteOrder);
        if (signatureInputStream.readShort() == 85) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x00c5 A[FALL_THROUGH] */
    private void getJpegAttributes(ByteOrderedDataInputStream in, int jpegOffset, int imageType) throws IOException {
        int i = imageType;
        if (DEBUG) {
            Log.d(TAG, "getJpegAttributes starting with: " + in);
        }
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        in.seek((long) jpegOffset);
        byte marker = in.readByte();
        byte b = -1;
        if (marker == -1) {
            int i2 = 1;
            int bytesRead = jpegOffset + 1;
            if (in.readByte() == -40) {
                int bytesRead2 = bytesRead + 1;
                while (true) {
                    byte marker2 = in.readByte();
                    if (marker2 == b) {
                        int bytesRead3 = bytesRead2 + 1;
                        byte marker3 = in.readByte();
                        if (DEBUG) {
                            Log.d(TAG, "Found JPEG segment indicator: " + Integer.toHexString(marker3 & 255));
                        }
                        int bytesRead4 = bytesRead3 + i2;
                        if (marker3 != -39 && marker3 != -38) {
                            int length = in.readUnsignedShort() - 2;
                            int bytesRead5 = bytesRead4 + 2;
                            if (DEBUG) {
                                Log.d(TAG, "JPEG segment: " + Integer.toHexString(marker3 & 255) + " (length: " + (length + 2) + ")");
                            }
                            if (length >= 0) {
                                if (marker3 == -31) {
                                    byte[] bytes = new byte[length];
                                    in.readFully(bytes);
                                    bytesRead5 += length;
                                    length = 0;
                                    if (ArrayUtils.startsWith(bytes, IDENTIFIER_EXIF_APP1)) {
                                        byte[] bArr = IDENTIFIER_EXIF_APP1;
                                        byte[] value = Arrays.copyOfRange(bytes, bArr.length, bytes.length);
                                        this.mExifOffset = (int) ((long) (bArr.length + bytesRead5));
                                        readExifSegment(value, i);
                                    } else if (ArrayUtils.startsWith(bytes, IDENTIFIER_XMP_APP1)) {
                                        byte[] bArr2 = IDENTIFIER_XMP_APP1;
                                        long offset = (long) (bArr2.length + bytesRead5);
                                        byte[] value2 = Arrays.copyOfRange(bytes, bArr2.length, bytes.length);
                                        if (getAttribute(TAG_XMP) == null) {
                                            this.mAttributes[0].put(TAG_XMP, new ExifAttribute(1, value2.length, offset, value2));
                                        }
                                    }
                                } else if (marker3 != -2) {
                                    switch (marker3) {
                                        default:
                                            switch (marker3) {
                                                default:
                                                    switch (marker3) {
                                                        default:
                                                            switch (marker3) {
                                                            }
                                                        case KeymasterDefs.KM_ERROR_CALLER_NONCE_PROHIBITED /* -55 */:
                                                        case KeymasterDefs.KM_ERROR_KEY_RATE_LIMIT_EXCEEDED /* -54 */:
                                                        case KeymasterDefs.KM_ERROR_MISSING_MAC_LENGTH /* -53 */:
                                                            if (in.skipBytes(i2) == i2) {
                                                                this.mAttributes[i].put(TAG_IMAGE_LENGTH, ExifAttribute.createULong((long) in.readUnsignedShort(), this.mExifByteOrder));
                                                                this.mAttributes[i].put(TAG_IMAGE_WIDTH, ExifAttribute.createULong((long) in.readUnsignedShort(), this.mExifByteOrder));
                                                                length -= 5;
                                                                break;
                                                            } else {
                                                                throw new IOException("Invalid SOFx");
                                                            }
                                                    }
                                                case KeymasterDefs.KM_ERROR_UNSUPPORTED_MIN_MAC_LENGTH /* -59 */:
                                                case KeymasterDefs.KM_ERROR_MISSING_MIN_MAC_LENGTH /* -58 */:
                                                case KeymasterDefs.KM_ERROR_INVALID_MAC_LENGTH /* -57 */:
                                                    break;
                                            }
                                        case -64:
                                        case -63:
                                        case -62:
                                        case -61:
                                            break;
                                    }
                                } else {
                                    byte[] bytes2 = new byte[length];
                                    if (in.read(bytes2) == length) {
                                        length = 0;
                                        if (getAttribute(TAG_USER_COMMENT) == null) {
                                            this.mAttributes[i2].put(TAG_USER_COMMENT, ExifAttribute.createString(new String(bytes2, ASCII)));
                                        }
                                    } else {
                                        throw new IOException("Invalid exif");
                                    }
                                }
                                if (length < 0) {
                                    throw new IOException("Invalid length");
                                } else if (in.skipBytes(length) == length) {
                                    bytesRead2 = bytesRead5 + length;
                                    i = imageType;
                                    i2 = 1;
                                    b = -1;
                                } else {
                                    throw new IOException("Invalid JPEG segment");
                                }
                            } else {
                                throw new IOException("Invalid length");
                            }
                        }
                    } else {
                        throw new IOException("Invalid marker:" + Integer.toHexString(marker2 & 255));
                    }
                }
                in.setByteOrder(this.mExifByteOrder);
                return;
            }
            throw new IOException("Invalid marker: " + Integer.toHexString(marker & 255));
        }
        throw new IOException("Invalid marker: " + Integer.toHexString(marker & 255));
    }

    private void getRawAttributes(ByteOrderedDataInputStream in) throws IOException {
        ExifAttribute makerNoteAttribute;
        parseTiffHeaders(in, in.available());
        readImageFileDirectory(in, 0);
        updateImageSizeValues(in, 0);
        updateImageSizeValues(in, 5);
        updateImageSizeValues(in, 4);
        validateImages(in);
        if (this.mMimeType == 8 && (makerNoteAttribute = (ExifAttribute) this.mAttributes[1].get(TAG_MAKER_NOTE)) != null) {
            ByteOrderedDataInputStream makerNoteDataInputStream = new ByteOrderedDataInputStream(makerNoteAttribute.bytes);
            makerNoteDataInputStream.setByteOrder(this.mExifByteOrder);
            makerNoteDataInputStream.seek(6);
            readImageFileDirectory(makerNoteDataInputStream, 9);
            ExifAttribute colorSpaceAttribute = (ExifAttribute) this.mAttributes[9].get(TAG_COLOR_SPACE);
            if (colorSpaceAttribute != null) {
                this.mAttributes[1].put(TAG_COLOR_SPACE, colorSpaceAttribute);
            }
        }
    }

    private void getRafAttributes(ByteOrderedDataInputStream in) throws IOException {
        in.skipBytes(84);
        byte[] jpegOffsetBytes = new byte[4];
        byte[] cfaHeaderOffsetBytes = new byte[4];
        in.read(jpegOffsetBytes);
        in.skipBytes(4);
        in.read(cfaHeaderOffsetBytes);
        int rafJpegOffset = ByteBuffer.wrap(jpegOffsetBytes).getInt();
        int rafCfaHeaderOffset = ByteBuffer.wrap(cfaHeaderOffsetBytes).getInt();
        getJpegAttributes(in, rafJpegOffset, 5);
        in.seek((long) rafCfaHeaderOffset);
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        int numberOfDirectoryEntry = in.readInt();
        if (DEBUG) {
            Log.d(TAG, "numberOfDirectoryEntry: " + numberOfDirectoryEntry);
        }
        for (int i = 0; i < numberOfDirectoryEntry; i++) {
            int tagNumber = in.readUnsignedShort();
            int numberOfBytes = in.readUnsignedShort();
            if (tagNumber == TAG_RAF_IMAGE_SIZE.number) {
                int imageLength = in.readShort();
                int imageWidth = in.readShort();
                ExifAttribute imageLengthAttribute = ExifAttribute.createUShort(imageLength, this.mExifByteOrder);
                ExifAttribute imageWidthAttribute = ExifAttribute.createUShort(imageWidth, this.mExifByteOrder);
                this.mAttributes[0].put(TAG_IMAGE_LENGTH, imageLengthAttribute);
                this.mAttributes[0].put(TAG_IMAGE_WIDTH, imageWidthAttribute);
                if (DEBUG) {
                    Log.d(TAG, "Updated to length: " + imageLength + ", width: " + imageWidth);
                    return;
                }
                return;
            }
            in.skipBytes(numberOfBytes);
        }
    }

    private void getHeifAttributes(final ByteOrderedDataInputStream in) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(new MediaDataSource() {
                /* class android.media.ExifInterface.AnonymousClass1 */
                long mPosition;

                @Override // java.io.Closeable, java.lang.AutoCloseable
                public void close() throws IOException {
                }

                @Override // android.media.MediaDataSource
                public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
                    if (size == 0) {
                        return 0;
                    }
                    if (position < 0) {
                        return -1;
                    }
                    try {
                        if (this.mPosition != position) {
                            if (this.mPosition >= 0 && position >= this.mPosition + ((long) in.available())) {
                                return -1;
                            }
                            in.seek(position);
                            this.mPosition = position;
                        }
                        if (size > in.available()) {
                            size = in.available();
                        }
                        int bytesRead = in.read(buffer, offset, size);
                        if (bytesRead >= 0) {
                            this.mPosition += (long) bytesRead;
                            return bytesRead;
                        }
                    } catch (IOException e) {
                    }
                    this.mPosition = -1;
                    return -1;
                }

                @Override // android.media.MediaDataSource
                public long getSize() throws IOException {
                    return -1;
                }
            });
            String exifOffsetStr = retriever.extractMetadata(33);
            String exifLengthStr = retriever.extractMetadata(34);
            String hasImage = retriever.extractMetadata(26);
            String hasVideo = retriever.extractMetadata(17);
            String width = null;
            String height = null;
            String rotation = null;
            if ("yes".equals(hasImage)) {
                width = retriever.extractMetadata(29);
                height = retriever.extractMetadata(30);
                rotation = retriever.extractMetadata(31);
            } else if ("yes".equals(hasVideo)) {
                width = retriever.extractMetadata(18);
                height = retriever.extractMetadata(19);
                rotation = retriever.extractMetadata(24);
            }
            if (width != null) {
                this.mAttributes[0].put(TAG_IMAGE_WIDTH, ExifAttribute.createUShort(Integer.parseInt(width), this.mExifByteOrder));
            }
            if (height != null) {
                this.mAttributes[0].put(TAG_IMAGE_LENGTH, ExifAttribute.createUShort(Integer.parseInt(height), this.mExifByteOrder));
            }
            if (rotation != null) {
                int orientation = 1;
                int parseInt = Integer.parseInt(rotation);
                if (parseInt == 90) {
                    orientation = 6;
                } else if (parseInt == 180) {
                    orientation = 3;
                } else if (parseInt == 270) {
                    orientation = 8;
                }
                this.mAttributes[0].put(TAG_ORIENTATION, ExifAttribute.createUShort(orientation, this.mExifByteOrder));
            }
            if (exifOffsetStr != null && exifLengthStr != null) {
                int offset = Integer.parseInt(exifOffsetStr);
                int length = Integer.parseInt(exifLengthStr);
                if (length > 6) {
                    in.seek((long) offset);
                    byte[] identifier = new byte[6];
                    if (in.read(identifier) == 6) {
                        int offset2 = offset + 6;
                        int length2 = length - 6;
                        if (Arrays.equals(identifier, IDENTIFIER_EXIF_APP1)) {
                            byte[] bytes = new byte[length2];
                            if (in.read(bytes) == length2) {
                                this.mExifOffset = offset2;
                                readExifSegment(bytes, 0);
                            } else {
                                throw new IOException("Can't read exif");
                            }
                        } else {
                            throw new IOException("Invalid identifier");
                        }
                    } else {
                        throw new IOException("Can't read identifier");
                    }
                } else {
                    throw new IOException("Invalid exif length");
                }
            }
            if (DEBUG) {
                Log.d(TAG, "Heif meta: " + width + "x" + height + ", rotation " + rotation);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Get heif attributes failed.");
        } catch (Throwable th) {
            retriever.release();
            throw th;
        }
        retriever.release();
    }

    private void getOrfAttributes(ByteOrderedDataInputStream in) throws IOException {
        getRawAttributes(in);
        ExifAttribute makerNoteAttribute = (ExifAttribute) this.mAttributes[1].get(TAG_MAKER_NOTE);
        if (makerNoteAttribute != null) {
            ByteOrderedDataInputStream makerNoteDataInputStream = new ByteOrderedDataInputStream(makerNoteAttribute.bytes);
            makerNoteDataInputStream.setByteOrder(this.mExifByteOrder);
            byte[] makerNoteHeader1Bytes = new byte[ORF_MAKER_NOTE_HEADER_1.length];
            makerNoteDataInputStream.readFully(makerNoteHeader1Bytes);
            makerNoteDataInputStream.seek(0);
            byte[] makerNoteHeader2Bytes = new byte[ORF_MAKER_NOTE_HEADER_2.length];
            makerNoteDataInputStream.readFully(makerNoteHeader2Bytes);
            if (Arrays.equals(makerNoteHeader1Bytes, ORF_MAKER_NOTE_HEADER_1)) {
                makerNoteDataInputStream.seek(8);
            } else if (Arrays.equals(makerNoteHeader2Bytes, ORF_MAKER_NOTE_HEADER_2)) {
                makerNoteDataInputStream.seek(12);
            }
            readImageFileDirectory(makerNoteDataInputStream, 6);
            ExifAttribute imageLengthAttribute = (ExifAttribute) this.mAttributes[7].get(TAG_ORF_PREVIEW_IMAGE_START);
            ExifAttribute bitsPerSampleAttribute = (ExifAttribute) this.mAttributes[7].get(TAG_ORF_PREVIEW_IMAGE_LENGTH);
            if (!(imageLengthAttribute == null || bitsPerSampleAttribute == null)) {
                this.mAttributes[5].put(TAG_JPEG_INTERCHANGE_FORMAT, imageLengthAttribute);
                this.mAttributes[5].put(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, bitsPerSampleAttribute);
            }
            ExifAttribute aspectFrameAttribute = (ExifAttribute) this.mAttributes[8].get(TAG_ORF_ASPECT_FRAME);
            if (aspectFrameAttribute != null) {
                int[] iArr = new int[4];
                int[] aspectFrameValues = (int[]) aspectFrameAttribute.getValue(this.mExifByteOrder);
                if (aspectFrameValues[2] > aspectFrameValues[0] && aspectFrameValues[3] > aspectFrameValues[1]) {
                    int primaryImageWidth = (aspectFrameValues[2] - aspectFrameValues[0]) + 1;
                    int primaryImageLength = (aspectFrameValues[3] - aspectFrameValues[1]) + 1;
                    if (primaryImageWidth < primaryImageLength) {
                        int primaryImageWidth2 = primaryImageWidth + primaryImageLength;
                        primaryImageLength = primaryImageWidth2 - primaryImageLength;
                        primaryImageWidth = primaryImageWidth2 - primaryImageLength;
                    }
                    ExifAttribute primaryImageWidthAttribute = ExifAttribute.createUShort(primaryImageWidth, this.mExifByteOrder);
                    ExifAttribute primaryImageLengthAttribute = ExifAttribute.createUShort(primaryImageLength, this.mExifByteOrder);
                    this.mAttributes[0].put(TAG_IMAGE_WIDTH, primaryImageWidthAttribute);
                    this.mAttributes[0].put(TAG_IMAGE_LENGTH, primaryImageLengthAttribute);
                }
            }
        }
    }

    private void getRw2Attributes(ByteOrderedDataInputStream in) throws IOException {
        getRawAttributes(in);
        if (((ExifAttribute) this.mAttributes[0].get(TAG_RW2_JPG_FROM_RAW)) != null) {
            getJpegAttributes(in, this.mRw2JpgFromRawOffset, 5);
        }
        ExifAttribute rw2IsoAttribute = (ExifAttribute) this.mAttributes[0].get(TAG_RW2_ISO);
        ExifAttribute exifIsoAttribute = (ExifAttribute) this.mAttributes[1].get("ISOSpeedRatings");
        if (rw2IsoAttribute != null && exifIsoAttribute == null) {
            this.mAttributes[1].put("ISOSpeedRatings", rw2IsoAttribute);
        }
    }

    private void saveJpegAttributes(InputStream inputStream, OutputStream outputStream) throws IOException {
        if (DEBUG) {
            Log.d(TAG, "saveJpegAttributes starting with (inputStream: " + inputStream + ", outputStream: " + outputStream + ")");
        }
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        ByteOrderedDataOutputStream dataOutputStream = new ByteOrderedDataOutputStream(outputStream, ByteOrder.BIG_ENDIAN);
        if (dataInputStream.readByte() == -1) {
            dataOutputStream.writeByte(-1);
            if (dataInputStream.readByte() == -40) {
                dataOutputStream.writeByte(-40);
                dataOutputStream.writeByte(-1);
                dataOutputStream.writeByte(-31);
                writeExifSegment(dataOutputStream);
                byte[] bytes = new byte[4096];
                while (dataInputStream.readByte() == -1) {
                    byte marker = dataInputStream.readByte();
                    if (marker == -39 || marker == -38) {
                        dataOutputStream.writeByte(-1);
                        dataOutputStream.writeByte(marker);
                        Streams.copy(dataInputStream, dataOutputStream);
                        return;
                    } else if (marker != -31) {
                        dataOutputStream.writeByte(-1);
                        dataOutputStream.writeByte(marker);
                        int length = dataInputStream.readUnsignedShort();
                        dataOutputStream.writeUnsignedShort(length);
                        int length2 = length - 2;
                        if (length2 >= 0) {
                            while (length2 > 0) {
                                int read = dataInputStream.read(bytes, 0, Math.min(length2, bytes.length));
                                if (read < 0) {
                                    break;
                                }
                                dataOutputStream.write(bytes, 0, read);
                                length2 -= read;
                            }
                        } else {
                            throw new IOException("Invalid length");
                        }
                    } else {
                        int length3 = dataInputStream.readUnsignedShort() - 2;
                        if (length3 >= 0) {
                            byte[] identifier = new byte[6];
                            if (length3 >= 6) {
                                if (dataInputStream.read(identifier) != 6) {
                                    throw new IOException("Invalid exif");
                                } else if (Arrays.equals(identifier, IDENTIFIER_EXIF_APP1)) {
                                    if (dataInputStream.skipBytes(length3 - 6) != length3 - 6) {
                                        throw new IOException("Invalid length");
                                    }
                                }
                            }
                            dataOutputStream.writeByte(-1);
                            dataOutputStream.writeByte(marker);
                            dataOutputStream.writeUnsignedShort(length3 + 2);
                            if (length3 >= 6) {
                                length3 -= 6;
                                dataOutputStream.write(identifier);
                            }
                            while (length3 > 0) {
                                int read2 = dataInputStream.read(bytes, 0, Math.min(length3, bytes.length));
                                if (read2 < 0) {
                                    break;
                                }
                                dataOutputStream.write(bytes, 0, read2);
                                length3 -= read2;
                            }
                        } else {
                            throw new IOException("Invalid length");
                        }
                    }
                }
                throw new IOException("Invalid marker");
            }
            throw new IOException("Invalid marker");
        }
        throw new IOException("Invalid marker");
    }

    private void readExifSegment(byte[] exifBytes, int imageType) throws IOException {
        ByteOrderedDataInputStream dataInputStream = new ByteOrderedDataInputStream(exifBytes);
        parseTiffHeaders(dataInputStream, exifBytes.length);
        readImageFileDirectory(dataInputStream, imageType);
    }

    private void addDefaultValuesForCompatibility() {
        String valueOfDateTimeOriginal = getAttribute(TAG_DATETIME_ORIGINAL);
        if (valueOfDateTimeOriginal != null && getAttribute(TAG_DATETIME) == null) {
            this.mAttributes[0].put(TAG_DATETIME, ExifAttribute.createString(valueOfDateTimeOriginal));
        }
        if (getAttribute(TAG_IMAGE_WIDTH) == null) {
            this.mAttributes[0].put(TAG_IMAGE_WIDTH, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (getAttribute(TAG_IMAGE_LENGTH) == null) {
            this.mAttributes[0].put(TAG_IMAGE_LENGTH, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (getAttribute(TAG_ORIENTATION) == null) {
            this.mAttributes[0].put(TAG_ORIENTATION, ExifAttribute.createUShort(0, this.mExifByteOrder));
        }
        if (getAttribute(TAG_LIGHT_SOURCE) == null) {
            this.mAttributes[1].put(TAG_LIGHT_SOURCE, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
    }

    private ByteOrder readByteOrder(ByteOrderedDataInputStream dataInputStream) throws IOException {
        short byteOrder = dataInputStream.readShort();
        if (byteOrder == 18761) {
            if (DEBUG) {
                Log.d(TAG, "readExifSegment: Byte Align II");
            }
            return ByteOrder.LITTLE_ENDIAN;
        } else if (byteOrder == 19789) {
            if (DEBUG) {
                Log.d(TAG, "readExifSegment: Byte Align MM");
            }
            return ByteOrder.BIG_ENDIAN;
        } else {
            throw new IOException("Invalid byte order: " + Integer.toHexString(byteOrder));
        }
    }

    private void parseTiffHeaders(ByteOrderedDataInputStream dataInputStream, int exifBytesLength) throws IOException {
        this.mExifByteOrder = readByteOrder(dataInputStream);
        dataInputStream.setByteOrder(this.mExifByteOrder);
        int startCode = dataInputStream.readUnsignedShort();
        int i = this.mMimeType;
        if (i == 7 || i == 10 || startCode == 42) {
            int firstIfdOffset = dataInputStream.readInt();
            if (firstIfdOffset < 8 || firstIfdOffset >= exifBytesLength) {
                throw new IOException("Invalid first Ifd offset: " + firstIfdOffset);
            }
            int firstIfdOffset2 = firstIfdOffset - 8;
            if (firstIfdOffset2 > 0 && dataInputStream.skipBytes(firstIfdOffset2) != firstIfdOffset2) {
                throw new IOException("Couldn't jump to first Ifd: " + firstIfdOffset2);
            }
            return;
        }
        throw new IOException("Invalid start code: " + Integer.toHexString(startCode));
    }

    private String getNewNameForMnote(byte[] value, int mnoteNum) {
        int length = value.length;
        byte[] bArr = MNOTE_HW_HEADER;
        if (length >= bArr.length) {
            byte[] header = new byte[bArr.length];
            System.arraycopy(value, 0, header, 0, bArr.length);
            if (Arrays.equals(header, MNOTE_HW_HEADER)) {
                return HW_MAKER_NOTE;
            }
        }
        ExifAttribute attribute = (ExifAttribute) this.mAttributes[1].get(TAG_MAKER_NOTE);
        if (attribute == null) {
            return TAG_MAKER_NOTE;
        }
        this.mAttributes[1].remove(TAG_MAKER_NOTE);
        HashMap hashMap = this.mAttributes[1];
        hashMap.put(TAG_MAKER_NOTE + mnoteNum, attribute);
        return TAG_MAKER_NOTE;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0099: APUT  (r11v5 java.lang.Object[]), (2 ??[int, float, short, byte, char]), (r13v9 java.lang.String) */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x034b  */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x0385  */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x038b  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0134  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x013d  */
    private void readImageFileDirectory(ByteOrderedDataInputStream dataInputStream, int ifdType) throws IOException {
        boolean valid;
        long byteCount;
        short i;
        short numberOfDirectoryEntry;
        int mnoteNum;
        int i2;
        int tagNumber;
        String str;
        int dataFormat;
        int i3 = ifdType;
        this.mHandledIfdOffsets.add(Integer.valueOf(dataInputStream.mPosition));
        if (dataInputStream.mPosition + 2 <= dataInputStream.mLength) {
            short numberOfDirectoryEntry2 = dataInputStream.readShort();
            if (dataInputStream.mPosition + (numberOfDirectoryEntry2 * 12) > dataInputStream.mLength) {
                return;
            }
            if (numberOfDirectoryEntry2 > 0) {
                if (DEBUG) {
                    Log.d(TAG, "numberOfDirectoryEntry: " + ((int) numberOfDirectoryEntry2));
                }
                int mnoteNum2 = 0;
                short i4 = 0;
                while (i4 < numberOfDirectoryEntry2) {
                    int tagNumber2 = dataInputStream.readUnsignedShort();
                    int dataFormat2 = dataInputStream.readUnsignedShort();
                    int numberOfComponents = dataInputStream.readInt();
                    long nextEntryOffset = (long) (dataInputStream.peek() + 4);
                    ExifTag tag = (ExifTag) sExifTagMapsForReading[i3].get(Integer.valueOf(tagNumber2));
                    if (DEBUG) {
                        Object[] objArr = new Object[5];
                        objArr[0] = Integer.valueOf(ifdType);
                        objArr[1] = Integer.valueOf(tagNumber2);
                        objArr[2] = tag != null ? tag.name : null;
                        objArr[3] = Integer.valueOf(dataFormat2);
                        objArr[4] = Integer.valueOf(numberOfComponents);
                        Log.d(TAG, String.format("ifdType: %d, tagNumber: %d, tagName: %s, dataFormat: %d, numberOfComponents: %d", objArr));
                    }
                    if (tag != null) {
                        if (dataFormat2 > 0) {
                            int[] iArr = IFD_FORMAT_BYTES_PER_FORMAT;
                            if (dataFormat2 >= iArr.length) {
                                valid = false;
                            } else {
                                valid = false;
                                long byteCount2 = ((long) numberOfComponents) * ((long) iArr[dataFormat2]);
                                if (byteCount2 < 0 || byteCount2 > 2147483647L) {
                                    if (DEBUG) {
                                        Log.d(TAG, "Skip the tag entry since the number of components is invalid: " + numberOfComponents);
                                    }
                                    byteCount = byteCount2;
                                    if (!valid) {
                                        dataInputStream.seek(nextEntryOffset);
                                        numberOfDirectoryEntry = numberOfDirectoryEntry2;
                                        i = i4;
                                    } else {
                                        numberOfDirectoryEntry = numberOfDirectoryEntry2;
                                        if (byteCount > 4) {
                                            int offset = dataInputStream.readInt();
                                            if (DEBUG) {
                                                i = i4;
                                                StringBuilder sb = new StringBuilder();
                                                dataFormat = dataFormat2;
                                                sb.append("seek to data offset: ");
                                                sb.append(offset);
                                                Log.d(TAG, sb.toString());
                                            } else {
                                                i = i4;
                                                dataFormat = dataFormat2;
                                            }
                                            int i5 = this.mMimeType;
                                            if (i5 != 7) {
                                                str = TAG_MAKER_NOTE;
                                                tagNumber = tagNumber2;
                                                if (i5 == 10 && tag.name == TAG_RW2_JPG_FROM_RAW) {
                                                    this.mRw2JpgFromRawOffset = offset;
                                                }
                                            } else if (tag.name == TAG_MAKER_NOTE) {
                                                this.mOrfMakerNoteOffset = offset;
                                                str = TAG_MAKER_NOTE;
                                                tagNumber = tagNumber2;
                                            } else if (i3 == 6 && tag.name == TAG_ORF_THUMBNAIL_IMAGE) {
                                                this.mOrfThumbnailOffset = offset;
                                                this.mOrfThumbnailLength = numberOfComponents;
                                                ExifAttribute compressionAttribute = ExifAttribute.createUShort(6, this.mExifByteOrder);
                                                int i6 = this.mOrfThumbnailOffset;
                                                str = TAG_MAKER_NOTE;
                                                ExifAttribute jpegInterchangeFormatAttribute = ExifAttribute.createULong((long) i6, this.mExifByteOrder);
                                                tagNumber = tagNumber2;
                                                ExifAttribute jpegInterchangeFormatLengthAttribute = ExifAttribute.createULong((long) this.mOrfThumbnailLength, this.mExifByteOrder);
                                                this.mAttributes[4].put(TAG_COMPRESSION, compressionAttribute);
                                                this.mAttributes[4].put(TAG_JPEG_INTERCHANGE_FORMAT, jpegInterchangeFormatAttribute);
                                                this.mAttributes[4].put(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, jpegInterchangeFormatLengthAttribute);
                                            } else {
                                                str = TAG_MAKER_NOTE;
                                                tagNumber = tagNumber2;
                                            }
                                            if (((long) offset) + byteCount <= ((long) dataInputStream.mLength)) {
                                                dataInputStream.seek((long) offset);
                                            } else {
                                                if (DEBUG) {
                                                    Log.d(TAG, "Skip the tag entry since data offset is invalid: " + offset);
                                                }
                                                dataInputStream.seek(nextEntryOffset);
                                            }
                                        } else {
                                            str = TAG_MAKER_NOTE;
                                            i = i4;
                                            tagNumber = tagNumber2;
                                            dataFormat = dataFormat2;
                                        }
                                        Integer nextIfdType = sExifPointerTagMap.get(Integer.valueOf(tagNumber));
                                        if (DEBUG) {
                                            Log.d(TAG, "nextIfdType: " + nextIfdType + " byteCount: " + byteCount);
                                        }
                                        if (nextIfdType != null) {
                                            long offset2 = -1;
                                            if (dataFormat == 3) {
                                                offset2 = (long) dataInputStream.readUnsignedShort();
                                            } else if (dataFormat == 4) {
                                                offset2 = dataInputStream.readUnsignedInt();
                                            } else if (dataFormat == 8) {
                                                offset2 = (long) dataInputStream.readShort();
                                            } else if (dataFormat == 9 || dataFormat == 13) {
                                                offset2 = (long) dataInputStream.readInt();
                                            }
                                            if (DEBUG) {
                                                Log.d(TAG, String.format("Offset: %d, tagName: %s", Long.valueOf(offset2), tag.name));
                                            }
                                            if (offset2 <= 0 || offset2 >= ((long) dataInputStream.mLength)) {
                                                if (DEBUG) {
                                                    Log.d(TAG, "Skip jump into the IFD since its offset is invalid: " + offset2);
                                                }
                                            } else if (!this.mHandledIfdOffsets.contains(Integer.valueOf((int) offset2))) {
                                                dataInputStream.seek(offset2);
                                                readImageFileDirectory(dataInputStream, nextIfdType.intValue());
                                            } else if (DEBUG) {
                                                Log.d(TAG, "Skip jump into the IFD since it has already been read: IfdType " + nextIfdType + " (at " + offset2 + ")");
                                            }
                                            dataInputStream.seek(nextEntryOffset);
                                        } else {
                                            int bytesOffset = dataInputStream.peek() + this.mExifOffset;
                                            byte[] bytes = new byte[((int) byteCount)];
                                            dataInputStream.readFully(bytes);
                                            ExifAttribute attribute = new ExifAttribute(dataFormat, numberOfComponents, (long) bytesOffset, bytes);
                                            String newName = tag.name;
                                            if (tag.name == str) {
                                                i2 = ifdType;
                                                if (i2 == 1) {
                                                    newName = getNewNameForMnote(bytes, mnoteNum2);
                                                    mnoteNum = mnoteNum2 + 1;
                                                    this.mAttributes[i2].put(newName, attribute);
                                                    if (tag.name == TAG_DNG_VERSION) {
                                                        this.mMimeType = 3;
                                                    }
                                                    if (((tag.name == TAG_MAKE || tag.name == TAG_MODEL) && attribute.getStringValue(this.mExifByteOrder).contains(PEF_SIGNATURE)) || (tag.name == TAG_COMPRESSION && attribute.getIntValue(this.mExifByteOrder) == 65535)) {
                                                        this.mMimeType = 8;
                                                    }
                                                    if (((long) dataInputStream.peek()) == nextEntryOffset) {
                                                        dataInputStream.seek(nextEntryOffset);
                                                    }
                                                    i4 = (short) (i + 1);
                                                    i3 = i2;
                                                    mnoteNum2 = mnoteNum;
                                                    numberOfDirectoryEntry2 = numberOfDirectoryEntry;
                                                }
                                            } else {
                                                i2 = ifdType;
                                            }
                                            mnoteNum = mnoteNum2;
                                            this.mAttributes[i2].put(newName, attribute);
                                            if (tag.name == TAG_DNG_VERSION) {
                                            }
                                            this.mMimeType = 8;
                                            if (((long) dataInputStream.peek()) == nextEntryOffset) {
                                            }
                                            i4 = (short) (i + 1);
                                            i3 = i2;
                                            mnoteNum2 = mnoteNum;
                                            numberOfDirectoryEntry2 = numberOfDirectoryEntry;
                                        }
                                    }
                                    i2 = ifdType;
                                    mnoteNum = mnoteNum2;
                                    i4 = (short) (i + 1);
                                    i3 = i2;
                                    mnoteNum2 = mnoteNum;
                                    numberOfDirectoryEntry2 = numberOfDirectoryEntry;
                                } else {
                                    valid = true;
                                    byteCount = byteCount2;
                                    if (!valid) {
                                    }
                                    i2 = ifdType;
                                    mnoteNum = mnoteNum2;
                                    i4 = (short) (i + 1);
                                    i3 = i2;
                                    mnoteNum2 = mnoteNum;
                                    numberOfDirectoryEntry2 = numberOfDirectoryEntry;
                                }
                            }
                        } else {
                            valid = false;
                        }
                        if (DEBUG) {
                            Log.d(TAG, "Skip the tag entry since data format is invalid: " + dataFormat2);
                        }
                    } else if (DEBUG) {
                        Log.d(TAG, "Skip the tag entry since tag number is not defined: " + tagNumber2);
                        valid = false;
                    } else {
                        valid = false;
                    }
                    byteCount = 0;
                    if (!valid) {
                    }
                    i2 = ifdType;
                    mnoteNum = mnoteNum2;
                    i4 = (short) (i + 1);
                    i3 = i2;
                    mnoteNum2 = mnoteNum;
                    numberOfDirectoryEntry2 = numberOfDirectoryEntry;
                }
                if (dataInputStream.peek() + 4 <= dataInputStream.mLength) {
                    int nextIfdOffset = dataInputStream.readInt();
                    if (DEBUG) {
                        Log.d(TAG, String.format("nextIfdOffset: %d", Integer.valueOf(nextIfdOffset)));
                    }
                    if (((long) nextIfdOffset) <= 0 || nextIfdOffset >= dataInputStream.mLength) {
                        if (DEBUG) {
                            Log.d(TAG, "Stop reading file since a wrong offset may cause an infinite loop: " + nextIfdOffset);
                        }
                    } else if (!this.mHandledIfdOffsets.contains(Integer.valueOf(nextIfdOffset))) {
                        dataInputStream.seek((long) nextIfdOffset);
                        if (this.mAttributes[4].isEmpty()) {
                            readImageFileDirectory(dataInputStream, 4);
                        } else if (this.mAttributes[5].isEmpty()) {
                            readImageFileDirectory(dataInputStream, 5);
                        }
                    } else if (DEBUG) {
                        Log.d(TAG, "Stop reading file since re-reading an IFD may cause an infinite loop: " + nextIfdOffset);
                    }
                }
            }
        }
    }

    private void retrieveJpegImageSize(ByteOrderedDataInputStream in, int imageType) throws IOException {
        ExifAttribute jpegInterchangeFormatAttribute;
        ExifAttribute imageLengthAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_IMAGE_LENGTH);
        ExifAttribute imageWidthAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_IMAGE_WIDTH);
        if ((imageLengthAttribute == null || imageWidthAttribute == null) && (jpegInterchangeFormatAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_JPEG_INTERCHANGE_FORMAT)) != null) {
            getJpegAttributes(in, jpegInterchangeFormatAttribute.getIntValue(this.mExifByteOrder), imageType);
        }
    }

    private void setThumbnailData(ByteOrderedDataInputStream in) throws IOException {
        HashMap thumbnailData = this.mAttributes[4];
        ExifAttribute compressionAttribute = (ExifAttribute) thumbnailData.get(TAG_COMPRESSION);
        if (compressionAttribute != null) {
            this.mThumbnailCompression = compressionAttribute.getIntValue(this.mExifByteOrder);
            int i = this.mThumbnailCompression;
            if (i != 1) {
                if (i == 6) {
                    handleThumbnailFromJfif(in, thumbnailData);
                    return;
                } else if (i != 7) {
                    return;
                }
            }
            if (isSupportedDataType(thumbnailData)) {
                handleThumbnailFromStrips(in, thumbnailData);
                return;
            }
            return;
        }
        handleThumbnailFromJfif(in, thumbnailData);
    }

    private void handleThumbnailFromJfif(ByteOrderedDataInputStream in, HashMap thumbnailData) throws IOException {
        ExifAttribute jpegInterchangeFormatAttribute = (ExifAttribute) thumbnailData.get(TAG_JPEG_INTERCHANGE_FORMAT);
        ExifAttribute jpegInterchangeFormatLengthAttribute = (ExifAttribute) thumbnailData.get(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
        if (jpegInterchangeFormatAttribute != null && jpegInterchangeFormatLengthAttribute != null) {
            int thumbnailOffset = jpegInterchangeFormatAttribute.getIntValue(this.mExifByteOrder);
            int thumbnailLength = Math.min(jpegInterchangeFormatLengthAttribute.getIntValue(this.mExifByteOrder), in.getLength() - thumbnailOffset);
            if (this.mMimeType == 7) {
                thumbnailOffset += this.mOrfMakerNoteOffset;
            }
            if (thumbnailOffset > 0 && thumbnailLength > 0) {
                this.mHasThumbnail = true;
                this.mThumbnailOffset = this.mExifOffset + thumbnailOffset;
                this.mThumbnailLength = thumbnailLength;
                this.mThumbnailCompression = 6;
                if (this.mFilename == null && this.mAssetInputStream == null && this.mSeekableFileDescriptor == null) {
                    byte[] thumbnailBytes = new byte[this.mThumbnailLength];
                    in.seek((long) this.mThumbnailOffset);
                    in.readFully(thumbnailBytes);
                    this.mThumbnailBytes = thumbnailBytes;
                }
                if (DEBUG) {
                    Log.d(TAG, "Setting thumbnail attributes with offset: " + thumbnailOffset + ", length: " + thumbnailLength);
                }
            }
        }
    }

    private void handleThumbnailFromStrips(ByteOrderedDataInputStream in, HashMap thumbnailData) throws IOException {
        ExifAttribute stripOffsetsAttribute;
        ExifAttribute stripOffsetsAttribute2 = (ExifAttribute) thumbnailData.get(TAG_STRIP_OFFSETS);
        ExifAttribute stripByteCountsAttribute = (ExifAttribute) thumbnailData.get(TAG_STRIP_BYTE_COUNTS);
        if (stripOffsetsAttribute2 != null && stripByteCountsAttribute != null) {
            long[] stripOffsets = convertToLongArray(stripOffsetsAttribute2.getValue(this.mExifByteOrder));
            long[] stripByteCounts = convertToLongArray(stripByteCountsAttribute.getValue(this.mExifByteOrder));
            if (stripOffsets != null) {
                if (stripOffsets.length != 0) {
                    if (stripByteCounts != null) {
                        if (stripByteCounts.length != 0) {
                            if (stripOffsets.length != stripByteCounts.length) {
                                Log.w(TAG, "stripOffsets and stripByteCounts should have same length.");
                                return;
                            }
                            byte[] totalStripBytes = new byte[((int) Arrays.stream(stripByteCounts).sum())];
                            int bytesRead = 0;
                            int bytesAdded = 0;
                            int i = 1;
                            this.mAreThumbnailStripsConsecutive = true;
                            this.mHasThumbnailStrips = true;
                            this.mHasThumbnail = true;
                            int i2 = 0;
                            while (i2 < stripOffsets.length) {
                                int stripOffset = (int) stripOffsets[i2];
                                int stripByteCount = (int) stripByteCounts[i2];
                                if (i2 < stripOffsets.length - i) {
                                    stripOffsetsAttribute = stripOffsetsAttribute2;
                                    if (((long) (stripOffset + stripByteCount)) != stripOffsets[i2 + 1]) {
                                        this.mAreThumbnailStripsConsecutive = false;
                                    }
                                } else {
                                    stripOffsetsAttribute = stripOffsetsAttribute2;
                                }
                                int skipBytes = stripOffset - bytesRead;
                                if (skipBytes < 0) {
                                    Log.d(TAG, "Invalid strip offset value");
                                }
                                in.seek((long) skipBytes);
                                byte[] stripBytes = new byte[stripByteCount];
                                in.read(stripBytes);
                                bytesRead = bytesRead + skipBytes + stripByteCount;
                                System.arraycopy(stripBytes, 0, totalStripBytes, bytesAdded, stripBytes.length);
                                bytesAdded += stripBytes.length;
                                i2++;
                                stripOffsetsAttribute2 = stripOffsetsAttribute;
                                i = 1;
                            }
                            this.mThumbnailBytes = totalStripBytes;
                            if (this.mAreThumbnailStripsConsecutive) {
                                this.mThumbnailOffset = ((int) stripOffsets[0]) + this.mExifOffset;
                                this.mThumbnailLength = totalStripBytes.length;
                                return;
                            }
                            return;
                        }
                    }
                    Log.w(TAG, "stripByteCounts should not be null or have zero length.");
                    return;
                }
            }
            Log.w(TAG, "stripOffsets should not be null or have zero length.");
        }
    }

    private boolean isSupportedDataType(HashMap thumbnailData) throws IOException {
        ExifAttribute photometricInterpretationAttribute;
        int photometricInterpretationValue;
        ExifAttribute bitsPerSampleAttribute = (ExifAttribute) thumbnailData.get(TAG_BITS_PER_SAMPLE);
        if (bitsPerSampleAttribute != null) {
            int[] bitsPerSampleValue = (int[]) bitsPerSampleAttribute.getValue(this.mExifByteOrder);
            if (Arrays.equals(BITS_PER_SAMPLE_RGB, bitsPerSampleValue)) {
                return true;
            }
            if (this.mMimeType == 3 && (photometricInterpretationAttribute = (ExifAttribute) thumbnailData.get(TAG_PHOTOMETRIC_INTERPRETATION)) != null && (((photometricInterpretationValue = photometricInterpretationAttribute.getIntValue(this.mExifByteOrder)) == 1 && Arrays.equals(bitsPerSampleValue, BITS_PER_SAMPLE_GREYSCALE_2)) || (photometricInterpretationValue == 6 && Arrays.equals(bitsPerSampleValue, BITS_PER_SAMPLE_RGB)))) {
                return true;
            }
        }
        if (!DEBUG) {
            return false;
        }
        Log.d(TAG, "Unsupported data type value");
        return false;
    }

    private boolean isThumbnail(HashMap map) throws IOException {
        ExifAttribute imageLengthAttribute = (ExifAttribute) map.get(TAG_IMAGE_LENGTH);
        ExifAttribute imageWidthAttribute = (ExifAttribute) map.get(TAG_IMAGE_WIDTH);
        if (imageLengthAttribute == null || imageWidthAttribute == null) {
            return false;
        }
        int imageLengthValue = imageLengthAttribute.getIntValue(this.mExifByteOrder);
        int imageWidthValue = imageWidthAttribute.getIntValue(this.mExifByteOrder);
        if (imageLengthValue > 512 || imageWidthValue > 512) {
            return false;
        }
        return true;
    }

    private void validateImages(InputStream in) throws IOException {
        swapBasedOnImageSize(0, 5);
        swapBasedOnImageSize(0, 4);
        swapBasedOnImageSize(5, 4);
        ExifAttribute pixelXDimAttribute = (ExifAttribute) this.mAttributes[1].get(TAG_PIXEL_X_DIMENSION);
        ExifAttribute pixelYDimAttribute = (ExifAttribute) this.mAttributes[1].get(TAG_PIXEL_Y_DIMENSION);
        if (!(pixelXDimAttribute == null || pixelYDimAttribute == null)) {
            this.mAttributes[0].put(TAG_IMAGE_WIDTH, pixelXDimAttribute);
            this.mAttributes[0].put(TAG_IMAGE_LENGTH, pixelYDimAttribute);
        }
        if (this.mAttributes[4].isEmpty() && isThumbnail(this.mAttributes[5])) {
            HashMap[] hashMapArr = this.mAttributes;
            hashMapArr[4] = hashMapArr[5];
            hashMapArr[5] = new HashMap();
        }
        if (!isThumbnail(this.mAttributes[4])) {
            Log.d(TAG, "No image meets the size requirements of a thumbnail image.");
        }
    }

    private void updateImageSizeValues(ByteOrderedDataInputStream in, int imageType) throws IOException {
        ExifAttribute defaultCropSizeXAttribute;
        ExifAttribute defaultCropSizeYAttribute;
        ExifAttribute defaultCropSizeAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_DEFAULT_CROP_SIZE);
        ExifAttribute topBorderAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_RW2_SENSOR_TOP_BORDER);
        ExifAttribute leftBorderAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_RW2_SENSOR_LEFT_BORDER);
        ExifAttribute bottomBorderAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_RW2_SENSOR_BOTTOM_BORDER);
        ExifAttribute rightBorderAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_RW2_SENSOR_RIGHT_BORDER);
        if (defaultCropSizeAttribute != null) {
            if (defaultCropSizeAttribute.format == 5) {
                Rational[] defaultCropSizeValue = (Rational[]) defaultCropSizeAttribute.getValue(this.mExifByteOrder);
                defaultCropSizeXAttribute = ExifAttribute.createURational(defaultCropSizeValue[0], this.mExifByteOrder);
                defaultCropSizeYAttribute = ExifAttribute.createURational(defaultCropSizeValue[1], this.mExifByteOrder);
            } else {
                int[] defaultCropSizeValue2 = (int[]) defaultCropSizeAttribute.getValue(this.mExifByteOrder);
                defaultCropSizeXAttribute = ExifAttribute.createUShort(defaultCropSizeValue2[0], this.mExifByteOrder);
                defaultCropSizeYAttribute = ExifAttribute.createUShort(defaultCropSizeValue2[1], this.mExifByteOrder);
            }
            this.mAttributes[imageType].put(TAG_IMAGE_WIDTH, defaultCropSizeXAttribute);
            this.mAttributes[imageType].put(TAG_IMAGE_LENGTH, defaultCropSizeYAttribute);
        } else if (topBorderAttribute == null || leftBorderAttribute == null || bottomBorderAttribute == null || rightBorderAttribute == null) {
            retrieveJpegImageSize(in, imageType);
        } else {
            int topBorderValue = topBorderAttribute.getIntValue(this.mExifByteOrder);
            int bottomBorderValue = bottomBorderAttribute.getIntValue(this.mExifByteOrder);
            int rightBorderValue = rightBorderAttribute.getIntValue(this.mExifByteOrder);
            int leftBorderValue = leftBorderAttribute.getIntValue(this.mExifByteOrder);
            if (bottomBorderValue > topBorderValue && rightBorderValue > leftBorderValue) {
                ExifAttribute imageLengthAttribute = ExifAttribute.createUShort(bottomBorderValue - topBorderValue, this.mExifByteOrder);
                ExifAttribute imageWidthAttribute = ExifAttribute.createUShort(rightBorderValue - leftBorderValue, this.mExifByteOrder);
                this.mAttributes[imageType].put(TAG_IMAGE_LENGTH, imageLengthAttribute);
                this.mAttributes[imageType].put(TAG_IMAGE_WIDTH, imageWidthAttribute);
            }
        }
    }

    /* JADX INFO: Multiple debug info for r7v6 int: [D('thumbnailOffset' int), D('totalSize' int)] */
    private int writeExifSegment(ByteOrderedDataOutputStream dataOutputStream) throws IOException {
        int i;
        boolean z;
        ExifTag[][] exifTagArr = EXIF_TAGS;
        int[] ifdOffsets = new int[exifTagArr.length];
        int[] ifdDataSizes = new int[exifTagArr.length];
        boolean z2 = false;
        for (ExifTag tag : EXIF_POINTER_TAGS) {
            removeAttribute(tag.name);
        }
        removeAttribute(JPEG_INTERCHANGE_FORMAT_TAG.name);
        removeAttribute(JPEG_INTERCHANGE_FORMAT_LENGTH_TAG.name);
        for (int ifdType = 0; ifdType < EXIF_TAGS.length; ifdType++) {
            for (Object obj : this.mAttributes[ifdType].entrySet().toArray()) {
                Map.Entry entry = (Map.Entry) obj;
                if (entry.getValue() == null) {
                    this.mAttributes[ifdType].remove(entry.getKey());
                }
            }
        }
        if (!this.mAttributes[1].isEmpty()) {
            this.mAttributes[0].put(EXIF_POINTER_TAGS[1].name, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        int i2 = 2;
        if (!this.mAttributes[2].isEmpty()) {
            this.mAttributes[0].put(EXIF_POINTER_TAGS[2].name, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (!this.mAttributes[3].isEmpty()) {
            this.mAttributes[1].put(EXIF_POINTER_TAGS[3].name, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        int i3 = 4;
        if (this.mHasThumbnail) {
            this.mAttributes[4].put(JPEG_INTERCHANGE_FORMAT_TAG.name, ExifAttribute.createULong(0, this.mExifByteOrder));
            this.mAttributes[4].put(JPEG_INTERCHANGE_FORMAT_LENGTH_TAG.name, ExifAttribute.createULong((long) this.mThumbnailLength, this.mExifByteOrder));
        }
        for (int i4 = 0; i4 < EXIF_TAGS.length; i4++) {
            int sum = 0;
            for (Map.Entry entry2 : this.mAttributes[i4].entrySet()) {
                int size = ((ExifAttribute) entry2.getValue()).size();
                if (size > 4) {
                    sum += size;
                }
            }
            ifdDataSizes[i4] = ifdDataSizes[i4] + sum;
        }
        int position = 8;
        for (int ifdType2 = 0; ifdType2 < EXIF_TAGS.length; ifdType2++) {
            if (!this.mAttributes[ifdType2].isEmpty()) {
                ifdOffsets[ifdType2] = position;
                position += (this.mAttributes[ifdType2].size() * 12) + 2 + 4 + ifdDataSizes[ifdType2];
            }
        }
        if (this.mHasThumbnail) {
            this.mAttributes[4].put(JPEG_INTERCHANGE_FORMAT_TAG.name, ExifAttribute.createULong((long) position, this.mExifByteOrder));
            this.mThumbnailOffset = this.mExifOffset + position;
            position += this.mThumbnailLength;
        }
        int totalSize = position + 8;
        if (DEBUG) {
            Log.d(TAG, "totalSize length: " + totalSize);
            for (int i5 = 0; i5 < EXIF_TAGS.length; i5++) {
                Log.d(TAG, String.format("index: %d, offsets: %d, tag count: %d, data sizes: %d", Integer.valueOf(i5), Integer.valueOf(ifdOffsets[i5]), Integer.valueOf(this.mAttributes[i5].size()), Integer.valueOf(ifdDataSizes[i5])));
            }
        }
        if (!this.mAttributes[1].isEmpty()) {
            this.mAttributes[0].put(EXIF_POINTER_TAGS[1].name, ExifAttribute.createULong((long) ifdOffsets[1], this.mExifByteOrder));
        }
        if (!this.mAttributes[2].isEmpty()) {
            this.mAttributes[0].put(EXIF_POINTER_TAGS[2].name, ExifAttribute.createULong((long) ifdOffsets[2], this.mExifByteOrder));
        }
        if (!this.mAttributes[3].isEmpty()) {
            this.mAttributes[1].put(EXIF_POINTER_TAGS[3].name, ExifAttribute.createULong((long) ifdOffsets[3], this.mExifByteOrder));
        }
        dataOutputStream.writeUnsignedShort(totalSize);
        dataOutputStream.write(IDENTIFIER_EXIF_APP1);
        dataOutputStream.writeShort(this.mExifByteOrder == ByteOrder.BIG_ENDIAN ? BYTE_ALIGN_MM : BYTE_ALIGN_II);
        dataOutputStream.setByteOrder(this.mExifByteOrder);
        dataOutputStream.writeUnsignedShort(42);
        dataOutputStream.writeUnsignedInt(8);
        int ifdType3 = 0;
        while (ifdType3 < EXIF_TAGS.length) {
            if (!this.mAttributes[ifdType3].isEmpty()) {
                dataOutputStream.writeUnsignedShort(this.mAttributes[ifdType3].size());
                int dataOffset = ifdOffsets[ifdType3] + i2 + (this.mAttributes[ifdType3].size() * 12) + i3;
                for (Map.Entry entry3 : this.mAttributes[ifdType3].entrySet()) {
                    if (entry3.getKey() instanceof String) {
                        String key = (String) entry3.getKey();
                        if (!(key == null || key.indexOf(TAG_MAKER_NOTE) == -1)) {
                            key = TAG_MAKER_NOTE;
                        }
                        int tagNumber = ((ExifTag) sExifTagMapsForWriting[ifdType3].get(key)).number;
                        ExifAttribute attribute = (ExifAttribute) entry3.getValue();
                        int size2 = attribute.size();
                        dataOutputStream.writeUnsignedShort(tagNumber);
                        dataOutputStream.writeUnsignedShort(attribute.format);
                        dataOutputStream.writeInt(attribute.numberOfComponents);
                        if (size2 > 4) {
                            dataOutputStream.writeUnsignedInt((long) dataOffset);
                            dataOffset += size2;
                        } else {
                            dataOutputStream.write(attribute.bytes);
                            if (size2 < 4) {
                                int i6 = size2;
                                for (int i7 = 4; i6 < i7; i7 = 4) {
                                    dataOutputStream.writeByte(0);
                                    i6++;
                                }
                            }
                        }
                    }
                }
                if (ifdType3 != 0 || this.mAttributes[4].isEmpty()) {
                    dataOutputStream.writeUnsignedInt(0);
                } else {
                    dataOutputStream.writeUnsignedInt((long) ifdOffsets[4]);
                }
                for (Map.Entry entry4 : this.mAttributes[ifdType3].entrySet()) {
                    ExifAttribute attribute2 = (ExifAttribute) entry4.getValue();
                    if (attribute2.bytes.length > 4) {
                        dataOutputStream.write(attribute2.bytes, 0, attribute2.bytes.length);
                    }
                }
                z = false;
                i = 4;
            } else {
                z = z2;
                i = i3;
            }
            ifdType3++;
            z2 = z;
            i3 = i;
            i2 = 2;
        }
        if (this.mHasThumbnail) {
            dataOutputStream.write(getThumbnailBytes());
        }
        dataOutputStream.setByteOrder(ByteOrder.BIG_ENDIAN);
        return totalSize;
    }

    public void saveAttributesFromHeicToJpg(String jpgPath) throws IOException {
        if (!this.mIsSupportedFile || this.mMimeType != 12) {
            throw new IOException("only supports saving attributes on HEIF formats.");
        }
        FileOutputStream out = null;
        File tempFile = null;
        FileInputStream jpgPathStream = null;
        if (jpgPath != null) {
            tempFile = new File(jpgPath + ".tmp");
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            } else {
                throw new IOException("tmp file existed");
            }
        }
        if (tempFile != null) {
            try {
                out = new FileOutputStream(tempFile);
            } catch (IOException e) {
                Log.e(TAG, "Failed to write");
            } catch (Throwable th) {
                IoUtils.closeQuietly((AutoCloseable) null);
                IoUtils.closeQuietly((AutoCloseable) null);
                throw th;
            }
        }
        ByteOrderedDataOutputStream dataOutputStream = new ByteOrderedDataOutputStream(out, ByteOrder.BIG_ENDIAN);
        dataOutputStream.writeByte(-1);
        dataOutputStream.writeByte(-40);
        dataOutputStream.writeByte(-1);
        dataOutputStream.writeByte(-31);
        writeExifSegment(dataOutputStream);
        File oldFile = new File(jpgPath);
        jpgPathStream = new FileInputStream(oldFile);
        jpgPathStream.skip(2);
        Streams.copy(jpgPathStream, out);
        if (!tempFile.renameTo(oldFile)) {
            if (!tempFile.delete()) {
                Log.w(TAG, "delete fail");
            }
            Log.e(TAG, "Could'nt rename tempFile ");
        }
        IoUtils.closeQuietly(out);
        IoUtils.closeQuietly(jpgPathStream);
    }

    private static Pair<Integer, Integer> guessDataFormat(String entryValue) {
        if (entryValue.contains(SmsManager.REGEX_PREFIX_DELIMITER)) {
            String[] entryValues = entryValue.split(SmsManager.REGEX_PREFIX_DELIMITER);
            Pair<Integer, Integer> dataFormat = guessDataFormat(entryValues[0]);
            if (dataFormat.first.intValue() == 2) {
                return dataFormat;
            }
            for (int i = 1; i < entryValues.length; i++) {
                Pair<Integer, Integer> guessDataFormat = guessDataFormat(entryValues[i]);
                int first = -1;
                int second = -1;
                if (guessDataFormat.first == dataFormat.first || guessDataFormat.second == dataFormat.first) {
                    first = dataFormat.first.intValue();
                }
                if (dataFormat.second.intValue() != -1 && (guessDataFormat.first == dataFormat.second || guessDataFormat.second == dataFormat.second)) {
                    second = dataFormat.second.intValue();
                }
                if (first == -1 && second == -1) {
                    return new Pair<>(2, -1);
                }
                if (first == -1) {
                    dataFormat = new Pair<>(Integer.valueOf(second), -1);
                } else if (second == -1) {
                    dataFormat = new Pair<>(Integer.valueOf(first), -1);
                }
            }
            return dataFormat;
        } else if (entryValue.contains("/")) {
            String[] rationalNumber = entryValue.split("/");
            if (rationalNumber.length == 2) {
                try {
                    long numerator = (long) Double.parseDouble(rationalNumber[0]);
                    long denominator = (long) Double.parseDouble(rationalNumber[1]);
                    if (numerator >= 0) {
                        if (denominator >= 0) {
                            if (numerator <= 2147483647L) {
                                if (denominator <= 2147483647L) {
                                    return new Pair<>(10, 5);
                                }
                            }
                            return new Pair<>(5, -1);
                        }
                    }
                    return new Pair<>(10, -1);
                } catch (NumberFormatException e) {
                }
            }
            return new Pair<>(2, -1);
        } else {
            try {
                Long longValue = Long.valueOf(Long.parseLong(entryValue));
                if (longValue.longValue() >= 0 && longValue.longValue() <= 65535) {
                    return new Pair<>(3, 4);
                }
                if (longValue.longValue() < 0) {
                    return new Pair<>(9, -1);
                }
                return new Pair<>(4, -1);
            } catch (NumberFormatException e2) {
                try {
                    Double.parseDouble(entryValue);
                    return new Pair<>(12, -1);
                } catch (NumberFormatException e3) {
                    return new Pair<>(2, -1);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ByteOrderedDataInputStream extends InputStream implements DataInput {
        private static final ByteOrder BIG_ENDIAN = ByteOrder.BIG_ENDIAN;
        private static final ByteOrder LITTLE_ENDIAN = ByteOrder.LITTLE_ENDIAN;
        private ByteOrder mByteOrder;
        private DataInputStream mDataInputStream;
        private InputStream mInputStream;
        private final int mLength;
        private int mPosition;

        public ByteOrderedDataInputStream(InputStream in) throws IOException {
            this.mByteOrder = ByteOrder.BIG_ENDIAN;
            this.mInputStream = in;
            this.mDataInputStream = new DataInputStream(in);
            this.mLength = this.mDataInputStream.available();
            this.mPosition = 0;
            this.mDataInputStream.mark(this.mLength);
        }

        public ByteOrderedDataInputStream(byte[] bytes) throws IOException {
            this(new ByteArrayInputStream(bytes));
        }

        public void setByteOrder(ByteOrder byteOrder) {
            this.mByteOrder = byteOrder;
        }

        public void seek(long byteCount) throws IOException {
            int i = this.mPosition;
            if (((long) i) > byteCount) {
                this.mPosition = 0;
                this.mDataInputStream.reset();
                this.mDataInputStream.mark(this.mLength);
            } else {
                byteCount -= (long) i;
            }
            if (skipBytes((int) byteCount) != ((int) byteCount)) {
                throw new IOException("Couldn't seek up to the byteCount");
            }
        }

        public int peek() {
            return this.mPosition;
        }

        @Override // java.io.InputStream
        public int available() throws IOException {
            return this.mDataInputStream.available();
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            this.mPosition++;
            return this.mDataInputStream.read();
        }

        @Override // java.io.DataInput
        public int readUnsignedByte() throws IOException {
            this.mPosition++;
            return this.mDataInputStream.readUnsignedByte();
        }

        @Override // java.io.DataInput
        public String readLine() throws IOException {
            Log.d(ExifInterface.TAG, "Currently unsupported");
            return null;
        }

        @Override // java.io.DataInput
        public boolean readBoolean() throws IOException {
            this.mPosition++;
            return this.mDataInputStream.readBoolean();
        }

        @Override // java.io.DataInput
        public char readChar() throws IOException {
            this.mPosition += 2;
            return this.mDataInputStream.readChar();
        }

        @Override // java.io.DataInput
        public String readUTF() throws IOException {
            this.mPosition += 2;
            return this.mDataInputStream.readUTF();
        }

        @Override // java.io.DataInput
        public void readFully(byte[] buffer, int offset, int length) throws IOException {
            this.mPosition += length;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            } else if (this.mDataInputStream.read(buffer, offset, length) != length) {
                throw new IOException("Couldn't read up to the length of buffer");
            }
        }

        @Override // java.io.DataInput
        public void readFully(byte[] buffer) throws IOException {
            this.mPosition += buffer.length;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            } else if (this.mDataInputStream.read(buffer, 0, buffer.length) != buffer.length) {
                throw new IOException("Couldn't read up to the length of buffer");
            }
        }

        @Override // java.io.DataInput
        public byte readByte() throws IOException {
            this.mPosition++;
            if (this.mPosition <= this.mLength) {
                int ch = this.mDataInputStream.read();
                if (ch >= 0) {
                    return (byte) ch;
                }
                throw new EOFException();
            }
            throw new EOFException();
        }

        @Override // java.io.DataInput
        public short readShort() throws IOException {
            this.mPosition += 2;
            if (this.mPosition <= this.mLength) {
                int ch1 = this.mDataInputStream.read();
                int ch2 = this.mDataInputStream.read();
                if ((ch1 | ch2) >= 0) {
                    ByteOrder byteOrder = this.mByteOrder;
                    if (byteOrder == LITTLE_ENDIAN) {
                        return (short) ((ch2 << 8) + ch1);
                    }
                    if (byteOrder == BIG_ENDIAN) {
                        return (short) ((ch1 << 8) + ch2);
                    }
                    throw new IOException("Invalid byte order: " + this.mByteOrder);
                }
                throw new EOFException();
            }
            throw new EOFException();
        }

        @Override // java.io.DataInput
        public int readInt() throws IOException {
            this.mPosition += 4;
            if (this.mPosition <= this.mLength) {
                int ch1 = this.mDataInputStream.read();
                int ch2 = this.mDataInputStream.read();
                int ch3 = this.mDataInputStream.read();
                int ch4 = this.mDataInputStream.read();
                if ((ch1 | ch2 | ch3 | ch4) >= 0) {
                    ByteOrder byteOrder = this.mByteOrder;
                    if (byteOrder == LITTLE_ENDIAN) {
                        return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1;
                    }
                    if (byteOrder == BIG_ENDIAN) {
                        return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4;
                    }
                    throw new IOException("Invalid byte order: " + this.mByteOrder);
                }
                throw new EOFException();
            }
            throw new EOFException();
        }

        @Override // java.io.DataInput
        public int skipBytes(int byteCount) throws IOException {
            int totalSkip = Math.min(byteCount, this.mLength - this.mPosition);
            int skipped = 0;
            while (skipped < totalSkip) {
                skipped += this.mDataInputStream.skipBytes(totalSkip - skipped);
            }
            this.mPosition += skipped;
            return skipped;
        }

        @Override // java.io.DataInput
        public int readUnsignedShort() throws IOException {
            this.mPosition += 2;
            if (this.mPosition <= this.mLength) {
                int ch1 = this.mDataInputStream.read();
                int ch2 = this.mDataInputStream.read();
                if ((ch1 | ch2) >= 0) {
                    ByteOrder byteOrder = this.mByteOrder;
                    if (byteOrder == LITTLE_ENDIAN) {
                        return (ch2 << 8) + ch1;
                    }
                    if (byteOrder == BIG_ENDIAN) {
                        return (ch1 << 8) + ch2;
                    }
                    throw new IOException("Invalid byte order: " + this.mByteOrder);
                }
                throw new EOFException();
            }
            throw new EOFException();
        }

        public long readUnsignedInt() throws IOException {
            return ((long) readInt()) & 4294967295L;
        }

        @Override // java.io.DataInput
        public long readLong() throws IOException {
            this.mPosition += 8;
            if (this.mPosition <= this.mLength) {
                int ch1 = this.mDataInputStream.read();
                int ch2 = this.mDataInputStream.read();
                int ch3 = this.mDataInputStream.read();
                int ch4 = this.mDataInputStream.read();
                int ch5 = this.mDataInputStream.read();
                int ch6 = this.mDataInputStream.read();
                int ch7 = this.mDataInputStream.read();
                int ch8 = this.mDataInputStream.read();
                if ((ch1 | ch2 | ch3 | ch4 | ch5 | ch6 | ch7 | ch8) >= 0) {
                    ByteOrder byteOrder = this.mByteOrder;
                    if (byteOrder == LITTLE_ENDIAN) {
                        return (((long) ch8) << 56) + (((long) ch7) << 48) + (((long) ch6) << 40) + (((long) ch5) << 32) + (((long) ch4) << 24) + (((long) ch3) << 16) + (((long) ch2) << 8) + ((long) ch1);
                    }
                    if (byteOrder == BIG_ENDIAN) {
                        return (((long) ch1) << 56) + (((long) ch2) << 48) + (((long) ch3) << 40) + (((long) ch4) << 32) + (((long) ch5) << 24) + (((long) ch6) << 16) + (((long) ch7) << 8) + ((long) ch8);
                    }
                    throw new IOException("Invalid byte order: " + this.mByteOrder);
                }
                throw new EOFException();
            }
            throw new EOFException();
        }

        @Override // java.io.DataInput
        public float readFloat() throws IOException {
            return Float.intBitsToFloat(readInt());
        }

        @Override // java.io.DataInput
        public double readDouble() throws IOException {
            return Double.longBitsToDouble(readLong());
        }

        public int getLength() {
            return this.mLength;
        }
    }

    /* access modifiers changed from: private */
    public static class ByteOrderedDataOutputStream extends FilterOutputStream {
        private ByteOrder mByteOrder;
        private final OutputStream mOutputStream;

        public ByteOrderedDataOutputStream(OutputStream out, ByteOrder byteOrder) {
            super(out);
            this.mOutputStream = out;
            this.mByteOrder = byteOrder;
        }

        public void setByteOrder(ByteOrder byteOrder) {
            this.mByteOrder = byteOrder;
        }

        @Override // java.io.FilterOutputStream, java.io.OutputStream
        public void write(byte[] bytes) throws IOException {
            this.mOutputStream.write(bytes);
        }

        @Override // java.io.FilterOutputStream, java.io.OutputStream
        public void write(byte[] bytes, int offset, int length) throws IOException {
            this.mOutputStream.write(bytes, offset, length);
        }

        public void writeByte(int val) throws IOException {
            this.mOutputStream.write(val);
        }

        public void writeShort(short val) throws IOException {
            if (this.mByteOrder == ByteOrder.LITTLE_ENDIAN) {
                this.mOutputStream.write((val >>> 0) & 255);
                this.mOutputStream.write((val >>> 8) & 255);
            } else if (this.mByteOrder == ByteOrder.BIG_ENDIAN) {
                this.mOutputStream.write((val >>> 8) & 255);
                this.mOutputStream.write((val >>> 0) & 255);
            }
        }

        public void writeInt(int val) throws IOException {
            if (this.mByteOrder == ByteOrder.LITTLE_ENDIAN) {
                this.mOutputStream.write((val >>> 0) & 255);
                this.mOutputStream.write((val >>> 8) & 255);
                this.mOutputStream.write((val >>> 16) & 255);
                this.mOutputStream.write((val >>> 24) & 255);
            } else if (this.mByteOrder == ByteOrder.BIG_ENDIAN) {
                this.mOutputStream.write((val >>> 24) & 255);
                this.mOutputStream.write((val >>> 16) & 255);
                this.mOutputStream.write((val >>> 8) & 255);
                this.mOutputStream.write((val >>> 0) & 255);
            }
        }

        public void writeUnsignedShort(int val) throws IOException {
            writeShort((short) val);
        }

        public void writeUnsignedInt(long val) throws IOException {
            writeInt((int) val);
        }
    }

    private void swapBasedOnImageSize(int firstIfdType, int secondIfdType) throws IOException {
        if (!this.mAttributes[firstIfdType].isEmpty() && !this.mAttributes[secondIfdType].isEmpty()) {
            ExifAttribute firstImageLengthAttribute = (ExifAttribute) this.mAttributes[firstIfdType].get(TAG_IMAGE_LENGTH);
            ExifAttribute firstImageWidthAttribute = (ExifAttribute) this.mAttributes[firstIfdType].get(TAG_IMAGE_WIDTH);
            ExifAttribute secondImageLengthAttribute = (ExifAttribute) this.mAttributes[secondIfdType].get(TAG_IMAGE_LENGTH);
            ExifAttribute secondImageWidthAttribute = (ExifAttribute) this.mAttributes[secondIfdType].get(TAG_IMAGE_WIDTH);
            if (firstImageLengthAttribute == null || firstImageWidthAttribute == null) {
                if (DEBUG) {
                    Log.d(TAG, "First image does not contain valid size information");
                }
            } else if (secondImageLengthAttribute != null && secondImageWidthAttribute != null) {
                int firstImageLengthValue = firstImageLengthAttribute.getIntValue(this.mExifByteOrder);
                int firstImageWidthValue = firstImageWidthAttribute.getIntValue(this.mExifByteOrder);
                int secondImageLengthValue = secondImageLengthAttribute.getIntValue(this.mExifByteOrder);
                int secondImageWidthValue = secondImageWidthAttribute.getIntValue(this.mExifByteOrder);
                if (firstImageLengthValue < secondImageLengthValue && firstImageWidthValue < secondImageWidthValue) {
                    HashMap[] hashMapArr = this.mAttributes;
                    HashMap tempMap = hashMapArr[firstIfdType];
                    hashMapArr[firstIfdType] = hashMapArr[secondIfdType];
                    hashMapArr[secondIfdType] = tempMap;
                }
            } else if (DEBUG) {
                Log.d(TAG, "Second image does not contain valid size information");
            }
        } else if (DEBUG) {
            Log.d(TAG, "Cannot perform swap since only one image data exists");
        }
    }

    private boolean containsMatch(byte[] mainBytes, byte[] findBytes) {
        int i = 0;
        while (i < mainBytes.length - findBytes.length) {
            int j = 0;
            while (j < findBytes.length && mainBytes[i + j] == findBytes[j]) {
                if (j == findBytes.length - 1) {
                    return true;
                }
                j++;
            }
            i++;
        }
        return false;
    }

    private static long[] convertToLongArray(Object inputObj) {
        if (inputObj instanceof int[]) {
            int[] input = (int[]) inputObj;
            long[] result = new long[input.length];
            for (int i = 0; i < input.length; i++) {
                result[i] = (long) input[i];
            }
            return result;
        } else if (inputObj instanceof long[]) {
            return (long[]) inputObj;
        } else {
            return null;
        }
    }
}
