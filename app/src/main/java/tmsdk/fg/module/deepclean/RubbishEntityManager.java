package tmsdk.fg.module.deepclean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class RubbishEntityManager {
    List<RubbishEntity> LG;
    List<RubbishEntity> LH;
    List<RubbishEntity> LI;
    HashMap<String, RubbishEntity> LJ;
    HashMap<String, RubbishEntity> LK;

    public RubbishEntityManager() {
        this.LG = new ArrayList();
    }

    private List<RubbishEntity> jd() {
        resetRubbishes();
        if (this.LJ != null) {
            for (String str : this.LJ.keySet()) {
                this.LG.add((RubbishEntity) this.LJ.get(str));
            }
            this.LJ = null;
        }
        if (this.LK != null) {
            for (String str2 : this.LK.keySet()) {
                this.LG.add((RubbishEntity) this.LK.get(str2));
            }
            this.LK = null;
        }
        if (this.LI != null) {
            for (RubbishEntity add : this.LI) {
                this.LG.add(add);
            }
            this.LI = null;
        }
        if (this.LH != null) {
            for (RubbishEntity add2 : this.LH) {
                this.LG.add(add2);
            }
            this.LH = null;
        }
        return this.LG;
    }

    public void addRubbish(RubbishEntity rubbishEntity) {
        String str;
        RubbishEntity rubbishEntity2;
        switch (rubbishEntity.getRubbishType()) {
            case SpaceManager.ERROR_CODE_OK /*0*/:
                if (this.LJ == null) {
                    this.LJ = new HashMap();
                }
                str = rubbishEntity.getPackageName() + rubbishEntity.getDescription();
                rubbishEntity2 = (RubbishEntity) this.LJ.get(str);
                if (rubbishEntity2 != null) {
                    rubbishEntity2.e((String) rubbishEntity.getRubbishKey().get(0), rubbishEntity.getSize());
                } else {
                    this.LJ.put(str, rubbishEntity);
                }
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                int i;
                if (this.LH == null) {
                    this.LH = new ArrayList();
                }
                for (RubbishEntity rubbishEntity22 : this.LH) {
                    if (rubbishEntity22.getDescription().equals(rubbishEntity.getDescription())) {
                        rubbishEntity22.e((String) rubbishEntity.getRubbishKey().get(0), rubbishEntity.getSize());
                        i = 1;
                        if (i == 0) {
                            this.LH.add(rubbishEntity);
                        }
                    }
                }
                i = 0;
                if (i == 0) {
                    this.LH.add(rubbishEntity);
                }
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                if (this.LI == null) {
                    this.LI = new ArrayList();
                }
                this.LI.add(rubbishEntity);
            case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                if (this.LK == null) {
                    this.LK = new HashMap();
                }
                str = rubbishEntity.getPackageName() + rubbishEntity.getDescription();
                rubbishEntity22 = (RubbishEntity) this.LK.get(str);
                if (rubbishEntity22 != null) {
                    rubbishEntity22.e((String) rubbishEntity.getRubbishKey().get(0), rubbishEntity.getSize());
                } else {
                    this.LK.put(str, rubbishEntity);
                }
            default:
        }
    }

    public void deleteFinished() {
        for (RubbishEntity rubbishEntity : this.LG) {
            if (1 == rubbishEntity.getStatus()) {
                rubbishEntity.setStatus(2);
            }
        }
    }

    public synchronized long getAllRubbishSize() {
        long j;
        if (this.LH == null) {
            if (this.LI == null && this.LJ == null && this.LK == null) {
                if (this.LG != null) {
                    return 0;
                }
                j = 0;
                for (RubbishEntity size : this.LG) {
                    j = size.getSize() + j;
                }
                return j;
            }
        }
        jd();
        if (this.LG != null) {
            return 0;
        }
        j = 0;
        while (r4.hasNext()) {
            j = size.getSize() + j;
        }
        return j;
    }

    public synchronized long getCleanRubbishSize() {
        long j = 0;
        synchronized (this) {
            if (this.LH == null) {
                if (this.LI == null && this.LJ == null && this.LK == null) {
                    if (this.LG == null) {
                        for (RubbishEntity rubbishEntity : this.LG) {
                            j = 2 == rubbishEntity.getStatus() ? j : rubbishEntity.getSize() + j;
                        }
                        return j;
                    }
                    return 0;
                }
            }
            jd();
            if (this.LG == null) {
                return 0;
            }
            for (RubbishEntity rubbishEntity2 : this.LG) {
                if (2 == rubbishEntity2.getStatus()) {
                }
                j = 2 == rubbishEntity2.getStatus() ? j : rubbishEntity2.getSize() + j;
            }
            return j;
        }
    }

    public synchronized List<RubbishEntity> getRubbishes() {
        if (this.LG.size() >= 1) {
            return this.LG;
        }
        return jd();
    }

    public synchronized long getSelectedRubbishSize() {
        long j = 0;
        synchronized (this) {
            if (this.LH == null) {
                if (this.LI == null && this.LJ == null && this.LK == null) {
                    if (this.LG == null) {
                        for (RubbishEntity rubbishEntity : this.LG) {
                            j = 1 == rubbishEntity.getStatus() ? j : rubbishEntity.getSize() + j;
                        }
                        return j;
                    }
                    return 0;
                }
            }
            jd();
            if (this.LG == null) {
                return 0;
            }
            for (RubbishEntity rubbishEntity2 : this.LG) {
                if (1 == rubbishEntity2.getStatus()) {
                }
                j = 1 == rubbishEntity2.getStatus() ? j : rubbishEntity2.getSize() + j;
            }
            return j;
        }
    }

    public synchronized long getSuggetRubbishSize() {
        long j = 0;
        synchronized (this) {
            if (this.LH == null) {
                if (this.LI == null && this.LJ == null && this.LK == null) {
                    if (this.LG == null) {
                        for (RubbishEntity rubbishEntity : this.LG) {
                            j = rubbishEntity.isSuggest() ? j : rubbishEntity.getSize() + j;
                        }
                        return j;
                    }
                    return 0;
                }
            }
            jd();
            if (this.LG == null) {
                return 0;
            }
            for (RubbishEntity rubbishEntity2 : this.LG) {
                if (rubbishEntity2.isSuggest()) {
                }
                j = rubbishEntity2.isSuggest() ? j : rubbishEntity2.getSize() + j;
            }
            return j;
        }
    }

    public void resetRubbishes() {
        this.LG.clear();
    }
}
