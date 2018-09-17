package com.android.server.policy;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Slog;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

public class AccessibilityShortcutController {
    private static final String TAG = "AccessibilityShortcutController";
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new Builder().setContentType(4).setUsage(11).build();
    private AlertDialog mAlertDialog;
    private final Context mContext;
    private boolean mEnabledOnLockScreen;
    public FrameworkObjectProvider mFrameworkObjectProvider = new FrameworkObjectProvider();
    private boolean mIsShortcutEnabled;
    private int mUserId;

    public static class FrameworkObjectProvider {
        public AccessibilityManager getAccessibilityManagerInstance(Context context) {
            return AccessibilityManager.getInstance(context);
        }

        public AlertDialog.Builder getAlertDialogBuilder(Context context) {
            return new AlertDialog.Builder(context);
        }

        public Toast makeToastFromText(Context context, CharSequence charSequence, int duration) {
            return Toast.makeText(context, charSequence, duration);
        }
    }

    public static String getTargetServiceComponentNameString(Context context, int userId) {
        String currentShortcutServiceId = Secure.getStringForUser(context.getContentResolver(), "accessibility_shortcut_target_service", userId);
        if (currentShortcutServiceId != null) {
            return currentShortcutServiceId;
        }
        return context.getString(17039765);
    }

    public AccessibilityShortcutController(Context context, Handler handler, int initialUserId) {
        this.mContext = context;
        ContentObserver co = new ContentObserver(handler) {
            public void onChange(boolean selfChange, Uri uri, int userId) {
                if (userId == AccessibilityShortcutController.this.mUserId) {
                    AccessibilityShortcutController.this.onSettingsChanged();
                }
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_shortcut_target_service"), false, co, -1);
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_shortcut_enabled"), false, co, -1);
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_shortcut_on_lock_screen"), false, co, -1);
        setCurrentUser(this.mUserId);
    }

    public void setCurrentUser(int currentUserId) {
        this.mUserId = currentUserId;
        onSettingsChanged();
    }

    public boolean isAccessibilityShortcutAvailable(boolean phoneLocked) {
        if (this.mIsShortcutEnabled) {
            return phoneLocked ? this.mEnabledOnLockScreen : true;
        } else {
            return false;
        }
    }

    public void onSettingsChanged() {
        boolean z = true;
        boolean haveValidService = TextUtils.isEmpty(getTargetServiceComponentNameString(this.mContext, this.mUserId)) ^ 1;
        ContentResolver cr = this.mContext.getContentResolver();
        boolean enabled = Secure.getIntForUser(cr, "accessibility_shortcut_enabled", 1, this.mUserId) == 1;
        if (Secure.getIntForUser(cr, "accessibility_shortcut_on_lock_screen", 0, this.mUserId) != 1) {
            z = false;
        }
        this.mEnabledOnLockScreen = z;
        if (!enabled) {
            haveValidService = false;
        }
        this.mIsShortcutEnabled = haveValidService;
    }

    public void performAccessibilityShortcut() {
        Slog.d(TAG, "Accessibility shortcut activated");
        ContentResolver cr = this.mContext.getContentResolver();
        int userId = ActivityManager.getCurrentUser();
        int dialogAlreadyShown = Secure.getIntForUser(cr, "accessibility_shortcut_dialog_shown", 0, userId);
        Ringtone tone = RingtoneManager.getRingtone(this.mContext, System.DEFAULT_NOTIFICATION_URI);
        if (tone != null) {
            tone.setAudioAttributes(new Builder().setUsage(10).build());
            tone.play();
        }
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(PhoneWindowManager.getLongIntArray(this.mContext.getResources(), 17236026), -1, VIBRATION_ATTRIBUTES);
        }
        if (dialogAlreadyShown == 0) {
            this.mAlertDialog = createShortcutWarningDialog(userId);
            if (this.mAlertDialog != null) {
                Window w = this.mAlertDialog.getWindow();
                LayoutParams attr = w.getAttributes();
                attr.type = 2009;
                w.setAttributes(attr);
                this.mAlertDialog.show();
                Secure.putIntForUser(cr, "accessibility_shortcut_dialog_shown", 1, userId);
            } else {
                return;
            }
        }
        if (this.mAlertDialog != null) {
            this.mAlertDialog.dismiss();
            this.mAlertDialog = null;
        }
        AccessibilityServiceInfo serviceInfo = getInfoForTargetService();
        if (serviceInfo == null) {
            Slog.e(TAG, "Accessibility shortcut set to invalid service");
            return;
        }
        int i;
        Context context = this.mContext;
        if (isServiceEnabled(serviceInfo)) {
            i = 17039527;
        } else {
            i = 17039528;
        }
        Toast warningToast = this.mFrameworkObjectProvider.makeToastFromText(this.mContext, String.format(context.getString(i), new Object[]{serviceInfo.getResolveInfo().loadLabel(this.mContext.getPackageManager()).toString()}), 1);
        LayoutParams windowParams = warningToast.getWindowParams();
        windowParams.privateFlags |= 16;
        warningToast.show();
        this.mFrameworkObjectProvider.getAccessibilityManagerInstance(this.mContext).performAccessibilityShortcut();
    }

    private AlertDialog createShortcutWarningDialog(int userId) {
        if (getInfoForTargetService() == null) {
            return null;
        }
        return new AlertDialog.Builder(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null)).setTitle(17039530).setMessage(String.format(this.mContext.getString(17039529), new Object[]{Integer.valueOf(3), serviceInfo.getResolveInfo().loadLabel(this.mContext.getPackageManager()).toString()})).setCancelable(false).setPositiveButton(17040253, null).setNegativeButton(17039896, new com.android.server.policy.-$Lambda$k6uOVlk6EqgDgfUMuhedgW8Qb2I.AnonymousClass1(userId, this)).setOnCancelListener(new -$Lambda$k6uOVlk6EqgDgfUMuhedgW8Qb2I(userId, this)).create();
    }

    /* synthetic */ void lambda$-com_android_server_policy_AccessibilityShortcutController_9607(int userId, DialogInterface d, int which) {
        Secure.putStringForUser(this.mContext.getContentResolver(), "accessibility_shortcut_target_service", "", userId);
    }

    /* synthetic */ void lambda$-com_android_server_policy_AccessibilityShortcutController_9939(int userId, DialogInterface d) {
        Secure.putIntForUser(this.mContext.getContentResolver(), "accessibility_shortcut_dialog_shown", 0, userId);
    }

    private AccessibilityServiceInfo getInfoForTargetService() {
        String currentShortcutServiceString = getTargetServiceComponentNameString(this.mContext, -2);
        if (currentShortcutServiceString == null) {
            return null;
        }
        return this.mFrameworkObjectProvider.getAccessibilityManagerInstance(this.mContext).getInstalledServiceInfoWithComponentName(ComponentName.unflattenFromString(currentShortcutServiceString));
    }

    private boolean isServiceEnabled(AccessibilityServiceInfo serviceInfo) {
        return this.mFrameworkObjectProvider.getAccessibilityManagerInstance(this.mContext).getEnabledAccessibilityServiceList(-1).contains(serviceInfo);
    }
}
