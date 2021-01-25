package ohos.data.search.model;

import java.util.List;

public class Recommendation {
    private long count;
    private String field;
    private List<IndexData> indexDataList;
    private String value;

    public Recommendation(String str, String str2, List<IndexData> list, long j) {
        this.field = str;
        this.value = str2;
        this.indexDataList = list;
        this.count = j;
    }

    public void setField(String str) {
        this.field = str;
    }

    public void setValue(String str) {
        this.value = str;
    }

    public void setIndexDataList(List<IndexData> list) {
        this.indexDataList = list;
    }

    public void setCount(long j) {
        this.count = j;
    }

    public String getField() {
        return this.field;
    }

    public String getValue() {
        return this.value;
    }

    public List<IndexData> getIndexDataList() {
        return this.indexDataList;
    }

    public long getCount() {
        return this.count;
    }

    public String toString() {
        return "Recommendation{field=" + this.field + ",value=" + this.value + ",count=" + this.count + "}";
    }
}
