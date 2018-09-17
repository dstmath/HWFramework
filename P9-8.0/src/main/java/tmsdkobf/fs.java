package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build.VERSION;
import android.text.TextUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import tmsdk.common.TMSDKContext;

public class fs {
    private static volatile fs ne = null;
    private Context mContext;
    public long nd = 0;

    private fs(Context context) {
        this.mContext = context;
    }

    private ft H(String str) {
        ft ftVar = null;
        try {
            if (TextUtils.isEmpty(str)) {
                return null;
            }
            String[] split = str.split(";");
            if (split != null && split.length >= 9) {
                ft ftVar2 = new ft();
                try {
                    ftVar2.I(split[0]);
                    ftVar2.J(split[1]);
                    ftVar2.K(split[2]);
                    ftVar2.F(Integer.valueOf(split[3]).intValue());
                    ftVar2.L(split[4]);
                    ftVar2.M(split[5]);
                    ftVar2.N(split[6]);
                    ftVar2.G(Integer.valueOf(split[7]).intValue());
                    ftVar2.a((float) Integer.valueOf(split[8]).intValue());
                    ftVar = ftVar2;
                } catch (Exception e) {
                    ftVar = ftVar2;
                }
            }
            return ftVar;
        } catch (Exception e2) {
        }
    }

