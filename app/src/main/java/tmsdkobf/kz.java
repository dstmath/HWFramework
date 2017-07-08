package tmsdkobf;

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
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.tcc.TccDiff;
import tmsdk.common.utils.d;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;
import tmsdkobf.kp.a;
import tmsdkobf.kp.b;
import tmsdkobf.kp.c;

/* compiled from: Unknown */
public class kz implements kp, qj {
    private static kz wr;
    b wj;
    lp ws;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.kz.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.kz.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.kz.<clinit>():void");
    }

    private kz() {
        this.wj = null;
        this.ws = null;
        this.ws = (lp) fe.ad(12);
    }

    private static ao a(py pyVar, String str) {
        ao aoVar = new ao();
        if (pyVar != null) {
            aoVar.bu = pyVar.getSize();
            aoVar.certMd5 = pyVar.hD();
            aoVar.bv = pyVar.hA();
            aoVar.packageName = pyVar.getPackageName();
            aoVar.softName = pyVar.getAppName();
            aoVar.version = pyVar.getVersion();
            aoVar.bw = (long) pyVar.hB();
            aoVar.bx = pyVar.hC() / 1000;
            String aZ = pyVar.aZ();
            try {
                aoVar.aA = TccDiff.fileMd5(aZ);
            } catch (Throwable th) {
                d.c("SoftListProfileService", th);
                aoVar.aA = "";
            }
            aoVar.dexSha1 = bU(aZ);
            return aoVar;
        }
        aoVar.packageName = str;
        return aoVar;
    }

    private void a(List<a> list, Collection<fs> collection, int i) {
        for (fs fsVar : collection) {
            if (fsVar != null) {
                a aVar = new a();
                aVar.action = i;
                aVar.vV = fsVar;
                list.add(aVar);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean a(ao aoVar, ao aoVar2) {
        return (aoVar == null && aoVar2 == null) ? true : aoVar != null && aoVar2 != null && aoVar.bu == aoVar2.bu && aoVar.bw == aoVar2.bw && n(aoVar.packageName, aoVar2.packageName) && n(aoVar.softName, aoVar2.softName) && n(aoVar.certMd5, aoVar2.certMd5) && n(aoVar.aA, aoVar2.aA) && n(aoVar.version, aoVar2.version) && n(aoVar.dexSha1, aoVar2.dexSha1) && aoVar.bv == aoVar2.bv;
    }

    private ao bT(String str) {
        return a(this.ws.b(str, 89), str);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String bU(String str) {
        Throwable th;
        Throwable th2;
        ZipFile zipFile = null;
        if (str == null) {
            return "";
        }
        ZipFile zipFile2;
        BufferedReader bufferedReader;
        BufferedReader bufferedReader2;
        try {
            zipFile2 = new ZipFile(str);
            try {
                String readLine;
                if (new File(str).exists()) {
                    ZipEntry entry = zipFile2.getEntry("META-INF/MANIFEST.MF");
                    if (entry != null) {
                        int indexOf;
                        bufferedReader = new BufferedReader(new InputStreamReader(zipFile2.getInputStream(entry)));
                        while (true) {
                            try {
                                readLine = bufferedReader.readLine();
                                if (readLine == null) {
                                    break;
                                } else if (readLine.contains("classes.dex")) {
                                    readLine = bufferedReader.readLine();
                                    if (readLine != null && readLine.contains("SHA1-Digest")) {
                                        indexOf = readLine.indexOf(":");
                                        if (indexOf > 0) {
                                            break;
                                        }
                                    }
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                zipFile = zipFile2;
                                th2 = th;
                            }
                        }
                        readLine = readLine.substring(indexOf + 1).trim();
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (Throwable th4) {
                                d.c("SoftListProfileService", th4);
                            }
                        }
                        if (zipFile2 != null) {
                            try {
                                zipFile2.close();
                            } catch (Throwable th22) {
                                d.c("SoftListProfileService", th22);
                            }
                        }
                        return readLine;
                    }
                    if (bufferedReader2 != null) {
                        try {
                            bufferedReader2.close();
                        } catch (Throwable th32) {
                            d.c("SoftListProfileService", th32);
                        }
                    }
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (Throwable th222) {
                            d.c("SoftListProfileService", th222);
                        }
                    }
                    return "";
                }
                readLine = "";
                if (zipFile2 != null) {
                    try {
                        zipFile2.close();
                    } catch (Throwable th2222) {
                        d.c("SoftListProfileService", th2222);
                    }
                }
                return readLine;
            } catch (Throwable th42) {
                th = th42;
                Object obj = zipFile;
                zipFile = zipFile2;
                th2222 = th;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable th422) {
                        d.c("SoftListProfileService", th422);
                    }
                }
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (Throwable th322) {
                        d.c("SoftListProfileService", th322);
                    }
                }
                throw th2222;
            }
        } catch (Throwable th5) {
            th2222 = th5;
            bufferedReader = zipFile;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (zipFile != null) {
                zipFile.close();
            }
            throw th2222;
        }
    }

    public static kz dx() {
        if (wr == null) {
            synchronized (kz.class) {
                if (wr == null) {
                    wr = new kz();
                }
            }
        }
        return wr;
    }

    public static ArrayList<fs> dz() {
        ArrayList<fs> arrayList = new ArrayList();
        ArrayList c = TMServiceFactory.getSystemInfoService().c(89, 2);
        if (c == null || c.size() <= 0) {
            return arrayList;
        }
        Iterator it = c.iterator();
        while (it.hasNext()) {
            py pyVar = (py) it.next();
            if (pyVar != null) {
                arrayList.add(a(pyVar, pyVar.getPackageName()));
            }
        }
        return arrayList;
    }

    private void f(String str, int i) {
        if (this.wj != null) {
            fs bT = bT(str);
            if ((bT.bx > 0 ? 1 : null) == null) {
                bT.bx = System.currentTimeMillis() / 1000;
            }
            ArrayList arrayList = new ArrayList();
            a aVar = new a();
            aVar.action = i;
            aVar.vV = bT;
            arrayList.add(aVar);
            this.wj.q(arrayList);
        }
    }

    private static final boolean n(String str, String str2) {
        return str != str2 ? str != null ? str.equals(str2) : false : true;
    }

    private ArrayList<a> t(ArrayList<fs> arrayList) {
        Object arrayList2 = new ArrayList();
        if (arrayList == null || arrayList.size() == 0) {
            return arrayList2;
        }
        ArrayList dv = ky.du().dv();
        if (dv == null || dv.size() == 0 || la.dA().dB()) {
            d.d("SoftListProfileService", "fullCheck|AllReport size:" + arrayList.size());
            a(arrayList2, arrayList, 0);
            if (la.dA().dB()) {
                la.dA().y(false);
            }
            return arrayList2;
        }
        d.d("SoftListProfileService", "not !!! fullCheck|AllReport size:" + arrayList.size());
        Collection linkedList = new LinkedList();
        Collection linkedList2 = new LinkedList();
        Iterator it = dv.iterator();
        while (it.hasNext()) {
            ao aoVar = (ao) it.next();
            if (!(aoVar == null || aoVar.packageName == null || aoVar.packageName.trim().equals(""))) {
                Iterator it2 = arrayList.iterator();
                while (it2.hasNext()) {
                    ao aoVar2 = (ao) ((fs) it2.next());
                    if (aoVar2.packageName.equals(aoVar.packageName)) {
                        if (!a(aoVar2, aoVar)) {
                            linkedList.add(aoVar2);
                        }
                        if (r1 == null) {
                            arrayList.remove(r1);
                        } else {
                            aoVar.bx = 0;
                            linkedList2.add(aoVar);
                        }
                    }
                }
                Object obj = null;
                if (obj == null) {
                    aoVar.bx = 0;
                    linkedList2.add(aoVar);
                } else {
                    arrayList.remove(obj);
                }
            }
        }
        a(arrayList2, linkedList, 3);
        a(arrayList2, linkedList2, 2);
        a(arrayList2, arrayList, 1);
        return arrayList2;
    }

    public void a(hw hwVar) {
        int ds = la.dA().ds();
        if (ds <= 0) {
            ds = 500000;
        }
        d.e("SoftListProfileService", "softListFullUploadQuantity : " + ds);
        hv.bG().a(CallerIdent.getIdent(1, UpdateConfig.UPDATE_FLAG_PAY_LIST), 2, new c() {
            final /* synthetic */ kz wt;

            {
                this.wt = r1;
            }

            public ArrayList<fs> dn() {
                return kz.dz();
            }
        }, hwVar, ds);
    }

    public void a(b bVar) {
        if (ht.bD().aJ(dl())) {
            ht.bD().b(dl(), false);
        }
        bVar.q(t(dz()));
    }

    public void aZ(int i) {
        la.dA().aZ(i);
    }

    public void b(b bVar) {
        this.ws.b(this);
        this.ws.a(this);
        this.wj = bVar;
    }

    public void bQ(String str) {
        d.e("SoftListProfileService", "onPackageAdded : " + str);
        f(str, 1);
    }

    public void bR(String str) {
        d.e("SoftListProfileService", "onPackageReinstall : " + str);
        f(str, 3);
    }

    public void bS(String str) {
        d.e("SoftListProfileService", "onPackageRemoved : " + str);
        f(str, 2);
    }

    public boolean dk() {
        return this.wj != null;
    }

    public int dl() {
        return 2;
    }

    public void dm() {
        ArrayList c = ((lp) fe.ad(12)).c(89, 2);
        LinkedList linkedList = new LinkedList();
        Iterator it = c.iterator();
        while (it.hasNext()) {
            py pyVar = (py) it.next();
            if (pyVar != null) {
                ao a = a(pyVar, pyVar.getPackageName());
                if (!(a == null || a.packageName == null || "".equals(a.packageName))) {
                    linkedList.add(a);
                }
            }
        }
    }

    public void dy() {
        la.dA().y(true);
    }

    public boolean o(ArrayList<a> arrayList) {
        boolean s = ky.du().s(arrayList);
        d.d("SoftListProfileService", "SoftListProfile UpdateImage : " + s);
        return s;
    }

    public void p(ArrayList<a> arrayList) {
        ArrayList arrayList2 = null;
        if (arrayList != null && arrayList.size() != 0) {
            Iterator it = arrayList.iterator();
            ArrayList arrayList3 = null;
            ArrayList arrayList4 = null;
            ArrayList arrayList5 = null;
            while (it.hasNext()) {
                a aVar = (a) it.next();
                if (!(aVar == null || aVar.vV == null)) {
                    switch (aVar.action) {
                        case SpaceManager.ERROR_CODE_OK /*0*/:
                            if (arrayList2 == null) {
                                arrayList2 = new ArrayList();
                            }
                            arrayList2.add(aVar.vV);
                            break;
                        case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                            if (arrayList3 == null) {
                                arrayList3 = new ArrayList();
                            }
                            arrayList3.add(aVar.vV);
                            break;
                        case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                            if (arrayList4 == null) {
                                arrayList4 = new ArrayList();
                            }
                            arrayList4.add(aVar.vV);
                            break;
                        case FileInfo.TYPE_BIGFILE /*3*/:
                            if (arrayList5 == null) {
                                arrayList5 = new ArrayList();
                            }
                            arrayList5.add(aVar.vV);
                            break;
                    }
                    arrayList3 = arrayList3;
                    arrayList4 = arrayList4;
                    arrayList5 = arrayList5;
                }
            }
            if (arrayList2 != null && arrayList2.size() > 0) {
                hv.bG().a(CallerIdent.getIdent(1, UpdateConfig.UPDATE_FLAG_PAY_LIST), 2, 0, arrayList2);
            }
            if (arrayList5 != null && arrayList5.size() > 0) {
                hv.bG().a(CallerIdent.getIdent(1, UpdateConfig.UPDATE_FLAG_PAY_LIST), 2, 3, arrayList5);
            }
            if (arrayList4 != null && arrayList4.size() > 0) {
                hv.bG().a(CallerIdent.getIdent(1, UpdateConfig.UPDATE_FLAG_PAY_LIST), 2, 2, arrayList4);
            }
            if (arrayList3 != null && arrayList3.size() > 0) {
                hv.bG().a(CallerIdent.getIdent(1, UpdateConfig.UPDATE_FLAG_PAY_LIST), 2, 1, arrayList3);
            }
        }
    }
}
