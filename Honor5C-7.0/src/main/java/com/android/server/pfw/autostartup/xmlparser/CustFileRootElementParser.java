package com.android.server.pfw.autostartup.xmlparser;

class CustFileRootElementParser extends AbsRootElementParser {
    CustFileRootElementParser() {
    }

    boolean needParsePreciseNode() {
        return true;
    }
}
