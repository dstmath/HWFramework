package android.media;

import android.common.HwMediaScannerManager;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.media.MediaScanner.MyMediaScannerClient;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import huawei.android.provider.HwSettings.System;
import huawei.com.android.internal.widget.HwFragmentContainer;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.Character.UnicodeBlock;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Pattern;

public class HwMediaScannerImpl implements HwMediaScannerManager {
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private static final int DEFAULT_THRESHOLD_MAX_BYTES = 524288000;
    private static final boolean ENABLE_BULK_INSERTS = true;
    private static final Uri EXTERNAL_AUDIO_URI = null;
    private static final Uri EXTERNAL_IMAGE_URI = null;
    private static final Uri EXTERNAL_VIDEO_URI = null;
    private static final String HW_ALLFOCUS_IMAGE_COLUMN = "hw_image_refocus";
    private static final int HW_ALLFOCUS_IMAGE_TYPE_DUAL_CAMERA = 2;
    private static final int HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA = 1;
    private static final int HW_CUSTOM_IMAGE_TAG_LEN = 20;
    private static final int HW_DUAL_CAMERA_ALLFOCUS_IMAGE_LEN = 8;
    private static final String HW_DUAL_CAMERA_ALLFOCUS_IMAGE_TAG = "DepthEn\u0000";
    private static final String HW_RECTIFY_IMAGE_COLUMN = "hw_rectify_offset";
    private static final String HW_RECTIFY_IMAGE_TAG = "RECTIFY_";
    private static final int HW_SINGLE_CAMERA_ALLFOCUS_IMAGE_LEN = 7;
    private static final String HW_SINGLE_CAMERA_ALLFOCUS_IMAGE_TAG = "Refocus";
    private static final String HW_VOICE_IMAGE_COLUMN = "hw_voice_offset";
    private static final String HW_VOICE_TAG = "HWVOICE_";
    private static final String INVALID_UTF8_TOKEN = "??";
    private static final int MEDIA_BUFFER_SIZE = 100;
    private static final String TAG = "HwMediaScannerImpl";
    private static HwMediaScannerManager mHwMediaScannerManager;
    private static Sniffer sniffer;
    private static MediaScannerUtils utils;
    private String mDefaultRingtoneFilename2;
    private boolean mDefaultRingtoneSet2;
    private final String mExternalStoragePath;
    private MediaInserter mMediaInserter;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.HwMediaScannerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.HwMediaScannerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.HwMediaScannerImpl.<clinit>():void");
    }

    private HwMediaScannerImpl() {
        this.mExternalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static HwMediaScannerManager getDefault() {
        return mHwMediaScannerManager;
    }

    private int getSkipCustomDirectory(String[] whiteList, String[] blackList, StringBuffer sb) {
        int i;
        int i2 = 0;
        int size = 0;
        int length = whiteList.length;
        for (i = 0; i < length; i += HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA) {
            String dir = whiteList[i];
            if (!dir.isEmpty()) {
                sb.append(dir);
                sb.append(",");
                size += HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA;
            }
        }
        i = blackList.length;
        while (i2 < i) {
            dir = blackList[i2];
            if (!dir.isEmpty()) {
                sb.append(dir);
                sb.append(",");
                size += HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA;
            }
            i2 += HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA;
        }
        return size;
    }

    public void setMediaInserter(MediaInserter mediaInserter) {
        this.mMediaInserter = mediaInserter;
    }

    public void scanCustomDirectories(MediaScanner scanner, MyMediaScannerClient mClient, String[] directories, String volumeName, String[] whiteList, String[] blackList) {
        try {
            int i;
            utils.prescan(scanner, null, ENABLE_BULK_INSERTS);
            for (i = 0; i < whiteList.length; i += HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA) {
                utils.processDirectory(scanner, whiteList[i], mClient);
            }
            if (this.mMediaInserter != null) {
                this.mMediaInserter.flushAll();
            }
            StringBuffer sb = new StringBuffer();
            scanner.addSkipCustomDirectory(sb.toString(), getSkipCustomDirectory(whiteList, blackList, sb));
            for (i = 0; i < directories.length; i += HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA) {
                utils.processDirectory(scanner, directories[i], mClient);
            }
            scanner.clearSkipCustomDirectory();
            if (this.mMediaInserter != null) {
                this.mMediaInserter.flushAll();
            }
            for (i = 0; i < blackList.length; i += HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA) {
                utils.processDirectory(scanner, blackList[i], mClient);
            }
            if (this.mMediaInserter != null) {
                this.mMediaInserter.flushAll();
            }
            scanner.postscan(directories);
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
        }
    }

    public int getBufferSize(Uri tableUri, int bufferSizePerUri) {
        boolean isImage = EXTERNAL_IMAGE_URI.equals(tableUri);
        boolean isVideo = EXTERNAL_VIDEO_URI.equals(tableUri);
        boolean isAudio = EXTERNAL_AUDIO_URI.equals(tableUri);
        if (isImage || isVideo || isAudio) {
            return MEDIA_BUFFER_SIZE;
        }
        return bufferSizePerUri;
    }

    public void setHwDefaultRingtoneFileNames() {
        if (isMultiSimEnabled()) {
            this.mDefaultRingtoneFilename2 = SystemProperties.get("ro.config.ringtone2");
        }
    }

    public boolean hwNeedSetSettings(String path) {
        if (isMultiSimEnabled() && !this.mDefaultRingtoneSet2 && (TextUtils.isEmpty(this.mDefaultRingtoneFilename2) || doesPathHaveFilename(path, this.mDefaultRingtoneFilename2))) {
            return ENABLE_BULK_INSERTS;
        }
        return false;
    }

    private boolean doesPathHaveFilename(String path, String filename) {
        int pathFilenameStart = path.lastIndexOf(File.separatorChar) + HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA;
        int filenameLength = filename.length();
        if (path.regionMatches(pathFilenameStart, filename, 0, filenameLength) && pathFilenameStart + filenameLength == path.length()) {
            return ENABLE_BULK_INSERTS;
        }
        return false;
    }

    public void hwSetRingtone2Settings(boolean needToSetSettings2, boolean ringtones, Uri tableUri, long rowId, Context context) {
        if (isMultiSimEnabled() && needToSetSettings2 && ringtones) {
            setSettingIfNotSet(System.RINGTONE2, tableUri, rowId, context);
            this.mDefaultRingtoneSet2 = ENABLE_BULK_INSERTS;
        }
    }

    private void setSettingIfNotSet(String settingName, Uri uri, long rowId, Context context) {
        if (TextUtils.isEmpty(Settings.System.getString(context.getContentResolver(), settingName))) {
            Settings.System.putString(context.getContentResolver(), settingName, ContentUris.withAppendedId(uri, rowId).toString());
        }
    }

    public String getExtSdcardVolumePath(Context context) {
        StorageVolume[] storageVolumes = ((StorageManager) context.getSystemService("storage")).getVolumeList();
        int length = storageVolumes.length;
        for (int i = 0; i < length; i += HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA) {
            StorageVolume storageVolume = storageVolumes[i];
            if (storageVolume.isRemovable() && !storageVolume.getPath().contains("usb")) {
                return storageVolume.getPath();
            }
        }
        return null;
    }

    public boolean isSkipExtSdcard(ContentProviderClient mMediaProvider, String mExtStroagePath, String mPackageName, Uri mFilesUriNoNotify) {
        boolean skip = false;
        if (mExtStroagePath == null) {
            return false;
        }
        int externelNum = -1;
        String[] projectionIn = new String[HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA];
        projectionIn[0] = "COUNT(*)";
        Cursor cursor = null;
        try {
            cursor = mMediaProvider.query(mFilesUriNoNotify, projectionIn, "_data LIKE '" + mExtStroagePath + "%'", null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                externelNum = cursor.getInt(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (externelNum == 0) {
            skip = ENABLE_BULK_INSERTS;
        }
        return skip;
    }

    public boolean isBitmapSizeTooLarge(String path) {
        File imageFile = new File(path);
        long limitSize = SystemProperties.getLong("ro.config.hw_pic_limit_size", 0);
        if (limitSize <= 0 || imageFile.length() <= (limitSize * 1024) * 1024) {
            return false;
        }
        return ENABLE_BULK_INSERTS;
    }

    private void processCustomImageOffset(String path, ContentValues values) {
        IOException ex;
        Throwable th;
        Exception ex2;
        RandomAccessFile randomAccessFile = null;
        try {
            RandomAccessFile randomFile = new RandomAccessFile(path, "r");
            try {
                long fileLength = randomFile.length();
                if (fileLength < 20) {
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (IOException e) {
                        }
                    }
                    return;
                }
                randomFile.seek(fileLength - 20);
                byte[] buffer = new byte[HW_CUSTOM_IMAGE_TAG_LEN];
                if (randomFile.read(buffer) != HW_CUSTOM_IMAGE_TAG_LEN) {
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (IOException e2) {
                        }
                    }
                    return;
                }
                String tag = new String(buffer, "ISO-8859-1").trim();
                ContentValues contentValues;
                if (tag.startsWith(HW_VOICE_TAG)) {
                    contentValues = values;
                    contentValues.put(HW_VOICE_IMAGE_COLUMN, Long.valueOf(tag.split("_")[HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA]));
                } else if (tag.startsWith(HW_RECTIFY_IMAGE_TAG)) {
                    contentValues = values;
                    contentValues.put(HW_RECTIFY_IMAGE_COLUMN, Long.valueOf(tag.split("_")[HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA]));
                }
                if (randomFile != null) {
                    try {
                        randomFile.close();
                    } catch (IOException e3) {
                    }
                }
                randomAccessFile = randomFile;
            } catch (IOException e4) {
                ex = e4;
                randomAccessFile = randomFile;
                try {
                    Log.w(TAG, "fail to process custom image", ex);
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e5) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e6) {
                        }
                    }
                    throw th;
                }
            } catch (Exception e7) {
                ex2 = e7;
                randomAccessFile = randomFile;
                Log.w(TAG, "fail to process custom image", ex2);
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e8) {
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                randomAccessFile = randomFile;
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                throw th;
            }
        } catch (IOException e9) {
            ex = e9;
            Log.w(TAG, "fail to process custom image", ex);
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        } catch (Exception e10) {
            ex2 = e10;
            Log.w(TAG, "fail to process custom image", ex2);
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
    }

    public void initializeHwVoiceAndFocus(String path, ContentValues values) {
        processCustomImageOffset(path, values);
        int allFocusImageType = checkAllFocusImage(path);
        if (allFocusImageType > 0) {
            values.put(HW_ALLFOCUS_IMAGE_COLUMN, Integer.valueOf(allFocusImageType));
        }
    }

    private int checkAllFocusImage(String path) {
        IOException ex;
        Exception ex2;
        Throwable th;
        RandomAccessFile randomAccessFile = null;
        try {
            RandomAccessFile randomFile = new RandomAccessFile(path, "r");
            try {
                long fileLength = randomFile.length();
                if (fileLength < 7) {
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    return 0;
                }
                randomFile.seek(fileLength - 7);
                byte[] buffer = new byte[HW_SINGLE_CAMERA_ALLFOCUS_IMAGE_LEN];
                if (randomFile.read(buffer) != HW_SINGLE_CAMERA_ALLFOCUS_IMAGE_LEN) {
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (IOException e12) {
                            e12.printStackTrace();
                        }
                    }
                    return 0;
                } else if (Arrays.equals(buffer, HW_SINGLE_CAMERA_ALLFOCUS_IMAGE_TAG.getBytes(Charset.forName("UTF-8")))) {
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (IOException e122) {
                            e122.printStackTrace();
                        }
                    }
                    return HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA;
                } else if (fileLength < 8) {
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (IOException e1222) {
                            e1222.printStackTrace();
                        }
                    }
                    return 0;
                } else {
                    randomFile.seek(fileLength - 8);
                    buffer = new byte[HW_DUAL_CAMERA_ALLFOCUS_IMAGE_LEN];
                    if (randomFile.read(buffer) != HW_DUAL_CAMERA_ALLFOCUS_IMAGE_LEN) {
                        if (randomFile != null) {
                            try {
                                randomFile.close();
                            } catch (IOException e12222) {
                                e12222.printStackTrace();
                            }
                        }
                        return 0;
                    } else if (Arrays.equals(buffer, HW_DUAL_CAMERA_ALLFOCUS_IMAGE_TAG.getBytes(Charset.forName("UTF-8")))) {
                        if (randomFile != null) {
                            try {
                                randomFile.close();
                            } catch (IOException e122222) {
                                e122222.printStackTrace();
                            }
                        }
                        return HW_ALLFOCUS_IMAGE_TYPE_DUAL_CAMERA;
                    } else {
                        if (randomFile != null) {
                            try {
                                randomFile.close();
                            } catch (IOException e1222222) {
                                e1222222.printStackTrace();
                            }
                        }
                        randomAccessFile = randomFile;
                        return 0;
                    }
                }
            } catch (IOException e) {
                ex = e;
                randomAccessFile = randomFile;
                ex.printStackTrace();
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e12222222) {
                        e12222222.printStackTrace();
                    }
                }
                return 0;
            } catch (Exception e2) {
                ex2 = e2;
                randomAccessFile = randomFile;
                try {
                    ex2.printStackTrace();
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e122222222) {
                            e122222222.printStackTrace();
                        }
                    }
                    return 0;
                } catch (Throwable th2) {
                    th = th2;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e1222222222) {
                            e1222222222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                randomAccessFile = randomFile;
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                throw th;
            }
        } catch (IOException e3) {
            ex = e3;
            ex.printStackTrace();
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            return 0;
        } catch (Exception e4) {
            ex2 = e4;
            ex2.printStackTrace();
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            return 0;
        }
    }

    public void pruneDeadThumbnailsFolder() {
        boolean isDelete = false;
        Log.v(TAG, "mExternalStoragePath is " + this.mExternalStoragePath);
        try {
            long thresholdMem;
            StatFs sdcardFileStats = new StatFs(this.mExternalStoragePath);
            long freeMem = ((long) sdcardFileStats.getAvailableBlocks()) * ((long) sdcardFileStats.getBlockSize());
            long totalMem = (((long) sdcardFileStats.getBlockCount()) * ((long) sdcardFileStats.getBlockSize())) / 10;
            if (totalMem > 524288000) {
                thresholdMem = 524288000;
            } else {
                thresholdMem = totalMem;
            }
            isDelete = freeMem > thresholdMem ? false : ENABLE_BULK_INSERTS;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException in pruneDeadThumbnailsFolder", e);
        }
        if (isDelete) {
            File thumbFolder = new File(this.mExternalStoragePath + "/DCIM/.thumbnails");
            if (thumbFolder == null || !thumbFolder.exists()) {
                Log.e(TAG, ".thumbnails folder not exists. ");
                return;
            }
            File[] files = thumbFolder.listFiles();
            if (files != null) {
                int length = files.length;
                for (int i = 0; i < length; i += HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA) {
                    if (!files[i].delete()) {
                        Log.e(TAG, "Failed to delete file!");
                    }
                }
            }
        }
    }

    private static boolean isMultiSimEnabled() {
        boolean flag = false;
        try {
            flag = TelephonyManager.getDefault().isMultiSimEnabled();
        } catch (Exception e) {
            Log.w(TAG, "isMultiSimEnabled api met Exception!");
        }
        return flag;
    }

    private boolean isMessyCharacter(char input) {
        UnicodeBlock unicodeBlock = UnicodeBlock.of(input);
        if (unicodeBlock == UnicodeBlock.LATIN_1_SUPPLEMENT || unicodeBlock == UnicodeBlock.SPECIALS || unicodeBlock == UnicodeBlock.HEBREW || unicodeBlock == UnicodeBlock.GREEK || unicodeBlock == UnicodeBlock.CYRILLIC_SUPPLEMENTARY || unicodeBlock == UnicodeBlock.LATIN_EXTENDED_A || unicodeBlock == UnicodeBlock.LATIN_EXTENDED_B || unicodeBlock == UnicodeBlock.COMBINING_DIACRITICAL_MARKS || unicodeBlock == UnicodeBlock.PRIVATE_USE_AREA || unicodeBlock == UnicodeBlock.ARMENIAN) {
            return ENABLE_BULK_INSERTS;
        }
        return false;
    }

    private boolean isMessyCharacterOrigin(char input) {
        UnicodeBlock unicodeBlock = UnicodeBlock.of(input);
        if (unicodeBlock == UnicodeBlock.SPECIALS || unicodeBlock == UnicodeBlock.GREEK || unicodeBlock == UnicodeBlock.CYRILLIC_SUPPLEMENTARY || unicodeBlock == UnicodeBlock.LATIN_EXTENDED_A || unicodeBlock == UnicodeBlock.LATIN_EXTENDED_B || unicodeBlock == UnicodeBlock.COMBINING_DIACRITICAL_MARKS || unicodeBlock == UnicodeBlock.PRIVATE_USE_AREA) {
            return ENABLE_BULK_INSERTS;
        }
        if ((unicodeBlock == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS && !CharacterTables.isFrequentHan(input)) || unicodeBlock == UnicodeBlock.BOX_DRAWING || unicodeBlock == UnicodeBlock.HANGUL_SYLLABLES || unicodeBlock == UnicodeBlock.ARMENIAN) {
            return ENABLE_BULK_INSERTS;
        }
        return false;
    }

    private boolean isAcceptableCharacter(char input) {
        UnicodeBlock unicodeBlock = UnicodeBlock.of(input);
        if (unicodeBlock == UnicodeBlock.BASIC_LATIN || unicodeBlock == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || unicodeBlock == UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || unicodeBlock == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || unicodeBlock == UnicodeBlock.GENERAL_PUNCTUATION || unicodeBlock == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || unicodeBlock == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return ENABLE_BULK_INSERTS;
        }
        return false;
    }

    private String trimIncorrectPunctuation(String input) {
        return Pattern.compile("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~\uff01@#\uffe5%\u2026\u2026&*\uff08\uff09\u2014\u2014+|{}\u3010\u3011\u2018\uff1b\uff1a\u201d\u201c\u2019\u3002\uff0c\u3001\uff1f]").matcher(Pattern.compile("\\s*|\t*|\r*|\n*").matcher(input).replaceAll("").replaceAll("\\p{P}", "")).replaceAll("");
    }

    private boolean isAcceptableString(String input) {
        char[] arrayChar = trimIncorrectPunctuation(input).trim().toCharArray();
        int length = arrayChar.length;
        for (int i = 0; i < length; i += HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA) {
            if (!isAcceptableCharacter(arrayChar[i])) {
                return false;
            }
        }
        return ENABLE_BULK_INSERTS;
    }

    private boolean isStringMessy(String input) {
        char[] arrayChar = trimIncorrectPunctuation(input).trim().toCharArray();
        int length = arrayChar.length;
        for (int i = 0; i < length; i += HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA) {
            if (isMessyCharacter(arrayChar[i])) {
                return ENABLE_BULK_INSERTS;
            }
        }
        return false;
    }

    private boolean isStringMessyOrigin(String input) {
        char[] arrayChar = trimIncorrectPunctuation(input).trim().toCharArray();
        int length = arrayChar.length;
        for (int i = 0; i < length; i += HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA) {
            if (isMessyCharacterOrigin(arrayChar[i])) {
                return ENABLE_BULK_INSERTS;
            }
        }
        return false;
    }

    private String getCorrectEncodedString(String input) {
        if (isStringMessy(input)) {
            try {
                String utf8 = new String(input.getBytes("ISO-8859-1"), "UTF-8");
                if (isAcceptableString(utf8)) {
                    return utf8;
                }
                String gbk = new String(input.getBytes("ISO-8859-1"), "GBK");
                if (isAcceptableString(gbk)) {
                    return gbk;
                }
                String big5 = new String(input.getBytes("ISO-8859-1"), "BIG5");
                if (isAcceptableString(big5)) {
                    return big5;
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "unsupported encoding : \n", e);
            }
        }
        return input;
    }

    private boolean isInvalidUtf8(String input) {
        return input != null ? input.contains(INVALID_UTF8_TOKEN) : false;
    }

    private boolean isInvalidString(String input) {
        return (TextUtils.isEmpty(input) || isInvalidUtf8(input)) ? ENABLE_BULK_INSERTS : isStringMessy(input);
    }

    private String finalCheck(String value, String path, int flag) {
        if (isInvalidString(value)) {
            if (flag == HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA || flag == HW_ALLFOCUS_IMAGE_TYPE_DUAL_CAMERA) {
                return "<unknown>";
            }
            value = getDisplayName(path);
        }
        return value;
    }

    private String getDisplayName(String path) {
        int lastdotIndex = path.lastIndexOf(".");
        int lastSlashIndex = path.lastIndexOf("/");
        if (lastdotIndex <= 0 || lastSlashIndex <= 0 || lastSlashIndex > lastdotIndex) {
            return "";
        }
        return path.substring(lastSlashIndex + HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA, lastdotIndex);
    }

    public boolean useMessyOptimize() {
        String debug = SystemProperties.get("ro.product.locale.region", "");
        return (debug == null || !"CN".equals(debug)) ? false : ENABLE_BULK_INSERTS;
    }

    public boolean isMp3(String mimetype) {
        if (mimetype == null || (!Sniffer.MEDIA_MIMETYPE_AUDIO_MPEG.equalsIgnoreCase(mimetype) && !"audio/x-mp3".equalsIgnoreCase(mimetype) && !"audio/x-mpeg".equalsIgnoreCase(mimetype) && !"audio/mp3".equalsIgnoreCase(mimetype))) {
            return false;
        }
        return ENABLE_BULK_INSERTS;
    }

    public boolean preHandleStringTag(String value, String mimetype) {
        if (!useMessyOptimize() || !isMp3(mimetype) || TextUtils.isEmpty(value) || !isStringMessyOrigin(value)) {
            return false;
        }
        Log.e(TAG, "value: " + value);
        return ENABLE_BULK_INSERTS;
    }

    public void initializeSniffer(String path) {
        sniffer.setDataSource(path);
    }

    public void resetSniffer() {
        sniffer.reset();
    }

    public String postHandleStringTag(String value, String path, int flag) {
        switch (flag) {
            case HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA /*1*/:
                try {
                    return finalCheck(getCorrectEncodedString(sniffer.getAlbum()), path, flag);
                } catch (Exception e) {
                    Log.e(TAG, "postHandleStringTag e: " + e);
                    break;
                }
            case HW_ALLFOCUS_IMAGE_TYPE_DUAL_CAMERA /*2*/:
                return finalCheck(getCorrectEncodedString(sniffer.getArtist()), path, flag);
            case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                return finalCheck(getCorrectEncodedString(sniffer.getTitle()), path, flag);
        }
        return value;
    }
}
