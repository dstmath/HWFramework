package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.dcerpc.ndr.NdrLong;
import jcifs.dcerpc.ndr.NdrObject;

public class netdfs {
    public static final int DFS_STORAGE_STATE_ACTIVE = 4;
    public static final int DFS_STORAGE_STATE_OFFLINE = 1;
    public static final int DFS_STORAGE_STATE_ONLINE = 2;
    public static final int DFS_VOLUME_FLAVOR_AD_BLOB = 512;
    public static final int DFS_VOLUME_FLAVOR_STANDALONE = 256;

    public static String getSyntax() {
        return "4fc742e0-4a10-11cf-8273-00aa004ae673:3.0";
    }

    public static class DfsInfo1 extends NdrObject {
        public String entry_path;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_referent(this.entry_path, 1);
            if (this.entry_path != null) {
                _dst.deferred.enc_ndr_string(this.entry_path);
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            if (_src.dec_ndr_long() != 0) {
                this.entry_path = _src.deferred.dec_ndr_string();
            }
        }
    }

    public static class DfsEnumArray1 extends NdrObject {
        public int count;
        public DfsInfo1[] s;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.count);
            _dst.enc_ndr_referent(this.s, 1);
            if (this.s != null) {
                NdrBuffer _dst2 = _dst.deferred;
                int _ss = this.count;
                _dst2.enc_ndr_long(_ss);
                int _si = _dst2.index;
                _dst2.advance(_ss * 4);
                NdrBuffer _dst3 = _dst2.derive(_si);
                for (int _i = 0; _i < _ss; _i++) {
                    this.s[_i].encode(_dst3);
                }
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.count = _src.dec_ndr_long();
            if (_src.dec_ndr_long() != 0) {
                NdrBuffer _src2 = _src.deferred;
                int _ss = _src2.dec_ndr_long();
                int _si = _src2.index;
                _src2.advance(_ss * 4);
                if (this.s == null) {
                    if (_ss < 0 || _ss > 65535) {
                        throw new NdrException(NdrException.INVALID_CONFORMANCE);
                    }
                    this.s = new DfsInfo1[_ss];
                }
                NdrBuffer _src3 = _src2.derive(_si);
                for (int _i = 0; _i < _ss; _i++) {
                    if (this.s[_i] == null) {
                        this.s[_i] = new DfsInfo1();
                    }
                    this.s[_i].decode(_src3);
                }
            }
        }
    }

    public static class DfsStorageInfo extends NdrObject {
        public String server_name;
        public String share_name;
        public int state;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.state);
            _dst.enc_ndr_referent(this.server_name, 1);
            _dst.enc_ndr_referent(this.share_name, 1);
            if (this.server_name != null) {
                _dst = _dst.deferred;
                _dst.enc_ndr_string(this.server_name);
            }
            if (this.share_name != null) {
                _dst.deferred.enc_ndr_string(this.share_name);
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.state = _src.dec_ndr_long();
            int _server_namep = _src.dec_ndr_long();
            int _share_namep = _src.dec_ndr_long();
            if (_server_namep != 0) {
                _src = _src.deferred;
                this.server_name = _src.dec_ndr_string();
            }
            if (_share_namep != 0) {
                this.share_name = _src.deferred.dec_ndr_string();
            }
        }
    }

    public static class DfsInfo3 extends NdrObject {
        public String comment;
        public int num_stores;
        public String path;
        public int state;
        public DfsStorageInfo[] stores;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_referent(this.path, 1);
            _dst.enc_ndr_referent(this.comment, 1);
            _dst.enc_ndr_long(this.state);
            _dst.enc_ndr_long(this.num_stores);
            _dst.enc_ndr_referent(this.stores, 1);
            if (this.path != null) {
                _dst = _dst.deferred;
                _dst.enc_ndr_string(this.path);
            }
            if (this.comment != null) {
                _dst = _dst.deferred;
                _dst.enc_ndr_string(this.comment);
            }
            if (this.stores != null) {
                NdrBuffer _dst2 = _dst.deferred;
                int _storess = this.num_stores;
                _dst2.enc_ndr_long(_storess);
                int _storesi = _dst2.index;
                _dst2.advance(_storess * 12);
                NdrBuffer _dst3 = _dst2.derive(_storesi);
                for (int _i = 0; _i < _storess; _i++) {
                    this.stores[_i].encode(_dst3);
                }
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            int _pathp = _src.dec_ndr_long();
            int _commentp = _src.dec_ndr_long();
            this.state = _src.dec_ndr_long();
            this.num_stores = _src.dec_ndr_long();
            int _storesp = _src.dec_ndr_long();
            if (_pathp != 0) {
                _src = _src.deferred;
                this.path = _src.dec_ndr_string();
            }
            if (_commentp != 0) {
                _src = _src.deferred;
                this.comment = _src.dec_ndr_string();
            }
            if (_storesp != 0) {
                NdrBuffer _src2 = _src.deferred;
                int _storess = _src2.dec_ndr_long();
                int _storesi = _src2.index;
                _src2.advance(_storess * 12);
                if (this.stores == null) {
                    if (_storess < 0 || _storess > 65535) {
                        throw new NdrException(NdrException.INVALID_CONFORMANCE);
                    }
                    this.stores = new DfsStorageInfo[_storess];
                }
                NdrBuffer _src3 = _src2.derive(_storesi);
                for (int _i = 0; _i < _storess; _i++) {
                    if (this.stores[_i] == null) {
                        this.stores[_i] = new DfsStorageInfo();
                    }
                    this.stores[_i].decode(_src3);
                }
            }
        }
    }

    public static class DfsEnumArray3 extends NdrObject {
        public int count;
        public DfsInfo3[] s;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.count);
            _dst.enc_ndr_referent(this.s, 1);
            if (this.s != null) {
                NdrBuffer _dst2 = _dst.deferred;
                int _ss = this.count;
                _dst2.enc_ndr_long(_ss);
                int _si = _dst2.index;
                _dst2.advance(_ss * 20);
                NdrBuffer _dst3 = _dst2.derive(_si);
                for (int _i = 0; _i < _ss; _i++) {
                    this.s[_i].encode(_dst3);
                }
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.count = _src.dec_ndr_long();
            if (_src.dec_ndr_long() != 0) {
                NdrBuffer _src2 = _src.deferred;
                int _ss = _src2.dec_ndr_long();
                int _si = _src2.index;
                _src2.advance(_ss * 20);
                if (this.s == null) {
                    if (_ss < 0 || _ss > 65535) {
                        throw new NdrException(NdrException.INVALID_CONFORMANCE);
                    }
                    this.s = new DfsInfo3[_ss];
                }
                NdrBuffer _src3 = _src2.derive(_si);
                for (int _i = 0; _i < _ss; _i++) {
                    if (this.s[_i] == null) {
                        this.s[_i] = new DfsInfo3();
                    }
                    this.s[_i].decode(_src3);
                }
            }
        }
    }

    public static class DfsInfo200 extends NdrObject {
        public String dfs_name;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_referent(this.dfs_name, 1);
            if (this.dfs_name != null) {
                _dst.deferred.enc_ndr_string(this.dfs_name);
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            if (_src.dec_ndr_long() != 0) {
                this.dfs_name = _src.deferred.dec_ndr_string();
            }
        }
    }

    public static class DfsEnumArray200 extends NdrObject {
        public int count;
        public DfsInfo200[] s;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.count);
            _dst.enc_ndr_referent(this.s, 1);
            if (this.s != null) {
                NdrBuffer _dst2 = _dst.deferred;
                int _ss = this.count;
                _dst2.enc_ndr_long(_ss);
                int _si = _dst2.index;
                _dst2.advance(_ss * 4);
                NdrBuffer _dst3 = _dst2.derive(_si);
                for (int _i = 0; _i < _ss; _i++) {
                    this.s[_i].encode(_dst3);
                }
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.count = _src.dec_ndr_long();
            if (_src.dec_ndr_long() != 0) {
                NdrBuffer _src2 = _src.deferred;
                int _ss = _src2.dec_ndr_long();
                int _si = _src2.index;
                _src2.advance(_ss * 4);
                if (this.s == null) {
                    if (_ss < 0 || _ss > 65535) {
                        throw new NdrException(NdrException.INVALID_CONFORMANCE);
                    }
                    this.s = new DfsInfo200[_ss];
                }
                NdrBuffer _src3 = _src2.derive(_si);
                for (int _i = 0; _i < _ss; _i++) {
                    if (this.s[_i] == null) {
                        this.s[_i] = new DfsInfo200();
                    }
                    this.s[_i].decode(_src3);
                }
            }
        }
    }

    public static class DfsInfo300 extends NdrObject {
        public String dfs_name;
        public int flags;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.flags);
            _dst.enc_ndr_referent(this.dfs_name, 1);
            if (this.dfs_name != null) {
                _dst.deferred.enc_ndr_string(this.dfs_name);
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.flags = _src.dec_ndr_long();
            if (_src.dec_ndr_long() != 0) {
                this.dfs_name = _src.deferred.dec_ndr_string();
            }
        }
    }

    public static class DfsEnumArray300 extends NdrObject {
        public int count;
        public DfsInfo300[] s;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.count);
            _dst.enc_ndr_referent(this.s, 1);
            if (this.s != null) {
                NdrBuffer _dst2 = _dst.deferred;
                int _ss = this.count;
                _dst2.enc_ndr_long(_ss);
                int _si = _dst2.index;
                _dst2.advance(_ss * 8);
                NdrBuffer _dst3 = _dst2.derive(_si);
                for (int _i = 0; _i < _ss; _i++) {
                    this.s[_i].encode(_dst3);
                }
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.count = _src.dec_ndr_long();
            if (_src.dec_ndr_long() != 0) {
                NdrBuffer _src2 = _src.deferred;
                int _ss = _src2.dec_ndr_long();
                int _si = _src2.index;
                _src2.advance(_ss * 8);
                if (this.s == null) {
                    if (_ss < 0 || _ss > 65535) {
                        throw new NdrException(NdrException.INVALID_CONFORMANCE);
                    }
                    this.s = new DfsInfo300[_ss];
                }
                NdrBuffer _src3 = _src2.derive(_si);
                for (int _i = 0; _i < _ss; _i++) {
                    if (this.s[_i] == null) {
                        this.s[_i] = new DfsInfo300();
                    }
                    this.s[_i].decode(_src3);
                }
            }
        }
    }

    public static class DfsEnumStruct extends NdrObject {
        public NdrObject e;
        public int level;

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(this.level);
            _dst.enc_ndr_long(this.level);
            _dst.enc_ndr_referent(this.e, 1);
            if (this.e != null) {
                this.e.encode(_dst.deferred);
            }
        }

        @Override // jcifs.dcerpc.ndr.NdrObject
        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            this.level = _src.dec_ndr_long();
            _src.dec_ndr_long();
            if (_src.dec_ndr_long() != 0) {
                if (this.e == null) {
                    this.e = new DfsEnumArray1();
                }
                this.e.decode(_src.deferred);
            }
        }
    }

    public static class NetrDfsEnumEx extends DcerpcMessage {
        public String dfs_name;
        public DfsEnumStruct info;
        public int level;
        public int prefmaxlen;
        public int retval;
        public NdrLong totalentries;

        @Override // jcifs.dcerpc.DcerpcMessage
        public int getOpnum() {
            return 21;
        }

        public NetrDfsEnumEx(String dfs_name2, int level2, int prefmaxlen2, DfsEnumStruct info2, NdrLong totalentries2) {
            this.dfs_name = dfs_name2;
            this.level = level2;
            this.prefmaxlen = prefmaxlen2;
            this.info = info2;
            this.totalentries = totalentries2;
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void encode_in(NdrBuffer _dst) throws NdrException {
            _dst.enc_ndr_string(this.dfs_name);
            _dst.enc_ndr_long(this.level);
            _dst.enc_ndr_long(this.prefmaxlen);
            _dst.enc_ndr_referent(this.info, 1);
            if (this.info != null) {
                this.info.encode(_dst);
            }
            _dst.enc_ndr_referent(this.totalentries, 1);
            if (this.totalentries != null) {
                this.totalentries.encode(_dst);
            }
        }

        @Override // jcifs.dcerpc.DcerpcMessage
        public void decode_out(NdrBuffer _src) throws NdrException {
            if (_src.dec_ndr_long() != 0) {
                if (this.info == null) {
                    this.info = new DfsEnumStruct();
                }
                this.info.decode(_src);
            }
            if (_src.dec_ndr_long() != 0) {
                this.totalentries.decode(_src);
            }
            this.retval = _src.dec_ndr_long();
        }
    }
}
