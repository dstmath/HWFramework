package com.huawei.odmf.user;

import com.huawei.odmf.predicate.FetchRequest;
import com.huawei.odmf.user.api.ObjectContext;
import com.huawei.odmf.user.api.Query;

public class QueryBuilder {
    public static Query buildQuery(String entityName, FetchRequest fetchRequest, ObjectContext objectContext) {
        return new AQueryImpl(entityName, fetchRequest, objectContext);
    }
}
