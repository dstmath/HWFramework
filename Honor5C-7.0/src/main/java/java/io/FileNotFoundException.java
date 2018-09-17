package java.io;

public class FileNotFoundException extends IOException {
    private static final long serialVersionUID = -897856973823710492L;

    public FileNotFoundException(String s) {
        super(s);
    }

    private FileNotFoundException(String path, String reason) {
        String str;
        StringBuilder append = new StringBuilder().append(path);
        if (reason == null) {
            str = "";
        } else {
            str = " (" + reason + ")";
        }
        super(append.append(str).toString());
    }
}
