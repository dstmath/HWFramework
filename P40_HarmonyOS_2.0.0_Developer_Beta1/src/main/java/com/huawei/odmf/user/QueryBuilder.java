package com.huawei.odmf.user;

import com.huawei.odmf.predicate.FetchRequest;
import com.huawei.odmf.user.api.ObjectContext;
import com.huawei.odmf.user.api.Query;

public final class QueryBuilder {
    private QueryBuilder() {
    }

    public static Query buildQuery(String str, FetchRequest fetchRequest, ObjectContext objectContext) {
        return new AQueryImpl(str, fetchRequest, objectContext);
    }
}
