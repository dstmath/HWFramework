package ohos.sysappcomponents.contact.creator;

import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.entity.Organization;

public class OrganizationCreator {
    private OrganizationCreator() {
    }

    public static Organization createOrganizationFromPhoneLookup(ResultSet resultSet) {
        if (resultSet == null) {
            return null;
        }
        int columnIndexForName = resultSet.getColumnIndexForName(Attribute.PhoneFinder.COMPANY);
        int columnIndexForName2 = resultSet.getColumnIndexForName("title");
        if (columnIndexForName == -1 && columnIndexForName2 == -1) {
            return null;
        }
        String str = "";
        String string = columnIndexForName != -1 ? resultSet.getString(columnIndexForName) : str;
        if (columnIndexForName2 != -1) {
            str = resultSet.getString(columnIndexForName2);
        }
        Organization organization = new Organization();
        organization.setName(string);
        organization.setTitle(str);
        return organization;
    }

    public static Organization createOrganizationFromDataContact(ResultSet resultSet) {
        if (resultSet == null) {
            return null;
        }
        int columnIndexForName = resultSet.getColumnIndexForName("data1");
        String str = "";
        String string = columnIndexForName != -1 ? resultSet.getString(columnIndexForName) : str;
        int columnIndexForName2 = resultSet.getColumnIndexForName("data4");
        if (columnIndexForName2 != -1) {
            str = resultSet.getString(columnIndexForName2);
        }
        Organization organization = new Organization();
        organization.setName(string);
        organization.setTitle(str);
        int columnIndexForName3 = resultSet.getColumnIndexForName("_id");
        if (columnIndexForName3 == -1) {
            return null;
        }
        organization.setId(resultSet.getInt(columnIndexForName3));
        return organization;
    }
}
