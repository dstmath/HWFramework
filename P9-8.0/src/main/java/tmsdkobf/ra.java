package tmsdkobf;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tmsdk.common.TMSDKContext;
import tmsdk.common.tcc.DeepCleanEngine.Callback;
import tmsdk.common.tcc.QFile;
import tmsdk.common.utils.f;
import tmsdk.common.utils.m;
import tmsdk.fg.module.cleanV2.IScanTaskCallBack;
import tmsdk.fg.module.cleanV2.RubbishEntity;
import tmsdk.fg.module.cleanV2.RubbishHolder;

public class ra implements Callback {
    List<String> ME;
    private RubbishHolder Mt;
    private final boolean NZ;
    Map<String, ov> OK;
    Map<String, List<Integer>> OL;
    private qq OM;
    IScanTaskCallBack ON;
    qv OO;
    a OP;
    String OQ;
    private int OR = 0;
    private int OS = 0;
    private final long OT;
    qu OU = null;
    long OV = 0;
    long OW = 0;
    StringBuffer OX = new StringBuffer();
    private final boolean Oa;

    public class a {
        public String NQ;
        public boolean OY;
        public String nf;
    }

    public ra(boolean z, boolean z2, IScanTaskCallBack iScanTaskCallBack) {
        this.NZ = z;
        this.Oa = z2;
        this.OT = System.currentTimeMillis();
        this.OK = jY();
        this.ON = iScanTaskCallBack;
        this.OL = new HashMap();
        this.Mt = new RubbishHolder();
        qq qrVar = !new qn().jv() ? new qr(this) : !qo.jz().jC() ? new qr(this) : new qp(this);
        this.OM = qrVar;
        this.ME = rh.jZ();
    }

