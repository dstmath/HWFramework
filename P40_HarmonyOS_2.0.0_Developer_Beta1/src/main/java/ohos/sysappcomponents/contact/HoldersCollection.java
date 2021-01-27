package ohos.sysappcomponents.contact;

import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.collection.Collection;
import ohos.sysappcomponents.contact.creator.HolderCreator;
import ohos.sysappcomponents.contact.entity.Holder;

public class HoldersCollection extends Collection {
    public HoldersCollection(ResultSet resultSet) {
        this.mResultSet = resultSet;
    }

    public Holder next() {
        if (this.mResultSet != null && this.mResultSet.goToNextRow()) {
            return HolderCreator.createHolder(this.mResultSet);
        }
        return null;
    }
}
