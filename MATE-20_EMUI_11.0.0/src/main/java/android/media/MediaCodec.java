package android.media;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.view.Surface;
import com.android.internal.midi.MidiConstants;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.NioUtils;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class MediaCodec {
    public static final int BUFFER_FLAG_CODEC_CONFIG = 2;
    public static final int BUFFER_FLAG_END_OF_STREAM = 4;
    public static final int BUFFER_FLAG_KEY_FRAME = 1;
    public static final int BUFFER_FLAG_MUXER_DATA = 16;
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
    public static final String PARAMETER_KEY_HDR10_PLUS_INFO = "hdr10-plus-info";
    public static final String PARAMETER_KEY_OFFSET_TIME = "time-offset-us";
    public static final String PARAMETER_KEY_REQUEST_SYNC_FRAME = "request-sync";
    public static final String PARAMETER_KEY_SUSPEND = "drop-input-frames";
    public static final String PARAMETER_KEY_SUSPEND_TIME = "drop-start-time-us";
    public static final String PARAMETER_KEY_VIDEO_BITRATE = "video-bitrate";
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;
    private final Object mBufferLock;
    private ByteBuffer[] mCachedInputBuffers;
    private ByteBuffer[] mCachedOutputBuffers;
    private Callback mCallback;
    private EventHandler mCallbackHandler;
    private MediaCodecInfo mCodecInfo;
    private final Object mCodecInfoLock = new Object();
    private MediaCrypto mCrypto;
    private final BufferMap mDequeuedInputBuffers;
    private final BufferMap mDequeuedOutputBuffers;
    private final Map<Integer, BufferInfo> mDequeuedOutputInfos;
    private EventHandler mEventHandler;
    private boolean mHasSurface = false;
    private final Object mListenerLock = new Object();
    private String mNameAtCreation;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private long mNativeContext;
    private final Lock mNativeContextLock;
    private EventHandler mOnFrameRenderedHandler;
    private OnFrameRenderedListener mOnFrameRenderedListener;

    @Retention(RetentionPolicy.SOURCE)
    public @interface BufferFlag {
    }

    public static abstract class Callback {
        public abstract void onError(MediaCodec mediaCodec, CodecException codecException);

        public abstract void onInputBufferAvailable(MediaCodec mediaCodec, int i);

        public abstract void onOutputBufferAvailable(MediaCodec mediaCodec, int i, BufferInfo bufferInfo);

        public abstract void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ConfigureFlag {
    }

    public interface OnFrameRenderedListener {
        void onFrameRendered(MediaCodec mediaCodec, long j, long j2);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface OutputBufferInfo {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface VideoScalingMode {
    }

    private final native ByteBuffer getBuffer(boolean z, int i);

    @UnsupportedAppUsage
    private final native ByteBuffer[] getBuffers(boolean z);

    private final native Map<String, Object> getFormatNative(boolean z);

    private final native Image getImage(boolean z, int i);

    private final native Map<String, Object> getOutputFormatNative(int i);

    private final native MediaCodecInfo getOwnCodecInfo();

    private final native void native_configure(String[] strArr, Object[] objArr, Surface surface, MediaCrypto mediaCrypto, IHwBinder iHwBinder, int i);

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

    /* access modifiers changed from: private */
    public static final native void native_releasePersistentInputSurface(Surface surface);

    private final native void native_reset();

    private native void native_setAudioPresentation(int i, int i2);

    private final native void native_setCallback(Callback callback);

    private final native void native_setInputSurface(Surface surface);

    private native void native_setSurface(Surface surface);

    private final native void native_setup(String str, boolean z, boolean z2);

    private final native void native_start();

    private final native void native_stop();

    @UnsupportedAppUsage
    private final native void releaseOutputBuffer(int i, boolean z, boolean z2, long j);

    @UnsupportedAppUsage
    private final native void setParameters(String[] strArr, Object[] objArr);

    public final native Surface createInputSurface();

    public final native String getCanonicalName();

    public final native void setVideoScalingMode(int i);

    public final native void signalEndOfInputStream();

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

    /* access modifiers changed from: private */
    public class EventHandler extends Handler {
        private MediaCodec mCodec;

        public EventHandler(MediaCodec codec, Looper looper) {
            super(looper);
            this.mCodec = codec;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                handleCallback(msg);
            } else if (i == 2) {
                MediaCodec.this.mCallback = (Callback) msg.obj;
            } else if (i == 3) {
                synchronized (MediaCodec.this.mListenerLock) {
                    Map<String, Object> map = (Map) msg.obj;
                    int i2 = 0;
                    while (true) {
                        Object mediaTimeUs = map.get(i2 + "-media-time-us");
                        Object systemNano = map.get(i2 + "-system-nano");
                        if (mediaTimeUs == null || systemNano == null) {
                            break;
                        } else if (MediaCodec.this.mOnFrameRenderedListener == null) {
                            break;
                        } else {
                            MediaCodec.this.mOnFrameRenderedListener.onFrameRendered(this.mCodec, ((Long) mediaTimeUs).longValue(), ((Long) systemNano).longValue());
                            i2++;
                        }
                    }
                }
            }
        }

        private void handleCallback(Message msg) {
            if (MediaCodec.this.mCallback != null) {
                int i = msg.arg1;
                if (i == 1) {
                    int index = msg.arg2;
                    synchronized (MediaCodec.this.mBufferLock) {
                        MediaCodec.this.validateInputByteBuffer(MediaCodec.this.mCachedInputBuffers, index);
                    }
                    MediaCodec.this.mCallback.onInputBufferAvailable(this.mCodec, index);
                } else if (i == 2) {
                    int index2 = msg.arg2;
                    BufferInfo info = (BufferInfo) msg.obj;
                    synchronized (MediaCodec.this.mBufferLock) {
                        MediaCodec.this.validateOutputByteBuffer(MediaCodec.this.mCachedOutputBuffers, index2, info);
                    }
                    MediaCodec.this.mCallback.onOutputBufferAvailable(this.mCodec, index2, info);
                } else if (i == 3) {
                    MediaCodec.this.mCallback.onError(this.mCodec, (CodecException) msg.obj);
                } else if (i == 4) {
                    MediaCodec.this.mCallback.onOutputFormatChanged(this.mCodec, new MediaFormat((Map) msg.obj));
                }
            }
        }
    }

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
        String str = null;
        this.mDequeuedInputBuffers = new BufferMap();
        this.mDequeuedOutputBuffers = new BufferMap();
        this.mDequeuedOutputInfos = new HashMap();
        this.mNativeContext = 0;
        this.mNativeContextLock = new ReentrantLock();
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            Looper looper2 = Looper.getMainLooper();
            if (looper2 != null) {
                this.mEventHandler = new EventHandler(this, looper2);
            } else {
                this.mEventHandler = null;
            }
        }
        EventHandler eventHandler = this.mEventHandler;
        this.mCallbackHandler = eventHandler;
        this.mOnFrameRenderedHandler = eventHandler;
        this.mBufferLock = new Object();
        this.mNameAtCreation = !nameIsType ? name : str;
        native_setup(name, nameIsType, encoder);
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        native_finalize();
        this.mCrypto = null;
    }

    public final void reset() {
        freeAllTrackedBuffers();
        native_reset();
        this.mCrypto = null;
    }

    public final void release() {
        freeAllTrackedBuffers();
        native_release();
        this.mCrypto = null;
    }

    public void configure(MediaFormat format, Surface surface, MediaCrypto crypto, int flags) {
        configure(format, surface, crypto, null, flags);
    }

    public void configure(MediaFormat format, Surface surface, int flags, MediaDescrambler descrambler) {
        configure(format, surface, null, descrambler != null ? descrambler.getBinder() : null, flags);
    }

    private void configure(MediaFormat format, Surface surface, MediaCrypto crypto, IHwBinder descramblerBinder, int flags) {
        Object[] values;
        if (crypto == null || descramblerBinder == null) {
            String[] keys = null;
            if (format != null) {
                Map<String, Object> formatMap = format.getMap();
                String[] keys2 = new String[formatMap.size()];
                Object[] values2 = new Object[formatMap.size()];
                int i = 0;
                for (Map.Entry<String, Object> entry : formatMap.entrySet()) {
                    if (entry.getKey().equals(MediaFormat.KEY_AUDIO_SESSION_ID)) {
                        try {
                            int sessionId = ((Integer) entry.getValue()).intValue();
                            keys2[i] = "audio-hw-sync";
                            values2[i] = Integer.valueOf(AudioSystem.getAudioHwSyncForSession(sessionId));
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Wrong Session ID Parameter!");
                        }
                    } else {
                        keys2[i] = entry.getKey();
                        values2[i] = entry.getValue();
                    }
                    i++;
                }
                values = values2;
                keys = keys2;
            } else {
                values = null;
            }
            this.mHasSurface = surface != null;
            this.mCrypto = crypto;
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

    static class PersistentSurface extends Surface {
        private long mPersistentObject;

        PersistentSurface() {
        }

        @Override // android.view.Surface
        public void release() {
            MediaCodec.native_releasePersistentInputSurface(this);
            super.release();
        }
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

    public static final class CodecException extends IllegalStateException {
        private static final int ACTION_RECOVERABLE = 2;
        private static final int ACTION_TRANSIENT = 1;
        public static final int ERROR_INSUFFICIENT_RESOURCE = 1100;
        public static final int ERROR_RECLAIMED = 1101;
        private final int mActionCode;
        private final String mDiagnosticInfo;
        private final int mErrorCode;

        @Retention(RetentionPolicy.SOURCE)
        public @interface ReasonCode {
        }

        @UnsupportedAppUsage
        CodecException(int errorCode, int actionCode, String detailMessage) {
            super(detailMessage);
            this.mErrorCode = errorCode;
            this.mActionCode = actionCode;
            String sign = errorCode < 0 ? "neg_" : "";
            this.mDiagnosticInfo = "android.media.MediaCodec.error_" + sign + Math.abs(errorCode);
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
        public static final int ERROR_FRAME_TOO_LARGE = 8;
        public static final int ERROR_INSUFFICIENT_OUTPUT_PROTECTION = 4;
        public static final int ERROR_INSUFFICIENT_SECURITY = 7;
        public static final int ERROR_KEY_EXPIRED = 2;
        public static final int ERROR_LOST_STATE = 9;
        public static final int ERROR_NO_KEY = 1;
        public static final int ERROR_RESOURCE_BUSY = 3;
        public static final int ERROR_SESSION_NOT_OPENED = 5;
        public static final int ERROR_UNSUPPORTED_OPERATION = 6;
        private int mErrorCode;

        @Retention(RetentionPolicy.SOURCE)
        public @interface CryptoErrorCode {
        }

        public CryptoException(int errorCode, String detailMessage) {
            super(detailMessage);
            this.mErrorCode = errorCode;
        }

        public int getErrorCode() {
            return this.mErrorCode;
        }
    }

    public final void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags) throws CryptoException {
        synchronized (this.mBufferLock) {
            invalidateByteBuffer(this.mCachedInputBuffers, index);
            this.mDequeuedInputBuffers.remove(index);
        }
        try {
            native_queueInputBuffer(index, offset, size, presentationTimeUs, flags);
        } catch (CryptoException | IllegalStateException e) {
            revalidateByteBuffer(this.mCachedInputBuffers, index);
            throw e;
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
        private final Pattern zeroPattern = new Pattern(0, 0);

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
            this.pattern = this.zeroPattern;
        }

        public void setPattern(Pattern newPattern) {
            this.pattern = newPattern;
        }

        private void setPattern(int blocksToEncrypt, int blocksToSkip) {
            this.pattern = new Pattern(blocksToEncrypt, blocksToSkip);
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(this.numSubSamples + " subsamples, key [");
            int i = 0;
            while (true) {
                byte[] bArr = this.key;
                if (i >= bArr.length) {
                    break;
                }
                builder.append("0123456789abcdef".charAt((bArr[i] & 240) >> 4));
                builder.append("0123456789abcdef".charAt(this.key[i] & MidiConstants.STATUS_CHANNEL_MASK));
                i++;
            }
            builder.append("], iv [");
            for (int i2 = 0; i2 < this.key.length; i2++) {
                builder.append("0123456789abcdef".charAt((this.iv[i2] & 240) >> 4));
                builder.append("0123456789abcdef".charAt(this.iv[i2] & MidiConstants.STATUS_CHANNEL_MASK));
            }
            builder.append("], clear ");
            builder.append(Arrays.toString(this.numBytesOfClearData));
            builder.append(", encrypted ");
            builder.append(Arrays.toString(this.numBytesOfEncryptedData));
            return builder.toString();
        }
    }

    public final void queueSecureInputBuffer(int index, int offset, CryptoInfo info, long presentationTimeUs, int flags) throws CryptoException {
        synchronized (this.mBufferLock) {
            invalidateByteBuffer(this.mCachedInputBuffers, index);
            this.mDequeuedInputBuffers.remove(index);
        }
        try {
            native_queueSecureInputBuffer(index, offset, info, presentationTimeUs, flags);
        } catch (CryptoException | IllegalStateException e) {
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
            if (res == -3) {
                try {
                    cacheBuffers(false);
                } catch (Throwable th) {
                    throw th;
                }
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
                this.mDequeuedOutputInfos.remove(Integer.valueOf(index));
            }
        }
        releaseOutputBuffer(index, render, false, 0);
    }

    public final void releaseOutputBuffer(int index, long renderTimestampNs) {
        synchronized (this.mBufferLock) {
            invalidateByteBuffer(this.mCachedOutputBuffers, index);
            this.mDequeuedOutputBuffers.remove(index);
            if (this.mHasSurface) {
                this.mDequeuedOutputInfos.remove(Integer.valueOf(index));
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

    /* access modifiers changed from: private */
    public static class BufferMap {
        private final Map<Integer, CodecBuffer> mMap;

        private BufferMap() {
            this.mMap = new HashMap();
        }

        /* access modifiers changed from: private */
        public static class CodecBuffer {
            private ByteBuffer mByteBuffer;
            private Image mImage;

            private CodecBuffer() {
            }

            public void free() {
                ByteBuffer byteBuffer = this.mByteBuffer;
                if (byteBuffer != null) {
                    NioUtils.freeDirectBuffer(byteBuffer);
                    this.mByteBuffer = null;
                }
                Image image = this.mImage;
                if (image != null) {
                    image.close();
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

        public void remove(int index) {
            CodecBuffer buffer = this.mMap.get(Integer.valueOf(index));
            if (buffer != null) {
                buffer.free();
                this.mMap.remove(Integer.valueOf(index));
            }
        }

        public void put(int index, ByteBuffer newBuffer) {
            CodecBuffer buffer = this.mMap.get(Integer.valueOf(index));
            if (buffer == null) {
                buffer = new CodecBuffer();
                this.mMap.put(Integer.valueOf(index), buffer);
            }
            buffer.setByteBuffer(newBuffer);
        }

        public void put(int index, Image newImage) {
            CodecBuffer buffer = this.mMap.get(Integer.valueOf(index));
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

    private final void invalidateByteBuffer(ByteBuffer[] buffers, int index) {
        ByteBuffer buffer;
        if (buffers != null && index >= 0 && index < buffers.length && (buffer = buffers[index]) != null) {
            buffer.setAccessible(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void validateInputByteBuffer(ByteBuffer[] buffers, int index) {
        ByteBuffer buffer;
        if (buffers != null && index >= 0 && index < buffers.length && (buffer = buffers[index]) != null) {
            buffer.setAccessible(true);
            buffer.clear();
        }
    }

    private final void revalidateByteBuffer(ByteBuffer[] buffers, int index) {
        ByteBuffer buffer;
        synchronized (this.mBufferLock) {
            if (buffers != null && index >= 0) {
                if (index < buffers.length && (buffer = buffers[index]) != null) {
                    buffer.setAccessible(true);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void validateOutputByteBuffer(ByteBuffer[] buffers, int index, BufferInfo info) {
        ByteBuffer buffer;
        if (buffers != null && index >= 0 && index < buffers.length && (buffer = buffers[index]) != null) {
            buffer.setAccessible(true);
            buffer.limit(info.offset + info.size).position(info.offset);
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
        ByteBuffer[] buffers = null;
        try {
            buffers = getBuffers(input);
            invalidateByteBuffers(buffers);
        } catch (IllegalStateException e) {
        }
        if (input) {
            this.mCachedInputBuffers = buffers;
        } else {
            this.mCachedOutputBuffers = buffers;
        }
    }

    public ByteBuffer[] getInputBuffers() {
        ByteBuffer[] byteBufferArr = this.mCachedInputBuffers;
        if (byteBufferArr != null) {
            return byteBufferArr;
        }
        throw new IllegalStateException();
    }

    public ByteBuffer[] getOutputBuffers() {
        ByteBuffer[] byteBufferArr = this.mCachedOutputBuffers;
        if (byteBufferArr != null) {
            return byteBufferArr;
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

    public void setAudioPresentation(AudioPresentation presentation) {
        if (presentation != null) {
            native_setAudioPresentation(presentation.getPresentationId(), presentation.getProgramId());
            return;
        }
        throw new NullPointerException("audio presentation is null");
    }

    public final String getName() {
        String canonicalName = getCanonicalName();
        String str = this.mNameAtCreation;
        return str != null ? str : canonicalName;
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
                Object value = params.get(key);
                if (value instanceof byte[]) {
                    values[i] = ByteBuffer.wrap((byte[]) value);
                } else {
                    values[i] = value;
                }
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
        } else {
            EventHandler eventHandler = this.mCallbackHandler;
            if (eventHandler != null) {
                eventHandler.removeMessages(2);
                this.mCallbackHandler.removeMessages(1);
            }
        }
        EventHandler eventHandler2 = this.mCallbackHandler;
        if (eventHandler2 != null) {
            this.mCallbackHandler.sendMessage(eventHandler2.obtainMessage(2, 0, 0, cb));
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
        MediaCodecInfo mediaCodecInfo;
        String name = getName();
        synchronized (this.mCodecInfoLock) {
            if (this.mCodecInfo == null) {
                this.mCodecInfo = getOwnCodecInfo();
                if (this.mCodecInfo == null) {
                    this.mCodecInfo = MediaCodecList.getInfoFor(name);
                }
            }
            mediaCodecInfo = this.mCodecInfo;
        }
        return mediaCodecInfo;
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    private final long lockAndGetContext() {
        this.mNativeContextLock.lock();
        return this.mNativeContext;
    }

    private final void setAndUnlockContext(long context) {
        this.mNativeContext = context;
        this.mNativeContextLock.unlock();
    }

    public static class MediaImage extends Image {
        private static final int TYPE_YUV = 1;
        private final ByteBuffer mBuffer;
        private final int mFormat = 35;
        private final int mHeight;
        private final ByteBuffer mInfo;
        private final boolean mIsReadOnly;
        private final Image.Plane[] mPlanes;
        private final int mScalingMode = 0;
        private long mTimestamp;
        private final int mTransform = 0;
        private final int mWidth;
        private final int mXOffset;
        private final int mYOffset;

        @Override // android.media.Image
        public int getFormat() {
            throwISEIfImageIsInvalid();
            return this.mFormat;
        }

        @Override // android.media.Image
        public int getHeight() {
            throwISEIfImageIsInvalid();
            return this.mHeight;
        }

        @Override // android.media.Image
        public int getWidth() {
            throwISEIfImageIsInvalid();
            return this.mWidth;
        }

        @Override // android.media.Image
        public int getTransform() {
            throwISEIfImageIsInvalid();
            return 0;
        }

        @Override // android.media.Image
        public int getScalingMode() {
            throwISEIfImageIsInvalid();
            return 0;
        }

        @Override // android.media.Image
        public long getTimestamp() {
            throwISEIfImageIsInvalid();
            return this.mTimestamp;
        }

        @Override // android.media.Image
        public Image.Plane[] getPlanes() {
            throwISEIfImageIsInvalid();
            Image.Plane[] planeArr = this.mPlanes;
            return (Image.Plane[]) Arrays.copyOf(planeArr, planeArr.length);
        }

        @Override // android.media.Image, java.lang.AutoCloseable
        public void close() {
            if (this.mIsImageValid) {
                NioUtils.freeDirectBuffer(this.mBuffer);
                this.mIsImageValid = false;
            }
        }

        @Override // android.media.Image
        public void setCropRect(Rect cropRect) {
            if (!this.mIsReadOnly) {
                super.setCropRect(cropRect);
                return;
            }
            throw new ReadOnlyBufferException();
        }

        public MediaImage(ByteBuffer buffer, ByteBuffer info, boolean readOnly, long timestamp, int xOffset, int yOffset, Rect cropRect) {
            Rect cropRect2;
            ByteBuffer byteBuffer = buffer;
            this.mTimestamp = timestamp;
            this.mIsImageValid = true;
            this.mIsReadOnly = buffer.isReadOnly();
            this.mBuffer = buffer.duplicate();
            this.mXOffset = xOffset;
            this.mYOffset = yOffset;
            this.mInfo = info;
            if (info.remaining() == 104) {
                int type = info.getInt();
                if (type == 1) {
                    int numPlanes = info.getInt();
                    if (numPlanes == 3) {
                        this.mWidth = info.getInt();
                        this.mHeight = info.getInt();
                        if (this.mWidth < 1 || this.mHeight < 1) {
                            throw new UnsupportedOperationException("unsupported size: " + this.mWidth + "x" + this.mHeight);
                        }
                        int bitDepth = info.getInt();
                        if (bitDepth == 8) {
                            int bitDepthAllocated = info.getInt();
                            if (bitDepthAllocated == 8) {
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
                                            byteBuffer.position(this.mBuffer.position() + planeOffset + ((xOffset / horiz) * colInc) + ((yOffset / vert) * rowInc));
                                            byteBuffer.limit(buffer.position() + Utils.divUp(bitDepth, 8) + (((this.mHeight / vert) - 1) * rowInc) + (((this.mWidth / horiz) - 1) * colInc));
                                            this.mPlanes[ix] = new MediaPlane(buffer.slice(), rowInc, colInc);
                                            ix++;
                                            byteBuffer = buffer;
                                        }
                                    }
                                    throw new UnsupportedOperationException("unexpected subsampling: " + horiz + "x" + vert + " on plane " + ix);
                                }
                                if (cropRect == null) {
                                    cropRect2 = new Rect(0, 0, this.mWidth, this.mHeight);
                                } else {
                                    cropRect2 = cropRect;
                                }
                                cropRect2.offset(-xOffset, -yOffset);
                                super.setCropRect(cropRect2);
                                return;
                            }
                            throw new UnsupportedOperationException("unsupported allocated bit depth: " + bitDepthAllocated);
                        }
                        throw new UnsupportedOperationException("unsupported bit depth: " + bitDepth);
                    }
                    throw new RuntimeException("unexpected number of planes: " + numPlanes);
                }
                throw new UnsupportedOperationException("unsupported type: " + type);
            }
            throw new UnsupportedOperationException("unsupported info length: " + info.remaining());
        }

        private class MediaPlane extends Image.Plane {
            private final int mColInc;
            private final ByteBuffer mData;
            private final int mRowInc;

            public MediaPlane(ByteBuffer buffer, int rowInc, int colInc) {
                this.mData = buffer;
                this.mRowInc = rowInc;
                this.mColInc = colInc;
            }

            @Override // android.media.Image.Plane
            public int getRowStride() {
                MediaImage.this.throwISEIfImageIsInvalid();
                return this.mRowInc;
            }

            @Override // android.media.Image.Plane
            public int getPixelStride() {
                MediaImage.this.throwISEIfImageIsInvalid();
                return this.mColInc;
            }

            @Override // android.media.Image.Plane
            public ByteBuffer getBuffer() {
                MediaImage.this.throwISEIfImageIsInvalid();
                return this.mData;
            }
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
}
