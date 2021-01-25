package ohos.miscservices.httpaccess.data;

public class FormFileData {
    private String fileName;
    private String name;
    private String type;
    private String uri;

    public String getFileName() {
        String str = this.fileName;
        return str == null ? "" : str;
    }

    public void setFileName(String str) {
        this.fileName = str;
    }

    public String getName() {
        String str = this.name;
        return str == null ? "" : str;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getUri() {
        String str = this.uri;
        return str == null ? "" : str;
    }

    public void setUri(String str) {
        this.uri = str;
    }

    public String getType() {
        String str = this.type;
        return str == null ? "" : str;
    }

    public void setType(String str) {
        this.type = str;
    }
}
