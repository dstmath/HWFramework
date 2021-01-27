package ohos.sysappcomponents.contact.entity;

public class Identity {
    private int id;
    private String identity;
    private String nameSpace;

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public String getIdentity() {
        return this.identity;
    }

    public void setIdentity(String str) {
        this.identity = str;
    }

    public String getNameSpace() {
        return this.nameSpace;
    }

    public void setNameSpace(String str) {
        this.nameSpace = str;
    }
}
