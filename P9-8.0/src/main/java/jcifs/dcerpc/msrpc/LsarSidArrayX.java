package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.msrpc.lsarpc.LsarSidArray;
import jcifs.dcerpc.msrpc.lsarpc.LsarSidPtr;
import jcifs.smb.SID;

class LsarSidArrayX extends LsarSidArray {
    LsarSidArrayX(SID[] sids) {
        this.num_sids = sids.length;
        this.sids = new LsarSidPtr[sids.length];
        for (int si = 0; si < sids.length; si++) {
            this.sids[si] = new LsarSidPtr();
            this.sids[si].sid = sids[si];
        }
    }
}
