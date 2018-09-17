package com.huawei.android.pushselfshow.utils.b;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.utils.a;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

public class b {
    public Handler a;
    public Context b;
    public String c;
    public String d;
    public boolean e;
    private boolean f;
    private Runnable g;

    public b() {
        this.a = null;
        this.e = false;
        this.g = new c(this);
        this.f = false;
    }

    public b(Handler handler, Context context, String str, String str2) {
        this.a = null;
        this.e = false;
        this.g = new c(this);
        this.a = handler;
        this.b = context;
        this.c = str;
        this.d = str2;
        this.f = false;
    }

    public static String a(Context context) {
        return b(context) + File.separator + "richpush";
    }

    public static void a(HttpClient httpClient) {
        if (httpClient != null) {
            try {
                httpClient.getConnectionManager().shutdown();
            } catch (Throwable e) {
                c.d("PushSelfShowLog", "close input stream failed." + e.getMessage(), e);
            }
        }
    }

    public static void a(HttpRequestBase httpRequestBase) {
        if (httpRequestBase != null) {
            try {
                httpRequestBase.abort();
            } catch (Throwable e) {
                c.d("PushSelfShowLog", "close input stream failed." + e.getMessage(), e);
            }
        }
    }

    public static String b(Context context) {
        String str = "";
        try {
            str = Environment.getExternalStorageState().equals("mounted") ? a.l(context) : context.getFilesDir().getPath();
        } catch (Throwable e) {
            c.d("PushSelfShowLog", "getLocalFileHeader failed ", e);
        }
        str = str + File.separator + "PushService";
        c.a("PushSelfShowLog", "getFileHeader:" + str);
        return str;
    }

    public static String c(Context context) {
        String str = "";
        try {
            if (!Environment.getExternalStorageState().equals("mounted")) {
                return null;
            }
            str = a.l(context);
            return str + File.separator + "PushService";
        } catch (Throwable e) {
            c.d("PushSelfShowLog", "getLocalFileHeader failed ", e);
        }
    }

    public String a(Context context, String str, String str2) {
        try {
            String str3 = "" + System.currentTimeMillis();
            String str4 = str3 + str2;
            String str5 = a(context) + File.separator + str3;
            String str6 = str5 + File.separator + str4;
            File file = new File(str5);
            if (file.exists()) {
                a.a(file);
            } else {
                c.a("PushSelfShowLog", "dir is not exists()," + file.getAbsolutePath());
            }
            if (file.mkdirs()) {
                c.a("PushSelfShowLog", "dir.mkdirs() success  ");
            }
            c.a("PushSelfShowLog", "begin to download zip file, path is " + str6 + ",dir is " + file.getAbsolutePath());
            if (b(context, str, str6)) {
                return str6;
            }
            c.a("PushSelfShowLog", "download richpush file failed，clear temp file");
            if (file.exists()) {
                a.a(file);
            }
            return null;
        } catch (Exception e) {
            c.a("PushSelfShowLog", "download err" + e.toString());
        }
    }

    public void a() {
        this.f = true;
    }

    public void a(String str) {
        Message message = new Message();
        message.what = 1;
        message.obj = str;
        c.a("PushSelfShowLog", "mDownloadHandler = " + this.a);
        if (this.a != null) {
            this.a.sendMessageDelayed(message, 1);
        }
    }

    public void b() {
        new Thread(this.g).start();
    }

