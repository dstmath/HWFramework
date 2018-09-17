package android.service.voice;

import android.content.Intent;
import android.hardware.soundtrigger.IRecognitionStatusCallback.Stub;
import android.hardware.soundtrigger.KeyphraseEnrollmentInfo;
import android.hardware.soundtrigger.KeyphraseMetadata;
import android.hardware.soundtrigger.SoundTrigger.ConfidenceLevel;
import android.hardware.soundtrigger.SoundTrigger.GenericRecognitionEvent;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionEvent;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionExtra;
import android.hardware.soundtrigger.SoundTrigger.ModuleProperties;
import android.hardware.soundtrigger.SoundTrigger.RecognitionConfig;
import android.media.AudioFormat;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.app.IVoiceInteractionManagerService;
import java.io.PrintWriter;
import java.util.Locale;

public class AlwaysOnHotwordDetector {
    static final boolean DBG = false;
    public static final int MANAGE_ACTION_ENROLL = 0;
    public static final int MANAGE_ACTION_RE_ENROLL = 1;
    public static final int MANAGE_ACTION_UN_ENROLL = 2;
    private static final int MSG_AVAILABILITY_CHANGED = 1;
    private static final int MSG_DETECTION_ERROR = 3;
    private static final int MSG_DETECTION_PAUSE = 4;
    private static final int MSG_DETECTION_RESUME = 5;
    private static final int MSG_HOTWORD_DETECTED = 2;
    public static final int RECOGNITION_FLAG_ALLOW_MULTIPLE_TRIGGERS = 2;
    public static final int RECOGNITION_FLAG_CAPTURE_TRIGGER_AUDIO = 1;
    public static final int RECOGNITION_FLAG_NONE = 0;
    public static final int RECOGNITION_MODE_USER_IDENTIFICATION = 2;
    public static final int RECOGNITION_MODE_VOICE_TRIGGER = 1;
    public static final int STATE_HARDWARE_UNAVAILABLE = -2;
    private static final int STATE_INVALID = -3;
    public static final int STATE_KEYPHRASE_ENROLLED = 2;
    public static final int STATE_KEYPHRASE_UNENROLLED = 1;
    public static final int STATE_KEYPHRASE_UNSUPPORTED = -1;
    private static final int STATE_NOT_READY = 0;
    private static final int STATUS_ERROR = Integer.MIN_VALUE;
    private static final int STATUS_OK = 0;
    static final String TAG = "AlwaysOnHotwordDetector";
    private int mAvailability;
    private final Callback mExternalCallback;
    private final Handler mHandler;
    private final SoundTriggerListener mInternalCallback;
    private final KeyphraseEnrollmentInfo mKeyphraseEnrollmentInfo;
    private final KeyphraseMetadata mKeyphraseMetadata;
    private final Locale mLocale;
    private final Object mLock;
    private final IVoiceInteractionManagerService mModelManagementService;
    private final String mText;
    private final IVoiceInteractionService mVoiceInteractionService;

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

    class MyHandler extends Handler {
        MyHandler() {
        }

        public void handleMessage(Message msg) {
            synchronized (AlwaysOnHotwordDetector.this.mLock) {
                if (AlwaysOnHotwordDetector.this.mAvailability == AlwaysOnHotwordDetector.STATE_INVALID) {
                    Slog.w(AlwaysOnHotwordDetector.TAG, "Received message: " + msg.what + " for an invalid detector");
                    return;
                }
                switch (msg.what) {
                    case AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNENROLLED /*1*/:
                        AlwaysOnHotwordDetector.this.mExternalCallback.onAvailabilityChanged(msg.arg1);
                        break;
                    case AlwaysOnHotwordDetector.STATE_KEYPHRASE_ENROLLED /*2*/:
                        AlwaysOnHotwordDetector.this.mExternalCallback.onDetected((EventPayload) msg.obj);
                        break;
                    case AlwaysOnHotwordDetector.MSG_DETECTION_ERROR /*3*/:
                        AlwaysOnHotwordDetector.this.mExternalCallback.onError();
                        break;
                    case AlwaysOnHotwordDetector.MSG_DETECTION_PAUSE /*4*/:
                        AlwaysOnHotwordDetector.this.mExternalCallback.onRecognitionPaused();
                        break;
                    case AlwaysOnHotwordDetector.MSG_DETECTION_RESUME /*5*/:
                        AlwaysOnHotwordDetector.this.mExternalCallback.onRecognitionResumed();
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
            }
        }
    }

