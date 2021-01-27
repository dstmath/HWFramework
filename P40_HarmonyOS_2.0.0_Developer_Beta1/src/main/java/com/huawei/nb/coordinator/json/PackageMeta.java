package com.huawei.nb.coordinator.json;

import java.util.List;

public class PackageMeta {
    private String name;
    private String packageHashes;
    private String packageHashesSign;
    private List<PackagesBean> packages;

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getPackageHashes() {
        return this.packageHashes;
    }

    public void setPackageHashes(String str) {
        this.packageHashes = str;
    }

    public String getPackageHashesSign() {
        return this.packageHashesSign;
    }

    public void setPackageHashesSign(String str) {
        this.packageHashesSign = str;
    }

    public List<PackagesBean> getPackages() {
        return this.packages;
    }

    public void setPackages(List<PackagesBean> list) {
        this.packages = list;
    }

    public static class PackagesBean {
        private String name;
        private String size;

        public String getName() {
            return this.name;
        }

        public void setName(String str) {
            this.name = str;
        }

        public String getSize() {
            return this.size;
        }

        public void setSize(String str) {
            this.size = str;
        }
    }
}
