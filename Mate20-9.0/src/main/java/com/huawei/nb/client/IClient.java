package com.huawei.nb.client;

import android.database.Cursor;
import com.huawei.nb.notification.ModelObserver;
import com.huawei.nb.notification.ObserverType;
import com.huawei.nb.query.Query;
import com.huawei.nb.query.RawQuery;
import com.huawei.nb.query.RelationshipQuery;
import com.huawei.odmf.core.AManagedObject;
import java.util.List;

public interface IClient {
    Double executeAvgQuery(Query query);

    long executeCountQuery(Query query);

    <T extends AManagedObject> boolean executeDelete(T t);

    <T extends AManagedObject> boolean executeDelete(List<T> list);

    <T extends AManagedObject> boolean executeDeleteAll(Class<T> cls) throws NoSuchMethodException;

    Cursor executeFusionQuery(RawQuery rawQuery);

    <T extends AManagedObject> T executeInsert(T t);

    <T extends AManagedObject> List<T> executeInsert(List<T> list);

    Object executeMaxQuery(Query query);

    Object executeMinQuery(Query query);

    <T extends AManagedObject> List<T> executeQuery(Query query);

    <T extends AManagedObject> List<T> executeQuery(RelationshipQuery relationshipQuery);

    Cursor executeRawQuery(RawQuery rawQuery);

    <T extends AManagedObject> T executeSingleQuery(Query query);

    Object executeSumQuery(Query query);

    <T extends AManagedObject> boolean executeUpdate(T t);

    <T extends AManagedObject> boolean executeUpdate(List<T> list);

    <T extends AManagedObject> boolean subscribe(Class<T> cls, ObserverType observerType, ModelObserver modelObserver);

    <T extends AManagedObject> boolean unSubscribe(Class<T> cls, ObserverType observerType, ModelObserver modelObserver);
}
