package com.huawei.media.scan;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.hwtheme.HwThemeManager;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.MediaStoreEx;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

public class HwModernScannerEx extends ModernScannerEx {
    private static final int ALBUM = 1;
    private static final int ARTIST = 2;
    private static final String DIR_STORAGE = "/storage";
    public static final boolean ENABLE_OPTIMIZE_FILE_CACHE = SystemPropertiesEx.getBoolean("persist.sys.optimize_fileCache", false);
    private static final boolean ENABLE_OPTIMIZE_STORAGE_ID = SystemPropertiesEx.getBoolean("persist.sys.optimize_storageId", true);
    private static final boolean LOGD = Log.isLoggable(TAG, 3);
    private static final boolean LOGV = Log.isLoggable(TAG, 2);
    private static final long MAX_NOMEDIA_SIZE = 1024;
    private static final int SQL_MEDIA_TYPE_BLACKLIST = 10;
    private static final int STORAGE_ID_INVALID = 0;
    private static final String TAG = "HwModernScannerEx";
    private static final int TITLE = 3;
    private final BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();
    private Context mContext;
    private HwMediaScannerFileVisitor mFileVisitor = null;
    private HwMediaScannerImpl mHwMediaScannerImpl;
    private boolean mIsSkipWhiteList;
    private ContentProviderClient mMediaProvider = null;
    private final File mRoot;
    private ContentValues mScanFileValues;
    private boolean mSingleFile = true;
    private SpecialImageUtils mSpecialImageUtils;
    private int mStorageIdForCurScanDIR = 0;
    private String mVolumeName;
    private WhiteListUtils mWhiteListUtils;

    public HwModernScannerEx(@NonNull Context context, @NonNull FileVisitor<Path> visitor, @NonNull HwMediaScannerImpl hwMediaScanner, File root, ContentValues contentValues) {
        super(context, visitor, root, contentValues);
        this.mContext = context;
        this.mRoot = root;
        File file = this.mRoot;
        if (file != null) {
            this.mSingleFile = file.isFile();
            this.mVolumeName = MediaStoreEx.getVolumeName(root);
        }
        this.mScanFileValues = contentValues;
        this.mIsSkipWhiteList = isSkipWhiteList();
        this.mHwMediaScannerImpl = hwMediaScanner;
        if (hwMediaScanner != null) {
            this.mWhiteListUtils = hwMediaScanner.getWhiteListUtils();
            this.mSpecialImageUtils = hwMediaScanner.getSpecialImageUtils();
            this.mFileVisitor = new HwMediaScannerFileVisitor(context, visitor, hwMediaScanner, root, this.mIsSkipWhiteList);
        }
        Context context2 = this.mContext;
        if (context2 != null) {
            this.mMediaProvider = context2.getContentResolver().acquireContentProviderClient("media");
        }
        if (ENABLE_OPTIMIZE_STORAGE_ID) {
            String filelPath = null;
            try {
                filelPath = root.getCanonicalPath();
            } catch (IOException e) {
                Log.e(TAG, "HwModernScannerEx root.getCanonicalPath exception");
            }
            setStorageIdForCurScanDirectory(filelPath);
        }
        BitmapFactory.Options options = this.mBitmapOptions;
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        if (LOGD) {
            Log.i(TAG, "scan " + root + ", volume " + this.mVolumeName + ", mIsSkipWhiteList " + this.mIsSkipWhiteList);
        }
    }

    private boolean isSkipWhiteList() {
        boolean skip = true;
        ContentValues contentValues = this.mScanFileValues;
        if (!(contentValues == null || !contentValues.containsKey("skipWhiteList") || this.mScanFileValues.get("skipWhiteList") == null)) {
            skip = ((Boolean) this.mScanFileValues.get("skipWhiteList")).booleanValue();
        }
        Log.i(TAG, "is skip white list " + skip);
        return skip;
    }

    public FileVisitor<Path> getFileVisitorEx() {
        return this.mFileVisitor;
    }

    public void withStorageId(ContentProviderOperation.Builder op, File file) {
        int i;
        if (op != null && file != null && ENABLE_OPTIMIZE_STORAGE_ID && (i = this.mStorageIdForCurScanDIR) != 0) {
            op.withValue("storage_id", Integer.valueOf(i));
        }
    }

