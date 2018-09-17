package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.samr.SamrEnumerateAliasesInDomain;
import jcifs.dcerpc.msrpc.samr.SamrSamArray;

public class MsrpcEnumerateAliasesInDomain extends SamrEnumerateAliasesInDomain {
    public MsrpcEnumerateAliasesInDomain(SamrDomainHandle domainHandle, int acct_flags, SamrSamArray sam) {
        super(domainHandle, 0, acct_flags, null, 0);
        this.sam = sam;
        this.ptype = 0;
        this.flags = 3;
    }
}
