package sun.security.x509;

public class X509AttributeName {
    private static final char SEPARATOR = '.';
    private String prefix = null;
    private String suffix = null;

    public X509AttributeName(String name) {
        int i = name.indexOf(46);
        if (i < 0) {
            this.prefix = name;
            return;
        }
        this.prefix = name.substring(0, i);
        this.suffix = name.substring(i + 1);
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getSuffix() {
        return this.suffix;
    }
}
