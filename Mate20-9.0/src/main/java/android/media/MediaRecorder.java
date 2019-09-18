package android.media;

import android.annotation.SystemApi;
import android.app.ActivityThread;
import android.common.HwFrameworkFactory;
import android.hardware.Camera;
import android.hdm.HwDeviceManager;
import android.hsm.HwMediaPermWrapper;
import android.media.AudioRouting;
import android.media.MediaCodec;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.storage.ExternalStorageRandomAccessFileImpl;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import com.android.internal.annotations.GuardedBy;
import com.huawei.android.audio.HwAudioServiceManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MediaRecorder implements AudioRouting {
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
    public static final int MEDIA_RECORDER_ERROR_UNKNOWN = 1;
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
    public static final int RECORDSTATE_STARTED = 3;
    public static final int RECORDSTATE_STOPPED = 1;
    private static final String TAG = "MediaRecorder";
    private int mChannelCount;
    private EventHandler mEventHandler;
    private FileDescriptor mFd;
    private File mFile;
    /* access modifiers changed from: private */
    public long mNativeContext;
    /* access modifiers changed from: private */
    public OnErrorListener mOnErrorListener;
    /* access modifiers changed from: private */
    public OnInfoListener mOnInfoListener;
    private String mPath;
    private HwMediaPermWrapper mPerm = new HwMediaPermWrapper();
    private AudioDeviceInfo mPreferredDevice = null;
    private int mRecordSource = -1;
    /* access modifiers changed from: private */
    @GuardedBy("mRoutingChangeListeners")
    public ArrayMap<AudioRouting.OnRoutingChangedListener, NativeRoutingEventHandlerDelegate> mRoutingChangeListeners = new ArrayMap<>();
    private Surface mSurface;

    public final class AudioEncoder {
        public static final int AAC = 3;
        public static final int AAC_ELD = 5;
        public static final int AMR_NB = 1;
        public static final int AMR_WB = 2;
        public static final int DEFAULT = 0;
        public static final int HE_AAC = 4;
        public static final int VORBIS = 6;
        public static final int WAV_PCM = 7;

        private AudioEncoder() {
        }
    }

    public final class AudioSource {
        public static final int AUDIO_SOURCE_INVALID = -1;
        public static final int CAMCORDER = 5;
        public static final int DEFAULT = 0;
        @SystemApi
        public static final int HOTWORD = 1999;
        public static final int MIC = 1;
        @SystemApi
        public static final int RADIO_TUNER = 1998;
        public static final int REMOTE_SUBMIX = 8;
        public static final int REMOTE_SUBMIX_EXTEND = 10007;
        public static final int UNPROCESSED = 9;
        public static final int VOICE_CALL = 4;
        public static final int VOICE_COMMUNICATION = 7;
        public static final int VOICE_DOWNLINK = 3;
        public static final int VOICE_RECOGNITION = 6;
        public static final int VOICE_UPLINK = 2;

        private AudioSource() {
        }
    }

    private class EventHandler extends Handler {
        private static final int MEDIA_RECORDER_AUDIO_ROUTING_CHANGED = 10000;
        private static final int MEDIA_RECORDER_EVENT_ERROR = 1;
        private static final int MEDIA_RECORDER_EVENT_INFO = 2;
        private static final int MEDIA_RECORDER_EVENT_LIST_END = 99;
        private static final int MEDIA_RECORDER_EVENT_LIST_START = 1;
        private static final int MEDIA_RECORDER_TRACK_EVENT_ERROR = 100;
        private static final int MEDIA_RECORDER_TRACK_EVENT_INFO = 101;
        private static final int MEDIA_RECORDER_TRACK_EVENT_LIST_END = 1000;
        private static final int MEDIA_RECORDER_TRACK_EVENT_LIST_START = 100;
        private MediaRecorder mMediaRecorder;

        public EventHandler(MediaRecorder mr, Looper looper) {
            super(looper);
            this.mMediaRecorder = mr;
        }

        public void handleMessage(Message msg) {
            if (this.mMediaRecorder.mNativeContext == 0) {
                Log.w(MediaRecorder.TAG, "mediarecorder went away with unhandled events");
                return;
            }
            switch (msg.what) {
                case 1:
                case 100:
                    if (MediaRecorder.this.mOnErrorListener != null) {
                        MediaRecorder.this.mOnErrorListener.onError(this.mMediaRecorder, msg.arg1, msg.arg2);
                    }
                    return;
                case 2:
                case 101:
                    if (MediaRecorder.this.mOnInfoListener != null) {
                        MediaRecorder.this.mOnInfoListener.onInfo(this.mMediaRecorder, msg.arg1, msg.arg2);
                    }
                    return;
                case 10000:
                    AudioManager.resetAudioPortGeneration();
                    synchronized (MediaRecorder.this.mRoutingChangeListeners) {
                        for (NativeRoutingEventHandlerDelegate delegate : MediaRecorder.this.mRoutingChangeListeners.values()) {
                            delegate.notifyClient();
                        }
                    }
                    return;
                default:
                    Log.e(MediaRecorder.TAG, "Unknown message type " + msg.what);
                    return;
            }
        }
    }

    public static final class MetricsConstants {
        public static final String AUDIO_BITRATE = "android.media.mediarecorder.audio-bitrate";
        public static final String AUDIO_CHANNELS = "android.media.mediarecorder.audio-channels";
        public static final String AUDIO_SAMPLERATE = "android.media.mediarecorder.audio-samplerate";
        public static final String AUDIO_TIMESCALE = "android.media.mediarecorder.audio-timescale";
        public static final String CAPTURE_FPS = "android.media.mediarecorder.capture-fps";
        public static final String CAPTURE_FPS_ENABLE = "android.media.mediarecorder.capture-fpsenable";
        public static final String FRAMERATE = "android.media.mediarecorder.frame-rate";
        public static final String HEIGHT = "android.media.mediarecorder.height";
        public static final String MOVIE_TIMESCALE = "android.media.mediarecorder.movie-timescale";
        public static final String ROTATION = "android.media.mediarecorder.rotation";
        public static final String VIDEO_BITRATE = "android.media.mediarecorder.video-bitrate";
        public static final String VIDEO_IFRAME_INTERVAL = "android.media.mediarecorder.video-iframe-interval";
        public static final String VIDEO_LEVEL = "android.media.mediarecorder.video-encoder-level";
        public static final String VIDEO_PROFILE = "android.media.mediarecorder.video-encoder-profile";
        public static final String VIDEO_TIMESCALE = "android.media.mediarecorder.video-timescale";
        public static final String WIDTH = "android.media.mediarecorder.width";

        private MetricsConstants() {
        }
    }

    public interface OnErrorListener {
        void onError(MediaRecorder mediaRecorder, int i, int i2);
    }

    public interface OnInfoListener {
        void onInfo(MediaRecorder mediaRecorder, int i, int i2);
    }

    public final class OutputFormat {
        public static final int AAC_ADIF = 5;
        public static final int AAC_ADTS = 6;
        public static final int AMR_NB = 3;
        public static final int AMR_WB = 4;
        public static final int DEFAULT = 0;
        public static final int MPEG_2_TS = 8;
        public static final int MPEG_4 = 2;
        public static final int OUTPUT_FORMAT_RTP_AVP = 7;
        public static final int RAW_AMR = 3;
        public static final int THREE_GPP = 1;
        public static final int WAV_PCM = 11;
        public static final int WEBM = 9;

        private OutputFormat() {
        }
    }

    public final class VideoEncoder {
        public static final int DEFAULT = 0;
        public static final int H263 = 1;
        public static final int H264 = 2;
        public static final int HEVC = 5;
        public static final int MPEG_4_SP = 3;
        public static final int VP8 = 4;

        private VideoEncoder() {
        }
    }

    public final class VideoSource {
        public static final int CAMERA = 1;
        public static final int DEFAULT = 0;
        public static final int SURFACE = 2;

        private VideoSource() {
        }
    }

    private native void _prepare() throws IllegalStateException, IOException;

    private native void _setNextOutputFile(FileDescriptor fileDescriptor) throws IllegalStateException, IOException;

    private native void _setOutputFile(FileDescriptor fileDescriptor) throws IllegalStateException, IOException;

    private final native void native_enableDeviceCallback(boolean z);

    private final native void native_finalize();

    private final native int native_getActiveMicrophones(ArrayList<MicrophoneInfo> arrayList);

    private native PersistableBundle native_getMetrics();

    private final native int native_getRoutedDeviceId();

    private static final native void native_init();

    private native void native_reset();

    private final native boolean native_setInputDevice(int i);

    private final native void native_setInputSurface(Surface surface);

    private final native void native_setup(Object obj, String str, String str2) throws IllegalStateException;

    private native void setParameter(String str);

    public native void _setOutputFile(FileDescriptor fileDescriptor, long j, long j2) throws IllegalStateException, IOException;

    public native int getMaxAmplitude() throws IllegalStateException;

    public native Surface getSurface();

    public native void native_setAudioSource(int i) throws IllegalStateException;

    public native void native_setVideoSource(int i) throws IllegalStateException;

    public native void native_start() throws IllegalStateException;

    public native void native_stop() throws IllegalStateException;

    public native void pause() throws IllegalStateException;

    public native void release();

    public native void resume() throws IllegalStateException;

    public native void setAudioEncoder(int i) throws IllegalStateException;

    @Deprecated
    public native void setCamera(Camera camera);

    public native void setMaxDuration(int i) throws IllegalArgumentException;

    public native void setMaxFileSize(long j) throws IllegalArgumentException;

    public native void setOutputFormat(int i) throws IllegalStateException;

    public native void setVideoEncoder(int i) throws IllegalStateException;

    public native void setVideoFrameRate(int i) throws IllegalStateException;

    public native void setVideoSize(int i, int i2) throws IllegalStateException;

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    public MediaRecorder() {
        Looper myLooper = Looper.myLooper();
        Looper looper = myLooper;
        if (myLooper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            Looper mainLooper = Looper.getMainLooper();
            Looper looper2 = mainLooper;
            if (mainLooper != null) {
                this.mEventHandler = new EventHandler(this, looper2);
            } else {
                this.mEventHandler = null;
            }
        }
        this.mChannelCount = 1;
        native_setup(new WeakReference(this), ActivityThread.currentPackageName(), ActivityThread.currentOpPackageName());
    }

    public void setInputSurface(Surface surface) {
        if (surface instanceof MediaCodec.PersistentSurface) {
            native_setInputSurface(surface);
            return;
        }
        throw new IllegalArgumentException("not a PersistentSurface");
    }

    public void setPreviewDisplay(Surface sv) {
        this.mSurface = this.mPerm.setPreviewDisplay(sv);
    }

    public static boolean isSystemOnlyAudioSource(int source) {
        switch (source) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 9:
                return false;
            default:
                return true;
        }
    }

    public static final String toLogFriendlyAudioSource(int source) {
        switch (source) {
            case -1:
                return "AUDIO_SOURCE_INVALID";
            case 0:
                return "DEFAULT";
            case 1:
                return "MIC";
            case 2:
                return "VOICE_UPLINK";
            case 3:
                return "VOICE_DOWNLINK";
            case 4:
                return "VOICE_CALL";
            case 5:
                return "CAMCORDER";
            case 6:
                return "VOICE_RECOGNITION";
            case 7:
                return "VOICE_COMMUNICATION";
            case 8:
                return "REMOTE_SUBMIX";
            case 9:
                return "UNPROCESSED";
            default:
                switch (source) {
                    case AudioSource.RADIO_TUNER /*1998*/:
                        return "RADIO_TUNER";
                    case 1999:
                        return "HOTWORD";
                    default:
                        return "unknown source " + source;
                }
        }
    }

    public static final int getAudioSourceMax() {
        return 9;
    }

    public void setVideoSource(int video_source) throws IllegalStateException {
        this.mPerm.confirmCameraPermission();
        native_setVideoSource(video_source);
    }

    public void setProfile(CamcorderProfile profile) {
        setOutputFormat(profile.fileFormat);
        setVideoFrameRate(profile.videoFrameRate);
        setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        setVideoEncodingBitRate(profile.videoBitRate);
        setVideoEncoder(profile.videoCodec);
        if (profile.quality < 1000 || profile.quality > 1007) {
            setAudioEncodingBitRate(profile.audioBitRate);
            setAudioChannels(profile.audioChannels);
            setAudioSamplingRate(profile.audioSampleRate);
            setAudioEncoder(profile.audioCodec);
        }
    }

    public void setCaptureRate(double fps) {
        setParameter("time-lapse-enable=1");
        setParameter("time-lapse-fps=" + fps);
    }

    public void setOrientationHint(int degrees) {
        if (degrees == 0 || degrees == 90 || degrees == 180 || degrees == 270) {
            setParameter("video-param-rotation-angle-degrees=" + degrees);
            return;
        }
        throw new IllegalArgumentException("Unsupported angle: " + degrees);
    }

    public void setLocation(float latitude, float longitude) {
        int latitudex10000 = (int) (((double) (latitude * 10000.0f)) + 0.5d);
        int longitudex10000 = (int) (((double) (10000.0f * longitude)) + 0.5d);
        if (latitudex10000 > 900000 || latitudex10000 < -900000) {
            throw new IllegalArgumentException("Latitude out of range.");
        } else if (longitudex10000 > 1800000 || longitudex10000 < -1800000) {
            throw new IllegalArgumentException("Longitude out of range");
        } else {
            setParameter("param-geotag-latitude=" + latitudex10000);
            setParameter("param-geotag-longitude=" + longitudex10000);
        }
    }

    public void setAudioSamplingRate(int samplingRate) {
        if (samplingRate > 0) {
            setParameter("audio-param-sampling-rate=" + samplingRate);
            return;
        }
        throw new IllegalArgumentException("Audio sampling rate is not positive");
    }

    public void setAudioChannels(int numChannels) {
        if (numChannels > 0) {
            this.mChannelCount = numChannels;
            setParameter("audio-param-number-of-channels=" + numChannels);
            return;
        }
        throw new IllegalArgumentException("Number of channels is not positive");
    }

    public void setAudioEncodingBitRate(int bitRate) {
        if (bitRate > 0) {
            setParameter("audio-param-encoding-bitrate=" + bitRate);
            return;
        }
        throw new IllegalArgumentException("Audio encoding bit rate is not positive");
    }

    public void setVideoEncodingBitRate(int bitRate) {
        if (bitRate > 0) {
            setParameter("video-param-encoding-bitrate=" + bitRate);
            return;
        }
        throw new IllegalArgumentException("Video encoding bit rate is not positive");
    }

    public void setVideoEncodingProfileLevel(int profile, int level) {
        if (profile <= 0) {
            throw new IllegalArgumentException("Video encoding profile is not positive");
        } else if (level > 0) {
            setParameter("video-param-encoder-profile=" + profile);
            setParameter("video-param-encoder-level=" + level);
        } else {
            throw new IllegalArgumentException("Video encoding level is not positive");
        }
    }

    public void setUse64bitOffset(int bUse64BitOffset) {
        Log.i(TAG, "setUse64bitOffset param-use-64bit-offset=" + bUse64BitOffset);
        setParameter("param-use-64bit-offset=" + bUse64BitOffset);
    }

    public void setAuxiliaryOutputFile(FileDescriptor fd) {
        Log.w(TAG, "setAuxiliaryOutputFile(FileDescriptor) is no longer supported.");
    }

    public void setAuxiliaryOutputFile(String path) {
        Log.w(TAG, "setAuxiliaryOutputFile(String) is no longer supported.");
    }

    public void setOutputFile(FileDescriptor fd) throws IllegalStateException {
        this.mPath = null;
        this.mFile = null;
        this.mFd = fd;
    }

    public void setOutputFile(File file) {
        this.mPath = null;
        this.mFd = null;
        this.mFile = file;
    }

    public void setNextOutputFile(FileDescriptor fd) throws IOException {
        _setNextOutputFile(fd);
    }

    public void setOutputFile(String path) throws IllegalStateException {
        this.mFd = null;
        this.mFile = null;
        this.mPath = path;
    }

    public void setNextOutputFile(File file) throws IOException {
        ExternalStorageRandomAccessFileImpl f = new ExternalStorageRandomAccessFileImpl(file, "rw");
        try {
            _setNextOutputFile(f.getFD());
        } finally {
            f.close();
        }
    }

    public void setInterOutputFile(FileDescriptor fd) throws IllegalStateException, IOException {
        _setOutputFile(fd);
    }

    public void prepare() throws IllegalStateException, IOException {
        if (this.mPath != null) {
            this.mPerm.setOutputFile(this, this.mPath);
        } else if (this.mFd != null) {
            this.mPerm.setOutputFile(this, this.mFd);
        } else if (this.mFile != null) {
            this.mPerm.setOutputFile(this, this.mFile);
        } else {
            throw new IOException("No valid output file");
        }
        _prepare();
    }

    public void reset() {
        native_reset();
        this.mEventHandler.removeCallbacksAndMessages(null);
    }

    public void setOnErrorListener(OnErrorListener l) {
        this.mOnErrorListener = l;
    }

    public void setOnInfoListener(OnInfoListener listener) {
        this.mOnInfoListener = listener;
    }

    public boolean setPreferredDevice(AudioDeviceInfo deviceInfo) {
        int preferredDeviceId = 0;
        if (deviceInfo != null && !deviceInfo.isSource()) {
            return false;
        }
        if (deviceInfo != null) {
            preferredDeviceId = deviceInfo.getId();
        }
        boolean status = native_setInputDevice(preferredDeviceId);
        if (status) {
            synchronized (this) {
                this.mPreferredDevice = deviceInfo;
            }
        }
        return status;
    }

    public AudioDeviceInfo getPreferredDevice() {
        AudioDeviceInfo audioDeviceInfo;
        synchronized (this) {
            audioDeviceInfo = this.mPreferredDevice;
        }
        return audioDeviceInfo;
    }

    public AudioDeviceInfo getRoutedDevice() {
        int deviceId = native_getRoutedDeviceId();
        if (deviceId == 0) {
            return null;
        }
        AudioDeviceInfo[] devices = AudioManager.getDevicesStatic(1);
        for (int i = 0; i < devices.length; i++) {
            if (devices[i].getId() == deviceId) {
                return devices[i];
            }
        }
        return null;
    }

    @GuardedBy("mRoutingChangeListeners")
    private void enableNativeRoutingCallbacksLocked(boolean enabled) {
        if (this.mRoutingChangeListeners.size() == 0) {
            native_enableDeviceCallback(enabled);
        }
    }

    public void addOnRoutingChangedListener(AudioRouting.OnRoutingChangedListener listener, Handler handler) {
        synchronized (this.mRoutingChangeListeners) {
            if (listener != null) {
                try {
                    if (!this.mRoutingChangeListeners.containsKey(listener)) {
                        enableNativeRoutingCallbacksLocked(true);
                        this.mRoutingChangeListeners.put(listener, new NativeRoutingEventHandlerDelegate(this, listener, handler != null ? handler : this.mEventHandler));
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public void removeOnRoutingChangedListener(AudioRouting.OnRoutingChangedListener listener) {
        synchronized (this.mRoutingChangeListeners) {
            if (this.mRoutingChangeListeners.containsKey(listener)) {
                this.mRoutingChangeListeners.remove(listener);
                enableNativeRoutingCallbacksLocked(false);
            }
        }
    }

    public List<MicrophoneInfo> getActiveMicrophones() throws IOException {
        ArrayList<MicrophoneInfo> activeMicrophones = new ArrayList<>();
        int status = native_getActiveMicrophones(activeMicrophones);
        if (status != 0) {
            if (status != -3) {
                Log.e(TAG, "getActiveMicrophones failed:" + status);
            }
            Log.i(TAG, "getActiveMicrophones failed, fallback on routed device info");
        }
        AudioManager.setPortIdForMicrophones(activeMicrophones);
        if (activeMicrophones.size() == 0) {
            AudioDeviceInfo device = getRoutedDevice();
            if (device != null) {
                MicrophoneInfo microphone = AudioManager.microphoneInfoFromAudioDeviceInfo(device);
                ArrayList<Pair<Integer, Integer>> channelMapping = new ArrayList<>();
                for (int i = 0; i < this.mChannelCount; i++) {
                    channelMapping.add(new Pair(Integer.valueOf(i), 1));
                }
                microphone.setChannelMapping(channelMapping);
                activeMicrophones.add(microphone);
            }
        }
        return activeMicrophones;
    }

    private static void postEventFromNative(Object mediarecorder_ref, int what, int arg1, int arg2, Object obj) {
        MediaRecorder mr = (MediaRecorder) ((WeakReference) mediarecorder_ref).get();
        if (!(mr == null || mr.mEventHandler == null)) {
            mr.mEventHandler.sendMessage(mr.mEventHandler.obtainMessage(what, arg1, arg2, obj));
        }
    }

    public PersistableBundle getMetrics() {
        return native_getMetrics();
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        native_finalize();
    }

    public void start() throws IllegalStateException {
        Log.i(TAG, "start");
        if (!HwDeviceManager.disallowOp(30)) {
            HwAudioServiceManager.checkRecordActive(this.mRecordSource);
            native_start();
            HwAudioServiceManager.sendRecordStateChangedIntent(3);
            HwFrameworkFactory.getHwMediaRecorder().sendStateChangedIntent(3);
            return;
        }
        HwFrameworkFactory.getHwMediaRecorder().showDisableMicrophoneToast();
        throw new IllegalStateException("microphone has been disabled.");
    }

    public void stop() throws IllegalStateException {
        Log.i(TAG, "stop");
        native_stop();
        HwAudioServiceManager.sendRecordStateChangedIntent(3);
        HwFrameworkFactory.getHwMediaRecorder().sendStateChangedIntent(1);
    }

    public void setAudioSource(int audio_source) throws IllegalStateException {
        this.mRecordSource = audio_source;
        native_setAudioSource(audio_source);
    }
}
