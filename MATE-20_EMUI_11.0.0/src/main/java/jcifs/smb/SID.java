package jcifs.smb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.UnicodeString;
import jcifs.dcerpc.msrpc.LsaPolicyHandle;
import jcifs.dcerpc.msrpc.MsrpcEnumerateAliasesInDomain;
import jcifs.dcerpc.msrpc.MsrpcGetMembersInAlias;
import jcifs.dcerpc.msrpc.MsrpcLookupSids;
import jcifs.dcerpc.msrpc.MsrpcQueryInformationPolicy;
import jcifs.dcerpc.msrpc.SamrAliasHandle;
import jcifs.dcerpc.msrpc.SamrDomainHandle;
import jcifs.dcerpc.msrpc.SamrPolicyHandle;
import jcifs.dcerpc.msrpc.lsarpc;
import jcifs.dcerpc.msrpc.samr;
import jcifs.dcerpc.rpc;
import jcifs.util.Encdec;
import jcifs.util.Hexdump;

public class SID extends rpc.sid_t {
    public static SID CREATOR_OWNER = null;
    public static SID EVERYONE = null;
    public static final int SID_FLAG_RESOLVE_SIDS = 1;
    public static final int SID_TYPE_ALIAS = 4;
    public static final int SID_TYPE_DELETED = 6;
    public static final int SID_TYPE_DOMAIN = 3;
    public static final int SID_TYPE_DOM_GRP = 2;
    public static final int SID_TYPE_INVALID = 7;
    static final String[] SID_TYPE_NAMES = {"0", "User", "Domain group", "Domain", "Local group", "Builtin group", "Deleted", "Invalid", "Unknown"};
    public static final int SID_TYPE_UNKNOWN = 8;
    public static final int SID_TYPE_USER = 1;
    public static final int SID_TYPE_USE_NONE = 0;
    public static final int SID_TYPE_WKN_GRP = 5;
    public static SID SYSTEM;
    static Map sid_cache = new HashMap();
    String acctName = null;
    String domainName = null;
    NtlmPasswordAuthentication origin_auth = null;
    String origin_server = null;
    int type;

    static {
        EVERYONE = null;
        CREATOR_OWNER = null;
        SYSTEM = null;
        try {
            EVERYONE = new SID("S-1-1-0");
            CREATOR_OWNER = new SID("S-1-3-0");
            SYSTEM = new SID("S-1-5-18");
        } catch (SmbException e) {
        }
    }

