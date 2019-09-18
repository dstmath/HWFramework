package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.lsarpc;
import jcifs.dcerpc.msrpc.samr;

public class MsrpcGetMembersInAlias extends samr.SamrGetMembersInAlias {
    public MsrpcGetMembersInAlias(SamrAliasHandle aliasHandle, lsarpc.LsarSidArray sids) {
        super(aliasHandle, sids);
        this.sids = sids;
        this.ptype = 0;
        this.flags = 3;
    }
}
