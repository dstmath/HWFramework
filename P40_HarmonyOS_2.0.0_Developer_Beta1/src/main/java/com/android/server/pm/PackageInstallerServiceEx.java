package com.android.server.pm;

import java.io.File;
import java.io.IOException;

public class PackageInstallerServiceEx {
    public static void prepareStageDir(File stageDir) throws IOException {
        PackageInstallerService.prepareStageDir(stageDir);
    }
}
