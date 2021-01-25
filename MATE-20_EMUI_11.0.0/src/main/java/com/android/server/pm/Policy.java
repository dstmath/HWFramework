package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.Signature;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/* access modifiers changed from: package-private */
/* compiled from: SELinuxMMAC */
public final class Policy {
    private final Set<Signature> mCerts;
    private final Map<String, String> mPkgMap;
    private final String mSeinfo;

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
        Iterator<Signature> it = this.mCerts.iterator();
        while (it.hasNext()) {
            sb.append("cert=" + it.next().toCharsString().substring(0, 11) + "... ");
        }
        if (this.mSeinfo != null) {
            sb.append("seinfo=" + this.mSeinfo);
        }
        for (String name : this.mPkgMap.keySet()) {
            sb.append(" " + name + "=" + this.mPkgMap.get(name));
        }
        return sb.toString();
    }

    public String getMatchedSeInfo(PackageParser.Package pkg) {
        Signature[] certs = (Signature[]) this.mCerts.toArray(new Signature[0]);
        if (pkg.mSigningDetails != PackageParser.SigningDetails.UNKNOWN && !Signature.areExactMatch(certs, pkg.mSigningDetails.signatures) && (certs.length > 1 || !pkg.mSigningDetails.hasCertificate(certs[0]))) {
            return null;
        }
        String seinfoValue = this.mPkgMap.get(pkg.packageName);
        if (seinfoValue != null) {
            return seinfoValue;
        }
        return this.mSeinfo;
    }

    /* compiled from: SELinuxMMAC */
    public static final class PolicyBuilder {
        private final Set<Signature> mCerts = new HashSet(2);
        private final Map<String, String> mPkgMap = new HashMap(2);
        private String mSeinfo;

        public PolicyBuilder addSignature(String cert) {
            if (cert != null) {
                this.mCerts.add(new Signature(cert));
                return this;
            }
            throw new IllegalArgumentException("Invalid signature value " + cert);
        }

        public PolicyBuilder setGlobalSeinfoOrThrow(String seinfo) {
            if (validateValue(seinfo)) {
                String str = this.mSeinfo;
                if (str == null || str.equals(seinfo)) {
                    this.mSeinfo = seinfo;
                    return this;
                }
                throw new IllegalStateException("Duplicate seinfo tag found");
            }
            throw new IllegalArgumentException("Invalid seinfo value " + seinfo);
        }

        public PolicyBuilder addInnerPackageMapOrThrow(String pkgName, String seinfo) {
            if (!validateValue(pkgName)) {
                throw new IllegalArgumentException("Invalid package name " + pkgName);
            } else if (validateValue(seinfo)) {
                String pkgValue = this.mPkgMap.get(pkgName);
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
            Policy p = new Policy(this);
            if (!p.mCerts.isEmpty()) {
                if ((p.mSeinfo == null) ^ p.mPkgMap.isEmpty()) {
                    return p;
                }
                throw new IllegalStateException("Only seinfo tag XOR package tags are allowed within a signer stanza.");
            }
            throw new IllegalStateException("Missing certs with signer tag. Expecting at least one.");
        }
    }
}
