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

public class Query implements IQuery {
    public static final Parcelable.Creator<Query> CREATOR = new Parcelable.Creator<Query>() {
        public Query createFromParcel(Parcel in) {
            return new Query(in);
        }

        public Query[] newArray(int size) {
            return new Query[size];
        }
    };
    private int aggregateType;
    private FetchRequest fetchRequest;
    private AQueryImpl queryImpl;
    private boolean valid;

    private Query(FetchRequest fetchRequest2) {
        init(fetchRequest2);
    }

    protected Query(Parcel in) {
        init((FetchRequest) in.readParcelable(FetchRequest.class.getClassLoader()));
    }

    private void init(FetchRequest request) {
        if (request != null) {
            this.fetchRequest = request;
            this.queryImpl = new AQueryImpl(request.getEntityName(), this.fetchRequest, null);
            this.valid = true;
            return;
        }
        this.fetchRequest = new FetchRequest();
        this.queryImpl = new AQueryImpl("", this.fetchRequest, null);
        this.valid = false;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(getFetchRequest(), flags);
    }

    public static <T extends ManagedObject> Query select(Class<T> entityClass) {
        if (entityClass == null) {
            return new Query((FetchRequest) null);
        }
        return new Query(new FetchRequest(entityClass.getName(), entityClass));
    }

    public boolean isValid() {
        return this.valid;
    }

    public String getEntityName() {
        return getFetchRequest().getEntityName();
    }

    public FetchRequest getFetchRequest() {
        if (this.fetchRequest.getSelectionArgs() == null || this.fetchRequest.getSelectionArgs().length == 0) {
            return this.queryImpl.getFetchRequest();
        }
        return this.fetchRequest;
    }

    public Query isNull(String field) {
        if (TextUtils.isEmpty(field)) {
            this.valid = false;
        } else {
            this.queryImpl.isNull(field);
        }
        return this;
    }

    public Query isNotNull(String field) {
        if (TextUtils.isEmpty(field)) {
            this.valid = false;
        } else {
            this.queryImpl.isNotNull(field);
        }
        return this;
    }

