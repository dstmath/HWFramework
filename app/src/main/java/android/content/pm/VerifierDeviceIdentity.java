package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.renderscript.ScriptIntrinsicBLAS;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Random;

public class VerifierDeviceIdentity implements Parcelable {
    public static final Creator<VerifierDeviceIdentity> CREATOR = null;
    private static final char[] ENCODE = null;
    private static final int GROUP_SIZE = 4;
    private static final int LONG_SIZE = 13;
    private static final char SEPARATOR = '-';
    private final long mIdentity;
    private final String mIdentityString;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.VerifierDeviceIdentity.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.VerifierDeviceIdentity.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.VerifierDeviceIdentity.<clinit>():void");
    }

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

    static VerifierDeviceIdentity generate(Random rng) {
        return new VerifierDeviceIdentity(rng.nextLong());
    }

    private static final String encodeBase32(long input) {
        char[] alphabet = ENCODE;
        char[] encoded = new char[16];
        int index = encoded.length;
        int i = 0;
        while (i < LONG_SIZE) {
            if (i > 0 && i % GROUP_SIZE == 1) {
                index--;
                encoded[index] = SEPARATOR;
            }
            input >>>= 5;
            index--;
            encoded[index] = alphabet[(int) (31 & input)];
            i++;
        }
        return String.valueOf(encoded);
    }

    private static final long decodeBase32(byte[] input) throws IllegalArgumentException {
        long output = 0;
        int numParsed = 0;
        for (int group : input) {
            int value;
            if (65 <= group && group <= 90) {
                value = group - 65;
            } else if (50 <= group && group <= 55) {
                value = group - 24;
            } else if (group == 45) {
                continue;
            } else if (97 <= group && group <= ScriptIntrinsicBLAS.LOWER) {
                value = group - 97;
            } else if (group == 48) {
                value = 14;
            } else if (group == 49) {
                value = 8;
            } else {
                throw new IllegalArgumentException("base base-32 character: " + group);
            }
            output = (output << 5) | ((long) value);
            numParsed++;
            if (numParsed == 1) {
                if ((value & 15) != value) {
                    throw new IllegalArgumentException("illegal start character; will overflow");
                }
            } else if (numParsed > LONG_SIZE) {
                throw new IllegalArgumentException("too long; should have 13 characters");
            }
        }
        if (numParsed == LONG_SIZE) {
            return output;
        }
        throw new IllegalArgumentException("too short; should have 13 characters");
    }

    public int hashCode() {
        return (int) this.mIdentity;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof VerifierDeviceIdentity)) {
            return false;
        }
        if (this.mIdentity == ((VerifierDeviceIdentity) other).mIdentity) {
            z = true;
        }
        return z;
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mIdentity);
    }
}
