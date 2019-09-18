package android.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.os.SystemProperties;
import android.os.storage.ExternalStorageFileImpl;
import android.os.storage.ExternalStorageFileOutputStreamImpl;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HeifUtils {
    private static final int DEFAULT_JPG_QUALITY = 90;
    public static final int ERROR_DECODE = -8;
    public static final int ERROR_EXCEED_JPG_MAX_SIZE = -7;
    public static final int ERROR_HEIF_FILE_NOT_FOUND = -5;
    public static final int ERROR_JPG_FILE_EXISTED = -6;
    public static final int ERROR_OK = 0;
    public static final int ERROR_PARAM_HEIF_FORMAT_WRONG = -3;
    public static final int ERROR_PARAM_HEIF_PATH_NULL = -1;
    public static final int ERROR_PARAM_JPG_FORMAT_WORNG = -4;
    public static final int ERROR_PARAM_JPG_PATH_NULL = -2;
    private static final String FILE_EXTENSION_HEIC = ".HEIC";
    private static final String FILE_EXTENSION_HEIF = ".HEIF";
    private static final String FILE_EXTENSION_JPG = ".JPG";
    private static final String HEIF_MIME_TYPE = "image/heif";
    private static final int JPG_MAX_HEIGHT = 65535;
    private static final int JPG_MAX_WIDTH = 65535;
    private static final int SAMPLE_SIZE = 1;
    private static String TAG = "HeifUtils";

    public static int convertHeifToJpg(String heifPath, String jpgPath) throws IOException {
        if (heifPath == null) {
            Log.e(TAG, "Parameter error : heif file path is null.");
            return -1;
        } else if (heifPath.toUpperCase().endsWith(FILE_EXTENSION_HEIC) || heifPath.toUpperCase().endsWith(FILE_EXTENSION_HEIF)) {
            File heifFile = new File(heifPath);
            if (!heifFile.isFile() || !heifFile.exists()) {
                Log.e(TAG, "IOException : heif file does not exist.");
                return -5;
            } else if (jpgPath == null) {
                Log.e(TAG, "Parameter error : jpg file path is null.");
                return -2;
            } else if (!jpgPath.toUpperCase().endsWith(FILE_EXTENSION_JPG)) {
                Log.e(TAG, "Parameter error : jpg format is wrong.");
                return -4;
            } else {
                ExternalStorageFileImpl jpgFile = new ExternalStorageFileImpl(jpgPath);
                if (jpgFile.exists()) {
                    Log.e(TAG, "IOException : jpg file existed.");
                    return -6;
                }
                InputStream heifStream = new FileInputStream(heifFile);
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inSampleSize = 1;
                option.inPreferredColorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
                option.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(heifStream, null, option);
                try {
                    heifStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "IO close Exception");
                }
                if (option.outHeight > 65535 || option.outWidth > 65535) {
                    Log.e(TAG, "Heif file size exceeds the max size of jpg file.");
                    return -7;
                } else if (option.outMimeType == null || !option.outMimeType.equals(HEIF_MIME_TYPE)) {
                    Log.e(TAG, "Parameter error : heif format is wrong.");
                    return -3;
                } else {
                    OutputStream jpgStream = null;
                    try {
                        jpgFile.createNewFile();
                        jpgStream = new ExternalStorageFileOutputStreamImpl(jpgFile);
                        boolean hardDecodingResult = false;
                        if (supportHeifHardDecoding()) {
                            hardDecodingResult = BitmapFactory.convertHeifToJpeg(heifPath, jpgStream);
                        }
                        if (!hardDecodingResult) {
                            option.inJustDecodeBounds = false;
                            heifStream = new FileInputStream(heifFile);
                            Bitmap bm = BitmapFactory.decodeStream(heifStream, null, option);
                            if (bm == null) {
                                Log.e(TAG, "Parameter error : heif file could not be decoded.");
                                jpgStream.flush();
                                jpgStream.close();
                                OutputStream jpgStream2 = null;
                                if (jpgStream2 != null) {
                                    try {
                                        jpgStream2.flush();
                                        jpgStream2.close();
                                    } catch (IOException e2) {
                                        Log.e(TAG, "IO close Exception for JPG stream");
                                    }
                                }
                                try {
                                    heifStream.close();
                                } catch (IOException e3) {
                                    Log.e(TAG, "IO close Exception for HEIF stream");
                                }
                                return -8;
                            }
                            bm.compress(Bitmap.CompressFormat.JPEG, 90, jpgStream);
                        }
                        try {
                            jpgStream.flush();
                            jpgStream.close();
                        } catch (IOException e4) {
                            Log.e(TAG, "IO close Exception for JPG stream");
                        }
                        try {
                            heifStream.close();
                        } catch (IOException e5) {
                            Log.e(TAG, "IO close Exception for HEIF stream");
                        }
                        try {
                            new ExifInterface(heifPath).saveAttributesFromHeicToJpg(jpgPath);
                        } catch (IOException e6) {
                            Log.e(TAG, "IO save Exception");
                        }
                        HwMediaMonitorManager.writeBigData(HwMediaMonitorUtils.BD_MEDIA_HEIF, HwMediaMonitorUtils.M_HEIF_TRANSCODE);
                        return 0;
                    } catch (IOException e7) {
                        Log.e(TAG, "failed to convert heif file to jpg");
                        throw e7;
                    } catch (Throwable th) {
                        if (jpgStream != null) {
                            try {
                                jpgStream.flush();
                                jpgStream.close();
                            } catch (IOException e8) {
                                Log.e(TAG, "IO close Exception for JPG stream");
                            }
                        }
                        try {
                            heifStream.close();
                        } catch (IOException e9) {
                            Log.e(TAG, "IO close Exception for HEIF stream");
                        }
                        throw th;
                    }
                }
            }
        } else {
            Log.e(TAG, "Parameter error : heif format is wrong.");
            return -3;
        }
    }

    private static boolean supportHeifHardDecoding() {
        String platform = SystemProperties.get("ro.board.platform", "unknown");
        String hardware = SystemProperties.get("ro.hardware", "unknown");
        if ((platform == null || !platform.startsWith("kirin980")) && (hardware == null || !hardware.startsWith("kirin980"))) {
            return false;
        }
        return true;
    }
}
