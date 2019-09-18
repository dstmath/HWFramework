package android.media;

import android.bluetooth.BluetoothClass;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.radio.RadioManager;
import android.hardware.radio.V1_0.RadioError;
import android.mtp.MtpConstants;
import android.os.storage.ExternalStorageFileImpl;
import android.os.storage.ExternalStorageFileOutputStreamImpl;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.util.Pair;
import com.android.internal.R;
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
import java.util.regex.Pattern;
import libcore.io.IoUtils;
import libcore.io.Streams;

public class ExifInterface {
    /* access modifiers changed from: private */
    public static final Charset ASCII = Charset.forName("US-ASCII");
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
    private static final boolean DEBUG = false;
    /* access modifiers changed from: private */
    public static final byte[] EXIF_ASCII_PREFIX = {65, 83, 67, 73, 73, 0, 0, 0};
    private static final int EXIF_HEAD_LENGTH = 6;
    private static final ExifTag[] EXIF_POINTER_TAGS = {new ExifTag(TAG_SUB_IFD_POINTER, (int) R.styleable.Theme_textColorSearchUrl, 4), new ExifTag(TAG_EXIF_IFD_POINTER, 34665, 4), new ExifTag(TAG_GPS_INFO_IFD_POINTER, 34853, 4), new ExifTag(TAG_INTEROPERABILITY_IFD_POINTER, 40965, 4), new ExifTag(TAG_ORF_CAMERA_SETTINGS_IFD_POINTER, (int) MtpConstants.RESPONSE_SPECIFICATION_OF_DESTINATION_UNSUPPORTED, 1), new ExifTag(TAG_ORF_IMAGE_PROCESSING_IFD_POINTER, 8256, 1)};
    private static final ExifTag[][] EXIF_TAGS = {IFD_TIFF_TAGS, IFD_EXIF_TAGS, IFD_GPS_TAGS, IFD_INTEROPERABILITY_TAGS, IFD_THUMBNAIL_TAGS, IFD_TIFF_TAGS, ORF_MAKER_NOTE_TAGS, ORF_CAMERA_SETTINGS_TAGS, ORF_IMAGE_PROCESSING_TAGS, PEF_TAGS};
    private static final byte[] HEIF_BRAND_HEIC = {104, 101, 105, 99};
    private static final byte[] HEIF_BRAND_MIF1 = {109, 105, 102, 49};
    private static final byte[] HEIF_TYPE_FTYP = {102, 116, 121, 112};
    private static final byte[] IDENTIFIER_EXIF_APP1 = "Exif\u0000\u0000".getBytes(ASCII);
    private static final ExifTag[] IFD_EXIF_TAGS;
    private static final int IFD_FORMAT_BYTE = 1;
    /* access modifiers changed from: private */
    public static final int[] IFD_FORMAT_BYTES_PER_FORMAT = {0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8, 1};
    private static final int IFD_FORMAT_DOUBLE = 12;
    private static final int IFD_FORMAT_IFD = 13;
    /* access modifiers changed from: private */
    public static final String[] IFD_FORMAT_NAMES = {"", "BYTE", "STRING", "USHORT", "ULONG", "URATIONAL", "SBYTE", "UNDEFINED", "SSHORT", "SLONG", "SRATIONAL", "SINGLE", "DOUBLE"};
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
    private static final ExifTag[] IFD_THUMBNAIL_TAGS;
    private static final ExifTag[] IFD_TIFF_TAGS;
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
    private static final ExifTag JPEG_INTERCHANGE_FORMAT_LENGTH_TAG = new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, (int) RadioError.OEM_ERROR_14, 4);
    private static final ExifTag JPEG_INTERCHANGE_FORMAT_TAG = new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT, (int) RadioError.OEM_ERROR_13, 4);
    private static final byte[] JPEG_SIGNATURE = {MARKER, MARKER_SOI, MARKER};
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
    private static final ExifTag[] ORF_CAMERA_SETTINGS_TAGS = {new ExifTag(TAG_ORF_PREVIEW_IMAGE_START, 257, 4), new ExifTag(TAG_ORF_PREVIEW_IMAGE_LENGTH, 258, 4)};
    private static final ExifTag[] ORF_IMAGE_PROCESSING_TAGS = {new ExifTag(TAG_ORF_ASPECT_FRAME, 4371, 3)};
    private static final byte[] ORF_MAKER_NOTE_HEADER_1 = {79, 76, 89, 77, 80, 0};
    private static final int ORF_MAKER_NOTE_HEADER_1_SIZE = 8;
    private static final byte[] ORF_MAKER_NOTE_HEADER_2 = {79, 76, 89, 77, 80, 85, 83, 0, 73, 73};
    private static final int ORF_MAKER_NOTE_HEADER_2_SIZE = 12;
    private static final ExifTag[] ORF_MAKER_NOTE_TAGS = {new ExifTag(TAG_ORF_THUMBNAIL_IMAGE, 256, 7), new ExifTag(TAG_ORF_CAMERA_SETTINGS_IFD_POINTER, (int) MtpConstants.RESPONSE_SPECIFICATION_OF_DESTINATION_UNSUPPORTED, 4), new ExifTag(TAG_ORF_IMAGE_PROCESSING_IFD_POINTER, 8256, 4)};
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
    public static final String TAG_X_RESOLUTION = "XResolution";
    public static final String TAG_Y_CB_CR_COEFFICIENTS = "YCbCrCoefficients";
    public static final String TAG_Y_CB_CR_POSITIONING = "YCbCrPositioning";
    public static final String TAG_Y_CB_CR_SUB_SAMPLING = "YCbCrSubSampling";
    public static final String TAG_Y_RESOLUTION = "YResolution";
    public static final int WHITEBALANCE_AUTO = 0;
    public static final int WHITEBALANCE_MANUAL = 1;
    private static final Object lockObj = new Object();
    private static final String[] patterns = {"yyyy:MM:dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy.MM.dd HH:mm:ss"};
    private static final HashMap<Integer, Integer> sExifPointerTagMap = new HashMap<>();
    private static final HashMap[] sExifTagMapsForReading = new HashMap[EXIF_TAGS.length];
    private static final HashMap[] sExifTagMapsForWriting = new HashMap[EXIF_TAGS.length];
    private static SimpleDateFormat sFormatter;
    private static SimpleDateFormat[] sFormatters = new SimpleDateFormat[patterns.length];
    private static SimpleDateFormat[] sFormattersUTC = new SimpleDateFormat[patterns.length];
    private static final Pattern sGpsTimestampPattern = Pattern.compile("^([0-9][0-9]):([0-9][0-9]):([0-9][0-9])$");
    private static final Pattern sNonZeroTimePattern = Pattern.compile(".*[1-9].*");
    private static final HashSet<String> sTagSetForCompatibility = new HashSet<>(Arrays.asList(new String[]{"FNumber", TAG_DIGITAL_ZOOM_RATIO, TAG_EXPOSURE_TIME, TAG_SUBJECT_DISTANCE, TAG_GPS_TIMESTAMP}));
    private final AssetManager.AssetInputStream mAssetInputStream;
    private final HashMap[] mAttributes = new HashMap[EXIF_TAGS.length];
    private Set<Integer> mAttributesOffsets = new HashSet(EXIF_TAGS.length);
    private ByteOrder mExifByteOrder = ByteOrder.BIG_ENDIAN;
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
        /* access modifiers changed from: private */
        public final int mLength;
        /* access modifiers changed from: private */
        public int mPosition;

        public ByteOrderedDataInputStream(InputStream in) throws IOException {
            this.mByteOrder = ByteOrder.BIG_ENDIAN;
            this.mInputStream = in;
            this.mDataInputStream = new DataInputStream(in);
            this.mLength = this.mDataInputStream.available();
            this.mPosition = 0;
            this.mDataInputStream.mark(this.mLength);
        }

        public ByteOrderedDataInputStream(byte[] bytes) throws IOException {
            this((InputStream) new ByteArrayInputStream(bytes));
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
            if (this.mPosition <= this.mLength) {
                int ch = this.mDataInputStream.read();
                if (ch >= 0) {
                    return (byte) ch;
                }
                throw new EOFException();
            }
            throw new EOFException();
        }

        public short readShort() throws IOException {
            this.mPosition += 2;
            if (this.mPosition <= this.mLength) {
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
            } else {
                throw new EOFException();
            }
        }

        public int readInt() throws IOException {
            this.mPosition += 4;
            if (this.mPosition <= this.mLength) {
                int ch1 = this.mDataInputStream.read();
                int ch2 = this.mDataInputStream.read();
                int ch3 = this.mDataInputStream.read();
                int ch4 = this.mDataInputStream.read();
                if ((ch1 | ch2 | ch3 | ch4) < 0) {
                    throw new EOFException();
                } else if (this.mByteOrder == LITTLE_ENDIAN) {
                    return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1;
                } else {
                    if (this.mByteOrder == BIG_ENDIAN) {
                        return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4;
                    }
                    throw new IOException("Invalid byte order: " + this.mByteOrder);
                }
            } else {
                throw new EOFException();
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
            if (this.mPosition <= this.mLength) {
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
            } else {
                throw new EOFException();
            }
        }

        public long readUnsignedInt() throws IOException {
            return ((long) readInt()) & 4294967295L;
        }

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
                if ((ch1 | ch2 | ch3 | ch4 | ch5 | ch6 | ch7 | ch8) < 0) {
                    throw new EOFException();
                } else if (this.mByteOrder == LITTLE_ENDIAN) {
                    return (((long) ch8) << 56) + (((long) ch7) << 48) + (((long) ch6) << 40) + (((long) ch5) << 32) + (((long) ch4) << 24) + (((long) ch3) << 16) + (((long) ch2) << 8) + ((long) ch1);
                } else {
                    int ch22 = ch2;
                    if (this.mByteOrder == BIG_ENDIAN) {
                        return (((long) ch1) << 56) + (((long) ch22) << 48) + (((long) ch3) << 40) + (((long) ch4) << 32) + (((long) ch5) << 24) + (((long) ch6) << 16) + (((long) ch7) << 8) + ((long) ch8);
                    }
                    throw new IOException("Invalid byte order: " + this.mByteOrder);
                }
            } else {
                throw new EOFException();
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

        private ExifAttribute(int format2, int numberOfComponents2, byte[] bytes2) {
            this.format = format2;
            this.numberOfComponents = numberOfComponents2;
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

        /* access modifiers changed from: private */
        public Object getValue(ByteOrder byteOrder) {
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
                            while (true) {
                                if (index < this.numberOfComponents) {
                                    byte ch = this.bytes[index];
                                    if (ch != 0) {
                                        if (ch >= 32) {
                                            stringBuilder.append((char) ch);
                                        } else {
                                            stringBuilder.append('?');
                                        }
                                        index++;
                                    }
                                }
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
                            } catch (OutOfMemoryError e) {
                                Log.e(ExifInterface.TAG, "no enough memory for format=" + this.format + "and numberOfComponents=" + this.numberOfComponents);
                                return null;
                            }
                        case 5:
                            Rational[] values3 = new Rational[this.numberOfComponents];
                            while (i < this.numberOfComponents) {
                                Rational rational = new Rational(inputStream.readUnsignedInt(), inputStream.readUnsignedInt());
                                values3[i] = rational;
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
                                long numerator = (long) inputStream.readInt();
                                long denominator = (long) inputStream.readInt();
                                long j = denominator;
                                Rational rational2 = new Rational(numerator, denominator);
                                values6[i] = rational2;
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
                } catch (IOException e2) {
                    e = e2;
                }
                e = e2;
            } catch (IOException e3) {
                e = e3;
                ByteOrder byteOrder2 = byteOrder;
            }
            Log.w(ExifInterface.TAG, "IOException occurred during reading a value", e);
            return null;
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
            int i = 0;
            if (value instanceof long[]) {
                long[] array = (long[]) value;
                while (true) {
                    int i2 = i;
                    if (i2 >= array.length) {
                        return stringBuilder.toString();
                    }
                    stringBuilder.append(array[i2]);
                    if (i2 + 1 != array.length) {
                        stringBuilder.append(",");
                    }
                    i = i2 + 1;
                }
            } else if (value instanceof int[]) {
                int[] array2 = (int[]) value;
                while (true) {
                    int i3 = i;
                    if (i3 >= array2.length) {
                        return stringBuilder.toString();
                    }
                    stringBuilder.append(array2[i3]);
                    if (i3 + 1 != array2.length) {
                        stringBuilder.append(",");
                    }
                    i = i3 + 1;
                }
            } else if (value instanceof double[]) {
                double[] array3 = (double[]) value;
                while (true) {
                    int i4 = i;
                    if (i4 >= array3.length) {
                        return stringBuilder.toString();
                    }
                    stringBuilder.append(array3[i4]);
                    if (i4 + 1 != array3.length) {
                        stringBuilder.append(",");
                    }
                    i = i4 + 1;
                }
            } else if (!(value instanceof Rational[])) {
                return null;
            } else {
                Rational[] array4 = (Rational[]) value;
                while (true) {
                    int i5 = i;
                    if (i5 >= array4.length) {
                        return stringBuilder.toString();
                    }
                    stringBuilder.append(array4[i5].numerator);
                    stringBuilder.append('/');
                    stringBuilder.append(array4[i5].denominator);
                    if (i5 + 1 != array4.length) {
                        stringBuilder.append(",");
                    }
                    i = i5 + 1;
                }
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface IfdType {
    }

    private static class Rational {
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

    static {
        ExifTag exifTag = new ExifTag(TAG_IMAGE_WIDTH, 256, 3, 4);
        ExifTag exifTag2 = new ExifTag(TAG_IMAGE_LENGTH, 257, 3, 4);
        ExifTag exifTag3 = new ExifTag(TAG_STRIP_OFFSETS, 273, 3, 4);
        ExifTag exifTag4 = new ExifTag(TAG_ROWS_PER_STRIP, R.styleable.Theme_accessibilityFocusedDrawable, 3, 4);
        ExifTag exifTag5 = new ExifTag(TAG_STRIP_BYTE_COUNTS, R.styleable.Theme_actionModePopupWindowStyle, 3, 4);
        IFD_TIFF_TAGS = new ExifTag[]{new ExifTag(TAG_NEW_SUBFILE_TYPE, 254, 4), new ExifTag(TAG_SUBFILE_TYPE, 255, 4), exifTag, exifTag2, new ExifTag(TAG_BITS_PER_SAMPLE, 258, 3), new ExifTag(TAG_COMPRESSION, 259, 3), new ExifTag(TAG_PHOTOMETRIC_INTERPRETATION, 262, 3), new ExifTag(TAG_IMAGE_DESCRIPTION, 270, 2), new ExifTag(TAG_MAKE, 271, 2), new ExifTag(TAG_MODEL, 272, 2), exifTag3, new ExifTag(TAG_ORIENTATION, 274, 3), new ExifTag(TAG_SAMPLES_PER_PIXEL, 277, 3), exifTag4, exifTag5, new ExifTag(TAG_X_RESOLUTION, (int) R.styleable.Theme_alertDialogCenterButtons, 5), new ExifTag(TAG_Y_RESOLUTION, (int) R.styleable.Theme_autofillDatasetPickerMaxHeight, 5), new ExifTag(TAG_PLANAR_CONFIGURATION, (int) R.styleable.Theme_autofillDatasetPickerMaxWidth, 3), new ExifTag(TAG_RESOLUTION_UNIT, (int) R.styleable.Theme_floatingToolbarCloseDrawable, 3), new ExifTag(TAG_TRANSFER_FUNCTION, 301, 3), new ExifTag(TAG_SOFTWARE, 305, 2), new ExifTag(TAG_DATETIME, 306, 2), new ExifTag(TAG_ARTIST, (int) R.styleable.Theme_preferenceFrameLayoutStyle, 2), new ExifTag(TAG_WHITE_POINT, (int) R.styleable.Theme_preferencePanelStyle, 5), new ExifTag(TAG_PRIMARY_CHROMATICITIES, (int) R.styleable.Theme_progressBarCornerRadius, 5), new ExifTag(TAG_SUB_IFD_POINTER, (int) R.styleable.Theme_textColorSearchUrl, 4), new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT, (int) RadioError.OEM_ERROR_13, 4), new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, (int) RadioError.OEM_ERROR_14, 4), new ExifTag(TAG_Y_CB_CR_COEFFICIENTS, 529, 5), new ExifTag(TAG_Y_CB_CR_SUB_SAMPLING, 530, 3), new ExifTag(TAG_Y_CB_CR_POSITIONING, 531, 3), new ExifTag(TAG_REFERENCE_BLACK_WHITE, (int) BluetoothClass.Device.PHONE_ISDN, 5), new ExifTag(TAG_COPYRIGHT, 33432, 2), new ExifTag(TAG_EXIF_IFD_POINTER, 34665, 4), new ExifTag(TAG_GPS_INFO_IFD_POINTER, 34853, 4), new ExifTag(TAG_RW2_SENSOR_TOP_BORDER, 4, 4), new ExifTag(TAG_RW2_SENSOR_LEFT_BORDER, 5, 4), new ExifTag(TAG_RW2_SENSOR_BOTTOM_BORDER, 6, 4), new ExifTag(TAG_RW2_SENSOR_RIGHT_BORDER, 7, 4), new ExifTag(TAG_RW2_ISO, 23, 3), new ExifTag(TAG_RW2_JPG_FROM_RAW, 46, 7)};
        ExifTag exifTag6 = new ExifTag(TAG_PIXEL_X_DIMENSION, 40962, 3, 4);
        ExifTag exifTag7 = new ExifTag(TAG_PIXEL_Y_DIMENSION, 40963, 3, 4);
        ExifTag exifTag8 = new ExifTag(TAG_DEFAULT_CROP_SIZE, 50720, 3, 4);
        IFD_EXIF_TAGS = new ExifTag[]{new ExifTag(TAG_EXPOSURE_TIME, 33434, 5), new ExifTag("FNumber", 33437, 5), new ExifTag(TAG_EXPOSURE_PROGRAM, 34850, 3), new ExifTag(TAG_SPECTRAL_SENSITIVITY, 34852, 2), new ExifTag("ISOSpeedRatings", 34855, 3), new ExifTag(TAG_OECF, 34856, 7), new ExifTag(TAG_EXIF_VERSION, 36864, 2), new ExifTag(TAG_DATETIME_ORIGINAL, 36867, 2), new ExifTag(TAG_DATETIME_DIGITIZED, 36868, 2), new ExifTag(TAG_COMPONENTS_CONFIGURATION, 37121, 7), new ExifTag(TAG_COMPRESSED_BITS_PER_PIXEL, 37122, 5), new ExifTag(TAG_SHUTTER_SPEED_VALUE, 37377, 10), new ExifTag(TAG_APERTURE_VALUE, 37378, 5), new ExifTag(TAG_BRIGHTNESS_VALUE, 37379, 10), new ExifTag(TAG_EXPOSURE_BIAS_VALUE, 37380, 10), new ExifTag(TAG_MAX_APERTURE_VALUE, 37381, 5), new ExifTag(TAG_SUBJECT_DISTANCE, 37382, 5), new ExifTag(TAG_METERING_MODE, 37383, 3), new ExifTag(TAG_LIGHT_SOURCE, 37384, 3), new ExifTag(TAG_FLASH, 37385, 3), new ExifTag(TAG_FOCAL_LENGTH, 37386, 5), new ExifTag(TAG_SUBJECT_AREA, 37396, 3), new ExifTag(TAG_MAKER_NOTE, 37500, 7), new ExifTag(TAG_USER_COMMENT, 37510, 7), new ExifTag(TAG_SUBSEC_TIME, 37520, 2), new ExifTag("SubSecTimeOriginal", 37521, 2), new ExifTag("SubSecTimeDigitized", 37522, 2), new ExifTag(TAG_FLASHPIX_VERSION, 40960, 7), new ExifTag(TAG_COLOR_SPACE, 40961, 3), exifTag6, exifTag7, new ExifTag(TAG_RELATED_SOUND_FILE, 40964, 2), new ExifTag(TAG_INTEROPERABILITY_IFD_POINTER, 40965, 4), new ExifTag(TAG_FLASH_ENERGY, 41483, 5), new ExifTag(TAG_SPATIAL_FREQUENCY_RESPONSE, 41484, 7), new ExifTag(TAG_FOCAL_PLANE_X_RESOLUTION, 41486, 5), new ExifTag(TAG_FOCAL_PLANE_Y_RESOLUTION, 41487, 5), new ExifTag(TAG_FOCAL_PLANE_RESOLUTION_UNIT, 41488, 3), new ExifTag(TAG_SUBJECT_LOCATION, 41492, 3), new ExifTag(TAG_EXPOSURE_INDEX, 41493, 5), new ExifTag(TAG_SENSING_METHOD, 41495, 3), new ExifTag(TAG_FILE_SOURCE, 41728, 7), new ExifTag(TAG_SCENE_TYPE, 41729, 7), new ExifTag(TAG_CFA_PATTERN, 41730, 7), new ExifTag(TAG_CUSTOM_RENDERED, 41985, 3), new ExifTag(TAG_EXPOSURE_MODE, 41986, 3), new ExifTag(TAG_WHITE_BALANCE, 41987, 3), new ExifTag(TAG_DIGITAL_ZOOM_RATIO, 41988, 5), new ExifTag(TAG_FOCAL_LENGTH_IN_35MM_FILM, 41989, 3), new ExifTag(TAG_SCENE_CAPTURE_TYPE, 41990, 3), new ExifTag(TAG_GAIN_CONTROL, 41991, 3), new ExifTag(TAG_CONTRAST, 41992, 3), new ExifTag(TAG_SATURATION, 41993, 3), new ExifTag(TAG_SHARPNESS, 41994, 3), new ExifTag(TAG_DEVICE_SETTING_DESCRIPTION, 41995, 7), new ExifTag(TAG_SUBJECT_DISTANCE_RANGE, 41996, 3), new ExifTag(TAG_IMAGE_UNIQUE_ID, 42016, 2), new ExifTag(TAG_DNG_VERSION, 50706, 1), exifTag8};
        ExifTag exifTag9 = new ExifTag(TAG_THUMBNAIL_IMAGE_WIDTH, 256, 3, 4);
        ExifTag exifTag10 = new ExifTag(TAG_THUMBNAIL_IMAGE_LENGTH, 257, 3, 4);
        ExifTag exifTag11 = new ExifTag(TAG_STRIP_OFFSETS, 273, 3, 4);
        ExifTag exifTag12 = new ExifTag(TAG_ROWS_PER_STRIP, R.styleable.Theme_accessibilityFocusedDrawable, 3, 4);
        ExifTag exifTag13 = new ExifTag(TAG_STRIP_BYTE_COUNTS, R.styleable.Theme_actionModePopupWindowStyle, 3, 4);
        ExifTag exifTag14 = new ExifTag(TAG_DEFAULT_CROP_SIZE, 50720, 3, 4);
        IFD_THUMBNAIL_TAGS = new ExifTag[]{new ExifTag(TAG_NEW_SUBFILE_TYPE, 254, 4), new ExifTag(TAG_SUBFILE_TYPE, 255, 4), exifTag9, exifTag10, new ExifTag(TAG_BITS_PER_SAMPLE, 258, 3), new ExifTag(TAG_COMPRESSION, 259, 3), new ExifTag(TAG_PHOTOMETRIC_INTERPRETATION, 262, 3), new ExifTag(TAG_IMAGE_DESCRIPTION, 270, 2), new ExifTag(TAG_MAKE, 271, 2), new ExifTag(TAG_MODEL, 272, 2), exifTag11, new ExifTag(TAG_ORIENTATION, 274, 3), new ExifTag(TAG_SAMPLES_PER_PIXEL, 277, 3), exifTag12, exifTag13, new ExifTag(TAG_X_RESOLUTION, (int) R.styleable.Theme_alertDialogCenterButtons, 5), new ExifTag(TAG_Y_RESOLUTION, (int) R.styleable.Theme_autofillDatasetPickerMaxHeight, 5), new ExifTag(TAG_PLANAR_CONFIGURATION, (int) R.styleable.Theme_autofillDatasetPickerMaxWidth, 3), new ExifTag(TAG_RESOLUTION_UNIT, (int) R.styleable.Theme_floatingToolbarCloseDrawable, 3), new ExifTag(TAG_TRANSFER_FUNCTION, 301, 3), new ExifTag(TAG_SOFTWARE, 305, 2), new ExifTag(TAG_DATETIME, 306, 2), new ExifTag(TAG_ARTIST, (int) R.styleable.Theme_preferenceFrameLayoutStyle, 2), new ExifTag(TAG_WHITE_POINT, (int) R.styleable.Theme_preferencePanelStyle, 5), new ExifTag(TAG_PRIMARY_CHROMATICITIES, (int) R.styleable.Theme_progressBarCornerRadius, 5), new ExifTag(TAG_SUB_IFD_POINTER, (int) R.styleable.Theme_textColorSearchUrl, 4), new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT, (int) RadioError.OEM_ERROR_13, 4), new ExifTag(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, (int) RadioError.OEM_ERROR_14, 4), new ExifTag(TAG_Y_CB_CR_COEFFICIENTS, 529, 5), new ExifTag(TAG_Y_CB_CR_SUB_SAMPLING, 530, 3), new ExifTag(TAG_Y_CB_CR_POSITIONING, 531, 3), new ExifTag(TAG_REFERENCE_BLACK_WHITE, (int) BluetoothClass.Device.PHONE_ISDN, 5), new ExifTag(TAG_COPYRIGHT, 33432, 2), new ExifTag(TAG_EXIF_IFD_POINTER, 34665, 4), new ExifTag(TAG_GPS_INFO_IFD_POINTER, 34853, 4), new ExifTag(TAG_DNG_VERSION, 50706, 1), exifTag14};
        for (int i = 0; i < patterns.length; i++) {
            sFormatters[i] = new SimpleDateFormat(patterns[i]);
            sFormattersUTC[i] = new SimpleDateFormat(patterns[i]);
            sFormattersUTC[i].setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        for (int ifdType = 0; ifdType < EXIF_TAGS.length; ifdType++) {
            sExifTagMapsForReading[ifdType] = new HashMap();
            sExifTagMapsForWriting[ifdType] = new HashMap();
            for (ExifTag tag : EXIF_TAGS[ifdType]) {
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

    public ExifInterface(String filename) throws IOException {
        if (filename != null) {
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
        } else {
            throw new IllegalArgumentException("filename cannot be null");
        }
    }

    /* JADX WARNING: type inference failed for: r0v6, types: [android.content.res.AssetManager$AssetInputStream, java.io.FileDescriptor, java.lang.String] */
    /* JADX WARNING: type inference failed for: r0v7, types: [java.lang.AutoCloseable] */
    /* JADX WARNING: type inference failed for: r0v10 */
    /* JADX WARNING: Multi-variable type inference failed */
    public ExifInterface(FileDescriptor fileDescriptor) throws IOException {
        if (fileDescriptor != null) {
            ? r0 = 0;
            this.mAssetInputStream = r0;
            this.mFilename = r0;
            if (isSeekableFD(fileDescriptor)) {
                this.mSeekableFileDescriptor = fileDescriptor;
                try {
                    fileDescriptor = Os.dup(fileDescriptor);
                } catch (ErrnoException e) {
                    throw e.rethrowAsIOException();
                }
            } else {
                this.mSeekableFileDescriptor = r0;
            }
            this.mIsInputStream = false;
            try {
                FileInputStream in = new FileInputStream(fileDescriptor);
                loadAttributes(in);
                r0 = in;
            } finally {
                IoUtils.closeQuietly(r0);
            }
        } else {
            throw new IllegalArgumentException("fileDescriptor cannot be null");
        }
    }

    public ExifInterface(InputStream inputStream) throws IOException {
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
        throw new IllegalArgumentException("inputStream cannot be null");
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
        if (tag.equals("HwMakerNote")) {
            Log.d(TAG, "get hw makernote");
            return new String(attribute.bytes, Charset.forName("ISO-8859-1"));
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
                return String.format("%02d:%02d:%02d", new Object[]{Integer.valueOf((int) (((float) array[0].numerator) / ((float) array[0].denominator))), Integer.valueOf((int) (((float) array[1].numerator) / ((float) array[1].denominator))), Integer.valueOf((int) (((float) array[2].numerator) / ((float) array[2].denominator)))});
            }
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

    /* JADX WARNING: Code restructure failed: missing block: B:79:0x029e, code lost:
        r15 = 1;
     */
    public void setAttribute(String tag, String value) {
        String tag2;
        int i;
        int i2;
        int dataFormat;
        String str;
        String str2;
        String value2 = value;
        boolean isHwMnote = false;
        String str3 = tag;
        if (str3.equals("HwMakerNote")) {
            tag2 = TAG_MAKER_NOTE;
            isHwMnote = true;
        } else {
            tag2 = str3;
        }
        boolean isHwMnote2 = isHwMnote;
        int i3 = 2;
        int i4 = 1;
        if (value2 != null && sTagSetForCompatibility.contains(tag2)) {
            if (tag2.equals(TAG_GPS_TIMESTAMP)) {
                if (!sGpsTimestampPattern.matcher(value2).find()) {
                    Log.w(TAG, "Invalid value for " + tag2 + " : " + value2);
                    return;
                }
                value2 = Integer.parseInt(m.group(1)) + "/1," + Integer.parseInt(m.group(2)) + "/1," + Integer.parseInt(m.group(3)) + "/1";
            } else {
                try {
                    double doubleValue = Double.parseDouble(value);
                    value2 = ((long) (10000.0d * doubleValue)) + "/10000";
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid value for " + tag2 + " : " + value2);
                    return;
                }
            }
        }
        int i5 = 0;
        int i6 = 0;
        while (i6 < EXIF_TAGS.length) {
            if (i6 != 4 || this.mHasThumbnail) {
                Object obj = sExifTagMapsForWriting[i6].get(tag2);
                if (obj != null) {
                    if (value2 != null) {
                        ExifTag exifTag = (ExifTag) obj;
                        Pair<Integer, Integer> guess = guessDataFormat(value2);
                        if (exifTag.primaryFormat == ((Integer) guess.first).intValue() || exifTag.primaryFormat == ((Integer) guess.second).intValue()) {
                            dataFormat = exifTag.primaryFormat;
                        } else if (exifTag.secondaryFormat != -1 && (exifTag.secondaryFormat == ((Integer) guess.first).intValue() || exifTag.secondaryFormat == ((Integer) guess.second).intValue())) {
                            dataFormat = exifTag.secondaryFormat;
                        } else if (exifTag.primaryFormat == i4 || exifTag.primaryFormat == 7 || exifTag.primaryFormat == i3) {
                            dataFormat = exifTag.primaryFormat;
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Given tag (");
                            sb.append(tag2);
                            sb.append(") value didn't match with one of expected formats: ");
                            sb.append(IFD_FORMAT_NAMES[exifTag.primaryFormat]);
                            if (exifTag.secondaryFormat == -1) {
                                str = "";
                            } else {
                                str = ", " + IFD_FORMAT_NAMES[exifTag.secondaryFormat];
                            }
                            sb.append(str);
                            sb.append(" (guess: ");
                            sb.append(IFD_FORMAT_NAMES[((Integer) guess.first).intValue()]);
                            if (((Integer) guess.second).intValue() == -1) {
                                str2 = "";
                            } else {
                                str2 = ", " + IFD_FORMAT_NAMES[((Integer) guess.second).intValue()];
                            }
                            sb.append(str2);
                            sb.append(")");
                            Log.w(TAG, sb.toString());
                        }
                        switch (dataFormat) {
                            case 1:
                                i2 = i4;
                                i = i6;
                                Object obj2 = obj;
                                ExifTag exifTag2 = exifTag;
                                this.mAttributes[i].put(tag2, ExifAttribute.createByte(value2));
                                break;
                            case 2:
                            case 7:
                                i2 = i4;
                                i = i6;
                                Object obj3 = obj;
                                ExifTag exifTag3 = exifTag;
                                if (!isHwMnote2) {
                                    this.mAttributes[i].put(tag2, ExifAttribute.createString(value2));
                                    break;
                                } else {
                                    byte[] bytes = value2.getBytes(Charset.forName("ISO-8859-1"));
                                    this.mAttributes[i].put("HwMakerNote", new ExifAttribute(7, bytes.length, bytes));
                                    break;
                                }
                            case 3:
                                i2 = i4;
                                i = i6;
                                Object obj4 = obj;
                                ExifTag exifTag4 = exifTag;
                                String[] values = value2.split(",");
                                int[] intArray = new int[values.length];
                                for (int j = 0; j < values.length; j++) {
                                    intArray[j] = Integer.parseInt(values[j]);
                                }
                                this.mAttributes[i].put(tag2, ExifAttribute.createUShort(intArray, this.mExifByteOrder));
                                break;
                            case 4:
                                i2 = i4;
                                i = i6;
                                Object obj5 = obj;
                                ExifTag exifTag5 = exifTag;
                                String[] values2 = value2.split(",");
                                long[] longArray = new long[values2.length];
                                for (int j2 = 0; j2 < values2.length; j2++) {
                                    longArray[j2] = Long.parseLong(values2[j2]);
                                }
                                this.mAttributes[i].put(tag2, ExifAttribute.createULong(longArray, this.mExifByteOrder));
                                break;
                            case 5:
                                i = i6;
                                Object obj6 = obj;
                                String[] values3 = value2.split(",");
                                Rational[] rationalArray = new Rational[values3.length];
                                int j3 = 0;
                                while (j3 < values3.length) {
                                    String[] numbers = values3[j3].split("/");
                                    String[] strArr = numbers;
                                    Rational rational = new Rational((long) Double.parseDouble(numbers[0]), (long) Double.parseDouble(numbers[1]));
                                    rationalArray[j3] = rational;
                                    j3++;
                                    exifTag = exifTag;
                                }
                                i2 = 1;
                                this.mAttributes[i].put(tag2, ExifAttribute.createURational(rationalArray, this.mExifByteOrder));
                                break;
                            case 9:
                                i = i6;
                                Object obj7 = obj;
                                String[] values4 = value2.split(",");
                                int[] intArray2 = new int[values4.length];
                                for (int j4 = 0; j4 < values4.length; j4++) {
                                    intArray2[j4] = Integer.parseInt(values4[j4]);
                                }
                                this.mAttributes[i].put(tag2, ExifAttribute.createSLong(intArray2, this.mExifByteOrder));
                                break;
                            case 10:
                                String[] values5 = value2.split(",");
                                Rational[] rationalArray2 = new Rational[values5.length];
                                int j5 = i5;
                                while (j5 < values5.length) {
                                    String[] numbers2 = values5[j5].split("/");
                                    Rational rational2 = new Rational((long) Double.parseDouble(numbers2[i5]), (long) Double.parseDouble(numbers2[1]));
                                    rationalArray2[j5] = rational2;
                                    j5++;
                                    i6 = i6;
                                    obj = obj;
                                    i5 = 0;
                                }
                                i = i6;
                                Object obj8 = obj;
                                this.mAttributes[i].put(tag2, ExifAttribute.createSRational(rationalArray2, this.mExifByteOrder));
                                break;
                            case 12:
                                String[] values6 = value2.split(",");
                                double[] doubleArray = new double[values6.length];
                                for (int j6 = i5; j6 < values6.length; j6++) {
                                    doubleArray[j6] = Double.parseDouble(values6[j6]);
                                }
                                this.mAttributes[i6].put(tag2, ExifAttribute.createDouble(doubleArray, this.mExifByteOrder));
                                break;
                            default:
                                i2 = i4;
                                i = i6;
                                Object obj9 = obj;
                                ExifTag exifTag6 = exifTag;
                                Log.w(TAG, "Data format isn't one of expected formats: " + dataFormat);
                                break;
                        }
                    } else {
                        this.mAttributes[i6].remove(tag2);
                    }
                } else {
                    i2 = i4;
                    i = i6;
                }
                i6 = i + 1;
                i4 = i2;
                i3 = 2;
                i5 = 0;
            }
            i2 = i4;
            i = i6;
            i6 = i + 1;
            i4 = i2;
            i3 = 2;
            i5 = 0;
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
        int i = 0;
        while (i < EXIF_TAGS.length) {
            try {
                this.mAttributes[i] = new HashMap();
                i++;
            } catch (IOException e) {
                this.mIsSupportedFile = false;
            } catch (Throwable th) {
                addDefaultValuesForCompatibility();
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
        for (HashMap entrySet : this.mAttributes) {
            Log.d(TAG, "The size of tag group[" + i + "]: " + this.mAttributes[i].size());
            for (Map.Entry entry : entrySet.entrySet()) {
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
                    IoUtils.closeQuietly(null);
                    IoUtils.closeQuietly(null);
                    tempFile.delete();
                    throw th;
                }
            } catch (ErrnoException e2) {
                throw e2.rethrowAsIOException();
            } catch (Throwable th2) {
                IoUtils.closeQuietly(null);
                IoUtils.closeQuietly(null);
                throw th2;
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

    public byte[] getThumbnailBytes() {
        if (!this.mHasThumbnail) {
            return null;
        }
        if (this.mThumbnailBytes != null) {
            return this.mThumbnailBytes;
        }
        InputStream in = null;
        try {
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
                in = new FileInputStream(this.mFilename);
            } else if (this.mSeekableFileDescriptor != null) {
                FileDescriptor fileDescriptor = Os.dup(this.mSeekableFileDescriptor);
                Os.lseek(fileDescriptor, 0, OsConstants.SEEK_SET);
                in = new FileInputStream(fileDescriptor);
            }
            if (in == null) {
                throw new FileNotFoundException();
            } else if (in.skip((long) this.mThumbnailOffset) == ((long) this.mThumbnailOffset)) {
                byte[] buffer = new byte[this.mThumbnailLength];
                if (in.read(buffer) == this.mThumbnailLength) {
                    this.mThumbnailBytes = buffer;
                    return buffer;
                }
                throw new IOException("Corrupted image");
            } else {
                throw new IOException("Corrupted image");
            }
        } catch (ErrnoException | IOException e) {
            Log.d(TAG, "Encountered exception while getting thumbnail", e);
            return null;
        } finally {
            IoUtils.closeQuietly(in);
        }
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
                rgbValues[i] = (this.mThumbnailBytes[3 * i] << 16) + 0 + (this.mThumbnailBytes[(3 * i) + 1] << 8) + this.mThumbnailBytes[(3 * i) + 2];
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

    public long getDateTime() {
        String dateTimeString = getAttribute(TAG_DATETIME);
        if (dateTimeString == null || !sNonZeroTimePattern.matcher(dateTimeString).matches()) {
            return -1;
        }
        int i = 0;
        ParsePosition pos = new ParsePosition(0);
        Date datetime = null;
        try {
            synchronized (lockObj) {
                SimpleDateFormat[] simpleDateFormatArr = sFormatters;
                int length = simpleDateFormatArr.length;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    SimpleDateFormat sFormatter2 = simpleDateFormatArr[i];
                    sFormatter2.setTimeZone(TimeZone.getDefault());
                    datetime = sFormatter2.parse(dateTimeString, pos);
                    if (datetime != null) {
                        break;
                    }
                    i++;
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

    public long getDateTimeOriginal() {
        String dateTimeString = getAttribute(TAG_DATETIME_ORIGINAL);
        if (dateTimeString == null || !sNonZeroTimePattern.matcher(dateTimeString).matches()) {
            return -1;
        }
        int i = 0;
        ParsePosition pos = new ParsePosition(0);
        Date datetime = null;
        try {
            synchronized (lockObj) {
                SimpleDateFormat[] simpleDateFormatArr = sFormatters;
                int length = simpleDateFormatArr.length;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    SimpleDateFormat sFormatter2 = simpleDateFormatArr[i];
                    sFormatter2.setTimeZone(TimeZone.getDefault());
                    datetime = sFormatter2.parse(dateTimeString, pos);
                    if (datetime != null) {
                        break;
                    }
                    i++;
                }
            }
            if (datetime == null) {
                return -1;
            }
            long msecs = datetime.getTime();
            String subSecs = getAttribute("SubSecTimeOriginal");
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
        String date = getAttribute(TAG_GPS_DATESTAMP);
        String time = getAttribute(TAG_GPS_TIMESTAMP);
        if (date == null || time == null || (!sNonZeroTimePattern.matcher(date).matches() && !sNonZeroTimePattern.matcher(time).matches())) {
            return -1;
        }
        String dateTimeString = date + ' ' + time;
        ParsePosition pos = new ParsePosition(0);
        try {
            synchronized (lockObj) {
                for (SimpleDateFormat sFormatter2 : sFormattersUTC) {
                    Date datetime = sFormatter2.parse(dateTimeString, pos);
                    if (datetime != null) {
                        long time2 = datetime.getTime();
                        return time2;
                    }
                }
                return -1;
            }
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    public static float convertRationalLatLonToFloat(String rationalString, String ref) {
        try {
            String[] parts = rationalString.split(",");
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

    private boolean isHeifFormat(byte[] signatureCheckBytes) throws IOException {
        byte[] bArr = signatureCheckBytes;
        ByteOrderedDataInputStream signatureInputStream = null;
        try {
            signatureInputStream = new ByteOrderedDataInputStream(bArr);
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
            if (chunkSize > ((long) bArr.length)) {
                chunkSize = (long) bArr.length;
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
            ByteOrderedDataInputStream signatureInputStream2 = signatureInputStream;
            if (signatureInputStream2 != null) {
                signatureInputStream2.close();
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

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0084 A[SYNTHETIC] */
    private void getJpegAttributes(ByteOrderedDataInputStream in, int jpegOffset, int imageType) throws IOException {
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        in.seek((long) jpegOffset);
        int bytesRead = jpegOffset;
        byte readByte = in.readByte();
        byte marker = readByte;
        if (readByte == -1) {
            int bytesRead2 = bytesRead + 1;
            if (in.readByte() == -40) {
                int bytesRead3 = bytesRead2 + 1;
                while (true) {
                    if (in.readByte() == -1) {
                        byte marker2 = in.readByte();
                        int bytesRead4 = bytesRead3 + 1 + 1;
                        if (marker2 == -39 || marker2 == -38) {
                            in.setByteOrder(this.mExifByteOrder);
                        } else {
                            int length = in.readUnsignedShort() - 2;
                            int bytesRead5 = bytesRead4 + 2;
                            if (length >= 0) {
                                if (marker2 != -31) {
                                    if (marker2 != -2) {
                                        switch (marker2) {
                                            case -64:
                                            case -63:
                                            case -62:
                                            case -61:
                                                if (in.skipBytes(1) != 1) {
                                                }
                                                break;
                                            default:
                                                switch (marker2) {
                                                    case -59:
                                                    case -58:
                                                    case -57:
                                                        break;
                                                    default:
                                                        switch (marker2) {
                                                            case -55:
                                                            case -54:
                                                            case -53:
                                                                break;
                                                            default:
                                                                switch (marker2) {
                                                                    case -51:
                                                                    case -50:
                                                                    case -49:
                                                                        break;
                                                                }
                                                        }
                                                }
                                                if (in.skipBytes(1) != 1) {
                                                    this.mAttributes[imageType].put(TAG_IMAGE_LENGTH, ExifAttribute.createULong((long) in.readUnsignedShort(), this.mExifByteOrder));
                                                    this.mAttributes[imageType].put(TAG_IMAGE_WIDTH, ExifAttribute.createULong((long) in.readUnsignedShort(), this.mExifByteOrder));
                                                    length -= 5;
                                                    break;
                                                } else {
                                                    throw new IOException("Invalid SOFx");
                                                }
                                        }
                                    } else {
                                        byte[] bytes = new byte[length];
                                        if (in.read(bytes) == length) {
                                            length = 0;
                                            if (getAttribute(TAG_USER_COMMENT) == null) {
                                                this.mAttributes[1].put(TAG_USER_COMMENT, ExifAttribute.createString(new String(bytes, ASCII)));
                                            }
                                        } else {
                                            throw new IOException("Invalid exif");
                                        }
                                    }
                                } else if (length >= 6) {
                                    byte[] identifier = new byte[6];
                                    if (in.read(identifier) == 6) {
                                        bytesRead5 += 6;
                                        length -= 6;
                                        if (Arrays.equals(identifier, IDENTIFIER_EXIF_APP1)) {
                                            if (length > 0) {
                                                this.mExifOffset = bytesRead5;
                                                byte[] bytes2 = new byte[length];
                                                if (in.read(bytes2) == length) {
                                                    bytesRead5 += length;
                                                    length = 0;
                                                    readExifSegment(bytes2, imageType);
                                                } else {
                                                    throw new IOException("Invalid exif");
                                                }
                                            } else {
                                                throw new IOException("Invalid exif");
                                            }
                                        }
                                    } else {
                                        throw new IOException("Invalid exif");
                                    }
                                }
                                if (length < 0) {
                                    throw new IOException("Invalid length");
                                } else if (in.skipBytes(length) == length) {
                                    bytesRead3 = bytesRead5 + length;
                                } else {
                                    throw new IOException("Invalid JPEG segment");
                                }
                            } else {
                                throw new IOException("Invalid length");
                            }
                        }
                    } else {
                        throw new IOException("Invalid marker:" + Integer.toHexString(marker & MARKER));
                    }
                }
                in.setByteOrder(this.mExifByteOrder);
                return;
            }
            throw new IOException("Invalid marker: " + Integer.toHexString(marker & MARKER));
        }
        throw new IOException("Invalid marker: " + Integer.toHexString(marker & MARKER));
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
        ByteOrderedDataInputStream byteOrderedDataInputStream = in;
        byteOrderedDataInputStream.skipBytes(84);
        byte[] jpegOffsetBytes = new byte[4];
        byte[] cfaHeaderOffsetBytes = new byte[4];
        byteOrderedDataInputStream.read(jpegOffsetBytes);
        byteOrderedDataInputStream.skipBytes(4);
        byteOrderedDataInputStream.read(cfaHeaderOffsetBytes);
        int rafJpegOffset = ByteBuffer.wrap(jpegOffsetBytes).getInt();
        int rafCfaHeaderOffset = ByteBuffer.wrap(cfaHeaderOffsetBytes).getInt();
        getJpegAttributes(byteOrderedDataInputStream, rafJpegOffset, 5);
        byteOrderedDataInputStream.seek((long) rafCfaHeaderOffset);
        byteOrderedDataInputStream.setByteOrder(ByteOrder.BIG_ENDIAN);
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
            byteOrderedDataInputStream.skipBytes(numberOfBytes);
        }
    }

    private void getHeifAttributes(ByteOrderedDataInputStream in) throws IOException {
        final ByteOrderedDataInputStream byteOrderedDataInputStream = in;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource((MediaDataSource) new MediaDataSource() {
                long mPosition;

                public void close() throws IOException {
                }

                public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
                    if (size == 0) {
                        return 0;
                    }
                    if (position < 0) {
                        return -1;
                    }
                    try {
                        if (this.mPosition != position) {
                            byteOrderedDataInputStream.seek(position);
                            this.mPosition = position;
                        }
                        int bytesRead = byteOrderedDataInputStream.read(buffer, offset, size);
                        if (bytesRead >= 0) {
                            this.mPosition += (long) bytesRead;
                            return bytesRead;
                        }
                    } catch (IOException e) {
                    }
                    this.mPosition = -1;
                    return -1;
                }

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
            if (!(exifOffsetStr == null || exifLengthStr == null)) {
                int offset = Integer.parseInt(exifOffsetStr);
                int length = Integer.parseInt(exifLengthStr);
                if (length > 6) {
                    byteOrderedDataInputStream.seek((long) offset);
                    byte[] identifier = new byte[6];
                    if (byteOrderedDataInputStream.read(identifier) == 6) {
                        int offset2 = offset + 6;
                        int length2 = length - 6;
                        if (Arrays.equals(identifier, IDENTIFIER_EXIF_APP1)) {
                            byte[] bytes = new byte[length2];
                            if (byteOrderedDataInputStream.read(bytes) == length2) {
                                readExifSegment(bytes, 0);
                            } else {
                                String str = exifOffsetStr;
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
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        ByteOrderedDataOutputStream dataOutputStream = new ByteOrderedDataOutputStream(outputStream, ByteOrder.BIG_ENDIAN);
        if (dataInputStream.readByte() == -1) {
            dataOutputStream.writeByte(-1);
            if (dataInputStream.readByte() == -40) {
                dataOutputStream.writeByte(-40);
                dataOutputStream.writeByte(-1);
                dataOutputStream.writeByte(-31);
                writeExifSegment(dataOutputStream, 6);
                byte[] bytes = new byte[4096];
                while (dataInputStream.readByte() == -1) {
                    byte marker = dataInputStream.readByte();
                    if (marker != -31) {
                        switch (marker) {
                            case -39:
                            case RadioManager.STATUS_INVALID_OPERATION:
                                dataOutputStream.writeByte(-1);
                                dataOutputStream.writeByte(marker);
                                Streams.copy(dataInputStream, dataOutputStream);
                                return;
                            default:
                                dataOutputStream.writeByte(-1);
                                dataOutputStream.writeByte(marker);
                                int length = dataInputStream.readUnsignedShort();
                                dataOutputStream.writeUnsignedShort(length);
                                int length2 = length - 2;
                                if (length2 >= 0) {
                                    while (length2 > 0) {
                                        int read = dataInputStream.read(bytes, 0, Math.min(length2, bytes.length));
                                        int read2 = read;
                                        if (read < 0) {
                                            break;
                                        } else {
                                            dataOutputStream.write(bytes, 0, read2);
                                            length2 -= read2;
                                        }
                                    }
                                    break;
                                } else {
                                    throw new IOException("Invalid length");
                                }
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
                                int read3 = dataInputStream.read(bytes, 0, Math.min(length3, bytes.length));
                                int read4 = read3;
                                if (read3 >= 0) {
                                    dataOutputStream.write(bytes, 0, read4);
                                    length3 -= read4;
                                }
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
            return ByteOrder.LITTLE_ENDIAN;
        }
        if (byteOrder == 19789) {
            return ByteOrder.BIG_ENDIAN;
        }
        throw new IOException("Invalid byte order: " + Integer.toHexString(byteOrder));
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
            int firstIfdOffset2 = firstIfdOffset - 8;
            if (firstIfdOffset2 > 0 && dataInputStream.skipBytes(firstIfdOffset2) != firstIfdOffset2) {
                throw new IOException("Couldn't jump to first Ifd: " + firstIfdOffset2);
            }
            return;
        }
        throw new IOException("Invalid start code: " + Integer.toHexString(startCode));
    }

    private String getNewNameForMnote(byte[] value, int mnoteNum) {
        byte[] MNOTE_HW_HEADER = {72, 85, 65, 87, 69, 73, 0, 0};
        if (value.length >= MNOTE_HW_HEADER.length) {
            byte[] header = new byte[MNOTE_HW_HEADER.length];
            System.arraycopy(value, 0, header, 0, MNOTE_HW_HEADER.length);
            if (Arrays.equals(header, MNOTE_HW_HEADER)) {
                return "HwMakerNote";
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

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0088  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x008d  */
    private void readImageFileDirectory(ByteOrderedDataInputStream dataInputStream, int ifdType) throws IOException {
        int mnoteNum;
        short numberOfDirectoryEntry;
        long byteCount;
        int numberOfComponents;
        int dataFormat;
        ByteOrderedDataInputStream byteOrderedDataInputStream = dataInputStream;
        int i = ifdType;
        if (dataInputStream.mPosition + 2 <= dataInputStream.mLength) {
            short numberOfDirectoryEntry2 = dataInputStream.readShort();
            if (dataInputStream.mPosition + (12 * numberOfDirectoryEntry2) > dataInputStream.mLength) {
            } else if (numberOfDirectoryEntry2 <= 0) {
                short s = numberOfDirectoryEntry2;
            } else {
                int mnoteNum2 = 0;
                short i2 = 0;
                while (i2 < numberOfDirectoryEntry2) {
                    int tagNumber = dataInputStream.readUnsignedShort();
                    int dataFormat2 = dataInputStream.readUnsignedShort();
                    int numberOfComponents2 = dataInputStream.readInt();
                    long nextEntryOffset = (long) (dataInputStream.peek() + 4);
                    ExifTag tag = (ExifTag) sExifTagMapsForReading[i].get(Integer.valueOf(tagNumber));
                    boolean valid = false;
                    if (tag != null) {
                        if (dataFormat2 <= 0) {
                            numberOfDirectoryEntry = numberOfDirectoryEntry2;
                            mnoteNum = mnoteNum2;
                            byteCount = 0;
                            if (!valid) {
                            }
                            mnoteNum2 = mnoteNum;
                            i2 = (short) (i2 + 1);
                            numberOfDirectoryEntry2 = numberOfDirectoryEntry;
                            i = ifdType;
                        } else if (dataFormat2 < IFD_FORMAT_BYTES_PER_FORMAT.length) {
                            numberOfDirectoryEntry = numberOfDirectoryEntry2;
                            mnoteNum = mnoteNum2;
                            long byteCount2 = ((long) numberOfComponents2) * ((long) IFD_FORMAT_BYTES_PER_FORMAT[dataFormat2]);
                            if (byteCount2 >= 0 && byteCount2 <= 2147483647L) {
                                valid = true;
                            }
                            byteCount = byteCount2;
                            if (!valid) {
                                byteOrderedDataInputStream.seek(nextEntryOffset);
                            } else {
                                if (byteCount > 4) {
                                    int offset = dataInputStream.readInt();
                                    if (this.mMimeType != 7) {
                                        dataFormat = dataFormat2;
                                        numberOfComponents = numberOfComponents2;
                                        if (this.mMimeType == 10 && tag.name == TAG_RW2_JPG_FROM_RAW) {
                                            this.mRw2JpgFromRawOffset = offset;
                                        }
                                    } else if (tag.name == TAG_MAKER_NOTE) {
                                        this.mOrfMakerNoteOffset = offset;
                                        dataFormat = dataFormat2;
                                        numberOfComponents = numberOfComponents2;
                                    } else if (i == 6 && tag.name == TAG_ORF_THUMBNAIL_IMAGE) {
                                        this.mOrfThumbnailOffset = offset;
                                        this.mOrfThumbnailLength = numberOfComponents2;
                                        ExifAttribute compressionAttribute = ExifAttribute.createUShort(6, this.mExifByteOrder);
                                        dataFormat = dataFormat2;
                                        numberOfComponents = numberOfComponents2;
                                        ExifAttribute jpegInterchangeFormatAttribute = ExifAttribute.createULong((long) this.mOrfThumbnailOffset, this.mExifByteOrder);
                                        ExifAttribute jpegInterchangeFormatLengthAttribute = ExifAttribute.createULong((long) this.mOrfThumbnailLength, this.mExifByteOrder);
                                        this.mAttributes[4].put(TAG_COMPRESSION, compressionAttribute);
                                        this.mAttributes[4].put(TAG_JPEG_INTERCHANGE_FORMAT, jpegInterchangeFormatAttribute);
                                        this.mAttributes[4].put(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, jpegInterchangeFormatLengthAttribute);
                                    } else {
                                        dataFormat = dataFormat2;
                                        numberOfComponents = numberOfComponents2;
                                    }
                                    if (((long) offset) + byteCount <= ((long) dataInputStream.mLength)) {
                                        byteOrderedDataInputStream.seek((long) offset);
                                    } else {
                                        Log.w(TAG, "Skip the tag entry since data offset is invalid: " + offset);
                                        byteOrderedDataInputStream.seek(nextEntryOffset);
                                    }
                                } else {
                                    dataFormat = dataFormat2;
                                    numberOfComponents = numberOfComponents2;
                                }
                                Integer nextIfdType = sExifPointerTagMap.get(Integer.valueOf(tagNumber));
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
                                    } else if (!this.mAttributesOffsets.contains(Integer.valueOf((int) offset2))) {
                                        this.mAttributesOffsets.add(Integer.valueOf(dataInputStream.mPosition));
                                        byteOrderedDataInputStream.seek(offset2);
                                        readImageFileDirectory(byteOrderedDataInputStream, nextIfdType.intValue());
                                    } else {
                                        Log.w(TAG, "Skip jump into the IFD since it has already been read: IfdType " + nextIfdType + " (at " + offset2 + ")");
                                    }
                                    byteOrderedDataInputStream.seek(nextEntryOffset);
                                } else {
                                    byte[] bytes = new byte[((int) byteCount)];
                                    byteOrderedDataInputStream.readFully(bytes);
                                    ExifAttribute attribute = new ExifAttribute(dataFormat, numberOfComponents, bytes);
                                    String newName = tag.name;
                                    long j = byteCount;
                                    if (tag.name == TAG_MAKER_NOTE && i == 1) {
                                        int mnoteNum3 = mnoteNum;
                                        newName = getNewNameForMnote(bytes, mnoteNum3);
                                        mnoteNum2 = mnoteNum3 + 1;
                                    } else {
                                        mnoteNum2 = mnoteNum;
                                    }
                                    this.mAttributes[i].put(newName, attribute);
                                    if (tag.name == TAG_DNG_VERSION) {
                                        this.mMimeType = 3;
                                    }
                                    if (((tag.name == TAG_MAKE || tag.name == TAG_MODEL) && attribute.getStringValue(this.mExifByteOrder).contains(PEF_SIGNATURE)) || (tag.name == TAG_COMPRESSION && attribute.getIntValue(this.mExifByteOrder) == 65535)) {
                                        this.mMimeType = 8;
                                    }
                                    if (((long) dataInputStream.peek()) != nextEntryOffset) {
                                        byteOrderedDataInputStream.seek(nextEntryOffset);
                                    }
                                    i2 = (short) (i2 + 1);
                                    numberOfDirectoryEntry2 = numberOfDirectoryEntry;
                                    i = ifdType;
                                }
                            }
                            mnoteNum2 = mnoteNum;
                            i2 = (short) (i2 + 1);
                            numberOfDirectoryEntry2 = numberOfDirectoryEntry;
                            i = ifdType;
                        }
                    }
                    numberOfDirectoryEntry = numberOfDirectoryEntry2;
                    mnoteNum = mnoteNum2;
                    byteCount = 0;
                    if (!valid) {
                    }
                    mnoteNum2 = mnoteNum;
                    i2 = (short) (i2 + 1);
                    numberOfDirectoryEntry2 = numberOfDirectoryEntry;
                    i = ifdType;
                }
                if (dataInputStream.peek() + 4 <= dataInputStream.mLength) {
                    int nextIfdOffset = dataInputStream.readInt();
                    if (((long) nextIfdOffset) <= 0 || nextIfdOffset >= dataInputStream.mLength) {
                        Log.w(TAG, "Stop reading file since a wrong offset may cause an infinite loop: " + nextIfdOffset);
                    } else if (!this.mAttributesOffsets.contains(Integer.valueOf(nextIfdOffset))) {
                        this.mAttributesOffsets.add(Integer.valueOf(dataInputStream.mPosition));
                        byteOrderedDataInputStream.seek((long) nextIfdOffset);
                        if (this.mAttributes[4].isEmpty()) {
                            readImageFileDirectory(byteOrderedDataInputStream, 4);
                        } else if (this.mAttributes[5].isEmpty()) {
                            readImageFileDirectory(byteOrderedDataInputStream, 5);
                        }
                    } else {
                        Log.w(TAG, "Stop reading file since re-reading an IFD may cause an infinite loop: " + nextIfdOffset);
                    }
                }
            }
        }
    }

    private void retrieveJpegImageSize(ByteOrderedDataInputStream in, int imageType) throws IOException {
        ExifAttribute imageLengthAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_IMAGE_LENGTH);
        ExifAttribute imageWidthAttribute = (ExifAttribute) this.mAttributes[imageType].get(TAG_IMAGE_WIDTH);
        if (imageLengthAttribute == null || imageWidthAttribute == null) {
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
            int i = this.mThumbnailCompression;
            if (i != 1) {
                switch (i) {
                    case 6:
                        handleThumbnailFromJfif(in, thumbnailData);
                        return;
                    case 7:
                        break;
                    default:
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
        ByteOrderedDataInputStream byteOrderedDataInputStream = in;
        HashMap hashMap = thumbnailData;
        ExifAttribute stripOffsetsAttribute = (ExifAttribute) hashMap.get(TAG_STRIP_OFFSETS);
        ExifAttribute stripByteCountsAttribute = (ExifAttribute) hashMap.get(TAG_STRIP_BYTE_COUNTS);
        if (stripOffsetsAttribute == null || stripByteCountsAttribute == null) {
        } else {
            long[] stripOffsets = convertToLongArray(stripOffsetsAttribute.getValue(this.mExifByteOrder));
            long[] stripByteCounts = convertToLongArray(stripByteCountsAttribute.getValue(this.mExifByteOrder));
            if (stripOffsets == null) {
                Log.w(TAG, "stripOffsets should not be null.");
            } else if (stripByteCounts == null) {
                Log.w(TAG, "stripByteCounts should not be null.");
            } else {
                byte[] totalStripBytes = new byte[((int) Arrays.stream(stripByteCounts).sum())];
                int bytesAdded = 0;
                int bytesRead = 0;
                int i = 0;
                while (i < stripOffsets.length) {
                    int stripByteCount = (int) stripByteCounts[i];
                    int skipBytes = ((int) stripOffsets[i]) - bytesRead;
                    if (skipBytes < 0) {
                        Log.d(TAG, "Invalid strip offset value");
                    }
                    byteOrderedDataInputStream.seek((long) skipBytes);
                    byte[] stripBytes = new byte[stripByteCount];
                    byteOrderedDataInputStream.read(stripBytes);
                    bytesRead = bytesRead + skipBytes + stripByteCount;
                    System.arraycopy(stripBytes, 0, totalStripBytes, bytesAdded, stripBytes.length);
                    bytesAdded += stripBytes.length;
                    i++;
                    stripOffsetsAttribute = stripOffsetsAttribute;
                    HashMap hashMap2 = thumbnailData;
                }
                this.mHasThumbnail = true;
                this.mThumbnailBytes = totalStripBytes;
                this.mThumbnailLength = totalStripBytes.length;
            }
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
            ExifAttribute exifAttribute = defaultCropSizeAttribute;
        } else if (topBorderAttribute == null || leftBorderAttribute == null || bottomBorderAttribute == null || rightBorderAttribute == null) {
            retrieveJpegImageSize(in, imageType);
        } else {
            int topBorderValue = topBorderAttribute.getIntValue(this.mExifByteOrder);
            int bottomBorderValue = bottomBorderAttribute.getIntValue(this.mExifByteOrder);
            int rightBorderValue = rightBorderAttribute.getIntValue(this.mExifByteOrder);
            int leftBorderValue = leftBorderAttribute.getIntValue(this.mExifByteOrder);
            if (bottomBorderValue <= topBorderValue || rightBorderValue <= leftBorderValue) {
                return;
            }
            ExifAttribute imageLengthAttribute = ExifAttribute.createUShort(bottomBorderValue - topBorderValue, this.mExifByteOrder);
            ExifAttribute imageWidthAttribute = ExifAttribute.createUShort(rightBorderValue - leftBorderValue, this.mExifByteOrder);
            ExifAttribute exifAttribute2 = defaultCropSizeAttribute;
            this.mAttributes[imageType].put(TAG_IMAGE_LENGTH, imageLengthAttribute);
            this.mAttributes[imageType].put(TAG_IMAGE_WIDTH, imageWidthAttribute);
        }
    }

    private int writeExifSegment(ByteOrderedDataOutputStream dataOutputStream, int exifOffsetFromBeginning) throws IOException {
        int totalSize;
        int[] ifdDataSizes;
        boolean z;
        int totalSize2;
        ByteOrderedDataOutputStream byteOrderedDataOutputStream = dataOutputStream;
        int[] ifdOffsets = new int[EXIF_TAGS.length];
        int[] ifdDataSizes2 = new int[EXIF_TAGS.length];
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
        int i = 2;
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
        for (int i2 = 0; i2 < EXIF_TAGS.length; i2++) {
            int sum = 0;
            for (Map.Entry entry2 : this.mAttributes[i2].entrySet()) {
                int size = ((ExifAttribute) entry2.getValue()).size();
                if (size > 4) {
                    sum += size;
                }
            }
            ifdDataSizes2[i2] = ifdDataSizes2[i2] + sum;
        }
        int position = 8;
        for (int ifdType2 = 0; ifdType2 < EXIF_TAGS.length; ifdType2++) {
            if (!this.mAttributes[ifdType2].isEmpty()) {
                ifdOffsets[ifdType2] = position;
                position += (this.mAttributes[ifdType2].size() * 12) + 2 + 4 + ifdDataSizes2[ifdType2];
            }
        }
        if (this.mHasThumbnail != 0) {
            int thumbnailOffset = position;
            this.mAttributes[4].put(JPEG_INTERCHANGE_FORMAT_TAG.name, ExifAttribute.createULong((long) thumbnailOffset, this.mExifByteOrder));
            this.mThumbnailOffset = exifOffsetFromBeginning + thumbnailOffset;
            position += this.mThumbnailLength;
        }
        int totalSize3 = position + 8;
        if (!this.mAttributes[1].isEmpty()) {
            this.mAttributes[0].put(EXIF_POINTER_TAGS[1].name, ExifAttribute.createULong((long) ifdOffsets[1], this.mExifByteOrder));
        }
        if (!this.mAttributes[2].isEmpty()) {
            this.mAttributes[0].put(EXIF_POINTER_TAGS[2].name, ExifAttribute.createULong((long) ifdOffsets[2], this.mExifByteOrder));
        }
        if (!this.mAttributes[3].isEmpty()) {
            this.mAttributes[1].put(EXIF_POINTER_TAGS[3].name, ExifAttribute.createULong((long) ifdOffsets[3], this.mExifByteOrder));
        }
        byteOrderedDataOutputStream.writeUnsignedShort(totalSize3);
        byteOrderedDataOutputStream.write(IDENTIFIER_EXIF_APP1);
        byteOrderedDataOutputStream.writeShort(this.mExifByteOrder == ByteOrder.BIG_ENDIAN ? BYTE_ALIGN_MM : BYTE_ALIGN_II);
        byteOrderedDataOutputStream.setByteOrder(this.mExifByteOrder);
        byteOrderedDataOutputStream.writeUnsignedShort(42);
        byteOrderedDataOutputStream.writeUnsignedInt(8);
        int ifdType3 = 0;
        while (ifdType3 < EXIF_TAGS.length) {
            if (!this.mAttributes[ifdType3].isEmpty()) {
                byteOrderedDataOutputStream.writeUnsignedShort(this.mAttributes[ifdType3].size());
                int dataOffset = ifdOffsets[ifdType3] + i + (this.mAttributes[ifdType3].size() * 12) + 4;
                for (Map.Entry entry3 : this.mAttributes[ifdType3].entrySet()) {
                    String key = (String) entry3.getKey();
                    if (!(key == null || key.indexOf(TAG_MAKER_NOTE) == -1)) {
                        key = TAG_MAKER_NOTE;
                    }
                    int tagNumber = ((ExifTag) sExifTagMapsForWriting[ifdType3].get(key)).number;
                    ExifAttribute attribute = (ExifAttribute) entry3.getValue();
                    int size2 = attribute.size();
                    byteOrderedDataOutputStream.writeUnsignedShort(tagNumber);
                    int[] ifdDataSizes3 = ifdDataSizes2;
                    byteOrderedDataOutputStream.writeUnsignedShort(attribute.format);
                    byteOrderedDataOutputStream.writeInt(attribute.numberOfComponents);
                    if (size2 > 4) {
                        totalSize2 = totalSize3;
                        byteOrderedDataOutputStream.writeUnsignedInt((long) dataOffset);
                        dataOffset += size2;
                    } else {
                        totalSize2 = totalSize3;
                        byteOrderedDataOutputStream.write(attribute.bytes);
                        if (size2 < 4) {
                            int i3 = size2;
                            for (int i4 = 4; i3 < i4; i4 = 4) {
                                byteOrderedDataOutputStream.writeByte(0);
                                i3++;
                            }
                        }
                    }
                    ifdDataSizes2 = ifdDataSizes3;
                    totalSize3 = totalSize2;
                }
                ifdDataSizes = ifdDataSizes2;
                totalSize = totalSize3;
                if (ifdType3 != 0 || this.mAttributes[4].isEmpty()) {
                    byteOrderedDataOutputStream.writeUnsignedInt(0);
                } else {
                    byteOrderedDataOutputStream.writeUnsignedInt((long) ifdOffsets[4]);
                }
                for (Map.Entry entry4 : this.mAttributes[ifdType3].entrySet()) {
                    ExifAttribute attribute2 = (ExifAttribute) entry4.getValue();
                    if (attribute2.bytes.length > 4) {
                        byteOrderedDataOutputStream.write(attribute2.bytes, 0, attribute2.bytes.length);
                    }
                }
                z = false;
            } else {
                ifdDataSizes = ifdDataSizes2;
                totalSize = totalSize3;
                z = z2;
            }
            ifdType3++;
            z2 = z;
            ifdDataSizes2 = ifdDataSizes;
            totalSize3 = totalSize;
            i = 2;
        }
        int totalSize4 = totalSize3;
        if (this.mHasThumbnail) {
            byteOrderedDataOutputStream.write(getThumbnailBytes());
        }
        byteOrderedDataOutputStream.setByteOrder(ByteOrder.BIG_ENDIAN);
        return totalSize4;
    }

    public void saveAttributesFromHeicToJpg(String jpgPath) throws IOException {
        if (!this.mIsSupportedFile || this.mMimeType != 12) {
            throw new IOException("only supports saving attributes on HEIF formats.");
        } else if (jpgPath == null) {
            Log.e(TAG, "jpgPath is null");
        } else {
            ExternalStorageFileOutputStreamImpl out = null;
            ExternalStorageFileImpl tempFile = null;
            FileInputStream jpgPathStream = null;
            if (jpgPath != null) {
                tempFile = new ExternalStorageFileImpl(jpgPath + ".tmp");
                if (!tempFile.exists()) {
                    tempFile.createNewFile();
                } else {
                    throw new IOException("tmp file existed");
                }
            }
            if (tempFile != null) {
                try {
                    out = new ExternalStorageFileOutputStreamImpl(tempFile);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to write");
                } catch (Throwable th) {
                    IoUtils.closeQuietly(null);
                    IoUtils.closeQuietly(null);
                    throw th;
                }
            }
            ByteOrderedDataOutputStream dataOutputStream = new ByteOrderedDataOutputStream(out, ByteOrder.BIG_ENDIAN);
            dataOutputStream.writeByte(-1);
            dataOutputStream.writeByte(-40);
            dataOutputStream.writeByte(-1);
            dataOutputStream.writeByte(-31);
            writeExifSegment(dataOutputStream, 6);
            ExternalStorageFileImpl oldFile = new ExternalStorageFileImpl(jpgPath);
            jpgPathStream = new FileInputStream(oldFile);
            jpgPathStream.skip(2);
            Streams.copy(jpgPathStream, out);
            if (!tempFile.renameTo(oldFile)) {
                tempFile.delete();
                Log.e(TAG, "Could'nt rename tempFile ");
            }
            IoUtils.closeQuietly(out);
            IoUtils.closeQuietly(jpgPathStream);
        }
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
