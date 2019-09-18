package com.msic.qarth;

import android.content.pm.PackageInfo;
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
            aPath = "/data/hotpatch/fwkpatchdir/system/all";
        }
        builder.setPath(aPath + File.separator + fileName.replace(".disable", ""));
        for (int i = 0; i < this.mRegulars.length; i++) {
            Matcher matcher = Pattern.compile(this.mRegulars[i]).matcher(fileName);
            if (matcher.find()) {
                switch (i) {
                    case 0:
                        this.mDisPatchFiles.add(builder.setFileName(matcher.group(1)).build());
                        break;
                    case 1:
                        this.mDisPatchFiles.add(builder.setFileName(matcher.group(1)).setVersionCode(matcher.group(2)).build());
                        break;
                    case 2:
                        this.mDisPatchFiles.add(builder.setFileName(matcher.group(1)).setVersionName(matcher.group(2)).build());
                        break;
                    case 3:
                        this.mDisPatchFiles.add(builder.setFileName(matcher.group(1)).setVersionName(matcher.group(2)).setVersionCode(matcher.group(3)).build());
                        break;
                }
                return matcher.matches();
            }
        }
        return false;
    }

    public List<PatchFile> getDisPatchFiles() {
        return this.mDisPatchFiles;
    }
}
