package ohos.sysappcomponents.contact.entity;

public class Holder {
    private static final int IVALID_COLUMN = -1;
    private String accountName;
    private String accoutType;
    private String authority;
    private String displayName;
    private int exportSupport;
    private long holderId = 0;
    private int id;
    private String packageName;
    private int photoSupport;
    private int shortcutSupport;
    private int typeResourceId;

    public Holder(long j) {
        this.holderId = j;
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public long getHolderId() {
        return this.holderId;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public String getBundleName() {
        return this.packageName;
    }

    public void setDisplayName(String str) {
        this.displayName = str;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setAuthority(String str) {
        this.authority = str;
    }

    public String getAuthority() {
        return this.authority;
    }

    public void setAccoutType(String str) {
        this.accoutType = str;
    }

    public String getAccoutType() {
        return this.accoutType;
    }

    public void setAccountName(String str) {
        this.accountName = str;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public void setExportSupport(int i) {
        this.exportSupport = i;
    }

    public int getExportSupport() {
        return this.exportSupport;
    }

    public void setShortcutSupport(int i) {
        this.shortcutSupport = i;
    }

    public int getShortcutSupport() {
        return this.shortcutSupport;
    }

    public void setPhotoSupport(int i) {
        this.photoSupport = i;
    }

    public int getPhotoSupport() {
        return this.photoSupport;
    }

    public void setTypeResourceId(int i) {
        this.typeResourceId = i;
    }

    public int getTypeResourceId() {
        return this.typeResourceId;
    }
}
