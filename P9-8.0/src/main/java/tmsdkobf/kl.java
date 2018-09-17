package tmsdkobf;

import android.os.Environment;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.common.utils.f;

public class kl {
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0042 A:{SYNTHETIC, Splitter: B:28:0x0042} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean a(byte[] bArr, String str) {
        Throwable th;
        if (bArr == null) {
            return false;
        }
        boolean z = false;
        FileOutputStream fileOutputStream = null;
        try {
            File file = new File(str);
            if (!file.exists()) {
                file.getAbsoluteFile().getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream fileOutputStream2 = new FileOutputStream(file, false);
            try {
                fileOutputStream2.write(bArr);
                z = true;
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (IOException e) {
                        fileOutputStream = fileOutputStream2;
                    }
                }
                fileOutputStream = fileOutputStream2;
            } catch (Throwable th2) {
                th = th2;
                fileOutputStream = fileOutputStream2;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e2) {
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (fileOutputStream != null) {
            }
            throw th;
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:63:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0027  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0056 A:{SYNTHETIC, Splitter: B:36:0x0056} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x003b A:{SYNTHETIC, Splitter: B:24:0x003b} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0027  */
    /* JADX WARNING: Removed duplicated region for block: B:63:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0065 A:{SYNTHETIC, Splitter: B:44:0x0065} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x006e A:{SYNTHETIC, Splitter: B:48:0x006e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] aV(String str) {
        Throwable th;
        byte[] bArr = null;
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            FileInputStream fileInputStream2 = new FileInputStream(str);
            try {
                ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream(fileInputStream2.available());
                try {
                    byte[] bArr2 = new byte[IncomingSmsFilterConsts.PAY_SMS];
                    while (true) {
                        int read = fileInputStream2.read(bArr2);
                        if (read < 0) {
                            bArr = byteArrayOutputStream2.toByteArray();
                            if (byteArrayOutputStream2 != null) {
                                try {
                                    byteArrayOutputStream2.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (fileInputStream2 != null) {
                                try {
                                    fileInputStream2.close();
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                            }
                            byteArrayOutputStream = byteArrayOutputStream2;
                            fileInputStream = fileInputStream2;
                        } else {
                            byteArrayOutputStream2.write(bArr2, 0, read);
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    byteArrayOutputStream = byteArrayOutputStream2;
                    fileInputStream = fileInputStream2;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fileInputStream2;
                if (byteArrayOutputStream != null) {
                }
                if (fileInputStream != null) {
                }
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
            throw th;
        }
        return bArr == null ? "".getBytes() : bArr;
    }

    public static boolean b(File file) {
        boolean z = false;
        Object obj = 1;
        if (file.isDirectory()) {
            String[] list = file.list();
            String[] strArr = list;
            int length = list.length;
            for (int i = 0; i < length; i++) {
                if (!b(new File(file, strArr[i]))) {
                    obj = null;
                }
            }
        }
        boolean delete = file.delete();
        if (obj != null && delete) {
            z = true;
        }
        if (!z) {
            f.f("FileUtil", "delete failed: " + file.getAbsolutePath());
        }
        return z;
    }

    public static int cK() {
        return lu.eF() ? cL() ? 0 : 2 : 1;
    }

    public static boolean cL() {
        String str = Environment.getExternalStorageDirectory().toString() + "/DCIM";
        File file = new File(str);
        if (!file.isDirectory() && !file.mkdirs()) {
            return false;
        }
        File file2 = new File(str, ".probe");
        try {
            if (file2.exists()) {
                file2.delete();
            }
            if (!file2.createNewFile()) {
                return false;
            }
            file2.delete();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:53:0x0076 A:{SYNTHETIC, Splitter: B:53:0x0076} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x007c A:{SYNTHETIC, Splitter: B:56:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0082 A:{SYNTHETIC, Splitter: B:59:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0058 A:{SYNTHETIC, Splitter: B:38:0x0058} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0092 A:{SYNTHETIC, Splitter: B:68:0x0092} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0098 A:{SYNTHETIC, Splitter: B:71:0x0098} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x009e A:{SYNTHETIC, Splitter: B:74:0x009e} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00a4 A:{SYNTHETIC, Splitter: B:77:0x00a4} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0076 A:{SYNTHETIC, Splitter: B:53:0x0076} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x007c A:{SYNTHETIC, Splitter: B:56:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0082 A:{SYNTHETIC, Splitter: B:59:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0058 A:{SYNTHETIC, Splitter: B:38:0x0058} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0092 A:{SYNTHETIC, Splitter: B:68:0x0092} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0098 A:{SYNTHETIC, Splitter: B:71:0x0098} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x009e A:{SYNTHETIC, Splitter: B:74:0x009e} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00a4 A:{SYNTHETIC, Splitter: B:77:0x00a4} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0076 A:{SYNTHETIC, Splitter: B:53:0x0076} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x007c A:{SYNTHETIC, Splitter: B:56:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0082 A:{SYNTHETIC, Splitter: B:59:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0058 A:{SYNTHETIC, Splitter: B:38:0x0058} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0092 A:{SYNTHETIC, Splitter: B:68:0x0092} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0098 A:{SYNTHETIC, Splitter: B:71:0x0098} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x009e A:{SYNTHETIC, Splitter: B:74:0x009e} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00a4 A:{SYNTHETIC, Splitter: B:77:0x00a4} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0076 A:{SYNTHETIC, Splitter: B:53:0x0076} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x007c A:{SYNTHETIC, Splitter: B:56:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0082 A:{SYNTHETIC, Splitter: B:59:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0058 A:{SYNTHETIC, Splitter: B:38:0x0058} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0092 A:{SYNTHETIC, Splitter: B:68:0x0092} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0098 A:{SYNTHETIC, Splitter: B:71:0x0098} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x009e A:{SYNTHETIC, Splitter: B:74:0x009e} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00a4 A:{SYNTHETIC, Splitter: B:77:0x00a4} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0076 A:{SYNTHETIC, Splitter: B:53:0x0076} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x007c A:{SYNTHETIC, Splitter: B:56:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0082 A:{SYNTHETIC, Splitter: B:59:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0058 A:{SYNTHETIC, Splitter: B:38:0x0058} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0092 A:{SYNTHETIC, Splitter: B:68:0x0092} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0098 A:{SYNTHETIC, Splitter: B:71:0x0098} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x009e A:{SYNTHETIC, Splitter: B:74:0x009e} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00a4 A:{SYNTHETIC, Splitter: B:77:0x00a4} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0076 A:{SYNTHETIC, Splitter: B:53:0x0076} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x007c A:{SYNTHETIC, Splitter: B:56:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0082 A:{SYNTHETIC, Splitter: B:59:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0058 A:{SYNTHETIC, Splitter: B:38:0x0058} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0092 A:{SYNTHETIC, Splitter: B:68:0x0092} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0098 A:{SYNTHETIC, Splitter: B:71:0x0098} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x009e A:{SYNTHETIC, Splitter: B:74:0x009e} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00a4 A:{SYNTHETIC, Splitter: B:77:0x00a4} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean copyFile(File file, File file2) {
        Throwable th;
        if (file.isFile()) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            BufferedInputStream bufferedInputStream = null;
            BufferedOutputStream bufferedOutputStream = null;
            try {
                OutputStream fileOutputStream;
                InputStream fileInputStream = new FileInputStream(file);
                try {
                    fileOutputStream = new FileOutputStream(file2);
                } catch (Exception e) {
                    inputStream = fileInputStream;
                    if (bufferedInputStream != null) {
                    }
                    if (bufferedOutputStream != null) {
                    }
                    if (inputStream != null) {
                    }
                    if (outputStream != null) {
                    }
                    return true;
                } catch (Throwable th2) {
                    th = th2;
                    inputStream = fileInputStream;
                    if (bufferedInputStream != null) {
                    }
                    if (bufferedOutputStream != null) {
                    }
                    if (inputStream != null) {
                    }
                    if (outputStream != null) {
                    }
                    throw th;
                }
                try {
                    try {
                        BufferedInputStream bufferedInputStream2 = new BufferedInputStream(fileInputStream);
                        try {
                            try {
                                BufferedOutputStream bufferedOutputStream2 = new BufferedOutputStream(fileOutputStream);
                                try {
                                    byte[] bArr = new byte[8192];
                                    while (true) {
                                        int read = bufferedInputStream2.read(bArr);
                                        if (read == -1) {
                                            break;
                                        }
                                        bufferedOutputStream2.write(bArr, 0, read);
                                    }
                                    bufferedOutputStream2.flush();
                                    if (bufferedInputStream2 != null) {
                                        try {
                                            bufferedInputStream2.close();
                                        } catch (Exception e2) {
                                        }
                                    }
                                    if (bufferedOutputStream2 != null) {
                                        try {
                                            bufferedOutputStream2.close();
                                        } catch (Exception e3) {
                                        }
                                    }
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (Exception e4) {
                                        }
                                    }
                                    if (fileOutputStream != null) {
                                        try {
                                            fileOutputStream.close();
                                        } catch (Exception e5) {
                                        }
                                    }
                                    bufferedOutputStream = bufferedOutputStream2;
                                    bufferedInputStream = bufferedInputStream2;
                                    outputStream = fileOutputStream;
                                    inputStream = fileInputStream;
                                } catch (Exception e6) {
                                    bufferedOutputStream = bufferedOutputStream2;
                                    bufferedInputStream = bufferedInputStream2;
                                    outputStream = fileOutputStream;
                                    inputStream = fileInputStream;
                                    if (bufferedInputStream != null) {
                                    }
                                    if (bufferedOutputStream != null) {
                                    }
                                    if (inputStream != null) {
                                    }
                                    if (outputStream != null) {
                                    }
                                    return true;
                                } catch (Throwable th3) {
                                    th = th3;
                                    bufferedOutputStream = bufferedOutputStream2;
                                    bufferedInputStream = bufferedInputStream2;
                                    outputStream = fileOutputStream;
                                    inputStream = fileInputStream;
                                    if (bufferedInputStream != null) {
                                    }
                                    if (bufferedOutputStream != null) {
                                    }
                                    if (inputStream != null) {
                                    }
                                    if (outputStream != null) {
                                    }
                                    throw th;
                                }
                            } catch (Exception e7) {
                                bufferedInputStream = bufferedInputStream2;
                                outputStream = fileOutputStream;
                                inputStream = fileInputStream;
                                if (bufferedInputStream != null) {
                                }
                                if (bufferedOutputStream != null) {
                                }
                                if (inputStream != null) {
                                }
                                if (outputStream != null) {
                                }
                                return true;
                            } catch (Throwable th4) {
                                th = th4;
                                bufferedInputStream = bufferedInputStream2;
                                outputStream = fileOutputStream;
                                inputStream = fileInputStream;
                                if (bufferedInputStream != null) {
                                }
                                if (bufferedOutputStream != null) {
                                }
                                if (inputStream != null) {
                                }
                                if (outputStream != null) {
                                }
                                throw th;
                            }
                        } catch (Exception e8) {
                            bufferedInputStream = bufferedInputStream2;
                            outputStream = fileOutputStream;
                            inputStream = fileInputStream;
                            if (bufferedInputStream != null) {
                            }
                            if (bufferedOutputStream != null) {
                            }
                            if (inputStream != null) {
                            }
                            if (outputStream != null) {
                            }
                            return true;
                        } catch (Throwable th5) {
                            th = th5;
                            bufferedInputStream = bufferedInputStream2;
                            outputStream = fileOutputStream;
                            inputStream = fileInputStream;
                            if (bufferedInputStream != null) {
                            }
                            if (bufferedOutputStream != null) {
                            }
                            if (inputStream != null) {
                            }
                            if (outputStream != null) {
                            }
                            throw th;
                        }
                    } catch (Exception e9) {
                        outputStream = fileOutputStream;
                        inputStream = fileInputStream;
                        if (bufferedInputStream != null) {
                        }
                        if (bufferedOutputStream != null) {
                        }
                        if (inputStream != null) {
                        }
                        if (outputStream != null) {
                        }
                        return true;
                    } catch (Throwable th6) {
                        th = th6;
                        outputStream = fileOutputStream;
                        inputStream = fileInputStream;
                        if (bufferedInputStream != null) {
                        }
                        if (bufferedOutputStream != null) {
                        }
                        if (inputStream != null) {
                        }
                        if (outputStream != null) {
                        }
                        throw th;
                    }
                } catch (Exception e10) {
                    outputStream = fileOutputStream;
                    inputStream = fileInputStream;
                    if (bufferedInputStream != null) {
                    }
                    if (bufferedOutputStream != null) {
                    }
                    if (inputStream != null) {
                    }
                    if (outputStream != null) {
                    }
                    return true;
                } catch (Throwable th7) {
                    th = th7;
                    outputStream = fileOutputStream;
                    inputStream = fileInputStream;
                    if (bufferedInputStream != null) {
                    }
                    if (bufferedOutputStream != null) {
                    }
                    if (inputStream != null) {
                    }
                    if (outputStream != null) {
                    }
                    throw th;
                }
            } catch (Exception e11) {
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (Exception e12) {
                    }
                }
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream.close();
                    } catch (Exception e13) {
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e14) {
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Exception e15) {
                    }
                }
                return true;
            } catch (Throwable th8) {
                th = th8;
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (Exception e16) {
                    }
                }
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream.close();
                    } catch (Exception e17) {
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e18) {
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Exception e19) {
                    }
                }
                throw th;
            }
        } else if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            file2.mkdir();
            if (listFiles != null) {
                for (int i = 0; i < listFiles.length; i++) {
                    copyFile(listFiles[i].getAbsoluteFile(), new File(file2.getAbsoluteFile() + File.separator + listFiles[i].getName()));
                }
            }
        }
        return true;
    }
}
