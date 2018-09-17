package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.samr.SamrConnect4;

public class MsrpcSamrConnect4 extends SamrConnect4 {
    public MsrpcSamrConnect4(String server, int access, SamrPolicyHandle policyHandle) {
        super(server, 2, access, policyHandle);
        this.ptype = 0;
        this.flags = 3;
    }
}
