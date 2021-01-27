package ohos.sysappcomponents.contact.creator;

import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.entity.Name;

public class NameCreator {
    private NameCreator() {
    }

    public static Name createFullNameFromPhoneLookUp(ResultSet resultSet) {
        int columnIndexForName;
        if (resultSet == null || (columnIndexForName = resultSet.getColumnIndexForName(Attribute.PhoneFinder.DISPLAY_NAME)) == -1) {
            return null;
        }
        Name name = new Name();
        name.setFullName(resultSet.getString(columnIndexForName));
        return name;
    }

    public static Name createNameFromDataContact(ResultSet resultSet) {
        if (resultSet == null) {
            return null;
        }
        Name name = new Name();
        int columnIndexForName = resultSet.getColumnIndexForName("_id");
        if (columnIndexForName == -1) {
            return null;
        }
        name.setId(resultSet.getInt(columnIndexForName));
        int columnIndexForName2 = resultSet.getColumnIndexForName("data1");
        if (columnIndexForName2 == -1) {
            return null;
        }
        name.setFullName(resultSet.getString(columnIndexForName2));
        int columnIndexForName3 = resultSet.getColumnIndexForName("data2");
        if (columnIndexForName3 != -1) {
            name.setGivenName(resultSet.getString(columnIndexForName3));
        }
        int columnIndexForName4 = resultSet.getColumnIndexForName("data3");
        if (columnIndexForName4 != -1) {
            name.setFamilyName(resultSet.getString(columnIndexForName4));
        }
        int columnIndexForName5 = resultSet.getColumnIndexForName("data4");
        if (columnIndexForName5 != -1) {
            name.setNamePrefix(resultSet.getString(columnIndexForName5));
        }
        int columnIndexForName6 = resultSet.getColumnIndexForName("data5");
        if (columnIndexForName6 != -1) {
            name.setMiddleName(resultSet.getString(columnIndexForName6));
        }
        int columnIndexForName7 = resultSet.getColumnIndexForName("data6");
        if (columnIndexForName7 != -1) {
            name.setNameSuffix(resultSet.getString(columnIndexForName7));
        }
        int columnIndexForName8 = resultSet.getColumnIndexForName("data7");
        if (columnIndexForName8 != -1) {
            name.setGivenNamePhonetic(resultSet.getString(columnIndexForName8));
        }
        int columnIndexForName9 = resultSet.getColumnIndexForName("data8");
        if (columnIndexForName9 != -1) {
            name.setMiddleNamePhonetic(resultSet.getString(columnIndexForName9));
        }
        int columnIndexForName10 = resultSet.getColumnIndexForName("data9");
        if (columnIndexForName10 != -1) {
            name.setFamilyNamePhonetic(resultSet.getString(columnIndexForName10));
        }
        return name;
    }
}
