package ohos.bundlemgr;

public class PackageInstalledStatus {
    private String shellInstalledDir;
    private int status = 1;
    private String statusMessage;

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int i) {
        this.status = i;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    public void setStatusMessage(String str) {
        this.statusMessage = str;
    }

    public String getShellInstalledDir() {
        return this.shellInstalledDir;
    }

    public void setShellInstalledDir(String str) {
        this.shellInstalledDir = str;
    }
}
