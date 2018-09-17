package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;

public class ll {
    public static void aM(int i) {
        if (gf.S().getStartCount() > 0 && eA()) {
            gf.S().c(System.currentTimeMillis());
            ((ki) fj.D(4)).addTask(new Runnable() {
                public void run() {
                    try {
                        boolean n = ll.n(TMSDKContext.getApplicaionContext());
                        boolean o = ll.o(TMSDKContext.getApplicaionContext());
                        if (!n) {
                            kt.saveActionData(1320009);
                        } else if (o) {
                            kt.saveActionData(29985);
                        } else {
                            kt.saveActionData(29986);
                        }
                        if (n && !o) {
                            ll.p(TMSDKContext.getApplicaionContext());
                            int startCount = gf.S().getStartCount();
                            if (startCount > 0) {
                                gf.S().J(startCount - 1);
                            }
                            Thread.sleep(15000);
                            if (ll.o(TMSDKContext.getApplicaionContext())) {
                                kt.saveActionData(29984);
                            } else {
                                kt.saveActionData(29983);
                            }
                        }
                    } catch (InterruptedException e) {
                    } catch (Throwable th) {
                        return;
                    }
                    kr.p(true);
                }
            }, "checkStartTMSecure");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0043 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x002b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static boolean eA() {
        boolean z;
        boolean z2 = true;
        boolean z3 = false;
        long currentTimeMillis = System.currentTimeMillis();
        long ae = gf.S().ae();
        long af = gf.S().af();
        if (af != 0) {
            if (currentTimeMillis / 1000 >= af) {
                z = false;
                if (z) {
                    return false;
                }
                if (!(currentTimeMillis <= ae)) {
                    if (currentTimeMillis - ae >= 86400000) {
                        z2 = false;
                    }
                    if (!z2) {
                        z3 = true;
                    }
                }
                return z3;
            }
        }
        z = true;
        if (z) {
        }
    }

    public static boolean n(Context context) {
        boolean z = false;
        try {
            return TMServiceFactory.getSystemInfoService().ai(ir.rV);
        } catch (Throwable th) {
            return z;
        }
    }

    public static boolean o(Context context) {
        return fy.a(context, ir.rV);
    }

    public static void p(Context context) {
        kv.n("cccccc", "startByActivity:");
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(ir.rV, 16384);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null && packageInfo.versionCode >= 1066) {
            String str = "EP_Secure_SDK_6.1.0";
            String str2 = "{'dest_view':65537,'show_id':'show_001','show_channel':'" + im.bQ() + "'}";
            kv.n("cccccc", "jumpParams:" + str2 + "   appToken:" + str);
            try {
                Intent launchIntentForPackage = context.getPackageManager().getLaunchIntentForPackage(ir.rV);
                Bundle bundle = new Bundle();
                bundle.putString("platform_id", str);
                bundle.putString("launch_param", str2);
                launchIntentForPackage.putExtras(bundle);
                launchIntentForPackage.setFlags(402653184);
                context.startActivity(launchIntentForPackage);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }
}
