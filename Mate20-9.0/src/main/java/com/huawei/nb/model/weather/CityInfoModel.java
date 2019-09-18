package com.huawei.nb.model.weather;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CityInfoModel extends AManagedObject {
    public static final Parcelable.Creator<CityInfoModel> CREATOR = new Parcelable.Creator<CityInfoModel>() {
        public CityInfoModel createFromParcel(Parcel in) {
            return new CityInfoModel(in);
        }

        public CityInfoModel[] newArray(int size) {
            return new CityInfoModel[size];
        }
    };
    private Long _id;
    private String ca;
    private String city_alias;
    private String city_code;
    private String city_name;
    private String city_native;
    private int city_type;
    private String co;
    private String country_name;
    private String country_name_cn;
    private int home_city;
    private String hw_id;
    private long insert_time;
    private int manual_set;
    private String province_name;
    private String province_name_cn;
    private long sequence_id = -1;
    private String state_name;
    private String state_name_cn;
    private String time_zone;
    private long weather_id;

    public CityInfoModel(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this._id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.city_name = cursor.getString(2);
        this.city_alias = cursor.getString(3);
        this.city_native = cursor.getString(4);
        this.state_name = cursor.getString(5);
        this.city_code = cursor.getString(6);
        this.city_type = cursor.getInt(7);
        this.time_zone = cursor.getString(8);
        this.insert_time = cursor.getLong(9);
        this.weather_id = cursor.getLong(10);
        this.manual_set = cursor.getInt(11);
        this.home_city = cursor.getInt(12);
        this.state_name_cn = cursor.getString(13);
        this.province_name = cursor.getString(14);
        this.province_name_cn = cursor.getString(15);
        this.country_name = cursor.getString(16);
        this.country_name_cn = cursor.getString(17);
        this.hw_id = cursor.getString(18);
        this.co = cursor.getString(19);
        this.ca = cursor.getString(20);
        this.sequence_id = cursor.getLong(21);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CityInfoModel(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this._id = null;
            in.readLong();
        } else {
            this._id = Long.valueOf(in.readLong());
        }
        this.city_name = in.readByte() == 0 ? null : in.readString();
        this.city_alias = in.readByte() == 0 ? null : in.readString();
        this.city_native = in.readByte() == 0 ? null : in.readString();
        this.state_name = in.readByte() == 0 ? null : in.readString();
        this.city_code = in.readByte() == 0 ? null : in.readString();
        this.city_type = in.readInt();
        this.time_zone = in.readByte() == 0 ? null : in.readString();
        this.insert_time = in.readLong();
        this.weather_id = in.readLong();
        this.manual_set = in.readInt();
        this.home_city = in.readInt();
        this.state_name_cn = in.readByte() == 0 ? null : in.readString();
        this.province_name = in.readByte() == 0 ? null : in.readString();
        this.province_name_cn = in.readByte() == 0 ? null : in.readString();
        this.country_name = in.readByte() == 0 ? null : in.readString();
        this.country_name_cn = in.readByte() == 0 ? null : in.readString();
        this.hw_id = in.readByte() == 0 ? null : in.readString();
        this.co = in.readByte() == 0 ? null : in.readString();
        this.ca = in.readByte() != 0 ? in.readString() : str;
        this.sequence_id = in.readLong();
    }

    private CityInfoModel(Long _id2, String city_name2, String city_alias2, String city_native2, String state_name2, String city_code2, int city_type2, String time_zone2, long insert_time2, long weather_id2, int manual_set2, int home_city2, String state_name_cn2, String province_name2, String province_name_cn2, String country_name2, String country_name_cn2, String hw_id2, String co2, String ca2, long sequence_id2) {
        this._id = _id2;
        this.city_name = city_name2;
        this.city_alias = city_alias2;
        this.city_native = city_native2;
        this.state_name = state_name2;
        this.city_code = city_code2;
        this.city_type = city_type2;
        this.time_zone = time_zone2;
        this.insert_time = insert_time2;
        this.weather_id = weather_id2;
        this.manual_set = manual_set2;
        this.home_city = home_city2;
        this.state_name_cn = state_name_cn2;
        this.province_name = province_name2;
        this.province_name_cn = province_name_cn2;
        this.country_name = country_name2;
        this.country_name_cn = country_name_cn2;
        this.hw_id = hw_id2;
        this.co = co2;
        this.ca = ca2;
        this.sequence_id = sequence_id2;
    }

    public CityInfoModel() {
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

    public String getCity_name() {
        return this.city_name;
    }

    public void setCity_name(String city_name2) {
        this.city_name = city_name2;
        setValue();
    }

    public String getCity_alias() {
        return this.city_alias;
    }

    public void setCity_alias(String city_alias2) {
        this.city_alias = city_alias2;
        setValue();
    }

    public String getCity_native() {
        return this.city_native;
    }

    public void setCity_native(String city_native2) {
        this.city_native = city_native2;
        setValue();
    }

    public String getState_name() {
        return this.state_name;
    }

    public void setState_name(String state_name2) {
        this.state_name = state_name2;
        setValue();
    }

    public String getCity_code() {
        return this.city_code;
    }

    public void setCity_code(String city_code2) {
        this.city_code = city_code2;
        setValue();
    }

    public int getCity_type() {
        return this.city_type;
    }

    public void setCity_type(int city_type2) {
        this.city_type = city_type2;
        setValue();
    }

    public String getTime_zone() {
        return this.time_zone;
    }

    public void setTime_zone(String time_zone2) {
        this.time_zone = time_zone2;
        setValue();
    }

    public long getInsert_time() {
        return this.insert_time;
    }

    public void setInsert_time(long insert_time2) {
        this.insert_time = insert_time2;
        setValue();
    }

    public long getWeather_id() {
        return this.weather_id;
    }

    public void setWeather_id(long weather_id2) {
        this.weather_id = weather_id2;
        setValue();
    }

    public int getManual_set() {
        return this.manual_set;
    }

    public void setManual_set(int manual_set2) {
        this.manual_set = manual_set2;
        setValue();
    }

    public int getHome_city() {
        return this.home_city;
    }

    public void setHome_city(int home_city2) {
        this.home_city = home_city2;
        setValue();
    }

    public String getState_name_cn() {
        return this.state_name_cn;
    }

    public void setState_name_cn(String state_name_cn2) {
        this.state_name_cn = state_name_cn2;
        setValue();
    }

    public String getProvince_name() {
        return this.province_name;
    }

    public void setProvince_name(String province_name2) {
        this.province_name = province_name2;
        setValue();
    }

    public String getProvince_name_cn() {
        return this.province_name_cn;
    }

    public void setProvince_name_cn(String province_name_cn2) {
        this.province_name_cn = province_name_cn2;
        setValue();
    }

    public String getCountry_name() {
        return this.country_name;
    }

    public void setCountry_name(String country_name2) {
        this.country_name = country_name2;
        setValue();
    }

    public String getCountry_name_cn() {
        return this.country_name_cn;
    }

    public void setCountry_name_cn(String country_name_cn2) {
        this.country_name_cn = country_name_cn2;
        setValue();
    }

    public String getHw_id() {
        return this.hw_id;
    }

    public void setHw_id(String hw_id2) {
        this.hw_id = hw_id2;
        setValue();
    }

    public String getCo() {
        return this.co;
    }

    public void setCo(String co2) {
        this.co = co2;
        setValue();
    }

    public String getCa() {
        return this.ca;
    }

    public void setCa(String ca2) {
        this.ca = ca2;
        setValue();
    }

    public long getSequence_id() {
        return this.sequence_id;
    }

    public void setSequence_id(long sequence_id2) {
        this.sequence_id = sequence_id2;
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
        if (this.city_name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.city_name);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.city_alias != null) {
            out.writeByte((byte) 1);
            out.writeString(this.city_alias);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.city_native != null) {
            out.writeByte((byte) 1);
            out.writeString(this.city_native);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.state_name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.state_name);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.city_code != null) {
            out.writeByte((byte) 1);
            out.writeString(this.city_code);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeInt(this.city_type);
        if (this.time_zone != null) {
            out.writeByte((byte) 1);
            out.writeString(this.time_zone);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeLong(this.insert_time);
        out.writeLong(this.weather_id);
        out.writeInt(this.manual_set);
        out.writeInt(this.home_city);
        if (this.state_name_cn != null) {
            out.writeByte((byte) 1);
            out.writeString(this.state_name_cn);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.province_name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.province_name);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.province_name_cn != null) {
            out.writeByte((byte) 1);
            out.writeString(this.province_name_cn);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.country_name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.country_name);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.country_name_cn != null) {
            out.writeByte((byte) 1);
            out.writeString(this.country_name_cn);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.hw_id != null) {
            out.writeByte((byte) 1);
            out.writeString(this.hw_id);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.co != null) {
            out.writeByte((byte) 1);
            out.writeString(this.co);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.ca != null) {
            out.writeByte((byte) 1);
            out.writeString(this.ca);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeLong(this.sequence_id);
    }

    public AEntityHelper<CityInfoModel> getHelper() {
        return CityInfoModelHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.weather.CityInfoModel";
    }

    public String getDatabaseName() {
        return "dsWeather";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CityInfoModel { _id: ").append(this._id);
        sb.append(", city_name: ").append(this.city_name);
        sb.append(", city_alias: ").append(this.city_alias);
        sb.append(", city_native: ").append(this.city_native);
        sb.append(", state_name: ").append(this.state_name);
        sb.append(", city_code: ").append(this.city_code);
        sb.append(", city_type: ").append(this.city_type);
        sb.append(", time_zone: ").append(this.time_zone);
        sb.append(", insert_time: ").append(this.insert_time);
        sb.append(", weather_id: ").append(this.weather_id);
        sb.append(", manual_set: ").append(this.manual_set);
        sb.append(", home_city: ").append(this.home_city);
        sb.append(", state_name_cn: ").append(this.state_name_cn);
        sb.append(", province_name: ").append(this.province_name);
        sb.append(", province_name_cn: ").append(this.province_name_cn);
        sb.append(", country_name: ").append(this.country_name);
        sb.append(", country_name_cn: ").append(this.country_name_cn);
        sb.append(", hw_id: ").append(this.hw_id);
        sb.append(", co: ").append(this.co);
        sb.append(", ca: ").append(this.ca);
        sb.append(", sequence_id: ").append(this.sequence_id);
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
