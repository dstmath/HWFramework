package com.huawei.nb.notification;

import com.huawei.odmf.core.AManagedObject;
import java.util.ArrayList;
import java.util.List;

public abstract class RecordObserver implements ModelObserver {
    /* access modifiers changed from: protected */
    public abstract boolean isEqual(AManagedObject aManagedObject);

    public abstract void onRecordChanged(ChangeNotification changeNotification);

    public final void onModelChanged(ChangeNotification changeNotification) {
        if (changeNotification != null) {
            List iItems = filterRecord(changeNotification.getInsertedItems());
            List uItems = filterRecord(changeNotification.getUpdatedItems());
            List dItems = filterRecord(changeNotification.getDeletedItems());
            if (iItems.size() != 0 || uItems.size() != 0 || dItems.size() != 0) {
                onRecordChanged(new ChangeNotification(changeNotification.getType(), changeNotification.isDeleteAll(), iItems, uItems, dItems));
            }
        }
    }

    private List filterRecord(List src) {
        List<AManagedObject> dst = new ArrayList<>();
        if (src != null) {
            for (Object obj : src) {
                if (isEqual((AManagedObject) obj)) {
                    dst.add((AManagedObject) obj);
                }
            }
        }
        return dst;
    }
}
