package tmsdkobf;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class rg {
    public static float df(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0.0f;
        }
        float parseFloat;
        try {
            parseFloat = Float.parseFloat(str);
        } catch (Exception e) {
            parseFloat = 0.0f;
        }
        return parseFloat;
    }

    public static int dg(String str) {
        int i = 0;
        if (TextUtils.isEmpty(str)) {
            return i;
        }
        try {
            i = Integer.valueOf(str).intValue();
        } catch (NumberFormatException e) {
        }
        return i < 0 ? 0 : i;
    }

    public static List<Integer> dh(String str) {
        if (str == null) {
            return null;
        }
        List<Integer> arrayList = new ArrayList();
        String[] split = str.split("\\|");
        String[] strArr = split;
        int length = split.length;
        for (int i = 0; i < length; i++) {
            try {
                arrayList.add(Integer.valueOf(strArr[i]));
            } catch (Exception e) {
                arrayList = null;
            }
        }
        return arrayList;
    }
}
