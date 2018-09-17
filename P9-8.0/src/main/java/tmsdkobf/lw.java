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
import tmsdk.common.utils.i;
import tmsdk.common.utils.n;

public final class lw {
    private static volatile boolean zp = false;
    private static volatile String zq = null;
    private static volatile boolean zr = false;
    private static volatile boolean zs = false;
    private static volatile long zt = 0;
    private static volatile long zu = 0;
    private static Object zv = new Object();
    private static volatile String zw;
    private static ArrayList<Pair<Integer, Long>> zx = new ArrayList();
    private byte[] mPostData;
    private String mUrl;
    private String yY;
    private int yZ;
    private String zb = "GET";
    private HttpURLConnection zc;
    private int zd = -1;
    private Hashtable<String, String> ze = new Hashtable(0);
    private boolean zf = false;
    private boolean zg = true;
    private byte zh = (byte) 0;
    private byte zi = (byte) 0;
    private byte zj = (byte) 0;
    private int zk = 30000;
    private int zl = 30000;
    private boolean zm = true;
    private boolean zn;
    private boolean zo;

    private lw(String str) {
        this.mUrl = str;
        eI();
    }

    private void a(String str, eb ebVar) throws NetWorkException {
        try {
            getHostAddress();
            if (eb.iH != ebVar) {
                if (eb.iK != ebVar) {
                    long currentTimeMillis = System.currentTimeMillis();
                    this.zc = (HttpURLConnection) new URL(str).openConnection();
                    mb.n("HttpConnection", "initConnection() openTimeMillis: " + (System.currentTimeMillis() - currentTimeMillis));
                    this.zf = false;
                    this.zc.setReadTimeout(this.zl);
                    this.zc.setConnectTimeout(this.zk);
                    return;
                }
                Proxy proxy = new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(i.iI(), i.iJ()));
                long currentTimeMillis2 = System.currentTimeMillis();
                this.zc = (HttpURLConnection) new URL(str).openConnection(proxy);
                mb.n("HttpConnection", "initConnection() proxy openTimeMillis: " + (System.currentTimeMillis() - currentTimeMillis2));
                this.zf = true;
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
                this.zc = (HttpURLConnection) new URL(str).openConnection(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(str2, i)));
                this.zf = true;
            } else {
                this.zc = (HttpURLConnection) new URL(str).openConnection();
                this.zf = false;
            }
            this.zc.setReadTimeout(30000);
            this.zc.setConnectTimeout(this.zk);
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

    public static lw bO(String str) throws NetWorkException {
        return e(str, true);
    }

    private byte[] d(InputStream inputStream) throws NetWorkException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        byte[] bArr = new byte[2048];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            try {
                int read = inputStream.read(bArr);
                if (read == -1) {
                    byte[] toByteArray = byteArrayOutputStream.toByteArray();
                    try {
                        bufferedInputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        byteArrayOutputStream.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    return toByteArray;
                }
                byteArrayOutputStream.write(bArr, 0, read);
            } catch (IOException e3) {
                throw new NetWorkException(-56, "get Bytes from inputStream when read buffer: " + e3.getMessage());
            } catch (Throwable th) {
                try {
                    bufferedInputStream.close();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
                try {
                    byteArrayOutputStream.close();
                } catch (Exception e42) {
                    e42.printStackTrace();
                }
            }
        }
    }

    public static lw e(String str, boolean z) throws NetWorkException {
        eI();
        if (!i.iK() && Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
            throw new NetworkOnMainThreadException();
        } else if (str == null || str.length() == 0) {
            throw new NetWorkException((int) ErrorCode.ERR_OPEN_CONNECTION, "url is null!");
        } else {
            lw lwVar = new lw(str);
            lwVar.zn = false;
            lwVar.zm = z;
            eb iG = i.iG();
            if (eb.iH != iG) {
                lwVar.a(lwVar.mUrl, iG);
                return lwVar;
            }
            throw new NetWorkException(-1052, "no connecition!");
        }
    }

