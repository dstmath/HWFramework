package com.huawei.nb.model.profile;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ShortPeriodUserProfile extends AManagedObject {
    public static final Parcelable.Creator<ShortPeriodUserProfile> CREATOR = new Parcelable.Creator<ShortPeriodUserProfile>() {
        /* class com.huawei.nb.model.profile.ShortPeriodUserProfile.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ShortPeriodUserProfile createFromParcel(Parcel parcel) {
            return new ShortPeriodUserProfile(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ShortPeriodUserProfile[] newArray(int i) {
            return new ShortPeriodUserProfile[i];
        }
    };
    private String accomCellId;
    private String accomCellLac;
    private String accomLatitude;
    private String accomLongtitude;
    private Double age;
    private String alarmTime_Workday;
    private String arriveHomeTime;
    private String arriveWorkplaceTime;
    private String callingDuration;
    private String callingTimes;
    private String city;
    private String contactNumber;
    private String deviceToken;
    private String gameTime;
    private String genDate;
    private String hwId;
    private Integer id;
    private String imei;
    private String leaveHomeTime;
    private String leaveWorkplaceTime;
    private String musicNumber;
    private String musicNumber_Before2000;
    private String offTime;
    private String onTime;
    private String photoNumber;
    private String photoNumber_Self;
    private String photoNumber_Turism;
    private String poi1CellId;
    private String poi1CellLac;
    private String poi1Latitude;
    private String poi1Longtitude;
    private String poi2CellId;
    private String poi2CellLac;
    private String poi2Latitude;
    private String poi2Longtitude;
    private String poi3CellId;
    private String poi3CellLac;
    private String poi3Latitude;
    private String poi3Longtitude;
    private String poi4CellId;
    private String poi4CellLac;
    private String poi4Latitude;
    private String poi4Longtitude;
    private String poi5CellId;
    private String poi5CellLac;
    private String poi5Latitude;
    private String poi5Longtitude;
    private String roamingRadius;
    private Double sexuality;
    private Integer sn;
    private String top10Tag;
    private String top1Tag;
    private String top2Tag;
    private String top3Tag;
    private String top4Tag;
    private String top5Tag;
    private String top6Tag;
    private String top7Tag;
    private String top8Tag;
    private String top9Tag;
    private String topMode;
    private String videoTime;
    private String workCellId;
    private String workCellLac;
    private String workLatitude;
    private String workLongtitude;

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
        return "com.huawei.nb.model.profile.ShortPeriodUserProfile";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public ShortPeriodUserProfile(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Double d = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.sn = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.genDate = cursor.getString(3);
        this.deviceToken = cursor.getString(4);
        this.hwId = cursor.getString(5);
        this.imei = cursor.getString(6);
        this.sexuality = cursor.isNull(7) ? null : Double.valueOf(cursor.getDouble(7));
        this.age = !cursor.isNull(8) ? Double.valueOf(cursor.getDouble(8)) : d;
        this.contactNumber = cursor.getString(9);
        this.accomLongtitude = cursor.getString(10);
        this.accomLatitude = cursor.getString(11);
        this.accomCellId = cursor.getString(12);
        this.accomCellLac = cursor.getString(13);
        this.workLongtitude = cursor.getString(14);
        this.workLatitude = cursor.getString(15);
        this.workCellId = cursor.getString(16);
        this.workCellLac = cursor.getString(17);
        this.poi1Longtitude = cursor.getString(18);
        this.poi1Latitude = cursor.getString(19);
        this.poi1CellId = cursor.getString(20);
        this.poi1CellLac = cursor.getString(21);
        this.poi2Longtitude = cursor.getString(22);
        this.poi2Latitude = cursor.getString(23);
        this.poi2CellId = cursor.getString(24);
        this.poi2CellLac = cursor.getString(25);
        this.poi3Longtitude = cursor.getString(26);
        this.poi3Latitude = cursor.getString(27);
        this.poi3CellId = cursor.getString(28);
        this.poi3CellLac = cursor.getString(29);
        this.poi4Longtitude = cursor.getString(30);
        this.poi4Latitude = cursor.getString(31);
        this.poi4CellId = cursor.getString(32);
        this.poi4CellLac = cursor.getString(33);
        this.poi5Longtitude = cursor.getString(34);
        this.poi5Latitude = cursor.getString(35);
        this.poi5CellId = cursor.getString(36);
        this.poi5CellLac = cursor.getString(37);
        this.roamingRadius = cursor.getString(38);
        this.city = cursor.getString(39);
        this.photoNumber = cursor.getString(40);
        this.photoNumber_Self = cursor.getString(41);
        this.gameTime = cursor.getString(42);
        this.videoTime = cursor.getString(43);
        this.alarmTime_Workday = cursor.getString(44);
        this.callingDuration = cursor.getString(45);
        this.callingTimes = cursor.getString(46);
        this.musicNumber = cursor.getString(47);
        this.musicNumber_Before2000 = cursor.getString(48);
        this.photoNumber_Turism = cursor.getString(49);
        this.topMode = cursor.getString(50);
        this.top1Tag = cursor.getString(51);
        this.top2Tag = cursor.getString(52);
        this.top3Tag = cursor.getString(53);
        this.top4Tag = cursor.getString(54);
        this.top5Tag = cursor.getString(55);
        this.top6Tag = cursor.getString(56);
        this.top7Tag = cursor.getString(57);
        this.top8Tag = cursor.getString(58);
        this.top9Tag = cursor.getString(59);
        this.top10Tag = cursor.getString(60);
        this.onTime = cursor.getString(61);
        this.leaveHomeTime = cursor.getString(62);
        this.arriveWorkplaceTime = cursor.getString(63);
        this.leaveWorkplaceTime = cursor.getString(64);
        this.arriveHomeTime = cursor.getString(65);
        this.offTime = cursor.getString(66);
    }

    public ShortPeriodUserProfile(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.sn = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.genDate = parcel.readByte() == 0 ? null : parcel.readString();
        this.deviceToken = parcel.readByte() == 0 ? null : parcel.readString();
        this.hwId = parcel.readByte() == 0 ? null : parcel.readString();
        this.imei = parcel.readByte() == 0 ? null : parcel.readString();
        this.sexuality = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.age = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.contactNumber = parcel.readByte() == 0 ? null : parcel.readString();
        this.accomLongtitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.accomLatitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.accomCellId = parcel.readByte() == 0 ? null : parcel.readString();
        this.accomCellLac = parcel.readByte() == 0 ? null : parcel.readString();
        this.workLongtitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.workLatitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.workCellId = parcel.readByte() == 0 ? null : parcel.readString();
        this.workCellLac = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi1Longtitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi1Latitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi1CellId = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi1CellLac = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi2Longtitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi2Latitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi2CellId = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi2CellLac = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi3Longtitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi3Latitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi3CellId = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi3CellLac = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi4Longtitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi4Latitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi4CellId = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi4CellLac = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi5Longtitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi5Latitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi5CellId = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi5CellLac = parcel.readByte() == 0 ? null : parcel.readString();
        this.roamingRadius = parcel.readByte() == 0 ? null : parcel.readString();
        this.city = parcel.readByte() == 0 ? null : parcel.readString();
        this.photoNumber = parcel.readByte() == 0 ? null : parcel.readString();
        this.photoNumber_Self = parcel.readByte() == 0 ? null : parcel.readString();
        this.gameTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.videoTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.alarmTime_Workday = parcel.readByte() == 0 ? null : parcel.readString();
        this.callingDuration = parcel.readByte() == 0 ? null : parcel.readString();
        this.callingTimes = parcel.readByte() == 0 ? null : parcel.readString();
        this.musicNumber = parcel.readByte() == 0 ? null : parcel.readString();
        this.musicNumber_Before2000 = parcel.readByte() == 0 ? null : parcel.readString();
        this.photoNumber_Turism = parcel.readByte() == 0 ? null : parcel.readString();
        this.topMode = parcel.readByte() == 0 ? null : parcel.readString();
        this.top1Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top2Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top3Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top4Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top5Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top6Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top7Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top8Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top9Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top10Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.onTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.leaveHomeTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.arriveWorkplaceTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.leaveWorkplaceTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.arriveHomeTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.offTime = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private ShortPeriodUserProfile(Integer num, Integer num2, String str, String str2, String str3, String str4, Double d, Double d2, String str5, String str6, String str7, String str8, String str9, String str10, String str11, String str12, String str13, String str14, String str15, String str16, String str17, String str18, String str19, String str20, String str21, String str22, String str23, String str24, String str25, String str26, String str27, String str28, String str29, String str30, String str31, String str32, String str33, String str34, String str35, String str36, String str37, String str38, String str39, String str40, String str41, String str42, String str43, String str44, String str45, String str46, String str47, String str48, String str49, String str50, String str51, String str52, String str53, String str54, String str55, String str56, String str57, String str58, String str59, String str60, String str61, String str62) {
        this.id = num;
        this.sn = num2;
        this.genDate = str;
        this.deviceToken = str2;
        this.hwId = str3;
        this.imei = str4;
        this.sexuality = d;
        this.age = d2;
        this.contactNumber = str5;
        this.accomLongtitude = str6;
        this.accomLatitude = str7;
        this.accomCellId = str8;
        this.accomCellLac = str9;
        this.workLongtitude = str10;
        this.workLatitude = str11;
        this.workCellId = str12;
        this.workCellLac = str13;
        this.poi1Longtitude = str14;
        this.poi1Latitude = str15;
        this.poi1CellId = str16;
        this.poi1CellLac = str17;
        this.poi2Longtitude = str18;
        this.poi2Latitude = str19;
        this.poi2CellId = str20;
        this.poi2CellLac = str21;
        this.poi3Longtitude = str22;
        this.poi3Latitude = str23;
        this.poi3CellId = str24;
        this.poi3CellLac = str25;
        this.poi4Longtitude = str26;
        this.poi4Latitude = str27;
        this.poi4CellId = str28;
        this.poi4CellLac = str29;
        this.poi5Longtitude = str30;
        this.poi5Latitude = str31;
        this.poi5CellId = str32;
        this.poi5CellLac = str33;
        this.roamingRadius = str34;
        this.city = str35;
        this.photoNumber = str36;
        this.photoNumber_Self = str37;
        this.gameTime = str38;
        this.videoTime = str39;
        this.alarmTime_Workday = str40;
        this.callingDuration = str41;
        this.callingTimes = str42;
        this.musicNumber = str43;
        this.musicNumber_Before2000 = str44;
        this.photoNumber_Turism = str45;
        this.topMode = str46;
        this.top1Tag = str47;
        this.top2Tag = str48;
        this.top3Tag = str49;
        this.top4Tag = str50;
        this.top5Tag = str51;
        this.top6Tag = str52;
        this.top7Tag = str53;
        this.top8Tag = str54;
        this.top9Tag = str55;
        this.top10Tag = str56;
        this.onTime = str57;
        this.leaveHomeTime = str58;
        this.arriveWorkplaceTime = str59;
        this.leaveWorkplaceTime = str60;
        this.arriveHomeTime = str61;
        this.offTime = str62;
    }

    public ShortPeriodUserProfile() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public Integer getSn() {
        return this.sn;
    }

    public void setSn(Integer num) {
        this.sn = num;
        setValue();
    }

    public String getGenDate() {
        return this.genDate;
    }

    public void setGenDate(String str) {
        this.genDate = str;
        setValue();
    }

    public String getDeviceToken() {
        return this.deviceToken;
    }

    public void setDeviceToken(String str) {
        this.deviceToken = str;
        setValue();
    }

    public String getHwId() {
        return this.hwId;
    }

    public void setHwId(String str) {
        this.hwId = str;
        setValue();
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String str) {
        this.imei = str;
        setValue();
    }

    public Double getSexuality() {
        return this.sexuality;
    }

    public void setSexuality(Double d) {
        this.sexuality = d;
        setValue();
    }

    public Double getAge() {
        return this.age;
    }

    public void setAge(Double d) {
        this.age = d;
        setValue();
    }

    public String getContactNumber() {
        return this.contactNumber;
    }

    public void setContactNumber(String str) {
        this.contactNumber = str;
        setValue();
    }

    public String getAccomLongtitude() {
        return this.accomLongtitude;
    }

    public void setAccomLongtitude(String str) {
        this.accomLongtitude = str;
        setValue();
    }

    public String getAccomLatitude() {
        return this.accomLatitude;
    }

    public void setAccomLatitude(String str) {
        this.accomLatitude = str;
        setValue();
    }

    public String getAccomCellId() {
        return this.accomCellId;
    }

    public void setAccomCellId(String str) {
        this.accomCellId = str;
        setValue();
    }

    public String getAccomCellLac() {
        return this.accomCellLac;
    }

    public void setAccomCellLac(String str) {
        this.accomCellLac = str;
        setValue();
    }

    public String getWorkLongtitude() {
        return this.workLongtitude;
    }

    public void setWorkLongtitude(String str) {
        this.workLongtitude = str;
        setValue();
    }

    public String getWorkLatitude() {
        return this.workLatitude;
    }

    public void setWorkLatitude(String str) {
        this.workLatitude = str;
        setValue();
    }

    public String getWorkCellId() {
        return this.workCellId;
    }

    public void setWorkCellId(String str) {
        this.workCellId = str;
        setValue();
    }

    public String getWorkCellLac() {
        return this.workCellLac;
    }

    public void setWorkCellLac(String str) {
        this.workCellLac = str;
        setValue();
    }

    public String getPoi1Longtitude() {
        return this.poi1Longtitude;
    }

    public void setPoi1Longtitude(String str) {
        this.poi1Longtitude = str;
        setValue();
    }

    public String getPoi1Latitude() {
        return this.poi1Latitude;
    }

    public void setPoi1Latitude(String str) {
        this.poi1Latitude = str;
        setValue();
    }

    public String getPoi1CellId() {
        return this.poi1CellId;
    }

    public void setPoi1CellId(String str) {
        this.poi1CellId = str;
        setValue();
    }

    public String getPoi1CellLac() {
        return this.poi1CellLac;
    }

    public void setPoi1CellLac(String str) {
        this.poi1CellLac = str;
        setValue();
    }

    public String getPoi2Longtitude() {
        return this.poi2Longtitude;
    }

    public void setPoi2Longtitude(String str) {
        this.poi2Longtitude = str;
        setValue();
    }

    public String getPoi2Latitude() {
        return this.poi2Latitude;
    }

    public void setPoi2Latitude(String str) {
        this.poi2Latitude = str;
        setValue();
    }

    public String getPoi2CellId() {
        return this.poi2CellId;
    }

    public void setPoi2CellId(String str) {
        this.poi2CellId = str;
        setValue();
    }

    public String getPoi2CellLac() {
        return this.poi2CellLac;
    }

    public void setPoi2CellLac(String str) {
        this.poi2CellLac = str;
        setValue();
    }

    public String getPoi3Longtitude() {
        return this.poi3Longtitude;
    }

    public void setPoi3Longtitude(String str) {
        this.poi3Longtitude = str;
        setValue();
    }

    public String getPoi3Latitude() {
        return this.poi3Latitude;
    }

    public void setPoi3Latitude(String str) {
        this.poi3Latitude = str;
        setValue();
    }

    public String getPoi3CellId() {
        return this.poi3CellId;
    }

    public void setPoi3CellId(String str) {
        this.poi3CellId = str;
        setValue();
    }

    public String getPoi3CellLac() {
        return this.poi3CellLac;
    }

    public void setPoi3CellLac(String str) {
        this.poi3CellLac = str;
        setValue();
    }

    public String getPoi4Longtitude() {
        return this.poi4Longtitude;
    }

    public void setPoi4Longtitude(String str) {
        this.poi4Longtitude = str;
        setValue();
    }

    public String getPoi4Latitude() {
        return this.poi4Latitude;
    }

    public void setPoi4Latitude(String str) {
        this.poi4Latitude = str;
        setValue();
    }

    public String getPoi4CellId() {
        return this.poi4CellId;
    }

    public void setPoi4CellId(String str) {
        this.poi4CellId = str;
        setValue();
    }

    public String getPoi4CellLac() {
        return this.poi4CellLac;
    }

    public void setPoi4CellLac(String str) {
        this.poi4CellLac = str;
        setValue();
    }

    public String getPoi5Longtitude() {
        return this.poi5Longtitude;
    }

    public void setPoi5Longtitude(String str) {
        this.poi5Longtitude = str;
        setValue();
    }

    public String getPoi5Latitude() {
        return this.poi5Latitude;
    }

    public void setPoi5Latitude(String str) {
        this.poi5Latitude = str;
        setValue();
    }

    public String getPoi5CellId() {
        return this.poi5CellId;
    }

    public void setPoi5CellId(String str) {
        this.poi5CellId = str;
        setValue();
    }

    public String getPoi5CellLac() {
        return this.poi5CellLac;
    }

    public void setPoi5CellLac(String str) {
        this.poi5CellLac = str;
        setValue();
    }

    public String getRoamingRadius() {
        return this.roamingRadius;
    }

    public void setRoamingRadius(String str) {
        this.roamingRadius = str;
        setValue();
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String str) {
        this.city = str;
        setValue();
    }

    public String getPhotoNumber() {
        return this.photoNumber;
    }

    public void setPhotoNumber(String str) {
        this.photoNumber = str;
        setValue();
    }

    public String getPhotoNumber_Self() {
        return this.photoNumber_Self;
    }

    public void setPhotoNumber_Self(String str) {
        this.photoNumber_Self = str;
        setValue();
    }

    public String getGameTime() {
        return this.gameTime;
    }

    public void setGameTime(String str) {
        this.gameTime = str;
        setValue();
    }

    public String getVideoTime() {
        return this.videoTime;
    }

    public void setVideoTime(String str) {
        this.videoTime = str;
        setValue();
    }

    public String getAlarmTime_Workday() {
        return this.alarmTime_Workday;
    }

    public void setAlarmTime_Workday(String str) {
        this.alarmTime_Workday = str;
        setValue();
    }

    public String getCallingDuration() {
        return this.callingDuration;
    }

    public void setCallingDuration(String str) {
        this.callingDuration = str;
        setValue();
    }

    public String getCallingTimes() {
        return this.callingTimes;
    }

    public void setCallingTimes(String str) {
        this.callingTimes = str;
        setValue();
    }

    public String getMusicNumber() {
        return this.musicNumber;
    }

    public void setMusicNumber(String str) {
        this.musicNumber = str;
        setValue();
    }

    public String getMusicNumber_Before2000() {
        return this.musicNumber_Before2000;
    }

    public void setMusicNumber_Before2000(String str) {
        this.musicNumber_Before2000 = str;
        setValue();
    }

    public String getPhotoNumber_Turism() {
        return this.photoNumber_Turism;
    }

    public void setPhotoNumber_Turism(String str) {
        this.photoNumber_Turism = str;
        setValue();
    }

    public String getTopMode() {
        return this.topMode;
    }

    public void setTopMode(String str) {
        this.topMode = str;
        setValue();
    }

    public String getTop1Tag() {
        return this.top1Tag;
    }

    public void setTop1Tag(String str) {
        this.top1Tag = str;
        setValue();
    }

    public String getTop2Tag() {
        return this.top2Tag;
    }

    public void setTop2Tag(String str) {
        this.top2Tag = str;
        setValue();
    }

    public String getTop3Tag() {
        return this.top3Tag;
    }

    public void setTop3Tag(String str) {
        this.top3Tag = str;
        setValue();
    }

    public String getTop4Tag() {
        return this.top4Tag;
    }

    public void setTop4Tag(String str) {
        this.top4Tag = str;
        setValue();
    }

    public String getTop5Tag() {
        return this.top5Tag;
    }

    public void setTop5Tag(String str) {
        this.top5Tag = str;
        setValue();
    }

    public String getTop6Tag() {
        return this.top6Tag;
    }

    public void setTop6Tag(String str) {
        this.top6Tag = str;
        setValue();
    }

    public String getTop7Tag() {
        return this.top7Tag;
    }

    public void setTop7Tag(String str) {
        this.top7Tag = str;
        setValue();
    }

    public String getTop8Tag() {
        return this.top8Tag;
    }

    public void setTop8Tag(String str) {
        this.top8Tag = str;
        setValue();
    }

    public String getTop9Tag() {
        return this.top9Tag;
    }

    public void setTop9Tag(String str) {
        this.top9Tag = str;
        setValue();
    }

    public String getTop10Tag() {
        return this.top10Tag;
    }

    public void setTop10Tag(String str) {
        this.top10Tag = str;
        setValue();
    }

    public String getOnTime() {
        return this.onTime;
    }

    public void setOnTime(String str) {
        this.onTime = str;
        setValue();
    }

    public String getLeaveHomeTime() {
        return this.leaveHomeTime;
    }

    public void setLeaveHomeTime(String str) {
        this.leaveHomeTime = str;
        setValue();
    }

    public String getArriveWorkplaceTime() {
        return this.arriveWorkplaceTime;
    }

    public void setArriveWorkplaceTime(String str) {
        this.arriveWorkplaceTime = str;
        setValue();
    }

    public String getLeaveWorkplaceTime() {
        return this.leaveWorkplaceTime;
    }

    public void setLeaveWorkplaceTime(String str) {
        this.leaveWorkplaceTime = str;
        setValue();
    }

    public String getArriveHomeTime() {
        return this.arriveHomeTime;
    }

    public void setArriveHomeTime(String str) {
        this.arriveHomeTime = str;
        setValue();
    }

    public String getOffTime() {
        return this.offTime;
    }

    public void setOffTime(String str) {
        this.offTime = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.id.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.sn != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.sn.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.genDate != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.genDate);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.deviceToken != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.deviceToken);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.hwId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.hwId);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.imei != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.imei);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.sexuality != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.sexuality.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.age != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.age.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.contactNumber != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.contactNumber);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.accomLongtitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.accomLongtitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.accomLatitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.accomLatitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.accomCellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.accomCellId);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.accomCellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.accomCellLac);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.workLongtitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.workLongtitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.workLatitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.workLatitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.workCellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.workCellId);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.workCellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.workCellLac);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi1Longtitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi1Longtitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi1Latitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi1Latitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi1CellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi1CellId);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi1CellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi1CellLac);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi2Longtitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi2Longtitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi2Latitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi2Latitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi2CellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi2CellId);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi2CellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi2CellLac);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi3Longtitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi3Longtitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi3Latitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi3Latitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi3CellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi3CellId);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi3CellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi3CellLac);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi4Longtitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi4Longtitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi4Latitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi4Latitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi4CellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi4CellId);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi4CellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi4CellLac);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi5Longtitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi5Longtitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi5Latitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi5Latitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi5CellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi5CellId);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi5CellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi5CellLac);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.roamingRadius != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.roamingRadius);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.city != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.city);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.photoNumber != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.photoNumber);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.photoNumber_Self != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.photoNumber_Self);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.gameTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.gameTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.videoTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.videoTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.alarmTime_Workday != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.alarmTime_Workday);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.callingDuration != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.callingDuration);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.callingTimes != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.callingTimes);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.musicNumber != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.musicNumber);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.musicNumber_Before2000 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.musicNumber_Before2000);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.photoNumber_Turism != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.photoNumber_Turism);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.topMode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.topMode);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top1Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top1Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top2Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top2Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top3Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top3Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top4Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top4Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top5Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top5Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top6Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top6Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top7Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top7Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top8Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top8Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top9Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top9Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top10Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top10Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.onTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.onTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.leaveHomeTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.leaveHomeTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.arriveWorkplaceTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.arriveWorkplaceTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.leaveWorkplaceTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.leaveWorkplaceTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.arriveHomeTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.arriveHomeTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.offTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.offTime);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<ShortPeriodUserProfile> getHelper() {
        return ShortPeriodUserProfileHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ShortPeriodUserProfile { id: " + this.id + ", sn: " + this.sn + ", genDate: " + this.genDate + ", deviceToken: " + this.deviceToken + ", hwId: " + this.hwId + ", imei: " + this.imei + ", sexuality: " + this.sexuality + ", age: " + this.age + ", contactNumber: " + this.contactNumber + ", accomLongtitude: " + this.accomLongtitude + ", accomLatitude: " + this.accomLatitude + ", accomCellId: " + this.accomCellId + ", accomCellLac: " + this.accomCellLac + ", workLongtitude: " + this.workLongtitude + ", workLatitude: " + this.workLatitude + ", workCellId: " + this.workCellId + ", workCellLac: " + this.workCellLac + ", poi1Longtitude: " + this.poi1Longtitude + ", poi1Latitude: " + this.poi1Latitude + ", poi1CellId: " + this.poi1CellId + ", poi1CellLac: " + this.poi1CellLac + ", poi2Longtitude: " + this.poi2Longtitude + ", poi2Latitude: " + this.poi2Latitude + ", poi2CellId: " + this.poi2CellId + ", poi2CellLac: " + this.poi2CellLac + ", poi3Longtitude: " + this.poi3Longtitude + ", poi3Latitude: " + this.poi3Latitude + ", poi3CellId: " + this.poi3CellId + ", poi3CellLac: " + this.poi3CellLac + ", poi4Longtitude: " + this.poi4Longtitude + ", poi4Latitude: " + this.poi4Latitude + ", poi4CellId: " + this.poi4CellId + ", poi4CellLac: " + this.poi4CellLac + ", poi5Longtitude: " + this.poi5Longtitude + ", poi5Latitude: " + this.poi5Latitude + ", poi5CellId: " + this.poi5CellId + ", poi5CellLac: " + this.poi5CellLac + ", roamingRadius: " + this.roamingRadius + ", city: " + this.city + ", photoNumber: " + this.photoNumber + ", photoNumber_Self: " + this.photoNumber_Self + ", gameTime: " + this.gameTime + ", videoTime: " + this.videoTime + ", alarmTime_Workday: " + this.alarmTime_Workday + ", callingDuration: " + this.callingDuration + ", callingTimes: " + this.callingTimes + ", musicNumber: " + this.musicNumber + ", musicNumber_Before2000: " + this.musicNumber_Before2000 + ", photoNumber_Turism: " + this.photoNumber_Turism + ", topMode: " + this.topMode + ", top1Tag: " + this.top1Tag + ", top2Tag: " + this.top2Tag + ", top3Tag: " + this.top3Tag + ", top4Tag: " + this.top4Tag + ", top5Tag: " + this.top5Tag + ", top6Tag: " + this.top6Tag + ", top7Tag: " + this.top7Tag + ", top8Tag: " + this.top8Tag + ", top9Tag: " + this.top9Tag + ", top10Tag: " + this.top10Tag + ", onTime: " + this.onTime + ", leaveHomeTime: " + this.leaveHomeTime + ", arriveWorkplaceTime: " + this.arriveWorkplaceTime + ", leaveWorkplaceTime: " + this.leaveWorkplaceTime + ", arriveHomeTime: " + this.arriveHomeTime + ", offTime: " + this.offTime + " }";
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
