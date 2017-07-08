package com.hianalytics.android.v1;

import com.hianalytics.android.a.a.a;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

/* compiled from: Unknown */
public final class b {
    private static final AllowAllHostnameVerifier a = null;
    private static X509TrustManager b;
    private static X509TrustManager[] c;
    private static HttpsURLConnection d;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.hianalytics.android.v1.b.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.hianalytics.android.v1.b.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.hianalytics.android.v1.b.<clinit>():void");
    }

    public static void a() {
        if (d != null) {
            a.h();
            d.disconnect();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean a(String str, byte[] bArr) {
        Throwable th;
        OutputStream outputStream = null;
        try {
            d = (HttpsURLConnection) new URL(str).openConnection();
            SSLContext instance = SSLContext.getInstance("TLS");
            instance.init(new KeyManager[0], c, new SecureRandom());
            d.setSSLSocketFactory(instance.getSocketFactory());
            d.setHostnameVerifier(a);
            d.setConnectTimeout(5000);
            d.setRequestMethod("POST");
            d.setDoOutput(true);
            d.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            d.setRequestProperty("Content-Length", String.valueOf(bArr.length));
            outputStream = d.getOutputStream();
            outputStream.write(bArr);
            outputStream.flush();
            int responseCode = d.getResponseCode();
            "conn.getResponseCode() = " + responseCode;
            a.h();
            boolean z = responseCode == SmsCheckResult.ESCT_200;
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return z;
        } catch (Exception e2) {
            Exception e3 = e2;
            OutputStream outputStream2 = outputStream;
            try {
                "conn error:" + e3.getMessage();
                a.h();
                e3.printStackTrace();
                if (outputStream2 != null) {
                    try {
                        outputStream2.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                return false;
            } catch (Throwable th2) {
                th = th2;
                outputStream = outputStream2;
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (outputStream != null) {
                outputStream.close();
            }
            throw th;
        }
    }

    public static boolean b(String str, byte[] bArr) {
        HttpURLConnection httpURLConnection;
        IOException iOException;
        Throwable th;
        boolean z = false;
        OutputStream outputStream = null;
        try {
            HttpURLConnection httpURLConnection2 = (HttpURLConnection) new URL(str).openConnection();
            try {
                a.h();
                httpURLConnection2.setRequestMethod("POST");
                httpURLConnection2.setConnectTimeout(5000);
                httpURLConnection2.setDoOutput(true);
                httpURLConnection2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                httpURLConnection2.setRequestProperty("Content-Length", String.valueOf(bArr.length));
                outputStream = httpURLConnection2.getOutputStream();
                outputStream.write(bArr);
                outputStream.flush();
                int responseCode = httpURLConnection2.getResponseCode();
                "connHttp.getResponseCode() = " + responseCode;
                a.h();
                if (responseCode == SmsCheckResult.ESCT_200) {
                    z = true;
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (httpURLConnection2 != null) {
                    httpURLConnection2.disconnect();
                    a.h();
                }
                return z;
            } catch (IOException e2) {
                IOException iOException2 = e2;
                httpURLConnection = httpURLConnection2;
                iOException = iOException2;
                try {
                    "connHttp error:" + iOException.getMessage();
                    a.h();
                    iOException.printStackTrace();
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException iOException3) {
                            iOException3.printStackTrace();
                        }
                    }
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                        a.h();
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                        a.h();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                httpURLConnection = httpURLConnection2;
                th = th3;
                if (outputStream != null) {
                    outputStream.close();
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                    a.h();
                }
                throw th;
            }
        } catch (IOException e4) {
            iOException3 = e4;
            httpURLConnection = null;
            "connHttp error:" + iOException3.getMessage();
            a.h();
            iOException3.printStackTrace();
            if (outputStream != null) {
                outputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
                a.h();
            }
            return false;
        } catch (Throwable th4) {
            th = th4;
            httpURLConnection = null;
            if (outputStream != null) {
                outputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
                a.h();
            }
            throw th;
        }
    }
}
