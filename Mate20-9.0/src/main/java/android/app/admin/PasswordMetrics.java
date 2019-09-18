package android.app.admin;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PasswordMetrics implements Parcelable {
    private static final int CHAR_DIGIT = 2;
    private static final int CHAR_LOWER_CASE = 0;
    private static final int CHAR_SYMBOL = 3;
    private static final int CHAR_UPPER_CASE = 1;
    public static final Parcelable.Creator<PasswordMetrics> CREATOR = new Parcelable.Creator<PasswordMetrics>() {
        public PasswordMetrics createFromParcel(Parcel in) {
            return new PasswordMetrics(in);
        }

        public PasswordMetrics[] newArray(int size) {
            return new PasswordMetrics[size];
        }
    };
    public static final int MAX_ALLOWED_SEQUENCE = 3;
    public int length;
    public int letters;
    public int lowerCase;
    public int nonLetter;
    public int numeric;
    public int quality;
    public int symbols;
    public int upperCase;

    @Retention(RetentionPolicy.SOURCE)
    private @interface CharacterCatagory {
    }

    public PasswordMetrics() {
        this.quality = 0;
        this.length = 0;
        this.letters = 0;
        this.upperCase = 0;
        this.lowerCase = 0;
        this.numeric = 0;
        this.symbols = 0;
        this.nonLetter = 0;
    }

    public PasswordMetrics(int quality2, int length2) {
        this.quality = 0;
        this.length = 0;
        this.letters = 0;
        this.upperCase = 0;
        this.lowerCase = 0;
        this.numeric = 0;
        this.symbols = 0;
        this.nonLetter = 0;
        this.quality = quality2;
        this.length = length2;
    }

    public PasswordMetrics(int quality2, int length2, int letters2, int upperCase2, int lowerCase2, int numeric2, int symbols2, int nonLetter2) {
        this(quality2, length2);
        this.letters = letters2;
        this.upperCase = upperCase2;
        this.lowerCase = lowerCase2;
        this.numeric = numeric2;
        this.symbols = symbols2;
        this.nonLetter = nonLetter2;
    }

    private PasswordMetrics(Parcel in) {
        this.quality = 0;
        this.length = 0;
        this.letters = 0;
        this.upperCase = 0;
        this.lowerCase = 0;
        this.numeric = 0;
        this.symbols = 0;
        this.nonLetter = 0;
        this.quality = in.readInt();
        this.length = in.readInt();
        this.letters = in.readInt();
        this.upperCase = in.readInt();
        this.lowerCase = in.readInt();
        this.numeric = in.readInt();
        this.symbols = in.readInt();
        this.nonLetter = in.readInt();
    }

    public boolean isDefault() {
        return this.quality == 0 && this.length == 0 && this.letters == 0 && this.upperCase == 0 && this.lowerCase == 0 && this.numeric == 0 && this.symbols == 0 && this.nonLetter == 0;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.quality);
        dest.writeInt(this.length);
        dest.writeInt(this.letters);
        dest.writeInt(this.upperCase);
        dest.writeInt(this.lowerCase);
        dest.writeInt(this.numeric);
        dest.writeInt(this.symbols);
        dest.writeInt(this.nonLetter);
    }

    public static PasswordMetrics computeForPassword(String password) {
        int lowerCase2 = 0;
        int numeric2 = 0;
        int nonLetter2 = 0;
        int length2 = password.length();
        int quality2 = 0;
        int symbols2 = 0;
        int upperCase2 = 0;
        int letters2 = 0;
        for (int i = 0; i < length2; i++) {
            switch (categoryChar(password.charAt(i))) {
                case 0:
                    letters2++;
                    lowerCase2++;
                    break;
                case 1:
                    letters2++;
                    upperCase2++;
                    break;
                case 2:
                    numeric2++;
                    nonLetter2++;
                    break;
                case 3:
                    symbols2++;
                    nonLetter2++;
                    break;
            }
        }
        String str = password;
        boolean hasNonNumeric = true;
        boolean hasNumeric = numeric2 > 0;
        if (letters2 + symbols2 <= 0) {
            hasNonNumeric = false;
        }
        if (hasNonNumeric && hasNumeric) {
            quality2 = DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC;
        } else if (hasNonNumeric) {
            quality2 = 262144;
        } else if (hasNumeric) {
            if (maxLengthSequence(password) > 3) {
                quality2 = 131072;
            } else {
                quality2 = 196608;
            }
        }
        PasswordMetrics passwordMetrics = new PasswordMetrics(quality2, length2, letters2, upperCase2, lowerCase2, numeric2, symbols2, nonLetter2);
        return passwordMetrics;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof PasswordMetrics)) {
            return false;
        }
        PasswordMetrics o = (PasswordMetrics) other;
        if (this.quality == o.quality && this.length == o.length && this.letters == o.letters && this.upperCase == o.upperCase && this.lowerCase == o.lowerCase && this.numeric == o.numeric && this.symbols == o.symbols && this.nonLetter == o.nonLetter) {
            z = true;
        }
        return z;
    }

    public static int maxLengthSequence(String string) {
        if (string.length() == 0) {
            return 0;
        }
        char previousChar = string.charAt(0);
        int category = categoryChar(previousChar);
        int diff = 0;
        boolean hasDiff = false;
        int maxLength = 0;
        int startSequence = 0;
        for (int current = 1; current < string.length(); current++) {
            char currentChar = string.charAt(current);
            int categoryCurrent = categoryChar(currentChar);
            int currentDiff = currentChar - previousChar;
            if (categoryCurrent != category || Math.abs(currentDiff) > maxDiffCategory(category)) {
                maxLength = Math.max(maxLength, current - startSequence);
                startSequence = current;
                hasDiff = false;
                category = categoryCurrent;
            } else {
                if (hasDiff && currentDiff != diff) {
                    maxLength = Math.max(maxLength, current - startSequence);
                    startSequence = current - 1;
                }
                diff = currentDiff;
                hasDiff = true;
            }
            previousChar = currentChar;
        }
        return Math.max(maxLength, string.length() - startSequence);
    }

    private static int categoryChar(char c) {
        if ('a' <= c && c <= 'z') {
            return 0;
        }
        if ('A' <= c && c <= 'Z') {
            return 1;
        }
        if ('0' > c || c > '9') {
            return 3;
        }
        return 2;
    }

    private static int maxDiffCategory(int category) {
        switch (category) {
            case 0:
            case 1:
                return 1;
            case 2:
                return 10;
            default:
                return 0;
        }
    }
}
