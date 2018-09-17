package tmsdkobf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class oe {
    private static pe<Integer, oe> HP = new pe(50);
    public String HB = "";
    public String HC = "";
    public int HD = 0;
    public String HE = "";
    private String HF = "";
    public long HG = -1;
    public String HH = "";
    public String HI = "";
    public String HJ = "";
    public String HK = "";
    public boolean HL = false;
    public boolean HM = false;
    private long HN = 0;
    private long HO = 0;
    public int errorCode = 0;

    public static void a(oe oeVar, int i) {
        if (oeVar != null) {
            oeVar.HN = System.currentTimeMillis();
            HP.put(Integer.valueOf(i), oeVar);
        }
    }

    public static oe bC(int i) {
        oe oeVar = (oe) HP.get(Integer.valueOf(i));
        if (oeVar != null) {
            oeVar.HO = System.currentTimeMillis();
        }
        HP.f(Integer.valueOf(i));
        return oeVar;
    }

    private HashMap<String, String> gR() {
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("B4", this.HE);
        hashMap.put("B20", this.HK);
        hashMap.put("B7", String.valueOf(this.errorCode));
        hashMap.put("B8", this.HH);
        hashMap.put("B10", this.HJ);
        hashMap.put("B9", this.HI);
        hashMap.put("B6", String.valueOf(this.HG));
        hashMap.put("B5", this.HF);
        hashMap.put("B3", this.HB);
        hashMap.put("B11", this.HC);
        hashMap.put("B12", String.valueOf(this.HD));
        hashMap.put("B21", String.valueOf(this.HL));
        hashMap.put("B22", String.valueOf(this.HM));
        return hashMap;
    }

    public void bB(int i) {
        this.HK += String.valueOf(i) + ";";
    }

    public void d(nl nlVar) {
        if (nlVar != null) {
            this.HF = "1";
            mb.n("TcpInfoUpload", toString());
            nlVar.a(gR());
        }
    }

    public void e(nl nlVar) {
    }

    public void f(nl nlVar) {
    }

    public void g(nl nlVar) {
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("|ip|" + this.HB);
        stringBuilder.append("|port|" + this.HC);
        stringBuilder.append("|tryTimes|" + this.HD);
        stringBuilder.append("|apn|" + this.HE);
        stringBuilder.append("|requestType|" + this.HF);
        stringBuilder.append("|requestTime|" + this.HG);
        stringBuilder.append("|errorCode|" + this.errorCode);
        stringBuilder.append("|cmdids|" + this.HK);
        stringBuilder.append("|iplist|" + this.HJ);
        stringBuilder.append("|lastRequest|" + this.HI);
        stringBuilder.append("|errorDetail|" + this.HH);
        stringBuilder.append("|isDetect|" + this.HL);
        stringBuilder.append("|isConnect|" + this.HM);
        return stringBuilder.toString();
    }

    public void u(ArrayList<String> arrayList) {
        if (arrayList != null && arrayList.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                if (str != null) {
                    stringBuilder.append(str);
                    stringBuilder.append(";");
                }
            }
        }
    }
}
