package java.security.interfaces;

import java.security.InvalidParameterException;
import java.security.SecureRandom;

public interface DSAKeyPairGenerator {
    void initialize(int i, boolean z, SecureRandom secureRandom) throws InvalidParameterException;

    void initialize(DSAParams dSAParams, SecureRandom secureRandom) throws InvalidParameterException;
}
