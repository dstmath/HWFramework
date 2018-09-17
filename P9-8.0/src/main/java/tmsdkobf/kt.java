package tmsdkobf;

import android.content.Context;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import tmsdk.common.TMSDKContext;

public final class kt {
    static kt xL;
    kf nX = ((kf) fj.D(9));

    kt(Context context) {
    }

    public static void aE(int i) {
        dE().r(i, 0);
    }

    public static void aF(int i) {
        dE().dG().remove(aG(i));
    }

    static String aG(int i) {
        return "" + i;
    }

    public static void aH(int i) {
        dE().dF().remove(aG(i));
    }

    public static void aI(int i) {
        dE().dH().remove(aG(i));
    }

    public static kt dE() {
        if (xL == null) {
            Class cls = kt.class;
            synchronized (kt.class) {
                if (xL == null) {
                    xL = new kt(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return xL;
    }

    public static void e(int i, String str) {
        dE().a(dE().dH(), i, str, false);
    }

    public static void f(int i, String str) {
        dE().a(dE().dH(), i, str, true);
    }

    public static String o(ArrayList<ks> arrayList) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayList.size(); i++) {
            stringBuffer.append(((ks) arrayList.get(i)).xI);
            stringBuffer.append("&");
            stringBuffer.append(((ks) arrayList.get(i)).xH);
            if (((ks) arrayList.get(i)).errorCode != 0) {
                stringBuffer.append("&");
                stringBuffer.append(((ks) arrayList.get(i)).errorCode);
            }
            stringBuffer.append(";");
        }
        return stringBuffer.toString();
    }

    public static void saveActionData(int i) {
        dE().q(i, 0);
    }

    public static void saveMultiValueData(int i, int i2) {
        dE().s(i, i2);
    }

    ArrayList<b> a(jx jxVar) {
        ArrayList<b> arrayList = new ArrayList();
        Map all = jxVar.getAll();
        if (all != null) {
            for (Entry entry : all.entrySet()) {
                try {
                    String str = (String) entry.getKey();
                    Object value = entry.getValue();
                    if (value != null && (value instanceof String)) {
                        String str2 = (String) value;
                        if (str2.indexOf("$") > 0) {
                            int intValue = Integer.valueOf(str).intValue();
                            String[] split = str2.split("\\$");
                            if (split != null && split.length > 0) {
                                String[] strArr = split;
                                for (String split2 : split) {
                                    String[] split3 = split2.split("\\|");
                                    if (split3 != null && split3.length == 2) {
                                        long longValue = Long.valueOf(split3[0]).longValue();
                                        Object obj = split3[1];
                                        b bVar = new b();
                                        bVar.c = intValue;
                                        bVar.timestamp = (int) (longValue / 1000);
                                        bVar.e = new ArrayList();
                                        bVar.e.add(obj);
                                        if (bVar != null) {
                                            arrayList.add(bVar);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        return arrayList;
    }

    ArrayList<b> a(jx jxVar, String str) {
        if (jxVar == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        Map map = null;
        try {
            map = jxVar.getAll();
        } catch (Exception e) {
        }
        if (map != null) {
            for (Entry entry : map.entrySet()) {
                try {
                    String str2 = (String) entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        arrayList.addAll(g(Integer.valueOf(str2).intValue(), (String) value));
                    }
                } catch (Exception e2) {
                }
            }
        }
        return p(arrayList);
    }

    void a(final jx jxVar, final int i, final int i2) {
        im.bJ().addTask(new Runnable() {
            public void run() {
                jxVar.putString(kt.aG(i), String.valueOf(i2));
            }
        }, "doxxx");
    }

    void a(jx jxVar, int i, String str, boolean z) {
        final String str2 = str;
        final int i2 = i;
        final jx jxVar2 = jxVar;
        final boolean z2 = z;
        im.bJ().addTask(new Runnable() {
            public void run() {
                if (str2 != null && str2.length() > 0) {
                    String aG = kt.aG(i2);
                    String string = jxVar2.getString(aG, null);
                    if (string == null || z2) {
                        string = "";
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(string);
                    stringBuilder.append(System.currentTimeMillis());
                    stringBuilder.append("|");
                    stringBuilder.append(str2);
                    stringBuilder.append("$");
                    if (stringBuilder.length() <= 16384) {
                        jxVar2.putString(aG, stringBuilder.toString());
                    }
                }
            }
        }, "doxxx");
    }

    ArrayList<b> b(jx jxVar, String str) {
        if (jxVar == null) {
            return null;
        }
        ArrayList<b> arrayList = new ArrayList();
        Map all = jxVar.getAll();
        if (all != null) {
            for (Entry entry : all.entrySet()) {
                try {
                    String str2 = (String) entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        int intValue = Integer.valueOf(str2).intValue();
                        kv.n("ccrService", "id: " + intValue + " | " + ((String) value));
                        b h = h(intValue, (String) value);
                        if (h != null) {
                            arrayList.add(h);
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        return arrayList;
    }

    ArrayList<Integer> bs(String str) {
        if (str == null) {
            return null;
        }
        ArrayList<Integer> arrayList = new ArrayList();
        String[] split = str.split("\\|");
        String[] strArr = split;
        try {
            int length = split.length;
            for (int i = 0; i < length; i++) {
                arrayList.add(Integer.valueOf(strArr[i]));
            }
        } catch (NumberFormatException e) {
        }
        return arrayList;
    }

    jx dF() {
        return this.nX.getPreferenceService("actionStats");
    }

    jx dG() {
        return this.nX.getPreferenceService("mulDataStats");
    }

    jx dH() {
        return this.nX.getPreferenceService("stringStats");
    }

    public void dI() {
        dF().clear();
    }

    public void dJ() {
        dG().clear();
    }

    public void dK() {
        dH().clear();
    }

    public ArrayList<b> dL() {
        return a(dF(), "Action");
    }

    public ArrayList<b> dM() {
        return b(dG(), "MultiValue");
    }

    public ArrayList<b> dN() {
        return a(dE().dH());
    }

    ArrayList<ks> g(int i, String str) {
        ArrayList<ks> arrayList = new ArrayList();
        if (str == null) {
            return arrayList;
        }
        while (true) {
            try {
                int indexOf = str.indexOf(";");
                if (indexOf != -1) {
                    ks ksVar = new ks();
                    String substring = str.substring(0, indexOf);
                    if (substring.indexOf("&") != -1) {
                        ksVar.xG = i;
                        ksVar.xI = Long.parseLong(substring.substring(0, substring.indexOf("&")));
                        if (ksVar.xI == 0) {
                            ksVar.xI = System.currentTimeMillis();
                        }
                        substring = substring.substring(substring.indexOf("&") + 1);
                        if (substring.indexOf("&") == -1) {
                            ksVar.xH = Integer.parseInt(substring);
                        } else {
                            ksVar.xH = Integer.parseInt(substring.substring(0, substring.indexOf("&")));
                            ksVar.errorCode = Integer.parseInt(substring.substring(substring.indexOf("&") + 1));
                        }
                        arrayList.add(ksVar);
                    }
                    if (indexOf == str.length()) {
                        break;
                    }
                    str = str.substring(indexOf + 1);
                } else {
                    break;
                }
            } catch (Exception e) {
            }
        }
        return arrayList;
    }

    b h(int i, String str) {
        b bVar = new b();
        bVar.c = i;
        bVar.d = bs(str);
        bVar.timestamp = (int) (System.currentTimeMillis() / 1000);
        return bVar;
    }

    ArrayList<b> p(ArrayList<ks> arrayList) {
        if (arrayList == null) {
            return null;
        }
        ArrayList<b> arrayList2 = new ArrayList();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            ks ksVar = (ks) it.next();
            if (ksVar.xH > 0) {
                b bVar = new b();
                bVar.c = ksVar.xG;
                bVar.timestamp = (int) (ksVar.xI / 1000);
                bVar.count = ksVar.xH;
                if (ksVar.errorCode != 0) {
                    bVar.d = new ArrayList();
                    bVar.d.add(Integer.valueOf(ksVar.errorCode));
                }
                arrayList2.add(bVar);
            }
        }
        return arrayList2;
    }

    void q(final int i, final int i2) {
        im.bJ().addTask(new Runnable() {
            public void run() {
                String aG = kt.aG(i);
                long currentTimeMillis = System.currentTimeMillis();
                String string = kt.this.dF().getString(aG, null);
                if (string == null || string.length() <= 8192) {
                    ArrayList arrayList = new ArrayList();
                    ks ksVar = new ks();
                    ksVar.xG = i;
                    ksVar.xI = currentTimeMillis;
                    ksVar.xH = 1;
                    ksVar.errorCode = i2;
                    arrayList.add(ksVar);
                    String o = kt.o(arrayList);
                    if (o != null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        if (string != null) {
                            stringBuilder.append(string);
                        }
                        stringBuilder.append(o);
                        kt.this.dF().putString(aG, stringBuilder.toString());
                    }
                }
            }
        }, "doxxx");
    }

    void r(final int i, final int i2) {
        im.bJ().addTask(new Runnable() {
            public void run() {
                String aG = kt.aG(i);
                long currentTimeMillis = System.currentTimeMillis();
                ArrayList arrayList = new ArrayList();
                ks ksVar = new ks();
                ksVar.xG = i;
                ksVar.xI = currentTimeMillis;
                ksVar.xH = 1;
                ksVar.errorCode = i2;
                arrayList.add(ksVar);
                String o = kt.o(arrayList);
                if (o != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(o);
                    kt.this.dF().putString(aG, stringBuilder.toString());
                }
            }
        }, "doxxx");
    }

    void s(int i, int i2) {
        a(dG(), i, i2);
    }
}
