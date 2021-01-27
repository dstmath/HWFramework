package ohos.data.distributed.file;

import java.io.IOException;

public class DistAgentFile extends DistFile {
    private static final long serialVersionUID = 2737905412410241777L;
    private final String path;
    private final String target;
    private final String targetDevice;

    DistAgentFile(String str, String str2, String str3) {
        super(str);
        this.path = str;
        this.target = str2;
        this.targetDevice = str3;
    }

    public DistAgentFile(String str) {
        this(str, "", "");
    }

    @Override // java.io.File
    public boolean createNewFile() throws IOException {
        String str;
        String str2 = this.path;
        if (str2 != null && !str2.isEmpty() && (str = this.target) != null && !str.isEmpty()) {
            return DistFileSystem.createAgentFile(this.path, this.target, this.targetDevice) == 0;
        }
        throw new IOException("Invalid arguments");
    }
}
