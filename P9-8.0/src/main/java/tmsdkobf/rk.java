package tmsdkobf;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tmsdk.common.TMSDKContext;

public class rk {
    private static List<String> Ph = new ArrayList();
    private static List<String> Pi = new ArrayList();
    public static int Pq = 86400000;
    private static List<String> Pr = new ArrayList();

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
            if (str.startsWith(str2)) {
                return str.substring(str2.length());
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

    public static void dl(String str) {
        if (str != null) {
            Pr.add(str);
        }
    }

    public static List<String> jZ() {
        List<String> s = lu.s(TMSDKContext.getApplicaionContext());
        if (s.size() <= 0) {
            s = G(kn.cM());
            if (s == null) {
                return null;
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
        return s != null ? s : null;
    }

    public static List<String> kh() {
        return Pr;
    }

    public static List<File> ki() {
        List<String> ke = ke();
        List arrayList = new ArrayList();
        if (ke != null) {
            for (String file : ke) {
                File[] listFiles = new File(file).listFiles();
                if (listFiles != null) {
                    arrayList.addAll(Arrays.asList(listFiles));
                }
            }
        }
        return arrayList;
    }
}
