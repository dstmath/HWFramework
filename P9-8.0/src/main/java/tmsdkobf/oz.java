package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.text.TextUtils;
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
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.utils.f;

public final class oz extends BaseManagerC implements pa {
    private CertificateFactory Ji = null;
    private Context mContext = null;
    private PackageManager mPackageManager = null;

    private Certificate a(Signature signature) {
        InputStream byteArrayInputStream = new ByteArrayInputStream(signature.toByteArray());
        Certificate certificate = null;
        try {
            certificate = (X509Certificate) this.Ji.generateCertificate(byteArrayInputStream);
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

    private void a(PackageInfo packageInfo, ov ovVar) {
        if (packageInfo != null && packageInfo.signatures != null && packageInfo.signatures.length >= 1) {
            X509Certificate x509Certificate = (X509Certificate) a(packageInfo.signatures[0]);
            if (x509Certificate != null) {
                Object obj = null;
                try {
                    obj = mc.n(x509Certificate.getEncoded());
                } catch (CertificateEncodingException e) {
                    e.printStackTrace();
                }
                ovVar.put("signatureCermMD5", obj);
            }
        }
    }

    private void a(PackageInfo packageInfo, ov ovVar, int i) {
        int i2 = -1;
        boolean z = false;
        if (packageInfo != null && ovVar != null) {
            if ((i & 16) != 0) {
                a(packageInfo, ovVar);
            }
            if ((i & 32) != 0) {
                ovVar.put("permissions", packageInfo.requestedPermissions);
            }
            if (packageInfo.applicationInfo != null) {
                if ((i & 1) != 0) {
                    ovVar.put("pkgName", packageInfo.applicationInfo.packageName);
                    ovVar.put("appName", this.mPackageManager.getApplicationLabel(packageInfo.applicationInfo).toString());
                    ovVar.put("isSystem", Boolean.valueOf((packageInfo.applicationInfo.flags & 1) != 0));
                    ovVar.put("uid", Integer.valueOf(packageInfo.applicationInfo == null ? -1 : packageInfo.applicationInfo.uid));
                }
                if ((i & 2) != 0) {
                    ovVar.put("pkgName", packageInfo.applicationInfo.packageName);
                    ovVar.put("isSystem", Boolean.valueOf((packageInfo.applicationInfo.flags & 1) != 0));
                    String str = "uid";
                    if (packageInfo.applicationInfo != null) {
                        i2 = packageInfo.applicationInfo.uid;
                    }
                    ovVar.put(str, Integer.valueOf(i2));
                }
                if ((i & 4) != 0) {
                    ovVar.put("icon", packageInfo.applicationInfo.loadIcon(this.mPackageManager));
                }
                if (!((i & 8) == 0 || TextUtils.isEmpty(packageInfo.applicationInfo.sourceDir))) {
                    ovVar.put("version", packageInfo.versionName);
                    ovVar.put("versionCode", Integer.valueOf(packageInfo.versionCode));
                    File file = new File(packageInfo.applicationInfo.sourceDir);
                    ovVar.put("size", Long.valueOf(file.length()));
                    ovVar.put("lastModified", Long.valueOf(file.lastModified()));
                }
                if ((i & 64) != 0) {
                    ovVar.put("apkPath", packageInfo.applicationInfo.sourceDir);
                    ovVar.put("isApk", Boolean.valueOf(false));
                }
                if (!((i & 8192) == 0 || VERSION.SDK_INT <= 7 || packageInfo.applicationInfo == null)) {
                    String str2 = "installedOnSdcard";
                    if ((packageInfo.applicationInfo.flags & 262144) != 0) {
                        z = true;
                    }
                    ovVar.put(str2, Boolean.valueOf(z));
                }
            }
        }
    }

    private int bF(int i) {
        int i2 = 0;
        if ((i & 16) != 0) {
            i2 = 64;
        }
        return (i & 32) == 0 ? i2 : i2 | 4096;
    }

    public ov a(String str, int i) {
        ov ovVar = new ov();
        ovVar.put("pkgName", str);
        return a(ovVar, i);
    }

    public ov a(ov ovVar, int i) {
        PackageInfo packageInfo = getPackageInfo((String) ovVar.get("pkgName"), bF(i));
        if (packageInfo == null) {
            return null;
        }
        a(packageInfo, ovVar, i);
        return ovVar;
    }

    public boolean ai(String str) {
        return getPackageInfo(str, 0) != null;
    }

    public ArrayList<ov> f(int i, int i2) {
        List list = null;
        try {
            list = this.mPackageManager.getInstalledPackages(bF(i));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<ov> arrayList = new ArrayList();
        if (list != null) {
            for (PackageInfo packageInfo : list) {
                Object obj;
                if ((packageInfo.applicationInfo.flags & 1) == 0) {
                    obj = null;
                } else {
                    int obj2 = 1;
                }
                if (obj2 != null || i2 != 1) {
                    if (obj2 == null || i2 != 0) {
                        ov ovVar = new ov();
                        a(packageInfo, ovVar, i);
                        arrayList.add(ovVar);
                    }
                }
            }
        }
        return arrayList;
    }

    public NetworkInfo getActiveNetworkInfo() {
        NetworkInfo networkInfo = null;
        try {
            return ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        } catch (Exception e) {
            f.g("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
            return networkInfo;
        }
    }

    public PackageInfo getPackageInfo(String str, int i) {
        try {
            return this.mPackageManager.getPackageInfo(str, i);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public void onCreate(Context context) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        try {
            this.Ji = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
        }
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, int i) {
        return this.mPackageManager.queryIntentServices(intent, i);
    }
}
