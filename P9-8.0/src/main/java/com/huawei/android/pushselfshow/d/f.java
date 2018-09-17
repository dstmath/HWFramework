package com.huawei.android.pushselfshow.d;

import android.app.Notification;
import android.app.Notification.Builder;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.RemoteViews;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.c.a;
import com.huawei.android.pushselfshow.utils.d;

public class f {
    public static Builder a(Context context, Builder builder, int i, a aVar, Bitmap bitmap) {
        c.a("PushSelfShowLog", "Notification addStyle");
        if (context == null || builder == null || aVar == null) {
            return builder;
        }
        a aVar2 = a.STYLE_1;
        if (aVar.D() >= 0 && aVar.D() < a.values().length) {
            aVar2 = a.values()[aVar.D()];
        }
        switch (aVar2) {
            case STYLE_2:
                builder.setContent(a(context, i, bitmap, aVar));
                break;
            case STYLE_4:
                builder.setContent(b(context, i, bitmap, aVar));
                break;
            case STYLE_5:
                e.a(context, builder, i, bitmap, aVar);
                break;
            case STYLE_6:
                if (!e.b(context, builder, i, bitmap, aVar)) {
                    return null;
                }
                break;
            case STYLE_7:
                builder.setContent(e.a(context, i, bitmap, aVar));
                break;
            case STYLE_8:
                RemoteViews a = e.a(context, bitmap, aVar);
                if (a != null) {
                    builder.setContent(a);
                    break;
                }
                return null;
        }
        return builder;
    }

    public static Notification a(Context context, Notification notification, int i, a aVar, Bitmap bitmap) {
        if (notification == null || aVar == null) {
            return notification;
        }
        RemoteViews a;
        a aVar2 = a.STYLE_1;
        if (aVar.D() >= 0 && aVar.D() < a.values().length) {
            aVar2 = a.values()[aVar.D()];
        }
        switch (aVar2) {
            case STYLE_2:
                a = a(context, i, bitmap, aVar);
                break;
            case STYLE_4:
                a = b(context, i, bitmap, aVar);
                break;
            case STYLE_7:
                a = e.a(context, i, bitmap, aVar);
                break;
            case STYLE_8:
                RemoteViews a2 = e.a(context, bitmap, aVar);
                if (a2 != null) {
                    notification.contentView = a2;
                    break;
                }
                return null;
        }
        notification.contentView = a;
        return notification;
    }

    private static RemoteViews a(Context context, int i, Bitmap bitmap, a aVar) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), d.c(context, "hwpush_layout2"));
        c.a(context, bitmap, remoteViews);
        c.a(context, i, remoteViews, aVar);
        remoteViews.setTextViewText(d.e(context, "title"), c.a(context, aVar));
        remoteViews.setTextViewText(d.e(context, "text"), aVar.n());
        return remoteViews;
    }

    private static RemoteViews b(Context context, int i, Bitmap bitmap, a aVar) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), d.c(context, "hwpush_layout4"));
        c.a(context, bitmap, remoteViews);
        c.a(context, i, remoteViews, aVar);
        remoteViews.setTextViewText(d.e(context, "title"), c.a(context, aVar));
        if (aVar.H() == null || aVar.H().length <= 0) {
            return remoteViews;
        }
        com.huawei.android.pushselfshow.utils.c.a aVar2 = new com.huawei.android.pushselfshow.utils.c.a();
        remoteViews.removeAllViews(d.e(context, "linear_icons"));
        Bitmap bitmap2 = null;
        for (int i2 = 0; i2 < aVar.H().length; i2++) {
            RemoteViews remoteViews2 = new RemoteViews(context.getPackageName(), d.a(context, "layout", "hwpush_icons_layout"));
            if (!TextUtils.isEmpty(aVar.H()[i2])) {
                bitmap2 = aVar2.a(context, aVar.H()[i2]);
            }
            if (bitmap2 != null) {
                c.a("PushSelfShowLog", "rescale bitmap success");
                remoteViews2.setImageViewBitmap(d.a(context, "id", "smallicon"), bitmap2);
                remoteViews.addView(d.a(context, "id", "linear_icons"), remoteViews2);
            }
        }
        return remoteViews;
    }
}
