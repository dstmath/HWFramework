package com.huawei.odmf.user.api;

import com.huawei.odmf.core.ManagedObject;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

public interface Query<T extends ManagedObject> {
    Query<T> and();

    Query<T> avg(String str);

    Query<T> beginGroup();

    Query<T> beginsWith(String str, String str2);

    Query<T> between(String str, Double d, Double d2);

    Query<T> between(String str, Float f, Float f2);

    Query<T> between(String str, Integer num, Integer num2);

    Query<T> between(String str, Long l, Long l2);

    Query<T> between(String str, String str2, String str3);

    Query<T> between(String str, Time time, Time time2);

    Query<T> between(String str, Timestamp timestamp, Timestamp timestamp2);

    Query<T> between(String str, Calendar calendar, Calendar calendar2);

    Query<T> between(String str, Date date, Date date2);

    Query<T> contains(String str, String str2);

    Query<T> count(String str);

    Query<T> endGroup();

    Query<T> endsWith(String str, String str2);

    Query<T> equalTo(String str, Boolean bool);

    Query<T> equalTo(String str, Byte b);

    Query<T> equalTo(String str, Double d);

    Query<T> equalTo(String str, Float f);

    Query<T> equalTo(String str, Integer num);

    Query<T> equalTo(String str, Long l);

    Query<T> equalTo(String str, Short sh);

    Query<T> equalTo(String str, String str2);

    Query<T> equalTo(String str, Time time);

    Query<T> equalTo(String str, Timestamp timestamp);

    Query<T> equalTo(String str, Calendar calendar);

    Query<T> equalTo(String str, Date date);

    List<T> findAll();

    List<T> findAllLazyList();

    Query<T> greaterThan(String str, Double d);

    Query<T> greaterThan(String str, Float f);

    Query<T> greaterThan(String str, Integer num);

    Query<T> greaterThan(String str, Long l);

    Query<T> greaterThan(String str, String str2);

    Query<T> greaterThan(String str, Time time);

    Query<T> greaterThan(String str, Timestamp timestamp);

    Query<T> greaterThan(String str, Calendar calendar);

    Query<T> greaterThan(String str, Date date);

    Query<T> greaterThanOrEqualTo(String str, Double d);

    Query<T> greaterThanOrEqualTo(String str, Float f);

    Query<T> greaterThanOrEqualTo(String str, Integer num);

    Query<T> greaterThanOrEqualTo(String str, Long l);

    Query<T> greaterThanOrEqualTo(String str, String str2);

    Query<T> greaterThanOrEqualTo(String str, Time time);

    Query<T> greaterThanOrEqualTo(String str, Timestamp timestamp);

    Query<T> greaterThanOrEqualTo(String str, Calendar calendar);

    Query<T> greaterThanOrEqualTo(String str, Date date);

    Query<T> isNotNull(String str);

    Query<T> isNull(String str);

    Query<T> lessThan(String str, Double d);

    Query<T> lessThan(String str, Float f);

    Query<T> lessThan(String str, Integer num);

    Query<T> lessThan(String str, Long l);

    Query<T> lessThan(String str, String str2);

    Query<T> lessThan(String str, Time time);

    Query<T> lessThan(String str, Timestamp timestamp);

    Query<T> lessThan(String str, Calendar calendar);

    Query<T> lessThan(String str, Date date);

    Query<T> lessThanOrEqualTo(String str, Double d);

    Query<T> lessThanOrEqualTo(String str, Float f);

    Query<T> lessThanOrEqualTo(String str, Integer num);

    Query<T> lessThanOrEqualTo(String str, Long l);

    Query<T> lessThanOrEqualTo(String str, String str2);

    Query<T> lessThanOrEqualTo(String str, Time time);

    Query<T> lessThanOrEqualTo(String str, Timestamp timestamp);

    Query<T> lessThanOrEqualTo(String str, Calendar calendar);

    Query<T> lessThanOrEqualTo(String str, Date date);

    Query<T> like(String str, String str2);

    Query<T> limit(int i);

    ListIterator<T> listIterator();

    Query<T> max(String str);

    Query<T> min(String str);

    Query<T> notEqualTo(String str, Boolean bool);

    Query<T> notEqualTo(String str, Byte b);

    Query<T> notEqualTo(String str, Double d);

    Query<T> notEqualTo(String str, Float f);

    Query<T> notEqualTo(String str, Integer num);

    Query<T> notEqualTo(String str, Long l);

    Query<T> notEqualTo(String str, Short sh);

    Query<T> notEqualTo(String str, String str2);

    Query<T> notEqualTo(String str, Time time);

    Query<T> notEqualTo(String str, Timestamp timestamp);

    Query<T> notEqualTo(String str, Calendar calendar);

    Query<T> notEqualTo(String str, Date date);

    Query<T> or();

    Query<T> orderByAsc(String str);

    Query<T> orderByDesc(String str);

    List<Object> queryWithAggregateFunction();

    Query<T> sum(String str);
}
