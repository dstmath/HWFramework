package ohos.sysappcomponents.contact.entity;

public class Note {
    private int id;
    private String noteContent;

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public String getNoteContent() {
        return this.noteContent;
    }

    public void setNoteContent(String str) {
        this.noteContent = str;
    }
}
