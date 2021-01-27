package ohos.sysappcomponents.contact.creator;

import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.entity.Group;

public class GroupCreator {
    private GroupCreator() {
    }

    public static Group createGroupFromGroupsTable(ResultSet resultSet) {
        int columnIndexForName;
        if (resultSet == null || (columnIndexForName = resultSet.getColumnIndexForName("_id")) == -1) {
            return null;
        }
        Group group = new Group();
        group.setGroupId(resultSet.getInt(columnIndexForName));
        int columnIndexForName2 = resultSet.getColumnIndexForName("title");
        if (columnIndexForName2 != -1) {
            group.setTitle(resultSet.getString(columnIndexForName2));
        }
        return group;
    }

    public static Group createGroupFromDataContact(ResultSet resultSet) {
        int columnIndexForName;
        if (resultSet == null || (columnIndexForName = resultSet.getColumnIndexForName("_id")) == -1) {
            return null;
        }
        int i = resultSet.getInt(columnIndexForName);
        int columnIndexForName2 = resultSet.getColumnIndexForName("data1");
        if (columnIndexForName2 == -1) {
            return null;
        }
        int i2 = resultSet.getInt(columnIndexForName2);
        Group group = new Group();
        group.setGroupId(i2);
        group.setId(i);
        return group;
    }
}
