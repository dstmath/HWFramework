package com.huawei.nb.coordinator.json;

import java.util.List;

public class PackageMeta {
    private String name;
    private String packageHashes;
    private String packageHashesSign;
    private List<PackagesBean> packages;

    public static class PackagesBean {
        private String name;
        private String size;

        public String getName() {
            return this.name;
        }

        public void setName(String name2) {
            this.name = name2;
        }

        public String getSize() {
            return this.size;
        }

        public void setSize(String size2) {
            this.size = size2;
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public String getPackageHashes() {
        return this.packageHashes;
    }

    public void setPackageHashes(String packageHashes2) {
        this.packageHashes = packageHashes2;
    }

    public String getPackageHashesSign() {
        return this.packageHashesSign;
    }

    public void setPackageHashesSign(String packageHashesSign2) {
        this.packageHashesSign = packageHashesSign2;
    }

    public List<PackagesBean> getPackages() {
        return this.packages;
    }

    public void setPackages(List<PackagesBean> packages2) {
        this.packages = packages2;
    }
}
