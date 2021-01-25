package jcifs.smb;

public class DosFileFilter implements SmbFileFilter {
    protected int attributes;
    protected String wildcard;

    public DosFileFilter(String wildcard2, int attributes2) {
        this.wildcard = wildcard2;
        this.attributes = attributes2;
    }

    @Override // jcifs.smb.SmbFileFilter
    public boolean accept(SmbFile file) throws SmbException {
        return (file.getAttributes() & this.attributes) != 0;
    }
}
