package com.huawei.android.launcher;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;

public class ShareDropHelper {
    private static final String SHARE_TYPE = "application/vnd.android.package-archive";
    private static final String TAG = "ShareDropHelper";

    public static void share(Context context, File file, String title) {
        if (context == null || file == null || TextUtils.isEmpty(title)) {
            Log.w(TAG, "Failed to share.");
            return;
        }
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType(SHARE_TYPE);
        intent.putExtra("android.intent.extra.STREAM", Uri.fromFile(file));
        try {
            context.startActivity(Intent.createChooser(intent, title));
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "ActivityNotFoundException appears.", e);
        }
    }
}
