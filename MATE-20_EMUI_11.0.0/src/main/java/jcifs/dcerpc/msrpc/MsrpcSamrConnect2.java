package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.samr;

public class MsrpcSamrConnect2 extends samr.SamrConnect2 {
    public MsrpcSamrConnect2(String server, int access, SamrPolicyHandle policyHandle) {
        super(server, access, policyHandle);
        this.ptype = 0;
        this.flags = 3;
    }
}
