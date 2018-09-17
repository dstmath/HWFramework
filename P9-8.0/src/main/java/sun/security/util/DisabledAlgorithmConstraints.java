package sun.security.util;

import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.Key;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
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
    private static final Debug debug = Debug.getInstance("certpath");
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

            static Operator of(String s) {
                if (s.equals("==")) {
                    return EQ;
                }
                if (s.equals("!=")) {
                    return NE;
                }
                if (s.equals("<")) {
                    return LT;
                }
                if (s.equals("<=")) {
                    return LE;
                }
                if (s.equals(">")) {
                    return GT;
                }
                if (s.equals(">=")) {
                    return GE;
                }
                throw new IllegalArgumentException("Error in security property. " + s + " is not a legal Operator");
            }
        }

        /* synthetic */ Constraint(Constraint -this0) {
            this();
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
            for (String constraintEntry : constraintArray) {
                String constraintEntry2;
                if (!(constraintEntry2 == null || constraintEntry2.isEmpty())) {
                    constraintEntry2 = constraintEntry2.trim();
                    if (DisabledAlgorithmConstraints.debug != null) {
                        DisabledAlgorithmConstraints.debug.println("Constraints: " + constraintEntry2);
                    }
                    int space = constraintEntry2.indexOf(32);
                    if (space > 0) {
                        String algorithm = AlgorithmDecomposer.hashName(constraintEntry2.substring(0, space).toUpperCase(Locale.ENGLISH));
                        Constraint c = null;
                        Constraint lastConstraint = null;
                        boolean jdkCALimit = false;
                        for (String entry : constraintEntry2.substring(space + 1).split("&")) {
                            String entry2 = entry2.trim();
                            Matcher matcher = keySizePattern.matcher(entry2);
                            if (matcher.matches()) {
                                if (DisabledAlgorithmConstraints.debug != null) {
                                    DisabledAlgorithmConstraints.debug.println("Constraints set to keySize: " + entry2);
                                }
                                c = new KeySizeConstraint(algorithm, Operator.of(matcher.group(1)), Integer.parseInt(matcher.group(2)));
                            } else if (entry2.equalsIgnoreCase("jdkCA")) {
                                if (DisabledAlgorithmConstraints.debug != null) {
                                    DisabledAlgorithmConstraints.debug.println("Constraints set to jdkCA.");
                                }
                                if (jdkCALimit) {
                                    throw new IllegalArgumentException("Only one jdkCA entry allowed in property. Constraint: " + constraintEntry2);
                                }
                                c = new jdkCAConstraint(algorithm);
                                jdkCALimit = true;
                            }
                            if (lastConstraint == null) {
                                if (!this.constraintsMap.containsKey(algorithm)) {
                                    this.constraintsMap.putIfAbsent(algorithm, new HashSet());
                                }
                                if (c != null) {
                                    ((Set) this.constraintsMap.get(algorithm)).-java_util_stream_Collectors-mthref-4(c);
                                }
                            } else {
                                lastConstraint.nextConstraint = c;
                            }
                            lastConstraint = c;
                        }
                        continue;
                    } else {
                        this.constraintsMap.putIfAbsent(constraintEntry2.toUpperCase(Locale.ENGLISH), new HashSet());
                    }
                }
            }
        }

        private Set<Constraint> getConstraints(String algorithm) {
            return (Set) this.constraintsMap.get(algorithm);
        }

        public boolean permits(Key key) {
            Set<Constraint> set = getConstraints(key.getAlgorithm());
            if (set == null) {
                return true;
            }
            for (Constraint constraint : set) {
                if (!constraint.permits(key)) {
                    if (DisabledAlgorithmConstraints.debug != null) {
                        DisabledAlgorithmConstraints.debug.println("keySizeConstraint: failed key constraint check " + KeyUtil.getKeySize(key));
                    }
                    return false;
                }
            }
            return true;
        }

        public void permits(CertConstraintParameters cp) throws CertPathValidatorException {
            X509Certificate cert = cp.getCertificate();
            if (DisabledAlgorithmConstraints.debug != null) {
                DisabledAlgorithmConstraints.debug.println("Constraints.permits(): " + cert.getSigAlgName());
            }
            Set<String> algorithms = AlgorithmDecomposer.decomposeOneHash(cert.getSigAlgName());
            if (algorithms != null && !algorithms.isEmpty()) {
                algorithms.-java_util_stream_Collectors-mthref-4(cert.getPublicKey().getAlgorithm());
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
        private static final /* synthetic */ int[] -sun-security-util-DisabledAlgorithmConstraints$Constraint$OperatorSwitchesValues = null;
        private int maxSize;
        private int minSize;
        private int prohibitedSize = -1;

        private static /* synthetic */ int[] -getsun-security-util-DisabledAlgorithmConstraints$Constraint$OperatorSwitchesValues() {
            if (-sun-security-util-DisabledAlgorithmConstraints$Constraint$OperatorSwitchesValues != null) {
                return -sun-security-util-DisabledAlgorithmConstraints$Constraint$OperatorSwitchesValues;
            }
            int[] iArr = new int[Operator.values().length];
            try {
                iArr[Operator.EQ.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Operator.GE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Operator.GT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Operator.LE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[Operator.LT.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[Operator.NE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            -sun-security-util-DisabledAlgorithmConstraints$Constraint$OperatorSwitchesValues = iArr;
            return iArr;
        }

        public KeySizeConstraint(String algo, Operator operator, int length) {
            int i = 0;
            super();
            this.algorithm = algo;
            switch (-getsun-security-util-DisabledAlgorithmConstraints$Constraint$OperatorSwitchesValues()[operator.ordinal()]) {
                case 1:
                    this.minSize = 0;
                    this.maxSize = Integer.MAX_VALUE;
                    this.prohibitedSize = length;
                    return;
                case 2:
                    this.minSize = 0;
                    if (length > 1) {
                        i = length - 1;
                    }
                    this.maxSize = i;
                    return;
                case 3:
                    this.minSize = 0;
                    this.maxSize = length;
                    return;
                case 4:
                    this.minSize = length + 1;
                    this.maxSize = Integer.MAX_VALUE;
                    return;
                case 5:
                    this.minSize = length;
                    this.maxSize = Integer.MAX_VALUE;
                    return;
                case 6:
                    this.minSize = length;
                    this.maxSize = length;
                    return;
                default:
                    this.minSize = Integer.MAX_VALUE;
                    this.maxSize = -1;
                    return;
            }
        }

        public void permits(CertConstraintParameters cp) throws CertPathValidatorException {
            if (!permitsImpl(cp.getCertificate().getPublicKey())) {
                if (this.nextConstraint != null) {
                    this.nextConstraint.permits(cp);
                } else {
                    throw new CertPathValidatorException("Algorithm constraints check failed on keysize limits", null, null, -1, BasicReason.ALGORITHM_CONSTRAINED);
                }
            }
        }

        public boolean permits(Key key) {
            if (this.nextConstraint != null && this.nextConstraint.permits(key)) {
                return true;
            }
            if (DisabledAlgorithmConstraints.debug != null) {
                DisabledAlgorithmConstraints.debug.println("KeySizeConstraints.permits(): " + this.algorithm);
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
            if (size < this.minSize || size > this.maxSize) {
                z = false;
            } else if (this.prohibitedSize == size) {
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
                DisabledAlgorithmConstraints.debug.println("jdkCAConstraints.permits(): " + this.algorithm);
            }
            if (!cp.isTrustedMatch()) {
                return;
            }
            if (this.nextConstraint != null) {
                this.nextConstraint.permits(cp);
            } else {
                throw new CertPathValidatorException("Algorithm constraints check failed on certificate anchor limits", null, null, -1, BasicReason.ALGORITHM_CONSTRAINED);
            }
        }
    }

    public DisabledAlgorithmConstraints(String propertyName) {
        this(propertyName, new AlgorithmDecomposer());
    }

    public DisabledAlgorithmConstraints(String propertyName, AlgorithmDecomposer decomposer) {
        super(decomposer);
        this.disabledAlgorithms = AbstractAlgorithmConstraints.getAlgorithms(propertyName);
        this.algorithmConstraints = new Constraints(this.disabledAlgorithms);
    }

    public final boolean permits(Set<CryptoPrimitive> primitives, String algorithm, AlgorithmParameters parameters) {
        if (primitives != null && !primitives.isEmpty()) {
            return AbstractAlgorithmConstraints.checkAlgorithm(this.disabledAlgorithms, algorithm, this.decomposer);
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
        param = param.toLowerCase(Locale.ENGLISH);
        for (String block : this.disabledAlgorithms) {
            if (block.toLowerCase(Locale.ENGLISH).indexOf(param) >= 0) {
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
        X509Certificate cert = cp.getCertificate();
        String algorithm = cert.getSigAlgName();
        if (!permits(primitives, algorithm, null)) {
            throw new CertPathValidatorException("Algorithm constraints check failed on disabled signature algorithm: " + algorithm, null, null, -1, BasicReason.ALGORITHM_CONSTRAINED);
        } else if (permits(primitives, cert.getPublicKey().getAlgorithm(), null)) {
            this.algorithmConstraints.permits(cp);
        } else {
            throw new CertPathValidatorException("Algorithm constraints check failed on disabled public key algorithm: " + algorithm, null, null, -1, BasicReason.ALGORITHM_CONSTRAINED);
        }
    }
}
