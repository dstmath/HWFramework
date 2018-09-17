package com.huawei.android.pushselfshow.d;

import android.app.Notification.BigPictureStyle;
import android.app.Notification.Builder;
import android.app.Notification.InboxStyle;
import android.app.Notification.Style;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.RemoteViews;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.c.a;
import com.huawei.android.pushselfshow.utils.d;

public class e {
    public static RemoteViews a(Context context, int i, Bitmap bitmap, a aVar) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), d.a(context, "layout", "hwpush_layout7"));
        c.a(context, bitmap, remoteViews);
        remoteViews.setTextViewText(d.a(context, "id", "title"), c.a(context, aVar));
        remoteViews.setTextViewText(d.a(context, "id", "text"), aVar.n());
        if (aVar.F() == null || aVar.F().length <= 0 || aVar.G() == null || aVar.G().length <= 0 || aVar.F().length != aVar.G().length) {
            return remoteViews;
        }
        com.huawei.android.pushselfshow.utils.c.a aVar2 = new com.huawei.android.pushselfshow.utils.c.a();
        remoteViews.removeAllViews(d.a(context, "id", "linear_buttons"));
        int i2 = 0;
        while (i2 < aVar.F().length) {
            RemoteViews remoteViews2 = new RemoteViews(context.getPackageName(), d.a(context, "layout", "hwpush_buttons_layout"));
            Bitmap bitmap2 = null;
            if (!TextUtils.isEmpty(aVar.F()[i2])) {
                bitmap2 = aVar2.a(context, aVar.F()[i2]);
            }
            if (!(bitmap2 == null || TextUtils.isEmpty(aVar.G()[i2]))) {
                int a = d.a(context, "id", "small_btn");
                remoteViews2.setImageViewBitmap(a, bitmap2);
                remoteViews2.setOnClickPendingIntent(a, c.a(context, i, aVar.G()[i2]));
                remoteViews.addView(d.a(context, "id", "linear_buttons"), remoteViews2);
            }
            i2++;
        }
        return remoteViews;
    }

    public static RemoteViews a(Context context, Bitmap bitmap, a aVar) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), d.a(context, "layout", "hwpush_layout8"));
        Bitmap bitmap2 = null;
        if (!TextUtils.isEmpty(aVar.J())) {
            bitmap2 = new com.huawei.android.pushselfshow.utils.c.a().a(context, aVar.J());
        }
        if (bitmap2 == null) {
            return null;
        }
        remoteViews.setViewVisibility(d.a(context, "id", "big_pic"), 0);
        remoteViews.setImageViewBitmap(d.a(context, "id", "big_pic"), bitmap2);
        return remoteViews;
    }

    public static void a(Context context, Builder builder, int i, Bitmap bitmap, a aVar) {
        if (aVar == null || aVar.n() == null) {
            c.b("PushSelfShowLog", "msg is null");
        } else if (!TextUtils.isEmpty(aVar.n()) && aVar.n().contains("##")) {
            builder.setTicker(aVar.n().replace("##", "，"));
            if (com.huawei.android.pushselfshow.utils.a.c()) {
                int i2;
                builder.setLargeIcon(bitmap);
                builder.setContentTitle(c.a(context, aVar));
                Style inboxStyle = new InboxStyle();
                String[] split = aVar.n().split("##");
                int length = split.length;
                if (length > 4) {
                    length = 4;
                }
                if (!TextUtils.isEmpty(aVar.I())) {
                    inboxStyle.setBigContentTitle(aVar.I());
                    builder.setContentText(aVar.I());
                    if (4 == length) {
                        length--;
                    }
                }
                for (i2 = 0; i2 < length; i2++) {
                    inboxStyle.addLine(split[i2]);
                }
                if (aVar.E() != null && aVar.E().length > 0) {
                    i2 = 0;
                    while (i2 < aVar.E().length) {
                        if (!(TextUtils.isEmpty(aVar.E()[i2]) || TextUtils.isEmpty(aVar.G()[i2]))) {
                            builder.addAction(0, aVar.E()[i2], c.a(context, i, aVar.G()[i2]));
                        }
                        i2++;
                    }
                }
                builder.setStyle(inboxStyle);
                return;
            }
            builder.setContentText(aVar.n().replace("##", "，"));
        }
    }

    public static boolean b(Context context, Builder builder, int i, Bitmap bitmap, a aVar) {
        builder.setContentTitle(c.a(context, aVar));
        builder.setContentText(aVar.n());
        builder.setLargeIcon(bitmap);
        if (!com.huawei.android.pushselfshow.utils.a.c()) {
            return true;
        }
        com.huawei.android.pushselfshow.utils.c.a aVar2 = new com.huawei.android.pushselfshow.utils.c.a();
        Bitmap bitmap2 = null;
        if (!TextUtils.isEmpty(aVar.J())) {
            bitmap2 = aVar2.a(context, aVar.J());
        }
        if (bitmap2 == null) {
            return false;
        }
        Style bigPictureStyle = new BigPictureStyle();
        bigPictureStyle.bigPicture(bitmap2);
        int i2 = 0;
        while (i2 < aVar.E().length) {
            if (!(TextUtils.isEmpty(aVar.E()[i2]) || TextUtils.isEmpty(aVar.G()[i2]))) {
                builder.addAction(0, aVar.E()[i2], c.a(context, i, aVar.G()[i2]));
            }
            i2++;
        }
        builder.setStyle(bigPictureStyle);
        return true;
    }
}
