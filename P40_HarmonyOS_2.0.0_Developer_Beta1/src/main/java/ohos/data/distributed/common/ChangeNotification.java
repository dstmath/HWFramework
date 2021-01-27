package ohos.data.distributed.common;

import java.util.List;

public class ChangeNotification {
    private List<Entry> deleteEntries;
    private String deviceId;
    private List<Entry> insertEntries;
    private List<Entry> updateEntries;

    private ChangeNotification() {
    }

    public ChangeNotification(List<Entry> list, List<Entry> list2, List<Entry> list3, String str) {
        this.insertEntries = list;
        this.updateEntries = list2;
        this.deleteEntries = list3;
        this.deviceId = str;
    }

    public List<Entry> getInsertEntries() {
        return this.insertEntries;
    }

    public List<Entry> getUpdateEntries() {
        return this.updateEntries;
    }

    public List<Entry> getDeleteEntries() {
        return this.deleteEntries;
    }

    public String getDeviceId() {
        return this.deviceId;
    }
}
