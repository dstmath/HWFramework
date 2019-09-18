package org.bouncycastle.jce;

import java.security.cert.CertStoreParameters;
import java.security.cert.LDAPCertStoreParameters;
import org.bouncycastle.x509.X509StoreParameters;

public class X509LDAPCertStoreParameters implements X509StoreParameters, CertStoreParameters {
    private String aACertificateAttribute;
    private String aACertificateSubjectAttributeName;
    private String attributeAuthorityRevocationListAttribute;
    private String attributeAuthorityRevocationListIssuerAttributeName;
    private String attributeCertificateAttributeAttribute;
    private String attributeCertificateAttributeSubjectAttributeName;
    private String attributeCertificateRevocationListAttribute;
    private String attributeCertificateRevocationListIssuerAttributeName;
    private String attributeDescriptorCertificateAttribute;
    private String attributeDescriptorCertificateSubjectAttributeName;
    private String authorityRevocationListAttribute;
    private String authorityRevocationListIssuerAttributeName;
    private String baseDN;
    private String cACertificateAttribute;
    private String cACertificateSubjectAttributeName;
    private String certificateRevocationListAttribute;
    private String certificateRevocationListIssuerAttributeName;
    private String crossCertificateAttribute;
    private String crossCertificateSubjectAttributeName;
    private String deltaRevocationListAttribute;
    private String deltaRevocationListIssuerAttributeName;
    private String ldapAACertificateAttributeName;
    private String ldapAttributeAuthorityRevocationListAttributeName;
    private String ldapAttributeCertificateAttributeAttributeName;
    private String ldapAttributeCertificateRevocationListAttributeName;
    private String ldapAttributeDescriptorCertificateAttributeName;
    private String ldapAuthorityRevocationListAttributeName;
    private String ldapCACertificateAttributeName;
    private String ldapCertificateRevocationListAttributeName;
    private String ldapCrossCertificateAttributeName;
    private String ldapDeltaRevocationListAttributeName;
    private String ldapURL;
    private String ldapUserCertificateAttributeName;
    private String searchForSerialNumberIn;
    private String userCertificateAttribute;
    private String userCertificateSubjectAttributeName;

    public static class Builder {
        /* access modifiers changed from: private */
        public String aACertificateAttribute;
        /* access modifiers changed from: private */
        public String aACertificateSubjectAttributeName;
        /* access modifiers changed from: private */
        public String attributeAuthorityRevocationListAttribute;
        /* access modifiers changed from: private */
        public String attributeAuthorityRevocationListIssuerAttributeName;
        /* access modifiers changed from: private */
        public String attributeCertificateAttributeAttribute;
        /* access modifiers changed from: private */
        public String attributeCertificateAttributeSubjectAttributeName;
        /* access modifiers changed from: private */
        public String attributeCertificateRevocationListAttribute;
        /* access modifiers changed from: private */
        public String attributeCertificateRevocationListIssuerAttributeName;
        /* access modifiers changed from: private */
        public String attributeDescriptorCertificateAttribute;
        /* access modifiers changed from: private */
        public String attributeDescriptorCertificateSubjectAttributeName;
        /* access modifiers changed from: private */
        public String authorityRevocationListAttribute;
        /* access modifiers changed from: private */
        public String authorityRevocationListIssuerAttributeName;
        /* access modifiers changed from: private */
        public String baseDN;
        /* access modifiers changed from: private */
        public String cACertificateAttribute;
        /* access modifiers changed from: private */
        public String cACertificateSubjectAttributeName;
        /* access modifiers changed from: private */
        public String certificateRevocationListAttribute;
        /* access modifiers changed from: private */
        public String certificateRevocationListIssuerAttributeName;
        /* access modifiers changed from: private */
        public String crossCertificateAttribute;
        /* access modifiers changed from: private */
        public String crossCertificateSubjectAttributeName;
        /* access modifiers changed from: private */
        public String deltaRevocationListAttribute;
        /* access modifiers changed from: private */
        public String deltaRevocationListIssuerAttributeName;
        /* access modifiers changed from: private */
        public String ldapAACertificateAttributeName;
        /* access modifiers changed from: private */
        public String ldapAttributeAuthorityRevocationListAttributeName;
        /* access modifiers changed from: private */
        public String ldapAttributeCertificateAttributeAttributeName;
        /* access modifiers changed from: private */
        public String ldapAttributeCertificateRevocationListAttributeName;
        /* access modifiers changed from: private */
        public String ldapAttributeDescriptorCertificateAttributeName;
        /* access modifiers changed from: private */
        public String ldapAuthorityRevocationListAttributeName;
        /* access modifiers changed from: private */
        public String ldapCACertificateAttributeName;
        /* access modifiers changed from: private */
        public String ldapCertificateRevocationListAttributeName;
        /* access modifiers changed from: private */
        public String ldapCrossCertificateAttributeName;
        /* access modifiers changed from: private */
        public String ldapDeltaRevocationListAttributeName;
        /* access modifiers changed from: private */
        public String ldapURL;
        /* access modifiers changed from: private */
        public String ldapUserCertificateAttributeName;
        /* access modifiers changed from: private */
        public String searchForSerialNumberIn;
        /* access modifiers changed from: private */
        public String userCertificateAttribute;
        /* access modifiers changed from: private */
        public String userCertificateSubjectAttributeName;