    class RefreshAvailabiltyTask extends AsyncTask<Void, Void, Void> {
        RefreshAvailabiltyTask() {
        }

        public Void doInBackground(Void... params) {
            int availability = internalGetInitialAvailability();
            if (!(availability == 0 || availability == AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNENROLLED)) {
                if (availability == AlwaysOnHotwordDetector.STATE_KEYPHRASE_ENROLLED) {
                }
                synchronized (AlwaysOnHotwordDetector.this.mLock) {
                    AlwaysOnHotwordDetector.this.mAvailability = availability;
                    AlwaysOnHotwordDetector.this.notifyStateChangedLocked();
                }
                return null;
            }
            if (internalGetIsEnrolled(AlwaysOnHotwordDetector.this.mKeyphraseMetadata.id, AlwaysOnHotwordDetector.this.mLocale)) {
                availability = AlwaysOnHotwordDetector.STATE_KEYPHRASE_ENROLLED;
            } else {
                availability = AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNENROLLED;
            }
            synchronized (AlwaysOnHotwordDetector.this.mLock) {
                AlwaysOnHotwordDetector.this.mAvailability = availability;
                AlwaysOnHotwordDetector.this.notifyStateChangedLocked();
            }
            return null;
        }

        private int internalGetInitialAvailability() {
            synchronized (AlwaysOnHotwordDetector.this.mLock) {
                if (AlwaysOnHotwordDetector.this.mAvailability == AlwaysOnHotwordDetector.STATE_INVALID) {
                    return AlwaysOnHotwordDetector.STATE_INVALID;
                }
                ModuleProperties dspModuleProperties = null;
                try {
                    dspModuleProperties = AlwaysOnHotwordDetector.this.mModelManagementService.getDspModuleProperties(AlwaysOnHotwordDetector.this.mVoiceInteractionService);
                } catch (RemoteException e) {
                    Slog.w(AlwaysOnHotwordDetector.TAG, "RemoteException in getDspProperties!", e);
                }
                if (dspModuleProperties == null) {
                    return AlwaysOnHotwordDetector.STATE_HARDWARE_UNAVAILABLE;
                }
                if (AlwaysOnHotwordDetector.this.mKeyphraseMetadata == null) {
                    return AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNSUPPORTED;
                }
                return AlwaysOnHotwordDetector.STATUS_OK;
            }
        }

        private boolean internalGetIsEnrolled(int keyphraseId, Locale locale) {
            try {
                return AlwaysOnHotwordDetector.this.mModelManagementService.isEnrolledForKeyphrase(AlwaysOnHotwordDetector.this.mVoiceInteractionService, keyphraseId, locale.toLanguageTag());
            } catch (RemoteException e) {
                Slog.w(AlwaysOnHotwordDetector.TAG, "RemoteException in listRegisteredKeyphraseSoundModels!", e);
                return AlwaysOnHotwordDetector.DBG;
            }
        }
    }

    static final class SoundTriggerListener extends Stub {
        private final Handler mHandler;

        public SoundTriggerListener(Handler handler) {
            this.mHandler = handler;
        }

        public void onKeyphraseDetected(KeyphraseRecognitionEvent event) {
            Slog.i(AlwaysOnHotwordDetector.TAG, "onDetected");
            Message.obtain(this.mHandler, AlwaysOnHotwordDetector.STATE_KEYPHRASE_ENROLLED, new EventPayload(event.captureAvailable, event.captureFormat, event.captureSession, event.data, null)).sendToTarget();
        }

        public void onGenericSoundTriggerDetected(GenericRecognitionEvent event) {
            Slog.w(AlwaysOnHotwordDetector.TAG, "Generic sound trigger event detected at AOHD: " + event);
        }

