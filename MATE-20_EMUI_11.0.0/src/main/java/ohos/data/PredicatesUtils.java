package ohos.data;

import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class PredicatesUtils {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "PredicatesUtils");

    private PredicatesUtils() {
    }

    public static void setWhereClauseAndArgs(AbsPredicates absPredicates, String str, List<String> list) {
        absPredicates.setWhereClause(str);
        absPredicates.setWhereArgs(list);
    }

    public static void setAttributes(AbsPredicates absPredicates, boolean z, String str, String str2, String str3, Integer num, Integer num2) {
        if (z) {
            absPredicates.distinct();
        }
        if (str != null) {
            absPredicates.indexedBy(str);
        }
        if (str2 != null && !str2.isEmpty()) {
            absPredicates.groupBy(str2.replace("`", "").split(","));
        }
        if (str3 != null) {
            absPredicates.setOrder(str3);
        }
        if (num != null) {
            try {
                absPredicates.limit(num.intValue());
            } catch (IllegalPredicateException e) {
                HiLog.info(LABEL, "limit is illegal in setAttributes: %{public}s", new Object[]{e.getMessage()});
            }
        }
        if (num2 != null) {
            try {
                absPredicates.offset(num2.intValue());
            } catch (IllegalPredicateException e2) {
                HiLog.info(LABEL, "offset is illegal in setAttributes: %{public}s", new Object[]{e2.getMessage()});
            }
        }
    }
}
