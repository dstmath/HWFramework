package ohos.sysappcomponents.contact.creator;

import android.net.Uri;
import ohos.data.resultset.ResultSet;
import ohos.net.UriConverter;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.entity.Portrait;

public class PhotoCreator {
    private PhotoCreator() {
    }

    public static Portrait createFromPhoneLookup(ResultSet resultSet) {
        String string;
        if (resultSet == null) {
            return null;
        }
        Portrait portrait = new Portrait();
        int columnIndexForName = resultSet.getColumnIndexForName(Attribute.PhoneFinder.PHOTO_URI);
        if (!(columnIndexForName == -1 || (string = resultSet.getString(columnIndexForName)) == null)) {
            portrait.setUri(UriConverter.convertToZidaneContentUri(Uri.parse(string), ""));
        }
        int columnIndexForName2 = resultSet.getColumnIndexForName(Attribute.PhoneFinder.PHOTO_FILE_ID);
        if (columnIndexForName2 != -1) {
            portrait.setPortraitFileId(resultSet.getInt(columnIndexForName2));
        }
        return portrait;
    }

    public static Portrait createPortraitFromDataContact(ResultSet resultSet) {
        Portrait portrait = new Portrait();
        int columnIndexForName = resultSet.getColumnIndexForName("_id");
        if (columnIndexForName == -1) {
            return null;
        }
        int i = resultSet.getInt(columnIndexForName);
        int columnIndexForName2 = resultSet.getColumnIndexForName("data14");
        if (columnIndexForName2 != -1) {
            portrait.setPortraitFileId(resultSet.getInt(columnIndexForName2));
        }
        portrait.setId(i);
        return portrait;
    }
}
