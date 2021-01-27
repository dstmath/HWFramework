package com.huawei.odmf.utils;

import com.huawei.odmf.core.ManagedObject;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import java.util.List;

public class JudgeUtils {
    public static final String INCOMPATIBLE_OBJECTS_NOT_ALLOWED_MESSAGE = "The element is incompatible with this LazyList";
    public static final String NULL_OBJECTS_NOT_ALLOWED_MESSAGE = "The specific object is null";

    private JudgeUtils() {
    }

    public static void checkNull(Object obj) {
        if (obj == null) {
            throw new ODMFIllegalArgumentException(NULL_OBJECTS_NOT_ALLOWED_MESSAGE);
        }
    }

    public static void checkInstance(Object obj) {
        if (!(obj instanceof ManagedObject)) {
            throw new ODMFIllegalArgumentException(INCOMPATIBLE_OBJECTS_NOT_ALLOWED_MESSAGE);
        }
    }

    public static boolean isContainedObject(List list, Object obj) {
        ManagedObject managedObject = (ManagedObject) obj;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i).equals(managedObject)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkVersion(String str) {
        return str.matches("[0-9]+(\\.[0-9]+)*");
    }
}
