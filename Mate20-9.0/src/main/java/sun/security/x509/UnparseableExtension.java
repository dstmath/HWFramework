package sun.security.x509;

import java.lang.reflect.Field;
import sun.misc.HexDumpEncoder;

/* compiled from: CertificateExtensions */
class UnparseableExtension extends Extension {
    private String name = "";
    private Throwable why;

    public UnparseableExtension(Extension ext, Throwable why2) {
        super(ext);
        try {
            Class<?> extClass = OIDMap.getClass(ext.getExtensionId());
            if (extClass != null) {
                Field field = extClass.getDeclaredField("NAME");
                this.name = ((String) field.get(null)) + " ";
            }
        } catch (Exception e) {
        }
        this.why = why2;
    }

    public String toString() {
        return super.toString() + "Unparseable " + this.name + "extension due to\n" + this.why + "\n\n" + new HexDumpEncoder().encodeBuffer(getExtensionValue());
    }
}
