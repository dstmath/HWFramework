package ohos.data.rdb;

import java.util.function.IntConsumer;
import ohos.data.rdb.impl.RdbStoreImpl;
import ohos.data.search.model.SearchParameter;

public class RdbUtils {
    private static final int NEGATIVE_ONE = -1;
    private static final char SINGLE_QUOTES = '\'';

    public enum OperationType {
        QUERY_TYPE(SearchParameter.QUERY),
        DELETE_TYPE("delete"),
        COUNT_TYPE("count");
        
        private String value;

        private OperationType(String str) {
            this.value = str;
        }

        public String getValue() {
            return this.value;
        }
    }

    private RdbUtils() {
    }

    public static void appendStringToSqlAndEscapeQuote(StringBuilder sb, String str) {
        if (str != null) {
            sb.append('\'');
            if (isContainSingleQuotes(str)) {
                str.codePoints().forEach(new IntConsumer(sb) {
                    /* class ohos.data.rdb.$$Lambda$RdbUtils$gWbiGRkg6NxGRufSHXbKgvp0ebU */
                    private final /* synthetic */ StringBuilder f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.IntConsumer
                    public final void accept(int i) {
                        RdbUtils.lambda$appendStringToSqlAndEscapeQuote$0(this.f$0, i);
                    }
                });
            } else {
                sb.append(str);
            }
            sb.append('\'');
        }
    }

    static /* synthetic */ void lambda$appendStringToSqlAndEscapeQuote$0(StringBuilder sb, int i) {
        if (39 == i) {
            sb.append('\'');
        }
        sb.append((char) i);
    }

    public static String escapeQuote(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        appendStringToSqlAndEscapeQuote(sb, str);
        return sb.toString();
    }

    public static void verifySql(RdbStore rdbStore, String str) {
        if (rdbStore != null) {
            RdbStoreImpl rdbStoreImpl = null;
            if (rdbStore instanceof RdbStoreImpl) {
                rdbStoreImpl = (RdbStoreImpl) rdbStore;
            }
            if (rdbStoreImpl != null) {
                rdbStoreImpl.verifySQl(str);
                return;
            }
            throw new IllegalArgumentException("The input rdbStore is invalid in verifySql.");
        }
        throw new IllegalArgumentException("The input rdbStore can't be null in verifySql.");
    }

    public static void verifyPredicates(RdbStore rdbStore, OperationType operationType, AbsRdbPredicates absRdbPredicates) {
        if (rdbStore == null || absRdbPredicates == null) {
            throw new IllegalArgumentException("The input rdbStore or predicates can't be null in verifyPredicates.");
        }
        RdbStoreImpl rdbStoreImpl = null;
        if (rdbStore instanceof RdbStoreImpl) {
            rdbStoreImpl = (RdbStoreImpl) rdbStore;
        }
        if (rdbStoreImpl != null) {
            rdbStoreImpl.verifyPredicates(operationType, absRdbPredicates);
            return;
        }
        throw new IllegalArgumentException("The input rdbStore is invalid in verifyPredicates.");
    }

    private static boolean isContainSingleQuotes(String str) {
        return str.indexOf(39) > -1;
    }
}
