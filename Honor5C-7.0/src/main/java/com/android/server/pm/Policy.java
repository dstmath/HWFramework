package com.android.server.pm;

import android.content.pm.PackageParser.Package;
import android.content.pm.Signature;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* compiled from: SELinuxMMAC */
final class Policy {
    private final Set<Signature> mCerts;
    private final Map<String, String> mPkgMap;
    private final String mSeinfo;

    /* compiled from: SELinuxMMAC */
    public static final class PolicyBuilder {
        private final Set<Signature> mCerts;
        private final Map<String, String> mPkgMap;
        private String mSeinfo;

        public PolicyBuilder() {
            this.mCerts = new HashSet(2);
            this.mPkgMap = new HashMap(2);
        }

        public PolicyBuilder addSignature(String cert) {
            if (cert == null) {
                throw new IllegalArgumentException("Invalid signature value " + cert);
            }
            this.mCerts.add(new Signature(cert));
            return this;
        }

        public PolicyBuilder setGlobalSeinfoOrThrow(String seinfo) {
            if (!validateValue(seinfo)) {
                throw new IllegalArgumentException("Invalid seinfo value " + seinfo);
            } else if (this.mSeinfo == null || this.mSeinfo.equals(seinfo)) {
                this.mSeinfo = seinfo;
                return this;
            } else {
                throw new IllegalStateException("Duplicate seinfo tag found");
            }
        }

        public PolicyBuilder addInnerPackageMapOrThrow(String pkgName, String seinfo) {
            if (!validateValue(pkgName)) {
                throw new IllegalArgumentException("Invalid package name " + pkgName);
            } else if (validateValue(seinfo)) {
                String pkgValue = (String) this.mPkgMap.get(pkgName);
                if (pkgValue == null || pkgValue.equals(seinfo)) {
                    this.mPkgMap.put(pkgName, seinfo);
                    return this;
                }
                throw new IllegalStateException("Conflicting seinfo value found");
            } else {
                throw new IllegalArgumentException("Invalid seinfo value " + seinfo);
            }
        }

        private boolean validateValue(String name) {
            if (name != null && name.matches("\\A[\\.\\w]+\\z")) {
                return true;
            }
            return false;
        }

        public Policy build() {
            Policy p = new Policy();
            if (p.mCerts.isEmpty()) {
                throw new IllegalStateException("Missing certs with signer tag. Expecting at least one.");
            }
            if (((p.mSeinfo == null ? 1 : 0) ^ p.mPkgMap.isEmpty()) != 0) {
                return p;
            }
            throw new IllegalStateException("Only seinfo tag XOR package tags are allowed within a signer stanza.");
        }
    }

    private Policy(PolicyBuilder builder) {
        this.mSeinfo = builder.mSeinfo;
        this.mCerts = Collections.unmodifiableSet(builder.mCerts);
        this.mPkgMap = Collections.unmodifiableMap(builder.mPkgMap);
    }

    public Set<Signature> getSignatures() {
        return this.mCerts;
    }

    public boolean hasInnerPackages() {
        return !this.mPkgMap.isEmpty();
    }

    public Map<String, String> getInnerPackages() {
        return this.mPkgMap;
    }

    public boolean hasGlobalSeinfo() {
        return this.mSeinfo != null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Signature cert : this.mCerts) {
            sb.append("cert=").append(cert.toCharsString().substring(0, 11)).append("... ");
        }
        if (this.mSeinfo != null) {
            sb.append("seinfo=").append(this.mSeinfo);
        }
        for (String name : this.mPkgMap.keySet()) {
            sb.append(" ").append(name).append("=").append((String) this.mPkgMap.get(name));
        }
        return sb.toString();
    }

    public String getMatchedSeinfo(Package pkg) {
        if (!Signature.areExactMatch((Signature[]) this.mCerts.toArray(new Signature[0]), pkg.mSignatures)) {
            return null;
        }
        String seinfoValue = (String) this.mPkgMap.get(pkg.packageName);
        if (seinfoValue != null) {
            return seinfoValue;
        }
        return this.mSeinfo;
    }
}
