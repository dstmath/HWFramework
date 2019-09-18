package java.security;

import java.util.Set;

public interface AlgorithmConstraints {
    boolean permits(Set<CryptoPrimitive> set, String str, AlgorithmParameters algorithmParameters);

    boolean permits(Set<CryptoPrimitive> set, String str, Key key, AlgorithmParameters algorithmParameters);

    boolean permits(Set<CryptoPrimitive> set, Key key);
}
