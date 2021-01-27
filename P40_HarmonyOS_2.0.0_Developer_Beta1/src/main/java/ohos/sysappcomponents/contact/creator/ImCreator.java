package ohos.sysappcomponents.contact.creator;

import ohos.app.Context;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.entity.ImAddress;

public class ImCreator {
    private ImCreator() {
    }

    public static ImAddress createImFromDataContact(Context context, ResultSet resultSet) {
        int columnIndexForName;
        ImAddress imAddress;
        int columnIndexForName2;
        if (resultSet == null || (columnIndexForName = resultSet.getColumnIndexForName("data1")) == -1) {
            return null;
        }
        String string = resultSet.getString(columnIndexForName);
        int columnIndexForName3 = resultSet.getColumnIndexForName("data5");
        if (columnIndexForName3 == -1) {
            return null;
        }
        int i = resultSet.getInt(columnIndexForName3);
        String string2 = (i != -1 || (columnIndexForName2 = resultSet.getColumnIndexForName("data6")) == -1) ? null : resultSet.getString(columnIndexForName2);
        if (string2 == null) {
            imAddress = new ImAddress(context, string, i);
        } else {
            imAddress = new ImAddress(string, string2);
        }
        int columnIndexForName4 = resultSet.getColumnIndexForName("_id");
        if (columnIndexForName4 == -1) {
            return null;
        }
        imAddress.setId(resultSet.getInt(columnIndexForName4));
        return imAddress;
    }
}
