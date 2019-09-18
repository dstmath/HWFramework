package java.io;

public class FileNotFoundException extends IOException {
    private static final long serialVersionUID = -897856973823710492L;

    public FileNotFoundException() {
    }

    public FileNotFoundException(String s) {
        super(s);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    private FileNotFoundException(String path, String reason) {
        super(r0.toString());
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        if (reason == null) {
            str = "";
        } else {
            str = " (" + reason + ")";
        }
        sb.append(str);
    }
}
