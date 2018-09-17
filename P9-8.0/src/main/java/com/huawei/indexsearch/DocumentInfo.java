package com.huawei.indexsearch;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.Arrays;

public class DocumentInfo implements Parcelable {
    public static final Creator<DocumentInfo> CREATOR = new Creator<DocumentInfo>() {
        public DocumentInfo createFromParcel(Parcel in) {
            return new DocumentInfo(in, null);
        }

        public DocumentInfo[] newArray(int size) {
            return new DocumentInfo[size];
        }
    };
    private String[] mDocFields;
    private int[] mFieldIndexStatus;
    private int[] mFieldStoreStatus;
    private ArrayList<String> mFieldValueList;

    /* synthetic */ DocumentInfo(Parcel in, DocumentInfo -this1) {
        this(in);
    }

    public DocumentInfo(String[] docFields, ArrayList<String> list, int[] fieldStoreStatus, int[] fieldIndexStatus) {
        this.mDocFields = (String[]) docFields.clone();
        this.mFieldValueList = list;
        this.mFieldStoreStatus = (int[]) fieldStoreStatus.clone();
        this.mFieldIndexStatus = (int[]) fieldIndexStatus.clone();
    }

    private DocumentInfo(Parcel in) {
        this.mDocFields = in.createStringArray();
        this.mFieldValueList = in.createStringArrayList();
        this.mFieldStoreStatus = in.createIntArray();
        this.mFieldIndexStatus = in.createIntArray();
    }

    public ArrayList<String> getList() {
        return this.mFieldValueList;
    }

    public String[] getDocFieldList() {
        return (String[]) this.mDocFields.clone();
    }

    public int[] getFieldStoreList() {
        return (int[]) this.mFieldStoreStatus.clone();
    }

    public int[] getFieldIndexList() {
        return (int[]) this.mFieldIndexStatus.clone();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(this.mDocFields);
        dest.writeStringList(this.mFieldValueList);
        dest.writeIntArray(this.mFieldStoreStatus);
        dest.writeIntArray(this.mFieldIndexStatus);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Arrays.toString(this.mDocFields)).append("\n");
        for (String str : this.mFieldValueList) {
            sb.append(str).append(",");
        }
        sb.append("\n");
        sb.append(Arrays.toString(this.mFieldStoreStatus)).append("\n");
        sb.append(Arrays.toString(this.mFieldIndexStatus));
        return sb.toString();
    }

    public void readFromParcel(Parcel in) {
        this.mDocFields = in.createStringArray();
        this.mFieldValueList = in.createStringArrayList();
        this.mFieldStoreStatus = in.createIntArray();
        this.mFieldIndexStatus = in.createIntArray();
    }
}
