package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.samr;
import jcifs.dcerpc.rpc;

public class MsrpcSamrOpenDomain extends samr.SamrOpenDomain {
    public MsrpcSamrOpenDomain(SamrPolicyHandle handle, int access, rpc.sid_t sid, SamrDomainHandle domainHandle) {
        super(handle, access, sid, domainHandle);
        this.ptype = 0;
        this.flags = 3;
    }
}
