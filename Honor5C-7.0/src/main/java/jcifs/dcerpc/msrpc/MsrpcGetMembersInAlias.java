package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.lsarpc.LsarSidArray;
import jcifs.dcerpc.msrpc.samr.SamrGetMembersInAlias;

public class MsrpcGetMembersInAlias extends SamrGetMembersInAlias {
    public MsrpcGetMembersInAlias(SamrAliasHandle aliasHandle, LsarSidArray sids) {
        super(aliasHandle, sids);
        this.sids = sids;
        this.ptype = 0;
        this.flags = 3;
    }
}
