package android.media;

import android.R;
import android.bluetooth.BluetoothClass.Device;
import android.content.res.AssetManager.AssetInputStream;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.hardware.radio.RadioManager;
import android.mtp.MtpConstants;
import android.net.ConnectivityManager.PacketKeepalive;
import android.net.ProxyInfo;
import android.net.wifi.hotspot2.pps.UpdateParameter;
import android.opengl.GLES30;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.util.Pair;
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.io.IoUtils;
import libcore.io.Streams;

public class ExifInterface {
    private static final Charset ASCII = Charset.forName("US-ASCII");
    private static final int[] BITS_PER_SAMPLE_GREYSCALE_1 = new int[]{4};
    private static final int[] BITS_PER_SAMPLE_GREYSCALE_2 = new int[]{8};
    private static final int[] BITS_PER_SAMPLE_RGB = new int[]{8, 8, 8};
    private static final short BYTE_ALIGN_II = (short) 18761;
    private static final short BYTE_ALIGN_MM = (short) 19789;
    private static final int DATA_DEFLATE_ZIP = 8;
    private static final int DATA_HUFFMAN_COMPRESSED = 2;
    private static final int DATA_JPEG = 6;
    private static final int DATA_JPEG_COMPRESSED = 7;
    private static final int DATA_LOSSY_JPEG = 34892;
    private static final int DATA_PACK_BITS_COMPRESSED = 32773;
    private static final int DATA_UNCOMPRESSED = 1;
    private static final boolean DEBUG = false;
    private static final byte[] EXIF_ASCII_PREFIX = new byte[]{(byte) 65, (byte) 83, (byte) 67, (byte) 73, (byte) 73, (byte) 0, (byte) 0, (byte) 0};
    private static final ExifTag[] EXIF_POINTER_TAGS = new ExifTag[]{new ExifTag(TAG_SUB_IFD_POINTER, 330, 4, null), new ExifTag(TAG_EXIF_IFD_POINTER, 34665, 4, null), new ExifTag(TAG_GPS_INFO_IFD_POINTER, (int) GLES30.GL_DRAW_BUFFER0, 4, null), new ExifTag(TAG_INTEROPERABILITY_IFD_POINTER, 40965, 4, null), new ExifTag(TAG_ORF_CAMERA_SETTINGS_IFD_POINTER, (int) MtpConstants.RESPONSE_SPECIFICATION_OF_DESTINATION_UNSUPPORTED, 1, null), new ExifTag(TAG_ORF_IMAGE_PROCESSING_IFD_POINTER, 8256, 1, null)};
    private static final ExifTag[][] EXIF_TAGS = new ExifTag[][]{IFD_TIFF_TAGS, IFD_EXIF_TAGS, IFD_GPS_TAGS, IFD_INTEROPERABILITY_TAGS, IFD_THUMBNAIL_TAGS, IFD_TIFF_TAGS, ORF_MAKER_NOTE_TAGS, ORF_CAMERA_SETTINGS_TAGS, ORF_IMAGE_PROCESSING_TAGS, PEF_TAGS};
    private static final byte[] IDENTIFIER_EXIF_APP1 = "Exif\u0000\u0000".getBytes(ASCII);
    private static final ExifTag[] IFD_EXIF_TAGS = new ExifTag[]{new ExifTag(TAG_EXPOSURE_TIME, 33434, 5, null), new ExifTag("FNumber", 33437, 5, null), new ExifTag(TAG_EXPOSURE_PROGRAM, 34850, 3, null), new ExifTag(TAG_SPECTRAL_SENSITIVITY, (int) GLES30.GL_MAX_DRAW_BUFFERS, 2, null), new ExifTag("ISOSpeedRatings", (int) GLES30.GL_DRAW_BUFFER2, 3, null), new ExifTag(TAG_OECF, (int) GLES30.GL_DRAW_BUFFER3, 7, null), new ExifTag(TAG_EXIF_VERSION, 36864, 2, null), new ExifTag(TAG_DATETIME_ORIGINAL, 36867, 2, null), new ExifTag(TAG_DATETIME_DIGITIZED, 36868, 2, null), new ExifTag(TAG_COMPONENTS_CONFIGURATION, 37121, 7, null), new ExifTag(TAG_COMPRESSED_BITS_PER_PIXEL, 37122, 5, null), new ExifTag(TAG_SHUTTER_SPEED_VALUE, 37377, 10, null), new ExifTag(TAG_APERTURE_VALUE, 37378, 5, null), new ExifTag(TAG_BRIGHTNESS_VALUE, 37379, 10, null), new ExifTag(TAG_EXPOSURE_BIAS_VALUE, 37380, 10, null), new ExifTag(TAG_MAX_APERTURE_VALUE, 37381, 5, null), new ExifTag(TAG_SUBJECT_DISTANCE, 37382, 5, null), new ExifTag(TAG_METERING_MODE, 37383, 3, null), new ExifTag(TAG_LIGHT_SOURCE, 37384, 3, null), new ExifTag(TAG_FLASH, 37385, 3, null), new ExifTag(TAG_FOCAL_LENGTH, 37386, 5, null), new ExifTag(TAG_SUBJECT_AREA, 37396, 3, null), new ExifTag(TAG_MAKER_NOTE, 37500, 7, null), new ExifTag(TAG_USER_COMMENT, 37510, 7, null), new ExifTag(TAG_SUBSEC_TIME, 37520, 2, null), new ExifTag("SubSecTimeOriginal", 37521, 2, null), new ExifTag("SubSecTimeDigitized", 37522, 2, null), new ExifTag(TAG_FLASHPIX_VERSION, 40960, 7, null), new ExifTag(TAG_COLOR_SPACE, 40961, 3, null), new ExifTag(TAG_PIXEL_X_DIMENSION, 40962, 3, 4, null), new ExifTag(TAG_PIXEL_Y_DIMENSION, 40963, 3, 4, null), new ExifTag(TAG_RELATED_SOUND_FILE, 40964, 2, null), new ExifTag(TAG_INTEROPERABILITY_IFD_POINTER, 40965, 4, null), new ExifTag(TAG_FLASH_ENERGY, 41483, 5, null), new ExifTag(TAG_SPATIAL_FREQUENCY_RESPONSE, 41484, 7, null), new ExifTag(TAG_FOCAL_PLANE_X_RESOLUTION, 41486, 5, null), new ExifTag(TAG_FOCAL_PLANE_Y_RESOLUTION, 41487, 5, null), new ExifTag(TAG_FOCAL_PLANE_RESOLUTION_UNIT, 41488, 3, null), new ExifTag(TAG_SUBJECT_LOCATION, 41492, 3, null), new ExifTag(TAG_EXPOSURE_INDEX, 41493, 5, null), new ExifTag(TAG_SENSING_METHOD, 41495, 3, null), new ExifTag(TAG_FILE_SOURCE, 41728, 7, null), new ExifTag(TAG_SCENE_TYPE, 41729, 7, null), new ExifTag(TAG_CFA_PATTERN, 41730, 7, null), new ExifTag(TAG_CUSTOM_RENDERED, 41985, 3, null), new ExifTag(TAG_EXPOSURE_MODE, 41986, 3, null), new ExifTag(TAG_WHITE_BALANCE, 41987, 3, null), new ExifTag(TAG_DIGITAL_ZOOM_RATIO, 41988, 5, null), new ExifTag(TAG_FOCAL_LENGTH_IN_35MM_FILM, 41989, 3, null), new ExifTag(TAG_SCENE_CAPTURE_TYPE, 41990, 3, null), new ExifTag(TAG_GAIN_CONTROL, 41991, 3, null), new ExifTag(TAG_CONTRAST, 41992, 3, null), new ExifTag(TAG_SATURATION, 41993, 3, null), new ExifTag(TAG_SHARPNESS, 41994, 3, null), new ExifTag(TAG_DEVICE_SETTING_DESCRIPTION, 41995, 7, null), new ExifTag(TAG_SUBJECT_DISTANCE_RANGE, 41996, 3, null), new ExifTag(TAG_IMAGE_UNIQUE_ID, 42016, 2, null), new ExifTag(TAG_DNG_VERSION, 50706, 1, null), new ExifTag(TAG_DEFAULT_CROP_SIZE, 50720, 3, 4, null)};
    private static final int IFD_FORMAT_BYTE = 1;
    private static final int[] IFD_FORMAT_BYTES_PER_FORMAT = new int[]{0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8, 1};
    private static final int IFD_FORMAT_DOUBLE = 12;
    private static final int IFD_FORMAT_IFD = 13;
    private static final String[] IFD_FORMAT_NAMES = new String[]{ProxyInfo.LOCAL_EXCL_LIST, "BYTE", "STRING", "USHORT", "ULONG", "URATIONAL", "SBYTE", "UNDEFINED", "SSHORT", "SLONG", "SRATIONAL", "SINGLE", "DOUBLE"};
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
    private static final ExifTag[] IFD_GPS_TAGS = new ExifTag[]{new ExifTag(TAG_GPS_VERSION_ID, 0, 1, null), new ExifTag(TAG_GPS_LATITUDE_REF, 1, 2, null), new ExifTag(TAG_GPS_LATITUDE, 2, 5, null), new ExifTag(TAG_GPS_LONGITUDE_REF, 3, 2, null), new ExifTag(TAG_GPS_LONGITUDE, 4, 5, null), new ExifTag(TAG_GPS_ALTITUDE_REF, 5, 1, null), new ExifTag(TAG_GPS_ALTITUDE, 6, 5, null), new ExifTag(TAG_GPS_TIMESTAMP, 7, 5, null), new ExifTag(TAG_GPS_SATELLITES, 8, 2, null), new ExifTag(TAG_GPS_STATUS, 9, 2, null), new ExifTag(TAG_GPS_MEASURE_MODE, 10, 2, null), new ExifTag(TAG_GPS_DOP, 11, 5, null), new ExifTag(TAG_GPS_SPEED_REF, 12, 2, null), new ExifTag(TAG_GPS_SPEED, 13, 5, null), new ExifTag(TAG_GPS_TRACK_REF, 14, 2, null), new ExifTag(TAG_GPS_TRACK, 15, 5, null), new ExifTag(TAG_GPS_IMG_DIRECTION_REF, 16, 2, null), new ExifTag(TAG_GPS_IMG_DIRECTION, 17, 5, null), new ExifTag(TAG_GPS_MAP_DATUM, 18, 2, null), new ExifTag(TAG_GPS_DEST_LATITUDE_REF, 19, 2, null), new ExifTag(TAG_GPS_DEST_LATITUDE, 20, 5, null), new ExifTag(TAG_GPS_DEST_LONGITUDE_REF, 21, 2, null), new ExifTag(TAG_GPS_DEST_LONGITUDE, 22, 5, null), new ExifTag(TAG_GPS_DEST_BEARING_REF, 23, 2, null), new ExifTag(TAG_GPS_DEST_BEARING, 24, 5, null), new ExifTag(TAG_GPS_DEST_DISTANCE_REF, 25, 2, null), new ExifTag(TAG_GPS_DEST_DISTANCE, 26, 5, null), new ExifTag(TAG_GPS_PROCESSING_METHOD, 27, 7, null), new ExifTag(TAG_GPS_AREA_INFORMATION, 28, 7, null), new ExifTag(TAG_GPS_DATESTAMP, 29, 2, null), new ExifTag(TAG_GPS_DIFFERENTIAL, 30, 3, null)};
    private static final ExifTag[] IFD_INTEROPERABILITY_TAGS = new ExifTag[]{new ExifTag(TAG_INTEROPERABILITY_INDEX, 1, 2, null)};
    private static final int IFD_OFFSET = 8;
    private static final ExifTag[] IFD_THUMBNAIL_TAGS = new ExifTag[]{new ExifTag(TAG_NEW_SUBFILE_TYPE, 254, 4, null), new ExifTag(TAG_SUBFILE_TYPE, 255, 4, null), new ExifTag(TAG_THUMBNAIL_IMAGE_WIDTH, 256, 3, 4, null), new ExifTag(TAG_THUMBNAIL_IMAGE_LENGTH, 257, 3, 4, null), new ExifTag(TAG_BITS_PER_SAMPLE, 258, 3, null), new ExifTag(TAG_COMPRESSION, 259, 3, null), new ExifTag(TAG_PHOTOMETRIC_INTERPRETATION, 262, 3, null), new ExifTag(TAG_IMAGE_DESCRIPTION, 270, 2, null), new ExifTag(TAG_MAKE, 271, 2, null), new ExifTag(TAG_MODEL, 272, 2, null), new ExifTag(TAG_STRIP_OFFSETS, R.styleable.Theme_primaryContentAlpha, 3, 4, null), new ExifTag(TAG_ORIENTATION, (int) R.styleable.Theme_secondaryContentAlpha, 3, null), new ExifTag(TAG_SAMPLES_PER_PIXEL, 277, 3, null), new ExifTag(TAG_ROWS_PER_STRIP, 278, 3, 4, null), new ExifTag(TAG_STRIP_BYTE_COUNTS, 279, 3, 4, null), new ExifTag(TAG_X_RESOLUTION, 282, 5, null), new ExifTag(TAG_Y_RESOLUTION, 283, 5, null), new ExifTag(TAG_PLANAR_CONFIGURATION, 284, 3, null), new ExifTag(TAG_RESOLUTION_UNIT, 296, 3, null), new ExifTag(TAG_TRANSFER_FUNCTION, (int) MediaFile.FILE_TYPE_CR2, 3, null), new ExifTag(TAG_SOFTWARE, (int) MediaFile.FILE_TYPE_RW2, 2, null), new ExifTag(TAG_DATETIME, (int) MediaFile.FILE_TYPE_ORF, 2, null), new ExifTag(TAG_ARTIST, 315, 2, null), new ExifTag(TAG_WHITE_POINT, 318, 5, null), new ExifTag(TAG_PRIMARY_CHROMATICITIES, 319, 5, null), new ExifTag(TAG_SUB_IFD_POINTER, 330, 4, null), new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT, 513, 4, null), new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, 514, 4, null), new ExifTag(TAG_Y_CB_CR_COEFFICIENTS, 529, 5, null), new ExifTag(TAG_Y_CB_CR_SUB_SAMPLING, 530, 3, null), new ExifTag(TAG_Y_CB_CR_POSITIONING, 531, 3, null), new ExifTag(TAG_REFERENCE_BLACK_WHITE, (int) Device.PHONE_ISDN, 5, null), new ExifTag(TAG_COPYRIGHT, 33432, 2, null), new ExifTag(TAG_EXIF_IFD_POINTER, 34665, 4, null), new ExifTag(TAG_GPS_INFO_IFD_POINTER, (int) GLES30.GL_DRAW_BUFFER0, 4, null), new ExifTag(TAG_DNG_VERSION, 50706, 1, null), new ExifTag(TAG_DEFAULT_CROP_SIZE, 50720, 3, 4, null)};
    private static final ExifTag[] IFD_TIFF_TAGS = new ExifTag[]{new ExifTag(TAG_NEW_SUBFILE_TYPE, 254, 4, null), new ExifTag(TAG_SUBFILE_TYPE, 255, 4, null), new ExifTag(TAG_IMAGE_WIDTH, 256, 3, 4, null), new ExifTag(TAG_IMAGE_LENGTH, 257, 3, 4, null), new ExifTag(TAG_BITS_PER_SAMPLE, 258, 3, null), new ExifTag(TAG_COMPRESSION, 259, 3, null), new ExifTag(TAG_PHOTOMETRIC_INTERPRETATION, 262, 3, null), new ExifTag(TAG_IMAGE_DESCRIPTION, 270, 2, null), new ExifTag(TAG_MAKE, 271, 2, null), new ExifTag(TAG_MODEL, 272, 2, null), new ExifTag(TAG_STRIP_OFFSETS, R.styleable.Theme_primaryContentAlpha, 3, 4, null), new ExifTag(TAG_ORIENTATION, (int) R.styleable.Theme_secondaryContentAlpha, 3, null), new ExifTag(TAG_SAMPLES_PER_PIXEL, 277, 3, null), new ExifTag(TAG_ROWS_PER_STRIP, 278, 3, 4, null), new ExifTag(TAG_STRIP_BYTE_COUNTS, 279, 3, 4, null), new ExifTag(TAG_X_RESOLUTION, 282, 5, null), new ExifTag(TAG_Y_RESOLUTION, 283, 5, null), new ExifTag(TAG_PLANAR_CONFIGURATION, 284, 3, null), new ExifTag(TAG_RESOLUTION_UNIT, 296, 3, null), new ExifTag(TAG_TRANSFER_FUNCTION, (int) MediaFile.FILE_TYPE_CR2, 3, null), new ExifTag(TAG_SOFTWARE, (int) MediaFile.FILE_TYPE_RW2, 2, null), new ExifTag(TAG_DATETIME, (int) MediaFile.FILE_TYPE_ORF, 2, null), new ExifTag(TAG_ARTIST, 315, 2, null), new ExifTag(TAG_WHITE_POINT, 318, 5, null), new ExifTag(TAG_PRIMARY_CHROMATICITIES, 319, 5, null), new ExifTag(TAG_SUB_IFD_POINTER, 330, 4, null), new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT, 513, 4, null), new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, 514, 4, null), new ExifTag(TAG_Y_CB_CR_COEFFICIENTS, 529, 5, null), new ExifTag(TAG_Y_CB_CR_SUB_SAMPLING, 530, 3, null), new ExifTag(TAG_Y_CB_CR_POSITIONING, 531, 3, null), new ExifTag(TAG_REFERENCE_BLACK_WHITE, (int) Device.PHONE_ISDN, 5, null), new ExifTag(TAG_COPYRIGHT, 33432, 2, null), new ExifTag(TAG_EXIF_IFD_POINTER, 34665, 4, null), new ExifTag(TAG_GPS_INFO_IFD_POINTER, (int) GLES30.GL_DRAW_BUFFER0, 4, null), new ExifTag(TAG_RW2_SENSOR_TOP_BORDER, 4, 4, null), new ExifTag(TAG_RW2_SENSOR_LEFT_BORDER, 5, 4, null), new ExifTag(TAG_RW2_SENSOR_BOTTOM_BORDER, 6, 4, null), new ExifTag(TAG_RW2_SENSOR_RIGHT_BORDER, 7, 4, null), new ExifTag(TAG_RW2_ISO, 23, 3, null), new ExifTag(TAG_RW2_JPG_FROM_RAW, 46, 7, null)};
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
    private static final int IMAGE_TYPE_JPEG = 4;
    private static final int IMAGE_TYPE_NEF = 5;
    private static final int IMAGE_TYPE_NRW = 6;
    private static final int IMAGE_TYPE_ORF = 7;
    private static final int IMAGE_TYPE_PEF = 8;
    private static final int IMAGE_TYPE_RAF = 9;
    private static final int IMAGE_TYPE_RW2 = 10;
    private static final int IMAGE_TYPE_SRW = 11;
    private static final int IMAGE_TYPE_UNKNOWN = 0;
    private static final ExifTag JPEG_INTERCHANGE_FORMAT_LENGTH_TAG = new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, 514, 4, null);
    private static final ExifTag JPEG_INTERCHANGE_FORMAT_TAG = new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT, 513, 4, null);
    private static final byte[] JPEG_SIGNATURE = new byte[]{(byte) -1, MARKER_SOI, (byte) -1};
    private static final byte MARKER = (byte) -1;
    private static final byte MARKER_APP1 = (byte) -31;
    private static final byte MARKER_COM = (byte) -2;
    private static final byte MARKER_EOI = (byte) -39;
    private static final byte MARKER_SOF0 = (byte) -64;
    private static final byte MARKER_SOF1 = (byte) -63;
    private static final byte MARKER_SOF10 = (byte) -54;
    private static final byte MARKER_SOF11 = (byte) -53;
    private static final byte MARKER_SOF13 = (byte) -51;
    private static final byte MARKER_SOF14 = (byte) -50;
    private static final byte MARKER_SOF15 = (byte) -49;
    private static final byte MARKER_SOF2 = (byte) -62;
    private static final byte MARKER_SOF3 = (byte) -61;
    private static final byte MARKER_SOF5 = (byte) -59;
    private static final byte MARKER_SOF6 = (byte) -58;
    private static final byte MARKER_SOF7 = (byte) -57;
    private static final byte MARKER_SOF9 = (byte) -55;
    private static final byte MARKER_SOI = (byte) -40;
    private static final byte MARKER_SOS = (byte) -38;
    private static final int MAX_THUMBNAIL_SIZE = 512;
    private static final ExifTag[] ORF_CAMERA_SETTINGS_TAGS = new ExifTag[]{new ExifTag(TAG_ORF_PREVIEW_IMAGE_START, 257, 4, null), new ExifTag(TAG_ORF_PREVIEW_IMAGE_LENGTH, 258, 4, null)};
    private static final ExifTag[] ORF_IMAGE_PROCESSING_TAGS = new ExifTag[]{new ExifTag(TAG_ORF_ASPECT_FRAME, 4371, 3, null)};
    private static final byte[] ORF_MAKER_NOTE_HEADER_1 = new byte[]{(byte) 79, (byte) 76, (byte) 89, (byte) 77, (byte) 80, (byte) 0};
    private static final int ORF_MAKER_NOTE_HEADER_1_SIZE = 8;
    private static final byte[] ORF_MAKER_NOTE_HEADER_2 = new byte[]{(byte) 79, (byte) 76, (byte) 89, (byte) 77, (byte) 80, (byte) 85, (byte) 83, (byte) 0, (byte) 73, (byte) 73};
    private static final int ORF_MAKER_NOTE_HEADER_2_SIZE = 12;
    private static final ExifTag[] ORF_MAKER_NOTE_TAGS = new ExifTag[]{new ExifTag(TAG_ORF_THUMBNAIL_IMAGE, 256, 7, null), new ExifTag(TAG_ORF_CAMERA_SETTINGS_IFD_POINTER, (int) MtpConstants.RESPONSE_SPECIFICATION_OF_DESTINATION_UNSUPPORTED, 4, null), new ExifTag(TAG_ORF_IMAGE_PROCESSING_IFD_POINTER, 8256, 4, null)};
    private static final short ORF_SIGNATURE_1 = (short) 20306;
    private static final short ORF_SIGNATURE_2 = (short) 21330;
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
    private static final int PEF_MAKER_NOTE_SKIP_SIZE = 6;
    private static final String PEF_SIGNATURE = "PENTAX";
    private static final ExifTag[] PEF_TAGS = new ExifTag[]{new ExifTag(TAG_COLOR_SPACE, 55, 3, null)};
    private static final int PHOTOMETRIC_INTERPRETATION_BLACK_IS_ZERO = 1;
    private static final int PHOTOMETRIC_INTERPRETATION_RGB = 2;
    private static final int PHOTOMETRIC_INTERPRETATION_WHITE_IS_ZERO = 0;
    private static final int PHOTOMETRIC_INTERPRETATION_YCBCR = 6;
    private static final int RAF_INFO_SIZE = 160;
    private static final int RAF_JPEG_LENGTH_VALUE_SIZE = 4;
    private static final int RAF_OFFSET_TO_JPEG_IMAGE_OFFSET = 84;
    private static final String RAF_SIGNATURE = "FUJIFILMCCD-RAW";
    private static final int REDUCED_RESOLUTION_IMAGE = 1;
    private static final short RW2_SIGNATURE = (short) 85;
    private static final int SIGNATURE_CHECK_SIZE = 5000;
    private static final byte START_CODE = (byte) 42;
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
    private static final ExifTag TAG_RAF_IMAGE_SIZE = new ExifTag(TAG_STRIP_OFFSETS, (int) R.styleable.Theme_primaryContentAlpha, 3, null);
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
    public static final String TAG_X_RESOLUTION = "XResolution";
    public static final String TAG_Y_CB_CR_COEFFICIENTS = "YCbCrCoefficients";
    public static final String TAG_Y_CB_CR_POSITIONING = "YCbCrPositioning";
    public static final String TAG_Y_CB_CR_SUB_SAMPLING = "YCbCrSubSampling";
    public static final String TAG_Y_RESOLUTION = "YResolution";
    public static final int WHITEBALANCE_AUTO = 0;
    public static final int WHITEBALANCE_MANUAL = 1;
    private static final String[] patterns = new String[]{"yyyy:MM:dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy.MM.dd HH:mm:ss"};
    private static final HashMap sExifPointerTagMap = new HashMap();
    private static final HashMap[] sExifTagMapsForReading = new HashMap[EXIF_TAGS.length];
    private static final HashMap[] sExifTagMapsForWriting = new HashMap[EXIF_TAGS.length];
    private static SimpleDateFormat sFormatter;
    private static SimpleDateFormat[] sFormatters = new SimpleDateFormat[patterns.length];
    private static final Pattern sGpsTimestampPattern = Pattern.compile("^([0-9][0-9]):([0-9][0-9]):([0-9][0-9])$");
    private static final Pattern sNonZeroTimePattern = Pattern.compile(".*[1-9].*");
    private static final HashSet<String> sTagSetForCompatibility = new HashSet(Arrays.asList(new String[]{"FNumber", TAG_DIGITAL_ZOOM_RATIO, TAG_EXPOSURE_TIME, TAG_SUBJECT_DISTANCE, TAG_GPS_TIMESTAMP}));
    private final AssetInputStream mAssetInputStream;
    private final HashMap[] mAttributes;
    private ByteOrder mExifByteOrder;
    private int mExifOffset;
    private final String mFilename;
    private boolean mHasThumbnail;
    private final boolean mIsInputStream;
    private boolean mIsSupportedFile;
    private int mMimeType;
    private int mOrfMakerNoteOffset;
    private int mOrfThumbnailLength;
    private int mOrfThumbnailOffset;
    private int mRw2JpgFromRawOffset;
    private final FileDescriptor mSeekableFileDescriptor;
    private byte[] mThumbnailBytes;
    private int mThumbnailCompression;
    private int mThumbnailLength;
    private int mThumbnailOffset;

    private static class ByteOrderedDataInputStream extends InputStream implements DataInput {
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
            if (((long) this.mPosition) > byteCount) {
                this.mPosition = 0;
                this.mDataInputStream.reset();
                this.mDataInputStream.mark(this.mLength);
            } else {
                byteCount -= (long) this.mPosition;
            }
            if (skipBytes((int) byteCount) != ((int) byteCount)) {
                throw new IOException("Couldn't seek up to the byteCount");
            }
        }

        public int peek() {
            return this.mPosition;
        }

        public int available() throws IOException {
            return this.mDataInputStream.available();
        }

        public int read() throws IOException {
            this.mPosition++;
            return this.mDataInputStream.read();
        }

        public int readUnsignedByte() throws IOException {
            this.mPosition++;
            return this.mDataInputStream.readUnsignedByte();
        }

        public String readLine() throws IOException {
            Log.d(ExifInterface.TAG, "Currently unsupported");
            return null;
        }

        public boolean readBoolean() throws IOException {
            this.mPosition++;
            return this.mDataInputStream.readBoolean();
        }

        public char readChar() throws IOException {
            this.mPosition += 2;
            return this.mDataInputStream.readChar();
        }

        public String readUTF() throws IOException {
            this.mPosition += 2;
            return this.mDataInputStream.readUTF();
        }

        public void readFully(byte[] buffer, int offset, int length) throws IOException {
            this.mPosition += length;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            } else if (this.mDataInputStream.read(buffer, offset, length) != length) {
                throw new IOException("Couldn't read up to the length of buffer");
            }
        }

        public void readFully(byte[] buffer) throws IOException {
            this.mPosition += buffer.length;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            } else if (this.mDataInputStream.read(buffer, 0, buffer.length) != buffer.length) {
                throw new IOException("Couldn't read up to the length of buffer");
            }
        }

        public byte readByte() throws IOException {
            this.mPosition++;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            }
            int ch = this.mDataInputStream.read();
            if (ch >= 0) {
                return (byte) ch;
            }
            throw new EOFException();
        }

        public short readShort() throws IOException {
            this.mPosition += 2;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            }
            int ch1 = this.mDataInputStream.read();
            int ch2 = this.mDataInputStream.read();
            if ((ch1 | ch2) < 0) {
                throw new EOFException();
            } else if (this.mByteOrder == LITTLE_ENDIAN) {
                return (short) ((ch2 << 8) + ch1);
            } else {
                if (this.mByteOrder == BIG_ENDIAN) {
                    return (short) ((ch1 << 8) + ch2);
                }
                throw new IOException("Invalid byte order: " + this.mByteOrder);
            }
        }

        public int readInt() throws IOException {
            this.mPosition += 4;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            }
            int ch1 = this.mDataInputStream.read();
            int ch2 = this.mDataInputStream.read();
            int ch3 = this.mDataInputStream.read();
            int ch4 = this.mDataInputStream.read();
            if ((((ch1 | ch2) | ch3) | ch4) < 0) {
                throw new EOFException();
            } else if (this.mByteOrder == LITTLE_ENDIAN) {
                return (((ch4 << 24) + (ch3 << 16)) + (ch2 << 8)) + ch1;
            } else {
                if (this.mByteOrder == BIG_ENDIAN) {
                    return (((ch1 << 24) + (ch2 << 16)) + (ch3 << 8)) + ch4;
                }
                throw new IOException("Invalid byte order: " + this.mByteOrder);
            }
        }

        public int skipBytes(int byteCount) throws IOException {
            int totalSkip = Math.min(byteCount, this.mLength - this.mPosition);
            int skipped = 0;
            while (skipped < totalSkip) {
                skipped += this.mDataInputStream.skipBytes(totalSkip - skipped);
            }
            this.mPosition += skipped;
            return skipped;
        }

        public int readUnsignedShort() throws IOException {
            this.mPosition += 2;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            }
            int ch1 = this.mDataInputStream.read();
            int ch2 = this.mDataInputStream.read();
            if ((ch1 | ch2) < 0) {
                throw new EOFException();
            } else if (this.mByteOrder == LITTLE_ENDIAN) {
                return (ch2 << 8) + ch1;
            } else {
                if (this.mByteOrder == BIG_ENDIAN) {
                    return (ch1 << 8) + ch2;
                }
                throw new IOException("Invalid byte order: " + this.mByteOrder);
            }
        }

        public long readUnsignedInt() throws IOException {
            return ((long) readInt()) & UpdateParameter.UPDATE_CHECK_INTERVAL_NEVER;
        }

        public long readLong() throws IOException {
            this.mPosition += 8;
            if (this.mPosition > this.mLength) {
                throw new EOFException();
            }
            int ch1 = this.mDataInputStream.read();
            int ch2 = this.mDataInputStream.read();
            int ch3 = this.mDataInputStream.read();
            int ch4 = this.mDataInputStream.read();
            int ch5 = this.mDataInputStream.read();
            int ch6 = this.mDataInputStream.read();
            int ch7 = this.mDataInputStream.read();
            int ch8 = this.mDataInputStream.read();
            if ((((((((ch1 | ch2) | ch3) | ch4) | ch5) | ch6) | ch7) | ch8) < 0) {
                throw new EOFException();
            } else if (this.mByteOrder == LITTLE_ENDIAN) {
                return (((((((((long) ch8) << 56) + (((long) ch7) << 48)) + (((long) ch6) << 40)) + (((long) ch5) << 32)) + (((long) ch4) << 24)) + (((long) ch3) << 16)) + (((long) ch2) << 8)) + ((long) ch1);
            } else {
                if (this.mByteOrder == BIG_ENDIAN) {
                    return (((((((((long) ch1) << 56) + (((long) ch2) << 48)) + (((long) ch3) << 40)) + (((long) ch4) << 32)) + (((long) ch5) << 24)) + (((long) ch6) << 16)) + (((long) ch7) << 8)) + ((long) ch8);
                }
                throw new IOException("Invalid byte order: " + this.mByteOrder);
            }
        }

        public float readFloat() throws IOException {
            return Float.intBitsToFloat(readInt());
        }

        public double readDouble() throws IOException {
            return Double.longBitsToDouble(readLong());
        }
    }

    private static class ByteOrderedDataOutputStream extends FilterOutputStream {
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

        public void write(byte[] bytes) throws IOException {
            this.mOutputStream.write(bytes);
        }

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

    private static class ExifAttribute {
        public final byte[] bytes;
        public final int format;
        public final int numberOfComponents;

        /* synthetic */ ExifAttribute(int format, int numberOfComponents, byte[] bytes, ExifAttribute -this3) {
            this(format, numberOfComponents, bytes);
        }

        private ExifAttribute(int format, int numberOfComponents, byte[] bytes) {
            this.format = format;
            this.numberOfComponents = numberOfComponents;
            this.bytes = bytes;
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
            byte[] bytes = new byte[]{(byte) (value.charAt(0) - 48)};
            return new ExifAttribute(1, bytes.length, bytes);
        }

        public static ExifAttribute createString(String value) {
            byte[] ascii = (value + 0).getBytes(ExifInterface.ASCII);
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

        /* JADX WARNING: Removed duplicated region for block: B:24:0x0086 A:{Catch:{ IOException -> 0x00b7 }} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private Object getValue(ByteOrder byteOrder) {
            try {
                ByteOrderedDataInputStream inputStream = new ByteOrderedDataInputStream(this.bytes);
                inputStream.setByteOrder(byteOrder);
                int i;
                int[] values;
                Rational[] values2;
                double[] values3;
                switch (this.format) {
                    case 1:
                    case 6:
                        if (this.bytes.length != 1 || this.bytes[0] < (byte) 0 || this.bytes[0] > (byte) 1) {
                            return new String(this.bytes, ExifInterface.ASCII);
                        }
                        return new String(new char[]{(char) (this.bytes[0] + 48)});
                    case 2:
                    case 7:
                        int index = 0;
                        if (this.numberOfComponents >= ExifInterface.EXIF_ASCII_PREFIX.length) {
                            boolean same = true;
                            i = 0;
                            while (i < ExifInterface.EXIF_ASCII_PREFIX.length) {
                                if (this.bytes[i] != ExifInterface.EXIF_ASCII_PREFIX[i]) {
                                    same = false;
                                    if (same) {
                                        index = ExifInterface.EXIF_ASCII_PREFIX.length;
                                    }
                                } else {
                                    i++;
                                }
                            }
                            if (same) {
                            }
                        }
                        StringBuilder stringBuilder = new StringBuilder();
                        for (index = 
/*
Method generation error in method: android.media.ExifInterface.ExifAttribute.getValue(java.nio.ByteOrder):java.lang.Object, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r12_2 'index' int) = (r12_0 'index' int), (r12_0 'index' int), (r12_1 'index' int) binds: {(r12_0 'index' int)=B:16:0x006a, (r12_0 'index' int)=B:23:0x0084, (r12_1 'index' int)=B:24:0x0086} in method: android.media.ExifInterface.ExifAttribute.getValue(java.nio.ByteOrder):java.lang.Object, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:183)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeSwitch(RegionGen.java:265)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:59)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:278)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:322)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:260)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:222)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:235)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:221)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 28 more

*/

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
            int i;
            if (value instanceof long[]) {
                long[] array = (long[]) value;
                for (i = 0; i < array.length; i++) {
                    stringBuilder.append(array[i]);
                    if (i + 1 != array.length) {
                        stringBuilder.append(",");
                    }
                }
                return stringBuilder.toString();
            } else if (value instanceof int[]) {
                int[] array2 = (int[]) value;
                for (i = 0; i < array2.length; i++) {
                    stringBuilder.append(array2[i]);
                    if (i + 1 != array2.length) {
                        stringBuilder.append(",");
                    }
                }
                return stringBuilder.toString();
            } else if (value instanceof double[]) {
                double[] array3 = (double[]) value;
                for (i = 0; i < array3.length; i++) {
                    stringBuilder.append(array3[i]);
                    if (i + 1 != array3.length) {
                        stringBuilder.append(",");
                    }
                }
                return stringBuilder.toString();
            } else if (!(value instanceof Rational[])) {
                return null;
            } else {
                Rational[] array4 = (Rational[]) value;
                for (i = 0; i < array4.length; i++) {
                    stringBuilder.append(array4[i].numerator);
                    stringBuilder.append('/');
                    stringBuilder.append(array4[i].denominator);
                    if (i + 1 != array4.length) {
                        stringBuilder.append(",");
                    }
                }
                return stringBuilder.toString();
            }
        }

        public int size() {
            return ExifInterface.IFD_FORMAT_BYTES_PER_FORMAT[this.format] * this.numberOfComponents;
        }
    }

    private static class ExifTag {
        public final String name;
        public final int number;
        public final int primaryFormat;
        public final int secondaryFormat;

        /* synthetic */ ExifTag(String name, int number, int primaryFormat, int secondaryFormat, ExifTag -this4) {
            this(name, number, primaryFormat, secondaryFormat);
        }

        private ExifTag(String name, int number, int format) {
            this.name = name;
            this.number = number;
            this.primaryFormat = format;
            this.secondaryFormat = -1;
        }

        private ExifTag(String name, int number, int primaryFormat, int secondaryFormat) {
            this.name = name;
            this.number = number;
            this.primaryFormat = primaryFormat;
            this.secondaryFormat = secondaryFormat;
        }
    }

    private static class Rational {
        public final long denominator;
        public final long numerator;

        /* synthetic */ Rational(long numerator, long denominator, Rational -this2) {
            this(numerator, denominator);
        }

        private Rational(long numerator, long denominator) {
            if (denominator == 0) {
                this.numerator = 0;
                this.denominator = 1;
                return;
            }
            this.numerator = numerator;
            this.denominator = denominator;
        }

        public String toString() {
            return this.numerator + "/" + this.denominator;
        }

        public double calculate() {
            return ((double) this.numerator) / ((double) this.denominator);
        }
    }

    static {
        for (int i = 0; i < patterns.length; i++) {
            sFormatters[i] = new SimpleDateFormat(patterns[i]);
            sFormatters[i].setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        for (int ifdType = 0; ifdType < EXIF_TAGS.length; ifdType++) {
            sExifTagMapsForReading[ifdType] = new HashMap();
            sExifTagMapsForWriting[ifdType] = new HashMap();
            for (ExifTag tag : EXIF_TAGS[ifdType]) {
                sExifTagMapsForReading[ifdType].put(Integer.valueOf(tag.number), tag);
                sExifTagMapsForWriting[ifdType].put(tag.name, tag);
            }
        }
        sExifPointerTagMap.put(Integer.valueOf(EXIF_POINTER_TAGS[0].number), Integer.valueOf(5));
        sExifPointerTagMap.put(Integer.valueOf(EXIF_POINTER_TAGS[1].number), Integer.valueOf(1));
        sExifPointerTagMap.put(Integer.valueOf(EXIF_POINTER_TAGS[2].number), Integer.valueOf(2));
        sExifPointerTagMap.put(Integer.valueOf(EXIF_POINTER_TAGS[3].number), Integer.valueOf(3));
        sExifPointerTagMap.put(Integer.valueOf(EXIF_POINTER_TAGS[4].number), Integer.valueOf(7));
        sExifPointerTagMap.put(Integer.valueOf(EXIF_POINTER_TAGS[5].number), Integer.valueOf(8));
    }

    public ExifInterface(String filename) throws IOException {
        Throwable th;
        this.mAttributes = new HashMap[EXIF_TAGS.length];
        this.mExifByteOrder = ByteOrder.BIG_ENDIAN;
        if (filename == null) {
            throw new IllegalArgumentException("filename cannot be null");
        }
        AutoCloseable autoCloseable = null;
        this.mAssetInputStream = null;
        this.mFilename = filename;
        this.mIsInputStream = false;
        try {
            FileInputStream in = new FileInputStream(filename);
            try {
                if (isSeekableFD(in.getFD())) {
                    this.mSeekableFileDescriptor = in.getFD();
                } else {
                    this.mSeekableFileDescriptor = null;
                }
                loadAttributes(in);
                IoUtils.closeQuietly(in);
            } catch (Throwable th2) {
                th = th2;
                autoCloseable = in;
            }
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(autoCloseable);
            throw th;
        }
    }

    public ExifInterface(FileDescriptor fileDescriptor) throws IOException {
        Throwable th;
        this.mAttributes = new HashMap[EXIF_TAGS.length];
        this.mExifByteOrder = ByteOrder.BIG_ENDIAN;
        if (fileDescriptor == null) {
            throw new IllegalArgumentException("fileDescriptor cannot be null");
        }
        this.mAssetInputStream = null;
        this.mFilename = null;
        if (isSeekableFD(fileDescriptor)) {
            this.mSeekableFileDescriptor = fileDescriptor;
            try {
                fileDescriptor = Os.dup(fileDescriptor);
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            }
        }
        this.mSeekableFileDescriptor = null;
        this.mIsInputStream = false;
        FileInputStream in = null;
        try {
            FileInputStream in2 = new FileInputStream(fileDescriptor);
            try {
                loadAttributes(in2);
                IoUtils.closeQuietly(in2);
            } catch (Throwable th2) {
                th = th2;
                in = in2;
                IoUtils.closeQuietly(in);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(in);
            throw th;
        }
    }

    public ExifInterface(InputStream inputStream) throws IOException {
        this.mAttributes = new HashMap[EXIF_TAGS.length];
        this.mExifByteOrder = ByteOrder.BIG_ENDIAN;
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream cannot be null");
        }
        this.mFilename = null;
        if (inputStream instanceof AssetInputStream) {
            this.mAssetInputStream = (AssetInputStream) inputStream;
            this.mSeekableFileDescriptor = null;
        } else if ((inputStream instanceof FileInputStream) && isSeekableFD(((FileInputStream) inputStream).getFD())) {
            this.mAssetInputStream = null;
            this.mSeekableFileDescriptor = ((FileInputStream) inputStream).getFD();
        } else {
            this.mAssetInputStream = null;
            this.mSeekableFileDescriptor = null;
        }
        this.mIsInputStream = true;
        loadAttributes(inputStream);
    }

    private ExifAttribute getExifAttribute(String tag) {
        for (int i = 0; i < EXIF_TAGS.length; i++) {
            Object value = this.mAttributes[i].get(tag);
            if (value != null) {
                return (ExifAttribute) value;
            }
        }
        return null;
    }

    public String getAttribute(String tag) {
        ExifAttribute attribute = getExifAttribute(tag);
        if (attribute == null) {
            return null;
        }
        if (!sTagSetForCompatibility.contains(tag)) {
            return attribute.getStringValue(this.mExifByteOrder);
        }
        if (!tag.equals(TAG_GPS_TIMESTAMP)) {
            try {
                return Double.toString(attribute.getDoubleValue(this.mExifByteOrder));
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (attribute.format != 5 && attribute.format != 10) {
            return null;
        } else {
            if (((Rational[]) attribute.getValue(this.mExifByteOrder)).length != 3) {
                return null;
            }
            return String.format("%02d:%02d:%02d", new Object[]{Integer.valueOf((int) (((float) ((Rational[]) attribute.getValue(this.mExifByteOrder))[0].numerator) / ((float) ((Rational[]) attribute.getValue(this.mExifByteOrder))[0].denominator))), Integer.valueOf((int) (((float) ((Rational[]) attribute.getValue(this.mExifByteOrder))[1].numerator) / ((float) ((Rational[]) attribute.getValue(this.mExifByteOrder))[1].denominator))), Integer.valueOf((int) (((float) ((Rational[]) attribute.getValue(this.mExifByteOrder))[2].numerator) / ((float) ((Rational[]) attribute.getValue(this.mExifByteOrder))[2].denominator)))});
        }
    }

    public int getAttributeInt(String tag, int defaultValue) {
        ExifAttribute exifAttribute = getExifAttribute(tag);
        if (exifAttribute == null) {
            return defaultValue;
        }
        try {
            return exifAttribute.getIntValue(this.mExifByteOrder);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getAttributeDouble(String tag, double defaultValue) {
        ExifAttribute exifAttribute = getExifAttribute(tag);
        if (exifAttribute == null) {
            return defaultValue;
        }
        try {
            return exifAttribute.getDoubleValue(this.mExifByteOrder);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void setAttribute(String tag, String value) {
        if (value != null && sTagSetForCompatibility.contains(tag)) {
            if (tag.equals(TAG_GPS_TIMESTAMP)) {
                Matcher m = sGpsTimestampPattern.matcher(value);
                if (m.find()) {
                    value = Integer.parseInt(m.group(1)) + "/1," + Integer.parseInt(m.group(2)) + "/1," + Integer.parseInt(m.group(3)) + "/1";
                } else {
                    Log.w(TAG, "Invalid value for " + tag + " : " + value);
                    return;
                }
            }
            try {
                value = ((long) (10000.0d * Double.parseDouble(value))) + "/10000";
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid value for " + tag + " : " + value);
                return;
            }
        }
        for (int i = 0; i < EXIF_TAGS.length; i++) {
            if (i != 4 || (this.mHasThumbnail ^ 1) == 0) {
                ExifTag obj = sExifTagMapsForWriting[i].get(tag);
                if (obj != null) {
                    if (value != null) {
                        int dataFormat;
                        ExifTag exifTag = obj;
                        Pair<Integer, Integer> guess = guessDataFormat(value);
                        if (exifTag.primaryFormat == ((Integer) guess.first).intValue() || exifTag.primaryFormat == ((Integer) guess.second).intValue()) {
                            dataFormat = exifTag.primaryFormat;
                        } else if (exifTag.secondaryFormat != -1 && (exifTag.secondaryFormat == ((Integer) guess.first).intValue() || exifTag.secondaryFormat == ((Integer) guess.second).intValue())) {
                            dataFormat = exifTag.secondaryFormat;
                        } else if (exifTag.primaryFormat == 1 || exifTag.primaryFormat == 7 || exifTag.primaryFormat == 2) {
                            dataFormat = exifTag.primaryFormat;
                        } else {
                            Log.w(TAG, "Given tag (" + tag + ") value didn't match with one of expected " + "formats: " + IFD_FORMAT_NAMES[exifTag.primaryFormat] + (exifTag.secondaryFormat == -1 ? ProxyInfo.LOCAL_EXCL_LIST : ", " + IFD_FORMAT_NAMES[exifTag.secondaryFormat]) + " (guess: " + IFD_FORMAT_NAMES[((Integer) guess.first).intValue()] + (((Integer) guess.second).intValue() == -1 ? ProxyInfo.LOCAL_EXCL_LIST : ", " + IFD_FORMAT_NAMES[((Integer) guess.second).intValue()]) + ")");
                        }
                        String[] values;
                        int[] intArray;
                        int j;
                        Rational[] rationalArray;
                        String[] numbers;
                        switch (dataFormat) {
                            case 1:
                                this.mAttributes[i].put(tag, ExifAttribute.createByte(value));
                                break;
                            case 2:
                            case 7:
                                this.mAttributes[i].put(tag, ExifAttribute.createString(value));
                                break;
                            case 3:
                                values = value.split(",");
                                intArray = new int[values.length];
                                for (j = 0; j < values.length; j++) {
                                    intArray[j] = Integer.parseInt(values[j]);
                                }
                                this.mAttributes[i].put(tag, ExifAttribute.createUShort(intArray, this.mExifByteOrder));
                                break;
                            case 4:
                                values = value.split(",");
                                long[] longArray = new long[values.length];
                                for (j = 0; j < values.length; j++) {
                                    longArray[j] = Long.parseLong(values[j]);
                                }
                                this.mAttributes[i].put(tag, ExifAttribute.createULong(longArray, this.mExifByteOrder));
                                break;
                            case 5:
                                values = value.split(",");
                                rationalArray = new Rational[values.length];
                                for (j = 0; j < values.length; j++) {
                                    numbers = values[j].split("/");
                                    rationalArray[j] = new Rational((long) Double.parseDouble(numbers[0]), (long) Double.parseDouble(numbers[1]), null);
                                }
                                this.mAttributes[i].put(tag, ExifAttribute.createURational(rationalArray, this.mExifByteOrder));
                                break;
                            case 9:
                                values = value.split(",");
                                intArray = new int[values.length];
                                for (j = 0; j < values.length; j++) {
                                    intArray[j] = Integer.parseInt(values[j]);
                                }
                                this.mAttributes[i].put(tag, ExifAttribute.createSLong(intArray, this.mExifByteOrder));
                                break;
                            case 10:
                                values = value.split(",");
                                rationalArray = new Rational[values.length];
                                for (j = 0; j < values.length; j++) {
                                    numbers = values[j].split("/");
                                    rationalArray[j] = new Rational((long) Double.parseDouble(numbers[0]), (long) Double.parseDouble(numbers[1]), null);
                                }
                                this.mAttributes[i].put(tag, ExifAttribute.createSRational(rationalArray, this.mExifByteOrder));
                                break;
                            case 12:
                                values = value.split(",");
                                double[] doubleArray = new double[values.length];
                                for (j = 0; j < values.length; j++) {
                                    doubleArray[j] = Double.parseDouble(values[j]);
                                }
                                this.mAttributes[i].put(tag, ExifAttribute.createDouble(doubleArray, this.mExifByteOrder));
                                break;
                            default:
                                Log.w(TAG, "Data format isn't one of expected formats: " + dataFormat);
                                break;
                        }
                    }
                    this.mAttributes[i].remove(tag);
                }
            }
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
        Throwable th;
        int i = 0;
        while (i < EXIF_TAGS.length) {
            try {
                this.mAttributes[i] = new HashMap();
                i++;
            } catch (IOException e) {
                try {
                    this.mIsSupportedFile = false;
                    addDefaultValuesForCompatibility();
                    return;
                } catch (Throwable th2) {
                    th = th2;
                    addDefaultValuesForCompatibility();
                    throw th;
                }
            }
        }
        InputStream in2 = new BufferedInputStream(in, 5000);
        try {
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
            }
            setThumbnailData(inputStream);
            this.mIsSupportedFile = true;
            addDefaultValuesForCompatibility();
            in = in2;
        } catch (IOException e2) {
            in = in2;
        } catch (Throwable th3) {
            th = th3;
            in = in2;
            addDefaultValuesForCompatibility();
            throw th;
        }
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
            for (Entry entry : this.mAttributes[i].entrySet()) {
                ExifAttribute tagValue = (ExifAttribute) entry.getValue();
                Log.d(TAG, "tagName: " + entry.getKey() + ", tagType: " + tagValue.toString() + ", tagValue: '" + tagValue.getStringValue(this.mExifByteOrder) + "'");
            }
        }
    }

    public void saveAttributes() throws IOException {
        ErrnoException e;
        Throwable th;
        Object out;
        Object in;
        if (!this.mIsSupportedFile || this.mMimeType != 4) {
            throw new IOException("ExifInterface only supports saving attributes on JPEG formats.");
        } else if (this.mIsInputStream || (this.mSeekableFileDescriptor == null && this.mFilename == null)) {
            throw new IOException("ExifInterface does not support saving attributes for the current input.");
        } else {
            this.mThumbnailBytes = getThumbnail();
            AutoCloseable in2 = null;
            AutoCloseable out2 = null;
            File tempFile = null;
            try {
                FileInputStream in3;
                if (this.mFilename != null) {
                    File tempFile2 = new File(this.mFilename + ".tmp");
                    try {
                        if (new File(this.mFilename).renameTo(tempFile2)) {
                            tempFile = tempFile2;
                        } else {
                            throw new IOException("Could'nt rename to " + tempFile2.getAbsolutePath());
                        }
                    } catch (ErrnoException e2) {
                        e = e2;
                        tempFile = tempFile2;
                        try {
                            throw e.rethrowAsIOException();
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(in2);
                            IoUtils.closeQuietly(out2);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        tempFile = tempFile2;
                        IoUtils.closeQuietly(in2);
                        IoUtils.closeQuietly(out2);
                        throw th;
                    }
                } else if (this.mSeekableFileDescriptor != null) {
                    tempFile = File.createTempFile("temp", "jpg");
                    Os.lseek(this.mSeekableFileDescriptor, 0, OsConstants.SEEK_SET);
                    in3 = new FileInputStream(this.mSeekableFileDescriptor);
                    try {
                        FileOutputStream out3 = new FileOutputStream(tempFile);
                        try {
                            Streams.copy(in3, out3);
                            out2 = out3;
                            in2 = in3;
                        } catch (ErrnoException e3) {
                            e = e3;
                            out = out3;
                            in = in3;
                            throw e.rethrowAsIOException();
                        } catch (Throwable th4) {
                            th = th4;
                            out = out3;
                            in = in3;
                            IoUtils.closeQuietly(in2);
                            IoUtils.closeQuietly(out2);
                            throw th;
                        }
                    } catch (ErrnoException e4) {
                        e = e4;
                        in = in3;
                        throw e.rethrowAsIOException();
                    } catch (Throwable th5) {
                        th = th5;
                        in = in3;
                        IoUtils.closeQuietly(in2);
                        IoUtils.closeQuietly(out2);
                        throw th;
                    }
                }
                IoUtils.closeQuietly(in2);
                IoUtils.closeQuietly(out2);
                in2 = null;
                out2 = null;
                try {
                    in3 = new FileInputStream(tempFile);
                    try {
                        if (this.mFilename != null) {
                            out2 = new FileOutputStream(this.mFilename);
                        } else if (this.mSeekableFileDescriptor != null) {
                            Os.lseek(this.mSeekableFileDescriptor, 0, OsConstants.SEEK_SET);
                            out = new FileOutputStream(this.mSeekableFileDescriptor);
                        }
                        saveJpegAttributes(in3, out2);
                        IoUtils.closeQuietly(in3);
                        IoUtils.closeQuietly(out2);
                        tempFile.delete();
                        this.mThumbnailBytes = null;
                    } catch (ErrnoException e5) {
                        e = e5;
                        in = in3;
                        try {
                            throw e.rethrowAsIOException();
                        } catch (Throwable th6) {
                            th = th6;
                            IoUtils.closeQuietly(in2);
                            IoUtils.closeQuietly(null);
                            tempFile.delete();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        in = in3;
                        IoUtils.closeQuietly(in2);
                        IoUtils.closeQuietly(null);
                        tempFile.delete();
                        throw th;
                    }
                } catch (ErrnoException e6) {
                    e = e6;
                    throw e.rethrowAsIOException();
                }
            } catch (ErrnoException e7) {
                e = e7;
                throw e.rethrowAsIOException();
            }
        }
    }

    public boolean hasThumbnail() {
        return this.mHasThumbnail;
    }

    public byte[] getThumbnail() {
        if (this.mThumbnailCompression == 6 || this.mThumbnailCompression == 7) {
            return getThumbnailBytes();
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0025 A:{ExcHandler: java.io.IOException (r1_0 'e' java.lang.Exception), PHI: r3 , Splitter: B:8:0x000e} */
    /* JADX WARNING: Missing block: B:17:0x0025, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:19:?, code:
            android.util.Log.d(TAG, "Encountered exception while getting thumbnail", r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public byte[] getThumbnailBytes() {
        if (!this.mHasThumbnail) {
            return null;
        }
        if (this.mThumbnailBytes != null) {
            return this.mThumbnailBytes;
        }
        AutoCloseable in = null;
        try {
            Object in2;
            if (this.mAssetInputStream != null) {
                in = this.mAssetInputStream;
                if (in.markSupported()) {
                    in.reset();
                } else {
                    Log.d(TAG, "Cannot read thumbnail from inputstream without mark/reset support");
                    IoUtils.closeQuietly(in);
                    return null;
                }
            } else if (this.mFilename != null) {
                in2 = new FileInputStream(this.mFilename);
            } else if (this.mSeekableFileDescriptor != null) {
                FileDescriptor fileDescriptor = Os.dup(this.mSeekableFileDescriptor);
                Os.lseek(fileDescriptor, 0, OsConstants.SEEK_SET);
                in2 = new FileInputStream(fileDescriptor);
            }
            if (in == null) {
                throw new FileNotFoundException();
            } else if (in.skip((long) this.mThumbnailOffset) != ((long) this.mThumbnailOffset)) {
                throw new IOException("Corrupted image");
            } else {
                byte[] buffer = new byte[this.mThumbnailLength];
                if (in.read(buffer) != this.mThumbnailLength) {
                    throw new IOException("Corrupted image");
                }
                this.mThumbnailBytes = buffer;
                return buffer;
            }
        } catch (Exception e) {
        } finally {
            IoUtils.closeQuietly(in);
        }
        return null;
    }

    public Bitmap getThumbnailBitmap() {
        if (!this.mHasThumbnail) {
            return null;
        }
        if (this.mThumbnailBytes == null) {
            this.mThumbnailBytes = getThumbnailBytes();
        }
        if (this.mThumbnailCompression == 6 || this.mThumbnailCompression == 7) {
            return BitmapFactory.decodeByteArray(this.mThumbnailBytes, 0, this.mThumbnailLength);
        }
        if (this.mThumbnailCompression == 1) {
            int[] rgbValues = new int[(this.mThumbnailBytes.length / 3)];
            for (int i = 0; i < rgbValues.length; i++) {
                rgbValues[i] = (((this.mThumbnailBytes[i * 3] << 16) + 0) + (this.mThumbnailBytes[(i * 3) + 1] << 8)) + this.mThumbnailBytes[(i * 3) + 2];
            }
            ExifAttribute imageLengthAttribute = (ExifAttribute) this.mAttributes[4].get(TAG_IMAGE_LENGTH);
            ExifAttribute imageWidthAttribute = (ExifAttribute) this.mAttributes[4].get(TAG_IMAGE_WIDTH);
            if (!(imageLengthAttribute == null || imageWidthAttribute == null)) {
                return Bitmap.createBitmap(rgbValues, imageWidthAttribute.getIntValue(this.mExifByteOrder), imageLengthAttribute.getIntValue(this.mExifByteOrder), Config.ARGB_8888);
            }
        }
        return null;
    }

    public boolean isThumbnailCompressed() {
        if (!this.mHasThumbnail) {
            return false;
        }
        if (this.mThumbnailCompression == 6 || this.mThumbnailCompression == 7) {
            return true;
        }
        return false;
    }

    public long[] getThumbnailRange() {
        if (!this.mHasThumbnail) {
            return null;
        }
        return new long[]{(long) this.mThumbnailOffset, (long) this.mThumbnailLength};
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
        int i = -1;
        double altitude = getAttributeDouble(TAG_GPS_ALTITUDE, -1.0d);
        int ref = getAttributeInt(TAG_GPS_ALTITUDE_REF, -1);
        if (altitude < 0.0d || ref < 0) {
            return defaultValue;
        }
        if (ref != 1) {
            i = 1;
        }
        return ((double) i) * altitude;
    }

    public long getDateTime() {
        String dateTimeString = getAttribute(TAG_DATETIME);
        if (dateTimeString == null || (sNonZeroTimePattern.matcher(dateTimeString).matches() ^ 1) != 0) {
            return -1;
        }
        ParsePosition pos = new ParsePosition(0);
        Date datetime = null;
        try {
            for (SimpleDateFormat sFormatter : sFormatters) {
                datetime = sFormatter.parse(dateTimeString, pos);
                if (datetime != null) {
                    break;
                }
            }
            if (datetime == null) {
                return -1;
            }
            long msecs = datetime.getTime();
            String subSecs = getAttribute(TAG_SUBSEC_TIME);
            if (subSecs != null) {
                try {
                    long sub = Long.parseLong(subSecs);
                    while (sub > 1000) {
                        sub /= 10;
                    }
                    msecs += sub;
                } catch (NumberFormatException e) {
                }
            }
            return msecs;
        } catch (IllegalArgumentException e2) {
            return -1;
        }
    }

    public long getGpsDateTime() {
        int i = 0;
        String date = getAttribute(TAG_GPS_DATESTAMP);
        String time = getAttribute(TAG_GPS_TIMESTAMP);
        if (date == null || time == null || (!sNonZeroTimePattern.matcher(date).matches() && (sNonZeroTimePattern.matcher(time).matches() ^ 1) != 0)) {
            return -1;
        }
        String dateTimeString = date + ' ' + time;
        ParsePosition pos = new ParsePosition(0);
        try {
            SimpleDateFormat[] simpleDateFormatArr = sFormatters;
            int length = simpleDateFormatArr.length;
            while (i < length) {
                Date datetime = simpleDateFormatArr[i].parse(dateTimeString, pos);
                if (datetime != null) {
                    return datetime.getTime();
                }
                i++;
            }
            return -1;
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0098 A:{ExcHandler: java.lang.NumberFormatException (e java.lang.NumberFormatException), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:11:0x009e, code:
            throw new java.lang.IllegalArgumentException();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static float convertRationalLatLonToFloat(String rationalString, String ref) {
        try {
            String[] parts = rationalString.split(",");
            String[] pair = parts[0].split("/");
            double degrees = Double.parseDouble(pair[0].trim()) / Double.parseDouble(pair[1].trim());
            pair = parts[1].split("/");
            double minutes = Double.parseDouble(pair[0].trim()) / Double.parseDouble(pair[1].trim());
            pair = parts[2].split("/");
            double result = ((minutes / 60.0d) + degrees) + ((Double.parseDouble(pair[0].trim()) / Double.parseDouble(pair[1].trim())) / 3600.0d);
            if (!ref.equals("S")) {
                if (!ref.equals("W")) {
                    return (float) result;
                }
            }
            return (float) (-result);
        } catch (NumberFormatException e) {
        }
    }

    private int getMimeType(BufferedInputStream in) throws IOException {
        in.mark(5000);
        byte[] signatureCheckBytes = new byte[5000];
        if (in.read(signatureCheckBytes) != 5000) {
            throw new EOFException();
        }
        in.reset();
        if (isJpegFormat(signatureCheckBytes)) {
            return 4;
        }
        if (isRafFormat(signatureCheckBytes)) {
            return 9;
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
        for (int i = 0; i < JPEG_SIGNATURE.length; i++) {
            if (signatureCheckBytes[i] != JPEG_SIGNATURE[i]) {
                return false;
            }
        }
        return true;
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

    private boolean isOrfFormat(byte[] signatureCheckBytes) throws IOException {
        ByteOrderedDataInputStream signatureInputStream = new ByteOrderedDataInputStream(signatureCheckBytes);
        this.mExifByteOrder = readByteOrder(signatureInputStream);
        signatureInputStream.setByteOrder(this.mExifByteOrder);
        short orfSignature = signatureInputStream.readShort();
        if (orfSignature == ORF_SIGNATURE_1 || orfSignature == ORF_SIGNATURE_2) {
            return true;
        }
        return false;
    }

    private boolean isRw2Format(byte[] signatureCheckBytes) throws IOException {
        ByteOrderedDataInputStream signatureInputStream = new ByteOrderedDataInputStream(signatureCheckBytes);
        this.mExifByteOrder = readByteOrder(signatureInputStream);
        signatureInputStream.setByteOrder(this.mExifByteOrder);
        if (signatureInputStream.readShort() == RW2_SIGNATURE) {
            return true;
        }
        return false;
    }

    private void getJpegAttributes(ByteOrderedDataInputStream in, int jpegOffset, int imageType) throws IOException {
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        in.seek((long) jpegOffset);
        int bytesRead = jpegOffset;
        byte marker = in.readByte();
        if (marker != (byte) -1) {
            throw new IOException("Invalid marker: " + Integer.toHexString(marker & 255));
        }
        bytesRead = jpegOffset + 1;
        if (in.readByte() != MARKER_SOI) {
            throw new IOException("Invalid marker: " + Integer.toHexString(marker & 255));
        }
        bytesRead++;
        while (true) {
            marker = in.readByte();
            if (marker != (byte) -1) {
                throw new IOException("Invalid marker:" + Integer.toHexString(marker & 255));
            }
            bytesRead++;
            marker = in.readByte();
            bytesRead++;
            if (marker == MARKER_EOI || marker == MARKER_SOS) {
                in.setByteOrder(this.mExifByteOrder);
            } else {
                int length = in.readUnsignedShort() - 2;
                bytesRead += 2;
                if (length < 0) {
                    throw new IOException("Invalid length");
                }
                byte[] bytes;
                switch (marker) {
                    case (byte) -64:
                    case (byte) -63:
                    case (byte) -62:
                    case (byte) -61:
                    case (byte) -59:
                    case (byte) -58:
                    case (byte) -57:
                    case (byte) -55:
                    case (byte) -54:
                    case (byte) -53:
                    case (byte) -51:
                    case (byte) -50:
                    case (byte) -49:
                        if (in.skipBytes(1) == 1) {
                            this.mAttributes[imageType].put(TAG_IMAGE_LENGTH, ExifAttribute.createULong((long) in.readUnsignedShort(), this.mExifByteOrder));
                            this.mAttributes[imageType].put(TAG_IMAGE_WIDTH, ExifAttribute.createULong((long) in.readUnsignedShort(), this.mExifByteOrder));
                            length -= 5;
                            break;
                        }
                        throw new IOException("Invalid SOFx");
                    case PacketKeepalive.ERROR_HARDWARE_ERROR /*-31*/:
                        if (length >= 6) {
                            byte[] identifier = new byte[6];
                            if (in.read(identifier) == 6) {
                                bytesRead += 6;
                                length -= 6;
                                if (Arrays.equals(identifier, IDENTIFIER_EXIF_APP1)) {
                                    if (length > 0) {
                                        this.mExifOffset = bytesRead;
                                        bytes = new byte[length];
                                        if (in.read(bytes) == length) {
                                            bytesRead += length;
                                            length = 0;
                                            readExifSegment(bytes, imageType);
                                            break;
                                        }
                                        throw new IOException("Invalid exif");
                                    }
                                    throw new IOException("Invalid exif");
                                }
                            }
                            throw new IOException("Invalid exif");
                        }
                        break;
                    case (byte) -2:
                        bytes = new byte[length];
                        if (in.read(bytes) == length) {
                            length = 0;
                            if (getAttribute(TAG_USER_COMMENT) == null) {
                                this.mAttributes[1].put(TAG_USER_COMMENT, ExifAttribute.createString(new String(bytes, ASCII)));
                                break;
                            }
                        }
                        throw new IOException("Invalid exif");
                        break;
                }
                if (length < 0) {
                    throw new IOException("Invalid length");
                } else if (in.skipBytes(length) != length) {
                    throw new IOException("Invalid JPEG segment");
                } else {
                    bytesRead += length;
                }
            }
        }
        in.setByteOrder(this.mExifByteOrder);
    }

    private void getRawAttributes(ByteOrderedDataInputStream in) throws IOException {
        parseTiffHeaders(in, in.available());
        readImageFileDirectory(in, 0);
        updateImageSizeValues(in, 0);
        updateImageSizeValues(in, 5);
        updateImageSizeValues(in, 4);
        validateImages(in);
        if (this.mMimeType == 8) {
            ExifAttribute makerNoteAttribute = (ExifAttribute) this.mAttributes[1].get(TAG_MAKER_NOTE);
            if (makerNoteAttribute != null) {
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
                return;
            }
            in.skipBytes(numberOfBytes);
        }
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
                int[] aspectFrameValues = new int[4];
                aspectFrameValues = (int[]) aspectFrameAttribute.getValue(this.mExifByteOrder);
                if (aspectFrameValues[2] > aspectFrameValues[0] && aspectFrameValues[3] > aspectFrameValues[1]) {
                    int primaryImageWidth = (aspectFrameValues[2] - aspectFrameValues[0]) + 1;
                    int primaryImageLength = (aspectFrameValues[3] - aspectFrameValues[1]) + 1;
                    if (primaryImageWidth < primaryImageLength) {
                        primaryImageWidth += primaryImageLength;
                        primaryImageLength = primaryImageWidth - primaryImageLength;
                        primaryImageWidth -= primaryImageLength;
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
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        ByteOrderedDataOutputStream dataOutputStream = new ByteOrderedDataOutputStream(outputStream, ByteOrder.BIG_ENDIAN);
        if (dataInputStream.readByte() != (byte) -1) {
            throw new IOException("Invalid marker");
        }
        dataOutputStream.writeByte(-1);
        if (dataInputStream.readByte() != MARKER_SOI) {
            throw new IOException("Invalid marker");
        }
        dataOutputStream.writeByte(-40);
        dataOutputStream.writeByte(-1);
        dataOutputStream.writeByte(-31);
        writeExifSegment(dataOutputStream, 6);
        byte[] bytes = new byte[4096];
        while (dataInputStream.readByte() == (byte) -1) {
            byte marker = dataInputStream.readByte();
            int length;
            int read;
            switch (marker) {
                case (byte) -39:
                case RadioManager.STATUS_INVALID_OPERATION /*-38*/:
                    dataOutputStream.writeByte(-1);
                    dataOutputStream.writeByte(marker);
                    Streams.copy(dataInputStream, dataOutputStream);
                    return;
                case PacketKeepalive.ERROR_HARDWARE_ERROR /*-31*/:
                    length = dataInputStream.readUnsignedShort() - 2;
                    if (length >= 0) {
                        byte[] identifier = new byte[6];
                        if (length >= 6) {
                            if (dataInputStream.read(identifier) == 6) {
                                if (Arrays.equals(identifier, IDENTIFIER_EXIF_APP1)) {
                                    if (dataInputStream.skipBytes(length - 6) == length - 6) {
                                        break;
                                    }
                                    throw new IOException("Invalid length");
                                }
                            }
                            throw new IOException("Invalid exif");
                        }
                        dataOutputStream.writeByte(-1);
                        dataOutputStream.writeByte(marker);
                        dataOutputStream.writeUnsignedShort(length + 2);
                        if (length >= 6) {
                            length -= 6;
                            dataOutputStream.write(identifier);
                        }
                        while (length > 0) {
                            read = dataInputStream.read(bytes, 0, Math.min(length, bytes.length));
                            if (read < 0) {
                                break;
                            }
                            dataOutputStream.write(bytes, 0, read);
                            length -= read;
                        }
                        break;
                    }
                    throw new IOException("Invalid length");
                default:
                    dataOutputStream.writeByte(-1);
                    dataOutputStream.writeByte(marker);
                    length = dataInputStream.readUnsignedShort();
                    dataOutputStream.writeUnsignedShort(length);
                    length -= 2;
                    if (length >= 0) {
                        while (length > 0) {
                            read = dataInputStream.read(bytes, 0, Math.min(length, bytes.length));
                            if (read < 0) {
                                break;
                            }
                            dataOutputStream.write(bytes, 0, read);
                            length -= read;
                        }
                        break;
                    }
                    throw new IOException("Invalid length");
            }
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
        if (valueOfDateTimeOriginal != null) {
            this.mAttributes[0].put(TAG_DATETIME, ExifAttribute.createString(valueOfDateTimeOriginal));
        }
        if (getAttribute(TAG_IMAGE_WIDTH) == null) {
            this.mAttributes[0].put(TAG_IMAGE_WIDTH, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (getAttribute(TAG_IMAGE_LENGTH) == null) {
            this.mAttributes[0].put(TAG_IMAGE_LENGTH, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (getAttribute(TAG_ORIENTATION) == null) {
            this.mAttributes[0].put(TAG_ORIENTATION, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (getAttribute(TAG_LIGHT_SOURCE) == null) {
            this.mAttributes[1].put(TAG_LIGHT_SOURCE, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
    }

    private ByteOrder readByteOrder(ByteOrderedDataInputStream dataInputStream) throws IOException {
        short byteOrder = dataInputStream.readShort();
        switch (byteOrder) {
            case (short) 18761:
                return ByteOrder.LITTLE_ENDIAN;
            case (short) 19789:
                return ByteOrder.BIG_ENDIAN;
            default:
                throw new IOException("Invalid byte order: " + Integer.toHexString(byteOrder));
        }
    }

    private void parseTiffHeaders(ByteOrderedDataInputStream dataInputStream, int exifBytesLength) throws IOException {
        this.mExifByteOrder = readByteOrder(dataInputStream);
        dataInputStream.setByteOrder(this.mExifByteOrder);
        int startCode = dataInputStream.readUnsignedShort();
        if (this.mMimeType == 7 || this.mMimeType == 10 || startCode == 42) {
            int firstIfdOffset = dataInputStream.readInt();
            if (firstIfdOffset < 8 || firstIfdOffset >= exifBytesLength) {
                throw new IOException("Invalid first Ifd offset: " + firstIfdOffset);
            }
            firstIfdOffset -= 8;
            if (firstIfdOffset > 0 && dataInputStream.skipBytes(firstIfdOffset) != firstIfdOffset) {
                throw new IOException("Couldn't jump to first Ifd: " + firstIfdOffset);
            }
            return;
        }
        throw new IOException("Invalid start code: " + Integer.toHexString(startCode));
    }

    private void readImageFileDirectory(ByteOrderedDataInputStream dataInputStream, int ifdType) throws IOException {
        if (dataInputStream.mPosition + 2 <= dataInputStream.mLength) {
            short numberOfDirectoryEntry = dataInputStream.readShort();
            if (dataInputStream.mPosition + (numberOfDirectoryEntry * 12) <= dataInputStream.mLength) {
                for (short i = (short) 0; i < numberOfDirectoryEntry; i = (short) (i + 1)) {
                    int tagNumber = dataInputStream.readUnsignedShort();
                    int dataFormat = dataInputStream.readUnsignedShort();
                    int numberOfComponents = dataInputStream.readInt();
                    long nextEntryOffset = (long) (dataInputStream.peek() + 4);
                    ExifTag tag = (ExifTag) sExifTagMapsForReading[ifdType].get(Integer.valueOf(tagNumber));
                    long byteCount = 0;
                    boolean valid = false;
                    if (tag != null && dataFormat > 0 && dataFormat < IFD_FORMAT_BYTES_PER_FORMAT.length) {
                        byteCount = ((long) numberOfComponents) * ((long) IFD_FORMAT_BYTES_PER_FORMAT[dataFormat]);
                        if (byteCount >= 0 && byteCount <= 2147483647L) {
                            valid = true;
                        }
                    }
                    if (valid) {
                        if (byteCount > 4) {
                            int offset = dataInputStream.readInt();
                            if (this.mMimeType == 7) {
                                if (tag.name == TAG_MAKER_NOTE) {
                                    this.mOrfMakerNoteOffset = offset;
                                } else if (ifdType == 6 && tag.name == TAG_ORF_THUMBNAIL_IMAGE) {
                                    this.mOrfThumbnailOffset = offset;
                                    this.mOrfThumbnailLength = numberOfComponents;
                                    ExifAttribute compressionAttribute = ExifAttribute.createUShort(6, this.mExifByteOrder);
                                    ExifAttribute jpegInterchangeFormatAttribute = ExifAttribute.createULong((long) this.mOrfThumbnailOffset, this.mExifByteOrder);
                                    ExifAttribute jpegInterchangeFormatLengthAttribute = ExifAttribute.createULong((long) this.mOrfThumbnailLength, this.mExifByteOrder);
                                    this.mAttributes[4].put(TAG_COMPRESSION, compressionAttribute);
                                    this.mAttributes[4].put(TAG_JPEG_INTERCHANGE_FORMAT, jpegInterchangeFormatAttribute);
                                    this.mAttributes[4].put(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, jpegInterchangeFormatLengthAttribute);
                                }
                            } else if (this.mMimeType == 10 && tag.name == TAG_RW2_JPG_FROM_RAW) {
                                this.mRw2JpgFromRawOffset = offset;
                            }
                            if (((long) offset) + byteCount <= ((long) dataInputStream.mLength)) {
                                dataInputStream.seek((long) offset);
                            } else {
                                Log.w(TAG, "Skip the tag entry since data offset is invalid: " + offset);
                                dataInputStream.seek(nextEntryOffset);
                            }
                        }
                        Object nextIfdType = sExifPointerTagMap.get(Integer.valueOf(tagNumber));
                        if (nextIfdType != null) {
                            long offset2 = -1;
                            switch (dataFormat) {
                                case 3:
                                    offset2 = (long) dataInputStream.readUnsignedShort();
                                    break;
                                case 4:
                                    offset2 = dataInputStream.readUnsignedInt();
                                    break;
                                case 8:
                                    offset2 = (long) dataInputStream.readShort();
                                    break;
                                case 9:
                                case 13:
                                    offset2 = (long) dataInputStream.readInt();
                                    break;
                            }
                            if (offset2 <= 0 || offset2 >= ((long) dataInputStream.mLength)) {
                                Log.w(TAG, "Skip jump into the IFD since its offset is invalid: " + offset2);
                            } else {
                                dataInputStream.seek(offset2);
                                readImageFileDirectory(dataInputStream, ((Integer) nextIfdType).intValue());
                            }
                            dataInputStream.seek(nextEntryOffset);
                        } else {
                            byte[] bytes = new byte[((int) byteCount)];
                            dataInputStream.readFully(bytes);
                            ExifAttribute attribute = new ExifAttribute(dataFormat, numberOfComponents, bytes, null);
                            this.mAttributes[ifdType].put(tag.name, attribute);
                            if (tag.name == TAG_DNG_VERSION) {
                                this.mMimeType = 3;
                            }
                            if (((tag.name == TAG_MAKE || tag.name == TAG_MODEL) && attribute.getStringValue(this.mExifByteOrder).contains(PEF_SIGNATURE)) || (tag.name == TAG_COMPRESSION && attribute.getIntValue(this.mExifByteOrder) == 65535)) {
                                this.mMimeType = 8;
                            }
                            if (((long) dataInputStream.peek()) != nextEntryOffset) {
                                dataInputStream.seek(nextEntryOffset);
                            }
                        }
                    } else {
                        dataInputStream.seek(nextEntryOffset);
                    }
                }
                if (dataInputStream.peek() + 4 <= dataInputStream.mLength) {
                    int nextIfdOffset = dataInputStream.readInt();
                    if (nextIfdOffset > 8 && nextIfdOffset < dataInputStream.mLength) {
                        dataInputStream.seek((long) nextIfdOffset);
                        if (this.mAttributes[4].isEmpty()) {
                            readImageFileDirectory(dataInputStream, 4);
                        } else if (this.mAttributes[5].isEmpty()) {
                            readImageFileDirectory(dataInputStream, 5);
                        }
                    }
                }
            }
        }
    }

    private void retrieveJpegImageSize(ByteOrderedDataInputStream in, int imageType) throws IOException {
        ExifAttribute imageWidthAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_IMAGE_WIDTH);
        if (((ExifAttribute) this.mAttributes[imageType].get(TAG_IMAGE_LENGTH)) == null || imageWidthAttribute == null) {
            ExifAttribute jpegInterchangeFormatAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_JPEG_INTERCHANGE_FORMAT);
            if (jpegInterchangeFormatAttribute != null) {
                getJpegAttributes(in, jpegInterchangeFormatAttribute.getIntValue(this.mExifByteOrder), imageType);
            }
        }
    }

    private void setThumbnailData(ByteOrderedDataInputStream in) throws IOException {
        HashMap thumbnailData = this.mAttributes[4];
        ExifAttribute compressionAttribute = (ExifAttribute) thumbnailData.get(TAG_COMPRESSION);
        if (compressionAttribute != null) {
            this.mThumbnailCompression = compressionAttribute.getIntValue(this.mExifByteOrder);
            switch (this.mThumbnailCompression) {
                case 1:
                case 7:
                    if (isSupportedDataType(thumbnailData)) {
                        handleThumbnailFromStrips(in, thumbnailData);
                        return;
                    }
                    return;
                case 6:
                    handleThumbnailFromJfif(in, thumbnailData);
                    return;
                default:
                    return;
            }
        }
        handleThumbnailFromJfif(in, thumbnailData);
    }

    private void handleThumbnailFromJfif(ByteOrderedDataInputStream in, HashMap thumbnailData) throws IOException {
        ExifAttribute jpegInterchangeFormatAttribute = (ExifAttribute) thumbnailData.get(TAG_JPEG_INTERCHANGE_FORMAT);
        ExifAttribute jpegInterchangeFormatLengthAttribute = (ExifAttribute) thumbnailData.get(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
        if (jpegInterchangeFormatAttribute != null && jpegInterchangeFormatLengthAttribute != null) {
            int thumbnailOffset = jpegInterchangeFormatAttribute.getIntValue(this.mExifByteOrder);
            int thumbnailLength = Math.min(jpegInterchangeFormatLengthAttribute.getIntValue(this.mExifByteOrder), in.available() - thumbnailOffset);
            if (this.mMimeType == 4 || this.mMimeType == 9 || this.mMimeType == 10) {
                thumbnailOffset += this.mExifOffset;
            } else if (this.mMimeType == 7) {
                thumbnailOffset += this.mOrfMakerNoteOffset;
            }
            if (thumbnailOffset > 0 && thumbnailLength > 0) {
                this.mHasThumbnail = true;
                this.mThumbnailOffset = thumbnailOffset;
                this.mThumbnailLength = thumbnailLength;
                this.mThumbnailCompression = 6;
                if (this.mFilename == null && this.mAssetInputStream == null && this.mSeekableFileDescriptor == null) {
                    byte[] thumbnailBytes = new byte[thumbnailLength];
                    in.seek((long) thumbnailOffset);
                    in.readFully(thumbnailBytes);
                    this.mThumbnailBytes = thumbnailBytes;
                }
            }
        }
    }

    private void handleThumbnailFromStrips(ByteOrderedDataInputStream in, HashMap thumbnailData) throws IOException {
        ExifAttribute stripOffsetsAttribute = (ExifAttribute) thumbnailData.get(TAG_STRIP_OFFSETS);
        ExifAttribute stripByteCountsAttribute = (ExifAttribute) thumbnailData.get(TAG_STRIP_BYTE_COUNTS);
        if (stripOffsetsAttribute != null && stripByteCountsAttribute != null) {
            long[] stripOffsets = (long[]) stripOffsetsAttribute.getValue(this.mExifByteOrder);
            long[] stripByteCounts = (long[]) stripByteCountsAttribute.getValue(this.mExifByteOrder);
            byte[] totalStripBytes = new byte[((int) Arrays.stream(stripByteCounts).sum())];
            int bytesRead = 0;
            int bytesAdded = 0;
            for (int i = 0; i < stripOffsets.length; i++) {
                int stripByteCount = (int) stripByteCounts[i];
                int skipBytes = ((int) stripOffsets[i]) - bytesRead;
                if (skipBytes < 0) {
                    Log.d(TAG, "Invalid strip offset value");
                }
                in.seek((long) skipBytes);
                bytesRead += skipBytes;
                byte[] stripBytes = new byte[stripByteCount];
                in.read(stripBytes);
                bytesRead += stripByteCount;
                System.arraycopy(stripBytes, 0, totalStripBytes, bytesAdded, stripBytes.length);
                bytesAdded += stripBytes.length;
            }
            this.mHasThumbnail = true;
            this.mThumbnailBytes = totalStripBytes;
            this.mThumbnailLength = totalStripBytes.length;
        }
    }

    private boolean isSupportedDataType(HashMap thumbnailData) throws IOException {
        ExifAttribute bitsPerSampleAttribute = (ExifAttribute) thumbnailData.get(TAG_BITS_PER_SAMPLE);
        if (bitsPerSampleAttribute != null) {
            int[] bitsPerSampleValue = (int[]) bitsPerSampleAttribute.getValue(this.mExifByteOrder);
            if (Arrays.equals(BITS_PER_SAMPLE_RGB, bitsPerSampleValue)) {
                return true;
            }
            if (this.mMimeType == 3) {
                ExifAttribute photometricInterpretationAttribute = (ExifAttribute) thumbnailData.get(TAG_PHOTOMETRIC_INTERPRETATION);
                if (photometricInterpretationAttribute != null) {
                    int photometricInterpretationValue = photometricInterpretationAttribute.getIntValue(this.mExifByteOrder);
                    if ((photometricInterpretationValue == 1 && Arrays.equals(bitsPerSampleValue, BITS_PER_SAMPLE_GREYSCALE_2)) || (photometricInterpretationValue == 6 && Arrays.equals(bitsPerSampleValue, BITS_PER_SAMPLE_RGB))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isThumbnail(HashMap map) throws IOException {
        ExifAttribute imageLengthAttribute = (ExifAttribute) map.get(TAG_IMAGE_LENGTH);
        ExifAttribute imageWidthAttribute = (ExifAttribute) map.get(TAG_IMAGE_WIDTH);
        if (!(imageLengthAttribute == null || imageWidthAttribute == null)) {
            int imageLengthValue = imageLengthAttribute.getIntValue(this.mExifByteOrder);
            int imageWidthValue = imageWidthAttribute.getIntValue(this.mExifByteOrder);
            if (imageLengthValue <= 512 && imageWidthValue <= 512) {
                return true;
            }
        }
        return false;
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
            this.mAttributes[4] = this.mAttributes[5];
            this.mAttributes[5] = new HashMap();
        }
        if (!isThumbnail(this.mAttributes[4])) {
            Log.d(TAG, "No image meets the size requirements of a thumbnail image.");
        }
    }

    private void updateImageSizeValues(ByteOrderedDataInputStream in, int imageType) throws IOException {
        ExifAttribute defaultCropSizeAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_DEFAULT_CROP_SIZE);
        ExifAttribute topBorderAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_RW2_SENSOR_TOP_BORDER);
        ExifAttribute leftBorderAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_RW2_SENSOR_LEFT_BORDER);
        ExifAttribute bottomBorderAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_RW2_SENSOR_BOTTOM_BORDER);
        ExifAttribute rightBorderAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_RW2_SENSOR_RIGHT_BORDER);
        if (defaultCropSizeAttribute != null) {
            ExifAttribute defaultCropSizeXAttribute;
            ExifAttribute defaultCropSizeYAttribute;
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
                int width = rightBorderValue - leftBorderValue;
                ExifAttribute imageLengthAttribute = ExifAttribute.createUShort(bottomBorderValue - topBorderValue, this.mExifByteOrder);
                ExifAttribute imageWidthAttribute = ExifAttribute.createUShort(width, this.mExifByteOrder);
                this.mAttributes[imageType].put(TAG_IMAGE_LENGTH, imageLengthAttribute);
                this.mAttributes[imageType].put(TAG_IMAGE_WIDTH, imageWidthAttribute);
            }
        }
    }

    private int writeExifSegment(ByteOrderedDataOutputStream dataOutputStream, int exifOffsetFromBeginning) throws IOException {
        int ifdType;
        int i;
        int size;
        int[] ifdOffsets = new int[EXIF_TAGS.length];
        int[] ifdDataSizes = new int[EXIF_TAGS.length];
        for (ExifTag tag : EXIF_POINTER_TAGS) {
            removeAttribute(tag.name);
        }
        removeAttribute(JPEG_INTERCHANGE_FORMAT_TAG.name);
        removeAttribute(JPEG_INTERCHANGE_FORMAT_LENGTH_TAG.name);
        for (ifdType = 0; ifdType < EXIF_TAGS.length; ifdType++) {
            for (Entry entry : this.mAttributes[ifdType].entrySet().toArray()) {
                if (entry.getValue() == null) {
                    this.mAttributes[ifdType].remove(entry.getKey());
                }
            }
        }
        if (!this.mAttributes[1].isEmpty()) {
            this.mAttributes[0].put(EXIF_POINTER_TAGS[1].name, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (!this.mAttributes[2].isEmpty()) {
            this.mAttributes[0].put(EXIF_POINTER_TAGS[2].name, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (!this.mAttributes[3].isEmpty()) {
            this.mAttributes[1].put(EXIF_POINTER_TAGS[3].name, ExifAttribute.createULong(0, this.mExifByteOrder));
        }
        if (this.mHasThumbnail) {
            this.mAttributes[4].put(JPEG_INTERCHANGE_FORMAT_TAG.name, ExifAttribute.createULong(0, this.mExifByteOrder));
            this.mAttributes[4].put(JPEG_INTERCHANGE_FORMAT_LENGTH_TAG.name, ExifAttribute.createULong((long) this.mThumbnailLength, this.mExifByteOrder));
        }
        for (i = 0; i < EXIF_TAGS.length; i++) {
            int sum = 0;
            for (Entry entry2 : this.mAttributes[i].entrySet()) {
                size = ((ExifAttribute) entry2.getValue()).size();
                if (size > 4) {
                    sum += size;
                }
            }
            ifdDataSizes[i] = ifdDataSizes[i] + sum;
        }
        int position = 8;
        for (ifdType = 0; ifdType < EXIF_TAGS.length; ifdType++) {
            if (!this.mAttributes[ifdType].isEmpty()) {
                ifdOffsets[ifdType] = position;
                position += (((this.mAttributes[ifdType].size() * 12) + 2) + 4) + ifdDataSizes[ifdType];
            }
        }
        if (this.mHasThumbnail) {
            int thumbnailOffset = position;
            this.mAttributes[4].put(JPEG_INTERCHANGE_FORMAT_TAG.name, ExifAttribute.createULong((long) thumbnailOffset, this.mExifByteOrder));
            this.mThumbnailOffset = exifOffsetFromBeginning + thumbnailOffset;
            position += this.mThumbnailLength;
        }
        int totalSize = position + 8;
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
        for (ifdType = 0; ifdType < EXIF_TAGS.length; ifdType++) {
            if (!this.mAttributes[ifdType].isEmpty()) {
                ExifAttribute attribute;
                dataOutputStream.writeUnsignedShort(this.mAttributes[ifdType].size());
                int dataOffset = ((ifdOffsets[ifdType] + 2) + (this.mAttributes[ifdType].size() * 12)) + 4;
                for (Entry entry22 : this.mAttributes[ifdType].entrySet()) {
                    int tagNumber = ((ExifTag) sExifTagMapsForWriting[ifdType].get(entry22.getKey())).number;
                    attribute = (ExifAttribute) entry22.getValue();
                    size = attribute.size();
                    dataOutputStream.writeUnsignedShort(tagNumber);
                    dataOutputStream.writeUnsignedShort(attribute.format);
                    dataOutputStream.writeInt(attribute.numberOfComponents);
                    if (size > 4) {
                        dataOutputStream.writeUnsignedInt((long) dataOffset);
                        dataOffset += size;
                    } else {
                        dataOutputStream.write(attribute.bytes);
                        if (size < 4) {
                            for (i = size; i < 4; i++) {
                                dataOutputStream.writeByte(0);
                            }
                        }
                    }
                }
                if (ifdType != 0 || (this.mAttributes[4].isEmpty() ^ 1) == 0) {
                    dataOutputStream.writeUnsignedInt(0);
                } else {
                    dataOutputStream.writeUnsignedInt((long) ifdOffsets[4]);
                }
                for (Entry entry222 : this.mAttributes[ifdType].entrySet()) {
                    attribute = (ExifAttribute) entry222.getValue();
                    if (attribute.bytes.length > 4) {
                        dataOutputStream.write(attribute.bytes, 0, attribute.bytes.length);
                    }
                }
            }
        }
        if (this.mHasThumbnail) {
            dataOutputStream.write(getThumbnailBytes());
        }
        dataOutputStream.setByteOrder(ByteOrder.BIG_ENDIAN);
        return totalSize;
    }

    private static Pair<Integer, Integer> guessDataFormat(String entryValue) {
        if (entryValue.contains(",")) {
            String[] entryValues = entryValue.split(",");
            Pair<Integer, Integer> dataFormat = guessDataFormat(entryValues[0]);
            if (((Integer) dataFormat.first).intValue() == 2) {
                return dataFormat;
            }
            for (int i = 1; i < entryValues.length; i++) {
                Pair<Integer, Integer> guessDataFormat = guessDataFormat(entryValues[i]);
                int first = -1;
                int second = -1;
                if (guessDataFormat.first == dataFormat.first || guessDataFormat.second == dataFormat.first) {
                    first = ((Integer) dataFormat.first).intValue();
                }
                if (((Integer) dataFormat.second).intValue() != -1 && (guessDataFormat.first == dataFormat.second || guessDataFormat.second == dataFormat.second)) {
                    second = ((Integer) dataFormat.second).intValue();
                }
                if (first == -1 && second == -1) {
                    return new Pair(Integer.valueOf(2), Integer.valueOf(-1));
                }
                if (first == -1) {
                    dataFormat = new Pair(Integer.valueOf(second), Integer.valueOf(-1));
                } else if (second == -1) {
                    dataFormat = new Pair(Integer.valueOf(first), Integer.valueOf(-1));
                }
            }
            return dataFormat;
        }
        if (entryValue.contains("/")) {
            String[] rationalNumber = entryValue.split("/");
            if (rationalNumber.length == 2) {
                try {
                    long numerator = (long) Double.parseDouble(rationalNumber[0]);
                    long denominator = (long) Double.parseDouble(rationalNumber[1]);
                    if (numerator < 0 || denominator < 0) {
                        return new Pair(Integer.valueOf(10), Integer.valueOf(-1));
                    }
                    if (numerator > 2147483647L || denominator > 2147483647L) {
                        return new Pair(Integer.valueOf(5), Integer.valueOf(-1));
                    }
                    return new Pair(Integer.valueOf(10), Integer.valueOf(5));
                } catch (NumberFormatException e) {
                }
            }
            return new Pair(Integer.valueOf(2), Integer.valueOf(-1));
        }
        try {
            Long longValue = Long.valueOf(Long.parseLong(entryValue));
            if (longValue.longValue() >= 0 && longValue.longValue() <= 65535) {
                return new Pair(Integer.valueOf(3), Integer.valueOf(4));
            }
            if (longValue.longValue() < 0) {
                return new Pair(Integer.valueOf(9), Integer.valueOf(-1));
            }
            return new Pair(Integer.valueOf(4), Integer.valueOf(-1));
        } catch (NumberFormatException e2) {
            try {
                Double.parseDouble(entryValue);
                return new Pair(Integer.valueOf(12), Integer.valueOf(-1));
            } catch (NumberFormatException e3) {
                return new Pair(Integer.valueOf(2), Integer.valueOf(-1));
            }
        }
    }

    private void swapBasedOnImageSize(int firstIfdType, int secondIfdType) throws IOException {
        if (!this.mAttributes[firstIfdType].isEmpty() && !this.mAttributes[secondIfdType].isEmpty()) {
            ExifAttribute firstImageLengthAttribute = (ExifAttribute) this.mAttributes[firstIfdType].get(TAG_IMAGE_LENGTH);
            ExifAttribute firstImageWidthAttribute = (ExifAttribute) this.mAttributes[firstIfdType].get(TAG_IMAGE_WIDTH);
            ExifAttribute secondImageLengthAttribute = (ExifAttribute) this.mAttributes[secondIfdType].get(TAG_IMAGE_LENGTH);
            ExifAttribute secondImageWidthAttribute = (ExifAttribute) this.mAttributes[secondIfdType].get(TAG_IMAGE_WIDTH);
            if (!(firstImageLengthAttribute == null || firstImageWidthAttribute == null || secondImageLengthAttribute == null || secondImageWidthAttribute == null)) {
                int firstImageLengthValue = firstImageLengthAttribute.getIntValue(this.mExifByteOrder);
                int firstImageWidthValue = firstImageWidthAttribute.getIntValue(this.mExifByteOrder);
                int secondImageLengthValue = secondImageLengthAttribute.getIntValue(this.mExifByteOrder);
                int secondImageWidthValue = secondImageWidthAttribute.getIntValue(this.mExifByteOrder);
                if (firstImageLengthValue < secondImageLengthValue && firstImageWidthValue < secondImageWidthValue) {
                    HashMap tempMap = this.mAttributes[firstIfdType];
                    this.mAttributes[firstIfdType] = this.mAttributes[secondIfdType];
                    this.mAttributes[secondIfdType] = tempMap;
                }
            }
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
}
