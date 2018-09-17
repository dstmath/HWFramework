package jcifs.smb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.UnicodeString;
import jcifs.dcerpc.msrpc.LsaPolicyHandle;
import jcifs.dcerpc.msrpc.MsrpcEnumerateAliasesInDomain;
import jcifs.dcerpc.msrpc.MsrpcGetMembersInAlias;
import jcifs.dcerpc.msrpc.MsrpcLookupSids;
import jcifs.dcerpc.msrpc.MsrpcQueryInformationPolicy;
import jcifs.dcerpc.msrpc.SamrAliasHandle;
import jcifs.dcerpc.msrpc.SamrDomainHandle;
import jcifs.dcerpc.msrpc.SamrPolicyHandle;
import jcifs.dcerpc.msrpc.lsarpc.LsarDomainInfo;
import jcifs.dcerpc.msrpc.lsarpc.LsarSidArray;
import jcifs.dcerpc.msrpc.samr.SamrSamArray;
import jcifs.dcerpc.msrpc.samr.SamrSamEntry;
import jcifs.dcerpc.rpc.sid_t;
import jcifs.dcerpc.rpc.unicode_string;
import jcifs.util.Encdec;
import jcifs.util.Hexdump;

public class SID extends sid_t {
    public static SID CREATOR_OWNER = null;
    public static SID EVERYONE = null;
    public static final int SID_FLAG_RESOLVE_SIDS = 1;
    public static final int SID_TYPE_ALIAS = 4;
    public static final int SID_TYPE_DELETED = 6;
    public static final int SID_TYPE_DOMAIN = 3;
    public static final int SID_TYPE_DOM_GRP = 2;
    public static final int SID_TYPE_INVALID = 7;
    static final String[] SID_TYPE_NAMES = new String[]{"0", "User", "Domain group", "Domain", "Local group", "Builtin group", "Deleted", "Invalid", "Unknown"};
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
            case NtStatus.NT_STATUS_NONE_MAPPED /*-1073741709*/:
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
                            unicode_string ustr = rpc.domains.domains[rpc.names.names[si].sid_index].name;
                            sids[si].domainName = new UnicodeString(ustr, false).toString();
                            break;
                        default:
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
    /* JADX WARNING: Missing block: B:17:0x0063, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void resolveSids0(String authorityServerName, NtlmPasswordAuthentication auth, SID[] sids) throws IOException {
        Throwable th;
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
                            } catch (Throwable th2) {
                                th = th2;
                                policyHandle = policyHandle2;
                                throw th;
                            }
                        }
                        handle.close();
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
                        try {
                            policyHandle.close();
                        } catch (Throwable th5) {
                            th = th5;
                            throw th;
                        }
                    }
                    handle.close();
                }
                throw th;
            }
        }
    }

    public static void resolveSids(String authorityServerName, NtlmPasswordAuthentication auth, SID[] sids, int offset, int length) throws IOException {
        ArrayList list = new ArrayList(sids.length);
        synchronized (sid_cache) {
            int si;
            for (si = 0; si < length; si++) {
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
                sids = (SID[]) list.toArray(new SID[0]);
                resolveSids0(authorityServerName, auth, sids);
                for (si = 0; si < sids.length; si++) {
                    sid_cache.put(sids[si], sids[si]);
                }
            }
        }
    }

    public static void resolveSids(String authorityServerName, NtlmPasswordAuthentication auth, SID[] sids) throws IOException {
        ArrayList list = new ArrayList(sids.length);
        synchronized (sid_cache) {
            int si;
            for (si = 0; si < sids.length; si++) {
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
                sids = (SID[]) list.toArray(new SID[0]);
                resolveSids0(authorityServerName, auth, sids);
                for (si = 0; si < sids.length; si++) {
                    sid_cache.put(sids[si], sids[si]);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0048  */
    /* JADX WARNING: Missing block: B:28:0x0075, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static SID getServerSid(String server, NtlmPasswordAuthentication auth) throws IOException {
        Throwable th;
        DcerpcHandle handle = null;
        LsaPolicyHandle policyHandle = null;
        LsarDomainInfo info = new LsarDomainInfo();
        synchronized (sid_cache) {
            try {
                handle = DcerpcHandle.getHandle("ncacn_np:" + server + "[\\PIPE\\lsarpc]", auth);
                LsaPolicyHandle policyHandle2 = new LsaPolicyHandle(handle, null, 1);
                try {
                    MsrpcQueryInformationPolicy rpc = new MsrpcQueryInformationPolicy(policyHandle2, (short) 5, info);
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
                                policyHandle = policyHandle2;
                                throw th;
                            }
                        }
                        handle.close();
                    }
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

    public static byte[] toByteArray(sid_t sid) {
        byte[] dst = new byte[((sid.sub_authority_count * 4) + 8)];
        int i = 0 + 1;
        dst[0] = sid.revision;
        int di = i + 1;
        dst[i] = sid.sub_authority_count;
        System.arraycopy(sid.identifier_authority, 0, dst, di, 6);
        di += 6;
        for (byte ii = (byte) 0; ii < sid.sub_authority_count; ii++) {
            Encdec.enc_uint32le(sid.sub_authority[ii], dst, di);
            di += 4;
        }
        return dst;
    }

    public SID(byte[] src, int si) {
        int si2 = si + 1;
        this.revision = src[si];
        si = si2 + 1;
        this.sub_authority_count = src[si2];
        this.identifier_authority = new byte[6];
        System.arraycopy(src, si, this.identifier_authority, 0, 6);
        si += 6;
        if (this.sub_authority_count > (byte) 100) {
            throw new RuntimeException("Invalid SID sub_authority_count");
        }
        this.sub_authority = new int[this.sub_authority_count];
        for (byte i = (byte) 0; i < this.sub_authority_count; i++) {
            this.sub_authority[i] = ServerMessageBlock.readInt4(src, si);
            si += 4;
        }
    }

    public SID(String textual) throws SmbException {
        StringTokenizer st = new StringTokenizer(textual, "-");
        if (st.countTokens() < 3 || !st.nextToken().equals("S")) {
            throw new SmbException("Bad textual SID format: " + textual);
        }
        long id;
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
        if (this.sub_authority_count > (byte) 0) {
            this.sub_authority = new int[this.sub_authority_count];
            for (byte i2 = (byte) 0; i2 < this.sub_authority_count; i2++) {
                this.sub_authority[i2] = (int) (Long.parseLong(st.nextToken()) & 4294967295L);
            }
        }
    }

    public SID(SID domsid, int rid) {
        this.revision = domsid.revision;
        this.identifier_authority = domsid.identifier_authority;
        this.sub_authority_count = (byte) (domsid.sub_authority_count + 1);
        this.sub_authority = new int[this.sub_authority_count];
        byte i = (byte) 0;
        while (i < domsid.sub_authority_count) {
            this.sub_authority[i] = domsid.sub_authority[i];
            i++;
        }
        this.sub_authority[i] = rid;
    }

    public SID(sid_t sid, int type, String domainName, String acctName, boolean decrementAuthority) {
        this.revision = sid.revision;
        this.sub_authority_count = sid.sub_authority_count;
        this.identifier_authority = sid.identifier_authority;
        this.sub_authority = sid.sub_authority;
        this.type = type;
        this.domainName = domainName;
        this.acctName = acctName;
        if (decrementAuthority) {
            this.sub_authority_count = (byte) (this.sub_authority_count - 1);
            this.sub_authority = new int[this.sub_authority_count];
            for (byte i = (byte) 0; i < this.sub_authority_count; i++) {
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
        int hcode = this.identifier_authority[5];
        for (byte i = (byte) 0; i < this.sub_authority_count; i++) {
            hcode += 65599 * this.sub_authority[i];
        }
        return hcode;
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
        byte i = this.sub_authority_count;
        while (true) {
            byte i2 = i;
            i = i2 - 1;
            if (i2 <= (byte) 0) {
                for (int i3 = 0; i3 < 6; i3++) {
                    if (sid.identifier_authority[i3] != this.identifier_authority[i3]) {
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
        String ret = "S-" + (this.revision & 255) + "-";
        if (this.identifier_authority[0] == (byte) 0 && this.identifier_authority[1] == (byte) 0) {
            long shift = 0;
            long id = 0;
            for (int i = 5; i > 1; i--) {
                id += (((long) this.identifier_authority[i]) & 255) << ((int) shift);
                shift += 8;
            }
            ret = ret + id;
        } else {
            ret = (ret + "0x") + Hexdump.toHexString(this.identifier_authority, 0, 6);
        }
        for (byte i2 = (byte) 0; i2 < this.sub_authority_count; i2++) {
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

    void resolveWeak() {
        if (this.origin_server != null) {
            try {
                resolve(this.origin_server, this.origin_auth);
                this.origin_server = null;
            } catch (IOException e) {
                this.origin_server = null;
            } catch (Throwable th) {
                this.origin_server = null;
                this.origin_auth = null;
            }
            this.origin_auth = null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0033  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static SID[] getGroupMemberSids0(DcerpcHandle handle, SamrDomainHandle domainHandle, SID domsid, int rid, int flags) throws IOException {
        Throwable th;
        SamrAliasHandle aliasHandle = null;
        LsarSidArray sidarray = new LsarSidArray();
        try {
            MsrpcGetMembersInAlias rpc;
            SamrAliasHandle aliasHandle2 = new SamrAliasHandle(handle, domainHandle, 131084, rid);
            try {
                rpc = new MsrpcGetMembersInAlias(aliasHandle2, sidarray);
            } catch (Throwable th2) {
                th = th2;
                aliasHandle = aliasHandle2;
                if (aliasHandle != null) {
                }
                throw th;
            }
            try {
                handle.sendrecv(rpc);
                if (rpc.retval != 0) {
                    throw new SmbException(rpc.retval, false);
                }
                SID[] sids = new SID[rpc.sids.num_sids];
                String origin_server = handle.getServer();
                NtlmPasswordAuthentication origin_auth = (NtlmPasswordAuthentication) handle.getPrincipal();
                for (int i = 0; i < sids.length; i++) {
                    sids[i] = new SID(rpc.sids.sids[i].sid, 0, null, null, false);
                    sids[i].origin_server = origin_server;
                    sids[i].origin_auth = origin_auth;
                }
                if (sids.length > 0 && (flags & 1) != 0) {
                    resolveSids(origin_server, origin_auth, sids);
                }
                if (aliasHandle2 != null) {
                    aliasHandle2.close();
                }
                return sids;
            } catch (Throwable th3) {
                th = th3;
                MsrpcGetMembersInAlias msrpcGetMembersInAlias = rpc;
                aliasHandle = aliasHandle2;
                if (aliasHandle != null) {
                }
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            if (aliasHandle != null) {
                aliasHandle.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0064  */
    /* JADX WARNING: Can't wrap try/catch for R(5:21|22|23|24|25) */
    /* JADX WARNING: Missing block: B:35:0x0072, code:
            r6 = th;
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SID[] getGroupMemberSids(String authorityServerName, NtlmPasswordAuthentication auth, int flags) throws IOException {
        Throwable th;
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
                        SID[] groupMemberSids0 = getGroupMemberSids0(handle, domainHandle2, domsid, getRid(), flags);
                        if (handle != null) {
                            if (policyHandle2 != null) {
                                if (domainHandle2 != null) {
                                    try {
                                        domainHandle2.close();
                                    } catch (Throwable th2) {
                                        th = th2;
                                        domainHandle = domainHandle2;
                                        policyHandle = policyHandle2;
                                        throw th;
                                    }
                                }
                                policyHandle2.close();
                            }
                            handle.close();
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        domainHandle = domainHandle2;
                        policyHandle = policyHandle2;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    policyHandle = policyHandle2;
                    if (handle != null) {
                    }
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
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
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0084  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0084  */
    /* JADX WARNING: Missing block: B:47:0x0137, code:
            return r11;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static Map getLocalGroupsMap(String authorityServerName, NtlmPasswordAuthentication auth, int flags) throws IOException {
        Throwable th;
        SID domsid = getServerSid(authorityServerName, auth);
        DcerpcHandle handle = null;
        SamrPolicyHandle policyHandle = null;
        SamrDomainHandle domainHandle = null;
        SamrSamArray sam = new SamrSamArray();
        synchronized (sid_cache) {
            try {
                SamrDomainHandle domainHandle2;
                handle = DcerpcHandle.getHandle("ncacn_np:" + authorityServerName + "[\\PIPE\\samr]", auth);
                SamrPolicyHandle policyHandle2 = new SamrPolicyHandle(handle, authorityServerName, 33554432);
                try {
                    domainHandle2 = new SamrDomainHandle(handle, policyHandle2, 33554432, domsid);
                } catch (Throwable th2) {
                    th = th2;
                    policyHandle = policyHandle2;
                    if (handle != null) {
                    }
                    throw th;
                }
                try {
                    DcerpcMessage msrpcEnumerateAliasesInDomain = new MsrpcEnumerateAliasesInDomain(domainHandle2, 65535, sam);
                    handle.sendrecv(msrpcEnumerateAliasesInDomain);
                    if (msrpcEnumerateAliasesInDomain.retval != 0) {
                        throw new SmbException(msrpcEnumerateAliasesInDomain.retval, false);
                    }
                    Map map = new HashMap();
                    for (int ei = 0; ei < msrpcEnumerateAliasesInDomain.sam.count; ei++) {
                        SamrSamEntry entry = msrpcEnumerateAliasesInDomain.sam.entries[ei];
                        SID[] mems = getGroupMemberSids0(handle, domainHandle2, domsid, entry.idx, flags);
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
                            if (domainHandle2 != null) {
                                try {
                                    domainHandle2.close();
                                } catch (Throwable th3) {
                                    th = th3;
                                    domainHandle = domainHandle2;
                                    policyHandle = policyHandle2;
                                    throw th;
                                }
                            }
                            policyHandle2.close();
                        }
                        handle.close();
                    }
                } catch (Throwable th4) {
                    th = th4;
                    domainHandle = domainHandle2;
                    policyHandle = policyHandle2;
                    if (handle != null) {
                    }
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
                if (handle != null) {
                    if (policyHandle != null) {
                        if (domainHandle != null) {
                            try {
                                domainHandle.close();
                            } catch (Throwable th6) {
                                th = th6;
                                throw th;
                            }
                        }
                        policyHandle.close();
                    }
                    handle.close();
                }
                throw th;
            }
        }
    }
}
