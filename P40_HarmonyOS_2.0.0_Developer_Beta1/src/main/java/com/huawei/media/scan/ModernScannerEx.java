package com.huawei.media.scan;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import com.huawei.annotation.HwSystemApi;
import java.io.File;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

@HwSystemApi
public class ModernScannerEx {
    private FileVisitor<Path> mVisitor;

    public ModernScannerEx(Context context, FileVisitor<Path> visitor, File root, ContentValues contentValues) {
        this.mVisitor = visitor;
    }

    public FileVisitor<Path> getFileVisitorEx() {
        return this.mVisitor;
    }

    public boolean isSkipQuery(Path file) {
        return false;
    }

    public long queryFromCache(Path file, BasicFileAttributes attrs) {
        return -1;
    }

    public void withStorageId(ContentProviderOperation.Builder op, File file) {
    }

    public void scanItemAudio(ContentProviderOperation.Builder op, MediaMetadataRetriever mmr, File file, String mimeType) {
    }

    public void scanItemImage(ExifInterface exif, ContentProviderOperation.Builder op, File file, BasicFileAttributes attrs) {
    }

    public void setScannedId(long scannedId) {
    }

    public ArrayList<Long> getWhiteListIds() {
        return new ArrayList<>();
    }
}
