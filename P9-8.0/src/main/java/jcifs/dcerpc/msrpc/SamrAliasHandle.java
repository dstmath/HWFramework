package jcifs.dcerpc.msrpc;

import java.io.IOException;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.rpc.policy_handle;
import jcifs.smb.SmbException;

public class SamrAliasHandle extends policy_handle {
    public SamrAliasHandle(DcerpcHandle handle, SamrDomainHandle domainHandle, int access, int rid) throws IOException {
        MsrpcSamrOpenAlias rpc = new MsrpcSamrOpenAlias(domainHandle, access, rid, this);
        handle.sendrecv(rpc);
        if (rpc.retval != 0) {
            throw new SmbException(rpc.retval, false);
        }
    }

    public void close() throws IOException {
    }
}
