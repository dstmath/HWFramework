package tmsdk.fg.module.deepclean;

import android.content.Context;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.lang.MultiLangManager;
import tmsdk.common.tcc.QFile;
import tmsdk.common.tcc.SdcardScannerFactory;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.b;
import tmsdk.common.utils.d;
import tmsdk.fg.creator.BaseManagerF;
import tmsdk.fg.module.deepclean.rubbish.SoftRubModel;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;
import tmsdkobf.ge;
import tmsdkobf.gg;
import tmsdkobf.gj;
import tmsdkobf.gk;
import tmsdkobf.gl;
import tmsdkobf.hb;
import tmsdkobf.hd;
import tmsdkobf.he;
import tmsdkobf.jq;

/* compiled from: Unknown */
final class a extends BaseManagerF {
    private int LA;
    private Set<String> LB;
    private DeepcleanManager LC;
    private String LD;
    private boolean LE;
    private hd Ly;
    private ScanProcessListener Lz;
    private final String TAG;

    a() {
        this.TAG = "DeepcleanManagerImpl";
        this.LA = 15;
        this.LE = false;
    }

    private static ArrayList<String> A(List<String> list) {
        if (list == null) {
            return null;
        }
        ArrayList<String> arrayList = new ArrayList();
        for (String bytes : list) {
            String bytes2 = b.encodeToString(TccCryptor.encrypt(bytes2.getBytes(), null), 0);
            if (bytes2 != null) {
                arrayList.add(bytes2);
            }
        }
        return arrayList;
    }

    private static ArrayList<String> B(List<String> list) {
        if (list == null) {
            return null;
        }
        ArrayList<String> arrayList = new ArrayList();
        for (String bytes : list) {
            String str = new String(TccCryptor.decrypt(b.decode(bytes.getBytes(), 0), null));
            if (str != null) {
                arrayList.add(str);
            }
        }
        return arrayList;
    }

    private void be() {
    }

    private static String ds(String str) {
        return new String(TccCryptor.decrypt(b.decode(str.getBytes(), 0), null));
    }

    private he jb() {
        return new he() {
            final /* synthetic */ a LF;

            {
                this.LF = r1;
            }

            public void a(int i, int i2, String str) {
                if (2 != this.LF.Ly.bh()) {
                    if (1 != i && 2 != i) {
                        return;
                    }
                    if (this.LF.Lz != null) {
                        this.LF.Lz.onScanProcessChange(i2, this.LF.LD);
                    }
                } else if (this.LF.Lz != null) {
                    this.LF.Lz.onScanProcessChange(99, this.LF.LD);
                }
            }

            public void a(int i, List<String> list, boolean z, long j, String str, String str2, String str3) {
                int i2;
                switch (i) {
                    case SpaceManager.ERROR_CODE_OK /*0*/:
                        i2 = 1;
                        break;
                    case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                        i2 = 4;
                        break;
                    case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                        i2 = 8;
                        break;
                    case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                        i2 = 2;
                        break;
                    default:
                        i2 = 0;
                        break;
                }
                if (list != null && list.size() > 0) {
                    this.LF.LD = (String) list.get(0);
                }
                if ((this.LF.LA == 0 || RubbishType.isOn(r1, this.LF.LA)) && this.LF.Lz != null) {
                    RubbishEntity rubbishEntity;
                    if (this.LF.LC.iZ()) {
                        rubbishEntity = new RubbishEntity(i, a.A(list), z, j, str, str2, str3);
                    } else {
                        rubbishEntity = new RubbishEntity(i, list, z, j, str, str2, str3);
                    }
                    this.LF.LC.getmRubbishEntityManager().addRubbish(rubbishEntity);
                    this.LF.Lz.onRubbishFound(rubbishEntity);
                }
            }

            public void aA(int i) {
                this.LF.be();
                this.LF.Lz.onScanStarted();
            }

            public void aC(int i) {
                if (this.LF.Lz != null) {
                    this.LF.Lz.onScanCanceled();
                }
            }

            public void bg() {
                if (this.LF.Lz != null) {
                    this.LF.Lz.onScanFinished();
                }
            }
        };
    }

    protected void a(DeepcleanManager deepcleanManager) {
        this.LC = deepcleanManager;
    }

    public void aF(String str) {
        gg.aI().aF(str);
    }

    public void appendWhitePath(String str) {
        if (this.LB == null) {
            this.LB = new HashSet();
        }
        this.LB.add(str.toLowerCase());
    }

    public boolean cF(int i) {
        if (this.Lz == null) {
            return false;
        }
        this.LC.getmRubbishEntityManager().resetRubbishes();
        this.LA = i;
        he jb = jb();
        this.Ly.bd();
        if (this.LB != null) {
            this.Ly.a((String[]) this.LB.toArray(new String[this.LB.size()]));
        }
        this.Ly.a(jb);
        this.Ly.t(true);
        return true;
    }

    public void cancelClean() {
        this.LE = true;
    }

    public void cancelScan() {
        if (this.Ly != null) {
            this.Ly.bf();
        }
    }

