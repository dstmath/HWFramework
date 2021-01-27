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

    public List parseCursor(String str, Object obj) {
        if (str == null || obj == null || !(obj instanceof BulkCursorDescriptor)) {
            DSLog.e("Failed to parse cursor, error: null input parameters.", new Object[0]);
            return null;
        }
        Constructor constructorOfClass = getConstructorOfClass(str);
        if (constructorOfClass == null) {
            DSLog.e("Failed to parse cursor, error: null object constructor.", new Object[0]);
            return null;
        }
        Cursor wrapCursor = wrapCursor(obj);
        ArrayList arrayList = new ArrayList(wrapCursor.getCount());
        while (wrapCursor.moveToNext()) {
            try {
                if (constructorOfClass.newInstance(wrapCursor) instanceof ManagedObject) {
                    ManagedObject managedObject = (ManagedObject) constructorOfClass.newInstance(wrapCursor);
                    managedObject.setState(4);
                    managedObject.setObjectContext(this.objectContext);
                    arrayList.add(managedObject);
                }
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException unused) {
                DSLog.e("Failed to read entity %s from cursor.", str);
                return null;
            } finally {
                wrapCursor.close();
            }
        }
        return arrayList;
    }

    public List parseNativeCursor(String str, Cursor cursor) {
        if (str == null || cursor == null) {
            DSLog.e("Failed to parse native cursor, error: null input parameters.", new Object[0]);
            return null;
        }
        Constructor constructorOfClass = getConstructorOfClass(str);
        if (constructorOfClass == null) {
            DSLog.e("Failed to parse native cursor, error: null object constructor.", new Object[0]);
            return null;
        }
        ArrayList arrayList = new ArrayList();
        while (cursor.moveToNext()) {
            try {
                if (constructorOfClass.newInstance(cursor) instanceof ManagedObject) {
                    ManagedObject managedObject = (ManagedObject) constructorOfClass.newInstance(cursor);
                    managedObject.setState(4);
                    managedObject.setObjectContext(this.objectContext);
                    arrayList.add(managedObject);
                }
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException unused) {
                DSLog.e("Failed to read entity %s from cursor.", str);
                return null;
            } finally {
                cursor.close();
            }
        }
        return arrayList;
    }

    public Cursor wrapCursor(Object obj) {
        if (obj == null) {
            return null;
        }
        BulkCursorToCursorAdaptor bulkCursorToCursorAdaptor = new BulkCursorToCursorAdaptor();
        bulkCursorToCursorAdaptor.initialize((BulkCursorDescriptor) obj);
        return bulkCursorToCursorAdaptor;
    }

    @Nullable
    private Constructor getConstructorOfClass(String str) {
        try {
            return Class.forName(str).getConstructor(Cursor.class);
        } catch (ClassNotFoundException unused) {
            DSLog.e("Failed to find Class %s.", str);
            return null;
        } catch (NoSuchMethodException unused2) {
            DSLog.e("Failed to find the constructor %s.", str);
            return null;
        }
    }

    public List assignObjectContext(List list) {
        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof ManagedObject) {
                    ((ManagedObject) obj).setObjectContext(this.objectContext);
                }
            }
        }
        return list;
    }

    public <T extends AManagedObject> void presetUriString(List<T> list) {
        if (!(list == null || list.isEmpty())) {
            String generateUriString = generateUriString(list.get(0).getDatabaseName());
            for (T t : list) {
                t.setUriString(generateUriString);
            }
        }
    }

    private String generateUriString(String str) {
        return "odmf://com.huawei.odmf/" + str;
    }
}
