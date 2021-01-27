package com.android.server.am;

import android.os.Binder;
import android.os.SystemClock;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.app.procstats.AssociationState;
import com.android.internal.app.procstats.ProcessStats;
import com.android.server.slice.SliceClientPermissions;

public final class ContentProviderConnection extends Binder {
    public AssociationState.SourceState association;
    public final ProcessRecord client;
    public final String clientPackage;
    public final long createTime = SystemClock.elapsedRealtime();
    public boolean dead;
    public int numStableIncs;
    public int numUnstableIncs;
    public final ContentProviderRecord provider;
    public int stableCount;
    public int unstableCount;
    public boolean waiting;

    public ContentProviderConnection(ContentProviderRecord _provider, ProcessRecord _client, String _clientPackage) {
        this.provider = _provider;
        this.client = _client;
        this.clientPackage = _clientPackage;
    }

    public void startAssociationIfNeeded() {
        if (this.association == null && this.provider.proc != null) {
            if (this.provider.appInfo.uid != this.client.uid || !this.provider.info.processName.equals(this.client.processName)) {
                ProcessStats.ProcessStateHolder holder = this.provider.proc.pkgList.get(this.provider.name.getPackageName());
                if (holder == null) {
                    Slog.wtf(ActivityManagerService.TAG, "No package in referenced provider " + this.provider.name.toShortString() + ": proc=" + this.provider.proc);
                } else if (holder.pkg == null) {
                    Slog.wtf(ActivityManagerService.TAG, "Inactive holder in referenced provider " + this.provider.name.toShortString() + ": proc=" + this.provider.proc);
                } else {
                    this.association = holder.pkg.getAssociationStateLocked(holder.state, this.provider.name.getClassName()).startSource(this.client.uid, this.client.processName, this.clientPackage);
                }
            }
        }
    }

    public void trackProcState(int procState, int seq, long now) {
        AssociationState.SourceState sourceState = this.association;
        if (sourceState != null) {
            sourceState.trackProcState(procState, seq, now);
        }
    }

    public void stopAssociation() {
        AssociationState.SourceState sourceState = this.association;
        if (sourceState != null) {
            sourceState.stop();
            this.association = null;
        }
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("ContentProviderConnection{");
        toShortString(sb);
        sb.append('}');
        return sb.toString();
    }

    public String toShortString() {
        StringBuilder sb = new StringBuilder(128);
        toShortString(sb);
        return sb.toString();
    }

    public String toClientString() {
        StringBuilder sb = new StringBuilder(128);
        toClientString(sb);
        return sb.toString();
    }

    public void toShortString(StringBuilder sb) {
        sb.append(this.provider.toShortString());
        sb.append("->");
        toClientString(sb);
    }

    public void toClientString(StringBuilder sb) {
        sb.append(this.client.toShortString());
        sb.append(" s");
        sb.append(this.stableCount);
        sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
        sb.append(this.numStableIncs);
        sb.append(" u");
        sb.append(this.unstableCount);
        sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
        sb.append(this.numUnstableIncs);
        if (this.waiting) {
            sb.append(" WAITING");
        }
        if (this.dead) {
            sb.append(" DEAD");
        }
        long nowReal = SystemClock.elapsedRealtime();
        sb.append(" ");
        TimeUtils.formatDuration(nowReal - this.createTime, sb);
    }
}
