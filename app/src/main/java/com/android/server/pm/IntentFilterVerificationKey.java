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
        if (!this.domains == null ? this.domains.equals(that.domains) : that.domains == null) {
            return false;
        }
        if (this.className == null ? that.className == null : this.className.equals(that.className)) {
            return this.packageName == null ? that.packageName == null : this.packageName.equals(that.packageName);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result;
        int hashCode;
        int i = 0;
        if (this.domains != null) {
            result = this.domains.hashCode();
        } else {
            result = 0;
        }
        int i2 = result * 31;
        if (this.packageName != null) {
            hashCode = this.packageName.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (i2 + hashCode) * 31;
        if (this.className != null) {
            i = this.className.hashCode();
        }
        return hashCode + i;
    }
}
