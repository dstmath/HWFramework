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
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;

/* compiled from: Unknown */
public class mv extends mt {
    private HttpGet BN;
    private String BO;
    private String BP;
    private String BQ;
    private String BR;
    private int BS;
    private long BT;
    private boolean BU;
    private Context mContext;
    private boolean mIsCanceled;
    private long mTotalSize;

    /* compiled from: Unknown */
    public interface a {
        boolean cE(String str);
    }

    public mv(Context context) {
        this.BN = null;
        this.BO = null;
        this.BP = null;
        this.BQ = null;
        this.BR = null;
        this.BS = 0;
        this.mIsCanceled = false;
        this.BT = 0;
        this.mTotalSize = 0;
        this.BU = false;
        if (!f.iA() && Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
            throw new NetworkOnMainThreadException();
        }
        this.mContext = context;
        this.BO = context.getCacheDir().getAbsolutePath();
        this.BP = context.getFilesDir().getAbsolutePath();
        this.BN = new HttpGet();
        if (f.iw() == cz.gE) {
            g(Proxy.getDefaultHost(), Proxy.getDefaultPort());
            E(true);
        }
    }

    private int F(boolean z) throws NetWorkException {
        File file;
        FileNotFoundException e;
        Object obj;
        FileInputStream fileInputStream;
        IOException e2;
        Exception e3;
        Throwable th;
        FileInputStream fileInputStream2;
        FileInputStream fileInputStream3 = -7001;
        FileInputStream fileInputStream4 = null;
        d.d("HttpGetFile", this.BO + File.separator + this.BQ);
        FileOutputStream fileOutputStream = "HttpGetFile";
        File file2 = this.BP + File.separator + this.BR;
        d.d(fileOutputStream, file2);
        try {
            file2 = new File(this.BO, this.BQ);
            try {
                FileOutputStream fileOutputStream2;
                int i;
                int i2;
                if (file2.exists()) {
                    byte[] bArr;
                    int read;
                    if (this.BS == 1) {
                        if (this.mContext.getFilesDir().getAbsolutePath().equals(this.BP)) {
                            fileOutputStream = this.mContext.openFileOutput(this.BR, 1);
                            fileInputStream3 = new FileInputStream(file2);
                            bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
                            while (true) {
                                read = fileInputStream3.read(bArr);
                                if (read != -1) {
                                    break;
                                }
                                fileOutputStream.write(bArr, 0, read);
                            }
                            fileInputStream4 = fileInputStream3;
                            fileOutputStream2 = fileOutputStream;
                            i = 0;
                        }
                    }
                    file = new File(this.BP + File.separator + this.BR);
                    if (file.exists()) {
                        file.delete();
                        fileOutputStream = new FileOutputStream(file);
                    } else {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                        fileOutputStream = new FileOutputStream(file);
                    }
                    try {
                        fileInputStream3 = new FileInputStream(file2);
                        try {
                            bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
                            while (true) {
                                read = fileInputStream3.read(bArr);
                                if (read != -1) {
                                    fileOutputStream.write(bArr, 0, read);
                                } else {
                                    break;
                                    fileInputStream4 = fileInputStream3;
                                    fileOutputStream2 = fileOutputStream;
                                    i = 0;
                                }
                            }
                        } catch (FileNotFoundException e4) {
                            e = e4;
                            obj = fileOutputStream;
                            fileInputStream = fileInputStream3;
                            file = file2;
                        } catch (IOException e5) {
                            e2 = e5;
                        } catch (Exception e6) {
                            e3 = e6;
                        }
                    } catch (FileNotFoundException e7) {
                        obj = fileOutputStream;
                        fileInputStream = null;
                        e = e7;
                        file = file2;
                        try {
                            d.c("HttpBase", "file not found");
                            e.printStackTrace();
                            throw new NetWorkException(-7001, e.getMessage());
                        } catch (Throwable th2) {
                            th = th2;
                            file2 = file;
                            fileInputStream3 = fileInputStream;
                            fileOutputStream = fileInputStream2;
                            if (fileInputStream3 != null) {
                                try {
                                    fileInputStream3.close();
                                } catch (IOException e8) {
                                    d.c("HttpBase", "fis close file error");
                                    e8.printStackTrace();
                                }
                            }
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e82) {
                                    d.c("HttpBase", "fosclose file error");
                                    e82.printStackTrace();
                                }
                            }
                            if (z && file2 != null && file2.exists()) {
                                file2.delete();
                            }
                            throw th;
                        }
                    } catch (IOException e822) {
                        IOException iOException = e822;
                        fileInputStream3 = null;
                        e2 = iOException;
                        d.c("HttpBase", "file io error");
                        e2.printStackTrace();
                        throw new NetWorkException(-7056, e2.getMessage());
                    } catch (Exception e9) {
                        Exception exception = e9;
                        fileInputStream3 = null;
                        e3 = exception;
                        d.c("HttpBase", "file op error");
                        e3.printStackTrace();
                        throw new NetWorkException((int) ErrorCode.ERR_FILE_OP, e3.getMessage());
                    } catch (Throwable th3) {
                        Throwable th4 = th3;
                        fileInputStream3 = null;
                        th = th4;
                        if (fileInputStream3 != null) {
                            fileInputStream3.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        file2.delete();
                        throw th;
                    }
                }
                i = -7001;
                fileOutputStream2 = null;
                if (fileInputStream4 != null) {
                    try {
                        fileInputStream4.close();
                    } catch (IOException e22) {
                        d.c("HttpBase", "fis close file error");
                        e22.printStackTrace();
                        i2 = ErrorCode.ERR_FILE_OP;
                    }
                }
                i2 = i;
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (IOException e222) {
                        d.c("HttpBase", "fosclose file error");
                        e222.printStackTrace();
                        i2 = ErrorCode.ERR_FILE_OP;
                    }
                }
                if (z && file2 != null && file2.exists()) {
                    file2.delete();
                }
                return i2;
            } catch (FileNotFoundException e72) {
                fileInputStream = null;
                fileInputStream2 = null;
                e = e72;
                file = file2;
                d.c("HttpBase", "file not found");
                e.printStackTrace();
                throw new NetWorkException(-7001, e.getMessage());
            } catch (IOException e8222) {
                Object obj2 = null;
                e222 = e8222;
                fileInputStream3 = null;
                d.c("HttpBase", "file io error");
                e222.printStackTrace();
                throw new NetWorkException(-7056, e222.getMessage());
            } catch (Exception e92) {
                fileInputStream = null;
                e3 = e92;
                fileInputStream3 = null;
                d.c("HttpBase", "file op error");
                e3.printStackTrace();
                throw new NetWorkException((int) ErrorCode.ERR_FILE_OP, e3.getMessage());
            } catch (Throwable th32) {
                fileOutputStream = null;
                th = th32;
                fileInputStream3 = null;
                if (fileInputStream3 != null) {
                    fileInputStream3.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                file2.delete();
                throw th;
            }
        } catch (FileNotFoundException e722) {
            fileInputStream = null;
            fileInputStream2 = null;
            FileNotFoundException fileNotFoundException = e722;
            Object obj3 = null;
            e = fileNotFoundException;
            d.c("HttpBase", "file not found");
            e.printStackTrace();
            throw new NetWorkException(-7001, e.getMessage());
        } catch (IOException e82222) {
            file2 = null;
            fileOutputStream = null;
            e222 = e82222;
            fileInputStream3 = null;
            d.c("HttpBase", "file io error");
            e222.printStackTrace();
            throw new NetWorkException(-7056, e222.getMessage());
        } catch (Exception e922) {
            FileInputStream fileInputStream5 = null;
            fileInputStream = null;
            e3 = e922;
            fileInputStream3 = null;
            d.c("HttpBase", "file op error");
            e3.printStackTrace();
            throw new NetWorkException((int) ErrorCode.ERR_FILE_OP, e3.getMessage());
        } catch (Throwable th5) {
            th = th5;
            if (fileInputStream3 != null) {
                fileInputStream3.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            file2.delete();
            throw th;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int a(HttpEntity httpEntity, Bundle bundle, boolean z) throws NetWorkException {
        FileOutputStream fileOutputStream;
        InputStream inflaterInputStream;
        FileNotFoundException e;
        Throwable th;
        SocketException e2;
        SocketTimeoutException e3;
        IOException e4;
        Exception e5;
        Object obj;
        FileOutputStream fileOutputStream2 = null;
        InputStream inputStream = null;
        byte[] bArr = new byte[8192];
        try {
            this.mTotalSize = httpEntity.getContentLength() + this.BT;
            int i = (int) ((this.BT * 100) / this.mTotalSize);
            File file = new File(this.BO, this.BQ);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file, true);
            if (z) {
                inflaterInputStream = new InflaterInputStream(httpEntity.getContent());
            } else {
                try {
                    inflaterInputStream = httpEntity.getContent();
                } catch (FileNotFoundException e6) {
                    e = e6;
                    fileOutputStream2 = fileOutputStream;
                    try {
                        d.c("HttpBase", "file not found");
                        e.printStackTrace();
                        throw new NetWorkException(-7001, e.getMessage());
                    } catch (Throwable th2) {
                        th = th2;
                        fileOutputStream = fileOutputStream2;
                        if (inputStream != null) {
                            d.d("HttpBase", "is closing file");
                            try {
                                inputStream.close();
                            } catch (IOException e7) {
                                d.c("HttpBase", "is close file error");
                                e7.printStackTrace();
                            }
                        }
                        if (fileOutputStream != null) {
                            d.d("HttpBase", "fos closing file");
                            try {
                                fileOutputStream.close();
                            } catch (IOException e72) {
                                d.c("HttpBase", "fos close file error");
                                e72.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (SocketException e8) {
                    e2 = e8;
                    d.c("HttpBase", "socket error:" + e2.getMessage());
                    e2.printStackTrace();
                    throw new NetWorkException(-5054, e2.getMessage());
                } catch (SocketTimeoutException e9) {
                    e3 = e9;
                    d.c("HttpBase", "socket timeout error:" + e3.getMessage());
                    e3.printStackTrace();
                    throw new NetWorkException(-5055, e3.getMessage());
                } catch (IOException e10) {
                    e4 = e10;
                    d.c("HttpBase", "socket or file io error");
                    e4.printStackTrace();
                    throw new NetWorkException(-5056, e4.getMessage());
                } catch (Exception e11) {
                    e5 = e11;
                    d.c("HttpBase", e5.toString());
                    d.c("HttpBase", "receive data error");
                    e5.printStackTrace();
                    throw new NetWorkException((int) ErrorCode.ERR_RECEIVE, e5.getMessage());
                }
            }
            int i2 = 0;
            int i3 = i;
            while (true) {
                try {
                    int read = inflaterInputStream.read(bArr);
                    if (read != -1) {
                        if (this.mIsCanceled) {
                            break;
                        }
                        this.BT += (long) read;
                        i = i2 + read;
                        i2 = (int) ((this.BT * 100) / this.mTotalSize);
                        if (i2 != i3) {
                            bundle.putInt("key_progress", i2);
                            a(2, bundle);
                            i3 = i2;
                        }
                        fileOutputStream.write(bArr, 0, read);
                        i2 = i;
                    } else {
                        break;
                    }
                } catch (FileNotFoundException e12) {
                    fileOutputStream2 = fileOutputStream;
                    InputStream inputStream2 = inflaterInputStream;
                    e = e12;
                    inputStream = inputStream2;
                } catch (SocketException e13) {
                    SocketException socketException = e13;
                    inputStream = inflaterInputStream;
                    e2 = socketException;
                } catch (SocketTimeoutException e14) {
                    SocketTimeoutException socketTimeoutException = e14;
                    inputStream = inflaterInputStream;
                    e3 = socketTimeoutException;
                } catch (IOException e722) {
                    IOException iOException = e722;
                    inputStream = inflaterInputStream;
                    e4 = iOException;
                } catch (Exception e15) {
                    Exception exception = e15;
                    inputStream = inflaterInputStream;
                    e5 = exception;
                } catch (Throwable th3) {
                    Throwable th4 = th3;
                    inputStream = inflaterInputStream;
                    th = th4;
                }
            }
            fileOutputStream.flush();
            d.d("HttpBase", "mTotalSize: " + this.mTotalSize + ", mCompletedSize: " + this.BT + ", httpEntity.getContentLength(): " + httpEntity.getContentLength());
            i3 = ((long) i2) == httpEntity.getContentLength() ? 0 : -7;
            if (inflaterInputStream != null) {
                d.d("HttpBase", "is closing file");
                try {
                    inflaterInputStream.close();
                } catch (IOException e42) {
                    i3 = ErrorCode.ERR_FILE_OP;
                    d.c("HttpBase", "is close file error");
                    e42.printStackTrace();
                }
            }
            int i4 = i3;
            if (fileOutputStream != null) {
                d.d("HttpBase", "fos closing file");
                try {
                    fileOutputStream.close();
                } catch (IOException e7222) {
                    i4 = ErrorCode.ERR_FILE_OP;
                    d.c("HttpBase", "fos close file error");
                    e7222.printStackTrace();
                }
            }
            return i4;
        } catch (FileNotFoundException e16) {
            e = e16;
            d.c("HttpBase", "file not found");
            e.printStackTrace();
            throw new NetWorkException(-7001, e.getMessage());
        } catch (SocketException e17) {
            e2 = e17;
            fileOutputStream = null;
            d.c("HttpBase", "socket error:" + e2.getMessage());
            e2.printStackTrace();
            throw new NetWorkException(-5054, e2.getMessage());
        } catch (SocketTimeoutException e18) {
            e3 = e18;
            obj = null;
            d.c("HttpBase", "socket timeout error:" + e3.getMessage());
            e3.printStackTrace();
            throw new NetWorkException(-5055, e3.getMessage());
        } catch (IOException e19) {
            e42 = e19;
            obj = null;
            d.c("HttpBase", "socket or file io error");
            e42.printStackTrace();
            throw new NetWorkException(-5056, e42.getMessage());
        } catch (Exception e20) {
            e5 = e20;
            obj = null;
            d.c("HttpBase", e5.toString());
            d.c("HttpBase", "receive data error");
            e5.printStackTrace();
            throw new NetWorkException((int) ErrorCode.ERR_RECEIVE, e5.getMessage());
        } catch (Throwable th5) {
            th = th5;
            if (inputStream != null) {
                d.d("HttpBase", "is closing file");
                inputStream.close();
            }
            if (fileOutputStream != null) {
                d.d("HttpBase", "fos closing file");
                fileOutputStream.close();
            }
            throw th;
        }
    }

    private int cD(String str) throws NetWorkException {
        try {
            URI uri = new URI(str);
            if (uri == null) {
                return ErrorCode.ERR_OPEN_CONNECTION;
            }
            this.BN.setURI(uri);
            return 0;
        } catch (URISyntaxException e) {
            d.c("HttpBase", "url error: " + e.getMessage());
            e.printStackTrace();
            throw new NetWorkException(-1053, e.getMessage());
        }
    }

    public int a(String str, String str2, boolean z, a aVar) {
        String str3;
        String message;
        Throwable th;
        int i;
        int i2 = ErrorCode.ERR_GET;
        String str4 = "";
        HttpClient httpClient = null;
        HttpResponse httpResponse = null;
        Bundle bundle = new Bundle();
        int i3;
        try {
            httpClient = eZ();
            i2 = cD(str2);
            if (i2 == 0) {
                if (!this.mIsCanceled) {
                    if (this.BN.getURI() != null) {
                        str3 = "downloadfile";
                        if (TextUtils.isEmpty(str)) {
                            str = ms.q(str2, null);
                        }
                        this.BQ = str + ".tmp";
                        d.d("HttpBase", "mTempName: " + this.BQ);
                        if (this.BR == null) {
                            this.BR = str;
                        }
                        File file = new File(this.BO, this.BQ);
                        if (file.exists()) {
                            this.BT = file.length();
                            this.BN.setHeader("RANGE", "bytes=" + this.BT + "-");
                            this.BU = true;
                        }
                        httpResponse = httpClient.execute(this.BN);
                        int statusCode = httpResponse.getStatusLine().getStatusCode();
                        d.d("HttpBase", "statusCode == " + statusCode);
                        if (statusCode != SmsCheckResult.ESCT_200 && statusCode != SmsCheckResult.ESCT_206) {
                            i3 = -3000 - statusCode;
                            if (httpClient != null) {
                                httpClient.getConnectionManager().shutdown();
                            }
                            if (httpResponse != null) {
                                if (i3 == 0 || i3 == -7) {
                                    return i3;
                                }
                                bundle.putInt("key_errcode", i3);
                                bundle.putString("key_errorMsg", str4);
                                bundle.putInt("key_downSize", (int) this.BT);
                                bundle.putInt("key_total", (int) this.mTotalSize);
                                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
                                bundle.putByte("key_downType", (byte) (this.BU ? 0 : 1));
                                a(1, bundle);
                                return i3;
                            } else if (i3 == 0) {
                                return i3;
                            } else {
                                bundle.putInt("key_errcode", i3);
                                bundle.putString("key_errorMsg", str4);
                                bundle.putInt("key_downSize", (int) this.BT);
                                bundle.putInt("key_total", (int) this.mTotalSize);
                                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
                                if (this.BU) {
                                }
                                bundle.putByte("key_downType", (byte) (this.BU ? 0 : 1));
                                a(1, bundle);
                                return i3;
                            }
                        } else if (!this.mIsCanceled) {
                            HttpEntity entity = httpResponse.getEntity();
                            if (entity != null) {
                                i2 = a(entity, bundle, z);
                                if (i2 == 0) {
                                    if (aVar != null) {
                                        if (!aVar.cE(this.BO + File.separator + this.BQ)) {
                                            i2 = ErrorCode.ERR_FILE_OP;
                                            new File(this.BO + File.separator + this.BQ).delete();
                                        }
                                    }
                                    i2 = F(true);
                                    if (i2 == 0) {
                                        i3 = 0;
                                        if (httpClient != null) {
                                            httpClient.getConnectionManager().shutdown();
                                        }
                                        if (httpResponse != null) {
                                            if (i3 == 0) {
                                                return i3;
                                            }
                                            bundle.putInt("key_errcode", i3);
                                            bundle.putString("key_errorMsg", str4);
                                            bundle.putInt("key_downSize", (int) this.BT);
                                            bundle.putInt("key_total", (int) this.mTotalSize);
                                            bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
                                            if (this.BU) {
                                            }
                                            bundle.putByte("key_downType", (byte) (this.BU ? 0 : 1));
                                            a(1, bundle);
                                            return i3;
                                        } else if (i3 == 0) {
                                            return i3;
                                        } else {
                                            bundle.putInt("key_errcode", i3);
                                            bundle.putString("key_errorMsg", str4);
                                            bundle.putInt("key_downSize", (int) this.BT);
                                            bundle.putInt("key_total", (int) this.mTotalSize);
                                            bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
                                            if (this.BU) {
                                            }
                                            bundle.putByte("key_downType", (byte) (this.BU ? 0 : 1));
                                            a(1, bundle);
                                            return i3;
                                        }
                                    }
                                } else if (i2 != -7) {
                                }
                            } else {
                                i2 = ErrorCode.ERR_RESPONSE;
                                d.c("HttpBase", "httpEntity == null");
                            }
                        }
                    } else {
                        i2 = -3053;
                        d.c("HttpBase", "url == null");
                    }
                }
                i3 = -3003;
                if (httpClient != null) {
                    httpClient.getConnectionManager().shutdown();
                }
                if (httpResponse != null) {
                    if (i3 == 0) {
                        return i3;
                    }
                    bundle.putInt("key_errcode", i3);
                    bundle.putString("key_errorMsg", str4);
                    bundle.putInt("key_downSize", (int) this.BT);
                    bundle.putInt("key_total", (int) this.mTotalSize);
                    bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
                    if (this.BU) {
                    }
                    bundle.putByte("key_downType", (byte) (this.BU ? 0 : 1));
                    a(1, bundle);
                    return i3;
                } else if (i3 == 0) {
                    return i3;
                } else {
                    bundle.putInt("key_errcode", i3);
                    bundle.putString("key_errorMsg", str4);
                    bundle.putInt("key_downSize", (int) this.BT);
                    bundle.putInt("key_total", (int) this.mTotalSize);
                    bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
                    if (this.BU) {
                    }
                    bundle.putByte("key_downType", (byte) (this.BU ? 0 : 1));
                    a(1, bundle);
                    return i3;
                }
            }
            i3 = i2;
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse != null) {
                if (i3 == 0) {
                    return i3;
                }
                bundle.putInt("key_errcode", i3);
                bundle.putString("key_errorMsg", str4);
                bundle.putInt("key_downSize", (int) this.BT);
                bundle.putInt("key_total", (int) this.mTotalSize);
                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
                if (this.BU) {
                }
                bundle.putByte("key_downType", (byte) (this.BU ? 0 : 1));
                a(1, bundle);
                return i3;
            } else if (i3 == 0) {
                return i3;
            } else {
                bundle.putInt("key_errcode", i3);
                bundle.putString("key_errorMsg", str4);
                bundle.putInt("key_downSize", (int) this.BT);
                bundle.putInt("key_total", (int) this.mTotalSize);
                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
                if (this.BU) {
                }
                bundle.putByte("key_downType", (byte) (this.BU ? 0 : 1));
                a(1, bundle);
                return i3;
            }
        } catch (ClientProtocolException e) {
            i3 = -3051;
            message = e.getMessage();
            d.c("HttpBase", "protocol error:" + e.getMessage());
            e.printStackTrace();
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse == null) {
                bundle.putInt("key_errcode", -3051);
                bundle.putString("key_errorMsg", message);
                bundle.putInt("key_downSize", (int) this.BT);
                bundle.putInt("key_total", (int) this.mTotalSize);
                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
            } else {
                bundle.putInt("key_errcode", -3051);
                bundle.putString("key_errorMsg", message);
                bundle.putInt("key_downSize", (int) this.BT);
                bundle.putInt("key_total", (int) this.mTotalSize);
                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
            }
            bundle.putByte("key_downType", (byte) (!this.BU ? 0 : 1));
            a(1, bundle);
            return -3051;
        } catch (SocketException e2) {
            i3 = -3054;
            message = e2.getMessage();
            d.c("HttpBase", "socket error:" + e2.getMessage());
            e2.printStackTrace();
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse == null) {
                bundle.putInt("key_errcode", -3054);
                bundle.putString("key_errorMsg", message);
                bundle.putInt("key_downSize", (int) this.BT);
                bundle.putInt("key_total", (int) this.mTotalSize);
                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
            } else {
                bundle.putInt("key_errcode", -3054);
                bundle.putString("key_errorMsg", message);
                bundle.putInt("key_downSize", (int) this.BT);
                bundle.putInt("key_total", (int) this.mTotalSize);
                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
            }
            bundle.putByte("key_downType", (byte) (!this.BU ? 0 : 1));
            a(1, bundle);
            return -3054;
        } catch (SocketTimeoutException e3) {
            i3 = -3055;
            message = e3.getMessage();
            d.c("HttpBase", "socket timeout error:" + e3.getMessage());
            e3.printStackTrace();
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse == null) {
                bundle.putInt("key_errcode", -3055);
                bundle.putString("key_errorMsg", message);
                bundle.putInt("key_downSize", (int) this.BT);
                bundle.putInt("key_total", (int) this.mTotalSize);
                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
            } else {
                bundle.putInt("key_errcode", -3055);
                bundle.putString("key_errorMsg", message);
                bundle.putInt("key_downSize", (int) this.BT);
                bundle.putInt("key_total", (int) this.mTotalSize);
                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
            }
            bundle.putByte("key_downType", (byte) (!this.BU ? 0 : 1));
            a(1, bundle);
            return -3055;
        } catch (IOException e4) {
            i3 = -3056;
            message = e4.getMessage();
            d.c("HttpBase", "io error:" + e4.getMessage());
            e4.printStackTrace();
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse == null) {
                bundle.putInt("key_errcode", -3056);
                bundle.putString("key_errorMsg", message);
                bundle.putInt("key_downSize", (int) this.BT);
                bundle.putInt("key_total", (int) this.mTotalSize);
                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
            } else {
                bundle.putInt("key_errcode", -3056);
                bundle.putString("key_errorMsg", message);
                bundle.putInt("key_downSize", (int) this.BT);
                bundle.putInt("key_total", (int) this.mTotalSize);
                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
            }
            bundle.putByte("key_downType", (byte) (!this.BU ? 0 : 1));
            a(1, bundle);
            return -3056;
        } catch (NetWorkException e5) {
            i2 = e5.getErrCode();
            str3 = e5.getMessage();
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse == null) {
                if (!(i2 == 0 || i2 == -7)) {
                    bundle.putInt("key_errcode", i2);
                    bundle.putString("key_errorMsg", str3);
                    bundle.putInt("key_downSize", (int) this.BT);
                    bundle.putInt("key_total", (int) this.mTotalSize);
                    bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
                    bundle.putByte("key_downType", (byte) (this.BU ? 0 : 1));
                    a(1, bundle);
                }
                return i2;
            }
            bundle.putInt("key_errcode", i2);
            bundle.putString("key_errorMsg", str3);
            bundle.putInt("key_downSize", (int) this.BT);
            bundle.putInt("key_total", (int) this.mTotalSize);
            bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
            if (this.BU) {
            }
            bundle.putByte("key_downType", (byte) (this.BU ? 0 : 1));
            a(1, bundle);
            return i2;
        } catch (Exception e6) {
            i3 = ErrorCode.ERR_GET;
            message = e6.getMessage();
            d.c("HttpBase", "get error:" + e6.getMessage());
            e6.printStackTrace();
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse == null) {
                bundle.putInt("key_errcode", ErrorCode.ERR_GET);
                bundle.putString("key_errorMsg", message);
                bundle.putInt("key_downSize", (int) this.BT);
                bundle.putInt("key_total", (int) this.mTotalSize);
                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
            } else {
                bundle.putInt("key_errcode", ErrorCode.ERR_GET);
                bundle.putString("key_errorMsg", message);
                bundle.putInt("key_downSize", (int) this.BT);
                bundle.putInt("key_total", (int) this.mTotalSize);
                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
            }
            bundle.putByte("key_downType", (byte) (!this.BU ? 0 : 1));
            a(1, bundle);
            return ErrorCode.ERR_GET;
        } catch (Throwable th2) {
            th = th2;
            String str5 = str4;
            i = i2;
            message = str5;
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
            if (httpResponse == null) {
                bundle.putInt("key_errcode", i);
                bundle.putString("key_errorMsg", message);
                bundle.putInt("key_downSize", (int) this.BT);
                bundle.putInt("key_total", (int) this.mTotalSize);
                bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
                if (this.BU) {
                }
                bundle.putByte("key_downType", (byte) (this.BU ? 0 : 1));
                a(1, bundle);
                throw th;
            }
            bundle.putInt("key_errcode", i);
            bundle.putString("key_errorMsg", message);
            bundle.putInt("key_downSize", (int) this.BT);
            bundle.putInt("key_total", (int) this.mTotalSize);
            bundle.putInt("key_sdcardstatus", ms.p(this.mTotalSize - this.BT));
            if (this.BU) {
            }
            bundle.putByte("key_downType", (byte) (this.BU ? 0 : 1));
            a(1, bundle);
            throw th;
        }
    }

    public void cB(String str) {
        this.BP = str;
    }

    public void cC(String str) {
        this.BR = str;
    }
}
