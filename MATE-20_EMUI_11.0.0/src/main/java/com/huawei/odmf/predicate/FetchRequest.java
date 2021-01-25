package com.huawei.odmf.predicate;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FetchRequest<T> implements Parcelable {
    public static final Parcelable.Creator<FetchRequest> CREATOR = new Parcelable.Creator<FetchRequest>() {
        /* class com.huawei.odmf.predicate.FetchRequest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public FetchRequest createFromParcel(Parcel parcel) {
            return new FetchRequest(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public FetchRequest[] newArray(int i) {
            return new FetchRequest[i];
        }
    };
    private int[] aggregateOp = null;
    private String[] columns = null;
    private String[] columnsWithAggregateFunction = null;
    private String entityName;
    private StringBuilder joinClause = null;
    private List<String> joinedEntities = new ArrayList();
    private String limit = null;
    private StringBuilder order = null;
    private String[] selectionArgs = null;
    private StringBuilder sqlRequest = null;
    private Class theClass;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public FetchRequest(String str, Class<T> cls) {
        if (str != null) {
            this.entityName = str;
            this.sqlRequest = new StringBuilder();
            this.selectionArgs = new String[0];
            this.order = new StringBuilder();
            this.limit = "";
            this.joinClause = new StringBuilder();
            this.theClass = cls;
            return;
        }
        throw new ODMFIllegalArgumentException("entity is null");
    }

    protected FetchRequest(Parcel parcel) {
        this.entityName = parcel.readString();
        this.sqlRequest = new StringBuilder();
        this.sqlRequest.append(parcel.readString());
        this.selectionArgs = parcel.createStringArray();
        this.order = new StringBuilder();
        this.order.append(parcel.readString());
        this.limit = parcel.readString();
        this.joinClause = new StringBuilder();
        this.joinClause.append(parcel.readString());
        if (parcel.readInt() == 0) {
            setColumnsWithAggregateFunction(parcel.createStringArray());
        }
        if (parcel.readInt() == 0) {
            setColumns(parcel.createStringArray());
        }
        if (parcel.readInt() == 0) {
            setAggregateOp(parcel.createIntArray());
        }
        if (parcel.readInt() > 0) {
            parcel.readStringList(this.joinedEntities);
        }
    }

    public FetchRequest() {
    }

    public List<String> getJoinedEntities() {
        return this.joinedEntities;
    }

    public void addToJoinedEntities(String str) {
        this.joinedEntities.add(str);
    }

    public Class getTheClass() {
        return this.theClass;
    }

    public boolean getIsJoined() {
        return this.joinedEntities.size() != 0;
    }

    public String[] getColumns() {
        String[] strArr = this.columns;
        if (strArr == null) {
            return null;
        }
        String[] strArr2 = new String[strArr.length];
        System.arraycopy(strArr, 0, strArr2, 0, strArr.length);
        return strArr2;
    }

    public void setColumns(String[] strArr) {
        if (strArr != null) {
            this.columns = new String[strArr.length];
            System.arraycopy(strArr, 0, this.columns, 0, strArr.length);
            return;
        }
        this.columns = null;
    }

    public int[] getAggregateOp() {
        int[] iArr = this.aggregateOp;
        if (iArr != null) {
            return Arrays.copyOf(iArr, iArr.length);
        }
        return null;
    }

    public void setAggregateOp(int[] iArr) {
        if (iArr != null) {
            this.aggregateOp = Arrays.copyOf(iArr, iArr.length);
        } else {
            this.aggregateOp = null;
        }
    }

    public String[] getColumnsWithAggregateFunction() {
        String[] strArr = this.columnsWithAggregateFunction;
        if (strArr == null) {
            return null;
        }
        String[] strArr2 = new String[strArr.length];
        System.arraycopy(strArr, 0, strArr2, 0, strArr.length);
        return strArr2;
    }

    public void setColumnsWithAggregateFunction(String[] strArr) {
        if (strArr != null) {
            this.columnsWithAggregateFunction = new String[strArr.length];
            System.arraycopy(strArr, 0, this.columnsWithAggregateFunction, 0, strArr.length);
            return;
        }
        this.columnsWithAggregateFunction = null;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public StringBuilder getSqlRequest() {
        return this.sqlRequest;
    }

    public String[] getSelectionArgs() {
        String[] strArr = this.selectionArgs;
        if (strArr == null) {
            return null;
        }
        String[] strArr2 = new String[strArr.length];
        System.arraycopy(strArr, 0, strArr2, 0, strArr.length);
        return strArr2;
    }

    public void setSelectionArgs(String[] strArr) {
        if (strArr != null) {
            this.selectionArgs = new String[strArr.length];
            System.arraycopy(strArr, 0, this.selectionArgs, 0, strArr.length);
            return;
        }
        this.selectionArgs = null;
    }

    public StringBuilder getOrder() {
        return this.order;
    }

    public String getLimit() {
        return this.limit;
    }

    public void setLimit(String str) {
        this.limit = str;
    }

    public StringBuilder getJoinClause() {
        return this.joinClause;
    }

    public void setJoinClause(StringBuilder sb) {
        this.joinClause = sb;
    }

    @Override // java.lang.Object
    public String toString() {
        return "entityName: '" + this.entityName + "', sqlRequest: " + ((Object) this.sqlRequest) + ", selectionArgs: " + Arrays.toString(this.selectionArgs) + ", order: " + ((Object) this.order) + ", limit: '" + this.limit + "', joinClause: " + ((Object) this.joinClause);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.entityName);
        parcel.writeString(this.sqlRequest.toString());
        parcel.writeStringArray(this.selectionArgs);
        parcel.writeString(this.order.toString());
        parcel.writeString(this.limit);
        parcel.writeString(this.joinClause.toString());
        if (this.columnsWithAggregateFunction == null) {
            parcel.writeInt(-1);
        } else {
            parcel.writeInt(0);
            parcel.writeStringArray(this.columnsWithAggregateFunction);
        }
        if (this.columns == null) {
            parcel.writeInt(-1);
        } else {
            parcel.writeInt(0);
            parcel.writeStringArray(this.columns);
        }
        if (this.aggregateOp == null) {
            parcel.writeInt(-1);
        } else {
            parcel.writeInt(0);
            parcel.writeIntArray(this.aggregateOp);
        }
        if (this.joinedEntities.size() == 0) {
            parcel.writeInt(0);
            return;
        }
        parcel.writeInt(this.joinedEntities.size());
        parcel.writeStringList(this.joinedEntities);
    }
}
