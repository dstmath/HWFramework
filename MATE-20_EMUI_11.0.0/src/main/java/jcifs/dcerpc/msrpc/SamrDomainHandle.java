package jcifs.dcerpc.msrpc;

import java.io.IOException;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.rpc;
import jcifs.smb.SmbException;

public class SamrDomainHandle extends rpc.policy_handle {
    public SamrDomainHandle(DcerpcHandle handle, SamrPolicyHandle policyHandle, int access, rpc.sid_t sid) throws IOException {
        MsrpcSamrOpenDomain rpc = new MsrpcSamrOpenDomain(policyHandle, access, sid, this);
        handle.sendrecv(rpc);
        if (rpc.retval != 0) {
            throw new SmbException(rpc.retval, false);
        }
    }

    public void close() throws IOException {
    }
}
