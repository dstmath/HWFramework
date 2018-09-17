package tmsdk.fg.module.qscanner;

import java.util.ArrayList;
import tmsdkobf.co;
import tmsdkobf.fq;
import tmsdkobf.fr;
import tmsdkobf.fs;

/* compiled from: Unknown */
public final class QScanResult extends fs {
    static ApkKey LV;
    static ArrayList<Integer> LW;
    static ArrayList<co> ga;
    public int advice;
    public ApkKey apkkey;
    public int category;
    public int certtype;
    public ArrayList<Integer> descids;
    public String dexsha1;
    public String discription;
    public String label;
    public int malwareid;
    public String name;
    public ArrayList<co> plugins;
    public int product;
    public int safelevel;
    public int type;
    public String url;

    public QScanResult() {
        this.apkkey = null;
        this.type = 0;
        this.label = "";
        this.discription = "";
        this.advice = 0;
        this.malwareid = 0;
        this.name = "";
        this.url = "";
        this.certtype = 0;
        this.safelevel = 0;
        this.product = 0;
        this.dexsha1 = "";
        this.plugins = null;
        this.descids = null;
        this.category = 0;
    }

    public QScanResult(ApkKey apkKey, int i, String str, String str2, int i2, int i3, String str3, String str4, int i4, int i5, int i6, String str5, ArrayList<co> arrayList, ArrayList<Integer> arrayList2, int i7) {
        this.apkkey = null;
        this.type = 0;
        this.label = "";
        this.discription = "";
        this.advice = 0;
        this.malwareid = 0;
        this.name = "";
        this.url = "";
        this.certtype = 0;
        this.safelevel = 0;
        this.product = 0;
        this.dexsha1 = "";
        this.plugins = null;
        this.descids = null;
        this.category = 0;
        this.apkkey = apkKey;
        this.type = i;
        this.label = str;
        this.discription = str2;
        this.advice = i2;
        this.malwareid = i3;
        this.name = str3;
        this.url = str4;
        this.certtype = i4;
        this.safelevel = i5;
        this.product = i6;
        this.dexsha1 = str5;
        this.plugins = arrayList;
        this.descids = arrayList2;
        this.category = i7;
    }

    public void readFrom(fq fqVar) {
        if (LV == null) {
            LV = new ApkKey();
        }
        this.apkkey = (ApkKey) fqVar.a(LV, 0, true);
        this.type = fqVar.a(this.type, 1, true);
        this.label = fqVar.a(2, false);
        this.discription = fqVar.a(3, false);
        this.advice = fqVar.a(this.advice, 4, false);
        this.malwareid = fqVar.a(this.malwareid, 5, false);
        this.name = fqVar.a(6, false);
        this.url = fqVar.a(7, false);
        this.certtype = fqVar.a(this.certtype, 8, false);
        this.safelevel = fqVar.a(this.safelevel, 9, false);
        this.product = fqVar.a(this.product, 10, false);
        this.dexsha1 = fqVar.a(11, false);
        if (ga == null) {
            ga = new ArrayList();
            ga.add(new co());
        }
        this.plugins = (ArrayList) fqVar.b(ga, 12, false);
        if (LW == null) {
            LW = new ArrayList();
            LW.add(Integer.valueOf(0));
        }
        this.descids = (ArrayList) fqVar.b(LW, 13, false);
        this.category = fqVar.a(this.category, 14, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.apkkey, 0);
        frVar.write(this.type, 1);
        if (this.label != null) {
            frVar.a(this.label, 2);
        }
        if (this.discription != null) {
            frVar.a(this.discription, 3);
        }
        frVar.write(this.advice, 4);
        frVar.write(this.malwareid, 5);
        if (this.name != null) {
            frVar.a(this.name, 6);
        }
        if (this.url != null) {
            frVar.a(this.url, 7);
        }
        frVar.write(this.certtype, 8);
        frVar.write(this.safelevel, 9);
        frVar.write(this.product, 10);
        if (this.dexsha1 != null) {
            frVar.a(this.dexsha1, 11);
        }
        if (this.plugins != null) {
            frVar.a(this.plugins, 12);
        }
        if (this.descids != null) {
            frVar.a(this.descids, 13);
        }
        frVar.write(this.category, 14);
    }
}
