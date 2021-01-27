package ohos.sysappcomponents.contact.creator;

import ohos.app.Context;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.entity.PhoneNumber;

public class PhoneNumberCreator {
    private PhoneNumberCreator() {
    }

    public static PhoneNumber createFromPhoneLookup(Context context, ResultSet resultSet) {
        int columnIndexForName;
        int columnIndexForName2;
        String str = null;
        if (resultSet == null || (columnIndexForName = resultSet.getColumnIndexForName(Attribute.PhoneFinder.NUMBER)) == -1) {
            return null;
        }
        String string = resultSet.getString(columnIndexForName);
        int columnIndexForName3 = resultSet.getColumnIndexForName(Attribute.PhoneFinder.TYPE);
        int i = columnIndexForName3 != -1 ? resultSet.getInt(columnIndexForName3) : -1;
        if (i == 0 && (columnIndexForName2 = resultSet.getColumnIndexForName(Attribute.PhoneFinder.LABEL)) != -1) {
            str = resultSet.getString(columnIndexForName2);
        }
        if (str == null) {
            return new PhoneNumber(context, string, i);
        }
        return new PhoneNumber(string, str);
    }

    public static PhoneNumber createPhoneNumberFromDataContact(Context context, ResultSet resultSet) {
        int columnIndexForName;
        PhoneNumber phoneNumber;
        int columnIndexForName2;
        if (resultSet == null || (columnIndexForName = resultSet.getColumnIndexForName("data1")) == -1) {
            return null;
        }
        String string = resultSet.getString(columnIndexForName);
        int columnIndexForName3 = resultSet.getColumnIndexForName("data2");
        if (columnIndexForName3 == -1) {
            return null;
        }
        int i = resultSet.getInt(columnIndexForName3);
        String string2 = (i != 0 || (columnIndexForName2 = resultSet.getColumnIndexForName("data3")) == -1) ? null : resultSet.getString(columnIndexForName2);
        if (string2 == null) {
            phoneNumber = new PhoneNumber(context, string, i);
        } else {
            phoneNumber = new PhoneNumber(string, string2);
        }
        int columnIndexForName4 = resultSet.getColumnIndexForName("_id");
        if (columnIndexForName4 == -1) {
            return null;
        }
        phoneNumber.setId(resultSet.getInt(columnIndexForName4));
        return phoneNumber;
    }
}
