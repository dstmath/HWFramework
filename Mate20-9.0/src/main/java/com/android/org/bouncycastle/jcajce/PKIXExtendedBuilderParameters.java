package com.android.org.bouncycastle.jcajce;

import com.android.org.bouncycastle.jcajce.PKIXExtendedParameters;
import java.security.InvalidParameterException;
import java.security.cert.CertPathParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

        public Builder(PKIXBuilderParameters baseParameters2) {
            this.baseParameters = new PKIXExtendedParameters.Builder((PKIXParameters) baseParameters2).build();
            this.maxPathLength = baseParameters2.getMaxPathLength();
        }

        public Builder(PKIXExtendedParameters baseParameters2) {
            this.baseParameters = baseParameters2;
        }

        public Builder addExcludedCerts(Set<X509Certificate> excludedCerts2) {
            this.excludedCerts.addAll(excludedCerts2);
            return this;
        }

        public Builder setMaxPathLength(int maxPathLength2) {
            if (maxPathLength2 >= -1) {
                this.maxPathLength = maxPathLength2;
                return this;
            }
            throw new InvalidParameterException("The maximum path length parameter can not be less than -1.");
        }

        public PKIXExtendedBuilderParameters build() {
            return new PKIXExtendedBuilderParameters(this);
        }
    }

    private PKIXExtendedBuilderParameters(Builder builder) {
        this.baseParameters = builder.baseParameters;
        this.excludedCerts = Collections.unmodifiableSet(builder.excludedCerts);
        this.maxPathLength = builder.maxPathLength;
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

    public Object clone() {
        return this;
    }
}
