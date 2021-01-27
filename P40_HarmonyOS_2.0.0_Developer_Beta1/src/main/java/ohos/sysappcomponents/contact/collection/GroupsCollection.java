package ohos.sysappcomponents.contact.collection;

import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.creator.GroupCreator;
import ohos.sysappcomponents.contact.entity.Group;

public class GroupsCollection extends Collection {
    public GroupsCollection(ResultSet resultSet) {
        this.mResultSet = resultSet;
    }

    public Group next() {
        if (this.mResultSet != null && this.mResultSet.goToNextRow()) {
            return GroupCreator.createGroupFromGroupsTable(this.mResultSet);
        }
        return null;
    }
}
