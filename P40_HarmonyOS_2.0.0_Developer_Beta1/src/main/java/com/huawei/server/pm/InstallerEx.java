package com.huawei.server.pm;

import android.content.Context;
import android.rms.iaware.AwareLog;
import com.android.server.pm.Installer;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class InstallerEx {
    private Installer mInstaller;

    public InstallerEx(Context context) {
        this.mInstaller = new Installer(context);
    }

    public void onStart() {
        this.mInstaller.onStart();
    }

    public boolean isInstallerNull() {
        return this.mInstaller == null;
    }

    public String[] getDexFileStatus(String[] fileNames, String[] instructionSets, int[] uids, String tag) {
        try {
            return this.mInstaller.getDexFileStatus(fileNames, instructionSets, uids);
        } catch (Installer.InstallerException e) {
            AwareLog.w(tag, "preread getDexFileStatus Installer.InstallerException!");
            return new String[0];
        }
    }
}
