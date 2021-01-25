package com.huawei.nb.model.profile;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ShortPeriodUserProfileHelper extends AEntityHelper<ShortPeriodUserProfile> {
    private static final ShortPeriodUserProfileHelper INSTANCE = new ShortPeriodUserProfileHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ShortPeriodUserProfile shortPeriodUserProfile) {
        return null;
    }

    private ShortPeriodUserProfileHelper() {
    }

    public static ShortPeriodUserProfileHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ShortPeriodUserProfile shortPeriodUserProfile) {
        Integer id = shortPeriodUserProfile.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer sn = shortPeriodUserProfile.getSn();
        if (sn != null) {
            statement.bindLong(2, (long) sn.intValue());
        } else {
            statement.bindNull(2);
        }
        String genDate = shortPeriodUserProfile.getGenDate();
        if (genDate != null) {
            statement.bindString(3, genDate);
        } else {
            statement.bindNull(3);
        }
        String deviceToken = shortPeriodUserProfile.getDeviceToken();
        if (deviceToken != null) {
            statement.bindString(4, deviceToken);
        } else {
            statement.bindNull(4);
        }
        String hwId = shortPeriodUserProfile.getHwId();
        if (hwId != null) {
            statement.bindString(5, hwId);
        } else {
            statement.bindNull(5);
        }
        String imei = shortPeriodUserProfile.getImei();
        if (imei != null) {
            statement.bindString(6, imei);
        } else {
            statement.bindNull(6);
        }
        Double sexuality = shortPeriodUserProfile.getSexuality();
        if (sexuality != null) {
            statement.bindDouble(7, sexuality.doubleValue());
        } else {
            statement.bindNull(7);
        }
        Double age = shortPeriodUserProfile.getAge();
        if (age != null) {
            statement.bindDouble(8, age.doubleValue());
        } else {
            statement.bindNull(8);
        }
        String contactNumber = shortPeriodUserProfile.getContactNumber();
        if (contactNumber != null) {
            statement.bindString(9, contactNumber);
        } else {
            statement.bindNull(9);
        }
        String accomLongtitude = shortPeriodUserProfile.getAccomLongtitude();
        if (accomLongtitude != null) {
            statement.bindString(10, accomLongtitude);
        } else {
            statement.bindNull(10);
        }
        String accomLatitude = shortPeriodUserProfile.getAccomLatitude();
        if (accomLatitude != null) {
            statement.bindString(11, accomLatitude);
        } else {
            statement.bindNull(11);
        }
        String accomCellId = shortPeriodUserProfile.getAccomCellId();
        if (accomCellId != null) {
            statement.bindString(12, accomCellId);
        } else {
            statement.bindNull(12);
        }
        String accomCellLac = shortPeriodUserProfile.getAccomCellLac();
        if (accomCellLac != null) {
            statement.bindString(13, accomCellLac);
        } else {
            statement.bindNull(13);
        }
        String workLongtitude = shortPeriodUserProfile.getWorkLongtitude();
        if (workLongtitude != null) {
            statement.bindString(14, workLongtitude);
        } else {
            statement.bindNull(14);
        }
        String workLatitude = shortPeriodUserProfile.getWorkLatitude();
        if (workLatitude != null) {
            statement.bindString(15, workLatitude);
        } else {
            statement.bindNull(15);
        }
        String workCellId = shortPeriodUserProfile.getWorkCellId();
        if (workCellId != null) {
            statement.bindString(16, workCellId);
        } else {
            statement.bindNull(16);
        }
        String workCellLac = shortPeriodUserProfile.getWorkCellLac();
        if (workCellLac != null) {
            statement.bindString(17, workCellLac);
        } else {
            statement.bindNull(17);
        }
        String poi1Longtitude = shortPeriodUserProfile.getPoi1Longtitude();
        if (poi1Longtitude != null) {
            statement.bindString(18, poi1Longtitude);
        } else {
            statement.bindNull(18);
        }
        String poi1Latitude = shortPeriodUserProfile.getPoi1Latitude();
        if (poi1Latitude != null) {
            statement.bindString(19, poi1Latitude);
        } else {
            statement.bindNull(19);
        }
        String poi1CellId = shortPeriodUserProfile.getPoi1CellId();
        if (poi1CellId != null) {
            statement.bindString(20, poi1CellId);
        } else {
            statement.bindNull(20);
        }
        String poi1CellLac = shortPeriodUserProfile.getPoi1CellLac();
        if (poi1CellLac != null) {
            statement.bindString(21, poi1CellLac);
        } else {
            statement.bindNull(21);
        }
        String poi2Longtitude = shortPeriodUserProfile.getPoi2Longtitude();
        if (poi2Longtitude != null) {
            statement.bindString(22, poi2Longtitude);
        } else {
            statement.bindNull(22);
        }
        String poi2Latitude = shortPeriodUserProfile.getPoi2Latitude();
        if (poi2Latitude != null) {
            statement.bindString(23, poi2Latitude);
        } else {
            statement.bindNull(23);
        }
        String poi2CellId = shortPeriodUserProfile.getPoi2CellId();
        if (poi2CellId != null) {
            statement.bindString(24, poi2CellId);
        } else {
            statement.bindNull(24);
        }
        String poi2CellLac = shortPeriodUserProfile.getPoi2CellLac();
        if (poi2CellLac != null) {
            statement.bindString(25, poi2CellLac);
        } else {
            statement.bindNull(25);
        }
        String poi3Longtitude = shortPeriodUserProfile.getPoi3Longtitude();
        if (poi3Longtitude != null) {
            statement.bindString(26, poi3Longtitude);
        } else {
            statement.bindNull(26);
        }
        String poi3Latitude = shortPeriodUserProfile.getPoi3Latitude();
        if (poi3Latitude != null) {
            statement.bindString(27, poi3Latitude);
        } else {
            statement.bindNull(27);
        }
        String poi3CellId = shortPeriodUserProfile.getPoi3CellId();
        if (poi3CellId != null) {
            statement.bindString(28, poi3CellId);
        } else {
            statement.bindNull(28);
        }
        String poi3CellLac = shortPeriodUserProfile.getPoi3CellLac();
        if (poi3CellLac != null) {
            statement.bindString(29, poi3CellLac);
        } else {
            statement.bindNull(29);
        }
        String poi4Longtitude = shortPeriodUserProfile.getPoi4Longtitude();
        if (poi4Longtitude != null) {
            statement.bindString(30, poi4Longtitude);
        } else {
            statement.bindNull(30);
        }
        String poi4Latitude = shortPeriodUserProfile.getPoi4Latitude();
        if (poi4Latitude != null) {
            statement.bindString(31, poi4Latitude);
        } else {
            statement.bindNull(31);
        }
        String poi4CellId = shortPeriodUserProfile.getPoi4CellId();
        if (poi4CellId != null) {
            statement.bindString(32, poi4CellId);
        } else {
            statement.bindNull(32);
        }
        String poi4CellLac = shortPeriodUserProfile.getPoi4CellLac();
        if (poi4CellLac != null) {
            statement.bindString(33, poi4CellLac);
        } else {
            statement.bindNull(33);
        }
        String poi5Longtitude = shortPeriodUserProfile.getPoi5Longtitude();
        if (poi5Longtitude != null) {
            statement.bindString(34, poi5Longtitude);
        } else {
            statement.bindNull(34);
        }
        String poi5Latitude = shortPeriodUserProfile.getPoi5Latitude();
        if (poi5Latitude != null) {
            statement.bindString(35, poi5Latitude);
        } else {
            statement.bindNull(35);
        }
        String poi5CellId = shortPeriodUserProfile.getPoi5CellId();
        if (poi5CellId != null) {
            statement.bindString(36, poi5CellId);
        } else {
            statement.bindNull(36);
        }
        String poi5CellLac = shortPeriodUserProfile.getPoi5CellLac();
        if (poi5CellLac != null) {
            statement.bindString(37, poi5CellLac);
        } else {
            statement.bindNull(37);
        }
        String roamingRadius = shortPeriodUserProfile.getRoamingRadius();
        if (roamingRadius != null) {
            statement.bindString(38, roamingRadius);
        } else {
            statement.bindNull(38);
        }
        String city = shortPeriodUserProfile.getCity();
        if (city != null) {
            statement.bindString(39, city);
        } else {
            statement.bindNull(39);
        }
        String photoNumber = shortPeriodUserProfile.getPhotoNumber();
        if (photoNumber != null) {
            statement.bindString(40, photoNumber);
        } else {
            statement.bindNull(40);
        }
        String photoNumber_Self = shortPeriodUserProfile.getPhotoNumber_Self();
        if (photoNumber_Self != null) {
            statement.bindString(41, photoNumber_Self);
        } else {
            statement.bindNull(41);
        }
        String gameTime = shortPeriodUserProfile.getGameTime();
        if (gameTime != null) {
            statement.bindString(42, gameTime);
        } else {
            statement.bindNull(42);
        }
        String videoTime = shortPeriodUserProfile.getVideoTime();
        if (videoTime != null) {
            statement.bindString(43, videoTime);
        } else {
            statement.bindNull(43);
        }
        String alarmTime_Workday = shortPeriodUserProfile.getAlarmTime_Workday();
        if (alarmTime_Workday != null) {
            statement.bindString(44, alarmTime_Workday);
        } else {
            statement.bindNull(44);
        }
        String callingDuration = shortPeriodUserProfile.getCallingDuration();
        if (callingDuration != null) {
            statement.bindString(45, callingDuration);
        } else {
            statement.bindNull(45);
        }
        String callingTimes = shortPeriodUserProfile.getCallingTimes();
        if (callingTimes != null) {
            statement.bindString(46, callingTimes);
        } else {
            statement.bindNull(46);
        }
        String musicNumber = shortPeriodUserProfile.getMusicNumber();
        if (musicNumber != null) {
            statement.bindString(47, musicNumber);
        } else {
            statement.bindNull(47);
        }
        String musicNumber_Before2000 = shortPeriodUserProfile.getMusicNumber_Before2000();
        if (musicNumber_Before2000 != null) {
            statement.bindString(48, musicNumber_Before2000);
        } else {
            statement.bindNull(48);
        }
        String photoNumber_Turism = shortPeriodUserProfile.getPhotoNumber_Turism();
        if (photoNumber_Turism != null) {
            statement.bindString(49, photoNumber_Turism);
        } else {
            statement.bindNull(49);
        }
        String topMode = shortPeriodUserProfile.getTopMode();
        if (topMode != null) {
            statement.bindString(50, topMode);
        } else {
            statement.bindNull(50);
        }
        String top1Tag = shortPeriodUserProfile.getTop1Tag();
        if (top1Tag != null) {
            statement.bindString(51, top1Tag);
        } else {
            statement.bindNull(51);
        }
        String top2Tag = shortPeriodUserProfile.getTop2Tag();
        if (top2Tag != null) {
            statement.bindString(52, top2Tag);
        } else {
            statement.bindNull(52);
        }
        String top3Tag = shortPeriodUserProfile.getTop3Tag();
        if (top3Tag != null) {
            statement.bindString(53, top3Tag);
        } else {
            statement.bindNull(53);
        }
        String top4Tag = shortPeriodUserProfile.getTop4Tag();
        if (top4Tag != null) {
            statement.bindString(54, top4Tag);
        } else {
            statement.bindNull(54);
        }
        String top5Tag = shortPeriodUserProfile.getTop5Tag();
        if (top5Tag != null) {
            statement.bindString(55, top5Tag);
        } else {
            statement.bindNull(55);
        }
        String top6Tag = shortPeriodUserProfile.getTop6Tag();
        if (top6Tag != null) {
            statement.bindString(56, top6Tag);
        } else {
            statement.bindNull(56);
        }
        String top7Tag = shortPeriodUserProfile.getTop7Tag();
        if (top7Tag != null) {
            statement.bindString(57, top7Tag);
        } else {
            statement.bindNull(57);
        }
        String top8Tag = shortPeriodUserProfile.getTop8Tag();
        if (top8Tag != null) {
            statement.bindString(58, top8Tag);
        } else {
            statement.bindNull(58);
        }
        String top9Tag = shortPeriodUserProfile.getTop9Tag();
        if (top9Tag != null) {
            statement.bindString(59, top9Tag);
        } else {
            statement.bindNull(59);
        }
        String top10Tag = shortPeriodUserProfile.getTop10Tag();
        if (top10Tag != null) {
            statement.bindString(60, top10Tag);
        } else {
            statement.bindNull(60);
        }
        String onTime = shortPeriodUserProfile.getOnTime();
        if (onTime != null) {
            statement.bindString(61, onTime);
        } else {
            statement.bindNull(61);
        }
        String leaveHomeTime = shortPeriodUserProfile.getLeaveHomeTime();
        if (leaveHomeTime != null) {
            statement.bindString(62, leaveHomeTime);
        } else {
            statement.bindNull(62);
        }
        String arriveWorkplaceTime = shortPeriodUserProfile.getArriveWorkplaceTime();
        if (arriveWorkplaceTime != null) {
            statement.bindString(63, arriveWorkplaceTime);
        } else {
            statement.bindNull(63);
        }
        String leaveWorkplaceTime = shortPeriodUserProfile.getLeaveWorkplaceTime();
        if (leaveWorkplaceTime != null) {
            statement.bindString(64, leaveWorkplaceTime);
        } else {
            statement.bindNull(64);
        }
        String arriveHomeTime = shortPeriodUserProfile.getArriveHomeTime();
        if (arriveHomeTime != null) {
            statement.bindString(65, arriveHomeTime);
        } else {
            statement.bindNull(65);
        }
        String offTime = shortPeriodUserProfile.getOffTime();
        if (offTime != null) {
            statement.bindString(66, offTime);
        } else {
            statement.bindNull(66);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ShortPeriodUserProfile readObject(Cursor cursor, int i) {
        return new ShortPeriodUserProfile(cursor);
    }

    public void setPrimaryKeyValue(ShortPeriodUserProfile shortPeriodUserProfile, long j) {
        shortPeriodUserProfile.setId(Integer.valueOf((int) j));
    }
}
