package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.lsarpc;
import jcifs.smb.SID;

public class MsrpcLookupSids extends lsarpc.LsarLookupSids {
    SID[] sids;

    public MsrpcLookupSids(LsaPolicyHandle policyHandle, SID[] sids2) {
        super(policyHandle, new LsarSidArrayX(sids2), new lsarpc.LsarRefDomainList(), new lsarpc.LsarTransNameArray(), 1, sids2.length);
        this.sids = sids2;
        this.ptype = 0;
        this.flags = 3;
    }
}
