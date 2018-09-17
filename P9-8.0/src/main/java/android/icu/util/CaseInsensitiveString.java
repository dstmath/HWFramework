package android.icu.util;

import android.icu.lang.UCharacter;

public class CaseInsensitiveString {
    private String folded = null;
    private int hash = 0;
    private String string;

    private static String foldCase(String foldee) {
        return UCharacter.foldCase(foldee, true);
    }

    private void getFolded() {
        if (this.folded == null) {
            this.folded = foldCase(this.string);
        }
    }

    public CaseInsensitiveString(String s) {
        this.string = s;
    }

    public String getString() {
        return this.string;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof CaseInsensitiveString)) {
            return false;
        }
        getFolded();
        CaseInsensitiveString cis = (CaseInsensitiveString) o;
        cis.getFolded();
        return this.folded.equals(cis.folded);
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