    public void scanItemAudio(ContentProviderOperation.Builder op, MediaMetadataRetriever mmr, File file, String mimeType) {
        if (op != null && mmr != null && file != null && mimeType != null) {
            try {
                String lowPath = file.getCanonicalPath().toLowerCase(Locale.ROOT);
                if (HwThemeManager.isTRingtones(lowPath)) {
                    op.withValue("is_ringtone", true);
                }
                if (HwThemeManager.isTNotifications(lowPath)) {
                    op.withValue("is_notification", true);
                }
                if (HwThemeManager.isTAlarms(lowPath)) {
                    op.withValue("is_alarm", true);
                }
                setOptionalValue(op, mmr, lowPath, mimeType);
            } catch (IOException e) {
            }
        }
    }

    private void setOptionalValue(ContentProviderOperation.Builder op, MediaMetadataRetriever mmr, String lowPath, String mimeType) {
        boolean isAlbumMessy = false;
        boolean isArtistMessy = false;
        boolean isTitleMessy = false;
        String album = mmr.extractMetadata(1);
        if (album != null) {
            isAlbumMessy = AudioMetadataMessyUtils.preHandleStringTag(album, mimeType);
        }
        String artist = mmr.extractMetadata(2);
        if (artist != null) {
            isArtistMessy = AudioMetadataMessyUtils.preHandleStringTag(artist, mimeType);
        }
        String title = mmr.extractMetadata(7);
        if (title != null) {
            isTitleMessy = AudioMetadataMessyUtils.preHandleStringTag(title, mimeType);
        }
        if (isAlbumMessy || isArtistMessy || isTitleMessy) {
            AudioMetadataMessyUtils.initializeSniffer(lowPath);
            if (isAlbumMessy) {
                withOptionalValue(op, "album", parseOptional(AudioMetadataMessyUtils.postHandleStringTag(album, lowPath, 1)));
            }
            if (isArtistMessy) {
                withOptionalValue(op, "artist", parseOptional(AudioMetadataMessyUtils.postHandleStringTag(artist, lowPath, 2)));
            }
            if (isTitleMessy) {
                withOptionalValue(op, "title", parseOptional(AudioMetadataMessyUtils.postHandleStringTag(title, lowPath, 3)));
            }
            AudioMetadataMessyUtils.resetSniffer();
        }
    }

    private void withOptionalValue(ContentProviderOperation.Builder op, String key, Optional<?> value) {
        if (value.isPresent()) {
            op.withValue(key, value.get());
        }
    }

