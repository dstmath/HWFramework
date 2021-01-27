package com.huawei.nb.coordinator.json;

import com.huawei.gson.JsonArray;
import com.huawei.gson.JsonObject;
import com.huawei.gson.annotations.SerializedName;

public class AiModelGetUrlResponseBodyJson {
    private String abTest;
    private JsonArray appVersion;
    private String chipset;
    private String chipsetVendor;
    private String decryptedKey;
    private String district;
    private String emuiFamily;
    private JsonObject extra;
    private String interfaceVersion;
    @SerializedName("package")
    private String packageX;
    private String param1;
    private String param2;
    private String product;
    private String productFamily;
    private String productModel;
    private String resid;
    private String teams;
    private boolean type;
    private boolean update;
    private String url;
    private String version;
    private String xpu;
    private String zipSha256;

    public String getResid() {
        return this.resid;
    }

    public void setResid(String str) {
        this.resid = str;
    }

    public String getPackageX() {
        return this.packageX;
    }

    public void setPackageX(String str) {
        this.packageX = str;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String str) {
        this.version = str;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public String getTeams() {
        return this.teams;
    }

    public void setTeams(String str) {
        this.teams = str;
    }

    public String getZipSha256() {
        return this.zipSha256;
    }

    public void setZipSha256(String str) {
        this.zipSha256 = str;
    }

    public String getDecryptedKey() {
        return this.decryptedKey;
    }

    public void setDecryptedKey(String str) {
        this.decryptedKey = str;
    }

    public boolean isType() {
        return this.type;
    }

    public void setType(boolean z) {
        this.type = z;
    }

    public boolean isUpdate() {
        return this.update;
    }

    public void setUpdate(boolean z) {
        this.update = z;
    }

    public String getXpu() {
        return this.xpu;
    }

    public void setXpu(String str) {
        this.xpu = str;
    }

    public String getEmuiFamily() {
        return this.emuiFamily;
    }

    public void setEmuiFamily(String str) {
        this.emuiFamily = str;
    }

    public String getProductFamily() {
        return this.productFamily;
    }

    public void setProductFamily(String str) {
        this.productFamily = str;
    }

    public String getChipsetVendor() {
        return this.chipsetVendor;
    }

    public void setChipsetVendor(String str) {
        this.chipsetVendor = str;
    }

    public String getChipset() {
        return this.chipset;
    }

    public void setChipset(String str) {
        this.chipset = str;
    }

    public String getProduct() {
        return this.product;
    }

    public void setProduct(String str) {
        this.product = str;
    }

    public String getProductModel() {
        return this.productModel;
    }

    public void setProductModel(String str) {
        this.productModel = str;
    }

    public String getDistrict() {
        return this.district;
    }

    public void setDistrict(String str) {
        this.district = str;
    }

    public String getAbTest() {
        return this.abTest;
    }

    public void setAbTest(String str) {
        this.abTest = str;
    }

    public String getInterfaceVersion() {
        return this.interfaceVersion;
    }

    public void setInterfaceVersion(String str) {
        this.interfaceVersion = str;
    }

    public String getParam1() {
        return this.param1;
    }

    public void setParam1(String str) {
        this.param1 = str;
    }

    public String getParam2() {
        return this.param2;
    }

    public void setParam2(String str) {
        this.param2 = str;
    }

    public JsonObject getExtra() {
        return this.extra;
    }

    public void setExtra(JsonObject jsonObject) {
        this.extra = jsonObject;
    }

    public JsonArray getAppVersion() {
        return this.appVersion;
    }

    public void setAppVersion(JsonArray jsonArray) {
        this.appVersion = jsonArray;
    }
}
