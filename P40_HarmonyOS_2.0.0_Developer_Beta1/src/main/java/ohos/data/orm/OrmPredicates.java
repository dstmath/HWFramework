package ohos.data.orm;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import ohos.data.AbsPredicates;
import ohos.data.IllegalPredicateException;
import ohos.hiviewdfx.HiLogLabel;

public class OrmPredicates extends AbsPredicates {
    private static final int DEFAULT_JOIN_TABLE_NUMBER = 8;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "OrmPredicates");
    private String entityName;
    private List<String> joinConditions;
    private int joinCount;
    private List<String> joinEntityNames;
    private List<String> joinTypes;

    public <T extends OrmObject> OrmPredicates(Class<T> cls) {
        this.entityName = cls.getName();
        initial();
    }

    @Override // ohos.data.AbsPredicates
    public void clear() {
        super.clear();
        initial();
    }

    private void initial() {
        this.joinTypes = new ArrayList(8);
        this.joinEntityNames = new ArrayList(8);
        this.joinConditions = new ArrayList(8);
        this.joinCount = 0;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public List<String> getJoinTypes() {
        return this.joinTypes;
    }

    public void setJoinTypes(List<String> list) {
        this.joinTypes = list;
    }

    public List<String> getJoinEntityNames() {
        return this.joinEntityNames;
    }

    public void setJoinEntityNames(List<String> list) {
        this.joinEntityNames = list;
    }

    public List<String> getJoinConditions() {
        return this.joinConditions;
    }

    public void setJoinConditions(List<String> list) {
        this.joinConditions = list;
    }

    public int getJoinCount() {
        return this.joinCount;
    }

    public void setJoinCount(int i) {
        this.joinCount = i;
    }

    public OrmPredicates equalTo(String str, byte b) {
        return equalTo(str, String.valueOf((int) b));
    }

    public OrmPredicates equalTo(String str, short s) {
        return equalTo(str, String.valueOf((int) s));
    }

    public OrmPredicates equalTo(String str, int i) {
        return equalTo(str, String.valueOf(i));
    }

    public OrmPredicates equalTo(String str, long j) {
        return equalTo(str, String.valueOf(j));
    }

    public OrmPredicates equalTo(String str, double d) {
        return equalTo(str, String.valueOf(d));
    }

    public OrmPredicates equalTo(String str, float f) {
        return equalTo(str, String.valueOf(f));
    }

    public OrmPredicates equalTo(String str, boolean z) {
        return equalTo(str, String.valueOf(z ? 1 : 0));
    }

    public OrmPredicates equalTo(String str, Date date) {
        return equalTo(str, String.valueOf(date.getTime()));
    }

    public OrmPredicates equalTo(String str, Time time) {
        return equalTo(str, String.valueOf(time.getTime()));
    }

    public OrmPredicates equalTo(String str, Timestamp timestamp) {
        return equalTo(str, String.valueOf(timestamp.getTime()));
    }

    public OrmPredicates equalTo(String str, Calendar calendar) {
        return equalTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates equalTo(String str, String str2) {
        return (OrmPredicates) super.equalTo(str, str2);
    }

    public OrmPredicates notEqualTo(String str, byte b) {
        return notEqualTo(str, String.valueOf((int) b));
    }

    public OrmPredicates notEqualTo(String str, short s) {
        return notEqualTo(str, String.valueOf((int) s));
    }

    public OrmPredicates notEqualTo(String str, int i) {
        return notEqualTo(str, String.valueOf(i));
    }

    public OrmPredicates notEqualTo(String str, long j) {
        return notEqualTo(str, String.valueOf(j));
    }

    public OrmPredicates notEqualTo(String str, double d) {
        return notEqualTo(str, String.valueOf(d));
    }

    public OrmPredicates notEqualTo(String str, float f) {
        return notEqualTo(str, String.valueOf(f));
    }

    public OrmPredicates notEqualTo(String str, boolean z) {
        return notEqualTo(str, String.valueOf(z ? 1 : 0));
    }

    public OrmPredicates notEqualTo(String str, Date date) {
        return notEqualTo(str, String.valueOf(date.getTime()));
    }

    public OrmPredicates notEqualTo(String str, Time time) {
        return notEqualTo(str, String.valueOf(time.getTime()));
    }

    public OrmPredicates notEqualTo(String str, Timestamp timestamp) {
        return notEqualTo(str, String.valueOf(timestamp.getTime()));
    }

    public OrmPredicates notEqualTo(String str, Calendar calendar) {
        return notEqualTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates notEqualTo(String str, String str2) {
        return (OrmPredicates) super.notEqualTo(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates beginWrap() {
        return (OrmPredicates) super.beginWrap();
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates endWrap() {
        return (OrmPredicates) super.endWrap();
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates or() {
        return (OrmPredicates) super.or();
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates and() {
        return (OrmPredicates) super.and();
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates contains(String str, String str2) {
        return (OrmPredicates) super.contains(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates beginsWith(String str, String str2) {
        return (OrmPredicates) super.beginsWith(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates endsWith(String str, String str2) {
        return (OrmPredicates) super.endsWith(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates isNull(String str) {
        return (OrmPredicates) super.isNull(str);
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates isNotNull(String str) {
        return (OrmPredicates) super.isNotNull(str);
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates like(String str, String str2) {
        return (OrmPredicates) super.like(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates glob(String str, String str2) {
        return (OrmPredicates) super.glob(str, str2);
    }

    public OrmPredicates between(String str, int i, int i2) {
        return between(str, String.valueOf(i), String.valueOf(i2));
    }

    public OrmPredicates between(String str, long j, long j2) {
        return between(str, String.valueOf(j), String.valueOf(j2));
    }

    public OrmPredicates between(String str, double d, double d2) {
        return between(str, String.valueOf(d), String.valueOf(d2));
    }

    public OrmPredicates between(String str, float f, float f2) {
        return between(str, String.valueOf(f), String.valueOf(f2));
    }

    public OrmPredicates between(String str, Date date, Date date2) {
        return between(str, String.valueOf(date.getTime()), String.valueOf(date2.getTime()));
    }

    public OrmPredicates between(String str, Time time, Time time2) {
        return between(str, String.valueOf(time.getTime()), String.valueOf(time2.getTime()));
    }

    public OrmPredicates between(String str, Timestamp timestamp, Timestamp timestamp2) {
        return between(str, String.valueOf(timestamp.getTime()), String.valueOf(timestamp2.getTime()));
    }

    public OrmPredicates between(String str, Calendar calendar, Calendar calendar2) {
        return between(str, String.valueOf(calendar.getTimeInMillis()), String.valueOf(calendar2.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates between(String str, String str2, String str3) {
        return (OrmPredicates) super.between(str, str2, str3);
    }

    public OrmPredicates notBetween(String str, int i, int i2) {
        return notBetween(str, String.valueOf(i), String.valueOf(i2));
    }

    public OrmPredicates notBetween(String str, long j, long j2) {
        return notBetween(str, String.valueOf(j), String.valueOf(j2));
    }

    public OrmPredicates notBetween(String str, double d, double d2) {
        return notBetween(str, String.valueOf(d), String.valueOf(d2));
    }

    public OrmPredicates notBetween(String str, float f, float f2) {
        return notBetween(str, String.valueOf(f), String.valueOf(f2));
    }

    public OrmPredicates notBetween(String str, Date date, Date date2) {
        return notBetween(str, String.valueOf(date.getTime()), String.valueOf(date2.getTime()));
    }

    public OrmPredicates notBetween(String str, Time time, Time time2) {
        return notBetween(str, String.valueOf(time.getTime()), String.valueOf(time2.getTime()));
    }

    public OrmPredicates notBetween(String str, Timestamp timestamp, Timestamp timestamp2) {
        return notBetween(str, String.valueOf(timestamp.getTime()), String.valueOf(timestamp2.getTime()));
    }

    public OrmPredicates notBetween(String str, Calendar calendar, Calendar calendar2) {
        return notBetween(str, String.valueOf(calendar.getTimeInMillis()), String.valueOf(calendar2.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates notBetween(String str, String str2, String str3) {
        return (OrmPredicates) super.notBetween(str, str2, str3);
    }

    public OrmPredicates greaterThan(String str, int i) {
        return greaterThan(str, String.valueOf(i));
    }

    public OrmPredicates greaterThan(String str, long j) {
        return greaterThan(str, String.valueOf(j));
    }

    public OrmPredicates greaterThan(String str, double d) {
        return greaterThan(str, String.valueOf(d));
    }

    public OrmPredicates greaterThan(String str, float f) {
        return greaterThan(str, String.valueOf(f));
    }

    public OrmPredicates greaterThan(String str, Date date) {
        return greaterThan(str, String.valueOf(date.getTime()));
    }

    public OrmPredicates greaterThan(String str, Time time) {
        return greaterThan(str, String.valueOf(time.getTime()));
    }

    public OrmPredicates greaterThan(String str, Timestamp timestamp) {
        return greaterThan(str, String.valueOf(timestamp.getTime()));
    }

    public OrmPredicates greaterThan(String str, Calendar calendar) {
        return greaterThan(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates greaterThan(String str, String str2) {
        return (OrmPredicates) super.greaterThan(str, str2);
    }

    public OrmPredicates lessThan(String str, int i) {
        return lessThan(str, String.valueOf(i));
    }

    public OrmPredicates lessThan(String str, long j) {
        return lessThan(str, String.valueOf(j));
    }

    public OrmPredicates lessThan(String str, double d) {
        return lessThan(str, String.valueOf(d));
    }

    public OrmPredicates lessThan(String str, float f) {
        return lessThan(str, String.valueOf(f));
    }

    public OrmPredicates lessThan(String str, Date date) {
        return lessThan(str, String.valueOf(date.getTime()));
    }

    public OrmPredicates lessThan(String str, Time time) {
        return lessThan(str, String.valueOf(time.getTime()));
    }

    public OrmPredicates lessThan(String str, Timestamp timestamp) {
        return lessThan(str, String.valueOf(timestamp.getTime()));
    }

    public OrmPredicates lessThan(String str, Calendar calendar) {
        return lessThan(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates lessThan(String str, String str2) {
        return (OrmPredicates) super.lessThan(str, str2);
    }

    public OrmPredicates greaterThanOrEqualTo(String str, int i) {
        return greaterThanOrEqualTo(str, String.valueOf(i));
    }

    public OrmPredicates greaterThanOrEqualTo(String str, long j) {
        return greaterThanOrEqualTo(str, String.valueOf(j));
    }

    public OrmPredicates greaterThanOrEqualTo(String str, double d) {
        return greaterThanOrEqualTo(str, String.valueOf(d));
    }

    public OrmPredicates greaterThanOrEqualTo(String str, float f) {
        return greaterThanOrEqualTo(str, String.valueOf(f));
    }

    public OrmPredicates greaterThanOrEqualTo(String str, Date date) {
        return greaterThanOrEqualTo(str, String.valueOf(date.getTime()));
    }

    public OrmPredicates greaterThanOrEqualTo(String str, Time time) {
        return greaterThanOrEqualTo(str, String.valueOf(time.getTime()));
    }

    public OrmPredicates greaterThanOrEqualTo(String str, Timestamp timestamp) {
        return greaterThanOrEqualTo(str, String.valueOf(timestamp.getTime()));
    }

    public OrmPredicates greaterThanOrEqualTo(String str, Calendar calendar) {
        return greaterThanOrEqualTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates greaterThanOrEqualTo(String str, String str2) {
        return (OrmPredicates) super.greaterThanOrEqualTo(str, str2);
    }

    public OrmPredicates lessThanOrEqualTo(String str, int i) {
        return lessThanOrEqualTo(str, String.valueOf(i));
    }

    public OrmPredicates lessThanOrEqualTo(String str, long j) {
        return lessThanOrEqualTo(str, String.valueOf(j));
    }

    public OrmPredicates lessThanOrEqualTo(String str, double d) {
        return lessThanOrEqualTo(str, String.valueOf(d));
    }

    public OrmPredicates lessThanOrEqualTo(String str, float f) {
        return lessThanOrEqualTo(str, String.valueOf(f));
    }

    public OrmPredicates lessThanOrEqualTo(String str, Date date) {
        return lessThanOrEqualTo(str, String.valueOf(date.getTime()));
    }

    public OrmPredicates lessThanOrEqualTo(String str, Time time) {
        return lessThanOrEqualTo(str, String.valueOf(time.getTime()));
    }

    public OrmPredicates lessThanOrEqualTo(String str, Timestamp timestamp) {
        return lessThanOrEqualTo(str, String.valueOf(timestamp.getTime()));
    }

    public OrmPredicates lessThanOrEqualTo(String str, Calendar calendar) {
        return lessThanOrEqualTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates lessThanOrEqualTo(String str, String str2) {
        return (OrmPredicates) super.lessThanOrEqualTo(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates orderByAsc(String str) {
        return (OrmPredicates) super.orderByAsc(str);
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates orderByDesc(String str) {
        return (OrmPredicates) super.orderByDesc(str);
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates distinct() {
        return (OrmPredicates) super.distinct();
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates limit(int i) {
        return (OrmPredicates) super.limit(i);
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates offset(int i) {
        return (OrmPredicates) super.offset(i);
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates groupBy(String[] strArr) {
        return (OrmPredicates) super.groupBy(strArr);
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates indexedBy(String str) {
        return (OrmPredicates) super.indexedBy(str);
    }

    public OrmPredicates in(String str, int[] iArr) {
        if (iArr == null || iArr.length == 0) {
            throw new IllegalPredicateException("OrmPredicates: in() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(iArr.length);
        for (int i : iArr) {
            arrayList.add(String.valueOf(i));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates in(String str, long[] jArr) {
        if (jArr == null || jArr.length == 0) {
            throw new IllegalPredicateException("OrmPredicates: in() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(jArr.length);
        for (long j : jArr) {
            arrayList.add(String.valueOf(j));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates in(String str, double[] dArr) {
        if (dArr == null || dArr.length == 0) {
            throw new IllegalPredicateException("OrmPredicates: in() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(dArr.length);
        for (double d : dArr) {
            arrayList.add(String.valueOf(d));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates in(String str, float[] fArr) {
        if (fArr == null || fArr.length == 0) {
            throw new IllegalPredicateException("OrmPredicates: in() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(fArr.length);
        for (float f : fArr) {
            arrayList.add(String.valueOf(f));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates in(String str, Date[] dateArr) {
        checkParamsValid(dateArr);
        ArrayList arrayList = new ArrayList(dateArr.length);
        for (Date date : dateArr) {
            arrayList.add(String.valueOf(date.getTime()));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates in(String str, Time[] timeArr) {
        checkParamsValid(timeArr);
        ArrayList arrayList = new ArrayList(timeArr.length);
        for (Time time : timeArr) {
            arrayList.add(String.valueOf(time.getTime()));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates in(String str, Timestamp[] timestampArr) {
        checkParamsValid(timestampArr);
        ArrayList arrayList = new ArrayList(timestampArr.length);
        for (Timestamp timestamp : timestampArr) {
            arrayList.add(String.valueOf(timestamp.getTime()));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates in(String str, Calendar[] calendarArr) {
        checkParamsValid(calendarArr);
        ArrayList arrayList = new ArrayList(calendarArr.length);
        for (Calendar calendar : calendarArr) {
            arrayList.add(String.valueOf(calendar.getTimeInMillis()));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates in(String str, String[] strArr) {
        return (OrmPredicates) super.in(str, strArr);
    }

    public OrmPredicates notIn(String str, int[] iArr) {
        if (iArr == null || iArr.length == 0) {
            throw new IllegalPredicateException("OrmPredicates: notIn() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(iArr.length);
        for (int i : iArr) {
            arrayList.add(String.valueOf(i));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates notIn(String str, long[] jArr) {
        if (jArr == null || jArr.length == 0) {
            throw new IllegalPredicateException("OrmPredicates: notIn() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(jArr.length);
        for (long j : jArr) {
            arrayList.add(String.valueOf(j));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates notIn(String str, double[] dArr) {
        if (dArr == null || dArr.length == 0) {
            throw new IllegalPredicateException("OrmPredicates: notIn() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(dArr.length);
        for (double d : dArr) {
            arrayList.add(String.valueOf(d));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates notIn(String str, float[] fArr) {
        if (fArr == null || fArr.length == 0) {
            throw new IllegalPredicateException("OrmPredicates: notIn() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(fArr.length);
        for (float f : fArr) {
            arrayList.add(String.valueOf(f));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates notIn(String str, Date[] dateArr) {
        checkParamsValid(dateArr);
        ArrayList arrayList = new ArrayList(dateArr.length);
        for (Date date : dateArr) {
            arrayList.add(String.valueOf(date.getTime()));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates notIn(String str, Time[] timeArr) {
        checkParamsValid(timeArr);
        ArrayList arrayList = new ArrayList(timeArr.length);
        for (Time time : timeArr) {
            arrayList.add(String.valueOf(time.getTime()));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates notIn(String str, Timestamp[] timestampArr) {
        checkParamsValid(timestampArr);
        ArrayList arrayList = new ArrayList(timestampArr.length);
        for (Timestamp timestamp : timestampArr) {
            arrayList.add(String.valueOf(timestamp.getTime()));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public OrmPredicates notIn(String str, Calendar[] calendarArr) {
        checkParamsValid(calendarArr);
        ArrayList arrayList = new ArrayList(calendarArr.length);
        for (Calendar calendar : calendarArr) {
            arrayList.add(String.valueOf(calendar.getTimeInMillis()));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    @Override // ohos.data.AbsPredicates
    public OrmPredicates notIn(String str, String[] strArr) {
        return (OrmPredicates) super.notIn(str, strArr);
    }

    public <J extends OrmObject> OrmPredicates crossJoin(Class<J> cls) {
        return join(AbsPredicates.JoinType.CROSS, cls);
    }

    public <J extends OrmObject> OrmPredicates innerJoin(Class<J> cls) {
        return join(AbsPredicates.JoinType.INNER, cls);
    }

    public <J extends OrmObject> OrmPredicates leftOuterJoin(Class<J> cls) {
        return join(AbsPredicates.JoinType.LEFT, cls);
    }

    public OrmPredicates using(String... strArr) {
        if (strArr == null || strArr.length == 0) {
            throw new IllegalPredicateException("Predicates using failed : fields is null.");
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

    public OrmPredicates on(String... strArr) {
        if (strArr == null || strArr.length == 0) {
            throw new IllegalPredicateException("Predicates on failed : clauses is null.");
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

    private <J extends OrmObject> OrmPredicates join(AbsPredicates.JoinType joinType, Class<J> cls) {
        if (cls != null) {
            this.joinTypes.add(joinType.grammar());
            this.joinEntityNames.add(cls.getName());
            this.joinCount++;
            return this;
        }
        throw new IllegalPredicateException("Predicates join failed: clz is null.");
    }

    private <T> void checkParamsValid(T[] tArr) {
        if (tArr == null || tArr.length == 0) {
            throw new IllegalPredicateException("RdbPredicates: predicates operation failsbecause values can't be null.");
        }
    }
}
