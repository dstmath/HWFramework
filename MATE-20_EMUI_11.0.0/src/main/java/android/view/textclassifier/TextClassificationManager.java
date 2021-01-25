package android.view.textclassifier;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.content.Context;
import android.database.ContentObserver;
import android.os.ServiceManager;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.service.textclassifier.TextClassifierService;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import java.lang.ref.WeakReference;
import java.util.function.Supplier;

public final class TextClassificationManager {
    private static final String LOG_TAG = "TextClassificationManager";
    private static final TextClassificationConstants sDefaultSettings = new TextClassificationConstants($$Lambda$TextClassificationManager$VwZ4EV_1i6FbjO7TtyaAnFL3oe0.INSTANCE);
    private final Context mContext;
    @GuardedBy({"mLock"})
    private TextClassifier mCustomTextClassifier;
    private final TextClassificationSessionFactory mDefaultSessionFactory = new TextClassificationSessionFactory() {
        /* class android.view.textclassifier.$$Lambda$TextClassificationManager$SIydN2POphTO3AmPTLEMmXPLSKY */

        @Override // android.view.textclassifier.TextClassificationSessionFactory
        public final TextClassifier createTextClassificationSession(TextClassificationContext textClassificationContext) {
            return TextClassificationManager.this.lambda$new$1$TextClassificationManager(textClassificationContext);
        }
    };
    @GuardedBy({"mLock"})
    private TextClassifier mLocalTextClassifier;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private TextClassificationSessionFactory mSessionFactory;
    @GuardedBy({"mLock"})
    private TextClassificationConstants mSettings;
    private final SettingsObserver mSettingsObserver;
    @GuardedBy({"mLock"})
    private TextClassifier mSystemTextClassifier;

    static /* synthetic */ String lambda$static$0() {
        return null;
    }

    public /* synthetic */ TextClassifier lambda$new$1$TextClassificationManager(TextClassificationContext classificationContext) {
        return new TextClassificationSession(classificationContext, getTextClassifier());
    }

