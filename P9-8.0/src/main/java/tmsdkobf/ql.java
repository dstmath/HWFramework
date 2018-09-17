package tmsdkobf;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import tmsdk.fg.module.cleanV2.RubbishEntity;
import tmsdk.fg.module.cleanV2.RubbishHolder;
import tmsdkobf.qi.b;

public class ql implements Comparable<ql> {
    private List<ql> NA;
    private ql NB;
    private ql NC;
    private Set<qj> ND;
    private int NE;
    private int[] NF;
    private String NG;
    private boolean[] NH;
    private ExecutorService NI;
    private qj NJ;
    private tmsdkobf.qi.a NK;
    private List<File> Nn;
    private String Ny;
    private List<qj> Nz;
    private String mAppName;
    private int mLevel;
    private String mPackageName;

    private class a implements Runnable {
        private AtomicBoolean MK;
        private File NL;

        a(File file, AtomicBoolean atomicBoolean) {
            this.NL = file;
            this.MK = atomicBoolean;
        }

        public void run() {
            ql.this.a(this.NL, this.MK);
        }
    }

    public ql(String str, String str2, String str3, boolean z) {
        this.mLevel = 0;
        this.Nz = new LinkedList();
        this.NA = new LinkedList();
        this.NC = null;
        this.Nn = new LinkedList();
        this.ND = null;
        this.NE = 0;
        this.NF = null;
        this.mAppName = null;
        this.mPackageName = null;
        this.NH = null;
        this.NI = null;
        this.NJ = null;
        this.Ny = "/";
        this.ND = new LinkedHashSet();
        this.NF = new int[]{1};
        this.NG = str3;
        this.mAppName = str2;
        this.mPackageName = str;
        this.NH = new boolean[]{z};
    }

    private ql(String str, ql qlVar) {
        this.mLevel = 0;
        this.Nz = new LinkedList();
        this.NA = new LinkedList();
        this.NC = null;
        this.Nn = new LinkedList();
        this.ND = null;
        this.NE = 0;
        this.NF = null;
        this.mAppName = null;
        this.mPackageName = null;
        this.NH = null;
        this.NI = null;
        this.NJ = null;
        this.Ny = str;
        this.mLevel = qlVar.mLevel + 1;
        this.NC = qlVar;
        this.ND = qlVar.ND;
        this.NG = qlVar.NG;
        this.NF = qlVar.NF;
        int[] iArr = this.NF;
        iArr[0] = iArr[0] + 1;
        this.NI = qlVar.NI;
        this.mAppName = qlVar.mAppName;
        this.mPackageName = qlVar.mPackageName;
        this.NH = qlVar.NH;
    }

    private static boolean D(String str, String str2) {
        char charAt = str.charAt(0);
        if (charAt != '*') {
            return charAt != '/' ? str.equalsIgnoreCase(str2) : qk.cT(str.substring(1)).matcher(str2).find();
        } else {
            return true;
        }
    }

    static List<RubbishEntity> a(qj qjVar, boolean z, String str, String str2) {
        List<RubbishEntity> linkedList = new LinkedList();
        int i = !z ? 4 : 0;
        boolean z2 = !qjVar.V(z);
        String str3 = qjVar.Nv;
        List list = null;
        if (str3 != null) {
            list = rg.dh(str3);
        }
        RubbishEntity rubbishEntity = new RubbishEntity(i, qjVar.Nn, z2, qjVar.No, str2, str, qjVar.mDescription);
        rubbishEntity.setExtendData(qjVar.Nu, qjVar.Ne, list);
        linkedList.add(rubbishEntity);
        if (qjVar.Nl == 3 && qjVar.Nq.size() > 0) {
            RubbishEntity rubbishEntity2 = new RubbishEntity(i, qjVar.Nq, true, qjVar.Nr, str2, str, qjVar.Np);
            rubbishEntity2.setExtendData(qjVar.Nu, qjVar.Ne, list);
            linkedList.add(rubbishEntity2);
        }
        return linkedList;
    }

    private void a(File file, AtomicBoolean atomicBoolean) {
        if (!atomicBoolean.get()) {
            if (file.isDirectory()) {
                File[] listFiles = file.listFiles();
                if (listFiles != null) {
                    File[] fileArr = listFiles;
                    for (File a : listFiles) {
                        a(a, atomicBoolean);
                    }
                }
            } else {
                i(file);
            }
        }
    }

