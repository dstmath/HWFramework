package com.huawei.android.pushselfshow.utils;

import android.content.Context;
import com.huawei.android.pushagent.a.a.c;

class b implements Runnable {
    final /* synthetic */ Context a;
    final /* synthetic */ String b;
    final /* synthetic */ String c;
    final /* synthetic */ String d;
    final /* synthetic */ String e;
    final /* synthetic */ int f;

    b(Context context, String str, String str2, String str3, String str4, int i) {
        this.a = context;
        this.b = str;
        this.c = str2;
        this.d = str3;
        this.e = str4;
        this.f = i;
    }

    public void run() {
        try {
            if (a.o(this.a)) {
                String str = "PUSH_PS";
                String stringBuffer = new StringBuffer(String.valueOf(a.a())).append("|").append("PS").append("|").append(a.b(this.a)).append("|").append(this.b).append("|").append(this.c).append("|").append(a.a(this.a)).append("|").append(this.d).append("|").append(this.e).append("|").append(this.f).toString();
                if (this.a != null) {
                    Class cls = Class.forName("com.hianalytics.android.v1.HiAnalytics");
                    cls.getMethod("onEvent", new Class[]{Context.class, String.class, String.class}).invoke(cls, new Object[]{this.a, str, stringBuffer});
                    cls.getMethod("onReport", new Class[]{Context.class}).invoke(cls, new Object[]{this.a});
                    c.b("PushSelfShowLog", "send HiAnalytics msg, report cmd =" + this.d + ", msgid = " + this.b + ", eventId = " + this.c);
                }
                return;
            }
            c.b("PushSelfShowLog", "not allowed to sendHiAnalytics!");
        } catch (Throwable e) {
            c.e("PushSelfShowLog", "sendHiAnalytics IllegalAccessException ", e);
        } catch (Throwable e2) {
            c.e("PushSelfShowLog", "sendHiAnalytics IllegalArgumentException ", e2);
        } catch (Throwable e22) {
            c.e("PushSelfShowLog", "sendHiAnalytics InvocationTargetException", e22);
        } catch (Throwable e222) {
            c.e("PushSelfShowLog", "sendHiAnalytics NoSuchMethodException", e222);
        } catch (ClassNotFoundException e3) {
            c.e("PushSelfShowLog", "sendHiAnalytics ClassNotFoundException");
        }
    }
}
