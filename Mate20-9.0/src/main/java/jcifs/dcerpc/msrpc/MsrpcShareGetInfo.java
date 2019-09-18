package jcifs.dcerpc.msrpc;

import java.io.IOException;
import jcifs.dcerpc.msrpc.srvsvc;
import jcifs.smb.ACE;
import jcifs.smb.SecurityDescriptor;

public class MsrpcShareGetInfo extends srvsvc.ShareGetInfo {
    public MsrpcShareGetInfo(String server, String sharename) {
        super(server, sharename, 502, new srvsvc.ShareInfo502());
        this.ptype = 0;
        this.flags = 3;
    }

    public ACE[] getSecurity() throws IOException {
        srvsvc.ShareInfo502 info502 = (srvsvc.ShareInfo502) this.info;
        if (info502.security_descriptor != null) {
            return new SecurityDescriptor(info502.security_descriptor, 0, info502.sd_size).aces;
        }
        return null;
    }
}