    private void a(List<String> list, qj qjVar) {
        int i = this.mLevel;
        if (i < list.size()) {
            String str = (String) list.get(i);
            if ("*".equalsIgnoreCase(str)) {
                if (this.NB == null) {
                    this.NB = new ql(str, this);
                }
                this.NB.a((List) list, qjVar);
                return;
            }
            for (ql qlVar : this.NA) {
                if (str.equalsIgnoreCase(qlVar.Ny)) {
                    qlVar.a((List) list, qjVar);
                    return;
                }
            }
            ql qlVar2 = new ql(str, this);
            this.NA.add(qlVar2);
            qlVar2.a((List) list, qjVar);
            return;
        }
        this.Nz.add(qjVar);
    }

    private static List<String> cU(String str) {
        List<String> arrayList = new ArrayList();
        int i = 1;
        while (true) {
            int indexOf = str.indexOf("/", i + 1);
            if (-1 != indexOf) {
                arrayList.add(str.substring(i, indexOf));
                i = indexOf + 1;
            } else {
                arrayList.add(str.substring(i));
                return arrayList;
            }
        }
    }

    private void i(File file) {
        for (qj qjVar : this.Nz) {
            if (qjVar.h(file)) {
                if (!this.NH[0] || (3 != qjVar.Nt && 4 != qjVar.Nt)) {
                    this.ND.add(qjVar);
                    this.NK.a(file, qjVar);
                    this.NJ = qjVar;
                    return;
                }
                return;
            }
        }
        if (this.NC != null) {
            this.NC.i(file);
        }
    }

    /* renamed from: a */
    public int compareTo(ql qlVar) {
        return this.NG.compareTo(qlVar.NG);
    }

    public void a(File file, ExecutorService executorService) {
        this.Nn.add(file);
        this.NI = executorService;
    }

    public void a(String str, b bVar, tmsdkobf.qi.a aVar, AtomicBoolean atomicBoolean) {
        this.NK = aVar;
        List<ql> arrayList = new ArrayList(this.NA);
        if (!(this.NC == null || this.NC.NB == null)) {
            arrayList.addAll(this.NC.NB.NA);
        }
        for (File listFiles : this.Nn) {
            File[] listFiles2 = listFiles.listFiles();
            if (listFiles2 != null && !atomicBoolean.get()) {
                File[] fileArr = listFiles2;
                for (File file : listFiles2) {
                    if (file.isDirectory()) {
                        String name = file.getName();
                        if (!atomicBoolean.get()) {
                            aVar.a(file, null);
                            Object obj = null;
                            for (ql qlVar : arrayList) {
                                if (D(qlVar.Ny, name)) {
                                    qlVar.Nn.add(file);
                                    obj = 1;
                                    break;
                                }
                            }
                            if (obj == null) {
                                if (this.NB != null) {
                                    this.NB.Nn.add(file);
                                } else {
                                    this.NI.execute(new a(file, atomicBoolean));
                                }
                            }
                        } else {
                            return;
                        }
                    }
                    i(file);
                }
            } else {
                return;
            }
        }
        for (ql a : this.NA) {
            a.a(str + "\t", bVar, aVar, atomicBoolean);
        }
        if (this.NB != null) {
            this.NB.a(str + "\t", bVar, aVar, atomicBoolean);
        }
    }

    public void a(RubbishHolder rubbishHolder) {
        for (qj a : this.ND) {
            List a2 = a(a, this.NH[0], this.mPackageName, this.mAppName);
            rubbishHolder.addRubbish((RubbishEntity) a2.get(0));
            if (a2.size() > 1) {
                rubbishHolder.addRubbish((RubbishEntity) a2.get(1));
            }
        }
    }

    public void b(qj qjVar) {
        String str = qjVar.Nd;
        this.NE++;
        if ("/".equalsIgnoreCase(str)) {
            this.Nz.add(qjVar);
        } else {
            a(cU(str), qjVar);
        }
    }

    public boolean jr() {
        return this.NH[0];
    }

    public boolean js() {
        for (qj qjVar : this.Nz) {
            if (qjVar.Nf == 0) {
                return true;
            }
        }
        return false;
    }

    public void jt() {
        Collections.sort(this.Nz);
        for (ql jt : this.NA) {
            jt.jt();
        }
    }
}
