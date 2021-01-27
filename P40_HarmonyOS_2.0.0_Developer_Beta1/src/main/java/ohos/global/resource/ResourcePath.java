package ohos.global.resource;

public class ResourcePath {
    private String aaName;
    private String resourcePath;
    private PathType type;

    public enum PathType {
        BASE,
        SPLITS,
        OVERLAYS
    }

    public String getResourcePath() {
        return this.resourcePath;
    }

    public String getAaName() {
        return this.aaName;
    }

    public void setResourcePath(String str, String str2) {
        this.resourcePath = str;
        this.aaName = str2;
    }

    public PathType getType() {
        return this.type;
    }

    public void setType(PathType pathType) {
        this.type = pathType;
    }
}
