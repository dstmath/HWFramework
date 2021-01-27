package ohos.sysappcomponents.contact.collection;

import ohos.data.resultset.ResultSet;

public class Collection {
    protected ResultSet mResultSet;

    public final boolean isEmpty() {
        ResultSet resultSet = this.mResultSet;
        if (resultSet == null) {
            return true;
        }
        boolean goToNextRow = resultSet.goToNextRow();
        if (goToNextRow) {
            this.mResultSet.goToPreviousRow();
        }
        return !goToNextRow;
    }

    public final void release() {
        ResultSet resultSet = this.mResultSet;
        if (resultSet != null) {
            resultSet.close();
        }
    }
}
