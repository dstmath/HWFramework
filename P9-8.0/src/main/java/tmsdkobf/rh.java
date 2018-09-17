package tmsdkobf;

import android.os.Environment;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.TMSDKContext;

public class rh {
    private static List<String> Ph = new ArrayList();
    private static List<String> Pi = new ArrayList();

    public static String E(String -l_2_R, String -l_3_R) {
        String[] strArr;
        String str = -l_2_R;
        while (str.startsWith("/")) {
            str = str.substring(1);
        }
        String str2 = -l_3_R;
        while (str2.startsWith("/")) {
            str2 = str2.substring(1);
        }
        Object split = str.split("/");
        Object strArr2 = str2.split("/");
        Object obj = null;
        if (split.length < strArr2.length) {
            split = strArr2;
            strArr2 = (String[]) split.clone();
            String str3 = str;
            str = str2;
            str2 = str3;
            obj = 1;
        }
        Object obj2 = null;
        int i = 0;
        int i2 = 0;
        while (i2 < split.length) {
            if (split[i2].equals(strArr2[0])) {
                i++;
                int i3 = 1;
                while (i3 < strArr2.length) {
                    if (i2 + i3 >= split.length || !split[i2 + i3].equals(strArr2[i3])) {
                        i = 0;
                        break;
                    }
                    i++;
                    i3++;
                }
                if (i == strArr2.length) {
                    obj2 = 1;
                    break;
                }
            }
            i2++;
        }
        if (obj2 == null) {
            return null;
        }
        return obj == null ? -l_2_R : -l_3_R;
    }

    public static void E(List<String> list) {
        Pi.clear();
        Pi.addAll(list);
    }

    private static void F(List<String> list) {
        for (String str : Pi) {
            if (list.contains(str)) {
                list.remove(str);
            }
        }
    }

    private static List<String> G(List<String> list) {
        Object obj = null;
        for (int i = 0; i < list.size(); i++) {
            for (int i2 = i + 1; i2 < list.size(); i2++) {
                String E = E((String) list.get(i), (String) list.get(i2));
                if (E != null) {
                    list.remove(E);
                    obj = 1;
                    break;
                }
            }
            if (obj != null) {
                return G(list);
            }
        }
        return list;
    }

    public static String a(String str, List<String> list) {
        if (str == null || list == null || list.isEmpty()) {
            return str;
        }
        for (String str2 : list) {
            if (str.contains(str2)) {
                return str.replaceFirst(str2, "");
            }
        }
        return str;
    }

    public static void appendCustomSdcardRoots(String str) {
        if (str != null) {
            Ph.add(str);
        }
    }

    public static void clearCustomSdcardRoots() {
        Ph.clear();
    }

    public static String di(String str) {
        if (str == null) {
            return str;
        }
        int lastIndexOf = str.lastIndexOf("/");
        return (lastIndexOf >= 0 && lastIndexOf < str.length() - 1) ? str.substring(lastIndexOf + 1, str.length()) : str;
    }

    public static String dj(String str) {
        String di = di(str);
        if (di == null) {
            return di;
        }
        int lastIndexOf = di.lastIndexOf(".");
        return (lastIndexOf > 0 && lastIndexOf < di.length() - 1) ? di.substring(0, lastIndexOf) : di;
    }

    public static List<String> jZ() {
        List<String> s = lu.s(TMSDKContext.getApplicaionContext());
        if (s.size() <= 0) {
            s = G(kn.cM());
            if (s == null) {
                s = new ArrayList();
                s.add(Environment.getExternalStorageDirectory().getAbsolutePath());
            }
            s.addAll(Ph);
            F(s);
            return s;
        }
        s.addAll(Ph);
        F(s);
        return s;
    }

    public static List<String> ke() {
        List<String> s = lu.s(TMSDKContext.getApplicaionContext());
        if (s.size() > 0) {
            return s;
        }
        s = G(kn.cM());
        if (s == null) {
            s = new ArrayList();
            s.add(Environment.getExternalStorageDirectory().getAbsolutePath());
        }
        return s;
    }
}