        public Builder() {
            this("ldap://localhost:389", "");
        }

        public Builder(String str, String str2) {
            this.ldapURL = str;
            if (str2 == null) {
                this.baseDN = "";
            } else {
                this.baseDN = str2;
            }
            this.userCertificateAttribute = "userCertificate";
            this.cACertificateAttribute = "cACertificate";
            this.crossCertificateAttribute = "crossCertificatePair";
            this.certificateRevocationListAttribute = "certificateRevocationList";
            this.deltaRevocationListAttribute = "deltaRevocationList";
            this.authorityRevocationListAttribute = "authorityRevocationList";
            this.attributeCertificateAttributeAttribute = "attributeCertificateAttribute";
            this.aACertificateAttribute = "aACertificate";
            this.attributeDescriptorCertificateAttribute = "attributeDescriptorCertificate";
            this.attributeCertificateRevocationListAttribute = "attributeCertificateRevocationList";
            this.attributeAuthorityRevocationListAttribute = "attributeAuthorityRevocationList";
            this.ldapUserCertificateAttributeName = "cn";
            this.ldapCACertificateAttributeName = "cn ou o";
            this.ldapCrossCertificateAttributeName = "cn ou o";
            this.ldapCertificateRevocationListAttributeName = "cn ou o";
            this.ldapDeltaRevocationListAttributeName = "cn ou o";
            this.ldapAuthorityRevocationListAttributeName = "cn ou o";
            this.ldapAttributeCertificateAttributeAttributeName = "cn";
            this.ldapAACertificateAttributeName = "cn o ou";
            this.ldapAttributeDescriptorCertificateAttributeName = "cn o ou";
            this.ldapAttributeCertificateRevocationListAttributeName = "cn o ou";
            this.ldapAttributeAuthorityRevocationListAttributeName = "cn o ou";
            this.userCertificateSubjectAttributeName = "cn";
            this.cACertificateSubjectAttributeName = "o ou";
            this.crossCertificateSubjectAttributeName = "o ou";
            this.certificateRevocationListIssuerAttributeName = "o ou";
            this.deltaRevocationListIssuerAttributeName = "o ou";
            this.authorityRevocationListIssuerAttributeName = "o ou";
            this.attributeCertificateAttributeSubjectAttributeName = "cn";
            this.aACertificateSubjectAttributeName = "o ou";
            this.attributeDescriptorCertificateSubjectAttributeName = "o ou";
            this.attributeCertificateRevocationListIssuerAttributeName = "o ou";
            this.attributeAuthorityRevocationListIssuerAttributeName = "o ou";
            this.searchForSerialNumberIn = "uid serialNumber cn";
        }

