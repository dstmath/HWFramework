package android.media;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import java.util.List;

public class HwAutoTuneImpl {
    private static final int AUDIO_CHANNEL_IN_MONO = 1;
    private static final int AUDIO_CHANNEL_IN_STEREO = 2;
    private static final int AUDIO_FORMAT_PCM_16_BIT = 1;
    private static final int AUDIO_FORMAT_PCM_32_BIT = 2;
    private static final int AUDIO_FORMAT_PCM_FLOAT = 3;
    private static final int AUDIO_SAMPLE_RATE_16K = 16000;
    private static final int AUDIO_SAMPLE_RATE_48K = 48000;
    private static final int BUFFER_SIZE_LEN = 5242880;
    private static final int HWAUTOTUNE_ERROR_ILLEGAL_STATE = -4;
    private static final int HWAUTOTUNE_ERROR_UNAUTHORIZED = -2;
    private static final int HWAUTOTUNE_ERROR_UNKNOWN = -100;
    private static final int HWAUTOTUNE_ERROR_UNSUPPORTED = -1;
    private static final int HWAUTOTUNE_ERROR_UNVALID_INPUT = -3;
    private static final int HWAUTOTUNE_NO_ERROR = 0;
    private static final boolean HWAUTOTUNE_SUPPORT = SystemProperties.getBoolean("ro.config.smart_voice_mode", false);
    private static final String TAG = "HwAutoTuneImpl";
    private static HwAutoTuneImpl mInstance = null;
    private static boolean mIsLoadSuccess;
    private Context mContext;
    private int mCurrentSampleRate = AUDIO_SAMPLE_RATE_16K;
    private boolean mIsDeinitSuccess = false;
    private boolean mIsInitSuccess = false;
    private boolean mIsSetBuffer = false;
    private boolean mIsSetRefFilePath = false;
    private int mStartCounts = 0;
    private int mSuccessCounts = 0;
    private int mTimes = 0;

    public static class AutoTuneWordDesc {
        private boolean mCorrectFlag;
        private int mCorrectNoteBeginMs;
        private int mCorrectNoteDurationMs;
        private float mCorrectNoteHigh;
        private int mOriginNoteBeginMs;
        private int mOriginNoteDurationMs;
        private float mOriginNoteHigh;
        private String mWordString;

        public String getmWordString() {
            return this.mWordString;
        }

        public void setmWordString(String mWordString2) {
            this.mWordString = mWordString2;
        }

        public boolean ismCorrectFlag() {
            return this.mCorrectFlag;
        }

        public void setmCorrectFlag(boolean mCorrectFlag2) {
            this.mCorrectFlag = mCorrectFlag2;
        }

        public int getmOriginNoteBeginMs() {
            return this.mOriginNoteBeginMs;
        }

        public void setmOriginNoteBeginMs(int mOriginNoteBeginMs2) {
            this.mOriginNoteBeginMs = mOriginNoteBeginMs2;
        }

        public int getmOriginNoteDurationMs() {
            return this.mOriginNoteDurationMs;
        }

        public void setmOriginNoteDurationMs(int mOriginNoteDurationMs2) {
            this.mOriginNoteDurationMs = mOriginNoteDurationMs2;
        }

        public float getmOriginNoteHigh() {
            return this.mOriginNoteHigh;
        }

        public void setmOriginNoteHigh(float mOriginNoteHigh2) {
            this.mOriginNoteHigh = mOriginNoteHigh2;
        }

        public int getmCorrectNoteBeginMs() {
            return this.mCorrectNoteBeginMs;
        }

        public void setmCorrectNoteBeginMs(int mCorrectNoteBeginMs2) {
            this.mCorrectNoteBeginMs = mCorrectNoteBeginMs2;
        }

        public int getmCorrectNoteDurationMs() {
            return this.mCorrectNoteDurationMs;
        }

        public void setmCorrectNoteDurationMs(int mCorrectNoteDurationMs2) {
            this.mCorrectNoteDurationMs = mCorrectNoteDurationMs2;
        }

        public float getmCorrectNoteHigh() {
            return this.mCorrectNoteHigh;
        }

        public void setmCorrectNoteHigh(float mCorrectNoteHigh2) {
            this.mCorrectNoteHigh = mCorrectNoteHigh2;
        }
    }

    private native int nativeDeinit();

    private native String nativeGetParameter(String str);

    private native boolean nativeGetSentenceCorrectFlag(int i);

    private native List<AutoTuneWordDesc> nativeGetSentenceWordDescs(int i);

    private native int nativeInit(int i, int i2, int i3);

    private native int nativeSetAudioBuffer(short[] sArr, int i, int i2, int i3);

    private native int nativeSetConfigFilePath(String str);

    private native int nativeSetParameter(String str);

    private native int nativeSetRefFilePath(String str);

    private native int nativeSetSentenceCorrectFlag(int i, boolean z);

    private native int nativeStart(boolean z);

    private native int nativeStop();

