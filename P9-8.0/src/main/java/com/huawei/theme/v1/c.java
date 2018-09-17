package com.huawei.theme.v1;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import com.huawei.theme.a.a.a;
import com.huawei.theme.a.a.b;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class c implements Runnable {
    boolean a;
    private Context b;
    private JSONObject c;

    public c(Context context, JSONObject jSONObject, boolean z) {
        this.b = context;
        this.c = jSONObject;
        this.a = z;
    }

    private String a(byte[] bArr) {
        String format = String.format("%016d", new Object[]{Long.valueOf(Math.abs(new SecureRandom().nextLong() % 10000000000000000L))});
        try {
            byte[] a = b.a(format, bArr);
            byte[] bytes = format.getBytes("UTF-8");
            RSAPublicKey rSAPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger("24907259431961377209480304447420314675278854956424737688244507998454379688588314890162679979323703303509240796245532111474023047392580178709435281576624542294613207523485034492914828565153172773053351891188090398210811384185501117117991603774176386409127476628856566065613009756131651597266262540467980974946876675842468600552312158771248419700603327630677244315755445967726919102965015263135288381740211593751262078285738436597133664401598420056690274760726854877181978220226448211936820860496708860964018593025172845041095854180953040116559241637133730839837036910305932797451786785855051024967644159284784940216337"), new BigInteger("65537")));
            if (rSAPublicKey == null) {
                throw new UnsupportedEncodingException();
            }
            Cipher instance = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-1ANDMGF1PADDING");
            instance.init(1, rSAPublicKey);
            byte[] doFinal = instance.doFinal(bytes);
            return "{\"vs\":\"" + a.e(this.b) + "\",\"ed\":\"" + a.b(a) + "\",\"ek\":\"" + a.b(doFinal) + "\"}";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean a(JSONObject jSONObject, String str) {
        String toLowerCase = str.toLowerCase();
        try {
            byte[] a = a.a(jSONObject.toString().getBytes("UTF-8"));
            if (a == null) {
                return false;
            }
            String a2 = a(a);
            if (a2 == null) {
                return false;
            }
            try {
                a = a2.getBytes("UTF-8");
                if (toLowerCase.indexOf("https") >= 0) {
                    return false;
                }
                a.h();
                return b.a(str, a);
            } catch (UnsupportedEncodingException e) {
                "UnsupportedEncodingException:" + e.getMessage();
                a.h();
                return false;
            }
        } catch (UnsupportedEncodingException e2) {
            "UnsupportedEncodingException:" + e2.getMessage();
            a.h();
            return false;
        }
    }

    public final void run() {
        try {
            if (this.c.getString("type") != null) {
                Object obj;
                Context context = this.b;
                JSONObject jSONObject = this.c;
                boolean z = this.a;
                StringBuffer stringBuffer = new StringBuffer("1.0");
                String a = a.a(context);
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                if (telephonyManager == null) {
                    a.h();
                    obj = null;
                } else {
                    Configuration configuration = context.getResources().getConfiguration();
                    String locale = (configuration == null || configuration.locale == null) ? "" : configuration.locale.toString();
                    String b = a.a(context, "android.permission.READ_PHONE_STATE") ? a.b(telephonyManager.getDeviceId()) : "";
                    String e = a.e(context);
                    if (a.f(context)) {
                        stringBuffer.append(",").append("Android" + VERSION.RELEASE).append(",").append(locale).append(",").append(Build.MODEL).append(",").append(Build.DISPLAY).append(",").append(e).append(",").append(b).append(",").append(a).append(",").append(a.b(context));
                        a.h();
                    } else {
                        stringBuffer.append(",,,,,").append(e).append(",").append(b).append(",").append(a).append(",");
                        a.h();
                    }
                    String obj2 = stringBuffer.toString();
                }
                if (obj2 == null) {
                    a.h();
                    return;
                }
                JSONObject b2 = com.huawei.theme.a.a.c.b(context, "cached");
                JSONObject jSONObject2 = new JSONObject();
                try {
                    String string = jSONObject.getString("type");
                    if (string != null) {
                        JSONArray jSONArray;
                        jSONObject.remove("type");
                        Object obj3 = 1;
                        if (b2 == null) {
                            b2 = new JSONObject();
                            jSONArray = new JSONArray();
                        } else if (b2.isNull(string)) {
                            jSONArray = new JSONArray();
                        } else {
                            obj3 = null;
                            jSONArray = b2.getJSONArray(string);
                        }
                        if (!z || obj3 == null) {
                            if (!z) {
                                jSONArray.put(jSONObject);
                            }
                            JSONArray jSONArray2 = new JSONArray();
                            int length = jSONArray.length();
                            for (int i = 0; i <= length - 1; i++) {
                                JSONObject jSONObject3 = jSONArray.getJSONObject(i);
                                JSONArray jSONArray3;
                                if (jSONObject3.has("b")) {
                                    jSONArray3 = jSONObject3.getJSONArray("b");
                                    if (jSONArray3 != null && jSONArray3.length() > 0) {
                                        String[] split = jSONArray3.getString(jSONArray3.length() - 1).split(",");
                                        if (((System.currentTimeMillis() / 1000) - a.a(split[1])) - Long.parseLong(split[2]) < a.b().longValue()) {
                                            jSONArray2.put(jSONObject3);
                                        } else {
                                            a.h();
                                        }
                                    }
                                } else if (jSONObject3.has("e")) {
                                    jSONArray3 = jSONObject3.getJSONArray("e");
                                    if (jSONArray3 != null && jSONArray3.length() > 0) {
                                        if ((System.currentTimeMillis() / 1000) - a.a(jSONArray3.getString(jSONArray3.length() - 1).split(",")[2]) < a.b().longValue()) {
                                            jSONArray2.put(jSONObject3);
                                        } else {
                                            a.h();
                                        }
                                    }
                                }
                            }
                            if (jSONArray2.length() <= 0) {
                                a.h();
                                return;
                            }
                            b2.remove(string);
                            b2.put(string, jSONArray2);
                            jSONObject2.put("g", obj2);
                            jSONObject2.put("s", jSONArray2);
                            "message=" + jSONObject2.toString();
                            a.h();
                            if (a(jSONObject2, a.i())) {
                                SharedPreferences a2 = com.huawei.theme.a.a.c.a(context, "flag");
                                if (a.f(context)) {
                                    Editor edit = a2.edit();
                                    edit.putString("rom_version", Build.DISPLAY);
                                    edit.commit();
                                }
                                com.huawei.theme.a.a.c.c(context, "cached");
                                a.h();
                                return;
                            }
                            com.huawei.theme.a.a.c.a(context, b2, "cached");
                            a.h();
                            return;
                        }
                        a.h();
                    }
                } catch (JSONException e2) {
                    e2.printStackTrace();
                    com.huawei.theme.a.a.c.c(context, "cached");
                }
            }
        } catch (Exception e3) {
            e3.getMessage();
            a.h();
            e3.printStackTrace();
            com.huawei.theme.a.a.c.c(this.b, "cached");
        }
    }
}
