package tmsdkobf;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.utils.m;
import tmsdk.fg.module.cleanV2.RubbishEntity;

public class qy {
    private static ox Cq = ((ox) ManagerCreatorC.getManager(ox.class));

    private int a(ov ovVar, Map<String, ov> map) {
        ov ovVar2 = (ov) map.get(ovVar.getPackageName());
        if (ovVar2 == null) {
            return -1;
        }
        if (ovVar2.getVersionCode() <= ovVar.getVersionCode()) {
            return ovVar2.getVersionCode() != ovVar.getVersionCode() ? 1 : 0;
        } else {
            return 2;
        }
    }

    private String a(ra raVar, int i) {
        switch (i) {
            case -1:
                return m.cF("apk_not_installed");
            case 0:
                return m.cF("apk_installed");
            case 1:
                return m.cF("apk_new_version");
            case 2:
                return m.cF("apk_old_version");
            default:
                return null;
        }
    }

    private boolean a(ra raVar, String str, List<qt> list) {
        String str2 = null;
        if (raVar.ME == null) {
            return false;
        }
        for (String str3 : raVar.ME) {
            if (str.startsWith(str3)) {
                str2 = str3;
            }
        }
        if (list == null || str2 == null) {
            return false;
        }
        long timeInMillis = (Calendar.getInstance().getTimeInMillis() - new File(str).lastModified()) / 86400000;
        for (qt qtVar : list) {
            if (!TextUtils.isEmpty(qtVar.Om)) {
                int[] db = db(qtVar.Om);
                if ((timeInMillis < ((long) db[0]) ? 1 : null) != null) {
                    continue;
                } else {
                    if ((timeInMillis > ((long) db[1]) ? 1 : null) == null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean b(ov ovVar, Map<String, List<Integer>> map) {
        if (ovVar == null || map == null) {
            return false;
        }
        List list = (List) map.get(ovVar.getPackageName());
        return (list == null || list.indexOf(Integer.valueOf(ovVar.getVersionCode())) == -1) ? false : true;
    }

    private int[] db(String str) {
        String[] split = str.split(",");
        int[] iArr = new int[2];
        iArr[0] = !split[0].equals("-") ? Integer.parseInt(split[0]) : Integer.MIN_VALUE;
        iArr[1] = !split[1].equals("-") ? Integer.parseInt(split[1]) : Integer.MAX_VALUE;
        return iArr;
    }

    private String dc(String str) {
        int lastIndexOf = str.lastIndexOf(File.separator);
        return lastIndexOf < 0 ? str : str.substring(lastIndexOf + 1);
    }

    private void f(ov ovVar) {
        if (ovVar.getAppName() == null) {
            try {
                PackageManager packageManager = TMSDKContext.getApplicaionContext().getPackageManager();
                ovVar.setAppName(packageManager.getApplicationLabel(packageManager.getApplicationInfo(ovVar.getPackageName(), 0)).toString());
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    protected RubbishEntity a(ra raVar, boolean z, String -l_13_R, long j, Map<String, List<Integer>> map, Map<String, ov> map2, List<qt> list) {
        RubbishEntity rubbishEntity = null;
        ov ovVar = null;
        try {
            ovVar = Cq.g(-l_13_R, 73);
        } catch (Throwable th) {
        }
        if (ovVar == null || ovVar.getPackageName() == null) {
            return new RubbishEntity(1, -l_13_R, true, j, dc(-l_13_R), null, m.cF("broken_apk"));
        }
        f(ovVar);
        int a = a(ovVar, (Map) map2);
        String a2 = a(raVar, a);
        if (b(ovVar, map)) {
            return new RubbishEntity(2, -l_13_R, true, j, ovVar.getAppName(), ovVar.getPackageName(), m.cF("apk_repeated"));
        }
        if (a2 == null) {
            a2 = Integer.toString(ovVar.getVersionCode());
        }
        boolean a3 = a(raVar, -l_13_R, list);
        if (2 == a) {
            a3 = true;
        }
        RubbishEntity rubbishEntity2;
        if (!z) {
            rubbishEntity2 = new RubbishEntity(2, -l_13_R, a3, j, ovVar.getAppName(), ovVar.getPackageName(), a2);
        } else if (a3) {
            rubbishEntity2 = new RubbishEntity(2, -l_13_R, a3, j, ovVar.getAppName(), ovVar.getPackageName(), a2);
        }
        return rubbishEntity;
    }
}
