package com.huawei.odmf.predicate;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FetchRequest<T> implements Parcelable {
    public static final Parcelable.Creator<FetchRequest> CREATOR = new Parcelable.Creator<FetchRequest>() {
        public FetchRequest createFromParcel(Parcel in) {
            return new FetchRequest(in);
        }

        public FetchRequest[] newArray(int size) {
            return new FetchRequest[size];
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

    public List<String> getJoinedEntities() {
        return this.joinedEntities;
    }

    public void addToJoinedEntities(String entityName2) {
        this.joinedEntities.add(entityName2);
    }

    public Class getTheClass() {
        return this.theClass;
    }

    public boolean getIsJoined() {
        return this.joinedEntities.size() != 0;
    }

    public FetchRequest(String entityName2, Class<T> clz) {
        if (entityName2 == null) {
            throw new ODMFIllegalArgumentException("entity is null");
        }
        this.entityName = entityName2;
        this.sqlRequest = new StringBuilder();
        this.selectionArgs = new String[0];
        this.order = new StringBuilder("");
        this.limit = "";
        this.joinClause = new StringBuilder("");
        this.theClass = clz;
    }

    public FetchRequest() {
    }

    public String[] getColumns() {
        if (this.columns == null) {
            return null;
        }
        String[] copy = new String[this.columns.length];
        System.arraycopy(this.columns, 0, copy, 0, this.columns.length);
        return copy;
    }

    public void setColumns(String[] columns2) {
        if (columns2 != null) {
            this.columns = new String[columns2.length];
            System.arraycopy(columns2, 0, this.columns, 0, columns2.length);
            return;
        }
        this.columns = null;
    }

    public int[] getAggregateOp() {
        if (this.aggregateOp != null) {
            return Arrays.copyOf(this.aggregateOp, this.aggregateOp.length);
        }
        return null;
    }

    public void setAggregateOp(int[] aggregateOp2) {
        if (aggregateOp2 != null) {
            this.aggregateOp = Arrays.copyOf(aggregateOp2, aggregateOp2.length);
        } else {
            this.aggregateOp = null;
        }
    }

    public String[] getColumnsWithAggregateFunction() {
        if (this.columnsWithAggregateFunction == null) {
            return null;
        }
        String[] copy = new String[this.columnsWithAggregateFunction.length];
        System.arraycopy(this.columnsWithAggregateFunction, 0, copy, 0, this.columnsWithAggregateFunction.length);
        return copy;
    }

    public void setColumnsWithAggregateFunction(String[] columnsWithAggregateFunction2) {
        if (columnsWithAggregateFunction2 != null) {
            this.columnsWithAggregateFunction = new String[columnsWithAggregateFunction2.length];
            System.arraycopy(columnsWithAggregateFunction2, 0, this.columnsWithAggregateFunction, 0, columnsWithAggregateFunction2.length);
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
        if (this.selectionArgs == null) {
            return null;
        }
        String[] copy = new String[this.selectionArgs.length];
        System.arraycopy(this.selectionArgs, 0, copy, 0, this.selectionArgs.length);
        return copy;
    }

    public void setSelectionArgs(String[] values) {
        if (values != null) {
            this.selectionArgs = new String[values.length];
            System.arraycopy(values, 0, this.selectionArgs, 0, values.length);
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

    public void setLimit(String limit2) {
        this.limit = limit2;
    }

    public StringBuilder getJoinClause() {
        return this.joinClause;
    }

    public void setJoinClause(StringBuilder joinClause2) {
        this.joinClause = joinClause2;
    }

    public String toString() {
        return "entityName: '" + this.entityName + '\'' + ", sqlRequest: " + this.sqlRequest + ", selectionArgs: " + Arrays.toString(this.selectionArgs) + ", order: " + this.order + ", limit: '" + this.limit + '\'' + ", joinClause: " + this.joinClause;
    }

    protected FetchRequest(Parcel in) {
        this.entityName = in.readString();
        this.sqlRequest = new StringBuilder();
        this.sqlRequest.append(in.readString());
        this.selectionArgs = in.createStringArray();
        this.order = new StringBuilder();
        this.order.append(in.readString());
        this.limit = in.readString();
        this.joinClause = new StringBuilder();
        this.joinClause.append(in.readString());
        if (in.readInt() == 0) {
            setColumnsWithAggregateFunction(in.createStringArray());
        }
        if (in.readInt() == 0) {
            setColumns(in.createStringArray());
        }
        if (in.readInt() == 0) {
            setAggregateOp(in.createIntArray());
        }
        if (in.readInt() > 0) {
            in.readStringList(this.joinedEntities);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.entityName);
        dest.writeString(this.sqlRequest.toString());
        dest.writeStringArray(this.selectionArgs);
        dest.writeString(this.order.toString());
        dest.writeString(this.limit);
        dest.writeString(this.joinClause.toString());
        if (this.columnsWithAggregateFunction == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(0);
            dest.writeStringArray(this.columnsWithAggregateFunction);
        }
        if (this.columns == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(0);
            dest.writeStringArray(this.columns);
        }
        if (this.aggregateOp == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(0);
            dest.writeIntArray(this.aggregateOp);
        }
        if (this.joinedEntities.size() == 0) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(this.joinedEntities.size());
        dest.writeStringList(this.joinedEntities);
    }
}
