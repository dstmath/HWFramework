package huawei.android.security;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.hsm.HwSystemManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import java.io.IOException;
import java.util.List;

public class HsmDefaultImpl implements HwSystemManager.HsmInterface {
    @Override // android.hsm.HwSystemManager.HsmInterface
    public boolean canStartActivity(Context context, Intent intent) {
        return true;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public boolean canSendBroadcast(Context context, Intent intent) {
        return true;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public boolean allowOp(Uri uri, int action) {
        return true;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public boolean allowOp(String destAddr, String smsBody, PendingIntent sentIntent) {
        return true;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public boolean allowOp(String destAddr, String smsBody, List<PendingIntent> list) {
        return true;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public boolean allowOp(int type) {
        return true;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public boolean allowOp(Context cxt, int type) {
        return true;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public boolean allowOp(Context cxt, int type, boolean enable) {
        return true;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public Cursor getDummyCursor(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public Location getFakeLocation(String name) {
        return null;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public void setOutputFile(MediaRecorder recorder) throws IllegalStateException, IOException {
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public boolean shouldInterceptAudience(String[] people, String pkgName) {
        return false;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public List<PackageInfo> getFakePackages(List<PackageInfo> installedList) {
        return installedList;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public List<ApplicationInfo> getFakeApplications(List<ApplicationInfo> installedList) {
        return installedList;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public List<ResolveInfo> getFakeResolveInfoList(List<ResolveInfo> originalList) {
        return originalList;
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public void insertSendBroadcastRecord(String pkgName, String action, int uid) {
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public void authenticateSmsSend(HwSystemManager.Notifier callback, int callingUid, int smsId, String smsBody, String smsAddress) {
    }

    @Override // android.hsm.HwSystemManager.HsmInterface
    public void notifyBackgroundMgr(String pkgName, int pid, int uidOf3RdApk, int permType, int permCfg) {
    }
}
