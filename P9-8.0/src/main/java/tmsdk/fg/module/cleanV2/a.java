package tmsdk.fg.module.cleanV2;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build.VERSION;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.lang.MultiLangManager;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.tcc.QFile;
import tmsdk.common.utils.f;
import tmsdk.common.utils.m;
import tmsdk.common.utils.s;
import tmsdk.fg.creator.BaseManagerF;
import tmsdkobf.al;
import tmsdkobf.ic;
import tmsdkobf.im;
import tmsdkobf.jk;
import tmsdkobf.js;
import tmsdkobf.mk;
import tmsdkobf.qi;
import tmsdkobf.qn;
import tmsdkobf.qo;
import tmsdkobf.qw;
import tmsdkobf.qz;
import tmsdkobf.ra;
import tmsdkobf.rc;
import tmsdkobf.rd;
import tmsdkobf.re;
import tmsdkobf.rf;

class a extends BaseManagerF implements qz {
    rc Mk;
    rc Ml;
    rd Mm;
    HashMap<Integer, AppGroupDesc> Mn;
    private Object Mo = new Object();
    private Object Mp = new Object();
    private Object Mq = new Object();
    a Mr;
    qi Ms = null;

    class a {
        RubbishHolder Mt;
        ICleanTaskCallBack Mu;
        boolean Mv;
        int Mw;
        final boolean Mx;

        protected a(RubbishHolder rubbishHolder, ICleanTaskCallBack iCleanTaskCallBack, boolean z) {
            this.Mt = rubbishHolder;
            this.Mu = iCleanTaskCallBack;
            this.Mx = z;
        }

        private int a(ContentResolver contentResolver, File file) {
            int i = 0;
            if (file.getName().startsWith(".")) {
                if (file.isDirectory()) {
                    new QFile(file.getPath()).deleteAllChildren();
                }
                file.delete();
                return 1;
            }
            if (file.isDirectory()) {
                for (File a : file.listFiles()) {
                    i += a(contentResolver, a);
                }
                if (file.delete()) {
                    i++;
                }
            } else {
                i = b(contentResolver, file) + 0;
            }
            return i;
        }

        private void a(File file, int i) {
            if (file != null) {
                int i2 = i + 1;
                File[] listFiles = file.listFiles();
                if (listFiles != null) {
                    int length = listFiles.length;
                    for (int i3 = 0; i3 < length; i3++) {
                        if (listFiles[i3].isDirectory()) {
                            a(listFiles[i3], i2);
                        }
                        listFiles[i3].delete();
                    }
                    file.delete();
                    return;
                }
                file.delete();
            }
        }

        private void a(RubbishEntity -l_4_R, int i, boolean z) {
            try {
                List rubbishKey = -l_4_R.getRubbishKey();
                if (rubbishKey != null) {
                    int size = rubbishKey.size();
                    if (size > 0) {
                        String str = (String) rubbishKey.get(0);
                        if (z) {
                            x(rubbishKey);
                        } else {
                            w(rubbishKey);
                        }
                        this.Mw += size;
                        this.Mu.onCleanProcessChange((this.Mw * 100) / i, str);
                        -l_4_R.ju();
                    }
                }
            } catch (Exception e) {
            }
        }

