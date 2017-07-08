package tmsdkobf;

import android.content.Context;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.ErrorCode;
import tmsdk.common.exception.NetWorkException;
import tmsdk.common.tcc.TccCryptor;

/* compiled from: Unknown */
public class mw {
    private Context mContext;
    private String mUrl;

    public mw(Context context) {
        this.mUrl = "http://uploadserver.3g.qq.com/upload_v3";
        this.mContext = context;
    }

    private byte[] a(String str, long j, int i, int i2) {
        fa faVar = new fa();
        faVar.W(str);
        faVar.a(j);
        faVar.Y(i);
        faVar.X("MD5");
        faVar.Z(i2);
        faVar.aa(0);
        fj fjVar = new fj();
        fjVar.Z("UTF-8");
        fjVar.n();
        fjVar.put("key_UploadPacketInfoReq", faVar);
        return fjVar.m();
    }

    private byte[] a(String str, long j, int i, byte[] bArr) {
        if (str == null || bArr == null || bArr.length == 0) {
            return null;
        }
        Object n = nb.n(bArr);
        Object a = a(str, j, i, n.length);
        if (a == null) {
            return null;
        }
        int length = a.length;
        Object obj = new byte[(((length + 4) + bArr.length) + n.length)];
        System.arraycopy(nb.bG(a.length), 0, obj, 0, 4);
        System.arraycopy(a, 0, obj, 4, length);
        System.arraycopy(bArr, 0, obj, length + 4, bArr.length);
        System.arraycopy(n, 0, obj, (length + 4) + bArr.length, n.length);
        return obj;
    }

    private int l(byte[] bArr) {
        if (bArr == null) {
            return ErrorCode.ERR_RESPONSE;
        }
        fb m = m(bArr);
        return m != null ? m.j() : ErrorCode.ERR_RESPONSE;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int a(String str, er erVar) {
        NetWorkException e;
        Exception e2;
        mu muVar = null;
        int i = -2;
        File file = new File(str);
        if (!file.exists()) {
            return -1;
        }
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            byte[] bArr = new byte[20480];
            if (erVar != null) {
                try {
                    if (erVar.g() != null) {
                        Iterator it = erVar.g().iterator();
                        mu muVar2 = muVar;
                        int i2 = i;
                        while (it.hasNext()) {
                            try {
                                eq eqVar = (eq) it.next();
                                long pos = (long) eqVar.getPos();
                                int size = eqVar.getSize();
                                randomAccessFile.seek(pos);
                                if (size != bArr.length) {
                                    bArr = new byte[size];
                                }
                                int read = randomAccessFile.read(bArr, 0, size);
                                if (read != -1 && read == size) {
                                    byte[] a = a(erVar.getId(), pos, size, bArr);
                                    muVar = mu.cA(this.mUrl);
                                    try {
                                        muVar.setRequestMethod("POST");
                                        muVar.setPostData(a);
                                        muVar.fc();
                                        AtomicReference atomicReference = new AtomicReference();
                                        i = muVar.a(false, atomicReference);
                                        muVar.close();
                                        if (i == 0) {
                                            i2 = l((byte[]) atomicReference.get());
                                            if (i2 == 0) {
                                                muVar2 = muVar;
                                            } else {
                                                if (randomAccessFile != null) {
                                                    try {
                                                        randomAccessFile.close();
                                                    } catch (IOException e3) {
                                                        e3.printStackTrace();
                                                    }
                                                }
                                                if (muVar != null) {
                                                    muVar.close();
                                                }
                                                return i2;
                                            }
                                        }
                                        if (randomAccessFile != null) {
                                            try {
                                                randomAccessFile.close();
                                            } catch (IOException e32) {
                                                e32.printStackTrace();
                                            }
                                        }
                                        if (muVar != null) {
                                            muVar.close();
                                        }
                                        return i;
                                    } catch (NetWorkException e4) {
                                        e = e4;
                                    } catch (Exception e5) {
                                        e2 = e5;
                                        i = i2;
                                    }
                                } else {
                                    if (randomAccessFile != null) {
                                        try {
                                            randomAccessFile.close();
                                        } catch (IOException e322) {
                                            e322.printStackTrace();
                                        }
                                    }
                                    if (muVar2 != null) {
                                        muVar2.close();
                                    }
                                    return -2060;
                                }
                            } catch (NetWorkException e6) {
                                e = e6;
                                muVar = muVar2;
                            } catch (Exception e7) {
                                e2 = e7;
                                muVar = muVar2;
                                i = i2;
                            } catch (Throwable th) {
                                Throwable th2 = th;
                                muVar = muVar2;
                            }
                        }
                        muVar = muVar2;
                    }
                } catch (NetWorkException e42) {
                    e = e42;
                } catch (Exception e8) {
                    e2 = e8;
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
            if (muVar != null) {
                muVar.close();
            }
            return 0;
        } catch (FileNotFoundException e9) {
            return -1;
        }
    }

    public fb m(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        fb fbVar;
        fj fjVar = new fj();
        fjVar.Z("UTF-8");
        fjVar.n();
        try {
            fjVar.b(TccCryptor.decrypt(bArr, null));
            fbVar = (fb) fjVar.a("key_UploadPacketInfoResp", (Object) new fb());
        } catch (Exception e) {
            e.printStackTrace();
            fbVar = null;
        }
        return fbVar;
    }

    public void setUrl(String str) {
        this.mUrl = str;
    }
}
