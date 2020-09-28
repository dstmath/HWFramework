package com.huawei.media.scan;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import com.huawei.annotation.HwSystemApi;
import java.io.File;

@HwSystemApi
public interface MediaServiceProxy {
    void scanDirectoryEx(Context context, File file);

    Uri scanFileEx(Context context, File file, ContentValues contentValues);
}
