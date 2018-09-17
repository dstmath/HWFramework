package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.pps.Credential;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NAIRealmElement extends ANQPElement {
    private final List<NAIRealmData> mRealmData;

    public NAIRealmElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        if (!payload.hasRemaining()) {
            this.mRealmData = Collections.emptyList();
        } else if (payload.remaining() < 2) {
            throw new ProtocolException("Runt NAI Realm: " + payload.remaining());
        } else {
            int count = payload.getShort() & Constants.SHORT_MASK;
            this.mRealmData = new ArrayList(count);
            while (count > 0) {
                this.mRealmData.add(new NAIRealmData(payload));
                count--;
            }
        }
    }

    public List<NAIRealmData> getRealmData() {
        return Collections.unmodifiableList(this.mRealmData);
    }

    public int match(Credential credential) {
        if (this.mRealmData.isEmpty()) {
            return 0;
        }
        List<String> credLabels = Utils.splitDomain(credential.getRealm());
        int best = -1;
        for (NAIRealmData realmData : this.mRealmData) {
            int match = realmData.match(credLabels, credential);
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
        sb.append("NAI Realm:\n");
        for (NAIRealmData data : this.mRealmData) {
            sb.append(data);
        }
        return sb.toString();
    }
}
