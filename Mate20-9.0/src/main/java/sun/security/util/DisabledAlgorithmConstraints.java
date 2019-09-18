package sun.security.util;

import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.Key;
import java.security.cert.CertPathValidatorException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisabledAlgorithmConstraints extends AbstractAlgorithmConstraints {
    public static final String PROPERTY_CERTPATH_DISABLED_ALGS = "jdk.certpath.disabledAlgorithms";
    public static final String PROPERTY_JAR_DISABLED_ALGS = "jdk.jar.disabledAlgorithms";
    public static final String PROPERTY_TLS_DISABLED_ALGS = "jdk.tls.disabledAlgorithms";
    /* access modifiers changed from: private */
    public static final Debug debug = Debug.getInstance("certpath");
    private final Constraints algorithmConstraints;
    private final String[] disabledAlgorithms;

    private static abstract class Constraint {
        String algorithm;
        Constraint nextConstraint;

        enum Operator {
            EQ,
            NE,
            LT,
            LE,
            GT,
            GE;

            /* JADX WARNING: Removed duplicated region for block: B:32:0x005d  */
            /* JADX WARNING: Removed duplicated region for block: B:34:0x0079  */
            /* JADX WARNING: Removed duplicated region for block: B:36:0x007c  */
            /* JADX WARNING: Removed duplicated region for block: B:38:0x007f  */
            /* JADX WARNING: Removed duplicated region for block: B:40:0x0082  */
            /* JADX WARNING: Removed duplicated region for block: B:42:0x0085  */
            /* JADX WARNING: Removed duplicated region for block: B:44:0x0088  */
            static Operator of(String s) {
                char c;
                int hashCode = s.hashCode();
                if (hashCode == 60) {
                    if (s.equals("<")) {
                        c = 2;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            case 4:
                                break;
                            case 5:
                                break;
                        }
                    }
                } else if (hashCode == 62) {
                    if (s.equals(">")) {
                        c = 4;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            case 4:
                                break;
                            case 5:
                                break;
                        }
                    }
                } else if (hashCode == 1084) {
                    if (s.equals("!=")) {
                        c = 1;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            case 4:
                                break;
                            case 5:
                                break;
                        }
                    }
                } else if (hashCode == 1921) {
                    if (s.equals("<=")) {
                        c = 3;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            case 4:
                                break;
                            case 5:
                                break;
                        }
                    }
                } else if (hashCode == 1952) {
                    if (s.equals("==")) {
                        c = 0;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            case 4:
                                break;
                            case 5:
                                break;
                        }
                    }
                } else if (hashCode == 1983 && s.equals(">=")) {
                    c = 5;
                    switch (c) {
                        case 0:
                            return EQ;
                        case 1:
                            return NE;
                        case 2:
                            return LT;
                        case 3:
                            return LE;
                        case 4:
                            return GT;
                        case 5:
                            return GE;
                        default:
                            throw new IllegalArgumentException("Error in security property. " + s + " is not a legal Operator");
                    }
                }
                c = 65535;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                }
            }
        }

        public abstract void permits(CertConstraintParameters certConstraintParameters) throws CertPathValidatorException;

        private Constraint() {
            this.nextConstraint = null;
        }

        public boolean permits(Key key) {
            return true;
        }
    }

    private static class Constraints {
        private static final Pattern keySizePattern = Pattern.compile("keySize\\s*(<=|<|==|!=|>|>=)\\s*(\\d+)");
        private Map<String, Set<Constraint>> constraintsMap = new HashMap();

        public Constraints(String[] constraintArray) {
            int i;
            int space;
            int i2;
            String[] strArr = constraintArray;
            int length = strArr.length;
            int i3 = 0;
            int i4 = 0;
            while (i4 < length) {
                String constraintEntry = strArr[i4];
                if (constraintEntry != null && !constraintEntry.isEmpty()) {
                    String constraintEntry2 = constraintEntry.trim();
                    if (DisabledAlgorithmConstraints.debug != null) {
                        DisabledAlgorithmConstraints.debug.println("Constraints: " + constraintEntry2);
                    }
                    int space2 = constraintEntry2.indexOf(32);
                    if (space2 > 0) {
                        String algorithm = AlgorithmDecomposer.hashName(constraintEntry2.substring(i3, space2).toUpperCase(Locale.ENGLISH));
                        String[] split = constraintEntry2.substring(space2 + 1).split("&");
                        int length2 = split.length;
                        boolean jdkCALimit = false;
                        Constraint lastConstraint = null;
                        Constraint c = null;
                        int i5 = i3;
                        while (i5 < length2) {
                            String entry = split[i5].trim();
                            Matcher matcher = keySizePattern.matcher(entry);
                            if (matcher.matches()) {
                                if (DisabledAlgorithmConstraints.debug != null) {
                                    Debug access$000 = DisabledAlgorithmConstraints.debug;
                                    i2 = length;
                                    StringBuilder sb = new StringBuilder();
                                    space = space2;
                                    sb.append("Constraints set to keySize: ");
                                    sb.append(entry);
                                    access$000.println(sb.toString());
                                } else {
                                    i2 = length;
                                    space = space2;
                                }
                                c = new KeySizeConstraint(algorithm, Constraint.Operator.of(matcher.group(1)), Integer.parseInt(matcher.group(2)));
                            } else {
                                i2 = length;
                                space = space2;
                                if (entry.equalsIgnoreCase("jdkCA")) {
                                    if (DisabledAlgorithmConstraints.debug != null) {
                                        DisabledAlgorithmConstraints.debug.println("Constraints set to jdkCA.");
                                    }
                                    if (!jdkCALimit) {
                                        c = new jdkCAConstraint(algorithm);
                                        jdkCALimit = true;
                                    } else {
                                        throw new IllegalArgumentException("Only one jdkCA entry allowed in property. Constraint: " + constraintEntry2);
                                    }
                                }
                            }
                            if (lastConstraint == null) {
                                if (!this.constraintsMap.containsKey(algorithm)) {
                                    this.constraintsMap.putIfAbsent(algorithm, new HashSet());
                                }
                                if (c != null) {
                                    this.constraintsMap.get(algorithm).add(c);
                                }
                            } else {
                                lastConstraint.nextConstraint = c;
                            }
                            lastConstraint = c;
                            i5++;
                            length = i2;
                            space2 = space;
                            String[] strArr2 = constraintArray;
                        }
                    } else {
                        i = length;
                        int i6 = space2;
                        this.constraintsMap.putIfAbsent(constraintEntry2.toUpperCase(Locale.ENGLISH), new HashSet());
                        i4++;
                        length = i;
                        strArr = constraintArray;
                        i3 = 0;
                    }
                }
                i = length;
                i4++;
                length = i;
                strArr = constraintArray;
                i3 = 0;
            }
        }

        private Set<Constraint> getConstraints(String algorithm) {
            return this.constraintsMap.get(algorithm);
        }

        public boolean permits(Key key) {
            Set<Constraint> set = getConstraints(key.getAlgorithm());
            if (set == null) {
                return true;
            }
            for (Constraint constraint : set) {
                if (!constraint.permits(key)) {
                    if (DisabledAlgorithmConstraints.debug != null) {
                        Debug access$000 = DisabledAlgorithmConstraints.debug;
                        access$000.println("keySizeConstraint: failed key constraint check " + KeyUtil.getKeySize(key));
                    }
                    return false;
                }
            }
            return true;
        }

        public void permits(CertConstraintParameters cp) throws CertPathValidatorException {
            X509Certificate cert = cp.getCertificate();
            if (DisabledAlgorithmConstraints.debug != null) {
                Debug access$000 = DisabledAlgorithmConstraints.debug;
                access$000.println("Constraints.permits(): " + cert.getSigAlgName());
            }
            Set<String> algorithms = AlgorithmDecomposer.decomposeOneHash(cert.getSigAlgName());
            if (algorithms != null && !algorithms.isEmpty()) {
                algorithms.add(cert.getPublicKey().getAlgorithm());
                for (String algorithm : algorithms) {
                    Set<Constraint> set = getConstraints(algorithm);
                    if (set != null) {
                        for (Constraint constraint : set) {
                            constraint.permits(cp);
                        }
                    }
                }
            }
        }
    }

    private static class KeySizeConstraint extends Constraint {
        private int maxSize;
        private int minSize;
        private int prohibitedSize = -1;

        public KeySizeConstraint(String algo, Constraint.Operator operator, int length) {
            super();
            this.algorithm = algo;
            int i = 0;
            switch (operator) {
                case EQ:
                    this.minSize = 0;
                    this.maxSize = Integer.MAX_VALUE;
                    this.prohibitedSize = length;
                    return;
                case NE:
                    this.minSize = length;
                    this.maxSize = length;
                    return;
                case LT:
                    this.minSize = length;
                    this.maxSize = Integer.MAX_VALUE;
                    return;
                case LE:
                    this.minSize = length + 1;
                    this.maxSize = Integer.MAX_VALUE;
                    return;
                case GT:
                    this.minSize = 0;
                    this.maxSize = length;
                    return;
                case GE:
                    this.minSize = 0;
                    this.maxSize = length > 1 ? length - 1 : i;
                    return;
                default:
                    this.minSize = Integer.MAX_VALUE;
                    this.maxSize = -1;
                    return;
            }
        }

        public void permits(CertConstraintParameters cp) throws CertPathValidatorException {
            if (permitsImpl(cp.getCertificate().getPublicKey())) {
                return;
            }
            if (this.nextConstraint != null) {
                this.nextConstraint.permits(cp);
            } else {
                CertPathValidatorException certPathValidatorException = new CertPathValidatorException("Algorithm constraints check failed on keysize limits", null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
                throw certPathValidatorException;
            }
        }

        public boolean permits(Key key) {
            if (this.nextConstraint != null && this.nextConstraint.permits(key)) {
                return true;
            }
            if (DisabledAlgorithmConstraints.debug != null) {
                Debug access$000 = DisabledAlgorithmConstraints.debug;
                access$000.println("KeySizeConstraints.permits(): " + this.algorithm);
            }
            return permitsImpl(key);
        }

        private boolean permitsImpl(Key key) {
            boolean z = true;
            if (this.algorithm.compareToIgnoreCase(key.getAlgorithm()) != 0) {
                return true;
            }
            int size = KeyUtil.getKeySize(key);
            if (size == 0) {
                return false;
            }
            if (size <= 0) {
                return true;
            }
            if (size < this.minSize || size > this.maxSize || this.prohibitedSize == size) {
                z = false;
            }
            return z;
        }
    }

    private static class jdkCAConstraint extends Constraint {
        jdkCAConstraint(String algo) {
            super();
            this.algorithm = algo;
        }

        public void permits(CertConstraintParameters cp) throws CertPathValidatorException {
            if (DisabledAlgorithmConstraints.debug != null) {
                Debug access$000 = DisabledAlgorithmConstraints.debug;
                access$000.println("jdkCAConstraints.permits(): " + this.algorithm);
            }
            if (!cp.isTrustedMatch()) {
                return;
            }
            if (this.nextConstraint != null) {
                this.nextConstraint.permits(cp);
            } else {
                CertPathValidatorException certPathValidatorException = new CertPathValidatorException("Algorithm constraints check failed on certificate anchor limits", null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
                throw certPathValidatorException;
            }
        }
    }

    public DisabledAlgorithmConstraints(String propertyName) {
        this(propertyName, new AlgorithmDecomposer());
    }

    public DisabledAlgorithmConstraints(String propertyName, AlgorithmDecomposer decomposer) {
        super(decomposer);
        this.disabledAlgorithms = getAlgorithms(propertyName);
        this.algorithmConstraints = new Constraints(this.disabledAlgorithms);
    }

    public final boolean permits(Set<CryptoPrimitive> primitives, String algorithm, AlgorithmParameters parameters) {
        if (primitives != null && !primitives.isEmpty()) {
            return checkAlgorithm(this.disabledAlgorithms, algorithm, this.decomposer);
        }
        throw new IllegalArgumentException("No cryptographic primitive specified");
    }

    public final boolean permits(Set<CryptoPrimitive> primitives, Key key) {
        return checkConstraints(primitives, "", key, null);
    }

    public final boolean permits(Set<CryptoPrimitive> primitives, String algorithm, Key key, AlgorithmParameters parameters) {
        if (algorithm != null && algorithm.length() != 0) {
            return checkConstraints(primitives, algorithm, key, parameters);
        }
        throw new IllegalArgumentException("No algorithm name specified");
    }

    public final void permits(Set<CryptoPrimitive> primitives, CertConstraintParameters cp) throws CertPathValidatorException {
        checkConstraints(primitives, cp);
    }

    public final void permits(Set<CryptoPrimitive> primitives, X509Certificate cert) throws CertPathValidatorException {
        checkConstraints(primitives, new CertConstraintParameters(cert));
    }

    public boolean checkProperty(String param) {
        String param2 = param.toLowerCase(Locale.ENGLISH);
        for (String block : this.disabledAlgorithms) {
            if (block.toLowerCase(Locale.ENGLISH).indexOf(param2) >= 0) {
                return true;
            }
        }
        return false;
    }

    private boolean checkConstraints(Set<CryptoPrimitive> primitives, String algorithm, Key key, AlgorithmParameters parameters) {
        if (key == null) {
            throw new IllegalArgumentException("The key cannot be null");
        } else if ((algorithm == null || algorithm.length() == 0 || permits(primitives, algorithm, parameters)) && permits(primitives, key.getAlgorithm(), null)) {
            return this.algorithmConstraints.permits(key);
        } else {
            return false;
        }
    }

    private void checkConstraints(Set<CryptoPrimitive> primitives, CertConstraintParameters cp) throws CertPathValidatorException {
        Set<CryptoPrimitive> set = primitives;
        X509Certificate cert = cp.getCertificate();
        String algorithm = cert.getSigAlgName();
        if (!permits(set, algorithm, null)) {
            CertConstraintParameters certConstraintParameters = cp;
            CertPathValidatorException certPathValidatorException = new CertPathValidatorException("Algorithm constraints check failed on disabled signature algorithm: " + algorithm, null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            throw certPathValidatorException;
        } else if (permits(set, cert.getPublicKey().getAlgorithm(), null)) {
            this.algorithmConstraints.permits(cp);
        } else {
            CertConstraintParameters certConstraintParameters2 = cp;
            CertPathValidatorException certPathValidatorException2 = new CertPathValidatorException("Algorithm constraints check failed on disabled public key algorithm: " + algorithm, null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            throw certPathValidatorException2;
        }
    }
}
