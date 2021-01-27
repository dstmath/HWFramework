package com.huawei.nb.model.geofence;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class MySpace extends AManagedObject {
    public static final Parcelable.Creator<MySpace> CREATOR = new Parcelable.Creator<MySpace>() {
        /* class com.huawei.nb.model.geofence.MySpace.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MySpace createFromParcel(Parcel parcel) {
            return new MySpace(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MySpace[] newArray(int i) {
            return new MySpace[i];
        }
    };
    private String allAOI;
    private String aoi1;
    private Float aoi1Confi;
    private String aoi2;
    private Float aoi2Confi;
    private String aoi3;
    private Float aoi3Confi;
    private String aoi4;
    private Float aoi4Confi;
    private String aoi5;
    private Float aoi5Confi;
    private String birthCity;
    private String childrenSchool;
    private String currentCity;
    private String familiarCities;
    private String friends;
    private String fun;
    private String gym;
    private String home;
    private String homeCity;
    private Float homeCityConfi;
    private Float homeConfi;
    private Long mID;
    private String meal;
    private String work;
    private String workCity;
    private Float workCityConfi;
    private Float workConfi;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.geofence.MySpace";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public MySpace(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Float f = null;
        this.mID = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.home = cursor.getString(2);
        this.work = cursor.getString(3);
        this.aoi1 = cursor.getString(4);
        this.aoi2 = cursor.getString(5);
        this.aoi3 = cursor.getString(6);
        this.aoi4 = cursor.getString(7);
        this.aoi5 = cursor.getString(8);
        this.homeConfi = cursor.isNull(9) ? null : Float.valueOf(cursor.getFloat(9));
        this.workConfi = cursor.isNull(10) ? null : Float.valueOf(cursor.getFloat(10));
        this.aoi1Confi = cursor.isNull(11) ? null : Float.valueOf(cursor.getFloat(11));
        this.aoi2Confi = cursor.isNull(12) ? null : Float.valueOf(cursor.getFloat(12));
        this.aoi3Confi = cursor.isNull(13) ? null : Float.valueOf(cursor.getFloat(13));
        this.aoi4Confi = cursor.isNull(14) ? null : Float.valueOf(cursor.getFloat(14));
        this.aoi5Confi = cursor.isNull(15) ? null : Float.valueOf(cursor.getFloat(15));
        this.meal = cursor.getString(16);
        this.fun = cursor.getString(17);
        this.childrenSchool = cursor.getString(18);
        this.friends = cursor.getString(19);
        this.gym = cursor.getString(20);
        this.allAOI = cursor.getString(21);
        this.familiarCities = cursor.getString(22);
        this.homeCity = cursor.getString(23);
        this.homeCityConfi = cursor.isNull(24) ? null : Float.valueOf(cursor.getFloat(24));
        this.workCity = cursor.getString(25);
        this.workCityConfi = !cursor.isNull(26) ? Float.valueOf(cursor.getFloat(26)) : f;
        this.birthCity = cursor.getString(27);
        this.currentCity = cursor.getString(28);
    }

    public MySpace(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mID = null;
            parcel.readLong();
        } else {
            this.mID = Long.valueOf(parcel.readLong());
        }
        this.home = parcel.readByte() == 0 ? null : parcel.readString();
        this.work = parcel.readByte() == 0 ? null : parcel.readString();
        this.aoi1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.aoi2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.aoi3 = parcel.readByte() == 0 ? null : parcel.readString();
        this.aoi4 = parcel.readByte() == 0 ? null : parcel.readString();
        this.aoi5 = parcel.readByte() == 0 ? null : parcel.readString();
        this.homeConfi = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.workConfi = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.aoi1Confi = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.aoi2Confi = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.aoi3Confi = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.aoi4Confi = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.aoi5Confi = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.meal = parcel.readByte() == 0 ? null : parcel.readString();
        this.fun = parcel.readByte() == 0 ? null : parcel.readString();
        this.childrenSchool = parcel.readByte() == 0 ? null : parcel.readString();
        this.friends = parcel.readByte() == 0 ? null : parcel.readString();
        this.gym = parcel.readByte() == 0 ? null : parcel.readString();
        this.allAOI = parcel.readByte() == 0 ? null : parcel.readString();
        this.familiarCities = parcel.readByte() == 0 ? null : parcel.readString();
        this.homeCity = parcel.readByte() == 0 ? null : parcel.readString();
        this.homeCityConfi = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.workCity = parcel.readByte() == 0 ? null : parcel.readString();
        this.workCityConfi = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.birthCity = parcel.readByte() == 0 ? null : parcel.readString();
        this.currentCity = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MySpace(Long l, String str, String str2, String str3, String str4, String str5, String str6, String str7, Float f, Float f2, Float f3, Float f4, Float f5, Float f6, Float f7, String str8, String str9, String str10, String str11, String str12, String str13, String str14, String str15, Float f8, String str16, Float f9, String str17, String str18) {
        this.mID = l;
        this.home = str;
        this.work = str2;
        this.aoi1 = str3;
        this.aoi2 = str4;
        this.aoi3 = str5;
        this.aoi4 = str6;
        this.aoi5 = str7;
        this.homeConfi = f;
        this.workConfi = f2;
        this.aoi1Confi = f3;
        this.aoi2Confi = f4;
        this.aoi3Confi = f5;
        this.aoi4Confi = f6;
        this.aoi5Confi = f7;
        this.meal = str8;
        this.fun = str9;
        this.childrenSchool = str10;
        this.friends = str11;
        this.gym = str12;
        this.allAOI = str13;
        this.familiarCities = str14;
        this.homeCity = str15;
        this.homeCityConfi = f8;
        this.workCity = str16;
        this.workCityConfi = f9;
        this.birthCity = str17;
        this.currentCity = str18;
    }

    public MySpace() {
    }

    public Long getMID() {
        return this.mID;
    }

    public void setMID(Long l) {
        this.mID = l;
        setValue();
    }

    public String getHome() {
        return this.home;
    }

    public void setHome(String str) {
        this.home = str;
        setValue();
    }

    public String getWork() {
        return this.work;
    }

    public void setWork(String str) {
        this.work = str;
        setValue();
    }

    public String getAoi1() {
        return this.aoi1;
    }

    public void setAoi1(String str) {
        this.aoi1 = str;
        setValue();
    }

    public String getAoi2() {
        return this.aoi2;
    }

    public void setAoi2(String str) {
        this.aoi2 = str;
        setValue();
    }

    public String getAoi3() {
        return this.aoi3;
    }

    public void setAoi3(String str) {
        this.aoi3 = str;
        setValue();
    }

    public String getAoi4() {
        return this.aoi4;
    }

    public void setAoi4(String str) {
        this.aoi4 = str;
        setValue();
    }

    public String getAoi5() {
        return this.aoi5;
    }

    public void setAoi5(String str) {
        this.aoi5 = str;
        setValue();
    }

    public Float getHomeConfi() {
        return this.homeConfi;
    }

    public void setHomeConfi(Float f) {
        this.homeConfi = f;
        setValue();
    }

    public Float getWorkConfi() {
        return this.workConfi;
    }

    public void setWorkConfi(Float f) {
        this.workConfi = f;
        setValue();
    }

    public Float getAoi1Confi() {
        return this.aoi1Confi;
    }

    public void setAoi1Confi(Float f) {
        this.aoi1Confi = f;
        setValue();
    }

    public Float getAoi2Confi() {
        return this.aoi2Confi;
    }

    public void setAoi2Confi(Float f) {
        this.aoi2Confi = f;
        setValue();
    }

    public Float getAoi3Confi() {
        return this.aoi3Confi;
    }

    public void setAoi3Confi(Float f) {
        this.aoi3Confi = f;
        setValue();
    }

    public Float getAoi4Confi() {
        return this.aoi4Confi;
    }

    public void setAoi4Confi(Float f) {
        this.aoi4Confi = f;
        setValue();
    }

    public Float getAoi5Confi() {
        return this.aoi5Confi;
    }

    public void setAoi5Confi(Float f) {
        this.aoi5Confi = f;
        setValue();
    }

    public String getMeal() {
        return this.meal;
    }

    public void setMeal(String str) {
        this.meal = str;
        setValue();
    }

    public String getFun() {
        return this.fun;
    }

    public void setFun(String str) {
        this.fun = str;
        setValue();
    }

    public String getChildrenSchool() {
        return this.childrenSchool;
    }

    public void setChildrenSchool(String str) {
        this.childrenSchool = str;
        setValue();
    }

    public String getFriends() {
        return this.friends;
    }

    public void setFriends(String str) {
        this.friends = str;
        setValue();
    }

    public String getGym() {
        return this.gym;
    }

    public void setGym(String str) {
        this.gym = str;
        setValue();
    }

    public String getAllAOI() {
        return this.allAOI;
    }

    public void setAllAOI(String str) {
        this.allAOI = str;
        setValue();
    }

    public String getFamiliarCities() {
        return this.familiarCities;
    }

    public void setFamiliarCities(String str) {
        this.familiarCities = str;
        setValue();
    }

    public String getHomeCity() {
        return this.homeCity;
    }

    public void setHomeCity(String str) {
        this.homeCity = str;
        setValue();
    }

    public Float getHomeCityConfi() {
        return this.homeCityConfi;
    }

    public void setHomeCityConfi(Float f) {
        this.homeCityConfi = f;
        setValue();
    }

    public String getWorkCity() {
        return this.workCity;
    }

    public void setWorkCity(String str) {
        this.workCity = str;
        setValue();
    }

    public Float getWorkCityConfi() {
        return this.workCityConfi;
    }

    public void setWorkCityConfi(Float f) {
        this.workCityConfi = f;
        setValue();
    }

    public String getBirthCity() {
        return this.birthCity;
    }

    public void setBirthCity(String str) {
        this.birthCity = str;
        setValue();
    }

    public String getCurrentCity() {
        return this.currentCity;
    }

    public void setCurrentCity(String str) {
        this.currentCity = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.mID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mID.longValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeLong(1);
        }
        if (this.home != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.home);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.work != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.work);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.aoi1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.aoi1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.aoi2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.aoi2);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.aoi3 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.aoi3);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.aoi4 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.aoi4);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.aoi5 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.aoi5);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.homeConfi != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.homeConfi.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.workConfi != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.workConfi.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.aoi1Confi != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.aoi1Confi.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.aoi2Confi != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.aoi2Confi.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.aoi3Confi != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.aoi3Confi.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.aoi4Confi != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.aoi4Confi.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.aoi5Confi != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.aoi5Confi.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.meal != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.meal);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.fun != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.fun);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.childrenSchool != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.childrenSchool);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.friends != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.friends);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.gym != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.gym);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.allAOI != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.allAOI);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.familiarCities != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.familiarCities);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.homeCity != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.homeCity);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.homeCityConfi != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.homeCityConfi.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.workCity != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.workCity);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.workCityConfi != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.workCityConfi.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.birthCity != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.birthCity);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.currentCity != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.currentCity);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<MySpace> getHelper() {
        return MySpaceHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MySpace { mID: " + this.mID + ", home: " + this.home + ", work: " + this.work + ", aoi1: " + this.aoi1 + ", aoi2: " + this.aoi2 + ", aoi3: " + this.aoi3 + ", aoi4: " + this.aoi4 + ", aoi5: " + this.aoi5 + ", homeConfi: " + this.homeConfi + ", workConfi: " + this.workConfi + ", aoi1Confi: " + this.aoi1Confi + ", aoi2Confi: " + this.aoi2Confi + ", aoi3Confi: " + this.aoi3Confi + ", aoi4Confi: " + this.aoi4Confi + ", aoi5Confi: " + this.aoi5Confi + ", meal: " + this.meal + ", fun: " + this.fun + ", childrenSchool: " + this.childrenSchool + ", friends: " + this.friends + ", gym: " + this.gym + ", allAOI: " + this.allAOI + ", familiarCities: " + this.familiarCities + ", homeCity: " + this.homeCity + ", homeCityConfi: " + this.homeCityConfi + ", workCity: " + this.workCity + ", workCityConfi: " + this.workCityConfi + ", birthCity: " + this.birthCity + ", currentCity: " + this.currentCity + " }";
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }
}
