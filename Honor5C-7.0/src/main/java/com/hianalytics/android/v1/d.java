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
import com.hianalytics.android.a.a.c;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class d implements Runnable {
    boolean a;
    private Context b;
    private JSONObject c;

    public d(Context context, JSONObject jSONObject, boolean z) {
        this.b = context;
        this.c = jSONObject;
        this.a = z;
    }

    private String a(byte[] bArr) {
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
            RSAPublicKey rSAPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDf5raDExuuXbsVNCWl48yuB89W\rfNOuuhPuS2Mptii/0UorpzypBkNTTGt11E7aorCc1lFwlB+4KDMIpFyQsdChSk+A\rt9UfhFKa95uiDpMe5rMfU+DAhoXGER6WQ2qGtrHmBWVv33i3lc76u9IgEfYuLwC6\r1mhQDHzAKPiViY6oeQIDAQAB\r", 0)));
            Cipher instance = Cipher.getInstance("RSA/ECB/PKCS1Padding");
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
                if (toLowerCase.indexOf("https") < 0) {
                    a.h();
                    return b.b(str, a);
                }
                a.h();
                boolean a3 = b.a(str, a);
                b.a();
                return a3;
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
                Object stringBuffer;
                Context context = this.b;
                JSONObject jSONObject = this.c;
                boolean z = this.a;
                StringBuffer stringBuffer2 = new StringBuffer("1.0");
                String a = a.a(context);
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                if (telephonyManager != null) {
                    Configuration configuration = context.getResources().getConfiguration();
                    String str = "";
                    if (!(configuration == null || configuration.locale == null)) {
                        str = configuration.locale.toString();
                    }
                    String str2 = str;
                    String b = !a.a(context, "android.permission.READ_PHONE_STATE") ? "" : a.b(telephonyManager.getDeviceId());
                    str = a.e(context);
                    if (a.f(context)) {
                        stringBuffer2.append(",").append("Android" + VERSION.RELEASE).append(",").append(str2).append(",").append(Build.MODEL).append(",").append(Build.DISPLAY).append(",").append(str).append(",").append(b).append(",").append(a).append(",").append(a.b(context));
                        a.h();
                    } else {
                        stringBuffer2.append(",,,,,").append(str).append(",").append(b).append(",").append(a).append(",");
                        a.h();
                    }
                    stringBuffer = stringBuffer2.toString();
                } else {
                    a.h();
                    stringBuffer = null;
                }
                if (stringBuffer != null) {
                    JSONObject b2 = c.b(context, "cached");
                    JSONObject jSONObject2 = new JSONObject();
                    try {
                        String string = jSONObject.getString("type");
                        if (string != null) {
                            JSONArray jSONArray;
                            jSONObject.remove("type");
                            Object obj = 1;
                            if (b2 == null) {
                                b2 = new JSONObject();
                                jSONArray = new JSONArray();
                            } else if (b2.isNull(string)) {
                                jSONArray = new JSONArray();
                            } else {
                                obj = null;
                                jSONArray = b2.getJSONArray(string);
                            }
                            JSONObject jSONObject3 = b2;
                            JSONArray jSONArray2 = jSONArray;
                            if (z && r2 != null) {
                                a.h();
                                return;
                            }
                            if (!z) {
                                jSONArray2.put(jSONObject);
                            }
                            JSONArray jSONArray3 = new JSONArray();
                            int length = jSONArray2.length();
                            for (int i = 0; i <= length - 1; i++) {
                                JSONObject jSONObject4 = jSONArray2.getJSONObject(i);
                                if (jSONObject4.has("b")) {
                                    jSONArray = jSONObject4.getJSONArray("b");
                                    if (jSONArray != null && jSONArray.length() > 0) {
                                        String[] split = jSONArray.getString(jSONArray.length() - 1).split(",");
                                        if ((((System.currentTimeMillis() / 1000) - a.a(split[1])) - Long.parseLong(split[2]) >= a.b().longValue() ? 1 : null) == null) {
                                            jSONArray3.put(jSONObject4);
                                        } else {
                                            a.h();
                                        }
                                    }
                                } else if (jSONObject4.has("e")) {
                                    jSONArray = jSONObject4.getJSONArray("e");
                                    if (jSONArray != null && jSONArray.length() > 0) {
                                        if (((System.currentTimeMillis() / 1000) - a.a(jSONArray.getString(jSONArray.length() + -1).split(",")[2]) >= a.b().longValue() ? 1 : null) == null) {
                                            jSONArray3.put(jSONObject4);
                                        } else {
                                            a.h();
                                        }
                                    }
                                }
                            }
                            if (jSONArray3.length() > 0) {
                                jSONObject3.remove(string);
                                jSONObject3.put(string, jSONArray3);
                                jSONObject2.put("g", stringBuffer);
                                jSONObject2.put("s", jSONArray3);
                                "message=" + jSONObject2.toString();
                                a.h();
                                if (a(jSONObject2, a.i())) {
                                    SharedPreferences a2 = c.a(context, "flag");
                                    if (a.f(context)) {
                                        Editor edit = a2.edit();
                                        edit.putString("rom_version", Build.DISPLAY);
                                        edit.commit();
                                    }
                                    c.c(context, "cached");
                                    a.h();
                                    return;
                                }
                                c.a(context, jSONObject3, "cached");
                                a.h();
                                return;
                            }
                            a.h();
                            return;
                        }
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        c.c(context, "cached");
                        return;
                    }
                }
                a.h();
            }
        } catch (Exception e2) {
            "MessageThread.run() throw exception:" + e2.getMessage();
            a.h();
            e2.printStackTrace();
            c.c(this.b, "cached");
        }
    }
}
