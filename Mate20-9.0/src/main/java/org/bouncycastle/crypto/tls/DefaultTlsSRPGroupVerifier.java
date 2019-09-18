package org.bouncycastle.crypto.tls;

import java.math.BigInteger;
import java.util.Vector;
import org.bouncycastle.crypto.agreement.srp.SRP6StandardGroups;
import org.bouncycastle.crypto.params.SRP6GroupParameters;

public class DefaultTlsSRPGroupVerifier implements TlsSRPGroupVerifier {
    protected static final Vector DEFAULT_GROUPS = new Vector();
    protected Vector groups;

    static {
        DEFAULT_GROUPS.addElement(SRP6StandardGroups.rfc5054_1024);
        DEFAULT_GROUPS.addElement(SRP6StandardGroups.rfc5054_1536);
        DEFAULT_GROUPS.addElement(SRP6StandardGroups.rfc5054_2048);
        DEFAULT_GROUPS.addElement(SRP6StandardGroups.rfc5054_3072);
        DEFAULT_GROUPS.addElement(SRP6StandardGroups.rfc5054_4096);
        DEFAULT_GROUPS.addElement(SRP6StandardGroups.rfc5054_6144);
        DEFAULT_GROUPS.addElement(SRP6StandardGroups.rfc5054_8192);
    }

    public DefaultTlsSRPGroupVerifier() {
        this(DEFAULT_GROUPS);
    }

    public DefaultTlsSRPGroupVerifier(Vector vector) {
        this.groups = vector;
    }

    public boolean accept(SRP6GroupParameters sRP6GroupParameters) {
        for (int i = 0; i < this.groups.size(); i++) {
            if (areGroupsEqual(sRP6GroupParameters, (SRP6GroupParameters) this.groups.elementAt(i))) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean areGroupsEqual(SRP6GroupParameters sRP6GroupParameters, SRP6GroupParameters sRP6GroupParameters2) {
        return sRP6GroupParameters == sRP6GroupParameters2 || (areParametersEqual(sRP6GroupParameters.getN(), sRP6GroupParameters2.getN()) && areParametersEqual(sRP6GroupParameters.getG(), sRP6GroupParameters2.getG()));
    }

    /* access modifiers changed from: protected */
    public boolean areParametersEqual(BigInteger bigInteger, BigInteger bigInteger2) {
        return bigInteger == bigInteger2 || bigInteger.equals(bigInteger2);
    }
}
