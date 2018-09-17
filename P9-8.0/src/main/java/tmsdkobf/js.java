package tmsdkobf;

import com.qq.taf.jce.JceStruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import tmsdk.common.CallerIdent;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.utils.f;
import tmsdkobf.ji.a;
import tmsdkobf.ji.b;
import tmsdkobf.ji.c;

public class js implements ji, pg {
    private static js tu = null;
    b tm;
    kh tv;

    private js() {
        this.tm = null;
        this.tv = null;
        this.tv = (kh) fj.D(12);
    }

    private static av a(ov ovVar, String str) {
        av avVar = new av();
        if (ovVar != null) {
            avVar.ca = ovVar.getSize();
            avVar.bZ = ovVar.hz();
            avVar.cb = ovVar.hx();
            avVar.packageName = ovVar.getPackageName();
            avVar.softName = ovVar.getAppName();
            avVar.version = ovVar.getVersion();
            avVar.cd = (long) ovVar.getVersionCode();
            avVar.ce = ovVar.hy() / 1000;
            String hB = ovVar.hB();
            avVar.aS = "";
            avVar.cc = aU(hB);
            return avVar;
        }
        avVar.packageName = str;
        return avVar;
    }

    private void a(List<a> list, Collection<JceStruct> collection, int i) {
        for (JceStruct jceStruct : collection) {
            if (jceStruct != null) {
                a aVar = new a();
                aVar.action = i;
                aVar.ta = jceStruct;
                list.add(aVar);
            }
        }
    }

