package android.common;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.media.ExifInterface;
import android.media.MediaInserter;
import android.media.MediaScanner;
import android.net.Uri;
import android.os.storage.StorageVolume;

public class HwMediaScannerManagerDummy implements HwMediaScannerManager {
    private static HwMediaScannerManager sHwMediaScannerManager = new HwMediaScannerManagerDummy();

    private HwMediaScannerManagerDummy() {
    }

    public static HwMediaScannerManager getDefault() {
        return sHwMediaScannerManager;
    }

    @Override // android.common.HwMediaScannerManager
    public void setMediaInserter(MediaInserter mediaInserter) {
    }

    @Override // android.common.HwMediaScannerManager
    public void scanCustomDirectories(MediaScanner scanner, MediaScanner.MyMediaScannerClient mClient, String[] directories, String[] whiteList, String[] blackList) {
        scanner.scanDirectories(directories);
    }

    @Override // android.common.HwMediaScannerManager
    public int getBufferSize(Uri tableUri, int bufferSizePerUri) {
        return bufferSizePerUri;
    }

    @Override // android.common.HwMediaScannerManager
    public void setHwDefaultRingtoneFileNames() {
    }

    @Override // android.common.HwMediaScannerManager
    public boolean hwNeedSetSettings(String path) {
        return false;
    }

    @Override // android.common.HwMediaScannerManager
    public void hwSetRingtone2Settings(boolean needToSetSettings2, boolean ringtones, Uri tableUri, long rowId, Context context) {
    }

    @Override // android.common.HwMediaScannerManager
    public String getExtSdcardVolumePath(Context context) {
        return null;
    }

    @Override // android.common.HwMediaScannerManager
    public boolean isSkipExtSdcard(ContentProviderClient mediaProvider, String extStroagePath, String packageName, Uri filesUriNoNotify) {
        return false;
    }

    @Override // android.common.HwMediaScannerManager
    public boolean isBitmapSizeTooLarge(String path) {
        return false;
    }

    @Override // android.common.HwMediaScannerManager
    public void initializeHwVoiceAndFocus(String path, ContentValues values) {
    }

    @Override // android.common.HwMediaScannerManager
    public void pruneDeadThumbnailsFolder() {
    }

    @Override // android.common.HwMediaScannerManager
    public boolean preHandleStringTag(String value, String mimeType) {
        return false;
    }

    @Override // android.common.HwMediaScannerManager
    public void initializeSniffer(String path) {
    }

    @Override // android.common.HwMediaScannerManager
    public void resetSniffer() {
    }

    @Override // android.common.HwMediaScannerManager
    public String postHandleStringTag(String value, String path, int flag) {
        return value;
    }

    @Override // android.common.HwMediaScannerManager
    public void deleteNomediaFile(StorageVolume[] volumes) {
    }

    @Override // android.common.HwMediaScannerManager
    public void scanHwMakerNote(ContentValues values, ExifInterface exif) {
    }

    @Override // android.common.HwMediaScannerManager
    public boolean loadAudioFilterConfig(Context context) {
        return false;
    }

    @Override // android.common.HwMediaScannerManager
    public boolean isAudioFilterFile(String path) {
        return false;
    }
}