        public X509LDAPCertStoreParameters build() {
            if (this.ldapUserCertificateAttributeName != null && this.ldapCACertificateAttributeName != null && this.ldapCrossCertificateAttributeName != null && this.ldapCertificateRevocationListAttributeName != null && this.ldapDeltaRevocationListAttributeName != null && this.ldapAuthorityRevocationListAttributeName != null && this.ldapAttributeCertificateAttributeAttributeName != null && this.ldapAACertificateAttributeName != null && this.ldapAttributeDescriptorCertificateAttributeName != null && this.ldapAttributeCertificateRevocationListAttributeName != null && this.ldapAttributeAuthorityRevocationListAttributeName != null && this.userCertificateSubjectAttributeName != null && this.cACertificateSubjectAttributeName != null && this.crossCertificateSubjectAttributeName != null && this.certificateRevocationListIssuerAttributeName != null && this.deltaRevocationListIssuerAttributeName != null && this.authorityRevocationListIssuerAttributeName != null && this.attributeCertificateAttributeSubjectAttributeName != null && this.aACertificateSubjectAttributeName != null && this.attributeDescriptorCertificateSubjectAttributeName != null && this.attributeCertificateRevocationListIssuerAttributeName != null && this.attributeAuthorityRevocationListIssuerAttributeName != null) {
                return new X509LDAPCertStoreParameters(this);
            }
            throw new IllegalArgumentException("Necessary parameters not specified.");
        }

        public Builder setAACertificateAttribute(String str) {
            this.aACertificateAttribute = str;
            return this;
        }

        public Builder setAACertificateSubjectAttributeName(String str) {
            this.aACertificateSubjectAttributeName = str;
            return this;
        }

        public Builder setAttributeAuthorityRevocationListAttribute(String str) {
            this.attributeAuthorityRevocationListAttribute = str;
            return this;
        }

        public Builder setAttributeAuthorityRevocationListIssuerAttributeName(String str) {
            this.attributeAuthorityRevocationListIssuerAttributeName = str;
            return this;
        }

        public Builder setAttributeCertificateAttributeAttribute(String str) {
            this.attributeCertificateAttributeAttribute = str;
            return this;
        }

        public Builder setAttributeCertificateAttributeSubjectAttributeName(String str) {
            this.attributeCertificateAttributeSubjectAttributeName = str;
            return this;
        }

        public Builder setAttributeCertificateRevocationListAttribute(String str) {
            this.attributeCertificateRevocationListAttribute = str;
            return this;
        }

        public Builder setAttributeCertificateRevocationListIssuerAttributeName(String str) {
            this.attributeCertificateRevocationListIssuerAttributeName = str;
            return this;
        }

        public Builder setAttributeDescriptorCertificateAttribute(String str) {
            this.attributeDescriptorCertificateAttribute = str;
            return this;
        }

        public Builder setAttributeDescriptorCertificateSubjectAttributeName(String str) {
            this.attributeDescriptorCertificateSubjectAttributeName = str;
            return this;
        }

        public Builder setAuthorityRevocationListAttribute(String str) {
            this.authorityRevocationListAttribute = str;
            return this;
        }

        public Builder setAuthorityRevocationListIssuerAttributeName(String str) {
            this.authorityRevocationListIssuerAttributeName = str;
            return this;
        }

        public Builder setCACertificateAttribute(String str) {
            this.cACertificateAttribute = str;
            return this;
        }

        public Builder setCACertificateSubjectAttributeName(String str) {
            this.cACertificateSubjectAttributeName = str;
            return this;
        }

        public Builder setCertificateRevocationListAttribute(String str) {
            this.certificateRevocationListAttribute = str;
            return this;
        }

        public Builder setCertificateRevocationListIssuerAttributeName(String str) {
            this.certificateRevocationListIssuerAttributeName = str;
            return this;
        }

        public Builder setCrossCertificateAttribute(String str) {
            this.crossCertificateAttribute = str;
            return this;
        }

        public Builder setCrossCertificateSubjectAttributeName(String str) {
            this.crossCertificateSubjectAttributeName = str;
            return this;
        }

        public Builder setDeltaRevocationListAttribute(String str) {
            this.deltaRevocationListAttribute = str;
            return this;
        }

        public Builder setDeltaRevocationListIssuerAttributeName(String str) {
            this.deltaRevocationListIssuerAttributeName = str;
            return this;
        }

        public Builder setLdapAACertificateAttributeName(String str) {
            this.ldapAACertificateAttributeName = str;
            return this;
        }

        public Builder setLdapAttributeAuthorityRevocationListAttributeName(String str) {
            this.ldapAttributeAuthorityRevocationListAttributeName = str;
            return this;
        }

