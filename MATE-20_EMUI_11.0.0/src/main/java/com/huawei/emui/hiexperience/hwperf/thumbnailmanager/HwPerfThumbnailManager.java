package com.huawei.emui.hiexperience.hwperf.thumbnailmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.huawei.emui.hiexperience.hwperf.HwPerfBase;
import com.huawei.kvdb.HwKVDatabase;

public class HwPerfThumbnailManager extends HwPerfBase {
    public static final int TYPE_MICROTHUMBNAIL = 2;
    public static final int TYPE_MICROTHUMBNAIL_SCALE = 1;
    private HwKVDatabase mHwkvDatabase;

    public HwPerfThumbnailManager(Context context) {
        this.mHwkvDatabase = HwKVDatabase.getInstance(context);
    }

    public Bitmap getThumbnail(int id, long timeModified, int mediaType, int type, BitmapFactory.Options options) {
        if (this.mHwkvDatabase == null) {
            return null;
        }
        return this.mHwkvDatabase.getBitmap(HwKVDatabase.generateKey(id, timeModified, mediaType, type), options);
    }
}
