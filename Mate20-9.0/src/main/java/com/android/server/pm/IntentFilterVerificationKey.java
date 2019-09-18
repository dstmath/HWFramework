package com.android.server.pm;

class IntentFilterVerificationKey {
    public String className;
    public String domains;
    public String packageName;

    public IntentFilterVerificationKey(String[] domains2, String packageName2, String className2) {
        StringBuilder sb = new StringBuilder();
        for (String host : domains2) {
            sb.append(host);
        }
        this.domains = sb.toString();
        this.packageName = packageName2;
        this.className = className2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IntentFilterVerificationKey that = (IntentFilterVerificationKey) o;
        if (this.domains == null ? that.domains != null : !this.domains.equals(that.domains)) {
            return false;
        }
        if (this.className == null ? that.className != null : !this.className.equals(that.className)) {
            return false;
        }
        if (this.packageName == null ? that.packageName == null : this.packageName.equals(that.packageName)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = 31 * ((31 * (this.domains != null ? this.domains.hashCode() : 0)) + (this.packageName != null ? this.packageName.hashCode() : 0));
        if (this.className != null) {
            i = this.className.hashCode();
        }
        return hashCode + i;
    }
}
