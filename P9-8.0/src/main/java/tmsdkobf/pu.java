package tmsdkobf;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Xml;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.roach.nest.ActionI;
import tmsdk.common.roach.nest.PowerNest;
import tmsdk.common.tcc.TccDiff;
import tmsdk.common.utils.i;
import tmsdk.common.utils.k;
import tmsdk.common.utils.q;
import tmsdkobf.oo.a;

public class pu {
    static final int[] KD = new int[]{SmsCheckResult.ESCT_PAY, 103, 104, PowerNest.sNestVersion};
    static final String[] KE = new String[]{"00B1208638DE0FCD3E920886D658DAF6", "7CC749CFC0FB5677E6ABA342EDBDBA5A"};
    static pu KL = null;
    HashMap<px, Class<?>> KF = new HashMap();
    HashMap<px, ActionI> KG = new HashMap();
    HandlerThread KH = null;
    Handler KI = null;
    a KJ = null;
    private pw KK = null;

    private pu() {
        ps.g("RoachManager-RoachManager-NEST_IDS:[" + Arrays.toString(KD) + "]");
    }

    private void a(pv pvVar) {
        if (pvVar != null && pvVar.KN != null) {
            ia();
            Message.obtain(this.KI, 259, pvVar).sendToTarget();
        }
    }

