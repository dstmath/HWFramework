package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.srvsvc;
import jcifs.smb.FileEntry;
import jcifs.smb.SmbShareInfo;

public class MsrpcShareEnum extends srvsvc.ShareEnumAll {

    class MsrpcShareInfo1 extends SmbShareInfo {
        MsrpcShareInfo1(srvsvc.ShareInfo1 info1) {
            this.netName = info1.netname;
            this.type = info1.type;
            this.remark = info1.remark;
        }
    }

    public MsrpcShareEnum(String server) {
        super("\\\\" + server, 1, new srvsvc.ShareInfoCtr1(), -1, 0, 0);
        this.ptype = 0;
        this.flags = 3;
    }

    public FileEntry[] getEntries() {
        srvsvc.ShareInfoCtr1 ctr = (srvsvc.ShareInfoCtr1) this.info;
        MsrpcShareInfo1[] entries = new MsrpcShareInfo1[ctr.count];
        for (int i = 0; i < ctr.count; i++) {
            entries[i] = new MsrpcShareInfo1(ctr.array[i]);
        }
        return entries;
    }
}
