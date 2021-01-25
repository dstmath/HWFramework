package ohos.bundle;

public class ModuleInfo {
    private String moduleName = "";
    private String moduleSourceDir = "";

    public ModuleInfo() {
    }

    public ModuleInfo(String str, String str2) {
        this.moduleName = str;
        this.moduleSourceDir = str2;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public String getModuleSourceDir() {
        return this.moduleSourceDir;
    }
}
