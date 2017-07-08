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
import java.util.List;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public final class qc extends BaseManagerC implements qd {
    private CertificateFactory Ji;
    private Context mContext;
    private PackageManager mPackageManager;

    public qc() {
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

    private void a(PackageInfo packageInfo, py pyVar) {
        if (packageInfo != null && packageInfo.signatures != null && packageInfo.signatures.length >= 1) {
            X509Certificate x509Certificate = (X509Certificate) a(packageInfo.signatures[0]);
            if (x509Certificate != null) {
                Object p;
                try {
                    p = nb.p(x509Certificate.getEncoded());
                } catch (CertificateEncodingException e) {
                    e.printStackTrace();
                    p = null;
                }
                pyVar.put("signatureCermMD5", p);
            }
        }
    }

    private void a(PackageInfo packageInfo, py pyVar, int i) {
        boolean z = true;
        int i2 = -1;
        if (packageInfo != null && pyVar != null) {
            if ((i & 1) != 0) {
                pyVar.put("pkgName", packageInfo.applicationInfo.packageName);
                pyVar.put("appName", this.mPackageManager.getApplicationLabel(packageInfo.applicationInfo).toString());
                pyVar.put("isSystem", Boolean.valueOf((packageInfo.applicationInfo.flags & 1) != 0));
                pyVar.put("uid", Integer.valueOf(packageInfo.applicationInfo == null ? -1 : packageInfo.applicationInfo.uid));
            }
            if ((i & 2) != 0) {
                pyVar.put("pkgName", packageInfo.applicationInfo.packageName);
                String str = "isSystem";
                if ((packageInfo.applicationInfo.flags & 1) == 0) {
                    z = false;
                }
                pyVar.put(str, Boolean.valueOf(z));
                str = "uid";
                if (packageInfo.applicationInfo != null) {
                    i2 = packageInfo.applicationInfo.uid;
                }
                pyVar.put(str, Integer.valueOf(i2));
            }
            if ((i & 4) != 0) {
                pyVar.put("icon", packageInfo.applicationInfo.loadIcon(this.mPackageManager));
            }
            if ((i & 8) != 0) {
                pyVar.put(CheckVersionField.CHECK_VERSION_VERSION, packageInfo.versionName);
                pyVar.put("versionCode", Integer.valueOf(packageInfo.versionCode));
                File file = new File(packageInfo.applicationInfo.sourceDir);
                pyVar.put("size", Long.valueOf(file.length()));
                pyVar.put("lastModified", Long.valueOf(file.lastModified()));
            }
            if ((i & 16) != 0) {
                a(packageInfo, pyVar);
            }
            if ((i & 32) != 0) {
                pyVar.put("permissions", packageInfo.requestedPermissions);
            }
            if ((i & 64) != 0) {
                pyVar.put("apkPath", packageInfo.applicationInfo.sourceDir);
                pyVar.put("isApk", Boolean.valueOf(false));
            }
        }
    }

    private int cp(int i) {
        int i2 = 0;
        if ((i & 16) != 0) {
            i2 = 64;
        }
        return (i & 32) == 0 ? i2 : i2 | 4096;
    }

    public py a(py pyVar, int i) {
        PackageInfo packageInfo = getPackageInfo((String) pyVar.get("pkgName"), cp(i));
        if (packageInfo == null) {
            return null;
        }
        a(packageInfo, pyVar, i);
        return pyVar;
    }

    public boolean aC(String str) {
        return getPackageInfo(str, 0) != null;
    }

    public py b(String str, int i) {
        py pyVar = new py();
        pyVar.put("pkgName", str);
        return a(pyVar, i);
    }

    public int c(String str, int i) {
        PackageInfo packageInfo = getPackageInfo(str, 0);
        return packageInfo != null ? i != packageInfo.versionCode ? i >= packageInfo.versionCode ? 1 : i != 0 ? 2 : -2 : 0 : -1;
    }

    public ArrayList<py> c(int i, int i2) {
        List installedPackages;
        try {
            installedPackages = this.mPackageManager.getInstalledPackages(cp(i));
        } catch (Exception e) {
            e.printStackTrace();
            installedPackages = null;
        }
        ArrayList<py> arrayList = new ArrayList();
        if (r0 != null) {
            for (PackageInfo packageInfo : r0) {
                Object obj;
                if ((packageInfo.applicationInfo.flags & 1) == 0) {
                    obj = null;
                } else {
                    int i3 = 1;
                }
                String str = packageInfo.applicationInfo.packageName;
                if (obj != null || i2 != 1) {
                    if (obj == null || i2 != 0) {
                        if (!str.equals(this.mContext.getPackageName())) {
                            py pyVar = new py();
                            a(packageInfo, pyVar, i);
                            arrayList.add(pyVar);
                        }
                    }
                }
            }
        }
        return arrayList;
    }

    public NetworkInfo getActiveNetworkInfo() {
        try {
            return ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        } catch (Exception e) {
            d.f("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
            return null;
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
