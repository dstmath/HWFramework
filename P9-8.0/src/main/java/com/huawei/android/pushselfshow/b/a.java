package com.huawei.android.pushselfshow.b;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.URLUtil;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.util.ArrayList;
import java.util.List;

public class a {
    private static final String[] a = new String[]{"phone", CheckVersionField.CHECK_VERSION_SERVER_URL, "email", "app", "cosa", "rp"};
    private Context b;
    private com.huawei.android.pushselfshow.c.a c;

    public a(Context context, com.huawei.android.pushselfshow.c.a aVar) {
        this.b = context;
        this.c = aVar;
    }

    public static boolean a(String str) {
        for (String equals : a) {
            if (equals.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private String b(String str) {
        try {
            int indexOf = str.indexOf(63);
            if (indexOf == -1) {
                return str;
            }
            int i;
            String[] split = str.substring(indexOf + 1).split("&");
            List arrayList = new ArrayList();
            String[] strArr = split;
            for (String str2 : split) {
                if (!(str2.startsWith("h_w_hiapp_referrer") || str2.startsWith("h_w_gp_referrer"))) {
                    arrayList.add(str2);
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (i = 0; i < arrayList.size(); i++) {
                stringBuilder.append((String) arrayList.get(i));
                if (i < arrayList.size() - 1) {
                    stringBuilder.append("&");
                }
            }
            String str3 = "";
            str3 = arrayList.size() != 0 ? str.substring(0, indexOf + 1) + stringBuilder.toString() : str.substring(0, indexOf);
            c.a("PushSelfShowLog", "after delete referrer, the new IntentUri is:" + str3);
            return str3;
        } catch (Throwable e) {
            c.c("PushSelfShowLog", "delete referrer exception", e);
            return str;
        }
    }

    private void b() {
        c.a("PushSelfShowLog", "enter launchUrl");
        try {
            String w = this.c.w();
            String K = this.c.K();
            String C = this.c.C();
            if (!(this.c.B() == 0 || C == null || C.length() <= 0)) {
                if (w.indexOf("?") == -1) {
                    this.c.c(w + "?" + C + "=" + com.huawei.android.pushselfshow.utils.a.a(K));
                } else {
                    this.c.c(w + "&" + C + "=" + com.huawei.android.pushselfshow.utils.a.a(K));
                }
            }
            c.a("PushSelfShowLog", "url =" + w);
            if (this.c.A() != 0) {
                this.c.d(w);
                this.c.f("text/html");
                this.c.e("html");
                i();
                return;
            }
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW").setFlags(268435456).setData(Uri.parse(w));
            this.b.startActivity(intent);
        } catch (Throwable e) {
            c.d("PushSelfShowLog", e.toString(), e);
        }
    }

    private void c() {
        c.a("PushSelfShowLog", "enter launchCall");
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.DIAL").setData(Uri.parse("tel:" + this.c.q())).setFlags(268435456);
            this.b.startActivity(intent);
        } catch (Throwable e) {
            c.d("PushSelfShowLog", e.toString(), e);
        }
    }

    private void d() {
        c.a("PushSelfShowLog", "enter launchMail");
        try {
            if (this.c.r() != null) {
                Intent intent = new Intent();
                String str = "android.intent.extra.SUBJECT";
                str = "android.intent.extra.TEXT";
                intent.setAction("android.intent.action.SENDTO").setFlags(268435456).setData(Uri.fromParts("mailto", this.c.r(), null)).putExtra(str, this.c.s()).putExtra(str, this.c.t()).setPackage("com.android.email");
                this.b.startActivity(intent);
            }
        } catch (Throwable e) {
            c.d("PushSelfShowLog", e.toString(), e);
        }
    }

    private void e() {
        try {
            c.b("PushSelfShowLog", "enter launchApp, appPackageName =" + this.c.u() + ",and msg.intentUri is " + this.c.g());
            if (com.huawei.android.pushselfshow.utils.a.c(this.b, this.c.u())) {
                h();
                return;
            }
            try {
                c.e("PushSelfShowLog", "insert into db message.getMsgId() is " + this.c.a() + ",message.appPackageName is " + this.c.u());
                com.huawei.android.pushselfshow.utils.a.a.a(this.b, this.c.a(), this.c.u());
            } catch (Throwable e) {
                c.e("PushSelfShowLog", "launchApp not exist ,insertAppinfo error", e);
            }
            c.b("PushSelfShowLog", "enter launch app, appPackageName =" + this.c.u() + ",and msg.intentUri is " + this.c.g());
            f();
        } catch (Exception e2) {
            c.d("PushSelfShowLog", "launchApp error:" + e2.toString());
        }
    }

    private void f() {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            if (!TextUtils.isEmpty(this.c.g())) {
                stringBuilder.append("&referrer=").append(Uri.encode(b(this.c.g())));
            }
            String str = "market://details?id=" + this.c.u() + stringBuilder;
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse(str));
            intent.setPackage("com.huawei.appmarket");
            Intent intent2 = new Intent("android.intent.action.VIEW");
            intent2.setData(Uri.parse(str));
            intent2.setPackage("com.android.vending");
            if (com.huawei.android.pushselfshow.utils.a.a(this.b, "com.android.vending", intent2).booleanValue()) {
                intent2.setFlags(402653184);
                c.b("PushSelfShowLog", "open google play store's app detail, IntentUrl is:" + intent2.toURI());
                this.b.startActivity(intent2);
            } else if (com.huawei.android.pushselfshow.utils.a.a(this.b, "com.huawei.appmarket", intent).booleanValue()) {
                com.huawei.android.pushselfshow.utils.a.a(this.b, "7", this.c, -1);
                intent.setFlags(402653184);
                c.b("PushSelfShowLog", "open HiApp's app detail, IntentUrl is:" + intent.toURI());
                this.b.startActivity(intent);
            } else {
                c.b("PushSelfShowLog", "open app detail by browser.");
                g();
            }
        } catch (Exception e) {
            c.d("PushSelfShowLog", "open market app detail failed,exception:" + e);
        }
    }

    private void g() {
        String str;
        String str2 = "";
        String str3 = "";
        try {
            Uri parse = Uri.parse(Uri.decode(this.c.g()));
            try {
                str2 = parse.getQueryParameter("h_w_hiapp_referrer");
            } catch (Exception e) {
                c.b("PushSelfShowLog", "parse h_w_hiapp_referrer faied");
            }
            try {
                str3 = parse.getQueryParameter("h_w_gp_referrer");
            } catch (Exception e2) {
                c.b("PushSelfShowLog", "parse h_w_hiapp_referrer faied");
            }
        } catch (Throwable e3) {
            c.b("PushSelfShowLog", "parse intentUri error,", e3);
        }
        String decode;
        if (com.huawei.android.pushagent.a.a.a.c() && com.huawei.android.pushagent.a.a.a.d()) {
            c.b("PushSelfShowLog", "It is China device, open Huawei market web, referrer: " + str2);
            decode = Uri.decode(str2);
            if (!URLUtil.isValidUrl(decode)) {
                str = "http://a.vmall.com/";
            }
            str = decode;
        } else {
            c.b("PushSelfShowLog", "not EMUI system or not in China, open google play web, referrer: " + str3);
            decode = Uri.decode(str3);
            if (!URLUtil.isValidUrl(decode)) {
                str = "https://play.google.com/store/apps/details?id=" + this.c.u();
            }
            str = decode;
        }
        c.b("PushSelfShowLog", "open the URL by browser: " + str);
        com.huawei.android.pushselfshow.utils.a.e(this.b, str);
    }

    private void h() {
        c.e("PushSelfShowLog", "run into launchCosaApp ");
        try {
            c.b("PushSelfShowLog", "enter launchExistApp cosa, appPackageName =" + this.c.u() + ",and msg.intentUri is " + this.c.g());
            Intent b = com.huawei.android.pushselfshow.utils.a.b(this.b, this.c.u());
            Object obj = null;
            Intent intent;
            if (this.c.g() == null) {
                if (this.c.v() != null) {
                    intent = new Intent(this.c.v());
                    if (com.huawei.android.pushselfshow.utils.a.a(this.b, this.c.u(), intent).booleanValue()) {
                        b = intent;
                    }
                }
                b.setPackage(this.c.u());
            } else {
                try {
                    intent = Intent.parseUri(this.c.g(), 0);
                    c.b("PushSelfShowLog", "Intent.parseUri(msg.intentUri, 0)," + intent.toURI());
                    if (com.huawei.android.pushselfshow.utils.a.a(this.b, this.c.u(), intent).booleanValue()) {
                        b = intent;
                        obj = 1;
                    }
                } catch (Throwable e) {
                    c.b("PushSelfShowLog", "intentUri error ", e);
                }
            }
            if (b == null) {
                c.b("PushSelfShowLog", "launchCosaApp,intent == null");
            } else if (com.huawei.android.pushselfshow.utils.a.a(this.b, b)) {
                if (obj == null) {
                    b.setFlags(805437440);
                } else {
                    b.addFlags(268435456);
                }
                c.b("PushSelfShowLog", "start " + b.toURI());
                this.b.startActivity(b);
            } else {
                c.c("PushSelfShowLog", "no permission to start Activity");
            }
        } catch (Throwable e2) {
            c.d("PushSelfShowLog", e2.toString(), e2);
        }
    }

    private void i() {
        try {
            c.e("PushSelfShowLog", "run into launchRichPush ");
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(this.b.getPackageName(), "com.huawei.android.pushselfshow.richpush.RichPushHtmlActivity"));
            intent.putExtra("type", this.c.y());
            intent.putExtra("selfshow_info", this.c.c());
            intent.putExtra("selfshow_token", this.c.d());
            intent.setFlags(268468240);
            intent.setPackage(this.b.getPackageName());
            this.b.startActivity(intent);
        } catch (Throwable e) {
            c.d("PushSelfShowLog", "launchRichPush failed", e);
        }
    }

    public void a() {
        c.a("PushSelfShowLog", "enter launchNotify()");
        if (this.b == null || this.c == null) {
            c.a("PushSelfShowLog", "launchNotify  context or msg is null");
            return;
        }
        if ("app".equals(this.c.m())) {
            e();
        } else {
            if ("cosa".equals(this.c.m())) {
                h();
            } else {
                if ("email".equals(this.c.m())) {
                    d();
                } else {
                    if ("phone".equals(this.c.m())) {
                        c();
                    } else {
                        if ("rp".equals(this.c.m())) {
                            i();
                        } else {
                            if (CheckVersionField.CHECK_VERSION_SERVER_URL.equals(this.c.m())) {
                                b();
                            } else {
                                c.a("PushSelfShowLog", this.c.m() + " is not exist in hShowType");
                            }
                        }
                    }
                }
            }
        }
    }
}
