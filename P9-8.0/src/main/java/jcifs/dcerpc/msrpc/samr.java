package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.msrpc.lsarpc.LsarSidArray;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.dcerpc.ndr.NdrObject;
import jcifs.dcerpc.rpc.policy_handle;
import jcifs.dcerpc.rpc.sid_t;
import jcifs.dcerpc.rpc.unicode_string;

public class samr {
    public static final int ACB_AUTOLOCK = 1024;
    public static final int ACB_DISABLED = 1;
    public static final int ACB_DOMTRUST = 64;
    public static final int ACB_DONT_REQUIRE_PREAUTH = 65536;
    public static final int ACB_ENC_TXT_PWD_ALLOWED = 2048;
    public static final int ACB_HOMDIRREQ = 2;
    public static final int ACB_MNS = 32;
    public static final int ACB_NORMAL = 16;
    public static final int ACB_NOT_DELEGATED = 16384;
    public static final int ACB_PWNOEXP = 512;
    public static final int ACB_PWNOTREQ = 4;
    public static final int ACB_SMARTCARD_REQUIRED = 4096;
    public static final int ACB_SVRTRUST = 256;
    public static final int ACB_TEMPDUP = 8;
    public static final int ACB_TRUSTED_FOR_DELEGATION = 8192;
    public static final int ACB_USE_DES_KEY_ONLY = 32768;
    public static final int ACB_WSTRUST = 128;
    public static final int SE_GROUP_ENABLED = 4;
    public static final int SE_GROUP_ENABLED_BY_DEFAULT = 2;
    public static final int SE_GROUP_LOGON_ID = -1073741824;
    public static final int SE_GROUP_MANDATORY = 1;
    public static final int SE_GROUP_OWNER = 8;
    public static final int SE_GROUP_RESOURCE = 536870912;
    public static final int SE_GROUP_USE_FOR_DENY_ONLY = 16;

    public static class SamrEnumerateAliasesInDomain extends DcerpcMessage {
        public int acct_flags;
        public policy_handle domain_handle;
        public int num_entries;
        public int resume_handle;
        public int retval;
        public SamrSamArray sam;

        public int getOpnum() {
            return 15;
        }

        public SamrEnumerateAliasesInDomain(policy_handle domain_handle, int resume_handle, int acct_flags, SamrSamArray sam, int num_entries) {
            this.domain_handle = domain_handle;
            this.resume_handle = resume_handle;
            this.acct_flags = acct_flags;
            this.sam = sam;
            this.num_entries = num_entries;
        }

        public void encode_in(NdrBuffer _dst) throws NdrException {
            this.domain_handle.encode(_dst);
            _dst.enc_ndr_long(this.resume_handle);
            _dst.enc_ndr_long(this.acct_flags);
        }

        public void decode_out(NdrBuffer _src) throws NdrException {
            this.resume_handle = _src.dec_ndr_long();
            if (_src.dec_ndr_long() != 0) {
                if (this.sam == null) {
                    this.sam = new SamrSamArray();
                }
                this.sam.decode(_src);
            }
            this.num_entries = _src.dec_ndr_long();
            this.retval = _src.dec_ndr_long();
        }
    }

    public static class SamrGetMembersInAlias extends DcerpcMessage {
        public policy_handle alias_handle;
        public int retval;
        public LsarSidArray sids;

        public int getOpnum() {
            return 33;
        }

        public SamrGetMembersInAlias(policy_handle alias_handle, LsarSidArray sids) {
            this.alias_handle = alias_handle;
            this.sids = sids;
        }

        public void encode_in(NdrBuffer _dst) throws NdrException {
            this.alias_handle.encode(_dst);
        }

        public void decode_out(NdrBuffer _src) throws NdrException {
            this.sids.decode(_src);
            this.retval = _src.dec_ndr_long();
        }
    }

    public static class SamrConnect2 extends DcerpcMessage {
        public int access_mask;
        public policy_handle handle;
        public int retval;
        public String system_name;

        public int getOpnum() {
            return 57;
        }

        public SamrConnect2(String system_name, int access_mask, policy_handle handle) {
            this.system_name = system_name;
            this.access_mask = access_mask;
            this.handle = handle;
        }

