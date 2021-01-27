package ohos.sysappcomponents.contact.entity;

public class Name {
    private String familyName;
    private String familyNamePhonetic;
    private String fullName;
    private String givenName;
    private String givenNamePhonetic;
    private int id;
    private String middleName;
    private String middleNamePhonetic;
    private String namePrefix;
    private String nameSuffix;

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String str) {
        this.fullName = str;
    }

    public String getGivenName() {
        return this.givenName;
    }

    public void setGivenName(String str) {
        this.givenName = str;
    }

    public String getFamilyName() {
        return this.familyName;
    }

    public void setFamilyName(String str) {
        this.familyName = str;
    }

    public String getNamePrefix() {
        return this.namePrefix;
    }

    public void setNamePrefix(String str) {
        this.namePrefix = str;
    }

    public String getMiddleName() {
        return this.middleName;
    }

    public void setMiddleName(String str) {
        this.middleName = str;
    }

    public String getNameSuffix() {
        return this.nameSuffix;
    }

    public void setNameSuffix(String str) {
        this.nameSuffix = str;
    }

    public String getGivenNamePhonetic() {
        return this.givenNamePhonetic;
    }

    public void setGivenNamePhonetic(String str) {
        this.givenNamePhonetic = str;
    }

    public String getMiddleNamePhonetic() {
        return this.middleNamePhonetic;
    }

    public void setMiddleNamePhonetic(String str) {
        this.middleNamePhonetic = str;
    }

    public String getFamilyNamePhonetic() {
        return this.familyNamePhonetic;
    }

    public void setFamilyNamePhonetic(String str) {
        this.familyNamePhonetic = str;
    }
}
