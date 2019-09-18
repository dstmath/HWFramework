package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.lsarpc;

public class MsrpcLsarOpenPolicy2 extends lsarpc.LsarOpenPolicy2 {
    public MsrpcLsarOpenPolicy2(String server, int access, LsaPolicyHandle policyHandle) {
        super(server, new lsarpc.LsarObjectAttributes(), access, policyHandle);
        this.object_attributes.length = 24;
        lsarpc.LsarQosInfo qos = new lsarpc.LsarQosInfo();
        qos.length = 12;
        qos.impersonation_level = 2;
        qos.context_mode = 1;
        qos.effective_only = 0;
        this.object_attributes.security_quality_of_service = qos;
        this.ptype = 0;
        this.flags = 3;
    }
}
