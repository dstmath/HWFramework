package ohos.sysappcomponents.contact.creator;

import ohos.app.Context;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.entity.Email;

public class EmailCreator {
    private EmailCreator() {
    }

    public static Email createEmailFromDataContact(Context context, ResultSet resultSet) {
        int columnIndexForName;
        Email email;
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
        int columnIndexForName4 = resultSet.getColumnIndexForName("data4");
        String string3 = columnIndexForName4 != -1 ? resultSet.getString(columnIndexForName4) : null;
        if (string2 == null) {
            email = new Email(context, string, i);
        } else {
            email = new Email(string, string2);
        }
        int columnIndexForName5 = resultSet.getColumnIndexForName("_id");
        if (columnIndexForName5 == -1) {
            return null;
        }
        email.setId(resultSet.getInt(columnIndexForName5));
        email.setDisplayName(string3);
        return email;
    }
}
