package com.android.server.pfw.autostartup.xmlparser;

class AssetFileRootElementParser extends AbsRootElementParser {
    AssetFileRootElementParser() {
    }

    boolean needParsePreciseNode() {
        return true;
    }
}
