package tmsdkobf;

import android.content.Context;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import tmsdk.common.TMSDKContext;

/* compiled from: Unknown */
public final class ma {
    static ma Au;
    ln np;

    ma(Context context) {
        this.np = (ln) fe.ad(9);
    }

    public static void bx(int i) {
        et().n(i, 0);
    }

    public static void by(int i) {
        et().o(i, 0);
    }

    static String bz(int i) {
        return "" + i;
    }

    public static void d(int i, String str) {
        et().e(i, str);
    }

    public static ma et() {
        if (Au == null) {
            synchronized (ma.class) {
                if (Au == null) {
                    Au = new ma(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return Au;
    }

    public static void p(int i, int i2) {
        et().q(i, i2);
    }

    public static String w(ArrayList<lz> arrayList) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayList.size(); i++) {
            stringBuffer.append(((lz) arrayList.get(i)).Ar);
            stringBuffer.append("&");
            stringBuffer.append(((lz) arrayList.get(i)).Aq);
            if (((lz) arrayList.get(i)).errorCode != 0) {
                stringBuffer.append("&");
                stringBuffer.append(((lz) arrayList.get(i)).errorCode);
            }
            stringBuffer.append(";");
        }
        return stringBuffer.toString();
    }

    ArrayList<dx> a(lf lfVar, String str) {
        Map map = null;
        if (lfVar == null) {
            return map;
        }
        ArrayList arrayList = new ArrayList();
        try {
            map = lfVar.getAll();
        } catch (Exception e) {
        }
        if (map != null) {
            for (Entry entry : map.entrySet()) {
                try {
                    String str2 = (String) entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        arrayList.addAll(f(Integer.valueOf(str2).intValue(), (String) value));
                    }
                } catch (Exception e2) {
                }
            }
        }
        return x(arrayList);
    }

    void a(lf lfVar, int i, int i2) {
        String bz = bz(i);
        String string = lfVar.getString(bz, null);
        lfVar.m(bz, string != null ? string + "|" + String.valueOf(i2) : String.valueOf(i2));
    }

    ArrayList<dx> b(lf lfVar, String str) {
        if (lfVar == null) {
            return null;
        }
        ArrayList<dx> arrayList = new ArrayList();
        Map all = lfVar.getAll();
        if (all != null) {
            for (Entry entry : all.entrySet()) {
                try {
                    String str2 = (String) entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        int intValue = Integer.valueOf(str2).intValue();
                        na.s("ccrService", "id: " + intValue + " | " + ((String) value));
                        dx g = g(intValue, (String) value);
                        if (g != null) {
                            arrayList.add(g);
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        return arrayList;
    }

    ArrayList<Integer> cp(String str) {
        if (str == null) {
            return null;
        }
        ArrayList<Integer> arrayList = new ArrayList();
        try {
            for (String valueOf : str.split("\\|")) {
                arrayList.add(Integer.valueOf(valueOf));
            }
        } catch (NumberFormatException e) {
        }
        return arrayList;
    }

    void e(int i, String str) {
        if (str != null && str.length() > 0) {
            String bz = bz(i);
            String string = ew().getString(bz, null);
            if (string == null) {
                string = "";
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(string);
            stringBuilder.append(System.currentTimeMillis());
            stringBuilder.append("|");
            stringBuilder.append(str);
            stringBuilder.append("$");
            if (stringBuilder.length() <= 16384) {
                ew().m(bz, stringBuilder.toString());
            }
        }
    }

    public void eA() {
        ev().clear();
    }

    public ArrayList<dx> eB() {
        return eD();
    }

    public void eC() {
        ew().clear();
    }

    ArrayList<dx> eD() {
        ArrayList<dx> arrayList = new ArrayList();
        Map all = ew().getAll();
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
                                for (String split2 : split) {
                                    String split22;
                                    String[] split3 = split22.split("\\|");
                                    if (split3 != null && split3.length == 2) {
                                        long longValue = Long.valueOf(split3[0]).longValue();
                                        split22 = split3[1];
                                        dx dxVar = new dx();
                                        dxVar.setId(intValue);
                                        dxVar.v((int) (longValue / 1000));
                                        dxVar.B(split22);
                                        dxVar.y("1");
                                        if (dxVar != null) {
                                            arrayList.add(dxVar);
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

    lf eu() {
        return this.np.getPreferenceService("actionStats");
    }

    lf ev() {
        return this.np.getPreferenceService("mulDataStats");
    }

    lf ew() {
        return this.np.getPreferenceService("stringStats");
    }

    public ArrayList<dx> ex() {
        return a(eu(), "Action");
    }

    public void ey() {
        eu().clear();
    }

    public ArrayList<dx> ez() {
        return b(ev(), "MultiValue");
    }

    ArrayList<lz> f(int i, String str) {
        ArrayList<lz> arrayList = new ArrayList();
        if (str == null) {
            return arrayList;
        }
        while (true) {
            try {
                int indexOf = str.indexOf(";");
                if (indexOf != -1) {
                    lz lzVar = new lz();
                    String substring = str.substring(0, indexOf);
                    if (substring.indexOf("&") != -1) {
                        lzVar.Ap = i;
                        lzVar.Ar = Long.parseLong(substring.substring(0, substring.indexOf("&")));
                        if (lzVar.Ar == 0) {
                            lzVar.Ar = System.currentTimeMillis();
                        }
                        substring = substring.substring(substring.indexOf("&") + 1);
                        if (substring.indexOf("&") == -1) {
                            lzVar.Aq = Integer.parseInt(substring);
                        } else {
                            lzVar.Aq = Integer.parseInt(substring.substring(0, substring.indexOf("&")));
                            lzVar.errorCode = Integer.parseInt(substring.substring(substring.indexOf("&") + 1));
                        }
                        arrayList.add(lzVar);
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

    dx g(int i, String str) {
        dx dxVar = new dx();
        dxVar.setId(i);
        dxVar.e(cp(str));
        if (dxVar.e() == null) {
            return null;
        }
        dxVar.y("1");
        dxVar.v((int) (System.currentTimeMillis() / 1000));
        return dxVar;
    }

    void n(int i, int i2) {
        String bz = bz(i);
        long currentTimeMillis = System.currentTimeMillis();
        String string = eu().getString(bz, null);
        ArrayList arrayList = new ArrayList();
        lz lzVar = new lz();
        lzVar.Ap = i;
        lzVar.Ar = currentTimeMillis;
        lzVar.Aq = 1;
        lzVar.errorCode = i2;
        arrayList.add(lzVar);
        String w = w(arrayList);
        if (w != null) {
            StringBuilder stringBuilder = new StringBuilder();
            if (string != null) {
                stringBuilder.append(string);
            }
            stringBuilder.append(w);
            eu().m(bz, stringBuilder.toString());
        }
    }

    void o(int i, int i2) {
        String bz = bz(i);
        long currentTimeMillis = System.currentTimeMillis();
        ArrayList arrayList = new ArrayList();
        lz lzVar = new lz();
        lzVar.Ap = i;
        lzVar.Ar = currentTimeMillis;
        lzVar.Aq = 1;
        lzVar.errorCode = i2;
        arrayList.add(lzVar);
        String w = w(arrayList);
        if (w != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(w);
            eu().m(bz, stringBuilder.toString());
        }
    }

    void q(int i, int i2) {
        a(ev(), i, i2);
    }

    ArrayList<dx> x(ArrayList<lz> arrayList) {
        if (arrayList == null) {
            return null;
        }
        ArrayList<dx> arrayList2 = new ArrayList();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            lz lzVar = (lz) it.next();
            if (lzVar.Aq > 0) {
                dx dxVar = new dx();
                dxVar.setId(lzVar.Ap);
                dxVar.y("" + lzVar.Aq);
                dxVar.v((int) (lzVar.Ar / 1000));
                if (lzVar.errorCode != 0) {
                    dxVar.iV = new ArrayList();
                    dxVar.iV.add(Integer.valueOf(lzVar.errorCode));
                }
                arrayList2.add(dxVar);
            }
        }
        return arrayList2;
    }
}
