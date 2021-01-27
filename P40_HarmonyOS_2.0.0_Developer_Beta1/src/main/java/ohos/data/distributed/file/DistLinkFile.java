package ohos.data.distributed.file;

import java.io.IOException;

public class DistLinkFile extends DistFile {
    private static final long serialVersionUID = -5709294896652123894L;
    private final String path;
    private final String target;

    public DistLinkFile(String str) {
        super(str);
        this.path = str;
        this.target = "";
    }

    DistLinkFile(String str, String str2) {
        super(str);
        this.path = str;
        this.target = str2;
    }

    @Override // java.io.File
    public boolean createNewFile() throws IOException {
        String str;
        String str2 = this.path;
        if (str2 != null && !str2.isEmpty() && (str = this.target) != null && !str.isEmpty()) {
            return DistFileSystem.createLinkFile(this.path, this.target) == 0;
        }
        throw new IOException("Invalid arguments");
    }
}
