package android.media.soundtrigger;

import android.hardware.soundtrigger.IRecognitionStatusCallback.Stub;
import android.hardware.soundtrigger.SoundTrigger.GenericRecognitionEvent;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionEvent;
import android.hardware.soundtrigger.SoundTrigger.RecognitionConfig;
import android.media.AudioFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.app.ISoundTriggerService;
import java.io.PrintWriter;
import java.util.UUID;

public final class SoundTriggerDetector {
    private static final boolean DBG = false;
    private static final int MSG_AVAILABILITY_CHANGED = 1;
    private static final int MSG_DETECTION_ERROR = 3;
    private static final int MSG_DETECTION_PAUSE = 4;
    private static final int MSG_DETECTION_RESUME = 5;
    private static final int MSG_SOUND_TRIGGER_DETECTED = 2;
    public static final int RECOGNITION_FLAG_ALLOW_MULTIPLE_TRIGGERS = 2;
    public static final int RECOGNITION_FLAG_CAPTURE_TRIGGER_AUDIO = 1;
    public static final int RECOGNITION_FLAG_NONE = 0;
    private static final String TAG = "SoundTriggerDetector";
    private final Callback mCallback;
    private final Handler mHandler;
    private final Object mLock;
    private final RecognitionCallback mRecognitionCallback;
    private final UUID mSoundModelId;
    private final ISoundTriggerService mSoundTriggerService;

    public static abstract class Callback {
        public abstract void onAvailabilityChanged(int i);

        public abstract void onDetected(EventPayload eventPayload);

        public abstract void onError();

        public abstract void onRecognitionPaused();

        public abstract void onRecognitionResumed();
    }

    public static class EventPayload {
        private final AudioFormat mAudioFormat;
        private final boolean mCaptureAvailable;
        private final int mCaptureSession;
        private final byte[] mData;
        private final boolean mTriggerAvailable;

        private EventPayload(boolean triggerAvailable, boolean captureAvailable, AudioFormat audioFormat, int captureSession, byte[] data) {
            this.mTriggerAvailable = triggerAvailable;
            this.mCaptureAvailable = captureAvailable;
            this.mCaptureSession = captureSession;
            this.mAudioFormat = audioFormat;
            this.mData = data;
        }

        public AudioFormat getCaptureAudioFormat() {
            return this.mAudioFormat;
        }

        public byte[] getTriggerAudio() {
            if (this.mTriggerAvailable) {
                return this.mData;
            }
            return null;
        }

        public Integer getCaptureSession() {
            if (this.mCaptureAvailable) {
                return Integer.valueOf(this.mCaptureSession);
            }
            return null;
        }
    }

    private class MyHandler extends Handler {
        MyHandler() {
        }

        MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (SoundTriggerDetector.this.mCallback == null) {
                Slog.w(SoundTriggerDetector.TAG, "Received message: " + msg.what + " for NULL callback.");
                return;
            }
            switch (msg.what) {
                case SoundTriggerDetector.RECOGNITION_FLAG_ALLOW_MULTIPLE_TRIGGERS /*2*/:
                    SoundTriggerDetector.this.mCallback.onDetected((EventPayload) msg.obj);
                    break;
                case SoundTriggerDetector.MSG_DETECTION_ERROR /*3*/:
                    SoundTriggerDetector.this.mCallback.onError();
                    break;
                case SoundTriggerDetector.MSG_DETECTION_PAUSE /*4*/:
                    SoundTriggerDetector.this.mCallback.onRecognitionPaused();
                    break;
                case SoundTriggerDetector.MSG_DETECTION_RESUME /*5*/:
                    SoundTriggerDetector.this.mCallback.onRecognitionResumed();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private class RecognitionCallback extends Stub {
        private RecognitionCallback() {
        }

        public void onGenericSoundTriggerDetected(GenericRecognitionEvent event) {
            Slog.d(SoundTriggerDetector.TAG, "onGenericSoundTriggerDetected()" + event);
            Message.obtain(SoundTriggerDetector.this.mHandler, SoundTriggerDetector.RECOGNITION_FLAG_ALLOW_MULTIPLE_TRIGGERS, new EventPayload(event.captureAvailable, event.captureFormat, event.captureSession, event.data, null)).sendToTarget();
        }

        public void onKeyphraseDetected(KeyphraseRecognitionEvent event) {
            Slog.e(SoundTriggerDetector.TAG, "Ignoring onKeyphraseDetected() called for " + event);
        }

        public void onError(int status) {
            Slog.d(SoundTriggerDetector.TAG, "onError()" + status);
            SoundTriggerDetector.this.mHandler.sendEmptyMessage(SoundTriggerDetector.MSG_DETECTION_ERROR);
        }

        public void onRecognitionPaused() {
            Slog.d(SoundTriggerDetector.TAG, "onRecognitionPaused()");
            SoundTriggerDetector.this.mHandler.sendEmptyMessage(SoundTriggerDetector.MSG_DETECTION_PAUSE);
        }

        public void onRecognitionResumed() {
            Slog.d(SoundTriggerDetector.TAG, "onRecognitionResumed()");
            SoundTriggerDetector.this.mHandler.sendEmptyMessage(SoundTriggerDetector.MSG_DETECTION_RESUME);
        }
    }

    SoundTriggerDetector(ISoundTriggerService soundTriggerService, UUID soundModelId, Callback callback, Handler handler) {
        this.mLock = new Object();
        this.mSoundTriggerService = soundTriggerService;
        this.mSoundModelId = soundModelId;
        this.mCallback = callback;
        if (handler == null) {
            this.mHandler = new MyHandler();
        } else {
            this.mHandler = new MyHandler(handler.getLooper());
        }
        this.mRecognitionCallback = new RecognitionCallback();
    }

    public boolean startRecognition(int recognitionFlags) {
        try {
            this.mSoundTriggerService.startRecognition(new ParcelUuid(this.mSoundModelId), this.mRecognitionCallback, new RecognitionConfig((recognitionFlags & RECOGNITION_FLAG_CAPTURE_TRIGGER_AUDIO) != 0 ? true : DBG, (recognitionFlags & RECOGNITION_FLAG_ALLOW_MULTIPLE_TRIGGERS) != 0 ? true : DBG, null, null));
            return true;
        } catch (RemoteException e) {
            return DBG;
        }
    }

    public boolean stopRecognition() {
        try {
            this.mSoundTriggerService.stopRecognition(new ParcelUuid(this.mSoundModelId), this.mRecognitionCallback);
            return true;
        } catch (RemoteException e) {
            return DBG;
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        synchronized (this.mLock) {
        }
    }
}
