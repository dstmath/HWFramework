package com.android.server.pm;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.IPinItemRequest.Stub;
import android.content.pm.LauncherApps.PinItemRequest;
import android.content.pm.ShortcutInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;

class ShortcutRequestPinProcessor {
    private static final boolean DEBUG = false;
    private static final String TAG = "ShortcutService";
    private final Object mLock;
    private final ShortcutService mService;

    private static abstract class PinItemRequestInner extends Stub {
        @GuardedBy("this")
        private boolean mAccepted;
        private final int mLauncherUid;
        protected final ShortcutRequestPinProcessor mProcessor;
        private final IntentSender mResultIntent;

        /* synthetic */ PinItemRequestInner(ShortcutRequestPinProcessor processor, IntentSender resultIntent, int launcherUid, PinItemRequestInner -this3) {
            this(processor, resultIntent, launcherUid);
        }

        private PinItemRequestInner(ShortcutRequestPinProcessor processor, IntentSender resultIntent, int launcherUid) {
            this.mProcessor = processor;
            this.mResultIntent = resultIntent;
            this.mLauncherUid = launcherUid;
        }

        public ShortcutInfo getShortcutInfo() {
            return null;
        }

        public AppWidgetProviderInfo getAppWidgetProviderInfo() {
            return null;
        }

        public Bundle getExtras() {
            return null;
        }

        private boolean isCallerValid() {
            return this.mProcessor.isCallerUid(this.mLauncherUid);
        }

        public boolean isValid() {
            if (!isCallerValid()) {
                return false;
            }
            boolean z;
            synchronized (this) {
                z = this.mAccepted ^ 1;
            }
            return z;
        }

        public boolean accept(Bundle options) {
            if (isCallerValid()) {
                Intent extras = null;
                if (options != null) {
                    try {
                        options.size();
                        extras = new Intent().putExtras(options);
                    } catch (RuntimeException e) {
                        throw new IllegalArgumentException("options cannot be unparceled", e);
                    }
                }
                synchronized (this) {
                    if (this.mAccepted) {
                        throw new IllegalStateException("accept() called already");
                    }
                    this.mAccepted = true;
                }
                if (!tryAccept()) {
                    return false;
                }
                this.mProcessor.sendResultIntent(this.mResultIntent, extras);
                return true;
            }
            throw new SecurityException("Calling uid mismatch");
        }

        protected boolean tryAccept() {
            return true;
        }
    }

    private static class PinAppWidgetRequestInner extends PinItemRequestInner {
        final AppWidgetProviderInfo mAppWidgetProviderInfo;
        final Bundle mExtras;

        /* synthetic */ PinAppWidgetRequestInner(ShortcutRequestPinProcessor processor, IntentSender resultIntent, int launcherUid, AppWidgetProviderInfo appWidgetProviderInfo, Bundle extras, PinAppWidgetRequestInner -this5) {
            this(processor, resultIntent, launcherUid, appWidgetProviderInfo, extras);
        }

        private PinAppWidgetRequestInner(ShortcutRequestPinProcessor processor, IntentSender resultIntent, int launcherUid, AppWidgetProviderInfo appWidgetProviderInfo, Bundle extras) {
            super(processor, resultIntent, launcherUid, null);
            this.mAppWidgetProviderInfo = appWidgetProviderInfo;
            this.mExtras = extras;
        }

        public AppWidgetProviderInfo getAppWidgetProviderInfo() {
            return this.mAppWidgetProviderInfo;
        }

        public Bundle getExtras() {
            return this.mExtras;
        }
    }

    private static class PinShortcutRequestInner extends PinItemRequestInner {
        public final String launcherPackage;
        public final int launcherUserId;
        public final boolean preExisting;
        public final ShortcutInfo shortcutForLauncher;
        public final ShortcutInfo shortcutOriginal;

        /* synthetic */ PinShortcutRequestInner(ShortcutRequestPinProcessor processor, ShortcutInfo shortcutOriginal, ShortcutInfo shortcutForLauncher, IntentSender resultIntent, String launcherPackage, int launcherUserId, int launcherUid, boolean preExisting, PinShortcutRequestInner -this8) {
            this(processor, shortcutOriginal, shortcutForLauncher, resultIntent, launcherPackage, launcherUserId, launcherUid, preExisting);
        }

