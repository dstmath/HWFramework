package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.http.client.ClientProtocolException;
import tmsdk.common.exception.NetWorkException;
import tmsdk.common.exception.WifiApproveException;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;
import tmsdk.common.utils.j;
import tmsdk.common.utils.p;
import tmsdk.common.utils.p.a;

/* compiled from: Unknown */
public class oj {
    private static String TAG;
    private final int Em;
    private final int En;
    private String Eo;
    private boolean Ep;
    private HttpURLConnection Eq;
    private os Er;
    private String Es;
    private on Et;
    private volatile String Eu;
    private int mRedirectCount;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.oj.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.oj.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.oj.<clinit>():void");
    }

    public oj(Context context, os osVar, on onVar) {
        this.Em = 5;
        this.En = 3;
        this.Eo = "POST";
        this.Ep = false;
        this.mRedirectCount = 0;
        this.Eu = null;
        this.Et = onVar;
        this.Er = osVar;
    }

    private int b(byte[] bArr, AtomicReference<byte[]> atomicReference) {
        if (this.Eq == null) {
            return -10000;
        }
        if (!"GET".equalsIgnoreCase(this.Eo)) {
            this.Eq.setRequestProperty("Content-length", "" + bArr.length);
        }
        try {
            long currentTimeMillis = System.currentTimeMillis();
            OutputStream outputStream = this.Eq.getOutputStream();
            currentTimeMillis = System.currentTimeMillis() - currentTimeMillis;
            d.d(TAG, "doSend() connectTimeMillis: " + currentTimeMillis);
            this.Et.i(currentTimeMillis);
            outputStream.write(bArr);
            outputStream.flush();
            outputStream.close();
            if (ca(this.Eq.getResponseCode())) {
                this.Es = fe();
                this.mRedirectCount++;
                d.d(TAG, "doSend() \u91cd\u5b9a\u5411\u4e86, mRedirectUrl: " + this.Es + " mRedirectCount: " + this.mRedirectCount);
                return -60000;
            }
            fJ();
            fG();
            try {
                Map headerFields = this.Eq.getHeaderFields();
                if (headerFields != null) {
                    d.e(TAG, "doSend() HeaderFields: " + headerFields.toString());
                }
                String headerField = this.Eq.getHeaderField("Server");
                if (TextUtils.isEmpty(headerField)) {
                    d.c(TAG, "doSend() not our server");
                    return -170000;
                }
                d.d(TAG, "doSend() server: " + headerField);
                if (headerField.equals("QBServer")) {
                    atomicReference.set(d(this.Eq.getInputStream()));
                    return 0;
                }
                d.c(TAG, "doSend() not our server equal");
                return -170000;
            } catch (Exception e) {
                d.c(TAG, e);
                return -40000;
            }
        } catch (UnknownHostException e2) {
            d.c(TAG, e2);
            return -70000;
        } catch (IllegalAccessError e3) {
            d.c(TAG, e3);
            return -80000;
        } catch (IllegalStateException e4) {
            d.c(TAG, e4);
            return -90000;
        } catch (ProtocolException e5) {
            d.c(TAG, e5);
            return -100000;
        } catch (ClientProtocolException e6) {
            d.c(TAG, e6);
            return -110000;
        } catch (SocketException e7) {
            if (fF()) {
                return -160000;
            }
            d.c(TAG, e7);
            return -120000;
        } catch (SocketTimeoutException e8) {
            d.c(TAG, e8);
            return -130000;
        } catch (IOException e9) {
            d.c(TAG, e9);
            return -140000;
        } catch (Exception e10) {
            d.c(TAG, e10);
            return -150000;
        }
    }

    private void bZ(int i) {
        if (-70000 == i || -130000 == i || -170000 == i) {
            this.Er.gd();
        }
    }

    private boolean ca(int i) {
        return i >= SmsCheckResult.ESCT_301 && i <= SmsCheckResult.ESCT_305;
    }

    private byte[] d(InputStream inputStream) throws NetWorkException {
        byte[] bArr = new byte[2048];
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            int read = inputStream.read(bArr);
            if (read == -1) {
                break;
            }
            try {
                byteArrayOutputStream.write(bArr, 0, read);
            } catch (IOException e) {
                throw new NetWorkException(-56, "get Bytes from inputStream when read buffer: " + e.getMessage());
            } catch (Throwable th) {
                try {
                    bufferedInputStream.close();
                    byteArrayOutputStream.close();
                } catch (Exception e2) {
                    d.c(TAG, e2);
                }
            }
        }
        byte[] toByteArray = byteArrayOutputStream.toByteArray();
        try {
            bufferedInputStream.close();
            byteArrayOutputStream.close();
        } catch (Exception e3) {
            d.c(TAG, e3);
        }
        return toByteArray;
    }

    private boolean fF() {
        if (!(cz.gD == f.iw())) {
            return false;
        }
        Object a;
        try {
            a = p.a(new a() {
                final /* synthetic */ oj Ev;

                {
                    this.Ev = r1;
                }

                public void b(boolean z, boolean z2) {
                    d.d(oj.TAG, "checkWifiApprovement() needWifiApprove: " + z + " receivedError" + z2);
                }
            });
        } catch (WifiApproveException e) {
            d.c(TAG, "checkWifiApprovement() WifiApproveException e: " + e.toString());
            a = null;
        }
        if (TextUtils.isEmpty(a)) {
            return false;
        }
        this.Eu = a;
        d.d(TAG, "checkWifiApprovement() mWifiApprovementUrl: " + this.Eu);
        return true;
    }

    private void fG() {
        this.Eu = null;
    }

    private void fI() {
        if (this.mRedirectCount >= 3) {
            fJ();
        }
    }

    private void fJ() {
        this.Es = null;
        this.mRedirectCount = 0;
    }

    private String fe() {
        d.e(TAG, "getRedirectUrl()");
        try {
            return this.Eq.getHeaderField("Location");
        } catch (Exception e) {
            d.c(TAG, "getRedirectUrl() e: " + e.toString());
            return null;
        }
    }

    private boolean isStarted() {
        return this.Ep;
    }

    public synchronized int a(byte[] bArr, AtomicReference<byte[]> atomicReference) {
        if (bArr == null) {
            return -10;
        }
        if (mu.fb()) {
            return -7;
        }
        int b;
        d.e(TAG, "sendData() data.length: " + bArr.length);
        int i = 0;
        int i2 = -1;
        while (i < 5) {
            boolean isStarted = isStarted();
            d.d(TAG, "sendData() hasStart: " + isStarted);
            AtomicReference atomicReference2 = new AtomicReference();
            if (!isStarted) {
                if (!a(atomicReference2)) {
                    continue;
                    i++;
                }
            }
            b = b(bArr, atomicReference);
            if (b == 0) {
                d.d(TAG, "sendData() \u53d1\u5305\u6210\u529f");
                fH();
                this.Er.cM((String) atomicReference2.get());
                break;
            } else if (-160000 != b) {
                d.c(TAG, "sendData() ret: " + b);
                fH();
                bZ(b);
                if (4 != i) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        d.c(TAG, "sendData() InterruptedException e: " + e.toString());
                    }
                }
                i2 = b;
                i++;
            } else {
                d.d(TAG, "sendData() \u9700\u8981wifi\u8ba4\u8bc1");
                fH();
            }
        }
        b = i2;
        return b;
    }

    public synchronized boolean a(AtomicReference<String> atomicReference) {
        String fY;
        d.d(TAG, "start()");
        this.Ep = true;
        fI();
        if (TextUtils.isEmpty(this.Es)) {
            fY = this.Er.fY();
            if (atomicReference != null) {
                atomicReference.set(fY);
            }
        } else {
            fY = this.Es;
        }
        cz iw = f.iw();
        d.d(TAG, "start() urlValue: " + fY + " networkType: " + iw);
        if (cz.gB != iw) {
            long currentTimeMillis = System.currentTimeMillis();
            try {
                URL url = new URL(fY);
                try {
                    if (cz.gE != iw) {
                        this.Eq = (HttpURLConnection) url.openConnection();
                        this.Eq.setReadTimeout(30000);
                        this.Eq.setConnectTimeout(30000);
                    } else {
                        this.Eq = (HttpURLConnection) url.openConnection(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(f.iy(), f.iz())));
                    }
                    d.d(TAG, "doSend() openTimeMillis: " + (System.currentTimeMillis() - currentTimeMillis));
                    if (j.iM() < 8) {
                        System.setProperty("http.keepAlive", "false");
                    }
                    this.Eq.setUseCaches(false);
                    this.Eq.setRequestProperty("Pragma", "no-cache");
                    this.Eq.setRequestProperty("Cache-Control", "no-cache");
                    this.Eq.setInstanceFollowRedirects(false);
                    if ("GET".equalsIgnoreCase(this.Eo)) {
                        this.Eq.setRequestMethod("GET");
                    } else {
                        this.Eq.setRequestMethod("POST");
                        this.Eq.setDoOutput(true);
                        this.Eq.setDoInput(true);
                        this.Eq.setRequestProperty("Accept", "*/*");
                        this.Eq.setRequestProperty("Accept-Charset", "utf-8");
                        this.Eq.setRequestProperty("Content-Type", "application/octet-stream");
                    }
                    return true;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    return false;
                } catch (IllegalArgumentException e2) {
                    e2.printStackTrace();
                    return false;
                } catch (SecurityException e3) {
                    e3.printStackTrace();
                    return false;
                } catch (UnsupportedOperationException e4) {
                    e4.printStackTrace();
                    return false;
                } catch (IOException e5) {
                    e5.printStackTrace();
                    return false;
                }
            } catch (MalformedURLException e6) {
                e6.printStackTrace();
                d.c(TAG, "start() MalformedURLException e:" + e6.toString());
                return false;
            }
        }
        d.c(TAG, "start() ConnectType.CT_NONE == networkType");
        return false;
    }

    public synchronized boolean fH() {
        d.e(TAG, "stop()");
        this.Ep = false;
        this.Ep = false;
        if (this.Eq == null) {
            return false;
        }
        this.Eq.disconnect();
        this.Eq = null;
        return true;
    }
}
