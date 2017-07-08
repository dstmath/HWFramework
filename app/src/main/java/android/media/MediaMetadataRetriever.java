package android.media;

import android.app.backup.FullBackup;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class MediaMetadataRetriever {
    private static final int EMBEDDED_PICTURE_TYPE_ANY = 65535;
    public static final int METADATA_KEY_ALBUM = 1;
    public static final int METADATA_KEY_ALBUMARTIST = 13;
    public static final int METADATA_KEY_ARTIST = 2;
    public static final int METADATA_KEY_AUTHOR = 3;
    public static final int METADATA_KEY_BITRATE = 20;
    public static final int METADATA_KEY_CAPTURE_FRAMERATE = 25;
    public static final int METADATA_KEY_CD_TRACK_NUMBER = 0;
    public static final int METADATA_KEY_COMPILATION = 15;
    public static final int METADATA_KEY_COMPOSER = 4;
    public static final int METADATA_KEY_DATE = 5;
    public static final int METADATA_KEY_DISC_NUMBER = 14;
    public static final int METADATA_KEY_DURATION = 9;
    public static final int METADATA_KEY_GENRE = 6;
    public static final int METADATA_KEY_HAS_AUDIO = 16;
    public static final int METADATA_KEY_HAS_VIDEO = 17;
    public static final int METADATA_KEY_IS_DRM = 22;
    public static final int METADATA_KEY_LOCATION = 23;
    public static final int METADATA_KEY_LYRIC = 1000;
    public static final int METADATA_KEY_MIMETYPE = 12;
    public static final int METADATA_KEY_NUM_TRACKS = 10;
    public static final int METADATA_KEY_TIMED_TEXT_LANGUAGES = 21;
    public static final int METADATA_KEY_TITLE = 7;
    public static final int METADATA_KEY_VIDEO_HEIGHT = 19;
    public static final int METADATA_KEY_VIDEO_ROTATION = 24;
    public static final int METADATA_KEY_VIDEO_WIDTH = 18;
    public static final int METADATA_KEY_WRITER = 11;
    public static final int METADATA_KEY_YEAR = 8;
    public static final int OPTION_ARGB8888 = 4;
    public static final int OPTION_CLOSEST = 3;
    public static final int OPTION_CLOSEST_SYNC = 2;
    public static final int OPTION_NEXT_SYNC = 1;
    public static final int OPTION_PREVIOUS_SYNC = 0;
    private long mNativeContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaMetadataRetriever.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaMetadataRetriever.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaMetadataRetriever.<clinit>():void");
    }

    private native Bitmap _getFrameAtTime(long j, int i);

    private native void _setDataSource(MediaDataSource mediaDataSource) throws IllegalArgumentException;

    private native void _setDataSource(IBinder iBinder, String str, String[] strArr, String[] strArr2) throws IllegalArgumentException;

    private native byte[] getEmbeddedPicture(int i);

    private final native void native_finalize();

    private static native void native_init();

    private native void native_setup();

    public native String extractMetadata(int i);

    public native void release();

    public native void setDataSource(FileDescriptor fileDescriptor, long j, long j2) throws IllegalArgumentException;

    public MediaMetadataRetriever() {
        native_setup();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDataSource(String path) throws IllegalArgumentException {
        Throwable th;
        Throwable th2;
        Throwable th3 = null;
        if (path == null) {
            throw new IllegalArgumentException();
        }
        FileInputStream fileInputStream = null;
        try {
            FileInputStream is = new FileInputStream(path);
            try {
                setDataSource(is.getFD(), 0, 576460752303423487L);
                if (is != null) {
                    try {
                        is.close();
                    } catch (Throwable th4) {
                        th3 = th4;
                    }
                }
                if (th3 != null) {
                    try {
                        throw th3;
                    } catch (FileNotFoundException e) {
                        fileInputStream = is;
                    } catch (IOException e2) {
                        throw new IllegalArgumentException();
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                th2 = null;
                fileInputStream = is;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th6) {
                        if (th2 == null) {
                            th2 = th6;
                        } else if (th2 != th6) {
                            th2.addSuppressed(th6);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (FileNotFoundException e3) {
                    } catch (IOException e4) {
                        throw new IllegalArgumentException();
                    }
                }
                throw th;
            }
        } catch (Throwable th7) {
            th = th7;
            th2 = null;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (th2 == null) {
                throw th;
            } else {
                throw th2;
            }
        }
    }

    public void setDataSource(String uri, Map<String, String> headers) throws IllegalArgumentException {
        int i = METADATA_KEY_CD_TRACK_NUMBER;
        String[] keys = new String[headers.size()];
        String[] values = new String[headers.size()];
        for (Entry<String, String> entry : headers.entrySet()) {
            keys[i] = (String) entry.getKey();
            values[i] = (String) entry.getValue();
            i += OPTION_NEXT_SYNC;
        }
        _setDataSource(MediaHTTPService.createHttpServiceBinderIfNecessary(uri), uri, keys, values);
    }

    public void setDataSource(FileDescriptor fd) throws IllegalArgumentException {
        setDataSource(fd, 0, 576460752303423487L);
    }

    public void setDataSource(Context context, Uri uri) throws IllegalArgumentException, SecurityException {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals(WifiManager.EXTRA_PASSPOINT_ICON_FILE)) {
            setDataSource(uri.getPath());
            return;
        }
        AssetFileDescriptor fd = null;
        try {
            fd = context.getContentResolver().openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
            if (fd == null) {
                throw new IllegalArgumentException();
            }
            FileDescriptor descriptor = fd.getFileDescriptor();
            if (descriptor.valid()) {
                if (fd.getDeclaredLength() < 0) {
                    setDataSource(descriptor);
                } else {
                    setDataSource(descriptor, fd.getStartOffset(), fd.getDeclaredLength());
                }
                if (fd != null) {
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                }
                return;
            }
            throw new IllegalArgumentException();
        } catch (FileNotFoundException e2) {
            throw new IllegalArgumentException();
        } catch (SecurityException e3) {
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e4) {
                }
            }
            setDataSource(uri.toString());
        } catch (Throwable th) {
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e5) {
                }
            }
        }
    }

    public void setDataSource(MediaDataSource dataSource) throws IllegalArgumentException {
        _setDataSource(dataSource);
    }

    public Bitmap getFrameAtTime(long timeUs, int option) {
        if (option >= 0 && option <= OPTION_ARGB8888) {
            return _getFrameAtTime(timeUs, option);
        }
        throw new IllegalArgumentException("Unsupported option: " + option);
    }

    public Bitmap getFrameAtTime(long timeUs) {
        return getFrameAtTime(timeUs, OPTION_CLOSEST_SYNC);
    }

    public Bitmap getFrameAtTime() {
        return getFrameAtTime(-1, OPTION_CLOSEST_SYNC);
    }

    public byte[] getEmbeddedPicture() {
        return getEmbeddedPicture(EMBEDDED_PICTURE_TYPE_ANY);
    }

    protected void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }
}
