package com.huawei.nb.query;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.huawei.nb.utils.logger.DSLog;
import com.huawei.odmf.core.ManagedObject;
import com.huawei.odmf.exception.ODMFException;
import com.huawei.odmf.predicate.FetchRequest;
import com.huawei.odmf.user.AQueryImpl;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public final class Query implements IQuery {
    public static final Parcelable.Creator<Query> CREATOR = new Parcelable.Creator<Query>() {
        /* class com.huawei.nb.query.Query.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Query createFromParcel(Parcel parcel) {
            return new Query(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Query[] newArray(int i) {
            return new Query[i];
        }
    };
    private int aggregateType;
    private FetchRequest fetchRequest;
    private AQueryImpl queryImpl;
    private boolean valid;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private Query(FetchRequest fetchRequest2) {
        init(fetchRequest2);
    }

    protected Query(Parcel parcel) {
        init((FetchRequest) parcel.readParcelable(FetchRequest.class.getClassLoader()));
    }

    private void init(FetchRequest fetchRequest2) {
        if (fetchRequest2 != null) {
            this.fetchRequest = fetchRequest2;
            this.queryImpl = new AQueryImpl(fetchRequest2.getEntityName(), this.fetchRequest, null);
            this.valid = true;
            return;
        }
        this.fetchRequest = new FetchRequest();
        this.queryImpl = new AQueryImpl("", this.fetchRequest, null);
        this.valid = false;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(getFetchRequest(), i);
    }

    public static <T extends ManagedObject> Query select(Class<T> cls) {
        if (cls == null) {
            return new Query((FetchRequest) null);
        }
        return new Query(new FetchRequest(cls.getName(), cls));
    }

    @Override // com.huawei.nb.query.IQuery
    public boolean isValid() {
        return this.valid;
    }

    @Override // com.huawei.nb.query.IQuery
    public String getEntityName() {
        return getFetchRequest().getEntityName();
    }

    public FetchRequest getFetchRequest() {
        if (this.fetchRequest.getSelectionArgs() == null || this.fetchRequest.getSelectionArgs().length == 0) {
            return this.queryImpl.getFetchRequest();
        }
        return this.fetchRequest;
    }

    public Query isNull(String str) {
        if (TextUtils.isEmpty(str)) {
            this.valid = false;
            return this;
        }
        this.queryImpl.isNull(str);
        return this;
    }

    public Query isNotNull(String str) {
        if (TextUtils.isEmpty(str)) {
            this.valid = false;
            return this;
        }
        this.queryImpl.isNotNull(str);
        return this;
    }

    private Query fieldEqualTo(String str, Object obj) {
        if (TextUtils.isEmpty(str) || obj == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.equalTo(str, String.valueOf(obj));
        return this;
    }

    public Query equalTo(String str, String str2) {
        return fieldEqualTo(str, str2);
    }

    public Query equalTo(String str, Byte b) {
        return fieldEqualTo(str, b);
    }

    public Query equalTo(String str, Short sh) {
        return fieldEqualTo(str, sh);
    }

    public Query equalTo(String str, Integer num) {
        return fieldEqualTo(str, num);
    }

    public Query equalTo(String str, Long l) {
        return fieldEqualTo(str, l);
    }

    public Query equalTo(String str, Double d) {
        return fieldEqualTo(str, d);
    }

    public Query equalTo(String str, Float f) {
        return fieldEqualTo(str, f);
    }

    public Query equalTo(String str, Boolean bool) {
        if (TextUtils.isEmpty(str) || bool == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.equalTo(str, bool);
        return this;
    }

    public Query equalTo(String str, Date date) {
        if (TextUtils.isEmpty(str) || date == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.equalTo(str, date);
        return this;
    }

    public Query equalTo(String str, Time time) {
        if (TextUtils.isEmpty(str) || time == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.equalTo(str, time);
        return this;
    }

    public Query equalTo(String str, Timestamp timestamp) {
        if (TextUtils.isEmpty(str) || timestamp == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.equalTo(str, timestamp);
        return this;
    }

    public Query equalTo(String str, Calendar calendar) {
        if (TextUtils.isEmpty(str) || calendar == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.equalTo(str, calendar);
        return this;
    }

    private Query fieldNotEqualTo(String str, Object obj) {
        if (TextUtils.isEmpty(str) || obj == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.notEqualTo(str, String.valueOf(obj));
        return this;
    }

    public Query notEqualTo(String str, String str2) {
        return fieldNotEqualTo(str, str2);
    }

    public Query notEqualTo(String str, Byte b) {
        return fieldNotEqualTo(str, b);
    }

    public Query notEqualTo(String str, Short sh) {
        return fieldNotEqualTo(str, sh);
    }

    public Query notEqualTo(String str, Integer num) {
        return fieldNotEqualTo(str, num);
    }

    public Query notEqualTo(String str, Long l) {
        return fieldNotEqualTo(str, l);
    }

    public Query notEqualTo(String str, Double d) {
        return fieldNotEqualTo(str, d);
    }

    public Query notEqualTo(String str, Float f) {
        return fieldNotEqualTo(str, f);
    }

    public Query notEqualTo(String str, Boolean bool) {
        if (TextUtils.isEmpty(str) || bool == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.notEqualTo(str, bool);
        return this;
    }

    public Query notEqualTo(String str, Date date) {
        if (TextUtils.isEmpty(str) || date == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.notEqualTo(str, date);
        return this;
    }

    public Query notEqualTo(String str, Time time) {
        if (TextUtils.isEmpty(str) || time == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.notEqualTo(str, time);
        return this;
    }

    public Query notEqualTo(String str, Timestamp timestamp) {
        if (TextUtils.isEmpty(str) || timestamp == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.notEqualTo(str, timestamp);
        return this;
    }

    public Query notEqualTo(String str, Calendar calendar) {
        if (TextUtils.isEmpty(str) || calendar == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.notEqualTo(str, calendar);
        return this;
    }

    private Query fieldGreaterThan(String str, Object obj) {
        if (TextUtils.isEmpty(str) || obj == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.greaterThan(str, String.valueOf(obj));
        return this;
    }

    public Query greaterThan(String str, String str2) {
        return fieldGreaterThan(str, str2);
    }

    public Query greaterThan(String str, Integer num) {
        return fieldGreaterThan(str, num);
    }

    public Query greaterThan(String str, Long l) {
        return fieldGreaterThan(str, l);
    }

    public Query greaterThan(String str, Double d) {
        return fieldGreaterThan(str, d);
    }

    public Query greaterThan(String str, Float f) {
        return fieldGreaterThan(str, f);
    }

    public Query greaterThan(String str, Date date) {
        if (TextUtils.isEmpty(str) || date == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.greaterThan(str, date);
        return this;
    }

    public Query greaterThan(String str, Time time) {
        if (TextUtils.isEmpty(str) || time == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.greaterThan(str, time);
        return this;
    }

    public Query greaterThan(String str, Timestamp timestamp) {
        if (TextUtils.isEmpty(str) || timestamp == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.greaterThan(str, timestamp);
        return this;
    }

    public Query greaterThan(String str, Calendar calendar) {
        if (TextUtils.isEmpty(str) || calendar == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.greaterThan(str, calendar);
        return this;
    }

    private Query fieldGreaterThanOrEqualTo(String str, Object obj) {
        if (TextUtils.isEmpty(str) || obj == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.greaterThanOrEqualTo(str, String.valueOf(obj));
        return this;
    }

    public Query greaterThanOrEqualTo(String str, String str2) {
        return fieldGreaterThanOrEqualTo(str, str2);
    }

    public Query greaterThanOrEqualTo(String str, Integer num) {
        return fieldGreaterThanOrEqualTo(str, num);
    }

    public Query greaterThanOrEqualTo(String str, Long l) {
        return fieldGreaterThanOrEqualTo(str, l);
    }

    public Query greaterThanOrEqualTo(String str, Double d) {
        return fieldGreaterThanOrEqualTo(str, d);
    }

    public Query greaterThanOrEqualTo(String str, Float f) {
        return fieldGreaterThanOrEqualTo(str, f);
    }

    public Query greaterThanOrEqualTo(String str, Date date) {
        if (TextUtils.isEmpty(str) || date == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.greaterThanOrEqualTo(str, date);
        return this;
    }

    public Query greaterThanOrEqualTo(String str, Time time) {
        if (TextUtils.isEmpty(str) || time == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.greaterThanOrEqualTo(str, time);
        return this;
    }

    public Query greaterThanOrEqualTo(String str, Timestamp timestamp) {
        if (TextUtils.isEmpty(str) || timestamp == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.greaterThanOrEqualTo(str, timestamp);
        return this;
    }

    public Query greaterThanOrEqualTo(String str, Calendar calendar) {
        if (TextUtils.isEmpty(str) || calendar == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.greaterThanOrEqualTo(str, calendar);
        return this;
    }

    private Query fieldLessThan(String str, Object obj) {
        if (TextUtils.isEmpty(str) || obj == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.lessThan(str, String.valueOf(obj));
        return this;
    }

    public Query lessThan(String str, String str2) {
        return fieldLessThan(str, str2);
    }

    public Query lessThan(String str, Integer num) {
        return fieldLessThan(str, num);
    }

    public Query lessThan(String str, Long l) {
        return fieldLessThan(str, l);
    }

    public Query lessThan(String str, Double d) {
        return fieldLessThan(str, d);
    }

    public Query lessThan(String str, Float f) {
        return fieldLessThan(str, f);
    }

    public Query lessThan(String str, Date date) {
        if (TextUtils.isEmpty(str) || date == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.lessThan(str, date);
        return this;
    }

    public Query lessThan(String str, Time time) {
        if (TextUtils.isEmpty(str) || time == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.lessThan(str, time);
        return this;
    }

    public Query lessThan(String str, Timestamp timestamp) {
        if (TextUtils.isEmpty(str) || timestamp == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.lessThan(str, timestamp);
        return this;
    }

    public Query lessThan(String str, Calendar calendar) {
        if (TextUtils.isEmpty(str) || calendar == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.lessThan(str, calendar);
        return this;
    }

    private Query fieldLessThanOrEqualTo(String str, Object obj) {
        if (TextUtils.isEmpty(str) || obj == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.lessThanOrEqualTo(str, String.valueOf(obj));
        return this;
    }

    public Query lessThanOrEqualTo(String str, String str2) {
        return fieldLessThanOrEqualTo(str, str2);
    }

    public Query lessThanOrEqualTo(String str, Integer num) {
        return fieldLessThanOrEqualTo(str, num);
    }

    public Query lessThanOrEqualTo(String str, Long l) {
        return fieldLessThanOrEqualTo(str, l);
    }

    public Query lessThanOrEqualTo(String str, Double d) {
        return fieldLessThanOrEqualTo(str, d);
    }

    public Query lessThanOrEqualTo(String str, Float f) {
        return fieldLessThanOrEqualTo(str, f);
    }

    public Query lessThanOrEqualTo(String str, Date date) {
        if (TextUtils.isEmpty(str) || date == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.lessThanOrEqualTo(str, date);
        return this;
    }

    public Query lessThanOrEqualTo(String str, Time time) {
        if (TextUtils.isEmpty(str) || time == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.lessThanOrEqualTo(str, time);
        return this;
    }

    public Query lessThanOrEqualTo(String str, Timestamp timestamp) {
        if (TextUtils.isEmpty(str) || timestamp == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.lessThanOrEqualTo(str, timestamp);
        return this;
    }

    public Query lessThanOrEqualTo(String str, Calendar calendar) {
        if (TextUtils.isEmpty(str) || calendar == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.lessThanOrEqualTo(str, calendar);
        return this;
    }

    private Query fieldBetween(String str, Object obj, Object obj2) {
        if (TextUtils.isEmpty(str) || obj == null || obj2 == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.between(str, String.valueOf(obj), String.valueOf(obj2));
        return this;
    }

    public Query between(String str, String str2, String str3) {
        return fieldBetween(str, str2, str3);
    }

    public Query between(String str, Integer num, Integer num2) {
        return fieldBetween(str, num, num2);
    }

    public Query between(String str, Long l, Long l2) {
        return fieldBetween(str, l, l2);
    }

    public Query between(String str, Double d, Double d2) {
        return fieldBetween(str, d, d2);
    }

    public Query between(String str, Float f, Float f2) {
        return fieldBetween(str, f, f2);
    }

    public Query between(String str, Date date, Date date2) {
        if (TextUtils.isEmpty(str) || date == null || date2 == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.between(str, date, date2);
        return this;
    }

    public Query between(String str, Time time, Time time2) {
        if (TextUtils.isEmpty(str) || time == null || time2 == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.between(str, time, time2);
        return this;
    }

    public Query between(String str, Timestamp timestamp, Timestamp timestamp2) {
        if (TextUtils.isEmpty(str) || timestamp == null || timestamp2 == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.between(str, timestamp, timestamp2);
        return this;
    }

    public Query between(String str, Calendar calendar, Calendar calendar2) {
        if (TextUtils.isEmpty(str) || calendar == null || calendar2 == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.between(str, calendar, calendar2);
        return this;
    }

    public Query contains(String str, String str2) {
        if (TextUtils.isEmpty(str) || str2 == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.contains(str, str2);
        return this;
    }

    public Query beginsWith(String str, String str2) {
        if (TextUtils.isEmpty(str) || str2 == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.beginsWith(str, str2);
        return this;
    }

    public Query endsWith(String str, String str2) {
        if (TextUtils.isEmpty(str) || str2 == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.endsWith(str, str2);
        return this;
    }

    public Query like(String str, String str2) {
        if (TextUtils.isEmpty(str) || str2 == null) {
            this.valid = false;
            return this;
        }
        this.queryImpl.like(str, str2);
        return this;
    }

    public Query beginGroup() {
        this.queryImpl.beginGroup();
        return this;
    }

    public Query endGroup() {
        try {
            this.queryImpl.endGroup();
        } catch (ODMFException e) {
            this.valid = false;
            DSLog.e(e.getMessage(), new Object[0]);
        }
        return this;
    }

    public Query or() {
        try {
            this.queryImpl.or();
        } catch (ODMFException e) {
            this.valid = false;
            DSLog.e(e.getMessage(), new Object[0]);
        }
        return this;
    }

    public Query and() {
        try {
            this.queryImpl.and();
        } catch (ODMFException e) {
            this.valid = false;
            DSLog.e(e.getMessage(), new Object[0]);
        }
        return this;
    }

    public Query orderByAsc(String str) {
        if (TextUtils.isEmpty(str)) {
            this.valid = false;
            return this;
        }
        this.queryImpl.orderByAsc(str);
        return this;
    }

    public Query orderByDesc(String str) {
        if (TextUtils.isEmpty(str)) {
            this.valid = false;
            return this;
        }
        this.queryImpl.orderByDesc(str);
        return this;
    }

    public Query limit(int i) {
        try {
            this.queryImpl.limit(i);
        } catch (ODMFException e) {
            this.valid = false;
            DSLog.e(e.getMessage(), new Object[0]);
        }
        return this;
    }

    public Query max(String str) {
        if (TextUtils.isEmpty(str)) {
            this.valid = false;
            return this;
        }
        this.aggregateType = 0;
        this.queryImpl.max(str);
        return this;
    }

    public Query min(String str) {
        if (TextUtils.isEmpty(str)) {
            this.valid = false;
            return this;
        }
        this.aggregateType = 1;
        this.queryImpl.min(str);
        return this;
    }

    public Query count(String str) {
        if (TextUtils.isEmpty(str)) {
            this.valid = false;
            return this;
        }
        this.aggregateType = 2;
        this.queryImpl.count(str);
        return this;
    }

    public Query avg(String str) {
        if (TextUtils.isEmpty(str)) {
            this.valid = false;
            return this;
        }
        this.aggregateType = 3;
        this.queryImpl.avg(str);
        return this;
    }

    public Query sum(String str) {
        if (TextUtils.isEmpty(str)) {
            this.valid = false;
            return this;
        }
        this.aggregateType = 4;
        this.queryImpl.sum(str);
        return this;
    }

    public int getAggregateType() {
        return this.aggregateType;
    }
}
