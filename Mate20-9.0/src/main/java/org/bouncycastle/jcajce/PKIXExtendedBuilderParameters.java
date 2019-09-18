package org.bouncycastle.jcajce;

import java.security.InvalidParameterException;
import java.security.cert.CertPathParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bouncycastle.jcajce.PKIXExtendedParameters;

public class PKIXExtendedBuilderParameters implements CertPathParameters {
    private final PKIXExtendedParameters baseParameters;
    private final Set<X509Certificate> excludedCerts;
    private final int maxPathLength;

    public static class Builder {
        /* access modifiers changed from: private */
        public final PKIXExtendedParameters baseParameters;
        /* access modifiers changed from: private */
        public Set<X509Certificate> excludedCerts = new HashSet();
        /* access modifiers changed from: private */
        public int maxPathLength = 5;

        public Builder(PKIXBuilderParameters pKIXBuilderParameters) {
            this.baseParameters = new PKIXExtendedParameters.Builder((PKIXParameters) pKIXBuilderParameters).build();
            this.maxPathLength = pKIXBuilderParameters.getMaxPathLength();
        }

        public Builder(PKIXExtendedParameters pKIXExtendedParameters) {
            this.baseParameters = pKIXExtendedParameters;
        }

        public Builder addExcludedCerts(Set<X509Certificate> set) {
            this.excludedCerts.addAll(set);
            return this;
        }

        public PKIXExtendedBuilderParameters build() {
            return new PKIXExtendedBuilderParameters(this);
        }

        public Builder setMaxPathLength(int i) {
            if (i >= -1) {
                this.maxPathLength = i;
                return this;
            }
            throw new InvalidParameterException("The maximum path length parameter can not be less than -1.");
        }
    }

    private PKIXExtendedBuilderParameters(Builder builder) {
        this.baseParameters = builder.baseParameters;
        this.excludedCerts = Collections.unmodifiableSet(builder.excludedCerts);
        this.maxPathLength = builder.maxPathLength;
    }

    public Object clone() {
        return this;
    }

    public PKIXExtendedParameters getBaseParameters() {
        return this.baseParameters;
    }

    public Set getExcludedCerts() {
        return this.excludedCerts;
    }

    public int getMaxPathLength() {
        return this.maxPathLength;
    }
}
