package ohos.com.sun.xml.internal.stream.events;

import java.io.IOException;
import java.io.Writer;
import ohos.javax.xml.stream.Location;
import ohos.javax.xml.stream.events.StartDocument;

public class StartDocumentEvent extends DummyEvent implements StartDocument {
    protected String fEncodingScheam;
    private boolean fEncodingSchemeSet;
    protected boolean fStandalone;
    private boolean fStandaloneSet;
    protected String fSystemId;
    protected String fVersion;
    private boolean nestedCall;

    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public boolean isStartDocument() {
        return true;
    }

    public StartDocumentEvent() {
        this.fEncodingSchemeSet = false;
        this.fStandaloneSet = false;
        this.nestedCall = false;
        init("UTF-8", "1.0", true, null);
    }

    public StartDocumentEvent(String str) {
        this.fEncodingSchemeSet = false;
        this.fStandaloneSet = false;
        this.nestedCall = false;
        init(str, "1.0", true, null);
    }

    public StartDocumentEvent(String str, String str2) {
        this.fEncodingSchemeSet = false;
        this.fStandaloneSet = false;
        this.nestedCall = false;
        init(str, str2, true, null);
    }

    public StartDocumentEvent(String str, String str2, boolean z) {
        this.fEncodingSchemeSet = false;
        this.fStandaloneSet = false;
        this.nestedCall = false;
        this.fStandaloneSet = true;
        init(str, str2, z, null);
    }

    public StartDocumentEvent(String str, String str2, boolean z, Location location) {
        this.fEncodingSchemeSet = false;
        this.fStandaloneSet = false;
        this.nestedCall = false;
        this.fStandaloneSet = true;
        init(str, str2, z, location);
    }

    /* access modifiers changed from: protected */
    public void init(String str, String str2, boolean z, Location location) {
        setEventType(7);
        this.fEncodingScheam = str;
        this.fVersion = str2;
        this.fStandalone = z;
        if (str == null || str.equals("")) {
            this.fEncodingSchemeSet = false;
            this.fEncodingScheam = "UTF-8";
        } else {
            this.fEncodingSchemeSet = true;
        }
        this.fLocation = location;
    }

    public String getSystemId() {
        if (this.fLocation == null) {
            return "";
        }
        return this.fLocation.getSystemId();
    }

    public String getCharacterEncodingScheme() {
        return this.fEncodingScheam;
    }

    public boolean isStandalone() {
        return this.fStandalone;
    }

    public String getVersion() {
        return this.fVersion;
    }

    public void setStandalone(boolean z) {
        this.fStandaloneSet = true;
        this.fStandalone = z;
    }

    public void setStandalone(String str) {
        this.fStandaloneSet = true;
        if (str == null) {
            this.fStandalone = true;
        } else if (str.equals("yes")) {
            this.fStandalone = true;
        } else {
            this.fStandalone = false;
        }
    }

    public boolean encodingSet() {
        return this.fEncodingSchemeSet;
    }

    public boolean standaloneSet() {
        return this.fStandaloneSet;
    }

    public void setEncoding(String str) {
        this.fEncodingScheam = str;
    }

    /* access modifiers changed from: package-private */
    public void setDeclaredEncoding(boolean z) {
        this.fEncodingSchemeSet = z;
    }

    public void setVersion(String str) {
        this.fVersion = str;
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        this.fEncodingScheam = "UTF-8";
        this.fStandalone = true;
        this.fVersion = "1.0";
        this.fEncodingSchemeSet = false;
        this.fStandaloneSet = false;
    }

    public String toString() {
        String str = ("<?xml version=\"" + this.fVersion + "\"") + " encoding='" + this.fEncodingScheam + "'";
        if (!this.fStandaloneSet) {
            return str + "?>";
        } else if (this.fStandalone) {
            return str + " standalone='yes'?>";
        } else {
            return str + " standalone='no'?>";
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.xml.internal.stream.events.DummyEvent
    public void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
        writer.write(toString());
    }
}
