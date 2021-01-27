package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonenumber;
import gov.nist.core.Separators;
import java.util.Arrays;

public final class PhoneNumberMatch {
    private final Phonenumber.PhoneNumber number;
    private final String rawString;
    private final int start;

    PhoneNumberMatch(int start2, String rawString2, Phonenumber.PhoneNumber number2) {
        if (start2 < 0) {
            throw new IllegalArgumentException("Start index must be >= 0.");
        } else if (rawString2 == null || number2 == null) {
            throw new NullPointerException();
        } else {
            this.start = start2;
            this.rawString = rawString2;
            this.number = number2;
        }
    }

    public Phonenumber.PhoneNumber number() {
        return this.number;
    }

    public int start() {
        return this.start;
    }

    public int end() {
        return this.start + this.rawString.length();
    }

    public String rawString() {
        return this.rawString;
    }

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Integer.valueOf(this.start), this.rawString, this.number});
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PhoneNumberMatch)) {
            return false;
        }
        PhoneNumberMatch other = (PhoneNumberMatch) obj;
        if (!this.rawString.equals(other.rawString) || this.start != other.start || !this.number.equals(other.number)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "PhoneNumberMatch [" + start() + Separators.COMMA + end() + ") " + this.rawString;
    }
}
