package ohos.data.search.model;

import java.util.ArrayList;
import java.util.List;

public class ChangedIndexContent {
    private List<IndexData> deletedItems;
    private List<IndexData> insertedItems;
    private List<IndexData> updatedItems;

    public ChangedIndexContent(List<IndexData> list, List<IndexData> list2, List<IndexData> list3) {
        this.insertedItems = list == null ? new ArrayList<>(0) : list;
        this.updatedItems = list2 == null ? new ArrayList<>(0) : list2;
        this.deletedItems = list3 == null ? new ArrayList<>(0) : list3;
    }

    public List<IndexData> getInsertedItems() {
        return this.insertedItems;
    }

    public List<IndexData> getUpdatedItems() {
        return this.updatedItems;
    }

    public List<IndexData> getDeletedItems() {
        return this.deletedItems;
    }

    public void setInsertedItems(List<IndexData> list) {
        this.insertedItems = list;
    }

    public void setUpdatedItems(List<IndexData> list) {
        this.updatedItems = list;
    }

    public void setDeletedItems(List<IndexData> list) {
        this.deletedItems = list;
    }
}
