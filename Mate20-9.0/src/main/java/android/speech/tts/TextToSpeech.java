package android.speech.tts;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.speech.tts.ITextToSpeechCallback;
import android.speech.tts.ITextToSpeechService;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

public class TextToSpeech {
    public static final String ACTION_TTS_QUEUE_PROCESSING_COMPLETED = "android.speech.tts.TTS_QUEUE_PROCESSING_COMPLETED";
    public static final int ERROR = -1;
    public static final int ERROR_INVALID_REQUEST = -8;
    public static final int ERROR_NETWORK = -6;
    public static final int ERROR_NETWORK_TIMEOUT = -7;
    public static final int ERROR_NOT_INSTALLED_YET = -9;
    public static final int ERROR_OUTPUT = -5;
    public static final int ERROR_SERVICE = -4;
    public static final int ERROR_SYNTHESIS = -3;
    public static final int LANG_AVAILABLE = 0;
    public static final int LANG_COUNTRY_AVAILABLE = 1;
    public static final int LANG_COUNTRY_VAR_AVAILABLE = 2;
    public static final int LANG_MISSING_DATA = -1;
    public static final int LANG_NOT_SUPPORTED = -2;
    public static final int QUEUE_ADD = 1;
    static final int QUEUE_DESTROY = 2;
    public static final int QUEUE_FLUSH = 0;
    public static final int STOPPED = -2;
    public static final int SUCCESS = 0;
    private static final String TAG = "TextToSpeech";
    /* access modifiers changed from: private */
    public Connection mConnectingServiceConnection;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public volatile String mCurrentEngine;
    /* access modifiers changed from: private */
    public final Map<String, Uri> mEarcons;
    private final TtsEngines mEnginesHelper;
    private OnInitListener mInitListener;
    /* access modifiers changed from: private */
    public final Bundle mParams;
    private String mRequestedEngine;
    /* access modifiers changed from: private */
    public Connection mServiceConnection;
    /* access modifiers changed from: private */
    public final Object mStartLock;
    private final boolean mUseFallback;
    /* access modifiers changed from: private */
    public volatile UtteranceProgressListener mUtteranceProgressListener;
    /* access modifiers changed from: private */
    public final Map<CharSequence, Uri> mUtterances;

    private interface Action<R> {
        R run(ITextToSpeechService iTextToSpeechService) throws RemoteException;
    }

    private class Connection implements ServiceConnection {
        /* access modifiers changed from: private */
        public final ITextToSpeechCallback.Stub mCallback;
        /* access modifiers changed from: private */
        public boolean mEstablished;
        /* access modifiers changed from: private */
        public SetupConnectionAsyncTask mOnSetupConnectionAsyncTask;
        /* access modifiers changed from: private */
        public ITextToSpeechService mService;

        private class SetupConnectionAsyncTask extends AsyncTask<Void, Void, Integer> {
            private final ComponentName mName;

            public SetupConnectionAsyncTask(ComponentName name) {
                this.mName = name;
            }

            /* access modifiers changed from: protected */
            public Integer doInBackground(Void... params) {
                synchronized (TextToSpeech.this.mStartLock) {
                    if (isCancelled()) {
                        return null;
                    }
                    try {
                        Connection.this.mService.setCallback(Connection.this.getCallerIdentity(), Connection.this.mCallback);
                        if (TextToSpeech.this.mParams.getString("language") == null) {
                            String[] defaultLanguage = Connection.this.mService.getClientDefaultLanguage();
                            TextToSpeech.this.mParams.putString("language", defaultLanguage[0]);
                            TextToSpeech.this.mParams.putString(Engine.KEY_PARAM_COUNTRY, defaultLanguage[1]);
                            TextToSpeech.this.mParams.putString(Engine.KEY_PARAM_VARIANT, defaultLanguage[2]);
                            TextToSpeech.this.mParams.putString(Engine.KEY_PARAM_VOICE_NAME, Connection.this.mService.getDefaultVoiceNameFor(defaultLanguage[0], defaultLanguage[1], defaultLanguage[2]));
                        }
                        Log.i(TextToSpeech.TAG, "Set up connection to " + this.mName);
                        return 0;
                    } catch (RemoteException e) {
                        Log.e(TextToSpeech.TAG, "Error connecting to service, setCallback() failed");
                        return -1;
                    }
                }
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Integer result) {
                synchronized (TextToSpeech.this.mStartLock) {
                    if (Connection.this.mOnSetupConnectionAsyncTask == this) {
                        SetupConnectionAsyncTask unused = Connection.this.mOnSetupConnectionAsyncTask = null;
                    }
                    boolean unused2 = Connection.this.mEstablished = true;
                    TextToSpeech.this.dispatchOnInit(result.intValue());
                }
            }
        }