    private Query fieldEqualTo(String field, Object value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.equalTo(field, String.valueOf(value));
        }
        return this;
    }

    public Query equalTo(String field, String value) {
        return fieldEqualTo(field, value);
    }

    public Query equalTo(String field, Byte value) {
        return fieldEqualTo(field, value);
    }

    public Query equalTo(String field, Short value) {
        return fieldEqualTo(field, value);
    }

    public Query equalTo(String field, Integer value) {
        return fieldEqualTo(field, value);
    }

    public Query equalTo(String field, Long value) {
        return fieldEqualTo(field, value);
    }

    public Query equalTo(String field, Double value) {
        return fieldEqualTo(field, value);
    }

    public Query equalTo(String field, Float value) {
        return fieldEqualTo(field, value);
    }

    public Query equalTo(String field, Boolean value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.equalTo(field, value);
        }
        return this;
    }

    public Query equalTo(String field, Date value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.equalTo(field, value);
        }
        return this;
    }

    public Query equalTo(String field, Time value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.equalTo(field, value);
        }
        return this;
    }

    public Query equalTo(String field, Timestamp value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.equalTo(field, value);
        }
        return this;
    }

    public Query equalTo(String field, Calendar value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.equalTo(field, value);
        }
        return this;
    }

    private Query fieldNotEqualTo(String field, Object value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.notEqualTo(field, String.valueOf(value));
        }
        return this;
    }

    public Query notEqualTo(String field, String value) {
        return fieldNotEqualTo(field, value);
    }

    public Query notEqualTo(String field, Byte value) {
        return fieldNotEqualTo(field, value);
    }

    public Query notEqualTo(String field, Short value) {
        return fieldNotEqualTo(field, value);
    }

    public Query notEqualTo(String field, Integer value) {
        return fieldNotEqualTo(field, value);
    }

    public Query notEqualTo(String field, Long value) {
        return fieldNotEqualTo(field, value);
    }

    public Query notEqualTo(String field, Double value) {
        return fieldNotEqualTo(field, value);
    }

    public Query notEqualTo(String field, Float value) {
        return fieldNotEqualTo(field, value);
    }

    public Query notEqualTo(String field, Boolean value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.notEqualTo(field, value);
        }
        return this;
    }

    public Query notEqualTo(String field, Date value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.notEqualTo(field, value);
        }
        return this;
    }

    public Query notEqualTo(String field, Time value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.notEqualTo(field, value);
        }
        return this;
    }

    public Query notEqualTo(String field, Timestamp value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.notEqualTo(field, value);
        }
        return this;
    }

    public Query notEqualTo(String field, Calendar value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.notEqualTo(field, value);
        }
        return this;
    }

    private Query fieldGreaterThan(String field, Object value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.greaterThan(field, String.valueOf(value));
        }
        return this;
    }

    public Query greaterThan(String field, String value) {
        return fieldGreaterThan(field, value);
    }

    public Query greaterThan(String field, Integer value) {
        return fieldGreaterThan(field, value);
    }

    public Query greaterThan(String field, Long value) {
        return fieldGreaterThan(field, value);
    }

    public Query greaterThan(String field, Double value) {
        return fieldGreaterThan(field, value);
    }

    public Query greaterThan(String field, Float value) {
        return fieldGreaterThan(field, value);
    }

    public Query greaterThan(String field, Date value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.greaterThan(field, value);
        }
        return this;
    }

    public Query greaterThan(String field, Time value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.greaterThan(field, value);
        }
        return this;
    }

    public Query greaterThan(String field, Timestamp value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.greaterThan(field, value);
        }
        return this;
    }

    public Query greaterThan(String field, Calendar value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.greaterThan(field, value);
        }
        return this;
    }

    private Query fieldGreaterThanOrEqualTo(String field, Object value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.greaterThanOrEqualTo(field, String.valueOf(value));
        }
        return this;
    }

    public Query greaterThanOrEqualTo(String field, String value) {
        return fieldGreaterThanOrEqualTo(field, value);
    }

    public Query greaterThanOrEqualTo(String field, Integer value) {
        return fieldGreaterThanOrEqualTo(field, value);
    }

    public Query greaterThanOrEqualTo(String field, Long value) {
        return fieldGreaterThanOrEqualTo(field, value);
    }

    public Query greaterThanOrEqualTo(String field, Double value) {
        return fieldGreaterThanOrEqualTo(field, value);
    }

    public Query greaterThanOrEqualTo(String field, Float value) {
        return fieldGreaterThanOrEqualTo(field, value);
    }

    public Query greaterThanOrEqualTo(String field, Date value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.greaterThanOrEqualTo(field, value);
        }
        return this;
    }

    public Query greaterThanOrEqualTo(String field, Time value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.greaterThanOrEqualTo(field, value);
        }
        return this;
    }

    public Query greaterThanOrEqualTo(String field, Timestamp value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.greaterThanOrEqualTo(field, value);
        }
        return this;
    }

    public Query greaterThanOrEqualTo(String field, Calendar value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.greaterThanOrEqualTo(field, value);
        }
        return this;
    }

    private Query fieldLessThan(String field, Object value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.lessThan(field, String.valueOf(value));
        }
        return this;
    }

    public Query lessThan(String field, String value) {
        return fieldLessThan(field, value);
    }

    public Query lessThan(String field, Integer value) {
        return fieldLessThan(field, value);
    }

    public Query lessThan(String field, Long value) {
        return fieldLessThan(field, value);
    }

    public Query lessThan(String field, Double value) {
        return fieldLessThan(field, value);
    }

    public Query lessThan(String field, Float value) {
        return fieldLessThan(field, value);
    }

    public Query lessThan(String field, Date value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.lessThan(field, value);
        }
        return this;
    }

    public Query lessThan(String field, Time value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.lessThan(field, value);
        }
        return this;
    }

    public Query lessThan(String field, Timestamp value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.lessThan(field, value);
        }
        return this;
    }

    public Query lessThan(String field, Calendar value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.lessThan(field, value);
        }
        return this;
    }

    private Query fieldLessThanOrEqualTo(String field, Object value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.lessThanOrEqualTo(field, String.valueOf(value));
        }
        return this;
    }

    public Query lessThanOrEqualTo(String field, String value) {
        return fieldLessThanOrEqualTo(field, value);
    }

    public Query lessThanOrEqualTo(String field, Integer value) {
        return fieldLessThanOrEqualTo(field, value);
    }

    public Query lessThanOrEqualTo(String field, Long value) {
        return fieldLessThanOrEqualTo(field, value);
    }

    public Query lessThanOrEqualTo(String field, Double value) {
        return fieldLessThanOrEqualTo(field, value);
    }

    public Query lessThanOrEqualTo(String field, Float value) {
        return fieldLessThanOrEqualTo(field, value);
    }

    public Query lessThanOrEqualTo(String field, Date value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.lessThanOrEqualTo(field, value);
        }
        return this;
    }

    public Query lessThanOrEqualTo(String field, Time value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.lessThanOrEqualTo(field, value);
        }
        return this;
    }

    public Query lessThanOrEqualTo(String field, Timestamp value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.lessThanOrEqualTo(field, value);
        }
        return this;
    }

    public Query lessThanOrEqualTo(String field, Calendar value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.lessThanOrEqualTo(field, value);
        }
        return this;
    }

    private Query fieldBetween(String field, Object value1, Object value2) {
        if (TextUtils.isEmpty(field) || value1 == null || value2 == null) {
            this.valid = false;
        } else {
            this.queryImpl.between(field, String.valueOf(value1), String.valueOf(value2));
        }
        return this;
    }

    public Query between(String field, String value1, String value2) {
        return fieldBetween(field, value1, value2);
    }

    public Query between(String field, Integer value1, Integer value2) {
        return fieldBetween(field, value1, value2);
    }

    public Query between(String field, Long value1, Long value2) {
        return fieldBetween(field, value1, value2);
    }

    public Query between(String field, Double value1, Double value2) {
        return fieldBetween(field, value1, value2);
    }

    public Query between(String field, Float value1, Float value2) {
        return fieldBetween(field, value1, value2);
    }

    public Query between(String field, Date value1, Date value2) {
        if (TextUtils.isEmpty(field) || value1 == null || value2 == null) {
            this.valid = false;
        } else {
            this.queryImpl.between(field, value1, value2);
        }
        return this;
    }

    public Query between(String field, Time value1, Time value2) {
        if (TextUtils.isEmpty(field) || value1 == null || value2 == null) {
            this.valid = false;
        } else {
            this.queryImpl.between(field, value1, value2);
        }
        return this;
    }

    public Query between(String field, Timestamp value1, Timestamp value2) {
        if (TextUtils.isEmpty(field) || value1 == null || value2 == null) {
            this.valid = false;
        } else {
            this.queryImpl.between(field, value1, value2);
        }
        return this;
    }

    public Query between(String field, Calendar value1, Calendar value2) {
        if (TextUtils.isEmpty(field) || value1 == null || value2 == null) {
            this.valid = false;
        } else {
            this.queryImpl.between(field, value1, value2);
        }
        return this;
    }

    public Query contains(String field, String value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.contains(field, value);
        }
        return this;
    }

    public Query beginsWith(String field, String value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.beginsWith(field, value);
        }
        return this;
    }

    public Query endsWith(String field, String value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.endsWith(field, value);
        }
        return this;
    }

    public Query like(String field, String value) {
        if (TextUtils.isEmpty(field) || value == null) {
            this.valid = false;
        } else {
            this.queryImpl.like(field, value);
        }
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

    public Query orderByAsc(String field) {
        if (TextUtils.isEmpty(field)) {
            this.valid = false;
        } else {
            this.queryImpl.orderByAsc(field);
        }
        return this;
    }

    public Query orderByDesc(String field) {
        if (TextUtils.isEmpty(field)) {
            this.valid = false;
        } else {
            this.queryImpl.orderByDesc(field);
        }
        return this;
    }

    public Query limit(int value) {
        try {
            this.queryImpl.limit(value);
        } catch (ODMFException e) {
            this.valid = false;
            DSLog.e(e.getMessage(), new Object[0]);
        }
        return this;
    }

    public Query max(String field) {
        if (TextUtils.isEmpty(field)) {
            this.valid = false;
        } else {
            this.aggregateType = 0;
            this.queryImpl.max(field);
        }
        return this;
    }

    public Query min(String field) {
        if (TextUtils.isEmpty(field)) {
            this.valid = false;
        } else {
            this.aggregateType = 1;
            this.queryImpl.min(field);
        }
        return this;
    }

    public Query count(String field) {
        if (TextUtils.isEmpty(field)) {
            this.valid = false;
        } else {
            this.aggregateType = 2;
            this.queryImpl.count(field);
        }
        return this;
    }

    public Query avg(String field) {
        if (TextUtils.isEmpty(field)) {
            this.valid = false;
        } else {
            this.aggregateType = 3;
            this.queryImpl.avg(field);
        }
        return this;
    }

    public Query sum(String field) {
        if (TextUtils.isEmpty(field)) {
            this.valid = false;
        } else {
            this.aggregateType = 4;
            this.queryImpl.sum(field);
        }
        return this;
    }

    public int getAggregateType() {
        return this.aggregateType;
    }
}
