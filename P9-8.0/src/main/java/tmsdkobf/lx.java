package tmsdkobf;

import android.content.Context;
import android.net.Proxy;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.InflaterInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import tmsdk.common.ErrorCode;
import tmsdk.common.exception.NetWorkException;
import tmsdk.common.exception.NetworkOnMainThreadException;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.f;
import tmsdk.common.utils.i;

public class lx extends lv {
    private Context mContext;
    private boolean mIsCanceled = false;
    private String zA = null;
    private String zB = null;
    private String zC = null;
    private int zD = 0;
    private long zE = 0;
    private long zF = 0;
    private boolean zG = false;
    private HttpGet zy = null;
    private String zz = null;

    public interface a {
        boolean bS(String str);
    }

    public lx(Context context) {
        if (!i.iK() && Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
            throw new NetworkOnMainThreadException();
        }
        this.mContext = context;
        this.zz = context.getCacheDir().getAbsolutePath();
        this.zA = context.getFilesDir().getAbsolutePath();
        this.zy = new HttpGet();
        if (i.iG() == eb.iK) {
            d(Proxy.getDefaultHost(), Proxy.getDefaultPort());
            u(true);
        }
    }

    private int a(HttpEntity httpEntity, Bundle bundle, boolean z) throws NetWorkException {
        FileNotFoundException e;
        SocketException e2;
        SocketTimeoutException e3;
        IOException e4;
        Exception e5;
        Throwable th;
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        byte[] bArr = new byte[8192];
        try {
            this.zF = httpEntity.getContentLength() + this.zE;
            int i = (int) ((this.zE * 100) / this.zF);
            File file = new File(this.zz, this.zB);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream fileOutputStream2 = new FileOutputStream(file, true);
            if (z) {
                inputStream = new InflaterInputStream(httpEntity.getContent());
            } else {
                try {
                    inputStream = httpEntity.getContent();
                } catch (FileNotFoundException e6) {
                    e = e6;
                    fileOutputStream = fileOutputStream2;
                } catch (SocketException e7) {
                    e2 = e7;
                    fileOutputStream = fileOutputStream2;
                } catch (SocketTimeoutException e8) {
                    e3 = e8;
                    fileOutputStream = fileOutputStream2;
                } catch (IOException e9) {
                    e4 = e9;
                    fileOutputStream = fileOutputStream2;
                } catch (Exception e10) {
                    e5 = e10;
                    fileOutputStream = fileOutputStream2;
                } catch (Throwable th2) {
                    th = th2;
                    fileOutputStream = fileOutputStream2;
                }
            }
            int i2 = 0;
            while (true) {
                int read = inputStream.read(bArr);
                if (read == -1) {
                    fileOutputStream2.flush();
                    f.f("HttpBase", "mTotalSize: " + this.zF + ", mCompletedSize: " + this.zE + ", httpEntity.getContentLength(): " + httpEntity.getContentLength());
                    int i3 = ((long) i2) == httpEntity.getContentLength() ? 0 : -7;
                    if (inputStream != null) {
                        f.f("HttpBase", "is closing file");
                        try {
                            inputStream.close();
                        } catch (IOException e42) {
                            i3 = ErrorCode.ERR_FILE_OP;
                            f.e("HttpBase", "is close file error");
                            e42.printStackTrace();
                        }
                    }
                    if (fileOutputStream2 != null) {
                        f.f("HttpBase", "fos closing file");
                        try {
                            fileOutputStream2.close();
                        } catch (IOException e422) {
                            i3 = ErrorCode.ERR_FILE_OP;
                            f.e("HttpBase", "fos close file error");
                            e422.printStackTrace();
                        }
                    }
                    return i3;
                } else if (this.mIsCanceled) {
                    if (inputStream != null) {
                        f.f("HttpBase", "is closing file");
                        try {
                            inputStream.close();
                        } catch (IOException e11) {
                            f.e("HttpBase", "is close file error");
                            e11.printStackTrace();
                        }
                    }
                    if (fileOutputStream2 != null) {
                        f.f("HttpBase", "fos closing file");
                        try {
                            fileOutputStream2.close();
                        } catch (IOException e112) {
                            f.e("HttpBase", "fos close file error");
                            e112.printStackTrace();
                        }
                    }
                    return -5003;
                } else {
                    this.zE += (long) read;
                    i2 += read;
                    int i4 = (int) ((this.zE * 100) / this.zF);
                    if (i4 != i) {
                        i = i4;
                        bundle.putInt("key_progress", i4);
                        a(2, bundle);
                    }
                    fileOutputStream2.write(bArr, 0, read);
                }
            }
        } catch (FileNotFoundException e12) {
            e = e12;
        } catch (SocketException e13) {
            e2 = e13;
            f.e("HttpBase", "socket error:" + e2.getMessage());
            e2.printStackTrace();
            throw new NetWorkException(-5054, e2.getMessage());
        } catch (SocketTimeoutException e14) {
            e3 = e14;
            f.e("HttpBase", "socket timeout error:" + e3.getMessage());
            e3.printStackTrace();
            throw new NetWorkException(-5055, e3.getMessage());
        } catch (IOException e15) {
            e422 = e15;
            f.e("HttpBase", "socket or file io error");
            e422.printStackTrace();
            throw new NetWorkException(-5056, e422.getMessage());
        } catch (Exception e16) {
            e5 = e16;
            f.e("HttpBase", e5.toString());
            f.e("HttpBase", "receive data error");
            e5.printStackTrace();
            throw new NetWorkException((int) ErrorCode.ERR_RECEIVE, e5.getMessage());
        } catch (Throwable th3) {
            th = th3;
            if (inputStream != null) {
                f.f("HttpBase", "is closing file");
                try {
                    inputStream.close();
                } catch (IOException e17) {
                    f.e("HttpBase", "is close file error");
                    e17.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                f.f("HttpBase", "fos closing file");
                try {
                    fileOutputStream.close();
                } catch (IOException e172) {
                    f.e("HttpBase", "fos close file error");
                    e172.printStackTrace();
                }
            }
            throw th;
        }
        f.e("HttpBase", "file not found");
        e.printStackTrace();
        throw new NetWorkException(-7001, e.getMessage());
    }

    private int bR(String str) throws NetWorkException {
        try {
            URI uri = new URI(str);
            if (uri == null) {
                return ErrorCode.ERR_OPEN_CONNECTION;
            }
            this.zy.setURI(uri);
            return 0;
        } catch (URISyntaxException e) {
            f.e("HttpBase", "url error: " + e.getMessage());
            e.printStackTrace();
            throw new NetWorkException(-1053, e.getMessage());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x00f7  */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x00ab A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x016f A:{SYNTHETIC, Splitter: B:69:0x016f} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0184 A:{SYNTHETIC, Splitter: B:73:0x0184} */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x016f A:{SYNTHETIC, Splitter: B:69:0x016f} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0184 A:{SYNTHETIC, Splitter: B:73:0x0184} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int v(boolean z) throws NetWorkException {
        FileNotFoundException e;
        IOException e2;
        Exception e3;
        Throwable th;
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;
        f.f("HttpGetFile", this.zz + File.separator + this.zB);
        f.f("HttpGetFile", this.zA + File.separator + this.zC);
        File file = null;
        try {
            File file2 = new File(this.zz, this.zB);
            try {
                int i;
                if (file2.exists()) {
                    FileInputStream fileInputStream2;
                    byte[] bArr;
                    int read;
                    FileOutputStream fileOutputStream2;
                    if (this.zD == 1) {
                        if (this.mContext.getFilesDir().getAbsolutePath().equals(this.zA)) {
                            fileOutputStream = this.mContext.openFileOutput(this.zC, 1);
                            fileInputStream2 = new FileInputStream(file2);
                            bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
                            while (true) {
                                read = fileInputStream2.read(bArr);
                                if (read != -1) {
                                    i = 0;
                                    fileInputStream = fileInputStream2;
                                    break;
                                }
                                fileOutputStream.write(bArr, 0, read);
                            }
                        }
                    }
                    File file3 = new File(this.zA + File.separator + this.zC);
                    if (file3.exists()) {
                        file3.delete();
                        fileOutputStream2 = new FileOutputStream(file3);
                    } else {
                        file3.getParentFile().mkdirs();
                        file3.createNewFile();
                        fileOutputStream2 = new FileOutputStream(file3);
                    }
                    fileOutputStream = fileOutputStream2;
                    try {
                        fileInputStream2 = new FileInputStream(file2);
                        try {
                            bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
                            while (true) {
                                read = fileInputStream2.read(bArr);
                                if (read != -1) {
                                }
                            }
                        } catch (FileNotFoundException e4) {
                            e = e4;
                            file = file2;
                            fileInputStream = fileInputStream2;
                        } catch (IOException e5) {
                            e2 = e5;
                            file = file2;
                            fileInputStream = fileInputStream2;
                        } catch (Exception e6) {
                            e3 = e6;
                            file = file2;
                            fileInputStream = fileInputStream2;
                        } catch (Throwable th2) {
                            th = th2;
                            file = file2;
                            fileInputStream = fileInputStream2;
                        }
                    } catch (FileNotFoundException e7) {
                        e = e7;
                        file = file2;
                        f.e("HttpBase", "file not found");
                        e.printStackTrace();
                        throw new NetWorkException(-7001, e.getMessage());
                    } catch (IOException e8) {
                        e2 = e8;
                        file = file2;
                        f.e("HttpBase", "file io error");
                        e2.printStackTrace();
                        throw new NetWorkException(-7056, e2.getMessage());
                    } catch (Exception e9) {
                        e3 = e9;
                        file = file2;
                        f.e("HttpBase", "file op error");
                        e3.printStackTrace();
                        throw new NetWorkException((int) ErrorCode.ERR_FILE_OP, e3.getMessage());
                    } catch (Throwable th3) {
                        th = th3;
                        file = file2;
                        if (fileInputStream != null) {
                        }
                        if (fileOutputStream != null) {
                        }
                        file.delete();
                        throw th;
                    }
                }
                i = -7001;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e22) {
                        i = ErrorCode.ERR_FILE_OP;
                        f.e("HttpBase", "fis close file error");
                        e22.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e222) {
                        i = ErrorCode.ERR_FILE_OP;
                        f.e("HttpBase", "fosclose file error");
                        e222.printStackTrace();
                    }
                }
                if (z && file2 != null && file2.exists()) {
                    file2.delete();
                }
                return i;
            } catch (FileNotFoundException e10) {
                e = e10;
                file = file2;
                f.e("HttpBase", "file not found");
                e.printStackTrace();
                throw new NetWorkException(-7001, e.getMessage());
            } catch (IOException e11) {
                e222 = e11;
                file = file2;
                f.e("HttpBase", "file io error");
                e222.printStackTrace();
                throw new NetWorkException(-7056, e222.getMessage());
            } catch (Exception e12) {
                e3 = e12;
                file = file2;
                f.e("HttpBase", "file op error");
                e3.printStackTrace();
                throw new NetWorkException((int) ErrorCode.ERR_FILE_OP, e3.getMessage());
            } catch (Throwable th4) {
                th = th4;
                file = file2;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e13) {
                        f.e("HttpBase", "fis close file error");
                        e13.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e132) {
                        f.e("HttpBase", "fosclose file error");
                        e132.printStackTrace();
                    }
                }
                if (z && file != null && file.exists()) {
                    file.delete();
                }
                throw th;
            }
        } catch (FileNotFoundException e14) {
            e = e14;
            f.e("HttpBase", "file not found");
            e.printStackTrace();
            throw new NetWorkException(-7001, e.getMessage());
        } catch (IOException e15) {
            e222 = e15;
            f.e("HttpBase", "file io error");
            e222.printStackTrace();
            throw new NetWorkException(-7056, e222.getMessage());
        } catch (Exception e16) {
            e3 = e16;
            f.e("HttpBase", "file op error");
            e3.printStackTrace();
            throw new NetWorkException((int) ErrorCode.ERR_FILE_OP, e3.getMessage());
        } catch (Throwable th5) {
            th = th5;
            if (fileInputStream != null) {
            }
            if (fileOutputStream != null) {
            }
            file.delete();
            throw th;
        }
    }

    public int a(String -l_10_R, String str, boolean z, a aVar) {
        int i = ErrorCode.ERR_GET;
        String str2 = "";
        HttpClient httpClient = null;
        HttpResponse httpResponse = null;
        Bundle bundle = new Bundle();
        try {
            httpClient = eH();
            i = bR(str);
            if (i == 0) {
                if (!this.mIsCanceled) {
                    if (this.zy.getURI() != null) {
                        String str3 = "downloadfile";
                        if (TextUtils.isEmpty(-l_10_R)) {
                            -l_10_R = lu.p(str, null);
                        }
                        this.zB = -l_10_R + ".tmp";
                        f.f("HttpBase", "mTempName: " + this.zB);
                        if (this.zC == null) {
                            this.zC = -l_10_R;
                        }
                        File file = new File(this.zz, this.zB);
                        if (file.exists()) {
                            this.zE = file.length();
                            this.zy.setHeader("RANGE", "bytes=" + this.zE + "-");
                            this.zG = true;
                        }
                        httpResponse = httpClient.execute(this.zy);
                        int statusCode = httpResponse.getStatusLine().getStatusCode();
                        f.f("HttpBase", "statusCode == " + statusCode);
                        if (statusCode != SmsCheckResult.ESCT_200 && statusCode != SmsCheckResult.ESCT_206) {
                            i = -3000 - statusCode;
                        } else if (!this.mIsCanceled) {
                            HttpEntity entity = httpResponse.getEntity();
                            if (entity != null) {
                                i = a(entity, bundle, z);
                                if (i == 0) {
                                    if (aVar != null) {
                                        if (!aVar.bS(this.zz + File.separator + this.zB)) {
                                            i = ErrorCode.ERR_FILE_OP;
                                            new File(this.zz + File.separator + this.zB).delete();
                                        }
                                    }
                                    i = v(true);
                                    if (i == 0) {
                                        i = 0;
                                    }
                                }
                            } else {
                                i = ErrorCode.ERR_RESPONSE;
                                f.e("HttpBase", "httpEntity == null");
                            }
                        }
                    } else {
                        i = -3053;
                        f.e("HttpBase", "url == null");
                    }
                }
                i = -3003;
            }
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse != null) {
            }
            if (!(i == 0 || i == -7)) {
                bundle.putInt("key_errcode", i);
                bundle.putString("key_errorMsg", str2);
                bundle.putInt("key_downSize", (int) this.zE);
                bundle.putInt("key_total", (int) this.zF);
                bundle.putInt("key_sdcardstatus", lu.t(this.zF - this.zE));
                bundle.putByte("key_downType", (byte) (!this.zG ? 0 : 1));
                a(1, bundle);
            }
        } catch (ClientProtocolException e) {
            i = -3051;
            str2 = e.getMessage();
            f.e("HttpBase", "protocol error:" + e.getMessage());
            e.printStackTrace();
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse != null) {
            }
            bundle.putInt("key_errcode", -3051);
            bundle.putString("key_errorMsg", str2);
            bundle.putInt("key_downSize", (int) this.zE);
            bundle.putInt("key_total", (int) this.zF);
            bundle.putInt("key_sdcardstatus", lu.t(this.zF - this.zE));
            bundle.putByte("key_downType", (byte) (!this.zG ? 0 : 1));
            a(1, bundle);
        } catch (SocketException e2) {
            i = -3054;
            str2 = e2.getMessage();
            f.e("HttpBase", "socket error:" + e2.getMessage());
            e2.printStackTrace();
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse != null) {
            }
            bundle.putInt("key_errcode", -3054);
            bundle.putString("key_errorMsg", str2);
            bundle.putInt("key_downSize", (int) this.zE);
            bundle.putInt("key_total", (int) this.zF);
            bundle.putInt("key_sdcardstatus", lu.t(this.zF - this.zE));
            bundle.putByte("key_downType", (byte) (!this.zG ? 0 : 1));
            a(1, bundle);
        } catch (SocketTimeoutException e3) {
            i = -3055;
            str2 = e3.getMessage();
            f.e("HttpBase", "socket timeout error:" + e3.getMessage());
            e3.printStackTrace();
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse != null) {
            }
            bundle.putInt("key_errcode", -3055);
            bundle.putString("key_errorMsg", str2);
            bundle.putInt("key_downSize", (int) this.zE);
            bundle.putInt("key_total", (int) this.zF);
            bundle.putInt("key_sdcardstatus", lu.t(this.zF - this.zE));
            bundle.putByte("key_downType", (byte) (!this.zG ? 0 : 1));
            a(1, bundle);
        } catch (IOException e4) {
            i = -3056;
            str2 = e4.getMessage();
            f.e("HttpBase", "io error:" + e4.getMessage());
            e4.printStackTrace();
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse != null) {
            }
            bundle.putInt("key_errcode", -3056);
            bundle.putString("key_errorMsg", str2);
            bundle.putInt("key_downSize", (int) this.zE);
            bundle.putInt("key_total", (int) this.zF);
            bundle.putInt("key_sdcardstatus", lu.t(this.zF - this.zE));
            bundle.putByte("key_downType", (byte) (!this.zG ? 0 : 1));
            a(1, bundle);
        } catch (NetWorkException e5) {
            i = e5.getErrCode();
            str2 = e5.getMessage();
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse != null) {
            }
            if (!(i == 0 || i == -7)) {
                bundle.putInt("key_errcode", i);
                bundle.putString("key_errorMsg", str2);
                bundle.putInt("key_downSize", (int) this.zE);
                bundle.putInt("key_total", (int) this.zF);
                bundle.putInt("key_sdcardstatus", lu.t(this.zF - this.zE));
                bundle.putByte("key_downType", (byte) (!this.zG ? 0 : 1));
                a(1, bundle);
            }
        } catch (Exception e6) {
            i = ErrorCode.ERR_GET;
            str2 = e6.getMessage();
            f.e("HttpBase", "get error:" + e6.getMessage());
            e6.printStackTrace();
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse != null) {
            }
            bundle.putInt("key_errcode", ErrorCode.ERR_GET);
            bundle.putString("key_errorMsg", str2);
            bundle.putInt("key_downSize", (int) this.zE);
            bundle.putInt("key_total", (int) this.zF);
            bundle.putInt("key_sdcardstatus", lu.t(this.zF - this.zE));
            bundle.putByte("key_downType", (byte) (!this.zG ? 0 : 1));
            a(1, bundle);
        } catch (Throwable th) {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse != null) {
            }
            if (!(i == 0 || i == -7)) {
                bundle.putInt("key_errcode", i);
                bundle.putString("key_errorMsg", str2);
                bundle.putInt("key_downSize", (int) this.zE);
                bundle.putInt("key_total", (int) this.zF);
                bundle.putInt("key_sdcardstatus", lu.t(this.zF - this.zE));
                bundle.putByte("key_downType", (byte) (!this.zG ? 0 : 1));
                a(1, bundle);
            }
        }
        return i;
    }

    public void bP(String str) {
        this.zA = str;
    }

    public void bQ(String str) {
        this.zC = str;
    }
}
