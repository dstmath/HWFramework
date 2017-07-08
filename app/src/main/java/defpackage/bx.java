package defpackage;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.json.JSONException;
import org.json.JSONObject;

/* renamed from: bx */
public class bx {
    private static String C(String str) {
        return str == null ? "" : str;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static HttpsURLConnection a(Context context, String str, String str2, boolean z, boolean z2) {
        OutputStream outputStream;
        Throwable e;
        Throwable th;
        HttpsURLConnection httpsURLConnection;
        Throwable th2;
        if (z2) {
            str = bx.b(context, str2, z2);
        }
        if (str == null) {
            aw.d("PushLog2828", "TRSUrl is null, cannot create connection.");
            return null;
        }
        aw.d("PushLog2828", "TRS httpUrl = " + str + ",useDefaultPort = " + z2);
        try {
            HttpsURLConnection c = bx.c(context, str, z);
            try {
                aw.d("PushLog2828", "get connection success.");
                c.connect();
                try {
                    outputStream = c.getOutputStream();
                    try {
                        outputStream.write(bx.z(context, str2).getBytes("UTF-8"));
                        outputStream.flush();
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (Exception e2) {
                        e = e2;
                        try {
                            aw.d("PushLog2828", e.toString(), e);
                            if (outputStream != null) {
                                outputStream.close();
                            }
                            aw.d("PushLog2828", "call conn.connect() success");
                            return c;
                        } catch (Throwable th3) {
                            e = th3;
                            if (outputStream != null) {
                                outputStream.close();
                            }
                            throw e;
                        }
                    }
                } catch (Exception e3) {
                    e = e3;
                    outputStream = null;
                    aw.d("PushLog2828", e.toString(), e);
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    aw.d("PushLog2828", "call conn.connect() success");
                    return c;
                } catch (Throwable th4) {
                    e = th4;
                    outputStream = null;
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    throw e;
                }
                aw.d("PushLog2828", "call conn.connect() success");
                return c;
            } catch (Throwable e4) {
                th = e4;
                httpsURLConnection = c;
                th2 = th;
                aw.d("PushLog2828", "connect to TRS cause IOException:" + th2.toString(), th2);
                if (httpsURLConnection == null) {
                    return null;
                }
                httpsURLConnection.disconnect();
                return null;
            } catch (Throwable e42) {
                th = e42;
                httpsURLConnection = c;
                th2 = th;
                aw.d("PushLog2828", "connect to TRS cause Exception:" + th2.toString(), th2);
                if (httpsURLConnection != null) {
                    return null;
                }
                httpsURLConnection.disconnect();
                return null;
            }
        } catch (IOException e5) {
            th2 = e5;
            httpsURLConnection = null;
            aw.d("PushLog2828", "connect to TRS cause IOException:" + th2.toString(), th2);
            if (httpsURLConnection == null) {
                return null;
            }
            httpsURLConnection.disconnect();
            return null;
        } catch (Exception e6) {
            th2 = e6;
            httpsURLConnection = null;
            aw.d("PushLog2828", "connect to TRS cause Exception:" + th2.toString(), th2);
            if (httpsURLConnection != null) {
                return null;
            }
            httpsURLConnection.disconnect();
            return null;
        }
    }

    private static String b(Context context, String str, boolean z) {
        String a = ag.a(context, "cloudpush_trsIp", "push.hicloud.com");
        if (TextUtils.isEmpty(a)) {
            a = "push.hicloud.com";
        }
        a = bx.g(a, str);
        if (z && a != null && a.length() > 0) {
            a = a.split(":")[0] + ":5222";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("https://").append(a).append("/TRSServer/TRSRequest3");
        aw.d("PushLog2828", "url:" + stringBuffer.toString());
        return stringBuffer.toString();
    }

    private static HttpsURLConnection c(Context context, String str, boolean z) {
        String property;
        int parseInt;
        Exception e;
        HttpsURLConnection httpsURLConnection;
        SSLContext instance = SSLContext.getInstance("TLS");
        instance.init(null, new TrustManager[]{new bw(context)}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(new bs(instance.getSocketFactory()));
        HttpsURLConnection.setDefaultHostnameVerifier(new by());
        if (z && 1 != au.G(context)) {
            try {
                if (VERSION.SDK_INT >= 11) {
                    property = System.getProperty("http.proxyHost");
                    try {
                        String property2 = System.getProperty("http.proxyPort");
                        if (property2 == null) {
                            property2 = "-1";
                        }
                        parseInt = Integer.parseInt(property2);
                    } catch (Exception e2) {
                        e = e2;
                        aw.e("PushLog2828", "get proxy ip or port error:" + e.getMessage());
                        parseInt = -1;
                        aw.d("PushLog2828", "use Proxy " + property + ":" + parseInt + " to open:" + str);
                        httpsURLConnection = (HttpsURLConnection) new URL(str).openConnection(new Proxy(Type.HTTP, new InetSocketAddress(property, parseInt)));
                        if (httpsURLConnection == null) {
                            aw.d("PushLog2828", "direct to open " + str);
                            httpsURLConnection = (HttpsURLConnection) new URL(str).openConnection();
                        }
                        httpsURLConnection.setConnectTimeout(((int) ae.l(context).H()) * 1000);
                        httpsURLConnection.setReadTimeout((int) (ae.l(context).I() * 1000));
                        httpsURLConnection.setDoOutput(true);
                        httpsURLConnection.setDoInput(true);
                        httpsURLConnection.setRequestMethod("POST");
                        httpsURLConnection.setRequestProperty("Content-type", "json/text; charset=UTF-8");
                        return httpsURLConnection;
                    }
                    if (!(property == null || property.length() <= 0 || parseInt == -1)) {
                        aw.d("PushLog2828", "use Proxy " + property + ":" + parseInt + " to open:" + str);
                        httpsURLConnection = (HttpsURLConnection) new URL(str).openConnection(new Proxy(Type.HTTP, new InetSocketAddress(property, parseInt)));
                        if (httpsURLConnection == null) {
                            aw.d("PushLog2828", "direct to open " + str);
                            httpsURLConnection = (HttpsURLConnection) new URL(str).openConnection();
                        }
                        httpsURLConnection.setConnectTimeout(((int) ae.l(context).H()) * 1000);
                        httpsURLConnection.setReadTimeout((int) (ae.l(context).I() * 1000));
                        httpsURLConnection.setDoOutput(true);
                        httpsURLConnection.setDoInput(true);
                        httpsURLConnection.setRequestMethod("POST");
                        httpsURLConnection.setRequestProperty("Content-type", "json/text; charset=UTF-8");
                        return httpsURLConnection;
                    }
                }
                property = android.net.Proxy.getHost(context);
                parseInt = android.net.Proxy.getPort(context);
                aw.d("PushLog2828", "use Proxy " + property + ":" + parseInt + " to open:" + str);
                httpsURLConnection = (HttpsURLConnection) new URL(str).openConnection(new Proxy(Type.HTTP, new InetSocketAddress(property, parseInt)));
                if (httpsURLConnection == null) {
                    aw.d("PushLog2828", "direct to open " + str);
                    httpsURLConnection = (HttpsURLConnection) new URL(str).openConnection();
                }
                httpsURLConnection.setConnectTimeout(((int) ae.l(context).H()) * 1000);
                httpsURLConnection.setReadTimeout((int) (ae.l(context).I() * 1000));
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setRequestMethod("POST");
                httpsURLConnection.setRequestProperty("Content-type", "json/text; charset=UTF-8");
                return httpsURLConnection;
            } catch (Exception e3) {
                e = e3;
                property = null;
                aw.e("PushLog2828", "get proxy ip or port error:" + e.getMessage());
                parseInt = -1;
                aw.d("PushLog2828", "use Proxy " + property + ":" + parseInt + " to open:" + str);
                httpsURLConnection = (HttpsURLConnection) new URL(str).openConnection(new Proxy(Type.HTTP, new InetSocketAddress(property, parseInt)));
                if (httpsURLConnection == null) {
                    aw.d("PushLog2828", "direct to open " + str);
                    httpsURLConnection = (HttpsURLConnection) new URL(str).openConnection();
                }
                httpsURLConnection.setConnectTimeout(((int) ae.l(context).H()) * 1000);
                httpsURLConnection.setReadTimeout((int) (ae.l(context).I() * 1000));
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setRequestMethod("POST");
                httpsURLConnection.setRequestProperty("Content-type", "json/text; charset=UTF-8");
                return httpsURLConnection;
            }
        }
        httpsURLConnection = null;
        if (httpsURLConnection == null) {
            aw.d("PushLog2828", "direct to open " + str);
            httpsURLConnection = (HttpsURLConnection) new URL(str).openConnection();
        }
        httpsURLConnection.setConnectTimeout(((int) ae.l(context).H()) * 1000);
        httpsURLConnection.setReadTimeout((int) (ae.l(context).I() * 1000));
        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setRequestProperty("Content-type", "json/text; charset=UTF-8");
        return httpsURLConnection;
    }

    private static String g(String str, String str2) {
        if (TextUtils.isEmpty(str2) || TextUtils.isEmpty(str)) {
            aw.w("PushLog2828", "belongId is null or trsAddress is null");
        } else if (str.startsWith("push")) {
            try {
                int parseInt = Integer.parseInt(str2.trim());
                if (parseInt <= 0) {
                    aw.w("PushLog2828", "belongId is invalid:" + parseInt);
                } else {
                    int indexOf = str.indexOf(".");
                    if (indexOf > -1) {
                        str = new StringBuffer().append(str.substring(0, indexOf)).append(parseInt).append(str.substring(indexOf)).toString();
                    }
                }
            } catch (Throwable e) {
                aw.d("PushLog2828", "belongId parseInt error " + str2, e);
            } catch (Throwable e2) {
                aw.d("PushLog2828", e2.getMessage(), e2);
            }
        } else {
            aw.w("PushLog2828", "trsAddress is invalid:" + str);
        }
        return str;
    }

    public static k y(Context context, String str) {
        HttpsURLConnection httpsURLConnection;
        Throwable e;
        BufferedReader bufferedReader;
        Throwable th;
        String b = bx.b(context, str, false);
        aw.d("PushLog2828", "queryTrs runing");
        InputStream inputStream = null;
        BufferedReader bufferedReader2 = null;
        try {
            HttpsURLConnection a = bx.a(context, b, str, false, false);
            if (a != null) {
                httpsURLConnection = a;
            } else {
                try {
                    a = bx.a(context, b, str, true, false);
                    if (a != null) {
                        httpsURLConnection = a;
                    } else {
                        a = bx.a(context, b, str, false, true);
                        if (a != null) {
                            httpsURLConnection = a;
                        } else {
                            a = bx.a(context, b, str, true, true);
                            if (a != null) {
                                httpsURLConnection = a;
                            } else {
                                aw.e("PushLog2828", "after all, trs connect is null");
                                if (null != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e2) {
                                        aw.e("PushLog2828", "close is err");
                                    }
                                }
                                if (null != null) {
                                    try {
                                        bufferedReader2.close();
                                    } catch (IOException e3) {
                                        e3.printStackTrace();
                                    }
                                }
                                if (a == null) {
                                    return null;
                                }
                                aw.d("PushLog2828", "disconnect the socket");
                                a.disconnect();
                                return null;
                            }
                        }
                    }
                } catch (Exception e4) {
                    e = e4;
                    inputStream = null;
                    httpsURLConnection = a;
                    bufferedReader = null;
                    try {
                        aw.a("PushLog2828", "query trs err:" + e.toString(), e);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e5) {
                                aw.e("PushLog2828", "close is err");
                            }
                        }
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e32) {
                                e32.printStackTrace();
                            }
                        }
                        if (httpsURLConnection != null) {
                            aw.d("PushLog2828", "disconnect the socket");
                            httpsURLConnection.disconnect();
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (Throwable e6) {
                            aw.d("PushLog2828", e6.toString(), e6);
                        }
                        aw.d("PushLog2828", "get IP/PORT failed, retry again.");
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e7) {
                                aw.e("PushLog2828", "close is err");
                            }
                        }
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e322) {
                                e322.printStackTrace();
                            }
                        }
                        if (httpsURLConnection != null) {
                            aw.d("PushLog2828", "disconnect the socket");
                            httpsURLConnection.disconnect();
                        }
                        throw th;
                    }
                } catch (Throwable e62) {
                    inputStream = null;
                    httpsURLConnection = a;
                    bufferedReader = null;
                    th = e62;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (httpsURLConnection != null) {
                        aw.d("PushLog2828", "disconnect the socket");
                        httpsURLConnection.disconnect();
                    }
                    throw th;
                }
            }
            try {
                aw.d("PushLog2828", "queryTrs connect() success");
                inputStream = httpsURLConnection.getInputStream();
            } catch (Exception e8) {
                e62 = e8;
                bufferedReader = null;
                inputStream = null;
                aw.a("PushLog2828", "query trs err:" + e62.toString(), e62);
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (httpsURLConnection != null) {
                    aw.d("PushLog2828", "disconnect the socket");
                    httpsURLConnection.disconnect();
                }
                Thread.sleep(2000);
                aw.d("PushLog2828", "get IP/PORT failed, retry again.");
                return null;
            } catch (Throwable e622) {
                bufferedReader = null;
                inputStream = null;
                th = e622;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (httpsURLConnection != null) {
                    aw.d("PushLog2828", "disconnect the socket");
                    httpsURLConnection.disconnect();
                }
                throw th;
            }
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                try {
                    String readLine = bufferedReader.readLine();
                    if (readLine != null) {
                        k kVar = new k(context, readLine);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e9) {
                                aw.e("PushLog2828", "close is err");
                            }
                        }
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e10) {
                                e10.printStackTrace();
                            }
                        }
                        if (httpsURLConnection != null) {
                            aw.d("PushLog2828", "disconnect the socket");
                            httpsURLConnection.disconnect();
                        }
                        return kVar;
                    }
                    aw.d("PushLog2828", "response is null");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e11) {
                            aw.e("PushLog2828", "close is err");
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e3222) {
                            e3222.printStackTrace();
                        }
                    }
                    if (httpsURLConnection != null) {
                        aw.d("PushLog2828", "disconnect the socket");
                        httpsURLConnection.disconnect();
                    }
                    Thread.sleep(2000);
                    aw.d("PushLog2828", "get IP/PORT failed, retry again.");
                    return null;
                } catch (Exception e12) {
                    e622 = e12;
                    aw.a("PushLog2828", "query trs err:" + e622.toString(), e622);
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (httpsURLConnection != null) {
                        aw.d("PushLog2828", "disconnect the socket");
                        httpsURLConnection.disconnect();
                    }
                    Thread.sleep(2000);
                    aw.d("PushLog2828", "get IP/PORT failed, retry again.");
                    return null;
                }
            } catch (Exception e13) {
                e622 = e13;
                bufferedReader = null;
                aw.a("PushLog2828", "query trs err:" + e622.toString(), e622);
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (httpsURLConnection != null) {
                    aw.d("PushLog2828", "disconnect the socket");
                    httpsURLConnection.disconnect();
                }
                Thread.sleep(2000);
                aw.d("PushLog2828", "get IP/PORT failed, retry again.");
                return null;
            } catch (Throwable e6222) {
                bufferedReader = null;
                th = e6222;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (httpsURLConnection != null) {
                    aw.d("PushLog2828", "disconnect the socket");
                    httpsURLConnection.disconnect();
                }
                throw th;
            }
        } catch (Exception e14) {
            e6222 = e14;
            bufferedReader = null;
            inputStream = null;
            httpsURLConnection = null;
            aw.a("PushLog2828", "query trs err:" + e6222.toString(), e6222);
            if (inputStream != null) {
                inputStream.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (httpsURLConnection != null) {
                aw.d("PushLog2828", "disconnect the socket");
                httpsURLConnection.disconnect();
            }
            Thread.sleep(2000);
            aw.d("PushLog2828", "get IP/PORT failed, retry again.");
            return null;
        } catch (Throwable e62222) {
            bufferedReader = null;
            inputStream = null;
            httpsURLConnection = null;
            th = e62222;
            if (inputStream != null) {
                inputStream.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (httpsURLConnection != null) {
                aw.d("PushLog2828", "disconnect the socket");
                httpsURLConnection.disconnect();
            }
            throw th;
        }
    }

    private static String z(Context context, String str) {
        String F = au.F(context);
        String E = au.E(context);
        String E2 = au.E(context);
        String str2 = "";
        String version = au.getVersion(context);
        String bJ = au.bJ();
        String packageName = context.getPackageName();
        long x = bu.x(context, "com.huawei.android.pushagent");
        if (x < 0) {
            x = -1;
        }
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("mccmnc", bx.C(F));
            jSONObject.put("PushID", bx.C(E));
            jSONObject.put("udid", bx.C(E2));
            jSONObject.put("belongid", bx.C(str));
            jSONObject.put("version", bx.C(version));
            jSONObject.put("protocolversion", "2.0");
            jSONObject.put("info", 0);
            jSONObject.put("channel", bx.C(packageName));
            jSONObject.put("mode", bx.C(Build.MODEL));
            jSONObject.put("mac", bx.C(str2));
            jSONObject.put("intelligent", 3);
            jSONObject.put("emV", bx.C(bJ));
            jSONObject.put("rV", bx.C(Build.DISPLAY));
            jSONObject.put("agentV", x);
        } catch (JSONException e) {
            aw.e("PushLog2828", e.toString());
        }
        aw.d("PushLog2828", "enter the buildReqTRSContent and the  content info is  = " + bi.b(jSONObject));
        return jSONObject.toString();
    }
}
