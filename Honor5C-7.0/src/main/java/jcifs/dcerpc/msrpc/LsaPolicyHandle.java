package jcifs.dcerpc.msrpc;

import java.io.IOException;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.rpc.policy_handle;
import jcifs.smb.SmbException;

public class LsaPolicyHandle extends policy_handle {
    public LsaPolicyHandle(DcerpcHandle handle, String server, int access) throws IOException {
        if (server == null) {
            server = "\\\\";
        }
        MsrpcLsarOpenPolicy2 rpc = new MsrpcLsarOpenPolicy2(server, access, this);
        handle.sendrecv(rpc);
        if (rpc.retval != 0) {
            throw new SmbException(rpc.retval, false);
        }
    }

    public void close() throws IOException {
    }
}
