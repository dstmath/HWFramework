package com.msic.qarth;

import android.content.pm.PackageInfo;
import com.msic.qarth.PatchFile;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatchFileFilter implements FilenameFilter {
    private static final String TAG = "PatchFileFilter";
    private final String mAllMatchRegex = "(.*)_all_{0,1}(.*)\\.qarth$";
    private final String mIgnoreVCodeMatchRegex = ("(.*)_(" + this.mVersionName + ")_{0,1}(.*)\\.qarth$");
    private final String mIgnoreVNameMatchRegex = ("(.*)_(" + this.mVersionCode + ")_{0,1}(.*)\\.qarth$");
    private final String mMathRegex = ("(.*)_(" + this.mVersionName + ")_(" + this.mVersionCode + ")_{0,1}(.*)\\.qarth$");
    private List<PatchFile> mPatchFiles = new ArrayList();
    private String[] mRegulars = {"(.*)_all_{0,1}(.*)\\.qarth$", this.mIgnoreVCodeMatchRegex, this.mIgnoreVNameMatchRegex, this.mMathRegex};
    private int mVersionCode = 0;
    private String mVersionName = null;

    public PatchFileFilter() {
    }

    public PatchFileFilter(PackageInfo pkgInfo) {
        this.mVersionCode = pkgInfo.versionCode;
        this.mVersionName = pkgInfo.versionName;
    }

    public boolean accept(File dir, String filename) {
        return matchPattern(dir, filename);
    }

    private boolean matchPattern(File dir, String fileName) {
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "matchPattern");
        }
        PatchFile.Builder builder = new PatchFile.Builder();
        builder.setPath(dir.getAbsolutePath() + File.separator + fileName);
        for (int i = 0; i < this.mRegulars.length; i++) {
            Matcher matcher = Pattern.compile(this.mRegulars[i]).matcher(fileName);
            if (matcher.find()) {
                switch (i) {
                    case 0:
                        this.mPatchFiles.add(builder.setFileName(matcher.group(1)).build());
                        break;
                    case 1:
                        this.mPatchFiles.add(builder.setFileName(matcher.group(1)).setVersionCode(matcher.group(2)).build());
                        break;
                    case 2:
                        this.mPatchFiles.add(builder.setFileName(matcher.group(1)).setVersionName(matcher.group(2)).build());
                        break;
                    case 3:
                        this.mPatchFiles.add(builder.setFileName(matcher.group(1)).setVersionName(matcher.group(2)).setVersionCode(matcher.group(3)).build());
                        break;
                }
                return matcher.matches();
            }
        }
        return false;
    }

    public List<PatchFile> getPatchFiles() {
        return this.mPatchFiles;
    }
}
