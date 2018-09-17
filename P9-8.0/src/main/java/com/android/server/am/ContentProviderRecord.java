package com.android.server.am;

import android.app.ContentProviderHolder;
import android.content.ComponentName;
import android.content.IContentProvider;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Slog;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class ContentProviderRecord {
    private static final HashSet<String> NEED_RELEASE_PROVIDERS = new HashSet<String>() {
        {
            add("com.huawei.systemmanager");
            add("com.huawei.android.FMRadio");
        }
    };
    private static final String TAG = "ContentProviderRecord";
    final ApplicationInfo appInfo;
    final ArrayList<ContentProviderConnection> connections = new ArrayList();
    int externalProcessNoHandleCount;
    HashMap<IBinder, ExternalProcessHandle> externalProcessTokenToHandle;
    public final ProviderInfo info;
    ProcessRecord launchingApp;
    final ComponentName name;
    public boolean noReleaseNeeded;
    public ProcessRecord proc;
    public IContentProvider provider;
    final ActivityManagerService service;
    String shortStringName;
    final boolean singleton;
    String stringName;
    final int uid;

    private class ExternalProcessHandle implements DeathRecipient {
        private static final String LOG_TAG = "ExternalProcessHanldle";
        private int mAcquisitionCount;
        private final IBinder mToken;

        public ExternalProcessHandle(IBinder token) {
            this.mToken = token;
            try {
                token.linkToDeath(this, 0);
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Couldn't register for death for token: " + this.mToken, re);
            }
        }

        public void unlinkFromOwnDeathLocked() {
            this.mToken.unlinkToDeath(this, 0);
        }

        public void binderDied() {
            synchronized (ContentProviderRecord.this.service) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (ContentProviderRecord.this.hasExternalProcessHandles() && ContentProviderRecord.this.externalProcessTokenToHandle.get(this.mToken) != null) {
                        ContentProviderRecord.this.removeExternalProcessHandleInternalLocked(this.mToken);
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    public ContentProviderRecord(ActivityManagerService _service, ProviderInfo _info, ApplicationInfo ai, ComponentName _name, boolean _singleton) {
        boolean z = false;
        this.service = _service;
        this.info = _info;
        this.uid = ai.uid;
        this.appInfo = ai;
        this.name = _name;
        this.singleton = _singleton;
        boolean releaseProvider = false;
        if (_info.processName != null) {
            for (String appName : NEED_RELEASE_PROVIDERS) {
                if (_info.processName.contains(appName)) {
                    Slog.d(TAG, "Install provider in app ui process, name:" + this.name);
                    releaseProvider = true;
                    break;
                }
            }
        }
        if (this.uid == 0 || this.uid == 1000) {
            z = releaseProvider ^ 1;
        }
        this.noReleaseNeeded = z;
    }

    public ContentProviderRecord(ContentProviderRecord cpr) {
        this.service = cpr.service;
        this.info = cpr.info;
        this.uid = cpr.uid;
        this.appInfo = cpr.appInfo;
        this.name = cpr.name;
        this.singleton = cpr.singleton;
        this.noReleaseNeeded = cpr.noReleaseNeeded;
    }

    public ContentProviderHolder newHolder(ContentProviderConnection conn) {
        ContentProviderHolder holder = new ContentProviderHolder(this.info);
        holder.provider = this.provider;
        holder.noReleaseNeeded = this.noReleaseNeeded;
        holder.connection = conn;
        return holder;
    }

    public boolean canRunHere(ProcessRecord app) {
        if ((this.info.multiprocess || this.info.processName.equals(app.processName)) && this.uid == app.info.uid) {
            return true;
        }
        return false;
    }

    public void addExternalProcessHandleLocked(IBinder token) {
        if (token == null) {
            this.externalProcessNoHandleCount++;
            return;
        }
        if (this.externalProcessTokenToHandle == null) {
            this.externalProcessTokenToHandle = new HashMap();
        }
        ExternalProcessHandle handle = (ExternalProcessHandle) this.externalProcessTokenToHandle.get(token);
        if (handle == null) {
            handle = new ExternalProcessHandle(token);
            this.externalProcessTokenToHandle.put(token, handle);
        }
        handle.mAcquisitionCount = handle.mAcquisitionCount + 1;
    }

    public boolean removeExternalProcessHandleLocked(IBinder token) {
        if (hasExternalProcessHandles()) {
            boolean hasHandle = false;
            if (this.externalProcessTokenToHandle != null) {
                ExternalProcessHandle handle = (ExternalProcessHandle) this.externalProcessTokenToHandle.get(token);
                if (handle != null) {
                    hasHandle = true;
                    handle.mAcquisitionCount = handle.mAcquisitionCount - 1;
                    if (handle.mAcquisitionCount == 0) {
                        removeExternalProcessHandleInternalLocked(token);
                        return true;
                    }
                }
            }
            if (!hasHandle) {
                this.externalProcessNoHandleCount--;
                return true;
            }
        }
        return false;
    }

    private void removeExternalProcessHandleInternalLocked(IBinder token) {
        ((ExternalProcessHandle) this.externalProcessTokenToHandle.get(token)).unlinkFromOwnDeathLocked();
        this.externalProcessTokenToHandle.remove(token);
        if (this.externalProcessTokenToHandle.size() == 0) {
            this.externalProcessTokenToHandle = null;
        }
    }

    public boolean hasExternalProcessHandles() {
        return this.externalProcessTokenToHandle != null || this.externalProcessNoHandleCount > 0;
    }

    public boolean hasConnectionOrHandle() {
        return this.connections.isEmpty() ? hasExternalProcessHandles() : true;
    }

    void dump(PrintWriter pw, String prefix, boolean full) {
        if (full) {
            pw.print(prefix);
            pw.print("package=");
            pw.print(this.info.applicationInfo.packageName);
            pw.print(" process=");
            pw.println(this.info.processName);
        }
        pw.print(prefix);
        pw.print("proc=");
        pw.println(this.proc);
        if (this.launchingApp != null) {
            pw.print(prefix);
            pw.print("launchingApp=");
            pw.println(this.launchingApp);
        }
        if (full) {
            pw.print(prefix);
            pw.print("uid=");
            pw.print(this.uid);
            pw.print(" provider=");
            pw.println(this.provider);
        }
        if (this.singleton) {
            pw.print(prefix);
            pw.print("singleton=");
            pw.println(this.singleton);
        }
        pw.print(prefix);
        pw.print("authority=");
        pw.println(this.info.authority);
        if (full && (this.info.isSyncable || this.info.multiprocess || this.info.initOrder != 0)) {
            pw.print(prefix);
            pw.print("isSyncable=");
            pw.print(this.info.isSyncable);
            pw.print(" multiprocess=");
            pw.print(this.info.multiprocess);
            pw.print(" initOrder=");
            pw.println(this.info.initOrder);
        }
        if (full) {
            if (hasExternalProcessHandles()) {
                pw.print(prefix);
                pw.print("externals:");
                if (this.externalProcessTokenToHandle != null) {
                    pw.print(" w/token=");
                    pw.print(this.externalProcessTokenToHandle.size());
                }
                if (this.externalProcessNoHandleCount > 0) {
                    pw.print(" notoken=");
                    pw.print(this.externalProcessNoHandleCount);
                }
                pw.println();
            }
        } else if (this.connections.size() > 0 || this.externalProcessNoHandleCount > 0) {
            pw.print(prefix);
            pw.print(this.connections.size());
            pw.print(" connections, ");
            pw.print(this.externalProcessNoHandleCount);
            pw.println(" external handles");
        }
        if (this.connections.size() > 0) {
            if (full) {
                pw.print(prefix);
                pw.println("Connections:");
            }
            for (int i = 0; i < this.connections.size(); i++) {
                ContentProviderConnection conn = (ContentProviderConnection) this.connections.get(i);
                pw.print(prefix);
                pw.print("  -> ");
                pw.println(conn.toClientString());
                if (conn.provider != this) {
                    pw.print(prefix);
                    pw.print("    *** WRONG PROVIDER: ");
                    pw.println(conn.provider);
                }
            }
        }
    }

    public String toString() {
        if (this.stringName != null) {
            return this.stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ContentProviderRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" u");
        sb.append(UserHandle.getUserId(this.uid));
        sb.append(' ');
        sb.append(this.name.flattenToShortString());
        sb.append('}');
        String stringBuilder = sb.toString();
        this.stringName = stringBuilder;
        return stringBuilder;
    }

    public String toShortString() {
        if (this.shortStringName != null) {
            return this.shortStringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append('/');
        sb.append(this.name.flattenToShortString());
        String stringBuilder = sb.toString();
        this.shortStringName = stringBuilder;
        return stringBuilder;
    }
}