    /* JADX WARNING: Missing block: B:3:0x0006, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean a(av avVar, av avVar2) {
        if (avVar == null && avVar2 == null) {
            return true;
        }
        return avVar != null && avVar2 != null && avVar.ca == avVar2.ca && avVar.cd == avVar2.cd && k(avVar.packageName, avVar2.packageName) && k(avVar.softName, avVar2.softName) && k(avVar.bZ, avVar2.bZ) && k(avVar.aS, avVar2.aS) && k(avVar.version, avVar2.version) && k(avVar.cc, avVar2.cc) && avVar.cb == avVar2.cb;
    }

    private av aT(String str) {
        return a(this.tv.a(str, 89), str);
    }

    /* JADX WARNING: Removed duplicated region for block: B:71:0x00d9 A:{SYNTHETIC, Splitter: B:71:0x00d9} */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x00cb A:{SYNTHETIC, Splitter: B:67:0x00cb} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00eb A:{SYNTHETIC, Splitter: B:79:0x00eb} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x00f7 A:{SYNTHETIC, Splitter: B:83:0x00f7} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x00d9 A:{SYNTHETIC, Splitter: B:71:0x00d9} */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x00cb A:{SYNTHETIC, Splitter: B:67:0x00cb} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00eb A:{SYNTHETIC, Splitter: B:79:0x00eb} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x00f7 A:{SYNTHETIC, Splitter: B:83:0x00f7} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String aU(String str) {
        Throwable th;
        if (str == null) {
            return "";
        }
        BufferedReader bufferedReader = null;
        ZipFile zipFile = null;
        try {
            ZipFile zipFile2 = new ZipFile(str);
            try {
                String readLine;
                if (new File(str).exists()) {
                    ZipEntry entry = zipFile2.getEntry("META-INF/MANIFEST.MF");
                    if (entry != null) {
                        BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(zipFile2.getInputStream(entry)));
                        while (true) {
                            try {
                                readLine = bufferedReader2.readLine();
                                if (readLine == null) {
                                    bufferedReader = bufferedReader2;
                                    break;
                                } else if (readLine.contains("classes.dex")) {
                                    readLine = bufferedReader2.readLine();
                                    if (readLine != null && readLine.contains("SHA1-Digest")) {
                                        int indexOf = readLine.indexOf(":");
                                        if (indexOf > 0) {
                                            String trim = readLine.substring(indexOf + 1).trim();
                                            if (bufferedReader2 != null) {
                                                try {
                                                    bufferedReader2.close();
                                                } catch (Throwable th2) {
                                                    f.e("SoftListProfileService", th2);
                                                }
                                            }
                                            if (zipFile2 != null) {
                                                try {
                                                    zipFile2.close();
                                                } catch (Throwable th22) {
                                                    f.e("SoftListProfileService", th22);
                                                }
                                            }
                                            return trim;
                                        }
                                    }
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                zipFile = zipFile2;
                                bufferedReader = bufferedReader2;
                                if (bufferedReader != null) {
                                }
                                if (zipFile != null) {
                                }
                                throw th;
                            }
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable th4) {
                            f.e("SoftListProfileService", th4);
                        }
                    }
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (Throwable th42) {
                            f.e("SoftListProfileService", th42);
                        }
                    }
                    zipFile = zipFile2;
                    return "";
                }
                readLine = "";
                if (null != null) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable th5) {
                        f.e("SoftListProfileService", th5);
                    }
                }
                if (zipFile2 != null) {
                    try {
                        zipFile2.close();
                    } catch (Throwable th52) {
                        f.e("SoftListProfileService", th52);
                    }
                }
                return readLine;
            } catch (Throwable th6) {
                th = th6;
                zipFile = zipFile2;
                if (bufferedReader != null) {
                }
                if (zipFile != null) {
                }
                throw th;
            }
        } catch (Throwable th7) {
            th = th7;
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Throwable th8) {
                    f.e("SoftListProfileService", th8);
                }
            }
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Throwable th82) {
                    f.e("SoftListProfileService", th82);
                }
            }
            throw th;
        }
    }

    private void b(String str, int i) {
        if (this.tm != null) {
            JceStruct aT = aT(str);
            if ((aT.ce > 0 ? 1 : null) == null) {
                aT.ce = System.currentTimeMillis() / 1000;
            }
            ArrayList arrayList = new ArrayList();
            a aVar = new a();
            aVar.action = i;
            aVar.ta = aT;
            arrayList.add(aVar);
            this.tm.j(arrayList);
        }
    }

    public static js cE() {
        if (tu == null) {
            Class cls = js.class;
            synchronized (js.class) {
                if (tu == null) {
                    tu = new js();
                }
            }
        }
        return tu;
    }

    private static final boolean k(String str, String str2) {
        return str != str2 ? str != null ? str.equals(str2) : false : true;
    }

    public void a(gt gtVar) {
        int cz = jt.cH().cz();
        if (cz <= 0) {
            cz = 500000;
        }
        f.d("SoftListProfileService", "softListFullUploadQuantity : " + cz);
        gs.bc().a(CallerIdent.getIdent(1, 4294967296L), 2, new c() {
            public ArrayList<JceStruct> cu() {
                return js.this.cG();
            }
        }, gtVar, cz);
    }

    public void a(b bVar) {
        this.tv.b(this);
        this.tv.a(this);
        this.tm = bVar;
    }

    public void aQ(String str) {
        f.d("SoftListProfileService", "onPackageAdded : " + str);
        b(str, 1);
    }

    public void aR(String str) {
        f.d("SoftListProfileService", "onPackageReinstall : " + str);
        b(str, 3);
    }

    public void aS(String str) {
        f.d("SoftListProfileService", "onPackageRemoved : " + str);
        b(str, 2);
    }

    public void ag(int i) {
        jt.cH().ag(i);
    }

    public void cF() {
        jt.cH().k(true);
    }

    public ArrayList<JceStruct> cG() {
        ArrayList<JceStruct> arrayList = new ArrayList();
        ArrayList f = TMServiceFactory.getSystemInfoService().f(89, 2);
        if (f == null || f.size() <= 0) {
            return arrayList;
        }
        Iterator it = f.iterator();
        while (it.hasNext()) {
            ov ovVar = (ov) it.next();
            if (ovVar != null) {
                arrayList.add(a(ovVar, ovVar.getPackageName()));
            }
        }
        return arrayList;
    }

    public int cs() {
        return 2;
    }

    public void ct() {
        ArrayList f = ((kh) fj.D(12)).f(89, 2);
        LinkedList linkedList = new LinkedList();
        Iterator it = f.iterator();
        while (it.hasNext()) {
            ov ovVar = (ov) it.next();
            if (ovVar != null) {
                av a = a(ovVar, ovVar.getPackageName());
                if (!(a == null || a.packageName == null)) {
                    if (!"".equals(a.packageName)) {
                        linkedList.add(a);
                    }
                }
            }
        }
    }

    public boolean h(ArrayList<a> arrayList) {
        boolean l = jr.cB().l(arrayList);
        f.f("SoftListProfileService", "SoftListProfile UpdateImage : " + l);
        return l;
    }

    public void i(ArrayList<a> arrayList) {
        if (arrayList != null && arrayList.size() != 0) {
            ArrayList arrayList2 = null;
            ArrayList arrayList3 = null;
            ArrayList arrayList4 = null;
            ArrayList arrayList5 = null;
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                if (!(aVar == null || aVar.ta == null)) {
                    switch (aVar.action) {
                        case 0:
                            if (arrayList5 == null) {
                                arrayList5 = new ArrayList();
                            }
                            arrayList5.add(aVar.ta);
                            break;
                        case 1:
                            if (arrayList4 == null) {
                                arrayList4 = new ArrayList();
                            }
                            arrayList4.add(aVar.ta);
                            break;
                        case 2:
                            if (arrayList3 == null) {
                                arrayList3 = new ArrayList();
                            }
                            arrayList3.add(aVar.ta);
                            break;
                        case 3:
                            if (arrayList2 == null) {
                                arrayList2 = new ArrayList();
                            }
                            arrayList2.add(aVar.ta);
                            break;
                        default:
                            break;
                    }
                }
            }
            if (arrayList5 != null && arrayList5.size() > 0) {
                gs.bc().a(CallerIdent.getIdent(1, 4294967296L), 2, 0, arrayList5);
            }
            if (arrayList2 != null && arrayList2.size() > 0) {
                gs.bc().a(CallerIdent.getIdent(1, 4294967296L), 2, 3, arrayList2);
            }
            if (arrayList3 != null && arrayList3.size() > 0) {
                gs.bc().a(CallerIdent.getIdent(1, 4294967296L), 2, 2, arrayList3);
            }
            if (arrayList4 != null && arrayList4.size() > 0) {
                gs.bc().a(CallerIdent.getIdent(1, 4294967296L), 2, 1, arrayList4);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00e6  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00c4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayList<a> m(ArrayList<JceStruct> arrayList) {
        Object arrayList2 = new ArrayList();
        if (arrayList == null || arrayList.size() == 0) {
            return arrayList2;
        }
        ArrayList cC = jr.cB().cC();
        if (cC == null || cC.size() == 0 || jt.cH().cI()) {
            f.f("SoftListProfileService", "fullCheck|AllReport size:" + arrayList.size());
            a(arrayList2, arrayList, 0);
            if (jt.cH().cI()) {
                jt.cH().k(false);
            }
            return arrayList2;
        }
        f.f("SoftListProfileService", "not !!! fullCheck|AllReport size:" + arrayList.size());
        Collection linkedList = new LinkedList();
        Collection linkedList2 = new LinkedList();
        Iterator it = cC.iterator();
        while (it.hasNext()) {
            av avVar = (av) it.next();
            if (!(avVar == null || avVar.packageName == null || avVar.packageName.trim().equals(""))) {
                Object obj = null;
                Iterator it2 = arrayList.iterator();
                while (it2.hasNext()) {
                    av avVar2 = (av) ((JceStruct) it2.next());
                    if (avVar2.packageName.equals(avVar.packageName)) {
                        av obj2 = avVar2;
                        if (!a(avVar2, avVar)) {
                            linkedList.add(avVar2);
                        }
                        if (obj2 == null) {
                            arrayList.remove(obj2);
                        } else {
                            avVar.ce = 0;
                            linkedList2.add(avVar);
                        }
                    }
                }
                if (obj2 == null) {
                }
            }
        }
        a(arrayList2, linkedList, 3);
        a(arrayList2, linkedList2, 2);
        a(arrayList2, arrayList, 1);
        return arrayList2;
    }
}
