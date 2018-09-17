package tmsdkobf;

import android.os.Environment;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.lang.MultiLangManager;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.tcc.DeepCleanEngine;
import tmsdk.common.tcc.QFile;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.d;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;

/* compiled from: Unknown */
public class rf {
    public static final List<String> MY = null;
    static DeepCleanEngine Nh;
    private boolean MZ;
    public long Na;
    public long Nb;
    public long Nc;
    public long Nd;
    public long Ne;
    public long Nf;
    private b Ng;
    private String Ni;
    private TreeMap<Integer, List<a>> Nj;
    private HashMap<String, b> Nk;

    /* compiled from: Unknown */
    static class a {
        public String Nl;
        public String Nm;
        public String Nn;
        public String No;
        public String Np;
        public String Nq;
        public String Nr;
        public String Ns;
        public String Nt;
        private String Nu;
        public String[] Nv;
        public String mFileName;
        public String mPath;

        a() {
            this.Nl = "";
            this.mPath = "";
            this.Nm = "";
            this.mFileName = "";
            this.Nn = "";
            this.No = "";
            this.Np = "";
            this.Nq = "";
            this.Nr = "";
            this.Ns = "";
            this.Nt = "";
            this.Nu = null;
            this.Nv = null;
        }
    }

    /* compiled from: Unknown */
    static class b {
        boolean NA;
        boolean NB;
        private String Nu;
        public List<a> Nw;
        public HashMap<String, List<a>> Nx;
        public TreeMap<String, List<a>> Ny;
        public TreeMap<String, List<a>> Nz;
        public String mAppName;
        public String mPkg;
        public String om;

        b() {
            this.Nu = null;
            this.Nx = new HashMap();
            this.Ny = new TreeMap();
            this.Nz = new TreeMap();
            this.NA = false;
            this.NB = false;
        }

