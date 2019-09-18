package sun.security.util;

import java.security.AccessController;
import java.security.AlgorithmConstraints;
import java.security.PrivilegedAction;
import java.security.Security;
import java.util.Set;

public abstract class AbstractAlgorithmConstraints implements AlgorithmConstraints {
    protected final AlgorithmDecomposer decomposer;

    protected AbstractAlgorithmConstraints(AlgorithmDecomposer decomposer2) {
        this.decomposer = decomposer2;
    }

    static String[] getAlgorithms(final String propertyName) {
        String property = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty(String.this);
            }
        });
        String[] algorithmsInProperty = null;
        if (property != null && !property.isEmpty()) {
            if (property.length() >= 2 && property.charAt(0) == '\"' && property.charAt(property.length() - 1) == '\"') {
                property = property.substring(1, property.length() - 1);
            }
            algorithmsInProperty = property.split(",");
            for (int i = 0; i < algorithmsInProperty.length; i++) {
                algorithmsInProperty[i] = algorithmsInProperty[i].trim();
            }
        }
        if (algorithmsInProperty == null) {
            return new String[0];
        }
        return algorithmsInProperty;
    }

    static boolean checkAlgorithm(String[] algorithms, String algorithm, AlgorithmDecomposer decomposer2) {
        if (algorithm == null || algorithm.length() == 0) {
            throw new IllegalArgumentException("No algorithm name specified");
        }
        Set<String> elements = null;
        for (String item : algorithms) {
            if (item != null && !item.isEmpty()) {
                if (item.equalsIgnoreCase(algorithm)) {
                    return false;
                }
                if (elements == null) {
                    elements = decomposer2.decompose(algorithm);
                }
                for (String element : elements) {
                    if (item.equalsIgnoreCase(element)) {
                        return false;
                    }
                }
                continue;
            }
        }
        return true;
    }
}
