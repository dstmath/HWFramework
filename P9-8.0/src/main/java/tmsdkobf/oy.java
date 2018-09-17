package tmsdkobf;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.f;
import tmsdk.common.utils.v;

public final class oy extends BaseManagerC {
    private v Jg;
    private ow Jh;
    private CertificateFactory Ji = null;
    private Context mContext = null;
    private PackageManager mPackageManager = null;

    private static Certificate a(CertificateFactory certificateFactory, Signature signature) {
        InputStream byteArrayInputStream = new ByteArrayInputStream(signature.toByteArray());
        Certificate certificate = null;
        try {
            certificate = (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (CertificateException e2) {
            e2.printStackTrace();
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (Exception e4) {
            e4.printStackTrace();
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
        }
        return certificate;
    }

    private ov a(String str, ov ovVar, int i) {
        PackageInfo packageInfo = null;
        try {
            Class cls = oy.class;
            synchronized (oy.class) {
                packageInfo = this.mPackageManager.getPackageArchiveInfo(str, bF(i));
                if (packageInfo == null) {
                    return null;
                }
                if ((i & 128) != 0) {
                    ovVar.put("pkgName", packageInfo.packageName);
                }
                if ((i & 256) != 0) {
                    ovVar.put("version", packageInfo.versionName);
                }
                if ((i & 512) != 0) {
                    ovVar.put("versionCode", Integer.valueOf(packageInfo.versionCode));
                }
                if ((i & 32) != 0) {
                    ovVar.put("permissions", packageInfo.requestedPermissions);
                }
                if ((i & 2048) != 0) {
                    ovVar.setAppName(this.mPackageManager.getApplicationLabel(packageInfo.applicationInfo).toString());
                }
                if (!((i & IncomingSmsFilterConsts.PAY_SMS) == 0 || packageInfo.applicationInfo == null)) {
                    ovVar.put("uid", Integer.valueOf(packageInfo.applicationInfo.uid));
                }
                return ovVar;
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private static boolean a(Context -l_2_R, Drawable drawable) {
        try {
            Context currentContext = TMSDKContext.getCurrentContext();
            if (currentContext != null) {
                -l_2_R = currentContext;
            }
            float f = -l_2_R.getResources().getDisplayMetrics().density;
            int intrinsicWidth = (int) (((float) drawable.getIntrinsicWidth()) / f);
            int intrinsicHeight = (int) (((float) drawable.getIntrinsicHeight()) / f);
            if (intrinsicWidth > SmsCheckResult.ESCT_320 || intrinsicHeight > SmsCheckResult.ESCT_320) {
                f.f("SoftwareManagerImpl", "too large: (" + intrinsicWidth + ", " + intrinsicHeight + ")");
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:105:0x01b3, code:
            if (r1.toString().length() > 0) goto L_0x0197;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ov b(String str, ov ovVar, int i) {
        try {
            Object bW = mf.bW(str);
            File file = new File(str);
            if (!file.exists()) {
                return null;
            }
            DisplayMetrics displayMetrics = new DisplayMetrics();
            displayMetrics.setToDefaults();
            Class cls = oy.class;
            synchronized (oy.class) {
                Object a = mf.a(bW, file, str, displayMetrics, 0);
                if (a == null || !file.exists()) {
                    return null;
                }
                String str2;
                if ((i & 128) != 0 && file.exists()) {
                    str2 = (String) mh.a(a, "packageName");
                    if (str2 != null) {
                        ovVar.put("pkgName", str2);
                    }
                }
                if ((i & 256) != 0 && file.exists()) {
                    str2 = (String) mh.a(a, "mVersionName");
                    if (str2 != null) {
                        ovVar.put("version", str2);
                    }
                }
                if ((i & 512) != 0 && file.exists()) {
                    ovVar.put("versionCode", Integer.valueOf(((Integer) mh.a(a, "mVersionCode")).intValue()));
                }
                if ((i & 32) != 0 && file.exists()) {
                    ArrayList arrayList = (ArrayList) mh.a(a, "requestedPermissions");
                    if (arrayList != null) {
                        ovVar.put("permissions", arrayList.toArray());
                    }
                }
                ApplicationInfo applicationInfo = null;
                if ((i & IncomingSmsFilterConsts.PAY_SMS) != 0 && file.exists()) {
                    applicationInfo = (ApplicationInfo) mh.a(a, "applicationInfo");
                    if (applicationInfo != null) {
                        ovVar.put("uid", Integer.valueOf(applicationInfo.uid));
                    }
                }
                if ((i & 8192) != 0 && file.exists()) {
                    if (VERSION.SDK_INT > 7) {
                        applicationInfo = (ApplicationInfo) mh.a(a, "applicationInfo");
                        if (applicationInfo != null) {
                            ovVar.put("installedOnSdcard", Boolean.valueOf((applicationInfo.flags & 262144) != 0));
                        }
                    } else if (str.startsWith("/data")) {
                        ovVar.put("installedOnSdcard", Boolean.valueOf(false));
                    } else {
                        ovVar.put("installedOnSdcard", Boolean.valueOf(true));
                    }
                }
                if (!((i & 2048) == 0 && (i & 4) == 0) && file.exists()) {
                    Object obj;
                    Resources resources = null;
                    if (applicationInfo == null) {
                        applicationInfo = (ApplicationInfo) mh.a(a, "applicationInfo");
                    }
                    if (!((i & 2048) == 0 || applicationInfo == null)) {
                        obj = null;
                        if (applicationInfo.labelRes != 0) {
                            try {
                                resources = co(str);
                                obj = resources.getText(applicationInfo.labelRes);
                            } catch (Throwable th) {
                            }
                        }
                        if (obj != null) {
                        }
                        obj = this.mPackageManager.getApplicationLabel(applicationInfo);
                        ovVar.put("appName", obj);
                    }
                    if (!((i & 4) == 0 || applicationInfo == null)) {
                        Drawable drawable = null;
                        if (applicationInfo.icon != 0) {
                            if (resources == null) {
                                resources = co(str);
                            }
                            try {
                                drawable = resources.getDrawable(applicationInfo.icon);
                            } catch (Throwable th2) {
                                f.e("SoftwareManagerImpl", "" + str + " | res.getDrawable() error: " + th2);
                            }
                        }
                        if (drawable == null || a(this.mContext, drawable)) {
                            obj = this.mPackageManager.getApplicationIcon(applicationInfo);
                        }
                        ovVar.put("icon", obj);
                    }
                }
                if ((i & 16) != 0 && file.exists()) {
                    mh.a(bW, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                    Signature[] signatureArr = (Signature[]) mh.a(a, "mSignatures");
                    if (signatureArr != null && signatureArr.length > 0) {
                        X509Certificate x509Certificate = (X509Certificate) a(this.Ji, signatureArr[0]);
                        if (x509Certificate != null) {
                            Object obj2 = null;
                            try {
                                obj2 = mc.n(x509Certificate.getEncoded());
                            } catch (CertificateEncodingException e) {
                                e.printStackTrace();
                            }
                            ovVar.put("signatureCermMD5", obj2);
                        }
                    }
                }
                return ovVar;
            }
        } catch (Throwable th3) {
            return null;
        }
    }

    private int bF(int i) {
        int i2 = 0;
        if ((i & 16) != 0) {
            i2 = 64;
        }
        return (i & 32) == 0 ? i2 : i2 | 4096;
    }

    private Resources co(String str) throws Exception {
        Context currentContext = TMSDKContext.getCurrentContext();
        if (currentContext == null) {
            currentContext = this.mContext;
        }
        Resources resources = currentContext.getResources();
        mh.a(mh.a("android.content.res.AssetManager", null), "addAssetPath", new Object[]{str});
        return (Resources) mh.a("android.content.res.Resources", new Object[]{r2, resources.getDisplayMetrics(), resources.getConfiguration()});
    }

    public static List<String> h(String str, int i) {
        List<String> arrayList = new ArrayList();
        try {
            PackageInfo packageInfo = TMSDKContext.getApplicaionContext().getPackageManager().getPackageInfo(str, 64);
            if (!(packageInfo == null || packageInfo.signatures == null || packageInfo.signatures.length <= 0)) {
                Signature[] signatureArr = packageInfo.signatures;
                int i2 = 0;
                while (i2 < signatureArr.length && i2 < i) {
                    X509Certificate x509Certificate = (X509Certificate) a(CertificateFactory.getInstance("X.509"), signatureArr[i2]);
                    if (x509Certificate != null) {
                        try {
                            arrayList.add(mc.n(x509Certificate.getEncoded()));
                        } catch (Throwable e) {
                            f.c("SoftwareManagerImpl", "extractPkgCertMd5s(), CertificateEncodingException: " + e, e);
                            e.printStackTrace();
                        }
                    }
                    i2++;
                }
            }
        } catch (Throwable e2) {
            f.c("SoftwareManagerImpl", "extractPkgCertMd5s(), Exception: " + e2, e2);
        }
        return arrayList;
    }

    public ov c(ov ovVar, int i) {
        String str = (String) ovVar.get("apkPath");
        try {
            if (!this.Jg.cO(str)) {
                return null;
            }
            int i2;
            if ((i & 1) == 0) {
                i2 = i;
            } else {
                ovVar.put("isSystem", Boolean.FALSE);
                i2 = ((i | 128) | 2048) | IncomingSmsFilterConsts.PAY_SMS;
            }
            if ((i2 & 2) != 0) {
                ovVar.put("isSystem", Boolean.FALSE);
                i2 = (i2 | 128) | IncomingSmsFilterConsts.PAY_SMS;
            }
            if ((i2 & 8) != 0) {
                File file = new File(str);
                ovVar.put("size", Long.valueOf(file.length()));
                ovVar.put("lastModified", Long.valueOf(file.lastModified()));
                i2 = (i2 | 256) | 512;
            }
            if ((i2 & 64) != 0) {
                ovVar.put("apkPath", str);
                ovVar.put("isApk", Boolean.valueOf(true));
            }
            return ((i2 & 2048) == 0 && (i2 & 4) == 0 && (i2 & 16) == 0) ? a(str, ovVar, i2) : b(str, ovVar, i2);
        } catch (Throwable th) {
            return null;
        }
    }

    public ov g(String str, int i) {
        ov ovVar = new ov();
        ovVar.put("apkPath", str);
        return c(ovVar, i);
    }

    public int getSingletonType() {
        return 2;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        this.Jg = new v();
        this.Jh = new ow();
        this.mPackageManager = context.getPackageManager();
        try {
            this.Ji = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            f.f("SoftwareManagerImpl", e.getLocalizedMessage());
        }
    }
}
