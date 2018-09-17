package com.hianalytics.android.v1;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.util.Base64;
import com.hianalytics.android.a.a.a;
import com.hianalytics.android.a.a.b;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
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
        Exception e;
        SecureRandom secureRandom = new SecureRandom();
        Object valueOf = String.valueOf(System.currentTimeMillis());
        int length = valueOf.length();
        if (length < 13) {
            StringBuffer stringBuffer = new StringBuffer(valueOf);
            for (int i = 0; i < 13 - length; i++) {
                stringBuffer.append("0");
            }
            valueOf = stringBuffer.toString();
        } else if (length > 13) {
            valueOf = valueOf.substring(0, 13);
        }
        String stringBuilder = new StringBuilder(String.valueOf(valueOf)).append(String.format("%03d", new Object[]{Integer.valueOf(secureRandom.nextInt(999))})).toString();
        try {
            byte[] a = b.a(stringBuilder, bArr);
            byte[] bytes = stringBuilder.getBytes("UTF-8");
            byte[] decode = Base64.decode("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDf5raDExuuXbsVNCWl48yuB89W\rfNOuuhPuS2Mptii/0UorpzypBkNTTGt11E7aorCc1lFwlB+4KDMIpFyQsdChSk+A\rt9UfhFKa95uiDpMe5rMfU+DAhoXGER6WQ2qGtrHmBWVv33i3lc76u9IgEfYuLwC6\r1mhQDHzAKPiViY6oeQIDAQAB\r", 0);
            KeyFactory instance = KeyFactory.getInstance("RSA");
            KeySpec x509EncodedKeySpec = new X509EncodedKeySpec(decode);
            try {
                RSAPublicKey rSAPublicKey = (RSAPublicKey) instance.generatePublic(x509EncodedKeySpec);
                Cipher instance2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                instance2.init(1, rSAPublicKey);
                bytes = instance2.doFinal(bytes);
                return "{\"vs\":\"" + a.e(this.b) + "\",\"ed\":\"" + a.b(a) + "\",\"ek\":\"" + a.b(bytes) + "\"}";
            } catch (Exception e2) {
                e = e2;
                KeySpec keySpec = x509EncodedKeySpec;
                e.printStackTrace();
                return null;
            }
        } catch (Exception e3) {
            e = e3;
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
        JSONException e;
        try {
            if (this.c.getString("type") != null) {
                String e2;
                Object stringBuffer;
                Context context = this.b;
                JSONObject jSONObject = this.c;
                boolean z = this.a;
                Context context2 = context;
                StringBuffer stringBuffer2 = new StringBuffer("1.0");
                String a = a.a(context2);
                TelephonyManager telephonyManager = (TelephonyManager) context2.getSystemService("phone");
                if (telephonyManager != null) {
                    Configuration configuration = context2.getResources().getConfiguration();
                    String str = "";
                    if (!(configuration == null || configuration.locale == null)) {
                        str = configuration.locale.toString();
                    }
                    String str2 = "";
                    if (a.a(context2, "android.permission.READ_PHONE_STATE")) {
                        str2 = a.b(telephonyManager.getDeviceId());
                    }
                    e2 = a.e(context2);
                    if (a.f(context2)) {
                        stringBuffer2.append(",").append("Android" + VERSION.RELEASE).append(",").append(str).append(",").append(Build.MODEL).append(",").append(Build.DISPLAY).append(",").append(e2).append(",").append(str2).append(",").append(a).append(",").append(a.b(context2));
                        a.h();
                    } else {
                        stringBuffer2.append(",,,,,").append(e2).append(",").append(str2).append(",").append(a).append(",");
                        a.h();
                    }
                    stringBuffer = stringBuffer2.toString();
                } else {
                    a.h();
                    stringBuffer = null;
                }
                if (stringBuffer != null) {
                    JSONObject b = com.hianalytics.android.a.a.c.b(context, "cached");
                    JSONObject jSONObject2 = new JSONObject();
                    try {
                        e2 = jSONObject.getString("type");
                        if (e2 != null) {
                            JSONArray jSONArray;
                            jSONObject.remove("type");
                            Object obj = 1;
                            if (b == null) {
                                JSONObject jSONObject3 = new JSONObject();
                                try {
                                    jSONArray = new JSONArray();
                                    b = jSONObject3;
                                } catch (JSONException e3) {
                                    e = e3;
                                    b = jSONObject3;
                                    e.printStackTrace();
                                    com.hianalytics.android.a.a.c.c(context, "cached");
                                    return;
                                }
                            } else if (b.isNull(e2)) {
                                jSONArray = new JSONArray();
                            } else {
                                obj = null;
                                jSONArray = b.getJSONArray(e2);
                            }
                            if (z && obj != null) {
                                a.h();
                                return;
                            }
                            if (!z) {
                                jSONArray.put(jSONObject);
                            }
                            JSONArray jSONArray2 = new JSONArray();
                            try {
                                int length = jSONArray.length();
                                for (int i = 0; i <= length - 1; i++) {
                                    JSONObject jSONObject4 = jSONArray.getJSONObject(i);
                                    JSONArray jSONArray3;
                                    if (jSONObject4.has("b")) {
                                        jSONArray3 = jSONObject4.getJSONArray("b");
                                        if (jSONArray3 != null && jSONArray3.length() > 0) {
                                            String[] split = jSONArray3.getString(jSONArray3.length() - 1).split(",");
                                            if ((((System.currentTimeMillis() / 1000) - a.a(split[1])) - Long.parseLong(split[2]) >= a.b().longValue() ? 1 : null) == null) {
                                                jSONArray2.put(jSONObject4);
                                            } else {
                                                a.h();
                                            }
                                        }
                                    } else if (jSONObject4.has("e")) {
                                        jSONArray3 = jSONObject4.getJSONArray("e");
                                        if (jSONArray3 != null && jSONArray3.length() > 0) {
                                            if (((System.currentTimeMillis() / 1000) - a.a(jSONArray3.getString(jSONArray3.length() + -1).split(",")[2]) >= a.b().longValue() ? 1 : null) == null) {
                                                jSONArray2.put(jSONObject4);
                                            } else {
                                                a.h();
                                            }
                                        }
                                    }
                                }
                                if (jSONArray2.length() > 0) {
                                    b.remove(e2);
                                    b.put(e2, jSONArray2);
                                    jSONObject2.put("g", stringBuffer);
                                    jSONObject2.put("s", jSONArray2);
                                    "message=" + jSONObject2.toString();
                                    a.h();
                                    if (a(jSONObject2, a.i())) {
                                        SharedPreferences a2 = com.hianalytics.android.a.a.c.a(context, "flag");
                                        if (a.f(context)) {
                                            Editor edit = a2.edit();
                                            edit.putString("rom_version", Build.DISPLAY);
                                            edit.commit();
                                        }
                                        com.hianalytics.android.a.a.c.c(context, "cached");
                                        a.h();
                                        return;
                                    }
                                    com.hianalytics.android.a.a.c.a(context, b, "cached");
                                    a.h();
                                    return;
                                }
                                a.h();
                                return;
                            } catch (JSONException e4) {
                                e = e4;
                                JSONArray jSONArray4 = jSONArray2;
                                e.printStackTrace();
                                com.hianalytics.android.a.a.c.c(context, "cached");
                                return;
                            }
                        }
                        return;
                    } catch (JSONException e5) {
                        e = e5;
                        e.printStackTrace();
                        com.hianalytics.android.a.a.c.c(context, "cached");
                        return;
                    }
                }
                a.h();
            }
        } catch (Exception e6) {
            "MessageThread.run() throw exception:" + e6.getMessage();
            a.h();
            e6.printStackTrace();
            com.hianalytics.android.a.a.c.c(this.b, "cached");
        }
    }
}
