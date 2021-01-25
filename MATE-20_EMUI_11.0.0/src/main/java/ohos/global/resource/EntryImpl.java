package ohos.global.resource;

import ohos.global.resource.Entry;

public class EntryImpl extends Entry {
    private String path;
    private Entry.Type type;

    @Override // ohos.global.resource.Entry
    public String getPath() {
        return this.path;
    }

    @Override // ohos.global.resource.Entry
    public Entry.Type getType() {
        return this.type;
    }

    public void setPath(String str) {
        this.path = str;
    }

    public void setType(Entry.Type type2) {
        this.type = type2;
    }
}
