package ohos.bundlemgr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstallShellInfo {
    private String entryHap;
    private String[] featureHaps;
    private int installLocation = 1;
    private int installerUid = -1;
    private boolean isAppend;
    private String packageName;
    private String provisioningBundleName = "";
    private String[] restrictedPermissions = new String[0];
    private int userId;

    public InstallShellInfo(String str, String str2, String[] strArr, int i, boolean z) {
        this.packageName = str;
        this.entryHap = str2;
        if (strArr != null) {
            this.featureHaps = (String[]) strArr.clone();
        }
        this.userId = i;
        this.isAppend = z;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getEntryHap() {
        return this.entryHap;
    }

    public String[] getFeatureHaps() {
        String[] strArr = this.featureHaps;
        return strArr != null ? (String[]) strArr.clone() : new String[0];
    }

    public int getUserId() {
        return this.userId;
    }

    public boolean isAppend() {
        return this.isAppend;
    }

    public int getInstallLocation() {
        return this.installLocation;
    }

    public List<String> getRestrictedPermissions() {
        ArrayList arrayList = new ArrayList();
        String[] strArr = this.restrictedPermissions;
        return (strArr == null || strArr.length == 0) ? arrayList : Arrays.asList(strArr);
    }

    public String getProvisioningBundleName() {
        return this.provisioningBundleName;
    }

    public int getInstallerUid() {
        return this.installerUid;
    }
}
