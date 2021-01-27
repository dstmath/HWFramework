package android.common;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.media.ExifInterface;
import android.media.MediaInserter;
import android.media.MediaScanner;
import android.net.Uri;
import android.os.storage.StorageVolume;

public interface HwMediaScannerManager {
    void deleteNomediaFile(StorageVolume[] storageVolumeArr);

    int getBufferSize(Uri uri, int i);

    String getExtSdcardVolumePath(Context context);

    boolean hwNeedSetSettings(String str);

    void hwSetRingtone2Settings(boolean z, boolean z2, Uri uri, long j, Context context);

    void initializeHwVoiceAndFocus(String str, ContentValues contentValues);

    void initializeSniffer(String str);

    boolean isAudioFilterFile(String str);

    boolean isBitmapSizeTooLarge(String str);

    boolean isSkipExtSdcard(ContentProviderClient contentProviderClient, String str, String str2, Uri uri);

    boolean loadAudioFilterConfig(Context context);

    String postHandleStringTag(String str, String str2, int i);

    boolean preHandleStringTag(String str, String str2);

    void pruneDeadThumbnailsFolder();

    void resetSniffer();

    void scanCustomDirectories(MediaScanner mediaScanner, MediaScanner.MyMediaScannerClient myMediaScannerClient, String[] strArr, String[] strArr2, String[] strArr3);

    void scanHwMakerNote(ContentValues contentValues, ExifInterface exifInterface);

    void setHwDefaultRingtoneFileNames();

    void setMediaInserter(MediaInserter mediaInserter);
}
