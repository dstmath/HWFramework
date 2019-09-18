package com.huawei.android.feature.install;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public class InstallSessionState {
    public long mBytesDownloaded;
    public int mErrorCode;
    public List<String> mModuleNames;
    public long[] mModuleVersions;
    public PendingIntent mPendingIntent;
    public int mSessionId;
    public int mStatus;
    public long mTotalDownloaded;
    public List<Intent> mUriIntents;

    private InstallSessionState(int i, int i2, int i3, long j, long j2, List<String> list, PendingIntent pendingIntent, long[] jArr, List<Intent> list2) {
        this.mSessionId = i;
        this.mStatus = i2;
        this.mErrorCode = i3;
        this.mBytesDownloaded = j;
        this.mTotalDownloaded = j2;
        this.mModuleNames = list;
        this.mPendingIntent = pendingIntent;
        this.mModuleVersions = jArr;
        this.mUriIntents = list2;
    }

    public static Bundle buildInstalledBundle(List<String> list) {
        Bundle bundle = new Bundle();
        bundle.putInt("session_id", 0);
        bundle.putInt("status", 5);
        bundle.putInt("error_code", 0);
        bundle.putStringArrayList("module_names", new ArrayList(list));
        bundle.putLong("total_bytes_to_download", 0);
        bundle.putLong("bytes_downloaded", 0);
        return bundle;
    }

    public static InstallSessionState buildWithBundle(Bundle bundle) {
        return new InstallSessionState(bundle.getInt("session_id"), bundle.getInt("status"), bundle.getInt("error_code"), bundle.getLong("bytes_downloaded"), bundle.getLong("total_bytes_to_download"), bundle.getStringArrayList("module_names"), (PendingIntent) bundle.getParcelable("user_confirmation_intent"), bundle.getLongArray("module_versions"), bundle.getParcelableArrayList("module_file_intents"));
    }

    /* access modifiers changed from: package-private */
    public final InstallSessionState buildWithStatus(int i) {
        return new InstallSessionState(this.mSessionId, i, this.mErrorCode, this.mBytesDownloaded, this.mTotalDownloaded, this.mModuleNames, this.mPendingIntent, this.mModuleVersions, this.mUriIntents);
    }

    public final List<String> moduleNames() {
        return this.mModuleNames != null ? new ArrayList(this.mModuleNames) : new ArrayList();
    }

    public final String toString() {
        return "SplitInstallSessionState{sessionId=" + this.mSessionId + ", status=" + this.mStatus + ", errorCode=" + this.mErrorCode + ", bytesDownloaded=" + this.mBytesDownloaded + ",totalBytesToDownload=" + this.mTotalDownloaded + ",moduleNames=" + moduleNames() + "}";
    }
}
