package com.huawei.android.pushselfshow.c;

import android.text.TextUtils;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.io.Serializable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class a implements Serializable {
    private String A;
    private String B = "";
    private String C;
    private String D;
    private String E;
    private String F;
    private String G;
    private String H = "";
    private int I = 1;
    private int J = 0;
    private String K;
    private String L;
    private String M;
    private int N = com.huawei.android.pushselfshow.d.a.STYLE_1.ordinal();
    private int O = 0;
    private String[] P = null;
    private String[] Q = null;
    private String[] R = null;
    private int S = 0;
    private String[] T = null;
    private String U = "";
    private String V = "";
    public int a = 1;
    public String b = "";
    private String c = "";
    private String d;
    private String e;
    private String f;
    private String g;
    private int h;
    private String i;
    private int j;
    private String k;
    private int l;
    private int m;
    private String n;
    private String o = "";
    private String p = "";
    private String q;
    private String r = "";
    private String s = "";
    private String t = "";
    private String u = "";
    private String v;
    private String w;
    private String x;
    private String y;
    private String z;

    public a(byte[] bArr, byte[] bArr2) {
        try {
            this.L = new String(bArr, "UTF-8");
            this.M = new String(bArr2, "UTF-8");
        } catch (Exception e) {
            c.d("PushSelfShowLog", "get msg byte arr error");
        }
    }

    private boolean a(JSONObject jSONObject) {
        try {
            JSONObject jSONObject2 = jSONObject.getJSONObject("param");
            if (jSONObject2.has("autoClear")) {
                this.h = jSONObject2.getInt("autoClear");
            } else {
                this.h = 0;
            }
            if (!"app".equals(this.r)) {
                if (!"cosa".equals(this.r)) {
                    if ("email".equals(this.r)) {
                        c(jSONObject2);
                    } else {
                        if (!"phone".equals(this.r)) {
                            if (CheckVersionField.CHECK_VERSION_SERVER_URL.equals(this.r)) {
                                d(jSONObject2);
                            } else {
                                if ("rp".equals(this.r)) {
                                    e(jSONObject2);
                                }
                            }
                        } else if (jSONObject2.has("phoneNum")) {
                            this.y = jSONObject2.getString("phoneNum");
                        } else {
                            c.a("PushSelfShowLog", "phoneNum is null");
                            return false;
                        }
                    }
                    return true;
                }
            }
            b(jSONObject2);
            return true;
        } catch (Throwable e) {
            c.d("PushSelfShowLog", "ParseParam error ", e);
            return false;
        }
    }

    private boolean b(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null) {
            return false;
        }
        if (jSONObject.has("acn")) {
            this.D = jSONObject.getString("acn");
            this.i = this.D;
        }
        if (jSONObject.has("intentUri")) {
            this.i = jSONObject.getString("intentUri");
        }
        if (jSONObject.has("appPackageName")) {
            this.C = jSONObject.getString("appPackageName");
            return true;
        }
        c.a("PushSelfShowLog", "appPackageName is null");
        return false;
    }

    private boolean c(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null) {
            return false;
        }
        if (jSONObject.has("emailAddr") && jSONObject.has("emailSubject")) {
            this.z = jSONObject.getString("emailAddr");
            this.A = jSONObject.getString("emailSubject");
            if (jSONObject.has("emailContent")) {
                this.B = jSONObject.getString("emailContent");
            }
            return true;
        }
        c.a("PushSelfShowLog", "emailAddr or emailSubject is null");
        return false;
    }

    private boolean d(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null) {
            return false;
        }
        if (jSONObject.has(CheckVersionField.CHECK_VERSION_SERVER_URL)) {
            this.E = jSONObject.getString(CheckVersionField.CHECK_VERSION_SERVER_URL);
            if (jSONObject.has("inBrowser")) {
                this.I = jSONObject.getInt("inBrowser");
            }
            if (jSONObject.has("needUserId")) {
                this.J = jSONObject.getInt("needUserId");
            }
            if (jSONObject.has("sign")) {
                this.K = jSONObject.getString("sign");
            }
            if (jSONObject.has("rpt") && jSONObject.has("rpl")) {
                this.F = jSONObject.getString("rpl");
                this.G = jSONObject.getString("rpt");
                if (jSONObject.has("rpct")) {
                    this.H = jSONObject.getString("rpct");
                }
            }
            return true;
        }
        c.a("PushSelfShowLog", "url is null");
        return false;
    }

    private boolean e(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null) {
            return false;
        }
        if (jSONObject.has("rpt") && jSONObject.has("rpl")) {
            this.F = jSONObject.getString("rpl");
            this.G = jSONObject.getString("rpt");
            if (jSONObject.has("rpct")) {
                this.H = jSONObject.getString("rpct");
            }
            if (jSONObject.has("needUserId")) {
                this.J = jSONObject.getInt("needUserId");
            }
            return true;
        }
        c.a("PushSelfShowLog", "rpl or rpt is null");
        return false;
    }

    private boolean f(JSONObject jSONObject) {
        c.a("PushSelfShowLog", "enter parseNotifyParam");
        try {
            JSONObject jSONObject2 = jSONObject.getJSONObject("notifyParam");
            if (!jSONObject2.has("style")) {
                return false;
            }
            String str;
            this.N = jSONObject2.getInt("style");
            c.a("PushSelfShowLog", "style:" + this.N);
            if (jSONObject2.has("btnCount")) {
                this.O = jSONObject2.getInt("btnCount");
            }
            if (this.O > 0) {
                if (this.O > 3) {
                    this.O = 3;
                }
                c.a("PushSelfShowLog", "btnCount:" + this.O);
                this.P = new String[this.O];
                this.Q = new String[this.O];
                this.R = new String[this.O];
                for (int i = 0; i < this.O; i++) {
                    String str2 = "btn" + (i + 1) + "Text";
                    str = "btn" + (i + 1) + "Image";
                    String str3 = "btn" + (i + 1) + "Event";
                    if (jSONObject2.has(str2)) {
                        this.P[i] = jSONObject2.getString(str2);
                    }
                    if (jSONObject2.has(str)) {
                        this.Q[i] = jSONObject2.getString(str);
                    }
                    if (jSONObject2.has(str3)) {
                        this.R[i] = jSONObject2.getString(str3);
                    }
                }
            }
            com.huawei.android.pushselfshow.d.a aVar = com.huawei.android.pushselfshow.d.a.STYLE_1;
            if (this.N >= 0 && this.N < com.huawei.android.pushselfshow.d.a.values().length) {
                aVar = com.huawei.android.pushselfshow.d.a.values()[this.N];
            }
            switch (aVar) {
                case STYLE_4:
                    if (jSONObject2.has("iconCount")) {
                        this.S = jSONObject2.getInt("iconCount");
                    }
                    if (this.S > 0) {
                        if (this.S > 6) {
                            this.S = 6;
                        }
                        c.a("PushSelfShowLog", "iconCount:" + this.S);
                        this.T = new String[this.S];
                        for (int i2 = 0; i2 < this.S; i2++) {
                            str = "icon" + (i2 + 1);
                            if (jSONObject2.has(str)) {
                                this.T[i2] = jSONObject2.getString(str);
                            }
                        }
                        break;
                    }
                    break;
                case STYLE_5:
                    if (jSONObject2.has("subTitle")) {
                        this.U = jSONObject2.getString("subTitle");
                        c.a("PushSelfShowLog", "subTitle:" + this.U);
                        break;
                    }
                    break;
                case STYLE_6:
                case STYLE_8:
                    if (jSONObject2.has("bigPic")) {
                        this.V = jSONObject2.getString("bigPic");
                        c.a("PushSelfShowLog", "bigPicUrl:" + this.V);
                        break;
                    }
                    break;
            }
            return true;
        } catch (JSONException e) {
            c.b("PushSelfShowLog", e.toString());
            return false;
        }
    }

    public int A() {
        return this.I;
    }

    public int B() {
        return this.J;
    }

    public String C() {
        return this.K;
    }

    public int D() {
        return this.N;
    }

    public String[] E() {
        return this.P;
    }

    public String[] F() {
        return this.Q;
    }

    public String[] G() {
        return this.R;
    }

    public String[] H() {
        return this.T;
    }

    public String I() {
        return this.U;
    }

    public String J() {
        return this.V;
    }

    public String K() {
        return this.M;
    }

    public int L() {
        return this.a;
    }

    public String M() {
        return this.b;
    }

    public String a() {
        c.a("PushSelfShowLog", "msgId =" + this.o);
        return this.o;
    }

    public void a(String str) {
        this.p = str;
    }

    public void b(String str) {
        this.r = str;
    }

    public boolean b() {
        try {
            if (this.M == null || this.M.length() == 0) {
                c.a("PushSelfShowLog", "token is null");
                return false;
            }
            this.k = this.M;
            if (this.L == null || this.L.length() == 0) {
                c.a("PushSelfShowLog", "msg is null");
                return false;
            }
            JSONObject jSONObject = new JSONObject(this.L);
            this.j = jSONObject.getInt("msgType");
            if (this.j == 1) {
                if (jSONObject.has("group")) {
                    this.c = jSONObject.getString("group");
                    c.a("PushSelfShowLog", "NOTIFY_GROUP:" + this.c);
                }
                if (jSONObject.has("badgeClass")) {
                    this.b = jSONObject.getString("badgeClass");
                    c.a("PushSelfShowLog", "BADGE_CLASS:" + this.b);
                }
                if (jSONObject.has("badgeAddNum")) {
                    this.a = jSONObject.getInt("badgeAddNum");
                    c.a("PushSelfShowLog", "BADGE_ADD_NUM:" + this.a);
                }
                JSONObject jSONObject2 = jSONObject.getJSONObject("msgContent");
                if (jSONObject2 == null) {
                    c.b("PushSelfShowLog", "msgObj == null");
                    return false;
                } else if (jSONObject2.has("msgId")) {
                    Object obj = jSONObject2.get("msgId");
                    if (obj instanceof String) {
                        this.o = (String) obj;
                    } else if (obj instanceof Integer) {
                        this.o = String.valueOf(((Integer) obj).intValue());
                    }
                    if (jSONObject2.has("dispPkgName")) {
                        this.p = jSONObject2.getString("dispPkgName");
                    }
                    if (jSONObject2.has("rtn")) {
                        this.m = jSONObject2.getInt("rtn");
                    } else {
                        this.m = 1;
                    }
                    if (jSONObject2.has("fm")) {
                        this.l = jSONObject2.getInt("fm");
                    } else {
                        this.l = 1;
                    }
                    if (jSONObject2.has("ap")) {
                        String string = jSONObject2.getString("ap");
                        StringBuilder stringBuilder = new StringBuilder();
                        if (!TextUtils.isEmpty(string) && string.length() < 48) {
                            for (int i = 0; i < 48 - string.length(); i++) {
                                stringBuilder.append("0");
                            }
                            stringBuilder.append(string);
                            this.n = stringBuilder.toString();
                        } else {
                            this.n = string.substring(0, 48);
                        }
                    }
                    if (jSONObject2.has("extras")) {
                        this.q = jSONObject2.getJSONArray("extras").toString();
                    }
                    if (!jSONObject2.has("psContent")) {
                        return false;
                    }
                    JSONObject jSONObject3 = jSONObject2.getJSONObject("psContent");
                    if (jSONObject3 == null) {
                        return false;
                    }
                    this.r = jSONObject3.getString("cmd");
                    if (jSONObject3.has("content")) {
                        this.s = jSONObject3.getString("content");
                    } else {
                        this.s = "";
                    }
                    if (jSONObject3.has("notifyIcon")) {
                        this.t = jSONObject3.getString("notifyIcon");
                    } else {
                        this.t = "" + this.o;
                    }
                    if (jSONObject3.has("statusIcon")) {
                        this.v = jSONObject3.getString("statusIcon");
                    }
                    if (jSONObject3.has("notifyTitle")) {
                        this.u = jSONObject3.getString("notifyTitle");
                    }
                    if (jSONObject3.has("notifyParam")) {
                        f(jSONObject3);
                    }
                    return !jSONObject3.has("param") ? false : a(jSONObject3);
                } else {
                    c.b("PushSelfShowLog", "msgId == null");
                    return false;
                }
            }
            c.a("PushSelfShowLog", "not a selefShowMsg");
            return false;
        } catch (Throwable e) {
            c.a("PushSelfShowLog", e.toString(), e);
            return false;
        }
    }

    public void c(String str) {
        this.E = str;
    }

    public byte[] c() {
        try {
            String str = "";
            JSONObject jSONObject = new JSONObject();
            JSONObject jSONObject2 = new JSONObject();
            JSONObject jSONObject3 = new JSONObject();
            JSONObject jSONObject4 = new JSONObject();
            jSONObject4.put("autoClear", this.h);
            jSONObject4.put("s", this.d);
            jSONObject4.put("r", this.e);
            jSONObject4.put("smsC", this.f);
            jSONObject4.put("mmsUrl", this.g);
            jSONObject4.put(CheckVersionField.CHECK_VERSION_SERVER_URL, this.E);
            jSONObject4.put("inBrowser", this.I);
            jSONObject4.put("needUserId", this.J);
            jSONObject4.put("sign", this.K);
            jSONObject4.put("rpl", this.F);
            jSONObject4.put("rpt", this.G);
            jSONObject4.put("rpct", this.H);
            jSONObject4.put("appPackageName", this.C);
            jSONObject4.put("acn", this.D);
            jSONObject4.put("intentUri", this.i);
            jSONObject4.put("emailAddr", this.z);
            jSONObject4.put("emailSubject", this.A);
            jSONObject4.put("emailContent", this.B);
            jSONObject4.put("phoneNum", this.y);
            jSONObject4.put("replyToSms", this.x);
            jSONObject4.put("smsNum", this.w);
            jSONObject3.put("cmd", this.r);
            jSONObject3.put("content", this.s);
            jSONObject3.put("notifyIcon", this.t);
            jSONObject3.put("notifyTitle", this.u);
            jSONObject3.put("statusIcon", this.v);
            jSONObject3.put("param", jSONObject4);
            jSONObject2.put("dispPkgName", this.p);
            jSONObject2.put("msgId", this.o);
            jSONObject2.put("fm", this.l);
            jSONObject2.put("ap", this.n);
            jSONObject2.put("rtn", this.m);
            jSONObject2.put("psContent", jSONObject3);
            if (this.q != null && this.q.length() > 0) {
                jSONObject2.put("extras", new JSONArray(this.q));
            }
            jSONObject.put("msgType", this.j);
            jSONObject.put("msgContent", jSONObject2);
            jSONObject.put("group", this.c);
            jSONObject.put("badgeClass", this.b);
            jSONObject.put("badgeAddNum", this.a);
            return jSONObject.toString().getBytes("UTF-8");
        } catch (Throwable e) {
            c.a("PushSelfShowLog", "getMsgData failed JSONException:", e);
            return new byte[0];
        } catch (Throwable e2) {
            c.a("PushSelfShowLog", "getMsgData failed UnsupportedEncodingException:", e2);
            return new byte[0];
        }
    }

    public void d(String str) {
        this.F = str;
    }

    public byte[] d() {
        try {
            if (this.k != null) {
                if (this.k.length() > 0) {
                    return this.k.getBytes("UTF-8");
                }
            }
        } catch (Throwable e) {
            c.a("PushSelfShowLog", "getToken getByte failed ", e);
        }
        return new byte[0];
    }

    public String e() {
        return this.c;
    }

    public void e(String str) {
        this.G = str;
    }

    public int f() {
        return this.h;
    }

    public void f(String str) {
        this.H = str;
    }

    public String g() {
        return this.i;
    }

    public void g(String str) {
        this.o = str;
    }

    public int h() {
        return this.l;
    }

    public int i() {
        return this.m;
    }

    public String j() {
        return this.n;
    }

    public String k() {
        return this.p;
    }

    public String l() {
        return this.q;
    }

    public String m() {
        return this.r;
    }

    public String n() {
        return this.s;
    }

    public String o() {
        return this.t;
    }

    public String p() {
        return this.u;
    }

    public String q() {
        return this.y;
    }

    public String r() {
        return this.z;
    }

    public String s() {
        return this.A;
    }

    public String t() {
        return this.B;
    }

    public String u() {
        return this.C;
    }

    public String v() {
        return this.D;
    }

    public String w() {
        return this.E;
    }

    public String x() {
        return this.F;
    }

    public String y() {
        return this.G;
    }

    public String z() {
        return this.H;
    }
}
