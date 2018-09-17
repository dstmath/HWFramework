package tmsdkobf;

import android.content.Context;
import android.os.Build.VERSION;
import android.text.TextUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.exception.NetWorkException;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.i;
import tmsdk.common.utils.n;
import tmsdkobf.nw.f;

public class nf {
    private static String TAG = "HttpNetwork";
    private final int CP = 3;
    private final int CQ = 3;
    private String CR = "POST";
    private HttpURLConnection CS;
    private nl CT;
    private om CU;
    private String CV;
    private int CW = 0;
    private boolean CX = false;

    public interface a {
        void b(int i, byte[] bArr);
    }

    public nf(Context context, nl nlVar, om omVar, boolean z) {
        this.CT = nlVar;
        this.CU = omVar;
        this.CX = z;
    }

    private int a(byte[] bArr, AtomicReference<byte[]> atomicReference) {
        mb.n(TAG, "[http_control]doSend()");
        if (this.CS == null) {
            return -10000;
        }
        try {
            if (!"GET".equalsIgnoreCase(this.CR)) {
                this.CS.setRequestProperty("Content-length", "" + bArr.length);
            }
            try {
                if (VERSION.SDK != null) {
                    if (VERSION.SDK_INT > 13) {
                        this.CS.setRequestProperty("Connection", "close");
                    }
                }
            } catch (Exception e) {
            }
            mb.n(TAG, "[http_control]doSend(), bf [http send] bytes: " + bArr.length);
            OutputStream outputStream = this.CS.getOutputStream();
            outputStream.write(bArr);
            outputStream.flush();
            outputStream.close();
            mb.d(TAG, "[flow_control][http_control]doSend(), [http send] bytes: " + bArr.length);
            int responseCode = this.CS.getResponseCode();
            if (bl(responseCode)) {
                this.CV = eM();
                this.CW++;
                mb.d(TAG, "[http_control]doSend()，需重定向, mRedirectUrl: " + this.CV + " mRedirectTimes: " + this.CW);
                return -60000;
            }
            fw();
            mb.n(TAG, "[http_control]doSend(), resposeCode: " + responseCode);
            try {
                if (mb.isEnable()) {
                    mb.n(TAG, "[http_control]doSend(), HeaderFields: " + this.CS.getHeaderFields());
                }
                String headerField = this.CS.getHeaderField("Server");
                if (TextUtils.isEmpty(headerField)) {
                    mb.o(TAG, "[http_control]doSend(), getHeaderField('BACK_KEY') should be 'QBServer', actually return: " + headerField);
                    return -170000;
                } else if (headerField.equals("QBServer")) {
                    Object d = d(this.CS.getInputStream());
                    atomicReference.set(d);
                    if (d != null) {
                        mb.d(TAG, "[flow_control][http_control]doSend(), [http receive] bytes: " + d.length);
                    }
                    return 0;
                } else {
                    mb.o(TAG, "[http_control]doSend(), getHeaderField('BACK_KEY') should be 'QBServer', actually return: " + headerField);
                    return -560000;
                }
            } catch (Exception e2) {
                mb.e(TAG, e2);
                return -40000;
            }
        } catch (Throwable e3) {
            mb.c(TAG, "doSend(), UnknownHostException: ", e3);
            return -70000;
        } catch (Throwable e32) {
            mb.c(TAG, "doSend(), IllegalAccessError: ", e32);
            return -80000;
        } catch (Throwable e322) {
            mb.c(TAG, "doSend(), IllegalStateException: ", e322);
            return -90000;
        } catch (Throwable e3222) {
            mb.c(TAG, "doSend(), ProtocolException: ", e3222);
            return -100000;
        } catch (Throwable e32222) {
            mb.c(TAG, "doSend(), ClientProtocolException: ", e32222);
            return -110000;
        } catch (Throwable e322222) {
            mb.c(TAG, "doSend(), ConnectException: ", e322222);
            return ne.f(e322222.toString(), -500000);
        } catch (Throwable e3222222) {
            mb.c(TAG, "doSend(), SocketException: ", e3222222);
            return ne.f(e3222222.toString(), -420000);
        } catch (Throwable e32222222) {
            mb.c(TAG, "doSend(), SecurityException: ", e32222222);
            return ne.f(e32222222.toString(), -440000);
        } catch (Throwable e322222222) {
            mb.c(TAG, "doSend(), SocketTimeoutException: ", e322222222);
            return -130000;
        } catch (Throwable e3222222222) {
            mb.c(TAG, "doSend(), IOException: ", e3222222222);
            return -140000;
        } catch (Throwable e32222222222) {
            mb.c(TAG, "doSend(), Exception: ", e32222222222);
            return -150000;
        }
    }

    private boolean bl(int i) {
        return i >= SmsCheckResult.ESCT_301 && i <= SmsCheckResult.ESCT_305;
    }

