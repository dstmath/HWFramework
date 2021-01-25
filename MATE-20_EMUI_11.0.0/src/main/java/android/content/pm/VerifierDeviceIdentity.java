package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateFormat;
import com.android.internal.annotations.VisibleForTesting;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Random;

public class VerifierDeviceIdentity implements Parcelable {
    public static final Parcelable.Creator<VerifierDeviceIdentity> CREATOR = new Parcelable.Creator<VerifierDeviceIdentity>() {
        /* class android.content.pm.VerifierDeviceIdentity.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VerifierDeviceIdentity createFromParcel(Parcel source) {
            return new VerifierDeviceIdentity(source);
        }

        @Override // android.os.Parcelable.Creator
        public VerifierDeviceIdentity[] newArray(int size) {
            return new VerifierDeviceIdentity[size];
        }
    };
    private static final char[] ENCODE = {DateFormat.CAPITAL_AM_PM, 'B', 'C', 'D', DateFormat.DAY, 'F', 'G', 'H', 'I', 'J', 'K', DateFormat.STANDALONE_MONTH, DateFormat.MONTH, PhoneNumberUtils.WILD, 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7'};
    private static final int GROUP_SIZE = 4;
    private static final int LONG_SIZE = 13;
    private static final char SEPARATOR = '-';
    private final long mIdentity;
    private final String mIdentityString;

    public VerifierDeviceIdentity(long identity) {
        this.mIdentity = identity;
        this.mIdentityString = encodeBase32(identity);
    }

    private VerifierDeviceIdentity(Parcel source) {
        long identity = source.readLong();
        this.mIdentity = identity;
        this.mIdentityString = encodeBase32(identity);
    }

    public static VerifierDeviceIdentity generate() {
        return generate(new SecureRandom());
    }

    @VisibleForTesting
    static VerifierDeviceIdentity generate(Random rng) {
        return new VerifierDeviceIdentity(rng.nextLong());
    }

    private static final String encodeBase32(long input) {
        char[] alphabet = ENCODE;
        char[] encoded = new char[16];
        int index = encoded.length;
        for (int i = 0; i < 13; i++) {
            if (i > 0 && i % 4 == 1) {
                index--;
                encoded[index] = SEPARATOR;
            }
            input >>>= 5;
            index--;
            encoded[index] = alphabet[(int) (31 & input)];
        }
        return String.valueOf(encoded);
    }

    private static final long decodeBase32(byte[] input) throws IllegalArgumentException {
        int value;
        long output = 0;
        int numParsed = 0;
        for (byte b : input) {
            if (65 <= b && b <= 90) {
                value = b - 65;
            } else if (50 <= b && b <= 55) {
                value = b - 24;
            } else if (b == 45) {
                continue;
            } else if (97 <= b && b <= 122) {
                value = b - 97;
            } else if (b == 48) {
                value = 14;
            } else if (b == 49) {
                value = 8;
            } else {
                throw new IllegalArgumentException("base base-32 character: " + ((int) b));
            }
            output = (output << 5) | ((long) value);
            numParsed++;
            if (numParsed == 1) {
                if ((value & 15) != value) {
                    throw new IllegalArgumentException("illegal start character; will overflow");
                }
            } else if (numParsed > 13) {
                throw new IllegalArgumentException("too long; should have 13 characters");
            }
        }
        if (numParsed == 13) {
            return output;
        }
        throw new IllegalArgumentException("too short; should have 13 characters");
    }

    public int hashCode() {
        return (int) this.mIdentity;
    }

    public boolean equals(Object other) {
        if ((other instanceof VerifierDeviceIdentity) && this.mIdentity == ((VerifierDeviceIdentity) other).mIdentity) {
            return true;
        }
        return false;
    }

    public String toString() {
        return this.mIdentityString;
    }

    public static VerifierDeviceIdentity parse(String deviceIdentity) throws IllegalArgumentException {
        try {
            return new VerifierDeviceIdentity(decodeBase32(deviceIdentity.getBytes("US-ASCII")));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("bad base-32 characters in input");
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mIdentity);
    }
}
