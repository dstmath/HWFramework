package tmsdkobf;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public final class it {
    public String mPkg;
    public int mState;
    public int sI;
    public boolean sJ;

    static String a(it itVar) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(itVar.mPkg + ",");
        stringBuilder.append(itVar.sI + ",");
        stringBuilder.append(itVar.mState + ",");
        stringBuilder.append(itVar.sJ);
        return stringBuilder.toString();
    }

    static it bA(String str) {
        String[] split = str == null ? null : str.trim().split(",");
        if (str == null || split.length < 4) {
            return null;
        }
        it itVar = new it();
        itVar.mPkg = split[0];
        itVar.sI = Integer.parseInt(split[1]);
        itVar.mState = Integer.parseInt(split[2]);
        itVar.sJ = Boolean.parseBoolean(split[3]);
        return itVar;
    }

    static ArrayList<it> bB(String str) {
        ArrayList<it> arrayList = new ArrayList();
        if (str != null) {
            for (Object obj : str.trim().split("\\|")) {
                if (!TextUtils.isEmpty(obj)) {
                    arrayList.add(bA(obj));
                }
            }
        }
        return arrayList;
    }

    static String i(List<it> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (it a : list) {
            stringBuilder.append(a(a) + "|");
        }
        return stringBuilder.toString();
    }
}
