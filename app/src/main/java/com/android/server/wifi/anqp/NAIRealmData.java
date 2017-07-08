package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.eap.EAPMethod;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.pps.Credential;
import com.android.server.wifi.hotspot2.pps.DomainMatcher;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NAIRealmData {
    private final List<EAPMethod> mEAPMethods;
    private final List<String> mRealms;

    public NAIRealmData(ByteBuffer payload) throws ProtocolException {
        if (payload.remaining() < 5) {
            throw new ProtocolException("Runt payload: " + payload.remaining());
        }
        int length = payload.getShort() & Constants.SHORT_MASK;
        if (length > payload.remaining()) {
            throw new ProtocolException("Invalid data length: " + length);
        }
        boolean utf8;
        Charset charset;
        if ((payload.get() & 1) == 1) {
            utf8 = true;
        } else {
            utf8 = false;
        }
        if (utf8) {
            charset = StandardCharsets.UTF_8;
        } else {
            charset = StandardCharsets.US_ASCII;
        }
        String[] realms = Constants.getPrefixedString(payload, 1, charset).split(";");
        this.mRealms = new ArrayList();
        for (String realmElement : realms) {
            if (realmElement.length() > 0) {
                this.mRealms.add(realmElement);
            }
        }
        int methodCount = payload.get() & Constants.BYTE_MASK;
        this.mEAPMethods = new ArrayList(methodCount);
        while (methodCount > 0) {
            this.mEAPMethods.add(new EAPMethod(payload));
            methodCount--;
        }
    }

    public List<String> getRealms() {
        return Collections.unmodifiableList(this.mRealms);
    }

    public List<EAPMethod> getEAPMethods() {
        return Collections.unmodifiableList(this.mEAPMethods);
    }

    public int match(List<String> credLabels, Credential credential) {
        int realmMatch = -1;
        if (!this.mRealms.isEmpty()) {
            for (String realm : this.mRealms) {
                if (DomainMatcher.arg2SubdomainOfArg1(credLabels, Utils.splitDomain(realm))) {
                    realmMatch = 4;
                    break;
                }
            }
            if (realmMatch == -1 || this.mEAPMethods.isEmpty()) {
                return realmMatch;
            }
        } else if (this.mEAPMethods.isEmpty()) {
            return 0;
        }
        int best = -1;
        for (EAPMethod eapMethod : this.mEAPMethods) {
            int match = eapMethod.match(credential) | realmMatch;
            if (match > best) {
                best = match;
                if (match == 7) {
                    return match;
                }
            }
        }
        return best;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  NAI Realm(s)");
        for (String realm : this.mRealms) {
            sb.append(' ').append(realm);
        }
        sb.append('\n');
        for (EAPMethod eapMethod : this.mEAPMethods) {
            sb.append("    ").append(eapMethod.toString());
        }
        return sb.toString();
    }
}
