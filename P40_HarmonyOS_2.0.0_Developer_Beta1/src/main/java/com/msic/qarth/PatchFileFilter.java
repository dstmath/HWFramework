package com.msic.qarth;

import android.content.pm.PackageInfo;
import com.msic.qarth.PatchFile;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
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

    @Override // java.io.FilenameFilter
    public boolean accept(File dir, String filename) {
        return matchPattern(dir, filename);
    }

    private boolean matchPattern(File dir, String fileName) {
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "matchPattern");
        }
        PatchFile.Builder builder = new PatchFile.Builder();
        try {
            builder.setPath(dir.getCanonicalPath() + File.separator + fileName);
        } catch (IOException e) {
            QarthLog.e(TAG, "get patch file path exception");
        }
        int i = 0;
        while (true) {
            String[] strArr = this.mRegulars;
            if (i >= strArr.length) {
                return false;
            }
            Matcher matcher = Pattern.compile(strArr[i]).matcher(fileName);
            if (matcher.find()) {
                if (i == 0) {
                    this.mPatchFiles.add(builder.setFileName(matcher.group(1)).build());
                } else if (i == 1) {
                    this.mPatchFiles.add(builder.setFileName(matcher.group(1)).setVersionCode(matcher.group(2)).build());
                } else if (i == 2) {
                    this.mPatchFiles.add(builder.setFileName(matcher.group(1)).setVersionName(matcher.group(2)).build());
                } else if (i == 3) {
                    this.mPatchFiles.add(builder.setFileName(matcher.group(1)).setVersionName(matcher.group(2)).setVersionCode(matcher.group(3)).build());
                }
                return matcher.matches();
            }
            i++;
        }
    }

    public List<PatchFile> getPatchFiles() {
        return this.mPatchFiles;
    }
}