        private PinShortcutRequestInner(ShortcutRequestPinProcessor processor, ShortcutInfo shortcutOriginal, ShortcutInfo shortcutForLauncher, IntentSender resultIntent, String launcherPackage, int launcherUserId, int launcherUid, boolean preExisting) {
            super(processor, resultIntent, launcherUid, null);
            this.shortcutOriginal = shortcutOriginal;
            this.shortcutForLauncher = shortcutForLauncher;
            this.launcherPackage = launcherPackage;
            this.launcherUserId = launcherUserId;
            this.preExisting = preExisting;
        }

        public ShortcutInfo getShortcutInfo() {
            return this.shortcutForLauncher;
        }

        protected boolean tryAccept() {
            return this.mProcessor.directPinShortcut(this);
        }
    }

    public ShortcutRequestPinProcessor(ShortcutService service, Object lock) {
        this.mService = service;
        this.mLock = lock;
    }

    public boolean isRequestPinItemSupported(int callingUserId, int requestType) {
        return getRequestPinConfirmationActivity(callingUserId, requestType) != null;
    }

    public boolean requestPinItemLocked(ShortcutInfo inShortcut, AppWidgetProviderInfo inAppWidget, Bundle extras, int userId, IntentSender resultIntent) {
        int requestType = inShortcut != null ? 1 : 2;
        Pair<ComponentName, Integer> confirmActivity = getRequestPinConfirmationActivity(userId, requestType);
        if (confirmActivity == null) {
            Log.w(TAG, "Launcher doesn't support requestPinnedShortcut(). Shortcut not created.");
            return false;
        }
        PinItemRequest request;
        int launcherUserId = ((Integer) confirmActivity.second).intValue();
        this.mService.throwIfUserLockedL(launcherUserId);
        if (inShortcut != null) {
            request = requestPinShortcutLocked(inShortcut, resultIntent, confirmActivity);
        } else {
            request = new PinItemRequest(new PinAppWidgetRequestInner(this, resultIntent, this.mService.injectGetPackageUid(((ComponentName) confirmActivity.first).getPackageName(), launcherUserId), inAppWidget, extras, null), 2);
        }
        return startRequestConfirmActivity((ComponentName) confirmActivity.first, launcherUserId, request, requestType);
    }

    public Intent createShortcutResultIntent(ShortcutInfo inShortcut, int userId) {
        int launcherUserId = this.mService.getParentOrSelfUserId(userId);
        ComponentName defaultLauncher = this.mService.getDefaultLauncher(launcherUserId);
        if (defaultLauncher == null) {
            Log.e(TAG, "Default launcher not found.");
            return null;
        }
        this.mService.throwIfUserLockedL(launcherUserId);
        return new Intent().putExtra("android.content.pm.extra.PIN_ITEM_REQUEST", requestPinShortcutLocked(inShortcut, null, Pair.create(defaultLauncher, Integer.valueOf(launcherUserId))));
    }

    private PinItemRequest requestPinShortcutLocked(ShortcutInfo inShortcut, IntentSender resultIntentOriginal, Pair<ComponentName, Integer> confirmActivity) {
        ShortcutInfo shortcutForLauncher;
        ShortcutInfo existing = this.mService.getPackageShortcutsForPublisherLocked(inShortcut.getPackage(), inShortcut.getUserId()).findShortcutById(inShortcut.getId());
        boolean existsAlready = existing != null;
        String launcherPackage = ((ComponentName) confirmActivity.first).getPackageName();
        int launcherUserId = ((Integer) confirmActivity.second).intValue();
        IntentSender resultIntentToSend = resultIntentOriginal;
        if (existsAlready) {
            validateExistingShortcut(existing);
            boolean isAlreadyPinned = this.mService.getLauncherShortcutsLocked(launcherPackage, existing.getUserId(), launcherUserId).hasPinned(existing);
            if (isAlreadyPinned) {
                sendResultIntent(resultIntentOriginal, null);
                resultIntentToSend = null;
            }
            shortcutForLauncher = existing.clone(11);
            if (!isAlreadyPinned) {
                shortcutForLauncher.clearFlags(2);
            }
        } else {
            if (inShortcut.getActivity() == null) {
                inShortcut.setActivity(this.mService.injectGetDefaultMainActivity(inShortcut.getPackage(), inShortcut.getUserId()));
            }
            this.mService.validateShortcutForPinRequest(inShortcut);
            inShortcut.resolveResourceStrings(this.mService.injectGetResourcesForApplicationAsUser(inShortcut.getPackage(), inShortcut.getUserId()));
            shortcutForLauncher = inShortcut.clone(10);
        }
        return new PinItemRequest(new PinShortcutRequestInner(this, inShortcut, shortcutForLauncher, resultIntentToSend, launcherPackage, launcherUserId, this.mService.injectGetPackageUid(launcherPackage, launcherUserId), existsAlready, null), 1);
    }

