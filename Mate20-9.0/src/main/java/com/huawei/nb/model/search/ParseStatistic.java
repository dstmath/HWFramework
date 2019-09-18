package com.huawei.nb.model.search;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ParseStatistic extends AManagedObject {
    public static final Parcelable.Creator<ParseStatistic> CREATOR = new Parcelable.Creator<ParseStatistic>() {
        public ParseStatistic createFromParcel(Parcel in) {
            return new ParseStatistic(in);
        }

        public ParseStatistic[] newArray(int size) {
            return new ParseStatistic[size];
        }
    };
    private Integer avgExecTime;
    private Integer avgFileDepth;
    private Integer avgFileSize;
    private String description;
    private String docType;
    private Integer fileNum;
    private Boolean hasReported;
    private Integer id;
    private Integer lifeTime;
    private Integer maxExecTime;
    private Integer maxFileDepth;
    private Integer maxFileSize;
    private Integer minExecTime;
    private Long reportTime;

    public ParseStatistic(Cursor cursor) {
        Boolean bool = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.docType = cursor.getString(2);
        this.fileNum = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.maxExecTime = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.minExecTime = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.avgExecTime = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.maxFileSize = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.avgFileSize = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.maxFileDepth = cursor.isNull(9) ? null : Integer.valueOf(cursor.getInt(9));
        this.avgFileDepth = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        this.lifeTime = cursor.isNull(11) ? null : Integer.valueOf(cursor.getInt(11));
        this.description = cursor.getString(12);
        this.reportTime = cursor.isNull(13) ? null : Long.valueOf(cursor.getLong(13));
        if (!cursor.isNull(14)) {
            bool = Boolean.valueOf(cursor.getInt(14) != 0);
        }
        this.hasReported = bool;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ParseStatistic(Parcel in) {
        super(in);
        Boolean bool = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.docType = in.readByte() == 0 ? null : in.readString();
        this.fileNum = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.maxExecTime = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.minExecTime = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.avgExecTime = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.maxFileSize = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.avgFileSize = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.maxFileDepth = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.avgFileDepth = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.lifeTime = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.description = in.readByte() == 0 ? null : in.readString();
        this.reportTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        if (in.readByte() != 0) {
            bool = Boolean.valueOf(in.readByte() != 0);
        }
        this.hasReported = bool;
    }

    private ParseStatistic(Integer id2, String docType2, Integer fileNum2, Integer maxExecTime2, Integer minExecTime2, Integer avgExecTime2, Integer maxFileSize2, Integer avgFileSize2, Integer maxFileDepth2, Integer avgFileDepth2, Integer lifeTime2, String description2, Long reportTime2, Boolean hasReported2) {
        this.id = id2;
        this.docType = docType2;
        this.fileNum = fileNum2;
        this.maxExecTime = maxExecTime2;
        this.minExecTime = minExecTime2;
        this.avgExecTime = avgExecTime2;
        this.maxFileSize = maxFileSize2;
        this.avgFileSize = avgFileSize2;
        this.maxFileDepth = maxFileDepth2;
        this.avgFileDepth = avgFileDepth2;
        this.lifeTime = lifeTime2;
        this.description = description2;
        this.reportTime = reportTime2;
        this.hasReported = hasReported2;
    }

    public ParseStatistic() {
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

    public String getDocType() {
        return this.docType;
    }

    public void setDocType(String docType2) {
        this.docType = docType2;
        setValue();
    }

    public Integer getFileNum() {
        return this.fileNum;
    }

    public void setFileNum(Integer fileNum2) {
        this.fileNum = fileNum2;
        setValue();
    }

    public Integer getMaxExecTime() {
        return this.maxExecTime;
    }

    public void setMaxExecTime(Integer maxExecTime2) {
        this.maxExecTime = maxExecTime2;
        setValue();
    }

    public Integer getMinExecTime() {
        return this.minExecTime;
    }

    public void setMinExecTime(Integer minExecTime2) {
        this.minExecTime = minExecTime2;
        setValue();
    }

    public Integer getAvgExecTime() {
        return this.avgExecTime;
    }

    public void setAvgExecTime(Integer avgExecTime2) {
        this.avgExecTime = avgExecTime2;
        setValue();
    }

    public Integer getMaxFileSize() {
        return this.maxFileSize;
    }

    public void setMaxFileSize(Integer maxFileSize2) {
        this.maxFileSize = maxFileSize2;
        setValue();
    }

    public Integer getAvgFileSize() {
        return this.avgFileSize;
    }

    public void setAvgFileSize(Integer avgFileSize2) {
        this.avgFileSize = avgFileSize2;
        setValue();
    }

    public Integer getMaxFileDepth() {
        return this.maxFileDepth;
    }

    public void setMaxFileDepth(Integer maxFileDepth2) {
        this.maxFileDepth = maxFileDepth2;
        setValue();
    }

    public Integer getAvgFileDepth() {
        return this.avgFileDepth;
    }

    public void setAvgFileDepth(Integer avgFileDepth2) {
        this.avgFileDepth = avgFileDepth2;
        setValue();
    }

    public Integer getLifeTime() {
        return this.lifeTime;
    }

    public void setLifeTime(Integer lifeTime2) {
        this.lifeTime = lifeTime2;
        setValue();
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description2) {
        this.description = description2;
        setValue();
    }

    public Long getReportTime() {
        return this.reportTime;
    }

    public void setReportTime(Long reportTime2) {
        this.reportTime = reportTime2;
        setValue();
    }

    public Boolean getHasReported() {
        return this.hasReported;
    }

    public void setHasReported(Boolean hasReported2) {
        this.hasReported = hasReported2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        byte b = 1;
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.docType != null) {
            out.writeByte((byte) 1);
            out.writeString(this.docType);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.fileNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.fileNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.maxExecTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.maxExecTime.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.minExecTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.minExecTime.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.avgExecTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.avgExecTime.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.maxFileSize != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.maxFileSize.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.avgFileSize != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.avgFileSize.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.maxFileDepth != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.maxFileDepth.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.avgFileDepth != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.avgFileDepth.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.lifeTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.lifeTime.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.description != null) {
            out.writeByte((byte) 1);
            out.writeString(this.description);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reportTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.reportTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.hasReported != null) {
            out.writeByte((byte) 1);
            if (!this.hasReported.booleanValue()) {
                b = 0;
            }
            out.writeByte(b);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<ParseStatistic> getHelper() {
        return ParseStatisticHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.search.ParseStatistic";
    }

    public String getDatabaseName() {
        return "dsSearch";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ParseStatistic { id: ").append(this.id);
        sb.append(", docType: ").append(this.docType);
        sb.append(", fileNum: ").append(this.fileNum);
        sb.append(", maxExecTime: ").append(this.maxExecTime);
        sb.append(", minExecTime: ").append(this.minExecTime);
        sb.append(", avgExecTime: ").append(this.avgExecTime);
        sb.append(", maxFileSize: ").append(this.maxFileSize);
        sb.append(", avgFileSize: ").append(this.avgFileSize);
        sb.append(", maxFileDepth: ").append(this.maxFileDepth);
        sb.append(", avgFileDepth: ").append(this.avgFileDepth);
        sb.append(", lifeTime: ").append(this.lifeTime);
        sb.append(", description: ").append(this.description);
        sb.append(", reportTime: ").append(this.reportTime);
        sb.append(", hasReported: ").append(this.hasReported);
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
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
