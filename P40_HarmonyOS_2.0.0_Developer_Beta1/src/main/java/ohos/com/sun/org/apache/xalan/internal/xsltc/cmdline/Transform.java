package ohos.com.sun.org.apache.xalan.internal.xsltc.cmdline;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.StripFilter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.DOMWSFilter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.XSLTCDTMManager;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.Parameter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output.TransletOutputHandlerFactory;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import ohos.javax.xml.parsers.SAXParserFactory;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;

public final class Transform {
    private String _className;
    private boolean _debug;
    private String _fileName;
    private SerializationHandler _handler;
    private boolean _isJarFileSpecified = false;
    private int _iterations;
    private String _jarFileSrc;
    private Vector _params = null;
    private boolean _uri;

    public Transform(String str, String str2, boolean z, boolean z2, int i) {
        this._fileName = str2;
        this._className = str;
        this._uri = z;
        this._debug = z2;
        this._iterations = i;
    }

    public String getFileName() {
        return this._fileName;
    }

    public String getClassName() {
        return this._className;
    }

    public void setParameters(Vector vector) {
        this._params = vector;
    }

    private void setJarFileInputSrc(boolean z, String str) {
        this._isJarFileSpecified = z;
        this._jarFileSrc = str;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x012d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x012e, code lost:
        r2 = r0.getException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0134, code lost:
        if (r11._debug != false) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0136, code lost:
        if (r2 != null) goto L_0x0138;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0138, code lost:
        r2.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x013b, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x013e, code lost:
        java.lang.System.err.print(new ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.RUNTIME_ERROR_KEY));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0148, code lost:
        if (r2 != null) goto L_0x014a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x014a, code lost:
        java.lang.System.err.println(r2.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0155, code lost:
        java.lang.System.err.println(r0.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0160, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0163, code lost:
        if (r11._debug != false) goto L_0x0165;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0165, code lost:
        r2.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0168, code lost:
        r2 = new ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.INVALID_URI_ERR, r11._fileName);
        java.lang.System.err.println(new ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.RUNTIME_ERROR_KEY) + r2.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x018e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0191, code lost:
        if (r11._debug != false) goto L_0x0193;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0193, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0196, code lost:
        r0 = new ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.CLASS_NOT_FOUND_ERR, r11._className);
        java.lang.System.err.println(new ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.RUNTIME_ERROR_KEY) + r0.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x01be, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x01c1, code lost:
        if (r11._debug != false) goto L_0x01c3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x01c3, code lost:
        r2.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x01c6, code lost:
        r2 = new ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.INVALID_URI_ERR, r11._fileName);
        java.lang.System.err.println(new ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.RUNTIME_ERROR_KEY) + r2.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x01ec, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x01ef, code lost:
        if (r11._debug != false) goto L_0x01f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x01f1, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x01f4, code lost:
        r0 = new ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.FILE_NOT_FOUND_ERR, r11._fileName);
        java.lang.System.err.println(new ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.RUNTIME_ERROR_KEY) + r0.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x021b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x021e, code lost:
        if (r11._debug != false) goto L_0x0220;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0220, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0223, code lost:
        java.lang.System.err.println(new ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.RUNTIME_ERROR_KEY) + r0.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0241, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0244, code lost:
        if (r11._debug != false) goto L_0x0246;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0246, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0249, code lost:
        java.lang.System.err.println(new ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg.RUNTIME_ERROR_KEY) + r0.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x001e, code lost:
        r4.setNamespaceAware(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:?, code lost:
        return;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x012d A[ExcHandler: SAXException (r0v19 'e' ohos.org.xml.sax.SAXException A[CUSTOM_DECLARE]), Splitter:B:1:0x0004] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0160 A[ExcHandler: UnknownHostException (r2v10 'e' java.net.UnknownHostException A[CUSTOM_DECLARE]), Splitter:B:1:0x0004] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x018e A[ExcHandler: ClassNotFoundException (r0v13 'e' java.lang.ClassNotFoundException A[CUSTOM_DECLARE]), Splitter:B:1:0x0004] */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x01be A[ExcHandler: MalformedURLException (r2v5 'e' java.net.MalformedURLException A[CUSTOM_DECLARE]), Splitter:B:1:0x0004] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x01ec A[ExcHandler: FileNotFoundException (r0v7 'e' java.io.FileNotFoundException A[CUSTOM_DECLARE]), Splitter:B:1:0x0004] */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x021b A[ExcHandler: RuntimeException (r0v4 'e' java.lang.RuntimeException A[CUSTOM_DECLARE]), Splitter:B:1:0x0004] */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0241 A[ExcHandler: TransletException (r0v1 'e' ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException A[CUSTOM_DECLARE]), Splitter:B:1:0x0004] */
    private void doTransform() {
        try {
            AbstractTranslet abstractTranslet = (AbstractTranslet) ObjectFactory.findProviderClass(this._className, true).newInstance();
            abstractTranslet.postInitialization();
            SAXParserFactory newInstance = SAXParserFactory.newInstance();
            newInstance.setFeature("http://xml.org/sax/features/namespaces", true);
            DOMEnhancedForDTM dOMEnhancedForDTM = (DOMEnhancedForDTM) XSLTCDTMManager.createNewDTMManagerInstance().getDTM(new SAXSource(newInstance.newSAXParser().getXMLReader(), new InputSource(this._fileName)), false, abstractTranslet instanceof StripFilter ? new DOMWSFilter(abstractTranslet) : null, true, false, abstractTranslet.hasIdCall());
            dOMEnhancedForDTM.setDocumentURI(this._fileName);
            abstractTranslet.prepassDocument(dOMEnhancedForDTM);
            int size = this._params.size();
            for (int i = 0; i < size; i++) {
                Parameter parameter = (Parameter) this._params.elementAt(i);
                abstractTranslet.addParameter(parameter._name, parameter._value);
            }
            TransletOutputHandlerFactory newInstance2 = TransletOutputHandlerFactory.newInstance();
            newInstance2.setOutputType(0);
            newInstance2.setEncoding(abstractTranslet._encoding);
            newInstance2.setOutputMethod(abstractTranslet._method);
            if (this._iterations == -1) {
                abstractTranslet.transform(dOMEnhancedForDTM, newInstance2.getSerializationHandler());
            } else if (this._iterations > 0) {
                long currentTimeMillis = System.currentTimeMillis();
                for (int i2 = 0; i2 < this._iterations; i2++) {
                    abstractTranslet.transform(dOMEnhancedForDTM, newInstance2.getSerializationHandler());
                }
                System.err.println("\n<!--");
                PrintStream printStream = System.err;
                StringBuilder sb = new StringBuilder();
                sb.append("  transform  = ");
                double currentTimeMillis2 = (double) (System.currentTimeMillis() - currentTimeMillis);
                sb.append(currentTimeMillis2 / ((double) this._iterations));
                sb.append(" ms");
                printStream.println(sb.toString());
                System.err.println("  throughput = " + (1000.0d / (currentTimeMillis2 / ((double) this._iterations))) + " tps");
                System.err.println("-->");
            }
        } catch (TransletException e) {
        } catch (RuntimeException e2) {
        } catch (FileNotFoundException e3) {
        } catch (MalformedURLException e4) {
        } catch (ClassNotFoundException e5) {
        } catch (UnknownHostException e6) {
        } catch (SAXException e7) {
        } catch (Exception e8) {
            if (this._debug) {
                e8.printStackTrace();
            }
            System.err.println(new ErrorMsg(ErrorMsg.RUNTIME_ERROR_KEY) + e8.getMessage());
        }
    }

    public static void printUsage() {
        System.err.println(new ErrorMsg(ErrorMsg.TRANSFORM_USAGE_STR));
    }

    public static void main(String[] strArr) {
        try {
            if (strArr.length > 0) {
                int i = -1;
                String str = null;
                int i2 = 0;
                boolean z = false;
                boolean z2 = false;
                boolean z3 = false;
                while (i2 < strArr.length && strArr[i2].charAt(0) == '-') {
                    if (strArr[i2].equals("-u")) {
                        z2 = true;
                    } else if (strArr[i2].equals("-x")) {
                        z3 = true;
                    } else if (strArr[i2].equals("-j")) {
                        i2++;
                        str = strArr[i2];
                        z = true;
                    } else if (strArr[i2].equals("-n")) {
                        i2++;
                        try {
                            i = Integer.parseInt(strArr[i2]);
                        } catch (NumberFormatException unused) {
                        }
                    } else {
                        printUsage();
                    }
                    i2++;
                }
                if (strArr.length - i2 < 2) {
                    printUsage();
                }
                Transform transform = new Transform(strArr[i2 + 1], strArr[i2], z2, z3, i);
                transform.setJarFileInputSrc(z, str);
                Vector vector = new Vector();
                int i3 = i2 + 2;
                while (i3 < strArr.length) {
                    int indexOf = strArr[i3].indexOf(61);
                    if (indexOf > 0) {
                        vector.addElement(new Parameter(strArr[i3].substring(0, indexOf), strArr[i3].substring(indexOf + 1)));
                    } else {
                        printUsage();
                    }
                    i3++;
                }
                if (i3 == strArr.length) {
                    transform.setParameters(vector);
                    transform.doTransform();
                    return;
                }
                return;
            }
            printUsage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
