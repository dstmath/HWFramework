package android.media;

import android.common.HwMediaScannerManager;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
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
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.android.provider.HwSettings.System;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.Character.UnicodeBlock;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class HwMediaScannerImpl implements HwMediaScannerManager {
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private static final int DEFAULT_THRESHOLD_MAX_BYTES = 524288000;
    private static final boolean ENABLE_BULK_INSERTS = true;
    private static final Uri EXTERNAL_AUDIO_URI = Media.getContentUri("external");
    private static final Uri EXTERNAL_IMAGE_URI = Images.Media.getContentUri("external");
    private static final Uri EXTERNAL_VIDEO_URI = Video.Media.getContentUri("external");
    private static final String HW_3D_MODEL_IMAGE_TAG = "H W 3 D ";
    private static final int HW_3D_MODEL_IMAGE_TYPE = 16;
    private static final String HW_ALLFOCUS_IMAGE_COLUMN = "hw_image_refocus";
    private static final int HW_ALLFOCUS_IMAGE_TYPE_DUAL_CAMERA = 2;
    private static final int HW_ALLFOCUS_IMAGE_TYPE_DUAL_CAMERA_ALTEK = 4;
    private static final int HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA = 1;
    private static final int HW_CUSTOM_IMAGE_TAG_LEN = 20;
    private static final int HW_DUAL_CAMERA_ALLFOCUS_IMAGE_LEN = 8;
    private static final String HW_DUAL_CAMERA_ALLFOCUS_IMAGE_TAG = "DepthEn\u0000";
    private static final String HW_DUAL_CAMERA_ALLFOCUS_IMAGE_TAG_ALTEK = "DepthIn\u0000";
    private static final int HW_LIVE_PHOTO_IMAGE_TYPE = 50;
    private static final String HW_LIVE_TAG = "LIVE_";
    private static final int HW_PANORAMA_3D_COMBINED_IMAGE_TYPE = 20;
    private static final String HW_PANORAMA_3D_COMBINED_TAG = "#FYUSEv3";
    private static final String HW_RECTIFY_IMAGE_COLUMN = "hw_rectify_offset";
    private static final String HW_RECTIFY_IMAGE_TAG = "RECTIFY_";
    private static final int HW_SINGLE_CAMERA_ALLFOCUS_IMAGE_LEN = 7;
    private static final String HW_SINGLE_CAMERA_ALLFOCUS_IMAGE_TAG = "Refocus";
    private static final String HW_SPECIAL_FILE_OFFSET_IMAGE_COLUMN = "special_file_offset";
    private static final String HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN = "special_file_type";
    private static final String HW_VOICE_IMAGE_COLUMN = "hw_voice_offset";
    private static final String HW_VOICE_TAG = "HWVOICE_";
    private static final String INVALID_UTF8_TOKEN = "??";
    private static final int MAX_HW_CUSTOM_IMAGE_TAG_LEN = 20;
    private static long MAX_NOMEDIA_SIZE = 1024;
    private static final int MEDIA_BUFFER_SIZE = 100;
    private static final String TAG = "HwMediaScannerImpl";
    private static HwMediaScannerManager mHwMediaScannerManager = new HwMediaScannerImpl();
    private static final String[] sNomediaFilepath = new String[]{"/.nomedia", "/DCIM/.nomedia", "/DCIM/Camera/.nomedia", "/Pictures/.nomedia", "/Pictures/Screenshots/.nomedia", "/tencent/.nomedia", "/tencent/MicroMsg/.nomedia", "/tencent/MicroMsg/Weixin/.nomedia"};
    private static Sniffer sniffer = new Sniffer();
    private static MediaScannerUtils utils = ((MediaScannerUtils) EasyInvokeFactory.getInvokeUtils(MediaScannerUtils.class));
    private CustomImageInfo[] mCustomImageInfos;
    private String mDefaultRingtoneFilename2;
    private boolean mDefaultRingtoneSet2;
    private final String mExternalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private MediaInserter mMediaInserter;

    private static abstract class CustomImageInfo {
        protected String customImageTag;
        protected String databaseColumn;
        protected int databaseType;
        protected String tagCharsetName;
        protected int tagLength;

        protected abstract boolean checkTag(byte[] bArr, ContentValues contentValues);

        protected CustomImageInfo(String customImageTag, int tagLength, String tagCharsetName, String databaseColumn, int databaseType) {
            this.customImageTag = customImageTag;
            this.tagLength = tagLength;
            this.tagCharsetName = tagCharsetName;
            this.databaseColumn = databaseColumn;
            this.databaseType = databaseType;
        }
    }

    private static class FixedEndTagCustomImageInfo extends CustomImageInfo {
        protected FixedEndTagCustomImageInfo(String customImageTag, int tagLength, String tagCharsetName, String databaseColumn, int databaseType) {
            super(customImageTag, tagLength, tagCharsetName, databaseColumn, databaseType);
        }

        /* JADX WARNING: Missing block: B:4:0x0008, code:
            return false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected boolean checkTag(byte[] fileEndBytes, ContentValues values) {
            if (fileEndBytes == null || fileEndBytes.length < this.tagLength || !Arrays.equals(Arrays.copyOfRange(fileEndBytes, fileEndBytes.length - this.tagLength, fileEndBytes.length), this.customImageTag.getBytes(Charset.forName(this.tagCharsetName)))) {
                return false;
            }
            values.put(this.databaseColumn, Integer.valueOf(this.databaseType));
            return true;
        }
    }

    private static class HwVoiceOrRectifyImageInfo extends CustomImageInfo {
        protected HwVoiceOrRectifyImageInfo(String customImageTag, int tagLength, String tagCharsetName, String databaseColumn) {
            super(customImageTag, tagLength, tagCharsetName, databaseColumn, 0);
        }

        protected boolean checkTag(byte[] fileEndBytes, ContentValues values) {
            if (fileEndBytes == null || fileEndBytes.length < this.tagLength) {
                return false;
            }
            try {
                String tag = new String(Arrays.copyOfRange(fileEndBytes, fileEndBytes.length - this.tagLength, fileEndBytes.length), this.tagCharsetName).trim();
                if (tag.startsWith(this.customImageTag)) {
                    String[] split = tag.split("_");
                    if (split.length < 2) {
                        return false;
                    }
                    values.put(this.databaseColumn, Long.valueOf(split[1]));
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                Log.w(HwMediaScannerImpl.TAG, "fail to check custom image tag, throws UnsupportedEncodingException");
            } catch (NumberFormatException e2) {
                Log.w(HwMediaScannerImpl.TAG, "fail to check custom image tag, throws NumberFormatException");
            } catch (UnsupportedCharsetException e3) {
                Log.w(HwMediaScannerImpl.TAG, "fail to check custom image tag, throws UnsupportedCharsetException");
            }
            return false;
        }
    }

    private static class SpecialOffsetImageInfo extends CustomImageInfo {
        protected SpecialOffsetImageInfo(String customImageTag, int tagLength, String tagCharsetName, String databaseColumn, int databaseType) {
            super(customImageTag, tagLength, tagCharsetName, databaseColumn, databaseType);
        }

        protected boolean checkTag(byte[] fileEndBytes, ContentValues values) {
            if (fileEndBytes == null || fileEndBytes.length < this.tagLength) {
                return false;
            }
            try {
                String tag = new String(Arrays.copyOfRange(fileEndBytes, fileEndBytes.length - this.tagLength, fileEndBytes.length), this.tagCharsetName).trim();
                if (tag.startsWith(this.customImageTag)) {
                    String[] split = tag.split("_");
                    if (split.length < 2) {
                        return false;
                    }
                    values.put(this.databaseColumn, Integer.valueOf(this.databaseType));
                    values.put(HwMediaScannerImpl.HW_SPECIAL_FILE_OFFSET_IMAGE_COLUMN, Long.valueOf(split[1]));
                    Log.d(HwMediaScannerImpl.TAG, "find a live tag. " + tag);
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                Log.w(HwMediaScannerImpl.TAG, "fail tddo check custom image tag, throws UnsupportedEncodingException " + this.databaseType);
            } catch (Exception e2) {
                Log.w(HwMediaScannerImpl.TAG, "fail to check custom image tag, throws UnsupportedCharsetException " + this.databaseType);
            }
            return false;
        }
    }

    private HwMediaScannerImpl() {
        initCustomImageInfos();
    }

    public static HwMediaScannerManager getDefault() {
        return mHwMediaScannerManager;
    }

    private int getSkipCustomDirectory(String[] whiteList, String[] blackList, StringBuffer sb) {
        String dir;
        int i = 0;
        int size = 0;
        for (String dir2 : whiteList) {
            if (!dir2.isEmpty()) {
                sb.append(dir2);
                sb.append(",");
                size++;
            }
        }
        int length = blackList.length;
        while (i < length) {
            dir2 = blackList[i];
            if (!dir2.isEmpty()) {
                sb.append(dir2);
                sb.append(",");
                size++;
            }
            i++;
        }
        return size;
    }

    public void setMediaInserter(MediaInserter mediaInserter) {
        this.mMediaInserter = mediaInserter;
    }

    public void scanCustomDirectories(MediaScanner scanner, MyMediaScannerClient mClient, String[] directories, String volumeName, String[] whiteList, String[] blackList) {
        try {
            int i;
            utils.prescan(scanner, null, true);
            for (i = 0; i < whiteList.length; i++) {
                insertDirectory(mClient, whiteList[i]);
                scanner.setExteLen(scanner.getRootDirLength(whiteList[i]));
                utils.processDirectory(scanner, whiteList[i], mClient);
            }
            if (this.mMediaInserter != null) {
                this.mMediaInserter.flushAll();
            }
            StringBuffer sb = new StringBuffer();
            scanner.addSkipCustomDirectory(sb.toString(), getSkipCustomDirectory(whiteList, blackList, sb));
            for (i = 0; i < directories.length; i++) {
                if (!shouldSkipDirectory(directories[i], whiteList, blackList)) {
                    insertDirectory(mClient, directories[i]);
                }
                scanner.setExteLen(scanner.getRootDirLength(directories[i]));
                utils.processDirectory(scanner, directories[i], mClient);
            }
            scanner.clearSkipCustomDirectory();
            if (this.mMediaInserter != null) {
                this.mMediaInserter.flushAll();
            }
            for (i = 0; i < blackList.length; i++) {
                insertDirectory(mClient, blackList[i]);
                scanner.setExteLen(scanner.getRootDirLength(blackList[i]));
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

    private static boolean shouldSkipDirectory(String path, String[] whiteList, String[] blackList) {
        for (String dir : whiteList) {
            if (dir.equals(path)) {
                return true;
            }
        }
        for (String dir2 : blackList) {
            if (dir2.equals(path)) {
                return true;
            }
        }
        return false;
    }

    private void insertDirectory(MediaScannerClient client, String path) {
        File file = new File(path);
        if (file.exists()) {
            String filePath;
            long lastModifiedSeconds = file.lastModified() / 1000;
            if (path.length() <= 1 || path.charAt(path.length() - 1) != '/') {
                filePath = path;
            } else {
                filePath = path.substring(0, path.length() - 1);
            }
            client.scanFile(filePath, lastModifiedSeconds, 0, true, false);
        }
    }

    public int getBufferSize(Uri tableUri, int bufferSizePerUri) {
        return (EXTERNAL_IMAGE_URI.equals(tableUri) || EXTERNAL_VIDEO_URI.equals(tableUri) || EXTERNAL_AUDIO_URI.equals(tableUri)) ? 100 : bufferSizePerUri;
    }

    public void setHwDefaultRingtoneFileNames() {
        if (isMultiSimEnabled()) {
            this.mDefaultRingtoneFilename2 = SystemProperties.get("ro.config.ringtone2");
        }
    }

    public boolean hwNeedSetSettings(String path) {
        if (isMultiSimEnabled() && (this.mDefaultRingtoneSet2 ^ 1) != 0 && (TextUtils.isEmpty(this.mDefaultRingtoneFilename2) || doesPathHaveFilename(path, this.mDefaultRingtoneFilename2))) {
            return true;
        }
        return false;
    }

    private boolean doesPathHaveFilename(String path, String filename) {
        int pathFilenameStart = path.lastIndexOf(File.separatorChar) + 1;
        int filenameLength = filename.length();
        if (path.regionMatches(pathFilenameStart, filename, 0, filenameLength) && pathFilenameStart + filenameLength == path.length()) {
            return true;
        }
        return false;
    }

    public void hwSetRingtone2Settings(boolean needToSetSettings2, boolean ringtones, Uri tableUri, long rowId, Context context) {
        if (isMultiSimEnabled() && needToSetSettings2 && ringtones) {
            setSettingIfNotSet(System.RINGTONE2, tableUri, rowId, context);
            this.mDefaultRingtoneSet2 = true;
        }
    }

    private String settingSetIndicatorName(String base) {
        return base + "_set";
    }

    private boolean wasRingtoneAlreadySet(ContentResolver cr, String name) {
        boolean z = false;
        try {
            if (Settings.System.getInt(cr, settingSetIndicatorName(name)) != 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    private void setSettingIfNotSet(String settingName, Uri uri, long rowId, Context context) {
        ContentResolver cr = context.getContentResolver();
        if (!wasRingtoneAlreadySet(cr, settingName)) {
            if (TextUtils.isEmpty(Settings.System.getString(cr, settingName))) {
                Uri settingUri = Settings.System.getUriFor(settingName);
                RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.getDefaultType(settingUri), ContentUris.withAppendedId(uri, rowId));
            }
            Settings.System.putInt(cr, settingSetIndicatorName(settingName), 1);
        }
    }

    public String getExtSdcardVolumePath(Context context) {
        for (StorageVolume storageVolume : ((StorageManager) context.getSystemService("storage")).getVolumeList()) {
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
        Cursor cursor = null;
        try {
            cursor = mMediaProvider.query(mFilesUriNoNotify, new String[]{"COUNT(*)"}, "_data LIKE '" + mExtStroagePath + "%'", null, null, null);
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
            skip = true;
        }
        return skip;
    }

    public boolean isBitmapSizeTooLarge(String path) {
        File imageFile = new File(path);
        long limitSize = SystemProperties.getLong("ro.config.hw_pic_limit_size", 0);
        if (limitSize <= 0 || imageFile.length() <= (limitSize * 1024) * 1024) {
            return false;
        }
        return true;
    }

    public void initializeHwVoiceAndFocus(String path, ContentValues values) {
        byte[] fileEndBytes = readFileEndBytes(path);
        int i = 0;
        while (i < this.mCustomImageInfos.length && !this.mCustomImageInfos[i].checkTag(fileEndBytes, values)) {
            i++;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:62:0x00c0 A:{SYNTHETIC, Splitter: B:62:0x00c0} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00a5 A:{SYNTHETIC, Splitter: B:54:0x00a5} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x008a A:{SYNTHETIC, Splitter: B:46:0x008a} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x006f A:{SYNTHETIC, Splitter: B:38:0x006f} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x00d2 A:{SYNTHETIC, Splitter: B:68:0x00d2} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] readFileEndBytes(String path) {
        Throwable th;
        RandomAccessFile randomFile = null;
        byte[] buffer = new byte[0];
        if (path == null) {
            return buffer;
        }
        try {
            RandomAccessFile randomFile2 = new RandomAccessFile(path, "r");
            try {
                long fileLength = randomFile2.length();
                if (fileLength < 20) {
                    if (randomFile2 != null) {
                        try {
                            randomFile2.close();
                        } catch (IOException e) {
                            Log.w(TAG, "fail to process custom image, readFileEndBytes close file fail");
                        }
                    }
                    return buffer;
                }
                byte[] tmp = new byte[20];
                randomFile2.seek(fileLength - 20);
                if (randomFile2.read(tmp) != 20) {
                    if (randomFile2 != null) {
                        try {
                            randomFile2.close();
                        } catch (IOException e2) {
                            Log.w(TAG, "fail to process custom image, readFileEndBytes close file fail");
                        }
                    }
                    return buffer;
                }
                buffer = tmp;
                if (randomFile2 != null) {
                    try {
                        randomFile2.close();
                    } catch (IOException e3) {
                        Log.w(TAG, "fail to process custom image, readFileEndBytes close file fail");
                    }
                }
                randomFile = randomFile2;
                return buffer;
            } catch (IOException e4) {
                randomFile = randomFile2;
                Log.w(TAG, "fail to process custom image, readFileEndBytes throws IOException");
                if (randomFile != null) {
                }
                return buffer;
            } catch (SecurityException e5) {
                randomFile = randomFile2;
                Log.w(TAG, "fail to process custom image, readFileEndBytes throws SecurityException");
                if (randomFile != null) {
                }
                return buffer;
            } catch (IllegalArgumentException e6) {
                randomFile = randomFile2;
                Log.w(TAG, "fail to process custom image, readFileEndBytes throws IllegalArgumentException");
                if (randomFile != null) {
                }
                return buffer;
            } catch (NullPointerException e7) {
                randomFile = randomFile2;
                try {
                    Log.w(TAG, "fail to process custom image, readFileEndBytes throws NullPointerException");
                    if (randomFile != null) {
                    }
                    return buffer;
                } catch (Throwable th2) {
                    th = th2;
                    if (randomFile != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                randomFile = randomFile2;
                if (randomFile != null) {
                    try {
                        randomFile.close();
                    } catch (IOException e8) {
                        Log.w(TAG, "fail to process custom image, readFileEndBytes close file fail");
                    }
                }
                throw th;
            }
        } catch (IOException e9) {
            Log.w(TAG, "fail to process custom image, readFileEndBytes throws IOException");
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e10) {
                    Log.w(TAG, "fail to process custom image, readFileEndBytes close file fail");
                }
            }
            return buffer;
        } catch (SecurityException e11) {
            Log.w(TAG, "fail to process custom image, readFileEndBytes throws SecurityException");
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e12) {
                    Log.w(TAG, "fail to process custom image, readFileEndBytes close file fail");
                }
            }
            return buffer;
        } catch (IllegalArgumentException e13) {
            Log.w(TAG, "fail to process custom image, readFileEndBytes throws IllegalArgumentException");
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e14) {
                    Log.w(TAG, "fail to process custom image, readFileEndBytes close file fail");
                }
            }
            return buffer;
        } catch (NullPointerException e15) {
            Log.w(TAG, "fail to process custom image, readFileEndBytes throws NullPointerException");
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e16) {
                    Log.w(TAG, "fail to process custom image, readFileEndBytes close file fail");
                }
            }
            return buffer;
        }
    }

    public void pruneDeadThumbnailsFolder() {
        boolean isDelete = false;
        try {
            StatFs sdcardFileStats = new StatFs(this.mExternalStoragePath);
            long freeMem = ((long) sdcardFileStats.getAvailableBlocks()) * ((long) sdcardFileStats.getBlockSize());
            long totalMem = (((long) sdcardFileStats.getBlockCount()) * ((long) sdcardFileStats.getBlockSize())) / 10;
            isDelete = freeMem <= ((totalMem > 524288000 ? 1 : (totalMem == 524288000 ? 0 : -1)) > 0 ? 524288000 : totalMem);
            Log.v(TAG, "freeMem[" + freeMem + "] 10%totalMem[" + totalMem + "] under " + this.mExternalStoragePath);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException in pruneDeadThumbnailsFolder", e);
        }
        if (isDelete) {
            File thumbFolder = new File(this.mExternalStoragePath + "/DCIM/.thumbnails");
            if (thumbFolder == null || (thumbFolder.exists() ^ 1) != 0) {
                Log.e(TAG, ".thumbnails folder not exists. ");
                return;
            }
            File[] files = thumbFolder.listFiles();
            if (files != null) {
                Log.v(TAG, "delete .thumbnails");
                for (File file : files) {
                    if (!file.delete()) {
                        Log.e(TAG, "Failed to delete file!");
                    }
                }
            }
        }
    }

    private static boolean isMultiSimEnabled() {
        boolean flag = false;
        try {
            return TelephonyManager.getDefault().isMultiSimEnabled();
        } catch (Exception e) {
            Log.w(TAG, "isMultiSimEnabled api met Exception!");
            return flag;
        }
    }

    private boolean isMessyCharacter(char input) {
        UnicodeBlock unicodeBlock = UnicodeBlock.of(input);
        if (unicodeBlock == UnicodeBlock.LATIN_1_SUPPLEMENT || unicodeBlock == UnicodeBlock.SPECIALS || unicodeBlock == UnicodeBlock.HEBREW || unicodeBlock == UnicodeBlock.GREEK || unicodeBlock == UnicodeBlock.CYRILLIC_SUPPLEMENTARY || unicodeBlock == UnicodeBlock.LATIN_EXTENDED_A || unicodeBlock == UnicodeBlock.LATIN_EXTENDED_B || unicodeBlock == UnicodeBlock.COMBINING_DIACRITICAL_MARKS || unicodeBlock == UnicodeBlock.PRIVATE_USE_AREA || unicodeBlock == UnicodeBlock.ARMENIAN) {
            return true;
        }
        return false;
    }

    private boolean isMessyCharacterOrigin(char input) {
        UnicodeBlock unicodeBlock = UnicodeBlock.of(input);
        if (unicodeBlock == UnicodeBlock.SPECIALS || unicodeBlock == UnicodeBlock.GREEK || unicodeBlock == UnicodeBlock.CYRILLIC_SUPPLEMENTARY || unicodeBlock == UnicodeBlock.LATIN_EXTENDED_A || unicodeBlock == UnicodeBlock.LATIN_EXTENDED_B || unicodeBlock == UnicodeBlock.COMBINING_DIACRITICAL_MARKS || unicodeBlock == UnicodeBlock.PRIVATE_USE_AREA) {
            return true;
        }
        if ((unicodeBlock == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS && (CharacterTables.isFrequentHan(input) ^ 1) != 0) || unicodeBlock == UnicodeBlock.BOX_DRAWING || unicodeBlock == UnicodeBlock.HANGUL_SYLLABLES || unicodeBlock == UnicodeBlock.ARMENIAN) {
            return true;
        }
        return false;
    }

    private boolean isAcceptableCharacter(char input) {
        UnicodeBlock unicodeBlock = UnicodeBlock.of(input);
        if (unicodeBlock == UnicodeBlock.BASIC_LATIN || unicodeBlock == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || unicodeBlock == UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || unicodeBlock == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || unicodeBlock == UnicodeBlock.GENERAL_PUNCTUATION || unicodeBlock == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || unicodeBlock == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    private String trimIncorrectPunctuation(String input) {
        return Pattern.compile("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]").matcher(Pattern.compile("\\s*|\t*|\r*|\n*").matcher(input).replaceAll("").replaceAll("\\p{P}", "")).replaceAll("");
    }

    private boolean isAcceptableString(String input) {
        for (char c : trimIncorrectPunctuation(input).trim().toCharArray()) {
            if (!isAcceptableCharacter(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isStringMessy(String input) {
        for (char c : trimIncorrectPunctuation(input).trim().toCharArray()) {
            if (isMessyCharacter(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStringMessyOrigin(String input) {
        for (char c : trimIncorrectPunctuation(input).trim().toCharArray()) {
            if (isMessyCharacterOrigin(c)) {
                return true;
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
        return (TextUtils.isEmpty(input) || isInvalidUtf8(input)) ? true : isStringMessy(input);
    }

    private String finalCheck(String value, String path, int flag) {
        if (isInvalidString(value)) {
            if (flag == 1 || flag == 2) {
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
        return path.substring(lastSlashIndex + 1, lastdotIndex);
    }

    public boolean useMessyOptimize() {
        String debug = SystemProperties.get("ro.product.locale.region", "");
        return debug != null && "CN".equals(debug);
    }

    public boolean isMp3(String mimetype) {
        if (mimetype == null || (!Sniffer.MEDIA_MIMETYPE_AUDIO_MPEG.equalsIgnoreCase(mimetype) && !"audio/x-mp3".equalsIgnoreCase(mimetype) && !"audio/x-mpeg".equalsIgnoreCase(mimetype) && !"audio/mp3".equalsIgnoreCase(mimetype))) {
            return false;
        }
        return true;
    }

    public boolean preHandleStringTag(String value, String mimetype) {
        if (!useMessyOptimize() || !isMp3(mimetype) || TextUtils.isEmpty(value) || !isStringMessyOrigin(value)) {
            return false;
        }
        Log.e(TAG, "value: " + value);
        return true;
    }

    public void initializeSniffer(String path) {
        sniffer.setDataSource(path);
    }

    public void resetSniffer() {
        sniffer.reset();
    }

    public String postHandleStringTag(String value, String path, int flag) {
        switch (flag) {
            case 1:
                try {
                    return finalCheck(getCorrectEncodedString(sniffer.getAlbum()), path, flag);
                } catch (Exception e) {
                    Log.e(TAG, "postHandleStringTag e: " + e);
                    break;
                }
            case 2:
                return finalCheck(getCorrectEncodedString(sniffer.getArtist()), path, flag);
            case 3:
                return finalCheck(getCorrectEncodedString(sniffer.getTitle()), path, flag);
        }
        return value;
    }

    private void initCustomImageInfos() {
        r9 = new CustomImageInfo[8];
        r9[4] = new FixedEndTagCustomImageInfo(HW_DUAL_CAMERA_ALLFOCUS_IMAGE_TAG, 8, "UTF-8", HW_ALLFOCUS_IMAGE_COLUMN, 2);
        r9[5] = new FixedEndTagCustomImageInfo(HW_DUAL_CAMERA_ALLFOCUS_IMAGE_TAG_ALTEK, 8, "UTF-8", HW_ALLFOCUS_IMAGE_COLUMN, 4);
        r9[6] = new FixedEndTagCustomImageInfo(HW_PANORAMA_3D_COMBINED_TAG, HW_PANORAMA_3D_COMBINED_TAG.length(), "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, 20);
        r9[7] = new FixedEndTagCustomImageInfo(HW_3D_MODEL_IMAGE_TAG, HW_3D_MODEL_IMAGE_TAG.length(), "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, 16);
        this.mCustomImageInfos = r9;
    }

    public void deleteNomediaFile() {
        for (StorageVolume storageVolume : StorageManager.getVolumeList(UserHandle.myUserId(), 256)) {
            String rootPath = storageVolume.getPath();
            for (String nomedia : sNomediaFilepath) {
                String nomediaPath = rootPath + nomedia;
                File nomediaFile = new File(nomediaPath);
                try {
                    if (nomediaFile.exists()) {
                        if (nomediaFile.isFile() && nomediaFile.length() > MAX_NOMEDIA_SIZE) {
                            Log.w(TAG, "skip nomedia file [" + nomediaPath + "]  size:" + nomediaFile.length());
                        } else if (deleteFile(nomediaFile)) {
                            Log.w(TAG, "delete nomedia file success [" + nomediaPath + "]");
                        } else {
                            Log.w(TAG, "delete nomedia file fail [" + nomediaPath + "]");
                        }
                    }
                } catch (IOException e) {
                    Log.w(TAG, "delete nomedia file exception [" + nomediaPath + "]");
                }
            }
        }
    }

    private boolean deleteFile(File file) throws IOException {
        boolean result = true;
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            if (!file.delete()) {
                result = false;
            }
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File deleteFile : files) {
                    if (!deleteFile(deleteFile)) {
                        result = false;
                    }
                }
            }
            if (!file.delete()) {
                result = false;
            }
        }
        return result;
    }
}
