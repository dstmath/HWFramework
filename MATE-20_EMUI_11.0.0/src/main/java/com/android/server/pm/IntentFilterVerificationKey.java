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
        String str = this.domains;
        if (str == null ? that.domains != null : !str.equals(that.domains)) {
            return false;
        }
        String str2 = this.className;
        if (str2 == null ? that.className != null : !str2.equals(that.className)) {
            return false;
        }
        String str3 = this.packageName;
        if (str3 == null ? that.packageName == null : str3.equals(that.packageName)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        String str = this.domains;
        int i = 0;
        int hashCode = (str != null ? str.hashCode() : 0) * 31;
        String str2 = this.packageName;
        int result = (hashCode + (str2 != null ? str2.hashCode() : 0)) * 31;
        String str3 = this.className;
        if (str3 != null) {
            i = str3.hashCode();
        }
        return result + i;
    }
}
