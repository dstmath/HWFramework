package com.huawei.media.scan;

import android.content.ContentProviderClient;
import android.content.Context;
import android.media.HwMediaMonitorImpl;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import com.huawei.android.provider.MediaStoreEx;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.Stack;

public class HwMediaScannerFileVisitor implements FileVisitor<Path> {
    private static final boolean LOGD = Log.isLoggable(TAG, 3);
    private static final boolean LOGI = Log.isLoggable(TAG, 4);
    private static final boolean LOGV = Log.isLoggable(TAG, 2);
    private static final String TAG = "HwMediaScannerFileVisitor";
    private static long sScanDirectoryFilesNum;
    private AudioFilterUtils mAudioFilterUtils;
    private int mBlackListFlag;
    private Stack<Integer> mBlackListStack;
    private BlackListUtils mBlackListUtils;
    private boolean mIsSkipWhiteList;
    private ContentProviderClient mMediaProvider = null;
    private FileVisitor<Path> mOriginalFileVisitor;
    private final File mRoot;
    private String mRootPath;
    private final boolean mSingleFile;
    private int mWhiteListFlag;
    private Stack<Integer> mWhiteListStack;
    private WhiteListUtils mWhiteListUtils;

    public HwMediaScannerFileVisitor(@NonNull Context context, FileVisitor<Path> originalFileVisitor, HwMediaScannerImpl hwMediaScanner, File root, boolean isSkipWhiteList) {
        this.mRoot = root;
        this.mSingleFile = this.mRoot.isFile();
        this.mOriginalFileVisitor = originalFileVisitor;
        if (context != null) {
            this.mMediaProvider = context.getContentResolver().acquireContentProviderClient("media");
        }
        if (hwMediaScanner != null) {
            this.mWhiteListUtils = hwMediaScanner.getWhiteListUtils();
            this.mBlackListUtils = hwMediaScanner.getBlackListUtils();
            this.mAudioFilterUtils = hwMediaScanner.getAudioFilterUtils();
        }
        this.mIsSkipWhiteList = isSkipWhiteList;
        this.mBlackListStack = new Stack<>();
        this.mWhiteListStack = new Stack<>();
        this.mBlackListFlag = 1;
        this.mWhiteListFlag = 1;
    }

    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        FileVisitResult result;
        Stack<Integer> stack;
        Stack<Integer> stack2;
        if (!this.mSingleFile) {
            addScanDirectoryFilesNum();
        }
        if (!(this.mBlackListUtils == null || (stack2 = this.mBlackListStack) == null)) {
            if (stack2.size() == 0) {
                this.mRootPath = path.toString().substring(0, this.mBlackListUtils.getRootDirLength(path.toString()));
            }
            this.mBlackListStack.push(Integer.valueOf(this.mBlackListFlag));
            if (LOGV) {
                Log.v(TAG, "preVisitDirectory1 " + path + " mBlackListStack:" + this.mBlackListStack.size() + " mBlackListFlag:" + this.mBlackListFlag);
            }
            if (this.mBlackListFlag == 1) {
                this.mBlackListFlag = this.mBlackListUtils.getBlackListFlag(path.toString().substring(this.mRootPath.length()));
            }
            if (LOGV) {
                Log.v(TAG, "preVisitDirectory2 " + path + " mBlackListStack:" + this.mBlackListStack.size() + " mBlackListFlag:" + this.mBlackListFlag);
            }
        }
        if (!(this.mWhiteListUtils == null || (stack = this.mWhiteListStack) == null)) {
            stack.push(Integer.valueOf(this.mWhiteListFlag));
            if (LOGV) {
                Log.v(TAG, "preVisitDirectory1 " + path + " mWhiteListStack:" + this.mWhiteListStack.size() + ",mWhiteListFlag:" + this.mWhiteListFlag);
            }
            if (this.mWhiteListFlag == 1) {
                this.mWhiteListFlag = this.mWhiteListUtils.getWhiteListFlag(path.toString());
            }
            if (LOGV) {
                Log.v(TAG, "preVisitDirectory2 " + path + " mWhiteListStack:" + this.mWhiteListStack.size() + ",mWhiteListFlag:" + this.mWhiteListFlag);
            }
        }
        if (skipDir(path)) {
            result = FileVisitResult.SKIP_SUBTREE;
        } else {
            result = this.mOriginalFileVisitor.preVisitDirectory(path, basicFileAttributes);
        }
        if (result != FileVisitResult.CONTINUE) {
            Stack<Integer> stack3 = this.mBlackListStack;
            if (stack3 != null && !stack3.empty()) {
                this.mBlackListFlag = this.mBlackListStack.pop().intValue();
            }
            Stack<Integer> stack4 = this.mWhiteListStack;
            if (stack4 != null && !stack4.empty()) {
                this.mWhiteListFlag = this.mWhiteListStack.pop().intValue();
            }
        }
        return result;
    }

    private boolean skipDir(Path path) {
        Object obj;
        boolean z = true;
        boolean skip = isInWhitekList() && this.mIsSkipWhiteList;
        WhiteListUtils whiteListUtils = this.mWhiteListUtils;
        if (whiteListUtils != null) {
            if (!skip || !whiteListUtils.isWhiteListValid()) {
                z = false;
            }
            skip = z;
        }
        if (LOGV) {
            StringBuilder sb = new StringBuilder();
            sb.append("skipDir path ");
            sb.append(path);
            sb.append(", skip ");
            sb.append(skip);
            sb.append(", mIsSkipWhiteList ");
            sb.append(this.mIsSkipWhiteList);
            sb.append(", isWhiteListValid ");
            WhiteListUtils whiteListUtils2 = this.mWhiteListUtils;
            if (whiteListUtils2 == null) {
                obj = "null";
            } else {
                obj = Boolean.valueOf(whiteListUtils2.isWhiteListValid());
            }
            sb.append(obj);
            Log.d(TAG, sb.toString());
        }
        return skip;
    }

    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        String filePath;
        AudioFilterUtils audioFilterUtils;
        if (LOGV) {
            Log.v(TAG, "visitFile " + path);
        }
        if (!this.mSingleFile) {
            addScanDirectoryFilesNum();
        }
        if (!(path == null || path.toString() == null || (filePath = path.toString()) == null)) {
            if (filePath.toUpperCase(Locale.US).endsWith(".HEIC") || filePath.endsWith(".HEIF")) {
                HwMediaMonitorImpl.getDefault().writeBigData(Utils.BD_MEDIA_HEIF, Utils.M_HEIF_SCAN);
            }
            if ("internal".equals(MediaStoreEx.getVolumeName(this.mRoot)) && (audioFilterUtils = this.mAudioFilterUtils) != null && audioFilterUtils.isNeedAudioFilter() && this.mAudioFilterUtils.isAudioFilterFile(filePath)) {
                deleteAudioFilterFileRecords(path);
                return FileVisitResult.CONTINUE;
            }
        }
        return this.mOriginalFileVisitor.visitFile(path, basicFileAttributes);
    }

    private void deleteAudioFilterFileRecords(Path path) {
        try {
            if (this.mMediaProvider != null) {
                this.mMediaProvider.delete(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, "_data=?", new String[]{path.toString()});
                Log.i(TAG, "deleteAudioFilterFile");
                if (LOGV) {
                    Log.i(TAG, "deleteAudioFilterFile" + path.toString());
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in deleteAudioFilterFileRecords");
        }
    }

    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        return this.mOriginalFileVisitor.visitFileFailed(path, e);
    }

    public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
        if (LOGV) {
            Log.e(TAG, "postVisitDirectory " + path + " mBlackListStack:" + this.mBlackListStack.size() + ",mWhiteListStack:" + this.mWhiteListStack.size());
        }
        Stack<Integer> stack = this.mBlackListStack;
        if (stack != null && !stack.empty()) {
            this.mBlackListFlag = this.mBlackListStack.pop().intValue();
        }
        Stack<Integer> stack2 = this.mWhiteListStack;
        if (stack2 != null && !stack2.empty()) {
            this.mWhiteListFlag = this.mWhiteListStack.pop().intValue();
        }
        return this.mOriginalFileVisitor.postVisitDirectory(path, e);
    }

    public boolean isInBlackList() {
        return this.mBlackListFlag == 2;
    }

    public boolean isInWhitekList() {
        return this.mWhiteListFlag == 2;
    }

    private static void addScanDirectoryFilesNum() {
        sScanDirectoryFilesNum++;
    }

    public static void startScanDirectory() {
        sScanDirectoryFilesNum = 0;
    }

    public static long getScanDirectoryFilesNum() {
        return sScanDirectoryFilesNum;
    }
}
