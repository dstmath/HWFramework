package com.huawei.android.feature.install;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public class InstallSessionState {
    public long bytesDownloaded;
    public int errorCode;
    public List<String> moduleNames;
    public long[] moduleVersions;
    public PendingIntent pendingIntent;
    public int sessionId;
    public int status;
    public long totalDownloaded;
    public List<Intent> uriIntents;

    private InstallSessionState(int i, int i2, int i3, long j, long j2, List<String> list, PendingIntent pendingIntent2, long[] jArr, List<Intent> list2) {
        this.sessionId = i;
        this.status = i2;
        this.errorCode = i3;
        this.bytesDownloaded = j;
        this.totalDownloaded = j2;
        this.moduleNames = list;
        this.pendingIntent = pendingIntent2;
        this.moduleVersions = jArr;
        this.uriIntents = list2;
    }

    public static Bundle buildInstalledBundle(List<String> list) {
        Bundle bundle = new Bundle();
        bundle.putInt("session_id", 0);
        bundle.putInt("status", 5);
        bundle.putInt("error_code", 0);
        bundle.putStringArrayList("module_names", new ArrayList<>(list));
        bundle.putLong("total_bytes_to_download", 0);
        bundle.putLong("bytes_downloaded", 0);
        return bundle;
    }

    public static InstallSessionState buildWithBundle(Bundle bundle) {
        return new InstallSessionState(bundle.getInt("session_id"), bundle.getInt("status"), bundle.getInt("error_code"), bundle.getLong("bytes_downloaded"), bundle.getLong("total_bytes_to_download"), bundle.getStringArrayList("module_names"), (PendingIntent) bundle.getParcelable("user_confirmation_intent"), bundle.getLongArray("module_versions"), bundle.getParcelableArrayList("module_file_intents"));
    }

    /* access modifiers changed from: package-private */
    public final InstallSessionState buildWithStatus(int i) {
        return new InstallSessionState(this.sessionId, i, this.errorCode, this.bytesDownloaded, this.totalDownloaded, this.moduleNames, this.pendingIntent, this.moduleVersions, this.uriIntents);
    }

    public final List<String> moduleNames() {
        return this.moduleNames != null ? new ArrayList(this.moduleNames) : new ArrayList();
    }

    public final String toString() {
        return "SplitInstallSessionState{status=" + this.status + ", errorCode=" + this.errorCode + ", bytesDownloaded=" + this.bytesDownloaded + ",totalBytesToDownload=" + this.totalDownloaded + ",moduleNames=" + moduleNames() + "}";
    }
}