        public void jB() {
            if (!this.NA && this != null && this.Nw != null) {
                for (a aVar : this.Nw) {
                    HashMap hashMap;
                    if (!this.NB && aVar.Nl.equals("4")) {
                        this.NB = true;
                    }
                    if (aVar.mPath.contains("*")) {
                        hashMap = this.Ny;
                        aVar.Nv = aVar.mPath.split("/");
                    } else {
                        hashMap = !aVar.mPath.contains("//") ? this.Nx : this.Nz;
                    }
                    HashMap hashMap2 = hashMap;
                    if (hashMap2 != null) {
                        List list = (List) hashMap2.get(aVar.mPath);
                        if (list == null) {
                            list = new ArrayList();
                        }
                        list.add(aVar);
                        hashMap2.put(aVar.mPath, list);
                    }
                }
                this.NA = true;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.rf.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.rf.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.rf.<clinit>():void");
    }

    public rf() {
        this.MZ = false;
        this.Na = 0;
        this.Nb = 0;
        this.Nc = 0;
        this.Nd = 0;
        this.Ne = 0;
        this.Nf = 0;
        this.Ng = null;
        this.Ni = null;
        this.Nj = null;
        this.Nk = new HashMap();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void Q(boolean z) {
        cx cxVar = (cx) nj.b(TMSDKContext.getApplicaionContext(), UpdateConfig.DEEPCLEAN_SDCARD_SCAN_RULE_NAME_V2, UpdateConfig.intToString(40225), new cx(), "UTF-8");
        if (cxVar != null && cxVar.gw != null) {
            Iterator it = cxVar.gw.iterator();
            while (it.hasNext()) {
                cw cwVar = (cw) it.next();
                try {
                    b bVar;
                    switch (Integer.valueOf(cwVar.go).intValue()) {
                        case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                            if (!(cwVar.go == null || cwVar.gp == null || cwVar.gq == null || cwVar.gr == null || "".equals(cwVar.gp) || "".equals(cwVar.gq) || "".equals(cwVar.gr))) {
                                bVar = (b) this.Nk.get(cwVar.gp);
                                if (bVar == null) {
                                    bVar = new b();
                                    bVar.Nw = new ArrayList();
                                    this.Nk.put(cwVar.gp, bVar);
                                }
                                bVar.om = cwVar.gp;
                                bVar.mPkg = cwVar.gq;
                                if (!z || TextUtils.isEmpty(cwVar.gs)) {
                                    bVar.mAppName = cwVar.gr;
                                    break;
                                } else {
                                    bVar.mAppName = cwVar.gs;
                                    break;
                                }
                            }
                            break;
                        case FileInfo.TYPE_BIGFILE /*3*/:
                            a aVar = new a();
                            if (!(cwVar.gp == null || cwVar.gq == null || cwVar.gr == null || cwVar.gs == null)) {
                                bVar = (b) this.Nk.get(cwVar.gq);
                                if (bVar == null) {
                                    bVar = new b();
                                    bVar.om = cwVar.gq;
                                    bVar.Nw = new ArrayList();
                                    this.Nk.put(cwVar.gq, bVar);
                                }
                                aVar.mPath = cwVar.gp.toLowerCase();
                                aVar.Nm = cwVar.gq;
                                aVar.Nl = cwVar.go;
                                if (z && !TextUtils.isEmpty(cwVar.gu)) {
                                    aVar.Nn = cwVar.gu;
                                } else {
                                    aVar.Nn = cwVar.gr;
                                }
                                aVar.No = cwVar.gs;
                                bVar.Nw.add(aVar);
                                break;
                            }
                            break;
                        case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                            break;
                        default:
                            break;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int a(a aVar) {
        int i = 0;
        if (aVar == null) {
            return 0;
        }
        if (!aVar.mFileName.equals("")) {
            i = 1;
        }
        if (!aVar.Np.equals("")) {
            i++;
        }
        if (!aVar.Nq.equals("")) {
            i++;
        }
        if (!aVar.Nr.equals("")) {
            i++;
        }
        if (!aVar.Ns.equals("")) {
            i++;
        }
        return i;
    }

    private a a(b bVar, String str, String[] strArr, QFile qFile) {
        if (bVar == null || bVar.Nw == null || bVar.Nw.size() == 0 || strArr == null) {
            return null;
        }
        int intValue;
        int i;
        TreeMap treeMap;
        TreeMap treeMap2;
        int i2;
        int length;
        String str2;
        int i3;
        List list;
        List arrayList;
        String[] strArr2;
        QFile qFile2;
        List<a> list2;
        a aVar;
        a aVar2;
        String str3 = "";
        if (strArr.length > 2) {
            int i4 = 0;
            for (String str4 : strArr) {
                i4++;
                if (i4 == strArr.length) {
                    break;
                }
                if (!(str4 == null || str4.equals(""))) {
                    str3 = str3 + "/" + str4;
                }
            }
        }
        if (this.Ni == null) {
            this.Ni = str3;
        } else if (str3.equals(this.Ni)) {
            int i5 = 1;
            treeMap = this.Nj;
            if (this.Ni != null) {
                return null;
            }
            if (treeMap == null && r2 == null) {
                treeMap2 = new TreeMap();
                i2 = 0;
                length = strArr.length;
                i = 0;
                str3 = "";
                while (i < length) {
                    str2 = strArr[i];
                    i3 = i2 + 1;
                    if (i3 != strArr.length) {
                        break;
                    }
                    if (!(str2 == null || str2.equals(""))) {
                        str2 = str3 + "/" + str2;
                        list = (List) bVar.Nx.get(str2);
                        if (list != null) {
                            if (bVar.NB) {
                                arrayList = new ArrayList();
                                arrayList.add(list.get(0));
                                treeMap2.put(Integer.valueOf(i3), arrayList);
                                this.Nj = treeMap2;
                                return (a) list.get(0);
                            }
                            arrayList = (List) treeMap2.get(Integer.valueOf(i3));
                            if (arrayList == null) {
                                arrayList = new ArrayList();
                                treeMap2.put(Integer.valueOf(i3), arrayList);
                            }
                            arrayList.addAll(list);
                        }
                        str3 = str2;
                    }
                    i++;
                    i2 = i3;
                }
                long currentTimeMillis = System.currentTimeMillis();
                for (Entry entry : bVar.Nz.entrySet()) {
                    if (Nh != null && Nh.isMatchPath(this.Ni, (String) entry.getKey())) {
                        arrayList = (List) treeMap2.get(Integer.valueOf(strArr.length - 1));
                        if (arrayList == null) {
                            arrayList = new ArrayList();
                            treeMap2.put(Integer.valueOf(strArr.length - 1), arrayList);
                        }
                        arrayList.addAll((Collection) entry.getValue());
                    }
                }
                this.Na = (System.currentTimeMillis() - currentTimeMillis) + this.Na;
                currentTimeMillis = System.currentTimeMillis();
                for (Entry entry2 : bVar.Ny.entrySet()) {
                    strArr2 = ((a) ((List) entry2.getValue()).get(0)).Nv;
                    if (a(strArr2, strArr)) {
                        if (bVar.NB) {
                            arrayList = (List) treeMap2.get(Integer.valueOf(strArr2.length));
                            if (arrayList == null) {
                                arrayList = new ArrayList();
                                treeMap2.put(Integer.valueOf(strArr2.length), arrayList);
                            }
                            arrayList.addAll((Collection) entry2.getValue());
                        } else {
                            List arrayList2 = new ArrayList();
                            arrayList2.add(((List) entry2.getValue()).get(0));
                            treeMap2.put(Integer.valueOf(strArr2.length), arrayList2);
                            this.Nj = treeMap2;
                            return (a) ((List) entry2.getValue()).get(0);
                        }
                    }
                }
                this.Nc = (System.currentTimeMillis() - currentTimeMillis) + this.Nc;
            } else {
                treeMap2 = treeMap;
            }
            if (treeMap2 == null || treeMap2.size() == 0) {
                return null;
            }
            if (r2 == null) {
                this.Nj = treeMap2;
            }
            qFile2 = qFile;
            for (intValue = ((Integer) treeMap2.lastKey()).intValue(); intValue > 0; intValue--) {
                list2 = (List) treeMap2.get(Integer.valueOf(intValue));
                if (list2 == null) {
                    aVar = null;
                    for (a aVar22 : list2) {
                        if (aVar22.Nl.equals("4")) {
                            if (qFile2 == null) {
                                qFile2 = new QFile(Environment.getExternalStorageDirectory().getAbsolutePath() + str);
                                qFile2.fillExtraInfo();
                            }
                            if (Nh == null) {
                                if (aVar22.mFileName.equals("") || Nh.isMatchFile(strArr[strArr.length - 1], aVar22.mFileName)) {
                                    if (aVar22.Np.equals("") || Nh.isMatchFileSize(qFile2.size, aVar22.Np)) {
                                        if (aVar22.Nq.equals("") || Nh.isMatchTime(qFile2.createTime, aVar22.Nq)) {
                                            if (aVar22.Nr.equals("") || Nh.isMatchTime(qFile2.modifyTime, aVar22.Nr)) {
                                                if (!aVar22.Ns.equals("")) {
                                                    if (Nh.isMatchTime(qFile2.accessTime, aVar22.Ns)) {
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (aVar != null) {
                            if (aVar.Nl.equals("3")) {
                                if (!aVar22.Nl.equals("4")) {
                                }
                            }
                            if (aVar22.Nl.equals("3")) {
                                if (a(aVar) >= a(aVar22)) {
                                    aVar22 = aVar;
                                }
                            }
                        }
                        aVar = aVar22;
                    }
                    if (aVar != null) {
                        return aVar;
                    }
                }
            }
            return null;
        } else {
            this.Ni = str3;
            this.Nj = null;
        }
        treeMap = null;
        Object obj = null;
        if (this.Ni != null) {
            return null;
        }
        if (treeMap == null) {
            treeMap2 = new TreeMap();
            i2 = 0;
            length = strArr.length;
            i = 0;
            str3 = "";
            while (i < length) {
                str2 = strArr[i];
                i3 = i2 + 1;
                if (i3 != strArr.length) {
                    break;
                    long currentTimeMillis2 = System.currentTimeMillis();
                    for (Entry entry22 : bVar.Nz.entrySet()) {
                        arrayList = (List) treeMap2.get(Integer.valueOf(strArr.length - 1));
                        if (arrayList == null) {
                            arrayList = new ArrayList();
                            treeMap2.put(Integer.valueOf(strArr.length - 1), arrayList);
                        }
                        arrayList.addAll((Collection) entry22.getValue());
                    }
                    this.Na = (System.currentTimeMillis() - currentTimeMillis2) + this.Na;
                    currentTimeMillis2 = System.currentTimeMillis();
                    for (Entry entry222 : bVar.Ny.entrySet()) {
                        strArr2 = ((a) ((List) entry222.getValue()).get(0)).Nv;
                        if (a(strArr2, strArr)) {
                            if (bVar.NB) {
                                List arrayList22 = new ArrayList();
                                arrayList22.add(((List) entry222.getValue()).get(0));
                                treeMap2.put(Integer.valueOf(strArr2.length), arrayList22);
                                this.Nj = treeMap2;
                                return (a) ((List) entry222.getValue()).get(0);
                            }
                            arrayList = (List) treeMap2.get(Integer.valueOf(strArr2.length));
                            if (arrayList == null) {
                                arrayList = new ArrayList();
                                treeMap2.put(Integer.valueOf(strArr2.length), arrayList);
                            }
                            arrayList.addAll((Collection) entry222.getValue());
                        }
                    }
                    this.Nc = (System.currentTimeMillis() - currentTimeMillis2) + this.Nc;
                    if (treeMap2 == null) {
                        if (obj == null) {
                            this.Nj = treeMap2;
                        }
                        qFile2 = qFile;
                        for (intValue = ((Integer) treeMap2.lastKey()).intValue(); intValue > 0; intValue--) {
                            list2 = (List) treeMap2.get(Integer.valueOf(intValue));
                            if (list2 == null) {
                                aVar = null;
                                for (a aVar222 : list2) {
                                    if (aVar222.Nl.equals("4")) {
                                        if (qFile2 == null) {
                                            qFile2 = new QFile(Environment.getExternalStorageDirectory().getAbsolutePath() + str);
                                            qFile2.fillExtraInfo();
                                        }
                                        if (Nh == null) {
                                            if (aVar222.mFileName.equals("")) {
                                            }
                                            if (aVar222.Np.equals("")) {
                                            }
                                            if (aVar222.Nq.equals("")) {
                                            }
                                            if (aVar222.Nr.equals("")) {
                                            }
                                            if (aVar222.Ns.equals("")) {
                                                if (Nh.isMatchTime(qFile2.accessTime, aVar222.Ns)) {
                                                }
                                            }
                                        }
                                    }
                                    if (aVar != null) {
                                        if (aVar.Nl.equals("3")) {
                                            if (aVar222.Nl.equals("4")) {
                                            }
                                        }
                                        if (aVar222.Nl.equals("3")) {
                                            if (a(aVar) >= a(aVar222)) {
                                                aVar222 = aVar;
                                            }
                                        }
                                    }
                                    aVar = aVar222;
                                }
                                if (aVar != null) {
                                    return aVar;
                                }
                            }
                        }
                        return null;
                    }
                    return null;
                }
                str2 = str3 + "/" + str2;
                list = (List) bVar.Nx.get(str2);
                if (list != null) {
                    if (bVar.NB) {
                        arrayList = new ArrayList();
                        arrayList.add(list.get(0));
                        treeMap2.put(Integer.valueOf(i3), arrayList);
                        this.Nj = treeMap2;
                        return (a) list.get(0);
                    }
                    arrayList = (List) treeMap2.get(Integer.valueOf(i3));
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                        treeMap2.put(Integer.valueOf(i3), arrayList);
                    }
                    arrayList.addAll(list);
                }
                str3 = str2;
                i++;
                i2 = i3;
            }
            long currentTimeMillis22 = System.currentTimeMillis();
            for (Entry entry2222 : bVar.Nz.entrySet()) {
                arrayList = (List) treeMap2.get(Integer.valueOf(strArr.length - 1));
                if (arrayList == null) {
                    arrayList = new ArrayList();
                    treeMap2.put(Integer.valueOf(strArr.length - 1), arrayList);
                }
                arrayList.addAll((Collection) entry2222.getValue());
            }
            this.Na = (System.currentTimeMillis() - currentTimeMillis22) + this.Na;
            currentTimeMillis22 = System.currentTimeMillis();
            for (Entry entry22222 : bVar.Ny.entrySet()) {
                strArr2 = ((a) ((List) entry22222.getValue()).get(0)).Nv;
                if (a(strArr2, strArr)) {
                    if (bVar.NB) {
                        arrayList = (List) treeMap2.get(Integer.valueOf(strArr2.length));
                        if (arrayList == null) {
                            arrayList = new ArrayList();
                            treeMap2.put(Integer.valueOf(strArr2.length), arrayList);
                        }
                        arrayList.addAll((Collection) entry22222.getValue());
                    } else {
                        List arrayList222 = new ArrayList();
                        arrayList222.add(((List) entry22222.getValue()).get(0));
                        treeMap2.put(Integer.valueOf(strArr2.length), arrayList222);
                        this.Nj = treeMap2;
                        return (a) ((List) entry22222.getValue()).get(0);
                    }
                }
            }
            this.Nc = (System.currentTimeMillis() - currentTimeMillis22) + this.Nc;
            if (treeMap2 == null) {
                if (obj == null) {
                    this.Nj = treeMap2;
                }
                qFile2 = qFile;
                for (intValue = ((Integer) treeMap2.lastKey()).intValue(); intValue > 0; intValue--) {
                    list2 = (List) treeMap2.get(Integer.valueOf(intValue));
                    if (list2 == null) {
                        aVar = null;
                        for (a aVar2222 : list2) {
                            if (aVar2222.Nl.equals("4")) {
                                if (qFile2 == null) {
                                    qFile2 = new QFile(Environment.getExternalStorageDirectory().getAbsolutePath() + str);
                                    qFile2.fillExtraInfo();
                                }
                                if (Nh == null) {
                                    if (aVar2222.mFileName.equals("")) {
                                    }
                                    if (aVar2222.Np.equals("")) {
                                    }
                                    if (aVar2222.Nq.equals("")) {
                                    }
                                    if (aVar2222.Nr.equals("")) {
                                    }
                                    if (aVar2222.Ns.equals("")) {
                                        if (Nh.isMatchTime(qFile2.accessTime, aVar2222.Ns)) {
                                        }
                                    }
                                }
                            }
                            if (aVar != null) {
                                if (aVar.Nl.equals("3")) {
                                    if (aVar2222.Nl.equals("4")) {
                                    }
                                }
                                if (aVar2222.Nl.equals("3")) {
                                    if (a(aVar) >= a(aVar2222)) {
                                        aVar2222 = aVar;
                                    }
                                }
                            }
                            aVar = aVar2222;
                        }
                        if (aVar != null) {
                            return aVar;
                        }
                    }
                }
                return null;
            }
            return null;
        }
        treeMap2 = treeMap;
        if (treeMap2 == null) {
            if (obj == null) {
                this.Nj = treeMap2;
            }
            qFile2 = qFile;
            for (intValue = ((Integer) treeMap2.lastKey()).intValue(); intValue > 0; intValue--) {
                list2 = (List) treeMap2.get(Integer.valueOf(intValue));
                if (list2 == null) {
                    aVar = null;
                    for (a aVar22222 : list2) {
                        if (aVar22222.Nl.equals("4")) {
                            if (qFile2 == null) {
                                qFile2 = new QFile(Environment.getExternalStorageDirectory().getAbsolutePath() + str);
                                qFile2.fillExtraInfo();
                            }
                            if (Nh == null) {
                                if (aVar22222.mFileName.equals("")) {
                                }
                                if (aVar22222.Np.equals("")) {
                                }
                                if (aVar22222.Nq.equals("")) {
                                }
                                if (aVar22222.Nr.equals("")) {
                                }
                                if (aVar22222.Ns.equals("")) {
                                    if (Nh.isMatchTime(qFile2.accessTime, aVar22222.Ns)) {
                                    }
                                }
                            }
                        }
                        if (aVar != null) {
                            if (aVar.Nl.equals("3")) {
                                if (aVar22222.Nl.equals("4")) {
                                }
                            }
                            if (aVar22222.Nl.equals("3")) {
                                if (a(aVar) >= a(aVar22222)) {
                                    aVar22222 = aVar;
                                }
                            }
                        }
                        aVar = aVar22222;
                    }
                    if (aVar != null) {
                        return aVar;
                    }
                }
            }
            return null;
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean a(String[] strArr, String[] strArr2) {
        if (strArr == null || strArr2 == null || strArr.length >= strArr2.length) {
            return false;
        }
        int i = 0;
        while (i < strArr.length) {
            if (!strArr[i].equals("*") && !strArr[i].equals(strArr2[i])) {
                return false;
            }
            i++;
        }
        return true;
    }

    private b b(String str, String[] strArr) {
        if (str == null || strArr == null) {
            return null;
        }
        b c;
        if (this.Ng == null || this.Ng.om == null || !str.startsWith(this.Ng.om)) {
            long currentTimeMillis = System.currentTimeMillis();
            c = c(str, strArr);
            this.Nf = (System.currentTimeMillis() - currentTimeMillis) + this.Nf;
            this.Ng = c;
        } else {
            c = this.Ng;
        }
        return c;
    }

    private b c(String str, String[] strArr) {
        if (str == null || strArr == null) {
            return null;
        }
        String str2 = "";
        for (String str3 : strArr) {
            String str32;
            if (!(str32 == null || str32.equals(""))) {
                str32 = str2 + "/" + str32;
                b bVar = (b) this.Nk.get(str32);
                if (bVar != null) {
                    return bVar;
                }
                str2 = str32;
            }
        }
        return null;
    }

    private String h(String str, boolean z) {
        String[] j = j(str, z);
        return j != null ? j[1] : null;
    }

    private String i(String str, boolean z) {
        String[] j = j(str, z);
        return j != null ? j[0] : null;
    }

    private String[] j(String str, boolean z) {
        return k(str, z);
    }

    private void jA() {
        ArrayList aY = gs.aW().aY();
        if (aY != null) {
            Iterator it = aY.iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                b bVar = new b();
                bVar.om = str;
                this.Nk.put(str, bVar);
            }
        }
    }

    private String[] k(String str, boolean z) {
        List<tmsdkobf.gw.a> c = gs.aW().c(str, z);
        if (c == null || c.size() == 0) {
            return null;
        }
        String str2;
        List arrayList = new ArrayList();
        List arrayList2 = new ArrayList();
        for (tmsdkobf.gw.a aVar : c) {
            try {
                arrayList.add(new String(TccCryptor.decrypt(aVar.pC, null)));
                arrayList2.add(new String(TccCryptor.decrypt(aVar.pD, null)));
            } catch (Exception e) {
            }
        }
        gp gpVar = new gp();
        gpVar.init();
        String c2 = gpVar.c(arrayList);
        if (c2 != null) {
            py b = TMServiceFactory.getSystemInfoService().b(c2, 2048);
            if (b == null) {
                c2 = null;
                str2 = null;
            } else {
                str2 = b.getAppName();
                if (str2 == null) {
                    str2 = gpVar.aM(c2);
                }
            }
        } else {
            int d = gpVar.d(arrayList);
            int i = d != -1 ? d : 0;
            c2 = (String) arrayList.get(i);
            str2 = (String) arrayList2.get(i);
        }
        if (str2 == null || c2 == null) {
            return new String[]{null, null};
        }
        d.c("xx", c2 + "  " + str2);
        return new String[]{c2.trim(), str2.trim()};
    }

    private List<a> l(String str, boolean z) {
        if (str == null) {
            return null;
        }
        try {
            fq fqVar;
            byte[] aE = gs.aW().aE(str);
            if (aE == null) {
                aE = null;
            } else {
                fqVar = new fq(aE);
                fqVar.ae("UTF-8");
                ah ahVar = new ah();
                ahVar.readFrom(fqVar);
                aE = ahVar.aW;
            }
            if (aE != null) {
                fqVar = new fq(TccCryptor.decrypt(aE, null));
                fqVar.ae("UTF-8");
                ag agVar = new ag();
                agVar.readFrom(fqVar);
                List<a> arrayList = new ArrayList();
                Iterator it = agVar.aT.iterator();
                while (it.hasNext()) {
                    Map map = (Map) it.next();
                    a aVar;
                    if (map.get(Integer.valueOf(3)) != null) {
                        aVar = new a();
                        aVar.Nl = "3";
                        aVar.No = (String) map.get(Integer.valueOf(9));
                        aVar.mPath = ((String) map.get(Integer.valueOf(3))).toLowerCase();
                        aVar.Nm = str;
                        if (aVar.mPath.equals("/")) {
                            aVar.mPath = str;
                        } else {
                            aVar.mPath = str + aVar.mPath.toLowerCase();
                        }
                        aVar.Nn = !z ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18));
                        if (aVar.Nn == null) {
                            aVar.Nn = (String) map.get(Integer.valueOf(8));
                        }
                        aVar.Nt = (String) map.get(Integer.valueOf(10));
                        arrayList.add(aVar);
                    } else if (map.get(Integer.valueOf(4)) != null) {
                        aVar = new a();
                        aVar.Nl = "4";
                        aVar.No = (String) map.get(Integer.valueOf(9));
                        aVar.mPath = ((String) map.get(Integer.valueOf(4))).toLowerCase();
                        aVar.Nm = str;
                        if (aVar.mPath.equals("/")) {
                            aVar.mPath = str;
                        } else {
                            aVar.mPath = str + aVar.mPath.toLowerCase();
                        }
                        aVar.Nn = !z ? (String) map.get(Integer.valueOf(8)) : (String) map.get(Integer.valueOf(18));
                        if (aVar.Nn == null) {
                            aVar.Nn = (String) map.get(Integer.valueOf(8));
                        }
                        aVar.mFileName = (String) map.get(Integer.valueOf(11));
                        aVar.Np = (String) map.get(Integer.valueOf(12));
                        aVar.Nq = (String) map.get(Integer.valueOf(13));
                        aVar.Nr = (String) map.get(Integer.valueOf(14));
                        aVar.Ns = (String) map.get(Integer.valueOf(15));
                        arrayList.add(aVar);
                    }
                }
                return arrayList;
            }
            d.c("xx", "null:" + str);
            return null;
        } catch (Exception e) {
            d.c("xx", e.getMessage());
            return null;
        }
    }

    public String a(String str, QFile qFile, boolean z) {
        if (str == null) {
            return null;
        }
        String[] split = str.split("/");
        b b = b(str, split);
        if (b == null) {
            return null;
        }
        if (this.MZ) {
            if (b.Nw == null || b.Nw.size() == 0) {
                b.Nw = l(b.om, z);
            }
            if (b.mAppName == null) {
                b.mAppName = h(b.om, z);
            }
            if (b.mPkg == null) {
                b.mPkg = i(b.om, z);
            }
        }
        if (b.Nw == null || b.Nw.size() == 0) {
            return b.mAppName;
        }
        b.jB();
        long currentTimeMillis = System.currentTimeMillis();
        a a = a(b, str, split, qFile);
        this.Ne = (System.currentTimeMillis() - currentTimeMillis) + this.Ne;
        return a != null ? b.mAppName + " " + a.Nn : b.mAppName;
    }

    public String f(String str, boolean z) {
        if (str == null) {
            return null;
        }
        b b = b(str, str.split("/"));
        if (b == null) {
            return null;
        }
        if (b.mPkg == null) {
            String[] j = j(b.om, z);
            if (j != null) {
                b.mPkg = j[0];
                b.mAppName = j[1];
            }
        }
        return b.mPkg;
    }

    public String g(String str, boolean z) {
        if (str == null) {
            return null;
        }
        b b = b(str, str.split("/"));
        if (b == null) {
            return null;
        }
        if (b.mAppName == null) {
            String[] j = j(b.om, z);
            if (j != null) {
                b.mPkg = j[0];
                b.mAppName = j[1];
            }
        }
        return b.mAppName;
    }

    public void jz() {
        boolean isENG = ((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG();
        this.MZ = new ge().aG();
        if (this.MZ) {
            jA();
        } else {
            Q(isENG);
        }
    }
}
