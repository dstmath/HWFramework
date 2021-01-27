package ohos.sysappcomponents.contact.creator;

import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.entity.Identity;

public class IdentityCreator {
    private IdentityCreator() {
    }

    public static Identity createIdentityFromDataContact(ResultSet resultSet) {
        int columnIndexForName;
        if (resultSet == null || (columnIndexForName = resultSet.getColumnIndexForName("data1")) == -1) {
            return null;
        }
        String string = resultSet.getString(columnIndexForName);
        int columnIndexForName2 = resultSet.getColumnIndexForName("data2");
        String string2 = columnIndexForName2 != -1 ? resultSet.getString(columnIndexForName2) : null;
        Identity identity = new Identity();
        identity.setIdentity(string);
        identity.setNameSpace(string2);
        int columnIndexForName3 = resultSet.getColumnIndexForName("_id");
        if (columnIndexForName3 == -1) {
            return null;
        }
        identity.setId(resultSet.getInt(columnIndexForName3));
        return identity;
    }
}