        public Builder setLdapAttributeCertificateAttributeAttributeName(String str) {
            this.ldapAttributeCertificateAttributeAttributeName = str;
            return this;
        }

        public Builder setLdapAttributeCertificateRevocationListAttributeName(String str) {
            this.ldapAttributeCertificateRevocationListAttributeName = str;
            return this;
        }

        public Builder setLdapAttributeDescriptorCertificateAttributeName(String str) {
            this.ldapAttributeDescriptorCertificateAttributeName = str;
            return this;
        }

        public Builder setLdapAuthorityRevocationListAttributeName(String str) {
            this.ldapAuthorityRevocationListAttributeName = str;
            return this;
        }

        public Builder setLdapCACertificateAttributeName(String str) {
            this.ldapCACertificateAttributeName = str;
            return this;
        }

        public Builder setLdapCertificateRevocationListAttributeName(String str) {
            this.ldapCertificateRevocationListAttributeName = str;
            return this;
        }

        public Builder setLdapCrossCertificateAttributeName(String str) {
            this.ldapCrossCertificateAttributeName = str;
            return this;
        }

        public Builder setLdapDeltaRevocationListAttributeName(String str) {
            this.ldapDeltaRevocationListAttributeName = str;
            return this;
        }

        public Builder setLdapUserCertificateAttributeName(String str) {
            this.ldapUserCertificateAttributeName = str;
            return this;
        }

        public Builder setSearchForSerialNumberIn(String str) {
            this.searchForSerialNumberIn = str;
            return this;
        }

        public Builder setUserCertificateAttribute(String str) {
            this.userCertificateAttribute = str;
            return this;
        }

        public Builder setUserCertificateSubjectAttributeName(String str) {
            this.userCertificateSubjectAttributeName = str;
            return this;
        }
    }

    private X509LDAPCertStoreParameters(Builder builder) {
        this.ldapURL = builder.ldapURL;
        this.baseDN = builder.baseDN;
        this.userCertificateAttribute = builder.userCertificateAttribute;
        this.cACertificateAttribute = builder.cACertificateAttribute;
        this.crossCertificateAttribute = builder.crossCertificateAttribute;
        this.certificateRevocationListAttribute = builder.certificateRevocationListAttribute;
        this.deltaRevocationListAttribute = builder.deltaRevocationListAttribute;
        this.authorityRevocationListAttribute = builder.authorityRevocationListAttribute;
        this.attributeCertificateAttributeAttribute = builder.attributeCertificateAttributeAttribute;
        this.aACertificateAttribute = builder.aACertificateAttribute;
        this.attributeDescriptorCertificateAttribute = builder.attributeDescriptorCertificateAttribute;
        this.attributeCertificateRevocationListAttribute = builder.attributeCertificateRevocationListAttribute;
        this.attributeAuthorityRevocationListAttribute = builder.attributeAuthorityRevocationListAttribute;
        this.ldapUserCertificateAttributeName = builder.ldapUserCertificateAttributeName;
        this.ldapCACertificateAttributeName = builder.ldapCACertificateAttributeName;
        this.ldapCrossCertificateAttributeName = builder.ldapCrossCertificateAttributeName;
        this.ldapCertificateRevocationListAttributeName = builder.ldapCertificateRevocationListAttributeName;
        this.ldapDeltaRevocationListAttributeName = builder.ldapDeltaRevocationListAttributeName;
        this.ldapAuthorityRevocationListAttributeName = builder.ldapAuthorityRevocationListAttributeName;
        this.ldapAttributeCertificateAttributeAttributeName = builder.ldapAttributeCertificateAttributeAttributeName;
        this.ldapAACertificateAttributeName = builder.ldapAACertificateAttributeName;
        this.ldapAttributeDescriptorCertificateAttributeName = builder.ldapAttributeDescriptorCertificateAttributeName;
        this.ldapAttributeCertificateRevocationListAttributeName = builder.ldapAttributeCertificateRevocationListAttributeName;
        this.ldapAttributeAuthorityRevocationListAttributeName = builder.ldapAttributeAuthorityRevocationListAttributeName;
        this.userCertificateSubjectAttributeName = builder.userCertificateSubjectAttributeName;
        this.cACertificateSubjectAttributeName = builder.cACertificateSubjectAttributeName;
        this.crossCertificateSubjectAttributeName = builder.crossCertificateSubjectAttributeName;
        this.certificateRevocationListIssuerAttributeName = builder.certificateRevocationListIssuerAttributeName;
        this.deltaRevocationListIssuerAttributeName = builder.deltaRevocationListIssuerAttributeName;
        this.authorityRevocationListIssuerAttributeName = builder.authorityRevocationListIssuerAttributeName;
        this.attributeCertificateAttributeSubjectAttributeName = builder.attributeCertificateAttributeSubjectAttributeName;
        this.aACertificateSubjectAttributeName = builder.aACertificateSubjectAttributeName;
        this.attributeDescriptorCertificateSubjectAttributeName = builder.attributeDescriptorCertificateSubjectAttributeName;
        this.attributeCertificateRevocationListIssuerAttributeName = builder.attributeCertificateRevocationListIssuerAttributeName;
        this.attributeAuthorityRevocationListIssuerAttributeName = builder.attributeAuthorityRevocationListIssuerAttributeName;
        this.searchForSerialNumberIn = builder.searchForSerialNumberIn;
    }