    /* JADX WARNING: Removed duplicated region for block: B:148:0x043e A:{SYNTHETIC, Splitter: B:148:0x043e} */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0466 A:{SYNTHETIC, Splitter: B:152:0x0466} */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x048e A:{SYNTHETIC, Splitter: B:156:0x048e} */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x03b7 A:{SYNTHETIC, Splitter: B:132:0x03b7} */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x03e1 A:{SYNTHETIC, Splitter: B:136:0x03e1} */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x028a A:{SYNTHETIC, Splitter: B:94:0x028a} */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x043e A:{SYNTHETIC, Splitter: B:148:0x043e} */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0466 A:{SYNTHETIC, Splitter: B:152:0x0466} */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x048e A:{SYNTHETIC, Splitter: B:156:0x048e} */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x03b7 A:{SYNTHETIC, Splitter: B:132:0x03b7} */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x03e1 A:{SYNTHETIC, Splitter: B:136:0x03e1} */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x028a A:{SYNTHETIC, Splitter: B:94:0x028a} */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x043e A:{SYNTHETIC, Splitter: B:148:0x043e} */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0466 A:{SYNTHETIC, Splitter: B:152:0x0466} */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x048e A:{SYNTHETIC, Splitter: B:156:0x048e} */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x043e A:{SYNTHETIC, Splitter: B:148:0x043e} */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0466 A:{SYNTHETIC, Splitter: B:152:0x0466} */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x048e A:{SYNTHETIC, Splitter: B:156:0x048e} */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x03b7 A:{SYNTHETIC, Splitter: B:132:0x03b7} */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x03e1 A:{SYNTHETIC, Splitter: B:136:0x03e1} */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x028a A:{SYNTHETIC, Splitter: B:94:0x028a} */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x03b7 A:{SYNTHETIC, Splitter: B:132:0x03b7} */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x03e1 A:{SYNTHETIC, Splitter: B:136:0x03e1} */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x028a A:{SYNTHETIC, Splitter: B:94:0x028a} */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x043e A:{SYNTHETIC, Splitter: B:148:0x043e} */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0466 A:{SYNTHETIC, Splitter: B:152:0x0466} */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x048e A:{SYNTHETIC, Splitter: B:156:0x048e} */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x03b7 A:{SYNTHETIC, Splitter: B:132:0x03b7} */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x03e1 A:{SYNTHETIC, Splitter: B:136:0x03e1} */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x028a A:{SYNTHETIC, Splitter: B:94:0x028a} */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x043e A:{SYNTHETIC, Splitter: B:148:0x043e} */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0466 A:{SYNTHETIC, Splitter: B:152:0x0466} */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x048e A:{SYNTHETIC, Splitter: B:156:0x048e} */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x03b7 A:{SYNTHETIC, Splitter: B:132:0x03b7} */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x03e1 A:{SYNTHETIC, Splitter: B:136:0x03e1} */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x028a A:{SYNTHETIC, Splitter: B:94:0x028a} */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x043e A:{SYNTHETIC, Splitter: B:148:0x043e} */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0466 A:{SYNTHETIC, Splitter: B:152:0x0466} */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x048e A:{SYNTHETIC, Splitter: B:156:0x048e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean b(Context context, String str, String str2) {
        Throwable e;
        Throwable th;
        BufferedOutputStream bufferedOutputStream = null;
        BufferedInputStream bufferedInputStream = null;
        HttpClient httpClient = null;
        OutputStream outputStream = null;
        HttpRequestBase httpRequestBase = null;
        try {
            HttpClient defaultHttpClient = new DefaultHttpClient();
            try {
                HttpRequestBase httpGet = new HttpGet(str);
                try {
                    try {
                        HttpResponse a = new d(context).a(str, defaultHttpClient, (HttpGet) httpGet);
                        if (a != null) {
                            if (a.getStatusLine() != null) {
                                if (a.getStatusLine().getStatusCode() != 200) {
                                    c.a("PushSelfShowLog", "fail, httprespone status is " + a.getStatusLine().getStatusCode());
                                    a(httpGet);
                                    a(defaultHttpClient);
                                    if (null != null) {
                                        try {
                                            bufferedOutputStream.close();
                                        } catch (Throwable e2) {
                                            c.d("PushSelfShowLog", " bos download  error" + e2.toString(), e2);
                                        }
                                    }
                                    if (null != null) {
                                        try {
                                            bufferedInputStream.close();
                                        } catch (Throwable e22) {
                                            c.d("PushSelfShowLog", " bis download  error" + e22.toString(), e22);
                                        }
                                    }
                                    if (null != null) {
                                        try {
                                            outputStream.close();
                                        } catch (Throwable e222) {
                                            c.d("PushSelfShowLog", "out download  error" + e222.toString(), e222);
                                        }
                                    }
                                    return false;
                                }
                            }
                            BufferedInputStream bufferedInputStream2 = new BufferedInputStream(a.getEntity().getContent());
                            try {
                                BufferedOutputStream bufferedOutputStream2;
                                c.a("PushSelfShowLog", "begin to write content to " + str2);
                                if (new File(str2).exists()) {
                                    c.a("PushSelfShowLog", "file delete " + new File(str2).delete());
                                }
                                OutputStream fileOutputStream = new FileOutputStream(str2);
                                try {
                                    try {
                                        bufferedOutputStream2 = new BufferedOutputStream(fileOutputStream);
                                    } catch (IOException e3) {
                                        e = e3;
                                        httpRequestBase = httpGet;
                                        outputStream = fileOutputStream;
                                        httpClient = defaultHttpClient;
                                        bufferedInputStream = bufferedInputStream2;
                                        try {
                                            c.d("PushSelfShowLog", "downLoadSgThread download  error" + e.toString(), e);
                                            a(httpRequestBase);
                                            a(httpClient);
                                            if (bufferedOutputStream != null) {
                                            }
                                            if (bufferedInputStream != null) {
                                            }
                                            if (outputStream != null) {
                                            }
                                            this.e = false;
                                            return false;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            a(httpRequestBase);
                                            a(httpClient);
                                            if (bufferedOutputStream != null) {
                                            }
                                            if (bufferedInputStream != null) {
                                            }
                                            if (outputStream != null) {
                                            }
                                            throw th;
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        httpRequestBase = httpGet;
                                        outputStream = fileOutputStream;
                                        httpClient = defaultHttpClient;
                                        bufferedInputStream = bufferedInputStream2;
                                        a(httpRequestBase);
                                        a(httpClient);
                                        if (bufferedOutputStream != null) {
                                        }
                                        if (bufferedInputStream != null) {
                                        }
                                        if (outputStream != null) {
                                        }
                                        throw th;
                                    }
                                } catch (IOException e4) {
                                    e = e4;
                                    httpRequestBase = httpGet;
                                    outputStream = fileOutputStream;
                                    httpClient = defaultHttpClient;
                                    bufferedInputStream = bufferedInputStream2;
                                    c.d("PushSelfShowLog", "downLoadSgThread download  error" + e.toString(), e);
                                    a(httpRequestBase);
                                    a(httpClient);
                                    if (bufferedOutputStream != null) {
                                    }
                                    if (bufferedInputStream != null) {
                                    }
                                    if (outputStream != null) {
                                    }
                                    this.e = false;
                                    return false;
                                } catch (Throwable th4) {
                                    th = th4;
                                    httpRequestBase = httpGet;
                                    outputStream = fileOutputStream;
                                    httpClient = defaultHttpClient;
                                    bufferedInputStream = bufferedInputStream2;
                                    a(httpRequestBase);
                                    a(httpClient);
                                    if (bufferedOutputStream != null) {
                                    }
                                    if (bufferedInputStream != null) {
                                    }
                                    if (outputStream != null) {
                                    }
                                    throw th;
                                }
                                try {
                                    byte[] bArr = new byte[32768];
                                    do {
                                        int read = bufferedInputStream2.read(bArr);
                                        if (read >= 0) {
                                            this.e = true;
                                            bufferedOutputStream2.write(bArr, 0, read);
                                        } else {
                                            c.a("PushSelfShowLog", "downLoad success ");
                                            this.e = false;
                                            a(httpGet);
                                            a(defaultHttpClient);
                                            if (bufferedOutputStream2 != null) {
                                                try {
                                                    bufferedOutputStream2.close();
                                                } catch (Throwable e5) {
                                                    c.d("PushSelfShowLog", " bos download  error" + e5.toString(), e5);
                                                }
                                            }
                                            if (bufferedInputStream2 != null) {
                                                try {
                                                    bufferedInputStream2.close();
                                                } catch (Throwable e52) {
                                                    c.d("PushSelfShowLog", " bis download  error" + e52.toString(), e52);
                                                }
                                            }
                                            if (fileOutputStream != null) {
                                                try {
                                                    fileOutputStream.close();
                                                } catch (Throwable e522) {
                                                    c.d("PushSelfShowLog", "out download  error" + e522.toString(), e522);
                                                }
                                                return true;
                                            }
                                            outputStream = fileOutputStream;
                                            return true;
                                        }
                                    } while (!this.f);
                                    a(httpGet);
                                    a(defaultHttpClient);
                                    if (bufferedOutputStream2 != null) {
                                        try {
                                            bufferedOutputStream2.close();
                                        } catch (Throwable e6) {
                                            c.d("PushSelfShowLog", " bos download  error" + e6.toString(), e6);
                                        }
                                    }
                                    if (bufferedInputStream2 != null) {
                                        try {
                                            bufferedInputStream2.close();
                                        } catch (Throwable e62) {
                                            c.d("PushSelfShowLog", " bis download  error" + e62.toString(), e62);
                                        }
                                    }
                                    if (fileOutputStream == null) {
                                        outputStream = fileOutputStream;
                                    } else {
                                        try {
                                            fileOutputStream.close();
                                        } catch (Throwable e622) {
                                            c.d("PushSelfShowLog", "out download  error" + e622.toString(), e622);
                                            httpRequestBase = httpGet;
                                            outputStream = fileOutputStream;
                                            httpClient = defaultHttpClient;
                                            bufferedInputStream = bufferedInputStream2;
                                            bufferedOutputStream = bufferedOutputStream2;
                                        }
                                    }
                                    httpRequestBase = httpGet;
                                    httpClient = defaultHttpClient;
                                    bufferedInputStream = bufferedInputStream2;
                                    bufferedOutputStream = bufferedOutputStream2;
                                } catch (IOException e7) {
                                    e622 = e7;
                                    httpRequestBase = httpGet;
                                    outputStream = fileOutputStream;
                                    httpClient = defaultHttpClient;
                                    bufferedInputStream = bufferedInputStream2;
                                    bufferedOutputStream = bufferedOutputStream2;
                                    c.d("PushSelfShowLog", "downLoadSgThread download  error" + e622.toString(), e622);
                                    a(httpRequestBase);
                                    a(httpClient);
                                    if (bufferedOutputStream != null) {
                                    }
                                    if (bufferedInputStream != null) {
                                    }
                                    if (outputStream != null) {
                                    }
                                    this.e = false;
                                    return false;
                                } catch (Throwable th5) {
                                    th = th5;
                                    httpRequestBase = httpGet;
                                    outputStream = fileOutputStream;
                                    httpClient = defaultHttpClient;
                                    bufferedInputStream = bufferedInputStream2;
                                    bufferedOutputStream = bufferedOutputStream2;
                                    a(httpRequestBase);
                                    a(httpClient);
                                    if (bufferedOutputStream != null) {
                                    }
                                    if (bufferedInputStream != null) {
                                    }
                                    if (outputStream != null) {
                                    }
                                    throw th;
                                }
                            } catch (IOException e8) {
                                e622 = e8;
                                httpRequestBase = httpGet;
                                httpClient = defaultHttpClient;
                                bufferedInputStream = bufferedInputStream2;
                            } catch (Throwable th6) {
                                th = th6;
                                httpRequestBase = httpGet;
                                httpClient = defaultHttpClient;
                                bufferedInputStream = bufferedInputStream2;
                                a(httpRequestBase);
                                a(httpClient);
                                if (bufferedOutputStream != null) {
                                }
                                if (bufferedInputStream != null) {
                                }
                                if (outputStream != null) {
                                }
                                throw th;
                            }
                            this.e = false;
                            return false;
                        }
                        c.a("PushSelfShowLog", "fail, httprespone  is null");
                        a(httpGet);
                        a(defaultHttpClient);
                        if (null != null) {
                            try {
                                bufferedOutputStream.close();
                            } catch (Throwable e2222) {
                                c.d("PushSelfShowLog", " bos download  error" + e2222.toString(), e2222);
                            }
                        }
                        if (null != null) {
                            try {
                                bufferedInputStream.close();
                            } catch (Throwable e22222) {
                                c.d("PushSelfShowLog", " bis download  error" + e22222.toString(), e22222);
                            }
                        }
                        if (null != null) {
                            try {
                                outputStream.close();
                            } catch (Throwable e222222) {
                                c.d("PushSelfShowLog", "out download  error" + e222222.toString(), e222222);
                            }
                        }
                        return false;
                    } catch (IOException e9) {
                        e622 = e9;
                        httpRequestBase = httpGet;
                        httpClient = defaultHttpClient;
                        c.d("PushSelfShowLog", "downLoadSgThread download  error" + e622.toString(), e622);
                        a(httpRequestBase);
                        a(httpClient);
                        if (bufferedOutputStream != null) {
                        }
                        if (bufferedInputStream != null) {
                        }
                        if (outputStream != null) {
                        }
                        this.e = false;
                        return false;
                    } catch (Throwable th7) {
                        th = th7;
                        httpRequestBase = httpGet;
                        httpClient = defaultHttpClient;
                        a(httpRequestBase);
                        a(httpClient);
                        if (bufferedOutputStream != null) {
                        }
                        if (bufferedInputStream != null) {
                        }
                        if (outputStream != null) {
                        }
                        throw th;
                    }
                } catch (IOException e10) {
                    e622 = e10;
                    httpRequestBase = httpGet;
                    httpClient = defaultHttpClient;
                    c.d("PushSelfShowLog", "downLoadSgThread download  error" + e622.toString(), e622);
                    a(httpRequestBase);
                    a(httpClient);
                    if (bufferedOutputStream != null) {
                        try {
                            bufferedOutputStream.close();
                        } catch (Throwable e6222) {
                            c.d("PushSelfShowLog", " bos download  error" + e6222.toString(), e6222);
                        }
                    }
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (Throwable e62222) {
                            c.d("PushSelfShowLog", " bis download  error" + e62222.toString(), e62222);
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (Throwable e622222) {
                            c.d("PushSelfShowLog", "out download  error" + e622222.toString(), e622222);
                        }
                    }
                    this.e = false;
                    return false;
                } catch (Throwable th8) {
                    th = th8;
                    httpRequestBase = httpGet;
                    httpClient = defaultHttpClient;
                    a(httpRequestBase);
                    a(httpClient);
                    if (bufferedOutputStream != null) {
                        try {
                            bufferedOutputStream.close();
                        } catch (Throwable e11) {
                            c.d("PushSelfShowLog", " bos download  error" + e11.toString(), e11);
                        }
                    }
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (Throwable e112) {
                            c.d("PushSelfShowLog", " bis download  error" + e112.toString(), e112);
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (Throwable e1122) {
                            c.d("PushSelfShowLog", "out download  error" + e1122.toString(), e1122);
                        }
                    }
                    throw th;
                }
            } catch (IOException e12) {
                e622222 = e12;
                httpClient = defaultHttpClient;
                c.d("PushSelfShowLog", "downLoadSgThread download  error" + e622222.toString(), e622222);
                a(httpRequestBase);
                a(httpClient);
                if (bufferedOutputStream != null) {
                }
                if (bufferedInputStream != null) {
                }
                if (outputStream != null) {
                }
                this.e = false;
                return false;
            } catch (Throwable th9) {
                th = th9;
                httpClient = defaultHttpClient;
                a(httpRequestBase);
                a(httpClient);
                if (bufferedOutputStream != null) {
                }
                if (bufferedInputStream != null) {
                }
                if (outputStream != null) {
                }
                throw th;
            }
        } catch (IOException e13) {
            e622222 = e13;
            c.d("PushSelfShowLog", "downLoadSgThread download  error" + e622222.toString(), e622222);
            a(httpRequestBase);
            a(httpClient);
            if (bufferedOutputStream != null) {
            }
            if (bufferedInputStream != null) {
            }
            if (outputStream != null) {
            }
            this.e = false;
            return false;
        }
    }

    public void c() {
        Message message = new Message();
        message.what = 2;
        c.a("PushSelfShowLog", "mDownloadHandler = " + this.a);
        if (this.a != null) {
            this.a.sendMessageDelayed(message, 1);
        }
    }
}
