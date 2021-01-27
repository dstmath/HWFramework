package ohos.sysappcomponents.contact.entity;

import ohos.app.Context;
import ohos.sysappcomponents.contact.Attribute;

public class PostalAddress extends Label {
    public static final int ADDR_HOME = 1;
    public static final int ADDR_OTHER = 3;
    public static final int ADDR_WORK = 2;
    private String city;
    private String country;
    private int id;
    private String neighborhood;
    private String pobox;
    private String postcode;
    private String region;
    private String street;

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public int getDefaultLabelId() {
        return 1;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public boolean isValidLabel(int i) {
        return i >= 0 && i <= 3;
    }

    public PostalAddress(Context context, String str, int i) {
        super(context, str, i);
    }

    public PostalAddress(String str, String str2) {
        super(str, 0, str2);
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public String getStreet() {
        return this.street;
    }

    public void setStreet(String str) {
        this.street = str;
    }

    public String getPobox() {
        return this.pobox;
    }

    public void setPobox(String str) {
        this.pobox = str;
    }

    public String getNeighborhood() {
        return this.neighborhood;
    }

    public void setNeighborhood(String str) {
        this.neighborhood = str;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String str) {
        this.city = str;
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String str) {
        this.region = str;
    }

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String str) {
        this.postcode = str;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String str) {
        this.country = str;
    }

    public String getPostalAddress() {
        return getMainData();
    }

    public void setPostalAddress(String str) {
        setMainData(str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public String getLabelNameResId(Context context, int i) {
        return Attribute.CommonDataKinds.PostalAddress.getLabelNameResId(context, i);
    }
}
