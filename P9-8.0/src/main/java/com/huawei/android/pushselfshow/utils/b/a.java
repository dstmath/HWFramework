package com.huawei.android.pushselfshow.utils.b;

import android.text.TextUtils;
import com.huawei.android.pushagent.a.a.c;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;

public class a {
    private String a;
    private String b;

    public a(String str, String str2) {
        this.a = str;
        this.b = str2;
    }

    public static File a(String str, String str2) {
        String str3;
        String str4;
        Exception e;
        String[] split = str2.split("/");
        File file = new File(str);
        int i = 0;
        while (i < split.length - 1) {
            try {
                str3 = new String(split[i].getBytes("8859_1"), "GB2312");
                i++;
                str4 = str3;
                file = new File(file, str3);
            } catch (Exception e2) {
                e = e2;
            }
        }
        c.a("PushSelfShowLog", "file1 = " + file);
        if (!file.exists()) {
            c.a("PushSelfShowLog", "getRealFileName ret.mkdirs success");
            if (!file.mkdirs()) {
                c.a("PushSelfShowLog", "ret.mkdirs faild");
            }
        }
        str3 = new String(split[split.length - 1].getBytes("8859_1"), "GB2312");
        try {
        } catch (Exception e3) {
            e = e3;
            str4 = str3;
        }
        try {
            c.a("PushSelfShowLog", "substr = " + str3);
            File file2 = new File(file, str3);
            try {
                c.a("PushSelfShowLog", "file2 = " + file2);
                return file2;
            } catch (Exception e4) {
                e = e4;
                str4 = str3;
                file = file2;
                c.d("PushSelfShowLog", e.toString());
                return file;
            }
        } catch (Exception e5) {
            e = e5;
            str4 = str3;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:149:0x047c A:{SYNTHETIC, Splitter: B:149:0x047c} */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x04a6 A:{SYNTHETIC, Splitter: B:154:0x04a6} */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x04d0 A:{SYNTHETIC, Splitter: B:159:0x04d0} */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x002d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x02b1 A:{SYNTHETIC, Splitter: B:89:0x02b1} */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x054f A:{SYNTHETIC, Splitter: B:178:0x054f} */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0577 A:{SYNTHETIC, Splitter: B:183:0x0577} */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x059f A:{SYNTHETIC, Splitter: B:188:0x059f} */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x002d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x0525 A:{SYNTHETIC, Splitter: B:171:0x0525} */
    /* JADX WARNING: Removed duplicated region for block: B:207:0x061d A:{SYNTHETIC, Splitter: B:207:0x061d} */
    /* JADX WARNING: Removed duplicated region for block: B:212:0x0645 A:{SYNTHETIC, Splitter: B:212:0x0645} */
    /* JADX WARNING: Removed duplicated region for block: B:217:0x066d A:{SYNTHETIC, Splitter: B:217:0x066d} */
    /* JADX WARNING: Removed duplicated region for block: B:396:0x002d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:200:0x05f3 A:{SYNTHETIC, Splitter: B:200:0x05f3} */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x047c A:{SYNTHETIC, Splitter: B:149:0x047c} */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x04a6 A:{SYNTHETIC, Splitter: B:154:0x04a6} */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x04d0 A:{SYNTHETIC, Splitter: B:159:0x04d0} */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x02b1 A:{SYNTHETIC, Splitter: B:89:0x02b1} */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x002d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x054f A:{SYNTHETIC, Splitter: B:178:0x054f} */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0577 A:{SYNTHETIC, Splitter: B:183:0x0577} */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x059f A:{SYNTHETIC, Splitter: B:188:0x059f} */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x0525 A:{SYNTHETIC, Splitter: B:171:0x0525} */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x002d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:207:0x061d A:{SYNTHETIC, Splitter: B:207:0x061d} */
    /* JADX WARNING: Removed duplicated region for block: B:212:0x0645 A:{SYNTHETIC, Splitter: B:212:0x0645} */
    /* JADX WARNING: Removed duplicated region for block: B:217:0x066d A:{SYNTHETIC, Splitter: B:217:0x066d} */
    /* JADX WARNING: Removed duplicated region for block: B:200:0x05f3 A:{SYNTHETIC, Splitter: B:200:0x05f3} */
    /* JADX WARNING: Removed duplicated region for block: B:396:0x002d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x047c A:{SYNTHETIC, Splitter: B:149:0x047c} */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x04a6 A:{SYNTHETIC, Splitter: B:154:0x04a6} */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x04d0 A:{SYNTHETIC, Splitter: B:159:0x04d0} */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x002d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x02b1 A:{SYNTHETIC, Splitter: B:89:0x02b1} */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x054f A:{SYNTHETIC, Splitter: B:178:0x054f} */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0577 A:{SYNTHETIC, Splitter: B:183:0x0577} */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x059f A:{SYNTHETIC, Splitter: B:188:0x059f} */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x002d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x0525 A:{SYNTHETIC, Splitter: B:171:0x0525} */
    /* JADX WARNING: Removed duplicated region for block: B:207:0x061d A:{SYNTHETIC, Splitter: B:207:0x061d} */
    /* JADX WARNING: Removed duplicated region for block: B:212:0x0645 A:{SYNTHETIC, Splitter: B:212:0x0645} */
    /* JADX WARNING: Removed duplicated region for block: B:217:0x066d A:{SYNTHETIC, Splitter: B:217:0x066d} */
    /* JADX WARNING: Removed duplicated region for block: B:396:0x002d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:200:0x05f3 A:{SYNTHETIC, Splitter: B:200:0x05f3} */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x03d3 A:{SYNTHETIC, Splitter: B:126:0x03d3} */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x044d A:{SYNTHETIC, Splitter: B:140:0x044d} */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x044d A:{SYNTHETIC, Splitter: B:140:0x044d} */
    /* JADX WARNING: Removed duplicated region for block: B:233:0x06c8 A:{SYNTHETIC, Splitter: B:233:0x06c8} */
    /* JADX WARNING: Removed duplicated region for block: B:238:0x06f0 A:{SYNTHETIC, Splitter: B:238:0x06f0} */
    /* JADX WARNING: Removed duplicated region for block: B:243:0x0718 A:{SYNTHETIC, Splitter: B:243:0x0718} */
    /* JADX WARNING: Removed duplicated region for block: B:238:0x06f0 A:{SYNTHETIC, Splitter: B:238:0x06f0} */
    /* JADX WARNING: Removed duplicated region for block: B:243:0x0718 A:{SYNTHETIC, Splitter: B:243:0x0718} */
    /* JADX WARNING: Removed duplicated region for block: B:243:0x0718 A:{SYNTHETIC, Splitter: B:243:0x0718} */
    /* JADX WARNING: Missing block: B:62:0x0210, code:
            if (r23 != null) goto L_0x02df;
     */
    /* JADX WARNING: Missing block: B:99:?, code:
            r23.close();
     */
    /* JADX WARNING: Missing block: B:100:0x02e4, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:102:?, code:
            com.huawei.android.pushagent.a.a.c.a("PushSelfShowLog", "zFileIn.close error:" + r4.getMessage());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a() {
        FileOutputStream fileOutputStream;
        OutputStream outputStream;
        InputStream inputStream;
        InputStream inputStream2;
        OutputStream fileOutputStream2;
        InputStream bufferedInputStream;
        IOException e;
        IllegalStateException e2;
        OutputStream outputStream2;
        IndexOutOfBoundsException e3;
        Throwable th;
        IOException e4;
        ZipException e5;
        IllegalStateException e6;
        NoSuchElementException e7;
        Throwable th2;
        ZipFile zipFile = null;
        try {
            if (!this.b.endsWith("/")) {
                this.b += "/";
            }
            ZipFile zipFile2 = new ZipFile(new File(this.a));
            try {
                Enumeration entries = zipFile2.entries();
                byte[] bArr = new byte[IncomingSmsFilterConsts.PAY_SMS];
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                    c.a("PushSelfShowLog", "ze.isDirectory()=" + zipEntry.isDirectory() + "ze.getName() = " + zipEntry.getName());
                    if (zipEntry.isDirectory()) {
                        String str = new String((this.b + zipEntry.getName()).getBytes("8859_1"), "GB2312");
                        c.a("PushSelfShowLog", "str = " + str);
                        if (new File(str).mkdir()) {
                            continue;
                        }
                    }
                    if (TextUtils.isEmpty(zipEntry.getName())) {
                        c.a("PushSelfShowLog", "ze.getName() is empty= ");
                        if (zipFile2 != null) {
                            try {
                                zipFile2.close();
                            } catch (IOException e8) {
                                c.a("PushSelfShowLog", "zfile.close error:" + e8.getMessage());
                            }
                        }
                        return;
                    }
                    if (!zipEntry.getName().contains("..\\")) {
                        if (!zipEntry.getName().contains("../")) {
                            File a = a(this.b, zipEntry.getName());
                            if (a.isDirectory()) {
                                if (zipFile2 != null) {
                                    try {
                                        zipFile2.close();
                                    } catch (IOException e9) {
                                        c.a("PushSelfShowLog", "zfile.close error:" + e9.getMessage());
                                    }
                                }
                                return;
                            }
                            c.a("PushSelfShowLog", ",output file :" + a.getAbsolutePath());
                            fileOutputStream = null;
                            outputStream = null;
                            inputStream = null;
                            inputStream2 = null;
                            try {
                                inputStream2 = zipFile2.getInputStream(zipEntry);
                                fileOutputStream2 = new FileOutputStream(a);
                                try {
                                    try {
                                        fileOutputStream2 = new BufferedOutputStream(fileOutputStream2);
                                        try {
                                            bufferedInputStream = new BufferedInputStream(inputStream2);
                                            while (true) {
                                                try {
                                                    int read = bufferedInputStream.read(bArr, 0, IncomingSmsFilterConsts.PAY_SMS);
                                                    if (read == -1) {
                                                        break;
                                                    }
                                                    try {
                                                        fileOutputStream2.write(bArr, 0, read);
                                                    } catch (IOException e10) {
                                                        e = e10;
                                                        inputStream = bufferedInputStream;
                                                        outputStream = fileOutputStream2;
                                                        fileOutputStream = fileOutputStream2;
                                                    } catch (IllegalStateException e11) {
                                                        e2 = e11;
                                                        inputStream = bufferedInputStream;
                                                        outputStream = fileOutputStream2;
                                                        outputStream2 = fileOutputStream2;
                                                    } catch (IndexOutOfBoundsException e12) {
                                                        e3 = e12;
                                                        inputStream = bufferedInputStream;
                                                        outputStream = fileOutputStream2;
                                                        outputStream2 = fileOutputStream2;
                                                    } catch (Throwable th3) {
                                                        th = th3;
                                                        inputStream = bufferedInputStream;
                                                        outputStream = fileOutputStream2;
                                                        outputStream2 = fileOutputStream2;
                                                    }
                                                } catch (IOException e13) {
                                                    e = e13;
                                                    inputStream = bufferedInputStream;
                                                    outputStream = fileOutputStream2;
                                                    outputStream2 = fileOutputStream2;
                                                } catch (IllegalStateException e14) {
                                                    e2 = e14;
                                                    inputStream = bufferedInputStream;
                                                    outputStream = fileOutputStream2;
                                                    outputStream2 = fileOutputStream2;
                                                } catch (IndexOutOfBoundsException e15) {
                                                    e3 = e15;
                                                    inputStream = bufferedInputStream;
                                                    outputStream = fileOutputStream2;
                                                    outputStream2 = fileOutputStream2;
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    inputStream = bufferedInputStream;
                                                    outputStream = fileOutputStream2;
                                                    outputStream2 = fileOutputStream2;
                                                }
                                            }
                                        } catch (IOException e16) {
                                            e = e16;
                                            outputStream = fileOutputStream2;
                                            outputStream2 = fileOutputStream2;
                                            try {
                                                c.a("PushSelfShowLog", "os.write error:" + e.getMessage());
                                                if (inputStream2 != null) {
                                                }
                                                if (inputStream != null) {
                                                }
                                                if (outputStream != null) {
                                                }
                                                if (fileOutputStream == null) {
                                                }
                                            } catch (Throwable th5) {
                                                th = th5;
                                            }
                                        } catch (IllegalStateException e17) {
                                            e2 = e17;
                                            outputStream = fileOutputStream2;
                                            outputStream2 = fileOutputStream2;
                                            c.a("PushSelfShowLog", "os.write error:" + e2.getMessage());
                                            if (inputStream2 != null) {
                                            }
                                            if (inputStream != null) {
                                            }
                                            if (outputStream != null) {
                                            }
                                            if (fileOutputStream == null) {
                                            }
                                        } catch (IndexOutOfBoundsException e18) {
                                            e3 = e18;
                                            outputStream = fileOutputStream2;
                                            outputStream2 = fileOutputStream2;
                                            c.a("PushSelfShowLog", "os.write error:" + e3.getMessage());
                                            if (inputStream2 != null) {
                                            }
                                            if (inputStream != null) {
                                            }
                                            if (outputStream != null) {
                                            }
                                            if (fileOutputStream == null) {
                                            }
                                        } catch (Throwable th6) {
                                            th = th6;
                                            outputStream = fileOutputStream2;
                                            outputStream2 = fileOutputStream2;
                                        }
                                    } catch (IOException e19) {
                                        e = e19;
                                        outputStream2 = fileOutputStream2;
                                        c.a("PushSelfShowLog", "os.write error:" + e.getMessage());
                                        if (inputStream2 != null) {
                                        }
                                        if (inputStream != null) {
                                        }
                                        if (outputStream != null) {
                                        }
                                        if (fileOutputStream == null) {
                                        }
                                    } catch (IllegalStateException e20) {
                                        e2 = e20;
                                        outputStream2 = fileOutputStream2;
                                        c.a("PushSelfShowLog", "os.write error:" + e2.getMessage());
                                        if (inputStream2 != null) {
                                        }
                                        if (inputStream != null) {
                                        }
                                        if (outputStream != null) {
                                        }
                                        if (fileOutputStream == null) {
                                        }
                                    } catch (IndexOutOfBoundsException e21) {
                                        e3 = e21;
                                        outputStream2 = fileOutputStream2;
                                        c.a("PushSelfShowLog", "os.write error:" + e3.getMessage());
                                        if (inputStream2 != null) {
                                        }
                                        if (inputStream != null) {
                                        }
                                        if (outputStream != null) {
                                        }
                                        if (fileOutputStream == null) {
                                        }
                                    } catch (Throwable th7) {
                                        th = th7;
                                        outputStream2 = fileOutputStream2;
                                    }
                                } catch (IOException e22) {
                                    e = e22;
                                    outputStream2 = fileOutputStream2;
                                    c.a("PushSelfShowLog", "os.write error:" + e.getMessage());
                                    if (inputStream2 != null) {
                                        try {
                                            inputStream2.close();
                                        } catch (IOException e23) {
                                            c.a("PushSelfShowLog", "zFileIn.close error:" + e23.getMessage());
                                        }
                                    }
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException e232) {
                                            c.a("PushSelfShowLog", "is.close error:" + e232.getMessage());
                                        }
                                    }
                                    if (outputStream != null) {
                                        try {
                                            outputStream.close();
                                        } catch (IOException e2322) {
                                            c.a("PushSelfShowLog", "os.close error:" + e2322.getMessage());
                                        }
                                    }
                                    if (fileOutputStream == null) {
                                        try {
                                            fileOutputStream.close();
                                        } catch (IOException e23222) {
                                            c.a("PushSelfShowLog", "tempFOS.close error:" + e23222.getMessage());
                                        }
                                    } else {
                                        continue;
                                    }
                                } catch (IllegalStateException e24) {
                                    e2 = e24;
                                    outputStream2 = fileOutputStream2;
                                    c.a("PushSelfShowLog", "os.write error:" + e2.getMessage());
                                    if (inputStream2 != null) {
                                        try {
                                            inputStream2.close();
                                        } catch (IOException e232222) {
                                            c.a("PushSelfShowLog", "zFileIn.close error:" + e232222.getMessage());
                                        }
                                    }
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException e2322222) {
                                            c.a("PushSelfShowLog", "is.close error:" + e2322222.getMessage());
                                        }
                                    }
                                    if (outputStream != null) {
                                        try {
                                            outputStream.close();
                                        } catch (IOException e23222222) {
                                            c.a("PushSelfShowLog", "os.close error:" + e23222222.getMessage());
                                        }
                                    }
                                    if (fileOutputStream == null) {
                                        try {
                                            fileOutputStream.close();
                                        } catch (IOException e232222222) {
                                            c.a("PushSelfShowLog", "tempFOS.close error:" + e232222222.getMessage());
                                        }
                                    } else {
                                        continue;
                                    }
                                } catch (IndexOutOfBoundsException e25) {
                                    e3 = e25;
                                    outputStream2 = fileOutputStream2;
                                    c.a("PushSelfShowLog", "os.write error:" + e3.getMessage());
                                    if (inputStream2 != null) {
                                        try {
                                            inputStream2.close();
                                        } catch (IOException e2322222222) {
                                            c.a("PushSelfShowLog", "zFileIn.close error:" + e2322222222.getMessage());
                                        }
                                    }
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException e23222222222) {
                                            c.a("PushSelfShowLog", "is.close error:" + e23222222222.getMessage());
                                        }
                                    }
                                    if (outputStream != null) {
                                        try {
                                            outputStream.close();
                                        } catch (IOException e232222222222) {
                                            c.a("PushSelfShowLog", "os.close error:" + e232222222222.getMessage());
                                        }
                                    }
                                    if (fileOutputStream == null) {
                                        try {
                                            fileOutputStream.close();
                                        } catch (IOException e2322222222222) {
                                            c.a("PushSelfShowLog", "tempFOS.close error:" + e2322222222222.getMessage());
                                        }
                                    } else {
                                        continue;
                                    }
                                } catch (Throwable th8) {
                                    th = th8;
                                    outputStream2 = fileOutputStream2;
                                }
                            } catch (IOException e26) {
                                e2322222222222 = e26;
                                c.a("PushSelfShowLog", "os.write error:" + e2322222222222.getMessage());
                                if (inputStream2 != null) {
                                }
                                if (inputStream != null) {
                                }
                                if (outputStream != null) {
                                }
                                if (fileOutputStream == null) {
                                }
                            } catch (IllegalStateException e27) {
                                e2 = e27;
                                c.a("PushSelfShowLog", "os.write error:" + e2.getMessage());
                                if (inputStream2 != null) {
                                }
                                if (inputStream != null) {
                                }
                                if (outputStream != null) {
                                }
                                if (fileOutputStream == null) {
                                }
                            } catch (IndexOutOfBoundsException e28) {
                                e3 = e28;
                                c.a("PushSelfShowLog", "os.write error:" + e3.getMessage());
                                if (inputStream2 != null) {
                                }
                                if (inputStream != null) {
                                }
                                if (outputStream != null) {
                                }
                                if (fileOutputStream == null) {
                                }
                            }
                        }
                    }
                    c.c("PushSelfShowLog", "upZipFile, path is invalid!");
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e82) {
                            c.a("PushSelfShowLog", "zfile.close error:" + e82.getMessage());
                        }
                    }
                    return;
                }
                if (zipFile2 != null) {
                    try {
                        zipFile2.close();
                    } catch (IOException e42) {
                        c.a("PushSelfShowLog", "zfile.close error:" + e42.getMessage());
                    }
                }
                zipFile = zipFile2;
            } catch (ZipException e29) {
                e5 = e29;
                zipFile = zipFile2;
            } catch (IOException e30) {
                e42 = e30;
                zipFile = zipFile2;
            } catch (IllegalStateException e31) {
                e6 = e31;
                zipFile = zipFile2;
            } catch (NoSuchElementException e32) {
                e7 = e32;
                zipFile = zipFile2;
            } catch (Throwable th9) {
                th2 = th9;
                zipFile = zipFile2;
            }
        } catch (ZipException e33) {
            e5 = e33;
        } catch (IOException e34) {
            e42 = e34;
            c.a("PushSelfShowLog", "upZipFile error:" + e42.getMessage());
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e422) {
                    c.a("PushSelfShowLog", "zfile.close error:" + e422.getMessage());
                }
            }
            return;
        } catch (IllegalStateException e35) {
            e6 = e35;
            c.a("PushSelfShowLog", "upZipFile error:" + e6.getMessage());
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e4222) {
                    c.a("PushSelfShowLog", "zfile.close error:" + e4222.getMessage());
                }
            }
            return;
        } catch (NoSuchElementException e36) {
            e7 = e36;
            c.a("PushSelfShowLog", "upZipFile error:" + e7.getMessage());
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e42222) {
                    c.a("PushSelfShowLog", "zfile.close error:" + e42222.getMessage());
                }
            }
            return;
        }
        return;
        if (bufferedInputStream != null) {
            try {
                bufferedInputStream.close();
            } catch (IOException e23222222222222) {
                c.a("PushSelfShowLog", "is.close error:" + e23222222222222.getMessage());
            }
        }
        if (fileOutputStream2 != null) {
            try {
                fileOutputStream2.close();
            } catch (IOException e232222222222222) {
                c.a("PushSelfShowLog", "os.close error:" + e232222222222222.getMessage());
            }
        }
        if (fileOutputStream2 != null) {
            try {
                fileOutputStream2.close();
            } catch (IOException e2322222222222222) {
                c.a("PushSelfShowLog", "tempFOS.close error:" + e2322222222222222.getMessage());
            }
        }
        inputStream = bufferedInputStream;
        outputStream = fileOutputStream2;
        outputStream2 = fileOutputStream2;
        if (fileOutputStream2 != null) {
        }
        if (fileOutputStream2 != null) {
        }
        inputStream = bufferedInputStream;
        outputStream = fileOutputStream2;
        outputStream2 = fileOutputStream2;
        if (fileOutputStream2 != null) {
        }
        inputStream = bufferedInputStream;
        outputStream = fileOutputStream2;
        outputStream2 = fileOutputStream2;
        inputStream = bufferedInputStream;
        outputStream = fileOutputStream2;
        outputStream2 = fileOutputStream2;
        try {
            c.a("PushSelfShowLog", "upZipFile error:" + e5.getMessage());
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e422222) {
                    c.a("PushSelfShowLog", "zfile.close error:" + e422222.getMessage());
                }
            }
            return;
        } catch (Throwable th10) {
            th2 = th10;
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e37) {
                    c.a("PushSelfShowLog", "zfile.close error:" + e37.getMessage());
                }
            }
            throw th2;
        }
        if (inputStream2 != null) {
            try {
                inputStream2.close();
            } catch (IOException e38) {
                c.a("PushSelfShowLog", "zFileIn.close error:" + e38.getMessage());
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e382) {
                c.a("PushSelfShowLog", "is.close error:" + e382.getMessage());
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e3822) {
                c.a("PushSelfShowLog", "os.close error:" + e3822.getMessage());
            }
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException e38222) {
                c.a("PushSelfShowLog", "tempFOS.close error:" + e38222.getMessage());
            }
        }
        throw th;
        if (inputStream != null) {
        }
        if (outputStream != null) {
        }
        if (fileOutputStream != null) {
        }
        throw th;
        if (outputStream != null) {
        }
        if (fileOutputStream != null) {
        }
        throw th;
        if (fileOutputStream != null) {
        }
        throw th;
        throw th;
    }
}
