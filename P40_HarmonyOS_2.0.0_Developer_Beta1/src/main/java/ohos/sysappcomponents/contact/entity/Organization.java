package ohos.sysappcomponents.contact.entity;

public class Organization {
    private int id;
    private String name;
    private String title;

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String str) {
        this.title = str;
    }
}
