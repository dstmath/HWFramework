package android.common;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaInserter;
import android.media.MediaScanner;
import android.media.MediaScanner.MyMediaScannerClient;
import android.net.Uri;

public class HwMediaScannerManagerDummy implements HwMediaScannerManager {
    private static HwMediaScannerManager mHwMediaScannerManager = new HwMediaScannerManagerDummy();

    private HwMediaScannerManagerDummy() {
    }

    public static HwMediaScannerManager getDefault() {
        return mHwMediaScannerManager;
    }

    public void setMediaInserter(MediaInserter mediaInserter) {
    }

    public void scanCustomDirectories(MediaScanner scanner, MyMediaScannerClient mClient, String[] directories, String volumeName, String[] whiteList, String[] blackList) {
        scanner.scanDirectories(directories);
    }

    public int getBufferSize(Uri tableUri, int bufferSizePerUri) {
        return bufferSizePerUri;
    }

    public void setHwDefaultRingtoneFileNames() {
    }

    public boolean hwNeedSetSettings(String path) {
        return false;
    }

    public void hwSetRingtone2Settings(boolean needToSetSettings2, boolean ringtones, Uri tableUri, long rowId, Context context) {
    }

    public String getExtSdcardVolumePath(Context context) {
        return null;
    }

    public boolean isSkipExtSdcard(ContentProviderClient mMediaProvider, String mExtStroagePath, String mPackageName, Uri mFilesUriNoNotify) {
        return false;
    }

    public boolean isBitmapSizeTooLarge(String path) {
        return false;
    }

    public void initializeHwVoiceAndFocus(String path, ContentValues values) {
    }

    public void pruneDeadThumbnailsFolder() {
    }

    public boolean preHandleStringTag(String value, String mimetype) {
        return false;
    }

    public void initializeSniffer(String path) {
    }

    public void resetSniffer() {
    }

    public String postHandleStringTag(String value, String path, int flag) {
        return value;
    }

    public void deleteNomediaFile() {
    }
}