        private Connection() {
            this.mCallback = new ITextToSpeechCallback.Stub() {
                public void onStop(String utteranceId, boolean isStarted) throws RemoteException {
                    UtteranceProgressListener listener = TextToSpeech.this.mUtteranceProgressListener;
                    if (listener != null) {
                        listener.onStop(utteranceId, isStarted);
                    }
                }

                public void onSuccess(String utteranceId) {
                    UtteranceProgressListener listener = TextToSpeech.this.mUtteranceProgressListener;
                    if (listener != null) {
                        listener.onDone(utteranceId);
                    }
                }

                public void onError(String utteranceId, int errorCode) {
                    UtteranceProgressListener listener = TextToSpeech.this.mUtteranceProgressListener;
                    if (listener != null) {
                        listener.onError(utteranceId);
                    }
                }

                public void onStart(String utteranceId) {
                    UtteranceProgressListener listener = TextToSpeech.this.mUtteranceProgressListener;
                    if (listener != null) {
                        listener.onStart(utteranceId);
                    }
                }

                public void onBeginSynthesis(String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) {
                    UtteranceProgressListener listener = TextToSpeech.this.mUtteranceProgressListener;
                    if (listener != null) {
                        listener.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount);
                    }
                }

                public void onAudioAvailable(String utteranceId, byte[] audio) {
                    UtteranceProgressListener listener = TextToSpeech.this.mUtteranceProgressListener;
                    if (listener != null) {
                        listener.onAudioAvailable(utteranceId, audio);
                    }
                }

                public void onRangeStart(String utteranceId, int start, int end, int frame) {
                    UtteranceProgressListener listener = TextToSpeech.this.mUtteranceProgressListener;
                    if (listener != null) {
                        listener.onRangeStart(utteranceId, start, end, frame);
                    }
                }
            };
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (TextToSpeech.this.mStartLock) {
                Connection unused = TextToSpeech.this.mConnectingServiceConnection = null;
                Log.i(TextToSpeech.TAG, "Connected to " + name);
                if (this.mOnSetupConnectionAsyncTask != null) {
                    this.mOnSetupConnectionAsyncTask.cancel(false);
                }
                this.mService = ITextToSpeechService.Stub.asInterface(service);
                Connection unused2 = TextToSpeech.this.mServiceConnection = this;
                this.mEstablished = false;
                this.mOnSetupConnectionAsyncTask = new SetupConnectionAsyncTask(name);
                this.mOnSetupConnectionAsyncTask.execute((Params[]) new Void[0]);
            }
        }

        public IBinder getCallerIdentity() {
            return this.mCallback;
        }

        private boolean clearServiceConnection() {
            boolean result;
            synchronized (TextToSpeech.this.mStartLock) {
                result = false;
                if (this.mOnSetupConnectionAsyncTask != null) {
                    result = this.mOnSetupConnectionAsyncTask.cancel(false);
                    this.mOnSetupConnectionAsyncTask = null;
                }
                this.mService = null;
                if (TextToSpeech.this.mServiceConnection == this) {
                    Connection unused = TextToSpeech.this.mServiceConnection = null;
                }
            }
            return result;
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.i(TextToSpeech.TAG, "Asked to disconnect from " + name);
            if (clearServiceConnection()) {
                TextToSpeech.this.dispatchOnInit(-1);
            }
        }

        public void disconnect() {
            TextToSpeech.this.mContext.unbindService(this);
            clearServiceConnection();
        }

        public boolean isEstablished() {
            return this.mService != null && this.mEstablished;
        }

