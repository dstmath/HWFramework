package com.huawei.nb.model.weather;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherAlarmModel extends AManagedObject {
    public static final Parcelable.Creator<WeatherAlarmModel> CREATOR = new Parcelable.Creator<WeatherAlarmModel>() {
        public WeatherAlarmModel createFromParcel(Parcel in) {
            return new WeatherAlarmModel(in);
        }

        public WeatherAlarmModel[] newArray(int size) {
            return new WeatherAlarmModel[size];
        }
    };
    private Long _id;
    private String alarm_content;
    private String alarm_id;
    private int alarm_type;
    private String alarm_type_name;
    private String city_name;
    private String county_name;
    private int level;
    private String level_name;
    private long observationtime;
    private String province_name;
    private long weather_id;

    public WeatherAlarmModel(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this._id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.weather_id = cursor.getLong(2);
        this.alarm_id = cursor.getString(3);
        this.province_name = cursor.getString(4);
        this.city_name = cursor.getString(5);
        this.county_name = cursor.getString(6);
        this.alarm_type = cursor.getInt(7);
        this.alarm_type_name = cursor.getString(8);
        this.level = cursor.getInt(9);
        this.level_name = cursor.getString(10);
        this.observationtime = cursor.getLong(11);
        this.alarm_content = cursor.getString(12);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public WeatherAlarmModel(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this._id = null;
            in.readLong();
        } else {
            this._id = Long.valueOf(in.readLong());
        }
        this.weather_id = in.readLong();
        this.alarm_id = in.readByte() == 0 ? null : in.readString();
        this.province_name = in.readByte() == 0 ? null : in.readString();
        this.city_name = in.readByte() == 0 ? null : in.readString();
        this.county_name = in.readByte() == 0 ? null : in.readString();
        this.alarm_type = in.readInt();
        this.alarm_type_name = in.readByte() == 0 ? null : in.readString();
        this.level = in.readInt();
        this.level_name = in.readByte() == 0 ? null : in.readString();
        this.observationtime = in.readLong();
        this.alarm_content = in.readByte() != 0 ? in.readString() : str;
    }

    private WeatherAlarmModel(Long _id2, long weather_id2, String alarm_id2, String province_name2, String city_name2, String county_name2, int alarm_type2, String alarm_type_name2, int level2, String level_name2, long observationtime2, String alarm_content2) {
        this._id = _id2;
        this.weather_id = weather_id2;
        this.alarm_id = alarm_id2;
        this.province_name = province_name2;
        this.city_name = city_name2;
        this.county_name = county_name2;
        this.alarm_type = alarm_type2;
        this.alarm_type_name = alarm_type_name2;
        this.level = level2;
        this.level_name = level_name2;
        this.observationtime = observationtime2;
        this.alarm_content = alarm_content2;
    }

    public WeatherAlarmModel() {
    }

    public int describeContents() {
        return 0;
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long _id2) {
        this._id = _id2;
        setValue();
    }

    public long getWeather_id() {
        return this.weather_id;
    }

    public void setWeather_id(long weather_id2) {
        this.weather_id = weather_id2;
        setValue();
    }

    public String getAlarm_id() {
        return this.alarm_id;
    }

    public void setAlarm_id(String alarm_id2) {
        this.alarm_id = alarm_id2;
        setValue();
    }

    public String getProvince_name() {
        return this.province_name;
    }

    public void setProvince_name(String province_name2) {
        this.province_name = province_name2;
        setValue();
    }

    public String getCity_name() {
        return this.city_name;
    }

    public void setCity_name(String city_name2) {
        this.city_name = city_name2;
        setValue();
    }

    public String getCounty_name() {
        return this.county_name;
    }

    public void setCounty_name(String county_name2) {
        this.county_name = county_name2;
        setValue();
    }

    public int getAlarm_type() {
        return this.alarm_type;
    }

    public void setAlarm_type(int alarm_type2) {
        this.alarm_type = alarm_type2;
        setValue();
    }

    public String getAlarm_type_name() {
        return this.alarm_type_name;
    }

    public void setAlarm_type_name(String alarm_type_name2) {
        this.alarm_type_name = alarm_type_name2;
        setValue();
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level2) {
        this.level = level2;
        setValue();
    }

    public String getLevel_name() {
        return this.level_name;
    }

    public void setLevel_name(String level_name2) {
        this.level_name = level_name2;
        setValue();
    }

    public long getObservationtime() {
        return this.observationtime;
    }

    public void setObservationtime(long observationtime2) {
        this.observationtime = observationtime2;
        setValue();
    }

    public String getAlarm_content() {
        return this.alarm_content;
    }

    public void setAlarm_content(String alarm_content2) {
        this.alarm_content = alarm_content2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this._id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this._id.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        out.writeLong(this.weather_id);
        if (this.alarm_id != null) {
            out.writeByte((byte) 1);
            out.writeString(this.alarm_id);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.province_name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.province_name);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.city_name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.city_name);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.county_name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.county_name);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeInt(this.alarm_type);
        if (this.alarm_type_name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.alarm_type_name);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeInt(this.level);
        if (this.level_name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.level_name);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeLong(this.observationtime);
        if (this.alarm_content != null) {
            out.writeByte((byte) 1);
            out.writeString(this.alarm_content);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<WeatherAlarmModel> getHelper() {
        return WeatherAlarmModelHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.weather.WeatherAlarmModel";
    }

    public String getDatabaseName() {
        return "dsWeather";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("WeatherAlarmModel { _id: ").append(this._id);
        sb.append(", weather_id: ").append(this.weather_id);
        sb.append(", alarm_id: ").append(this.alarm_id);
        sb.append(", province_name: ").append(this.province_name);
        sb.append(", city_name: ").append(this.city_name);
        sb.append(", county_name: ").append(this.county_name);
        sb.append(", alarm_type: ").append(this.alarm_type);
        sb.append(", alarm_type_name: ").append(this.alarm_type_name);
        sb.append(", level: ").append(this.level);
        sb.append(", level_name: ").append(this.level_name);
        sb.append(", observationtime: ").append(this.observationtime);
        sb.append(", alarm_content: ").append(this.alarm_content);
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
        return "0.0.17";
    }

    public int getDatabaseVersionCode() {
        return 17;
    }

    public String getEntityVersion() {
        return "0.0.14";
    }

    public int getEntityVersionCode() {
        return 14;
    }
}
