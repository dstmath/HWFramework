package android.speech.tts;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.Settings;
import android.speech.tts.ITextToSpeechService;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

public abstract class TextToSpeechService extends Service {
    private static final boolean DBG = false;
    private static final String SYNTH_THREAD_NAME = "SynthThread";
    private static final String TAG = "TextToSpeechService";
    private AudioPlaybackHandler mAudioPlaybackHandler;
    private final ITextToSpeechService.Stub mBinder = new ITextToSpeechService.Stub() {
        /* class android.speech.tts.TextToSpeechService.AnonymousClass1 */

        @Override // android.speech.tts.ITextToSpeechService
        public int speak(IBinder caller, CharSequence text, int queueMode, Bundle params, String utteranceId) {
            if (!checkNonNull(caller, text, params)) {
                return -1;
            }
            return TextToSpeechService.this.mSynthHandler.enqueueSpeechItem(queueMode, new SynthesisSpeechItem(caller, Binder.getCallingUid(), Binder.getCallingPid(), params, utteranceId, text));
        }

        @Override // android.speech.tts.ITextToSpeechService
        public int synthesizeToFileDescriptor(IBinder caller, CharSequence text, ParcelFileDescriptor fileDescriptor, Bundle params, String utteranceId) {
            if (!checkNonNull(caller, text, fileDescriptor, params)) {
                return -1;
            }
            return TextToSpeechService.this.mSynthHandler.enqueueSpeechItem(1, new SynthesisToFileOutputStreamSpeechItem(caller, Binder.getCallingUid(), Binder.getCallingPid(), params, utteranceId, text, new ParcelFileDescriptor.AutoCloseOutputStream(ParcelFileDescriptor.adoptFd(fileDescriptor.detachFd()))));
        }

        @Override // android.speech.tts.ITextToSpeechService
        public int playAudio(IBinder caller, Uri audioUri, int queueMode, Bundle params, String utteranceId) {
            if (!checkNonNull(caller, audioUri, params)) {
                return -1;
            }
            return TextToSpeechService.this.mSynthHandler.enqueueSpeechItem(queueMode, new AudioSpeechItem(caller, Binder.getCallingUid(), Binder.getCallingPid(), params, utteranceId, audioUri));
        }

        @Override // android.speech.tts.ITextToSpeechService
        public int playSilence(IBinder caller, long duration, int queueMode, String utteranceId) {
            if (!checkNonNull(caller)) {
                return -1;
            }
            return TextToSpeechService.this.mSynthHandler.enqueueSpeechItem(queueMode, new SilenceSpeechItem(caller, Binder.getCallingUid(), Binder.getCallingPid(), utteranceId, duration));
        }

        @Override // android.speech.tts.ITextToSpeechService
        public boolean isSpeaking() {
            return TextToSpeechService.this.mSynthHandler.isSpeaking() || TextToSpeechService.this.mAudioPlaybackHandler.isSpeaking();
        }

        @Override // android.speech.tts.ITextToSpeechService
        public int stop(IBinder caller) {
            if (!checkNonNull(caller)) {
                return -1;
            }
            return TextToSpeechService.this.mSynthHandler.stopForApp(caller);
        }

        @Override // android.speech.tts.ITextToSpeechService
        public String[] getLanguage() {
            return TextToSpeechService.this.onGetLanguage();
        }

        @Override // android.speech.tts.ITextToSpeechService
        public String[] getClientDefaultLanguage() {
            return TextToSpeechService.this.getSettingsLocale();
        }

        @Override // android.speech.tts.ITextToSpeechService
        public int isLanguageAvailable(String lang, String country, String variant) {
            if (!checkNonNull(lang)) {
                return -1;
            }
            return TextToSpeechService.this.onIsLanguageAvailable(lang, country, variant);
        }

        @Override // android.speech.tts.ITextToSpeechService
        public String[] getFeaturesForLanguage(String lang, String country, String variant) {
            Set<String> features = TextToSpeechService.this.onGetFeaturesForLanguage(lang, country, variant);
            if (features == null) {
                return new String[0];
            }
            String[] featuresArray = new String[features.size()];
            features.toArray(featuresArray);
            return featuresArray;
        }

        @Override // android.speech.tts.ITextToSpeechService
        public int loadLanguage(IBinder caller, String lang, String country, String variant) {
            if (!checkNonNull(lang)) {
                return -1;
            }
            int retVal = TextToSpeechService.this.onIsLanguageAvailable(lang, country, variant);
            if ((retVal == 0 || retVal == 1 || retVal == 2) && TextToSpeechService.this.mSynthHandler.enqueueSpeechItem(1, new LoadLanguageItem(caller, Binder.getCallingUid(), Binder.getCallingPid(), lang, country, variant)) != 0) {
                return -1;
            }
            return retVal;
        }

        @Override // android.speech.tts.ITextToSpeechService
        public List<Voice> getVoices() {
            return TextToSpeechService.this.onGetVoices();
        }

        @Override // android.speech.tts.ITextToSpeechService
        public int loadVoice(IBinder caller, String voiceName) {
            if (!checkNonNull(voiceName)) {
                return -1;
            }
            int retVal = TextToSpeechService.this.onIsValidVoiceName(voiceName);
            if (retVal != 0 || TextToSpeechService.this.mSynthHandler.enqueueSpeechItem(1, new LoadVoiceItem(caller, Binder.getCallingUid(), Binder.getCallingPid(), voiceName)) == 0) {
                return retVal;
            }
            return -1;
        }

        @Override // android.speech.tts.ITextToSpeechService
        public String getDefaultVoiceNameFor(String lang, String country, String variant) {
            if (!checkNonNull(lang)) {
                return null;
            }
            int retVal = TextToSpeechService.this.onIsLanguageAvailable(lang, country, variant);
            if (retVal == 0 || retVal == 1 || retVal == 2) {
                return TextToSpeechService.this.onGetDefaultVoiceNameFor(lang, country, variant);
            }
            return null;
        }

        @Override // android.speech.tts.ITextToSpeechService
        public void setCallback(IBinder caller, ITextToSpeechCallback cb) {
            if (checkNonNull(caller)) {
                TextToSpeechService.this.mCallbacks.setCallback(caller, cb);
            }
        }

        private String intern(String in) {
            return in.intern();
        }

        private boolean checkNonNull(Object... args) {
            for (Object o : args) {
                if (o == null) {
                    return false;
                }
            }
            return true;
        }
    };
    private CallbackMap mCallbacks;
    private TtsEngines mEngineHelper;
    private String mPackageName;
    private SynthHandler mSynthHandler;
    private final Object mVoicesInfoLock = new Object();

