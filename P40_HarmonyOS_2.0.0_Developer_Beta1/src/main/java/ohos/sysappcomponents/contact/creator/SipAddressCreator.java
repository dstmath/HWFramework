package ohos.sysappcomponents.contact.creator;

import ohos.app.Context;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.entity.SipAddress;

public class SipAddressCreator {
    private SipAddressCreator() {
    }

    public static SipAddress createSipAddressFromDataContact(Context context, ResultSet resultSet) {
        int columnIndexForName;
        SipAddress sipAddress;
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
            sipAddress = new SipAddress(context, string, i);
        } else {
            sipAddress = new SipAddress(string, string2);
        }
        int columnIndexForName4 = resultSet.getColumnIndexForName("_id");
        if (columnIndexForName4 == -1) {
            return null;
        }
        sipAddress.setId(resultSet.getInt(columnIndexForName4));
        return sipAddress;
    }
}
