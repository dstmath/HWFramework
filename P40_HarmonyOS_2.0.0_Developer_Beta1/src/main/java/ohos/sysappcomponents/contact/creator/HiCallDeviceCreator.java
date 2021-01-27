package ohos.sysappcomponents.contact.creator;

import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.entity.HiCallDevice;

public class HiCallDeviceCreator {
    private HiCallDeviceCreator() {
    }

    public static HiCallDevice createHiCallDeviceFromDataContact(ResultSet resultSet) {
        int columnIndexForName;
        if (resultSet == null || (columnIndexForName = resultSet.getColumnIndexForName("data1")) == -1) {
            return null;
        }
        HiCallDevice hiCallDevice = new HiCallDevice(resultSet.getString(columnIndexForName));
        int columnIndexForName2 = resultSet.getColumnIndexForName("_id");
        if (columnIndexForName2 == -1) {
            return null;
        }
        hiCallDevice.setId(resultSet.getInt(columnIndexForName2));
        setOtherHiCallInfo(hiCallDevice, resultSet);
        return hiCallDevice;
    }

    private static void setOtherHiCallInfo(HiCallDevice hiCallDevice, ResultSet resultSet) {
        int columnIndexForName = resultSet.getColumnIndexForName("data4");
        if (columnIndexForName != -1) {
            hiCallDevice.setDeviceCommuncationId(resultSet.getString(columnIndexForName));
        }
        int columnIndexForName2 = resultSet.getColumnIndexForName("data5");
        if (columnIndexForName2 != -1) {
            hiCallDevice.setDeviceType(resultSet.getString(columnIndexForName2));
        }
        int columnIndexForName3 = resultSet.getColumnIndexForName("data6");
        if (columnIndexForName3 != -1) {
            hiCallDevice.setPrivateState(resultSet.getString(columnIndexForName3));
        }
        int columnIndexForName4 = resultSet.getColumnIndexForName("data7");
        if (columnIndexForName4 != -1) {
            hiCallDevice.setDeviceProfile(resultSet.getString(columnIndexForName4));
        }
        int columnIndexForName5 = resultSet.getColumnIndexForName("data8");
        if (columnIndexForName5 != -1) {
            hiCallDevice.setSameVibrationState(resultSet.getString(columnIndexForName5));
        }
        int columnIndexForName6 = resultSet.getColumnIndexForName("data9");
        if (columnIndexForName6 != -1) {
            hiCallDevice.setDeviceOrdinal(resultSet.getString(columnIndexForName6));
        }
        int columnIndexForName7 = resultSet.getColumnIndexForName("data10");
        if (columnIndexForName7 != -1) {
            hiCallDevice.setDeviceModel(resultSet.getString(columnIndexForName7));
        }
        int columnIndexForName8 = resultSet.getColumnIndexForName("data11");
        if (columnIndexForName8 != -1) {
            hiCallDevice.setRemarkName(resultSet.getString(columnIndexForName8));
        }
        int columnIndexForName9 = resultSet.getColumnIndexForName("data12");
        if (columnIndexForName9 != -1) {
            hiCallDevice.setUserName(resultSet.getString(columnIndexForName9));
        }
        int columnIndexForName10 = resultSet.getColumnIndexForName("data13");
        if (columnIndexForName10 != -1) {
            hiCallDevice.setDeviceInfo(resultSet.getString(columnIndexForName10));
        }
    }
}
