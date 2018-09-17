package android.media;

import android.app.backup.FullBackup;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.DrmInitData.SchemeInitData;
import android.media.MediaCodec.CryptoInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.IBinder;
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
    public static final int SAMPLE_FLAG_SYNC = 1;
    public static final int SEEK_TO_CLOSEST_SYNC = 2;
    public static final int SEEK_TO_NEXT_SYNC = 1;
    public static final int SEEK_TO_PREVIOUS_SYNC = 0;
    private long mNativeContext;

    /* renamed from: android.media.MediaExtractor.1 */
    class AnonymousClass1 extends DrmInitData {
        final /* synthetic */ Map val$initDataMap;

        AnonymousClass1(Map val$initDataMap) {
            this.val$initDataMap = val$initDataMap;
        }

        public SchemeInitData get(UUID schemeUuid) {
            return (SchemeInitData) this.val$initDataMap.get(schemeUuid);
        }
    }

    /* renamed from: android.media.MediaExtractor.2 */
    class AnonymousClass2 extends DrmInitData {
        final /* synthetic */ byte[] val$data;

        AnonymousClass2(byte[] val$data) {
            this.val$data = val$data;
        }

        public SchemeInitData get(UUID schemeUuid) {
            return new SchemeInitData("webm", this.val$data);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaExtractor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaExtractor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaExtractor.<clinit>():void");
    }

    private native Map<String, Object> getFileFormatNative();

    private native Map<String, Object> getTrackFormatNative(int i);

    private final native void nativeSetDataSource(IBinder iBinder, String str, String[] strArr, String[] strArr2) throws IOException;

    private final native void native_finalize();

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

    public final void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException {
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals(WifiManager.EXTRA_PASSPOINT_ICON_FILE)) {
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
            setDataSource(uri.toString(), headers);
        } catch (IOException e2) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
            setDataSource(uri.toString(), headers);
        } catch (Throwable th) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        }
    }

    public final void setDataSource(String path, Map<String, String> headers) throws IOException {
        String[] strArr = null;
        String[] strArr2 = null;
        if (headers != null) {
            strArr = new String[headers.size()];
            strArr2 = new String[headers.size()];
            int i = 0;
            for (Entry<String, String> entry : headers.entrySet()) {
                strArr[i] = (String) entry.getKey();
                strArr2[i] = (String) entry.getValue();
                i += SEEK_TO_NEXT_SYNC;
            }
        }
        nativeSetDataSource(MediaHTTPService.createHttpServiceBinderIfNecessary(path), path, strArr, strArr2);
    }

    public final void setDataSource(String path) throws IOException {
        nativeSetDataSource(MediaHTTPService.createHttpServiceBinderIfNecessary(path), path, null, null);
    }

    public final void setDataSource(AssetFileDescriptor afd) throws IOException, IllegalArgumentException, IllegalStateException {
        Preconditions.checkNotNull(afd);
        if (afd.getDeclaredLength() < 0) {
            setDataSource(afd.getFileDescriptor());
        } else {
            setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
        }
    }

    public final void setDataSource(FileDescriptor fd) throws IOException {
        setDataSource(fd, 0, 576460752303423487L);
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
            Map<UUID, SchemeInitData> initDataMap = new HashMap();
            for (Entry<UUID, byte[]> e : psshMap.entrySet()) {
                initDataMap.put((UUID) e.getKey(), new SchemeInitData("cenc", (byte[]) e.getValue()));
            }
            return new AnonymousClass1(initDataMap);
        }
        int numTracks = getTrackCount();
        for (int i = 0; i < numTracks; i += SEEK_TO_NEXT_SYNC) {
            Map<String, Object> trackFormatMap = getTrackFormatNative(i);
            if (trackFormatMap.containsKey("crypto-key")) {
                ByteBuffer buf = (ByteBuffer) trackFormatMap.get("crypto-key");
                buf.rewind();
                byte[] data = new byte[buf.remaining()];
                buf.get(data);
                return new AnonymousClass2(data);
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
}
