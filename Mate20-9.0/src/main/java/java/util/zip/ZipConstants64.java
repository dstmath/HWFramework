package java.util.zip;

class ZipConstants64 {
    static final int EFS = 2048;
    static final int EXTID_EXTT = 21589;
    static final int EXTID_NTFS = 10;
    static final int EXTID_UNIX = 13;
    static final int EXTID_ZIP64 = 1;
    static final int EXTT_FLAG_LAT = 2;
    static final int EXTT_FLAG_LMT = 1;
    static final int EXTT_FLAT_CT = 4;
    static final int ZIP64_ENDDSK = 20;
    static final int ZIP64_ENDEXT = 56;
    static final int ZIP64_ENDHDR = 56;
    static final int ZIP64_ENDLEN = 4;
    static final int ZIP64_ENDNMD = 16;
    static final int ZIP64_ENDOFF = 48;
    static final long ZIP64_ENDSIG = 101075792;
    static final int ZIP64_ENDSIZ = 40;
    static final int ZIP64_ENDTOD = 24;
    static final int ZIP64_ENDTOT = 32;
    static final int ZIP64_ENDVEM = 12;
    static final int ZIP64_ENDVER = 14;
    static final int ZIP64_EXTCRC = 4;
    static final int ZIP64_EXTHDR = 24;
    static final int ZIP64_EXTID = 1;
    static final int ZIP64_EXTLEN = 16;
    static final int ZIP64_EXTSIZ = 8;
    static final int ZIP64_LOCDSK = 4;
    static final int ZIP64_LOCHDR = 20;
    static final int ZIP64_LOCOFF = 8;
    static final long ZIP64_LOCSIG = 117853008;
    static final int ZIP64_LOCTOT = 16;
    static final int ZIP64_MAGICCOUNT = 65535;
    static final long ZIP64_MAGICVAL = 4294967295L;

    private ZipConstants64() {
    }
}
