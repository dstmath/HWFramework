package com.huawei.odmf.utils;

import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.model.api.Attribute;

public class CursorUtils {
    private CursorUtils() {
    }

    public static Object extractAggregateResult(String str, int i, Attribute attribute) {
        if (i == 2) {
            return Long.valueOf(str);
        }
        if (i == 3) {
            if (attribute != null) {
                int type = attribute.getType();
                if (type != 11 && type != 21 && type != 14 && type != 22 && type != 6 && type != 7 && type != 3 && type != 20) {
                    return Double.valueOf(str);
                }
                LOG.logE("Execute FetchRequestWithAggregateFunction failed : The aggregate function AVG is not support for the data type byte, char, boolean, blob and clob.");
                throw new ODMFIllegalArgumentException("Execute FetchRequestWithAggregateFunction failed : The aggregate function AVG is not support for the data type byte, char, boolean, blob and clob.");
            }
            LOG.logE("Execute FetchRequestWithAggregateFunction failed : the column name is wrong.");
            throw new ODMFRuntimeException("Execute FetchRequestWithAggregateFunction failed : the column name is wrong.");
        } else if (attribute != null) {
            int type2 = attribute.getType();
            if (type2 == 9 || type2 == 12 || type2 == 10 || type2 == 13) {
                return Long.valueOf(str);
            }
            if (type2 == 4 || type2 == 5 || type2 == 20 || type2 == 18) {
                return Double.valueOf(str);
            }
            if (type2 == 0 || type2 == 1 || type2 == 8 || type2 == 15 || type2 == 16 || type2 == 17) {
                return Long.valueOf(str);
            }
            if (type2 == 2) {
                return str;
            }
            LOG.logE("Execute FetchRequestWithAggregateFunction failed : The aggregate function MAX,MIN,SUM is not support for the data type byte, char, boolean, blob and clob.");
            throw new ODMFIllegalArgumentException("The aggregate function MAX,MIN,SUM is not support for the data type byte, char, boolean, blob and clob.");
        } else {
            LOG.logE("Execute FetchRequestWithAggregateFunction failed : the column name is wrong.");
            throw new ODMFRuntimeException("Execute FetchRequestWithAggregateFunction failed : the column name is wrong");
        }
    }
}
