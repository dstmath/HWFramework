package com.android.server.wm;

import android.app.ActivityOptions;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.RemoteAnimationAdapter;
import com.android.server.wm.PendingRemoteAnimationRegistry;

/* access modifiers changed from: package-private */
public class PendingRemoteAnimationRegistry {
    private static final long TIMEOUT_MS = 3000;
    private final ArrayMap<String, Entry> mEntries = new ArrayMap<>();
    private final Handler mHandler;
    private final ActivityTaskManagerService mService;

    PendingRemoteAnimationRegistry(ActivityTaskManagerService service, Handler handler) {
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

    /* access modifiers changed from: private */
    public class Entry {
        final RemoteAnimationAdapter adapter;
        final String packageName;

        Entry(String packageName2, RemoteAnimationAdapter adapter2) {
            this.packageName = packageName2;
            this.adapter = adapter2;
            PendingRemoteAnimationRegistry.this.mHandler.postDelayed(new Runnable(packageName2) {
                /* class com.android.server.wm.$$Lambda$PendingRemoteAnimationRegistry$Entry$giivzkMgzIxukCXvO2EVzLb0oxo */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    PendingRemoteAnimationRegistry.Entry.this.lambda$new$0$PendingRemoteAnimationRegistry$Entry(this.f$1);
                }
            }, PendingRemoteAnimationRegistry.TIMEOUT_MS);
        }

        public /* synthetic */ void lambda$new$0$PendingRemoteAnimationRegistry$Entry(String packageName2) {
            synchronized (PendingRemoteAnimationRegistry.this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (((Entry) PendingRemoteAnimationRegistry.this.mEntries.get(packageName2)) == this) {
                        PendingRemoteAnimationRegistry.this.mEntries.remove(packageName2);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }
}
