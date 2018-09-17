package tmsdkobf;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public final class hp {
    public String mPkg;
    public int mState;
    public int qh;
    public boolean qi;

    static String a(List<hp> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (hp a : list) {
            stringBuilder.append(a(a) + "|");
        }
        return stringBuilder.toString();
    }

    static String a(hp hpVar) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hpVar.mPkg + ",");
        stringBuilder.append(hpVar.qh + ",");
        stringBuilder.append(hpVar.mState + ",");
        stringBuilder.append(hpVar.qi);
        return stringBuilder.toString();
    }

    static hp aC(String str) {
        String[] strArr = null;
        if (str != null) {
            strArr = str.trim().split(",");
        }
        if (str == null || strArr.length < 4) {
            return null;
        }
        hp hpVar = new hp();
        hpVar.mPkg = strArr[0];
        hpVar.qh = Integer.parseInt(strArr[1]);
        hpVar.mState = Integer.parseInt(strArr[2]);
        hpVar.qi = Boolean.parseBoolean(strArr[3]);
        return hpVar;
    }

    static ArrayList<hp> aD(String str) {
        ArrayList<hp> arrayList = new ArrayList();
        if (str != null) {
            String[] split = str.trim().split("\\|");
            String[] strArr = split;
            int length = split.length;
            for (int i = 0; i < length; i++) {
                Object obj = strArr[i];
                if (!TextUtils.isEmpty(obj)) {
                    arrayList.add(aC(obj));
                }
            }
        }
        return arrayList;
    }
}
