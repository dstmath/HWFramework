package com.huawei.media.scan;

import android.content.Context;
import com.huawei.annotation.HwSystemApi;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

@HwSystemApi
public class DefaultHwMediaStore {
    private static DefaultHwMediaStore sHwMediaStore = new DefaultHwMediaStore();

    public static DefaultHwMediaStore getDefault() {
        if (sHwMediaStore == null) {
            sHwMediaStore = new DefaultHwMediaStore();
        }
        return sHwMediaStore;
    }

    public String getVolumeNameEx(File path) {
        return null;
    }

    public void getHwVolumeScanPaths(Context context, List<File> list, String volumeName) throws FileNotFoundException {
    }
}
