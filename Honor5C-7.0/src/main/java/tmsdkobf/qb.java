package tmsdkobf;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
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
import tmsdk.bg.module.wifidetect.WifiDetectManager;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.common.module.aresengine.SystemCallLogFilterConsts;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.d;
import tmsdk.common.utils.q;

/* compiled from: Unknown */
final class qb extends BaseManagerC {
    private q Jg;
    private pz Jh;
    private CertificateFactory Ji;
    private Context mContext;
    private PackageManager mPackageManager;

    qb() {
        this.mPackageManager = null;
        this.mContext = null;
        this.Ji = null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Certificate a(Signature signature) {
        InputStream byteArrayInputStream = new ByteArrayInputStream(signature.toByteArray());
        try {
            X509Certificate x509Certificate = (X509Certificate) this.Ji.generateCertificate(byteArrayInputStream);
            if (byteArrayInputStream == null) {
                return x509Certificate;
            }
            try {
                byteArrayInputStream.close();
                return x509Certificate;
            } catch (IOException e) {
                e.printStackTrace();
                return x509Certificate;
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
    }

    private py a(String str, py pyVar, int i) {
        PackageInfo packageArchiveInfo;
        try {
            packageArchiveInfo = this.mPackageManager.getPackageArchiveInfo(str, cp(i));
        } catch (RuntimeException e) {
            nd.a(e);
            packageArchiveInfo = null;
        }
        if (packageArchiveInfo == null) {
            return null;
        }
        if ((i & SystemCallLogFilterConsts.NOTIFY_SHORT_CALL) != 0) {
            pyVar.put("pkgName", packageArchiveInfo.packageName);
        }
        if ((i & WifiDetectManager.SECURITY_NONE) != 0) {
            pyVar.put(CheckVersionField.CHECK_VERSION_VERSION, packageArchiveInfo.versionName);
        }
        if ((i & SystemCallLogFilterConsts.ANONYMOUS_CALL) != 0) {
            pyVar.put("versionCode", Integer.valueOf(packageArchiveInfo.versionCode));
        }
        if ((i & 32) != 0) {
            pyVar.put("permissions", packageArchiveInfo.requestedPermissions);
        }
        if ((i & 2048) != 0) {
            pyVar.setAppName(this.mPackageManager.getApplicationLabel(packageArchiveInfo.applicationInfo).toString());
        }
        if (!((i & IncomingSmsFilterConsts.PAY_SMS) == 0 || packageArchiveInfo.applicationInfo == null)) {
            pyVar.put("uid", Integer.valueOf(packageArchiveInfo.applicationInfo.uid));
        }
        return pyVar;
    }

    private static boolean a(Context context, Drawable drawable) {
        try {
            float f = context.getResources().getDisplayMetrics().density;
            int intrinsicWidth = (int) (((float) drawable.getIntrinsicWidth()) / f);
            int intrinsicHeight = (int) (((float) drawable.getIntrinsicHeight()) / f);
            if (intrinsicWidth > SmsCheckResult.ESCT_320 || intrinsicHeight > SmsCheckResult.ESCT_320) {
                d.d("SoftwareManagerImpl", "too large: (" + intrinsicWidth + ", " + intrinsicHeight + ")");
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private py b(String str, py pyVar, int i) {
        Resources resources;
        Object drawable;
        Signature[] signatureArr;
        try {
            Object cI = ne.cI(str);
            File file = new File(str);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            displayMetrics.setToDefaults();
            Object a = ne.a(cI, file, str, displayMetrics, 0);
            if (a == null) {
                return null;
            }
            String str2;
            ApplicationInfo applicationInfo;
            X509Certificate x509Certificate;
            if ((i & SystemCallLogFilterConsts.NOTIFY_SHORT_CALL) != 0) {
                str2 = (String) ng.b(a, "packageName");
                if (str2 != null) {
                    pyVar.put("pkgName", str2);
                }
            }
            if ((i & WifiDetectManager.SECURITY_NONE) != 0) {
                str2 = (String) ng.b(a, "mVersionName");
                if (str2 != null) {
                    pyVar.put(CheckVersionField.CHECK_VERSION_VERSION, str2);
                }
            }
            if ((i & SystemCallLogFilterConsts.ANONYMOUS_CALL) != 0) {
                pyVar.put("versionCode", Integer.valueOf(((Integer) ng.b(a, "mVersionCode")).intValue()));
            }
            if ((i & 32) != 0) {
                ArrayList arrayList = (ArrayList) ng.b(a, "requestedPermissions");
                if (arrayList != null) {
                    pyVar.put("permissions", arrayList.toArray());
                }
            }
            if ((i & IncomingSmsFilterConsts.PAY_SMS) == 0) {
                applicationInfo = null;
            } else {
                applicationInfo = (ApplicationInfo) ng.b(a, "applicationInfo");
                if (applicationInfo != null) {
                    pyVar.put("uid", Integer.valueOf(applicationInfo.uid));
                }
            }
            if ((i & 2048) != 0 || (i & 4) != 0) {
                if (applicationInfo == null) {
                    applicationInfo = (ApplicationInfo) ng.b(a, "applicationInfo");
                }
                ApplicationInfo applicationInfo2 = applicationInfo;
                if ((i & 2048) == 0 || applicationInfo2 == null) {
                    resources = null;
                } else {
                    Object obj;
                    if (applicationInfo2.labelRes == 0) {
                        obj = null;
                        resources = null;
                    } else {
                        try {
                            resources = cV(str);
                            try {
                                obj = resources.getText(applicationInfo2.labelRes);
                            } catch (Exception e) {
                                obj = null;
                                if (obj != null) {
                                    if (obj.toString().length() > 0) {
                                        pyVar.put("appName", obj);
                                        if (applicationInfo2.icon != 0) {
                                            if (resources == null) {
                                                resources = cV(str);
                                            }
                                            try {
                                                drawable = resources.getDrawable(applicationInfo2.icon);
                                            } catch (Throwable th) {
                                                d.c("SoftwareManagerImpl", "" + str + " | res.getDrawable() error: " + th);
                                            }
                                            if (drawable == null) {
                                                pyVar.put("icon", drawable);
                                                if ((i & 16) != 0) {
                                                    ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                                    signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                                    x509Certificate = (X509Certificate) a(signatureArr[0]);
                                                    if (x509Certificate != null) {
                                                        try {
                                                            drawable = nb.p(x509Certificate.getEncoded());
                                                        } catch (CertificateEncodingException e2) {
                                                            e2.printStackTrace();
                                                            drawable = null;
                                                        }
                                                        pyVar.put("signatureCermMD5", drawable);
                                                    }
                                                }
                                                return pyVar;
                                            }
                                            drawable = this.mPackageManager.getApplicationIcon(applicationInfo2);
                                            pyVar.put("icon", drawable);
                                            if ((i & 16) != 0) {
                                                ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                                signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                                x509Certificate = (X509Certificate) a(signatureArr[0]);
                                                if (x509Certificate != null) {
                                                    drawable = nb.p(x509Certificate.getEncoded());
                                                    pyVar.put("signatureCermMD5", drawable);
                                                }
                                            }
                                            return pyVar;
                                        }
                                        drawable = null;
                                        if (drawable == null) {
                                            pyVar.put("icon", drawable);
                                            if ((i & 16) != 0) {
                                                ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                                signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                                x509Certificate = (X509Certificate) a(signatureArr[0]);
                                                if (x509Certificate != null) {
                                                    drawable = nb.p(x509Certificate.getEncoded());
                                                    pyVar.put("signatureCermMD5", drawable);
                                                }
                                            }
                                            return pyVar;
                                        }
                                        drawable = this.mPackageManager.getApplicationIcon(applicationInfo2);
                                        pyVar.put("icon", drawable);
                                        if ((i & 16) != 0) {
                                            ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                            signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                            x509Certificate = (X509Certificate) a(signatureArr[0]);
                                            if (x509Certificate != null) {
                                                drawable = nb.p(x509Certificate.getEncoded());
                                                pyVar.put("signatureCermMD5", drawable);
                                            }
                                        }
                                        return pyVar;
                                    }
                                }
                                obj = this.mPackageManager.getApplicationLabel(applicationInfo2);
                                pyVar.put("appName", obj);
                                if (applicationInfo2.icon != 0) {
                                    if (resources == null) {
                                        resources = cV(str);
                                    }
                                    drawable = resources.getDrawable(applicationInfo2.icon);
                                    if (drawable == null) {
                                        pyVar.put("icon", drawable);
                                        if ((i & 16) != 0) {
                                            ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                            signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                            x509Certificate = (X509Certificate) a(signatureArr[0]);
                                            if (x509Certificate != null) {
                                                drawable = nb.p(x509Certificate.getEncoded());
                                                pyVar.put("signatureCermMD5", drawable);
                                            }
                                        }
                                        return pyVar;
                                    }
                                    drawable = this.mPackageManager.getApplicationIcon(applicationInfo2);
                                    pyVar.put("icon", drawable);
                                    if ((i & 16) != 0) {
                                        ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                        signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                        x509Certificate = (X509Certificate) a(signatureArr[0]);
                                        if (x509Certificate != null) {
                                            drawable = nb.p(x509Certificate.getEncoded());
                                            pyVar.put("signatureCermMD5", drawable);
                                        }
                                    }
                                    return pyVar;
                                }
                                drawable = null;
                                if (drawable == null) {
                                    pyVar.put("icon", drawable);
                                    if ((i & 16) != 0) {
                                        ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                        signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                        x509Certificate = (X509Certificate) a(signatureArr[0]);
                                        if (x509Certificate != null) {
                                            drawable = nb.p(x509Certificate.getEncoded());
                                            pyVar.put("signatureCermMD5", drawable);
                                        }
                                    }
                                    return pyVar;
                                }
                                drawable = this.mPackageManager.getApplicationIcon(applicationInfo2);
                                pyVar.put("icon", drawable);
                                if ((i & 16) != 0) {
                                    ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                    signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                    x509Certificate = (X509Certificate) a(signatureArr[0]);
                                    if (x509Certificate != null) {
                                        drawable = nb.p(x509Certificate.getEncoded());
                                        pyVar.put("signatureCermMD5", drawable);
                                    }
                                }
                                return pyVar;
                            }
                        } catch (Exception e3) {
                            resources = null;
                            obj = null;
                            if (obj != null) {
                                if (obj.toString().length() > 0) {
                                    pyVar.put("appName", obj);
                                    if (applicationInfo2.icon != 0) {
                                        if (resources == null) {
                                            resources = cV(str);
                                        }
                                        drawable = resources.getDrawable(applicationInfo2.icon);
                                        if (drawable == null) {
                                            pyVar.put("icon", drawable);
                                            if ((i & 16) != 0) {
                                                ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                                signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                                x509Certificate = (X509Certificate) a(signatureArr[0]);
                                                if (x509Certificate != null) {
                                                    drawable = nb.p(x509Certificate.getEncoded());
                                                    pyVar.put("signatureCermMD5", drawable);
                                                }
                                            }
                                            return pyVar;
                                        }
                                        drawable = this.mPackageManager.getApplicationIcon(applicationInfo2);
                                        pyVar.put("icon", drawable);
                                        if ((i & 16) != 0) {
                                            ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                            signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                            x509Certificate = (X509Certificate) a(signatureArr[0]);
                                            if (x509Certificate != null) {
                                                drawable = nb.p(x509Certificate.getEncoded());
                                                pyVar.put("signatureCermMD5", drawable);
                                            }
                                        }
                                        return pyVar;
                                    }
                                    drawable = null;
                                    if (drawable == null) {
                                        pyVar.put("icon", drawable);
                                        if ((i & 16) != 0) {
                                            ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                            signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                            x509Certificate = (X509Certificate) a(signatureArr[0]);
                                            if (x509Certificate != null) {
                                                drawable = nb.p(x509Certificate.getEncoded());
                                                pyVar.put("signatureCermMD5", drawable);
                                            }
                                        }
                                        return pyVar;
                                    }
                                    drawable = this.mPackageManager.getApplicationIcon(applicationInfo2);
                                    pyVar.put("icon", drawable);
                                    if ((i & 16) != 0) {
                                        ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                        signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                        x509Certificate = (X509Certificate) a(signatureArr[0]);
                                        if (x509Certificate != null) {
                                            drawable = nb.p(x509Certificate.getEncoded());
                                            pyVar.put("signatureCermMD5", drawable);
                                        }
                                    }
                                    return pyVar;
                                }
                            }
                            obj = this.mPackageManager.getApplicationLabel(applicationInfo2);
                            pyVar.put("appName", obj);
                            if (applicationInfo2.icon != 0) {
                                if (resources == null) {
                                    resources = cV(str);
                                }
                                drawable = resources.getDrawable(applicationInfo2.icon);
                                if (drawable == null) {
                                    pyVar.put("icon", drawable);
                                    if ((i & 16) != 0) {
                                        ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                        signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                        x509Certificate = (X509Certificate) a(signatureArr[0]);
                                        if (x509Certificate != null) {
                                            drawable = nb.p(x509Certificate.getEncoded());
                                            pyVar.put("signatureCermMD5", drawable);
                                        }
                                    }
                                    return pyVar;
                                }
                                drawable = this.mPackageManager.getApplicationIcon(applicationInfo2);
                                pyVar.put("icon", drawable);
                                if ((i & 16) != 0) {
                                    ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                    signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                    x509Certificate = (X509Certificate) a(signatureArr[0]);
                                    if (x509Certificate != null) {
                                        drawable = nb.p(x509Certificate.getEncoded());
                                        pyVar.put("signatureCermMD5", drawable);
                                    }
                                }
                                return pyVar;
                            }
                            drawable = null;
                            if (drawable == null) {
                                pyVar.put("icon", drawable);
                                if ((i & 16) != 0) {
                                    ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                    signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                    x509Certificate = (X509Certificate) a(signatureArr[0]);
                                    if (x509Certificate != null) {
                                        drawable = nb.p(x509Certificate.getEncoded());
                                        pyVar.put("signatureCermMD5", drawable);
                                    }
                                }
                                return pyVar;
                            }
                            drawable = this.mPackageManager.getApplicationIcon(applicationInfo2);
                            pyVar.put("icon", drawable);
                            if ((i & 16) != 0) {
                                ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                                signatureArr = (Signature[]) ng.b(a, "mSignatures");
                                x509Certificate = (X509Certificate) a(signatureArr[0]);
                                if (x509Certificate != null) {
                                    drawable = nb.p(x509Certificate.getEncoded());
                                    pyVar.put("signatureCermMD5", drawable);
                                }
                            }
                            return pyVar;
                        }
                    }
                    if (obj != null) {
                        if (obj.toString().length() > 0) {
                            pyVar.put("appName", obj);
                        }
                    }
                    obj = this.mPackageManager.getApplicationLabel(applicationInfo2);
                    pyVar.put("appName", obj);
                }
                if (!((i & 4) == 0 || applicationInfo2 == null)) {
                    if (applicationInfo2.icon != 0) {
                        if (resources == null) {
                            resources = cV(str);
                        }
                        drawable = resources.getDrawable(applicationInfo2.icon);
                        if (drawable == null || a(this.mContext, drawable)) {
                            drawable = this.mPackageManager.getApplicationIcon(applicationInfo2);
                        }
                        pyVar.put("icon", drawable);
                    }
                    drawable = null;
                    if (drawable == null) {
                        pyVar.put("icon", drawable);
                    }
                    drawable = this.mPackageManager.getApplicationIcon(applicationInfo2);
                    pyVar.put("icon", drawable);
                }
            }
            if ((i & 16) != 0) {
                ng.a(cI, "collectCertificates", new Object[]{a, Integer.valueOf(0)});
                signatureArr = (Signature[]) ng.b(a, "mSignatures");
                if (signatureArr != null && signatureArr.length > 0) {
                    x509Certificate = (X509Certificate) a(signatureArr[0]);
                    if (x509Certificate != null) {
                        drawable = nb.p(x509Certificate.getEncoded());
                        pyVar.put("signatureCermMD5", drawable);
                    }
                }
            }
            return pyVar;
        } catch (Exception e4) {
            return null;
        }
    }

    private Resources cV(String str) throws Exception {
        Resources resources = this.mContext.getResources();
        ng.a(ng.a("android.content.res.AssetManager", null), "addAssetPath", new Object[]{str});
        return (Resources) ng.a("android.content.res.Resources", new Object[]{r1, resources.getDisplayMetrics(), resources.getConfiguration()});
    }

    private int cp(int i) {
        int i2 = 0;
        if ((i & 16) != 0) {
            i2 = 64;
        }
        return (i & 32) == 0 ? i2 : i2 | 4096;
    }

    public py b(py pyVar, int i) {
        String str = (String) pyVar.get("apkPath");
        if (!this.Jg.dq(str)) {
            return null;
        }
        int i2;
        if ((i & 1) == 0) {
            i2 = i;
        } else {
            pyVar.put("isSystem", Boolean.FALSE);
            i2 = ((i | SystemCallLogFilterConsts.NOTIFY_SHORT_CALL) | 2048) | IncomingSmsFilterConsts.PAY_SMS;
        }
        if ((i2 & 2) != 0) {
            pyVar.put("isSystem", Boolean.FALSE);
            i2 = (i2 | SystemCallLogFilterConsts.NOTIFY_SHORT_CALL) | IncomingSmsFilterConsts.PAY_SMS;
        }
        if ((i2 & 8) != 0) {
            File file = new File(str);
            pyVar.put("size", Long.valueOf(file.length()));
            pyVar.put("lastModified", Long.valueOf(file.lastModified()));
            i2 = (i2 | WifiDetectManager.SECURITY_NONE) | SystemCallLogFilterConsts.ANONYMOUS_CALL;
        }
        if ((i2 & 64) != 0) {
            pyVar.put("apkPath", str);
            pyVar.put("isApk", Boolean.valueOf(true));
        }
        return ((i2 & 2048) == 0 && (i2 & 4) == 0 && (i2 & 16) == 0) ? a(str, pyVar, i2) : b(str, pyVar, i2);
    }

    public int getSingletonType() {
        return 2;
    }

    public py i(String str, int i) {
        py pyVar = new py();
        pyVar.put("apkPath", str);
        return b(pyVar, i);
    }

    public void onCreate(Context context) {
        this.mContext = context;
        this.Jg = new q();
        this.Jh = new pz();
        this.mPackageManager = context.getPackageManager();
        try {
            this.Ji = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            d.d("SoftwareManagerImpl", e.getLocalizedMessage());
        }
    }
}
