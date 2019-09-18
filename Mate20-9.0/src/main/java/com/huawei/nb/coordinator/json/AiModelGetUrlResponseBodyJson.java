package com.huawei.nb.coordinator.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

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

    public void setResid(String resid2) {
        this.resid = resid2;
    }

    public String getPackageX() {
        return this.packageX;
    }

    public void setPackageX(String packageX2) {
        this.packageX = packageX2;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version2) {
        this.version = version2;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url2) {
        this.url = url2;
    }

    public String getTeams() {
        return this.teams;
    }

    public void setTeams(String teams2) {
        this.teams = teams2;
    }

    public String getZipSha256() {
        return this.zipSha256;
    }

    public void setZipSha256(String zipSha2562) {
        this.zipSha256 = zipSha2562;
    }

    public String getDecryptedKey() {
        return this.decryptedKey;
    }

    public void setDecryptedKey(String decryptedKey2) {
        this.decryptedKey = decryptedKey2;
    }

    public boolean isType() {
        return this.type;
    }

    public void setType(boolean type2) {
        this.type = type2;
    }

    public boolean isUpdate() {
        return this.update;
    }

    public void setUpdate(boolean update2) {
        this.update = update2;
    }

    public String getXpu() {
        return this.xpu;
    }

    public void setXpu(String xpu2) {
        this.xpu = xpu2;
    }

    public String getEmuiFamily() {
        return this.emuiFamily;
    }

    public void setEmuiFamily(String emuiFamily2) {
        this.emuiFamily = emuiFamily2;
    }

    public String getProductFamily() {
        return this.productFamily;
    }

    public void setProductFamily(String productFamily2) {
        this.productFamily = productFamily2;
    }

    public String getChipsetVendor() {
        return this.chipsetVendor;
    }

    public void setChipsetVendor(String chipsetVendor2) {
        this.chipsetVendor = chipsetVendor2;
    }

    public String getChipset() {
        return this.chipset;
    }

    public void setChipset(String chipset2) {
        this.chipset = chipset2;
    }

    public String getProduct() {
        return this.product;
    }

    public void setProduct(String product2) {
        this.product = product2;
    }

    public String getProductModel() {
        return this.productModel;
    }

    public void setProductModel(String productModel2) {
        this.productModel = productModel2;
    }

    public String getDistrict() {
        return this.district;
    }

    public void setDistrict(String district2) {
        this.district = district2;
    }

    public String getAbTest() {
        return this.abTest;
    }

    public void setAbTest(String abTest2) {
        this.abTest = abTest2;
    }

    public String getInterfaceVersion() {
        return this.interfaceVersion;
    }

    public void setInterfaceVersion(String interfaceVersion2) {
        this.interfaceVersion = interfaceVersion2;
    }

    public String getParam1() {
        return this.param1;
    }

    public void setParam1(String param12) {
        this.param1 = param12;
    }

    public String getParam2() {
        return this.param2;
    }

    public void setParam2(String param22) {
        this.param2 = param22;
    }

    public JsonObject getExtra() {
        return this.extra;
    }

    public void setExtra(JsonObject extra2) {
        this.extra = extra2;
    }

    public JsonArray getAppVersion() {
        return this.appVersion;
    }

    public void setAppVersion(JsonArray appVersion2) {
        this.appVersion = appVersion2;
    }
}
