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
    static final String[] SID_TYPE_NAMES = null;
    public static final int SID_TYPE_UNKNOWN = 8;
    public static final int SID_TYPE_USER = 1;
    public static final int SID_TYPE_USE_NONE = 0;
    public static final int SID_TYPE_WKN_GRP = 5;
    public static SID SYSTEM;
    static Map sid_cache;
    String acctName;
    String domainName;
    NtlmPasswordAuthentication origin_auth;
    String origin_server;
    int type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.SID.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.SID.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.SID.<clinit>():void");
    }

    static void resolveSids(DcerpcHandle handle, LsaPolicyHandle policyHandle, SID[] sids) throws IOException {
        MsrpcLookupSids rpc = new MsrpcLookupSids(policyHandle, sids);
        handle.sendrecv(rpc);
        switch (rpc.retval) {
            case NtStatus.NT_STATUS_NONE_MAPPED /*-1073741709*/:
            case SID_TYPE_USE_NONE /*0*/:
            case 263:
                for (int si = SID_TYPE_USE_NONE; si < sids.length; si += SID_TYPE_USER) {
                    sids[si].type = rpc.names.names[si].sid_type;
                    sids[si].domainName = null;
                    switch (sids[si].type) {
                        case SID_TYPE_USER /*1*/:
                        case SID_TYPE_DOM_GRP /*2*/:
                        case SID_TYPE_DOMAIN /*3*/:
                        case SID_TYPE_ALIAS /*4*/:
                        case SID_TYPE_WKN_GRP /*5*/:
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
            default:
                throw new SmbException(rpc.retval, false);
        }
    }

    static void resolveSids0(String authorityServerName, NtlmPasswordAuthentication auth, SID[] sids) throws IOException {
        Throwable th;
        DcerpcHandle handle = null;
        LsaPolicyHandle policyHandle = null;
        synchronized (sid_cache) {
            try {
                handle = DcerpcHandle.getHandle("ncacn_np:" + authorityServerName + "[\\PIPE\\lsarpc]", auth);
                String server = authorityServerName;
                int dot = server.indexOf(46);
                if (dot > 0 && !Character.isDigit(server.charAt(SID_TYPE_USE_NONE))) {
                    server = server.substring(SID_TYPE_USE_NONE, dot);
                }
                LsaPolicyHandle policyHandle2 = new LsaPolicyHandle(handle, "\\\\" + server, SmbConstants.FLAGS2_EXTENDED_SECURITY_NEGOTIATION);
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
                    if (policyHandle != null) {
                        policyHandle.close();
                    }
                    handle.close();
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void resolveSids(String authorityServerName, NtlmPasswordAuthentication auth, SID[] sids, int offset, int length) throws IOException {
        ArrayList list = new ArrayList(sids.length);
        synchronized (sid_cache) {
            int si;
            for (si = SID_TYPE_USE_NONE; si < length; si += SID_TYPE_USER) {
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
                sids = (SID[]) list.toArray(new SID[SID_TYPE_USE_NONE]);
                resolveSids0(authorityServerName, auth, sids);
                for (si = SID_TYPE_USE_NONE; si < sids.length; si += SID_TYPE_USER) {
                    sid_cache.put(sids[si], sids[si]);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void resolveSids(String authorityServerName, NtlmPasswordAuthentication auth, SID[] sids) throws IOException {
        ArrayList list = new ArrayList(sids.length);
        synchronized (sid_cache) {
            int si = SID_TYPE_USE_NONE;
            while (true) {
                if (si < sids.length) {
                    SID sid = (SID) sid_cache.get(sids[si]);
                    if (sid != null) {
                        sids[si].type = sid.type;
                        sids[si].domainName = sid.domainName;
                        sids[si].acctName = sid.acctName;
                    } else {
                        list.add(sids[si]);
                    }
                    si += SID_TYPE_USER;
                } else {
                    if (list.size() > 0) {
                        sids = (SID[]) list.toArray(new SID[SID_TYPE_USE_NONE]);
                        resolveSids0(authorityServerName, auth, sids);
                        for (si = SID_TYPE_USE_NONE; si < sids.length; si += SID_TYPE_USER) {
                            sid_cache.put(sids[si], sids[si]);
                        }
                    }
                }
            }
        }
    }

    public static SID getServerSid(String server, NtlmPasswordAuthentication auth) throws IOException {
        Throwable th;
        DcerpcHandle handle = null;
        LsaPolicyHandle policyHandle = null;
        LsarDomainInfo info = new LsarDomainInfo();
        synchronized (sid_cache) {
            try {
                handle = DcerpcHandle.getHandle("ncacn_np:" + server + "[\\PIPE\\lsarpc]", auth);
                LsaPolicyHandle policyHandle2 = new LsaPolicyHandle(handle, null, SID_TYPE_USER);
                try {
                    MsrpcQueryInformationPolicy rpc = new MsrpcQueryInformationPolicy(policyHandle2, (short) 5, info);
                    handle.sendrecv(rpc);
                    if (rpc.retval != 0) {
                        throw new SmbException(rpc.retval, false);
                    }
                    SID sid = new SID(info.sid, SID_TYPE_DOMAIN, new UnicodeString(info.name, false).toString(), null, false);
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
                    if (policyHandle != null) {
                        policyHandle.close();
                    }
                    handle.close();
                }
                throw th;
            }
        }
    }

    public static byte[] toByteArray(sid_t sid) {
        byte[] dst = new byte[((sid.sub_authority_count * SID_TYPE_ALIAS) + SID_TYPE_UNKNOWN)];
        int i = SID_TYPE_USE_NONE + SID_TYPE_USER;
        dst[SID_TYPE_USE_NONE] = sid.revision;
        int di = i + SID_TYPE_USER;
        dst[i] = sid.sub_authority_count;
        System.arraycopy(sid.identifier_authority, SID_TYPE_USE_NONE, dst, di, SID_TYPE_DELETED);
        di += SID_TYPE_DELETED;
        for (byte ii = (byte) 0; ii < sid.sub_authority_count; ii += SID_TYPE_USER) {
            Encdec.enc_uint32le(sid.sub_authority[ii], dst, di);
            di += SID_TYPE_ALIAS;
        }
        return dst;
    }

    public SID(byte[] src, int si) {
        this.domainName = null;
        this.acctName = null;
        this.origin_server = null;
        this.origin_auth = null;
        int si2 = si + SID_TYPE_USER;
        this.revision = src[si];
        si = si2 + SID_TYPE_USER;
        this.sub_authority_count = src[si2];
        this.identifier_authority = new byte[SID_TYPE_DELETED];
        System.arraycopy(src, si, this.identifier_authority, SID_TYPE_USE_NONE, SID_TYPE_DELETED);
        si += SID_TYPE_DELETED;
        if (this.sub_authority_count > 100) {
            throw new RuntimeException("Invalid SID sub_authority_count");
        }
        this.sub_authority = new int[this.sub_authority_count];
        for (byte i = (byte) 0; i < this.sub_authority_count; i += SID_TYPE_USER) {
            this.sub_authority[i] = ServerMessageBlock.readInt4(src, si);
            si += SID_TYPE_ALIAS;
        }
    }

    public SID(String textual) throws SmbException {
        this.domainName = null;
        this.acctName = null;
        this.origin_server = null;
        this.origin_auth = null;
        StringTokenizer st = new StringTokenizer(textual, "-");
        if (st.countTokens() < SID_TYPE_DOMAIN || !st.nextToken().equals("S")) {
            throw new SmbException("Bad textual SID format: " + textual);
        }
        long id;
        this.revision = Byte.parseByte(st.nextToken());
        String tmp = st.nextToken();
        if (tmp.startsWith("0x")) {
            id = Long.parseLong(tmp.substring(SID_TYPE_DOM_GRP), 16);
        } else {
            id = Long.parseLong(tmp);
        }
        this.identifier_authority = new byte[SID_TYPE_DELETED];
        int i = SID_TYPE_WKN_GRP;
        while (id > 0) {
            this.identifier_authority[i] = (byte) ((int) (id % 256));
            id >>= SID_TYPE_UNKNOWN;
            i--;
        }
        this.sub_authority_count = (byte) st.countTokens();
        if (this.sub_authority_count > null) {
            this.sub_authority = new int[this.sub_authority_count];
            for (byte i2 = (byte) 0; i2 < this.sub_authority_count; i2 += SID_TYPE_USER) {
                this.sub_authority[i2] = (int) (Long.parseLong(st.nextToken()) & 4294967295L);
            }
        }
    }

    public SID(SID domsid, int rid) {
        this.domainName = null;
        this.acctName = null;
        this.origin_server = null;
        this.origin_auth = null;
        this.revision = domsid.revision;
        this.identifier_authority = domsid.identifier_authority;
        this.sub_authority_count = (byte) (domsid.sub_authority_count + SID_TYPE_USER);
        this.sub_authority = new int[this.sub_authority_count];
        byte i = (byte) 0;
        while (i < domsid.sub_authority_count) {
            this.sub_authority[i] = domsid.sub_authority[i];
            i += SID_TYPE_USER;
        }
        this.sub_authority[i] = rid;
    }

    public SID(sid_t sid, int type, String domainName, String acctName, boolean decrementAuthority) {
        this.domainName = null;
        this.acctName = null;
        this.origin_server = null;
        this.origin_auth = null;
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
            for (byte i = (byte) 0; i < this.sub_authority_count; i += SID_TYPE_USER) {
                this.sub_authority[i] = sid.sub_authority[i];
            }
        }
    }

    public SID getDomainSid() {
        return new SID(this, SID_TYPE_DOMAIN, this.domainName, null, getType() != SID_TYPE_DOMAIN);
    }

    public int getRid() {
        if (getType() != SID_TYPE_DOMAIN) {
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
        if (this.type != SID_TYPE_UNKNOWN) {
            return this.domainName;
        }
        String full = toString();
        return full.substring(SID_TYPE_USE_NONE, (full.length() - getAccountName().length()) - 1);
    }

    public String getAccountName() {
        if (this.origin_server != null) {
            resolveWeak();
        }
        if (this.type == SID_TYPE_UNKNOWN) {
            return "" + this.sub_authority[this.sub_authority_count - 1];
        }
        if (this.type == SID_TYPE_DOMAIN) {
            return "";
        }
        return this.acctName;
    }

    public int hashCode() {
        int hcode = this.identifier_authority[SID_TYPE_WKN_GRP];
        for (byte i = (byte) 0; i < this.sub_authority_count; i += SID_TYPE_USER) {
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
        int i = this.sub_authority_count;
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                break;
            } else if (sid.sub_authority[i2] != this.sub_authority[i2]) {
                return false;
            } else {
                i = i2;
            }
        }
        for (i2 = SID_TYPE_USE_NONE; i2 < SID_TYPE_DELETED; i2 += SID_TYPE_USER) {
            if (sid.identifier_authority[i2] != this.identifier_authority[i2]) {
                return false;
            }
        }
        if (sid.revision != this.revision) {
            z = false;
        }
        return z;
    }

    public String toString() {
        String ret = "S-" + (this.revision & 255) + "-";
        if (this.identifier_authority[SID_TYPE_USE_NONE] == null && this.identifier_authority[SID_TYPE_USER] == null) {
            long shift = 0;
            long id = 0;
            for (int i = SID_TYPE_WKN_GRP; i > SID_TYPE_USER; i--) {
                id += (((long) this.identifier_authority[i]) & 255) << ((int) shift);
                shift += 8;
            }
            ret = ret + id;
        } else {
            ret = (ret + "0x") + Hexdump.toHexString(this.identifier_authority, SID_TYPE_USE_NONE, SID_TYPE_DELETED);
        }
        for (byte i2 = (byte) 0; i2 < this.sub_authority_count; i2 += SID_TYPE_USER) {
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
        if (this.type == SID_TYPE_DOMAIN) {
            return this.domainName;
        }
        if (this.type != SID_TYPE_WKN_GRP && !this.domainName.equals("BUILTIN")) {
            return this.domainName + "\\" + this.acctName;
        }
        if (this.type == SID_TYPE_UNKNOWN) {
            return toString();
        }
        return this.acctName;
    }

    public void resolve(String authorityServerName, NtlmPasswordAuthentication auth) throws IOException {
        SID[] sids = new SID[SID_TYPE_USER];
        sids[SID_TYPE_USE_NONE] = this;
        resolveSids(authorityServerName, auth, sids);
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

    static SID[] getGroupMemberSids0(DcerpcHandle handle, SamrDomainHandle domainHandle, SID domsid, int rid, int flags) throws IOException {
        MsrpcGetMembersInAlias rpc;
        Throwable th;
        SamrAliasHandle aliasHandle = null;
        LsarSidArray sidarray = new LsarSidArray();
        try {
            SamrAliasHandle aliasHandle2 = new SamrAliasHandle(handle, domainHandle, 131084, rid);
            try {
                rpc = new MsrpcGetMembersInAlias(aliasHandle2, sidarray);
            } catch (Throwable th2) {
                th = th2;
                aliasHandle = aliasHandle2;
                if (aliasHandle != null) {
                    aliasHandle.close();
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
                for (int i = SID_TYPE_USE_NONE; i < sids.length; i += SID_TYPE_USER) {
                    sids[i] = new SID(rpc.sids.sids[i].sid, SID_TYPE_USE_NONE, null, null, false);
                    sids[i].origin_server = origin_server;
                    sids[i].origin_auth = origin_auth;
                }
                if (sids.length > 0 && (flags & SID_TYPE_USER) != 0) {
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
                    aliasHandle.close();
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

    public SID[] getGroupMemberSids(String authorityServerName, NtlmPasswordAuthentication auth, int flags) throws IOException {
        Throwable th;
        if (this.type != SID_TYPE_DOM_GRP && this.type != SID_TYPE_ALIAS) {
            return new SID[SID_TYPE_USE_NONE];
        }
        DcerpcHandle handle = null;
        SamrPolicyHandle policyHandle = null;
        SamrDomainHandle domainHandle = null;
        SID domsid = getDomainSid();
        synchronized (sid_cache) {
            try {
                SamrDomainHandle domainHandle2;
                handle = DcerpcHandle.getHandle("ncacn_np:" + authorityServerName + "[\\PIPE\\samr]", auth);
                SamrPolicyHandle policyHandle2 = new SamrPolicyHandle(handle, authorityServerName, 48);
                try {
                    domainHandle2 = new SamrDomainHandle(handle, policyHandle2, SmbNamedPipe.PIPE_TYPE_TRANSACT, domsid);
                } catch (Throwable th2) {
                    th = th2;
                    policyHandle = policyHandle2;
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
                try {
                    SID[] groupMemberSids0 = getGroupMemberSids0(handle, domainHandle2, domsid, getRid(), flags);
                    if (handle != null) {
                        if (policyHandle2 != null) {
                            if (domainHandle2 != null) {
                                try {
                                    domainHandle2.close();
                                } catch (Throwable th3) {
                                    th = th3;
                                    domainHandle = domainHandle2;
                                    policyHandle = policyHandle2;
                                    try {
                                    } catch (Throwable th4) {
                                        th = th4;
                                        throw th;
                                    }
                                    throw th;
                                }
                            }
                            policyHandle2.close();
                        }
                        handle.close();
                    }
                    return groupMemberSids0;
                } catch (Throwable th5) {
                    th = th5;
                    domainHandle = domainHandle2;
                    policyHandle = policyHandle2;
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
            } catch (Throwable th6) {
                th = th6;
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
                        if (policyHandle != null) {
                            if (domainHandle != null) {
                                try {
                                    domainHandle.close();
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
                    DcerpcMessage msrpcEnumerateAliasesInDomain = new MsrpcEnumerateAliasesInDomain(domainHandle2, 65535, sam);
                    handle.sendrecv(msrpcEnumerateAliasesInDomain);
                    if (msrpcEnumerateAliasesInDomain.retval != 0) {
                        throw new SmbException(msrpcEnumerateAliasesInDomain.retval, false);
                    }
                    Map map = new HashMap();
                    int ei = SID_TYPE_USE_NONE;
                    while (true) {
                        int i = msrpcEnumerateAliasesInDomain.sam.count;
                        if (ei >= r0) {
                            break;
                        }
                        SamrSamEntry entry = msrpcEnumerateAliasesInDomain.sam.entries[ei];
                        SID[] mems = getGroupMemberSids0(handle, domainHandle2, domsid, entry.idx, flags);
                        SID groupSid = new SID(domsid, entry.idx);
                        groupSid.type = SID_TYPE_ALIAS;
                        groupSid.domainName = domsid.getDomainName();
                        groupSid.acctName = new UnicodeString(entry.name, false).toString();
                        int mi = SID_TYPE_USE_NONE;
                        while (true) {
                            i = mems.length;
                            if (mi >= r0) {
                                break;
                            }
                            ArrayList groups = (ArrayList) map.get(mems[mi]);
                            if (groups == null) {
                                groups = new ArrayList();
                                map.put(mems[mi], groups);
                            }
                            if (!groups.contains(groupSid)) {
                                groups.add(groupSid);
                            }
                            mi += SID_TYPE_USER;
                        }
                        ei += SID_TYPE_USER;
                    }
                    if (handle != null) {
                        if (policyHandle2 != null) {
                            if (domainHandle2 != null) {
                                try {
                                    domainHandle2.close();
                                } catch (Throwable th4) {
                                    th = th4;
                                    domainHandle = domainHandle2;
                                    policyHandle = policyHandle2;
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
                    domainHandle = domainHandle2;
                    policyHandle = policyHandle2;
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
            } catch (Throwable th6) {
                th = th6;
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
}
