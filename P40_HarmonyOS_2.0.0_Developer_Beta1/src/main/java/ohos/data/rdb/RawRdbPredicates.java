package ohos.data.rdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RawRdbPredicates extends AbsRdbPredicates {
    public RawRdbPredicates(String str) {
        super(str);
    }

    public RawRdbPredicates(String str, String str2, String[] strArr) {
        super(str);
        ArrayList arrayList;
        super.setWhereClause(str2);
        if (strArr != null) {
            arrayList = new ArrayList(Arrays.asList(strArr));
        }
        super.setWhereArgs(arrayList);
    }

    @Override // ohos.data.AbsPredicates
    public void setWhereClause(String str) {
        super.setWhereClause(str);
    }

    @Override // ohos.data.AbsPredicates
    public void setWhereArgs(List<String> list) {
        super.setWhereArgs(list);
    }

    @Override // ohos.data.AbsPredicates
    public String getWhereClause() {
        return super.getWhereClause();
    }

    @Override // ohos.data.AbsPredicates
    public List<String> getWhereArgs() {
        return super.getWhereArgs();
    }
}
