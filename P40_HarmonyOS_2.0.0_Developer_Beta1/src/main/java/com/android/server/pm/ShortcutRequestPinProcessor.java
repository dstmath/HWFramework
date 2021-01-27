package com.android.server.pm;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.IPinItemRequest;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;

/* access modifiers changed from: package-private */
public class ShortcutRequestPinProcessor {
    private static final boolean DEBUG = false;
    private static final String TAG = "ShortcutService";
    private final Object mLock;
    private final ShortcutService mService;

    private static abstract class PinItemRequestInner extends IPinItemRequest.Stub {
        @GuardedBy({"this"})
        private boolean mAccepted;
        private final int mLauncherUid;
        protected final ShortcutRequestPinProcessor mProcessor;
        private final IntentSender mResultIntent;

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
            boolean z = false;
            if (!isCallerValid()) {
                return false;
            }
            synchronized (this) {
                if (!this.mAccepted) {
                    z = true;
                }
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
                    if (!this.mAccepted) {
                        this.mAccepted = true;
                    } else {
                        throw new IllegalStateException("accept() called already");
                    }
                }
                if (!tryAccept()) {
                    return false;
                }
                this.mProcessor.sendResultIntent(this.mResultIntent, extras);
                return true;
            }
            throw new SecurityException("Calling uid mismatch");
        }

        /* access modifiers changed from: protected */
        public boolean tryAccept() {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class PinAppWidgetRequestInner extends PinItemRequestInner {
        final AppWidgetProviderInfo mAppWidgetProviderInfo;
        final Bundle mExtras;

        private PinAppWidgetRequestInner(ShortcutRequestPinProcessor processor, IntentSender resultIntent, int launcherUid, AppWidgetProviderInfo appWidgetProviderInfo, Bundle extras) {
            super(resultIntent, launcherUid);
            this.mAppWidgetProviderInfo = appWidgetProviderInfo;
            this.mExtras = extras;
        }

        @Override // com.android.server.pm.ShortcutRequestPinProcessor.PinItemRequestInner
        public AppWidgetProviderInfo getAppWidgetProviderInfo() {
            return this.mAppWidgetProviderInfo;
        }

        @Override // com.android.server.pm.ShortcutRequestPinProcessor.PinItemRequestInner
        public Bundle getExtras() {
            return this.mExtras;
        }
    }

    /* access modifiers changed from: private */
    public static class PinShortcutRequestInner extends PinItemRequestInner {
        public final String launcherPackage;
        public final int launcherUserId;
        public final boolean preExisting;
        public final ShortcutInfo shortcutForLauncher;
        public final ShortcutInfo shortcutOriginal;

        private PinShortcutRequestInner(ShortcutRequestPinProcessor processor, ShortcutInfo shortcutOriginal2, ShortcutInfo shortcutForLauncher2, IntentSender resultIntent, String launcherPackage2, int launcherUserId2, int launcherUid, boolean preExisting2) {
            super(resultIntent, launcherUid);
            this.shortcutOriginal = shortcutOriginal2;
            this.shortcutForLauncher = shortcutForLauncher2;
            this.launcherPackage = launcherPackage2;
            this.launcherUserId = launcherUserId2;
            this.preExisting = preExisting2;
        }

        @Override // com.android.server.pm.ShortcutRequestPinProcessor.PinItemRequestInner
        public ShortcutInfo getShortcutInfo() {
            return this.shortcutForLauncher;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.pm.ShortcutRequestPinProcessor.PinItemRequestInner
        public boolean tryAccept() {
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
        LauncherApps.PinItemRequest request;
        int requestType = inShortcut != null ? 1 : 2;
        Pair<ComponentName, Integer> confirmActivity = getRequestPinConfirmationActivity(userId, requestType);
        if (confirmActivity == null) {
            Log.w(TAG, "Launcher doesn't support requestPinnedShortcut(). Shortcut not created.");
            return false;
        }
        int launcherUserId = ((Integer) confirmActivity.second).intValue();
        this.mService.throwIfUserLockedL(launcherUserId);
        if (inShortcut != null) {
            request = requestPinShortcutLocked(inShortcut, resultIntent, confirmActivity);
        } else {
            request = new LauncherApps.PinItemRequest(new PinAppWidgetRequestInner(resultIntent, this.mService.injectGetPackageUid(((ComponentName) confirmActivity.first).getPackageName(), launcherUserId), inAppWidget, extras), 2);
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

    private LauncherApps.PinItemRequest requestPinShortcutLocked(ShortcutInfo inShortcut, IntentSender resultIntentOriginal, Pair<ComponentName, Integer> confirmActivity) {
        IntentSender resultIntentToSend;
        ShortcutInfo shortcutForLauncher;
        ShortcutInfo existing = this.mService.getPackageShortcutsForPublisherLocked(inShortcut.getPackage(), inShortcut.getUserId()).findShortcutById(inShortcut.getId());
        boolean z = false;
        boolean existsAlready = existing != null;
        if (existsAlready && existing.isVisibleToPublisher()) {
            z = true;
        }
        String launcherPackage = ((ComponentName) confirmActivity.first).getPackageName();
        int launcherUserId = ((Integer) confirmActivity.second).intValue();
        IntentSender resultIntentToSend2 = resultIntentOriginal;
        if (existsAlready) {
            validateExistingShortcut(existing);
            boolean isAlreadyPinned = this.mService.getLauncherShortcutsLocked(launcherPackage, existing.getUserId(), launcherUserId).hasPinned(existing);
            if (isAlreadyPinned) {
                sendResultIntent(resultIntentOriginal, null);
                resultIntentToSend2 = null;
            }
            ShortcutInfo shortcutForLauncher2 = existing.clone(27);
            if (!isAlreadyPinned) {
                shortcutForLauncher2.clearFlags(2);
            }
            resultIntentToSend = resultIntentToSend2;
            shortcutForLauncher = shortcutForLauncher2;
        } else {
            if (inShortcut.getActivity() == null) {
                inShortcut.setActivity(this.mService.injectGetDefaultMainActivity(inShortcut.getPackage(), inShortcut.getUserId()));
            }
            this.mService.validateShortcutForPinRequest(inShortcut);
            inShortcut.resolveResourceStrings(this.mService.injectGetResourcesForApplicationAsUser(inShortcut.getPackage(), inShortcut.getUserId()));
            resultIntentToSend = resultIntentToSend2;
            shortcutForLauncher = inShortcut.clone(26);
        }
        return new LauncherApps.PinItemRequest(new PinShortcutRequestInner(inShortcut, shortcutForLauncher, resultIntentToSend, launcherPackage, launcherUserId, this.mService.injectGetPackageUid(launcherPackage, launcherUserId), existsAlready), 1);
    }

    private void validateExistingShortcut(ShortcutInfo shortcutInfo) {
        boolean isEnabled = shortcutInfo.isEnabled();
        Preconditions.checkArgument(isEnabled, "Shortcut ID=" + shortcutInfo + " already exists but disabled.");
    }

    private boolean startRequestConfirmActivity(ComponentName activity, int launcherUserId, LauncherApps.PinItemRequest request, int requestType) {
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
            return true;
        } catch (RuntimeException e) {
            Log.e(TAG, "Unable to start activity " + activity, e);
            return false;
        } finally {
            this.mService.injectRestoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Pair<ComponentName, Integer> getRequestPinConfirmationActivity(int callingUserId, int requestType) {
        int launcherUserId = this.mService.getParentOrSelfUserId(callingUserId);
        ComponentName defaultLauncher = this.mService.getDefaultLauncher(launcherUserId);
        if (defaultLauncher == null) {
            Log.e(TAG, "Default launcher not found.");
            return null;
        }
        ComponentName activity = this.mService.injectGetPinConfirmationActivity(defaultLauncher.getPackageName(), launcherUserId, requestType);
        if (activity == null) {
            return null;
        }
        return Pair.create(activity, Integer.valueOf(launcherUserId));
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
            if (this.mService.isUserUnlockedL(appUserId)) {
                if (this.mService.isUserUnlockedL(request.launcherUserId)) {
                    ShortcutLauncher launcher = this.mService.getLauncherShortcutsLocked(launcherPackage, appUserId, launcherUserId);
                    launcher.attemptToRestoreIfNeededAndSave();
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
                    } else {
                        validateExistingShortcut(current);
                    }
                    if (current == null) {
                        if (original.getActivity() == null) {
                            original.setActivity(this.mService.getDummyMainActivity(appPackageName));
                        }
                        ps.addOrReplaceDynamicShortcut(original);
                    }
                    launcher.addPinnedShortcut(appPackageName, appUserId, shortcutId, true);
                    if (current == null) {
                        ps.deleteDynamicWithId(shortcutId, false);
                    }
                    ps.adjustRanks();
                    this.mService.verifyStates();
                    this.mService.packageShortcutsChanged(appPackageName, appUserId);
                    return true;
                }
            }
            Log.w(TAG, "User is locked now.");
            return false;
        }
    }
}
