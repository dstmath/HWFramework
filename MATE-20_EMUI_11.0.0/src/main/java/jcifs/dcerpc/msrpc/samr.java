package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.msrpc.lsarpc;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.dcerpc.ndr.NdrObject;
import jcifs.dcerpc.rpc;

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

    public static String getSyntax() {
        return "12345778-1234-abcd-ef00-0123456789ac:1.0";
    }

    public static class SamrCloseHandle extends DcerpcMessage {
        public rpc.policy_handle handle;
        public int retval;

        @Override // jcifs.dcerpc.DcerpcMessage
        public int getOpnum() {
            return 1;
        }

        public SamrCloseHandle(rpc.policy_handle handle2) {
            this.handle = handle2;
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void encode_in(NdrBuffer _dst) throws NdrException {
            this.handle.encode(_dst);
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void decode_out(NdrBuffer _src) throws NdrException {
            this.retval = _src.dec_ndr_long();
        }
    }

    public static class SamrConnect2 extends DcerpcMessage {
        public int access_mask;
        public rpc.policy_handle handle;
        public int retval;
        public String system_name;

        @Override // jcifs.dcerpc.DcerpcMessage
        public int getOpnum() {
            return 57;
        }

        public SamrConnect2(String system_name2, int access_mask2, rpc.policy_handle handle2) {
            this.system_name = system_name2;
            this.access_mask = access_mask2;
            this.handle = handle2;
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void encode_in(NdrBuffer _dst) throws NdrException {
            _dst.enc_ndr_referent(this.system_name, 1);
            if (this.system_name != null) {
                _dst.enc_ndr_string(this.system_name);
            }
            _dst.enc_ndr_long(this.access_mask);
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void decode_out(NdrBuffer _src) throws NdrException {
            this.handle.decode(_src);
            this.retval = _src.dec_ndr_long();
        }
    }

    public static class SamrConnect4 extends DcerpcMessage {
        public int access_mask;
        public rpc.policy_handle handle;
        public int retval;
        public String system_name;
        public int unknown;

        @Override // jcifs.dcerpc.DcerpcMessage
        public int getOpnum() {
            return 62;
        }

        public SamrConnect4(String system_name2, int unknown2, int access_mask2, rpc.policy_handle handle2) {
            this.system_name = system_name2;
            this.unknown = unknown2;
            this.access_mask = access_mask2;
            this.handle = handle2;
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void encode_in(NdrBuffer _dst) throws NdrException {
            _dst.enc_ndr_referent(this.system_name, 1);
            if (this.system_name != null) {
                _dst.enc_ndr_string(this.system_name);
            }
            _dst.enc_ndr_long(this.unknown);
            _dst.enc_ndr_long(this.access_mask);
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void decode_out(NdrBuffer _src) throws NdrException {
            this.handle.decode(_src);
            this.retval = _src.dec_ndr_long();
        }
    }

    public static class SamrOpenDomain extends DcerpcMessage {
        public int access_mask;
        public rpc.policy_handle domain_handle;
        public rpc.policy_handle handle;
        public int retval;
        public rpc.sid_t sid;

        @Override // jcifs.dcerpc.DcerpcMessage
        public int getOpnum() {
            return 7;
        }

        public SamrOpenDomain(rpc.policy_handle handle2, int access_mask2, rpc.sid_t sid2, rpc.policy_handle domain_handle2) {
            this.handle = handle2;
            this.access_mask = access_mask2;
            this.sid = sid2;
            this.domain_handle = domain_handle2;
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void encode_in(NdrBuffer _dst) throws NdrException {
            this.handle.encode(_dst);
            _dst.enc_ndr_long(this.access_mask);
            this.sid.encode(_dst);
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void decode_out(NdrBuffer _src) throws NdrException {
            this.domain_handle.decode(_src);
            this.retval = _src.dec_ndr_long();
        }
    }

    public static class SamrSamEntry extends NdrObject {
        public int idx;
        public rpc.unicode_string name;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.idx);
            _dst.enc_ndr_short(this.name.length);
            _dst.enc_ndr_short(this.name.maximum_length);
            _dst.enc_ndr_referent(this.name.buffer, 1);
            if (this.name.buffer != null) {
                NdrBuffer _dst2 = _dst.deferred;
                int _name_bufferl = this.name.length / 2;
                _dst2.enc_ndr_long(this.name.maximum_length / 2);
                _dst2.enc_ndr_long(0);
                _dst2.enc_ndr_long(_name_bufferl);
                int _name_bufferi = _dst2.index;
                _dst2.advance(_name_bufferl * 2);
                NdrBuffer _dst3 = _dst2.derive(_name_bufferi);
                for (int _i = 0; _i < _name_bufferl; _i++) {
                    _dst3.enc_ndr_short(this.name.buffer[_i]);
                }
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.idx = _src.dec_ndr_long();
            _src.align(4);
            if (this.name == null) {
                this.name = new rpc.unicode_string();
            }
            this.name.length = (short) _src.dec_ndr_short();
            this.name.maximum_length = (short) _src.dec_ndr_short();
            if (_src.dec_ndr_long() != 0) {
                NdrBuffer _src2 = _src.deferred;
                int _name_buffers = _src2.dec_ndr_long();
                _src2.dec_ndr_long();
                int _name_bufferl = _src2.dec_ndr_long();
                int _name_bufferi = _src2.index;
                _src2.advance(_name_bufferl * 2);
                if (this.name.buffer == null) {
                    if (_name_buffers < 0 || _name_buffers > 65535) {
                        throw new NdrException(NdrException.INVALID_CONFORMANCE);
                    }
                    this.name.buffer = new short[_name_buffers];
                }
                NdrBuffer _src3 = _src2.derive(_name_bufferi);
                for (int _i = 0; _i < _name_bufferl; _i++) {
                    this.name.buffer[_i] = (short) _src3.dec_ndr_short();
                }
            }
        }
    }

    public static class SamrSamArray extends NdrObject {
        public int count;
        public SamrSamEntry[] entries;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.count);
            _dst.enc_ndr_referent(this.entries, 1);
            if (this.entries != null) {
                NdrBuffer _dst2 = _dst.deferred;
                int _entriess = this.count;
                _dst2.enc_ndr_long(_entriess);
                int _entriesi = _dst2.index;
                _dst2.advance(_entriess * 12);
                NdrBuffer _dst3 = _dst2.derive(_entriesi);
                for (int _i = 0; _i < _entriess; _i++) {
                    this.entries[_i].encode(_dst3);
                }
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.count = _src.dec_ndr_long();
            if (_src.dec_ndr_long() != 0) {
                NdrBuffer _src2 = _src.deferred;
                int _entriess = _src2.dec_ndr_long();
                int _entriesi = _src2.index;
                _src2.advance(_entriess * 12);
                if (this.entries == null) {
                    if (_entriess < 0 || _entriess > 65535) {
                        throw new NdrException(NdrException.INVALID_CONFORMANCE);
                    }
                    this.entries = new SamrSamEntry[_entriess];
                }
                NdrBuffer _src3 = _src2.derive(_entriesi);
                for (int _i = 0; _i < _entriess; _i++) {
                    if (this.entries[_i] == null) {
                        this.entries[_i] = new SamrSamEntry();
                    }
                    this.entries[_i].decode(_src3);
                }
            }
        }
    }

    public static class SamrEnumerateAliasesInDomain extends DcerpcMessage {
        public int acct_flags;
        public rpc.policy_handle domain_handle;
        public int num_entries;
        public int resume_handle;
        public int retval;
        public SamrSamArray sam;

        @Override // jcifs.dcerpc.DcerpcMessage
        public int getOpnum() {
            return 15;
        }

        public SamrEnumerateAliasesInDomain(rpc.policy_handle domain_handle2, int resume_handle2, int acct_flags2, SamrSamArray sam2, int num_entries2) {
            this.domain_handle = domain_handle2;
            this.resume_handle = resume_handle2;
            this.acct_flags = acct_flags2;
            this.sam = sam2;
            this.num_entries = num_entries2;
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void encode_in(NdrBuffer _dst) throws NdrException {
            this.domain_handle.encode(_dst);
            _dst.enc_ndr_long(this.resume_handle);
            _dst.enc_ndr_long(this.acct_flags);
        }

        @Override // jcifs.dcerpc.DcerpcMessage
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

    public static class SamrOpenAlias extends DcerpcMessage {
        public int access_mask;
        public rpc.policy_handle alias_handle;
        public rpc.policy_handle domain_handle;
        public int retval;
        public int rid;

        @Override // jcifs.dcerpc.DcerpcMessage
        public int getOpnum() {
            return 27;
        }

        public SamrOpenAlias(rpc.policy_handle domain_handle2, int access_mask2, int rid2, rpc.policy_handle alias_handle2) {
            this.domain_handle = domain_handle2;
            this.access_mask = access_mask2;
            this.rid = rid2;
            this.alias_handle = alias_handle2;
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void encode_in(NdrBuffer _dst) throws NdrException {
            this.domain_handle.encode(_dst);
            _dst.enc_ndr_long(this.access_mask);
            _dst.enc_ndr_long(this.rid);
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void decode_out(NdrBuffer _src) throws NdrException {
            this.alias_handle.decode(_src);
            this.retval = _src.dec_ndr_long();
        }
    }

    public static class SamrGetMembersInAlias extends DcerpcMessage {
        public rpc.policy_handle alias_handle;
        public int retval;
        public lsarpc.LsarSidArray sids;

        @Override // jcifs.dcerpc.DcerpcMessage
        public int getOpnum() {
            return 33;
        }

        public SamrGetMembersInAlias(rpc.policy_handle alias_handle2, lsarpc.LsarSidArray sids2) {
            this.alias_handle = alias_handle2;
            this.sids = sids2;
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void encode_in(NdrBuffer _dst) throws NdrException {
            this.alias_handle.encode(_dst);
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void decode_out(NdrBuffer _src) throws NdrException {
            this.sids.decode(_src);
            this.retval = _src.dec_ndr_long();
        }
    }

    public static class SamrRidWithAttribute extends NdrObject {
        public int attributes;
        public int rid;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.rid);
            _dst.enc_ndr_long(this.attributes);
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.rid = _src.dec_ndr_long();
            this.attributes = _src.dec_ndr_long();
        }
    }

    public static class SamrRidWithAttributeArray extends NdrObject {
        public int count;
        public SamrRidWithAttribute[] rids;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.count);
            _dst.enc_ndr_referent(this.rids, 1);
            if (this.rids != null) {
                NdrBuffer _dst2 = _dst.deferred;
                int _ridss = this.count;
                _dst2.enc_ndr_long(_ridss);
                int _ridsi = _dst2.index;
                _dst2.advance(_ridss * 8);
                NdrBuffer _dst3 = _dst2.derive(_ridsi);
                for (int _i = 0; _i < _ridss; _i++) {
                    this.rids[_i].encode(_dst3);
                }
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.count = _src.dec_ndr_long();
            if (_src.dec_ndr_long() != 0) {
                NdrBuffer _src2 = _src.deferred;
                int _ridss = _src2.dec_ndr_long();
                int _ridsi = _src2.index;
                _src2.advance(_ridss * 8);
                if (this.rids == null) {
                    if (_ridss < 0 || _ridss > 65535) {
                        throw new NdrException(NdrException.INVALID_CONFORMANCE);
                    }
                    this.rids = new SamrRidWithAttribute[_ridss];
                }
                NdrBuffer _src3 = _src2.derive(_ridsi);
                for (int _i = 0; _i < _ridss; _i++) {
                    if (this.rids[_i] == null) {
                        this.rids[_i] = new SamrRidWithAttribute();
                    }
                    this.rids[_i].decode(_src3);
                }
            }
        }
    }
}
