package tmsdkobf;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;

public class rv {
    private static ro a(dz dzVar) {
        ro roVar = new ro();
        roVar.Ok = dzVar.iv;
        if (!TextUtils.isEmpty(dzVar.iw)) {
            String[] split = dzVar.iw.split("&");
            if (split != null) {
                String[] strArr = split;
                for (String str : split) {
                    if (str.length() > 2) {
                        char charAt = str.charAt(0);
                        String substring = str.substring(2);
                        switch (charAt) {
                            case '1':
                                roVar.mFileName = substring;
                                break;
                            case '2':
                                roVar.Ol = substring;
                                break;
                            case '3':
                                roVar.Om = substring;
                                break;
                            case '4':
                                roVar.On = substring;
                                break;
                            case '5':
                                roVar.Oo = substring;
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(dzVar.ix)) {
            roVar.mPlayers = dzVar.ix.split("&");
        }
        if (!TextUtils.isEmpty(dzVar.iy)) {
            roVar.mAdapter = dzVar.iy;
        }
        return roVar;
    }

    private static void a(List<ro> list, ro roVar) {
        if (TextUtils.isEmpty(roVar.Ok)) {
            list.add(roVar);
            return;
        }
        int i = 0;
        while (i < list.size() && !TextUtils.isEmpty(((ro) list.get(i)).Ok)) {
            i++;
        }
        list.add(i, roVar);
    }

    public static List<ro> kp() {
        ea eaVar = (ea) mk.b(TMSDKContext.getApplicaionContext(), UpdateConfig.PROCESSMANAGER_WHITE_LIST_NAME, UpdateConfig.intToString(40006), new ea(), "UTF-8");
        List<ro> arrayList = new ArrayList();
        if (eaVar == null || eaVar.iC == null) {
            return arrayList;
        }
        Iterator it = eaVar.iC.iterator();
        while (it.hasNext()) {
            dz dzVar = (dz) it.next();
            if (dzVar.iu != null) {
                try {
                    switch (Integer.valueOf(dzVar.iu).intValue()) {
                        case 5:
                            ro a = a(dzVar);
                            if (a == null) {
                                break;
                            }
                            a(arrayList, a);
                            break;
                        default:
                            break;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return arrayList;
    }
}
