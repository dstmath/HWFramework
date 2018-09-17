package tmsdkobf;

import android.content.Context;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.utils.q;

public class se {
    public List<String> QO = new ArrayList();
    public List<a> QP = new ArrayList();
    public List<a> QQ = new ArrayList();
    public List<a> QR = new ArrayList();

    public static class a extends qt {
        public String mAdapter;
        public String[] mPlayers;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean V(Context context) {
        ea eaVar = (ea) mk.b(TMSDKContext.getApplicaionContext(), UpdateConfig.PROCESSMANAGER_WHITE_LIST_NAME, UpdateConfig.intToString(40006), new ea(), "UTF-8");
        if (eaVar == null || eaVar.iC == null) {
            return false;
        }
        Iterator it = eaVar.iC.iterator();
        while (it.hasNext()) {
            dz dzVar = (dz) it.next();
            if (dzVar.iu != null) {
                try {
                    switch (Integer.valueOf(dzVar.iu).intValue()) {
                        case 3:
                            if (dzVar.iv == null) {
                                break;
                            }
                            this.QO.add(dzVar.iv);
                            break;
                        case 4:
                            break;
                        case 5:
                            a b = b(dzVar);
                            if (b != null) {
                                if (!q.cK(b.mAdapter)) {
                                    a(this.QR, b);
                                    break;
                                }
                                a(this.QQ, b);
                                break;
                            }
                            break;
                        case 6:
                            a b2 = b(dzVar);
                            if (b2 == null) {
                                break;
                            }
                            a(this.QP, b2);
                            break;
                        default:
                            break;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private static void a(List<a> list, a aVar) {
        if (q.cK(aVar.Ok)) {
            list.add(aVar);
            return;
        }
        int i = 0;
        while (i < list.size() && !q.cK(((a) list.get(i)).Ok)) {
            i++;
        }
        list.add(i, aVar);
    }

    private static a b(dz dzVar) {
        a aVar = new a();
        aVar.Ok = dzVar.iv;
        if (q.cJ(dzVar.iw)) {
            String[] split = dzVar.iw.split("&");
            if (split != null) {
                String[] strArr = split;
                for (String str : split) {
                    if (str.length() > 2) {
                        char charAt = str.charAt(0);
                        String substring = str.substring(2);
                        switch (charAt) {
                            case '1':
                                aVar.mFileName = substring;
                                break;
                            case '2':
                                aVar.Ol = substring;
                                break;
                            case '3':
                                aVar.Om = substring;
                                break;
                            case '4':
                                aVar.On = substring;
                                break;
                            case '5':
                                aVar.Oo = substring;
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        if (q.cJ(dzVar.ix)) {
            aVar.mPlayers = dzVar.ix.split("&");
        }
        if (q.cJ(dzVar.iy)) {
            aVar.mAdapter = dzVar.iy;
        }
        return aVar;
    }

    public boolean U(Context context) {
        return V(context);
    }
}
