package android.app.admin;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PasswordMetrics implements Parcelable {
    private static final int CHAR_DIGIT = 2;
    private static final int CHAR_LOWER_CASE = 0;
    private static final int CHAR_SYMBOL = 3;
    private static final int CHAR_UPPER_CASE = 1;
    public static final Creator<PasswordMetrics> CREATOR = new Creator<PasswordMetrics>() {
        public PasswordMetrics createFromParcel(Parcel in) {
            return new PasswordMetrics(in, null);
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

    public PasswordMetrics(int quality, int length) {
        this.quality = 0;
        this.length = 0;
        this.letters = 0;
        this.upperCase = 0;
        this.lowerCase = 0;
        this.numeric = 0;
        this.symbols = 0;
        this.nonLetter = 0;
        this.quality = quality;
        this.length = length;
    }

    public PasswordMetrics(int quality, int length, int letters, int upperCase, int lowerCase, int numeric, int symbols, int nonLetter) {
        this(quality, length);
        this.letters = letters;
        this.upperCase = upperCase;
        this.lowerCase = lowerCase;
        this.numeric = numeric;
        this.symbols = symbols;
        this.nonLetter = nonLetter;
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
        if (this.quality == 0 && this.length == 0 && this.letters == 0 && this.upperCase == 0 && this.lowerCase == 0 && this.numeric == 0 && this.symbols == 0 && this.nonLetter == 0) {
            return true;
        }
        return false;
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
        int quality;
        int letters = 0;
        int upperCase = 0;
        int lowerCase = 0;
        int numeric = 0;
        int symbols = 0;
        int nonLetter = 0;
        int length = password.length();
        for (int i = 0; i < length; i++) {
            switch (categoryChar(password.charAt(i))) {
                case 0:
                    letters++;
                    lowerCase++;
                    break;
                case 1:
                    letters++;
                    upperCase++;
                    break;
                case 2:
                    numeric++;
                    nonLetter++;
                    break;
                case 3:
                    symbols++;
                    nonLetter++;
                    break;
                default:
                    break;
            }
        }
        boolean hasNumeric = numeric > 0;
        boolean hasNonNumeric = letters + symbols > 0;
        if (hasNonNumeric && hasNumeric) {
            quality = DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC;
        } else if (hasNonNumeric) {
            quality = 262144;
        } else if (!hasNumeric) {
            quality = 0;
        } else if (maxLengthSequence(password) > 3) {
            quality = 131072;
        } else {
            quality = 196608;
        }
        return new PasswordMetrics(quality, length, letters, upperCase, lowerCase, numeric, symbols, nonLetter);
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