        public void onError(int status) {
            Slog.i(AlwaysOnHotwordDetector.TAG, "onError: " + status);
            this.mHandler.sendEmptyMessage(AlwaysOnHotwordDetector.MSG_DETECTION_ERROR);
        }

        public void onRecognitionPaused() {
            Slog.i(AlwaysOnHotwordDetector.TAG, "onRecognitionPaused");
            this.mHandler.sendEmptyMessage(AlwaysOnHotwordDetector.MSG_DETECTION_PAUSE);
        }

        public void onRecognitionResumed() {
            Slog.i(AlwaysOnHotwordDetector.TAG, "onRecognitionResumed");
            this.mHandler.sendEmptyMessage(AlwaysOnHotwordDetector.MSG_DETECTION_RESUME);
        }
    }

    public AlwaysOnHotwordDetector(String text, Locale locale, Callback callback, KeyphraseEnrollmentInfo keyphraseEnrollmentInfo, IVoiceInteractionService voiceInteractionService, IVoiceInteractionManagerService modelManagementService) {
        this.mLock = new Object();
        this.mAvailability = STATUS_OK;
        this.mText = text;
        this.mLocale = locale;
        this.mKeyphraseEnrollmentInfo = keyphraseEnrollmentInfo;
        this.mKeyphraseMetadata = this.mKeyphraseEnrollmentInfo.getKeyphraseMetadata(text, locale);
        this.mExternalCallback = callback;
        this.mHandler = new MyHandler();
        this.mInternalCallback = new SoundTriggerListener(this.mHandler);
        this.mVoiceInteractionService = voiceInteractionService;
        this.mModelManagementService = modelManagementService;
        new RefreshAvailabiltyTask().execute((Object[]) new Void[STATUS_OK]);
    }

    public int getSupportedRecognitionModes() {
        int supportedRecognitionModesLocked;
        synchronized (this.mLock) {
            supportedRecognitionModesLocked = getSupportedRecognitionModesLocked();
        }
        return supportedRecognitionModesLocked;
    }

    private int getSupportedRecognitionModesLocked() {
        if (this.mAvailability == STATE_INVALID) {
            throw new IllegalStateException("getSupportedRecognitionModes called on an invalid detector");
        } else if (this.mAvailability == STATE_KEYPHRASE_ENROLLED || this.mAvailability == STATE_KEYPHRASE_UNENROLLED) {
            return this.mKeyphraseMetadata.recognitionModeFlags;
        } else {
            throw new UnsupportedOperationException("Getting supported recognition modes for the keyphrase is not supported");
        }
    }

    public boolean startRecognition(int recognitionFlags) {
        boolean z = DBG;
        synchronized (this.mLock) {
            if (this.mAvailability == STATE_INVALID) {
                throw new IllegalStateException("startRecognition called on an invalid detector");
            } else if (this.mAvailability != STATE_KEYPHRASE_ENROLLED) {
                throw new UnsupportedOperationException("Recognition for the given keyphrase is not supported");
            } else {
                if (startRecognitionLocked(recognitionFlags) == 0) {
                    z = true;
                }
            }
        }
        return z;
    }

    public boolean stopRecognition() {
        boolean z = DBG;
        synchronized (this.mLock) {
            if (this.mAvailability == STATE_INVALID) {
                throw new IllegalStateException("stopRecognition called on an invalid detector");
            } else if (this.mAvailability != STATE_KEYPHRASE_ENROLLED) {
                throw new UnsupportedOperationException("Recognition for the given keyphrase is not supported");
            } else {
                if (stopRecognitionLocked() == 0) {
                    z = true;
                }
            }
        }
        return z;
    }

    public Intent createEnrollIntent() {
        Intent manageIntentLocked;
        synchronized (this.mLock) {
            manageIntentLocked = getManageIntentLocked(STATUS_OK);
        }
        return manageIntentLocked;
    }

    public Intent createUnEnrollIntent() {
        Intent manageIntentLocked;
        synchronized (this.mLock) {
            manageIntentLocked = getManageIntentLocked(STATE_KEYPHRASE_ENROLLED);
        }
        return manageIntentLocked;
    }

