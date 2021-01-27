package ohos.sysappcomponents.contact.entity;

public class Group {
    private int groupId;
    private int id;
    private String title;

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public int getGroupId() {
        return this.groupId;
    }

    public void setGroupId(int i) {
        this.groupId = i;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String str) {
        this.title = str;
    }
}
