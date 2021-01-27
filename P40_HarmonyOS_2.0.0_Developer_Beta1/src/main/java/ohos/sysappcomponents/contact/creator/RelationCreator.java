package ohos.sysappcomponents.contact.creator;

import ohos.app.Context;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.entity.Relation;

public class RelationCreator {
    private RelationCreator() {
    }

    public static Relation createRelationFromDataContact(Context context, ResultSet resultSet) {
        int columnIndexForName;
        Relation relation;
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
            relation = new Relation(context, string, i);
        } else {
            relation = new Relation(string, string2);
        }
        int columnIndexForName4 = resultSet.getColumnIndexForName("_id");
        if (columnIndexForName4 == -1) {
            return null;
        }
        relation.setId(resultSet.getInt(columnIndexForName4));
        return relation;
    }
}
