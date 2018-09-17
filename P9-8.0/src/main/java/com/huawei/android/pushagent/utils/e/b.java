package com.huawei.android.pushagent.utils.e;

import android.content.Context;
import android.os.Build.VERSION;
import com.huawei.android.pushagent.constant.HttpMethod;
import com.huawei.android.pushagent.utils.a.d;
import com.huawei.android.pushagent.utils.d.c;
import com.huawei.secure.android.common.a;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.conn.ssl.SSLSocketFactory;

public class b {
    private Context appCtx;
    private String fs;
    private int ft = 30000;
    private int fu = 0;
    private List<String> fv;
    private int fw = 30000;
    private boolean fx = false;

    public b(Context context, String str, List<String> list) {
        this.appCtx = context.getApplicationContext();
        this.fs = str;
        this.fv = list;
    }

    public void st(int i) {
        this.fu = i;
    }

    public void ss(boolean z) {
        this.fx = z;
    }

    public void sq(int i) {
        this.ft = i;
    }

    public void sr(int i) {
        this.fw = i;
    }

    public String su(String str, HttpMethod httpMethod) {
        Closeable inputStream;
        Closeable closeable;
        Throwable th;
        if (str == null) {
            return null;
        }
        String sx = new d().sz(this.fs).ta(this.fv).tb(this.fu).tc().sx();
        c.sg("PushLog2951", "execute url is: " + d.nr(sx));
        int i = -1;
        HttpURLConnection sv;
        Closeable bufferedOutputStream;
        try {
            sv = sv(sx, httpMethod.vi());
            if (sv == null) {
                com.huawei.android.pushagent.utils.d.uw(null);
                com.huawei.android.pushagent.utils.d.uw(null);
                com.huawei.android.pushagent.utils.d.uw(null);
                com.huawei.android.pushagent.utils.d.ux(sv);
                c.sh("PushLog2951", "close connection");
                return null;
            }
            try {
                bufferedOutputStream = new BufferedOutputStream(sv.getOutputStream());
                try {
                    bufferedOutputStream.write(str.getBytes("UTF-8"));
                    bufferedOutputStream.flush();
                    i = sv.getResponseCode();
                    inputStream = sv.getInputStream();
                } catch (IOException e) {
                    closeable = null;
                    inputStream = null;
                } catch (RuntimeException e2) {
                    closeable = null;
                    inputStream = null;
                    c.sj("PushLog2951", "http execute encounter RuntimeException - http code:" + i);
                    com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                    com.huawei.android.pushagent.utils.d.uw(inputStream);
                    com.huawei.android.pushagent.utils.d.uw(closeable);
                    com.huawei.android.pushagent.utils.d.ux(sv);
                    c.sh("PushLog2951", "close connection");
                    return null;
                } catch (Exception e3) {
                    closeable = null;
                    inputStream = null;
                    try {
                        c.sj("PushLog2951", "http execute encounter unknown exception - http code:" + i);
                        com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                        com.huawei.android.pushagent.utils.d.uw(inputStream);
                        com.huawei.android.pushagent.utils.d.uw(closeable);
                        com.huawei.android.pushagent.utils.d.ux(sv);
                        c.sh("PushLog2951", "close connection");
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                        com.huawei.android.pushagent.utils.d.uw(inputStream);
                        com.huawei.android.pushagent.utils.d.uw(closeable);
                        com.huawei.android.pushagent.utils.d.ux(sv);
                        c.sh("PushLog2951", "close connection");
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    closeable = null;
                    inputStream = null;
                    com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                    com.huawei.android.pushagent.utils.d.uw(inputStream);
                    com.huawei.android.pushagent.utils.d.uw(closeable);
                    com.huawei.android.pushagent.utils.d.ux(sv);
                    c.sh("PushLog2951", "close connection");
                    throw th;
                }
            } catch (IOException e4) {
                closeable = null;
                inputStream = null;
                bufferedOutputStream = null;
                c.sj("PushLog2951", "http execute encounter IOException - http code:" + i);
                com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                com.huawei.android.pushagent.utils.d.uw(inputStream);
                com.huawei.android.pushagent.utils.d.uw(closeable);
                com.huawei.android.pushagent.utils.d.ux(sv);
                c.sh("PushLog2951", "close connection");
                return null;
            } catch (RuntimeException e5) {
                closeable = null;
                inputStream = null;
                bufferedOutputStream = null;
                c.sj("PushLog2951", "http execute encounter RuntimeException - http code:" + i);
                com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                com.huawei.android.pushagent.utils.d.uw(inputStream);
                com.huawei.android.pushagent.utils.d.uw(closeable);
                com.huawei.android.pushagent.utils.d.ux(sv);
                c.sh("PushLog2951", "close connection");
                return null;
            } catch (Exception e6) {
                closeable = null;
                inputStream = null;
                bufferedOutputStream = null;
                c.sj("PushLog2951", "http execute encounter unknown exception - http code:" + i);
                com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                com.huawei.android.pushagent.utils.d.uw(inputStream);
                com.huawei.android.pushagent.utils.d.uw(closeable);
                com.huawei.android.pushagent.utils.d.ux(sv);
                c.sh("PushLog2951", "close connection");
                return null;
            } catch (Throwable th4) {
                th = th4;
                closeable = null;
                inputStream = null;
                bufferedOutputStream = null;
                com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                com.huawei.android.pushagent.utils.d.uw(inputStream);
                com.huawei.android.pushagent.utils.d.uw(closeable);
                com.huawei.android.pushagent.utils.d.ux(sv);
                c.sh("PushLog2951", "close connection");
                throw th;
            }
            try {
                closeable = new BufferedInputStream(inputStream);
                try {
                    String uy = com.huawei.android.pushagent.utils.d.uy(closeable);
                    com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                    com.huawei.android.pushagent.utils.d.uw(inputStream);
                    com.huawei.android.pushagent.utils.d.uw(closeable);
                    com.huawei.android.pushagent.utils.d.ux(sv);
                    c.sh("PushLog2951", "close connection");
                    return uy;
                } catch (IOException e7) {
                    c.sj("PushLog2951", "http execute encounter IOException - http code:" + i);
                    com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                    com.huawei.android.pushagent.utils.d.uw(inputStream);
                    com.huawei.android.pushagent.utils.d.uw(closeable);
                    com.huawei.android.pushagent.utils.d.ux(sv);
                    c.sh("PushLog2951", "close connection");
                    return null;
                } catch (RuntimeException e8) {
                    c.sj("PushLog2951", "http execute encounter RuntimeException - http code:" + i);
                    com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                    com.huawei.android.pushagent.utils.d.uw(inputStream);
                    com.huawei.android.pushagent.utils.d.uw(closeable);
                    com.huawei.android.pushagent.utils.d.ux(sv);
                    c.sh("PushLog2951", "close connection");
                    return null;
                } catch (Exception e9) {
                    c.sj("PushLog2951", "http execute encounter unknown exception - http code:" + i);
                    com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                    com.huawei.android.pushagent.utils.d.uw(inputStream);
                    com.huawei.android.pushagent.utils.d.uw(closeable);
                    com.huawei.android.pushagent.utils.d.ux(sv);
                    c.sh("PushLog2951", "close connection");
                    return null;
                }
            } catch (IOException e10) {
                closeable = null;
                c.sj("PushLog2951", "http execute encounter IOException - http code:" + i);
                com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                com.huawei.android.pushagent.utils.d.uw(inputStream);
                com.huawei.android.pushagent.utils.d.uw(closeable);
                com.huawei.android.pushagent.utils.d.ux(sv);
                c.sh("PushLog2951", "close connection");
                return null;
            } catch (RuntimeException e11) {
                closeable = null;
                c.sj("PushLog2951", "http execute encounter RuntimeException - http code:" + i);
                com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                com.huawei.android.pushagent.utils.d.uw(inputStream);
                com.huawei.android.pushagent.utils.d.uw(closeable);
                com.huawei.android.pushagent.utils.d.ux(sv);
                c.sh("PushLog2951", "close connection");
                return null;
            } catch (Exception e12) {
                closeable = null;
                c.sj("PushLog2951", "http execute encounter unknown exception - http code:" + i);
                com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                com.huawei.android.pushagent.utils.d.uw(inputStream);
                com.huawei.android.pushagent.utils.d.uw(closeable);
                com.huawei.android.pushagent.utils.d.ux(sv);
                c.sh("PushLog2951", "close connection");
                return null;
            } catch (Throwable th5) {
                th = th5;
                closeable = null;
                com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
                com.huawei.android.pushagent.utils.d.uw(inputStream);
                com.huawei.android.pushagent.utils.d.uw(closeable);
                com.huawei.android.pushagent.utils.d.ux(sv);
                c.sh("PushLog2951", "close connection");
                throw th;
            }
        } catch (IOException e13) {
            sv = null;
            closeable = null;
            inputStream = null;
            bufferedOutputStream = null;
        } catch (RuntimeException e14) {
            sv = null;
            closeable = null;
            inputStream = null;
            bufferedOutputStream = null;
            c.sj("PushLog2951", "http execute encounter RuntimeException - http code:" + i);
            com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
            com.huawei.android.pushagent.utils.d.uw(inputStream);
            com.huawei.android.pushagent.utils.d.uw(closeable);
            com.huawei.android.pushagent.utils.d.ux(sv);
            c.sh("PushLog2951", "close connection");
            return null;
        } catch (Exception e15) {
            sv = null;
            closeable = null;
            inputStream = null;
            bufferedOutputStream = null;
            c.sj("PushLog2951", "http execute encounter unknown exception - http code:" + i);
            com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
            com.huawei.android.pushagent.utils.d.uw(inputStream);
            com.huawei.android.pushagent.utils.d.uw(closeable);
            com.huawei.android.pushagent.utils.d.ux(sv);
            c.sh("PushLog2951", "close connection");
            return null;
        } catch (Throwable th6) {
            th = th6;
            sv = null;
            closeable = null;
            inputStream = null;
            bufferedOutputStream = null;
            com.huawei.android.pushagent.utils.d.uw(bufferedOutputStream);
            com.huawei.android.pushagent.utils.d.uw(inputStream);
            com.huawei.android.pushagent.utils.d.uw(closeable);
            com.huawei.android.pushagent.utils.d.ux(sv);
            c.sh("PushLog2951", "close connection");
            throw th;
        }
    }

    private HttpsURLConnection sv(String str, String str2) {
        HttpsURLConnection httpsURLConnection;
        c.sh("PushLog2951", "createConnection");
        URL url = new URL(str);
        if (this.fx) {
            Proxy sw = sw();
            if (sw == null) {
                return null;
            }
            httpsURLConnection = (HttpsURLConnection) url.openConnection(sw);
        } else {
            httpsURLConnection = (HttpsURLConnection) url.openConnection();
        }
        httpsURLConnection.setSSLSocketFactory(new a(new a(com.huawei.android.pushagent.utils.c.uu(), "")));
        httpsURLConnection.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
        httpsURLConnection.setRequestMethod(str2);
        httpsURLConnection.setConnectTimeout(this.ft);
        httpsURLConnection.setReadTimeout(this.fw);
        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setRequestProperty("Content-type", "json/text; charset=UTF-8");
        return httpsURLConnection;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0028  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Proxy sw() {
        String property;
        int parseInt;
        Exception e;
        if (1 == com.huawei.android.pushagent.utils.b.tm(this.appCtx)) {
            return null;
        }
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
                }
                if (property != null || property.length() <= 0 || parseInt == -1) {
                    return null;
                }
                c.sg("PushLog2951", "use Proxy " + property + ":" + parseInt);
                return new Proxy(Type.HTTP, new InetSocketAddress(property, parseInt));
            }
            property = android.net.Proxy.getHost(this.appCtx);
            parseInt = android.net.Proxy.getPort(this.appCtx);
            if (property != null) {
            }
            return null;
        } catch (Exception e3) {
            e = e3;
            property = null;
        }
        c.sf("PushLog2951", "get proxy ip or port error:" + e.getMessage());
        parseInt = -1;
        if (property != null) {
        }
        return null;
    }
}
