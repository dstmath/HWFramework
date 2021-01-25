package com.android.internal.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.accessibility.AccessibilityShortcutController;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.function.pooled.PooledLambda;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class AccessibilityShortcutController {
    private static final int A11Y_SHORTCUT_KEY_TIMEOUT = 3;
    public static final ComponentName COLOR_INVERSION_COMPONENT_NAME = new ComponentName("com.android.server.accessibility", "ColorInversion");
    public static final ComponentName DALTONIZER_COMPONENT_NAME = new ComponentName("com.android.server.accessibility", "Daltonizer");
    private static final boolean IS_SIDE_PROP = (!"".equals(SystemProperties.get("ro.config.hw_curved_side_disp", "")));
    private static final String PKG_SCREENREADER = "com.bjbyhd.screenreader_huawei";
    private static final String PKG_TALKBACK = "com.google.android.marvin.talkback";
    private static final String SCREEN_READER_SERVICE = "com.bjbyhd.screenreader_huawei/.ScreenReaderService";
    private static final String TAG = "AccessibilityShortcutController";
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(11).build();
    private static Map<ComponentName, ToggleableFrameworkFeatureInfo> sFrameworkShortcutFeaturesMap;
    private AlertDialog mAlertDialog;
    private final Context mContext;
    private boolean mEnabledOnLockScreen;
    public FrameworkObjectProvider mFrameworkObjectProvider = new FrameworkObjectProvider();
    private final Handler mHandler;
    private boolean mIsShortcutEnabled;
    private int mUserId;

    public static String getTargetServiceComponentNameString(Context context, int userId) {
        String currentShortcutServiceId = Settings.Secure.getStringForUser(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_SHORTCUT_TARGET_SERVICE, userId);
        if (currentShortcutServiceId != null) {
            return currentShortcutServiceId;
        }
        if (checkPackageInstalled(context, PKG_TALKBACK)) {
            return context.getString(R.string.config_defaultAccessibilityService);
        }
        if (checkPackageInstalled(context, PKG_SCREENREADER)) {
            return SCREEN_READER_SERVICE;
        }
        Slog.w(TAG, "talkback and screenreader both not exists");
        return "";
    }

    public static Map<ComponentName, ToggleableFrameworkFeatureInfo> getFrameworkShortcutFeaturesMap() {
        if (sFrameworkShortcutFeaturesMap == null) {
            Map<ComponentName, ToggleableFrameworkFeatureInfo> featuresMap = new ArrayMap<>(2);
            featuresMap.put(COLOR_INVERSION_COMPONENT_NAME, new ToggleableFrameworkFeatureInfo(Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, "1", WifiEnterpriseConfig.ENGINE_DISABLE, R.string.color_inversion_feature_name));
            featuresMap.put(DALTONIZER_COMPONENT_NAME, new ToggleableFrameworkFeatureInfo(Settings.Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED, "1", WifiEnterpriseConfig.ENGINE_DISABLE, R.string.color_correction_feature_name));
            sFrameworkShortcutFeaturesMap = Collections.unmodifiableMap(featuresMap);
        }
        return sFrameworkShortcutFeaturesMap;
    }

    public AccessibilityShortcutController(Context context, Handler handler, int initialUserId) {
        this.mContext = context;
        this.mHandler = handler;
        this.mUserId = initialUserId;
        ContentObserver co = new ContentObserver(handler) {
            /* class com.android.internal.accessibility.AccessibilityShortcutController.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange, Uri uri, int userId) {
                if (userId == AccessibilityShortcutController.this.mUserId) {
                    AccessibilityShortcutController.this.onSettingsChanged();
                }
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ACCESSIBILITY_SHORTCUT_TARGET_SERVICE), false, co, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ACCESSIBILITY_SHORTCUT_ENABLED), false, co, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ACCESSIBILITY_SHORTCUT_ON_LOCK_SCREEN), false, co, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ACCESSIBILITY_SHORTCUT_DIALOG_SHOWN), false, co, -1);
        setCurrentUser(this.mUserId);
    }

    public void setCurrentUser(int currentUserId) {
        this.mUserId = currentUserId;
        onSettingsChanged();
    }

    public boolean isAccessibilityShortcutAvailable(boolean phoneLocked) {
        return this.mIsShortcutEnabled && (!phoneLocked || this.mEnabledOnLockScreen);
    }

    public void onSettingsChanged() {
        boolean z = true;
        boolean haveValidService = !TextUtils.isEmpty(getTargetServiceComponentNameString(this.mContext, this.mUserId));
        ContentResolver cr = this.mContext.getContentResolver();
        boolean enabled = Settings.Secure.getIntForUser(cr, Settings.Secure.ACCESSIBILITY_SHORTCUT_ENABLED, 1, this.mUserId) == 1;
        this.mEnabledOnLockScreen = Settings.Secure.getIntForUser(cr, Settings.Secure.ACCESSIBILITY_SHORTCUT_ON_LOCK_SCREEN, Settings.Secure.getIntForUser(cr, Settings.Secure.ACCESSIBILITY_SHORTCUT_DIALOG_SHOWN, 0, this.mUserId), this.mUserId) == 1;
        if (!enabled || !haveValidService) {
            z = false;
        }
        this.mIsShortcutEnabled = z;
    }

    public void performAccessibilityShortcut() {
        int i;
        Slog.d(TAG, "Accessibility shortcut activated");
        ContentResolver cr = this.mContext.getContentResolver();
        int userId = ActivityManager.getCurrentUser();
        int dialogAlreadyShown = Settings.Secure.getIntForUser(cr, Settings.Secure.ACCESSIBILITY_SHORTCUT_DIALOG_SHOWN, 0, userId);
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(ArrayUtils.convertToLongArray(this.mContext.getResources().getIntArray(R.array.config_longPressVibePattern)), -1, VIBRATION_ATTRIBUTES);
        }
        if (dialogAlreadyShown == 0) {
            this.mAlertDialog = createShortcutWarningDialog(userId);
            AlertDialog alertDialog = this.mAlertDialog;
            if (alertDialog != null) {
                if (!performTtsPrompt(alertDialog)) {
                    playNotificationTone();
                }
                Window w = this.mAlertDialog.getWindow();
                WindowManager.LayoutParams attr = w.getAttributes();
                attr.type = 2009;
                w.setAttributes(attr);
                this.mAlertDialog.show();
                Settings.Secure.putIntForUser(cr, Settings.Secure.ACCESSIBILITY_SHORTCUT_DIALOG_SHOWN, 1, userId);
                return;
            }
            return;
        }
        playNotificationTone();
        AlertDialog alertDialog2 = this.mAlertDialog;
        if (alertDialog2 != null) {
            alertDialog2.dismiss();
            this.mAlertDialog = null;
        }
        String serviceName = getShortcutFeatureDescription(false);
        if (serviceName == null) {
            Slog.e(TAG, "Accessibility shortcut set to invalid service");
            return;
        }
        AccessibilityServiceInfo serviceInfo = getInfoForTargetService();
        if (serviceInfo != null) {
            Context context = this.mContext;
            if (isServiceEnabled(serviceInfo)) {
                i = R.string.accessibility_shortcut_disabling_service;
            } else {
                i = R.string.accessibility_shortcut_enabling_service;
            }
            Toast warningToast = this.mFrameworkObjectProvider.makeToastFromText(this.mContext, String.format(context.getString(i), serviceName), 1);
            warningToast.getWindowParams().privateFlags |= 16;
            warningToast.show();
        }
        this.mFrameworkObjectProvider.getAccessibilityManagerInstance(this.mContext).performAccessibilityShortcut();
    }

    private AlertDialog createShortcutWarningDialog(int userId) {
        String serviceDescription = getShortcutFeatureDescription(true);
        if (serviceDescription == null) {
            return null;
        }
        String message = IS_SIDE_PROP ? String.format(this.mContext.getString(com.android.hwext.internal.R.string.accessibility_shortcut_toogle_warning_no_volume), serviceDescription) : String.format(this.mContext.getString(com.android.hwext.internal.R.string.accessibility_shortcut_dialog_content), 3, serviceDescription);
        int themeId = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
        FrameworkObjectProvider frameworkObjectProvider = this.mFrameworkObjectProvider;
        return frameworkObjectProvider.getAlertDialogBuilder(frameworkObjectProvider.getSystemUiContext(), themeId).setTitle(R.string.accessibility_shortcut_warning_dialog_title).setMessage(message).setCancelable(false).setPositiveButton(R.string.leave_accessibility_shortcut_on, (DialogInterface.OnClickListener) null).setNegativeButton(R.string.disable_accessibility_shortcut, new DialogInterface.OnClickListener(userId) {
            /* class com.android.internal.accessibility.$$Lambda$AccessibilityShortcutController$2NcDVJHkpsPbwr45v1_NfIM8row */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                AccessibilityShortcutController.this.lambda$createShortcutWarningDialog$0$AccessibilityShortcutController(this.f$1, dialogInterface, i);
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener(userId) {
            /* class com.android.internal.accessibility.$$Lambda$AccessibilityShortcutController$T96D356n5VObNOonEIYV8s83Fc */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.content.DialogInterface.OnCancelListener
            public final void onCancel(DialogInterface dialogInterface) {
                AccessibilityShortcutController.this.lambda$createShortcutWarningDialog$1$AccessibilityShortcutController(this.f$1, dialogInterface);
            }
        }).create();
    }

    public /* synthetic */ void lambda$createShortcutWarningDialog$0$AccessibilityShortcutController(int userId, DialogInterface d, int which) {
        Settings.Secure.putStringForUser(this.mContext.getContentResolver(), Settings.Secure.ACCESSIBILITY_SHORTCUT_TARGET_SERVICE, "", userId);
    }

    public /* synthetic */ void lambda$createShortcutWarningDialog$1$AccessibilityShortcutController(int userId, DialogInterface d) {
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), Settings.Secure.ACCESSIBILITY_SHORTCUT_DIALOG_SHOWN, 0, userId);
    }

    private AccessibilityServiceInfo getInfoForTargetService() {
        String currentShortcutServiceString = getTargetServiceComponentNameString(this.mContext, -2);
        if (currentShortcutServiceString == null) {
            return null;
        }
        return this.mFrameworkObjectProvider.getAccessibilityManagerInstance(this.mContext).getInstalledServiceInfoWithComponentName(ComponentName.unflattenFromString(currentShortcutServiceString));
    }

    private String getShortcutFeatureDescription(boolean includeSummary) {
        String currentShortcutServiceString = getTargetServiceComponentNameString(this.mContext, -2);
        if (currentShortcutServiceString == null) {
            return null;
        }
        ComponentName targetComponentName = ComponentName.unflattenFromString(currentShortcutServiceString);
        ToggleableFrameworkFeatureInfo frameworkFeatureInfo = getFrameworkShortcutFeaturesMap().get(targetComponentName);
        if (frameworkFeatureInfo != null) {
            return frameworkFeatureInfo.getLabel(this.mContext);
        }
        AccessibilityServiceInfo serviceInfo = this.mFrameworkObjectProvider.getAccessibilityManagerInstance(this.mContext).getInstalledServiceInfoWithComponentName(targetComponentName);
        if (serviceInfo == null) {
            return null;
        }
        PackageManager pm = this.mContext.getPackageManager();
        String label = serviceInfo.getResolveInfo().loadLabel(pm).toString();
        CharSequence summary = serviceInfo.loadSummary(pm);
        if (!includeSummary || TextUtils.isEmpty(summary)) {
            return label;
        }
        return String.format("%s\n%s", label, summary);
    }

    private boolean isServiceEnabled(AccessibilityServiceInfo serviceInfo) {
        return this.mFrameworkObjectProvider.getAccessibilityManagerInstance(this.mContext).getEnabledAccessibilityServiceList(-1).contains(serviceInfo);
    }

    private boolean hasFeatureLeanback() {
        return this.mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void playNotificationTone() {
        int audioAttributesUsage;
        if (hasFeatureLeanback()) {
            audioAttributesUsage = 11;
        } else {
            audioAttributesUsage = 10;
        }
        Ringtone tone = this.mFrameworkObjectProvider.getRingtone(this.mContext, Settings.System.DEFAULT_NOTIFICATION_URI);
        if (tone != null) {
            tone.setAudioAttributes(new AudioAttributes.Builder().setUsage(audioAttributesUsage).build());
            tone.play();
        }
    }

    private boolean performTtsPrompt(AlertDialog alertDialog) {
        String serviceName = getShortcutFeatureDescription(false);
        AccessibilityServiceInfo serviceInfo = getInfoForTargetService();
        if (TextUtils.isEmpty(serviceName) || serviceInfo == null || (serviceInfo.flags & 1024) == 0) {
            return false;
        }
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.android.internal.accessibility.$$Lambda$AccessibilityShortcutController$cQtLiNhDc4H3BvMBZy00zj21oKg */

            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(DialogInterface dialogInterface) {
                AccessibilityShortcutController.TtsPrompt.this.dismiss();
            }
        });
        return true;
    }

    /* access modifiers changed from: private */
    public class TtsPrompt implements TextToSpeech.OnInitListener {
        private boolean mDismiss;
        private final CharSequence mText;
        private TextToSpeech mTts;

        TtsPrompt(String serviceName) {
            this.mText = AccessibilityShortcutController.this.mContext.getString(R.string.accessibility_shortcut_spoken_feedback, serviceName);
            this.mTts = AccessibilityShortcutController.this.mFrameworkObjectProvider.getTextToSpeech(AccessibilityShortcutController.this.mContext, this);
        }

        public void dismiss() {
            this.mDismiss = true;
            AccessibilityShortcutController.this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$qdzoyIBhDB17ZFWPp1Rf8ICvR8.INSTANCE, this.mTts));
        }

        @Override // android.speech.tts.TextToSpeech.OnInitListener
        public void onInit(int status) {
            if (status != 0) {
                Slog.d(AccessibilityShortcutController.TAG, "Tts init fail, status=" + Integer.toString(status));
                AccessibilityShortcutController.this.playNotificationTone();
                return;
            }
            AccessibilityShortcutController.this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityShortcutController$TtsPrompt$HwizF4cvqRFiaqAcMrC7W8y6zYA.INSTANCE, this));
        }

        /* access modifiers changed from: private */
        public void play() {
            if (!this.mDismiss) {
                int status = -1;
                if (setLanguage(Locale.getDefault())) {
                    status = this.mTts.speak(this.mText, 0, null, null);
                }
                if (status != 0) {
                    Slog.d(AccessibilityShortcutController.TAG, "Tts play fail");
                    AccessibilityShortcutController.this.playNotificationTone();
                }
            }
        }

        private boolean setLanguage(Locale locale) {
            int status = this.mTts.isLanguageAvailable(locale);
            if (status == -1 || status == -2) {
                return false;
            }
            this.mTts.setLanguage(locale);
            Voice voice = this.mTts.getVoice();
            if (voice == null || (voice.getFeatures() != null && voice.getFeatures().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED))) {
                return false;
            }
            return true;
        }
    }

    public static class ToggleableFrameworkFeatureInfo {
        private int mIconDrawableId;
        private final int mLabelStringResourceId;
        private final String mSettingKey;
        private final String mSettingOffValue;
        private final String mSettingOnValue;

        ToggleableFrameworkFeatureInfo(String settingKey, String settingOnValue, String settingOffValue, int labelStringResourceId) {
            this.mSettingKey = settingKey;
            this.mSettingOnValue = settingOnValue;
            this.mSettingOffValue = settingOffValue;
            this.mLabelStringResourceId = labelStringResourceId;
        }

        public String getSettingKey() {
            return this.mSettingKey;
        }

        public String getSettingOnValue() {
            return this.mSettingOnValue;
        }

        public String getSettingOffValue() {
            return this.mSettingOffValue;
        }

        public String getLabel(Context context) {
            return context.getString(this.mLabelStringResourceId);
        }
    }

    public static class FrameworkObjectProvider {
        public AccessibilityManager getAccessibilityManagerInstance(Context context) {
            return AccessibilityManager.getInstance(context);
        }

        public AlertDialog.Builder getAlertDialogBuilder(Context context) {
            return new AlertDialog.Builder(context);
        }

        public AlertDialog.Builder getAlertDialogBuilder(Context context, int themeResId) {
            return new AlertDialog.Builder(context, themeResId);
        }

        public Toast makeToastFromText(Context context, CharSequence charSequence, int duration) {
            return Toast.makeText(context, charSequence, duration);
        }

        public Context getSystemUiContext() {
            return ActivityThread.currentActivityThread().getSystemUiContext();
        }

        public TextToSpeech getTextToSpeech(Context ctx, TextToSpeech.OnInitListener listener) {
            return new TextToSpeech(ctx, listener);
        }

        public Ringtone getRingtone(Context ctx, Uri uri) {
            return RingtoneManager.getRingtone(ctx, uri);
        }
    }

    private static boolean checkPackageInstalled(Context context, String packageName) {
        boolean isInstalled = true;
        try {
            context.getPackageManager().getPackageInfo(packageName, 128);
        } catch (PackageManager.NameNotFoundException e) {
            isInstalled = false;
        }
        Slog.i(TAG, "checkPackageInstalled packageName:" + packageName + ",isInstalled:" + isInstalled);
        return isInstalled;
    }
}
