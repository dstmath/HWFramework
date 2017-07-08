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

        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.time_low);
            _dst.enc_ndr_short(this.time_mid);
            _dst.enc_ndr_short(this.time_hi_and_version);
            _dst.enc_ndr_small(this.clock_seq_hi_and_reserved);
            _dst.enc_ndr_small(this.clock_seq_low);
            int _nodei = _dst.index;
            _dst.advance(6);
            _dst = _dst.derive(_nodei);
            for (int _i = 0; _i < 6; _i++) {
                _dst.enc_ndr_small(this.node[_i]);
            }
        }

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
                if (6 < null || 6 > 65535) {
                    throw new NdrException(NdrException.INVALID_CONFORMANCE);
                }
                this.node = new byte[6];
            }
            _src = _src.derive(_nodei);
            for (int _i = 0; _i < 6; _i++) {
                this.node[_i] = (byte) _src.dec_ndr_small();
            }
        }
    }

    public static class unicode_string extends NdrObject {
        public short[] buffer;
        public short length;
        public short maximum_length;

        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_short(this.length);
            _dst.enc_ndr_short(this.maximum_length);
            _dst.enc_ndr_referent(this.buffer, 1);
            if (this.buffer != null) {
                _dst = _dst.deferred;
                int _bufferl = this.length / 2;
                _dst.enc_ndr_long(this.maximum_length / 2);
                _dst.enc_ndr_long(0);
                _dst.enc_ndr_long(_bufferl);
                int _bufferi = _dst.index;
                _dst.advance(_bufferl * 2);
                _dst = _dst.derive(_bufferi);
                for (int _i = 0; _i < _bufferl; _i++) {
                    _dst.enc_ndr_short(this.buffer[_i]);
                }
            }
        }

        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.length = (short) _src.dec_ndr_short();
            this.maximum_length = (short) _src.dec_ndr_short();
            if (_src.dec_ndr_long() != 0) {
                _src = _src.deferred;
                int _buffers = _src.dec_ndr_long();
                _src.dec_ndr_long();
                int _bufferl = _src.dec_ndr_long();
                int _bufferi = _src.index;
                _src.advance(_bufferl * 2);
                if (this.buffer == null) {
                    if (_buffers < 0 || _buffers > 65535) {
                        throw new NdrException(NdrException.INVALID_CONFORMANCE);
                    }
                    this.buffer = new short[_buffers];
                }
                _src = _src.derive(_bufferi);
                for (int _i = 0; _i < _bufferl; _i++) {
                    this.buffer[_i] = (short) _src.dec_ndr_short();
                }
            }
        }
    }

    public static class policy_handle extends NdrObject {
        public int type;
        public uuid_t uuid;

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
            _dst = _dst.derive(_uuid_nodei);
            for (int _i = 0; _i < 6; _i++) {
                _dst.enc_ndr_small(this.uuid.node[_i]);
            }
        }

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
                if (6 < null || 6 > 65535) {
                    throw new NdrException(NdrException.INVALID_CONFORMANCE);
                }
                this.uuid.node = new byte[6];
            }
            _src = _src.derive(_uuid_nodei);
            for (int _i = 0; _i < 6; _i++) {
                this.uuid.node[_i] = (byte) _src.dec_ndr_small();
            }
        }
    }

    public static class sid_t extends NdrObject {
        public byte[] identifier_authority;
        public byte revision;
        public int[] sub_authority;
        public byte sub_authority_count;

        public void encode(NdrBuffer _dst) throws NdrException {
            int _i;
            _dst.align(4);
            int _sub_authoritys = this.sub_authority_count;
            _dst.enc_ndr_long(_sub_authoritys);
            _dst.enc_ndr_small(this.revision);
            _dst.enc_ndr_small(this.sub_authority_count);
            int _identifier_authorityi = _dst.index;
            _dst.advance(6);
            int _sub_authorityi = _dst.index;
            _dst.advance(_sub_authoritys * 4);
            _dst = _dst.derive(_identifier_authorityi);
            for (_i = 0; _i < 6; _i++) {
                _dst.enc_ndr_small(this.identifier_authority[_i]);
            }
            _dst = _dst.derive(_sub_authorityi);
            for (_i = 0; _i < _sub_authoritys; _i++) {
                _dst.enc_ndr_long(this.sub_authority[_i]);
            }
        }

        public void decode(NdrBuffer _src) throws NdrException {
            int _i;
            _src.align(4);
            int _sub_authoritys = _src.dec_ndr_long();
            this.revision = (byte) _src.dec_ndr_small();
            this.sub_authority_count = (byte) _src.dec_ndr_small();
            int _identifier_authorityi = _src.index;
            _src.advance(6);
            int _sub_authorityi = _src.index;
            _src.advance(_sub_authoritys * 4);
            if (this.identifier_authority == null) {
                if (6 < null || 6 > 65535) {
                    throw new NdrException(NdrException.INVALID_CONFORMANCE);
                }
                this.identifier_authority = new byte[6];
            }
            _src = _src.derive(_identifier_authorityi);
            for (_i = 0; _i < 6; _i++) {
                this.identifier_authority[_i] = (byte) _src.dec_ndr_small();
            }
            if (this.sub_authority == null) {
                if (_sub_authoritys < 0 || _sub_authoritys > 65535) {
                    throw new NdrException(NdrException.INVALID_CONFORMANCE);
                }
                this.sub_authority = new int[_sub_authoritys];
            }
            _src = _src.derive(_sub_authorityi);
            for (_i = 0; _i < _sub_authoritys; _i++) {
                this.sub_authority[_i] = _src.dec_ndr_long();
            }
        }
    }
}
