package com.android.internal.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Slog;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Protocol;

public class IntentForwarderActivity extends Activity {
    public static String FORWARD_INTENT_TO_MANAGED_PROFILE = "com.android.internal.app.ForwardIntentToManagedProfile";
    public static String FORWARD_INTENT_TO_PARENT = "com.android.internal.app.ForwardIntentToParent";
    public static String TAG = "IntentForwarderActivity";
    private Injector mInjector;

    public interface Injector {
        IPackageManager getIPackageManager();

        PackageManager getPackageManager();

        UserManager getUserManager();
    }

    private class InjectorImpl implements Injector {
        private InjectorImpl() {
        }

        public IPackageManager getIPackageManager() {
            return AppGlobals.getPackageManager();
        }

        public UserManager getUserManager() {
            return (UserManager) IntentForwarderActivity.this.getSystemService(UserManager.class);
        }

        public PackageManager getPackageManager() {
            return IntentForwarderActivity.this.getPackageManager();
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x005f  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x005b  */
    public void onCreate(Bundle savedInstanceState) {
        int userMessageId;
        int targetUserId;
        int userMessageId2;
        super.onCreate(savedInstanceState);
        this.mInjector = createInjector();
        Intent intentReceived = getIntent();
        String className = intentReceived.getComponent().getClassName();
        if (className.equals(FORWARD_INTENT_TO_PARENT)) {
            userMessageId2 = 17040105;
            targetUserId = getProfileParent();
        } else if (className.equals(FORWARD_INTENT_TO_MANAGED_PROFILE)) {
            userMessageId2 = 17040106;
            targetUserId = getManagedProfile();
        } else {
            String str = TAG;
            Slog.wtf(str, IntentForwarderActivity.class.getName() + " cannot be called directly");
            userMessageId = -1;
            targetUserId = -10000;
            if (targetUserId != -10000) {
                finish();
                return;
            }
            int callingUserId = getUserId();
            Intent newIntent = canForward(intentReceived, targetUserId);
            if (newIntent != null) {
                if ("android.intent.action.CHOOSER".equals(newIntent.getAction())) {
                    ((Intent) newIntent.getParcelableExtra("android.intent.extra.INTENT")).prepareToLeaveUser(callingUserId);
                } else {
                    newIntent.prepareToLeaveUser(callingUserId);
                }
                ResolveInfo ri = this.mInjector.getPackageManager().resolveActivityAsUser(newIntent, Protocol.BASE_SYSTEM_RESERVED, targetUserId);
                boolean shouldShowDisclosure = ri == null || ri.activityInfo == null || !"android".equals(ri.activityInfo.packageName) || (!ResolverActivity.class.getName().equals(ri.activityInfo.name) && !ChooserActivity.class.getName().equals(ri.activityInfo.name));
                try {
                    startActivityAsCaller(newIntent, null, false, targetUserId);
                } catch (RuntimeException e) {
                    RuntimeException e2 = e;
                    int launchedFromUid = -1;
                    String launchedFromPackage = "?";
                    try {
                        launchedFromUid = ActivityManager.getService().getLaunchedFromUid(getActivityToken());
                        launchedFromPackage = ActivityManager.getService().getLaunchedFromPackage(getActivityToken());
                    } catch (RemoteException e3) {
                    }
                    String str2 = TAG;
                    Slog.wtf(str2, "Unable to launch as UID " + launchedFromUid + " package " + launchedFromPackage + ", while running in " + ActivityThread.currentProcessName(), e2);
                }
                if (shouldShowDisclosure) {
                    Toast.makeText(this, getString(userMessageId), 1).show();
                }
            } else {
                String str3 = TAG;
                Slog.wtf(str3, "the intent: " + intentReceived + " cannot be forwarded from user " + callingUserId + " to user " + targetUserId);
            }
            finish();
            return;
        }
        userMessageId = userMessageId2;
        if (targetUserId != -10000) {
        }
    }

    /* JADX WARNING: type inference failed for: r3v10, types: [android.os.Parcelable] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    public Intent canForward(Intent incomingIntent, int targetUserId) {
        if (incomingIntent.getAction() == null) {
            Slog.wtf(TAG, "The action of fowarded intent is null");
            return null;
        }
        Intent forwardIntent = new Intent(incomingIntent);
        forwardIntent.addFlags(50331648);
        sanitizeIntent(forwardIntent);
        Intent intentToCheck = forwardIntent;
        if ("android.intent.action.CHOOSER".equals(forwardIntent.getAction())) {
            if (forwardIntent.hasExtra("android.intent.extra.INITIAL_INTENTS")) {
                Slog.wtf(TAG, "An chooser intent with extra initial intents cannot be forwarded to a different user");
                return null;
            } else if (forwardIntent.hasExtra("android.intent.extra.REPLACEMENT_EXTRAS")) {
                Slog.wtf(TAG, "A chooser intent with replacement extras cannot be forwarded to a different user");
                return null;
            } else {
                intentToCheck = forwardIntent.getParcelableExtra("android.intent.extra.INTENT");
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
    @VisibleForTesting
    public Injector createInjector() {
        return new InjectorImpl();
    }
}