        public void encode_in(NdrBuffer _dst) throws NdrException {
            _dst.enc_ndr_referent(this.system_name, 1);
            if (this.system_name != null) {
                _dst.enc_ndr_string(this.system_name);
            }
            _dst.enc_ndr_long(this.access_mask);
        }

        public void decode_out(NdrBuffer _src) throws NdrException {
            this.handle.decode(_src);
            this.retval = _src.dec_ndr_long();
        }
    }

    public static class SamrConnect4 extends DcerpcMessage {
        public int access_mask;
        public policy_handle handle;
        public int retval;
        public String system_name;
        public int unknown;

        public int getOpnum() {
            return 62;
        }

        public SamrConnect4(String system_name, int unknown, int access_mask, policy_handle handle) {
            this.system_name = system_name;
            this.unknown = unknown;
            this.access_mask = access_mask;
            this.handle = handle;
        }

        public void encode_in(NdrBuffer _dst) throws NdrException {
            _dst.enc_ndr_referent(this.system_name, 1);
            if (this.system_name != null) {
                _dst.enc_ndr_string(this.system_name);
            }
            _dst.enc_ndr_long(this.unknown);
            _dst.enc_ndr_long(this.access_mask);
        }

        public void decode_out(NdrBuffer _src) throws NdrException {
            this.handle.decode(_src);
            this.retval = _src.dec_ndr_long();
        }
    }

    public static class SamrOpenAlias extends DcerpcMessage {
        public int access_mask;
        public policy_handle alias_handle;
        public policy_handle domain_handle;
        public int retval;
        public int rid;

        public int getOpnum() {
            return 27;
        }

        public SamrOpenAlias(policy_handle domain_handle, int access_mask, int rid, policy_handle alias_handle) {
            this.domain_handle = domain_handle;
            this.access_mask = access_mask;
            this.rid = rid;
            this.alias_handle = alias_handle;
        }

        public void encode_in(NdrBuffer _dst) throws NdrException {
            this.domain_handle.encode(_dst);
            _dst.enc_ndr_long(this.access_mask);
            _dst.enc_ndr_long(this.rid);
        }

        public void decode_out(NdrBuffer _src) throws NdrException {
            this.alias_handle.decode(_src);
            this.retval = _src.dec_ndr_long();
        }
    }

    public static class SamrOpenDomain extends DcerpcMessage {
        public int access_mask;
        public policy_handle domain_handle;
        public policy_handle handle;
        public int retval;
        public sid_t sid;

        public int getOpnum() {
            return 7;
        }

        public SamrOpenDomain(policy_handle handle, int access_mask, sid_t sid, policy_handle domain_handle) {
            this.handle = handle;
            this.access_mask = access_mask;
            this.sid = sid;
            this.domain_handle = domain_handle;
        }

        public void encode_in(NdrBuffer _dst) throws NdrException {
            this.handle.encode(_dst);
            _dst.enc_ndr_long(this.access_mask);
            this.sid.encode(_dst);
        }

        public void decode_out(NdrBuffer _src) throws NdrException {
            this.domain_handle.decode(_src);
            this.retval = _src.dec_ndr_long();
        }
    }

    public static class SamrCloseHandle extends DcerpcMessage {
        public policy_handle handle;
        public int retval;

        public int getOpnum() {
            return 1;
        }

        public SamrCloseHandle(policy_handle handle) {
            this.handle = handle;
        }

        public void encode_in(NdrBuffer _dst) throws NdrException {
            this.handle.encode(_dst);
        }

        public void decode_out(NdrBuffer _src) throws NdrException {
            this.retval = _src.dec_ndr_long();
        }
    }

