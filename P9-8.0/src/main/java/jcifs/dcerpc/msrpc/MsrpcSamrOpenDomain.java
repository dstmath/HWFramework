package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.samr.SamrOpenDomain;
import jcifs.dcerpc.rpc.sid_t;

public class MsrpcSamrOpenDomain extends SamrOpenDomain {
    public MsrpcSamrOpenDomain(SamrPolicyHandle handle, int access, sid_t sid, SamrDomainHandle domainHandle) {
        super(handle, access, sid, domainHandle);
        this.ptype = 0;
        this.flags = 3;
    }
}
