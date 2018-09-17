package android.media;

import android.app.backup.FullBackup;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.DrmInitData.SchemeInitData;
import android.media.MediaCas.Session;
import android.media.MediaCodec.CryptoInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.PersistableBundle;
import com.android.internal.util.Preconditions;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public final class MediaExtractor {
    public static final int SAMPLE_FLAG_ENCRYPTED = 2;
    public static final int SAMPLE_FLAG_PARTIAL_FRAME = 4;
    public static final int SAMPLE_FLAG_SYNC = 1;
    public static final int SEEK_TO_CLOSEST_SYNC = 2;
    public static final int SEEK_TO_NEXT_SYNC = 1;
    public static final int SEEK_TO_PREVIOUS_SYNC = 0;
    private MediaCas mMediaCas;
    private long mNativeContext;

    public static final class CasInfo {
        private final Session mSession;
        private final int mSystemId;

        CasInfo(int systemId, Session session) {
            this.mSystemId = systemId;
            this.mSession = session;
        }

        public int getSystemId() {
            return this.mSystemId;
        }

        public Session getSession() {
            return this.mSession;
        }
    }

    public static final class MetricsConstants {
        public static final String FORMAT = "android.media.mediaextractor.fmt";
        public static final String MIME_TYPE = "android.media.mediaextractor.mime";
        public static final String TRACKS = "android.media.mediaextractor.ntrk";

        private MetricsConstants() {
        }
    }

    private native Map<String, Object> getFileFormatNative();

    private native Map<String, Object> getTrackFormatNative(int i);

    private final native void nativeSetDataSource(IBinder iBinder, String str, String[] strArr, String[] strArr2) throws IOException;

    private final native void nativeSetMediaCas(IBinder iBinder);

    private final native void native_finalize();

    private native PersistableBundle native_getMetrics();

    private static final native void native_init();

    private final native void native_setup();

    public native boolean advance();

    public native long getCachedDuration();

    public native boolean getSampleCryptoInfo(CryptoInfo cryptoInfo);

    public native int getSampleFlags();

    public native long getSampleTime();

    public native int getSampleTrackIndex();

    public final native int getTrackCount();

    public native boolean hasCacheReachedEndOfStream();

    public native int readSampleData(ByteBuffer byteBuffer, int i);

    public final native void release();

    public native void seekTo(long j, int i);

    public native void selectTrack(int i);

    public final native void setDataSource(MediaDataSource mediaDataSource) throws IOException;

    public final native void setDataSource(FileDescriptor fileDescriptor, long j, long j2) throws IOException;

    public native void unselectTrack(int i);

    public MediaExtractor() {
        native_setup();
    }

    /* JADX WARNING: Missing block: B:25:0x0059, code:
            setDataSource(r13.toString(), r14);
     */
    /* JADX WARNING: Missing block: B:26:0x0060, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException {
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals(ContentResolver.SCHEME_FILE)) {
            setDataSource(uri.getPath());
            return;
        }
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
            if (assetFileDescriptor == null) {
                if (assetFileDescriptor != null) {
                    assetFileDescriptor.close();
                }
                return;
            }
            if (assetFileDescriptor.getDeclaredLength() < 0) {
                setDataSource(assetFileDescriptor.getFileDescriptor());
            } else {
                setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getDeclaredLength());
            }
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (SecurityException e) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (IOException e2) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (Throwable th) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        }
    }

    public final void setDataSource(String path, Map<String, String> headers) throws IOException {
        String[] keys = null;
        String[] values = null;
        if (headers != null) {
            keys = new String[headers.size()];
            values = new String[headers.size()];
            int i = 0;
            for (Entry<String, String> entry : headers.entrySet()) {
                keys[i] = (String) entry.getKey();
                values[i] = (String) entry.getValue();
                i++;
            }
        }
        nativeSetDataSource(MediaHTTPService.createHttpServiceBinderIfNecessary(path), path, keys, values);
    }

    public final void setDataSource(String path) throws IOException {
        nativeSetDataSource(MediaHTTPService.createHttpServiceBinderIfNecessary(path), path, null, null);
    }

    public final void setDataSource(AssetFileDescriptor afd) throws IOException, IllegalArgumentException, IllegalStateException {
        Preconditions.checkNotNull(afd);
        if (afd.getDeclaredLength() < 0) {
            setDataSource(afd.getFileDescriptor());
            return;
        }
        setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
    }

    public final void setDataSource(FileDescriptor fd) throws IOException {
        setDataSource(fd, 0, 576460752303423487L);
    }

    public final void setMediaCas(MediaCas mediaCas) {
        this.mMediaCas = mediaCas;
        nativeSetMediaCas(mediaCas.getBinder());
    }

    public CasInfo getCasInfo(int index) {
        Map<String, Object> formatMap = getTrackFormatNative(index);
        if (!formatMap.containsKey(MediaFormat.KEY_CA_SYSTEM_ID)) {
            return null;
        }
        int systemId = ((Integer) formatMap.get(MediaFormat.KEY_CA_SYSTEM_ID)).intValue();
        Session session = null;
        if (this.mMediaCas != null && formatMap.containsKey(MediaFormat.KEY_CA_SESSION_ID)) {
            ByteBuffer buf = (ByteBuffer) formatMap.get(MediaFormat.KEY_CA_SESSION_ID);
            buf.rewind();
            byte[] sessionId = new byte[buf.remaining()];
            buf.get(sessionId);
            session = this.mMediaCas.createFromSessionId(sessionId);
        }
        return new CasInfo(systemId, session);
    }

    protected void finalize() {
        native_finalize();
    }

    public DrmInitData getDrmInitData() {
        Map<String, Object> formatMap = getFileFormatNative();
        if (formatMap == null) {
            return null;
        }
        if (formatMap.containsKey("pssh")) {
            Map<UUID, byte[]> psshMap = getPsshInfo();
            final Map<UUID, SchemeInitData> initDataMap = new HashMap();
            for (Entry<UUID, byte[]> e : psshMap.entrySet()) {
                initDataMap.put((UUID) e.getKey(), new SchemeInitData("cenc", (byte[]) e.getValue()));
            }
            return new DrmInitData() {
                public SchemeInitData get(UUID schemeUuid) {
                    return (SchemeInitData) initDataMap.get(schemeUuid);
                }
            };
        }
        int numTracks = getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            Map<String, Object> trackFormatMap = getTrackFormatNative(i);
            if (trackFormatMap.containsKey("crypto-key")) {
                ByteBuffer buf = (ByteBuffer) trackFormatMap.get("crypto-key");
                buf.rewind();
                final byte[] data = new byte[buf.remaining()];
                buf.get(data);
                return new DrmInitData() {
                    public SchemeInitData get(UUID schemeUuid) {
                        return new SchemeInitData("webm", data);
                    }
                };
            }
        }
        return null;
    }

    public Map<UUID, byte[]> getPsshInfo() {
        Map<UUID, byte[]> psshMap = null;
        Map<String, Object> formatMap = getFileFormatNative();
        if (formatMap != null && formatMap.containsKey("pssh")) {
            ByteBuffer rawpssh = (ByteBuffer) formatMap.get("pssh");
            rawpssh.order(ByteOrder.nativeOrder());
            rawpssh.rewind();
            formatMap.remove("pssh");
            psshMap = new HashMap();
            while (rawpssh.remaining() > 0) {
                rawpssh.order(ByteOrder.BIG_ENDIAN);
                UUID uuid = new UUID(rawpssh.getLong(), rawpssh.getLong());
                rawpssh.order(ByteOrder.nativeOrder());
                byte[] psshdata = new byte[rawpssh.getInt()];
                rawpssh.get(psshdata);
                psshMap.put(uuid, psshdata);
            }
        }
        return psshMap;
    }

    public MediaFormat getTrackFormat(int index) {
        return new MediaFormat(getTrackFormatNative(index));
    }

    public PersistableBundle getMetrics() {
        return native_getMetrics();
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }
}
