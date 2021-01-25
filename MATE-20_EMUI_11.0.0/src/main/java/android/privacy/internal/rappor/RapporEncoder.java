package android.privacy.internal.rappor;

import android.privacy.DifferentialPrivacyEncoder;
import android.security.keystore.KeyProperties;
import com.android.internal.midi.MidiConstants;
import com.google.android.rappor.Encoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class RapporEncoder implements DifferentialPrivacyEncoder {
    private static final byte[] INSECURE_SECRET = {-41, 104, -103, -109, -108, 19, 83, 84, -2, MidiConstants.STATUS_CHANNEL_PRESSURE, 126, 84, -2, MidiConstants.STATUS_CHANNEL_PRESSURE, 126, 84, -41, 104, -103, -109, -108, 19, 83, 84, -2, MidiConstants.STATUS_CHANNEL_PRESSURE, 126, 84, -2, MidiConstants.STATUS_CHANNEL_PRESSURE, 126, 84, -41, 104, -103, -109, -108, 19, 83, 84, -2, MidiConstants.STATUS_CHANNEL_PRESSURE, 126, 84, -2, MidiConstants.STATUS_CHANNEL_PRESSURE, 126, 84};
    private static final SecureRandom sSecureRandom = new SecureRandom();
    private final RapporConfig mConfig;
    private final Encoder mEncoder;
    private final boolean mIsSecure;

    private RapporEncoder(RapporConfig config, boolean secureEncoder, byte[] userSecret) {
        byte[] userSecret2;
        Random random;
        this.mConfig = config;
        this.mIsSecure = secureEncoder;
        if (secureEncoder) {
            random = sSecureRandom;
            userSecret2 = userSecret;
        } else {
            random = new Random(getInsecureSeed(config.mEncoderId));
            userSecret2 = INSECURE_SECRET;
        }
        this.mEncoder = new Encoder(random, (MessageDigest) null, (MessageDigest) null, userSecret2, config.mEncoderId, config.mNumBits, config.mProbabilityF, config.mProbabilityP, config.mProbabilityQ, config.mNumCohorts, config.mNumBloomHashes);
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

    @Override // android.privacy.DifferentialPrivacyEncoder
    public byte[] encodeString(String original) {
        return this.mEncoder.encodeString(original);
    }

    @Override // android.privacy.DifferentialPrivacyEncoder
    public byte[] encodeBoolean(boolean original) {
        return this.mEncoder.encodeBoolean(original);
    }

    @Override // android.privacy.DifferentialPrivacyEncoder
    public byte[] encodeBits(byte[] bits) {
        return this.mEncoder.encodeBits(bits);
    }

    @Override // android.privacy.DifferentialPrivacyEncoder
    public RapporConfig getConfig() {
        return this.mConfig;
    }

    @Override // android.privacy.DifferentialPrivacyEncoder
    public boolean isInsecureEncoderForTest() {
        return !this.mIsSecure;
    }
}
