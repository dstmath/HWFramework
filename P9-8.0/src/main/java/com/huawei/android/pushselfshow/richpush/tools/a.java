package com.huawei.android.pushselfshow.richpush.tools;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.widget.Toast;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.utils.d;
import java.io.File;

public class a {
    public Resources a;
    public Activity b;
    private com.huawei.android.pushselfshow.c.a c = null;

    public a(Activity activity) {
        this.b = activity;
        this.a = activity.getResources();
    }

    public void a() {
        try {
            c.a("PushSelfShowLog", "creat shortcut");
            Intent intent = new Intent();
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            Parcelable decodeResource = BitmapFactory.decodeResource(this.b.getResources(), d.g(this.b, "hwpush_main_icon"));
            intent.putExtra("android.intent.extra.shortcut.NAME", this.b.getResources().getString(d.a(this.b, "hwpush_msg_collect")));
            intent.putExtra("android.intent.extra.shortcut.ICON", decodeResource);
            intent.putExtra("duplicate", false);
            Object intent2 = new Intent("com.huawei.android.push.intent.RICHPUSH");
            intent2.putExtra("type", "favorite");
            intent2.addFlags(1476395008);
            String str = "com.huawei.android.pushagent";
            if (com.huawei.android.pushselfshow.utils.a.c(this.b, str)) {
                intent2.setPackage(str);
            } else {
                intent2.setPackage(this.b.getPackageName());
            }
            intent.putExtra("android.intent.extra.shortcut.INTENT", intent2);
            this.b.sendBroadcast(intent);
        } catch (Throwable e) {
            c.e("PushSelfShowLog", "creat shortcut error", e);
        }
    }

    public void a(com.huawei.android.pushselfshow.c.a aVar) {
        this.c = aVar;
    }

    public void b() {
        try {
            if (this.c == null || this.c.x() == null) {
                Toast.makeText(this.b, this.b.getResources().getString(d.a(this.b, "hwpush_save_failed")), 0).show();
            }
            c.e("PushSelfShowLog", "the rpl is " + this.c.x());
            String str = "";
            str = !this.c.x().startsWith("file://") ? this.c.x() : this.c.x().substring(7);
            c.e("PushSelfShowLog", "filePath is " + str);
            if ("text/html_local".equals(this.c.z())) {
                File parentFile = new File(str).getParentFile();
                if (parentFile != null && parentFile.isDirectory() && this.c.x().contains("richpush")) {
                    String absolutePath = parentFile.getAbsolutePath();
                    String replace = absolutePath.replace("richpush", "shotcut");
                    c.b("PushSelfShowLog", "srcDir is %s ,destDir is %s", absolutePath, replace);
                    if (com.huawei.android.pushselfshow.utils.a.a(absolutePath, replace)) {
                        this.c.d(Uri.fromFile(new File(replace + File.separator + "index.html")).toString());
                    } else {
                        c.b("PushSelfShowLog", "rich push save failed");
                        return;
                    }
                }
            }
            c.a("PushSelfShowLog", "insert data into db");
            a();
            boolean a = com.huawei.android.pushselfshow.utils.a.d.a(this.b, this.c.o(), this.c);
            c.e("PushSelfShowLog", "insert result is " + a);
            if (a) {
                com.huawei.android.pushselfshow.utils.a.a(this.b, "14", this.c, -1);
            } else {
                c.d("PushSelfShowLog", "save icon fail");
            }
        } catch (Throwable e) {
            c.d("PushSelfShowLog", "SaveBtnClickListener error ", e);
        }
    }
}