    static void resolveSids(DcerpcHandle handle, LsaPolicyHandle policyHandle, SID[] sids) throws IOException {
        MsrpcLookupSids rpc = new MsrpcLookupSids(policyHandle, sids);
        handle.sendrecv(rpc);
        switch (rpc.retval) {
            case NtStatus.NT_STATUS_NONE_MAPPED /* -1073741709 */:
            case 0:
            case 263:
                for (int si = 0; si < sids.length; si++) {
                    sids[si].type = rpc.names.names[si].sid_type;
                    sids[si].domainName = null;
                    switch (sids[si].type) {
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                            rpc.unicode_string ustr = rpc.domains.domains[rpc.names.names[si].sid_index].name;
                            sids[si].domainName = new UnicodeString(ustr, false).toString();
                            break;
                    }
                    sids[si].acctName = new UnicodeString(rpc.names.names[si].name, false).toString();
                    sids[si].origin_server = null;
                    sids[si].origin_auth = null;
                }
                return;
            default:
                throw new SmbException(rpc.retval, false);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0067  */
    static void resolveSids0(String authorityServerName, NtlmPasswordAuthentication auth, SID[] sids) throws IOException {
        Throwable th;
        Throwable th2;
        DcerpcHandle handle = null;
        LsaPolicyHandle policyHandle = null;
        synchronized (sid_cache) {
            try {
                handle = DcerpcHandle.getHandle("ncacn_np:" + authorityServerName + "[\\PIPE\\lsarpc]", auth);
                String server = authorityServerName;
                int dot = server.indexOf(46);
                if (dot > 0 && !Character.isDigit(server.charAt(0))) {
                    server = server.substring(0, dot);
                }
                LsaPolicyHandle policyHandle2 = new LsaPolicyHandle(handle, "\\\\" + server, 2048);
                try {
                    resolveSids(handle, policyHandle2, sids);
                    if (handle != null) {
                        if (policyHandle2 != null) {
                            try {
                                policyHandle2.close();
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        }
                        handle.close();
                    }
                } catch (Throwable th4) {
                    th2 = th4;
                    policyHandle = policyHandle2;
                    if (handle != null) {
                    }
                    throw th2;
                }
            } catch (Throwable th5) {
                th2 = th5;
                if (handle != null) {
                    if (policyHandle != null) {
                        try {
                            policyHandle.close();
                        } catch (Throwable th6) {
                            th = th6;
                            throw th;
                        }
                    }
                    handle.close();
                }
                throw th2;
            }
        }
    }

    public static void resolveSids(String authorityServerName, NtlmPasswordAuthentication auth, SID[] sids, int offset, int length) throws IOException {
        ArrayList list = new ArrayList(sids.length);
        synchronized (sid_cache) {
            for (int si = 0; si < length; si++) {
                SID sid = (SID) sid_cache.get(sids[offset + si]);
                if (sid != null) {
                    sids[offset + si].type = sid.type;
                    sids[offset + si].domainName = sid.domainName;
                    sids[offset + si].acctName = sid.acctName;
                } else {
                    list.add(sids[offset + si]);
                }
            }
            if (list.size() > 0) {
                SID[] sids2 = (SID[]) list.toArray(new SID[0]);
                resolveSids0(authorityServerName, auth, sids2);
                for (int si2 = 0; si2 < sids2.length; si2++) {
                    sid_cache.put(sids2[si2], sids2[si2]);
                }
            }
        }
    }

    public static void resolveSids(String authorityServerName, NtlmPasswordAuthentication auth, SID[] sids) throws IOException {
        ArrayList list = new ArrayList(sids.length);
        synchronized (sid_cache) {
            for (int si = 0; si < sids.length; si++) {
                SID sid = (SID) sid_cache.get(sids[si]);
                if (sid != null) {
                    sids[si].type = sid.type;
                    sids[si].domainName = sid.domainName;
                    sids[si].acctName = sid.acctName;
                } else {
                    list.add(sids[si]);
                }
            }
            if (list.size() > 0) {
                SID[] sids2 = (SID[]) list.toArray(new SID[0]);
                resolveSids0(authorityServerName, auth, sids2);
                for (int si2 = 0; si2 < sids2.length; si2++) {
                    sid_cache.put(sids2[si2], sids2[si2]);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0048  */
    public static SID getServerSid(String server, NtlmPasswordAuthentication auth) throws IOException {
        Throwable th;
        DcerpcHandle handle = null;
        LsaPolicyHandle policyHandle = null;
        lsarpc.LsarDomainInfo info = new lsarpc.LsarDomainInfo();
        synchronized (sid_cache) {
            try {
                handle = DcerpcHandle.getHandle("ncacn_np:" + server + "[\\PIPE\\lsarpc]", auth);
                LsaPolicyHandle policyHandle2 = new LsaPolicyHandle(handle, null, 1);
                try {
                    MsrpcQueryInformationPolicy rpc = new MsrpcQueryInformationPolicy(policyHandle2, 5, info);
                    handle.sendrecv(rpc);
                    if (rpc.retval != 0) {
                        throw new SmbException(rpc.retval, false);
                    }
                    SID sid = new SID(info.sid, 3, new UnicodeString(info.name, false).toString(), null, false);
                    if (handle != null) {
                        if (policyHandle2 != null) {
                            try {
                                policyHandle2.close();
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        handle.close();
                    }
                    return sid;
                } catch (Throwable th3) {
                    th = th3;
                    policyHandle = policyHandle2;
                    if (handle != null) {
                        if (policyHandle != null) {
                            try {
                                policyHandle.close();
                            } catch (Throwable th4) {
                                th = th4;
                                throw th;
                            }
                        }
                        handle.close();
                    }
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
                if (handle != null) {
                }
                throw th;
            }
        }
    }

    public static byte[] toByteArray(rpc.sid_t sid) {
        byte[] dst = new byte[((sid.sub_authority_count * 4) + 8)];
        int di = 0 + 1;
        dst[0] = sid.revision;
        int di2 = di + 1;
        dst[di] = sid.sub_authority_count;
        System.arraycopy(sid.identifier_authority, 0, dst, di2, 6);
        int di3 = di2 + 6;
        for (int ii = 0; ii < sid.sub_authority_count; ii++) {
            Encdec.enc_uint32le(sid.sub_authority[ii], dst, di3);
            di3 += 4;
        }
        return dst;
    }

    public SID(byte[] src, int si) {
        int si2 = si + 1;
        this.revision = src[si];
        int si3 = si2 + 1;
        this.sub_authority_count = src[si2];
        this.identifier_authority = new byte[6];
        System.arraycopy(src, si3, this.identifier_authority, 0, 6);
        int si4 = si3 + 6;
        if (this.sub_authority_count > 100) {
            throw new RuntimeException("Invalid SID sub_authority_count");
        }
        this.sub_authority = new int[this.sub_authority_count];
        for (int i = 0; i < this.sub_authority_count; i++) {
            this.sub_authority[i] = ServerMessageBlock.readInt4(src, si4);
            si4 += 4;
        }
    }

    public SID(String textual) throws SmbException {
        long id;
        StringTokenizer st = new StringTokenizer(textual, "-");
        if (st.countTokens() < 3 || !st.nextToken().equals("S")) {
            throw new SmbException("Bad textual SID format: " + textual);
        }
        this.revision = Byte.parseByte(st.nextToken());
        String tmp = st.nextToken();
        if (tmp.startsWith("0x")) {
            id = Long.parseLong(tmp.substring(2), 16);
        } else {
            id = Long.parseLong(tmp);
        }
        this.identifier_authority = new byte[6];
        int i = 5;
        while (id > 0) {
            this.identifier_authority[i] = (byte) ((int) (id % 256));
            id >>= 8;
            i--;
        }
        this.sub_authority_count = (byte) st.countTokens();
        if (this.sub_authority_count > 0) {
            this.sub_authority = new int[this.sub_authority_count];
            for (int i2 = 0; i2 < this.sub_authority_count; i2++) {
                this.sub_authority[i2] = (int) (Long.parseLong(st.nextToken()) & 4294967295L);
            }
        }
    }

    public SID(SID domsid, int rid) {
        this.revision = domsid.revision;
        this.identifier_authority = domsid.identifier_authority;
        this.sub_authority_count = (byte) (domsid.sub_authority_count + 1);
        this.sub_authority = new int[this.sub_authority_count];
        int i = 0;
        while (i < domsid.sub_authority_count) {
            this.sub_authority[i] = domsid.sub_authority[i];
            i++;
        }
        this.sub_authority[i] = rid;
    }

    public SID(rpc.sid_t sid, int type2, String domainName2, String acctName2, boolean decrementAuthority) {
        this.revision = sid.revision;
        this.sub_authority_count = sid.sub_authority_count;
        this.identifier_authority = sid.identifier_authority;
        this.sub_authority = sid.sub_authority;
        this.type = type2;
        this.domainName = domainName2;
        this.acctName = acctName2;
        if (decrementAuthority) {
            this.sub_authority_count = (byte) (this.sub_authority_count - 1);
            this.sub_authority = new int[this.sub_authority_count];
            for (int i = 0; i < this.sub_authority_count; i++) {
                this.sub_authority[i] = sid.sub_authority[i];
            }
        }
    }

    public SID getDomainSid() {
        return new SID(this, 3, this.domainName, null, getType() != 3);
    }

    public int getRid() {
        if (getType() != 3) {
            return this.sub_authority[this.sub_authority_count - 1];
        }
        throw new IllegalArgumentException("This SID is a domain sid");
    }

    public int getType() {
        if (this.origin_server != null) {
            resolveWeak();
        }
        return this.type;
    }

    public String getTypeText() {
        if (this.origin_server != null) {
            resolveWeak();
        }
        return SID_TYPE_NAMES[this.type];
    }

    public String getDomainName() {
        if (this.origin_server != null) {
            resolveWeak();
        }
        if (this.type != 8) {
            return this.domainName;
        }
        String full = toString();
        return full.substring(0, (full.length() - getAccountName().length()) - 1);
    }

    public String getAccountName() {
        if (this.origin_server != null) {
            resolveWeak();
        }
        if (this.type == 8) {
            return "" + this.sub_authority[this.sub_authority_count - 1];
        }
        if (this.type == 3) {
            return "";
        }
        return this.acctName;
    }

    public int hashCode() {
        int i = 0;
        int hcode = this.identifier_authority[5];
        while (i < this.sub_authority_count) {
            i++;
            hcode += 65599 * this.sub_authority[i];
        }
        return hcode == 1 ? 1 : 0;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (!(obj instanceof SID)) {
            return false;
        }
        SID sid = (SID) obj;
        if (sid == this) {
            return true;
        }
        if (sid.sub_authority_count != this.sub_authority_count) {
            return false;
        }
        int i = this.sub_authority_count;
        while (true) {
            i--;
            if (i <= 0) {
                for (int i2 = 0; i2 < 6; i2++) {
                    if (sid.identifier_authority[i2] != this.identifier_authority[i2]) {
                        return false;
                    }
                }
                if (sid.revision != this.revision) {
                    z = false;
                }
                return z;
            } else if (sid.sub_authority[i] != this.sub_authority[i]) {
                return false;
            }
        }
    }

    public String toString() {
        String ret;
        String ret2 = "S-" + (this.revision & 255) + "-";
        if (this.identifier_authority[0] == 0 && this.identifier_authority[1] == 0) {
            long shift = 0;
            long id = 0;
            for (int i = 5; i > 1; i--) {
                id += (((long) this.identifier_authority[i]) & 255) << ((int) shift);
                shift += 8;
            }
            ret = ret2 + id;
        } else {
            ret = (ret2 + "0x") + Hexdump.toHexString(this.identifier_authority, 0, 6);
        }
        for (int i2 = 0; i2 < this.sub_authority_count; i2++) {
            ret = ret + "-" + (((long) this.sub_authority[i2]) & 4294967295L);
        }
        return ret;
    }

    public String toDisplayString() {
        if (this.origin_server != null) {
            resolveWeak();
        }
        if (this.domainName == null) {
            return toString();
        }
        if (this.type == 3) {
            return this.domainName;
        }
        if (this.type != 5 && !this.domainName.equals("BUILTIN")) {
            return this.domainName + "\\" + this.acctName;
        }
        if (this.type == 8) {
            return toString();
        }
        return this.acctName;
    }

    public void resolve(String authorityServerName, NtlmPasswordAuthentication auth) throws IOException {
        resolveSids(authorityServerName, auth, new SID[]{this});
    }

    /* access modifiers changed from: package-private */
    public void resolveWeak() {
        if (this.origin_server != null) {
            try {
                resolve(this.origin_server, this.origin_auth);
                this.origin_server = null;
            } catch (IOException e) {
                this.origin_server = null;
            } catch (Throwable th) {
                this.origin_server = null;
                this.origin_auth = null;
                throw th;
            }
            this.origin_auth = null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0033  */
    static SID[] getGroupMemberSids0(DcerpcHandle handle, SamrDomainHandle domainHandle, SID domsid, int rid, int flags) throws IOException {
        SamrAliasHandle aliasHandle = null;
        lsarpc.LsarSidArray sidarray = new lsarpc.LsarSidArray();
        try {
            SamrAliasHandle aliasHandle2 = new SamrAliasHandle(handle, domainHandle, 131084, rid);
            try {
                MsrpcGetMembersInAlias rpc = new MsrpcGetMembersInAlias(aliasHandle2, sidarray);
                try {
                    handle.sendrecv(rpc);
                    if (rpc.retval != 0) {
                        throw new SmbException(rpc.retval, false);
                    }
                    SID[] sids = new SID[rpc.sids.num_sids];
                    String origin_server2 = handle.getServer();
                    NtlmPasswordAuthentication origin_auth2 = (NtlmPasswordAuthentication) handle.getPrincipal();
                    for (int i = 0; i < sids.length; i++) {
                        sids[i] = new SID(rpc.sids.sids[i].sid, 0, null, null, false);
                        sids[i].origin_server = origin_server2;
                        sids[i].origin_auth = origin_auth2;
                    }
                    if (sids.length > 0 && (flags & 1) != 0) {
                        resolveSids(origin_server2, origin_auth2, sids);
                    }
                    if (aliasHandle2 != null) {
                        aliasHandle2.close();
                    }
                    return sids;
                } catch (Throwable th) {
                    th = th;
                    aliasHandle = aliasHandle2;
                    if (aliasHandle != null) {
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                aliasHandle = aliasHandle2;
                if (aliasHandle != null) {
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (aliasHandle != null) {
                aliasHandle.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0072, code lost:
        r6 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0064  */
    public SID[] getGroupMemberSids(String authorityServerName, NtlmPasswordAuthentication auth, int flags) throws IOException {
        Throwable th;
        SID[] groupMemberSids0;
        if (this.type != 2 && this.type != 4) {
            return new SID[0];
        }
        DcerpcHandle handle = null;
        SamrPolicyHandle policyHandle = null;
        SamrDomainHandle domainHandle = null;
        SID domsid = getDomainSid();
        synchronized (sid_cache) {
            try {
                handle = DcerpcHandle.getHandle("ncacn_np:" + authorityServerName + "[\\PIPE\\samr]", auth);
                SamrPolicyHandle policyHandle2 = new SamrPolicyHandle(handle, authorityServerName, 48);
                try {
                    SamrDomainHandle domainHandle2 = new SamrDomainHandle(handle, policyHandle2, 512, domsid);
                    try {
                        groupMemberSids0 = getGroupMemberSids0(handle, domainHandle2, domsid, getRid(), flags);
                        if (handle != null) {
                            if (policyHandle2 != null) {
                                if (domainHandle2 != null) {
                                    domainHandle2.close();
                                }
                                policyHandle2.close();
                            }
                            handle.close();
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        domainHandle = domainHandle2;
                        policyHandle = policyHandle2;
                        if (handle != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    policyHandle = policyHandle2;
                    if (handle != null) {
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                if (handle != null) {
                    if (policyHandle != null) {
                        if (domainHandle != null) {
                            domainHandle.close();
                        }
                        policyHandle.close();
                    }
                    handle.close();
                }
                throw th;
            }
        }
        return groupMemberSids0;
        while (true) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0084  */
    static Map getLocalGroupsMap(String authorityServerName, NtlmPasswordAuthentication auth, int flags) throws IOException {
        Throwable th;
        SamrDomainHandle domainHandle;
        SID domsid = getServerSid(authorityServerName, auth);
        DcerpcHandle handle = null;
        SamrPolicyHandle policyHandle = null;
        SamrDomainHandle domainHandle2 = null;
        samr.SamrSamArray sam = new samr.SamrSamArray();
        synchronized (sid_cache) {
            try {
                handle = DcerpcHandle.getHandle("ncacn_np:" + authorityServerName + "[\\PIPE\\samr]", auth);
                SamrPolicyHandle policyHandle2 = new SamrPolicyHandle(handle, authorityServerName, 33554432);
                try {
                    domainHandle = new SamrDomainHandle(handle, policyHandle2, 33554432, domsid);
                } catch (Throwable th2) {
                    th = th2;
                    policyHandle = policyHandle2;
                    if (handle != null) {
                        if (policyHandle != null) {
                            if (domainHandle2 != null) {
                                try {
                                    domainHandle2.close();
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
                            }
                            policyHandle.close();
                        }
                        handle.close();
                    }
                    throw th;
                }
                try {
                    MsrpcEnumerateAliasesInDomain rpc = new MsrpcEnumerateAliasesInDomain(domainHandle, 65535, sam);
                    handle.sendrecv(rpc);
                    if (rpc.retval != 0) {
                        throw new SmbException(rpc.retval, false);
                    }
                    Map map = new HashMap();
                    for (int ei = 0; ei < rpc.sam.count; ei++) {
                        samr.SamrSamEntry entry = rpc.sam.entries[ei];
                        SID[] mems = getGroupMemberSids0(handle, domainHandle, domsid, entry.idx, flags);
                        SID groupSid = new SID(domsid, entry.idx);
                        groupSid.type = 4;
                        groupSid.domainName = domsid.getDomainName();
                        groupSid.acctName = new UnicodeString(entry.name, false).toString();
                        for (int mi = 0; mi < mems.length; mi++) {
                            ArrayList groups = (ArrayList) map.get(mems[mi]);
                            if (groups == null) {
                                groups = new ArrayList();
                                map.put(mems[mi], groups);
                            }
                            if (!groups.contains(groupSid)) {
                                groups.add(groupSid);
                            }
                        }
                    }
                    if (handle != null) {
                        if (policyHandle2 != null) {
                            if (domainHandle != null) {
                                try {
                                    domainHandle.close();
                                } catch (Throwable th4) {
                                    th = th4;
                                    throw th;
                                }
                            }
                            policyHandle2.close();
                        }
                        handle.close();
                    }
                    return map;
                } catch (Throwable th5) {
                    th = th5;
                    domainHandle2 = domainHandle;
                    policyHandle = policyHandle2;
                    if (handle != null) {
                    }
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                if (handle != null) {
                }
                throw th;
            }
        }
    }
}
