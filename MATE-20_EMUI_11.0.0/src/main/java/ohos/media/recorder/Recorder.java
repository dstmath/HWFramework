package ohos.media.recorder;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import ohos.agp.graphics.Surface;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.media.common.AudioProperty;
import ohos.media.common.Source;
import ohos.media.common.StorageProperty;
import ohos.media.common.VideoProperty;
import ohos.media.recorderimpl.adapter.RecorderServiceAdapter;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class Recorder {
    private static final int ERROR = -1;
    private static final String HANDLER_NAME = "nativeEventHandler";
    private static final Logger LOGGER = LoggerFactory.getImageLogger(Recorder.class);
    private static final float MAX_LATITUDE = 90.0f;
    private static final float MAX_LONGITUDE = 180.0f;
    private static final int MEDIA_RECORDER_EVENT_ERROR = 1;
    private static final int MEDIA_RECORDER_EVENT_INFO = 2;
    private static final int MEDIA_RECORDER_TRACK_EVENT_ERROR = 100;
    private static final int MEDIA_RECORDER_TRACK_EVENT_INFO = 101;
    private static final float MIN_LATITUDE = -90.0f;
    private static final float MIN_LONGITUDE = -180.0f;
    private static final double ONE_FRAME_PER_DAY = 1.1574074074074073E-5d;
    private static final int SUCCESS = 0;
    private volatile EventHandler handler;
    private int maxDurationMs;
    private long maxFileSizeBytes;
    private long nativeZRecorder = 0;
    private FileDescriptor outputFd;
    private File outputFile;
    private String outputPath;
    private volatile IRecorderListener recorderListener;

    public interface IRecorderListener {
        void onError(int i, int i2);

        void onInfo(int i, int i2);
    }

    private native Surface nativeGetSurface();

    private static native void nativeInit();

    private native int nativePause();

    private native int nativePrepare();

    private native int nativeRelease();

    private native int nativeReset();

    private native int nativeResume();

    private native int nativeSetAudioChannels(int i);

    private native int nativeSetAudioEncoder(int i);

    private native int nativeSetAudioEncodingBitRate(int i);

    private native int nativeSetAudioSamplingRate(int i);

    private native int nativeSetAudioSource(int i);

    private native int nativeSetCaptureRate(double d);

    private native int nativeSetLocation(int i, int i2);

    private native int nativeSetMaxDuration(long j);

    private native int nativeSetMaxFileSize(long j);

    private native int nativeSetOrientationHint(int i);

    private native int nativeSetOutputFile(FileDescriptor fileDescriptor);

    private native int nativeSetOutputFormat(int i);

    private native int nativeSetVideoEncoder(int i);

    private native int nativeSetVideoEncodingBitRate(int i);

    private native int nativeSetVideoFrameRate(int i);

    private native int nativeSetVideoSize(int i, int i2);

    private native int nativeSetVideoSource(int i);

    private native void nativeSetup(Recorder recorder, String str, String str2);

    private native int nativeStart();

    private native int nativeStop();

    static {
        System.loadLibrary("zrecorder_jni.z");
        nativeInit();
    }

    public Recorder() {
        nativeSetup(this, RecorderServiceAdapter.getPackageName(), RecorderServiceAdapter.getOpPackageName());
    }

    public boolean setSource(Source source) {
        if (source == null) {
            LOGGER.error("Source cannot be null", new Object[0]);
            return false;
        } else if (source.getRecorderAudioSource() != -1 && !setAudioSource(source.getRecorderAudioSource())) {
            LOGGER.error("SetAudioSource failed", new Object[0]);
            return false;
        } else if (source.getRecorderVideoSource() == -1 || setVideoSource(source.getRecorderVideoSource())) {
            return true;
        } else {
            LOGGER.error("SetVideoSource failed", new Object[0]);
            return false;
        }
    }

    public boolean setAudioProperty(AudioProperty audioProperty) {
        if (audioProperty == null) {
            LOGGER.error("AudioProperty cannot be null", new Object[0]);
            return false;
        }
        int recorderAudioEncoder = audioProperty.getRecorderAudioEncoder();
        if (recorderAudioEncoder == -1 || setAudioEncoder(recorderAudioEncoder)) {
            int recorderBitRate = audioProperty.getRecorderBitRate();
            if (recorderBitRate == 0 || setAudioEncodingBitRate(recorderBitRate)) {
                int recorderSamplingRate = audioProperty.getRecorderSamplingRate();
                if (recorderSamplingRate == 0 || setAudioSamplingRate(recorderSamplingRate)) {
                    int recorderNumChannels = audioProperty.getRecorderNumChannels();
                    if (recorderNumChannels == 0 || setAudioChannels(recorderNumChannels)) {
                        return true;
                    }
                    LOGGER.error("SetAudioChannels failed", new Object[0]);
                    return false;
                }
                LOGGER.error("SetAudioSamplingRate failed", new Object[0]);
                return false;
            }
            LOGGER.error("SetAudioEncodingBitRate failed", new Object[0]);
            return false;
        }
        LOGGER.error("SetAudioEncoder failed", new Object[0]);
        return false;
    }

    public boolean setVideoProperty(VideoProperty videoProperty) {
        if (videoProperty == null) {
            LOGGER.error("VideoProperty cannot be null", new Object[0]);
            return false;
        } else if ((videoProperty.getRecorderFps() != 0 && !setCaptureRate((double) videoProperty.getRecorderFps())) || (videoProperty.getRecorderDegrees() != -1 && !setOrientationHint(videoProperty.getRecorderDegrees()))) {
            LOGGER.error("SetCaptureRate or SetOrientationHint failed", new Object[0]);
            return false;
        } else if ((videoProperty.getRecorderVideoEncoder() != -1 && !setVideoEncoder(videoProperty.getRecorderVideoEncoder())) || (videoProperty.getRecorderBitRate() != -1 && !setVideoEncodingBitRate(videoProperty.getRecorderBitRate()))) {
            LOGGER.error("SetVideoEncoder or SetVideoEncodingBitRate failed", new Object[0]);
            return false;
        } else if ((videoProperty.getRecorderRate() == -1 || setVideoFrameRate(videoProperty.getRecorderRate())) && (videoProperty.getRecorderWidth() == -1 || videoProperty.getRecorderHeight() == -1 || setVideoSize(videoProperty.getRecorderWidth(), videoProperty.getRecorderHeight()))) {
            return true;
        } else {
            LOGGER.error("SetVideoFrameRate or SetVideoSize failed", new Object[0]);
            return false;
        }
    }

    public boolean setStorageProperty(StorageProperty storageProperty) {
        if (storageProperty == null) {
            LOGGER.error("StorageProperty cannot be null", new Object[0]);
            return false;
        }
        this.outputFd = storageProperty.getRecorderFd();
        this.outputPath = storageProperty.getRecorderPath();
        this.outputFile = storageProperty.getRecorderFile();
        this.maxDurationMs = storageProperty.getRecorderMaxDurationMs();
        this.maxFileSizeBytes = storageProperty.getRecorderMaxFileSizeBytes();
        return true;
    }

    private boolean setAudioSource(int i) {
        if (i < 0 || i > 10) {
            LOGGER.error("Invalid audioSource:%{public}d", Integer.valueOf(i));
            return false;
        }
        int nativeSetAudioSource = nativeSetAudioSource(i);
        if (nativeSetAudioSource == 0) {
            return true;
        }
        LOGGER.error("setAudioSource failed, error code:%{public}d, audioSource:%{public}d", Integer.valueOf(nativeSetAudioSource), Integer.valueOf(i));
        return false;
    }

    private boolean setVideoSource(int i) {
        if (i < 0 || i > 2) {
            LOGGER.error("Invalid videoSource:%{public}d", Integer.valueOf(i));
            return false;
        }
        int nativeSetVideoSource = nativeSetVideoSource(i);
        if (nativeSetVideoSource == 0) {
            return true;
        }
        LOGGER.error("setVideoSource failed, error code:%{public}d, videoSource:%{public}d", Integer.valueOf(nativeSetVideoSource), Integer.valueOf(i));
        return false;
    }

    public boolean prepare() {
        int i = this.maxDurationMs;
        if (i == -1 || setMaxDuration(i)) {
            long j = this.maxFileSizeBytes;
            if (j == -1 || setMaxFileSize(j)) {
                int outputFile2 = setOutputFile();
                if (outputFile2 != 0) {
                    LOGGER.error("set output file failed, error code:%{public}d", Integer.valueOf(outputFile2));
                    return false;
                }
                int nativePrepare = nativePrepare();
                if (nativePrepare == 0) {
                    return true;
                }
                LOGGER.error("prepare failed, error code:%{public}d", Integer.valueOf(nativePrepare));
                return false;
            }
            LOGGER.error("setMaxFileSize failed", new Object[0]);
            return false;
        }
        LOGGER.error("setMaxDuration failed", new Object[0]);
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0024, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0025, code lost:
        $closeResource(r6, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0028, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0063, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0064, code lost:
        $closeResource(r6, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0067, code lost:
        throw r0;
     */
    private int setOutputFile() {
        String str = this.outputPath;
        if (str != null && this.outputFd == null && this.outputFile == null) {
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(str, "rw");
                int nativeSetOutputFile = nativeSetOutputFile(randomAccessFile.getFD());
                $closeResource(null, randomAccessFile);
                return nativeSetOutputFile;
            } catch (IOException unused) {
                LOGGER.error("set output file with path occurs IOException", new Object[0]);
                return -1;
            }
        } else {
            FileDescriptor fileDescriptor = this.outputFd;
            if (fileDescriptor != null && this.outputPath == null && this.outputFile == null) {
                return nativeSetOutputFile(fileDescriptor);
            }
            File file = this.outputFile;
            if (file != null && this.outputPath == null && this.outputFd == null) {
                try {
                    RandomAccessFile randomAccessFile2 = new RandomAccessFile(file, "rw");
                    int nativeSetOutputFile2 = nativeSetOutputFile(randomAccessFile2.getFD());
                    $closeResource(null, randomAccessFile2);
                    return nativeSetOutputFile2;
                } catch (IOException unused2) {
                    LOGGER.error("set output file with File occurs IOException", new Object[0]);
                    return -1;
                }
            } else {
                LOGGER.error("invalid output file type, maybe more than one output file or all type is empty", new Object[0]);
                return -1;
            }
        }
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th != null) {
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        } else {
            autoCloseable.close();
        }
    }

    public boolean start() {
        int nativeStart = nativeStart();
        if (nativeStart == 0) {
            return true;
        }
        LOGGER.error("start failed, error code:%{public}d", Integer.valueOf(nativeStart));
        return false;
    }

    public boolean stop() {
        int nativeStop = nativeStop();
        if (nativeStop == 0) {
            return true;
        }
        LOGGER.error("stop failed, error code:%{public}d", Integer.valueOf(nativeStop));
        return false;
    }

    public boolean pause() {
        int nativePause = nativePause();
        if (nativePause == 0) {
            return true;
        }
        LOGGER.error("pause failed, error code:%{public}d", Integer.valueOf(nativePause));
        return false;
    }

    public boolean resume() {
        int nativeResume = nativeResume();
        if (nativeResume == 0) {
            return true;
        }
        LOGGER.error("resume failed, error code:%{public}d", Integer.valueOf(nativeResume));
        return false;
    }

    public boolean reset() {
        int nativeReset = nativeReset();
        if (nativeReset != 0) {
            LOGGER.error("reset failed, error code:%{public}d", Integer.valueOf(nativeReset));
            return false;
        }
        if (this.handler != null) {
            this.handler.removeAllEvent();
        }
        return true;
    }

    private boolean setAudioChannels(int i) {
        if (i <= 0) {
            LOGGER.error("Number of channels is not positive, numChannels:%{public}d", Integer.valueOf(i));
            return false;
        }
        int nativeSetAudioChannels = nativeSetAudioChannels(i);
        if (nativeSetAudioChannels == 0) {
            return true;
        }
        LOGGER.error("setAudioChannels failed, error code:%{public}d, numChannels:%{public}d", Integer.valueOf(nativeSetAudioChannels), Integer.valueOf(i));
        return false;
    }

    private boolean setAudioEncoder(int i) {
        if ((i < 0 || i > 5) && i != 7) {
            LOGGER.error("Invalid audioEncoder:%{public}d", Integer.valueOf(i));
            return false;
        }
        int nativeSetAudioEncoder = nativeSetAudioEncoder(i);
        if (nativeSetAudioEncoder == 0) {
            return true;
        }
        LOGGER.error("setAudioEncoder failed, error code:%{public}d, audioEncoder:%{public}d", Integer.valueOf(nativeSetAudioEncoder), Integer.valueOf(i));
        return false;
    }

    private boolean setAudioEncodingBitRate(int i) {
        if (i <= 0) {
            LOGGER.error("Audio encoding bit rate is not positive, bitRate:%{public}d", Integer.valueOf(i));
            return false;
        }
        int nativeSetAudioEncodingBitRate = nativeSetAudioEncodingBitRate(i);
        if (nativeSetAudioEncodingBitRate == 0) {
            return true;
        }
        LOGGER.error("setAudioEncodingBitRate failed, error code:%{public}d, bitRate:%{public}d", Integer.valueOf(nativeSetAudioEncodingBitRate), Integer.valueOf(i));
        return false;
    }

    private boolean setAudioSamplingRate(int i) {
        if (i <= 0) {
            LOGGER.error("Audio sampling rate is not positive:%{public}d", Integer.valueOf(i));
            return false;
        }
        int nativeSetAudioSamplingRate = nativeSetAudioSamplingRate(i);
        if (nativeSetAudioSamplingRate == 0) {
            return true;
        }
        LOGGER.error("setAudioSamplingRate failed, error code:%{public}d, positive:%{public}d", Integer.valueOf(nativeSetAudioSamplingRate), Integer.valueOf(i));
        return false;
    }

    private boolean setCaptureRate(double d) {
        if (d < ONE_FRAME_PER_DAY) {
            LOGGER.error("Invalid fps:%{public}f", Double.valueOf(d));
            return false;
        }
        int nativeSetCaptureRate = nativeSetCaptureRate(d);
        if (nativeSetCaptureRate == 0) {
            return true;
        }
        LOGGER.error("setCaptureRate failed, error code:%{public}d, fps:%{public}f", Integer.valueOf(nativeSetCaptureRate), Double.valueOf(d));
        return false;
    }

    public boolean setRecorderLocation(float f, float f2) {
        if (f > MAX_LATITUDE || f < MIN_LATITUDE) {
            LOGGER.error("Invalid latitude:%{public}f", Float.valueOf(f));
            return false;
        } else if (f2 > MAX_LONGITUDE || f2 < MIN_LONGITUDE) {
            LOGGER.error("Invalid longitude:%{public}f", Float.valueOf(f2));
            return false;
        } else {
            int nativeSetLocation = nativeSetLocation((int) (((double) (f * 10000.0f)) + 0.5d), (int) (((double) (10000.0f * f2)) + 0.5d));
            if (nativeSetLocation == 0) {
                return true;
            }
            LOGGER.error("setLocation failed, error code:%{public}d, latitude:%{public}f, longitude:%{public}f", Integer.valueOf(nativeSetLocation), Float.valueOf(f), Float.valueOf(f2));
            return false;
        }
    }

    private boolean setMaxDuration(int i) {
        int nativeSetMaxDuration = nativeSetMaxDuration((long) i);
        if (nativeSetMaxDuration == 0) {
            return true;
        }
        LOGGER.error("setMaxDuration failed, error code:%{public}d, maxDurationMs:%{public}d", Integer.valueOf(nativeSetMaxDuration), Integer.valueOf(i));
        return false;
    }

    private boolean setMaxFileSize(long j) {
        int nativeSetMaxFileSize = nativeSetMaxFileSize(j);
        if (nativeSetMaxFileSize == 0) {
            return true;
        }
        LOGGER.error("setMaxFileSize failed, error code:%{public}d, maxFileSizeBytes:%{public}d", Integer.valueOf(nativeSetMaxFileSize), Long.valueOf(j));
        return false;
    }

    private boolean setOrientationHint(int i) {
        if (i == 0 || i == 90 || i == 180 || i == 270) {
            int nativeSetOrientationHint = nativeSetOrientationHint(i);
            if (nativeSetOrientationHint == 0) {
                return true;
            }
            LOGGER.error("setOrientationHint failed, error code:%{public}d, degrees:%{public}d", Integer.valueOf(nativeSetOrientationHint), Integer.valueOf(i));
            return false;
        }
        LOGGER.error("Invalid degrees of setOrientationHint, degrees:%{public}d", Integer.valueOf(i));
        return false;
    }

    public boolean setOutputFormat(int i) {
        int nativeSetOutputFormat = nativeSetOutputFormat(i);
        if (nativeSetOutputFormat == 0) {
            return true;
        }
        LOGGER.error("setOutputFormat failed, error code:%{public}d, outputFormat:%{public}d", Integer.valueOf(nativeSetOutputFormat), Integer.valueOf(i));
        return false;
    }

    public Surface getVideoSurface() {
        return nativeGetSurface();
    }

    private boolean setVideoFrameRate(int i) {
        if (i <= 0) {
            LOGGER.error("Invalid rate:%{public}d", Integer.valueOf(i));
            return false;
        }
        int nativeSetVideoFrameRate = nativeSetVideoFrameRate(i);
        if (nativeSetVideoFrameRate == 0) {
            return true;
        }
        LOGGER.error("setVideoFrameRate failed, error code:%{public}d, rate:%{public}d", Integer.valueOf(nativeSetVideoFrameRate), Integer.valueOf(i));
        return false;
    }

    private boolean setVideoSize(int i, int i2) {
        int nativeSetVideoSize = nativeSetVideoSize(i, i2);
        if (nativeSetVideoSize == 0) {
            return true;
        }
        LOGGER.error("setVideoSize failed, error code:%{public}d, width:%{public}d, height:%{public}d", Integer.valueOf(nativeSetVideoSize), Integer.valueOf(i), Integer.valueOf(i2));
        return false;
    }

    private boolean setVideoEncoder(int i) {
        if (i == 0 || i == 2 || i == 5) {
            int nativeSetVideoEncoder = nativeSetVideoEncoder(i);
            if (nativeSetVideoEncoder == 0) {
                return true;
            }
            LOGGER.error("setVideoEncoder failed, error code:%{public}d, videoEncoder:%{public}d", Integer.valueOf(nativeSetVideoEncoder), Integer.valueOf(i));
            return false;
        }
        LOGGER.error("Invalid videoEncoder:%{public}d", Integer.valueOf(i));
        return false;
    }

    public boolean setRecorderProfile(RecorderProfile recorderProfile) {
        if (recorderProfile != null) {
            return setRecorderProfileInner(recorderProfile);
        }
        LOGGER.error("RecorderProfile cannot be null", new Object[0]);
        return false;
    }

    private boolean setRecorderProfileInner(RecorderProfile recorderProfile) {
        if (!setOutputFormat(recorderProfile.fileFormat)) {
            LOGGER.error("setOutputFormat of setProfile failed, fileFormat:%{public}d", Integer.valueOf(recorderProfile.fileFormat));
            return false;
        } else if (!setVideoFrameRate(recorderProfile.vFrameRate)) {
            LOGGER.error("setVideoFrameRate of setProfile failed, videoFrameRate:%{public}d", Integer.valueOf(recorderProfile.vFrameRate));
            return false;
        } else if (!setVideoSize(recorderProfile.vFrameWidth, recorderProfile.vFrameHeight)) {
            LOGGER.error("setVideoSize of setProfile failed, vFrameHeight:%{public}d, vFrameWidth:%{public}d", Integer.valueOf(recorderProfile.vFrameHeight), Integer.valueOf(recorderProfile.vFrameWidth));
            return false;
        } else if (!setVideoEncodingBitRate(recorderProfile.vBitRate)) {
            LOGGER.error("setVideoEncodingBitRate of setProfile failed, vBitRate:%{public}d", Integer.valueOf(recorderProfile.vBitRate));
            return false;
        } else if (!setVideoEncoder(recorderProfile.vCodec)) {
            LOGGER.error("setVideoEncoder of setProfile failed, vCodec:%{public}d", Integer.valueOf(recorderProfile.vCodec));
            return false;
        } else {
            if (recorderProfile.qualityLevel < 1000 || recorderProfile.qualityLevel > 1007) {
                if (!setAudioEncodingBitRate(recorderProfile.aBitRate)) {
                    LOGGER.error("setAudioEncodingBitRate of setProfile failed, aBitRate:%{public}d", Integer.valueOf(recorderProfile.aBitRate));
                    return false;
                } else if (!setAudioChannels(recorderProfile.aChannels)) {
                    LOGGER.error("setAudioChannels of setProfile failed, aChannels:%{public}d", Integer.valueOf(recorderProfile.aChannels));
                    return false;
                } else if (!setAudioSamplingRate(recorderProfile.aSampleRate)) {
                    LOGGER.error("setAudioSamplingRate of setProfile failed, aSampleRate:%{public}d", Integer.valueOf(recorderProfile.aSampleRate));
                    return false;
                } else if (!setAudioEncoder(recorderProfile.aCodec)) {
                    LOGGER.error("setAudioEncoder of setProfile failed, aCodec:%{public}d", Integer.valueOf(recorderProfile.aCodec));
                    return false;
                }
            }
            return true;
        }
    }

    private boolean setVideoEncodingBitRate(int i) {
        if (i <= 0) {
            LOGGER.error("Video encoding bit rate is not positive, bitRate:%{public}d", Integer.valueOf(i));
            return false;
        }
        int nativeSetVideoEncodingBitRate = nativeSetVideoEncodingBitRate(i);
        if (nativeSetVideoEncodingBitRate == 0) {
            return true;
        }
        LOGGER.error("setVideoEncodingBitRate failed, error code:%{public}d, bitRate:%{public}d", Integer.valueOf(nativeSetVideoEncodingBitRate), Integer.valueOf(i));
        return false;
    }

    public void registerRecorderListener(IRecorderListener iRecorderListener) {
        this.recorderListener = iRecorderListener;
    }

    private void onEventFromNative(int i, int i2, int i3) {
        if (this.handler == null) {
            this.handler = new EventHandler(EventRunner.create(HANDLER_NAME));
        }
        this.handler.postTask(new Runnable(i, i2, i3) {
            /* class ohos.media.recorder.$$Lambda$Recorder$1GptEUNfu0Jtf_tIQy61senahUo */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                Recorder.this.lambda$onEventFromNative$0$Recorder(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$onEventFromNative$0$Recorder(int i, int i2, int i3) {
        if (this.nativeZRecorder == 0) {
            LOGGER.warn("onEventFromNative recorder has been release", new Object[0]);
        } else if (this.recorderListener == null) {
            LOGGER.debug("onEventFromNative no recorder listener", new Object[0]);
        } else {
            if (i != 1) {
                if (i != 2) {
                    if (i != 100) {
                        if (i != 101) {
                            LOGGER.error("onEventFromNative unknown type %{public}d", Integer.valueOf(i));
                            return;
                        }
                    }
                }
                this.recorderListener.onInfo(i2, i3);
                return;
            }
            this.recorderListener.onError(i2, i3);
        }
    }

    public boolean release() {
        int nativeRelease = nativeRelease();
        if (nativeRelease == 0) {
            return true;
        }
        LOGGER.error("release failed, error code:%{public}d", Integer.valueOf(nativeRelease));
        return false;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        release();
        super.finalize();
    }

    public static final class RecorderOnErrorType {
        public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
        public static final int MEDIA_ERROR_SERVER_DIED = 100;
        public static final int MEDIA_RECORDER_ERROR_UNKNOWN = 1;

        private RecorderOnErrorType() {
        }
    }

    public static final class RecorderOnInfoType {
        public static final int MEDIA_RECORDER_INFO_MAX_DURATION_REACHED = 800;
        public static final int MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING = 802;
        public static final int MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED = 801;
        public static final int MEDIA_RECORDER_INFO_NEXT_OUTPUT_FILE_STARTED = 803;
        public static final int MEDIA_RECORDER_INFO_UNKNOWN = 1;
        public static final int MEDIA_RECORDER_TRACK_INFO_COMPLETION_STATUS = 1000;
        public static final int MEDIA_RECORDER_TRACK_INFO_DATA_KBYTES = 1009;
        public static final int MEDIA_RECORDER_TRACK_INFO_DURATION_MS = 1003;
        public static final int MEDIA_RECORDER_TRACK_INFO_ENCODED_FRAMES = 1005;
        public static final int MEDIA_RECORDER_TRACK_INFO_INITIAL_DELAY_MS = 1007;
        public static final int MEDIA_RECORDER_TRACK_INFO_LIST_END = 2000;
        public static final int MEDIA_RECORDER_TRACK_INFO_LIST_START = 1000;
        public static final int MEDIA_RECORDER_TRACK_INFO_MAX_CHUNK_DUR_MS = 1004;
        public static final int MEDIA_RECORDER_TRACK_INFO_PROGRESS_IN_TIME = 1001;
        public static final int MEDIA_RECORDER_TRACK_INFO_START_OFFSET_MS = 1008;
        public static final int MEDIA_RECORDER_TRACK_INFO_TYPE = 1002;
        public static final int MEDIA_RECORDER_TRACK_INTER_CHUNK_TIME_MS = 1006;

        private RecorderOnInfoType() {
        }
    }

    public static final class AudioSource {
        public static final int CAMCORDER = 5;
        public static final int DEFAULT = 0;
        public static final int MIC = 1;
        public static final int REMOTE_SUBMIX = 8;
        public static final int UNPROCESSED = 9;
        public static final int VOICE_CALL = 4;
        public static final int VOICE_COMMUNICATION = 7;
        public static final int VOICE_DOWNLINK = 3;
        public static final int VOICE_PERFORMANCE = 10;
        public static final int VOICE_RECOGNITION = 6;
        public static final int VOICE_UPLINK = 2;

        private AudioSource() {
        }
    }

    public static final class VideoSource {
        public static final int CAMERA = 1;
        public static final int DEFAULT = 0;
        public static final int SURFACE = 2;

        private VideoSource() {
        }
    }

    public static final class AudioEncoder {
        public static final int AAC = 3;
        public static final int AAC_ELD = 5;
        public static final int AMR_NB = 1;
        public static final int AMR_WB = 2;
        public static final int DEFAULT = 0;
        public static final int HE_AAC = 4;
        public static final int OPUS = 7;

        private AudioEncoder() {
        }
    }

    public static final class VideoEncoder {
        public static final int DEFAULT = 0;
        public static final int H264 = 2;
        public static final int HEVC = 5;

        private VideoEncoder() {
        }
    }

    public static final class OutputFormat {
        public static final int AAC_ADTS = 6;
        public static final int AMR_NB = 3;
        public static final int AMR_WB = 4;
        public static final int DEFAULT = 0;
        public static final int MPEG_2_TS = 8;
        public static final int MPEG_4 = 2;
        public static final int OGG = 11;
        public static final int THREE_GPP = 1;

        private OutputFormat() {
        }
    }

    public static final class OrientationHint {
        public static final int FIRST_PLAYBACK_DERGREE = 0;
        public static final int FOURTH_PLAYBACK_DERGREE = 270;
        public static final int SECOND_PLAYBACK_DERGREE = 90;
        public static final int THIRD_PLAYBACK_DERGREE = 180;

        private OrientationHint() {
        }
    }
}
