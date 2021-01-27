package ohos.sysappcomponents.contact.creator;

import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.entity.Website;

public class WebsiteCreator {
    private WebsiteCreator() {
    }

    public static Website createWebsiteFromDataContact(ResultSet resultSet) {
        int columnIndexForName;
        if (resultSet == null || (columnIndexForName = resultSet.getColumnIndexForName("_id")) == -1) {
            return null;
        }
        int i = resultSet.getInt(columnIndexForName);
        int columnIndexForName2 = resultSet.getColumnIndexForName("data1");
        if (columnIndexForName2 == -1) {
            return null;
        }
        String string = resultSet.getString(columnIndexForName2);
        Website website = new Website();
        website.setId(i);
        website.setWebsite(string);
        return website;
    }
}
