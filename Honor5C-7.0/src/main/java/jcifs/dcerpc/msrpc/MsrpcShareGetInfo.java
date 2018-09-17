package jcifs.dcerpc.msrpc;

import java.io.IOException;
import jcifs.dcerpc.msrpc.srvsvc.ShareGetInfo;
import jcifs.dcerpc.msrpc.srvsvc.ShareInfo502;
import jcifs.smb.ACE;
import jcifs.smb.SecurityDescriptor;

public class MsrpcShareGetInfo extends ShareGetInfo {
    public MsrpcShareGetInfo(String server, String sharename) {
        super(server, sharename, 502, new ShareInfo502());
        this.ptype = 0;
        this.flags = 3;
    }

    public ACE[] getSecurity() throws IOException {
        ShareInfo502 info502 = this.info;
        if (info502.security_descriptor != null) {
            return new SecurityDescriptor(info502.security_descriptor, 0, info502.sd_size).aces;
        }
        return null;
    }
}