    static {
        mIsLoadSuccess = false;
        try {
            System.loadLibrary("autotune_jni");
            mIsLoadSuccess = true;
            Log.d(TAG, "loadLibrary autotune_jni success");
        } catch (UnsatisfiedLinkError e) {
            mIsLoadSuccess = false;
            Log.e(TAG, "loadLibrary autotune_jni error");
        }
    }

    private HwAutoTuneImpl(Context context) {
        this.mContext = context;
    }

    public static HwAutoTuneImpl createImpl(Context context) {
        HwAutoTuneImpl hwAutoTuneImpl;
        synchronized (HwAutoTuneImpl.class) {
            if (mInstance == null) {
                mInstance = new HwAutoTuneImpl(context);
            }
            hwAutoTuneImpl = mInstance;
        }
        return hwAutoTuneImpl;
    }

    public boolean isSupported() {
        Log.d(TAG, "isSupported");
        return HWAUTOTUNE_SUPPORT;
    }

    public int init(int sampleRate, int channels, int format) {
        if (!HWAUTOTUNE_SUPPORT) {
            return -1;
        }
        if (!mIsLoadSuccess) {
            Log.w(TAG, "loadLibrary autotune_jni error");
            return -4;
        }
        Log.d(TAG, "init  sampleRate:" + sampleRate + "  channels:" + channels + "  format:" + format);
        if (sampleRate != AUDIO_SAMPLE_RATE_16K) {
            Log.w(TAG, "sampleRate unvalid");
            return -3;
        } else if (channels != 1) {
            Log.w(TAG, "channels unvalid");
            return -3;
        } else if (format != 1) {
            Log.w(TAG, "format unvalid");
            return -3;
        } else {
            this.mCurrentSampleRate = sampleRate;
            this.mStartCounts = 0;
            this.mSuccessCounts = 0;
            this.mTimes = 0;
            int ret = nativeInit(sampleRate, channels, format);
            if (ret == 0) {
                this.mIsDeinitSuccess = false;
                this.mIsInitSuccess = true;
            } else {
                Log.w(TAG, "init fail");
                this.mIsInitSuccess = false;
            }
            return ret;
        }
    }

    public int deinit() {
        Log.d(TAG, "deinit");
        if (!HWAUTOTUNE_SUPPORT) {
            return -1;
        }
        if (!this.mIsInitSuccess) {
            Log.w(TAG, "init first");
            return -4;
        } else if (this.mIsDeinitSuccess) {
            Log.w(TAG, "Already deinit");
            return -4;
        } else {
            if (this.mTimes > 0 && this.mStartCounts > 0 && this.mContext != null) {
                IHwMediaMonitor monitor = HwFrameworkFactory.getHwMediaMonitor();
                if (monitor != null) {
                    monitor.writeBigData(907403001, "appName", this.mContext.getPackageName(), 0);
                    monitor.writeBigData(907403001, "useTimes", this.mStartCounts, 0);
                    monitor.writeBigData(907403001, "songTime", this.mTimes, 0);
                    monitor.writeBigData(907403001, "sucTimes", this.mSuccessCounts, 0);
                }
            }
            int ret = nativeDeinit();
            if (ret == 0) {
                this.mIsDeinitSuccess = true;
                this.mIsInitSuccess = false;
                this.mIsSetRefFilePath = false;
                this.mIsSetBuffer = false;
            } else {
                Log.w(TAG, "deinit fail");
                this.mIsDeinitSuccess = false;
            }
            return ret;
        }
    }

    public int setConfigFilePath(String configFilepath) {
        Log.d(TAG, "setConfigFilePath");
        if (!HWAUTOTUNE_SUPPORT) {
            return -1;
        }
        if (!this.mIsInitSuccess) {
            Log.w(TAG, "init first");
            return -4;
        } else if (configFilepath != null && !configFilepath.isEmpty()) {
            return nativeSetConfigFilePath(configFilepath);
        } else {
            Log.w(TAG, "configFilepath unvalid");
            return -3;
        }
    }

    public int setRefFilePath(String refFilepath) {
        Log.d(TAG, "setRefFilePath");
        if (!HWAUTOTUNE_SUPPORT) {
            return -1;
        }
        if (!this.mIsInitSuccess) {
            Log.w(TAG, "init first");
            return -4;
        } else if (refFilepath == null || refFilepath.isEmpty()) {
            Log.w(TAG, "refFilepath unvalid");
            return -3;
        } else {
            int ret = nativeSetRefFilePath(refFilepath);
            if (ret == 0) {
                this.mIsSetRefFilePath = true;
            } else {
                Log.w(TAG, "setRefFilePath fail");
                this.mIsSetRefFilePath = false;
            }
            return ret;
        }
    }

