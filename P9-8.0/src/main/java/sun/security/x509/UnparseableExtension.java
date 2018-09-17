package sun.security.x509;

import sun.misc.HexDumpEncoder;

/* compiled from: CertificateExtensions */
class UnparseableExtension extends Extension {
    private String name = "";
    private Throwable why;

    public UnparseableExtension(Extension ext, Throwable why) {
        super(ext);
        try {
            Class<?> extClass = OIDMap.getClass(ext.getExtensionId());
            if (extClass != null) {
                this.name = ((String) extClass.getDeclaredField("NAME").get(null)) + " ";
            }
        } catch (Exception e) {
        }
        this.why = why;
    }

    public String toString() {
        return super.toString() + "Unparseable " + this.name + "extension due to\n" + this.why + "\n\n" + new HexDumpEncoder().encodeBuffer(getExtensionValue());
    }
}