    private int cb(String -l_3_R) {
        mb.n(TAG, "[http_control]start()");
        if (this.CW >= 3) {
            fw();
        }
        if (!TextUtils.isEmpty(this.CV)) {
            -l_3_R = this.CV;
        }
        try {
            URL url = new URL(-l_3_R);
            int i;
            try {
                eb iG = i.iG();
                if (eb.iH != iG) {
                    if (eb.iK != iG) {
                        this.CS = (HttpURLConnection) url.openConnection();
                        this.CS.setReadTimeout(15000);
                        this.CS.setConnectTimeout(15000);
                    } else {
                        this.CS = (HttpURLConnection) url.openConnection(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(i.iI(), i.iJ())));
                    }
                    if (n.iX() < 8) {
                        System.setProperty("http.keepAlive", "false");
                    }
                    this.CS.setUseCaches(false);
                    this.CS.setRequestProperty("Pragma", "no-cache");
                    this.CS.setRequestProperty("Cache-Control", "no-cache");
                    this.CS.setInstanceFollowRedirects(false);
                    if ("GET".equalsIgnoreCase(this.CR)) {
                        this.CS.setRequestMethod("GET");
                    } else {
                        this.CS.setRequestMethod("POST");
                        this.CS.setDoOutput(true);
                        this.CS.setDoInput(true);
                        this.CS.setRequestProperty("Accept", "*/*");
                        this.CS.setRequestProperty("Accept-Charset", "utf-8");
                        this.CS.setRequestProperty("Content-Type", "application/octet-stream");
                    }
                    i = 0;
                    return i;
                }
                mb.s(TAG, "[http_control]start() no network");
                return -220000;
            } catch (UnknownHostException e) {
                e.printStackTrace();
                i = -520000;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                i = -240000;
            } catch (SecurityException e3) {
                e3.printStackTrace();
                i = -440000;
            } catch (IOException e4) {
                e4.printStackTrace();
                i = -140000;
            }
        } catch (MalformedURLException e5) {
            e5.printStackTrace();
            mb.o(TAG, "[http_control]start() MalformedURLException e:" + e5.toString());
            return -510000;
        }
    }

    private byte[] d(InputStream inputStream) throws NetWorkException {
        byte[] bArr = new byte[2048];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            try {
                int read = inputStream.read(bArr);
                if (read == -1) {
                    byte[] toByteArray = byteArrayOutputStream.toByteArray();
                    try {
                        byteArrayOutputStream.close();
                    } catch (Exception e) {
                        mb.e(TAG, e);
                    }
                    return toByteArray;
                }
                byteArrayOutputStream.write(bArr, 0, read);
            } catch (IOException e2) {
                throw new NetWorkException(-56, "get Bytes from inputStream when read buffer: " + e2.getMessage());
            } catch (Throwable th) {
                try {
                    byteArrayOutputStream.close();
                } catch (Exception e3) {
                    mb.e(TAG, e3);
                }
            }
        }
    }

    private String eM() {
        mb.d(TAG, "[http_control]getRedirectUrl()");
        try {
            return this.CS.getHeaderField("Location");
        } catch (Exception e) {
            mb.o(TAG, "getRedirectUrl() e: " + e.toString());
            return null;
        }
    }

    private boolean fv() {
        mb.n(TAG, "[http_control]stop()");
        if (this.CS == null) {
            return false;
        }
        try {
            this.CS.disconnect();
            this.CS = null;
        } catch (Throwable th) {
        }
        return true;
    }

    private void fw() {
        this.CV = null;
        this.CW = 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0040 A:{Catch:{ InterruptedException -> 0x014f }} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0042 A:{Catch:{ InterruptedException -> 0x014f }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized int a(f fVar, byte[] bArr, AtomicReference<byte[]> atomicReference) {
        if (bArr == null || fVar == null) {
            return -10;
        }
        Object obj;
        int i;
        int i2;
        int i3;
        mb.n(TAG, "[http_control]sendData()");
        if (fVar.Fh == 2048) {
            if (!this.CX) {
                obj = 1;
                i = 3;
                if (obj != null) {
                    i = 1;
                }
                i2 = -1;
                i3 = 0;
                while (i3 < i) {
                    if (eb.iH == i.iG()) {
                        mb.s(TAG, "[http_control]sendData() no network");
                        return -220000;
                    } else if (fVar.gp()) {
                        mb.d(TAG, "[http_control][time_out]sendData(), send time out");
                        return -17;
                    } else if (!lw.eJ()) {
                        String fJ;
                        if (obj == null) {
                            fJ = this.CU.fJ();
                        } else {
                            fJ = nj.a(this.CT);
                            if (fJ != null && (fJ.length() < "http://".length() || !fJ.substring(0, "http://".length()).equalsIgnoreCase("http://"))) {
                                fJ = "http://" + fJ;
                            }
                        }
                        i2 = cb(fJ);
                        mb.n(TAG, "[http_control]start(), ret: " + i2 + " httpUrl: " + fJ);
                        if (i2 == 0) {
                            fVar.Fw = true;
                            i2 = a(bArr, atomicReference);
                        }
                        fv();
                        if (i2 == 0 || i2 == -220000) {
                            mb.n(TAG, "[http_control]sendData() 发包成功或无网络，不重试， ret: " + i2);
                            break;
                        } else if (i2 != -60000 && nu.ch("http send")) {
                            i2 = -160000;
                            mb.n(TAG, "[http_control]sendData() 需要wifi认证，不重试");
                            break;
                        } else {
                            if (obj == null && i2 != -60000) {
                                this.CU.C(false);
                            }
                            if (i3 < i - 1) {
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    mb.o(TAG, "[http_control]sendData() InterruptedException e: " + e.toString());
                                }
                            }
                            i3++;
                        }
                    } else {
                        return -7;
                    }
                }
                mb.d(TAG, "[http_control]sendData() ret: " + i2);
                return i2;
            }
        }
        obj = null;
        i = 3;
        if (obj != null) {
        }
        i2 = -1;
        i3 = 0;
        while (i3 < i) {
        }
        mb.d(TAG, "[http_control]sendData() ret: " + i2);
        return i2;
    }
}
