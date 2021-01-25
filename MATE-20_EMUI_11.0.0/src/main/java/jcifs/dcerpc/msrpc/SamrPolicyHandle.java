package jcifs.dcerpc.msrpc;

import java.io.IOException;
import jcifs.dcerpc.DcerpcException;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.rpc;

public class SamrPolicyHandle extends rpc.policy_handle {
    public SamrPolicyHandle(DcerpcHandle handle, String server, int access) throws IOException {
        server = server == null ? "\\\\" : server;
        try {
            handle.sendrecv(new MsrpcSamrConnect4(server, access, this));
        } catch (DcerpcException de) {
            if (de.getErrorCode() != 469827586) {
                throw de;
            }
            handle.sendrecv(new MsrpcSamrConnect2(server, access, this));
        }
    }

    public void close() throws IOException {
    }
}