    private a a(boolean z, qv qvVar) {
        a aVar = new a();
        if (qvVar == null) {
            return null;
        }
        Map map = qvVar.Oz;
        if (map == null || map.size() < 1) {
            return null;
        }
        aVar.nf = this.OQ;
        ov ovVar = (ov) this.OK.get(this.OQ);
        if (ovVar != null) {
            aVar.nf = ovVar.getPackageName();
            if (ovVar.getAppName() == null) {
                try {
                    PackageManager packageManager = TMSDKContext.getApplicaionContext().getPackageManager();
                    ovVar.setAppName(packageManager.getApplicationLabel(packageManager.getApplicationInfo(ovVar.getPackageName(), 0)).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            aVar.NQ = ovVar.getAppName();
            aVar.OY = false;
        } else {
            aVar.NQ = (String) map.get(this.OQ);
            ov ovVar2 = null;
            for (String str : map.keySet()) {
                ovVar2 = (ov) this.OK.get(str);
                if (ovVar2 != null) {
                    break;
                }
            }
            aVar.OY = ovVar2 == null;
        }
        return aVar;
    }

    /* JADX WARNING: Missing block: B:2:0x0005, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(PackageInfo packageInfo, ov ovVar, int i, PackageManager packageManager) {
        boolean z = true;
        int i2 = -1;
        if (packageInfo != null && ovVar != null && packageInfo.applicationInfo != null) {
            if ((i & 1) != 0) {
                ovVar.put("pkgName", packageInfo.applicationInfo.packageName);
                ovVar.put("isSystem", Boolean.valueOf((packageInfo.applicationInfo.flags & 1) != 0));
                ovVar.put("uid", Integer.valueOf(packageInfo.applicationInfo == null ? -1 : packageInfo.applicationInfo.uid));
            }
            if ((i & 2) != 0) {
                ovVar.put("pkgName", packageInfo.applicationInfo.packageName);
                String str = "isSystem";
                if ((packageInfo.applicationInfo.flags & 1) == 0) {
                    z = false;
                }
                ovVar.put(str, Boolean.valueOf(z));
                String str2 = "uid";
                if (packageInfo.applicationInfo != null) {
                    i2 = packageInfo.applicationInfo.uid;
                }
                ovVar.put(str2, Integer.valueOf(i2));
            }
            if ((i & 8) != 0) {
                ovVar.put("version", packageInfo.versionName);
                ovVar.put("versionCode", Integer.valueOf(packageInfo.versionCode));
            }
            if ((i & 64) != 0) {
                ovVar.put("apkPath", packageInfo.applicationInfo.sourceDir);
                ovVar.put("isApk", Boolean.valueOf(false));
            }
        }
    }

    private a b(boolean z, qv qvVar) {
        ov ovVar = null;
        a aVar = new a();
        if (qvVar == null) {
            return null;
        }
        Map map = qvVar.Oz;
        if (map == null || map.size() < 1) {
            return null;
        }
        for (String str : map.keySet()) {
            ovVar = (ov) this.OK.get(str);
            if (ovVar != null) {
                break;
            }
        }
        if (ovVar != null) {
            aVar.nf = ovVar.getPackageName();
            if (ovVar.getAppName() == null) {
                try {
                    PackageManager packageManager = TMSDKContext.getApplicaionContext().getPackageManager();
                    ovVar.setAppName(packageManager.getApplicationLabel(packageManager.getApplicationInfo(ovVar.getPackageName(), 0)).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            aVar.NQ = ovVar.getAppName();
            aVar.OY = false;
        } else {
            long j = 0;
            String str2 = null;
            Map jB = qo.jz().jB();
            if (jB.size() > 0) {
                for (String str3 : map.keySet()) {
                    if (jB.get(str3) != null) {
                        long longValue = ((Long) jB.get(str3)).longValue();
                        if ((longValue <= j ? 1 : null) == null) {
                            j = longValue;
                            str2 = str3;
                        }
                    }
                }
                aVar.NQ = (String) map.get(str2);
            }
            if (str2 == null) {
                str2 = (String) map.keySet().toArray()[0];
                aVar.NQ = !z ? "疑似" + ((String) map.get(str2)) : (String) map.get(str2);
            }
            aVar.nf = str2;
            aVar.OY = true;
        }
        return aVar;
    }

    private int bF(int i) {
        int i2 = 0;
        if ((i & 16) != 0) {
            i2 = 64;
        }
        return (i & 32) == 0 ? i2 : i2 | 4096;
    }

    private boolean de(String str) {
        List<String> jZ = jZ();
        String[] jE = this.OM.jE();
        if (jE == null) {
            return false;
        }
        for (String str2 : jZ) {
            if (str.toLowerCase().contains(str2.toLowerCase())) {
                String substring = str.toLowerCase().substring(str2.length());
                String[] strArr = jE;
                for (String toLowerCase : jE) {
                    if (toLowerCase.toLowerCase().startsWith(substring)) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    private ArrayList<ov> x(int i, int i2) {
        List list = null;
        PackageManager packageManager = TMSDKContext.getApplicaionContext().getPackageManager();
        try {
            list = packageManager.getInstalledPackages(bF(i));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<ov> arrayList = new ArrayList();
        if (list != null) {
            for (PackageInfo packageInfo : list) {
                Object obj;
                if ((packageInfo.applicationInfo.flags & 1) == 0) {
                    obj = null;
                } else {
                    int obj2 = 1;
                }
                if (obj2 != null || i2 != 1) {
                    if (obj2 == null || i2 != 0) {
                        ov ovVar = new ov();
                        a(packageInfo, ovVar, i, packageManager);
                        arrayList.add(ovVar);
                    }
                }
            }
        }
        return arrayList;
    }

    public boolean a(Set<String> set) {
        if (set != null) {
            for (String cY : set) {
                this.OM.cY(cY);
            }
        }
        String[] jE = this.OM.jE();
        if (jE != null) {
            String[] strArr = jE;
            for (String str : jE) {
                f.e("ZhongSi", "setWhitePath: " + str);
            }
        }
        return true;
    }

    public void dd(String str) {
        this.OQ = str;
        Map cX = this.OM.cX(str);
        if (!(this.OM instanceof qr)) {
            if (cX == null || cX.size() == 0) {
                this.OM = new qr(this);
            } else {
                qq qrVar = new qr(this);
                Map cX2 = qrVar.cX(str);
                if (cX2 != null && cX2.size() > cX.size()) {
                    this.OM = qrVar;
                }
            }
        }
    }

    public String getDetailRule(String str) {
        if (this.OM == null) {
            return null;
        }
        String str2 = null;
        try {
            this.OO = this.OM.cZ(str.toLowerCase());
            if (this.OO == null) {
                return null;
            }
            if (this.OQ == null) {
                this.OP = b(this.NZ, this.OO);
                if (this.OP == null) {
                    return null;
                }
            }
            this.OP = a(this.NZ, this.OO);
            if (this.OP == null) {
                return null;
            }
            str2 = this.OM.a(this.OO, this.OK);
            return str2;
        } catch (Exception e) {
            e.printStackTrace();
            this.OO = null;
            this.OP = null;
        }
    }

    public boolean jD() {
        return this.OM == null ? false : this.OM.jD();
    }

    public boolean jR() {
        return this.Oa;
    }

    public boolean jS() {
        return this.NZ;
    }

    protected IScanTaskCallBack jT() {
        return this.ON;
    }

    protected String jU() {
        return this.OQ;
    }

    protected qq jV() {
        return this.OM;
    }

    public void jW() {
        if (this.ON != null) {
            this.ON.onScanCanceled(this.Mt);
        }
    }

    public void jX() {
        if (this.ON != null) {
            this.ON.onScanFinished(this.Mt);
            this.ON = null;
        }
    }

    protected Map<String, ov> jY() {
        ArrayList x = x(73, 2);
        Map<String, ov> hashMap = new HashMap();
        Iterator it = x.iterator();
        while (it.hasNext()) {
            ov ovVar = (ov) it.next();
            hashMap.put(ovVar.getPackageName(), ovVar);
        }
        return hashMap;
    }

    public List<String> jZ() {
        if (this.ME == null || this.ME.size() < 1) {
            this.ME = rh.jZ();
        }
        return this.ME;
    }

    public void onDirectoryChange(String str, int i) {
        if (i == 0) {
            this.OR = this.OS;
        }
        this.OS = this.OR + i;
        if (this.ON != null) {
            this.ON.onDirectoryChange(str, this.OS);
        }
    }

    public void onFoundComRubbish(String str, String -l_8_R, long j) {
        qt da = this.OM.da(str);
        if (da != null) {
            RubbishEntity rubbishEntity;
            qy qyVar = new qy();
            if (da != this.OM.jJ()) {
                rubbishEntity = new RubbishEntity(1, -l_8_R, !da.Or, j, null, null, da.mDescription);
            } else {
                rubbishEntity = qyVar.a(this, this.Oa, -l_8_R, j, this.OL, this.OK, this.OM.jI());
                if (rubbishEntity == null) {
                    return;
                }
                if (rubbishEntity.getPackageName() != null) {
                    if (this.OL.get(rubbishEntity.getPackageName()) == null) {
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(Integer.valueOf(rubbishEntity.getVersionCode()));
                        this.OL.put(rubbishEntity.getPackageName(), arrayList);
                    } else {
                        ((List) this.OL.get(rubbishEntity.getPackageName())).add(Integer.valueOf(rubbishEntity.getVersionCode()));
                    }
                }
            }
            if (rubbishEntity != null) {
                this.Mt.addRubbish(rubbishEntity);
                if (this.ON != null) {
                    this.ON.onRubbishFound(rubbishEntity);
                }
            }
        }
    }

    public void onFoundEmptyDir(String -l_5_R, long j) {
        if (!de(-l_5_R)) {
            RubbishEntity rubbishEntity = new RubbishEntity(1, -l_5_R, true, j, null, null, m.cF("scan_item_empty_folders"));
            if (rubbishEntity != null) {
                this.Mt.addRubbish(rubbishEntity);
                if (this.ON != null) {
                    this.ON.onRubbishFound(rubbishEntity);
                }
            }
        }
    }

    public void onFoundKeySoftRubbish(String str, String[] strArr, long j) {
        if (this.OO != null && this.OP != null && strArr != null) {
            try {
                int parseInt = Integer.parseInt(str);
                long j2 = j / 1000;
                List asList = Arrays.asList(strArr);
                qu bY = this.OU != null ? this.OU.mID != parseInt ? this.OO.bY(parseInt) : this.OU : this.OO.bY(parseInt);
                if (bY != null) {
                    RubbishEntity rubbishEntity;
                    this.OU = bY;
                    if (this.OP.OY) {
                        rubbishEntity = new RubbishEntity(4, asList, (bY.Nt != 3 ? null : 1) == null, j2, this.OP.NQ, this.OP.nf, bY.mDescription);
                    } else {
                        Object obj;
                        if (bY.Nt == 1) {
                            obj = null;
                        } else if (bY.Nt == 2) {
                            obj = 1;
                        } else {
                            return;
                        }
                        rubbishEntity = new RubbishEntity(0, asList, obj == null, j2, this.OP.NQ, this.OP.nf, bY.mDescription);
                    }
                    if (rubbishEntity != null) {
                        rubbishEntity.setExtendData(bY.Nu, bY.Ne, bY.Nw);
                        if (this.ON != null) {
                            this.ON.onRubbishFound(rubbishEntity);
                        }
                        this.Mt.addRubbish(rubbishEntity);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    public void onFoundSoftRubbish(String str, String -l_11_R, String str2, long j) {
        if (this.OO != null && this.OP != null) {
            try {
                int parseInt = Integer.parseInt(str);
                long j2 = j / 1000;
                if (str2 != null) {
                    -l_11_R = -l_11_R + str2;
                }
                qu bY = this.OU != null ? this.OU.mID != parseInt ? this.OO.bY(parseInt) : this.OU : this.OO.bY(parseInt);
                if (bY != null) {
                    RubbishEntity rubbishEntity;
                    this.OU = bY;
                    if (this.OP.OY) {
                        rubbishEntity = new RubbishEntity(4, -l_11_R, (bY.Nt != 3 ? null : 1) == null, j2, this.OP.NQ, this.OP.nf, bY.mDescription);
                    } else {
                        Object obj;
                        if (bY.Nt == 1) {
                            obj = null;
                        } else if (bY.Nt == 2) {
                            obj = 1;
                        } else {
                            return;
                        }
                        rubbishEntity = new RubbishEntity(0, -l_11_R, obj == null, j2, this.OP.NQ, this.OP.nf, bY.mDescription);
                    }
                    if (rubbishEntity != null) {
                        rubbishEntity.setExtendData(bY.Nu, bY.Ne, bY.Nw);
                        this.Mt.addRubbish(rubbishEntity);
                        if (this.ON != null) {
                            this.ON.onRubbishFound(rubbishEntity);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    public void onProcessChange(int i) {
    }

    public void onScanError(int i) {
        if (this.ON != null) {
            this.ON.onScanError(i, this.Mt);
        }
    }

    public void onScanStarted() {
        if (this.ON != null) {
            this.ON.onScanStarted();
        }
    }

    public void onVisit(QFile qFile) {
    }

    public void release() {
        this.ON = null;
        this.OM = null;
        this.Mt = null;
        this.OK = null;
        this.OL = null;
        this.OO = null;
        this.OP = null;
        this.OQ = null;
        this.OU = null;
        this.ME = null;
    }
}
