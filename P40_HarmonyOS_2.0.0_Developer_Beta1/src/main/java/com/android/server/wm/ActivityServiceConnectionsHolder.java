package com.android.server.wm;

import com.android.server.wm.ActivityStack;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Consumer;

public class ActivityServiceConnectionsHolder<T> {
    private final ActivityRecord mActivity;
    private HashSet<T> mConnections;
    private final ActivityTaskManagerService mService;

    ActivityServiceConnectionsHolder(ActivityTaskManagerService service, ActivityRecord activity) {
        this.mService = service;
        this.mActivity = activity;
    }

    public void addConnection(T c) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mConnections == null) {
                    this.mConnections = new HashSet<>();
                }
                this.mConnections.add(c);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void removeConnection(T c) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mConnections != null) {
                    this.mConnections.remove(c);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean isActivityVisible() {
        boolean z;
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (!this.mActivity.visible) {
                    if (!this.mActivity.isState(ActivityStack.ActivityState.RESUMED, ActivityStack.ActivityState.PAUSING)) {
                        z = false;
                    }
                }
                z = true;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public int getActivityPid() {
        int pid;
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                pid = this.mActivity.hasProcess() ? this.mActivity.app.getPid() : -1;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return pid;
    }

    public void forEachConnection(Consumer<T> consumer) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mConnections != null) {
                    if (!this.mConnections.isEmpty()) {
                        Iterator<T> it = this.mConnections.iterator();
                        while (it.hasNext()) {
                            consumer.accept(it.next());
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void disconnectActivityFromServices() {
        HashSet<T> hashSet = this.mConnections;
        if (hashSet != null && !hashSet.isEmpty()) {
            Object disc = this.mConnections;
            this.mConnections = null;
            this.mService.mH.post(new Runnable(disc) {
                /* class com.android.server.wm.$$Lambda$ActivityServiceConnectionsHolder$3WnpJbbvyxcEr6D6eCp22ebnxPk */
                private final /* synthetic */ Object f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActivityServiceConnectionsHolder.this.lambda$disconnectActivityFromServices$0$ActivityServiceConnectionsHolder(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$disconnectActivityFromServices$0$ActivityServiceConnectionsHolder(Object disc) {
        this.mService.mAmInternal.disconnectActivityFromServices(this, disc);
    }

    public void dump(PrintWriter pw, String prefix) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                pw.println(prefix + "activity=" + this.mActivity);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }
}
