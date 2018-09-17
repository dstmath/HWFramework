package android.media;

import android.common.HwFrameworkFactory;
import android.graphics.Rect;
import android.media.Image.Plane;
import android.net.ProxyInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Surface;
import android.view.ViewRootImpl;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.NioUtils;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class MediaCodec {
    public static final int BUFFER_FLAG_CODEC_CONFIG = 2;
    public static final int BUFFER_FLAG_END_OF_STREAM = 4;
    public static final int BUFFER_FLAG_KEY_FRAME = 1;
    public static final int BUFFER_FLAG_PARTIAL_FRAME = 8;
    public static final int BUFFER_FLAG_SYNC_FRAME = 1;
    private static final int CB_ERROR = 3;
    private static final int CB_INPUT_AVAILABLE = 1;
    private static final int CB_OUTPUT_AVAILABLE = 2;
    private static final int CB_OUTPUT_FORMAT_CHANGE = 4;
    public static final int CONFIGURE_FLAG_ENCODE = 1;
    public static final int CRYPTO_MODE_AES_CBC = 2;
    public static final int CRYPTO_MODE_AES_CTR = 1;
    public static final int CRYPTO_MODE_UNENCRYPTED = 0;
    private static final int EVENT_CALLBACK = 1;
    private static final int EVENT_FRAME_RENDERED = 3;
    private static final int EVENT_SET_CALLBACK = 2;
    public static final int INFO_OUTPUT_BUFFERS_CHANGED = -3;
    public static final int INFO_OUTPUT_FORMAT_CHANGED = -2;
    public static final int INFO_TRY_AGAIN_LATER = -1;
    public static final String PARAMETER_KEY_REQUEST_SYNC_FRAME = "request-sync";
    public static final String PARAMETER_KEY_SUSPEND = "drop-input-frames";
    public static final String PARAMETER_KEY_VIDEO_BITRATE = "video-bitrate";
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;
    private final Object mBufferLock;
    private ByteBuffer[] mCachedInputBuffers;
    private ByteBuffer[] mCachedOutputBuffers;
    private Callback mCallback;
    private EventHandler mCallbackHandler;
    private final BufferMap mDequeuedInputBuffers = new BufferMap();
    private final BufferMap mDequeuedOutputBuffers = new BufferMap();
    private final Map<Integer, BufferInfo> mDequeuedOutputInfos = new HashMap();
    private EventHandler mEventHandler;
    private boolean mHasSurface = false;
    private Object mListenerLock = new Object();
    private long mNativeContext;
    private EventHandler mOnFrameRenderedHandler;
    private OnFrameRenderedListener mOnFrameRenderedListener;

    public static final class BufferInfo {
        public int flags;
        public int offset;
        public long presentationTimeUs;
        public int size;

        public void set(int newOffset, int newSize, long newTimeUs, int newFlags) {
            this.offset = newOffset;
            this.size = newSize;
            this.presentationTimeUs = newTimeUs;
            this.flags = newFlags;
        }

        public BufferInfo dup() {
            BufferInfo copy = new BufferInfo();
            copy.set(this.offset, this.size, this.presentationTimeUs, this.flags);
            return copy;
        }
    }

    private static class BufferMap {
        private final Map<Integer, CodecBuffer> mMap;

        private static class CodecBuffer {
            private ByteBuffer mByteBuffer;
            private Image mImage;

            /* synthetic */ CodecBuffer(CodecBuffer -this0) {
                this();
            }

            private CodecBuffer() {
            }

            public void free() {
                if (this.mByteBuffer != null) {
                    NioUtils.freeDirectBuffer(this.mByteBuffer);
                    this.mByteBuffer = null;
                }
                if (this.mImage != null) {
                    this.mImage.close();
                    this.mImage = null;
                }
            }

            public void setImage(Image image) {
                free();
                this.mImage = image;
            }

            public void setByteBuffer(ByteBuffer buffer) {
                free();
                this.mByteBuffer = buffer;
            }
        }

        /* synthetic */ BufferMap(BufferMap -this0) {
            this();
        }

        private BufferMap() {
            this.mMap = new HashMap();
        }

        public void remove(int index) {
            CodecBuffer buffer = (CodecBuffer) this.mMap.get(Integer.valueOf(index));
            if (buffer != null) {
                buffer.free();
                this.mMap.remove(Integer.valueOf(index));
            }
        }

        public void put(int index, ByteBuffer newBuffer) {
            CodecBuffer buffer = (CodecBuffer) this.mMap.get(Integer.valueOf(index));
            if (buffer == null) {
                buffer = new CodecBuffer();
                this.mMap.put(Integer.valueOf(index), buffer);
            }
            buffer.setByteBuffer(newBuffer);
        }

        public void put(int index, Image newImage) {
            CodecBuffer buffer = (CodecBuffer) this.mMap.get(Integer.valueOf(index));
            if (buffer == null) {
                buffer = new CodecBuffer();
                this.mMap.put(Integer.valueOf(index), buffer);
            }
            buffer.setImage(newImage);
        }

        public void clear() {
            for (CodecBuffer buffer : this.mMap.values()) {
                buffer.free();
            }
            this.mMap.clear();
        }
    }

    public static abstract class Callback {
        public abstract void onError(MediaCodec mediaCodec, CodecException codecException);

        public abstract void onInputBufferAvailable(MediaCodec mediaCodec, int i);

        public abstract void onOutputBufferAvailable(MediaCodec mediaCodec, int i, BufferInfo bufferInfo);

        public abstract void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat);
    }

    public static final class CodecException extends IllegalStateException {
        private static final int ACTION_RECOVERABLE = 2;
        private static final int ACTION_TRANSIENT = 1;
        public static final int ERROR_INSUFFICIENT_RESOURCE = 1100;
        public static final int ERROR_RECLAIMED = 1101;
        private final int mActionCode;
        private final String mDiagnosticInfo;
        private final int mErrorCode;

        CodecException(int errorCode, int actionCode, String detailMessage) {
            super(detailMessage);
            this.mErrorCode = errorCode;
            this.mActionCode = actionCode;
            this.mDiagnosticInfo = "android.media.MediaCodec.error_" + (errorCode < 0 ? "neg_" : ProxyInfo.LOCAL_EXCL_LIST) + Math.abs(errorCode);
        }

        public boolean isTransient() {
            return this.mActionCode == 1;
        }

        public boolean isRecoverable() {
            return this.mActionCode == 2;
        }

        public int getErrorCode() {
            return this.mErrorCode;
        }

        public String getDiagnosticInfo() {
            return this.mDiagnosticInfo;
        }
    }

    public static final class CryptoException extends RuntimeException {
        public static final int ERROR_INSUFFICIENT_OUTPUT_PROTECTION = 4;
        public static final int ERROR_KEY_EXPIRED = 2;
        public static final int ERROR_NO_KEY = 1;
        public static final int ERROR_RESOURCE_BUSY = 3;
        public static final int ERROR_SESSION_NOT_OPENED = 5;
        public static final int ERROR_UNSUPPORTED_OPERATION = 6;
        private int mErrorCode;

        public CryptoException(int errorCode, String detailMessage) {
            super(detailMessage);
            this.mErrorCode = errorCode;
        }

        public int getErrorCode() {
            return this.mErrorCode;
        }
    }

    public static final class CryptoInfo {
        public byte[] iv;
        public byte[] key;
        public int mode;
        public int[] numBytesOfClearData;
        public int[] numBytesOfEncryptedData;
        public int numSubSamples;
        private Pattern pattern;

        public static final class Pattern {
            private int mEncryptBlocks;
            private int mSkipBlocks;

            public Pattern(int blocksToEncrypt, int blocksToSkip) {
                set(blocksToEncrypt, blocksToSkip);
            }

            public void set(int blocksToEncrypt, int blocksToSkip) {
                this.mEncryptBlocks = blocksToEncrypt;
                this.mSkipBlocks = blocksToSkip;
            }

            public int getSkipBlocks() {
                return this.mSkipBlocks;
            }

            public int getEncryptBlocks() {
                return this.mEncryptBlocks;
            }
        }

        public void set(int newNumSubSamples, int[] newNumBytesOfClearData, int[] newNumBytesOfEncryptedData, byte[] newKey, byte[] newIV, int newMode) {
            this.numSubSamples = newNumSubSamples;
            this.numBytesOfClearData = newNumBytesOfClearData;
            this.numBytesOfEncryptedData = newNumBytesOfEncryptedData;
            this.key = newKey;
            this.iv = newIV;
            this.mode = newMode;
            this.pattern = new Pattern(0, 0);
        }

        public void setPattern(Pattern newPattern) {
            this.pattern = newPattern;
        }

        public String toString() {
            int i;
            StringBuilder builder = new StringBuilder();
            builder.append(this.numSubSamples).append(" subsamples, key [");
            String hexdigits = "0123456789abcdef";
            for (i = 0; i < this.key.length; i++) {
                builder.append(hexdigits.charAt((this.key[i] & 240) >> 4));
                builder.append(hexdigits.charAt(this.key[i] & 15));
            }
            builder.append("], iv [");
            for (i = 0; i < this.key.length; i++) {
                builder.append(hexdigits.charAt((this.iv[i] & 240) >> 4));
                builder.append(hexdigits.charAt(this.iv[i] & 15));
            }
            builder.append("], clear ");
            builder.append(Arrays.toString(this.numBytesOfClearData));
            builder.append(", encrypted ");
            builder.append(Arrays.toString(this.numBytesOfEncryptedData));
            return builder.toString();
        }
    }

    private class EventHandler extends Handler {
        private MediaCodec mCodec;

        public EventHandler(MediaCodec codec, Looper looper) {
            super(looper);
            this.mCodec = codec;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    handleCallback(msg);
                    return;
                case 2:
                    MediaCodec.this.mCallback = (Callback) msg.obj;
                    return;
                case 3:
                    synchronized (MediaCodec.this.mListenerLock) {
                        Map<String, Object> map = msg.obj;
                        int i = 0;
                        while (true) {
                            Object mediaTimeUs = map.get(i + "-media-time-us");
                            Object systemNano = map.get(i + "-system-nano");
                            if (!(mediaTimeUs == null || systemNano == null)) {
                                if (MediaCodec.this.mOnFrameRenderedListener != null) {
                                    MediaCodec.this.mOnFrameRenderedListener.onFrameRendered(this.mCodec, ((Long) mediaTimeUs).longValue(), ((Long) systemNano).longValue());
                                    i++;
                                }
                            }
                        }
                    }
                    return;
                default:
                    return;
            }
        }

        private void handleCallback(Message msg) {
            if (MediaCodec.this.mCallback != null) {
                int index;
                switch (msg.arg1) {
                    case 1:
                        index = msg.arg2;
                        synchronized (MediaCodec.this.mBufferLock) {
                            MediaCodec.this.validateInputByteBuffer(MediaCodec.this.mCachedInputBuffers, index);
                        }
                        MediaCodec.this.mCallback.onInputBufferAvailable(this.mCodec, index);
                        break;
                    case 2:
                        index = msg.arg2;
                        BufferInfo info = msg.obj;
                        synchronized (MediaCodec.this.mBufferLock) {
                            MediaCodec.this.validateOutputByteBuffer(MediaCodec.this.mCachedOutputBuffers, index, info);
                        }
                        MediaCodec.this.mCallback.onOutputBufferAvailable(this.mCodec, index, info);
                        break;
                    case 3:
                        MediaCodec.this.mCallback.onError(this.mCodec, (CodecException) msg.obj);
                        break;
                    case 4:
                        MediaCodec.this.mCallback.onOutputFormatChanged(this.mCodec, new MediaFormat((Map) msg.obj));
                        break;
                }
            }
        }
    }

    public static class MediaImage extends Image {
        private static final int TYPE_YUV = 1;
        private final ByteBuffer mBuffer;
        private final int mFormat = 35;
        private final int mHeight;
        private final ByteBuffer mInfo;
        private final boolean mIsReadOnly;
        private final Plane[] mPlanes;
        private long mTimestamp;
        private final int mWidth;
        private final int mXOffset;
        private final int mYOffset;

        private class MediaPlane extends Plane {
            private final int mColInc;
            private final ByteBuffer mData;
            private final int mRowInc;

            public MediaPlane(ByteBuffer buffer, int rowInc, int colInc) {
                this.mData = buffer;
                this.mRowInc = rowInc;
                this.mColInc = colInc;
            }

            public int getRowStride() {
                MediaImage.this.throwISEIfImageIsInvalid();
                return this.mRowInc;
            }

            public int getPixelStride() {
                MediaImage.this.throwISEIfImageIsInvalid();
                return this.mColInc;
            }

            public ByteBuffer getBuffer() {
                MediaImage.this.throwISEIfImageIsInvalid();
                return this.mData;
            }
        }

        public int getFormat() {
            throwISEIfImageIsInvalid();
            return this.mFormat;
        }

        public int getHeight() {
            throwISEIfImageIsInvalid();
            return this.mHeight;
        }

        public int getWidth() {
            throwISEIfImageIsInvalid();
            return this.mWidth;
        }

        public long getTimestamp() {
            throwISEIfImageIsInvalid();
            return this.mTimestamp;
        }

        public Plane[] getPlanes() {
            throwISEIfImageIsInvalid();
            return (Plane[]) Arrays.copyOf(this.mPlanes, this.mPlanes.length);
        }

        public void close() {
            if (this.mIsImageValid) {
                NioUtils.freeDirectBuffer(this.mBuffer);
                this.mIsImageValid = false;
            }
        }

        public void setCropRect(Rect cropRect) {
            if (this.mIsReadOnly) {
                throw new ReadOnlyBufferException();
            }
            super.setCropRect(cropRect);
        }

        public MediaImage(ByteBuffer buffer, ByteBuffer info, boolean readOnly, long timestamp, int xOffset, int yOffset, Rect cropRect) {
            this.mTimestamp = timestamp;
            this.mIsImageValid = true;
            this.mIsReadOnly = buffer.isReadOnly();
            this.mBuffer = buffer.duplicate();
            this.mXOffset = xOffset;
            this.mYOffset = yOffset;
            this.mInfo = info;
            if (info.remaining() == 104) {
                int type = info.getInt();
                if (type != 1) {
                    throw new UnsupportedOperationException("unsupported type: " + type);
                }
                int numPlanes = info.getInt();
                if (numPlanes != 3) {
                    throw new RuntimeException("unexpected number of planes: " + numPlanes);
                }
                this.mWidth = info.getInt();
                this.mHeight = info.getInt();
                if (this.mWidth < 1 || this.mHeight < 1) {
                    throw new UnsupportedOperationException("unsupported size: " + this.mWidth + "x" + this.mHeight);
                }
                int bitDepth = info.getInt();
                if (bitDepth != 8) {
                    throw new UnsupportedOperationException("unsupported bit depth: " + bitDepth);
                }
                int bitDepthAllocated = info.getInt();
                if (bitDepthAllocated != 8) {
                    throw new UnsupportedOperationException("unsupported allocated bit depth: " + bitDepthAllocated);
                }
                this.mPlanes = new MediaPlane[numPlanes];
                int ix = 0;
                while (ix < numPlanes) {
                    int planeOffset = info.getInt();
                    int colInc = info.getInt();
                    int rowInc = info.getInt();
                    int horiz = info.getInt();
                    int vert = info.getInt();
                    if (horiz == vert) {
                        if (horiz == (ix == 0 ? 1 : 2)) {
                            if (colInc < 1 || rowInc < 1) {
                                throw new UnsupportedOperationException("unexpected strides: " + colInc + " pixel, " + rowInc + " row on plane " + ix);
                            }
                            buffer.clear();
                            buffer.position(((this.mBuffer.position() + planeOffset) + ((xOffset / horiz) * colInc)) + ((yOffset / vert) * rowInc));
                            buffer.limit(((buffer.position() + Utils.divUp(bitDepth, 8)) + (((this.mHeight / vert) - 1) * rowInc)) + (((this.mWidth / horiz) - 1) * colInc));
                            this.mPlanes[ix] = new MediaPlane(buffer.slice(), rowInc, colInc);
                            ix++;
                        }
                    }
                    throw new UnsupportedOperationException("unexpected subsampling: " + horiz + "x" + vert + " on plane " + ix);
                }
                if (cropRect == null) {
                    Rect rect = new Rect(0, 0, this.mWidth, this.mHeight);
                }
                cropRect.offset(-xOffset, -yOffset);
                super.setCropRect(cropRect);
                return;
            }
            throw new UnsupportedOperationException("unsupported info length: " + info.remaining());
        }
    }

    public static final class MetricsConstants {
        public static final String CODEC = "android.media.mediacodec.codec";
        public static final String ENCODER = "android.media.mediacodec.encoder";
        public static final String HEIGHT = "android.media.mediacodec.height";
        public static final String MIME_TYPE = "android.media.mediacodec.mime";
        public static final String MODE = "android.media.mediacodec.mode";
        public static final String MODE_AUDIO = "audio";
        public static final String MODE_VIDEO = "video";
        public static final String ROTATION = "android.media.mediacodec.rotation";
        public static final String SECURE = "android.media.mediacodec.secure";
        public static final String WIDTH = "android.media.mediacodec.width";

        private MetricsConstants() {
        }
    }

    public interface OnFrameRenderedListener {
        void onFrameRendered(MediaCodec mediaCodec, long j, long j2);
    }

    static class PersistentSurface extends Surface {
        private long mPersistentObject;

        PersistentSurface() {
        }

        public void release() {
            MediaCodec.native_releasePersistentInputSurface(this);
            super.release();
        }
    }

    private final native ByteBuffer getBuffer(boolean z, int i);

    private final native ByteBuffer[] getBuffers(boolean z);

    private final native Map<String, Object> getFormatNative(boolean z);

    private final native Image getImage(boolean z, int i);

    private final native Map<String, Object> getOutputFormatNative(int i);

    private final native void native_configure(String[] strArr, Object[] objArr, Surface surface, MediaCrypto mediaCrypto, IBinder iBinder, int i);

    private static final native PersistentSurface native_createPersistentInputSurface();

    private final native int native_dequeueInputBuffer(long j);

    private final native int native_dequeueOutputBuffer(BufferInfo bufferInfo, long j);

    private native void native_enableOnFrameRenderedListener(boolean z);

    private final native void native_finalize();

    private final native void native_flush();

    private native PersistableBundle native_getMetrics();

    private static final native void native_init();

    private final native void native_queueInputBuffer(int i, int i2, int i3, long j, int i4) throws CryptoException;

    private final native void native_queueSecureInputBuffer(int i, int i2, CryptoInfo cryptoInfo, long j, int i3) throws CryptoException;

    private final native void native_release();

    private static final native void native_releasePersistentInputSurface(Surface surface);

    private final native void native_reset();

    private final native void native_setCallback(Callback callback);

    private final native void native_setInputSurface(Surface surface);

    private native void native_setSurface(Surface surface);

    private final native void native_setup(String str, boolean z, boolean z2);

    private final native void native_start();

    private final native void native_stop();

    private final native void releaseOutputBuffer(int i, boolean z, boolean z2, long j);

    private final native void setParameters(String[] strArr, Object[] objArr);

    public final native Surface createInputSurface();

    public final native String getName();

    public final native void setVideoScalingMode(int i);

    public final native void signalEndOfInputStream();

    public static MediaCodec createDecoderByType(String type) throws IOException {
        return new MediaCodec(type, true, false);
    }

    public static MediaCodec createEncoderByType(String type) throws IOException {
        return new MediaCodec(type, true, true);
    }

    public static MediaCodec createByCodecName(String name) throws IOException {
        return new MediaCodec(name, false, false);
    }

    private MediaCodec(String name, boolean nameIsType, boolean encoder) {
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            looper = Looper.getMainLooper();
            if (looper != null) {
                this.mEventHandler = new EventHandler(this, looper);
            } else {
                this.mEventHandler = null;
            }
        }
        this.mCallbackHandler = this.mEventHandler;
        this.mOnFrameRenderedHandler = this.mEventHandler;
        this.mBufferLock = new Object();
        native_setup(name, nameIsType, encoder);
    }

    protected void finalize() {
        native_finalize();
    }

    public final void reset() {
        freeAllTrackedBuffers();
        native_reset();
    }

    public final void release() {
        if (ViewRootImpl.sSLBSwitch) {
            HwFrameworkFactory.getHwNsdImpl().setPlayingVideoSLB(false);
            Log.i("MediaCodec", "APS: SLB: MediaCodec is working  @ release");
        }
        freeAllTrackedBuffers();
        native_release();
    }

    public void configure(MediaFormat format, Surface surface, MediaCrypto crypto, int flags) {
        configure(format, surface, crypto, null, flags);
    }

    public void configure(MediaFormat format, Surface surface, int flags, MediaDescrambler descrambler) {
        IBinder binder;
        if (descrambler != null) {
            binder = descrambler.getBinder();
        } else {
            binder = null;
        }
        configure(format, surface, null, binder, flags);
    }

    private void configure(MediaFormat format, Surface surface, MediaCrypto crypto, IBinder descramblerBinder, int flags) {
        if (crypto == null || descramblerBinder == null) {
            String[] keys = null;
            Object[] values = null;
            if (format != null) {
                Map<String, Object> formatMap = format.getMap();
                keys = new String[formatMap.size()];
                values = new Object[formatMap.size()];
                int i = 0;
                for (Entry<String, Object> entry : formatMap.entrySet()) {
                    if (((String) entry.getKey()).equals(MediaFormat.KEY_AUDIO_SESSION_ID)) {
                        try {
                            int sessionId = ((Integer) entry.getValue()).intValue();
                            keys[i] = "audio-hw-sync";
                            values[i] = Integer.valueOf(AudioSystem.getAudioHwSyncForSession(sessionId));
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Wrong Session ID Parameter!");
                        }
                    }
                    keys[i] = (String) entry.getKey();
                    values[i] = entry.getValue();
                    i++;
                }
            }
            this.mHasSurface = surface != null;
            native_configure(keys, values, surface, crypto, descramblerBinder, flags);
            return;
        }
        throw new IllegalArgumentException("Can't use crypto and descrambler together!");
    }

    public void setOutputSurface(Surface surface) {
        if (this.mHasSurface) {
            native_setSurface(surface);
            return;
        }
        throw new IllegalStateException("codec was not configured for an output surface");
    }

    public static Surface createPersistentInputSurface() {
        return native_createPersistentInputSurface();
    }

    public void setInputSurface(Surface surface) {
        if (surface instanceof PersistentSurface) {
            native_setInputSurface(surface);
            return;
        }
        throw new IllegalArgumentException("not a PersistentSurface");
    }

    public final void start() {
        if (ViewRootImpl.sSLBSwitch) {
            HwFrameworkFactory.getHwNsdImpl().setPlayingVideoSLB(true);
            Log.i("MediaCodec", "APS: SLB: MediaCodec is working @ start");
        }
        native_start();
        synchronized (this.mBufferLock) {
            cacheBuffers(true);
            cacheBuffers(false);
        }
    }

    public final void stop() {
        native_stop();
        freeAllTrackedBuffers();
        synchronized (this.mListenerLock) {
            if (this.mCallbackHandler != null) {
                this.mCallbackHandler.removeMessages(2);
                this.mCallbackHandler.removeMessages(1);
            }
            if (this.mOnFrameRenderedHandler != null) {
                this.mOnFrameRenderedHandler.removeMessages(3);
            }
        }
    }

    public final void flush() {
        synchronized (this.mBufferLock) {
            invalidateByteBuffers(this.mCachedInputBuffers);
            invalidateByteBuffers(this.mCachedOutputBuffers);
            this.mDequeuedInputBuffers.clear();
            this.mDequeuedOutputBuffers.clear();
        }
        native_flush();
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0015 A:{ExcHandler: android.media.MediaCodec.CryptoException (r0_0 'e' java.lang.RuntimeException), Splitter: B:5:0x000e} */
    /* JADX WARNING: Missing block: B:11:0x0015, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:12:0x0016, code:
            revalidateByteBuffer(r4.mCachedInputBuffers, r5);
     */
    /* JADX WARNING: Missing block: B:13:0x001b, code:
            throw r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags) throws CryptoException {
        synchronized (this.mBufferLock) {
            invalidateByteBuffer(this.mCachedInputBuffers, index);
            this.mDequeuedInputBuffers.remove(index);
        }
        try {
            native_queueInputBuffer(index, offset, size, presentationTimeUs, flags);
        } catch (RuntimeException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0015 A:{ExcHandler: android.media.MediaCodec.CryptoException (r0_0 'e' java.lang.RuntimeException), Splitter: B:5:0x000e} */
    /* JADX WARNING: Missing block: B:11:0x0015, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:12:0x0016, code:
            revalidateByteBuffer(r4.mCachedInputBuffers, r5);
     */
    /* JADX WARNING: Missing block: B:13:0x001b, code:
            throw r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void queueSecureInputBuffer(int index, int offset, CryptoInfo info, long presentationTimeUs, int flags) throws CryptoException {
        synchronized (this.mBufferLock) {
            invalidateByteBuffer(this.mCachedInputBuffers, index);
            this.mDequeuedInputBuffers.remove(index);
        }
        try {
            native_queueSecureInputBuffer(index, offset, info, presentationTimeUs, flags);
        } catch (RuntimeException e) {
        }
    }

    public final int dequeueInputBuffer(long timeoutUs) {
        int res = native_dequeueInputBuffer(timeoutUs);
        if (res >= 0) {
            synchronized (this.mBufferLock) {
                validateInputByteBuffer(this.mCachedInputBuffers, res);
            }
        }
        return res;
    }

    public final int dequeueOutputBuffer(BufferInfo info, long timeoutUs) {
        int res = native_dequeueOutputBuffer(info, timeoutUs);
        synchronized (this.mBufferLock) {
            if (res == -3) {
                cacheBuffers(false);
            } else if (res >= 0) {
                validateOutputByteBuffer(this.mCachedOutputBuffers, res, info);
                if (this.mHasSurface) {
                    this.mDequeuedOutputInfos.put(Integer.valueOf(res), info.dup());
                }
            }
        }
        return res;
    }

    public final void releaseOutputBuffer(int index, boolean render) {
        synchronized (this.mBufferLock) {
            invalidateByteBuffer(this.mCachedOutputBuffers, index);
            this.mDequeuedOutputBuffers.remove(index);
            if (this.mHasSurface) {
                BufferInfo info = (BufferInfo) this.mDequeuedOutputInfos.remove(Integer.valueOf(index));
            }
        }
        releaseOutputBuffer(index, render, false, 0);
    }

    public final void releaseOutputBuffer(int index, long renderTimestampNs) {
        synchronized (this.mBufferLock) {
            invalidateByteBuffer(this.mCachedOutputBuffers, index);
            this.mDequeuedOutputBuffers.remove(index);
            if (this.mHasSurface) {
                BufferInfo info = (BufferInfo) this.mDequeuedOutputInfos.remove(Integer.valueOf(index));
            }
        }
        releaseOutputBuffer(index, true, true, renderTimestampNs);
    }

    public final MediaFormat getOutputFormat() {
        return new MediaFormat(getFormatNative(false));
    }

    public final MediaFormat getInputFormat() {
        return new MediaFormat(getFormatNative(true));
    }

    public final MediaFormat getOutputFormat(int index) {
        return new MediaFormat(getOutputFormatNative(index));
    }

    private final void invalidateByteBuffer(ByteBuffer[] buffers, int index) {
        if (buffers != null && index >= 0 && index < buffers.length) {
            ByteBuffer buffer = buffers[index];
            if (buffer != null) {
                buffer.setAccessible(false);
            }
        }
    }

    private final void validateInputByteBuffer(ByteBuffer[] buffers, int index) {
        if (buffers != null && index >= 0 && index < buffers.length) {
            ByteBuffer buffer = buffers[index];
            if (buffer != null) {
                buffer.setAccessible(true);
                buffer.clear();
            }
        }
    }

    private final void revalidateByteBuffer(ByteBuffer[] buffers, int index) {
        synchronized (this.mBufferLock) {
            if (buffers != null && index >= 0) {
                if (index < buffers.length) {
                    ByteBuffer buffer = buffers[index];
                    if (buffer != null) {
                        buffer.setAccessible(true);
                    }
                }
            }
        }
    }

    private final void validateOutputByteBuffer(ByteBuffer[] buffers, int index, BufferInfo info) {
        if (buffers != null && index >= 0 && index < buffers.length) {
            ByteBuffer buffer = buffers[index];
            if (buffer != null) {
                buffer.setAccessible(true);
                buffer.limit(info.offset + info.size).position(info.offset);
            }
        }
    }

    private final void invalidateByteBuffers(ByteBuffer[] buffers) {
        if (buffers != null) {
            for (ByteBuffer buffer : buffers) {
                if (buffer != null) {
                    buffer.setAccessible(false);
                }
            }
        }
    }

    private final void freeByteBuffer(ByteBuffer buffer) {
        if (buffer != null) {
            NioUtils.freeDirectBuffer(buffer);
        }
    }

    private final void freeByteBuffers(ByteBuffer[] buffers) {
        if (buffers != null) {
            for (ByteBuffer buffer : buffers) {
                freeByteBuffer(buffer);
            }
        }
    }

    private final void freeAllTrackedBuffers() {
        synchronized (this.mBufferLock) {
            freeByteBuffers(this.mCachedInputBuffers);
            freeByteBuffers(this.mCachedOutputBuffers);
            this.mCachedInputBuffers = null;
            this.mCachedOutputBuffers = null;
            this.mDequeuedInputBuffers.clear();
            this.mDequeuedOutputBuffers.clear();
        }
    }

    private final void cacheBuffers(boolean input) {
        ByteBuffer[] byteBufferArr = null;
        try {
            byteBufferArr = getBuffers(input);
            invalidateByteBuffers(byteBufferArr);
        } catch (IllegalStateException e) {
        }
        if (input) {
            this.mCachedInputBuffers = byteBufferArr;
        } else {
            this.mCachedOutputBuffers = byteBufferArr;
        }
    }

    public ByteBuffer[] getInputBuffers() {
        if (this.mCachedInputBuffers != null) {
            return this.mCachedInputBuffers;
        }
        throw new IllegalStateException();
    }

    public ByteBuffer[] getOutputBuffers() {
        if (this.mCachedOutputBuffers != null) {
            return this.mCachedOutputBuffers;
        }
        throw new IllegalStateException();
    }

    public ByteBuffer getInputBuffer(int index) {
        ByteBuffer newBuffer = getBuffer(true, index);
        synchronized (this.mBufferLock) {
            invalidateByteBuffer(this.mCachedInputBuffers, index);
            this.mDequeuedInputBuffers.put(index, newBuffer);
        }
        return newBuffer;
    }

    public Image getInputImage(int index) {
        Image newImage = getImage(true, index);
        synchronized (this.mBufferLock) {
            invalidateByteBuffer(this.mCachedInputBuffers, index);
            this.mDequeuedInputBuffers.put(index, newImage);
        }
        return newImage;
    }

    public ByteBuffer getOutputBuffer(int index) {
        ByteBuffer newBuffer = getBuffer(false, index);
        synchronized (this.mBufferLock) {
            invalidateByteBuffer(this.mCachedOutputBuffers, index);
            this.mDequeuedOutputBuffers.put(index, newBuffer);
        }
        return newBuffer;
    }

    public Image getOutputImage(int index) {
        Image newImage = getImage(false, index);
        synchronized (this.mBufferLock) {
            invalidateByteBuffer(this.mCachedOutputBuffers, index);
            this.mDequeuedOutputBuffers.put(index, newImage);
        }
        return newImage;
    }

    public PersistableBundle getMetrics() {
        return native_getMetrics();
    }

    public final void setParameters(Bundle params) {
        if (params != null) {
            String[] keys = new String[params.size()];
            Object[] values = new Object[params.size()];
            int i = 0;
            for (String key : params.keySet()) {
                keys[i] = key;
                values[i] = params.get(key);
                i++;
            }
            setParameters(keys, values);
        }
    }

    public void setCallback(Callback cb, Handler handler) {
        if (cb != null) {
            synchronized (this.mListenerLock) {
                EventHandler newHandler = getEventHandlerOn(handler, this.mCallbackHandler);
                if (newHandler != this.mCallbackHandler) {
                    this.mCallbackHandler.removeMessages(2);
                    this.mCallbackHandler.removeMessages(1);
                    this.mCallbackHandler = newHandler;
                }
            }
        } else if (this.mCallbackHandler != null) {
            this.mCallbackHandler.removeMessages(2);
            this.mCallbackHandler.removeMessages(1);
        }
        if (this.mCallbackHandler != null) {
            this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(2, 0, 0, cb));
            native_setCallback(cb);
        }
    }

    public void setCallback(Callback cb) {
        setCallback(cb, null);
    }

    public void setOnFrameRenderedListener(OnFrameRenderedListener listener, Handler handler) {
        synchronized (this.mListenerLock) {
            this.mOnFrameRenderedListener = listener;
            if (listener != null) {
                EventHandler newHandler = getEventHandlerOn(handler, this.mOnFrameRenderedHandler);
                if (newHandler != this.mOnFrameRenderedHandler) {
                    this.mOnFrameRenderedHandler.removeMessages(3);
                }
                this.mOnFrameRenderedHandler = newHandler;
            } else if (this.mOnFrameRenderedHandler != null) {
                this.mOnFrameRenderedHandler.removeMessages(3);
            }
            native_enableOnFrameRenderedListener(listener != null);
        }
    }

    private EventHandler getEventHandlerOn(Handler handler, EventHandler lastHandler) {
        if (handler == null) {
            return this.mEventHandler;
        }
        Looper looper = handler.getLooper();
        if (lastHandler.getLooper() == looper) {
            return lastHandler;
        }
        return new EventHandler(this, looper);
    }

    private void postEventFromNative(int what, int arg1, int arg2, Object obj) {
        synchronized (this.mListenerLock) {
            EventHandler handler = this.mEventHandler;
            if (what == 1) {
                handler = this.mCallbackHandler;
            } else if (what == 3) {
                handler = this.mOnFrameRenderedHandler;
            }
            if (handler != null) {
                handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
            }
        }
    }

    public MediaCodecInfo getCodecInfo() {
        return MediaCodecList.getInfoFor(getName());
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }
}