    public TextClassificationManager(Context context) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mSessionFactory = this.mDefaultSessionFactory;
        this.mSettingsObserver = new SettingsObserver(this);
    }

    public TextClassifier getTextClassifier() {
        synchronized (this.mLock) {
            if (this.mCustomTextClassifier != null) {
                return this.mCustomTextClassifier;
            } else if (isSystemTextClassifierEnabled()) {
                return getSystemTextClassifier();
            } else {
                return getLocalTextClassifier();
            }
        }
    }

    public void setTextClassifier(TextClassifier textClassifier) {
        synchronized (this.mLock) {
            this.mCustomTextClassifier = textClassifier;
        }
    }

    @UnsupportedAppUsage
    public TextClassifier getTextClassifier(int type) {
        if (type != 0) {
            return getSystemTextClassifier();
        }
        return getLocalTextClassifier();
    }

    private TextClassificationConstants getSettings() {
        TextClassificationConstants textClassificationConstants;
        synchronized (this.mLock) {
            if (this.mSettings == null) {
                this.mSettings = new TextClassificationConstants(new Supplier() {
                    /* class android.view.textclassifier.$$Lambda$TextClassificationManager$oweIEhDWxy3_0kZSXp3oRbSuNW4 */

                    @Override // java.util.function.Supplier
                    public final Object get() {
                        return TextClassificationManager.this.lambda$getSettings$2$TextClassificationManager();
                    }
                });
            }
            textClassificationConstants = this.mSettings;
        }
        return textClassificationConstants;
    }

    public /* synthetic */ String lambda$getSettings$2$TextClassificationManager() {
        return Settings.Global.getString(getApplicationContext().getContentResolver(), Settings.Global.TEXT_CLASSIFIER_CONSTANTS);
    }

    public TextClassifier createTextClassificationSession(TextClassificationContext classificationContext) {
        Preconditions.checkNotNull(classificationContext);
        TextClassifier textClassifier = this.mSessionFactory.createTextClassificationSession(classificationContext);
        Preconditions.checkNotNull(textClassifier, "Session Factory should never return null");
        return textClassifier;
    }

    public TextClassifier createTextClassificationSession(TextClassificationContext classificationContext, TextClassifier textClassifier) {
        Preconditions.checkNotNull(classificationContext);
        Preconditions.checkNotNull(textClassifier);
        return new TextClassificationSession(classificationContext, textClassifier);
    }

    public void setTextClassificationSessionFactory(TextClassificationSessionFactory factory) {
        synchronized (this.mLock) {
            if (factory != null) {
                this.mSessionFactory = factory;
            } else {
                this.mSessionFactory = this.mDefaultSessionFactory;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mSettingsObserver != null) {
                getApplicationContext().getContentResolver().unregisterContentObserver(this.mSettingsObserver);
                DeviceConfig.removeOnPropertiesChangedListener(this.mSettingsObserver);
            }
        } finally {
            super.finalize();
        }
    }

    private TextClassifier getSystemTextClassifier() {
        synchronized (this.mLock) {
            if (this.mSystemTextClassifier == null && isSystemTextClassifierEnabled()) {
                try {
                    this.mSystemTextClassifier = new SystemTextClassifier(this.mContext, getSettings());
                    Log.d(LOG_TAG, "Initialized SystemTextClassifier");
                } catch (ServiceManager.ServiceNotFoundException e) {
                    Log.e(LOG_TAG, "Could not initialize SystemTextClassifier", e);
                }
            }
        }
        TextClassifier textClassifier = this.mSystemTextClassifier;
        if (textClassifier != null) {
            return textClassifier;
        }
        return TextClassifier.NO_OP;
    }

    private TextClassifier getLocalTextClassifier() {
        TextClassifier textClassifier;
        synchronized (this.mLock) {
            if (this.mLocalTextClassifier == null) {
                if (getSettings().isLocalTextClassifierEnabled()) {
                    this.mLocalTextClassifier = new TextClassifierImpl(this.mContext, getSettings(), TextClassifier.NO_OP);
                } else {
                    Log.d(LOG_TAG, "Local TextClassifier disabled");
                    this.mLocalTextClassifier = TextClassifier.NO_OP;
                }
            }
            textClassifier = this.mLocalTextClassifier;
        }
        return textClassifier;
    }

    private boolean isSystemTextClassifierEnabled() {
        return getSettings().isSystemTextClassifierEnabled() && TextClassifierService.getServiceComponentName(this.mContext) != null;
    }

    @VisibleForTesting
    public void invalidateForTesting() {
        invalidate();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void invalidate() {
        synchronized (this.mLock) {
            this.mSettings = null;
            this.mLocalTextClassifier = null;
            this.mSystemTextClassifier = null;
        }
    }

    /* access modifiers changed from: package-private */
    public Context getApplicationContext() {
        if (this.mContext.getApplicationContext() != null) {
            return this.mContext.getApplicationContext();
        }
        return this.mContext;
    }

    public void dump(IndentingPrintWriter pw) {
        getLocalTextClassifier().dump(pw);
        getSystemTextClassifier().dump(pw);
        getSettings().dump(pw);
    }

    public static TextClassificationConstants getSettings(Context context) {
        Preconditions.checkNotNull(context);
        TextClassificationManager tcm = (TextClassificationManager) context.getSystemService(TextClassificationManager.class);
        if (tcm != null) {
            return tcm.getSettings();
        }
        return sDefaultSettings;
    }

    private static final class SettingsObserver extends ContentObserver implements DeviceConfig.OnPropertiesChangedListener {
        private final WeakReference<TextClassificationManager> mTcm;

        SettingsObserver(TextClassificationManager tcm) {
            super(null);
            this.mTcm = new WeakReference<>(tcm);
            tcm.getApplicationContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.Global.TEXT_CLASSIFIER_CONSTANTS), false, this);
            DeviceConfig.addOnPropertiesChangedListener(DeviceConfig.NAMESPACE_TEXTCLASSIFIER, ActivityThread.currentApplication().getMainExecutor(), this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            invalidateSettings();
        }

        @Override // android.provider.DeviceConfig.OnPropertiesChangedListener
        public void onPropertiesChanged(DeviceConfig.Properties properties) {
            invalidateSettings();
        }

        private void invalidateSettings() {
            TextClassificationManager tcm = this.mTcm.get();
            if (tcm != null) {
                tcm.invalidate();
            }
        }
    }
}