    public static class SamrRidWithAttribute extends NdrObject {
        public int attributes;
        public int rid;

        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.rid);
            _dst.enc_ndr_long(this.attributes);
        }

        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.rid = _src.dec_ndr_long();
            this.attributes = _src.dec_ndr_long();
        }
    }

    public static class SamrRidWithAttributeArray extends NdrObject {
        public int count;
        public SamrRidWithAttribute[] rids;

        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.count);
            _dst.enc_ndr_referent(this.rids, 1);
            if (this.rids != null) {
                _dst = _dst.deferred;
                int _ridss = this.count;
                _dst.enc_ndr_long(_ridss);
                int _ridsi = _dst.index;
                _dst.advance(_ridss * 8);
                _dst = _dst.derive(_ridsi);
                for (int _i = 0; _i < _ridss; _i++) {
                    this.rids[_i].encode(_dst);
                }
            }
        }

        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.count = _src.dec_ndr_long();
            if (_src.dec_ndr_long() != 0) {
                _src = _src.deferred;
                int _ridss = _src.dec_ndr_long();
                int _ridsi = _src.index;
                _src.advance(_ridss * 8);
                if (this.rids == null) {
                    if (_ridss < 0 || _ridss > 65535) {
                        throw new NdrException(NdrException.INVALID_CONFORMANCE);
                    }
                    this.rids = new SamrRidWithAttribute[_ridss];
                }
                _src = _src.derive(_ridsi);
                for (int _i = 0; _i < _ridss; _i++) {
                    if (this.rids[_i] == null) {
                        this.rids[_i] = new SamrRidWithAttribute();
                    }
                    this.rids[_i].decode(_src);
                }
            }
        }
    }

    public static class SamrSamArray extends NdrObject {
        public int count;
        public SamrSamEntry[] entries;

        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.count);
            _dst.enc_ndr_referent(this.entries, 1);
            if (this.entries != null) {
                _dst = _dst.deferred;
                int _entriess = this.count;
                _dst.enc_ndr_long(_entriess);
                int _entriesi = _dst.index;
                _dst.advance(_entriess * 12);
                _dst = _dst.derive(_entriesi);
                for (int _i = 0; _i < _entriess; _i++) {
                    this.entries[_i].encode(_dst);
                }
            }
        }

        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.count = _src.dec_ndr_long();
            if (_src.dec_ndr_long() != 0) {
                _src = _src.deferred;
                int _entriess = _src.dec_ndr_long();
                int _entriesi = _src.index;
                _src.advance(_entriess * 12);
                if (this.entries == null) {
                    if (_entriess < 0 || _entriess > 65535) {
                        throw new NdrException(NdrException.INVALID_CONFORMANCE);
                    }
                    this.entries = new SamrSamEntry[_entriess];
                }
                _src = _src.derive(_entriesi);
                for (int _i = 0; _i < _entriess; _i++) {
                    if (this.entries[_i] == null) {
                        this.entries[_i] = new SamrSamEntry();
                    }
                    this.entries[_i].decode(_src);
                }
            }
        }
    }

    public static class SamrSamEntry extends NdrObject {
        public int idx;
        public unicode_string name;

        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.idx);
            _dst.enc_ndr_short(this.name.length);
            _dst.enc_ndr_short(this.name.maximum_length);
            _dst.enc_ndr_referent(this.name.buffer, 1);
            if (this.name.buffer != null) {
                _dst = _dst.deferred;
                int _name_bufferl = this.name.length / 2;
                _dst.enc_ndr_long(this.name.maximum_length / 2);
                _dst.enc_ndr_long(0);
                _dst.enc_ndr_long(_name_bufferl);
                int _name_bufferi = _dst.index;
                _dst.advance(_name_bufferl * 2);
                _dst = _dst.derive(_name_bufferi);
                for (int _i = 0; _i < _name_bufferl; _i++) {
                    _dst.enc_ndr_short(this.name.buffer[_i]);
                }
            }
        }

        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.idx = _src.dec_ndr_long();
            _src.align(4);
            if (this.name == null) {
                this.name = new unicode_string();
            }
            this.name.length = (short) _src.dec_ndr_short();
            this.name.maximum_length = (short) _src.dec_ndr_short();
            if (_src.dec_ndr_long() != 0) {
                _src = _src.deferred;
                int _name_buffers = _src.dec_ndr_long();
                _src.dec_ndr_long();
                int _name_bufferl = _src.dec_ndr_long();
                int _name_bufferi = _src.index;
                _src.advance(_name_bufferl * 2);
                if (this.name.buffer == null) {
                    if (_name_buffers < 0 || _name_buffers > 65535) {
                        throw new NdrException(NdrException.INVALID_CONFORMANCE);
                    }
                    this.name.buffer = new short[_name_buffers];
                }
                _src = _src.derive(_name_bufferi);
                for (int _i = 0; _i < _name_bufferl; _i++) {
                    this.name.buffer[_i] = (short) _src.dec_ndr_short();
                }
            }
        }
    }

    public static String getSyntax() {
        return "12345778-1234-abcd-ef00-0123456789ac:1.0";
    }
}
