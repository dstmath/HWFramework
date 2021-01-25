package com.android.internal.app;

import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.app.ActivityTaskManager;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.metrics.LogMaker;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.telecom.PhoneAccount;
import android.util.Slog;
import android.view.View;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.telephony.PhoneConstants;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IntentForwarderActivity extends Activity {
    private static final Set<String> ALLOWED_TEXT_MESSAGE_SCHEMES = new HashSet(Arrays.asList("sms", PhoneAccount.SCHEME_SMSTO, PhoneConstants.APN_TYPE_MMS, "mmsto"));
    public static String FORWARD_INTENT_TO_MANAGED_PROFILE = "com.android.internal.app.ForwardIntentToManagedProfile";
    public static String FORWARD_INTENT_TO_PARENT = "com.android.internal.app.ForwardIntentToParent";
    @UnsupportedAppUsage
    public static String TAG = "IntentForwarderActivity";
    private static final String TEL_SCHEME = "tel";
    private Injector mInjector;
    private MetricsLogger mMetricsLogger;

    public interface Injector {
        IPackageManager getIPackageManager();

        PackageManager getPackageManager();

        UserManager getUserManager();

        ResolveInfo resolveActivityAsUser(Intent intent, int i, int i2);

        void showToast(int i, int i2);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        int targetUserId;
        int userMessageId;
        super.onCreate(savedInstanceState);
        this.mInjector = createInjector();
        Intent intentReceived = getIntent();
        String className = intentReceived.getComponent().getClassName();
        if (className.equals(FORWARD_INTENT_TO_PARENT)) {
            userMessageId = R.string.forward_intent_to_owner;
            targetUserId = getProfileParent();
            getMetricsLogger().write(new LogMaker((int) MetricsProto.MetricsEvent.ACTION_SWITCH_SHARE_PROFILE).setSubtype(1));
        } else if (className.equals(FORWARD_INTENT_TO_MANAGED_PROFILE)) {
            userMessageId = R.string.forward_intent_to_work;
            targetUserId = getManagedProfile();
            getMetricsLogger().write(new LogMaker((int) MetricsProto.MetricsEvent.ACTION_SWITCH_SHARE_PROFILE).setSubtype(2));
        } else {
            String str = TAG;
            Slog.wtf(str, IntentForwarderActivity.class.getName() + " cannot be called directly");
            userMessageId = -1;
            targetUserId = -10000;
        }
        if (targetUserId == -10000) {
            finish();
            return;
        }
        int callingUserId = getUserId();
        Intent newIntent = canForward(intentReceived, targetUserId);
        if (newIntent != null) {
            if (Intent.ACTION_CHOOSER.equals(newIntent.getAction())) {
                Intent innerIntent = (Intent) newIntent.getParcelableExtra(Intent.EXTRA_INTENT);
                innerIntent.prepareToLeaveUser(callingUserId);
                innerIntent.fixUris(callingUserId);
            } else {
                newIntent.prepareToLeaveUser(callingUserId);
            }
            ResolveInfo ri = this.mInjector.resolveActivityAsUser(newIntent, 65536, targetUserId);
            try {
                startActivityAsCaller(newIntent, null, null, false, targetUserId);
            } catch (RuntimeException e) {
                int launchedFromUid = -1;
                String launchedFromPackage = "?";
                try {
                    launchedFromUid = ActivityTaskManager.getService().getLaunchedFromUid(getActivityToken());
                    launchedFromPackage = ActivityTaskManager.getService().getLaunchedFromPackage(getActivityToken());
                } catch (RemoteException e2) {
                }
                String str2 = TAG;
                Slog.wtf(str2, "Unable to launch as UID " + launchedFromUid + " package " + launchedFromPackage + ", while running in " + ActivityThread.currentProcessName(), e);
            }
            if (shouldShowDisclosure(ri, intentReceived)) {
                this.mInjector.showToast(userMessageId, 1);
            }
        } else {
            String str3 = TAG;
            Slog.wtf(str3, "the intent: " + intentReceived + " cannot be forwarded from user " + callingUserId + " to user " + targetUserId);
        }
        finish();
    }

    private boolean shouldShowDisclosure(ResolveInfo ri, Intent intent) {
        if (ri == null || ri.activityInfo == null) {
            return true;
        }
        if (!ri.activityInfo.applicationInfo.isSystemApp() || (!isDialerIntent(intent) && !isTextMessageIntent(intent))) {
            return true ^ isTargetResolverOrChooserActivity(ri.activityInfo);
        }
        return false;
    }

    private boolean isTextMessageIntent(Intent intent) {
        return (Intent.ACTION_SENDTO.equals(intent.getAction()) || isViewActionIntent(intent)) && ALLOWED_TEXT_MESSAGE_SCHEMES.contains(intent.getScheme());
    }

    private boolean isDialerIntent(Intent intent) {
        return Intent.ACTION_DIAL.equals(intent.getAction()) || Intent.ACTION_CALL.equals(intent.getAction()) || Intent.ACTION_CALL_PRIVILEGED.equals(intent.getAction()) || Intent.ACTION_CALL_EMERGENCY.equals(intent.getAction()) || (isViewActionIntent(intent) && "tel".equals(intent.getScheme()));
    }

    private boolean isViewActionIntent(Intent intent) {
        return "android.intent.action.VIEW".equals(intent.getAction()) && intent.hasCategory(Intent.CATEGORY_BROWSABLE);
    }

    private boolean isTargetResolverOrChooserActivity(ActivityInfo activityInfo) {
        if (!"android".equals(activityInfo.packageName)) {
            return false;
        }
        if (ResolverActivity.class.getName().equals(activityInfo.name) || ChooserActivity.class.getName().equals(activityInfo.name)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public Intent canForward(Intent incomingIntent, int targetUserId) {
        Intent forwardIntent = new Intent(incomingIntent);
        forwardIntent.addFlags(View.SCROLLBARS_OUTSIDE_INSET);
        sanitizeIntent(forwardIntent);
        Intent intentToCheck = forwardIntent;
        if (Intent.ACTION_CHOOSER.equals(forwardIntent.getAction())) {
            if (forwardIntent.hasExtra(Intent.EXTRA_INITIAL_INTENTS)) {
                Slog.wtf(TAG, "An chooser intent with extra initial intents cannot be forwarded to a different user");
                return null;
            } else if (forwardIntent.hasExtra(Intent.EXTRA_REPLACEMENT_EXTRAS)) {
                Slog.wtf(TAG, "A chooser intent with replacement extras cannot be forwarded to a different user");
                return null;
            } else {
                intentToCheck = (Intent) forwardIntent.getParcelableExtra(Intent.EXTRA_INTENT);
                if (intentToCheck == null) {
                    Slog.wtf(TAG, "Cannot forward a chooser intent with no extra android.intent.extra.INTENT");
                    return null;
                }
            }
        }
        if (forwardIntent.getSelector() != null) {
            intentToCheck = forwardIntent.getSelector();
        }
        String resolvedType = intentToCheck.resolveTypeIfNeeded(getContentResolver());
        sanitizeIntent(intentToCheck);
        try {
            if (this.mInjector.getIPackageManager().canForwardTo(intentToCheck, resolvedType, getUserId(), targetUserId)) {
                return forwardIntent;
            }
            return null;
        } catch (RemoteException e) {
            Slog.e(TAG, "PackageManagerService is dead?");
        }
    }

    private int getManagedProfile() {
        for (UserInfo userInfo : this.mInjector.getUserManager().getProfiles(UserHandle.myUserId())) {
            if (userInfo.isManagedProfile()) {
                return userInfo.id;
            }
        }
        String str = TAG;
        Slog.wtf(str, FORWARD_INTENT_TO_MANAGED_PROFILE + " has been called, but there is no managed profile");
        return -10000;
    }

    private int getProfileParent() {
        UserInfo parent = this.mInjector.getUserManager().getProfileParent(UserHandle.myUserId());
        if (parent != null) {
            return parent.id;
        }
        String str = TAG;
        Slog.wtf(str, FORWARD_INTENT_TO_PARENT + " has been called, but there is no parent");
        return -10000;
    }

    private void sanitizeIntent(Intent intent) {
        intent.setPackage(null);
        intent.setComponent(null);
    }

    /* access modifiers changed from: protected */
    public MetricsLogger getMetricsLogger() {
        if (this.mMetricsLogger == null) {
            this.mMetricsLogger = new MetricsLogger();
        }
        return this.mMetricsLogger;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Injector createInjector() {
        return new InjectorImpl();
    }

    /* access modifiers changed from: private */
    public class InjectorImpl implements Injector {
        private InjectorImpl() {
        }

        @Override // com.android.internal.app.IntentForwarderActivity.Injector
        public IPackageManager getIPackageManager() {
            return AppGlobals.getPackageManager();
        }

        @Override // com.android.internal.app.IntentForwarderActivity.Injector
        public UserManager getUserManager() {
            return (UserManager) IntentForwarderActivity.this.getSystemService(UserManager.class);
        }

        @Override // com.android.internal.app.IntentForwarderActivity.Injector
        public PackageManager getPackageManager() {
            return IntentForwarderActivity.this.getPackageManager();
        }

        @Override // com.android.internal.app.IntentForwarderActivity.Injector
        public ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {
            return getPackageManager().resolveActivityAsUser(intent, flags, userId);
        }

        @Override // com.android.internal.app.IntentForwarderActivity.Injector
        public void showToast(int messageId, int duration) {
            IntentForwarderActivity intentForwarderActivity = IntentForwarderActivity.this;
            Toast.makeText(intentForwarderActivity, intentForwarderActivity.getString(messageId), duration).show();
        }
    }
}
