package tmsdk.fg.module.cleanV2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RubbishHolder {
    Map<String, RubbishEntity> NT;
    List<RubbishEntity> NU;
    Map<String, RubbishEntity> NV;
    Map<String, RubbishEntity> NW;

    private void a(RubbishEntity rubbishEntity) {
        if (this.NT == null) {
            this.NT = new HashMap();
        }
        Object obj = null;
        RubbishEntity rubbishEntity2 = (RubbishEntity) this.NT.get(rubbishEntity.getDescription());
        if (rubbishEntity2 != null) {
            rubbishEntity2.a(rubbishEntity.getRubbishKey(), rubbishEntity.getSize());
            obj = 1;
        }
        if (obj == null) {
            this.NT.put(rubbishEntity.getDescription(), rubbishEntity);
        }
    }

    private void b(RubbishEntity rubbishEntity) {
        if (this.NU == null) {
            this.NU = new ArrayList();
        }
        this.NU.add(rubbishEntity);
    }

    private void c(RubbishEntity rubbishEntity) {
        if (this.NV == null) {
            this.NV = new HashMap();
        }
        String str = rubbishEntity.getPackageName() + rubbishEntity.getDescription();
        RubbishEntity rubbishEntity2 = (RubbishEntity) this.NV.get(str);
        if (rubbishEntity2 != null) {
            rubbishEntity2.a(rubbishEntity.getRubbishKey(), rubbishEntity.getSize());
        } else {
            this.NV.put(str, rubbishEntity);
        }
    }

    private void d(RubbishEntity rubbishEntity) {
        if (this.NW == null) {
            this.NW = new HashMap();
        }
        String str = rubbishEntity.getPackageName() + rubbishEntity.getDescription();
        RubbishEntity rubbishEntity2 = (RubbishEntity) this.NW.get(str);
        if (rubbishEntity2 != null) {
            rubbishEntity2.a(rubbishEntity.getRubbishKey(), rubbishEntity.getSize());
        } else {
            this.NW.put(str, rubbishEntity);
        }
    }

    public void addRubbish(RubbishEntity rubbishEntity) {
        switch (rubbishEntity.getRubbishType()) {
            case 0:
                c(rubbishEntity);
                return;
            case 1:
                a(rubbishEntity);
                return;
            case 2:
                b(rubbishEntity);
                return;
            case 4:
                d(rubbishEntity);
                return;
            default:
                return;
        }
    }

    public long getAllRubbishFileSize() {
        long j = 0;
        if (this.NT != null) {
            for (RubbishEntity size : this.NT.values()) {
                j += size.getSize();
            }
        }
        if (this.NU != null) {
            for (RubbishEntity size2 : this.NU) {
                j += size2.getSize();
            }
        }
        if (this.NV != null) {
            for (RubbishEntity size22 : this.NV.values()) {
                j += size22.getSize();
            }
        }
        if (this.NW != null) {
            for (RubbishEntity size222 : this.NW.values()) {
                j += size222.getSize();
            }
        }
        return j;
    }

    public long getCleanRubbishFileSize() {
        long j = 0;
        if (this.NT != null) {
            for (RubbishEntity rubbishEntity : this.NT.values()) {
                if (2 == rubbishEntity.getStatus()) {
                    j += rubbishEntity.getSize();
                }
            }
        }
        if (this.NU != null) {
            for (RubbishEntity rubbishEntity2 : this.NU) {
                if (2 == rubbishEntity2.getStatus()) {
                    j += rubbishEntity2.getSize();
                }
            }
        }
        if (this.NV != null) {
            for (RubbishEntity rubbishEntity22 : this.NV.values()) {
                if (2 == rubbishEntity22.getStatus()) {
                    j += rubbishEntity22.getSize();
                }
            }
        }
        if (this.NW != null) {
            for (RubbishEntity rubbishEntity222 : this.NW.values()) {
                if (2 == rubbishEntity222.getStatus()) {
                    j += rubbishEntity222.getSize();
                }
            }
        }
        return j;
    }

    public long getSelectedRubbishFileSize() {
        long j = 0;
        if (this.NT != null) {
            for (RubbishEntity rubbishEntity : this.NT.values()) {
                if (1 == rubbishEntity.getStatus()) {
                    j += rubbishEntity.getSize();
                }
            }
        }
        if (this.NU != null) {
            for (RubbishEntity rubbishEntity2 : this.NU) {
                if (1 == rubbishEntity2.getStatus()) {
                    j += rubbishEntity2.getSize();
                }
            }
        }
        if (this.NV != null) {
            for (RubbishEntity rubbishEntity22 : this.NV.values()) {
                if (1 == rubbishEntity22.getStatus()) {
                    j += rubbishEntity22.getSize();
                }
            }
        }
        if (this.NW != null) {
            for (RubbishEntity rubbishEntity222 : this.NW.values()) {
                if (1 == rubbishEntity222.getStatus()) {
                    j += rubbishEntity222.getSize();
                }
            }
        }
        return j;
    }

    public long getSuggetRubbishFileSize() {
        long j = 0;
        if (this.NT != null) {
            for (RubbishEntity rubbishEntity : this.NT.values()) {
                if (rubbishEntity.isSuggest()) {
                    j += rubbishEntity.getSize();
                }
            }
        }
        if (this.NU != null) {
            for (RubbishEntity rubbishEntity2 : this.NU) {
                if (rubbishEntity2.isSuggest()) {
                    j += rubbishEntity2.getSize();
                }
            }
        }
        if (this.NV != null) {
            for (RubbishEntity rubbishEntity22 : this.NV.values()) {
                if (rubbishEntity22.isSuggest()) {
                    j += rubbishEntity22.getSize();
                }
            }
        }
        if (this.NW != null) {
            for (RubbishEntity rubbishEntity222 : this.NW.values()) {
                if (rubbishEntity222.isSuggest()) {
                    j += rubbishEntity222.getSize();
                }
            }
        }
        return j;
    }

    public List<RubbishEntity> getmApkRubbishes() {
        return this.NU;
    }

    public Map<String, RubbishEntity> getmInstallRubbishes() {
        return this.NV;
    }

    public Map<String, RubbishEntity> getmSystemRubbishes() {
        return this.NT;
    }

    public Map<String, RubbishEntity> getmUnInstallRubbishes() {
        return this.NW;
    }

    public void resetRubbishes() {
        this.NT.clear();
        this.NU.clear();
        this.NV.clear();
        this.NW.clear();
    }
}