    public Intent createReEnrollIntent() {
        Intent manageIntentLocked;
        synchronized (this.mLock) {
            manageIntentLocked = getManageIntentLocked(STATE_KEYPHRASE_UNENROLLED);
        }
        return manageIntentLocked;
    }

    private Intent getManageIntentLocked(int action) {
        if (this.mAvailability == STATE_INVALID) {
            throw new IllegalStateException("getManageIntent called on an invalid detector");
        } else if (this.mAvailability == STATE_KEYPHRASE_ENROLLED || this.mAvailability == STATE_KEYPHRASE_UNENROLLED) {
            return this.mKeyphraseEnrollmentInfo.getManageKeyphraseIntent(action, this.mText, this.mLocale);
        } else {
            throw new UnsupportedOperationException("Managing the given keyphrase is not supported");
        }
    }

    void invalidate() {
        synchronized (this.mLock) {
            this.mAvailability = STATE_INVALID;
            notifyStateChangedLocked();
        }
    }

    void onSoundModelsChanged() {
        synchronized (this.mLock) {
            if (!(this.mAvailability == STATE_INVALID || this.mAvailability == STATE_HARDWARE_UNAVAILABLE)) {
                if (this.mAvailability != STATE_KEYPHRASE_UNSUPPORTED) {
                    stopRecognitionLocked();
                    new RefreshAvailabiltyTask().execute((Object[]) new Void[STATUS_OK]);
                    return;
                }
            }
            Slog.w(TAG, "Received onSoundModelsChanged for an unsupported keyphrase/config");
        }
    }

    private int startRecognitionLocked(int recognitionFlags) {
        KeyphraseRecognitionExtra[] recognitionExtra = new KeyphraseRecognitionExtra[STATE_KEYPHRASE_UNENROLLED];
        recognitionExtra[STATUS_OK] = new KeyphraseRecognitionExtra(this.mKeyphraseMetadata.id, this.mKeyphraseMetadata.recognitionModeFlags, STATUS_OK, new ConfidenceLevel[STATUS_OK]);
        boolean captureTriggerAudio = (recognitionFlags & STATE_KEYPHRASE_UNENROLLED) != 0 ? true : DBG;
        boolean allowMultipleTriggers = (recognitionFlags & STATE_KEYPHRASE_ENROLLED) != 0 ? true : DBG;
        int code = STATUS_ERROR;
        try {
            code = this.mModelManagementService.startRecognition(this.mVoiceInteractionService, this.mKeyphraseMetadata.id, this.mLocale.toLanguageTag(), this.mInternalCallback, new RecognitionConfig(captureTriggerAudio, allowMultipleTriggers, recognitionExtra, null));
        } catch (RemoteException e) {
            Slog.w(TAG, "RemoteException in startRecognition!", e);
        }
        if (code != 0) {
            Slog.w(TAG, "startRecognition() failed with error code " + code);
        }
        return code;
    }

    private int stopRecognitionLocked() {
        int code = STATUS_ERROR;
        try {
            code = this.mModelManagementService.stopRecognition(this.mVoiceInteractionService, this.mKeyphraseMetadata.id, this.mInternalCallback);
        } catch (RemoteException e) {
            Slog.w(TAG, "RemoteException in stopRecognition!", e);
        }
        if (code != 0) {
            Slog.w(TAG, "stopRecognition() failed with error code " + code);
        }
        return code;
    }

    private void notifyStateChangedLocked() {
        Message message = Message.obtain(this.mHandler, (int) STATE_KEYPHRASE_UNENROLLED);
        message.arg1 = this.mAvailability;
        message.sendToTarget();
    }

    public void dump(String prefix, PrintWriter pw) {
        synchronized (this.mLock) {
            pw.print(prefix);
            pw.print("Text=");
            pw.println(this.mText);
            pw.print(prefix);
            pw.print("Locale=");
            pw.println(this.mLocale);
            pw.print(prefix);
            pw.print("Availability=");
            pw.println(this.mAvailability);
            pw.print(prefix);
            pw.print("KeyphraseMetadata=");
            pw.println(this.mKeyphraseMetadata);
            pw.print(prefix);
            pw.print("EnrollmentInfo=");
            pw.println(this.mKeyphraseEnrollmentInfo);
        }
    }
}
