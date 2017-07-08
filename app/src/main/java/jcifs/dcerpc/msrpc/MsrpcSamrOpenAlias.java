package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.samr.SamrOpenAlias;

public class MsrpcSamrOpenAlias extends SamrOpenAlias {
    public MsrpcSamrOpenAlias(SamrDomainHandle handle, int access, int rid, SamrAliasHandle aliasHandle) {
        super(handle, access, rid, aliasHandle);
        this.ptype = 0;
        this.flags = 3;
    }
}
