package java.nio.file.attribute;

import java.io.IOException;
import java.util.List;

public interface AclFileAttributeView extends FileOwnerAttributeView {
    List<AclEntry> getAcl() throws IOException;

    String name();

    void setAcl(List<AclEntry> list) throws IOException;
}
