package com.huawei.nb.model.profile;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class MyProfileHelper extends AEntityHelper<MyProfile> {
    private static final MyProfileHelper INSTANCE = new MyProfileHelper();

    private MyProfileHelper() {
    }

    public static MyProfileHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MyProfile object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String deviceID = object.getDeviceID();
        if (deviceID != null) {
            statement.bindString(2, deviceID);
        } else {
            statement.bindNull(2);
        }
        String hwId = object.getHwId();
        if (hwId != null) {
            statement.bindString(3, hwId);
        } else {
            statement.bindNull(3);
        }
        Double age = object.getAge();
        if (age != null) {
            statement.bindDouble(4, age.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Double sexuality = object.getSexuality();
        if (sexuality != null) {
            statement.bindDouble(5, sexuality.doubleValue());
        } else {
            statement.bindNull(5);
        }
        Integer contactNumber = object.getContactNumber();
        if (contactNumber != null) {
            statement.bindLong(6, (long) contactNumber.intValue());
        } else {
            statement.bindNull(6);
        }
        String accomGPS = object.getAccomGPS();
        if (accomGPS != null) {
            statement.bindString(7, accomGPS);
        } else {
            statement.bindNull(7);
        }
        Integer accomCellId = object.getAccomCellId();
        if (accomCellId != null) {
            statement.bindLong(8, (long) accomCellId.intValue());
        } else {
            statement.bindNull(8);
        }
        Integer accomCellLac = object.getAccomCellLac();
        if (accomCellLac != null) {
            statement.bindLong(9, (long) accomCellLac.intValue());
        } else {
            statement.bindNull(9);
        }
        String workPlaceGPS = object.getWorkPlaceGPS();
        if (workPlaceGPS != null) {
            statement.bindString(10, workPlaceGPS);
        } else {
            statement.bindNull(10);
        }
        Integer workPlaceCellId = object.getWorkPlaceCellId();
        if (workPlaceCellId != null) {
            statement.bindLong(11, (long) workPlaceCellId.intValue());
        } else {
            statement.bindNull(11);
        }
        Integer workPlaceCellLac = object.getWorkPlaceCellLac();
        if (workPlaceCellLac != null) {
            statement.bindLong(12, (long) workPlaceCellLac.intValue());
        } else {
            statement.bindNull(12);
        }
        String poi1GPS = object.getPoi1GPS();
        if (poi1GPS != null) {
            statement.bindString(13, poi1GPS);
        } else {
            statement.bindNull(13);
        }
        Integer poi1CellId = object.getPoi1CellId();
        if (poi1CellId != null) {
            statement.bindLong(14, (long) poi1CellId.intValue());
        } else {
            statement.bindNull(14);
        }
        Integer poi1CellLac = object.getPoi1CellLac();
        if (poi1CellLac != null) {
            statement.bindLong(15, (long) poi1CellLac.intValue());
        } else {
            statement.bindNull(15);
        }
        String poi2GPS = object.getPoi2GPS();
        if (poi2GPS != null) {
            statement.bindString(16, poi2GPS);
        } else {
            statement.bindNull(16);
        }
        Integer poi2CellId = object.getPoi2CellId();
        if (poi2CellId != null) {
            statement.bindLong(17, (long) poi2CellId.intValue());
        } else {
            statement.bindNull(17);
        }
        Integer poi2CellLac = object.getPoi2CellLac();
        if (poi2CellLac != null) {
            statement.bindLong(18, (long) poi2CellLac.intValue());
        } else {
            statement.bindNull(18);
        }
        String poi3GPS = object.getPoi3GPS();
        if (poi3GPS != null) {
            statement.bindString(19, poi3GPS);
        } else {
            statement.bindNull(19);
        }
        Integer poi3CellId = object.getPoi3CellId();
        if (poi3CellId != null) {
            statement.bindLong(20, (long) poi3CellId.intValue());
        } else {
            statement.bindNull(20);
        }
        Integer poi3CellLac = object.getPoi3CellLac();
        if (poi3CellLac != null) {
            statement.bindLong(21, (long) poi3CellLac.intValue());
        } else {
            statement.bindNull(21);
        }
        String poi4GPS = object.getPoi4GPS();
        if (poi4GPS != null) {
            statement.bindString(22, poi4GPS);
        } else {
            statement.bindNull(22);
        }
        Integer poi4CellId = object.getPoi4CellId();
        if (poi4CellId != null) {
            statement.bindLong(23, (long) poi4CellId.intValue());
        } else {
            statement.bindNull(23);
        }
        Integer poi4CellLac = object.getPoi4CellLac();
        if (poi4CellLac != null) {
            statement.bindLong(24, (long) poi4CellLac.intValue());
        } else {
            statement.bindNull(24);
        }
        String poi5GPS = object.getPoi5GPS();
        if (poi5GPS != null) {
            statement.bindString(25, poi5GPS);
        } else {
            statement.bindNull(25);
        }
        Integer poi5CellId = object.getPoi5CellId();
        if (poi5CellId != null) {
            statement.bindLong(26, (long) poi5CellId.intValue());
        } else {
            statement.bindNull(26);
        }
        Integer poi5CellLac = object.getPoi5CellLac();
        if (poi5CellLac != null) {
            statement.bindLong(27, (long) poi5CellLac.intValue());
        } else {
            statement.bindNull(27);
        }
        String baseCity = object.getBaseCity();
        if (baseCity != null) {
            statement.bindString(28, baseCity);
        } else {
            statement.bindNull(28);
        }
        Double roamingRadius = object.getRoamingRadius();
        if (roamingRadius != null) {
            statement.bindDouble(29, roamingRadius.doubleValue());
        } else {
            statement.bindNull(29);
        }
        Float buziTripPrefer = object.getBuziTripPrefer();
        if (buziTripPrefer != null) {
            statement.bindString(30, Float.toString(buziTripPrefer.floatValue()));
        } else {
            statement.bindNull(30);
        }
        Float photoPrefer = object.getPhotoPrefer();
        if (photoPrefer != null) {
            statement.bindString(31, Float.toString(photoPrefer.floatValue()));
        } else {
            statement.bindNull(31);
        }
        Float photoPreferTurism = object.getPhotoPreferTurism();
        if (photoPreferTurism != null) {
            statement.bindString(32, Float.toString(photoPreferTurism.floatValue()));
        } else {
            statement.bindNull(32);
        }
        String topMode = object.getTopMode();
        if (topMode != null) {
            statement.bindString(33, topMode);
        } else {
            statement.bindNull(33);
        }
        String top1Tag = object.getTop1Tag();
        if (top1Tag != null) {
            statement.bindString(34, top1Tag);
        } else {
            statement.bindNull(34);
        }
        String top2Tag = object.getTop2Tag();
        if (top2Tag != null) {
            statement.bindString(35, top2Tag);
        } else {
            statement.bindNull(35);
        }
        String top3Tag = object.getTop3Tag();
        if (top3Tag != null) {
            statement.bindString(36, top3Tag);
        } else {
            statement.bindNull(36);
        }
        String top4Tag = object.getTop4Tag();
        if (top4Tag != null) {
            statement.bindString(37, top4Tag);
        } else {
            statement.bindNull(37);
        }
        String top5Tag = object.getTop5Tag();
        if (top5Tag != null) {
            statement.bindString(38, top5Tag);
        } else {
            statement.bindNull(38);
        }
        String top6Tag = object.getTop6Tag();
        if (top6Tag != null) {
            statement.bindString(39, top6Tag);
        } else {
            statement.bindNull(39);
        }
        String top7Tag = object.getTop7Tag();
        if (top7Tag != null) {
            statement.bindString(40, top7Tag);
        } else {
            statement.bindNull(40);
        }
        String top8Tag = object.getTop8Tag();
        if (top8Tag != null) {
            statement.bindString(41, top8Tag);
        } else {
            statement.bindNull(41);
        }
        String top9Tag = object.getTop9Tag();
        if (top9Tag != null) {
            statement.bindString(42, top9Tag);
        } else {
            statement.bindNull(42);
        }
        String top10Tag = object.getTop10Tag();
        if (top10Tag != null) {
            statement.bindString(43, top10Tag);
        } else {
            statement.bindNull(43);
        }
        String onTimeWorkday = object.getOnTimeWorkday();
        if (onTimeWorkday != null) {
            statement.bindString(44, onTimeWorkday);
        } else {
            statement.bindNull(44);
        }
        String leaveHomeTimeWorkday = object.getLeaveHomeTimeWorkday();
        if (leaveHomeTimeWorkday != null) {
            statement.bindString(45, leaveHomeTimeWorkday);
        } else {
            statement.bindNull(45);
        }
        String arriveWorkplaceTimeWorkday = object.getArriveWorkplaceTimeWorkday();
        if (arriveWorkplaceTimeWorkday != null) {
            statement.bindString(46, arriveWorkplaceTimeWorkday);
        } else {
            statement.bindNull(46);
        }
        String leaveWorkplaceTimeWorkday = object.getLeaveWorkplaceTimeWorkday();
        if (leaveWorkplaceTimeWorkday != null) {
            statement.bindString(47, leaveWorkplaceTimeWorkday);
        } else {
            statement.bindNull(47);
        }
        String arriveHomeTimeWorkday = object.getArriveHomeTimeWorkday();
        if (arriveHomeTimeWorkday != null) {
            statement.bindString(48, arriveHomeTimeWorkday);
        } else {
            statement.bindNull(48);
        }
        String offTimeWorkday = object.getOffTimeWorkday();
        if (offTimeWorkday != null) {
            statement.bindString(49, offTimeWorkday);
        } else {
            statement.bindNull(49);
        }
        String onTimeWeekend = object.getOnTimeWeekend();
        if (onTimeWeekend != null) {
            statement.bindString(50, onTimeWeekend);
        } else {
            statement.bindNull(50);
        }
        String leaveHomeTimeWeekend = object.getLeaveHomeTimeWeekend();
        if (leaveHomeTimeWeekend != null) {
            statement.bindString(51, leaveHomeTimeWeekend);
        } else {
            statement.bindNull(51);
        }
        String arriveHomeTimeWeekend = object.getArriveHomeTimeWeekend();
        if (arriveHomeTimeWeekend != null) {
            statement.bindString(52, arriveHomeTimeWeekend);
        } else {
            statement.bindNull(52);
        }
        String offTimeWeekend = object.getOffTimeWeekend();
        if (offTimeWeekend != null) {
            statement.bindString(53, offTimeWeekend);
        } else {
            statement.bindNull(53);
        }
        Float imeiConfidence = object.getImeiConfidence();
        if (imeiConfidence != null) {
            statement.bindString(54, Float.toString(imeiConfidence.floatValue()));
        } else {
            statement.bindNull(54);
        }
        Float hwIdConfidence = object.getHwIdConfidence();
        if (hwIdConfidence != null) {
            statement.bindString(55, Float.toString(hwIdConfidence.floatValue()));
        } else {
            statement.bindNull(55);
        }
        Float ageConfidence = object.getAgeConfidence();
        if (ageConfidence != null) {
            statement.bindString(56, Float.toString(ageConfidence.floatValue()));
        } else {
            statement.bindNull(56);
        }
        Float sexualityConfidence = object.getSexualityConfidence();
        if (sexualityConfidence != null) {
            statement.bindString(57, Float.toString(sexualityConfidence.floatValue()));
        } else {
            statement.bindNull(57);
        }
        Float contactNumberConfidence = object.getContactNumberConfidence();
        if (contactNumberConfidence != null) {
            statement.bindString(58, Float.toString(contactNumberConfidence.floatValue()));
        } else {
            statement.bindNull(58);
        }
        Float accomConfidence = object.getAccomConfidence();
        if (accomConfidence != null) {
            statement.bindString(59, Float.toString(accomConfidence.floatValue()));
        } else {
            statement.bindNull(59);
        }
        Float workplaceConfidence = object.getWorkplaceConfidence();
        if (workplaceConfidence != null) {
            statement.bindString(60, Float.toString(workplaceConfidence.floatValue()));
        } else {
            statement.bindNull(60);
        }
        Float poi1Confidence = object.getPoi1Confidence();
        if (poi1Confidence != null) {
            statement.bindString(61, Float.toString(poi1Confidence.floatValue()));
        } else {
            statement.bindNull(61);
        }
        Float poi2Confidence = object.getPoi2Confidence();
        if (poi2Confidence != null) {
            statement.bindString(62, Float.toString(poi2Confidence.floatValue()));
        } else {
            statement.bindNull(62);
        }
        Float poi3Confidence = object.getPoi3Confidence();
        if (poi3Confidence != null) {
            statement.bindString(63, Float.toString(poi3Confidence.floatValue()));
        } else {
            statement.bindNull(63);
        }
        Float poi4Confidence = object.getPoi4Confidence();
        if (poi4Confidence != null) {
            statement.bindString(64, Float.toString(poi4Confidence.floatValue()));
        } else {
            statement.bindNull(64);
        }
        Float poi5Confidence = object.getPoi5Confidence();
        if (poi5Confidence != null) {
            statement.bindString(65, Float.toString(poi5Confidence.floatValue()));
        } else {
            statement.bindNull(65);
        }
        Float baseCityConfidence = object.getBaseCityConfidence();
        if (baseCityConfidence != null) {
            statement.bindString(66, Float.toString(baseCityConfidence.floatValue()));
        } else {
            statement.bindNull(66);
        }
        Float roamingRadiusConfidence = object.getRoamingRadiusConfidence();
        if (roamingRadiusConfidence != null) {
            statement.bindString(67, Float.toString(roamingRadiusConfidence.floatValue()));
        } else {
            statement.bindNull(67);
        }
        Float buziTripPreferConfidence = object.getBuziTripPreferConfidence();
        if (buziTripPreferConfidence != null) {
            statement.bindString(68, Float.toString(buziTripPreferConfidence.floatValue()));
        } else {
            statement.bindNull(68);
        }
        Float photoPreferConfidence = object.getPhotoPreferConfidence();
        if (photoPreferConfidence != null) {
            statement.bindString(69, Float.toString(photoPreferConfidence.floatValue()));
        } else {
            statement.bindNull(69);
        }
        Float photoPreferTurismConfidence = object.getPhotoPreferTurismConfidence();
        if (photoPreferTurismConfidence != null) {
            statement.bindString(70, Float.toString(photoPreferTurismConfidence.floatValue()));
        } else {
            statement.bindNull(70);
        }
        Float topModeConfidence = object.getTopModeConfidence();
        if (topModeConfidence != null) {
            statement.bindString(71, Float.toString(topModeConfidence.floatValue()));
        } else {
            statement.bindNull(71);
        }
        Float top1TagConfidence = object.getTop1TagConfidence();
        if (top1TagConfidence != null) {
            statement.bindString(72, Float.toString(top1TagConfidence.floatValue()));
        } else {
            statement.bindNull(72);
        }
        Float top2TagConfidence = object.getTop2TagConfidence();
        if (top2TagConfidence != null) {
            statement.bindString(73, Float.toString(top2TagConfidence.floatValue()));
        } else {
            statement.bindNull(73);
        }
        Float top3TagConfidence = object.getTop3TagConfidence();
        if (top3TagConfidence != null) {
            statement.bindString(74, Float.toString(top3TagConfidence.floatValue()));
        } else {
            statement.bindNull(74);
        }
        Float top4TagConfidence = object.getTop4TagConfidence();
        if (top4TagConfidence != null) {
            statement.bindString(75, Float.toString(top4TagConfidence.floatValue()));
        } else {
            statement.bindNull(75);
        }
        Float top5TagConfidence = object.getTop5TagConfidence();
        if (top5TagConfidence != null) {
            statement.bindString(76, Float.toString(top5TagConfidence.floatValue()));
        } else {
            statement.bindNull(76);
        }
        Float top6TagConfidence = object.getTop6TagConfidence();
        if (top6TagConfidence != null) {
            statement.bindString(77, Float.toString(top6TagConfidence.floatValue()));
        } else {
            statement.bindNull(77);
        }
        Float top7TagConfidence = object.getTop7TagConfidence();
        if (top7TagConfidence != null) {
            statement.bindString(78, Float.toString(top7TagConfidence.floatValue()));
        } else {
            statement.bindNull(78);
        }
        Float top8TagConfidence = object.getTop8TagConfidence();
        if (top8TagConfidence != null) {
            statement.bindString(79, Float.toString(top8TagConfidence.floatValue()));
        } else {
            statement.bindNull(79);
        }
        Float top9TagConfidence = object.getTop9TagConfidence();
        if (top9TagConfidence != null) {
            statement.bindString(80, Float.toString(top9TagConfidence.floatValue()));
        } else {
            statement.bindNull(80);
        }
        Float top10TagConfidence = object.getTop10TagConfidence();
        if (top10TagConfidence != null) {
            statement.bindString(81, Float.toString(top10TagConfidence.floatValue()));
        } else {
            statement.bindNull(81);
        }
        Float onTimeWorkdayConfidence = object.getOnTimeWorkdayConfidence();
        if (onTimeWorkdayConfidence != null) {
            statement.bindString(82, Float.toString(onTimeWorkdayConfidence.floatValue()));
        } else {
            statement.bindNull(82);
        }
        Float leaveHomeTimeWorkdayConfidence = object.getLeaveHomeTimeWorkdayConfidence();
        if (leaveHomeTimeWorkdayConfidence != null) {
            statement.bindString(83, Float.toString(leaveHomeTimeWorkdayConfidence.floatValue()));
        } else {
            statement.bindNull(83);
        }
        Float arriveWorkplaceTimeWorkdayConfidence = object.getArriveWorkplaceTimeWorkdayConfidence();
        if (arriveWorkplaceTimeWorkdayConfidence != null) {
            statement.bindString(84, Float.toString(arriveWorkplaceTimeWorkdayConfidence.floatValue()));
        } else {
            statement.bindNull(84);
        }
        Float leaveWorkplaceTimeWorkdayConfidence = object.getLeaveWorkplaceTimeWorkdayConfidence();
        if (leaveWorkplaceTimeWorkdayConfidence != null) {
            statement.bindString(85, Float.toString(leaveWorkplaceTimeWorkdayConfidence.floatValue()));
        } else {
            statement.bindNull(85);
        }
        Float arriveHomeTimeWorkdayConfidence = object.getArriveHomeTimeWorkdayConfidence();
        if (arriveHomeTimeWorkdayConfidence != null) {
            statement.bindString(86, Float.toString(arriveHomeTimeWorkdayConfidence.floatValue()));
        } else {
            statement.bindNull(86);
        }
        Float offTimeWorkdayConfidence = object.getOffTimeWorkdayConfidence();
        if (offTimeWorkdayConfidence != null) {
            statement.bindString(87, Float.toString(offTimeWorkdayConfidence.floatValue()));
        } else {
            statement.bindNull(87);
        }
        Float onTimeWeekendConfidence = object.getOnTimeWeekendConfidence();
        if (onTimeWeekendConfidence != null) {
            statement.bindString(88, Float.toString(onTimeWeekendConfidence.floatValue()));
        } else {
            statement.bindNull(88);
        }
        Float leaveHomeTimeWeekendConfidence = object.getLeaveHomeTimeWeekendConfidence();
        if (leaveHomeTimeWeekendConfidence != null) {
            statement.bindString(89, Float.toString(leaveHomeTimeWeekendConfidence.floatValue()));
        } else {
            statement.bindNull(89);
        }
        Float arriveHomeTimeWeekendConfidence = object.getArriveHomeTimeWeekendConfidence();
        if (arriveHomeTimeWeekendConfidence != null) {
            statement.bindString(90, Float.toString(arriveHomeTimeWeekendConfidence.floatValue()));
        } else {
            statement.bindNull(90);
        }
        Float offTimeWeekendConfidence = object.getOffTimeWeekendConfidence();
        if (offTimeWeekendConfidence != null) {
            statement.bindString(91, Float.toString(offTimeWeekendConfidence.floatValue()));
        } else {
            statement.bindNull(91);
        }
    }

    public MyProfile readObject(Cursor cursor, int offset) {
        return new MyProfile(cursor);
    }

    public void setPrimaryKeyValue(MyProfile object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, MyProfile object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
