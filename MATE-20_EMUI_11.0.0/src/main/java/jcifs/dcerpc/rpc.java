package jcifs.dcerpc;

import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.dcerpc.ndr.NdrObject;

public class rpc {

    public static class uuid_t extends NdrObject {
        public byte clock_seq_hi_and_reserved;
        public byte clock_seq_low;
        public byte[] node;
        public short time_hi_and_version;
        public int time_low;
        public short time_mid;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.time_low);
            _dst.enc_ndr_short(this.time_mid);
            _dst.enc_ndr_short(this.time_hi_and_version);
            _dst.enc_ndr_small(this.clock_seq_hi_and_reserved);
            _dst.enc_ndr_small(this.clock_seq_low);
            int _nodei = _dst.index;
            _dst.advance(6);
            NdrBuffer _dst2 = _dst.derive(_nodei);
            for (int _i = 0; _i < 6; _i++) {
                _dst2.enc_ndr_small(this.node[_i]);
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.time_low = _src.dec_ndr_long();
            this.time_mid = (short) _src.dec_ndr_short();
            this.time_hi_and_version = (short) _src.dec_ndr_short();
            this.clock_seq_hi_and_reserved = (byte) _src.dec_ndr_small();
            this.clock_seq_low = (byte) _src.dec_ndr_small();
            int _nodei = _src.index;
            _src.advance(6);
            if (this.node == null) {
                if (6 < 0 || 6 > 65535) {
                    throw new NdrException(NdrException.INVALID_CONFORMANCE);
                }
                this.node = new byte[6];
            }
            NdrBuffer _src2 = _src.derive(_nodei);
            for (int _i = 0; _i < 6; _i++) {
                this.node[_i] = (byte) _src2.dec_ndr_small();
            }
        }
    }

    public static class policy_handle extends NdrObject {
        public int type;
        public uuid_t uuid;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.type);
            _dst.enc_ndr_long(this.uuid.time_low);
            _dst.enc_ndr_short(this.uuid.time_mid);
            _dst.enc_ndr_short(this.uuid.time_hi_and_version);
            _dst.enc_ndr_small(this.uuid.clock_seq_hi_and_reserved);
            _dst.enc_ndr_small(this.uuid.clock_seq_low);
            int _uuid_nodei = _dst.index;
            _dst.advance(6);
            NdrBuffer _dst2 = _dst.derive(_uuid_nodei);
            for (int _i = 0; _i < 6; _i++) {
                _dst2.enc_ndr_small(this.uuid.node[_i]);
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.type = _src.dec_ndr_long();
            _src.align(4);
            if (this.uuid == null) {
                this.uuid = new uuid_t();
            }
            this.uuid.time_low = _src.dec_ndr_long();
            this.uuid.time_mid = (short) _src.dec_ndr_short();
            this.uuid.time_hi_and_version = (short) _src.dec_ndr_short();
            this.uuid.clock_seq_hi_and_reserved = (byte) _src.dec_ndr_small();
            this.uuid.clock_seq_low = (byte) _src.dec_ndr_small();
            int _uuid_nodei = _src.index;
            _src.advance(6);
            if (this.uuid.node == null) {
                if (6 < 0 || 6 > 65535) {
                    throw new NdrException(NdrException.INVALID_CONFORMANCE);
                }
                this.uuid.node = new byte[6];
            }
            NdrBuffer _src2 = _src.derive(_uuid_nodei);
            for (int _i = 0; _i < 6; _i++) {
                this.uuid.node[_i] = (byte) _src2.dec_ndr_small();
            }
        }
    }

    public static class unicode_string extends NdrObject {
        public short[] buffer;
        public short length;
        public short maximum_length;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_short(this.length);
            _dst.enc_ndr_short(this.maximum_length);
            _dst.enc_ndr_referent(this.buffer, 1);
            if (this.buffer != null) {
                NdrBuffer _dst2 = _dst.deferred;
                int _bufferl = this.length / 2;
                _dst2.enc_ndr_long(this.maximum_length / 2);
                _dst2.enc_ndr_long(0);
                _dst2.enc_ndr_long(_bufferl);
                int _bufferi = _dst2.index;
                _dst2.advance(_bufferl * 2);
                NdrBuffer _dst3 = _dst2.derive(_bufferi);
                for (int _i = 0; _i < _bufferl; _i++) {
                    _dst3.enc_ndr_short(this.buffer[_i]);
                }
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.length = (short) _src.dec_ndr_short();
            this.maximum_length = (short) _src.dec_ndr_short();
            if (_src.dec_ndr_long() != 0) {
                NdrBuffer _src2 = _src.deferred;
                int _buffers = _src2.dec_ndr_long();
                _src2.dec_ndr_long();
                int _bufferl = _src2.dec_ndr_long();
                int _bufferi = _src2.index;
                _src2.advance(_bufferl * 2);
                if (this.buffer == null) {
                    if (_buffers < 0 || _buffers > 65535) {
                        throw new NdrException(NdrException.INVALID_CONFORMANCE);
                    }
                    this.buffer = new short[_buffers];
                }
                NdrBuffer _src3 = _src2.derive(_bufferi);
                for (int _i = 0; _i < _bufferl; _i++) {
                    this.buffer[_i] = (short) _src3.dec_ndr_short();
                }
            }
        }
    }

    public static class sid_t extends NdrObject {
        public byte[] identifier_authority;
        public byte revision;
        public int[] sub_authority;
        public byte sub_authority_count;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            int _sub_authoritys = this.sub_authority_count;
            _dst.enc_ndr_long(_sub_authoritys);
            _dst.enc_ndr_small(this.revision);
            _dst.enc_ndr_small(this.sub_authority_count);
            int _identifier_authorityi = _dst.index;
            _dst.advance(6);
            int _sub_authorityi = _dst.index;
            _dst.advance(_sub_authoritys * 4);
            NdrBuffer _dst2 = _dst.derive(_identifier_authorityi);
            for (int _i = 0; _i < 6; _i++) {
                _dst2.enc_ndr_small(this.identifier_authority[_i]);
            }
            NdrBuffer _dst3 = _dst2.derive(_sub_authorityi);
            for (int _i2 = 0; _i2 < _sub_authoritys; _i2++) {
                _dst3.enc_ndr_long(this.sub_authority[_i2]);
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            int _sub_authoritys = _src.dec_ndr_long();
            this.revision = (byte) _src.dec_ndr_small();
            this.sub_authority_count = (byte) _src.dec_ndr_small();
            int _identifier_authorityi = _src.index;
            _src.advance(6);
            int _sub_authorityi = _src.index;
            _src.advance(_sub_authoritys * 4);
            if (this.identifier_authority == null) {
                if (6 < 0 || 6 > 65535) {
                    throw new NdrException(NdrException.INVALID_CONFORMANCE);
                }
                this.identifier_authority = new byte[6];
            }
            NdrBuffer _src2 = _src.derive(_identifier_authorityi);
            for (int _i = 0; _i < 6; _i++) {
                this.identifier_authority[_i] = (byte) _src2.dec_ndr_small();
            }
            if (this.sub_authority == null) {
                if (_sub_authoritys < 0 || _sub_authoritys > 65535) {
                    throw new NdrException(NdrException.INVALID_CONFORMANCE);
                }
                this.sub_authority = new int[_sub_authoritys];
            }
            NdrBuffer _src3 = _src2.derive(_sub_authorityi);
            for (int _i2 = 0; _i2 < _sub_authoritys; _i2++) {
                this.sub_authority[_i2] = _src3.dec_ndr_long();
            }
        }
    }
}
