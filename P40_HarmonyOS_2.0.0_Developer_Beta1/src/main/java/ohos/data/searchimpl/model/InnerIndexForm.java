package ohos.data.searchimpl.model;

import android.os.Parcel;
import android.os.Parcelable;
import ohos.data.search.model.IndexType;

public class InnerIndexForm implements Parcelable {
    public static final Parcelable.Creator<InnerIndexForm> CREATOR = new Parcelable.Creator<InnerIndexForm>() {
        /* class ohos.data.searchimpl.model.InnerIndexForm.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InnerIndexForm createFromParcel(Parcel parcel) {
            return new InnerIndexForm(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public InnerIndexForm[] newArray(int i) {
            return new InnerIndexForm[i];
        }
    };
    private String indexFieldName;
    private String indexType;
    private boolean isPrimaryKey;
    private boolean isSearch;
    private boolean isStore;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public InnerIndexForm(String str) {
        this.indexType = IndexType.ANALYZED;
        this.isPrimaryKey = false;
        this.isStore = true;
        this.isSearch = true;
        this.indexFieldName = str;
    }

    public InnerIndexForm() {
        this.indexType = IndexType.ANALYZED;
        this.isPrimaryKey = false;
        this.isStore = true;
        this.isSearch = true;
    }

    private InnerIndexForm(Parcel parcel) {
        this.indexType = IndexType.ANALYZED;
        boolean z = false;
        this.isPrimaryKey = false;
        this.isStore = true;
        this.isSearch = true;
        this.indexFieldName = parcel.readString();
        this.indexType = parcel.readString();
        this.isPrimaryKey = parcel.readByte() != 0;
        this.isStore = parcel.readByte() != 0;
        this.isSearch = parcel.readByte() != 0 ? true : z;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.indexFieldName);
        parcel.writeString(this.indexType);
        parcel.writeByte(this.isPrimaryKey ? (byte) 1 : 0);
        parcel.writeByte(this.isStore ? (byte) 1 : 0);
        parcel.writeByte(this.isSearch ? (byte) 1 : 0);
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

    @Override // java.lang.Object
    public String toString() {
        return "InnerIndexForm{indexFieldName=" + this.indexFieldName + ",indexType=" + this.indexType + ",isPrimaryKey=" + this.isPrimaryKey + ",isStore=" + this.isStore + ",isSearch=" + this.isSearch + "}";
    }
}