    /* access modifiers changed from: package-private */
    public interface UtteranceProgressDispatcher {
        void dispatchOnAudioAvailable(byte[] bArr);

        void dispatchOnBeginSynthesis(int i, int i2, int i3);

        void dispatchOnError(int i);

        void dispatchOnRangeStart(int i, int i2, int i3);

        void dispatchOnStart();

        void dispatchOnStop();

        void dispatchOnSuccess();
    }

    /* access modifiers changed from: protected */
    public abstract String[] onGetLanguage();

    /* access modifiers changed from: protected */
    public abstract int onIsLanguageAvailable(String str, String str2, String str3);

    /* access modifiers changed from: protected */
    public abstract int onLoadLanguage(String str, String str2, String str3);

    /* access modifiers changed from: protected */
    public abstract void onStop();

    /* access modifiers changed from: protected */
    public abstract void onSynthesizeText(SynthesisRequest synthesisRequest, SynthesisCallback synthesisCallback);

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        SynthThread synthThread = new SynthThread();
        synthThread.start();
        this.mSynthHandler = new SynthHandler(synthThread.getLooper());
        this.mAudioPlaybackHandler = new AudioPlaybackHandler();
        this.mAudioPlaybackHandler.start();
        this.mEngineHelper = new TtsEngines(this);
        this.mCallbacks = new CallbackMap();
        this.mPackageName = getApplicationInfo().packageName;
        String[] defaultLocale = getSettingsLocale();
        onLoadLanguage(defaultLocale[0], defaultLocale[1], defaultLocale[2]);
    }

    @Override // android.app.Service
    public void onDestroy() {
        this.mSynthHandler.quit();
        this.mAudioPlaybackHandler.quit();
        this.mCallbacks.kill();
        super.onDestroy();
    }

    /* access modifiers changed from: protected */
    public Set<String> onGetFeaturesForLanguage(String lang, String country, String variant) {
        return new HashSet();
    }

    private int getExpectedLanguageAvailableStatus(Locale locale) {
        if (!locale.getVariant().isEmpty()) {
            return 2;
        }
        if (locale.getCountry().isEmpty()) {
            return 0;
        }
        return 1;
    }

    public List<Voice> onGetVoices() {
        TextToSpeechService textToSpeechService = this;
        ArrayList<Voice> voices = new ArrayList<>();
        Locale[] availableLocales = Locale.getAvailableLocales();
        int length = availableLocales.length;
        int i = 0;
        while (i < length) {
            Locale locale = availableLocales[i];
            try {
                if (textToSpeechService.onIsLanguageAvailable(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant()) == textToSpeechService.getExpectedLanguageAvailableStatus(locale)) {
                    voices.add(new Voice(textToSpeechService.onGetDefaultVoiceNameFor(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant()), locale, 300, 300, false, textToSpeechService.onGetFeaturesForLanguage(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant())));
                }
            } catch (MissingResourceException e) {
            }
            i++;
            textToSpeechService = this;
        }
        return voices;
    }

    public String onGetDefaultVoiceNameFor(String lang, String country, String variant) {
        Locale iso3Locale;
        int localeStatus = onIsLanguageAvailable(lang, country, variant);
        if (localeStatus == 0) {
            iso3Locale = new Locale(lang);
        } else if (localeStatus == 1) {
            iso3Locale = new Locale(lang, country);
        } else if (localeStatus != 2) {
            return null;
        } else {
            iso3Locale = new Locale(lang, country, variant);
        }
        String voiceName = TtsEngines.normalizeTTSLocale(iso3Locale).toLanguageTag();
        if (onIsValidVoiceName(voiceName) == 0) {
            return voiceName;
        }
        return null;
    }

    public int onLoadVoice(String voiceName) {
        Locale locale = Locale.forLanguageTag(voiceName);
        if (locale == null) {
            return -1;
        }
        try {
            if (onIsLanguageAvailable(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant()) != getExpectedLanguageAvailableStatus(locale)) {
                return -1;
            }
            onLoadLanguage(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant());
            return 0;
        } catch (MissingResourceException e) {
            return -1;
        }
    }

    public int onIsValidVoiceName(String voiceName) {
        Locale locale = Locale.forLanguageTag(voiceName);
        if (locale == null) {
            return -1;
        }
        try {
            if (onIsLanguageAvailable(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant()) != getExpectedLanguageAvailableStatus(locale)) {
                return -1;
            }
            return 0;
        } catch (MissingResourceException e) {
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDefaultSpeechRate() {
        return getSecureSettingInt(Settings.Secure.TTS_DEFAULT_RATE, 100);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDefaultPitch() {
        return getSecureSettingInt(Settings.Secure.TTS_DEFAULT_PITCH, 100);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String[] getSettingsLocale() {
        return TtsEngines.toOldLocaleStringFormat(this.mEngineHelper.getLocalePrefForEngine(this.mPackageName));
    }

    private int getSecureSettingInt(String name, int defaultValue) {
        return Settings.Secure.getInt(getContentResolver(), name, defaultValue);
    }

    private class SynthThread extends HandlerThread implements MessageQueue.IdleHandler {
        private boolean mFirstIdle = true;

        public SynthThread() {
            super(TextToSpeechService.SYNTH_THREAD_NAME, 0);
        }

        /* access modifiers changed from: protected */
        @Override // android.os.HandlerThread
        public void onLooperPrepared() {
            getLooper().getQueue().addIdleHandler(this);
        }

        @Override // android.os.MessageQueue.IdleHandler
        public boolean queueIdle() {
            if (this.mFirstIdle) {
                this.mFirstIdle = false;
                return true;
            }
            broadcastTtsQueueProcessingCompleted();
            return true;
        }

        private void broadcastTtsQueueProcessingCompleted() {
            TextToSpeechService.this.sendBroadcast(new Intent(TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED));
        }
    }

    /* access modifiers changed from: private */
    public class SynthHandler extends Handler {
        private SpeechItem mCurrentSpeechItem = null;
        private int mFlushAll = 0;
        private List<Object> mFlushedObjects = new ArrayList();

        public SynthHandler(Looper looper) {
            super(looper);
        }

        private void startFlushingSpeechItems(Object callerIdentity) {
            synchronized (this.mFlushedObjects) {
                if (callerIdentity == null) {
                    this.mFlushAll++;
                } else {
                    this.mFlushedObjects.add(callerIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void endFlushingSpeechItems(Object callerIdentity) {
            synchronized (this.mFlushedObjects) {
                if (callerIdentity == null) {
                    this.mFlushAll--;
                } else {
                    this.mFlushedObjects.remove(callerIdentity);
                }
            }
        }

        private boolean isFlushed(SpeechItem speechItem) {
            boolean z;
            synchronized (this.mFlushedObjects) {
                if (this.mFlushAll <= 0) {
                    if (!this.mFlushedObjects.contains(speechItem.getCallerIdentity())) {
                        z = false;
                    }
                }
                z = true;
            }
            return z;
        }

        private synchronized SpeechItem getCurrentSpeechItem() {
            return this.mCurrentSpeechItem;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private synchronized boolean setCurrentSpeechItem(SpeechItem speechItem) {
            if (speechItem != null) {
                if (isFlushed(speechItem)) {
                    return false;
                }
            }
            this.mCurrentSpeechItem = speechItem;
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private synchronized SpeechItem removeCurrentSpeechItem() {
            SpeechItem current;
            current = this.mCurrentSpeechItem;
            this.mCurrentSpeechItem = null;
            return current;
        }

        private synchronized SpeechItem maybeRemoveCurrentSpeechItem(Object callerIdentity) {
            if (this.mCurrentSpeechItem == null || this.mCurrentSpeechItem.getCallerIdentity() != callerIdentity) {
                return null;
            }
            SpeechItem current = this.mCurrentSpeechItem;
            this.mCurrentSpeechItem = null;
            return current;
        }

        public boolean isSpeaking() {
            return getCurrentSpeechItem() != null;
        }

        public void quit() {
            getLooper().quit();
            SpeechItem current = removeCurrentSpeechItem();
            if (current != null) {
                current.stop();
            }
        }

        public int enqueueSpeechItem(int queueMode, final SpeechItem speechItem) {
            UtteranceProgressDispatcher utterenceProgress = null;
            if (speechItem instanceof UtteranceProgressDispatcher) {
                utterenceProgress = (UtteranceProgressDispatcher) speechItem;
            }
            if (!speechItem.isValid()) {
                if (utterenceProgress != null) {
                    utterenceProgress.dispatchOnError(-8);
                }
                return -1;
            }
            if (queueMode == 0) {
                stopForApp(speechItem.getCallerIdentity());
            } else if (queueMode == 2) {
                stopAll();
            }
            Message msg = Message.obtain(this, new Runnable() {
                /* class android.speech.tts.TextToSpeechService.SynthHandler.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    if (SynthHandler.this.setCurrentSpeechItem(speechItem)) {
                        speechItem.play();
                        SynthHandler.this.removeCurrentSpeechItem();
                        return;
                    }
                    speechItem.stop();
                }
            });
            msg.obj = speechItem.getCallerIdentity();
            if (sendMessage(msg)) {
                return 0;
            }
            Log.w(TextToSpeechService.TAG, "SynthThread has quit");
            if (utterenceProgress != null) {
                utterenceProgress.dispatchOnError(-4);
            }
            return -1;
        }

        public int stopForApp(final Object callerIdentity) {
            if (callerIdentity == null) {
                return -1;
            }
            startFlushingSpeechItems(callerIdentity);
            SpeechItem current = maybeRemoveCurrentSpeechItem(callerIdentity);
            if (current != null) {
                current.stop();
            }
            TextToSpeechService.this.mAudioPlaybackHandler.stopForApp(callerIdentity);
            sendMessage(Message.obtain(this, new Runnable() {
                /* class android.speech.tts.TextToSpeechService.SynthHandler.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    SynthHandler.this.endFlushingSpeechItems(callerIdentity);
                }
            }));
            return 0;
        }

        public int stopAll() {
            startFlushingSpeechItems(null);
            SpeechItem current = removeCurrentSpeechItem();
            if (current != null) {
                current.stop();
            }
            TextToSpeechService.this.mAudioPlaybackHandler.stop();
            sendMessage(Message.obtain(this, new Runnable() {
                /* class android.speech.tts.TextToSpeechService.SynthHandler.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    SynthHandler.this.endFlushingSpeechItems(null);
                }
            }));
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public static class AudioOutputParams {
        public final AudioAttributes mAudioAttributes;
        public final float mPan;
        public final int mSessionId;
        public final float mVolume;

        AudioOutputParams() {
            this.mSessionId = 0;
            this.mVolume = 1.0f;
            this.mPan = 0.0f;
            this.mAudioAttributes = null;
        }

        AudioOutputParams(int sessionId, float volume, float pan, AudioAttributes audioAttributes) {
            this.mSessionId = sessionId;
            this.mVolume = volume;
            this.mPan = pan;
            this.mAudioAttributes = audioAttributes;
        }

        static AudioOutputParams createFromParamsBundle(Bundle paramsBundle, boolean isSpeech) {
            int i;
            if (paramsBundle == null) {
                return new AudioOutputParams();
            }
            AudioAttributes audioAttributes = (AudioAttributes) paramsBundle.getParcelable(TextToSpeech.Engine.KEY_PARAM_AUDIO_ATTRIBUTES);
            if (audioAttributes == null) {
                AudioAttributes.Builder legacyStreamType = new AudioAttributes.Builder().setLegacyStreamType(paramsBundle.getInt(TextToSpeech.Engine.KEY_PARAM_STREAM, 3));
                if (isSpeech) {
                    i = 1;
                } else {
                    i = 4;
                }
                audioAttributes = legacyStreamType.setContentType(i).build();
            }
            return new AudioOutputParams(paramsBundle.getInt(TextToSpeech.Engine.KEY_PARAM_SESSION_ID, 0), paramsBundle.getFloat("volume", 1.0f), paramsBundle.getFloat(TextToSpeech.Engine.KEY_PARAM_PAN, 0.0f), audioAttributes);
        }
    }

    /* access modifiers changed from: private */
    public abstract class SpeechItem {
        private final Object mCallerIdentity;
        private final int mCallerPid;
        private final int mCallerUid;
        private boolean mStarted = false;
        private boolean mStopped = false;

        public abstract boolean isValid();

        /* access modifiers changed from: protected */
        public abstract void playImpl();

        /* access modifiers changed from: protected */
        public abstract void stopImpl();

        public SpeechItem(Object caller, int callerUid, int callerPid) {
            this.mCallerIdentity = caller;
            this.mCallerUid = callerUid;
            this.mCallerPid = callerPid;
        }

        public Object getCallerIdentity() {
            return this.mCallerIdentity;
        }

        public int getCallerUid() {
            return this.mCallerUid;
        }

        public int getCallerPid() {
            return this.mCallerPid;
        }

        public void play() {
            synchronized (this) {
                if (!this.mStarted) {
                    this.mStarted = true;
                } else {
                    throw new IllegalStateException("play() called twice");
                }
            }
            playImpl();
        }

        public void stop() {
            synchronized (this) {
                if (!this.mStopped) {
                    this.mStopped = true;
                } else {
                    throw new IllegalStateException("stop() called twice");
                }
            }
            stopImpl();
        }

        /* access modifiers changed from: protected */
        public synchronized boolean isStopped() {
            return this.mStopped;
        }

        /* access modifiers changed from: protected */
        public synchronized boolean isStarted() {
            return this.mStarted;
        }
    }

    private abstract class UtteranceSpeechItem extends SpeechItem implements UtteranceProgressDispatcher {
        public abstract String getUtteranceId();

        public UtteranceSpeechItem(Object caller, int callerUid, int callerPid) {
            super(caller, callerUid, callerPid);
        }

        @Override // android.speech.tts.TextToSpeechService.UtteranceProgressDispatcher
        public void dispatchOnSuccess() {
            String utteranceId = getUtteranceId();
            if (utteranceId != null) {
                TextToSpeechService.this.mCallbacks.dispatchOnSuccess(getCallerIdentity(), utteranceId);
            }
        }

        @Override // android.speech.tts.TextToSpeechService.UtteranceProgressDispatcher
        public void dispatchOnStop() {
            String utteranceId = getUtteranceId();
            if (utteranceId != null) {
                TextToSpeechService.this.mCallbacks.dispatchOnStop(getCallerIdentity(), utteranceId, isStarted());
            }
        }

        @Override // android.speech.tts.TextToSpeechService.UtteranceProgressDispatcher
        public void dispatchOnStart() {
            String utteranceId = getUtteranceId();
            if (utteranceId != null) {
                TextToSpeechService.this.mCallbacks.dispatchOnStart(getCallerIdentity(), utteranceId);
            }
        }

        @Override // android.speech.tts.TextToSpeechService.UtteranceProgressDispatcher
        public void dispatchOnError(int errorCode) {
            String utteranceId = getUtteranceId();
            if (utteranceId != null) {
                TextToSpeechService.this.mCallbacks.dispatchOnError(getCallerIdentity(), utteranceId, errorCode);
            }
        }

        @Override // android.speech.tts.TextToSpeechService.UtteranceProgressDispatcher
        public void dispatchOnBeginSynthesis(int sampleRateInHz, int audioFormat, int channelCount) {
            String utteranceId = getUtteranceId();
            if (utteranceId != null) {
                TextToSpeechService.this.mCallbacks.dispatchOnBeginSynthesis(getCallerIdentity(), utteranceId, sampleRateInHz, audioFormat, channelCount);
            }
        }

        @Override // android.speech.tts.TextToSpeechService.UtteranceProgressDispatcher
        public void dispatchOnAudioAvailable(byte[] audio) {
            String utteranceId = getUtteranceId();
            if (utteranceId != null) {
                TextToSpeechService.this.mCallbacks.dispatchOnAudioAvailable(getCallerIdentity(), utteranceId, audio);
            }
        }

        @Override // android.speech.tts.TextToSpeechService.UtteranceProgressDispatcher
        public void dispatchOnRangeStart(int start, int end, int frame) {
            String utteranceId = getUtteranceId();
            if (utteranceId != null) {
                TextToSpeechService.this.mCallbacks.dispatchOnRangeStart(getCallerIdentity(), utteranceId, start, end, frame);
            }
        }

        /* access modifiers changed from: package-private */
        public String getStringParam(Bundle params, String key, String defaultValue) {
            return params == null ? defaultValue : params.getString(key, defaultValue);
        }

        /* access modifiers changed from: package-private */
        public int getIntParam(Bundle params, String key, int defaultValue) {
            return params == null ? defaultValue : params.getInt(key, defaultValue);
        }

        /* access modifiers changed from: package-private */
        public float getFloatParam(Bundle params, String key, float defaultValue) {
            return params == null ? defaultValue : params.getFloat(key, defaultValue);
        }
    }

    private abstract class UtteranceSpeechItemWithParams extends UtteranceSpeechItem {
        protected final Bundle mParams;
        protected final String mUtteranceId;

        UtteranceSpeechItemWithParams(Object callerIdentity, int callerUid, int callerPid, Bundle params, String utteranceId) {
            super(callerIdentity, callerUid, callerPid);
            this.mParams = params;
            this.mUtteranceId = utteranceId;
        }

        /* access modifiers changed from: package-private */
        public boolean hasLanguage() {
            return !TextUtils.isEmpty(getStringParam(this.mParams, "language", null));
        }

        /* access modifiers changed from: package-private */
        public int getSpeechRate() {
            return getIntParam(this.mParams, TextToSpeech.Engine.KEY_PARAM_RATE, TextToSpeechService.this.getDefaultSpeechRate());
        }

        /* access modifiers changed from: package-private */
        public int getPitch() {
            return getIntParam(this.mParams, TextToSpeech.Engine.KEY_PARAM_PITCH, TextToSpeechService.this.getDefaultPitch());
        }

        @Override // android.speech.tts.TextToSpeechService.UtteranceSpeechItem
        public String getUtteranceId() {
            return this.mUtteranceId;
        }

        /* access modifiers changed from: package-private */
        public AudioOutputParams getAudioParams() {
            return AudioOutputParams.createFromParamsBundle(this.mParams, true);
        }
    }

    class SynthesisSpeechItem extends UtteranceSpeechItemWithParams {
        private final int mCallerUid;
        private final String[] mDefaultLocale;
        private final EventLogger mEventLogger;
        private AbstractSynthesisCallback mSynthesisCallback;
        private final SynthesisRequest mSynthesisRequest = new SynthesisRequest(this.mText, this.mParams);
        private final CharSequence mText;

        public SynthesisSpeechItem(Object callerIdentity, int callerUid, int callerPid, Bundle params, String utteranceId, CharSequence text) {
            super(callerIdentity, callerUid, callerPid, params, utteranceId);
            this.mText = text;
            this.mCallerUid = callerUid;
            this.mDefaultLocale = TextToSpeechService.this.getSettingsLocale();
            setRequestParams(this.mSynthesisRequest);
            this.mEventLogger = new EventLogger(this.mSynthesisRequest, callerUid, callerPid, TextToSpeechService.this.mPackageName);
        }

        public CharSequence getText() {
            return this.mText;
        }

        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public boolean isValid() {
            CharSequence charSequence = this.mText;
            if (charSequence == null) {
                Log.e(TextToSpeechService.TAG, "null synthesis text");
                return false;
            } else if (charSequence.length() <= TextToSpeech.getMaxSpeechInputLength()) {
                return true;
            } else {
                Log.w(TextToSpeechService.TAG, "Text too long: " + this.mText.length() + " chars");
                return false;
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public void playImpl() {
            AbstractSynthesisCallback synthesisCallback;
            this.mEventLogger.onRequestProcessingStart();
            synchronized (this) {
                if (!isStopped()) {
                    this.mSynthesisCallback = createSynthesisCallback();
                    synthesisCallback = this.mSynthesisCallback;
                } else {
                    return;
                }
            }
            TextToSpeechService.this.onSynthesizeText(this.mSynthesisRequest, synthesisCallback);
            if (synthesisCallback.hasStarted() && !synthesisCallback.hasFinished()) {
                synthesisCallback.done();
            }
        }

        /* access modifiers changed from: protected */
        public AbstractSynthesisCallback createSynthesisCallback() {
            return new PlaybackSynthesisCallback(getAudioParams(), TextToSpeechService.this.mAudioPlaybackHandler, this, getCallerIdentity(), this.mEventLogger, false);
        }

        private void setRequestParams(SynthesisRequest request) {
            String voiceName = getVoiceName();
            request.setLanguage(getLanguage(), getCountry(), getVariant());
            if (!TextUtils.isEmpty(voiceName)) {
                request.setVoiceName(getVoiceName());
            }
            request.setSpeechRate(getSpeechRate());
            request.setCallerUid(this.mCallerUid);
            request.setPitch(getPitch());
        }

        /* access modifiers changed from: protected */
        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public void stopImpl() {
            AbstractSynthesisCallback synthesisCallback;
            synchronized (this) {
                synthesisCallback = this.mSynthesisCallback;
            }
            if (synthesisCallback != null) {
                synthesisCallback.stop();
                TextToSpeechService.this.onStop();
                return;
            }
            dispatchOnStop();
        }

        private String getCountry() {
            if (!hasLanguage()) {
                return this.mDefaultLocale[1];
            }
            return getStringParam(this.mParams, TextToSpeech.Engine.KEY_PARAM_COUNTRY, "");
        }

        private String getVariant() {
            if (!hasLanguage()) {
                return this.mDefaultLocale[2];
            }
            return getStringParam(this.mParams, TextToSpeech.Engine.KEY_PARAM_VARIANT, "");
        }

        public String getLanguage() {
            return getStringParam(this.mParams, "language", this.mDefaultLocale[0]);
        }

        public String getVoiceName() {
            return getStringParam(this.mParams, TextToSpeech.Engine.KEY_PARAM_VOICE_NAME, "");
        }
    }

    private class SynthesisToFileOutputStreamSpeechItem extends SynthesisSpeechItem {
        private final FileOutputStream mFileOutputStream;

        public SynthesisToFileOutputStreamSpeechItem(Object callerIdentity, int callerUid, int callerPid, Bundle params, String utteranceId, CharSequence text, FileOutputStream fileOutputStream) {
            super(callerIdentity, callerUid, callerPid, params, utteranceId, text);
            this.mFileOutputStream = fileOutputStream;
        }

        /* access modifiers changed from: protected */
        @Override // android.speech.tts.TextToSpeechService.SynthesisSpeechItem
        public AbstractSynthesisCallback createSynthesisCallback() {
            return new FileSynthesisCallback(this.mFileOutputStream.getChannel(), this, false);
        }

        /* access modifiers changed from: protected */
        @Override // android.speech.tts.TextToSpeechService.SynthesisSpeechItem, android.speech.tts.TextToSpeechService.SpeechItem
        public void playImpl() {
            super.playImpl();
            try {
                this.mFileOutputStream.close();
            } catch (IOException e) {
                Log.w(TextToSpeechService.TAG, "Failed to close output file", e);
            }
        }
    }

    private class AudioSpeechItem extends UtteranceSpeechItemWithParams {
        private final AudioPlaybackQueueItem mItem;

        public AudioSpeechItem(Object callerIdentity, int callerUid, int callerPid, Bundle params, String utteranceId, Uri uri) {
            super(callerIdentity, callerUid, callerPid, params, utteranceId);
            this.mItem = new AudioPlaybackQueueItem(this, getCallerIdentity(), TextToSpeechService.this, uri, getAudioParams());
        }

        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public boolean isValid() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public void playImpl() {
            TextToSpeechService.this.mAudioPlaybackHandler.enqueue(this.mItem);
        }

        /* access modifiers changed from: protected */
        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public void stopImpl() {
        }

        @Override // android.speech.tts.TextToSpeechService.UtteranceSpeechItemWithParams, android.speech.tts.TextToSpeechService.UtteranceSpeechItem
        public String getUtteranceId() {
            return getStringParam(this.mParams, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, null);
        }

        /* access modifiers changed from: package-private */
        @Override // android.speech.tts.TextToSpeechService.UtteranceSpeechItemWithParams
        public AudioOutputParams getAudioParams() {
            return AudioOutputParams.createFromParamsBundle(this.mParams, false);
        }
    }

    private class SilenceSpeechItem extends UtteranceSpeechItem {
        private final long mDuration;
        private final String mUtteranceId;

        public SilenceSpeechItem(Object callerIdentity, int callerUid, int callerPid, String utteranceId, long duration) {
            super(callerIdentity, callerUid, callerPid);
            this.mUtteranceId = utteranceId;
            this.mDuration = duration;
        }

        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public boolean isValid() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public void playImpl() {
            TextToSpeechService.this.mAudioPlaybackHandler.enqueue(new SilencePlaybackQueueItem(this, getCallerIdentity(), this.mDuration));
        }

        /* access modifiers changed from: protected */
        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public void stopImpl() {
        }

        @Override // android.speech.tts.TextToSpeechService.UtteranceSpeechItem
        public String getUtteranceId() {
            return this.mUtteranceId;
        }
    }

    private class LoadLanguageItem extends SpeechItem {
        private final String mCountry;
        private final String mLanguage;
        private final String mVariant;

        public LoadLanguageItem(Object callerIdentity, int callerUid, int callerPid, String language, String country, String variant) {
            super(callerIdentity, callerUid, callerPid);
            this.mLanguage = language;
            this.mCountry = country;
            this.mVariant = variant;
        }

        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public boolean isValid() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public void playImpl() {
            TextToSpeechService.this.onLoadLanguage(this.mLanguage, this.mCountry, this.mVariant);
        }

        /* access modifiers changed from: protected */
        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public void stopImpl() {
        }
    }

    private class LoadVoiceItem extends SpeechItem {
        private final String mVoiceName;

        public LoadVoiceItem(Object callerIdentity, int callerUid, int callerPid, String voiceName) {
            super(callerIdentity, callerUid, callerPid);
            this.mVoiceName = voiceName;
        }

        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public boolean isValid() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public void playImpl() {
            TextToSpeechService.this.onLoadVoice(this.mVoiceName);
        }

        /* access modifiers changed from: protected */
        @Override // android.speech.tts.TextToSpeechService.SpeechItem
        public void stopImpl() {
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        if (TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE.equals(intent.getAction())) {
            return this.mBinder;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public class CallbackMap extends RemoteCallbackList<ITextToSpeechCallback> {
        private final HashMap<IBinder, ITextToSpeechCallback> mCallerToCallback;

        private CallbackMap() {
            this.mCallerToCallback = new HashMap<>();
        }

        public void setCallback(IBinder caller, ITextToSpeechCallback cb) {
            ITextToSpeechCallback old;
            synchronized (this.mCallerToCallback) {
                if (cb != null) {
                    register(cb, caller);
                    old = this.mCallerToCallback.put(caller, cb);
                } else {
                    old = this.mCallerToCallback.remove(caller);
                }
                if (!(old == null || old == cb)) {
                    unregister(old);
                }
            }
        }

        public void dispatchOnStop(Object callerIdentity, String utteranceId, boolean started) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb != null) {
                try {
                    cb.onStop(utteranceId, started);
                } catch (RemoteException e) {
                    Log.e(TextToSpeechService.TAG, "Callback onStop failed: " + e);
                }
            }
        }

        public void dispatchOnSuccess(Object callerIdentity, String utteranceId) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb != null) {
                try {
                    cb.onSuccess(utteranceId);
                } catch (RemoteException e) {
                    Log.e(TextToSpeechService.TAG, "Callback onDone failed: " + e);
                }
            }
        }

        public void dispatchOnStart(Object callerIdentity, String utteranceId) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb != null) {
                try {
                    cb.onStart(utteranceId);
                } catch (RemoteException e) {
                    Log.e(TextToSpeechService.TAG, "Callback onStart failed: " + e);
                }
            }
        }

        public void dispatchOnError(Object callerIdentity, String utteranceId, int errorCode) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb != null) {
                try {
                    cb.onError(utteranceId, errorCode);
                } catch (RemoteException e) {
                    Log.e(TextToSpeechService.TAG, "Callback onError failed: " + e);
                }
            }
        }

        public void dispatchOnBeginSynthesis(Object callerIdentity, String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb != null) {
                try {
                    cb.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount);
                } catch (RemoteException e) {
                    Log.e(TextToSpeechService.TAG, "Callback dispatchOnBeginSynthesis(String, int, int, int) failed: " + e);
                }
            }
        }

        public void dispatchOnAudioAvailable(Object callerIdentity, String utteranceId, byte[] buffer) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb != null) {
                try {
                    cb.onAudioAvailable(utteranceId, buffer);
                } catch (RemoteException e) {
                    Log.e(TextToSpeechService.TAG, "Callback dispatchOnAudioAvailable(String, byte[]) failed: " + e);
                }
            }
        }

        public void dispatchOnRangeStart(Object callerIdentity, String utteranceId, int start, int end, int frame) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb != null) {
                try {
                    cb.onRangeStart(utteranceId, start, end, frame);
                } catch (RemoteException e) {
                    Log.e(TextToSpeechService.TAG, "Callback dispatchOnRangeStart(String, int, int, int) failed: " + e);
                }
            }
        }

        public void onCallbackDied(ITextToSpeechCallback callback, Object cookie) {
            IBinder caller = (IBinder) cookie;
            synchronized (this.mCallerToCallback) {
                this.mCallerToCallback.remove(caller);
            }
            TextToSpeechService.this.mSynthHandler.stopForApp(caller);
        }

        @Override // android.os.RemoteCallbackList
        public void kill() {
            synchronized (this.mCallerToCallback) {
                this.mCallerToCallback.clear();
                super.kill();
            }
        }

        private ITextToSpeechCallback getCallbackFor(Object caller) {
            ITextToSpeechCallback cb;
            IBinder asBinder = (IBinder) caller;
            synchronized (this.mCallerToCallback) {
                cb = this.mCallerToCallback.get(asBinder);
            }
            return cb;
        }
    }
}
