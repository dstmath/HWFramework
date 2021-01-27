package ohos.data.dataability;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import ohos.data.AbsPredicates;
import ohos.data.IllegalPredicateException;
import ohos.data.PredicatesUtils;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class DataAbilityPredicates extends AbsPredicates implements Sequenceable {
    private static final int INVALID_OBJECT_FLAG = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109536, "DataAbilityPredicates");
    private static final int VALID_OBJECT_FLAG = 1;
    private boolean isRawSelection;

    public boolean unmarshalling(Parcel parcel) {
        return true;
    }

    public DataAbilityPredicates() {
        this.isRawSelection = false;
    }

    public DataAbilityPredicates(String str) {
        super.setWhereClause(str);
        this.isRawSelection = true;
    }

    public DataAbilityPredicates(Parcel parcel) {
        ArrayList arrayList;
        if (parcel != null) {
            this.isRawSelection = parcel.readBoolean();
            Integer num = null;
            String readString = parcel.readInt() != 0 ? parcel.readString() : null;
            if (parcel.readInt() != 0) {
                arrayList = new ArrayList(Arrays.asList(parcel.readStringArray()));
            } else {
                arrayList = new ArrayList(8);
            }
            boolean readBoolean = parcel.readBoolean();
            String readString2 = parcel.readInt() != 0 ? parcel.readString() : null;
            String readString3 = parcel.readInt() != 0 ? parcel.readString() : null;
            String readString4 = parcel.readInt() != 0 ? parcel.readString() : null;
            Integer valueOf = parcel.readInt() != 0 ? Integer.valueOf(parcel.readInt()) : null;
            num = parcel.readInt() != 0 ? Integer.valueOf(parcel.readInt()) : num;
            PredicatesUtils.setWhereClauseAndArgs(this, readString, arrayList);
            PredicatesUtils.setAttributes(this, readBoolean, readString2, readString3, readString4, valueOf, num);
            return;
        }
        throw new IllegalPredicateException("input param source is null.");
    }

    @Override // ohos.data.AbsPredicates
    public void clear() {
        super.clear();
    }

    @Override // ohos.data.AbsPredicates
    public String getWhereClause() {
        return super.getWhereClause();
    }

    @Override // ohos.data.AbsPredicates
    public void setWhereArgs(List<String> list) {
        super.setWhereArgs(list);
    }

    @Override // ohos.data.AbsPredicates
    public void setOrder(String str) {
        super.setOrder(str);
    }

    @Override // ohos.data.AbsPredicates
    public String getOrder() {
        return super.getOrder();
    }

    @Override // ohos.data.AbsPredicates
    public List<String> getWhereArgs() {
        return super.getWhereArgs();
    }

    public DataAbilityPredicates equalTo(String str, byte b) {
        return equalTo(str, String.valueOf((int) b));
    }

    public DataAbilityPredicates equalTo(String str, short s) {
        return equalTo(str, String.valueOf((int) s));
    }

    public DataAbilityPredicates equalTo(String str, int i) {
        return equalTo(str, String.valueOf(i));
    }

    public DataAbilityPredicates equalTo(String str, long j) {
        return equalTo(str, String.valueOf(j));
    }

    public DataAbilityPredicates equalTo(String str, double d) {
        return equalTo(str, String.valueOf(d));
    }

    public DataAbilityPredicates equalTo(String str, float f) {
        return equalTo(str, String.valueOf(f));
    }

    public DataAbilityPredicates equalTo(String str, boolean z) {
        return equalTo(str, String.valueOf(z ? 1 : 0));
    }

    public DataAbilityPredicates equalTo(String str, Date date) {
        return equalTo(str, String.valueOf(date.getTime()));
    }

    public DataAbilityPredicates equalTo(String str, Time time) {
        return equalTo(str, String.valueOf(time.getTime()));
    }

    public DataAbilityPredicates equalTo(String str, Timestamp timestamp) {
        return equalTo(str, String.valueOf(timestamp.getTime()));
    }

    public DataAbilityPredicates equalTo(String str, Calendar calendar) {
        return equalTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates equalTo(String str, String str2) {
        return (DataAbilityPredicates) super.equalTo(str, str2);
    }

    public DataAbilityPredicates notEqualTo(String str, byte b) {
        return notEqualTo(str, String.valueOf((int) b));
    }

    public DataAbilityPredicates notEqualTo(String str, short s) {
        return notEqualTo(str, String.valueOf((int) s));
    }

    public DataAbilityPredicates notEqualTo(String str, int i) {
        return notEqualTo(str, String.valueOf(i));
    }

    public DataAbilityPredicates notEqualTo(String str, long j) {
        return notEqualTo(str, String.valueOf(j));
    }

    public DataAbilityPredicates notEqualTo(String str, double d) {
        return notEqualTo(str, String.valueOf(d));
    }

    public DataAbilityPredicates notEqualTo(String str, float f) {
        return notEqualTo(str, String.valueOf(f));
    }

    public DataAbilityPredicates notEqualTo(String str, boolean z) {
        return notEqualTo(str, String.valueOf(z ? 1 : 0));
    }

    public DataAbilityPredicates notEqualTo(String str, Date date) {
        return notEqualTo(str, String.valueOf(date.getTime()));
    }

    public DataAbilityPredicates notEqualTo(String str, Time time) {
        return notEqualTo(str, String.valueOf(time.getTime()));
    }

    public DataAbilityPredicates notEqualTo(String str, Timestamp timestamp) {
        return notEqualTo(str, String.valueOf(timestamp.getTime()));
    }

    public DataAbilityPredicates notEqualTo(String str, Calendar calendar) {
        return notEqualTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates notEqualTo(String str, String str2) {
        return (DataAbilityPredicates) super.notEqualTo(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates beginWrap() {
        return (DataAbilityPredicates) super.beginWrap();
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates endWrap() {
        return (DataAbilityPredicates) super.endWrap();
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates or() {
        return (DataAbilityPredicates) super.or();
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates and() {
        return (DataAbilityPredicates) super.and();
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates contains(String str, String str2) {
        return (DataAbilityPredicates) super.contains(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates beginsWith(String str, String str2) {
        return (DataAbilityPredicates) super.beginsWith(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates endsWith(String str, String str2) {
        return (DataAbilityPredicates) super.endsWith(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates isNull(String str) {
        return (DataAbilityPredicates) super.isNull(str);
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates isNotNull(String str) {
        return (DataAbilityPredicates) super.isNotNull(str);
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates like(String str, String str2) {
        return (DataAbilityPredicates) super.like(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates glob(String str, String str2) {
        return (DataAbilityPredicates) super.glob(str, str2);
    }

    public DataAbilityPredicates between(String str, int i, int i2) {
        return between(str, String.valueOf(i), String.valueOf(i2));
    }

    public DataAbilityPredicates between(String str, long j, long j2) {
        return between(str, String.valueOf(j), String.valueOf(j2));
    }

    public DataAbilityPredicates between(String str, double d, double d2) {
        return between(str, String.valueOf(d), String.valueOf(d2));
    }

    public DataAbilityPredicates between(String str, float f, float f2) {
        return between(str, String.valueOf(f), String.valueOf(f2));
    }

    public DataAbilityPredicates between(String str, Date date, Date date2) {
        return between(str, String.valueOf(date.getTime()), String.valueOf(date2.getTime()));
    }

    public DataAbilityPredicates between(String str, Time time, Time time2) {
        return between(str, String.valueOf(time.getTime()), String.valueOf(time2.getTime()));
    }

    public DataAbilityPredicates between(String str, Timestamp timestamp, Timestamp timestamp2) {
        return between(str, String.valueOf(timestamp.getTime()), String.valueOf(timestamp2.getTime()));
    }

    public DataAbilityPredicates between(String str, Calendar calendar, Calendar calendar2) {
        return between(str, String.valueOf(calendar.getTimeInMillis()), String.valueOf(calendar2.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates between(String str, String str2, String str3) {
        return (DataAbilityPredicates) super.between(str, str2, str3);
    }

    public DataAbilityPredicates notBetween(String str, int i, int i2) {
        return notBetween(str, String.valueOf(i), String.valueOf(i2));
    }

    public DataAbilityPredicates notBetween(String str, long j, long j2) {
        return notBetween(str, String.valueOf(j), String.valueOf(j2));
    }

    public DataAbilityPredicates notBetween(String str, double d, double d2) {
        return notBetween(str, String.valueOf(d), String.valueOf(d2));
    }

    public DataAbilityPredicates notBetween(String str, float f, float f2) {
        return notBetween(str, String.valueOf(f), String.valueOf(f2));
    }

    public DataAbilityPredicates notBetween(String str, Date date, Date date2) {
        return notBetween(str, String.valueOf(date.getTime()), String.valueOf(date2.getTime()));
    }

    public DataAbilityPredicates notBetween(String str, Time time, Time time2) {
        return notBetween(str, String.valueOf(time.getTime()), String.valueOf(time2.getTime()));
    }

    public DataAbilityPredicates notBetween(String str, Timestamp timestamp, Timestamp timestamp2) {
        return notBetween(str, String.valueOf(timestamp.getTime()), String.valueOf(timestamp2.getTime()));
    }

    public DataAbilityPredicates notBetween(String str, Calendar calendar, Calendar calendar2) {
        return notBetween(str, String.valueOf(calendar.getTimeInMillis()), String.valueOf(calendar2.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates notBetween(String str, String str2, String str3) {
        return (DataAbilityPredicates) super.notBetween(str, str2, str3);
    }

    public DataAbilityPredicates greaterThan(String str, int i) {
        return greaterThan(str, String.valueOf(i));
    }

    public DataAbilityPredicates greaterThan(String str, long j) {
        return greaterThan(str, String.valueOf(j));
    }

    public DataAbilityPredicates greaterThan(String str, double d) {
        return greaterThan(str, String.valueOf(d));
    }

    public DataAbilityPredicates greaterThan(String str, float f) {
        return greaterThan(str, String.valueOf(f));
    }

    public DataAbilityPredicates greaterThan(String str, Date date) {
        return greaterThan(str, String.valueOf(date.getTime()));
    }

    public DataAbilityPredicates greaterThan(String str, Time time) {
        return greaterThan(str, String.valueOf(time.getTime()));
    }

    public DataAbilityPredicates greaterThan(String str, Timestamp timestamp) {
        return greaterThan(str, String.valueOf(timestamp.getTime()));
    }

    public DataAbilityPredicates greaterThan(String str, Calendar calendar) {
        return greaterThan(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates greaterThan(String str, String str2) {
        return (DataAbilityPredicates) super.greaterThan(str, str2);
    }

    public DataAbilityPredicates lessThan(String str, int i) {
        return lessThan(str, String.valueOf(i));
    }

    public DataAbilityPredicates lessThan(String str, long j) {
        return lessThan(str, String.valueOf(j));
    }

    public DataAbilityPredicates lessThan(String str, double d) {
        return lessThan(str, String.valueOf(d));
    }

    public DataAbilityPredicates lessThan(String str, float f) {
        return lessThan(str, String.valueOf(f));
    }

    public DataAbilityPredicates lessThan(String str, Date date) {
        return lessThan(str, String.valueOf(date.getTime()));
    }

    public DataAbilityPredicates lessThan(String str, Time time) {
        return lessThan(str, String.valueOf(time.getTime()));
    }

    public DataAbilityPredicates lessThan(String str, Timestamp timestamp) {
        return lessThan(str, String.valueOf(timestamp.getTime()));
    }

    public DataAbilityPredicates lessThan(String str, Calendar calendar) {
        return lessThan(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates lessThan(String str, String str2) {
        return (DataAbilityPredicates) super.lessThan(str, str2);
    }

    public DataAbilityPredicates greaterThanOrEqualTo(String str, int i) {
        return greaterThanOrEqualTo(str, String.valueOf(i));
    }

    public DataAbilityPredicates greaterThanOrEqualTo(String str, long j) {
        return greaterThanOrEqualTo(str, String.valueOf(j));
    }

    public DataAbilityPredicates greaterThanOrEqualTo(String str, double d) {
        return greaterThanOrEqualTo(str, String.valueOf(d));
    }

    public DataAbilityPredicates greaterThanOrEqualTo(String str, float f) {
        return greaterThanOrEqualTo(str, String.valueOf(f));
    }

    public DataAbilityPredicates greaterThanOrEqualTo(String str, Date date) {
        return greaterThanOrEqualTo(str, String.valueOf(date.getTime()));
    }

    public DataAbilityPredicates greaterThanOrEqualTo(String str, Time time) {
        return greaterThanOrEqualTo(str, String.valueOf(time.getTime()));
    }

    public DataAbilityPredicates greaterThanOrEqualTo(String str, Timestamp timestamp) {
        return greaterThanOrEqualTo(str, String.valueOf(timestamp.getTime()));
    }

    public DataAbilityPredicates greaterThanOrEqualTo(String str, Calendar calendar) {
        return greaterThanOrEqualTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates greaterThanOrEqualTo(String str, String str2) {
        return (DataAbilityPredicates) super.greaterThanOrEqualTo(str, str2);
    }

    public DataAbilityPredicates lessThanOrEqualTo(String str, int i) {
        return lessThanOrEqualTo(str, String.valueOf(i));
    }

    public DataAbilityPredicates lessThanOrEqualTo(String str, long j) {
        return lessThanOrEqualTo(str, String.valueOf(j));
    }

    public DataAbilityPredicates lessThanOrEqualTo(String str, double d) {
        return lessThanOrEqualTo(str, String.valueOf(d));
    }

    public DataAbilityPredicates lessThanOrEqualTo(String str, float f) {
        return lessThanOrEqualTo(str, String.valueOf(f));
    }

    public DataAbilityPredicates lessThanOrEqualTo(String str, Date date) {
        return lessThanOrEqualTo(str, String.valueOf(date.getTime()));
    }

    public DataAbilityPredicates lessThanOrEqualTo(String str, Time time) {
        return lessThanOrEqualTo(str, String.valueOf(time.getTime()));
    }

    public DataAbilityPredicates lessThanOrEqualTo(String str, Timestamp timestamp) {
        return lessThanOrEqualTo(str, String.valueOf(timestamp.getTime()));
    }

    public DataAbilityPredicates lessThanOrEqualTo(String str, Calendar calendar) {
        return lessThanOrEqualTo(str, String.valueOf(calendar.getTimeInMillis()));
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates lessThanOrEqualTo(String str, String str2) {
        return (DataAbilityPredicates) super.lessThanOrEqualTo(str, str2);
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates orderByAsc(String str) {
        return (DataAbilityPredicates) super.orderByAsc(str);
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates orderByDesc(String str) {
        return (DataAbilityPredicates) super.orderByDesc(str);
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates distinct() {
        return (DataAbilityPredicates) super.distinct();
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates limit(int i) {
        return (DataAbilityPredicates) super.limit(i);
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates offset(int i) {
        return (DataAbilityPredicates) super.offset(i);
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates groupBy(String[] strArr) {
        return (DataAbilityPredicates) super.groupBy(strArr);
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates indexedBy(String str) {
        return (DataAbilityPredicates) super.indexedBy(str);
    }

    public DataAbilityPredicates in(String str, int[] iArr) {
        if (iArr == null || iArr.length == 0) {
            throw new IllegalPredicateException("DataAbilityPredicates: in() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(iArr.length);
        for (int i : iArr) {
            arrayList.add(String.valueOf(i));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates in(String str, long[] jArr) {
        if (jArr == null || jArr.length == 0) {
            throw new IllegalPredicateException("DataAbilityPredicates: in() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(jArr.length);
        for (long j : jArr) {
            arrayList.add(String.valueOf(j));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates in(String str, double[] dArr) {
        if (dArr == null || dArr.length == 0) {
            throw new IllegalPredicateException("DataAbilityPredicates: in() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(dArr.length);
        for (double d : dArr) {
            arrayList.add(String.valueOf(d));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates in(String str, float[] fArr) {
        if (fArr == null || fArr.length == 0) {
            throw new IllegalPredicateException("DataAbilityPredicates: in() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(fArr.length);
        for (float f : fArr) {
            arrayList.add(String.valueOf(f));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates in(String str, Date[] dateArr) {
        checkParamsValid(dateArr);
        ArrayList arrayList = new ArrayList(dateArr.length);
        for (Date date : dateArr) {
            arrayList.add(String.valueOf(date.getTime()));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates in(String str, Time[] timeArr) {
        checkParamsValid(timeArr);
        ArrayList arrayList = new ArrayList(timeArr.length);
        for (Time time : timeArr) {
            arrayList.add(String.valueOf(time.getTime()));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates in(String str, Timestamp[] timestampArr) {
        checkParamsValid(timestampArr);
        ArrayList arrayList = new ArrayList(timestampArr.length);
        for (Timestamp timestamp : timestampArr) {
            arrayList.add(String.valueOf(timestamp.getTime()));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates in(String str, Calendar[] calendarArr) {
        checkParamsValid(calendarArr);
        ArrayList arrayList = new ArrayList(calendarArr.length);
        for (Calendar calendar : calendarArr) {
            arrayList.add(String.valueOf(calendar.getTimeInMillis()));
        }
        return in(str, (String[]) arrayList.toArray(new String[0]));
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates in(String str, String[] strArr) {
        return (DataAbilityPredicates) super.in(str, strArr);
    }

    public DataAbilityPredicates notIn(String str, int[] iArr) {
        if (iArr == null || iArr.length == 0) {
            throw new IllegalPredicateException("DataAbilityPredicates: notIn() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(iArr.length);
        for (int i : iArr) {
            arrayList.add(String.valueOf(i));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates notIn(String str, long[] jArr) {
        if (jArr == null || jArr.length == 0) {
            throw new IllegalPredicateException("DataAbilityPredicates: notIn() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(jArr.length);
        for (long j : jArr) {
            arrayList.add(String.valueOf(j));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates notIn(String str, double[] dArr) {
        if (dArr == null || dArr.length == 0) {
            throw new IllegalPredicateException("DataAbilityPredicates: notIn() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(dArr.length);
        for (double d : dArr) {
            arrayList.add(String.valueOf(d));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates notIn(String str, float[] fArr) {
        if (fArr == null || fArr.length == 0) {
            throw new IllegalPredicateException("DataAbilityPredicates: notIn() fails because values can't be null.");
        }
        ArrayList arrayList = new ArrayList(fArr.length);
        for (float f : fArr) {
            arrayList.add(String.valueOf(f));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates notIn(String str, Date[] dateArr) {
        checkParamsValid(dateArr);
        ArrayList arrayList = new ArrayList(dateArr.length);
        for (Date date : dateArr) {
            arrayList.add(String.valueOf(date.getTime()));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates notIn(String str, Time[] timeArr) {
        checkParamsValid(timeArr);
        ArrayList arrayList = new ArrayList(timeArr.length);
        for (Time time : timeArr) {
            arrayList.add(String.valueOf(time.getTime()));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates notIn(String str, Timestamp[] timestampArr) {
        checkParamsValid(timestampArr);
        ArrayList arrayList = new ArrayList(timestampArr.length);
        for (Timestamp timestamp : timestampArr) {
            arrayList.add(String.valueOf(timestamp.getTime()));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    public DataAbilityPredicates notIn(String str, Calendar[] calendarArr) {
        checkParamsValid(calendarArr);
        ArrayList arrayList = new ArrayList(calendarArr.length);
        for (Calendar calendar : calendarArr) {
            arrayList.add(String.valueOf(calendar.getTimeInMillis()));
        }
        return notIn(str, (String[]) arrayList.toArray(new String[0]));
    }

    @Override // ohos.data.AbsPredicates
    public DataAbilityPredicates notIn(String str, String[] strArr) {
        return (DataAbilityPredicates) super.notIn(str, strArr);
    }

    public boolean isRawSelection() {
        return this.isRawSelection;
    }

    @Override // ohos.data.AbsPredicates
    public void setWhereClause(String str) {
        if (this.isRawSelection) {
            HiLog.info(LABEL, "setWhereClause is not allowed to use.", new Object[0]);
        } else {
            super.setWhereClause(str);
        }
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeBoolean(this.isRawSelection);
        marshallingString(super.getWhereClause(), parcel);
        marshallingStringList(super.getWhereArgs(), parcel);
        parcel.writeBoolean(super.isDistinct());
        marshallingString(super.getIndex(), parcel);
        marshallingString(super.getGroup(), parcel);
        marshallingString(super.getOrder(), parcel);
        Integer limit = super.getLimit();
        Integer offset = super.getOffset();
        if (limit != null) {
            parcel.writeInt(1);
            parcel.writeInt(limit.intValue());
        } else {
            parcel.writeInt(0);
        }
        if (offset != null) {
            parcel.writeInt(1);
            parcel.writeInt(offset.intValue());
        } else {
            parcel.writeInt(0);
        }
        return true;
    }

    private void marshallingString(String str, Parcel parcel) {
        if (str != null) {
            parcel.writeInt(1);
            parcel.writeString(str);
            return;
        }
        parcel.writeInt(0);
    }

    private void marshallingStringList(List<String> list, Parcel parcel) {
        if (list != null) {
            parcel.writeInt(1);
            parcel.writeStringArray((String[]) list.toArray(new String[0]));
            return;
        }
        parcel.writeInt(0);
    }

    private <T> void checkParamsValid(T[] tArr) {
        if (tArr == null || tArr.length == 0) {
            throw new IllegalPredicateException("DataAbilityPredicates: predicates operation failsbecause values can't be null.");
        }
    }
}
