package jcifs.smb;

public class DosFileFilter implements SmbFileFilter {
    protected int attributes;
    protected String wildcard;

    public DosFileFilter(String wildcard, int attributes) {
        this.wildcard = wildcard;
        this.attributes = attributes;
    }

    public boolean accept(SmbFile file) throws SmbException {
        return (file.getAttributes() & this.attributes) != 0;
    }
}
