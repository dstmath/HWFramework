package android.privacy.internal.rappor;

import android.privacy.DifferentialPrivacyEncoder;
import android.security.keystore.KeyProperties;
import com.google.android.rappor.Encoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class RapporEncoder implements DifferentialPrivacyEncoder {
    private static final byte[] INSECURE_SECRET = {-41, 104, -103, -109, -108, 19, 83, 84, -2, -48, 126, 84, -2, -48, 126, 84, -41, 104, -103, -109, -108, 19, 83, 84, -2, -48, 126, 84, -2, -48, 126, 84, -41, 104, -103, -109, -108, 19, 83, 84, -2, -48, 126, 84, -2, -48, 126, 84};
    private static final SecureRandom sSecureRandom = new SecureRandom();
    private final RapporConfig mConfig;
    private final Encoder mEncoder;
    private final boolean mIsSecure;

    private RapporEncoder(RapporConfig config, boolean secureEncoder, byte[] userSecret) {
        byte[] userSecret2;
        Random random;
        RapporConfig rapporConfig = config;
        boolean z = secureEncoder;
        this.mConfig = rapporConfig;
        this.mIsSecure = z;
        if (z) {
            random = sSecureRandom;
            userSecret2 = userSecret;
        } else {
            random = new Random(getInsecureSeed(rapporConfig.mEncoderId));
            userSecret2 = INSECURE_SECRET;
        }
        String str = rapporConfig.mEncoderId;
        int i = rapporConfig.mNumBits;
        double d = rapporConfig.mProbabilityF;
        double d2 = rapporConfig.mProbabilityP;
        double d3 = rapporConfig.mProbabilityQ;
        double d4 = d3;
        Encoder encoder = r5;
        Encoder encoder2 = new Encoder(random, null, null, userSecret2, str, i, d, d2, d4, rapporConfig.mNumCohorts, rapporConfig.mNumBloomHashes);
        this.mEncoder = encoder;
    }

    private long getInsecureSeed(String input) {
        try {
            return ByteBuffer.wrap(MessageDigest.getInstance(KeyProperties.DIGEST_SHA256).digest(input.getBytes(StandardCharsets.UTF_8))).getLong();
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("Unable generate insecure seed");
        }
    }

    public static RapporEncoder createEncoder(RapporConfig config, byte[] userSecret) {
        return new RapporEncoder(config, true, userSecret);
    }

    public static RapporEncoder createInsecureEncoderForTest(RapporConfig config) {
        return new RapporEncoder(config, false, null);
    }

    public byte[] encodeString(String original) {
        return this.mEncoder.encodeString(original);
    }

    public byte[] encodeBoolean(boolean original) {
        return this.mEncoder.encodeBoolean(original);
    }

    public byte[] encodeBits(byte[] bits) {
        return this.mEncoder.encodeBits(bits);
    }

    public RapporConfig getConfig() {
        return this.mConfig;
    }

    public boolean isInsecureEncoderForTest() {
        return !this.mIsSecure;
    }
}