    private void validateExistingShortcut(ShortcutInfo shortcutInfo) {
        Preconditions.checkArgument(shortcutInfo.isEnabled(), "Shortcut ID=" + shortcutInfo + " already exists but disabled.");
    }

    private boolean startRequestConfirmActivity(ComponentName activity, int launcherUserId, PinItemRequest request, int requestType) {
        String action;
        if (requestType == 1) {
            action = "android.content.pm.action.CONFIRM_PIN_SHORTCUT";
        } else {
            action = "android.content.pm.action.CONFIRM_PIN_APPWIDGET";
        }
        Intent confirmIntent = new Intent(action);
        confirmIntent.setComponent(activity);
        confirmIntent.putExtra("android.content.pm.extra.PIN_ITEM_REQUEST", request);
        confirmIntent.addFlags(268468224);
        long token = this.mService.injectClearCallingIdentity();
        try {
            this.mService.mContext.startActivityAsUser(confirmIntent, UserHandle.of(launcherUserId));
            this.mService.injectRestoreCallingIdentity(token);
            return true;
        } catch (RuntimeException e) {
            Log.e(TAG, "Unable to start activity " + activity, e);
            this.mService.injectRestoreCallingIdentity(token);
            return false;
        } catch (Throwable th) {
            this.mService.injectRestoreCallingIdentity(token);
            throw th;
        }
    }

    Pair<ComponentName, Integer> getRequestPinConfirmationActivity(int callingUserId, int requestType) {
        Pair<ComponentName, Integer> pair = null;
        int launcherUserId = this.mService.getParentOrSelfUserId(callingUserId);
        ComponentName defaultLauncher = this.mService.getDefaultLauncher(launcherUserId);
        if (defaultLauncher == null) {
            Log.e(TAG, "Default launcher not found.");
            return null;
        }
        ComponentName activity = this.mService.injectGetPinConfirmationActivity(defaultLauncher.getPackageName(), launcherUserId, requestType);
        if (activity != null) {
            pair = Pair.create(activity, Integer.valueOf(launcherUserId));
        }
        return pair;
    }

    public void sendResultIntent(IntentSender intent, Intent extras) {
        this.mService.injectSendIntentSender(intent, extras);
    }

    public boolean isCallerUid(int uid) {
        return uid == this.mService.injectBinderCallingUid();
    }

    public boolean directPinShortcut(PinShortcutRequestInner request) {
        ShortcutInfo original = request.shortcutOriginal;
        int appUserId = original.getUserId();
        String appPackageName = original.getPackage();
        int launcherUserId = request.launcherUserId;
        String launcherPackage = request.launcherPackage;
        String shortcutId = original.getId();
        synchronized (this.mLock) {
            boolean isUserUnlockedL;
            if (this.mService.isUserUnlockedL(appUserId)) {
                isUserUnlockedL = this.mService.isUserUnlockedL(request.launcherUserId);
            } else {
                isUserUnlockedL = false;
            }
            if (isUserUnlockedL) {
                ShortcutLauncher launcher = this.mService.getLauncherShortcutsLocked(launcherPackage, appUserId, launcherUserId);
                launcher.lambda$-com_android_server_pm_ShortcutUser_11189();
                if (launcher.hasPinned(original)) {
                    return true;
                }
                ShortcutPackage ps = this.mService.getPackageShortcutsForPublisherLocked(appPackageName, appUserId);
                ShortcutInfo current = ps.findShortcutById(shortcutId);
                if (current == null) {
                    try {
                        this.mService.validateShortcutForPinRequest(original);
                    } catch (RuntimeException e) {
                        Log.w(TAG, "Unable to pin shortcut: " + e.getMessage());
                        return false;
                    }
                }
                validateExistingShortcut(current);
                if (current == null) {
                    if (original.getActivity() == null) {
                        original.setActivity(this.mService.getDummyMainActivity(appPackageName));
                    }
                    ps.addOrUpdateDynamicShortcut(original);
                }
                launcher.addPinnedShortcut(appPackageName, appUserId, shortcutId);
                if (current == null) {
                    ps.deleteDynamicWithId(shortcutId);
                }
                ps.adjustRanks();
                this.mService.verifyStates();
                this.mService.packageShortcutsChanged(appPackageName, appUserId);
                return true;
            }
            Log.w(TAG, "User is locked now.");
            return false;
        }
    }
}
