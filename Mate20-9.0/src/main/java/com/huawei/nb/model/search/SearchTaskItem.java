package com.huawei.nb.model.search;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class SearchTaskItem extends AManagedObject {
    public static final Parcelable.Creator<SearchTaskItem> CREATOR = new Parcelable.Creator<SearchTaskItem>() {
        public SearchTaskItem createFromParcel(Parcel in) {
            return new SearchTaskItem(in);
        }

        public SearchTaskItem[] newArray(int size) {
            return new SearchTaskItem[size];
        }
    };
    private Integer id;
    private String ids;
    private Boolean isCrawlContent;
    private Integer op;
    private String pkgName;
    private Integer priority;
    private Integer runTimes;
    private Long startTime;
    private Integer status;
    private Integer type;
    private Integer userId;

    public SearchTaskItem(Cursor cursor) {
        Boolean valueOf;
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.pkgName = cursor.getString(2);
        this.ids = cursor.getString(3);
        this.op = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.userId = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.type = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.runTimes = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        if (cursor.isNull(8)) {
            valueOf = null;
        } else {
            valueOf = Boolean.valueOf(cursor.getInt(8) != 0);
        }
        this.isCrawlContent = valueOf;
        this.priority = cursor.isNull(9) ? null : Integer.valueOf(cursor.getInt(9));
        this.status = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        this.startTime = !cursor.isNull(11) ? Long.valueOf(cursor.getLong(11)) : l;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SearchTaskItem(Parcel in) {
        super(in);
        Boolean valueOf;
        Long l = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.pkgName = in.readByte() == 0 ? null : in.readString();
        this.ids = in.readByte() == 0 ? null : in.readString();
        this.op = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.userId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.type = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.runTimes = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        if (in.readByte() == 0) {
            valueOf = null;
        } else {
            valueOf = Boolean.valueOf(in.readByte() != 0);
        }
        this.isCrawlContent = valueOf;
        this.priority = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.status = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.startTime = in.readByte() != 0 ? Long.valueOf(in.readLong()) : l;
    }

    private SearchTaskItem(Integer id2, String pkgName2, String ids2, Integer op2, Integer userId2, Integer type2, Integer runTimes2, Boolean isCrawlContent2, Integer priority2, Integer status2, Long startTime2) {
        this.id = id2;
        this.pkgName = pkgName2;
        this.ids = ids2;
        this.op = op2;
        this.userId = userId2;
        this.type = type2;
        this.runTimes = runTimes2;
        this.isCrawlContent = isCrawlContent2;
        this.priority = priority2;
        this.status = status2;
        this.startTime = startTime2;
    }

    public SearchTaskItem() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id2) {
        this.id = id2;
        setValue();
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName2) {
        this.pkgName = pkgName2;
        setValue();
    }

    public String getIds() {
        return this.ids;
    }

    public void setIds(String ids2) {
        this.ids = ids2;
        setValue();
    }

    public Integer getOp() {
        return this.op;
    }

    public void setOp(Integer op2) {
        this.op = op2;
        setValue();
    }

    public Integer getUserId() {
        return this.userId;
    }

    public void setUserId(Integer userId2) {
        this.userId = userId2;
        setValue();
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type2) {
        this.type = type2;
        setValue();
    }

    public Integer getRunTimes() {
        return this.runTimes;
    }

    public void setRunTimes(Integer runTimes2) {
        this.runTimes = runTimes2;
        setValue();
    }

    public Boolean getIsCrawlContent() {
        return this.isCrawlContent;
    }

    public void setIsCrawlContent(Boolean isCrawlContent2) {
        this.isCrawlContent = isCrawlContent2;
        setValue();
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority2) {
        this.priority = priority2;
        setValue();
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status2) {
        this.status = status2;
        setValue();
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Long startTime2) {
        this.startTime = startTime2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        byte b;
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.pkgName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.pkgName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.ids != null) {
            out.writeByte((byte) 1);
            out.writeString(this.ids);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.op != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.op.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.userId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.userId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.type != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.type.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.runTimes != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.runTimes.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isCrawlContent != null) {
            out.writeByte((byte) 1);
            if (this.isCrawlContent.booleanValue()) {
                b = 1;
            } else {
                b = 0;
            }
            out.writeByte(b);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.priority != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.priority.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.status != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.status.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.startTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.startTime.longValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<SearchTaskItem> getHelper() {
        return SearchTaskItemHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.search.SearchTaskItem";
    }

    public String getDatabaseName() {
        return "dsSearch";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("SearchTaskItem { id: ").append(this.id);
        sb.append(", pkgName: ").append(this.pkgName);
        sb.append(", ids: ").append(this.ids);
        sb.append(", op: ").append(this.op);
        sb.append(", userId: ").append(this.userId);
        sb.append(", type: ").append(this.type);
        sb.append(", runTimes: ").append(this.runTimes);
        sb.append(", isCrawlContent: ").append(this.isCrawlContent);
        sb.append(", priority: ").append(this.priority);
        sb.append(", status: ").append(this.status);
        sb.append(", startTime: ").append(this.startTime);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.7";
    }

    public int getDatabaseVersionCode() {
        return 7;
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }
}
