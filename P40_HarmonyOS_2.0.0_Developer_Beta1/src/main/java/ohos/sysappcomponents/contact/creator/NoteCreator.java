package ohos.sysappcomponents.contact.creator;

import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.contact.entity.Note;

public class NoteCreator {
    private NoteCreator() {
    }

    public static Note createNoteFromDataContact(ResultSet resultSet) {
        int columnIndexForName;
        if (resultSet == null || (columnIndexForName = resultSet.getColumnIndexForName("_id")) == -1) {
            return null;
        }
        int i = resultSet.getInt(columnIndexForName);
        int columnIndexForName2 = resultSet.getColumnIndexForName("data1");
        Note note = new Note();
        if (columnIndexForName2 == -1) {
            return null;
        }
        note.setNoteContent(resultSet.getString(columnIndexForName2));
        note.setId(i);
        return note;
    }
}
