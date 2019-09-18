package com.huawei.odmf.utils;

import com.huawei.odmf.core.ManagedObject;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import java.util.List;

public class JudgeUtils {
    public static final String INCOMPATIBLE_OBJECTS_NOT_ALLOWED_MESSAGE = "The element is incompatible with this LazyList";
    public static final String NULL_OBJECTS_NOT_ALLOWED_MESSAGE = "The specific object is null";

    public static void checkNull(Object o) {
        if (o == null) {
            throw new ODMFIllegalArgumentException(NULL_OBJECTS_NOT_ALLOWED_MESSAGE);
        }
    }

    public static void checkInstance(Object o) {
        if (!(o instanceof ManagedObject)) {
            throw new ODMFIllegalArgumentException(INCOMPATIBLE_OBJECTS_NOT_ALLOWED_MESSAGE);
        }
    }

    public static boolean isContainedObject(List list, Object e) {
        ManagedObject obj = (ManagedObject) e;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i).equals(obj)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkVersion(String version) {
        return version.matches("[0-9]+(\\.[0-9]+)*");
    }
}