    private int addHashCode(int i, Object obj) {
        return (i * 29) + (obj == null ? 0 : obj.hashCode());
    }

    private boolean checkField(Object obj, Object obj2) {
        if (obj == obj2) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return obj.equals(obj2);
    }

    public static X509LDAPCertStoreParameters getInstance(LDAPCertStoreParameters lDAPCertStoreParameters) {
        return new Builder("ldap://" + lDAPCertStoreParameters.getServerName() + ":" + lDAPCertStoreParameters.getPort(), "").build();
    }

    public Object clone() {
        return this;
    }

    public boolean equal(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof X509LDAPCertStoreParameters)) {
            return false;
        }
        X509LDAPCertStoreParameters x509LDAPCertStoreParameters = (X509LDAPCertStoreParameters) obj;
        return checkField(this.ldapURL, x509LDAPCertStoreParameters.ldapURL) && checkField(this.baseDN, x509LDAPCertStoreParameters.baseDN) && checkField(this.userCertificateAttribute, x509LDAPCertStoreParameters.userCertificateAttribute) && checkField(this.cACertificateAttribute, x509LDAPCertStoreParameters.cACertificateAttribute) && checkField(this.crossCertificateAttribute, x509LDAPCertStoreParameters.crossCertificateAttribute) && checkField(this.certificateRevocationListAttribute, x509LDAPCertStoreParameters.certificateRevocationListAttribute) && checkField(this.deltaRevocationListAttribute, x509LDAPCertStoreParameters.deltaRevocationListAttribute) && checkField(this.authorityRevocationListAttribute, x509LDAPCertStoreParameters.authorityRevocationListAttribute) && checkField(this.attributeCertificateAttributeAttribute, x509LDAPCertStoreParameters.attributeCertificateAttributeAttribute) && checkField(this.aACertificateAttribute, x509LDAPCertStoreParameters.aACertificateAttribute) && checkField(this.attributeDescriptorCertificateAttribute, x509LDAPCertStoreParameters.attributeDescriptorCertificateAttribute) && checkField(this.attributeCertificateRevocationListAttribute, x509LDAPCertStoreParameters.attributeCertificateRevocationListAttribute) && checkField(this.attributeAuthorityRevocationListAttribute, x509LDAPCertStoreParameters.attributeAuthorityRevocationListAttribute) && checkField(this.ldapUserCertificateAttributeName, x509LDAPCertStoreParameters.ldapUserCertificateAttributeName) && checkField(this.ldapCACertificateAttributeName, x509LDAPCertStoreParameters.ldapCACertificateAttributeName) && checkField(this.ldapCrossCertificateAttributeName, x509LDAPCertStoreParameters.ldapCrossCertificateAttributeName) && checkField(this.ldapCertificateRevocationListAttributeName, x509LDAPCertStoreParameters.ldapCertificateRevocationListAttributeName) && checkField(this.ldapDeltaRevocationListAttributeName, x509LDAPCertStoreParameters.ldapDeltaRevocationListAttributeName) && checkField(this.ldapAuthorityRevocationListAttributeName, x509LDAPCertStoreParameters.ldapAuthorityRevocationListAttributeName) && checkField(this.ldapAttributeCertificateAttributeAttributeName, x509LDAPCertStoreParameters.ldapAttributeCertificateAttributeAttributeName) && checkField(this.ldapAACertificateAttributeName, x509LDAPCertStoreParameters.ldapAACertificateAttributeName) && checkField(this.ldapAttributeDescriptorCertificateAttributeName, x509LDAPCertStoreParameters.ldapAttributeDescriptorCertificateAttributeName) && checkField(this.ldapAttributeCertificateRevocationListAttributeName, x509LDAPCertStoreParameters.ldapAttributeCertificateRevocationListAttributeName) && checkField(this.ldapAttributeAuthorityRevocationListAttributeName, x509LDAPCertStoreParameters.ldapAttributeAuthorityRevocationListAttributeName) && checkField(this.userCertificateSubjectAttributeName, x509LDAPCertStoreParameters.userCertificateSubjectAttributeName) && checkField(this.cACertificateSubjectAttributeName, x509LDAPCertStoreParameters.cACertificateSubjectAttributeName) && checkField(this.crossCertificateSubjectAttributeName, x509LDAPCertStoreParameters.crossCertificateSubjectAttributeName) && checkField(this.certificateRevocationListIssuerAttributeName, x509LDAPCertStoreParameters.certificateRevocationListIssuerAttributeName) && checkField(this.deltaRevocationListIssuerAttributeName, x509LDAPCertStoreParameters.deltaRevocationListIssuerAttributeName) && checkField(this.authorityRevocationListIssuerAttributeName, x509LDAPCertStoreParameters.authorityRevocationListIssuerAttributeName) && checkField(this.attributeCertificateAttributeSubjectAttributeName, x509LDAPCertStoreParameters.attributeCertificateAttributeSubjectAttributeName) && checkField(this.aACertificateSubjectAttributeName, x509LDAPCertStoreParameters.aACertificateSubjectAttributeName) && checkField(this.attributeDescriptorCertificateSubjectAttributeName, x509LDAPCertStoreParameters.attributeDescriptorCertificateSubjectAttributeName) && checkField(this.attributeCertificateRevocationListIssuerAttributeName, x509LDAPCertStoreParameters.attributeCertificateRevocationListIssuerAttributeName) && checkField(this.attributeAuthorityRevocationListIssuerAttributeName, x509LDAPCertStoreParameters.attributeAuthorityRevocationListIssuerAttributeName) && checkField(this.searchForSerialNumberIn, x509LDAPCertStoreParameters.searchForSerialNumberIn);
    }

    public String getAACertificateAttribute() {
        return this.aACertificateAttribute;
    }

    public String getAACertificateSubjectAttributeName() {
        return this.aACertificateSubjectAttributeName;
    }

    public String getAttributeAuthorityRevocationListAttribute() {
        return this.attributeAuthorityRevocationListAttribute;
    }

    public String getAttributeAuthorityRevocationListIssuerAttributeName() {
        return this.attributeAuthorityRevocationListIssuerAttributeName;
    }

    public String getAttributeCertificateAttributeAttribute() {
        return this.attributeCertificateAttributeAttribute;
    }

    public String getAttributeCertificateAttributeSubjectAttributeName() {
        return this.attributeCertificateAttributeSubjectAttributeName;
    }

    public String getAttributeCertificateRevocationListAttribute() {
        return this.attributeCertificateRevocationListAttribute;
    }

    public String getAttributeCertificateRevocationListIssuerAttributeName() {
        return this.attributeCertificateRevocationListIssuerAttributeName;
    }

    public String getAttributeDescriptorCertificateAttribute() {
        return this.attributeDescriptorCertificateAttribute;
    }

    public String getAttributeDescriptorCertificateSubjectAttributeName() {
        return this.attributeDescriptorCertificateSubjectAttributeName;
    }

    public String getAuthorityRevocationListAttribute() {
        return this.authorityRevocationListAttribute;
    }

    public String getAuthorityRevocationListIssuerAttributeName() {
        return this.authorityRevocationListIssuerAttributeName;
    }

    public String getBaseDN() {
        return this.baseDN;
    }

    public String getCACertificateAttribute() {
        return this.cACertificateAttribute;
    }

    public String getCACertificateSubjectAttributeName() {
        return this.cACertificateSubjectAttributeName;
    }

    public String getCertificateRevocationListAttribute() {
        return this.certificateRevocationListAttribute;
    }

    public String getCertificateRevocationListIssuerAttributeName() {
        return this.certificateRevocationListIssuerAttributeName;
    }

    public String getCrossCertificateAttribute() {
        return this.crossCertificateAttribute;
    }

    public String getCrossCertificateSubjectAttributeName() {
        return this.crossCertificateSubjectAttributeName;
    }

    public String getDeltaRevocationListAttribute() {
        return this.deltaRevocationListAttribute;
    }

    public String getDeltaRevocationListIssuerAttributeName() {
        return this.deltaRevocationListIssuerAttributeName;
    }

    public String getLdapAACertificateAttributeName() {
        return this.ldapAACertificateAttributeName;
    }

    public String getLdapAttributeAuthorityRevocationListAttributeName() {
        return this.ldapAttributeAuthorityRevocationListAttributeName;
    }

    public String getLdapAttributeCertificateAttributeAttributeName() {
        return this.ldapAttributeCertificateAttributeAttributeName;
    }

    public String getLdapAttributeCertificateRevocationListAttributeName() {
        return this.ldapAttributeCertificateRevocationListAttributeName;
    }

    public String getLdapAttributeDescriptorCertificateAttributeName() {
        return this.ldapAttributeDescriptorCertificateAttributeName;
    }

    public String getLdapAuthorityRevocationListAttributeName() {
        return this.ldapAuthorityRevocationListAttributeName;
    }

    public String getLdapCACertificateAttributeName() {
        return this.ldapCACertificateAttributeName;
    }

    public String getLdapCertificateRevocationListAttributeName() {
        return this.ldapCertificateRevocationListAttributeName;
    }

    public String getLdapCrossCertificateAttributeName() {
        return this.ldapCrossCertificateAttributeName;
    }

    public String getLdapDeltaRevocationListAttributeName() {
        return this.ldapDeltaRevocationListAttributeName;
    }

    public String getLdapURL() {
        return this.ldapURL;
    }

    public String getLdapUserCertificateAttributeName() {
        return this.ldapUserCertificateAttributeName;
    }

    public String getSearchForSerialNumberIn() {
        return this.searchForSerialNumberIn;
    }

    public String getUserCertificateAttribute() {
        return this.userCertificateAttribute;
    }

    public String getUserCertificateSubjectAttributeName() {
        return this.userCertificateSubjectAttributeName;
    }

    public int hashCode() {
        return addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(addHashCode(0, this.userCertificateAttribute), this.cACertificateAttribute), this.crossCertificateAttribute), this.certificateRevocationListAttribute), this.deltaRevocationListAttribute), this.authorityRevocationListAttribute), this.attributeCertificateAttributeAttribute), this.aACertificateAttribute), this.attributeDescriptorCertificateAttribute), this.attributeCertificateRevocationListAttribute), this.attributeAuthorityRevocationListAttribute), this.ldapUserCertificateAttributeName), this.ldapCACertificateAttributeName), this.ldapCrossCertificateAttributeName), this.ldapCertificateRevocationListAttributeName), this.ldapDeltaRevocationListAttributeName), this.ldapAuthorityRevocationListAttributeName), this.ldapAttributeCertificateAttributeAttributeName), this.ldapAACertificateAttributeName), this.ldapAttributeDescriptorCertificateAttributeName), this.ldapAttributeCertificateRevocationListAttributeName), this.ldapAttributeAuthorityRevocationListAttributeName), this.userCertificateSubjectAttributeName), this.cACertificateSubjectAttributeName), this.crossCertificateSubjectAttributeName), this.certificateRevocationListIssuerAttributeName), this.deltaRevocationListIssuerAttributeName), this.authorityRevocationListIssuerAttributeName), this.attributeCertificateAttributeSubjectAttributeName), this.aACertificateSubjectAttributeName), this.attributeDescriptorCertificateSubjectAttributeName), this.attributeCertificateRevocationListIssuerAttributeName), this.attributeAuthorityRevocationListIssuerAttributeName), this.searchForSerialNumberIn);
    }
}
