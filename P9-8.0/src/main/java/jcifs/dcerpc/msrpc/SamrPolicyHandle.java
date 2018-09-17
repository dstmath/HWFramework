package jcifs.dcerpc.msrpc;

import java.io.IOException;
import jcifs.dcerpc.DcerpcError;
import jcifs.dcerpc.DcerpcException;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.rpc.policy_handle;

public class SamrPolicyHandle extends policy_handle {
    public SamrPolicyHandle(DcerpcHandle handle, String server, int access) throws IOException {
        if (server == null) {
            server = "\\\\";
        }
        try {
            handle.sendrecv(new MsrpcSamrConnect4(server, access, this));
        } catch (DcerpcException de) {
            if (de.getErrorCode() != DcerpcError.DCERPC_FAULT_OP_RNG_ERROR) {
                throw de;
            }
            handle.sendrecv(new MsrpcSamrConnect2(server, access, this));
        }
    }

    public void close() throws IOException {
    }
}
