package ohos.data.rdb;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import ohos.data.AbsPredicates;
import ohos.data.IllegalPredicateException;
import ohos.data.orm.StringUtils;

public class RdbPredicates extends AbsPredicates {
    private static final int DEFAULT_JOIN_TABLE_NUMBER = 8;
    private List<String> joinConditions;
    private int joinCount;
    private List<String> joinTableNames;
    private List<String> joinTypes;
    private String tableName;

    public RdbPredicates(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("no tableName specified.");
        }
        this.tableName = str;
        initial();
    }

    @Override // ohos.data.AbsPredicates
    public void clear() {
        super.clear();
        initial();
    }

    private void initial() {
        this.joinTypes = new ArrayList(8);
        this.joinTableNames = new ArrayList(8);
        this.joinConditions = new ArrayList(8);
        this.joinCount = 0;
    }

    public String getTableName() {
        return this.tableName;
    }

    public List<String> getJoinTypes() {
        return this.joinTypes;
    }

    public void setJoinTypes(List<String> list) {
        this.joinTypes = list;
    }

    public List<String> getJoinTableNames() {
        return this.joinTableNames;
    }

    public void setJoinTableNames(List<String> list) {
        this.joinTableNames = list;
    }

    public List<String> getJoinConditions() {
        return this.joinConditions;
    }

    public void setJoinConditions(List<String> list) {
        this.joinConditions = list;
    }

    public String getJoinClause() {
        List<String> list = this.joinTableNames;
        if (list == null || list.isEmpty()) {
            return null;
        }
        return processJoins();
    }

    public int getJoinCount() {
        return this.joinCount;
    }

    public void setJoinCount(int i) {
        this.joinCount = i;
    }

    public RdbPredicates equalTo(String str, byte b) {
        return equalTo(str, String.valueOf((int) b));
    }

    public RdbPredicates equalTo(String str, short s) {
        return equalTo(str, String.valueOf((int) s));
    }

    public RdbPredicates equalTo(String str, int i) {
        return equalTo(str, String.valueOf(i));
    }

    public RdbPredicates equalTo(String str, long j) {
        return equalTo(str, String.valueOf(j));
    }

    public RdbPredicates equalTo(String str, double d) {
        return equalTo(str, String.valueOf(d));
    }

    public RdbPredicates equalTo(String str, float f) {
        return equalTo(str, String.valueOf(f));
    }

    public RdbPredicates equalTo(String str, boolean z) {
        return equalTo(str, String.valueOf(z ? 1 : 0));
    }

    public RdbPredicates equalTo(String str, Date date) {
        return equalTo(str, String.valueOf(date.getTime()));
    }

    public RdbPredicates equalTo(String str, Time time) {
        return equalTo(str, String.valueOf(time.getTime()));
    }

    public RdbPredicates equalTo(String str, Timestamp timestamp) {
        return equalTo(str, String.valueOf(timestamp.getTime()));
    }

    public RdbPredicates equalTo(String str, Calendar calendar) {
        return equalTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates equalTo(String str, String str2) {
        return (RdbPredicates) super.equalTo(str, str2);
    }

    public RdbPredicates notEqualTo(String str, byte b) {
        return notEqualTo(str, String.valueOf((int) b));
    }

    public RdbPredicates notEqualTo(String str, short s) {
        return notEqualTo(str, String.valueOf((int) s));
    }

    public RdbPredicates notEqualTo(String str, int i) {
        return notEqualTo(str, String.valueOf(i));
    }

    public RdbPredicates notEqualTo(String str, long j) {
        return notEqualTo(str, String.valueOf(j));
    }

    public RdbPredicates notEqualTo(String str, double d) {
        return notEqualTo(str, String.valueOf(d));
    }

    public RdbPredicates notEqualTo(String str, float f) {
        return notEqualTo(str, String.valueOf(f));
    }

    public RdbPredicates notEqualTo(String str, boolean z) {
        return notEqualTo(str, String.valueOf(z ? 1 : 0));
    }

    public RdbPredicates notEqualTo(String str, Date date) {
        return notEqualTo(str, String.valueOf(date.getTime()));
    }

    public RdbPredicates notEqualTo(String str, Time time) {
        return notEqualTo(str, String.valueOf(time.getTime()));
    }

    public RdbPredicates notEqualTo(String str, Timestamp timestamp) {
        return notEqualTo(str, String.valueOf(timestamp.getTime()));
    }

    public RdbPredicates notEqualTo(String str, Calendar calendar) {
        return notEqualTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates notEqualTo(String str, String str2) {
        return (RdbPredicates) super.notEqualTo(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates beginWrap() {
        return (RdbPredicates) super.beginWrap();
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates endWrap() {
        return (RdbPredicates) super.endWrap();
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates or() {
        return (RdbPredicates) super.or();
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates and() {
        return (RdbPredicates) super.and();
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates contains(String str, String str2) {
        return (RdbPredicates) super.contains(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates beginsWith(String str, String str2) {
        return (RdbPredicates) super.beginsWith(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates endsWith(String str, String str2) {
        return (RdbPredicates) super.endsWith(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates isNull(String str) {
        return (RdbPredicates) super.isNull(str);
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates isNotNull(String str) {
        return (RdbPredicates) super.isNotNull(str);
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates like(String str, String str2) {
        return (RdbPredicates) super.like(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates glob(String str, String str2) {
        return (RdbPredicates) super.glob(str, str2);
    }

    public RdbPredicates between(String str, int i, int i2) {
        return between(str, String.valueOf(i), String.valueOf(i2));
    }

    public RdbPredicates between(String str, long j, long j2) {
        return between(str, String.valueOf(j), String.valueOf(j2));
    }

    public RdbPredicates between(String str, double d, double d2) {
        return between(str, String.valueOf(d), String.valueOf(d2));
    }

    public RdbPredicates between(String str, float f, float f2) {
        return between(str, String.valueOf(f), String.valueOf(f2));
    }

    public RdbPredicates between(String str, Date date, Date date2) {
        return between(str, String.valueOf(date.getTime()), String.valueOf(date2.getTime()));
    }

    public RdbPredicates between(String str, Time time, Time time2) {
        return between(str, String.valueOf(time.getTime()), String.valueOf(time2.getTime()));
    }

    public RdbPredicates between(String str, Timestamp timestamp, Timestamp timestamp2) {
        return between(str, String.valueOf(timestamp.getTime()), String.valueOf(timestamp2.getTime()));
    }

    public RdbPredicates between(String str, Calendar calendar, Calendar calendar2) {
        return between(str, String.valueOf(calendar.getTimeInMillis()), String.valueOf(calendar2.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates between(String str, String str2, String str3) {
        return (RdbPredicates) super.between(str, str2, str3);
    }

    public RdbPredicates notBetween(String str, int i, int i2) {
        return notBetween(str, String.valueOf(i), String.valueOf(i2));
    }

    public RdbPredicates notBetween(String str, long j, long j2) {
        return notBetween(str, String.valueOf(j), String.valueOf(j2));
    }

    public RdbPredicates notBetween(String str, double d, double d2) {
        return notBetween(str, String.valueOf(d), String.valueOf(d2));
    }

    public RdbPredicates notBetween(String str, float f, float f2) {
        return notBetween(str, String.valueOf(f), String.valueOf(f2));
    }

    public RdbPredicates notBetween(String str, Date date, Date date2) {
        return notBetween(str, String.valueOf(date.getTime()), String.valueOf(date2.getTime()));
    }

    public RdbPredicates notBetween(String str, Time time, Time time2) {
        return notBetween(str, String.valueOf(time.getTime()), String.valueOf(time2.getTime()));
    }

    public RdbPredicates notBetween(String str, Timestamp timestamp, Timestamp timestamp2) {
        return notBetween(str, String.valueOf(timestamp.getTime()), String.valueOf(timestamp2.getTime()));
    }

    public RdbPredicates notBetween(String str, Calendar calendar, Calendar calendar2) {
        return notBetween(str, String.valueOf(calendar.getTimeInMillis()), String.valueOf(calendar2.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates notBetween(String str, String str2, String str3) {
        return (RdbPredicates) super.notBetween(str, str2, str3);
    }

    public RdbPredicates greaterThan(String str, int i) {
        return greaterThan(str, String.valueOf(i));
    }

    public RdbPredicates greaterThan(String str, long j) {
        return greaterThan(str, String.valueOf(j));
    }

    public RdbPredicates greaterThan(String str, double d) {
        return greaterThan(str, String.valueOf(d));
    }

    public RdbPredicates greaterThan(String str, float f) {
        return greaterThan(str, String.valueOf(f));
    }

    public RdbPredicates greaterThan(String str, Date date) {
        return greaterThan(str, String.valueOf(date.getTime()));
    }

    public RdbPredicates greaterThan(String str, Time time) {
        return greaterThan(str, String.valueOf(time.getTime()));
    }

    public RdbPredicates greaterThan(String str, Timestamp timestamp) {
        return greaterThan(str, String.valueOf(timestamp.getTime()));
    }

    public RdbPredicates greaterThan(String str, Calendar calendar) {
        return greaterThan(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates greaterThan(String str, String str2) {
        return (RdbPredicates) super.greaterThan(str, str2);
    }

    public RdbPredicates lessThan(String str, int i) {
        return lessThan(str, String.valueOf(i));
    }

    public RdbPredicates lessThan(String str, long j) {
        return lessThan(str, String.valueOf(j));
    }

    public RdbPredicates lessThan(String str, double d) {
        return lessThan(str, String.valueOf(d));
    }

    public RdbPredicates lessThan(String str, float f) {
        return lessThan(str, String.valueOf(f));
    }

    public RdbPredicates lessThan(String str, Date date) {
        return lessThan(str, String.valueOf(date.getTime()));
    }

    public RdbPredicates lessThan(String str, Time time) {
        return lessThan(str, String.valueOf(time.getTime()));
    }

    public RdbPredicates lessThan(String str, Timestamp timestamp) {
        return lessThan(str, String.valueOf(timestamp.getTime()));
    }

    public RdbPredicates lessThan(String str, Calendar calendar) {
        return lessThan(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates lessThan(String str, String str2) {
        return (RdbPredicates) super.lessThan(str, str2);
    }

    public RdbPredicates greaterThanOrEqualTo(String str, int i) {
        return greaterThanOrEqualTo(str, String.valueOf(i));
    }

    public RdbPredicates greaterThanOrEqualTo(String str, long j) {
        return greaterThanOrEqualTo(str, String.valueOf(j));
    }

    public RdbPredicates greaterThanOrEqualTo(String str, double d) {
        return greaterThanOrEqualTo(str, String.valueOf(d));
    }

    public RdbPredicates greaterThanOrEqualTo(String str, float f) {
        return greaterThanOrEqualTo(str, String.valueOf(f));
    }

    public RdbPredicates greaterThanOrEqualTo(String str, Date date) {
        return greaterThanOrEqualTo(str, String.valueOf(date.getTime()));
    }

    public RdbPredicates greaterThanOrEqualTo(String str, Time time) {
        return greaterThanOrEqualTo(str, String.valueOf(time.getTime()));
    }

    public RdbPredicates greaterThanOrEqualTo(String str, Timestamp timestamp) {
        return greaterThanOrEqualTo(str, String.valueOf(timestamp.getTime()));
    }

    public RdbPredicates greaterThanOrEqualTo(String str, Calendar calendar) {
        return greaterThanOrEqualTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates greaterThanOrEqualTo(String str, String str2) {
        return (RdbPredicates) super.greaterThanOrEqualTo(str, str2);
    }

    public RdbPredicates lessThanOrEqualTo(String str, int i) {
        return lessThanOrEqualTo(str, String.valueOf(i));
    }

    public RdbPredicates lessThanOrEqualTo(String str, long j) {
        return lessThanOrEqualTo(str, String.valueOf(j));
    }

    public RdbPredicates lessThanOrEqualTo(String str, double d) {
        return lessThanOrEqualTo(str, String.valueOf(d));
    }

    public RdbPredicates lessThanOrEqualTo(String str, float f) {
        return lessThanOrEqualTo(str, String.valueOf(f));
    }

    public RdbPredicates lessThanOrEqualTo(String str, Date date) {
        return lessThanOrEqualTo(str, String.valueOf(date.getTime()));
    }

    public RdbPredicates lessThanOrEqualTo(String str, Time time) {
        return lessThanOrEqualTo(str, String.valueOf(time.getTime()));
    }

    public RdbPredicates lessThanOrEqualTo(String str, Timestamp timestamp) {
        return lessThanOrEqualTo(str, String.valueOf(timestamp.getTime()));
    }

    public RdbPredicates lessThanOrEqualTo(String str, Calendar calendar) {
        return lessThanOrEqualTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates lessThanOrEqualTo(String str, String str2) {
        return (RdbPredicates) super.lessThanOrEqualTo(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates orderByAsc(String str) {
        return (RdbPredicates) super.orderByAsc(str);
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates orderByDesc(String str) {
        return (RdbPredicates) super.orderByDesc(str);
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates distinct() {
        return (RdbPredicates) super.distinct();
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates limit(int i) {
        return (RdbPredicates) super.limit(i);
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates offset(int i) {
        return (RdbPredicates) super.offset(i);
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates groupBy(String[] strArr) {
        return (RdbPredicates) super.groupBy(strArr);
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates indexedBy(String str) {
        return (RdbPredicates) super.indexedBy(str);
    }

    public RdbPredicates in(String str, int[] iArr) {
        if (iArr == null || iArr.length == 0) {
            throw new IllegalPredicateException("RdbPredicates: in() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(iArr.length);
        for (int i : iArr) {
            arrayList.add(String.valueOf(i));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates in(String str, long[] jArr) {
        if (jArr == null || jArr.length == 0) {
            throw new IllegalPredicateException("RdbPredicates: in() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(jArr.length);
        for (long j : jArr) {
            arrayList.add(String.valueOf(j));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates in(String str, double[] dArr) {
        if (dArr == null || dArr.length == 0) {
            throw new IllegalPredicateException("RdbPredicates: in() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(dArr.length);
        for (double d : dArr) {
            arrayList.add(String.valueOf(d));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates in(String str, float[] fArr) {
        if (fArr == null || fArr.length == 0) {
            throw new IllegalPredicateException("RdbPredicates: in() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(fArr.length);
        for (float f : fArr) {
            arrayList.add(String.valueOf(f));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates in(String str, Date[] dateArr) {
        checkParamsValid(dateArr);
        ArrayList arrayList = new ArrayList(dateArr.length);
        for (Date date : dateArr) {
            arrayList.add(String.valueOf(date.getTime()));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates in(String str, Time[] timeArr) {
        checkParamsValid(timeArr);
        ArrayList arrayList = new ArrayList(timeArr.length);
        for (Time time : timeArr) {
            arrayList.add(String.valueOf(time.getTime()));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates in(String str, Timestamp[] timestampArr) {
        checkParamsValid(timestampArr);
        ArrayList arrayList = new ArrayList(timestampArr.length);
        for (Timestamp timestamp : timestampArr) {
            arrayList.add(String.valueOf(timestamp.getTime()));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates in(String str, Calendar[] calendarArr) {
        checkParamsValid(calendarArr);
        ArrayList arrayList = new ArrayList(calendarArr.length);
        for (Calendar calendar : calendarArr) {
            arrayList.add(String.valueOf(calendar.getTimeInMillis()));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates in(String str, String[] strArr) {
        return (RdbPredicates) super.in(str, strArr);
    }

    public RdbPredicates notIn(String str, int[] iArr) {
        if (iArr == null || iArr.length == 0) {
            throw new IllegalPredicateException("RdbPredicates: notIn() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(iArr.length);
        for (int i : iArr) {
            arrayList.add(String.valueOf(i));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates notIn(String str, long[] jArr) {
        if (jArr == null || jArr.length == 0) {
            throw new IllegalPredicateException("RdbPredicates: notIn() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(jArr.length);
        for (long j : jArr) {
            arrayList.add(String.valueOf(j));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates notIn(String str, double[] dArr) {
        if (dArr == null || dArr.length == 0) {
            throw new IllegalPredicateException("RdbPredicates: notIn() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(dArr.length);
        for (double d : dArr) {
            arrayList.add(String.valueOf(d));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates notIn(String str, float[] fArr) {
        if (fArr == null || fArr.length == 0) {
            throw new IllegalPredicateException("RdbPredicates: notIn() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(fArr.length);
        for (float f : fArr) {
            arrayList.add(String.valueOf(f));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates notIn(String str, Date[] dateArr) {
        checkParamsValid(dateArr);
        ArrayList arrayList = new ArrayList(dateArr.length);
        for (Date date : dateArr) {
            arrayList.add(String.valueOf(date.getTime()));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates notIn(String str, Time[] timeArr) {
        checkParamsValid(timeArr);
        ArrayList arrayList = new ArrayList(timeArr.length);
        for (Time time : timeArr) {
            arrayList.add(String.valueOf(time.getTime()));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates notIn(String str, Timestamp[] timestampArr) {
        checkParamsValid(timestampArr);
        ArrayList arrayList = new ArrayList(timestampArr.length);
        for (Timestamp timestamp : timestampArr) {
            arrayList.add(String.valueOf(timestamp.getTime()));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public RdbPredicates notIn(String str, Calendar[] calendarArr) {
        checkParamsValid(calendarArr);
        ArrayList arrayList = new ArrayList(calendarArr.length);
        for (Calendar calendar : calendarArr) {
            arrayList.add(String.valueOf(calendar.getTimeInMillis()));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    @Override // ohos.data.AbsPredicates
    public RdbPredicates notIn(String str, String[] strArr) {
        return (RdbPredicates) super.notIn(str, strArr);
    }

    public RdbPredicates crossJoin(String str) {
        return join(AbsPredicates.JoinType.CROSS, str);
    }

    public RdbPredicates innerJoin(String str) {
        return join(AbsPredicates.JoinType.INNER, str);
    }

    public RdbPredicates leftOuterJoin(String str) {
        return join(AbsPredicates.JoinType.LEFT, str);
    }

    public RdbPredicates using(String... strArr) {
        if (strArr == null || strArr.length == 0) {
            throw new IllegalPredicateException("RdbPredicates using failed : fields is null.");
        } else if (this.joinCount > 0) {
            while (true) {
                int i = this.joinCount;
                if (i > 1) {
                    this.joinConditions.add(null);
                    this.joinCount--;
                } else {
                    this.joinCount = i - 1;
                    this.joinConditions.add(StringUtils.surroundWithFunction("USING", ",", strArr));
                    return this;
                }
            }
        } else {
            throw new IllegalStateException("No active join operation befor using.");
        }
    }

    public RdbPredicates on(String... strArr) {
        if (strArr == null || strArr.length == 0) {
            throw new IllegalPredicateException("RdbPredicates on failed : clauses is null.");
        } else if (this.joinCount > 0) {
            while (true) {
                int i = this.joinCount;
                if (i > 1) {
                    this.joinConditions.add(null);
                    this.joinCount--;
                } else {
                    this.joinCount = i - 1;
                    this.joinConditions.add(StringUtils.surroundWithFunction("ON", "AND", strArr));
                    return this;
                }
            }
        } else {
            throw new IllegalStateException("No active join operation befor on.");
        }
    }

    private RdbPredicates join(AbsPredicates.JoinType joinType, String str) {
        if (str == null || "".equals(str)) {
            throw new IllegalPredicateException("RdbPredicates join failed: table name is null or empty.");
        }
        this.joinTypes.add(joinType.grammar());
        this.joinTableNames.add(str);
        this.joinCount++;
        return this;
    }

    private String processJoins() {
        StringBuilder sb = new StringBuilder(getTableName());
        int size = this.joinTableNames.size();
        for (int i = 0; i < size; i++) {
            sb.append(" ");
            sb.append(this.joinTypes.get(i));
            sb.append(" ");
            sb.append(this.joinTableNames.get(i));
            if (this.joinConditions.get(i) != null) {
                sb.append(" ");
                sb.append(this.joinConditions.get(i));
            }
        }
        return sb.toString();
    }

    private <T> void checkParamsValid(T[] tArr) {
        if (tArr == null || tArr.length == 0) {
            throw new IllegalPredicateException("RdbPredicates: predicates operation failsbecause values can't be null.");
        }
    }
}
