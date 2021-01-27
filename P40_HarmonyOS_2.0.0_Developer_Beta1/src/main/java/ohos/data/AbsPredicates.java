package ohos.data;

import java.util.ArrayList;
import java.util.List;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;
import ohos.data.orm.StringUtils;
import ohos.hiviewdfx.HiLogLabel;

public abstract class AbsPredicates {
    private static final int DEFAULT_GROUP_CLAUSE_LENGTH = 20;
    private static final int DEFAULT_ORDER_CLAUSE_LENGTH = 20;
    protected static final int DEFAULT_SELECT_ARG_NUMBER = 8;
    private static final int DEFAULT_SELECT_CLAUSE_LENGTH = 120;
    protected static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "AbsPredicates");
    private boolean distinct;
    private StringBuffer group;
    private String index;
    private boolean isNeedAnd;
    private boolean isSorted;
    private Integer limit;
    private Integer offset;
    private StringBuffer order;
    private List<String> whereArgs;
    private StringBuffer whereClause;

    public AbsPredicates() {
        initial();
    }

    /* access modifiers changed from: protected */
    public void clear() {
        initial();
    }

    /* access modifiers changed from: protected */
    public AbsPredicates equalTo(String str, String str2) {
        checkParameter("equalTo", str, str2);
        if (this.isNeedAnd) {
            this.whereClause.append("AND ");
        } else {
            this.isNeedAnd = true;
        }
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" = ? ");
        this.whereArgs.add(str2);
        return this;
    }

    private void initial() {
        this.distinct = false;
        this.isNeedAnd = false;
        this.isSorted = false;
        this.whereArgs = new ArrayList(8);
        this.whereClause = new StringBuffer(120);
        this.order = new StringBuffer(20);
        this.group = new StringBuffer(20);
        this.index = null;
        this.limit = null;
        this.offset = null;
    }

    public enum JoinType {
        INNER("INNER JOIN"),
        LEFT("LEFT OUTER JOIN"),
        CROSS("CROSS JOIN");
        
        private String sqlGrammar;

        private JoinType(String str) {
            this.sqlGrammar = str;
        }

        public String grammar() {
            return this.sqlGrammar;
        }
    }

    public String getWhereClause() {
        return this.whereClause.toString();
    }

    public void setWhereClause(String str) {
        if (str != null) {
            this.whereClause = new StringBuffer(str);
        }
    }

    public List<String> getWhereArgs() {
        return this.whereArgs;
    }

    public void setWhereArgs(List<String> list) {
        this.whereArgs = list;
    }

    public String getOrder() {
        return this.order.toString();
    }

    public void setOrder(String str) {
        if (str != null) {
            this.order = new StringBuffer(str);
        }
    }

    public Integer getLimit() {
        return this.limit;
    }

    public Integer getOffset() {
        return this.offset;
    }

    public boolean isDistinct() {
        return this.distinct;
    }

    public String getGroup() {
        return this.group.toString();
    }

    public String getIndex() {
        return this.index;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates notEqualTo(String str, String str2) {
        checkParameter("notEqualTo", str, str2);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" <> ? ");
        this.whereArgs.add(str2);
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates beginWrap() {
        if (this.isNeedAnd) {
            this.whereClause.append("AND ");
            this.isNeedAnd = false;
        }
        this.whereClause.append(" ( ");
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates endWrap() {
        if (this.isNeedAnd) {
            this.whereClause.append(" ) ");
            return this;
        }
        throw new IllegalPredicateException("AbsPredicates.endGroup(): you cannot use function or() before end parenthesis, start a AbsPredicates with endGroup(), or use endGroup() right after beginGroup().");
    }

    /* access modifiers changed from: protected */
    public AbsPredicates or() {
        if (this.isNeedAnd) {
            this.whereClause.append(" OR ");
            this.isNeedAnd = false;
            return this;
        }
        throw new IllegalPredicateException("QueryImpl.or(): you are starting a sql request with predicate \"or\" or using function or() immediately after another or(). that is ridiculous.");
    }

    /* access modifiers changed from: protected */
    public AbsPredicates and() {
        if (this.isNeedAnd) {
            return this;
        }
        throw new IllegalPredicateException("QueryImpl.and(): you should not start a request with \"and\" or use or() before this function.");
    }

    /* access modifiers changed from: protected */
    public AbsPredicates contains(String str, String str2) {
        checkParameter(Keywords.FUNC_CONTAINS_STRING, str, str2);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" LIKE ? ");
        List<String> list = this.whereArgs;
        list.add("%" + str2 + "%");
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates beginsWith(String str, String str2) {
        checkParameter("beginsWith", str, str2);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" LIKE ? ");
        List<String> list = this.whereArgs;
        list.add(str2 + "%");
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates endsWith(String str, String str2) {
        checkParameter("endsWith", str, str2);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" LIKE ? ");
        List<String> list = this.whereArgs;
        list.add("%" + str2);
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates isNull(String str) {
        checkParameter("isNull", str, new String[0]);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" is null ");
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates isNotNull(String str) {
        checkParameter("isNotNull", str, new String[0]);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" is not null ");
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates like(String str, String str2) {
        checkParameter("like", str, str2);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" LIKE ? ");
        this.whereArgs.add(str2);
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates glob(String str, String str2) {
        checkParameter("glob", str, str2);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" GLOB ? ");
        this.whereArgs.add(str2);
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates between(String str, String str2, String str3) {
        checkParameter("between", str, str2, str3);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" BETWEEN ? AND ? ");
        this.whereArgs.add(str2);
        this.whereArgs.add(str3);
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates notBetween(String str, String str2, String str3) {
        checkParameter("notBetween", str, str2, str3);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" NOT BETWEEN ? AND ? ");
        this.whereArgs.add(str2);
        this.whereArgs.add(str3);
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates greaterThan(String str, String str2) {
        checkParameter("greaterThan", str, str2);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" > ? ");
        this.whereArgs.add(str2);
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates lessThan(String str, String str2) {
        checkParameter("lessThan", str, str2);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" < ? ");
        this.whereArgs.add(str2);
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates greaterThanOrEqualTo(String str, String str2) {
        checkParameter("greaterThanOrEqualTo", str, str2);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" >= ? ");
        this.whereArgs.add(str2);
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates lessThanOrEqualTo(String str, String str2) {
        checkParameter("lessThanOrEqualTo", str, str2);
        checkIsNeedAnd();
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(" <= ? ");
        this.whereArgs.add(str2);
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates orderByAsc(String str) {
        checkParameter("orderByAsc", str, new String[0]);
        if (this.isSorted) {
            this.order.append(',');
        }
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.order;
        stringBuffer.append(normalized);
        stringBuffer.append(" ASC ");
        this.isSorted = true;
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates orderByDesc(String str) {
        checkParameter("orderByDesc", str, new String[0]);
        if (this.isSorted) {
            this.whereClause.append(',');
        }
        String normalized = normalized(removeQuotes(str));
        StringBuffer stringBuffer = this.order;
        stringBuffer.append(normalized);
        stringBuffer.append(" DESC ");
        this.isSorted = true;
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates distinct() {
        this.distinct = true;
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates limit(int i) {
        if (this.limit != null) {
            throw new IllegalPredicateException("AbsPredicates limit(): limit cannot be set twice.");
        } else if (i >= 1) {
            this.limit = Integer.valueOf(i);
            return this;
        } else {
            throw new IllegalPredicateException("AbsPredicates limit(): limit cannot be less than or equal to zero.");
        }
    }

    /* access modifiers changed from: protected */
    public AbsPredicates offset(int i) {
        if (this.offset != null) {
            throw new IllegalPredicateException("AbsPredicates offset(): offset cannot be set twice.");
        } else if (i >= 1) {
            this.offset = Integer.valueOf(i);
            return this;
        } else {
            throw new IllegalPredicateException("AbsPredicates offset(): the value of offset can't be less than or equal to zero.");
        }
    }

    /* access modifiers changed from: protected */
    public AbsPredicates groupBy(String[] strArr) {
        if (strArr == null || strArr.length == 0) {
            throw new IllegalPredicateException("AbsPredicates: groupBy() fails because fields can't be null.");
        }
        for (String str : strArr) {
            checkParameter("groupBy", str, new String[0]);
            StringBuffer stringBuffer = this.group;
            stringBuffer.append(normalized(removeQuotes(str)));
            stringBuffer.append(",");
        }
        StringBuffer stringBuffer2 = this.group;
        stringBuffer2.deleteCharAt(stringBuffer2.lastIndexOf(","));
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates indexedBy(String str) {
        checkParameter("indexedBy", str, new String[0]);
        this.index = removeQuotes(str);
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates in(String str, String[] strArr) {
        checkParameter("in", str, new String[0]);
        if (strArr == null || strArr.length == 0) {
            throw new IllegalPredicateException("AbsPredicates: in() fails because values can't be null.");
        }
        checkIsNeedAnd();
        ArrayList arrayList = new ArrayList(strArr.length);
        for (String str2 : strArr) {
            arrayList.add("?");
            this.whereArgs.add(str2);
        }
        appendWhereClauseWithInOrNotIn(" IN ", str, arrayList);
        return this;
    }

    /* access modifiers changed from: protected */
    public AbsPredicates notIn(String str, String[] strArr) {
        checkParameter("notIn", str, new String[0]);
        if (strArr == null || strArr.length == 0) {
            throw new IllegalPredicateException("AbsPredicates: notIn() fails because values is null");
        }
        checkIsNeedAnd();
        ArrayList arrayList = new ArrayList(strArr.length);
        for (String str2 : strArr) {
            arrayList.add("?");
            this.whereArgs.add(str2);
        }
        appendWhereClauseWithInOrNotIn(" NOT IN ", str, arrayList);
        return this;
    }

    private void checkParameter(String str, String str2, String... strArr) {
        if (str2 == null) {
            throw new IllegalPredicateException("QueryImpl." + str + "(): field is null.");
        } else if ("".equals(str2)) {
            throw new IllegalPredicateException("QueryImpl." + str + "(): string 'field' is empty.");
        } else if (strArr != null) {
            int length = strArr.length;
            for (int i = 0; i < length; i++) {
                if (strArr[i] == null) {
                    throw new IllegalPredicateException("QueryImpl." + str + "(): value" + i + " is null.");
                }
            }
        }
    }

    private String removeQuotes(String str) {
        if (str == null || "".equals(str)) {
            return "";
        }
        return str.replace("'", "").replace("\"", "").replace("`", "");
    }

    private String normalized(String str) {
        if (str == null || "".equals(str)) {
            return "";
        }
        if (!str.contains(".")) {
            return StringUtils.surroundWithQuote(str, "`");
        }
        String[] split = str.split("\\.");
        if (split.length == 2) {
            return StringUtils.surroundWithQuote(split[0], "`") + "." + StringUtils.surroundWithQuote(split[1], "`");
        }
        throw new IllegalPredicateException("Wrong field name.");
    }

    private void checkIsNeedAnd() {
        if (this.isNeedAnd) {
            this.whereClause.append(" AND ");
        } else {
            this.isNeedAnd = true;
        }
    }

    private void appendWhereClauseWithInOrNotIn(String str, String str2, List<String> list) {
        String normalized = normalized(removeQuotes(str2));
        StringBuffer stringBuffer = this.whereClause;
        stringBuffer.append(normalized);
        stringBuffer.append(StringUtils.surroundWithFunction(str, ",", (String[]) list.toArray(new String[0])));
    }
}
