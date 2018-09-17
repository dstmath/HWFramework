package com.android.server.pm;

class IntentFilterVerificationKey {
    public String className;
    public String domains;
    public String packageName;

    public IntentFilterVerificationKey(String[] domains, String packageName, String className) {
        StringBuilder sb = new StringBuilder();
        for (String host : domains) {
            sb.append(host);
        }
        this.domains = sb.toString();
        this.packageName = packageName;
        this.className = className;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IntentFilterVerificationKey that = (IntentFilterVerificationKey) o;
        if (this.domains == null ? that.domains != null : (this.domains.equals(that.domains) ^ 1) != 0) {
            return false;
        }
        if (this.className == null ? that.className != null : (this.className.equals(that.className) ^ 1) != 0) {
            return false;
        }
        return this.packageName == null ? that.packageName != null : (this.packageName.equals(that.packageName) ^ 1) != 0;
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        int hashCode2 = (this.domains != null ? this.domains.hashCode() : 0) * 31;
        if (this.packageName != null) {
            hashCode = this.packageName.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode2 + hashCode) * 31;
        if (this.className != null) {
            i = this.className.hashCode();
        }
        return hashCode + i;
    }
}
