package ohos.sysappcomponents.contact.creator;

import ohos.app.Context;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.entity.PostalAddress;

public class PostalAddressCreator {
    private PostalAddressCreator() {
    }

    public static PostalAddress createPostalAddressFromDataContact(Context context, ResultSet resultSet) {
        int columnIndexForName;
        PostalAddress postalAddress;
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
            postalAddress = new PostalAddress(context, string, i);
        } else {
            postalAddress = new PostalAddress(string, string2);
        }
        int columnIndexForName4 = resultSet.getColumnIndexForName("_id");
        if (columnIndexForName4 == -1) {
            return null;
        }
        postalAddress.setId(resultSet.getInt(columnIndexForName4));
        addOtherPostalInfo(postalAddress, resultSet);
        return postalAddress;
    }

    private static void addOtherPostalInfo(PostalAddress postalAddress, ResultSet resultSet) {
        int columnIndexForName = resultSet.getColumnIndexForName("data4");
        if (columnIndexForName != -1) {
            postalAddress.setStreet(resultSet.getString(columnIndexForName));
        }
        int columnIndexForName2 = resultSet.getColumnIndexForName("data5");
        if (columnIndexForName2 != -1) {
            postalAddress.setPobox(resultSet.getString(columnIndexForName2));
        }
        int columnIndexForName3 = resultSet.getColumnIndexForName("data6");
        if (columnIndexForName3 != -1) {
            postalAddress.setNeighborhood(resultSet.getString(columnIndexForName3));
        }
        int columnIndexForName4 = resultSet.getColumnIndexForName("data7");
        if (columnIndexForName4 != -1) {
            postalAddress.setCity(resultSet.getString(columnIndexForName4));
        }
        int columnIndexForName5 = resultSet.getColumnIndexForName("data8");
        if (columnIndexForName5 != -1) {
            postalAddress.setRegion(resultSet.getString(columnIndexForName5));
        }
        int columnIndexForName6 = resultSet.getColumnIndexForName("data9");
        if (columnIndexForName6 != -1) {
            postalAddress.setPostcode(resultSet.getString(columnIndexForName6));
        }
        int columnIndexForName7 = resultSet.getColumnIndexForName("data10");
        if (columnIndexForName7 != -1) {
            postalAddress.setCountry(resultSet.getString(columnIndexForName7));
        }
    }
}
