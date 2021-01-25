package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.samr;

public class MsrpcEnumerateAliasesInDomain extends samr.SamrEnumerateAliasesInDomain {
    public MsrpcEnumerateAliasesInDomain(SamrDomainHandle domainHandle, int acct_flags, samr.SamrSamArray sam) {
        super(domainHandle, 0, acct_flags, null, 0);
        this.sam = sam;
        this.ptype = 0;
        this.flags = 3;
    }
}
