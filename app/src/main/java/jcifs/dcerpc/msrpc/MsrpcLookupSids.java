package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.lsarpc.LsarLookupSids;
import jcifs.dcerpc.msrpc.lsarpc.LsarRefDomainList;
import jcifs.dcerpc.msrpc.lsarpc.LsarTransNameArray;
import jcifs.smb.SID;

public class MsrpcLookupSids extends LsarLookupSids {
    SID[] sids;

    public MsrpcLookupSids(LsaPolicyHandle policyHandle, SID[] sids) {
        super(policyHandle, new LsarSidArrayX(sids), new LsarRefDomainList(), new LsarTransNameArray(), (short) 1, sids.length);
        this.sids = sids;
        this.ptype = 0;
        this.flags = 3;
    }
}
