package ohos.data.dataability;

import ohos.data.PredicatesUtils;
import ohos.data.orm.OrmObject;
import ohos.data.orm.OrmPredicates;
import ohos.data.rdb.RdbPredicates;
import ohos.hiviewdfx.HiLogLabel;

public class DataAbilityUtils {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109536, "DataAbilityUtils");

    private DataAbilityUtils() {
    }

    public static RdbPredicates createRdbPredicates(DataAbilityPredicates dataAbilityPredicates, String str) {
        if (str == null || "".equals(str)) {
            throw new IllegalArgumentException("tableName cannot be null.");
        }
        RdbPredicates rdbPredicates = new RdbPredicates(str);
        if (dataAbilityPredicates == null) {
            return rdbPredicates;
        }
        PredicatesUtils.setWhereClauseAndArgs(rdbPredicates, dataAbilityPredicates.getWhereClause(), dataAbilityPredicates.getWhereArgs());
        PredicatesUtils.setAttributes(rdbPredicates, dataAbilityPredicates.isDistinct(), dataAbilityPredicates.getIndex(), dataAbilityPredicates.getGroup(), dataAbilityPredicates.getOrder(), dataAbilityPredicates.getLimit(), dataAbilityPredicates.getOffset());
        return rdbPredicates;
    }

    public static <T extends OrmObject> OrmPredicates createOrmPredicates(DataAbilityPredicates dataAbilityPredicates, Class<T> cls) {
        if (cls != null) {
            OrmPredicates ormPredicates = new OrmPredicates(cls);
            if (dataAbilityPredicates == null) {
                return ormPredicates;
            }
            PredicatesUtils.setWhereClauseAndArgs(ormPredicates, dataAbilityPredicates.getWhereClause(), dataAbilityPredicates.getWhereArgs());
            PredicatesUtils.setAttributes(ormPredicates, dataAbilityPredicates.isDistinct(), dataAbilityPredicates.getIndex(), dataAbilityPredicates.getGroup(), dataAbilityPredicates.getOrder(), dataAbilityPredicates.getLimit(), dataAbilityPredicates.getOffset());
            return ormPredicates;
        }
        throw new IllegalArgumentException("klass cannot be null.");
    }
}
