package com.huawei.ace.runtime;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public final class AceEnv {
    public static final int ACE_PLATFORM_ANDROID = 0;
    public static final int ACE_PLATFORM_HARMONY = 1;
    public static final int ACE_PLATFORM_INVALID = -1;
    private static final AceEnv INSTANCE = new AceEnv();
    private static final String LOG_TAG = "AceEnv";
    public static final int TYPE_AGP_COMPONENT = 2;
    public static final int TYPE_CARD_NATIVE_VIEW = 3;
    public static final int TYPE_NATIVE_VIEW = 1;
    public static final int TYPE_SURFACE_VIEW = 0;
    private int containerType = 1;
    private IAceViewCreator creator;
    private AtomicInteger instanceIdGenerator = new AtomicInteger(1);
    private boolean isLoadSuccess = false;

    private native void nativeSetupFirstFrameHandler(int i);

    private native void nativeSetupNatives(int i, int i2);

    private AceEnv() {
        ALog.i(LOG_TAG, "AceEnv init start.");
        this.isLoadSuccess = LibraryLoader.loadJniLibrary();
        if (!this.isLoadSuccess) {
            ALog.e(LOG_TAG, "LoadLibrary failed.");
        }
    }

    public static AceEnv getInstance() {
        return INSTANCE;
    }

    public void setLibraryLoaded() {
        this.isLoadSuccess = true;
    }

    public boolean isLibraryLoaded() {
        return this.isLoadSuccess;
    }

    public static void setViewCreator(IAceViewCreator iAceViewCreator) {
        INSTANCE.setViewCreatorInner(iAceViewCreator);
    }

    private void setViewCreatorInner(IAceViewCreator iAceViewCreator) {
        this.creator = iAceViewCreator;
    }

    public static void setContainerType(int i) {
        INSTANCE.setContainerTypeInner(i);
    }

    private void setContainerTypeInner(int i) {
        if (i >= 0 && i <= 2) {
            this.containerType = i;
        }
    }

    public static boolean isJSONContainerType() {
        return INSTANCE.isJSONContainerTypeInner();
    }

    private boolean isJSONContainerTypeInner() {
        return this.containerType == 0;
    }

    public static AceContainer createContainer(AceEventCallback aceEventCallback, AcePluginMessage acePluginMessage, int i) {
        return createContainer(aceEventCallback, acePluginMessage, i, "");
    }

    public static AceContainer createContainer(AceEventCallback aceEventCallback, AcePluginMessage acePluginMessage, int i, String str) {
        return INSTANCE.createContainerInner(aceEventCallback, acePluginMessage, i, str);
    }

    private AceContainer createContainerInner(AceEventCallback aceEventCallback, AcePluginMessage acePluginMessage, int i, String str) {
        IAceViewCreator iAceViewCreator = this.creator;
        if (iAceViewCreator == null) {
            return null;
        }
        return new AceContainer(i, this.containerType, iAceViewCreator, aceEventCallback, acePluginMessage, str);
    }

    public static void destroyContainer(AceContainer aceContainer) {
        INSTANCE.destroyContainerInner(aceContainer);
    }

    private void destroyContainerInner(AceContainer aceContainer) {
        if (aceContainer != null) {
            aceContainer.destroyContainer();
        }
    }

    public int generateInstanceId() {
        return this.instanceIdGenerator.getAndIncrement();
    }

    public static void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        INSTANCE.dumpInner(str, fileDescriptor, printWriter, strArr);
    }

    private void dumpInner(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        DumpHelper.dump(str, fileDescriptor, printWriter, strArr);
    }

    public void setupFirstFrameHandler(int i) {
        nativeSetupFirstFrameHandler(i);
    }

    public void setupNatives(int i, int i2) {
        nativeSetupNatives(i, i2);
    }
}