        private int b(ContentResolver contentResolver, File file) {
            if (file == null) {
                return 0;
            }
            try {
                return (new rf(contentResolver, file).delete() || file.delete()) ? 1 : 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void b(RubbishEntity -l_4_R, int i, boolean z) {
            try {
                List<String> rubbishKey = -l_4_R.getRubbishKey();
                if (rubbishKey != null && rubbishKey.size() > 0) {
                    File file;
                    String str = (String) rubbishKey.get(0);
                    File file2 = null;
                    for (String str2 : rubbishKey) {
                        try {
                            if (str2 != null) {
                                file = new File(str2);
                                if (z) {
                                    file.delete();
                                } else if (VERSION.SDK_INT < 11) {
                                    file.delete();
                                } else {
                                    b(TMSDKContext.getApplicaionContext().getContentResolver(), file);
                                }
                                this.Mw++;
                                file2 = file;
                            }
                        } catch (Exception e) {
                            file = file2;
                        }
                    }
                    this.Mu.onCleanProcessChange((this.Mw * 100) / i, str);
                    -l_4_R.ju();
                    file = file2;
                }
            } catch (Exception e2) {
            }
        }

        private void w(List<String> list) {
            ContentResolver contentResolver = null;
            if (VERSION.SDK_INT < 11) {
                x(list);
                return;
            }
            if (null == null) {
                contentResolver = TMSDKContext.getApplicaionContext().getContentResolver();
            }
            int size = list.size();
            for (int i = 0; i < size; i++) {
                String str = (String) list.get(i);
                if (str != null) {
                    a(contentResolver, new File(str.trim()));
                }
            }
        }

        private void x(List<String> list) {
            for (String str : list) {
                if (str != null) {
                    File file = new File(str);
                    if (file.isDirectory()) {
                        a(file, 0);
                    } else {
                        file.delete();
                    }
                }
            }
        }

        protected void jm() {
            this.Mv = true;
        }

        protected boolean jn() {
            if (this.Mu == null) {
                return false;
            }
            im.bJ().addTask(new Runnable() {
                private int jo() {
                    int i = 0;
                    if (a.this.Mt.getmApkRubbishes() != null) {
                        for (RubbishEntity rubbishEntity : a.this.Mt.getmApkRubbishes()) {
                            if (rubbishEntity.getStatus() == 1) {
                                i += rubbishEntity.getRubbishKey().size();
                            }
                        }
                    }
                    if (a.this.Mt.getmSystemRubbishes() != null) {
                        for (Entry entry : a.this.Mt.getmSystemRubbishes().entrySet()) {
                            if (((RubbishEntity) entry.getValue()).getStatus() == 1) {
                                i += ((RubbishEntity) entry.getValue()).getRubbishKey().size();
                            }
                        }
                    }
                    if (a.this.Mt.getmInstallRubbishes() != null) {
                        for (Entry entry2 : a.this.Mt.getmInstallRubbishes().entrySet()) {
                            if (((RubbishEntity) entry2.getValue()).getStatus() == 1) {
                                i += ((RubbishEntity) entry2.getValue()).getRubbishKey().size();
                            }
                        }
                    }
                    if (a.this.Mt.getmUnInstallRubbishes() != null) {
                        for (Entry entry22 : a.this.Mt.getmUnInstallRubbishes().entrySet()) {
                            if (((RubbishEntity) entry22.getValue()).getStatus() == 1) {
                                i += ((RubbishEntity) entry22.getValue()).getRubbishKey().size();
                            }
                        }
                    }
                    return i;
                }

                public void run() {
                    a.this.Mw = 0;
                    a.this.Mu.onCleanStarted();
                    int jo = jo();
                    if (jo > 0) {
                        if (a.this.Mt.getmApkRubbishes() != null) {
                            for (RubbishEntity rubbishEntity : a.this.Mt.getmApkRubbishes()) {
                                if (a.this.Mv) {
                                    a.this.Mu.onCleanCanceled();
                                    a.this.release();
                                    return;
                                } else if (rubbishEntity.getStatus() == 1) {
                                    a.this.b(rubbishEntity, jo, a.this.Mx);
                                }
                            }
                        }
                        if (a.this.Mt.getmSystemRubbishes() != null) {
                            for (Entry entry : a.this.Mt.getmSystemRubbishes().entrySet()) {
                                if (a.this.Mv) {
                                    a.this.Mu.onCleanCanceled();
                                    a.this.release();
                                    return;
                                } else if (((RubbishEntity) entry.getValue()).getStatus() == 1) {
                                    a.this.a((RubbishEntity) entry.getValue(), jo, a.this.Mx);
                                }
                            }
                        }
                        if (a.this.Mt.getmInstallRubbishes() != null) {
                            for (Entry entry2 : a.this.Mt.getmInstallRubbishes().entrySet()) {
                                if (a.this.Mv) {
                                    a.this.Mu.onCleanCanceled();
                                    a.this.release();
                                    return;
                                } else if (((RubbishEntity) entry2.getValue()).getStatus() == 1) {
                                    a.this.a((RubbishEntity) entry2.getValue(), jo, a.this.Mx);
                                }
                            }
                        }
                        if (a.this.Mt.getmUnInstallRubbishes() != null) {
                            for (Entry entry22 : a.this.Mt.getmUnInstallRubbishes().entrySet()) {
                                if (a.this.Mv) {
                                    a.this.Mu.onCleanCanceled();
                                    a.this.release();
                                    return;
                                } else if (((RubbishEntity) entry22.getValue()).getStatus() == 1) {
                                    a.this.a((RubbishEntity) entry22.getValue(), jo, a.this.Mx);
                                }
                            }
                        }
                    }
                    a.this.Mu.onCleanFinished();
                    a.this.release();
                }
            }, null);
            return true;
        }

        protected void release() {
            this.Mt = null;
            this.Mu = null;
            a.this.Mr = null;
        }
    }

    protected a() {
    }

    private boolean jj() {
        boolean z = false;
        al alVar = (al) mk.a(TMSDKContext.getApplicaionContext(), UpdateConfig.DEEPCLEAN_SDCARD_SCAN_RULE_NAME_V2_SDK, UpdateConfig.intToString(40415), new al(), "UTF-8");
        if (alVar == null || alVar.bt == null) {
            return false;
        }
        if (qo.jz().y(alVar.bt).size() > 10) {
            z = true;
        }
        return z;
    }

    private void jk() {
        qn qnVar = new qn();
        if (!qnVar.jv()) {
            qnVar.W(jj());
        }
    }

    protected boolean SlowCleanRubbish(RubbishHolder rubbishHolder, ICleanTaskCallBack iCleanTaskCallBack) {
        if (ic.bE() || this.Mr != null || rubbishHolder == null) {
            return false;
        }
        this.Mr = new a(rubbishHolder, iCleanTaskCallBack, false);
        return this.Mr.jn();
    }

    public void a(IScanTaskCallBack iScanTaskCallBack, String str) {
        jk();
        if (iScanTaskCallBack == null) {
            f.e("ZhongSi", "aListener is null!!!");
        } else if (str != null) {
            boolean isENG = ((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG();
            m.T(isENG);
            if (this.Ms == null) {
                this.Ms = new qi();
            }
            this.Ms.a(iScanTaskCallBack, str, isENG);
        } else {
            f.e("ZhongSi", "packageName is null!!!");
            iScanTaskCallBack.onScanError(-1, null);
        }
    }

    protected boolean a(IUpdateCallBack iUpdateCallBack) {
        return !ic.bE() ? qw.jM().b(iUpdateCallBack) : false;
    }

    protected boolean addUninstallPkg(String str) {
        return !ic.bE() ? qo.jz().addUninstallPkg(str) : false;
    }

    public void bX(int i) {
        switch (i) {
            case 0:
                synchronized (this.Mq) {
                    this.Mk = null;
                }
            case 1:
                synchronized (this.Mo) {
                    this.Ml = null;
                }
            case 2:
                synchronized (this.Mp) {
                    this.Mm = null;
                }
            default:
                return;
        }
    }

    protected boolean cancelClean() {
        if (ic.bE() || this.Mr == null) {
            return false;
        }
        this.Mr.jm();
        this.Mr = null;
        return true;
    }

    protected boolean cancelScan(int i) {
        switch (i) {
            case 0:
                if (this.Mk != null) {
                    this.Mk.cancel();
                    break;
                }
                return false;
            case 1:
                if (this.Ml != null) {
                    this.Ml.cancel();
                    break;
                }
                return false;
            case 2:
                if (this.Mm != null) {
                    this.Mm.cancel();
                    break;
                }
                return false;
            default:
                return false;
        }
        return true;
    }

    protected boolean cleanRubbish(RubbishHolder rubbishHolder, ICleanTaskCallBack iCleanTaskCallBack) {
        if (ic.bE() || this.Mr != null || rubbishHolder == null) {
            return false;
        }
        this.Mr = new a(rubbishHolder, iCleanTaskCallBack, true);
        return this.Mr.jn();
    }

    protected boolean delUninstallPkg(String str) {
        return !ic.bE() ? qo.jz().delUninstallPkg(str) : false;
    }

    protected boolean easyScan(IScanTaskCallBack iScanTaskCallBack, Set<String> set) {
        if (ic.bE() || this.Ml != null) {
            return false;
        }
        boolean a;
        synchronized (this.Mo) {
            this.Ml = new rc(this);
            jk();
            boolean isENG = ((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG();
            m.T(isENG);
            ra raVar = new ra(isENG, true, iScanTaskCallBack);
            raVar.a(set);
            a = this.Ml.a(raVar);
        }
        return a;
    }

    protected AppGroupDesc getGroupInfo(int i) {
        if (this.Mn == null) {
            this.Mn = re.kd();
        }
        if (this.Mn == null) {
            return null;
        }
        return (AppGroupDesc) this.Mn.get(Integer.valueOf(i));
    }

    public int getSingletonType() {
        return 1;
    }

    public void jl() {
        if (this.Ms != null) {
            this.Ms.cancel();
        }
    }

    public void onCreate(Context context) {
        s.bW(16);
        qw.jM().de();
        jk.cv().a(js.cE());
    }

    public void onDestroy() {
        synchronized (this.Mq) {
            if (this.Mk != null) {
                this.Mk.cancel();
                this.Mk.release();
                this.Mk = null;
            }
        }
        synchronized (this.Mo) {
            if (this.Ml != null) {
                this.Ml.cancel();
                this.Ml.release();
                this.Ml = null;
            }
        }
        synchronized (this.Mp) {
            if (this.Mm != null) {
                this.Mm.cancel();
                this.Mm.release();
                this.Mm = null;
            }
        }
        this.Mn = null;
    }

    protected boolean scan4app(String str, IScanTaskCallBack iScanTaskCallBack) {
        if (ic.bE() || this.Mm != null) {
            return false;
        }
        boolean a;
        synchronized (this.Mp) {
            rd rdVar = new rd(this);
            jk();
            boolean isENG = ((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG();
            m.T(isENG);
            ra raVar = new ra(isENG, false, iScanTaskCallBack);
            raVar.dd(str);
            a = rdVar.a(raVar);
            this.Mm = rdVar;
        }
        return a;
    }

    protected boolean scanDisk(IScanTaskCallBack iScanTaskCallBack, Set<String> set) {
        if (ic.bE()) {
            f.e("ZhongSi", "scanDisk: isExpired");
            return false;
        } else if (this.Mk == null) {
            boolean a;
            if (set != null) {
                for (String str : set) {
                    String str2 = "ZhongSi";
                    f.e(str2, "scanDisk whitePath: " + str);
                }
            }
            synchronized (this.Mq) {
                rc rcVar = new rc(this);
                jk();
                boolean isENG = ((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG();
                m.T(isENG);
                ra raVar = new ra(isENG, false, iScanTaskCallBack);
                raVar.a(set);
                a = rcVar.a(raVar);
                this.Mk = rcVar;
            }
            return a;
        } else {
            f.e("ZhongSi", "scanDisk: null!=mScanTaskDisk");
            return false;
        }
    }
}
