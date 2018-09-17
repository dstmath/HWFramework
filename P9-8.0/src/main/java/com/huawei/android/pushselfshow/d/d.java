package com.huawei.android.pushselfshow.d;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.c.a;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class d {
    private static int a = 0;
    private static HashMap b = new HashMap();

    public static Notification a(Context context, a aVar, int i, int i2, int i3) {
        int identifier;
        Bitmap b;
        Notification notification = new Notification();
        notification.icon = b.a(context, aVar);
        int i4 = context.getApplicationInfo().labelRes;
        notification.tickerText = aVar.n();
        notification.when = System.currentTimeMillis();
        notification.flags |= 16;
        notification.defaults |= 1;
        PendingIntent a = a(context, aVar, i, i2);
        notification.contentIntent = a;
        notification.deleteIntent = b(context, aVar, i, i3);
        if (aVar.p() != null) {
            if (!"".equals(aVar.p())) {
                notification.setLatestEventInfo(context, aVar.p(), aVar.n(), a);
                identifier = context.getResources().getIdentifier("icon", "id", "android");
                b = b.b(context, aVar);
                if (!(identifier == 0 || notification.contentView == null || b == null)) {
                    notification.contentView.setImageViewBitmap(identifier, b);
                }
                return f.a(context, notification, i, aVar, b);
            }
        }
        notification.setLatestEventInfo(context, context.getResources().getString(i4), aVar.n(), a);
        identifier = context.getResources().getIdentifier("icon", "id", "android");
        b = b.b(context, aVar);
        notification.contentView.setImageViewBitmap(identifier, b);
        return f.a(context, notification, i, aVar, b);
    }

    private static PendingIntent a(Context context, a aVar, int i, int i2) {
        Intent intent = new Intent("com.huawei.intent.action.PUSH");
        String str = "selfshow_token";
        str = "extra_encrypt_data";
        intent.putExtra("selfshow_info", aVar.c()).putExtra(str, aVar.d()).putExtra("selfshow_event_id", "1").putExtra(str, com.huawei.android.pushselfshow.utils.a.m(context)).putExtra("selfshow_notify_id", i).setPackage(context.getPackageName()).setFlags(268435456);
        return PendingIntent.getBroadcast(context, i2, intent, 134217728);
    }

    private static void a(Context context, Builder builder, a aVar) {
        if ("com.huawei.android.pushagent".equals(context.getPackageName())) {
            Bundle bundle = new Bundle();
            Object k = aVar.k();
            if (!TextUtils.isEmpty(k)) {
                bundle.putString("hw_origin_sender_package_name", k);
                builder.setExtras(bundle);
            }
        }
    }

    public static void a(Context context, Intent intent, long j, int i) {
        try {
            c.a("PushSelfShowLog", "enter setDelayAlarm(intent:" + intent.toURI() + " interval:" + j + "ms, context:" + context);
            ((AlarmManager) context.getSystemService("alarm")).set(0, System.currentTimeMillis() + j, PendingIntent.getBroadcast(context, i, intent, 134217728));
        } catch (Throwable e) {
            c.a("PushSelfShowLog", "set DelayAlarm error", e);
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0006, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void a(Context context, a aVar) {
        synchronized (d.class) {
            if (!(context == null || aVar == null)) {
                try {
                    int i;
                    int i2;
                    int i3;
                    int i4;
                    c.a("PushSelfShowLog", " showNotification , the msg id = " + aVar.a());
                    com.huawei.android.pushselfshow.utils.a.a(2, (int) SmsCheckResult.ESCT_180);
                    if (a == 0) {
                        a = (context.getPackageName() + new Date().toString()).hashCode();
                    }
                    if (TextUtils.isEmpty(aVar.e())) {
                        i = a + 1;
                        a = i;
                        i2 = a + 1;
                        a = i2;
                        i3 = a + 1;
                        a = i3;
                        i4 = a + 1;
                        a = i4;
                    } else {
                        i = (aVar.k() + aVar.e()).hashCode();
                        i2 = a + 1;
                        a = i2;
                        i3 = a + 1;
                        a = i3;
                        i4 = a + 1;
                        a = i4;
                    }
                    c.a("PushSelfShowLog", "notifyId:" + i + ",openNotifyId:" + i2 + ",delNotifyId:" + i3 + ",alarmNotifyId:" + i4);
                    Notification a = !com.huawei.android.pushselfshow.utils.a.b() ? a(context, aVar, i, i2, i3) : b(context, aVar, i, i2, i3);
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
                    if (!(notificationManager == null || a == null)) {
                        notificationManager.notify(i, a);
                        if (aVar.f() > 0) {
                            Intent intent = new Intent("com.huawei.intent.action.PUSH");
                            String str = "extra_encrypt_data";
                            intent.putExtra("selfshow_info", aVar.c()).putExtra("selfshow_token", aVar.d()).putExtra("selfshow_event_id", "-1").putExtra(str, com.huawei.android.pushselfshow.utils.a.m(context)).putExtra("selfshow_notify_id", i).setPackage(context.getPackageName()).setFlags(32);
                            a(context, intent, (long) aVar.f(), i4);
                            c.a("PushSelfShowLog", "setDelayAlarm alarmNotityId" + i4 + " and intent is " + intent.toURI());
                        }
                        if (!"com.huawei.android.pushagent".equals(context.getPackageName()) || TextUtils.isEmpty(aVar.M()) || TextUtils.isEmpty(aVar.k()) || aVar.L() == 0) {
                            c.a("PushSelfShowLog", "badgeClassName is null or permission denied ");
                        } else {
                            c.a("PushSelfShowLog", "need to refresh badge number. package name is " + aVar.k());
                            com.huawei.android.pushselfshow.a.a.a(context, aVar.k(), aVar.M(), aVar.L());
                        }
                        com.huawei.android.pushselfshow.utils.a.a(context, "0", aVar, i);
                    }
                } catch (Exception e) {
                    c.d("PushSelfShowLog", "showNotification error " + e.toString());
                }
            }
        }
    }

    public static void a(String str) {
        if (TextUtils.isEmpty(str)) {
            c.d("PushSelfShowLog", "enter clearGroupCount, key is empty");
            return;
        }
        if (b.containsKey(str)) {
            b.remove(str);
            c.a("PushSelfShowLog", "after remove, groupMap.size is:" + b.get(str));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0144  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0149  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0144  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0149  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Notification b(Context context, a aVar, int i, int i2, int i3) {
        Bitmap b;
        Builder builder = new Builder(context);
        b.a(context, builder, aVar);
        int i4 = context.getApplicationInfo().labelRes;
        builder.setTicker(aVar.n());
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);
        builder.setDefaults(1);
        String str = aVar.k() + aVar.e();
        if (!TextUtils.isEmpty(aVar.e())) {
            c.a("PushSelfShowLog", "groupMap key is " + str);
            if (b.containsKey(str)) {
                b.put(str, Integer.valueOf(((Integer) b.get(str)).intValue() + 1));
                c.a("PushSelfShowLog", "groupMap.size:" + b.get(str));
            } else {
                b.put(str, Integer.valueOf(1));
            }
        }
        if (aVar.p() != null) {
            if (!"".equals(aVar.p())) {
                builder.setContentTitle(aVar.p());
                if (TextUtils.isEmpty(aVar.e()) || ((Integer) b.get(str)).intValue() == 1) {
                    builder.setContentText(aVar.n());
                } else {
                    builder.setContentText(context.getResources().getQuantityString(com.huawei.android.pushselfshow.utils.d.b(context, "hwpush_message_hint"), r3, new Object[]{Integer.valueOf(((Integer) b.get(str)).intValue())}));
                }
                builder.setContentIntent(a(context, aVar, i, i2));
                builder.setDeleteIntent(b(context, aVar, i, i3));
                b = b.b(context, aVar);
                if (b != null) {
                    builder.setLargeIcon(b);
                }
                a(context, builder, aVar);
                b(context, builder, aVar);
                return f.a(context, builder, i, aVar, b) != null ? builder.getNotification() : null;
            }
        }
        builder.setContentTitle(context.getResources().getString(i4));
        if (TextUtils.isEmpty(aVar.e())) {
            builder.setContentText(context.getResources().getQuantityString(com.huawei.android.pushselfshow.utils.d.b(context, "hwpush_message_hint"), r3, new Object[]{Integer.valueOf(((Integer) b.get(str)).intValue())}));
            builder.setContentIntent(a(context, aVar, i, i2));
            builder.setDeleteIntent(b(context, aVar, i, i3));
            b = b.b(context, aVar);
            if (b != null) {
            }
            a(context, builder, aVar);
            b(context, builder, aVar);
            if (f.a(context, builder, i, aVar, b) != null) {
            }
        }
        builder.setContentText(aVar.n());
        builder.setContentIntent(a(context, aVar, i, i2));
        builder.setDeleteIntent(b(context, aVar, i, i3));
        b = b.b(context, aVar);
        if (b != null) {
        }
        a(context, builder, aVar);
        b(context, builder, aVar);
        if (f.a(context, builder, i, aVar, b) != null) {
        }
    }

    private static PendingIntent b(Context context, a aVar, int i, int i2) {
        Intent intent = new Intent("com.huawei.intent.action.PUSH");
        String str = "selfshow_token";
        str = "extra_encrypt_data";
        intent.putExtra("selfshow_info", aVar.c()).putExtra(str, aVar.d()).putExtra("selfshow_event_id", "2").putExtra("selfshow_notify_id", i).setPackage(context.getPackageName()).putExtra(str, com.huawei.android.pushselfshow.utils.a.m(context)).setFlags(268435456);
        return PendingIntent.getBroadcast(context, i2, intent, 134217728);
    }

    private static void b(Context context, Builder builder, a aVar) {
        if (com.huawei.android.pushagent.a.a.a.a() >= 11 && com.huawei.android.pushselfshow.utils.a.f(context)) {
            Bundle bundle = new Bundle();
            String k = aVar.k();
            c.b("PushSelfShowLog", "the package name of notification is:" + k);
            if (!TextUtils.isEmpty(k)) {
                CharSequence a = com.huawei.android.pushselfshow.utils.a.a(context, k);
                c.b("PushSelfShowLog", "the app name is:" + a);
                if (a != null) {
                    bundle.putCharSequence("android.extraAppName", a);
                }
            }
            builder.setExtras(bundle);
        }
    }

    public static void b(String str) {
        if (TextUtils.isEmpty(str)) {
            c.d("PushSelfShowLog", "enter clearAllGroupCount, pkgname is empty");
            return;
        }
        LinkedList linkedList = new LinkedList();
        for (String str2 : b.keySet()) {
            c.a("PushSelfShowLog", "clearAllGroupCount, group is:" + str2);
            if (str2.contains(str)) {
                linkedList.add(str2);
            }
        }
        Iterator it = linkedList.iterator();
        while (it.hasNext()) {
            b.remove((String) it.next());
        }
    }
}
