package com.huawei.android.content.pm.dex;

import android.content.pm.dex.DexMetadataHelper;
import java.io.File;

public class DexMetadataHelperEx {
    public static File findDexMetadataForFile(File targetFile) {
        return DexMetadataHelper.findDexMetadataForFile(targetFile);
    }
}