    public void cleanSoftRubModelRubbish(SoftRubModel softRubModel) {
        if (softRubModel != null) {
            if (this.LC.iZ()) {
                for (String ds : softRubModel.mRubbishFiles) {
                    new QFile(ds(ds)).deleteAllChildren();
                }
            } else {
                for (String ds2 : softRubModel.mRubbishFiles) {
                    new QFile(ds2).deleteAllChildren();
                }
            }
        }
    }

    public void cleanSoftRubModelRubbishInstall(SoftRubModel softRubModel) {
        for (RubbishEntity rubbishEntity : softRubModel.mRubbishFilesInstall) {
            if (1 == rubbishEntity.getStatus()) {
                List rubbishKey = rubbishEntity.getRubbishKey();
                if (this.LC.iZ()) {
                    rubbishKey = B(rubbishKey);
                }
                if (r1 != null) {
                    for (String qFile : r1) {
                        new QFile(qFile).deleteAllChildren();
                        rubbishEntity.setStatus(2);
                    }
                }
            }
        }
    }

    public int getSingletonType() {
        return 2;
    }

    public boolean init(ScanProcessListener scanProcessListener) {
        if (!SdcardScannerFactory.isLoadNativeOK) {
            return false;
        }
        this.Lz = scanProcessListener;
        this.Ly = new hd(-1);
        return true;
    }

    public void jc() {
        long j = 0;
        int i = 0;
        List rubbishes = this.LC.getmRubbishEntityManager().getRubbishes();
        long selectedRubbishSize = this.LC.getmRubbishEntityManager().getSelectedRubbishSize();
        if (0 != selectedRubbishSize) {
            Iterator it = rubbishes.iterator();
            while (true) {
                int i2 = i;
                if (!it.hasNext()) {
                    break;
                }
                RubbishEntity rubbishEntity = (RubbishEntity) it.next();
                if (this.LE) {
                    break;
                } else if (1 != rubbishEntity.getStatus()) {
                    i = i2;
                } else {
                    j += rubbishEntity.getSize();
                    List rubbishKey = rubbishEntity.getRubbishKey();
                    if (this.LC.iZ()) {
                        rubbishKey = B(rubbishKey);
                    }
                    if (r4 == null) {
                        i = i2;
                    } else {
                        for (String qFile : r4) {
                            new QFile(qFile).deleteAllChildren();
                            rubbishEntity.setStatus(2);
                            i2 = (int) ((100 * j) / selectedRubbishSize);
                        }
                        if (this.Lz == null) {
                            i = i2;
                        } else {
                            this.Lz.onCleanProcessChange(j, i2);
                            d.g("DeepcleanManagerImpl", "onCleanProcessChange-- [" + i2 + "]" + j);
                            i = i2;
                        }
                    }
                }
            }
            d.g("DeepcleanManagerImpl", "onCleanFinish-- [true]");
        }
    }

    public void onCreate(Context context) {
        this.LC.getmRubbishEntityManager().resetRubbishes();
        gj.aJ().aK();
        gl.freeSoftwareRubbishManagerInstall();
    }

    public void onDestory() {
        if (this.Ly != null) {
            cancelClean();
            this.Ly.onDestroy();
        }
    }

    public void preLoad() {
        this.Ly.preLoad();
    }

    public SoftRubModel scanSoftRubbish(String str) {
        if (str == null) {
            return null;
        }
        hb.s(((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG());
        gk gkVar = new gk();
        try {
            SoftRubModel aG = !new ge().aG() ? gkVar.aG(str) : gkVar.aH(str);
            if (aG != null) {
                gkVar.a(aG);
                if (this.LC.iZ()) {
                    aG.mRubbishFiles = A(aG.mRubbishFiles);
                }
            }
            return aG;
        } catch (Exception e) {
            d.c("xx", e);
            return null;
        }
    }

    public SoftRubModel scanSoftRubbishInstall(String str) {
        if (str == null) {
            return null;
        }
        hb.s(((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG());
        gl aQ = gl.aQ();
        try {
            SoftRubModel aG = !new ge().aG() ? aQ.aG(str) : aQ.aH(str);
            if (aG != null && this.LC.iZ()) {
                for (RubbishEntity rubbishEntity : aG.mRubbishFilesInstall) {
                    rubbishEntity.path = A(rubbishEntity.path);
                }
            }
            return aG;
        } catch (Exception e) {
            d.c("xx", e);
            return null;
        }
    }

    public boolean startClean() {
        if (this.LC.getmRubbishEntityManager() == null) {
            return false;
        }
        this.LE = false;
        jq.ct().a(new Runnable() {
            final /* synthetic */ a LF;

            {
                this.LF = r1;
            }

            public void run() {
                this.LF.Lz.onCleanStart();
                this.LF.jc();
                this.LF.Lz.onCleanFinish();
            }
        }, null);
        d.g("DeepcleanManagerImpl", "onCleanStart-- [true]");
        return true;
    }

    public boolean startScan(int i) {
        if (this.Lz == null) {
            return false;
        }
        this.LC.getmRubbishEntityManager().resetRubbishes();
        this.LA = i;
        he jb = jb();
        this.Ly.bd();
        if (this.LB != null) {
            this.Ly.a((String[]) this.LB.toArray(new String[this.LB.size()]));
        }
        this.Ly.a(jb);
        this.Ly.t(false);
        return true;
    }
}
