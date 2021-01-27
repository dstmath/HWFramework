package com.msic.qarth;

import android.content.pm.PackageInfo;
import com.huawei.android.os.storage.StorageManagerExt;
import com.msic.qarth.PatchFile;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatchDisFileFilter implements FilenameFilter {
    private static final String SYSTEM_DISABLE_PATCH_OVERLAY_ROOT = "/data/hotpatch/fwkpatchdir/system/";
    private static final String TAG = "PatchDisFileFilter";
    private List<PatchFile> mDisPatchFiles = new ArrayList();
    private final String mDisableAllMatchRegex = "(.*)_all_{0,1}(.*)\\.qarth\\.disable$";
    private final String mDisableIgnoreVCodeMatchRegex = ("(.*)_(" + this.mVersionName + ")_{0,1}(.*)\\.qarth\\.disable$");
    private final String mDisableIgnoreVNameMatchRegex = ("(.*)_(" + this.mVersionCode + ")_{0,1}(.*)\\.qarth\\.disable$");
    private final String mDisableMathRegex = ("(.*)_(" + this.mVersionName + ")_(" + this.mVersionCode + ")_{0,1}(.*)\\.qarth\\.disable$");
    private String mPackageName = null;
    private String[] mRegulars = {"(.*)_all_{0,1}(.*)\\.qarth\\.disable$", this.mDisableIgnoreVCodeMatchRegex, this.mDisableIgnoreVNameMatchRegex, this.mDisableMathRegex};
    private int mVersionCode = 0;
    private String mVersionName = null;

    public PatchDisFileFilter(String packageName) {
        this.mPackageName = packageName;
    }

    public PatchDisFileFilter(PackageInfo pkgInfo, String packageName) {
        this.mVersionCode = pkgInfo.versionCode;
        this.mVersionName = pkgInfo.versionName;
        this.mPackageName = packageName;
    }

    @Override // java.io.FilenameFilter
    public boolean accept(File dir, String filename) {
        return matchPattern(dir, filename);
    }

    private boolean matchPattern(File dir, String fileName) {
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "matchPattern");
        }
        PatchFile.Builder builder = new PatchFile.Builder();
        String aPath = Constants.FWK_HOT_PATCH_PATH + this.mPackageName;
        if (Constants.COMMON_PATCH_PKG_NAME.equals(this.mPackageName)) {
            aPath = Constants.FWK_HOT_PATCH_PATH + "all";
        }
        builder.setPath(aPath + File.separator + fileName.replace(".disable", StorageManagerExt.INVALID_KEY_DESC));
        int i = 0;
        while (true) {
            String[] strArr = this.mRegulars;
            if (i >= strArr.length) {
                return false;
            }
            Matcher matcher = Pattern.compile(strArr[i]).matcher(fileName);
            if (matcher.find()) {
                if (i == 0) {
                    this.mDisPatchFiles.add(builder.setFileName(matcher.group(1)).build());
                } else if (i == 1) {
                    this.mDisPatchFiles.add(builder.setFileName(matcher.group(1)).setVersionCode(matcher.group(2)).build());
                } else if (i == 2) {
                    this.mDisPatchFiles.add(builder.setFileName(matcher.group(1)).setVersionName(matcher.group(2)).build());
                } else if (i == 3) {
                    this.mDisPatchFiles.add(builder.setFileName(matcher.group(1)).setVersionName(matcher.group(2)).setVersionCode(matcher.group(3)).build());
                }
                return matcher.matches();
            }
            i++;
        }
    }

    public List<PatchFile> getDisPatchFiles() {
        return this.mDisPatchFiles;
    }
}
