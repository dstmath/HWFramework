package tmsdk.fg.module.deepclean;

import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class RubbishEntity {
    public String description;
    public boolean isSuggest;
    private String packageName;
    public List<String> path;
    private String qf;
    public int rubbishType;
    public long size;
    int status;

    public RubbishEntity() {
        this.status = 0;
    }

    protected RubbishEntity(int i, List<String> list, boolean z, long j, String str, String str2, String str3) {
        this.status = 0;
        this.rubbishType = i;
        this.path = list;
        this.isSuggest = z;
        if (this.isSuggest) {
            this.status = 1;
        }
        this.size = j;
        this.qf = str;
        this.packageName = str2;
        this.description = str3;
    }

    protected void e(String str, long j) {
        if (this.path == null) {
            this.path = new ArrayList();
        }
        this.size += j;
        for (String equals : this.path) {
            if (equals.equals(str)) {
                return;
            }
        }
        this.path.add(str);
    }

    public String getAppName() {
        return this.qf;
    }

    public String getDescription() {
        return this.description;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public List<String> getRubbishKey() {
        return this.path;
    }

    public int getRubbishType() {
        return this.rubbishType;
    }

    public long getSize() {
        return this.size;
    }

    public int getStatus() {
        return this.status;
    }

    public boolean isSuggest() {
        return this.isSuggest;
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
}
