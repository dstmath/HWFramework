package org.bouncycastle.pqc.jcajce.spec;

import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.pqc.crypto.qtesla.QTESLASecurityCategory;

public class QTESLAParameterSpec implements AlgorithmParameterSpec {
    public static final String PROVABLY_SECURE_I = QTESLASecurityCategory.getName(5);
    public static final String PROVABLY_SECURE_III = QTESLASecurityCategory.getName(6);
    private String securityCategory;

    public QTESLAParameterSpec(String str) {
        this.securityCategory = str;
    }

    public String getSecurityCategory() {
        return this.securityCategory;
    }
}
