package ohos.security.asset;

import java.util.Collections;
import java.util.List;

public class AssetResult {
    public final int resultCode;
    public final List<String> resultInfo;
    public final int resultNumber;

    public AssetResult(int i) {
        this(i, null);
    }

    public AssetResult(int i, List<String> list) {
        this(i, list, 0);
    }

    public AssetResult(int i, List<String> list, int i2) {
        List<String> list2;
        this.resultCode = i;
        if (list == null) {
            list2 = null;
        } else {
            list2 = Collections.unmodifiableList(list);
        }
        this.resultInfo = list2;
        this.resultNumber = i2;
    }

    public int getResultCode() {
        return this.resultCode;
    }

    public List<String> getResultInfo() {
        return this.resultInfo;
    }

    public int getResultNumber() {
        return this.resultNumber;
    }
}
