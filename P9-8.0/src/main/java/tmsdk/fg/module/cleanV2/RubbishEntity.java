package tmsdk.fg.module.cleanV2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RubbishEntity implements Cloneable, Comparable<RubbishEntity> {
    public static final int FILE_TYPE_DOC = 3;
    public static final int FILE_TYPE_OTHER = -1;
    public static final int FILE_TYPE_PIC = 1;
    public static final int FILE_TYPE_SOUND = 0;
    public static final int FILE_TYPE_VIDEO = 2;
    public static final int INDEX_APK = 2;
    public static final int INDEX_SOFTWARE_CACHE = 0;
    public static final int INDEX_SYSTEM_RUBBISH = 1;
    public static final int INDEX_UNINSTALL_RETAIL = 4;
    public static final int MODEL_TYPE_DELETED = 2;
    public static final int MODEL_TYPE_SELECTED = 1;
    public static final int MODEL_TYPE_UNSELECTED = 0;
    private int NN;
    private HashSet<String> NO;
    private boolean NP;
    private String NQ;
    private int NR;
    private Integer[] NS;
    private String Ne;
    private String description;
    private int mVersionCode;
    private String packageName;
    private long size;
    int status = 0;

    public RubbishEntity(int i, String str, boolean z, long j, String str2, String str3, String str4) {
        this.NN = i;
        this.NO = new HashSet();
        this.NO.add(str);
        this.NP = z;
        if (this.NP) {
            this.status = 1;
        }
        this.size = j;
        this.NQ = str2;
        this.packageName = str3;
        this.description = str4;
        this.mVersionCode = 0;
    }

    public RubbishEntity(int i, List<String> list, boolean z, long j, String str, String str2, String str3) {
        this.NN = i;
        this.NO = new HashSet(list);
        this.NP = z;
        if (this.NP) {
            this.status = 1;
        }
        this.size = j;
        this.NQ = str;
        this.packageName = str2;
        this.description = str3;
        this.mVersionCode = 0;
    }

    protected void a(List<String> list, long j) {
        this.size += j;
        this.NO.addAll(list);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int compareTo(RubbishEntity rubbishEntity) {
        return this.description.compareTo(rubbishEntity.description);
    }

    public String getAppName() {
        return this.NQ;
    }

    public String getDescription() {
        return this.description;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public List<String> getRubbishKey() {
        return new ArrayList(this.NO);
    }

    public int getRubbishType() {
        return this.NN;
    }

    public long getSize() {
        return this.size;
    }

    public int getStatus() {
        return this.status;
    }

    public int getVersionCode() {
        return this.mVersionCode;
    }

    public String getmCleanTips() {
        return this.Ne;
    }

    public int getmFileType() {
        return this.NR;
    }

    public Integer[] getmGroupIds() {
        return this.NS;
    }

    public boolean isSuggest() {
        return this.NP;
    }

    protected void ju() {
        this.status = 2;
        this.NO.clear();
    }

    public void setExtendData(int i, String str, List<Integer> list) {
        this.NR = i;
        this.Ne = str;
        if (list != null) {
            this.NS = (Integer[]) list.toArray(new Integer[list.size()]);
        }
    }

    public boolean setStatus(int i) {
        if (2 == i) {
            return false;
        }
        if (i != 0 && 1 != i) {
            return false;
        }
        this.status = i;
        return true;
    }

    public void setVersionCode(int i) {
        this.mVersionCode = i;
    }
}
