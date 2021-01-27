package com.huawei.odmf.user;

import android.text.TextUtils;
import com.huawei.odmf.core.AObjectContext;
import com.huawei.odmf.core.ManagedObject;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalPredicateException;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.predicate.FetchRequest;
import com.huawei.odmf.user.api.ObjectContext;
import com.huawei.odmf.user.api.Query;
import java.lang.reflect.Field;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class AQueryImpl<T extends ManagedObject> implements Query<T> {
    public static final int AVG = 3;
    public static final int COUNT = 2;
    private static final String DOT = ".";
    public static final int MAX = 0;
    public static final int MIN = 1;
    public static final int SUM = 4;
    private List<Integer> aggregateOps = new ArrayList();
    private List<String> columns = new ArrayList();
    private List<String> columnsWithAggregateFunction = new ArrayList();
    private String entityName = null;
    private FetchRequest<T> fetchRequest = null;
    private boolean isSorted = false;
    private boolean needOpBeforeThis = false;
    private AObjectContext objectContext = null;
    private List<String> sqlArgs = new ArrayList();
    private String tableName = null;

    public AQueryImpl(String str, FetchRequest<T> fetchRequest2, ObjectContext objectContext2) {
        this.entityName = str;
        this.fetchRequest = fetchRequest2;
        this.objectContext = (AObjectContext) objectContext2;
        this.needOpBeforeThis = false;
        this.tableName = str.substring(str.lastIndexOf(DOT) + 1);
    }

    public Iterator iterator() {
        return findAllLazyList().iterator();
    }

    @Override // com.huawei.odmf.user.api.Query
    public List<Object> queryWithAggregateFunction() {
        this.fetchRequest.setSelectionArgs((String[]) this.sqlArgs.toArray(new String[0]));
        this.fetchRequest.setColumnsWithAggregateFunction((String[]) this.columnsWithAggregateFunction.toArray(new String[0]));
        this.fetchRequest.setColumns((String[]) this.columns.toArray(new String[0]));
        Integer[] numArr = (Integer[]) this.aggregateOps.toArray(new Integer[0]);
        int[] iArr = new int[numArr.length];
        for (int i = 0; i < numArr.length; i++) {
            iArr[i] = numArr[i].intValue();
        }
        this.fetchRequest.setAggregateOp(iArr);
        return this.objectContext.executeFetchRequestWithAggregateFunction(this.fetchRequest);
    }

    @Override // com.huawei.odmf.user.api.Query
    public List<T> findAll() {
        this.fetchRequest.setSelectionArgs((String[]) this.sqlArgs.toArray(new String[0]));
        return this.objectContext.executeFetchRequest(this.fetchRequest);
    }

    @Override // com.huawei.odmf.user.api.Query
    public List<T> findAllLazyList() {
        this.fetchRequest.setSelectionArgs((String[]) this.sqlArgs.toArray(new String[0]));
        return this.objectContext.executeFetchRequestLazyList(this.fetchRequest);
    }

    @Override // com.huawei.odmf.user.api.Query
    public ListIterator<T> listIterator() {
        this.fetchRequest.setSelectionArgs((String[]) this.sqlArgs.toArray(new String[0]));
        return this.objectContext.executeFetchRequestLazyList(this.fetchRequest).listIterator();
    }

    private void checkParameter(String str, String str2, String... strArr) {
        if (str2 == null) {
            throw new ODMFIllegalArgumentException("QueryImpl." + str + "(): field is null.");
        } else if (str2.equals("")) {
            throw new ODMFIllegalArgumentException("QueryImpl." + str + "(): string 'field' is empty.");
        } else if (strArr != null) {
            int length = strArr.length;
            for (int i = 0; i < length; i++) {
                if (strArr[i] == null) {
                    throw new ODMFIllegalArgumentException("QueryImpl." + str + "(): value" + i + " is null.");
                }
            }
        }
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> equalTo(String str, Byte b) {
        return equalTo(str, String.valueOf(b));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> equalTo(String str, Short sh) {
        return equalTo(str, String.valueOf(sh));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> equalTo(String str, Integer num) {
        return equalTo(str, String.valueOf(num));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> equalTo(String str, Long l) {
        return equalTo(str, String.valueOf(l));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> equalTo(String str, Double d) {
        return equalTo(str, String.valueOf(d));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> equalTo(String str, Float f) {
        return equalTo(str, String.valueOf(f));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> equalTo(String str, Boolean bool) {
        return equalTo(str, String.valueOf(bool.booleanValue() ? 1 : 0));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> equalTo(String str, Date date) {
        return equalTo(str, String.valueOf(date.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> equalTo(String str, Time time) {
        return equalTo(str, String.valueOf(time.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> equalTo(String str, Timestamp timestamp) {
        return equalTo(str, String.valueOf(timestamp.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> equalTo(String str, Calendar calendar) {
        return equalTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> equalTo(String str, String str2) {
        checkParameter("equalTo", str, str2);
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append("AND ");
        } else {
            this.needOpBeforeThis = true;
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder sqlRequest = this.fetchRequest.getSqlRequest();
        sqlRequest.append(processFieldAndJoinClause);
        sqlRequest.append(" = ? ");
        this.sqlArgs.add(str2);
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> notEqualTo(String str, Byte b) {
        return notEqualTo(str, String.valueOf(b));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> notEqualTo(String str, Short sh) {
        return notEqualTo(str, String.valueOf(sh));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> notEqualTo(String str, Integer num) {
        return notEqualTo(str, String.valueOf(num));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> notEqualTo(String str, Long l) {
        return notEqualTo(str, String.valueOf(l));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> notEqualTo(String str, Double d) {
        return notEqualTo(str, String.valueOf(d));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> notEqualTo(String str, Float f) {
        return notEqualTo(str, String.valueOf(f));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> notEqualTo(String str, Boolean bool) {
        return notEqualTo(str, String.valueOf(bool.booleanValue() ? 1 : 0));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> notEqualTo(String str, Date date) {
        return notEqualTo(str, String.valueOf(date.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> notEqualTo(String str, Time time) {
        return notEqualTo(str, String.valueOf(time.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> notEqualTo(String str, Timestamp timestamp) {
        return notEqualTo(str, String.valueOf(timestamp.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> notEqualTo(String str, Calendar calendar) {
        return notEqualTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> notEqualTo(String str, String str2) {
        checkParameter("notEqualTo", str, str2);
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append("AND ");
        } else {
            this.needOpBeforeThis = true;
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder sqlRequest = this.fetchRequest.getSqlRequest();
        sqlRequest.append(processFieldAndJoinClause);
        sqlRequest.append(" <> ? ");
        this.sqlArgs.add(str2);
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> beginGroup() {
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append("AND ");
            this.needOpBeforeThis = false;
        }
        this.fetchRequest.getSqlRequest().append(" ( ");
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> endGroup() {
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append(" ) ");
            return this;
        }
        throw new ODMFIllegalPredicateException("QueryImpl.endGroup(): you cannot use function or() before end parenthesis, start a query with endGroup(), or use endGroup() right after beginGroup().");
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> or() {
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append(" OR ");
            this.needOpBeforeThis = false;
            return this;
        }
        throw new ODMFIllegalPredicateException("QueryImpl.or(): you are starting a sql request with predicate \"or\" or using function or() immediately after another or(). that is ridiculous.");
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> and() {
        if (this.needOpBeforeThis) {
            return this;
        }
        throw new ODMFIllegalPredicateException("QueryImpl.and(): you should not start a request with \"and\" or use or() before this function.");
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> contains(String str, String str2) {
        checkParameter("contains", str, str2);
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append(" AND ");
        } else {
            this.needOpBeforeThis = true;
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder sqlRequest = this.fetchRequest.getSqlRequest();
        sqlRequest.append(processFieldAndJoinClause);
        sqlRequest.append(" LIKE ? ");
        List<String> list = this.sqlArgs;
        list.add("%" + str2 + "%");
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> beginsWith(String str, String str2) {
        checkParameter("beginsWith", str, str2);
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append(" AND ");
        } else {
            this.needOpBeforeThis = true;
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder sqlRequest = this.fetchRequest.getSqlRequest();
        sqlRequest.append(processFieldAndJoinClause);
        sqlRequest.append(" LIKE ? ");
        List<String> list = this.sqlArgs;
        list.add(str2 + "%");
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> endsWith(String str, String str2) {
        checkParameter("endsWith", str, str2);
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append(" AND ");
        } else {
            this.needOpBeforeThis = true;
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder sqlRequest = this.fetchRequest.getSqlRequest();
        sqlRequest.append(processFieldAndJoinClause);
        sqlRequest.append(" LIKE ? ");
        List<String> list = this.sqlArgs;
        list.add("%" + str2);
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> isNull(String str) {
        checkParameter("isNull", str, new String[0]);
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append(" AND ");
        } else {
            this.needOpBeforeThis = true;
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder sqlRequest = this.fetchRequest.getSqlRequest();
        sqlRequest.append(processFieldAndJoinClause);
        sqlRequest.append(" is null ");
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> isNotNull(String str) {
        checkParameter("isNotNull", str, new String[0]);
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append(" AND ");
        } else {
            this.needOpBeforeThis = true;
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder sqlRequest = this.fetchRequest.getSqlRequest();
        sqlRequest.append(processFieldAndJoinClause);
        sqlRequest.append(" is not null ");
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> like(String str, String str2) {
        checkParameter("like", str, str2);
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append(" AND ");
        } else {
            this.needOpBeforeThis = true;
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder sqlRequest = this.fetchRequest.getSqlRequest();
        sqlRequest.append(processFieldAndJoinClause);
        sqlRequest.append(" LIKE ? ");
        this.sqlArgs.add(str2);
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> between(String str, Integer num, Integer num2) {
        return between(str, String.valueOf(num), String.valueOf(num2));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> between(String str, Long l, Long l2) {
        return between(str, String.valueOf(l), String.valueOf(l2));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> between(String str, Double d, Double d2) {
        return between(str, String.valueOf(d), String.valueOf(d2));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> between(String str, Float f, Float f2) {
        return between(str, String.valueOf(f), String.valueOf(f2));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> between(String str, Date date, Date date2) {
        return between(str, String.valueOf(date.getTime()), String.valueOf(date2.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> between(String str, Time time, Time time2) {
        return between(str, String.valueOf(time.getTime()), String.valueOf(time2.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> between(String str, Timestamp timestamp, Timestamp timestamp2) {
        return between(str, String.valueOf(timestamp.getTime()), String.valueOf(timestamp2.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> between(String str, Calendar calendar, Calendar calendar2) {
        return between(str, String.valueOf(calendar.getTimeInMillis()), String.valueOf(calendar2.getTimeInMillis()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> between(String str, String str2, String str3) {
        checkParameter("between", str, str2, str3);
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append(" AND ");
        } else {
            this.needOpBeforeThis = true;
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder sqlRequest = this.fetchRequest.getSqlRequest();
        sqlRequest.append(processFieldAndJoinClause);
        sqlRequest.append(" BETWEEN ? AND ? ");
        this.sqlArgs.add(str2);
        this.sqlArgs.add(str3);
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThan(String str, Integer num) {
        return greaterThan(str, String.valueOf(num));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThan(String str, Long l) {
        return greaterThan(str, String.valueOf(l));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThan(String str, Double d) {
        return greaterThan(str, String.valueOf(d));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThan(String str, Float f) {
        return greaterThan(str, String.valueOf(f));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThan(String str, Date date) {
        return greaterThan(str, String.valueOf(date.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThan(String str, Time time) {
        return greaterThan(str, String.valueOf(time.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThan(String str, Timestamp timestamp) {
        return greaterThan(str, String.valueOf(timestamp.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThan(String str, Calendar calendar) {
        return greaterThan(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThan(String str, String str2) {
        checkParameter("greaterThan", str, str2);
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append("AND ");
        } else {
            this.needOpBeforeThis = true;
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder sqlRequest = this.fetchRequest.getSqlRequest();
        sqlRequest.append(processFieldAndJoinClause);
        sqlRequest.append(" > ? ");
        this.sqlArgs.add(str2);
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThan(String str, Integer num) {
        return lessThan(str, String.valueOf(num));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThan(String str, Long l) {
        return lessThan(str, String.valueOf(l));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThan(String str, Double d) {
        return lessThan(str, String.valueOf(d));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThan(String str, Float f) {
        return lessThan(str, String.valueOf(f));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThan(String str, Date date) {
        return lessThan(str, String.valueOf(date.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThan(String str, Time time) {
        return lessThan(str, String.valueOf(time.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThan(String str, Timestamp timestamp) {
        return lessThan(str, String.valueOf(timestamp.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThan(String str, Calendar calendar) {
        return lessThan(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThan(String str, String str2) {
        checkParameter("lessThan", str, str2);
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append("AND ");
        } else {
            this.needOpBeforeThis = true;
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder sqlRequest = this.fetchRequest.getSqlRequest();
        sqlRequest.append(processFieldAndJoinClause);
        sqlRequest.append(" < ? ");
        this.sqlArgs.add(str2);
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThanOrEqualTo(String str, Integer num) {
        return greaterThanOrEqualTo(str, String.valueOf(num));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThanOrEqualTo(String str, Long l) {
        return greaterThanOrEqualTo(str, String.valueOf(l));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThanOrEqualTo(String str, Double d) {
        return greaterThanOrEqualTo(str, String.valueOf(d));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThanOrEqualTo(String str, Float f) {
        return greaterThanOrEqualTo(str, String.valueOf(f));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThanOrEqualTo(String str, Date date) {
        return greaterThanOrEqualTo(str, String.valueOf(date.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThanOrEqualTo(String str, Time time) {
        return greaterThanOrEqualTo(str, String.valueOf(time.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThanOrEqualTo(String str, Timestamp timestamp) {
        return greaterThanOrEqualTo(str, String.valueOf(timestamp.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThanOrEqualTo(String str, Calendar calendar) {
        return greaterThanOrEqualTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> greaterThanOrEqualTo(String str, String str2) {
        checkParameter("greaterThanOrEqualTo", str, str2);
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append("AND ");
        } else {
            this.needOpBeforeThis = true;
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder sqlRequest = this.fetchRequest.getSqlRequest();
        sqlRequest.append(processFieldAndJoinClause);
        sqlRequest.append(" >= ? ");
        this.sqlArgs.add(str2);
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThanOrEqualTo(String str, Integer num) {
        return lessThanOrEqualTo(str, String.valueOf(num));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThanOrEqualTo(String str, Long l) {
        return lessThanOrEqualTo(str, String.valueOf(l));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThanOrEqualTo(String str, Double d) {
        return lessThanOrEqualTo(str, String.valueOf(d));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThanOrEqualTo(String str, Float f) {
        return lessThanOrEqualTo(str, String.valueOf(f));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThanOrEqualTo(String str, Date date) {
        return lessThanOrEqualTo(str, String.valueOf(date.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThanOrEqualTo(String str, Time time) {
        return lessThanOrEqualTo(str, String.valueOf(time.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThanOrEqualTo(String str, Timestamp timestamp) {
        return lessThanOrEqualTo(str, String.valueOf(timestamp.getTime()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThanOrEqualTo(String str, Calendar calendar) {
        return lessThanOrEqualTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> lessThanOrEqualTo(String str, String str2) {
        checkParameter("lessThanOrEqualTo", str, str2);
        if (this.needOpBeforeThis) {
            this.fetchRequest.getSqlRequest().append("AND ");
        } else {
            this.needOpBeforeThis = true;
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder sqlRequest = this.fetchRequest.getSqlRequest();
        sqlRequest.append(processFieldAndJoinClause);
        sqlRequest.append(" <= ? ");
        this.sqlArgs.add(str2);
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> orderByAsc(String str) {
        checkParameter("orderByAsc", str, new String[0]);
        if (this.isSorted) {
            this.fetchRequest.getOrder().append(',');
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder order = this.fetchRequest.getOrder();
        order.append(processFieldAndJoinClause);
        order.append(" ASC ");
        this.isSorted = true;
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> orderByDesc(String str) {
        checkParameter("orderByDesc", str, new String[0]);
        if (this.isSorted) {
            this.fetchRequest.getOrder().append(',');
        }
        String processFieldAndJoinClause = processFieldAndJoinClause(removeQuotes(str));
        StringBuilder order = this.fetchRequest.getOrder();
        order.append(processFieldAndJoinClause);
        order.append(" DESC ");
        this.isSorted = true;
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> limit(int i) {
        if (!this.fetchRequest.getLimit().equals("")) {
            throw new ODMFIllegalPredicateException("QueryImpl.limit(): limit cannot be set twice.");
        } else if (i >= 1) {
            this.fetchRequest.setLimit(Integer.toString(i));
            return this;
        } else {
            throw new ODMFIllegalArgumentException("QueryImpl.limit(): limit cannot be less than zero.");
        }
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> max(String str) {
        String str2;
        checkParameter("max", str, new String[0]);
        String removeQuotes = removeQuotes(str);
        if (removeQuotes.equals("*")) {
            str2 = removeQuotes;
        } else {
            str2 = appendTableName(surroundWithQuote(removeQuotes));
        }
        List<String> list = this.columnsWithAggregateFunction;
        list.add("MAX(" + str2 + ")");
        this.columns.add(removeQuotes);
        this.aggregateOps.add(0);
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> min(String str) {
        String str2;
        checkParameter("min", str, new String[0]);
        String removeQuotes = removeQuotes(str);
        if (removeQuotes.equals("*")) {
            str2 = removeQuotes;
        } else {
            str2 = appendTableName(surroundWithQuote(removeQuotes));
        }
        List<String> list = this.columnsWithAggregateFunction;
        list.add("MIN(" + str2 + ")");
        this.columns.add(removeQuotes);
        this.aggregateOps.add(1);
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> count(String str) {
        String str2;
        checkParameter("count", str, new String[0]);
        String removeQuotes = removeQuotes(str);
        if (removeQuotes.equals("*")) {
            str2 = removeQuotes;
        } else {
            str2 = appendTableName(surroundWithQuote(removeQuotes));
        }
        List<String> list = this.columnsWithAggregateFunction;
        list.add("COUNT(" + str2 + ")");
        this.columns.add(removeQuotes);
        this.aggregateOps.add(2);
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> avg(String str) {
        String str2;
        checkParameter("avg", str, new String[0]);
        String removeQuotes = removeQuotes(str);
        if (removeQuotes.equals("*")) {
            str2 = removeQuotes;
        } else {
            str2 = appendTableName(surroundWithQuote(removeQuotes));
        }
        List<String> list = this.columnsWithAggregateFunction;
        list.add("AVG(" + str2 + ")");
        this.columns.add(removeQuotes);
        this.aggregateOps.add(3);
        return this;
    }

    @Override // com.huawei.odmf.user.api.Query
    public Query<T> sum(String str) {
        String str2;
        checkParameter("sum", str, new String[0]);
        String removeQuotes = removeQuotes(str);
        if (removeQuotes.equals("*")) {
            str2 = removeQuotes;
        } else {
            str2 = appendTableName(surroundWithQuote(removeQuotes));
        }
        List<String> list = this.columnsWithAggregateFunction;
        list.add("SUM(" + str2 + ")");
        this.columns.add(removeQuotes);
        this.aggregateOps.add(4);
        return this;
    }

    public FetchRequest<T> getFetchRequest() {
        this.fetchRequest.setSelectionArgs((String[]) this.sqlArgs.toArray(new String[0]));
        if (this.aggregateOps.size() > 0) {
            this.fetchRequest.setColumnsWithAggregateFunction((String[]) this.columnsWithAggregateFunction.toArray(new String[0]));
            this.fetchRequest.setColumns((String[]) this.columns.toArray(new String[0]));
            Integer[] numArr = (Integer[]) this.aggregateOps.toArray(new Integer[0]);
            int[] iArr = new int[numArr.length];
            for (int i = 0; i < numArr.length; i++) {
                iArr[i] = numArr[i].intValue();
            }
            this.fetchRequest.setAggregateOp(iArr);
        }
        return this.fetchRequest;
    }

    private String processFieldAndJoinClause(String str) {
        if (str.contains(DOT)) {
            String[] split = str.split("\\.");
            if (split.length == 2) {
                String str2 = split[0];
                String surroundWithQuote = surroundWithQuote(split[1]);
                try {
                    Field declaredField = this.fetchRequest.getTheClass().getDeclaredField(str2);
                    if (declaredField.getType().toString().equals("interface java.util.List")) {
                        String obj = declaredField.getGenericType().toString();
                        String substring = obj.substring(obj.indexOf("<") + 1, obj.indexOf(">"));
                        String substring2 = substring.substring(substring.lastIndexOf(DOT) + 1);
                        if (!this.fetchRequest.getJoinedEntities().contains(substring)) {
                            StringBuilder joinClause = this.fetchRequest.getJoinClause();
                            joinClause.append(" INNER JOIN ");
                            joinClause.append(substring2);
                            joinClause.append(" ON ");
                            joinClause.append(this.entityName);
                            joinClause.append("=");
                            joinClause.append(substring);
                            String str3 = substring2 + DOT + surroundWithQuote;
                            this.fetchRequest.addToJoinedEntities(substring);
                            return str3;
                        }
                        return substring2 + DOT + surroundWithQuote;
                    }
                    String name = declaredField.getType().getName();
                    String substring3 = name.substring(name.lastIndexOf(DOT) + 1);
                    if (!this.fetchRequest.getJoinedEntities().contains(name)) {
                        StringBuilder joinClause2 = this.fetchRequest.getJoinClause();
                        joinClause2.append(" INNER JOIN ");
                        joinClause2.append(substring3);
                        joinClause2.append(" ON ");
                        joinClause2.append(this.entityName);
                        joinClause2.append("=");
                        joinClause2.append(name);
                        String str4 = substring3 + DOT + surroundWithQuote;
                        this.fetchRequest.addToJoinedEntities(name);
                        return str4;
                    }
                    return substring3 + DOT + surroundWithQuote;
                } catch (NoSuchFieldException unused) {
                    throw new ODMFRuntimeException("NoSuchFieldException");
                }
            } else {
                throw new ODMFIllegalArgumentException("Wrong field name.");
            }
        } else {
            return this.tableName + DOT + surroundWithQuote(str);
        }
    }

    private static String removeQuotes(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return str.replace("'", "").replace("\"", "");
    }

    private static String surroundWithQuote(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return '\"' + str + '\"';
    }

    private String appendTableName(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return surroundWithQuote(this.tableName) + DOT + str;
    }
}
