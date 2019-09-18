package com.android.server.am;

import android.app.ActivityOptions;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.RemoteAnimationAdapter;
import com.android.server.am.PendingRemoteAnimationRegistry;

class PendingRemoteAnimationRegistry {
    private static final long TIMEOUT_MS = 3000;
    /* access modifiers changed from: private */
    public final ArrayMap<String, Entry> mEntries = new ArrayMap<>();
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public final ActivityManagerService mService;

    private class Entry {
        final RemoteAnimationAdapter adapter;
        final String packageName;

        Entry(String packageName2, RemoteAnimationAdapter adapter2) {
            this.packageName = packageName2;
            this.adapter = adapter2;
            PendingRemoteAnimationRegistry.this.mHandler.postDelayed(new Runnable(packageName2) {
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PendingRemoteAnimationRegistry.Entry.lambda$new$0(PendingRemoteAnimationRegistry.Entry.this, this.f$1);
                }
            }, PendingRemoteAnimationRegistry.TIMEOUT_MS);
        }

        public static /* synthetic */ void lambda$new$0(Entry entry, String packageName2) {
            synchronized (PendingRemoteAnimationRegistry.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (((Entry) PendingRemoteAnimationRegistry.this.mEntries.get(packageName2)) == entry) {
                        PendingRemoteAnimationRegistry.this.mEntries.remove(packageName2);
                    }
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }
    }

    PendingRemoteAnimationRegistry(ActivityManagerService service, Handler handler) {
        this.mService = service;
        this.mHandler = handler;
    }

    /* access modifiers changed from: package-private */
    public void addPendingAnimation(String packageName, RemoteAnimationAdapter adapter) {
        this.mEntries.put(packageName, new Entry(packageName, adapter));
    }

    /* access modifiers changed from: package-private */
    public ActivityOptions overrideOptionsIfNeeded(String callingPackage, ActivityOptions options) {
        Entry entry = this.mEntries.get(callingPackage);
        if (entry == null) {
            return options;
        }
        if (options == null) {
            options = ActivityOptions.makeRemoteAnimation(entry.adapter);
        } else {
            options.setRemoteAnimationAdapter(entry.adapter);
        }
        this.mEntries.remove(callingPackage);
        return options;
    }
}
