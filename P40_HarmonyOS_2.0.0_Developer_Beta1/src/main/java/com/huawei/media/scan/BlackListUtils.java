package com.huawei.media.scan;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.media.BuildConfig;
import android.net.Uri;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.util.Log;
import com.huawei.android.os.storage.StorageVolumeEx;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class BlackListUtils {
    private static final String BLACKLIST_NATIVE_CONFIG_FILE = "/com.android.providers.media/media_scan_config.xml";
    private static final String BLACKLIST_REMOTE_CONFIG_FILE = "/Pictures/.Gallery2/config/media_scan_config.xml";
    private static final int BLACKLIST_VERSION_NUMBER = 110;
    private static final Set<String> BLACK_LIST = new HashSet();
    private static final String DIR_DATA_DATA = "/data/data";
    public static final int DIR_IS_BLACKLIST = 2;
    public static final int DIR_MAYBE_BLACKLIST = 1;
    public static final int DIR_NOT_BLACKLIST = 0;
    private static final String DIR_STORAGE_EMULATED = "/storage/emulated/0";
    private static final String[] FILES_BLACKLIST_PROJECTION_MEDIA = {"_id", "_data", "media_type"};
    private static final String SHARDPREFERENCE_BLACKLIST = "MediaScanBlackList";
    private static final int SQL_MEDIA_TYPE_BLACKLIST = 10;
    private static final int SQL_MEDIA_TYPE_IMGAGE = 1;
    private static final int SQL_QUERY_COUNT = 100;
    private static final String SQL_QUERY_LIMIT = "1000";
    private static final String TAG = "BlackListUtils";
    private static final String UPDATE_BLACKLIST_VERSION = "updateBlackListVersion";
    private Context mContext;
    private int mCurBlackListVersion;
    private final Uri mFilesUri = MediaStore.Files.getContentUri("external");
    private final HwMediaScannerImpl mHwMediaScannerImpl;
    private final Uri mImagesUri = MediaStore.Images.Media.getContentUri("external");
    private final ContentProviderClient mMediaProvider = this.mContext.getContentResolver().acquireContentProviderClient("media");

    public BlackListUtils(Context context, HwMediaScannerImpl hwMediaScanner) {
        this.mContext = context;
        this.mHwMediaScannerImpl = hwMediaScanner;
    }

    private static String parserXml(FileInputStream input, boolean extractOnly) {
        String version = BuildConfig.FLAVOR;
        try {
            XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
            xpp.setInput(input, "UTF-8");
            for (int evtType = xpp.getEventType(); evtType != 1; evtType = xpp.next()) {
                if (evtType == 2) {
                    String tag = xpp.getName();
                    if (tag.equals("filedir") && !extractOnly && BLACK_LIST != null) {
                        BLACK_LIST.add(xpp.nextText());
                    } else if (tag.equals("version")) {
                        version = xpp.nextText();
                    } else {
                        Log.v(TAG, "parserXml, tag = " + tag);
                    }
                } else if (evtType != 3) {
                }
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "XmlPullParserException FileNotFoundException !");
        } catch (IOException e2) {
            Log.e(TAG, "IOException fail!");
        }
        return version;
    }

    private static int extractBlackList(boolean extractOnly, String filePath) {
        FileInputStream input = null;
        String version = BuildConfig.FLAVOR;
        try {
            File filename = new File(filePath);
            if (!filename.exists()) {
                if (0 != 0) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        Log.e(TAG, "extractBlackList input close fail!");
                    }
                }
                return 0;
            }
            if (!extractOnly) {
                BLACK_LIST.clear();
            }
            FileInputStream input2 = new FileInputStream(filename);
            version = parserXml(input2, extractOnly);
            try {
                input2.close();
            } catch (IOException e2) {
                Log.e(TAG, "extractBlackList input close fail!");
            }
            try {
                return Integer.parseInt(version);
            } catch (NumberFormatException e3) {
                Log.e(TAG, "extractBlackList versionNumber parse error!");
                return 0;
            }
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "extractBlackList FileNotFoundException!");
            if (0 != 0) {
                input.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    input.close();
                } catch (IOException e5) {
                    Log.e(TAG, "extractBlackList input close fail!");
                }
            }
            throw th;
        }
    }

    private String getBlacklistRemoteConfigFile() {
        return "/storage/emulated/0/Pictures/.Gallery2/config/media_scan_config.xml";
    }

    private boolean isNeedUpgrade() {
        int remVersion = extractBlackList(true, getBlacklistRemoteConfigFile());
        if (remVersion > this.mCurBlackListVersion) {
            return true;
        }
        Log.e(TAG, "Version roll-back error, sCurBlackListVersion = " + this.mCurBlackListVersion + " remVersion = " + remVersion);
        return false;
    }

    private String getRemoteFileDir() {
        return "/storage/emulated/0/Pictures/.Gallery2/config/media_scan_config.xml";
    }

    private String getBlacklistNativeConfigFile() {
        return "/data/data/com.android.providers.media/media_scan_config.xml";
    }

    private boolean copyFileFromRemote() {
        File nativeFile = null;
        try {
            File remoteFile = new File(getRemoteFileDir());
            if (remoteFile.exists()) {
                if (isNeedUpgrade()) {
                    File nativeFile2 = new File(getBlacklistNativeConfigFile());
                    if (nativeFile2.exists() || nativeFile2.createNewFile()) {
                        FileInputStream input = new FileInputStream(remoteFile);
                        FileOutputStream output = new FileOutputStream(nativeFile2);
                        byte[] array = new byte[1024];
                        while (true) {
                            int len = input.read(array);
                            if (len != -1) {
                                output.write(array, 0, len);
                            } else {
                                output.flush();
                                closeStream(input, output);
                                return true;
                            }
                        }
                    } else {
                        closeStream(null, null);
                        return false;
                    }
                }
            }
            Log.e(TAG, "remoteFile not exist or no need upgrade");
            closeStream(null, null);
            return false;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "copy copyFileFromRemote FileNotFoundException!");
        } catch (IOException e2) {
            if (0 != 0) {
                if (nativeFile.exists() && !nativeFile.delete()) {
                    Log.e(TAG, "delete fail");
                }
            }
        } catch (Throwable th) {
            closeStream(null, null);
            throw th;
        }
        closeStream(null, null);
        return false;
    }

    private void closeStream(FileInputStream input, FileOutputStream output) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                Log.e(TAG, "copyFileFromRemote  output intput fail:");
            } catch (Throwable th) {
                Log.i(TAG, "input stream close");
                throw th;
            }
        }
        Log.i(TAG, "input stream close");
        if (output != null) {
            try {
                output.close();
            } catch (IOException e2) {
                Log.e(TAG, "copyFileFromRemote output close fail:");
            } catch (Throwable th2) {
                Log.i(TAG, "output stream close");
                throw th2;
            }
        }
        Log.i(TAG, "output stream close");
    }

    private boolean startWithIgnoreCase(String src, String sub) {
        if (sub.length() > src.length()) {
            return false;
        }
        return src.substring(0, sub.length()).equalsIgnoreCase(sub);
    }

    private boolean isBlackListPath(String path, int exterlen) {
        if (path == null || BLACK_LIST.size() == 0 || exterlen == 0 || path.length() <= exterlen) {
            return false;
        }
        String subPath = path.substring(exterlen);
        for (String itp : BLACK_LIST) {
            if (startWithIgnoreCase(subPath, itp)) {
                return true;
            }
        }
        return false;
    }

    public int getRootDirLength(String path) {
        if (path == null) {
            return 0;
        }
        StorageManager storageManager = null;
        if (this.mContext.getSystemService("storage") instanceof StorageManager) {
            storageManager = (StorageManager) this.mContext.getSystemService("storage");
        }
        if (storageManager == null) {
            return 0;
        }
        for (StorageVolume storageVolume : storageManager.getStorageVolumes()) {
            String rootPath = StorageVolumeEx.getPath(storageVolume);
            if (path.startsWith(rootPath)) {
                return rootPath.length();
            }
        }
        return 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:103:0x018f, code lost:
        if (r4 != null) goto L_0x0191;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x01b4, code lost:
        if (r4 == null) goto L_0x0194;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00eb, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00ec, code lost:
        r4 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00f2, code lost:
        r9 = r3;
        r4 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00f9, code lost:
        r9 = r3;
        r4 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0100, code lost:
        r9 = r3;
        r4 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0150, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0158, code lost:
        r9 = com.huawei.media.scan.BlackListUtils.TAG;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x015f, code lost:
        r9 = com.huawei.media.scan.BlackListUtils.TAG;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0166, code lost:
        r9 = com.huawei.media.scan.BlackListUtils.TAG;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x01a2 A[Catch:{ SQLException -> 0x01ad, OperationApplicationException -> 0x01a3, RemoteException -> 0x0199, all -> 0x0197 }] */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x01ac A[Catch:{ SQLException -> 0x01ad, OperationApplicationException -> 0x01a3, RemoteException -> 0x0199, all -> 0x0197 }] */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x01dc  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00eb A[ExcHandler: all (th java.lang.Throwable), Splitter:B:32:0x00a4] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0150 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:18:0x0074] */
    public void updateBlackListInMediaProvider() {
        ArrayList<ContentProviderOperation> ops;
        ContentProviderClient mediaProvider;
        String str;
        Throwable th;
        int count;
        String path;
        String[] selectionArgs = {"0"};
        int count2 = 0;
        Uri limitUri = this.mFilesUri.buildUpon().appendQueryParameter("limit", SQL_QUERY_LIMIT).build();
        long start = System.currentTimeMillis();
        ArrayList<ContentProviderOperation> ops2 = new ArrayList<>();
        ContentProviderClient mediaProvider2 = this.mMediaProvider;
        int fileCount = 0;
        Cursor cursor = null;
        long lastId = Long.MIN_VALUE;
        while (count2 < 100) {
            try {
                selectionArgs[0] = BuildConfig.FLAVOR + lastId;
                count = count2 + 1;
                if (cursor != null) {
                    try {
                        cursor.close();
                        cursor = null;
                    } catch (SQLException e) {
                        str = TAG;
                        Log.e(str, "updateBlackListFile SQLException ! ");
                    } catch (OperationApplicationException e2) {
                        str = TAG;
                        Log.e(str, "MediaProvider upate all file Exception when use the applyBatch ! ");
                        if (cursor != null) {
                        }
                        long end = System.currentTimeMillis();
                        Log.i(str, "updateBlackListFile time:" + (end - start) + ",fileCount" + fileCount);
                        return;
                    } catch (RemoteException e3) {
                        str = TAG;
                        Log.e(str, "updateBlackListFile RemoteException ! ");
                        if (cursor != null) {
                        }
                        long end2 = System.currentTimeMillis();
                        Log.i(str, "updateBlackListFile time:" + (end2 - start) + ",fileCount" + fileCount);
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                }
            } catch (SQLException e4) {
                str = TAG;
                Log.e(str, "updateBlackListFile SQLException ! ");
            } catch (OperationApplicationException e5) {
                str = TAG;
                Log.e(str, "MediaProvider upate all file Exception when use the applyBatch ! ");
                if (cursor != null) {
                }
                long end22 = System.currentTimeMillis();
                Log.i(str, "updateBlackListFile time:" + (end22 - start) + ",fileCount" + fileCount);
                return;
            } catch (RemoteException e6) {
                str = TAG;
                Log.e(str, "updateBlackListFile RemoteException ! ");
                if (cursor != null) {
                }
                long end222 = System.currentTimeMillis();
                Log.i(str, "updateBlackListFile time:" + (end222 - start) + ",fileCount" + fileCount);
                return;
            } catch (Throwable th3) {
                th = th3;
                if (cursor != null) {
                }
                throw th;
            }
            try {
                String[] strArr = FILES_BLACKLIST_PROJECTION_MEDIA;
                path = TAG;
                Cursor cursor2 = mediaProvider2.query(limitUri, strArr, "_id>? and (media_type=10 or media_type=1)", selectionArgs, "_id", null);
                if (cursor2 != null) {
                    try {
                        if (cursor2.getCount() == 0) {
                            str = path;
                            ops = ops2;
                            mediaProvider = mediaProvider2;
                        } else {
                            while (cursor2.moveToNext()) {
                                long rowId = cursor2.getLong(0);
                                try {
                                    str = path;
                                    fileCount++;
                                    try {
                                        withValues(ops2, cursor2.getString(1), cursor2.getInt(2), rowId);
                                        path = str;
                                        ops2 = ops2;
                                        mediaProvider2 = mediaProvider2;
                                        lastId = rowId;
                                    } catch (SQLException e7) {
                                        cursor = cursor2;
                                        Log.e(str, "updateBlackListFile SQLException ! ");
                                    } catch (OperationApplicationException e8) {
                                        cursor = cursor2;
                                        Log.e(str, "MediaProvider upate all file Exception when use the applyBatch ! ");
                                        if (cursor != null) {
                                        }
                                        long end2222 = System.currentTimeMillis();
                                        Log.i(str, "updateBlackListFile time:" + (end2222 - start) + ",fileCount" + fileCount);
                                        return;
                                    } catch (RemoteException e9) {
                                        cursor = cursor2;
                                        Log.e(str, "updateBlackListFile RemoteException ! ");
                                        if (cursor != null) {
                                        }
                                        long end22222 = System.currentTimeMillis();
                                        Log.i(str, "updateBlackListFile time:" + (end22222 - start) + ",fileCount" + fileCount);
                                        return;
                                    } catch (Throwable th4) {
                                        th = th4;
                                        cursor = cursor2;
                                        if (cursor != null) {
                                        }
                                        throw th;
                                    }
                                } catch (SQLException e10) {
                                    cursor = cursor2;
                                    Log.e(str, "updateBlackListFile SQLException ! ");
                                } catch (OperationApplicationException e11) {
                                    cursor = cursor2;
                                    Log.e(str, "MediaProvider upate all file Exception when use the applyBatch ! ");
                                    if (cursor != null) {
                                    }
                                    long end222222 = System.currentTimeMillis();
                                    Log.i(str, "updateBlackListFile time:" + (end222222 - start) + ",fileCount" + fileCount);
                                    return;
                                } catch (RemoteException e12) {
                                    cursor = cursor2;
                                    Log.e(str, "updateBlackListFile RemoteException ! ");
                                    if (cursor != null) {
                                    }
                                    long end2222222 = System.currentTimeMillis();
                                    Log.i(str, "updateBlackListFile time:" + (end2222222 - start) + ",fileCount" + fileCount);
                                    return;
                                } catch (Throwable th5) {
                                }
                            }
                            cursor = cursor2;
                            count2 = count;
                        }
                    } catch (SQLException e13) {
                        str = path;
                        cursor = cursor2;
                        Log.e(str, "updateBlackListFile SQLException ! ");
                    } catch (OperationApplicationException e14) {
                        str = path;
                        cursor = cursor2;
                        Log.e(str, "MediaProvider upate all file Exception when use the applyBatch ! ");
                        if (cursor != null) {
                        }
                        long end22222222 = System.currentTimeMillis();
                        Log.i(str, "updateBlackListFile time:" + (end22222222 - start) + ",fileCount" + fileCount);
                        return;
                    } catch (RemoteException e15) {
                        str = path;
                        cursor = cursor2;
                        Log.e(str, "updateBlackListFile RemoteException ! ");
                        if (cursor != null) {
                        }
                        long end222222222 = System.currentTimeMillis();
                        Log.i(str, "updateBlackListFile time:" + (end222222222 - start) + ",fileCount" + fileCount);
                        return;
                    } catch (Throwable th6) {
                        th = th6;
                        cursor = cursor2;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                } else {
                    str = path;
                    ops = ops2;
                    mediaProvider = mediaProvider2;
                }
                cursor = cursor2;
                count2 = count;
                break;
            } catch (SQLException e16) {
                str = path;
                Log.e(str, "updateBlackListFile SQLException ! ");
            } catch (OperationApplicationException e17) {
                str = path;
                Log.e(str, "MediaProvider upate all file Exception when use the applyBatch ! ");
                if (cursor != null) {
                }
                long end2222222222 = System.currentTimeMillis();
                Log.i(str, "updateBlackListFile time:" + (end2222222222 - start) + ",fileCount" + fileCount);
                return;
            } catch (RemoteException e18) {
                str = path;
                Log.e(str, "updateBlackListFile RemoteException ! ");
                if (cursor != null) {
                }
                long end22222222222 = System.currentTimeMillis();
                Log.i(str, "updateBlackListFile time:" + (end22222222222 - start) + ",fileCount" + fileCount);
                return;
            } catch (Throwable th7) {
            }
        }
        ops = ops2;
        mediaProvider = mediaProvider2;
        str = TAG;
        if (count2 == 100) {
            try {
                Log.i(str, "SQL query exceed the limit 100 !");
            } catch (SQLException e19) {
                Log.e(str, "updateBlackListFile SQLException ! ");
            } catch (OperationApplicationException e20) {
                Log.e(str, "MediaProvider upate all file Exception when use the applyBatch ! ");
                if (cursor != null) {
                    cursor.close();
                }
                long end222222222222 = System.currentTimeMillis();
                Log.i(str, "updateBlackListFile time:" + (end222222222222 - start) + ",fileCount" + fileCount);
                return;
            } catch (RemoteException e21) {
                Log.e(str, "updateBlackListFile RemoteException ! ");
                if (cursor != null) {
                    cursor.close();
                }
                long end2222222222222 = System.currentTimeMillis();
                Log.i(str, "updateBlackListFile time:" + (end2222222222222 - start) + ",fileCount" + fileCount);
                return;
            } catch (Throwable th8) {
                th = th8;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
        mediaProvider.applyBatch(ops);
    }

    private void withValues(ArrayList<ContentProviderOperation> ops, String path, int mediatype, long rowId) {
        ContentValues values = new ContentValues();
        boolean isBlackListFlag = isBlackListPath(path, getRootDirLength(path));
        if (isBlackListFlag && mediatype == 1) {
            values.put("media_type", (Integer) 10);
            ops.add(ContentProviderOperation.newUpdate(ContentUris.withAppendedId(this.mImagesUri, rowId)).withValues(values).build());
        } else if (isBlackListFlag || mediatype != 10) {
            Log.v(TAG, "withValues, mediatype = " + mediatype);
        } else {
            values.put("media_type", (Integer) 1);
            ops.add(ContentProviderOperation.newUpdate(ContentUris.withAppendedId(this.mImagesUri, rowId)).withValues(values).build());
        }
    }

    public void init() {
        Log.i(TAG, "init");
        CacheBlackList.getBlackList(BLACK_LIST);
        this.mCurBlackListVersion = BLACKLIST_VERSION_NUMBER;
        File file = new File(getBlacklistNativeConfigFile());
        if (file.exists()) {
            int cmpVersionNumber = extractBlackList(true, getBlacklistNativeConfigFile());
            if (cmpVersionNumber > this.mCurBlackListVersion) {
                extractBlackList(false, getBlacklistNativeConfigFile());
                this.mCurBlackListVersion = cmpVersionNumber;
            } else if (!file.delete()) {
                Log.e(TAG, "delete fail");
            }
        }
        SharedPreferences sp = this.mContext.getSharedPreferences(SHARDPREFERENCE_BLACKLIST, 0);
        int sharedVersionNumber = sp.getInt(UPDATE_BLACKLIST_VERSION, 0);
        Log.i(TAG, "get blackList version from SharedPreferences is " + sharedVersionNumber + ", current version is " + this.mCurBlackListVersion);
        if (this.mCurBlackListVersion > sharedVersionNumber) {
            SharedPreferences.Editor edit = sp.edit();
            edit.putInt(UPDATE_BLACKLIST_VERSION, this.mCurBlackListVersion);
            edit.commit();
            HwMediaScannerImpl hwMediaScannerImpl = this.mHwMediaScannerImpl;
            if (hwMediaScannerImpl != null) {
                hwMediaScannerImpl.initAndUpdateBlackList();
            }
        }
    }

    public void updateBlackList() {
        if (copyFileFromRemote()) {
            Log.i(TAG, "receiver the updateBlackList, copyFileFromRemote sucess and start update .");
            this.mCurBlackListVersion = extractBlackList(false, getBlacklistNativeConfigFile());
            SharedPreferences.Editor edit = this.mContext.getSharedPreferences(SHARDPREFERENCE_BLACKLIST, 0).edit();
            edit.putInt(UPDATE_BLACKLIST_VERSION, this.mCurBlackListVersion);
            edit.commit();
            updateBlackListInMediaProvider();
            Log.i(TAG, "receiver the updateBlackList, updated over! in use BlackListVersionNumber = " + this.mCurBlackListVersion);
        }
    }

    public int getBlackListFlag(String filePath) {
        if (filePath == null) {
            return 0;
        }
        String path = filePath + File.separatorChar;
        int result = 0;
        for (String blacklist : BLACK_LIST) {
            if (blacklist.length() > path.length()) {
                if (blacklist.substring(0, path.length()).equalsIgnoreCase(path)) {
                    result = 1;
                }
            } else if (blacklist.length() < path.length()) {
                if (path.substring(0, blacklist.length()).equalsIgnoreCase(blacklist)) {
                    return 2;
                }
            } else if (path.equalsIgnoreCase(blacklist)) {
                return 2;
            }
        }
        return result;
    }
}