    @NonNull
    private static <T> Optional<T> parseOptional(@Nullable T value) {
        if (value == null) {
            return Optional.empty();
        }
        if ((value instanceof String) && value.length() == 0) {
            return Optional.empty();
        }
        if ((value instanceof String) && value.equals("-1")) {
            return Optional.empty();
        }
        if (!(value instanceof Number) || value.intValue() != -1) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public void scanItemImage(ExifInterface exif, ContentProviderOperation.Builder op, File file, BasicFileAttributes attrs) {
        if (exif != null && op != null && file != null && attrs != null) {
            ContentValues contentValues = new ContentValues();
            if (isInBlackList()) {
                contentValues.put("media_type", (Integer) 10);
            }
            updateOverrideValues(file, contentValues);
            if (this.mSpecialImageUtils != null) {
                SpecialImageUtils.scannerSpecialImageType(contentValues, exif.getAttribute("ImageDescription"));
                SpecialImageUtils specialImageUtils = this.mSpecialImageUtils;
                SpecialImageUtils.initializeHwVoiceAndFocus(file.getPath(), contentValues);
            }
            if (isBitmapSizeTooLarge(file.getPath())) {
                contentValues.put("width", (Integer) -1);
                contentValues.put("height", (Integer) -1);
            } else if (!"0".equals(exif.getAttribute("ImageWidth")) || !"0".equals(exif.getAttribute("ImageLength"))) {
                Log.i(TAG, "scanItemImage get file path fail");
            } else {
                BitmapFactory.Options options = this.mBitmapOptions;
                options.outWidth = 0;
                options.outHeight = 0;
                String filePath = null;
                try {
                    filePath = file.getCanonicalPath();
                } catch (IOException e) {
                    Log.e(TAG, "scanItemImage file.getCanonicalPath excetion");
                }
                BitmapFactory.decodeFile(filePath, this.mBitmapOptions);
                withOptionalValue(op, "width", parseOptional(Integer.valueOf(this.mBitmapOptions.outWidth)));
                withOptionalValue(op, "height", parseOptional(Integer.valueOf(this.mBitmapOptions.outHeight)));
            }
            op.withValues(contentValues);
        }
    }

    private void updateOverrideValues(File file, ContentValues contentValues) {
        ContentValues contentValues2;
        if (this.mSingleFile && (contentValues2 = this.mScanFileValues) != null && contentValues2.containsKey("datetaken")) {
            contentValues.put("datetaken", (Long) this.mScanFileValues.get("datetaken"));
        }
    }

    private boolean isBitmapSizeTooLarge(String path) {
        File imageFile = new File(path);
        long limitSize = SystemPropertiesEx.getLong("ro.config.hw_pic_limit_size", 0);
        long newSize = limitSize * MAX_NOMEDIA_SIZE * MAX_NOMEDIA_SIZE;
        if (limitSize <= 0 || imageFile.length() <= newSize) {
            return false;
        }
        return true;
    }

    public boolean isInBlackList() {
        HwMediaScannerFileVisitor hwMediaScannerFileVisitor = this.mFileVisitor;
        if (hwMediaScannerFileVisitor != null) {
            return hwMediaScannerFileVisitor.isInBlackList();
        }
        return false;
    }

    public boolean isSkipQuery(Path file) {
        return this.mHwMediaScannerImpl.isSkipExtSdcardQuery(file);
    }

    public static class FileEntry {
        private long mLastModified;
        private long mRowId;
        private long mSize;

        FileEntry(long rowId, long lastModified, long size) {
            this.mRowId = rowId;
            this.mLastModified = lastModified;
            this.mSize = size;
        }
    }

    public long queryFromCache(Path file, BasicFileAttributes attrs) {
        if (!ENABLE_OPTIMIZE_FILE_CACHE) {
            return -1;
        }
        if (file != null) {
            if (attrs != null) {
                HashMap<String, FileEntry> fileCache = HwMediaScannerImpl.getFileCache();
                if (this.mSingleFile || fileCache == null) {
                    return -1;
                }
                long existingId = -1;
                File realFile = file.toFile();
                try {
                    FileEntry entry = fileCache.remove(realFile.getCanonicalPath());
                    if (entry != null) {
                        existingId = entry.mRowId;
                        long dateModified = entry.mLastModified;
                        long size = entry.mSize;
                        boolean sameSize = true;
                        boolean sameTime = lastModifiedTime(realFile, attrs) == dateModified;
                        if (attrs.size() != size) {
                            sameSize = false;
                        }
                        if (attrs.isDirectory() || (sameTime && sameSize)) {
                            if (LOGD) {
                                Log.v(TAG, "Skipping unchanged " + file + ", existingId " + existingId);
                            }
                            return existingId;
                        }
                    }
                    return existingId;
                } catch (IOException e) {
                    Log.e(TAG, "queryFromCache.getCanonicalPath exception");
                    return -1;
                }
            }
        }
        Log.e(TAG, "file or attrs is null");
        return -1;
    }

    private static String getStorageDir() {
        return DIR_STORAGE;
    }

    private static long lastModifiedTime(@NonNull File file, @NonNull BasicFileAttributes attrs) {
        if (Utils.contains(new File(getStorageDir()), file)) {
            return attrs.lastModifiedTime().toMillis() / 1000;
        }
        return Build.TIME / 1000;
    }

    public void setScannedId(long scannedId) {
        WhiteListUtils whiteListUtils;
        String str;
        if (LOGV) {
            Log.v(TAG, "setScannedId:" + scannedId + ", singleFile:" + this.mSingleFile + ", isInWhiteScanList:" + this.mWhiteListUtils.isInWhiteScanList() + ", volume " + this.mVolumeName + ", mIsSkipWhiteList " + this.mIsSkipWhiteList);
        }
        if (!this.mSingleFile && (whiteListUtils = this.mWhiteListUtils) != null && whiteListUtils.isInWhiteScanList() && (str = this.mVolumeName) != null && !str.equals("internal") && this.mIsSkipWhiteList) {
            HwMediaScannerImpl.setScannedId(scannedId);
        }
    }

    public ArrayList<Long> getWhiteListIds() {
        return HwMediaScannerImpl.getWhiteListIds();
    }

    private void setStorageIdForCurScanDirectory(String directory) {
        Bundle result;
        try {
            if (this.mMediaProvider != null && directory != null && (result = this.mMediaProvider.call("get_storageId", directory, null)) != null) {
                this.mStorageIdForCurScanDIR = result.getInt("storage_id", 0);
            }
        } catch (RemoteException | UnsupportedOperationException e) {
            Log.w(TAG, "setStorageIdForCurScanDIR exception.");
        }
    }
}
