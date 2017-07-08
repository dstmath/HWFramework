package com.android.org.conscrypt;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PinListEntry {
    private final TrustedCertificateStore certStore;
    private final String cn;
    private final boolean enforcing;
    private final Set<String> pinnedFingerprints;

    public String getCommonName() {
        return this.cn;
    }

    public boolean getEnforcing() {
        return this.enforcing;
    }

    public PinListEntry(String entry, TrustedCertificateStore store) throws PinEntryException {
        this.pinnedFingerprints = new HashSet();
        if (entry == null) {
            throw new NullPointerException("entry == null");
        }
        this.certStore = store;
        String[] values = entry.split("[=,|]");
        if (values.length < 3) {
            throw new PinEntryException("Received malformed pin entry");
        }
        this.cn = values[0];
        this.enforcing = enforcementValueFromString(values[1]);
        addPins((String[]) Arrays.copyOfRange(values, 2, values.length));
    }

    private static boolean enforcementValueFromString(String val) throws PinEntryException {
        if (val.equals("true")) {
            return true;
        }
        if (val.equals("false")) {
            return false;
        }
        throw new PinEntryException("Enforcement status is not a valid value");
    }

    public boolean isChainValid(List<X509Certificate> chain) {
        boolean containsUserCert = chainContainsUserCert(chain);
        if (!containsUserCert) {
            for (X509Certificate cert : chain) {
                if (this.pinnedFingerprints.contains(getFingerprint(cert))) {
                    return true;
                }
            }
        }
        logPinFailure(chain, containsUserCert);
        if (!this.enforcing) {
            containsUserCert = true;
        }
        return containsUserCert;
    }

    private static String getFingerprint(X509Certificate cert) {
        try {
            return Hex.bytesToHexString(MessageDigest.getInstance("SHA512").digest(cert.getPublicKey().getEncoded()));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private void addPins(String[] pins) {
        for (String pin : pins) {
            validatePin(pin);
        }
        Collections.addAll(this.pinnedFingerprints, pins);
    }

    private static void validatePin(String pin) {
        if (pin.length() != NativeConstants.SSL_MODE_HANDSHAKE_CUTTHROUGH) {
            throw new IllegalArgumentException("Pin is not a valid length");
        }
        try {
            BigInteger bigInteger = new BigInteger(pin, 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Pin is not a valid hex string", e);
        }
    }

    private boolean chainContainsUserCert(List<X509Certificate> chain) {
        if (this.certStore == null) {
            return false;
        }
        for (X509Certificate cert : chain) {
            if (this.certStore.isUserAddedCertificate(cert)) {
                return true;
            }
        }
        return false;
    }

    private void logPinFailure(List<X509Certificate> chain, boolean containsUserCert) {
        PinFailureLogger.log(this.cn, containsUserCert, this.enforcing, chain);
    }
}
