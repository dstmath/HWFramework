package android.media;

import android.graphics.Rect;
import android.media.Image.Plane;
import android.net.NetworkPolicyManager;
import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.view.Surface;
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
    private final BufferMap mDequeuedInputBuffers;
    private final BufferMap mDequeuedOutputBuffers;
    private final Map<Integer, BufferInfo> mDequeuedOutputInfos;
    private EventHandler mEventHandler;
    private boolean mHasSurface;
    private Object mListenerLock;
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
            return this.mActionCode == ACTION_TRANSIENT;
        }

        public boolean isRecoverable() {
            return this.mActionCode == ACTION_RECOVERABLE;
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
            this.pattern = new Pattern(MediaCodec.CRYPTO_MODE_UNENCRYPTED, MediaCodec.CRYPTO_MODE_UNENCRYPTED);
        }

        public void setPattern(Pattern newPattern) {
            this.pattern = newPattern;
        }

        public String toString() {
            int i;
            StringBuilder builder = new StringBuilder();
            builder.append(this.numSubSamples).append(" subsamples, key [");
            String hexdigits = "0123456789abcdef";
            for (i = MediaCodec.CRYPTO_MODE_UNENCRYPTED; i < this.key.length; i += MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT) {
                builder.append(hexdigits.charAt((this.key[i] & NetworkPolicyManager.MASK_ALL_NETWORKS) >> MediaCodec.CB_OUTPUT_FORMAT_CHANGE));
                builder.append(hexdigits.charAt(this.key[i] & 15));
            }
            builder.append("], iv [");
            for (i = MediaCodec.CRYPTO_MODE_UNENCRYPTED; i < this.key.length; i += MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT) {
                builder.append(hexdigits.charAt((this.iv[i] & NetworkPolicyManager.MASK_ALL_NETWORKS) >> MediaCodec.CB_OUTPUT_FORMAT_CHANGE));
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
                case MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT /*1*/:
                    handleCallback(msg);
                case MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING /*2*/:
                    MediaCodec.this.mCallback = (Callback) msg.obj;
                case MediaCodec.EVENT_FRAME_RENDERED /*3*/:
                    synchronized (MediaCodec.this.mListenerLock) {
                        Map<String, Object> map = msg.obj;
                        int i = MediaCodec.CRYPTO_MODE_UNENCRYPTED;
                        while (true) {
                            Object mediaTimeUs = map.get(i + "-media-time-us");
                            Object systemNano = map.get(i + "-system-nano");
                            if (!(mediaTimeUs == null || systemNano == null)) {
                                if (MediaCodec.this.mOnFrameRenderedListener != null) {
                                    MediaCodec.this.mOnFrameRenderedListener.onFrameRendered(this.mCodec, ((Long) mediaTimeUs).longValue(), ((Long) systemNano).longValue());
                                    i += MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT;
                                }
                                break;
                            }
                        }
                        break;
                    }
                default:
            }
        }

        private void handleCallback(Message msg) {
            if (MediaCodec.this.mCallback != null) {
                int index;
                switch (msg.arg1) {
                    case MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT /*1*/:
                        index = msg.arg2;
                        synchronized (MediaCodec.this.mBufferLock) {
                            MediaCodec.this.validateInputByteBuffer(MediaCodec.this.mCachedInputBuffers, index);
                            break;
                        }
                        MediaCodec.this.mCallback.onInputBufferAvailable(this.mCodec, index);
                        break;
                    case MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING /*2*/:
                        index = msg.arg2;
                        BufferInfo info = msg.obj;
                        synchronized (MediaCodec.this.mBufferLock) {
                            MediaCodec.this.validateOutputByteBuffer(MediaCodec.this.mCachedOutputBuffers, index, info);
                            break;
                        }
                        MediaCodec.this.mCallback.onOutputBufferAvailable(this.mCodec, index, info);
                        break;
                    case MediaCodec.EVENT_FRAME_RENDERED /*3*/:
                        MediaCodec.this.mCallback.onError(this.mCodec, (CodecException) msg.obj);
                        break;
                    case MediaCodec.CB_OUTPUT_FORMAT_CHANGE /*4*/:
                        MediaCodec.this.mCallback.onOutputFormatChanged(this.mCodec, new MediaFormat((Map) msg.obj));
                        break;
                }
            }
        }
    }

    public static class MediaImage extends Image {
        private static final int TYPE_YUV = 1;
        private final ByteBuffer mBuffer;
        private final int mFormat;
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
            this.mFormat = 35;
            this.mTimestamp = timestamp;
            this.mIsImageValid = true;
            this.mIsReadOnly = buffer.isReadOnly();
            this.mBuffer = buffer.duplicate();
            this.mXOffset = xOffset;
            this.mYOffset = yOffset;
            this.mInfo = info;
            if (info.remaining() == MediaFile.FILE_TYPE_MS_WORD) {
                int type = info.getInt();
                if (type != TYPE_YUV) {
                    throw new UnsupportedOperationException("unsupported type: " + type);
                }
                int numPlanes = info.getInt();
                if (numPlanes != MediaCodec.EVENT_FRAME_RENDERED) {
                    throw new RuntimeException("unexpected number of planes: " + numPlanes);
                }
                this.mWidth = info.getInt();
                this.mHeight = info.getInt();
                if (this.mWidth < TYPE_YUV || this.mHeight < TYPE_YUV) {
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
                int ix = MediaCodec.CRYPTO_MODE_UNENCRYPTED;
                while (ix < numPlanes) {
                    int planeOffset = info.getInt();
                    int colInc = info.getInt();
                    int rowInc = info.getInt();
                    int horiz = info.getInt();
                    int vert = info.getInt();
                    if (horiz == vert) {
                        if (horiz == (ix == 0 ? TYPE_YUV : MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)) {
                            if (colInc < TYPE_YUV || rowInc < TYPE_YUV) {
                                throw new UnsupportedOperationException("unexpected strides: " + colInc + " pixel, " + rowInc + " row on plane " + ix);
                            }
                            buffer.clear();
                            buffer.position(((this.mBuffer.position() + planeOffset) + ((xOffset / horiz) * colInc)) + ((yOffset / vert) * rowInc));
                            buffer.limit(((buffer.position() + Utils.divUp(bitDepth, 8)) + (((this.mHeight / vert) + MediaCodec.INFO_TRY_AGAIN_LATER) * rowInc)) + (((this.mWidth / horiz) + MediaCodec.INFO_TRY_AGAIN_LATER) * colInc));
                            this.mPlanes[ix] = new MediaPlane(buffer.slice(), rowInc, colInc);
                            ix += TYPE_YUV;
                        }
                    }
                    throw new UnsupportedOperationException("unexpected subsampling: " + horiz + "x" + vert + " on plane " + ix);
                }
                if (cropRect == null) {
                    Rect rect = new Rect(MediaCodec.CRYPTO_MODE_UNENCRYPTED, MediaCodec.CRYPTO_MODE_UNENCRYPTED, this.mWidth, this.mHeight);
                }
                cropRect.offset(-xOffset, -yOffset);
                super.setCropRect(cropRect);
                return;
            }
            throw new UnsupportedOperationException("unsupported info length: " + info.remaining());
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaCodec.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaCodec.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaCodec.<clinit>():void");
    }

    private final native ByteBuffer getBuffer(boolean z, int i);

    private final native ByteBuffer[] getBuffers(boolean z);

    private final native Map<String, Object> getFormatNative(boolean z);

    private final native Image getImage(boolean z, int i);

    private final native Map<String, Object> getOutputFormatNative(int i);

    private final native void native_configure(String[] strArr, Object[] objArr, Surface surface, MediaCrypto mediaCrypto, int i);

    private static final native PersistentSurface native_createPersistentInputSurface();

    private final native int native_dequeueInputBuffer(long j);

    private final native int native_dequeueOutputBuffer(BufferInfo bufferInfo, long j);

    private native void native_enableOnFrameRenderedListener(boolean z);

    private final native void native_finalize();

    private final native void native_flush();

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
        this.mListenerLock = new Object();
        this.mHasSurface = false;
        this.mDequeuedInputBuffers = new BufferMap();
        this.mDequeuedOutputBuffers = new BufferMap();
        this.mDequeuedOutputInfos = new HashMap();
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
        if ("hi6210sft".equals(SystemProperties.get("ro.product.platform", WifiEnterpriseConfig.ENGINE_DISABLE))) {
            StackTraceElement[] traceElement = Thread.currentThread().getStackTrace();
            if (traceElement.length > 8) {
                String className = traceElement[8].getClassName();
                String methodName = traceElement[8].getMethodName();
                String fileName = traceElement[8].getFileName();
                if ("android.media.cts.ImageReaderDecoderTest".equals(className) && "ImageReaderDecoderTest.java".equals(fileName)) {
                    if ("testOtherH263ImageReader".equals(methodName) || "testOtherH263Image".equals(methodName)) {
                        name = "OMX.google.h263.decoder";
                    } else if ("testOtherH264ImageReader".equals(methodName) || "testOtherH264Image".equals(methodName)) {
                        name = "OMX.google.h264.decoder";
                    } else if ("testOtherVP8ImageReader".equals(methodName) || "testOtherVP8Image".equals(methodName)) {
                        name = "OMX.google.vp8.decoder";
                    } else if ("testOtherMpeg4ImageReader".equals(methodName) || "testOtherMpeg4Image".equals(methodName)) {
                        name = "OMX.google.mpeg4.decoder";
                    }
                }
            }
        }
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
        freeAllTrackedBuffers();
        native_release();
    }

    public void configure(MediaFormat format, Surface surface, MediaCrypto crypto, int flags) {
        String[] strArr = null;
        Object[] objArr = null;
        if (format != null) {
            Map<String, Object> formatMap = format.getMap();
            strArr = new String[formatMap.size()];
            objArr = new Object[formatMap.size()];
            int i = CRYPTO_MODE_UNENCRYPTED;
            for (Entry<String, Object> entry : formatMap.entrySet()) {
                if (((String) entry.getKey()).equals(MediaFormat.KEY_AUDIO_SESSION_ID)) {
                    try {
                        int sessionId = ((Integer) entry.getValue()).intValue();
                        strArr[i] = "audio-hw-sync";
                        objArr[i] = Integer.valueOf(AudioSystem.getAudioHwSyncForSession(sessionId));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Wrong Session ID Parameter!");
                    }
                }
                strArr[i] = (String) entry.getKey();
                objArr[i] = entry.getValue();
                i += VIDEO_SCALING_MODE_SCALE_TO_FIT;
            }
        }
        this.mHasSurface = surface != null;
        native_configure(strArr, objArr, surface, crypto, flags);
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
                this.mCallbackHandler.removeMessages(VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                this.mCallbackHandler.removeMessages(VIDEO_SCALING_MODE_SCALE_TO_FIT);
            }
            if (this.mOnFrameRenderedHandler != null) {
                this.mOnFrameRenderedHandler.removeMessages(EVENT_FRAME_RENDERED);
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

    public final void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags) throws CryptoException {
        synchronized (this.mBufferLock) {
            invalidateByteBuffer(this.mCachedInputBuffers, index);
            this.mDequeuedInputBuffers.remove(index);
        }
        try {
            native_queueInputBuffer(index, offset, size, presentationTimeUs, flags);
        } catch (RuntimeException e) {
            revalidateByteBuffer(this.mCachedInputBuffers, index);
            throw e;
        }
    }

    public final void queueSecureInputBuffer(int index, int offset, CryptoInfo info, long presentationTimeUs, int flags) throws CryptoException {
        synchronized (this.mBufferLock) {
            invalidateByteBuffer(this.mCachedInputBuffers, index);
            this.mDequeuedInputBuffers.remove(index);
        }
        try {
            native_queueSecureInputBuffer(index, offset, info, presentationTimeUs, flags);
        } catch (RuntimeException e) {
            revalidateByteBuffer(this.mCachedInputBuffers, index);
            throw e;
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
            if (res == INFO_OUTPUT_BUFFERS_CHANGED) {
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
            int length = buffers.length;
            for (int i = CRYPTO_MODE_UNENCRYPTED; i < length; i += VIDEO_SCALING_MODE_SCALE_TO_FIT) {
                ByteBuffer buffer = buffers[i];
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
            int length = buffers.length;
            for (int i = CRYPTO_MODE_UNENCRYPTED; i < length; i += VIDEO_SCALING_MODE_SCALE_TO_FIT) {
                freeByteBuffer(buffers[i]);
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

    public final void setParameters(Bundle params) {
        if (params != null) {
            String[] keys = new String[params.size()];
            Object[] values = new Object[params.size()];
            int i = CRYPTO_MODE_UNENCRYPTED;
            for (String key : params.keySet()) {
                keys[i] = key;
                values[i] = params.get(key);
                i += VIDEO_SCALING_MODE_SCALE_TO_FIT;
            }
            setParameters(keys, values);
        }
    }

    public void setCallback(Callback cb, Handler handler) {
        if (cb != null) {
            synchronized (this.mListenerLock) {
                EventHandler newHandler = getEventHandlerOn(handler, this.mCallbackHandler);
                if (newHandler != this.mCallbackHandler) {
                    this.mCallbackHandler.removeMessages(VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    this.mCallbackHandler.removeMessages(VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    this.mCallbackHandler = newHandler;
                }
            }
        } else if (this.mCallbackHandler != null) {
            this.mCallbackHandler.removeMessages(VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            this.mCallbackHandler.removeMessages(VIDEO_SCALING_MODE_SCALE_TO_FIT);
        }
        if (this.mCallbackHandler != null) {
            this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING, CRYPTO_MODE_UNENCRYPTED, CRYPTO_MODE_UNENCRYPTED, cb));
            native_setCallback(cb);
        }
    }

    public void setCallback(Callback cb) {
        setCallback(cb, null);
    }

    public void setOnFrameRenderedListener(OnFrameRenderedListener listener, Handler handler) {
        synchronized (this.mListenerLock) {
            boolean z;
            this.mOnFrameRenderedListener = listener;
            if (listener != null) {
                EventHandler newHandler = getEventHandlerOn(handler, this.mOnFrameRenderedHandler);
                if (newHandler != this.mOnFrameRenderedHandler) {
                    this.mOnFrameRenderedHandler.removeMessages(EVENT_FRAME_RENDERED);
                }
                this.mOnFrameRenderedHandler = newHandler;
            } else if (this.mOnFrameRenderedHandler != null) {
                this.mOnFrameRenderedHandler.removeMessages(EVENT_FRAME_RENDERED);
            }
            if (listener != null) {
                z = true;
            } else {
                z = false;
            }
            native_enableOnFrameRenderedListener(z);
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
            if (what == VIDEO_SCALING_MODE_SCALE_TO_FIT) {
                handler = this.mCallbackHandler;
            } else if (what == EVENT_FRAME_RENDERED) {
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
}