    public static void eI() {
        md mdVar = new md("wup");
        zu = mdVar.getLong("dnc", 0);
        zw = mdVar.getString("cn_t_a", "");
        if (!TextUtils.isEmpty(zw)) {
            String[] split = zw.split("\\|");
            for (Object obj : split) {
                if (!TextUtils.isEmpty(obj)) {
                    String[] split2 = obj.split(",");
                    try {
                        synchronized (zx) {
                            if (zx.size() <= 4) {
                                zx.add(new Pair(Integer.valueOf(split2[0]), Long.valueOf(split2[1])));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static boolean eJ() {
        mb.d("HttpConnection", " couldNotConnect()");
        synchronized (zv) {
            long currentTimeMillis = System.currentTimeMillis();
            long j = zu - currentTimeMillis;
            mb.d("HttpConnection", " couldNotConnect() diff: " + j);
            boolean z = !((j > 5184000 ? 1 : (j == 5184000 ? 0 : -1)) <= 0);
            boolean z2 = !(((zu - currentTimeMillis) > 0 ? 1 : ((zu - currentTimeMillis) == 0 ? 0 : -1)) < 0);
            if (!z && z2) {
                mb.d("HttpConnection", " couldNotConnect() true");
                return true;
            }
            mb.d("HttpConnection", " couldNotConnect() false");
            return false;
        }
    }

    private int eL() throws NetWorkException {
        close();
        if (this.zn) {
            a(this.mUrl, this.zo, this.yY, this.yZ);
        } else if (i.iG().value() != 0) {
            a(this.mUrl, i.iG());
        } else {
            throw new NetWorkException(-1052, "no connecition!");
        }
        setRequestMethod(this.zb);
        if ("POST".equalsIgnoreCase(this.zb) && this.mPostData != null) {
            setPostData(this.mPostData);
        }
        a(this.ze);
        return eK();
    }

    private boolean isConnected() {
        return this.zd == SmsCheckResult.ESCT_200 || this.zd == SmsCheckResult.ESCT_206;
    }

    private String[] split(String str) {
        String[] strArr = new String[2];
        int indexOf = str.indexOf("://");
        if (-1 != indexOf) {
            str = str.substring(indexOf + 3);
        }
        indexOf = str.indexOf("/");
        if (-1 == indexOf) {
            strArr[0] = str;
            strArr[1] = "";
        } else {
            strArr[0] = str.substring(0, indexOf);
            strArr[1] = str.substring(indexOf);
        }
        return strArr;
    }

    public int a(boolean z, AtomicReference<byte[]> atomicReference) throws NetWorkException {
        if (this.zc == null || !isConnected()) {
            return ErrorCode.ERR_RESPONSE;
        }
        InputStream inflaterInputStream;
        if (z) {
            inflaterInputStream = new InflaterInputStream(this.zc.getInputStream());
        } else {
            try {
                inflaterInputStream = this.zc.getInputStream();
            } catch (NetWorkException e) {
                throw new NetWorkException(e.getErrCode() + ErrorCode.ERR_RESPONSE, "get response exception : " + e.getMessage());
            } catch (Exception e2) {
                throw new NetWorkException(-4002, "get response exception : " + e2.getMessage());
            }
        }
        atomicReference.set(d(inflaterInputStream));
        return 0;
    }

    /* JADX WARNING: Missing block: B:1:0x0002, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a(Hashtable<String, String> hashtable) {
        if (!(hashtable == null || hashtable.size() == 0 || this.zc == null)) {
            for (Entry entry : hashtable.entrySet()) {
                this.zc.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    public void close() {
        if (this.zc != null) {
            this.zc.disconnect();
            this.zc = null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:182:0x06d0  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x069a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int eK() throws NetWorkException {
        boolean eJ = eJ();
        mb.d("sendRequest", " sendRequest() couldNotConnect:" + eJ);
        if (eJ) {
            throw new NetWorkException(ErrorCode.ERR_OPEN_CONNECTION, "sendRequest() is forbidden couldNotConnect");
        }
        int i = 0;
        byte b;
        try {
            if (n.iX() < 8) {
                System.setProperty("http.keepAlive", "false");
            }
            this.zc.setUseCaches(false);
            this.zc.setRequestProperty("Pragma", "no-cache");
            this.zc.setRequestProperty("Cache-Control", "no-cache");
            this.zc.setInstanceFollowRedirects(false);
            if ("GET".equalsIgnoreCase(this.zb)) {
                i = ErrorCode.ERR_GET;
                this.zc.setRequestMethod("GET");
            } else {
                i = ErrorCode.ERR_POST;
                this.zc.setRequestMethod("POST");
                if (!this.ze.containsKey("Accept")) {
                    this.zc.setRequestProperty("Accept", "*/*");
                    this.zc.setRequestProperty("Accept-Charset", "utf-8");
                }
                this.zc.setDoOutput(true);
                this.zc.setDoInput(true);
                if (this.mPostData != null) {
                    if (!this.ze.containsKey("Content-Type")) {
                        this.zc.setRequestProperty("Content-Type", "application/octet-stream");
                    }
                    this.zc.setRequestProperty("Content-length", "" + this.mPostData.length);
                    long currentTimeMillis = System.currentTimeMillis();
                    OutputStream outputStream = this.zc.getOutputStream();
                    long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
                    mb.n("HttpConnection", "sendRequest() connectTimeMillis: " + currentTimeMillis2);
                    if ((currentTimeMillis2 <= 0 ? 1 : null) == null) {
                        if ((currentTimeMillis2 >= 60000 ? 1 : null) == null && zx.size() <= 4) {
                            synchronized (zx) {
                                zx.add(new Pair(Integer.valueOf(ln.yR), Long.valueOf(currentTimeMillis2)));
                                if (4 == zx.size()) {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    for (int i2 = 0; i2 < zx.size(); i2++) {
                                        Pair pair = (Pair) zx.get(i2);
                                        if (pair != null) {
                                            stringBuilder.append(pair.first);
                                            stringBuilder.append(",");
                                            stringBuilder.append(pair.second);
                                            if (zx.size() - 1 != i2) {
                                                stringBuilder.append("|");
                                            }
                                        }
                                    }
                                    zw = stringBuilder.toString();
                                    new md("wup").a("cn_t_a", zw, true);
                                    mb.n("HttpConnection", "sendRequest() mConnectTimeMillisAll: " + zw);
                                }
                            }
                        }
                    }
                    outputStream.write(this.mPostData);
                    outputStream.flush();
                    outputStream.close();
                }
            }
            this.zd = this.zc.getResponseCode();
            mb.d("HttpConnection", "HttpUrlConn.getResponseCode : " + this.zd);
            if (this.zd >= 301) {
                if (this.zd <= 305) {
                    b = this.zh;
                    this.zh = (byte) ((byte) (b + 1));
                    if (b < (byte) 3) {
                        this.mUrl = eM();
                        return eL();
                    }
                    if (this.zd == 206 || this.zd == 200) {
                        return this.zd;
                    }
                    b = this.zj;
                    this.zj = (byte) ((byte) (b + 1));
                    if (b < (byte) 2) {
                        throw new NetWorkException(i + this.zd, "response code is unnormal: " + this.zd + " SDK Version:" + n.iX());
                    }
                    if (i == -1) {
                        if ("true".equals(System.getProperty("http.keepAlive"))) {
                            System.setProperty("http.keepAlive", "false");
                        }
                    }
                    return eL();
                }
            }
            if (this.zd == 200) {
                String contentType = getContentType();
                if (!(!this.zf || contentType == null || contentType.toLowerCase().indexOf("vnd.wap.wml") == -1)) {
                    b = this.zi;
                    this.zi = (byte) ((byte) (b + 1));
                    if (b < (byte) 1) {
                        return eL();
                    }
                }
            }
            if (this.zd == 206) {
                b = this.zj;
                this.zj = (byte) ((byte) (b + 1));
                if (b < (byte) 2) {
                }
            }
            return this.zd;
        } catch (UnknownHostException e) {
            b = this.zj;
            this.zj = (byte) ((byte) (b + 1));
            if (b >= (byte) 2) {
                if (this.zg) {
                    this.zg = false;
                    close();
                    if (this.zf) {
                        a(this.mUrl, eb.iL);
                    } else if (eb.iJ == i.iG()) {
                        String iI = i.iI();
                        if (iI != null && iI.length() > 0 && i.iJ() > 0) {
                            a(this.mUrl, eb.iK);
                        } else {
                            throw new NetWorkException(i - 62, "sendRequest UnknownHostException: " + e.getMessage() + " networktype:" + i.iG());
                        }
                    }
                    if (this.zc != null) {
                        setRequestMethod(this.zb);
                        if ("POST".equalsIgnoreCase(this.zb) && this.mPostData != null) {
                            setPostData(this.mPostData);
                        }
                        a(this.ze);
                        return eK();
                    }
                }
                throw new NetWorkException(i - 62, "sendRequest UnknownHostException: " + e.getMessage() + " networktype:" + i.iG());
            }
            getHostAddress();
            return eL();
        } catch (IllegalAccessError e2) {
            b = this.zj;
            this.zj = (byte) ((byte) (b + 1));
            if (b < (byte) 2) {
                return eL();
            }
            throw new NetWorkException(i - 60, "sendRequest IllegalAccessError: " + e2.getMessage());
        } catch (IllegalStateException e3) {
            b = this.zj;
            this.zj = (byte) ((byte) (b + 1));
            if (b < (byte) 2) {
                return eL();
            }
            throw new NetWorkException(i - 61, "sendRequest IllegalStateException: " + e3.getMessage());
        } catch (ProtocolException e4) {
            b = this.zj;
            this.zj = (byte) ((byte) (b + 1));
            if (b < (byte) 2) {
                return eL();
            }
            throw new NetWorkException(i - 51, "sendRequest ProtocolException: " + e4.getMessage());
        } catch (ClientProtocolException e5) {
            b = this.zj;
            this.zj = (byte) ((byte) (b + 1));
            if (b < (byte) 2) {
                return eL();
            }
            throw new NetWorkException(i - 51, "sendRequest ClientProtocolException: " + e5.getMessage());
        } catch (SocketException e6) {
            b = this.zj;
            this.zj = (byte) ((byte) (b + 1));
            if (b < (byte) 2) {
                return eL();
            }
            throw new NetWorkException(i - 54, "sendRequest SocketException: " + e6.getMessage());
        } catch (SocketTimeoutException e7) {
            b = this.zj;
            this.zj = (byte) ((byte) (b + 1));
            if (b >= (byte) 2) {
                throw new NetWorkException(i - 55, "sendRequest" + e7.getMessage());
            }
            this.zk = 60000;
            this.zl = 60000;
            return eL();
        } catch (NetWorkException e8) {
            b = this.zj;
            this.zj = (byte) ((byte) (b + 1));
            if (b < (byte) 2) {
                return eL();
            }
            throw new NetWorkException(i - 56, "sendRequest NetWorkException: " + e8.getMessage());
        } catch (Exception e9) {
            b = this.zj;
            this.zj = (byte) ((byte) (b + 1));
            if (b < (byte) 2) {
                return eL();
            }
            throw new NetWorkException(i, "sendRequest " + e9.getClass().getName() + " : " + e9.getMessage());
        }
    }

    public String eM() throws NetWorkException {
        try {
            return this.zc.getHeaderField("Location");
        } catch (Exception e) {
            throw new NetWorkException(-56, "get redirect url: " + e.getMessage());
        }
    }

    public String getContentType() throws NetWorkException {
        try {
            return this.zc.getHeaderField("Content-Type");
        } catch (Exception e) {
            throw new NetWorkException(-56, "get content type: " + e.getMessage());
        }
    }

    public String getHostAddress() {
        if (this.mUrl == null) {
            return "";
        }
        String host = this.zc == null ? split(this.mUrl)[0] : this.zc.getURL().getHost();
        if ((host == null || host.length() == 0) && this.zc == null) {
            host = split(this.mUrl)[0];
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
        return this.zd;
    }

    public void setPostData(byte[] bArr) {
        this.mPostData = bArr;
    }

    public void setRequestMethod(String str) {
        String str2;
        this.zb = str;
        if ("GET".equalsIgnoreCase(str)) {
            str2 = "GET";
        } else if ("POST".equalsIgnoreCase(str)) {
            str2 = "POST";
        } else {
            return;
        }
        this.zb = str2;
    }
}