    public int setAudioBuffer(short[] buffer, int buflen, int offsetms, int accompanyShift) {
        Log.d(TAG, "setAudioBuffer  len:" + buflen + "  offsetms:" + offsetms + "  accompanyShift:" + accompanyShift);
        if (!HWAUTOTUNE_SUPPORT) {
            return -1;
        }
        if (!this.mIsSetRefFilePath) {
            Log.w(TAG, "setRefFilePath first");
            return -4;
        } else if (buffer == null || buffer.length <= 0 || buffer.length > BUFFER_SIZE_LEN) {
            Log.w(TAG, "buffer unvalid");
            return -3;
        } else if (buflen <= 0 || buflen > BUFFER_SIZE_LEN) {
            Log.w(TAG, "buffer length unvalid");
            return -3;
        } else {
            int ret = nativeSetAudioBuffer(buffer, buflen, offsetms, accompanyShift);
            if (ret == 0) {
                this.mIsSetBuffer = true;
                this.mTimes = buflen / this.mCurrentSampleRate;
            } else {
                Log.w(TAG, "setAudioBuffer fail");
            }
            return ret;
        }
    }

    public int setParameter(String keyValuePair) {
        Log.d(TAG, "setParameter:" + keyValuePair);
        if (!HWAUTOTUNE_SUPPORT) {
            return -1;
        }
        if (!this.mIsSetBuffer) {
            Log.w(TAG, "setBuffer first");
            return -4;
        } else if (keyValuePair == null || keyValuePair.isEmpty()) {
            Log.w(TAG, "buffer length unvalid");
            return -3;
        } else {
            int ret = nativeSetParameter(keyValuePair);
            if (ret != 0) {
                Log.w(TAG, "setParameter fail");
            }
            return ret;
        }
    }

    public String getParameter(String key) {
        Log.d(TAG, "getParameter:" + key);
        if (!HWAUTOTUNE_SUPPORT) {
            return null;
        }
        if (!this.mIsSetBuffer) {
            Log.w(TAG, "setBuffer first");
            return null;
        } else if (key == null || key.isEmpty()) {
            Log.w(TAG, "setParameter unvalid input");
            return null;
        } else {
            String ret = nativeGetParameter(key);
            if (ret != null) {
                return ret;
            }
            Log.w(TAG, "getParameter fail");
            return null;
        }
    }

    public int start(boolean mode) {
        Log.d(TAG, "start mode:" + mode);
        if (!HWAUTOTUNE_SUPPORT) {
            return -1;
        }
        if (!this.mIsSetBuffer) {
            Log.w(TAG, "setBuffer first");
            return -4;
        }
        int ret = nativeStart(mode);
        this.mStartCounts++;
        if (ret == 0) {
            this.mSuccessCounts++;
            return 0;
        }
        Log.w(TAG, "start fail");
        return ret;
    }

    public int stop() {
        Log.d(TAG, "stop");
        if (!HWAUTOTUNE_SUPPORT) {
            return -1;
        }
        if (!this.mIsInitSuccess) {
            Log.w(TAG, "init first");
            return -4;
        }
        int ret = nativeStop();
        if (ret != 0) {
            Log.w(TAG, "stop fail");
        }
        return ret;
    }

    public int setSentenceCorrectFlag(int sentenceIndex, boolean correctFlag) {
        Log.d(TAG, "setSentenceCorrectFlag  index:" + sentenceIndex + "  flag:" + correctFlag);
        if (!HWAUTOTUNE_SUPPORT) {
            return -1;
        }
        if (!this.mIsSetBuffer) {
            Log.w(TAG, "setBuffer first");
            return -4;
        } else if (sentenceIndex < 0) {
            Log.w(TAG, "sentenceIndex unvalid");
            return -3;
        } else {
            int ret = nativeSetSentenceCorrectFlag(sentenceIndex, correctFlag);
            if (ret != 0) {
                Log.w(TAG, "setSentenceCorrectFlag fail");
            }
            return ret;
        }
    }

    public boolean getSentenceCorrectFlag(int sentenceIndex) {
        Log.d(TAG, "getSentenceCorrectFlag:" + sentenceIndex);
        if (!HWAUTOTUNE_SUPPORT) {
            return false;
        }
        if (!this.mIsSetBuffer) {
            Log.w(TAG, "setBuffer first");
            return false;
        } else if (sentenceIndex >= 0) {
            return nativeGetSentenceCorrectFlag(sentenceIndex);
        } else {
            Log.w(TAG, "getSentenceCorrectFlag unvalid");
            return false;
        }
    }

    public List<AutoTuneWordDesc> getSentenceWordDescs(int sentenceIndex) {
        Log.d(TAG, "getSentenceWordDescs:" + sentenceIndex);
        if (!HWAUTOTUNE_SUPPORT) {
            return null;
        }
        if (!this.mIsSetRefFilePath) {
            Log.w(TAG, "setRefFilePath first");
            return null;
        } else if (sentenceIndex >= 0) {
            return nativeGetSentenceWordDescs(sentenceIndex);
        } else {
            Log.w(TAG, "getSentenceWordDescs unvalid");
            return null;
        }
    }
}
