package ohos.data.search.model;

import java.util.Objects;

public class IndexForm {
    private String indexFieldName;
    private String indexType = IndexType.ANALYZED;
    private boolean isPrimaryKey = false;
    private boolean isSearch = true;
    private boolean isStore = true;

    public IndexForm(String str) {
        this.indexFieldName = str;
    }

    public IndexForm(String str, String str2, boolean z, boolean z2, boolean z3) {
        this.indexFieldName = str;
        this.indexType = str2;
        this.isPrimaryKey = z;
        this.isStore = z2;
        this.isSearch = z3;
    }

    public String getIndexFieldName() {
        return this.indexFieldName;
    }

    public void setIndexFieldName(String str) {
        this.indexFieldName = str;
    }

    public String getIndexType() {
        return this.indexType;
    }

    public void setIndexType(String str) {
        this.indexType = str;
    }

    public boolean isPrimaryKey() {
        return this.isPrimaryKey;
    }

    public void setPrimaryKey(boolean z) {
        this.isPrimaryKey = z;
    }

    public boolean isStore() {
        return this.isStore;
    }

    public void setStore(boolean z) {
        this.isStore = z;
    }

    public boolean isSearch() {
        return this.isSearch;
    }

    public void setSearch(boolean z) {
        this.isSearch = z;
    }

    public String toString() {
        return "IndexForm{indexFieldName=" + this.indexFieldName + ",indexType=" + this.indexType + ",isPrimaryKey=" + this.isPrimaryKey + ",isStore=" + this.isStore + ",isSearch=" + this.isSearch + "}";
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof IndexForm)) {
            return false;
        }
        IndexForm indexForm = (IndexForm) obj;
        return this.isPrimaryKey == indexForm.isPrimaryKey && this.isStore == indexForm.isStore && this.isSearch == indexForm.isSearch && Objects.equals(this.indexFieldName, indexForm.indexFieldName) && Objects.equals(this.indexType, indexForm.indexType);
    }

    public int hashCode() {
        return Objects.hash(this.indexFieldName, Boolean.valueOf(this.isPrimaryKey), this.indexType, Boolean.valueOf(this.isStore), Boolean.valueOf(this.isSearch));
    }
}
