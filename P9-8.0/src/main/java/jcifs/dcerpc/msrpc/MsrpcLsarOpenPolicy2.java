package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.lsarpc.LsarObjectAttributes;
import jcifs.dcerpc.msrpc.lsarpc.LsarOpenPolicy2;
import jcifs.dcerpc.msrpc.lsarpc.LsarQosInfo;

public class MsrpcLsarOpenPolicy2 extends LsarOpenPolicy2 {
    public MsrpcLsarOpenPolicy2(String server, int access, LsaPolicyHandle policyHandle) {
        super(server, new LsarObjectAttributes(), access, policyHandle);
        LsarQosInfo qos = new LsarQosInfo();
        qos.length = 12;
        qos.impersonation_level = (short) 2;
        qos.context_mode = (byte) 1;
        qos.effective_only = (byte) 0;
        this.object_attributes.security_quality_of_service = qos;
        this.ptype = 0;
        this.flags = 3;
    }
}
