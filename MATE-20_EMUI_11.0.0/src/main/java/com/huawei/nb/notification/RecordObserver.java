package com.huawei.nb.notification;

import com.huawei.odmf.core.AManagedObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class RecordObserver implements ModelObserver {
    /* access modifiers changed from: protected */
    public abstract boolean isEqual(AManagedObject aManagedObject);

    public abstract void onRecordChanged(ChangeNotification changeNotification);

    @Override // com.huawei.nb.notification.ModelObserver
    public final void onModelChanged(ChangeNotification changeNotification) {
        if (changeNotification != null) {
            List filterRecord = filterRecord(changeNotification.getInsertedItems());
            List filterRecord2 = filterRecord(changeNotification.getUpdatedItems());
            List filterRecord3 = filterRecord(changeNotification.getDeletedItems());
            if (filterRecord.size() != 0 || filterRecord2.size() != 0 || filterRecord3.size() != 0) {
                onRecordChanged(new ChangeNotification(changeNotification.getType(), changeNotification.isDeleteAll(), filterRecord, filterRecord2, filterRecord3));
            }
        }
    }

    private List filterRecord(List list) {
        ArrayList arrayList = new ArrayList();
        if (list != null) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                AManagedObject aManagedObject = (AManagedObject) it.next();
                if (isEqual(aManagedObject)) {
                    arrayList.add(aManagedObject);
                }
            }
        }
        return arrayList;
    }
}
