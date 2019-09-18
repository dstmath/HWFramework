package com.huawei.nb.odmfadapter;

import android.database.Cursor;
import android.support.annotation.Nullable;
import com.huawei.nb.query.bulkcursor.BulkCursorDescriptor;
import com.huawei.nb.query.bulkcursor.BulkCursorToCursorAdaptor;
import com.huawei.nb.utils.logger.DSLog;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.core.ManagedObject;
import com.huawei.odmf.user.api.ObjectContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class OdmfHelper {
    private ObjectContext objectContext;

    public OdmfHelper(ObjectContext objectContext2) {
        this.objectContext = objectContext2;
    }

    /* JADX INFO: finally extract failed */
    public List parseCursor(String entityName, Object cursor) {
        if (entityName == null || cursor == null || !(cursor instanceof BulkCursorDescriptor)) {
            DSLog.e("Failed to parse cursor, error: null input parameters.", new Object[0]);
            return null;
        }
        Constructor constructor = getConstructorOfClass(entityName);
        if (constructor == null) {
            DSLog.e("Failed to parse cursor, error: null object constructor.", new Object[0]);
            return null;
        }
        Cursor wrappedCursor = wrapCursor(cursor);
        List<ManagedObject> results = new ArrayList<>();
        while (wrappedCursor.moveToNext()) {
            try {
                ManagedObject object = (ManagedObject) constructor.newInstance(new Object[]{wrappedCursor});
                object.setState(4);
                object.setObjectContext(this.objectContext);
                results.add(object);
            } catch (InstantiationException e) {
                InstantiationException instantiationException = e;
                try {
                    DSLog.e("Failed to read entity %s from cursor.", entityName);
                    wrappedCursor.close();
                    return null;
                } catch (Throwable th) {
                    wrappedCursor.close();
                    throw th;
                }
            } catch (IllegalAccessException e2) {
                IllegalAccessException illegalAccessException = e2;
                DSLog.e("Failed to read entity %s from cursor.", entityName);
                wrappedCursor.close();
                return null;
            } catch (InvocationTargetException e3) {
                InvocationTargetException invocationTargetException = e3;
                DSLog.e("Failed to read entity %s from cursor.", entityName);
                wrappedCursor.close();
                return null;
            }
        }
        wrappedCursor.close();
        return results;
    }

    /* JADX INFO: finally extract failed */
    public List parseNativeCursor(String entityName, Cursor cursor) {
        if (entityName == null || cursor == null) {
            DSLog.e("Failed to parse native cursor, error: null input parameters.", new Object[0]);
            return null;
        }
        Constructor constructor = getConstructorOfClass(entityName);
        if (constructor == null) {
            DSLog.e("Failed to parse native cursor, error: null object constructor.", new Object[0]);
            return null;
        }
        List<ManagedObject> results = new ArrayList<>();
        while (cursor.moveToNext()) {
            try {
                ManagedObject object = (ManagedObject) constructor.newInstance(new Object[]{cursor});
                object.setState(4);
                object.setObjectContext(this.objectContext);
                results.add(object);
            } catch (InstantiationException e) {
                InstantiationException instantiationException = e;
                try {
                    DSLog.e("Failed to read entity %s from cursor.", entityName);
                    cursor.close();
                    return null;
                } catch (Throwable th) {
                    cursor.close();
                    throw th;
                }
            } catch (IllegalAccessException e2) {
                IllegalAccessException illegalAccessException = e2;
                DSLog.e("Failed to read entity %s from cursor.", entityName);
                cursor.close();
                return null;
            } catch (InvocationTargetException e3) {
                InvocationTargetException invocationTargetException = e3;
                DSLog.e("Failed to read entity %s from cursor.", entityName);
                cursor.close();
                return null;
            }
        }
        cursor.close();
        return results;
    }

    public Cursor wrapCursor(Object cursor) {
        BulkCursorToCursorAdaptor adaptor = new BulkCursorToCursorAdaptor();
        adaptor.initialize((BulkCursorDescriptor) cursor);
        return adaptor;
    }

    @Nullable
    private Constructor getConstructorOfClass(String name) {
        boolean z = false;
        try {
            return Class.forName(name).getConstructor(new Class[]{Cursor.class});
        } catch (ClassNotFoundException e) {
            DSLog.e("Failed to find Class %s.", name);
            return z;
        } catch (NoSuchMethodException e2) {
            DSLog.e("Failed to find the constructor %s.", name);
            return z;
        }
    }

    public List assignObjectContext(List rawObjects) {
        if (rawObjects != null) {
            for (Object object : rawObjects) {
                if (object instanceof ManagedObject) {
                    ((ManagedObject) object).setObjectContext(this.objectContext);
                }
            }
        }
        return rawObjects;
    }

    public <T extends AManagedObject> void presetUriString(List<T> entities) {
        if (entities != null && !entities.isEmpty()) {
            String uriString = generateUriString(((AManagedObject) entities.get(0)).getDatabaseName());
            for (T obj : entities) {
                obj.setUriString(uriString);
            }
        }
    }

    private String generateUriString(String dbName) {
        return "odmf://com.huawei.odmf/" + dbName;
    }
}
