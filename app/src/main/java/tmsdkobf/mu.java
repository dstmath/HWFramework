package tmsdkobf;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.InflaterInputStream;
import org.apache.http.client.ClientProtocolException;
import tmsdk.common.ErrorCode;
import tmsdk.common.exception.NetWorkException;
import tmsdk.common.exception.NetworkOnMainThreadException;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;
import tmsdk.common.utils.j;

/* compiled from: Unknown */
public final class mu {
    private static volatile boolean BE;
    private static volatile String BF;
    private static volatile boolean BG;
    private static volatile boolean BH;
    private static volatile long BI;
    private static volatile long BJ;
    private static Object BK;
    private static volatile String BL;
    private static ArrayList<Pair<Integer, Long>> BM;
    private int BA;
    private boolean BB;
    private boolean BC;
    private boolean BD;
    private String Bn;
    private int Bo;
    private String Bq;
    private HttpURLConnection Br;
    private int Bs;
    private Hashtable<String, String> Bt;
    private boolean Bu;
    private boolean Bv;
    private byte Bw;
    private byte Bx;
    private byte By;
    private int Bz;
    private byte[] mPostData;
    private String mUrl;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.mu.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.mu.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.mu.<clinit>():void");
    }

    private mu(String str) {
        this.Bq = "GET";
        this.Bs = -1;
        this.Bt = new Hashtable(0);
        this.Bu = false;
        this.Bv = true;
        this.Bw = (byte) 0;
        this.Bx = (byte) 0;
        this.By = (byte) 0;
        this.Bz = 30000;
        this.BA = 30000;
        this.BB = true;
        this.mUrl = str;
        fa();
    }

    private void a(String str, cz czVar) throws NetWorkException {
        try {
            getHostAddress();
            if (cz.gB != czVar) {
                long currentTimeMillis;
                if (cz.gE != czVar) {
                    currentTimeMillis = System.currentTimeMillis();
                    this.Br = (HttpURLConnection) new URL(str).openConnection();
                    d.d("HttpConnection", "initConnection() openTimeMillis: " + (System.currentTimeMillis() - currentTimeMillis));
                    this.Bu = false;
                    this.Br.setReadTimeout(this.BA);
                    this.Br.setConnectTimeout(this.Bz);
                    return;
                }
                Proxy proxy = new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(f.iy(), f.iz()));
                currentTimeMillis = System.currentTimeMillis();
                this.Br = (HttpURLConnection) new URL(str).openConnection(proxy);
                d.d("HttpConnection", "initConnection() proxy openTimeMillis: " + (System.currentTimeMillis() - currentTimeMillis));
                this.Bu = true;
            }
        } catch (IllegalArgumentException e) {
            throw new NetWorkException(-1057, "IllegalArgumentException : " + e.getMessage());
        } catch (SecurityException e2) {
            throw new NetWorkException(-1058, "SecurityException: " + e2.getMessage());
        } catch (UnsupportedOperationException e3) {
            throw new NetWorkException(-1059, "UnsupportedOperationException: " + e3.getMessage());
        } catch (IOException e4) {
            throw new NetWorkException(-1056, "IOException : " + e4.getMessage());
        }
    }

    private void a(String str, boolean z, String str2, int i) throws NetWorkException {
        try {
            getHostAddress();
            if (z) {
                if (str2 == null) {
                    str2 = "10.0.0.172";
                }
                if (i < 0) {
                    i = 80;
                }
                this.Br = (HttpURLConnection) new URL(str).openConnection(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(str2, i)));
                this.Bu = true;
            } else {
                this.Br = (HttpURLConnection) new URL(str).openConnection();
                this.Bu = false;
            }
            this.Br.setReadTimeout(30000);
            this.Br.setConnectTimeout(this.Bz);
        } catch (IllegalArgumentException e) {
            throw new NetWorkException(-1057, "IllegalArgumentException : " + e.getMessage());
        } catch (SecurityException e2) {
            throw new NetWorkException(-1058, "SecurityException: " + e2.getMessage());
        } catch (UnsupportedOperationException e3) {
            throw new NetWorkException(-1059, "UnsupportedOperationException: " + e3.getMessage());
        } catch (IOException e4) {
            throw new NetWorkException(-1056, "IOException : " + e4.getMessage());
        }
    }

    public static mu cA(String str) throws NetWorkException {
        return e(str, true);
    }

    private byte[] d(InputStream inputStream) throws NetWorkException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        byte[] bArr = new byte[2048];
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
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                try {
                    byteArrayOutputStream.close();
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
            }
        }
        bArr = byteArrayOutputStream.toByteArray();
        try {
            bufferedInputStream.close();
        } catch (Exception e222) {
            e222.printStackTrace();
        }
        try {
            byteArrayOutputStream.close();
        } catch (Exception e2222) {
            e2222.printStackTrace();
        }
        return bArr;
    }

    public static mu e(String str, boolean z) throws NetWorkException {
        fa();
        if (!f.iA() && Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
            throw new NetworkOnMainThreadException();
        } else if (str == null || str.length() == 0) {
            throw new NetWorkException((int) ErrorCode.ERR_OPEN_CONNECTION, "url is null!");
        } else {
            mu muVar = new mu(str);
            muVar.BC = false;
            muVar.BB = z;
            cz iw = f.iw();
            if (cz.gB != iw) {
                muVar.a(muVar.mUrl, iw);
                return muVar;
            }
            throw new NetWorkException(-1052, "no connecition!");
        }
    }

    public static void fa() {
        nc ncVar = new nc("wup");
        BJ = ncVar.getLong("dnc", 0);
        BL = ncVar.getString("cn_t_a", "");
        if (!TextUtils.isEmpty(BL)) {
            String[] split = BL.split("\\|");
            for (Object obj : split) {
                if (!TextUtils.isEmpty(obj)) {
                    String[] split2 = obj.split(",");
                    try {
                        synchronized (BM) {
                            if (BM.size() <= 4) {
                                BM.add(new Pair(Integer.valueOf(split2[0]), Long.valueOf(split2[1])));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static boolean fb() {
        d.e("HttpConnection", " couldNotConnect()");
        synchronized (BK) {
            long currentTimeMillis = System.currentTimeMillis();
            long j = BJ - currentTimeMillis;
            d.e("HttpConnection", " couldNotConnect() diff: " + j);
            boolean z = !((j > 5184000 ? 1 : (j == 5184000 ? 0 : -1)) <= 0);
            boolean z2 = !(((BJ - currentTimeMillis) > 0 ? 1 : ((BJ - currentTimeMillis) == 0 ? 0 : -1)) < 0);
            if (!z && z2) {
                d.e("HttpConnection", " couldNotConnect() true");
                return true;
            }
            d.e("HttpConnection", " couldNotConnect() false");
            return false;
        }
    }

    private int fd() throws NetWorkException {
        close();
        if (this.BC) {
            a(this.mUrl, this.BD, this.Bn, this.Bo);
        } else if (f.iw().value() != 0) {
            a(this.mUrl, f.iw());
        } else {
            throw new NetWorkException(-1052, "no connecition!");
        }
        setRequestMethod(this.Bq);
        if ("POST".equalsIgnoreCase(this.Bq) && this.mPostData != null) {
            setPostData(this.mPostData);
        }
        a(this.Bt);
        return fc();
    }

    private boolean isConnected() {
        return this.Bs == SmsCheckResult.ESCT_200 || this.Bs == SmsCheckResult.ESCT_206;
    }

    private String[] split(String str) {
        String str2;
        String[] strArr = new String[2];
        int indexOf = str.indexOf("://");
        if (-1 != indexOf) {
            str = str.substring(indexOf + 3);
        }
        indexOf = str.indexOf("/");
        if (-1 == indexOf) {
            strArr[0] = str;
            str2 = "";
        } else {
            strArr[0] = str.substring(0, indexOf);
            str2 = str.substring(indexOf);
        }
        strArr[1] = str2;
        return strArr;
    }

    public int a(boolean z, AtomicReference<byte[]> atomicReference) throws NetWorkException {
        if (this.Br == null || !isConnected()) {
            return ErrorCode.ERR_RESPONSE;
        }
        InputStream inflaterInputStream;
        if (z) {
            inflaterInputStream = new InflaterInputStream(this.Br.getInputStream());
        } else {
            try {
                inflaterInputStream = this.Br.getInputStream();
            } catch (NetWorkException e) {
                throw new NetWorkException(e.getErrCode() + ErrorCode.ERR_RESPONSE, "get response exception : " + e.getMessage());
            } catch (Exception e2) {
                throw new NetWorkException(-4002, "get response exception : " + e2.getMessage());
            }
        }
        atomicReference.set(d(inflaterInputStream));
        return 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a(Hashtable<String, String> hashtable) {
        if (!(hashtable == null || hashtable.size() == 0 || this.Br == null)) {
            for (Entry entry : hashtable.entrySet()) {
                this.Br.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    public void close() {
        if (this.Br != null) {
            this.Br.disconnect();
            this.Br = null;
        }
    }

    public int fc() throws NetWorkException {
        int i;
        UnknownHostException e;
        IllegalAccessError e2;
        byte b;
        IllegalStateException e3;
        ProtocolException e4;
        ClientProtocolException e5;
        SocketException e6;
        SocketTimeoutException e7;
        NetWorkException e8;
        Exception e9;
        boolean fb = fb();
        d.e("sendRequest", " sendRequest() couldNotConnect:" + fb);
        if (fb) {
            throw new NetWorkException((int) ErrorCode.ERR_OPEN_CONNECTION, "sendRequest() is forbidden couldNotConnect");
        }
        try {
            byte b2;
            if (j.iM() < 8) {
                System.setProperty("http.keepAlive", "false");
            }
            this.Br.setUseCaches(false);
            this.Br.setRequestProperty("Pragma", "no-cache");
            this.Br.setRequestProperty("Cache-Control", "no-cache");
            this.Br.setInstanceFollowRedirects(false);
            if ("GET".equalsIgnoreCase(this.Bq)) {
                i = ErrorCode.ERR_GET;
                this.Br.setRequestMethod("GET");
            } else {
                i = ErrorCode.ERR_POST;
                try {
                    this.Br.setRequestMethod("POST");
                    if (!this.Bt.containsKey("Accept")) {
                        this.Br.setRequestProperty("Accept", "*/*");
                        this.Br.setRequestProperty("Accept-Charset", "utf-8");
                    }
                    this.Br.setDoOutput(true);
                    this.Br.setDoInput(true);
                    if (this.mPostData != null) {
                        if (!this.Bt.containsKey("Content-Type")) {
                            this.Br.setRequestProperty("Content-Type", "application/octet-stream");
                        }
                        this.Br.setRequestProperty("Content-length", "" + this.mPostData.length);
                        long currentTimeMillis = System.currentTimeMillis();
                        OutputStream outputStream = this.Br.getOutputStream();
                        currentTimeMillis = System.currentTimeMillis() - currentTimeMillis;
                        d.d("HttpConnection", "sendRequest() connectTimeMillis: " + currentTimeMillis);
                        if (!(currentTimeMillis <= 0)) {
                            if (!(currentTimeMillis >= 60000) && BM.size() <= 4) {
                                synchronized (BM) {
                                    BM.add(new Pair(Integer.valueOf(ml.Bg), Long.valueOf(currentTimeMillis)));
                                    if (4 == BM.size()) {
                                        StringBuilder stringBuilder = new StringBuilder();
                                        for (int i2 = 0; i2 < BM.size(); i2++) {
                                            Pair pair = (Pair) BM.get(i2);
                                            if (pair != null) {
                                                stringBuilder.append(pair.first);
                                                stringBuilder.append(",");
                                                stringBuilder.append(pair.second);
                                                if (BM.size() - 1 != i2) {
                                                    stringBuilder.append("|");
                                                }
                                            }
                                        }
                                        BL = stringBuilder.toString();
                                        new nc("wup").a("cn_t_a", BL, true);
                                        d.d("HttpConnection", "sendRequest() mConnectTimeMillisAll: " + BL);
                                    }
                                }
                            }
                        }
                        outputStream.write(this.mPostData);
                        outputStream.flush();
                        outputStream.close();
                    }
                } catch (UnknownHostException e10) {
                    e = e10;
                } catch (IllegalAccessError e11) {
                    e2 = e11;
                    b = this.By;
                    this.By = (byte) ((byte) (b + 1));
                    if (b >= (byte) 2) {
                        return fd();
                    }
                    throw new NetWorkException(i - 60, "sendRequest IllegalAccessError: " + e2.getMessage());
                } catch (IllegalStateException e12) {
                    e3 = e12;
                    b = this.By;
                    this.By = (byte) ((byte) (b + 1));
                    if (b >= (byte) 2) {
                        return fd();
                    }
                    throw new NetWorkException(i - 61, "sendRequest IllegalStateException: " + e3.getMessage());
                } catch (ProtocolException e13) {
                    e4 = e13;
                    b = this.By;
                    this.By = (byte) ((byte) (b + 1));
                    if (b >= (byte) 2) {
                        return fd();
                    }
                    throw new NetWorkException(i - 51, "sendRequest ProtocolException: " + e4.getMessage());
                } catch (ClientProtocolException e14) {
                    e5 = e14;
                    b = this.By;
                    this.By = (byte) ((byte) (b + 1));
                    if (b >= (byte) 2) {
                        return fd();
                    }
                    throw new NetWorkException(i - 51, "sendRequest ClientProtocolException: " + e5.getMessage());
                } catch (SocketException e15) {
                    e6 = e15;
                    b = this.By;
                    this.By = (byte) ((byte) (b + 1));
                    if (b >= (byte) 2) {
                        return fd();
                    }
                    throw new NetWorkException(i - 54, "sendRequest SocketException: " + e6.getMessage());
                } catch (SocketTimeoutException e16) {
                    e7 = e16;
                    b = this.By;
                    this.By = (byte) ((byte) (b + 1));
                    if (b < (byte) 2) {
                        this.Bz = 60000;
                        this.BA = 60000;
                        return fd();
                    }
                    throw new NetWorkException(i - 55, "sendRequest" + e7.getMessage());
                } catch (NetWorkException e17) {
                    e8 = e17;
                    b = this.By;
                    this.By = (byte) ((byte) (b + 1));
                    if (b >= (byte) 2) {
                        return fd();
                    }
                    throw new NetWorkException(i - 56, "sendRequest NetWorkException: " + e8.getMessage());
                } catch (Exception e18) {
                    e9 = e18;
                    b = this.By;
                    this.By = (byte) ((byte) (b + 1));
                    if (b >= (byte) 2) {
                        return fd();
                    }
                    throw new NetWorkException(i, "sendRequest " + e9.getClass().getName() + " : " + e9.getMessage());
                }
            }
            this.Bs = this.Br.getResponseCode();
            d.e("HttpConnection", "HttpUrlConn.getResponseCode : " + this.Bs);
            if (this.Bs >= SmsCheckResult.ESCT_301) {
                if (this.Bs <= SmsCheckResult.ESCT_305) {
                    b2 = this.Bw;
                    this.Bw = (byte) ((byte) (b2 + 1));
                    if (b2 < 3) {
                        this.mUrl = fe();
                        return fd();
                    }
                    if (this.Bs == SmsCheckResult.ESCT_206 || this.Bs == SmsCheckResult.ESCT_200) {
                        return this.Bs;
                    }
                    b2 = this.By;
                    this.By = (byte) ((byte) (b2 + 1));
                    if (b2 < (byte) 2) {
                        throw new NetWorkException(this.Bs + i, "response code is unnormal: " + this.Bs + " SDK Version:" + j.iM());
                    }
                    if (i == -1 && "true".equals(System.getProperty("http.keepAlive"))) {
                        System.setProperty("http.keepAlive", "false");
                    }
                    return fd();
                }
            }
            if (this.Bs == SmsCheckResult.ESCT_200) {
                String contentType = getContentType();
                if (!(!this.Bu || contentType == null || contentType.toLowerCase().indexOf("vnd.wap.wml") == -1)) {
                    b2 = this.Bx;
                    this.Bx = (byte) ((byte) (b2 + 1));
                    if (b2 < (byte) 1) {
                        return fd();
                    }
                }
            }
            if (this.Bs == SmsCheckResult.ESCT_206) {
                b2 = this.By;
                this.By = (byte) ((byte) (b2 + 1));
                if (b2 < (byte) 2) {
                    System.setProperty("http.keepAlive", "false");
                    return fd();
                }
                throw new NetWorkException(this.Bs + i, "response code is unnormal: " + this.Bs + " SDK Version:" + j.iM());
            }
            return this.Bs;
        } catch (UnknownHostException e19) {
            e = e19;
            i = 0;
            byte b3 = this.By;
            this.By = (byte) ((byte) (b3 + 1));
            if (b3 >= (byte) 2) {
                if (this.Bv) {
                    this.Bv = false;
                    close();
                    if (this.Bu) {
                        a(this.mUrl, cz.gF);
                    } else if (cz.gD == f.iw()) {
                        String iy = f.iy();
                        if (iy != null && iy.length() > 0 && f.iz() > 0) {
                            a(this.mUrl, cz.gE);
                        } else {
                            throw new NetWorkException(i - 62, "sendRequest UnknownHostException: " + e.getMessage() + " networktype:" + f.iw());
                        }
                    }
                    if (this.Br != null) {
                        setRequestMethod(this.Bq);
                        if ("POST".equalsIgnoreCase(this.Bq) && this.mPostData != null) {
                            setPostData(this.mPostData);
                        }
                        a(this.Bt);
                        return fc();
                    }
                }
                throw new NetWorkException(i - 62, "sendRequest UnknownHostException: " + e.getMessage() + " networktype:" + f.iw());
            }
            getHostAddress();
            return fd();
        } catch (IllegalAccessError e20) {
            e2 = e20;
            i = 0;
            b = this.By;
            this.By = (byte) ((byte) (b + 1));
            if (b >= (byte) 2) {
                return fd();
            }
            throw new NetWorkException(i - 60, "sendRequest IllegalAccessError: " + e2.getMessage());
        } catch (IllegalStateException e21) {
            e3 = e21;
            i = 0;
            b = this.By;
            this.By = (byte) ((byte) (b + 1));
            if (b >= (byte) 2) {
                return fd();
            }
            throw new NetWorkException(i - 61, "sendRequest IllegalStateException: " + e3.getMessage());
        } catch (ProtocolException e22) {
            e4 = e22;
            i = 0;
            b = this.By;
            this.By = (byte) ((byte) (b + 1));
            if (b >= (byte) 2) {
                return fd();
            }
            throw new NetWorkException(i - 51, "sendRequest ProtocolException: " + e4.getMessage());
        } catch (ClientProtocolException e23) {
            e5 = e23;
            i = 0;
            b = this.By;
            this.By = (byte) ((byte) (b + 1));
            if (b >= (byte) 2) {
                return fd();
            }
            throw new NetWorkException(i - 51, "sendRequest ClientProtocolException: " + e5.getMessage());
        } catch (SocketException e24) {
            e6 = e24;
            i = 0;
            b = this.By;
            this.By = (byte) ((byte) (b + 1));
            if (b >= (byte) 2) {
                return fd();
            }
            throw new NetWorkException(i - 54, "sendRequest SocketException: " + e6.getMessage());
        } catch (SocketTimeoutException e25) {
            e7 = e25;
            i = 0;
            b = this.By;
            this.By = (byte) ((byte) (b + 1));
            if (b < (byte) 2) {
                throw new NetWorkException(i - 55, "sendRequest" + e7.getMessage());
            }
            this.Bz = 60000;
            this.BA = 60000;
            return fd();
        } catch (NetWorkException e26) {
            e8 = e26;
            i = 0;
            b = this.By;
            this.By = (byte) ((byte) (b + 1));
            if (b >= (byte) 2) {
                return fd();
            }
            throw new NetWorkException(i - 56, "sendRequest NetWorkException: " + e8.getMessage());
        } catch (Exception e27) {
            e9 = e27;
            i = 0;
            b = this.By;
            this.By = (byte) ((byte) (b + 1));
            if (b >= (byte) 2) {
                return fd();
            }
            throw new NetWorkException(i, "sendRequest " + e9.getClass().getName() + " : " + e9.getMessage());
        }
    }

    public String fe() throws NetWorkException {
        try {
            return this.Br.getHeaderField("Location");
        } catch (Exception e) {
            throw new NetWorkException(-56, "get redirect url: " + e.getMessage());
        }
    }

    public String getContentType() throws NetWorkException {
        try {
            return this.Br.getHeaderField("Content-Type");
        } catch (Exception e) {
            throw new NetWorkException(-56, "get content type: " + e.getMessage());
        }
    }

    public String getHostAddress() {
        if (this.mUrl == null) {
            return "";
        }
        String host = this.Br == null ? split(this.mUrl)[0] : this.Br.getURL().getHost();
        if (host == null || host.length() == 0) {
            if (this.Br == null) {
                host = split(this.mUrl)[0];
            }
        }
        try {
            InetAddress byName = InetAddress.getByName(host);
            if (byName != null) {
                return byName.getHostAddress();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SecurityException e2) {
            e2.printStackTrace();
        }
        return "";
    }

    public int getResponseCode() {
        return this.Bs;
    }

    public void setPostData(byte[] bArr) {
        this.mPostData = bArr;
    }

    public void setRequestMethod(String str) {
        String str2;
        this.Bq = str;
        if ("GET".equalsIgnoreCase(str)) {
            str2 = "GET";
        } else if ("POST".equalsIgnoreCase(str)) {
            str2 = "POST";
        } else {
            return;
        }
        this.Bq = str2;
    }
}
