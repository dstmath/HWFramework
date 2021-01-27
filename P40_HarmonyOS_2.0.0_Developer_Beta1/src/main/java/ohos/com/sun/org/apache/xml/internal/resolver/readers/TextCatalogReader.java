package ohos.com.sun.org.apache.xml.internal.resolver.readers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;
import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogEntry;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogException;

public class TextCatalogReader implements CatalogReader {
    protected boolean caseSensitive = false;
    protected InputStream catfile = null;
    protected int[] stack = new int[3];
    protected Stack tokenStack = new Stack();
    protected int top = -1;

    public void setCaseSensitive(boolean z) {
        this.caseSensitive = z;
    }

    public boolean getCaseSensitive() {
        return this.caseSensitive;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.CatalogReader
    public void readCatalog(Catalog catalog, String str) throws MalformedURLException, IOException {
        URL url;
        try {
            url = new URL(str);
        } catch (MalformedURLException unused) {
            url = new URL("file:///" + str);
        }
        try {
            readCatalog(catalog, url.openConnection().getInputStream());
        } catch (FileNotFoundException unused2) {
            catalog.getCatalogManager().debug.message(1, "Failed to load catalog, file not found", url.toString());
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.CatalogReader
    public void readCatalog(Catalog catalog, InputStream inputStream) throws MalformedURLException, IOException {
        Vector vector;
        String str;
        this.catfile = inputStream;
        if (this.catfile != null) {
            loop0:
            while (true) {
                vector = null;
                while (true) {
                    try {
                        String nextToken = nextToken();
                        if (nextToken == null) {
                            break loop0;
                        }
                        if (this.caseSensitive) {
                            str = nextToken;
                        } else {
                            str = nextToken.toUpperCase();
                        }
                        try {
                            int entryArgCount = CatalogEntry.getEntryArgCount(CatalogEntry.getEntryType(str));
                            Vector vector2 = new Vector();
                            if (vector != null) {
                                catalog.unknownEntry(vector);
                                vector = null;
                            }
                            for (int i = 0; i < entryArgCount; i++) {
                                vector2.addElement(nextToken());
                            }
                            catalog.addEntry(new CatalogEntry(str, vector2));
                        } catch (CatalogException e) {
                            if (e.getExceptionType() == 3) {
                                if (vector == null) {
                                    vector = new Vector();
                                }
                                vector.addElement(nextToken);
                            } else if (e.getExceptionType() == 2) {
                                catalog.getCatalogManager().debug.message(1, "Invalid catalog entry", nextToken);
                            } else if (e.getExceptionType() == 8) {
                                catalog.getCatalogManager().debug.message(1, e.getMessage());
                            }
                        }
                    } catch (CatalogException e2) {
                        if (e2.getExceptionType() == 8) {
                            catalog.getCatalogManager().debug.message(1, e2.getMessage());
                            return;
                        }
                        return;
                    }
                }
            }
            if (vector != null) {
                catalog.unknownEntry(vector);
            }
            this.catfile.close();
            this.catfile = null;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        InputStream inputStream = this.catfile;
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException unused) {
            }
        }
        this.catfile = null;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0025, code lost:
        r3 = r9.catfile.read();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002b, code lost:
        if (r3 >= 0) goto L_0x002e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002d, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0030, code lost:
        if (r0 != 45) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0032, code lost:
        if (r3 != 45) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0034, code lost:
        r0 = nextChar();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0038, code lost:
        if (r2 != 45) goto L_0x003c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003a, code lost:
        if (r0 == 45) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003c, code lost:
        if (r0 <= 0) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003e, code lost:
        r2 = r0;
        r0 = nextChar();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0053, code lost:
        r4 = r9.stack;
        r5 = r9.top + 1;
        r9.top = r5;
        r4[r5] = r3;
        r3 = r9.top + 1;
        r9.top = r3;
        r4[r3] = r0;
        r0 = nextChar();
        r5 = "";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006d, code lost:
        if (r0 == 34) goto L_0x00a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0071, code lost:
        if (r0 != 39) goto L_0x0074;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0074, code lost:
        if (r0 <= 32) goto L_0x009f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0076, code lost:
        r3 = nextChar();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007a, code lost:
        if (r0 != 45) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007c, code lost:
        if (r3 != 45) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x007e, code lost:
        r1 = r9.stack;
        r2 = r9.top + 1;
        r9.top = r2;
        r1[r2] = r0;
        r0 = r9.top + 1;
        r9.top = r0;
        r1[r0] = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x008e, code lost:
        return r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x008f, code lost:
        r5 = r5.concat(new java.lang.String(new char[]{(char) r0}));
        r0 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x009f, code lost:
        return r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a0, code lost:
        r1 = nextChar();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a4, code lost:
        if (r1 == r0) goto L_0x00b5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a6, code lost:
        r5 = r5.concat(new java.lang.String(new char[]{(char) r1}));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00b5, code lost:
        return r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0046, code lost:
        continue;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0046, code lost:
        continue;
     */
    public String nextToken() throws IOException, CatalogException {
        int nextChar;
        if (!this.tokenStack.empty()) {
            return (String) this.tokenStack.pop();
        }
        do {
            int read = this.catfile.read();
            while (true) {
                int i = 32;
                if (read > 32) {
                    break;
                }
                read = this.catfile.read();
                if (read < 0) {
                    return null;
                }
            }
        } while (nextChar >= 0);
        throw new CatalogException(8, "Unterminated comment in catalog file; EOF treated as end-of-comment.");
    }

    /* access modifiers changed from: protected */
    public int nextChar() throws IOException {
        int i = this.top;
        if (i < 0) {
            return this.catfile.read();
        }
        int[] iArr = this.stack;
        this.top = i - 1;
        return iArr[i];
    }
}
