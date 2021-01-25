package ohos.data.orm.impl;

import java.util.ArrayList;
import java.util.List;
import ohos.data.orm.OrmObject;

public class Transaction {
    private static final int DEFAULT_QUEUE_LENGTH = 20;
    private List<OrmObject> transactionDeleteObjectList = new ArrayList(20);
    private List<OrmObject> transactionInsertObjectList = new ArrayList(20);
    private List<OrmObject> transactionUpdateObjectList = new ArrayList(20);

    public void setInsertObjectList(List<OrmObject> list) {
        for (OrmObject ormObject : list) {
            if (ormObject.getObjectId() != null) {
                OrmObject sameObjectFromList = getSameObjectFromList(this.transactionUpdateObjectList, ormObject);
                if (sameObjectFromList != null) {
                    this.transactionUpdateObjectList.remove(sameObjectFromList);
                    this.transactionUpdateObjectList.add(ormObject);
                } else {
                    OrmObject sameObjectFromList2 = getSameObjectFromList(this.transactionDeleteObjectList, ormObject);
                    if (sameObjectFromList2 != null) {
                        this.transactionDeleteObjectList.remove(sameObjectFromList2);
                    }
                    OrmObject sameObjectFromList3 = getSameObjectFromList(this.transactionInsertObjectList, ormObject);
                    if (sameObjectFromList3 != null) {
                        this.transactionInsertObjectList.remove(sameObjectFromList3);
                    }
                    this.transactionInsertObjectList.add(ormObject);
                }
            }
        }
    }

    private OrmObject getSameObjectFromList(List<OrmObject> list, OrmObject ormObject) {
        for (OrmObject ormObject2 : list) {
            if (ormObject2.getObjectId().equals(ormObject.getObjectId())) {
                return ormObject2;
            }
        }
        return null;
    }

    public void setUpdateObjectList(List<OrmObject> list) {
        for (OrmObject ormObject : list) {
            OrmObject sameObjectFromList = getSameObjectFromList(this.transactionInsertObjectList, ormObject);
            if (sameObjectFromList != null) {
                this.transactionInsertObjectList.remove(sameObjectFromList);
                this.transactionInsertObjectList.add(ormObject);
            } else {
                OrmObject sameObjectFromList2 = getSameObjectFromList(this.transactionDeleteObjectList, ormObject);
                if (sameObjectFromList2 != null) {
                    this.transactionDeleteObjectList.remove(sameObjectFromList2);
                }
                OrmObject sameObjectFromList3 = getSameObjectFromList(this.transactionUpdateObjectList, ormObject);
                if (sameObjectFromList3 != null) {
                    this.transactionUpdateObjectList.remove(sameObjectFromList3);
                }
                this.transactionUpdateObjectList.add(ormObject);
            }
        }
    }

    public void setDeleteObjectList(List<OrmObject> list) {
        for (OrmObject ormObject : list) {
            OrmObject sameObjectFromList = getSameObjectFromList(this.transactionInsertObjectList, ormObject);
            if (sameObjectFromList != null) {
                this.transactionInsertObjectList.remove(sameObjectFromList);
            }
            OrmObject sameObjectFromList2 = getSameObjectFromList(this.transactionUpdateObjectList, ormObject);
            if (sameObjectFromList2 != null) {
                this.transactionUpdateObjectList.remove(sameObjectFromList2);
            }
            OrmObject sameObjectFromList3 = getSameObjectFromList(this.transactionDeleteObjectList, ormObject);
            if (sameObjectFromList3 != null) {
                this.transactionDeleteObjectList.remove(sameObjectFromList3);
            }
            this.transactionDeleteObjectList.add(ormObject);
        }
    }

    public SaveRequest getSaveRequest() {
        return new SaveRequest(this.transactionInsertObjectList, this.transactionUpdateObjectList, this.transactionDeleteObjectList);
    }

    public void clearObjectList() {
        this.transactionInsertObjectList.clear();
        this.transactionUpdateObjectList.clear();
        this.transactionDeleteObjectList.clear();
    }
}
