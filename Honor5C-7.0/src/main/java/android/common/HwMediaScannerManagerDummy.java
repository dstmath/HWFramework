package android.common;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaInserter;
import android.media.MediaScanner;
import android.media.MediaScanner.MyMediaScannerClient;
import android.net.Uri;

public class HwMediaScannerManagerDummy implements HwMediaScannerManager {
    private static HwMediaScannerManager mHwMediaScannerManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.common.HwMediaScannerManagerDummy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.common.HwMediaScannerManagerDummy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.common.HwMediaScannerManagerDummy.<clinit>():void");
    }

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
}
