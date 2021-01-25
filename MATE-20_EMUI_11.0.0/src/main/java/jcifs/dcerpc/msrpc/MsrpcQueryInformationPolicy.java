package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.lsarpc;
import jcifs.dcerpc.ndr.NdrObject;

public class MsrpcQueryInformationPolicy extends lsarpc.LsarQueryInformationPolicy {
    public MsrpcQueryInformationPolicy(LsaPolicyHandle policyHandle, short level, NdrObject info) {
        super(policyHandle, level, info);
        this.ptype = 0;
        this.flags = 3;
    }
}
