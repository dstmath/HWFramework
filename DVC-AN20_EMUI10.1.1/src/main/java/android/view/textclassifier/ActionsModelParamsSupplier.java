package android.view.textclassifier;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.KeyValueListParser;
import android.view.textclassifier.ModelFileManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Supplier;

public final class ActionsModelParamsSupplier implements Supplier<ActionsModelParams> {
    @VisibleForTesting
    static final String KEY_REQUIRED_LOCALES = "required_locales";
    @VisibleForTesting
    static final String KEY_REQUIRED_MODEL_VERSION = "required_model_version";
    @VisibleForTesting
    static final String KEY_SERIALIZED_PRECONDITIONS = "serialized_preconditions";
    private static final String TAG = "androidtc";
    @GuardedBy({"mLock"})
    private ActionsModelParams mActionsModelParams;
    private final Context mAppContext;
    private final Object mLock = new Object();
    private final Runnable mOnChangedListener;
    @GuardedBy({"mLock"})
    private boolean mParsed = true;
    private final SettingsObserver mSettingsObserver;

    public ActionsModelParamsSupplier(Context context, Runnable onChangedListener) {
        Context appContext = ((Context) Preconditions.checkNotNull(context)).getApplicationContext();
        this.mAppContext = appContext != null ? appContext : context;
        this.mOnChangedListener = onChangedListener == null ? $$Lambda$ActionsModelParamsSupplier$GCXILXtg_S2la6x__ANOhbYxetw.INSTANCE : onChangedListener;
        this.mSettingsObserver = new SettingsObserver(this.mAppContext, new Runnable() {
            /* class android.view.textclassifier.$$Lambda$ActionsModelParamsSupplier$zElxNeuL3A8paTXvw8GWdpp4rFo */

            public final void run() {
                ActionsModelParamsSupplier.this.lambda$new$1$ActionsModelParamsSupplier();
            }
        });
    }

    static /* synthetic */ void lambda$new$0() {
    }

    public /* synthetic */ void lambda$new$1$ActionsModelParamsSupplier() {
        synchronized (this.mLock) {
            Log.v("androidtc", "Settings.Global.TEXT_CLASSIFIER_ACTION_MODEL_PARAMS is updated");
            this.mParsed = true;
            this.mOnChangedListener.run();
        }
    }

    @Override // java.util.function.Supplier
    public ActionsModelParams get() {
        synchronized (this.mLock) {
            if (this.mParsed) {
                this.mActionsModelParams = parse(this.mAppContext.getContentResolver());
                this.mParsed = false;
            }
        }
        return this.mActionsModelParams;
    }

    private ActionsModelParams parse(ContentResolver contentResolver) {
        String settingStr = Settings.Global.getString(contentResolver, Settings.Global.TEXT_CLASSIFIER_ACTION_MODEL_PARAMS);
        if (TextUtils.isEmpty(settingStr)) {
            return ActionsModelParams.INVALID;
        }
        try {
            KeyValueListParser keyValueListParser = new KeyValueListParser(',');
            keyValueListParser.setString(settingStr);
            int version = keyValueListParser.getInt(KEY_REQUIRED_MODEL_VERSION, -1);
            if (version == -1) {
                Log.w("androidtc", "ActionsModelParams.Parse, invalid model version");
                return ActionsModelParams.INVALID;
            }
            String locales = keyValueListParser.getString(KEY_REQUIRED_LOCALES, null);
            if (locales == null) {
                Log.w("androidtc", "ActionsModelParams.Parse, invalid locales");
                return ActionsModelParams.INVALID;
            }
            String serializedPreconditionsStr = keyValueListParser.getString(KEY_SERIALIZED_PRECONDITIONS, null);
            if (serializedPreconditionsStr != null) {
                return new ActionsModelParams(version, locales, Base64.decode(serializedPreconditionsStr, 2));
            }
            Log.w("androidtc", "ActionsModelParams.Parse, invalid preconditions");
            return ActionsModelParams.INVALID;
        } catch (Throwable t) {
            Log.e("androidtc", "Invalid TEXT_CLASSIFIER_ACTION_MODEL_PARAMS, ignore", t);
            return ActionsModelParams.INVALID;
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
        } finally {
            super.finalize();
        }
    }

    public static final class ActionsModelParams {
        public static final ActionsModelParams INVALID = new ActionsModelParams(-1, "", new byte[0]);
        private final String mRequiredModelLocales;
        private final int mRequiredModelVersion;
        private final byte[] mSerializedPreconditions;

        public ActionsModelParams(int requiredModelVersion, String requiredModelLocales, byte[] serializedPreconditions) {
            this.mRequiredModelVersion = requiredModelVersion;
            this.mRequiredModelLocales = (String) Preconditions.checkNotNull(requiredModelLocales);
            this.mSerializedPreconditions = (byte[]) Preconditions.checkNotNull(serializedPreconditions);
        }

        public byte[] getSerializedPreconditions(ModelFileManager.ModelFile modelInUse) {
            if (this == INVALID) {
                return null;
            }
            int version = modelInUse.getVersion();
            int i = this.mRequiredModelVersion;
            if (version != i) {
                Log.w("androidtc", String.format("Not applying mSerializedPreconditions, required version=%d, actual=%d", Integer.valueOf(i), Integer.valueOf(modelInUse.getVersion())));
                return null;
            } else if (Objects.equals(modelInUse.getSupportedLocalesStr(), this.mRequiredModelLocales)) {
                return this.mSerializedPreconditions;
            } else {
                Log.w("androidtc", String.format("Not applying mSerializedPreconditions, required locales=%s, actual=%s", this.mRequiredModelLocales, modelInUse.getSupportedLocalesStr()));
                return null;
            }
        }
    }

    private static final class SettingsObserver extends ContentObserver {
        private final WeakReference<Runnable> mOnChangedListener;

        SettingsObserver(Context appContext, Runnable listener) {
            super(null);
            this.mOnChangedListener = new WeakReference<>(listener);
            appContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.Global.TEXT_CLASSIFIER_ACTION_MODEL_PARAMS), false, this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            if (this.mOnChangedListener.get() != null) {
                this.mOnChangedListener.get().run();
            }
        }
    }
}
