package jcifs.dcerpc.msrpc;

import java.io.IOException;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.rpc.policy_handle;
import jcifs.dcerpc.rpc.sid_t;
import jcifs.smb.SmbException;

public class SamrDomainHandle extends policy_handle {
    public SamrDomainHandle(DcerpcHandle handle, SamrPolicyHandle policyHandle, int access, sid_t sid) throws IOException {
        MsrpcSamrOpenDomain rpc = new MsrpcSamrOpenDomain(policyHandle, access, sid, this);
        handle.sendrecv(rpc);
        if (rpc.retval != 0) {
            throw new SmbException(rpc.retval, false);
        }
    }

    public void close() throws IOException {
    }
}