    private void a(Context context, ft ftVar) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(ftVar.y()).append(";");
            if (fy.c(context, ftVar)) {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(ftVar.y(), 4);
                String trim = packageInfo == null ? "0" : packageInfo.versionName.trim();
                stringBuilder.append("100").append(";").append(trim);
                boolean a = fy.a(context, ftVar.D());
                stringBuilder.append(";").append(!a ? "120" : "102");
                int intValue = Integer.valueOf(ftVar.B()).intValue();
                if (!(a || 1 == intValue)) {
                    if (fy.a(trim, ftVar.A().trim())) {
                        stringBuilder.append(";").append("105");
                        boolean z = false;
                        if (2 == intValue) {
                            z = fy.a(context, ftVar.y(), ftVar.C());
                            if (z) {
                                stringBuilder.append(";").append("103");
                            }
                        }
                        if (!z) {
                            if ((ga.Q() / 1000 < ((long) ftVar.F()) ? 1 : null) == null) {
                                stringBuilder.append(";").append("106");
                                int b = b(context, ftVar);
                                if (b <= 0) {
                                    stringBuilder.append(";").append("124");
                                } else {
                                    stringBuilder.append(";").append("107");
                                    if (b >= 2) {
                                        stringBuilder.append(";").append("123");
                                    }
                                }
                                Object obj = null;
                                int i = 30;
                                do {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                    i--;
                                } while (i > 0);
                                if (fy.a(context, ftVar.D())) {
                                    obj = 1;
                                    stringBuilder.append(";").append("108");
                                }
                                i = 120;
                                do {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e2) {
                                    }
                                    i--;
                                } while (i > 0);
                                if (fy.a(context, ftVar.D())) {
                                    obj = 1;
                                    stringBuilder.append(";").append("109");
                                }
                                if (obj == null) {
                                    stringBuilder.append(";").append("125");
                                }
                            } else {
                                stringBuilder.append(";").append("122");
                            }
                        }
                    } else {
                        stringBuilder.append(";").append("121");
                    }
                }
            } else {
                stringBuilder.append("101");
            }
            fr.r().a(1320063, stringBuilder.toString());
        } catch (Exception e3) {
        }
    }

    private int b(Context context, ft ftVar) {
        Object obj = null;
        kv.d("WakeupUtil", "wakeUpApp-cmd:[" + ftVar.C() + "][" + ftVar.y() + "][" + ftVar.z() + "]");
        if (TextUtils.isEmpty(ftVar.C())) {
            return -1;
        }
        try {
            String str = "catfish" + "." + TMSDKContext.getApplicaionContext().getPackageName() + ".0.0";
            String C;
            switch (Integer.valueOf(ftVar.B()).intValue()) {
                case 2:
                    Object obj2;
                    Intent intent = new Intent();
                    intent.setClassName(ftVar.y(), ftVar.C());
                    intent.putExtra("platform_id", str);
                    intent.putExtra("channel_id", im.bQ());
                    intent.setPackage(ftVar.y());
                    intent.addFlags(32);
                    kv.d("WakeupUtil", "startService-intent:" + intent + "]");
                    if (context.startService(intent) == null) {
                        obj2 = null;
                    } else {
                        int obj22 = 1;
                    }
                    if (obj22 == null) {
                        return 1;
                    }
                    break;
                case 3:
                    C = ftVar.C();
                    if (VERSION.SDK_INT >= 17) {
                        C = C + " --user 0";
                    }
                    C = (C + " --include-stopped-packages") + " -e platform_id " + str + " -e channel_id " + im.bQ();
                    kv.d("WakeupUtil", "AM-cmd:" + C + "]");
                    if (Runtime.getRuntime().exec(C) != null) {
                        int obj3 = 1;
                    }
                    if (obj3 == null) {
                        return 1;
                    }
                    break;
                case 4:
                    C = ftVar.C() + " -e platform_id " + str + " -e channel_id " + im.bQ();
                    kv.d("WakeupUtil", "AM_TO_INTENT-cmd:" + C + "]");
                    fx fxVar = new fx(C);
                    kv.d("WakeupUtil", "AM_TO_INTENT-intent:" + fxVar.getIntent() + "]");
                    if (!fxVar.d(context)) {
                        return 1;
                    }
                    break;
                default:
                    return -1;
            }
            return 2;
        } catch (Exception e) {
            return -1;
        }
    }

    public static fs c(Context context) {
        if (ne == null) {
            Class cls = fs.class;
            synchronized (fs.class) {
                if (ne == null) {
                    ne = new fs(context);
                }
            }
        }
        return ne;
    }

    private int w() {
        int i = 0;
        List<ft> x = x();
        if (x != null && x.size() > 0) {
            Map v = fr.r().v();
            for (ft ftVar : x) {
                if (!(ftVar == null || ftVar.y().equals(this.mContext.getPackageName()))) {
                    long currentTimeMillis = System.currentTimeMillis();
                    long j = 0;
                    String str = (String) v.get(ftVar.y());
                    if (!TextUtils.isEmpty(str)) {
                        j = Long.valueOf(str).longValue();
                    }
                    if (((float) ((currentTimeMillis - j) / 3600000)) >= Float.valueOf(ftVar.G()).floatValue()) {
                        i++;
                        Object E = ftVar.E();
                        Object obj = null;
                        if (!TextUtils.isEmpty(E)) {
                            try {
                                if ((new Date().getTime() > new SimpleDateFormat("yyyyMMdd").parse(E).getTime() ? 1 : null) == null) {
                                    obj = 1;
                                }
                            } catch (Exception e) {
                            }
                        }
                        if (obj == null) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(ftVar.y()).append(";").append("119");
                            fr.r().a(1320063, stringBuilder.toString());
                        } else {
                            a(this.mContext, ftVar);
                        }
                        fr.r().v().put(ftVar.y(), "" + System.currentTimeMillis());
                    }
                }
            }
            fr.r().b(this.mContext);
        }
        return i;
    }

    private List<ft> x() {
        List<ft> arrayList = new ArrayList();
        fv u = fr.r().u();
        if (u == null) {
            return null;
        }
        Iterator it = u.H().iterator();
        while (it.hasNext()) {
            ft H = H((String) it.next());
            if (H != null) {
                arrayList.add(H);
            }
        }
        return arrayList;
    }

    /* JADX WARNING: Missing block: B:25:0x0063, code:
            if (r3 == null) goto L_0x0044;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void c(boolean z) {
        Object obj = 1;
        synchronized (this) {
            try {
                long currentTimeMillis = System.currentTimeMillis();
                fr.r().a(this.mContext);
                fv u = fr.r().u();
                if (u != null) {
                    if (u.no) {
                        if ((currentTimeMillis / 1000 > ((long) u.R) ? 1 : null) == null) {
                            w();
                        }
                        if (!z) {
                            if (currentTimeMillis / 1000 > ((long) u.R)) {
                                obj = null;
                            }
                        }
                        fr.r().t();
                        gf.S().K(0);
                        fr.s();
                    }
                }
                gf.S().K(0);
                kt.saveActionData(1320054);
            } catch (Throwable th) {
            }
            fr.s();
        }
    }
}