        public <R> R runAction(Action<R> action, R errorResult, String method, boolean reconnect, boolean onlyEstablishedConnection) {
            synchronized (TextToSpeech.this.mStartLock) {
                try {
                    if (this.mService == null) {
                        Log.w(TextToSpeech.TAG, method + " failed: not connected to TTS engine");
                        return errorResult;
                    }
                    if (onlyEstablishedConnection) {
                        if (!isEstablished()) {
                            Log.w(TextToSpeech.TAG, method + " failed: TTS engine connection not fully set up");
                            return errorResult;
                        }
                    }
                    R run = action.run(this.mService);
                    return run;
                } catch (RemoteException ex) {
                    Log.e(TextToSpeech.TAG, method + " failed", ex);
                    if (reconnect) {
                        disconnect();
                        int unused = TextToSpeech.this.initTts();
                    }
                    return errorResult;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public class Engine {
        public static final String ACTION_CHECK_TTS_DATA = "android.speech.tts.engine.CHECK_TTS_DATA";
        public static final String ACTION_GET_SAMPLE_TEXT = "android.speech.tts.engine.GET_SAMPLE_TEXT";
        public static final String ACTION_INSTALL_TTS_DATA = "android.speech.tts.engine.INSTALL_TTS_DATA";
        public static final String ACTION_TTS_DATA_INSTALLED = "android.speech.tts.engine.TTS_DATA_INSTALLED";
        @Deprecated
        public static final int CHECK_VOICE_DATA_BAD_DATA = -1;
        public static final int CHECK_VOICE_DATA_FAIL = 0;
        @Deprecated
        public static final int CHECK_VOICE_DATA_MISSING_DATA = -2;
        @Deprecated
        public static final int CHECK_VOICE_DATA_MISSING_VOLUME = -3;
        public static final int CHECK_VOICE_DATA_PASS = 1;
        @Deprecated
        public static final String DEFAULT_ENGINE = "com.svox.pico";
        public static final float DEFAULT_PAN = 0.0f;
        public static final int DEFAULT_PITCH = 100;
        public static final int DEFAULT_RATE = 100;
        public static final int DEFAULT_STREAM = 3;
        public static final float DEFAULT_VOLUME = 1.0f;
        public static final String EXTRA_AVAILABLE_VOICES = "availableVoices";
        @Deprecated
        public static final String EXTRA_CHECK_VOICE_DATA_FOR = "checkVoiceDataFor";
        public static final String EXTRA_SAMPLE_TEXT = "sampleText";
        @Deprecated
        public static final String EXTRA_TTS_DATA_INSTALLED = "dataInstalled";
        public static final String EXTRA_UNAVAILABLE_VOICES = "unavailableVoices";
        @Deprecated
        public static final String EXTRA_VOICE_DATA_FILES = "dataFiles";
        @Deprecated
        public static final String EXTRA_VOICE_DATA_FILES_INFO = "dataFilesInfo";
        @Deprecated
        public static final String EXTRA_VOICE_DATA_ROOT_DIRECTORY = "dataRoot";
        public static final String INTENT_ACTION_TTS_SERVICE = "android.intent.action.TTS_SERVICE";
        @Deprecated
        public static final String KEY_FEATURE_EMBEDDED_SYNTHESIS = "embeddedTts";
        public static final String KEY_FEATURE_NETWORK_RETRIES_COUNT = "networkRetriesCount";
        @Deprecated
        public static final String KEY_FEATURE_NETWORK_SYNTHESIS = "networkTts";
        public static final String KEY_FEATURE_NETWORK_TIMEOUT_MS = "networkTimeoutMs";
        public static final String KEY_FEATURE_NOT_INSTALLED = "notInstalled";
        public static final String KEY_PARAM_AUDIO_ATTRIBUTES = "audioAttributes";
        public static final String KEY_PARAM_COUNTRY = "country";
        public static final String KEY_PARAM_ENGINE = "engine";
        public static final String KEY_PARAM_LANGUAGE = "language";
        public static final String KEY_PARAM_PAN = "pan";
        public static final String KEY_PARAM_PITCH = "pitch";
        public static final String KEY_PARAM_RATE = "rate";
        public static final String KEY_PARAM_SESSION_ID = "sessionId";
        public static final String KEY_PARAM_STREAM = "streamType";
        public static final String KEY_PARAM_UTTERANCE_ID = "utteranceId";
        public static final String KEY_PARAM_VARIANT = "variant";
        public static final String KEY_PARAM_VOICE_NAME = "voiceName";
        public static final String KEY_PARAM_VOLUME = "volume";
        public static final String SERVICE_META_DATA = "android.speech.tts";
        public static final int USE_DEFAULTS = 0;

        public Engine() {
        }
    }

    public static class EngineInfo {
        public int icon;
        public String label;
        public String name;
        public int priority;
        public boolean system;

        public String toString() {
            return "EngineInfo{name=" + this.name + "}";
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Error {
    }

    public interface OnInitListener {
        void onInit(int i);
    }

    @Deprecated
    public interface OnUtteranceCompletedListener {
        void onUtteranceCompleted(String str);
    }

    public TextToSpeech(Context context, OnInitListener listener) {
        this(context, listener, null);
    }

    public TextToSpeech(Context context, OnInitListener listener, String engine) {
        this(context, listener, engine, null, true);
    }

    public TextToSpeech(Context context, OnInitListener listener, String engine, String packageName, boolean useFallback) {
        this.mStartLock = new Object();
        this.mParams = new Bundle();
        this.mCurrentEngine = null;
        this.mContext = context;
        this.mInitListener = listener;
        this.mRequestedEngine = engine;
        this.mUseFallback = useFallback;
        this.mEarcons = new HashMap();
        this.mUtterances = new HashMap();
        this.mUtteranceProgressListener = null;
        this.mEnginesHelper = new TtsEngines(this.mContext);
        initTts();
    }

    private <R> R runActionNoReconnect(Action<R> action, R errorResult, String method, boolean onlyEstablishedConnection) {
        return runAction(action, errorResult, method, false, onlyEstablishedConnection);
    }

    private <R> R runAction(Action<R> action, R errorResult, String method) {
        return runAction(action, errorResult, method, true, true);
    }

    private <R> R runAction(Action<R> action, R errorResult, String method, boolean reconnect, boolean onlyEstablishedConnection) {
        synchronized (this.mStartLock) {
            if (this.mServiceConnection == null) {
                Log.w(TAG, method + " failed: not bound to TTS engine");
                return errorResult;
            }
            R runAction = this.mServiceConnection.runAction(action, errorResult, method, reconnect, onlyEstablishedConnection);
            return runAction;
        }
    }

    /* access modifiers changed from: private */
    public int initTts() {
        if (this.mRequestedEngine != null) {
            if (this.mEnginesHelper.isEngineInstalled(this.mRequestedEngine)) {
                if (connectToEngine(this.mRequestedEngine)) {
                    this.mCurrentEngine = this.mRequestedEngine;
                    return 0;
                } else if (!this.mUseFallback) {
                    this.mCurrentEngine = null;
                    dispatchOnInit(-1);
                    return -1;
                }
            } else if (!this.mUseFallback) {
                Log.i(TAG, "Requested engine not installed: " + this.mRequestedEngine);
                this.mCurrentEngine = null;
                dispatchOnInit(-1);
                return -1;
            }
        }
        String defaultEngine = getDefaultEngine();
        if (defaultEngine == null || defaultEngine.equals(this.mRequestedEngine) || !connectToEngine(defaultEngine)) {
            String highestRanked = this.mEnginesHelper.getHighestRankedEngineName();
            if (highestRanked == null || highestRanked.equals(this.mRequestedEngine) || highestRanked.equals(defaultEngine) || !connectToEngine(highestRanked)) {
                this.mCurrentEngine = null;
                dispatchOnInit(-1);
                return -1;
            }
            this.mCurrentEngine = highestRanked;
            return 0;
        }
        this.mCurrentEngine = defaultEngine;
        return 0;
    }

    private boolean connectToEngine(String engine) {
        Connection connection = new Connection();
        Intent intent = new Intent(Engine.INTENT_ACTION_TTS_SERVICE);
        intent.setPackage(engine);
        if (!this.mContext.bindService(intent, connection, 1)) {
            Log.e(TAG, "Failed to bind to " + engine);
            return false;
        }
        Log.i(TAG, "Sucessfully bound to " + engine);
        this.mConnectingServiceConnection = connection;
        return true;
    }

    /* access modifiers changed from: private */
    public void dispatchOnInit(int result) {
        synchronized (this.mStartLock) {
            if (this.mInitListener != null) {
                this.mInitListener.onInit(result);
                this.mInitListener = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public IBinder getCallerIdentity() {
        return this.mServiceConnection.getCallerIdentity();
    }

    public void shutdown() {
        synchronized (this.mStartLock) {
            if (this.mConnectingServiceConnection != null) {
                this.mContext.unbindService(this.mConnectingServiceConnection);
                this.mConnectingServiceConnection = null;
                return;
            }
            runActionNoReconnect(new Action<Void>() {
                public Void run(ITextToSpeechService service) throws RemoteException {
                    service.setCallback(TextToSpeech.this.getCallerIdentity(), null);
                    service.stop(TextToSpeech.this.getCallerIdentity());
                    TextToSpeech.this.mServiceConnection.disconnect();
                    Connection unused = TextToSpeech.this.mServiceConnection = null;
                    String unused2 = TextToSpeech.this.mCurrentEngine = null;
                    return null;
                }
            }, null, "shutdown", false);
        }
    }

    public int addSpeech(String text, String packagename, int resourceId) {
        synchronized (this.mStartLock) {
            this.mUtterances.put(text, makeResourceUri(packagename, resourceId));
        }
        return 0;
    }

    public int addSpeech(CharSequence text, String packagename, int resourceId) {
        synchronized (this.mStartLock) {
            this.mUtterances.put(text, makeResourceUri(packagename, resourceId));
        }
        return 0;
    }

    public int addSpeech(String text, String filename) {
        synchronized (this.mStartLock) {
            this.mUtterances.put(text, Uri.parse(filename));
        }
        return 0;
    }

    public int addSpeech(CharSequence text, File file) {
        synchronized (this.mStartLock) {
            this.mUtterances.put(text, Uri.fromFile(file));
        }
        return 0;
    }

    public int addEarcon(String earcon, String packagename, int resourceId) {
        synchronized (this.mStartLock) {
            this.mEarcons.put(earcon, makeResourceUri(packagename, resourceId));
        }
        return 0;
    }

    @Deprecated
    public int addEarcon(String earcon, String filename) {
        synchronized (this.mStartLock) {
            this.mEarcons.put(earcon, Uri.parse(filename));
        }
        return 0;
    }

    public int addEarcon(String earcon, File file) {
        synchronized (this.mStartLock) {
            this.mEarcons.put(earcon, Uri.fromFile(file));
        }
        return 0;
    }

    private Uri makeResourceUri(String packageName, int resourceId) {
        return new Uri.Builder().scheme("android.resource").encodedAuthority(packageName).appendEncodedPath(String.valueOf(resourceId)).build();
    }

    public int speak(CharSequence text, int queueMode, Bundle params, String utteranceId) {
        final CharSequence charSequence = text;
        final int i = queueMode;
        final Bundle bundle = params;
        final String str = utteranceId;
        AnonymousClass2 r0 = new Action<Integer>() {
            public Integer run(ITextToSpeechService service) throws RemoteException {
                Uri utteranceUri = (Uri) TextToSpeech.this.mUtterances.get(charSequence);
                if (utteranceUri != null) {
                    return Integer.valueOf(service.playAudio(TextToSpeech.this.getCallerIdentity(), utteranceUri, i, TextToSpeech.this.getParams(bundle), str));
                }
                return Integer.valueOf(service.speak(TextToSpeech.this.getCallerIdentity(), charSequence, i, TextToSpeech.this.getParams(bundle), str));
            }
        };
        return ((Integer) runAction(r0, -1, "speak")).intValue();
    }

    @Deprecated
    public int speak(String text, int queueMode, HashMap<String, String> params) {
        return speak(text, queueMode, convertParamsHashMaptoBundle(params), params == null ? null : params.get(Engine.KEY_PARAM_UTTERANCE_ID));
    }

    public int playEarcon(String earcon, int queueMode, Bundle params, String utteranceId) {
        final String str = earcon;
        final int i = queueMode;
        final Bundle bundle = params;
        final String str2 = utteranceId;
        AnonymousClass3 r0 = new Action<Integer>() {
            public Integer run(ITextToSpeechService service) throws RemoteException {
                Uri earconUri = (Uri) TextToSpeech.this.mEarcons.get(str);
                if (earconUri == null) {
                    return -1;
                }
                return Integer.valueOf(service.playAudio(TextToSpeech.this.getCallerIdentity(), earconUri, i, TextToSpeech.this.getParams(bundle), str2));
            }
        };
        return ((Integer) runAction(r0, -1, "playEarcon")).intValue();
    }

    @Deprecated
    public int playEarcon(String earcon, int queueMode, HashMap<String, String> params) {
        return playEarcon(earcon, queueMode, convertParamsHashMaptoBundle(params), params == null ? null : params.get(Engine.KEY_PARAM_UTTERANCE_ID));
    }

    public int playSilentUtterance(long durationInMs, int queueMode, String utteranceId) {
        final long j = durationInMs;
        final int i = queueMode;
        final String str = utteranceId;
        AnonymousClass4 r0 = new Action<Integer>() {
            public Integer run(ITextToSpeechService service) throws RemoteException {
                return Integer.valueOf(service.playSilence(TextToSpeech.this.getCallerIdentity(), j, i, str));
            }
        };
        return ((Integer) runAction(r0, -1, "playSilentUtterance")).intValue();
    }

    @Deprecated
    public int playSilence(long durationInMs, int queueMode, HashMap<String, String> params) {
        return playSilentUtterance(durationInMs, queueMode, params == null ? null : params.get(Engine.KEY_PARAM_UTTERANCE_ID));
    }

    @Deprecated
    public Set<String> getFeatures(final Locale locale) {
        return (Set) runAction(new Action<Set<String>>() {
            public Set<String> run(ITextToSpeechService service) throws RemoteException {
                try {
                    String[] features = service.getFeaturesForLanguage(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant());
                    if (features == null) {
                        return null;
                    }
                    Set<String> featureSet = new HashSet<>();
                    Collections.addAll(featureSet, features);
                    return featureSet;
                } catch (MissingResourceException e) {
                    Log.w(TextToSpeech.TAG, "Couldn't retrieve 3 letter ISO 639-2/T language and/or ISO 3166 country code for locale: " + locale, e);
                    return null;
                }
            }
        }, null, "getFeatures");
    }

    public boolean isSpeaking() {
        return ((Boolean) runAction(new Action<Boolean>() {
            public Boolean run(ITextToSpeechService service) throws RemoteException {
                return Boolean.valueOf(service.isSpeaking());
            }
        }, false, "isSpeaking")).booleanValue();
    }

    public int stop() {
        return ((Integer) runAction(new Action<Integer>() {
            public Integer run(ITextToSpeechService service) throws RemoteException {
                return Integer.valueOf(service.stop(TextToSpeech.this.getCallerIdentity()));
            }
        }, -1, "stop")).intValue();
    }

    public int setSpeechRate(float speechRate) {
        if (speechRate > 0.0f) {
            int intRate = (int) (100.0f * speechRate);
            if (intRate > 0) {
                synchronized (this.mStartLock) {
                    this.mParams.putInt(Engine.KEY_PARAM_RATE, intRate);
                }
                return 0;
            }
        }
        return -1;
    }

    public int setPitch(float pitch) {
        if (pitch > 0.0f) {
            int intPitch = (int) (100.0f * pitch);
            if (intPitch > 0) {
                synchronized (this.mStartLock) {
                    this.mParams.putInt(Engine.KEY_PARAM_PITCH, intPitch);
                }
                return 0;
            }
        }
        return -1;
    }

    public int setAudioAttributes(AudioAttributes audioAttributes) {
        if (audioAttributes == null) {
            return -1;
        }
        synchronized (this.mStartLock) {
            this.mParams.putParcelable(Engine.KEY_PARAM_AUDIO_ATTRIBUTES, audioAttributes);
        }
        return 0;
    }

    public String getCurrentEngine() {
        return this.mCurrentEngine;
    }

    @Deprecated
    public Locale getDefaultLanguage() {
        return (Locale) runAction(new Action<Locale>() {
            public Locale run(ITextToSpeechService service) throws RemoteException {
                String[] defaultLanguage = service.getClientDefaultLanguage();
                return new Locale(defaultLanguage[0], defaultLanguage[1], defaultLanguage[2]);
            }
        }, null, "getDefaultLanguage");
    }

    public int setLanguage(final Locale loc) {
        return ((Integer) runAction(new Action<Integer>() {
            public Integer run(ITextToSpeechService service) throws RemoteException {
                if (loc == null) {
                    return -2;
                }
                try {
                    String language = loc.getISO3Language();
                    try {
                        String country = loc.getISO3Country();
                        String variant = loc.getVariant();
                        int result = service.isLanguageAvailable(language, country, variant);
                        if (result >= 0) {
                            String voiceName = service.getDefaultVoiceNameFor(language, country, variant);
                            if (TextUtils.isEmpty(voiceName)) {
                                Log.w(TextToSpeech.TAG, "Couldn't find the default voice for " + language + "-" + country + "-" + variant);
                                return -2;
                            } else if (service.loadVoice(TextToSpeech.this.getCallerIdentity(), voiceName) == -1) {
                                Log.w(TextToSpeech.TAG, "The service claimed " + language + "-" + country + "-" + variant + " was available with voice name " + voiceName + " but loadVoice returned ERROR");
                                return -2;
                            } else {
                                Voice voice = TextToSpeech.this.getVoice(service, voiceName);
                                if (voice == null) {
                                    Log.w(TextToSpeech.TAG, "getDefaultVoiceNameFor returned " + voiceName + " for locale " + language + "-" + country + "-" + variant + " but getVoice returns null");
                                    return -2;
                                }
                                String voiceLanguage = "";
                                try {
                                    voiceLanguage = voice.getLocale().getISO3Language();
                                } catch (MissingResourceException e) {
                                    Log.w(TextToSpeech.TAG, "Couldn't retrieve ISO 639-2/T language code for locale: " + voice.getLocale(), e);
                                }
                                String voiceCountry = "";
                                try {
                                    voiceCountry = voice.getLocale().getISO3Country();
                                } catch (MissingResourceException e2) {
                                    Log.w(TextToSpeech.TAG, "Couldn't retrieve ISO 3166 country code for locale: " + voice.getLocale(), e2);
                                }
                                TextToSpeech.this.mParams.putString(Engine.KEY_PARAM_VOICE_NAME, voiceName);
                                TextToSpeech.this.mParams.putString("language", voiceLanguage);
                                TextToSpeech.this.mParams.putString(Engine.KEY_PARAM_COUNTRY, voiceCountry);
                                TextToSpeech.this.mParams.putString(Engine.KEY_PARAM_VARIANT, voice.getLocale().getVariant());
                            }
                        }
                        return Integer.valueOf(result);
                    } catch (MissingResourceException e3) {
                        Log.w(TextToSpeech.TAG, "Couldn't retrieve ISO 3166 country code for locale: " + loc, e3);
                        return -2;
                    }
                } catch (MissingResourceException e4) {
                    Log.w(TextToSpeech.TAG, "Couldn't retrieve ISO 639-2/T language code for locale: " + loc, e4);
                    return -2;
                }
            }
        }, -2, "setLanguage")).intValue();
    }

    @Deprecated
    public Locale getLanguage() {
        return (Locale) runAction(new Action<Locale>() {
            public Locale run(ITextToSpeechService service) {
                return new Locale(TextToSpeech.this.mParams.getString("language", ""), TextToSpeech.this.mParams.getString(Engine.KEY_PARAM_COUNTRY, ""), TextToSpeech.this.mParams.getString(Engine.KEY_PARAM_VARIANT, ""));
            }
        }, null, "getLanguage");
    }

    public Set<Locale> getAvailableLanguages() {
        return (Set) runAction(new Action<Set<Locale>>() {
            public Set<Locale> run(ITextToSpeechService service) throws RemoteException {
                List<Voice> voices = service.getVoices();
                if (voices == null) {
                    return new HashSet();
                }
                HashSet<Locale> locales = new HashSet<>();
                for (Voice voice : voices) {
                    locales.add(voice.getLocale());
                }
                return locales;
            }
        }, null, "getAvailableLanguages");
    }

    public Set<Voice> getVoices() {
        return (Set) runAction(new Action<Set<Voice>>() {
            public Set<Voice> run(ITextToSpeechService service) throws RemoteException {
                List<Voice> voices = service.getVoices();
                return voices != null ? new HashSet(voices) : new HashSet();
            }
        }, null, "getVoices");
    }

    public int setVoice(final Voice voice) {
        return ((Integer) runAction(new Action<Integer>() {
            public Integer run(ITextToSpeechService service) throws RemoteException {
                int result = service.loadVoice(TextToSpeech.this.getCallerIdentity(), voice.getName());
                if (result == 0) {
                    TextToSpeech.this.mParams.putString(Engine.KEY_PARAM_VOICE_NAME, voice.getName());
                    String language = "";
                    try {
                        language = voice.getLocale().getISO3Language();
                    } catch (MissingResourceException e) {
                        Log.w(TextToSpeech.TAG, "Couldn't retrieve ISO 639-2/T language code for locale: " + voice.getLocale(), e);
                    }
                    String country = "";
                    try {
                        country = voice.getLocale().getISO3Country();
                    } catch (MissingResourceException e2) {
                        Log.w(TextToSpeech.TAG, "Couldn't retrieve ISO 3166 country code for locale: " + voice.getLocale(), e2);
                    }
                    TextToSpeech.this.mParams.putString("language", language);
                    TextToSpeech.this.mParams.putString(Engine.KEY_PARAM_COUNTRY, country);
                    TextToSpeech.this.mParams.putString(Engine.KEY_PARAM_VARIANT, voice.getLocale().getVariant());
                }
                return Integer.valueOf(result);
            }
        }, -2, "setVoice")).intValue();
    }

    public Voice getVoice() {
        return (Voice) runAction(new Action<Voice>() {
            public Voice run(ITextToSpeechService service) throws RemoteException {
                String voiceName = TextToSpeech.this.mParams.getString(Engine.KEY_PARAM_VOICE_NAME, "");
                if (TextUtils.isEmpty(voiceName)) {
                    return null;
                }
                return TextToSpeech.this.getVoice(service, voiceName);
            }
        }, null, "getVoice");
    }

    /* access modifiers changed from: private */
    public Voice getVoice(ITextToSpeechService service, String voiceName) throws RemoteException {
        try {
            List<Voice> voices = service.getVoices();
            if (voices == null) {
                Log.w(TAG, "getVoices returned null");
                return null;
            }
            for (Voice voice : voices) {
                if (voice.getName().equals(voiceName)) {
                    return voice;
                }
            }
            Log.w(TAG, "Could not find voice " + voiceName + " in voice list");
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public Voice getDefaultVoice() {
        return (Voice) runAction(new Action<Voice>() {
            public Voice run(ITextToSpeechService service) throws RemoteException {
                String[] defaultLanguage = service.getClientDefaultLanguage();
                if (defaultLanguage == null || defaultLanguage.length == 0) {
                    Log.e(TextToSpeech.TAG, "service.getClientDefaultLanguage() returned empty array");
                    return null;
                }
                String language = defaultLanguage[0];
                String country = defaultLanguage.length > 1 ? defaultLanguage[1] : "";
                String variant = defaultLanguage.length > 2 ? defaultLanguage[2] : "";
                if (service.isLanguageAvailable(language, country, variant) < 0) {
                    return null;
                }
                String voiceName = service.getDefaultVoiceNameFor(language, country, variant);
                if (TextUtils.isEmpty(voiceName)) {
                    return null;
                }
                List<Voice> voices = service.getVoices();
                if (voices == null) {
                    return null;
                }
                for (Voice voice : voices) {
                    if (voice.getName().equals(voiceName)) {
                        return voice;
                    }
                }
                return null;
            }
        }, null, "getDefaultVoice");
    }

    public int isLanguageAvailable(final Locale loc) {
        return ((Integer) runAction(new Action<Integer>() {
            public Integer run(ITextToSpeechService service) throws RemoteException {
                try {
                    try {
                        return Integer.valueOf(service.isLanguageAvailable(loc.getISO3Language(), loc.getISO3Country(), loc.getVariant()));
                    } catch (MissingResourceException e) {
                        Log.w(TextToSpeech.TAG, "Couldn't retrieve ISO 3166 country code for locale: " + loc, e);
                        return -2;
                    }
                } catch (MissingResourceException e2) {
                    Log.w(TextToSpeech.TAG, "Couldn't retrieve ISO 639-2/T language code for locale: " + loc, e2);
                    return -2;
                }
            }
        }, -2, "isLanguageAvailable")).intValue();
    }

    public int synthesizeToFile(CharSequence text, Bundle params, File file, String utteranceId) {
        final File file2 = file;
        final CharSequence charSequence = text;
        final Bundle bundle = params;
        final String str = utteranceId;
        AnonymousClass17 r0 = new Action<Integer>() {
            public Integer run(ITextToSpeechService service) throws RemoteException {
                try {
                    if (!file2.exists() || file2.canWrite()) {
                        ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file2, 738197504);
                        int returnValue = service.synthesizeToFileDescriptor(TextToSpeech.this.getCallerIdentity(), charSequence, fileDescriptor, TextToSpeech.this.getParams(bundle), str);
                        fileDescriptor.close();
                        return Integer.valueOf(returnValue);
                    }
                    Log.e(TextToSpeech.TAG, "Can't write to " + file2);
                    return -1;
                } catch (FileNotFoundException e) {
                    Log.e(TextToSpeech.TAG, "Opening file " + file2 + " failed", e);
                    return -1;
                } catch (IOException e2) {
                    Log.e(TextToSpeech.TAG, "Closing file " + file2 + " failed", e2);
                    return -1;
                }
            }
        };
        return ((Integer) runAction(r0, -1, "synthesizeToFile")).intValue();
    }

    @Deprecated
    public int synthesizeToFile(String text, HashMap<String, String> params, String filename) {
        return synthesizeToFile(text, convertParamsHashMaptoBundle(params), new File(filename), params.get(Engine.KEY_PARAM_UTTERANCE_ID));
    }

    private Bundle convertParamsHashMaptoBundle(HashMap<String, String> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        Bundle bundle = new Bundle();
        copyIntParam(bundle, params, Engine.KEY_PARAM_STREAM);
        copyIntParam(bundle, params, Engine.KEY_PARAM_SESSION_ID);
        copyStringParam(bundle, params, Engine.KEY_PARAM_UTTERANCE_ID);
        copyFloatParam(bundle, params, "volume");
        copyFloatParam(bundle, params, Engine.KEY_PARAM_PAN);
        copyStringParam(bundle, params, Engine.KEY_FEATURE_NETWORK_SYNTHESIS);
        copyStringParam(bundle, params, Engine.KEY_FEATURE_EMBEDDED_SYNTHESIS);
        copyIntParam(bundle, params, Engine.KEY_FEATURE_NETWORK_TIMEOUT_MS);
        copyIntParam(bundle, params, Engine.KEY_FEATURE_NETWORK_RETRIES_COUNT);
        if (!TextUtils.isEmpty(this.mCurrentEngine)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                if (key != null && key.startsWith(this.mCurrentEngine)) {
                    bundle.putString(key, entry.getValue());
                }
            }
        }
        return bundle;
    }

    /* access modifiers changed from: private */
    public Bundle getParams(Bundle params) {
        if (params == null || params.isEmpty()) {
            return this.mParams;
        }
        Bundle bundle = new Bundle(this.mParams);
        bundle.putAll(params);
        verifyIntegerBundleParam(bundle, Engine.KEY_PARAM_STREAM);
        verifyIntegerBundleParam(bundle, Engine.KEY_PARAM_SESSION_ID);
        verifyStringBundleParam(bundle, Engine.KEY_PARAM_UTTERANCE_ID);
        verifyFloatBundleParam(bundle, "volume");
        verifyFloatBundleParam(bundle, Engine.KEY_PARAM_PAN);
        verifyBooleanBundleParam(bundle, Engine.KEY_FEATURE_NETWORK_SYNTHESIS);
        verifyBooleanBundleParam(bundle, Engine.KEY_FEATURE_EMBEDDED_SYNTHESIS);
        verifyIntegerBundleParam(bundle, Engine.KEY_FEATURE_NETWORK_TIMEOUT_MS);
        verifyIntegerBundleParam(bundle, Engine.KEY_FEATURE_NETWORK_RETRIES_COUNT);
        return bundle;
    }

    private static boolean verifyIntegerBundleParam(Bundle bundle, String key) {
        if (!bundle.containsKey(key) || (bundle.get(key) instanceof Integer) || (bundle.get(key) instanceof Long)) {
            return true;
        }
        bundle.remove(key);
        Log.w(TAG, "Synthesis request paramter " + key + " containst value  with invalid type. Should be an Integer or a Long");
        return false;
    }

    private static boolean verifyStringBundleParam(Bundle bundle, String key) {
        if (!bundle.containsKey(key) || (bundle.get(key) instanceof String)) {
            return true;
        }
        bundle.remove(key);
        Log.w(TAG, "Synthesis request paramter " + key + " containst value  with invalid type. Should be a String");
        return false;
    }

    private static boolean verifyBooleanBundleParam(Bundle bundle, String key) {
        if (!bundle.containsKey(key) || (bundle.get(key) instanceof Boolean) || (bundle.get(key) instanceof String)) {
            return true;
        }
        bundle.remove(key);
        Log.w(TAG, "Synthesis request paramter " + key + " containst value  with invalid type. Should be a Boolean or String");
        return false;
    }

    private static boolean verifyFloatBundleParam(Bundle bundle, String key) {
        if (!bundle.containsKey(key) || (bundle.get(key) instanceof Float) || (bundle.get(key) instanceof Double)) {
            return true;
        }
        bundle.remove(key);
        Log.w(TAG, "Synthesis request paramter " + key + " containst value  with invalid type. Should be a Float or a Double");
        return false;
    }

    private void copyStringParam(Bundle bundle, HashMap<String, String> params, String key) {
        String value = params.get(key);
        if (value != null) {
            bundle.putString(key, value);
        }
    }

    private void copyIntParam(Bundle bundle, HashMap<String, String> params, String key) {
        String valueString = params.get(key);
        if (!TextUtils.isEmpty(valueString)) {
            try {
                bundle.putInt(key, Integer.parseInt(valueString));
            } catch (NumberFormatException e) {
            }
        }
    }

    private void copyFloatParam(Bundle bundle, HashMap<String, String> params, String key) {
        String valueString = params.get(key);
        if (!TextUtils.isEmpty(valueString)) {
            try {
                bundle.putFloat(key, Float.parseFloat(valueString));
            } catch (NumberFormatException e) {
            }
        }
    }

    @Deprecated
    public int setOnUtteranceCompletedListener(OnUtteranceCompletedListener listener) {
        this.mUtteranceProgressListener = UtteranceProgressListener.from(listener);
        return 0;
    }

    public int setOnUtteranceProgressListener(UtteranceProgressListener listener) {
        this.mUtteranceProgressListener = listener;
        return 0;
    }

    @Deprecated
    public int setEngineByPackageName(String enginePackageName) {
        this.mRequestedEngine = enginePackageName;
        return initTts();
    }

    public String getDefaultEngine() {
        return this.mEnginesHelper.getDefaultEngine();
    }

    @Deprecated
    public boolean areDefaultsEnforced() {
        return false;
    }

    public List<EngineInfo> getEngines() {
        return this.mEnginesHelper.getEngines();
    }

    public static int getMaxSpeechInputLength() {
        return 4000;
    }
}