    private void a(pv pvVar, int i) {
        Throwable th;
        Throwable th2;
        ps.g("loadItem:[" + pvVar + "]type:[" + i + "]");
        px pxVar = pvVar.KN;
        for (px pxVar2 : this.KG.keySet()) {
            if (pxVar2.KV == pxVar.KV) {
                ps.g("[" + pxVar2.KV + "]is running:[" + pxVar2.KW + "]new:[" + pxVar.KW + "]");
                ActionI actionI = (ActionI) this.KG.get(pxVar2);
                ps.g("rEntityRunning:[" + actionI + "]");
                try {
                    if (pxVar.KW > pxVar2.KW) {
                        ps.g("entity--onStop()/clean()");
                        actionI.onStop();
                        actionI.clean();
                        this.KG.remove(pxVar2);
                        this.KF.remove(pxVar2);
                        if (i == 1) {
                            break;
                        } else if (i == 2) {
                            return;
                        }
                    } else {
                        if (pxVar.KW == pxVar2.KW) {
                            if (i == 1) {
                                ps.g("save version, do nothing");
                                return;
                            } else if (i == 2) {
                                ps.g("save version, entity--onStop()/clean()");
                                actionI.onStop();
                                actionI.clean();
                                this.KG.remove(pxVar2);
                                this.KF.remove(pxVar2);
                                return;
                            }
                        } else {
                            return;
                        }
                    }
                } catch (Throwable th3) {
                    if (i == 1) {
                        kt.e(1320042, "0;1017;" + pvVar.ie());
                    } else if (i == 2) {
                        kt.e(1320042, "0;1018;" + pvVar.ie());
                    }
                    ps.h("e:[" + th3 + "]");
                    return;
                }
            }
        }
        for (px pxVar22 : this.KF.keySet()) {
            if (pxVar22.KV == pxVar.KV) {
                ps.g("[" + pxVar.KV + "]is loaded:[" + pxVar22.KW + "]new:[" + pxVar.KW + "]");
                try {
                    Class cls;
                    ActionI actionI2;
                    if (pxVar.KW > pxVar22.KW) {
                        cls = (Class) this.KF.get(pxVar22);
                        actionI2 = (ActionI) cls.getConstructor(new Class[0]).newInstance(new Object[0]);
                        ps.g("rEntityClass:[" + cls + "]rEntity:[" + actionI2 + "]");
                        ps.g("entity--clean()");
                        actionI2.clean();
                        if (i == 1) {
                            break;
                        } else if (i == 2) {
                            return;
                        }
                    } else {
                        if (pxVar22.KW == pxVar.KW) {
                            cls = (Class) this.KF.get(pxVar22);
                            actionI2 = (ActionI) cls.getConstructor(new Class[0]).newInstance(new Object[0]);
                            ps.g("rEntityClass:[" + cls + "]rEntity:[" + actionI2 + "]");
                            if (i == 1) {
                                ps.g("entity--onStart()");
                                Bundle bundle = new Bundle();
                                bundle.putString(ActionI.privDirKey, pvVar.if());
                                actionI2.onStart(bundle);
                                this.KG.put(pxVar22, actionI2);
                            } else if (i == 2) {
                                ps.g("entity--clean()");
                                actionI2.clean();
                                this.KF.remove(pxVar22);
                            }
                            return;
                        }
                        return;
                    }
                } catch (Throwable th4) {
                    if (i == 1) {
                        kt.e(1320042, "0;1017;" + pvVar.ie());
                    } else if (i == 2) {
                        kt.e(1320042, "0;1018;" + pvVar.ie());
                    }
                    ps.h("e:[" + th4 + "]");
                    return;
                }
            }
        }
        ZipFile zipFile = null;
        try {
            ps.g("private path:[" + pvVar.if() + "]");
            String str = pvVar.if() + File.separator + pvVar.ie();
            File file = new File(str);
            if (!file.exists()) {
                ps.g("srcFile:[" + file + "]not exist");
                if (null != null) {
                    try {
                        zipFile.close();
                    } catch (Throwable th32) {
                        ps.h("e :[" + th32 + "]");
                    }
                }
                return;
            } else if (b(str, str, false)) {
                String str2 = "armeabi";
                if (TMSDKContext.is_arm64v8a()) {
                    str2 = "arm64-v8a";
                }
                ps.g("lib:[" + str2 + "]");
                ZipFile zipFile2 = new ZipFile(file);
                try {
                    Enumeration entries = zipFile2.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                        if (!(zipEntry == null || zipEntry.isDirectory())) {
                            String name = zipEntry.getName();
                            if (!q.cK(name) && name.contains(str2)) {
                                String substring = name.substring(name.lastIndexOf("/") + 1);
                                int lastIndexOf = substring.lastIndexOf(".");
                                if (lastIndexOf > 0) {
                                    substring = substring.substring(0, lastIndexOf) + ".dat";
                                }
                                String str3 = pvVar.if() + File.separator + substring;
                                ps.g("destPath:[" + str3 + "]");
                                File file2 = new File(str3);
                                if (file2.exists()) {
                                    if (file2.length() == zipEntry.getSize()) {
                                        ps.g("dest file exists and same length, not copy");
                                    } else {
                                        file2.delete();
                                    }
                                }
                                BufferedInputStream bufferedInputStream = new BufferedInputStream(zipFile2.getInputStream(zipEntry));
                                FileOutputStream fileOutputStream = new FileOutputStream(file2);
                                byte[] bArr = new byte[8192];
                                while (true) {
                                    int read = bufferedInputStream.read(bArr);
                                    if (read <= 0) {
                                        break;
                                    }
                                    fileOutputStream.write(bArr, 0, read);
                                }
                                fileOutputStream.flush();
                                try {
                                    bufferedInputStream.close();
                                } catch (IOException e) {
                                    ps.h("e :[" + e + "]");
                                }
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e2) {
                                    ps.h("e :[" + e2 + "]");
                                }
                                ps.g("copy done, destPath:[" + str3 + "]");
                            }
                        }
                    }
                    String absolutePath = file.getAbsolutePath();
                    int lastIndexOf2 = file.getAbsolutePath().lastIndexOf(".");
                    if (lastIndexOf2 > 0) {
                        absolutePath = file.getAbsolutePath().substring(0, lastIndexOf2);
                    }
                    File file3 = new File(absolutePath + new String(new byte[]{(byte) 46, (byte) 97, (byte) 112, (byte) 107}));
                    file.renameTo(file3);
                    Class cls2 = Class.forName(new String(new byte[]{(byte) 100, (byte) 97, (byte) 108, (byte) 118, (byte) 105, (byte) 107, (byte) 46, (byte) 115, (byte) 121, (byte) 115, (byte) 116, (byte) 101, (byte) 109, (byte) 46, (byte) 68, (byte) 101, (byte) 120, (byte) 67, (byte) 108, (byte) 97, (byte) 115, (byte) 115, (byte) 76, (byte) 111, (byte) 97, (byte) 100, (byte) 101, (byte) 114}));
                    Object newInstance = cls2.getConstructor(new Class[]{String.class, String.class, String.class, ClassLoader.class}).newInstance(new Object[]{file3.getAbsolutePath(), pvVar.ig(), null, TMSDKContext.class.getClassLoader()});
                    Method method = cls2.getMethod(new String(new byte[]{(byte) 108, (byte) 111, (byte) 97, (byte) 100, (byte) 67, (byte) 108, (byte) 97, (byte) 115, (byte) 115}), new Class[]{String.class});
                    lu.bL(pvVar.ig());
                    file3.renameTo(file);
                    if (!b(str, str, true)) {
                        kt.e(1320038, "0;1015;" + pvVar.ie());
                        a(pvVar, false);
                    }
                    if (method != null) {
                        method.setAccessible(true);
                        Class cls3 = (Class) method.invoke(newInstance, new Object[]{"com.roach.REntity"});
                        ActionI actionI3 = (ActionI) cls3.getConstructor(new Class[0]).newInstance(new Object[0]);
                        ps.g("rEntityClass:[" + cls3 + "]rEntity:[" + actionI3 + "]");
                        if (i == 1) {
                            ps.g("entity--onStart()");
                            Bundle bundle2 = new Bundle(pu.class.getClassLoader());
                            bundle2.putString(ActionI.privDirKey, pvVar.if());
                            actionI3.onStart(bundle2);
                            this.KF.put(pxVar, cls3);
                            this.KG.put(pxVar, actionI3);
                            if (pxVar.KY == 2) {
                                a(pvVar, true);
                            }
                        } else if (i == 2) {
                            ps.g("entity--clean()");
                            actionI3.clean();
                        }
                    }
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (Throwable th5) {
                            ps.h("e :[" + th5 + "]");
                        }
                    }
                    if (i == 1) {
                        kt.e(1320042, "1;;" + pvVar.ie());
                    }
                    return;
                } catch (Throwable th6) {
                    th2 = th6;
                    zipFile = zipFile2;
                }
            } else {
                kt.e(1320038, "0;1016;" + pvVar.ie());
                a(pvVar, false);
                if (null != null) {
                    try {
                        zipFile.close();
                    } catch (Throwable th322) {
                        ps.h("e :[" + th322 + "]");
                    }
                }
                return;
            }
        } catch (Throwable th7) {
            th5 = th7;
        }
        try {
            ps.h("e :[" + th5 + "]");
            if (i == 1) {
                kt.e(1320042, "0;1017;" + pvVar.ie());
            } else if (i == 2) {
                kt.e(1320042, "0;1018;" + pvVar.ie());
            }
            a(pvVar, false);
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Throwable th42) {
                    ps.h("e :[" + th42 + "]");
                }
            }
        } catch (Throwable th8) {
            th2 = th8;
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Throwable th9) {
                    ps.h("e :[" + th9 + "]");
                }
            }
            throw th2;
        }
    }

    private void a(pv pvVar, boolean z) {
        if (pvVar != null && pvVar.KN != null) {
            try {
                ps.g("cleanItem-itemClean:[" + pvVar + "]");
                if (z) {
                    a(pvVar, 2);
                }
                lu.bL(pvVar.if());
                lu.bL(pvVar.ig());
                this.KK.bR(pvVar.KN.KV);
                kt.e(1320043, "1;;" + pvVar.ie());
            } catch (Throwable th) {
                kt.e(1320043, "0;1019;" + pvVar.ie());
                ps.h("cleanItem-error:[" + th + "]");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:79:0x01e8 A:{SYNTHETIC, Splitter: B:79:0x01e8} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x020f A:{SYNTHETIC, Splitter: B:84:0x020f} */
    /* JADX WARNING: Missing block: B:31:0x00dd, code:
            r13 = new java.io.FileOutputStream(r22);
     */
    /* JADX WARNING: Missing block: B:32:0x00e4, code:
            if (r23 != false) goto L_0x0107;
     */
    /* JADX WARNING: Missing block: B:35:?, code:
            r13.write(tmsdk.common.tcc.TccCryptor.decrypt(r9, null));
     */
    /* JADX WARNING: Missing block: B:36:0x00f3, code:
            r13.flush();
     */
    /* JADX WARNING: Missing block: B:37:0x00f6, code:
            r8 = true;
     */
    /* JADX WARNING: Missing block: B:38:0x00f7, code:
            if (r11 != null) goto L_0x0143;
     */
    /* JADX WARNING: Missing block: B:39:0x00f9, code:
            r10 = r11;
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            r13.write(tmsdk.common.tcc.TccCryptor.encrypt(r9, null));
     */
    /* JADX WARNING: Missing block: B:50:0x0115, code:
            r16 = th;
     */
    /* JADX WARNING: Missing block: B:51:0x0116, code:
            r12 = r13;
            r10 = r11;
     */
    /* JADX WARNING: Missing block: B:60:?, code:
            r11.close();
     */
    /* JADX WARNING: Missing block: B:62:0x0148, code:
            r16 = move-exception;
     */
    /* JADX WARNING: Missing block: B:63:0x0149, code:
            tmsdkobf.ps.h("e:[" + r16 + "]");
     */
    /* JADX WARNING: Missing block: B:91:0x0239, code:
            r6 = th;
     */
    /* JADX WARNING: Missing block: B:92:0x023a, code:
            r12 = r13;
            r10 = r11;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean b(String str, String str2, boolean z) {
        Throwable th;
        Throwable th2;
        ps.g("encryptFile-srcPath:[" + str + "]destPath:[" + str2 + "]encrypt:[" + z + "]");
        boolean z2 = false;
        BufferedInputStream bufferedInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            long length = new File(str).length();
            if ((length > 0 ? 1 : null) == null) {
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (Throwable th3) {
                        ps.h("e:[" + th3 + "]");
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable th32) {
                        ps.h("e:[" + th32 + "]");
                    }
                }
                return false;
            }
            byte[] bArr = new byte[((int) length)];
            int i = 0;
            BufferedInputStream bufferedInputStream2 = new BufferedInputStream(new FileInputStream(str));
            try {
                Object obj = new byte[8192];
                while (true) {
                    int read = bufferedInputStream2.read(obj);
                    if (read == -1) {
                        break;
                    }
                    System.arraycopy(obj, 0, bArr, i, read);
                    i += read;
                }
            } catch (Throwable th4) {
                th2 = th4;
                bufferedInputStream = bufferedInputStream2;
            }
            return z2;
            if (r13 == null) {
                fileOutputStream = r13;
                return z2;
            }
            try {
                r13.close();
            } catch (Throwable th5) {
                ps.h("e:[" + th5 + "]");
            }
            return z2;
        } catch (Throwable th6) {
            th5 = th6;
            try {
                ps.h("e:[" + th5 + "]");
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (Throwable th52) {
                        ps.h("e:[" + th52 + "]");
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable th522) {
                        ps.h("e:[" + th522 + "]");
                    }
                }
                return z2;
            } catch (Throwable th7) {
                th2 = th7;
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (Throwable th8) {
                        ps.h("e:[" + th8 + "]");
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable th82) {
                        ps.h("e:[" + th82 + "]");
                    }
                }
                throw th2;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:114:0x05f1 A:{SYNTHETIC, Splitter: B:114:0x05f1} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean b(pv pvVar) {
        ps.g("checkValidity:[" + pvVar + "]");
        if (pvVar == null || pvVar.KN == null) {
            return false;
        }
        String str = pvVar.if() + File.separator + pvVar.ie();
        File file = new File(str);
        if (!file.exists()) {
            ps.h(str + "[" + str + "]not exists");
            kt.e(1320038, "0;1005;" + pvVar.ie());
            return false;
        } else if (file.length() != ((long) pvVar.KN.La)) {
            ps.h("file size:[" + file.length() + "]config item size:[" + pvVar.KN.La + "]");
            kt.e(1320038, "0;1006;" + pvVar.ie());
            return false;
        } else {
            String fileMd5 = TccDiff.fileMd5(str);
            if (fileMd5.compareToIgnoreCase(pvVar.KN.Lb) == 0) {
                String cr = cr(str);
                if (q.cK(cr)) {
                    kt.e(1320038, "0;1008;" + pvVar.ie());
                    return false;
                }
                int i = 0;
                while (i < KE.length && cr.compareToIgnoreCase(KE[i]) != 0) {
                    i++;
                }
                if (i < KE.length) {
                    InputStream inputStream = null;
                    try {
                        ps.g("parse [info.xml]");
                        AssetManager assetManager = (AssetManager) AssetManager.class.newInstance();
                        Method declaredMethod = AssetManager.class.getDeclaredMethod("addAssetPath", new Class[]{String.class});
                        declaredMethod.setAccessible(true);
                        declaredMethod.invoke(assetManager, new Object[]{str});
                        inputStream = assetManager.open("info.xml", 1);
                        XmlPullParser newPullParser = Xml.newPullParser();
                        newPullParser.setInput(inputStream, "UTF-8");
                        int eventType = newPullParser.getEventType();
                        while (eventType != 1) {
                            String name;
                            switch (eventType) {
                                case 2:
                                    name = newPullParser.getName();
                                    int intValue;
                                    if (name.compareTo("id") == 0) {
                                        intValue = Integer.valueOf(newPullParser.nextText()).intValue();
                                        if (intValue != pvVar.KN.KV) {
                                            ps.h("id:[" + intValue + "]config id:[" + pvVar.KN.KV + "]");
                                            kt.e(1320038, "0;1010;" + pvVar.ie());
                                            if (inputStream != null) {
                                                try {
                                                    inputStream.close();
                                                } catch (IOException e) {
                                                    ps.h("e:[" + e + "]");
                                                }
                                            }
                                            return false;
                                        }
                                    } else if (name.compareTo("version_plugin") == 0) {
                                        intValue = Integer.valueOf(newPullParser.nextText()).intValue();
                                        if (intValue != pvVar.KN.KW) {
                                            ps.h("version_r:[" + intValue + "]config version:[" + pvVar.KN.KW + "]");
                                            kt.e(1320038, "0;1011;" + pvVar.ie());
                                            if (inputStream != null) {
                                                try {
                                                    inputStream.close();
                                                } catch (IOException e2) {
                                                    ps.h("e:[" + e2 + "]");
                                                }
                                            }
                                            return false;
                                        }
                                    } else if (name.compareTo("version_host") == 0) {
                                        intValue = Integer.valueOf(newPullParser.nextText()).intValue();
                                        if (intValue != pvVar.KN.KX) {
                                            ps.h("version_nest:[" + intValue + "]config version_nest:[" + pvVar.KN.KX + "]");
                                            kt.e(1320038, "0;1012;" + pvVar.ie());
                                            if (inputStream != null) {
                                                try {
                                                    inputStream.close();
                                                } catch (IOException e22) {
                                                    ps.h("e:[" + e22 + "]");
                                                }
                                            }
                                            return false;
                                        }
                                    } else if (name.compareTo("run_type") == 0) {
                                        intValue = Integer.valueOf(newPullParser.nextText()).intValue();
                                        if (intValue != pvVar.KN.KY) {
                                            ps.h("runtype:[" + intValue + "]config runtype:[" + pvVar.KN.KY + "]");
                                            kt.e(1320038, "0;1013;" + pvVar.ie());
                                            if (inputStream != null) {
                                                try {
                                                    inputStream.close();
                                                } catch (IOException e222) {
                                                    ps.h("e:[" + e222 + "]");
                                                }
                                            }
                                            return false;
                                        }
                                    }
                                case 3:
                                    name = newPullParser.getName();
                                default:
                                    eventType = newPullParser.next();
                            }
                            ps.h("e:[" + e + "]");
                            kt.e(1320038, "0;1014;" + pvVar.ie());
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e3) {
                                    ps.h("e:[" + e3 + "]");
                                }
                            }
                            return false;
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e4) {
                                ps.h("e:[" + e4 + "]");
                            }
                        }
                        kt.e(1320038, "1;;" + pvVar.ie());
                        return true;
                    } catch (Exception e5) {
                        ps.h("e:[" + e5 + "]");
                        kt.e(1320038, "0;1014;" + pvVar.ie());
                        if (inputStream != null) {
                        }
                        return false;
                    } catch (Throwable th) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e6) {
                                ps.h("e:[" + e6 + "]");
                            }
                        }
                    }
                } else {
                    ps.h("Signature error");
                    kt.e(1320038, "0;1009;" + pvVar.ie());
                    return false;
                }
            }
            ps.h("file md5:[" + fileMd5 + "]config item md5:[" + pvVar.KN.Lb + "]");
            kt.e(1320038, "0;1007;" + pvVar.ie());
            return false;
        }
    }

    private void c(pv pvVar) {
        try {
            ps.g("download:[" + pvVar + "]");
            if (this.KK.bQ(pvVar.KN.KV) == null) {
                ps.g("DB no item");
                a(pvVar, false);
            } else if (pvVar.KN.La > 51200 && !i.K(TMSDKContext.getApplicaionContext())) {
                ps.g("no wifi connected and size:[" + pvVar.KN.La + "]");
            } else {
                File file = new File(pvVar.if());
                if (!file.exists()) {
                    file.mkdir();
                }
                pvVar.KP = 2;
                this.KK.d(pvVar);
                if (pvVar.KN.La != -199) {
                    int a;
                    ps.g("start download:[" + pvVar + "]");
                    lx lxVar = new lx(TMSDKContext.getApplicaionContext());
                    lxVar.bP(pvVar.if());
                    lxVar.bQ(pvVar.ie());
                    do {
                        a = lxVar.a(null, pvVar.KN.Lc, false, null);
                    } while (a == -7);
                    ps.g("end download:[" + pvVar + "]");
                    if (this.KK.bQ(pvVar.KN.KV) == null) {
                        a(pvVar, false);
                        return;
                    } else if (a != 0) {
                        ps.g("download failed:[" + a + "]");
                        kt.e(1320036, "0;1004;" + pvVar.ie());
                        pvVar.KP = 1;
                        this.KK.d(pvVar);
                        ic();
                    } else {
                        ps.g("download success");
                        kt.e(1320036, "1;;" + pvVar.ie());
                        if (b(pvVar)) {
                            String str = pvVar.if() + File.separator + pvVar.ie();
                            if (b(str, str, true)) {
                                pvVar.KP = 3;
                                this.KK.d(pvVar);
                                a(pvVar, 1);
                            } else {
                                ps.h("encOrdecFile false, need clean item:" + pvVar + "]");
                                kt.e(1320038, "0;1015;" + pvVar.ie());
                                a(pvVar, false);
                                return;
                            }
                        }
                        ps.h("checkValidity false, need clean item:" + pvVar + "]");
                        a(pvVar, false);
                        return;
                    }
                }
                lu.q(pvVar.KN.Lc, pvVar.if() + File.separator + pvVar.ie());
                String str2 = pvVar.if() + File.separator + pvVar.ie();
                if (b(str2, str2, true)) {
                    pvVar.KP = 3;
                    this.KK.d(pvVar);
                    a(pvVar, 1);
                } else {
                    ps.h("encOrdecFile false, need clean item:" + pvVar + "]");
                    kt.e(1320038, "0;1015;" + pvVar.ie());
                    a(pvVar, false);
                }
            }
        } catch (Throwable th) {
            ps.h("e:[" + th + "]");
            kt.e(1320036, "0;1004;" + pvVar.ie());
            pvVar.KP = 1;
            this.KK.e(pvVar);
            ic();
        }
    }

    private String cr(String str) {
        ps.g("getSignatureMd5-apkPath:[" + str + "]");
        String str2 = "";
        try {
            Object bW = k.bW(str);
            PackageParser packageParser = (PackageParser) bW;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            displayMetrics.setToDefaults();
            Package packageR = (Package) k.a(bW, new File(str), str, displayMetrics, 0);
            PackageParser.class.getMethod("collectCertificates", new Class[]{packageR.getClass(), Integer.TYPE}).invoke(packageParser, new Object[]{packageR, Integer.valueOf(0)});
            if (packageR.mSignatures != null) {
                Signature signature = packageR.mSignatures[0];
                if (signature != null) {
                    str2 = TccDiff.getByteMd5(((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(signature.toByteArray()))).getEncoded());
                    str2 = str2 == null ? null : str2.toUpperCase();
                }
            }
        } catch (Throwable th) {
            ps.h("e:[" + th + "]");
        }
        ps.g("SignatureMd5:[" + str2 + "]");
        return str2;
    }

    public static synchronized pu hW() {
        pu puVar;
        synchronized (pu.class) {
            Class cls = pu.class;
            synchronized (pu.class) {
                if (KL == null) {
                    Class cls2 = pu.class;
                    synchronized (pu.class) {
                        if (KL == null) {
                            ps.g("RoachManager-getInstance");
                            KL = new pu();
                        }
                    }
                }
                puVar = KL;
            }
        }
        return puVar;
    }

    private void ia() {
        if (this.KK == null) {
            this.KK = pw.ih();
        }
        if (this.KH == null) {
            ps.g("startMainJobScheduler");
            this.KH = im.bJ().newFreeHandlerThread("m_d");
            this.KH.start();
            this.KI = new Handler(this.KH.getLooper()) {
                public void handleMessage(Message message) {
                    List<pv> il;
                    if (message.what == 259) {
                        pv pvVar = (pv) message.obj;
                        ps.g("MSG_CONFIG_ITEM_ARRIVE aConfigItem:[" + pvVar + "]");
                        if (pvVar == null || pvVar.KN == null) {
                            ps.h("MSG_CONFIG_ITEM_ARRIVE item no base info");
                            return;
                        }
                        pv bQ = pu.this.KK.bQ(pvVar.KN.KV);
                        if (bQ == null) {
                            ps.g("item not exist");
                            if (pvVar.KO == 1) {
                                ps.g("op_add config item");
                                pu.this.KK.e(pvVar);
                            } else if (pvVar.KO == 2) {
                                ps.g("op_del config item");
                                pu.this.a(pvVar, true);
                            }
                        } else {
                            ps.g("item exist, config version:[" + pvVar.KN.KW + "]local version:[" + bQ.KN.KW + "]state:[" + bQ.KP + "]");
                            if (pvVar.KN.KW >= bQ.KN.KW) {
                                if (pvVar.KO == 1) {
                                    ps.g("op_add config item");
                                    if (bQ.KP != 3) {
                                        if (pvVar.KN.KW > bQ.KN.KW) {
                                            ps.g("has new version and not local state, update item");
                                            pu.this.KK.d(pvVar);
                                        }
                                    } else if (pvVar.KN.KW != bQ.KN.KW) {
                                        ps.g("has new version, clean old item and download new version");
                                        pu.this.a(bQ, true);
                                        pu.this.KK.e(pvVar);
                                    } else {
                                        ps.g("same version, try load");
                                        pu.this.a(bQ, 1);
                                    }
                                } else if (pvVar.KO == 2) {
                                    ps.g("op_del config item");
                                    pu.this.a(bQ, true);
                                }
                            }
                        }
                        pu.this.KI.removeMessages(260);
                        pu.this.KI.sendEmptyMessage(260);
                        pu.this.KI.removeMessages(258);
                        pu.this.KI.sendEmptyMessage(258);
                        pu.this.KI.removeMessages(262);
                        pu.this.KI.sendEmptyMessage(262);
                    } else if (message.what == 257) {
                        ps.g("MSG_SCAN_AUTO_RUN_ITEMS");
                        il = pu.this.KK.il();
                        if (il != null && il.size() > 0) {
                            ps.g("auto run item size:[" + il.size() + "]");
                            for (pv pvVar2 : il) {
                                if (pvVar2.KP == 3 && pvVar2.KO == 1 && pvVar2.KN != null) {
                                    if ((pvVar2.KN.KZ >= System.currentTimeMillis()) && new File(pvVar2.if() + File.separator + pvVar2.ie()).exists()) {
                                        pu.this.a(pvVar2, 1);
                                    }
                                }
                            }
                        }
                    } else if (message.what == 258) {
                        ps.g("MSG_DOWNLOAD_ITEMS");
                        il = pu.this.KK.ik();
                        if (il != null) {
                            for (pv pvVar22 : il) {
                                pu.this.c(pvVar22);
                            }
                            if (pu.this.KK.ik() == null) {
                                pu.this.id();
                            } else {
                                pu.this.ic();
                            }
                        }
                    } else if (message.what == 260) {
                        ps.g("MSG_SCAN_CLEAN_ITEMS");
                        il = pu.this.KK.ij();
                        if (il != null) {
                            for (pv pvVar222 : il) {
                                pu.this.a(pvVar222, true);
                            }
                        }
                    } else if (message.what == 261) {
                        ps.g("MSG_RELEASE_ITEM");
                        ActionI actionI = (ActionI) message.obj;
                        if (actionI != null) {
                            try {
                                for (px pxVar : pu.this.KG.keySet()) {
                                    if (((ActionI) pu.this.KG.get(pxVar)) == actionI) {
                                        ps.g("releaseItem[" + actionI + "]");
                                        pu.this.KG.remove(pxVar);
                                        break;
                                    }
                                }
                            } catch (Throwable th) {
                                ps.h("releaseItem-error:[" + th + "]");
                            }
                        }
                    } else if (message.what == 262) {
                        pu.this.ib();
                    }
                }
            };
        }
    }

    private void ib() {
        if (this.KH != null) {
            if (this.KI.hasMessages(257) || this.KI.hasMessages(258) || this.KI.hasMessages(259) || this.KI.hasMessages(260) || this.KI.hasMessages(261)) {
                ps.g("stopMainJobScheduler, but has messages, not stop");
                this.KI.removeMessages(262);
                this.KI.sendEmptyMessage(262);
                return;
            }
            if (this.KK.getCount() <= 0) {
                gf.S().l(Boolean.valueOf(false));
            }
            ps.g("stopMainJobScheduler");
            this.KH.quit();
            this.KH = null;
            this.KI = null;
        }
        if (this.KK != null) {
            this.KK.ii();
            this.KK = null;
        }
    }

    private void ic() {
        if (this.KJ == null) {
            ps.g("addNetworkChange");
            this.KJ = new a() {
                public void dC() {
                    pu.this.hZ();
                }

                public void dD() {
                }
            };
            oo.A(TMSDKContext.getApplicaionContext()).a(this.KJ);
        }
    }

    private void id() {
        if (this.KJ != null) {
            ps.g("removeNetworkChange");
            oo.A(TMSDKContext.getApplicaionContext()).b(this.KJ);
            this.KJ = null;
        }
    }

    public synchronized void a(ActionI actionI) {
        ia();
        Message.obtain(this.KI, 261, actionI).sendToTarget();
        this.KI.sendEmptyMessage(262);
    }

    /* JADX WARNING: Missing block: B:7:0x0036, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void b(ju.a aVar) {
        ps.g("onRecvPush-[" + aVar + "]");
        pv pvVar = new pv();
        pvVar.KN = new px();
        try {
            if (hX() == aVar.tA.Y) {
                t tVar = (t) nn.a(aVar.tA.ae, new t(), false);
                long j = ((long) aVar.tA.ag.R) * 1000;
                if ((j >= System.currentTimeMillis() ? 1 : null) == null) {
                    ps.h("config item expired");
                    return;
                }
                if (!TMSDKContext.is_armeabi()) {
                    if (!TMSDKContext.is_arm64v8a()) {
                        ps.h("[not armeabi\\arm64-v8a]");
                        kt.e(1320035, "0;1002;" + pvVar.ie());
                        return;
                    }
                }
                Calendar instance = Calendar.getInstance();
                instance.setTimeInMillis(j);
                ps.g("validEndTime[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(instance.getTime()) + "]");
                pvVar.KO = com.tencent.tcuser.util.a.av((String) tVar.ar.get(0));
                pvVar.KN.KV = com.tencent.tcuser.util.a.av((String) tVar.ar.get(1));
                pvVar.KN.KW = com.tencent.tcuser.util.a.av((String) tVar.ar.get(2));
                pvVar.KN.KX = com.tencent.tcuser.util.a.av((String) tVar.ar.get(3));
                pvVar.KN.KY = com.tencent.tcuser.util.a.av((String) tVar.ar.get(4));
                pvVar.KN.La = com.tencent.tcuser.util.a.av((String) tVar.ar.get(5));
                pvVar.KN.Lb = (String) tVar.ar.get(6);
                pvVar.KN.Lc = (String) tVar.ar.get(7);
                pvVar.KN.KZ = j;
                pvVar.KP = 1;
                if (pvVar.KO != 1) {
                    if (pvVar.KO != 2) {
                        ps.h("config item op error:[" + pvVar.KO + "]");
                        kt.e(1320035, "0;1000;" + pvVar.ie());
                        return;
                    }
                }
                ps.g("push config item:[" + pvVar + "]");
                int i = 0;
                while (i < KD.length) {
                    if (pvVar.KN.KX == KD[i]) {
                        break;
                    }
                    i++;
                }
                if (i < KD.length) {
                    kt.e(1320035, "1;;" + pvVar.ie());
                    gf.S().l(Boolean.valueOf(true));
                    a(pvVar);
                } else {
                    ps.h("current nest not support, roach nest id:[" + pvVar.KN.KX + "]");
                    kt.e(1320035, "0;1001;" + pvVar.ie());
                }
            }
        } catch (Throwable th) {
            kt.e(1320035, "0;1003;" + pvVar.ie());
            ps.h("e:[" + th + "]");
        }
    }

    public int hX() {
        return 519;
    }

    public synchronized void hY() {
        ia();
        this.KI.removeMessages(260);
        this.KI.sendEmptyMessage(260);
        this.KI.removeMessages(257);
        this.KI.sendEmptyMessage(257);
        this.KI.removeMessages(258);
        this.KI.sendEmptyMessage(258);
        this.KI.removeMessages(262);
        this.KI.sendEmptyMessage(262);
    }

    public synchronized void hZ() {
        ps.g("onNetworkConnected");
        if (this.KK == null) {
            this.KK = pw.ih();
        }
        if (this.KK.ik() != null) {
            ia();
            this.KI.removeMessages(258);
            this.KI.sendEmptyMessage(258);
            this.KI.removeMessages(262);
            this.KI.sendEmptyMessage(262);
            return;
        }
        id();
    }
}
