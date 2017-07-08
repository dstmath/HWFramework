package tmsdkobf;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class nj {
    public static <T> T a(Context context, String str, String str2, T t, String str3) {
        if (str == null || str2 == null) {
            return t;
        }
        T a;
        try {
            a = c(context, str, str3).a(str2, (Object) t);
        } catch (Exception e) {
            e.printStackTrace();
            d.c("deepclean", "head error::" + e.toString());
            a = t;
        }
        if (a != null) {
            t = a;
        }
        return t;
    }

    public static <T> T b(Context context, String str, String str2, T t) {
        return a(context, str, str2, t, null);
    }

    public static <T> T b(Context context, String str, String str2, T t, String str3) {
        if (str == null || str2 == null) {
            return t;
        }
        T a;
        try {
            a = b(context, str, str3).a(str2, (Object) t);
        } catch (Exception e) {
            e.printStackTrace();
            a = t;
        }
        if (a != null) {
            t = a;
        }
        return t;
    }

    private static fi b(Context context, String str, String str2) {
        FileInputStream fileInputStream;
        Exception e;
        Throwable th;
        fi fiVar = new fi();
        String a = ms.a(context, str, null);
        InputStream open;
        mq c;
        byte[] bArr;
        byte[] decrypt;
        if (a == null || a.length() == 0) {
            try {
                open = context.getResources().getAssets().open(str, 1);
                c = mr.c(open);
                bArr = new byte[open.available()];
                open.read(bArr);
                if (!mo.bytesToHexString(nb.n(bArr)).equals(mo.bytesToHexString(c.Bj))) {
                    return fiVar;
                }
                decrypt = TccCryptor.decrypt(bArr, null);
                if (str2 != null) {
                    if (str2.length() > 0) {
                        fiVar.Z(str2);
                    }
                }
                fiVar.b(decrypt);
                return fiVar;
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        } else {
            File file = new File(a);
            if (file.exists() && !file.isDirectory()) {
                try {
                    fileInputStream = new FileInputStream(file);
                    try {
                        mq c2 = mr.c(fileInputStream);
                        bArr = new byte[fileInputStream.available()];
                        fileInputStream.read(bArr);
                        if (mo.bytesToHexString(nb.n(bArr)).equals(mo.bytesToHexString(c2.Bj))) {
                            decrypt = TccCryptor.decrypt(bArr, null);
                            if (str2 != null) {
                                if (str2.length() > 0) {
                                    fiVar.Z(str2);
                                }
                            }
                            fiVar.b(decrypt);
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            }
                            return fiVar;
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                        }
                        return fiVar;
                    } catch (Exception e3) {
                        e = e3;
                        try {
                            e.printStackTrace();
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e2222) {
                                    e2222.printStackTrace();
                                }
                            }
                            return fiVar;
                        } catch (Throwable th2) {
                            th = th2;
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e4) {
                                    e4.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    }
                } catch (Exception e5) {
                    e = e5;
                    fileInputStream = null;
                    e.printStackTrace();
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return fiVar;
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = null;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            }
            try {
                open = context.getResources().getAssets().open(str, 1);
                c = mr.c(open);
                bArr = new byte[open.available()];
                open.read(bArr);
                if (!mo.bytesToHexString(nb.n(bArr)).equals(mo.bytesToHexString(c.Bj))) {
                    return fiVar;
                }
                decrypt = TccCryptor.decrypt(bArr, null);
                if (str2 != null) {
                    if (str2.length() > 0) {
                        fiVar.Z(str2);
                    }
                }
                fiVar.b(decrypt);
                return fiVar;
            } catch (IOException e22222) {
                e22222.printStackTrace();
            }
        }
    }

    private static fi c(Context context, String str, String str2) {
        FileInputStream fileInputStream;
        Exception e;
        Throwable th;
        fi fiVar = new fi();
        String a = ms.a(context, str, null);
        if (a == null || a.length() == 0) {
            return fiVar;
        }
        File file = new File(a);
        if (!file.exists()) {
            return fiVar;
        }
        try {
            fileInputStream = new FileInputStream(file);
            try {
                mq c = mr.c(fileInputStream);
                byte[] bArr = new byte[fileInputStream.available()];
                fileInputStream.read(bArr);
                if (mo.bytesToHexString(nb.n(bArr)).equals(mo.bytesToHexString(c.Bj))) {
                    byte[] decrypt = TccCryptor.decrypt(bArr, null);
                    if (str2 != null) {
                        if (str2.length() > 0) {
                            fiVar.Z(str2);
                        }
                    }
                    fiVar.b(decrypt);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    return fiVar;
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
                return fiVar;
            } catch (Exception e3) {
                e = e3;
                try {
                    e.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    return fiVar;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
        } catch (Exception e5) {
            e = e5;
            fileInputStream = null;
            e.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return fiVar;
        } catch (Throwable th3) {
            th = th3;
            fileInputStream = null;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
    }
}
