package android.media;

import android.bluetooth.BluetoothAssignedNumbers;
import android.hardware.SensorManager;
import android.media.MediaCodec.BufferInfo;
import android.net.wifi.AnqpInformationElement;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

public final class MediaMuxer {
    private static final int MUXER_STATE_INITIALIZED = 0;
    private static final int MUXER_STATE_STARTED = 1;
    private static final int MUXER_STATE_STOPPED = 2;
    private static final int MUXER_STATE_UNINITIALIZED = -1;
    private final CloseGuard mCloseGuard;
    private int mLastTrackIndex;
    private long mNativeObject;
    private int mState;

    public static final class OutputFormat {
        public static final int MUXER_OUTPUT_MPEG_4 = 0;
        public static final int MUXER_OUTPUT_WEBM = 1;

        private OutputFormat() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaMuxer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaMuxer.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaMuxer.<clinit>():void");
    }

    private static native int nativeAddTrack(long j, String[] strArr, Object[] objArr);

    private static native void nativeRelease(long j);

    private static native void nativeSetLocation(long j, int i, int i2);

    private static native void nativeSetOrientationHint(long j, int i);

    private static native long nativeSetup(FileDescriptor fileDescriptor, int i);

    private static native void nativeStart(long j);

    private static native void nativeStop(long j);

    private static native void nativeWriteSampleData(long j, int i, ByteBuffer byteBuffer, int i2, int i3, long j2, int i4);

    public MediaMuxer(String path, int format) throws IOException {
        Throwable th;
        this.mState = MUXER_STATE_UNINITIALIZED;
        this.mCloseGuard = CloseGuard.get();
        this.mLastTrackIndex = MUXER_STATE_UNINITIALIZED;
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        } else if (format == 0 || format == MUXER_STATE_STARTED) {
            RandomAccessFile randomAccessFile = null;
            try {
                RandomAccessFile file = new RandomAccessFile(path, "rws");
                try {
                    this.mNativeObject = nativeSetup(file.getFD(), format);
                    this.mState = MUXER_STATE_INITIALIZED;
                    this.mCloseGuard.open("release");
                    if (file != null) {
                        file.close();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    randomAccessFile = file;
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                throw th;
            }
        } else {
            throw new IllegalArgumentException("format is invalid");
        }
    }

    public void setOrientationHint(int degrees) {
        if (degrees != 0 && degrees != 90 && degrees != BluetoothAssignedNumbers.BDE_TECHNOLOGY && degrees != AnqpInformationElement.ANQP_TDLS_CAP) {
            throw new IllegalArgumentException("Unsupported angle: " + degrees);
        } else if (this.mState == 0) {
            nativeSetOrientationHint(this.mNativeObject, degrees);
        } else {
            throw new IllegalStateException("Can't set rotation degrees due to wrong state.");
        }
    }

    public void setLocation(float latitude, float longitude) {
        int latitudex10000 = (int) (((double) (latitude * SensorManager.LIGHT_OVERCAST)) + 0.5d);
        int longitudex10000 = (int) (((double) (longitude * SensorManager.LIGHT_OVERCAST)) + 0.5d);
        if (latitudex10000 > 900000 || latitudex10000 < -900000) {
            throw new IllegalArgumentException("Latitude: " + latitude + " out of range.");
        } else if (longitudex10000 > 1800000 || longitudex10000 < -1800000) {
            throw new IllegalArgumentException("Longitude: " + longitude + " out of range");
        } else if (this.mState != 0 || this.mNativeObject == 0) {
            throw new IllegalStateException("Can't set location due to wrong state.");
        } else {
            nativeSetLocation(this.mNativeObject, latitudex10000, longitudex10000);
        }
    }

    public void start() {
        if (this.mNativeObject == 0) {
            throw new IllegalStateException("Muxer has been released!");
        } else if (this.mState == 0) {
            nativeStart(this.mNativeObject);
            this.mState = MUXER_STATE_STARTED;
        } else {
            throw new IllegalStateException("Can't start due to wrong state.");
        }
    }

    public void stop() {
        if (this.mState == MUXER_STATE_STARTED) {
            nativeStop(this.mNativeObject);
            this.mState = MUXER_STATE_STOPPED;
            return;
        }
        throw new IllegalStateException("Can't stop due to wrong state.");
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            if (this.mNativeObject != 0) {
                nativeRelease(this.mNativeObject);
                this.mNativeObject = 0;
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public int addTrack(MediaFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null.");
        } else if (this.mState != 0) {
            throw new IllegalStateException("Muxer is not initialized.");
        } else if (this.mNativeObject == 0) {
            throw new IllegalStateException("Muxer has been released!");
        } else {
            Map<String, Object> formatMap = format.getMap();
            int mapSize = formatMap.size();
            if (mapSize > 0) {
                String[] keys = new String[mapSize];
                Object[] values = new Object[mapSize];
                int i = MUXER_STATE_INITIALIZED;
                for (Entry<String, Object> entry : formatMap.entrySet()) {
                    keys[i] = (String) entry.getKey();
                    values[i] = entry.getValue();
                    i += MUXER_STATE_STARTED;
                }
                int trackIndex = nativeAddTrack(this.mNativeObject, keys, values);
                if (this.mLastTrackIndex >= trackIndex) {
                    throw new IllegalArgumentException("Invalid format.");
                }
                this.mLastTrackIndex = trackIndex;
                return trackIndex;
            }
            throw new IllegalArgumentException("format must not be empty.");
        }
    }

    public void writeSampleData(int trackIndex, ByteBuffer byteBuf, BufferInfo bufferInfo) {
        if (trackIndex < 0 || trackIndex > this.mLastTrackIndex) {
            throw new IllegalArgumentException("trackIndex is invalid");
        } else if (byteBuf == null) {
            throw new IllegalArgumentException("byteBuffer must not be null");
        } else if (bufferInfo == null) {
            throw new IllegalArgumentException("bufferInfo must not be null");
        } else if (bufferInfo.size < 0 || bufferInfo.offset < 0 || bufferInfo.offset + bufferInfo.size > byteBuf.capacity() || bufferInfo.presentationTimeUs < 0) {
            throw new IllegalArgumentException("bufferInfo must specify a valid buffer offset, size and presentation time");
        } else if (this.mNativeObject == 0) {
            throw new IllegalStateException("Muxer has been released!");
        } else if (this.mState != MUXER_STATE_STARTED) {
            throw new IllegalStateException("Can't write, muxer is not started");
        } else {
            nativeWriteSampleData(this.mNativeObject, trackIndex, byteBuf, bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags);
        }
    }

    public void release() {
        if (this.mState == MUXER_STATE_STARTED) {
            stop();
        }
        if (this.mNativeObject != 0) {
            nativeRelease(this.mNativeObject);
            this.mNativeObject = 0;
            this.mCloseGuard.close();
        }
        this.mState = MUXER_STATE_UNINITIALIZED;
    }
}
