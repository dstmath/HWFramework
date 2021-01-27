package ohos.sysappcomponents.contact.creator;

import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.entity.Holder;

public class HolderCreator {
    private static final int IVALID_COLUMN = -1;

    public static Holder createHolder(ResultSet resultSet) {
        int columnIndexForName = resultSet.getColumnIndexForName("_id");
        if (columnIndexForName == -1) {
            return null;
        }
        Holder holder = new Holder((long) resultSet.getInt(columnIndexForName));
        setHolder(resultSet, holder);
        return holder;
    }

    private static void setHolder(ResultSet resultSet, Holder holder) {
        int columnIndexForName = resultSet.getColumnIndexForName("_id");
        if (columnIndexForName != -1) {
            holder.setId(resultSet.getInt(columnIndexForName));
        }
        int columnIndexForName2 = resultSet.getColumnIndexForName(Attribute.Holder.PACKAGE_NAME);
        if (columnIndexForName2 != -1) {
            holder.setPackageName(resultSet.getString(columnIndexForName2));
        }
        int columnIndexForName3 = resultSet.getColumnIndexForName(Attribute.Holder.TYPE_RESOURCE_ID);
        if (columnIndexForName3 != -1) {
            holder.setTypeResourceId(resultSet.getInt(columnIndexForName3));
        }
        int columnIndexForName4 = resultSet.getColumnIndexForName(Attribute.Holder.DISPLAY_NAME);
        if (columnIndexForName4 != -1) {
            holder.setDisplayName(resultSet.getString(columnIndexForName4));
        }
        int columnIndexForName5 = resultSet.getColumnIndexForName(Attribute.Holder.HOLDER_AUTHORITY);
        if (columnIndexForName5 != -1) {
            holder.setAuthority(resultSet.getString(columnIndexForName5));
        }
        int columnIndexForName6 = resultSet.getColumnIndexForName(Attribute.Holder.ACCOUNT_TYPE);
        if (columnIndexForName6 != -1) {
            holder.setAccoutType(resultSet.getString(columnIndexForName6));
        }
        int columnIndexForName7 = resultSet.getColumnIndexForName(Attribute.Holder.ACCOUNT_NAME);
        if (columnIndexForName7 != -1) {
            holder.setAccountName(resultSet.getString(columnIndexForName7));
        }
        int columnIndexForName8 = resultSet.getColumnIndexForName(Attribute.Holder.EXPORT_SUPPORT);
        if (columnIndexForName8 != -1) {
            holder.setExportSupport(resultSet.getInt(columnIndexForName8));
        }
        int columnIndexForName9 = resultSet.getColumnIndexForName(Attribute.Holder.SHORTCUT_SUPPORT);
        if (columnIndexForName9 != -1) {
            holder.setShortcutSupport(resultSet.getInt(columnIndexForName9));
        }
        int columnIndexForName10 = resultSet.getColumnIndexForName(Attribute.Holder.PHOTO_SUPPORT);
        if (columnIndexForName10 != -1) {
            holder.setPhotoSupport(resultSet.getInt(columnIndexForName10));
        }
    }
}
