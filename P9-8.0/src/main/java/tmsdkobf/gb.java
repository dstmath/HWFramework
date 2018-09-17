package tmsdkobf;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import tmsdk.common.ErrorCode;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;

public class gb {
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00a0 A:{SYNTHETIC, Splitter: B:54:0x00a0} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x007c A:{SYNTHETIC, Splitter: B:38:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00b7 A:{SYNTHETIC, Splitter: B:65:0x00b7} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00ae A:{SYNTHETIC, Splitter: B:61:0x00ae} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x00c6 A:{SYNTHETIC, Splitter: B:73:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00cf A:{SYNTHETIC, Splitter: B:77:0x00cf} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0097 A:{SYNTHETIC, Splitter: B:50:0x0097} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0061 A:{SYNTHETIC, Splitter: B:28:0x0061} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00a0 A:{SYNTHETIC, Splitter: B:54:0x00a0} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x007c A:{SYNTHETIC, Splitter: B:38:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00b7 A:{SYNTHETIC, Splitter: B:65:0x00b7} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00ae A:{SYNTHETIC, Splitter: B:61:0x00ae} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x00c6 A:{SYNTHETIC, Splitter: B:73:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00cf A:{SYNTHETIC, Splitter: B:77:0x00cf} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Object a(Context context, String str, Object obj, String str2) {
        Throwable th;
        Object obj2 = null;
        if (str == null || str2 == null) {
            return null;
        }
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            FileInputStream fileInputStream2 = new FileInputStream(str2);
            try {
                ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
                try {
                    byte[] bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
                    while (true) {
                        int read = fileInputStream2.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        byteArrayOutputStream2.write(bArr, 0, read);
                    }
                    byte[] toByteArray = byteArrayOutputStream2.toByteArray();
                    fn fnVar = new fn();
                    fnVar.B("UTF-8");
                    fnVar.b(fz.b(toByteArray, fz.P()));
                    obj2 = fnVar.a(str, obj);
                    if (obj2 == null) {
                        mb.o("FileUtil", "wupObject is null");
                    } else {
                        mb.n("FileUtil", obj2.toString());
                    }
                    if (fileInputStream2 != null) {
                        try {
                            fileInputStream2.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (byteArrayOutputStream2 != null) {
                        try {
                            byteArrayOutputStream2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    byteArrayOutputStream = byteArrayOutputStream2;
                    fileInputStream = fileInputStream2;
                } catch (FileNotFoundException e3) {
                    byteArrayOutputStream = byteArrayOutputStream2;
                    fileInputStream = fileInputStream2;
                } catch (IOException e4) {
                    byteArrayOutputStream = byteArrayOutputStream2;
                    fileInputStream = fileInputStream2;
                    if (fileInputStream != null) {
                    }
                    if (byteArrayOutputStream != null) {
                    }
                    return obj2;
                } catch (Throwable th2) {
                    th = th2;
                    byteArrayOutputStream = byteArrayOutputStream2;
                    fileInputStream = fileInputStream2;
                    if (fileInputStream != null) {
                    }
                    if (byteArrayOutputStream != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                }
                if (byteArrayOutputStream != null) {
                }
                return obj2;
            } catch (IOException e6) {
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                }
                if (byteArrayOutputStream != null) {
                }
                return obj2;
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                }
                if (byteArrayOutputStream != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
            return obj2;
        } catch (IOException e8) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e2222) {
                    e2222.printStackTrace();
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e22222) {
                    e22222.printStackTrace();
                }
            }
            return obj2;
        } catch (Throwable th4) {
            th = th4;
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e9) {
                    e9.printStackTrace();
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e92) {
                    e92.printStackTrace();
                }
            }
            throw th;
        }
        return obj2;
    }

    private static boolean a(File file) {
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:61:0x008c A:{SYNTHETIC, Splitter: B:61:0x008c} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0070 A:{SYNTHETIC, Splitter: B:46:0x0070} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x007f A:{SYNTHETIC, Splitter: B:54:0x007f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int c(Context context, Object obj, String str, String str2) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        Throwable th2;
        int i = -2;
        if (obj == null || str == null || str2 == null) {
            return -57;
        }
        FileOutputStream fileOutputStream = null;
        try {
            File file = new File(str2);
            if (a(file)) {
                FileOutputStream fileOutputStream2 = new FileOutputStream(file);
                try {
                    fn fnVar = new fn();
                    fnVar.B("UTF-8");
                    fnVar.put(str, obj);
                    byte[] a = fz.a(fnVar.l(), fz.P());
                    if (a != null) {
                        fileOutputStream2.write(a);
                        i = 0;
                    }
                    if (fileOutputStream2 != null) {
                        try {
                            fileOutputStream2.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    fileOutputStream = fileOutputStream2;
                } catch (FileNotFoundException e4) {
                    e2 = e4;
                    fileOutputStream = fileOutputStream2;
                } catch (IOException e5) {
                    e3 = e5;
                    fileOutputStream = fileOutputStream2;
                    i = ErrorCode.ERR_FILE_OP;
                    e3.printStackTrace();
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                    return i;
                } catch (Throwable th3) {
                    th2 = th3;
                    fileOutputStream = fileOutputStream2;
                    if (fileOutputStream != null) {
                    }
                    throw th2;
                }
                return i;
            }
            if (null != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            return -2;
        } catch (FileNotFoundException e7) {
            e2 = e7;
            i = -1;
            try {
                e2.printStackTrace();
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
                return i;
            } catch (Throwable th4) {
                th2 = th4;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e8) {
                        e8.printStackTrace();
                    }
                }
                throw th2;
            }
        } catch (IOException e9) {
            e322 = e9;
            i = ErrorCode.ERR_FILE_OP;
            e322.printStackTrace();
            if (fileOutputStream != null) {
            }
            return i;
        } catch (Throwable th5) {
            th = th5;
            th.printStackTrace();
            if (fileOutputStream != null) {
            }
            return i;
        }
    }

    public static String e(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }
}
