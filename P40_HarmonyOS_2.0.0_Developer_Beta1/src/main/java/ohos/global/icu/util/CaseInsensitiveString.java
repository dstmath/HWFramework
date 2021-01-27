package ohos.global.icu.util;

import ohos.global.icu.lang.UCharacter;

public class CaseInsensitiveString {
    private String folded = null;
    private int hash = 0;
    private String string;

    private static String foldCase(String str) {
        return UCharacter.foldCase(str, true);
    }

    private void getFolded() {
        if (this.folded == null) {
            this.folded = foldCase(this.string);
        }
    }

    public CaseInsensitiveString(String str) {
        this.string = str;
    }

    public String getString() {
        return this.string;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CaseInsensitiveString)) {
            return false;
        }
        getFolded();
        CaseInsensitiveString caseInsensitiveString = (CaseInsensitiveString) obj;
        caseInsensitiveString.getFolded();
        return this.folded.equals(caseInsensitiveString.folded);
    }

    public int hashCode() {
        getFolded();
        if (this.hash == 0) {
            this.hash = this.folded.hashCode();
        }
        return this.hash;
    }

    public String toString() {
        return this.string;
    }
}
