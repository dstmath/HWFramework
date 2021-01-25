package ohos.com.sun.org.apache.xalan.internal.xslt;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ListResourceBundle;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.Version;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xalan.internal.utils.ConfigurationError;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xml.internal.utils.DefaultErrorHandler;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xml.internal.utils.res.XResourceBundle;
import ohos.javax.xml.parsers.DocumentBuilder;
import ohos.javax.xml.parsers.DocumentBuilderFactory;
import ohos.javax.xml.parsers.FactoryConfigurationError;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.javax.xml.parsers.SAXParserFactory;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.Templates;
import ohos.javax.xml.transform.Transformer;
import ohos.javax.xml.transform.TransformerConfigurationException;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.transform.TransformerFactory;
import ohos.javax.xml.transform.TransformerFactoryConfigurationError;
import ohos.javax.xml.transform.URIResolver;
import ohos.javax.xml.transform.dom.DOMResult;
import ohos.javax.xml.transform.dom.DOMSource;
import ohos.javax.xml.transform.sax.SAXResult;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.javax.xml.transform.sax.SAXTransformerFactory;
import ohos.javax.xml.transform.stream.StreamResult;
import ohos.javax.xml.transform.stream.StreamSource;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentFragment;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.helpers.XMLReaderFactory;

public class Process {
    protected static void printArgOptions(ResourceBundle resourceBundle) {
        System.out.println(resourceBundle.getString("xslProc_option"));
        PrintStream printStream = System.out;
        printStream.println("\n\t\t\t" + resourceBundle.getString("xslProc_common_options") + "\n");
        System.out.println(resourceBundle.getString("optionXSLTC"));
        System.out.println(resourceBundle.getString("optionIN"));
        System.out.println(resourceBundle.getString("optionXSL"));
        System.out.println(resourceBundle.getString("optionOUT"));
        System.out.println(resourceBundle.getString("optionV"));
        System.out.println(resourceBundle.getString("optionEDUMP"));
        System.out.println(resourceBundle.getString("optionXML"));
        System.out.println(resourceBundle.getString("optionTEXT"));
        System.out.println(resourceBundle.getString("optionHTML"));
        System.out.println(resourceBundle.getString("optionPARAM"));
        System.out.println(resourceBundle.getString("optionMEDIA"));
        System.out.println(resourceBundle.getString("optionFLAVOR"));
        System.out.println(resourceBundle.getString("optionDIAG"));
        System.out.println(resourceBundle.getString("optionURIRESOLVER"));
        System.out.println(resourceBundle.getString("optionENTITYRESOLVER"));
        waitForReturnKey(resourceBundle);
        System.out.println(resourceBundle.getString("optionCONTENTHANDLER"));
        System.out.println(resourceBundle.getString("optionSECUREPROCESSING"));
        PrintStream printStream2 = System.out;
        printStream2.println("\n\t\t\t" + resourceBundle.getString("xslProc_xsltc_options") + "\n");
        System.out.println(resourceBundle.getString("optionXO"));
        waitForReturnKey(resourceBundle);
        System.out.println(resourceBundle.getString("optionXD"));
        System.out.println(resourceBundle.getString("optionXJ"));
        System.out.println(resourceBundle.getString("optionXP"));
        System.out.println(resourceBundle.getString("optionXN"));
        System.out.println(resourceBundle.getString("optionXX"));
        System.out.println(resourceBundle.getString("optionXT"));
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x058f: APUT  (r2v11 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r7v3 java.lang.String) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x05a6: APUT  (r3v43 java.lang.Object[]), (0 ??[int, short, byte, char]), (r11v1 java.lang.String) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x07bb: APUT  (r0v18 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r17v2 java.lang.String) */
    /* JADX WARNING: Code restructure failed: missing block: B:327:0x06aa, code lost:
        r3 = null;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:328:? A[ExcHandler: AbstractMethodError | NoSuchMethodError (unused java.lang.Throwable), SYNTHETIC, Splitter:B:320:0x0698] */
    /* JADX WARNING: Removed duplicated region for block: B:419:0x07f4 A[LOOP:4: B:417:0x07f0->B:419:0x07f4, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:427:0x080b  */
    /* JADX WARNING: Removed duplicated region for block: B:428:0x080f  */
    /* JADX WARNING: Removed duplicated region for block: B:430:0x0846  */
    /* JADX WARNING: Removed duplicated region for block: B:433:0x0852  */
    /* JADX WARNING: Removed duplicated region for block: B:446:? A[RETURN, SYNTHETIC] */
    public static void _main(String[] strArr) {
        String str;
        TransformerFactory transformerFactory;
        String str2;
        boolean z;
        PrintWriter printWriter;
        PrintWriter printWriter2;
        Exception th;
        PrintWriter printWriter3;
        Templates templates;
        String str3;
        PrintWriter printWriter4;
        StreamResult streamResult;
        String str4;
        XMLReader xMLReader;
        PrintWriter printWriter5;
        String str5;
        String str6;
        int i;
        String str7;
        int i2;
        char c;
        char c2;
        int i3;
        int i4;
        int i5;
        String str8;
        char c3;
        char c4;
        String str9 = "ER_NOT_SUCCESSFUL";
        PrintWriter printWriter6 = new PrintWriter((OutputStream) System.err, true);
        ListResourceBundle resourceBundle = SecuritySupport.getResourceBundle(XResourceBundle.ERROR_RESOURCES);
        if (strArr.length < 1) {
            printArgOptions(resourceBundle);
            return;
        }
        int i6 = 0;
        while (true) {
            str = "-XSLTC";
            if (i6 >= strArr.length) {
                break;
            }
            str.equalsIgnoreCase(strArr[i6]);
            i6++;
        }
        Properties properties = System.getProperties();
        properties.put("ohos.javax.xml.transform.TransformerFactory", "ohos.com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
        System.setProperties(properties);
        String str10 = null;
        try {
            transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setErrorListener(new DefaultErrorHandler());
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace(printWriter6);
            String createMessage = XSLMessages.createMessage(str9, null);
            printWriter6.println(createMessage);
            doExit(createMessage);
            transformerFactory = null;
        }
        Vector vector = new Vector();
        String str11 = null;
        String str12 = null;
        String str13 = null;
        String str14 = null;
        String str15 = null;
        URIResolver uRIResolver = null;
        EntityResolver entityResolver = null;
        ContentHandler contentHandler = null;
        String str16 = "s2s";
        int i7 = 0;
        boolean z2 = false;
        boolean z3 = false;
        boolean z4 = false;
        while (i7 < strArr.length) {
            if (str.equalsIgnoreCase(strArr[i7])) {
                str5 = str9;
                printWriter5 = printWriter6;
                str7 = str10;
                str6 = str;
            } else {
                str6 = str;
                if ("-INDENT".equalsIgnoreCase(strArr[i7])) {
                    int i8 = i7 + 1;
                    if (i8 < strArr.length) {
                        str5 = str9;
                        if (strArr[i8].charAt(0) != '-') {
                            Integer.parseInt(strArr[i8]);
                            i7 = i8;
                        }
                    } else {
                        str5 = str9;
                    }
                    printWriter5 = printWriter6;
                } else {
                    str5 = str9;
                    if ("-IN".equalsIgnoreCase(strArr[i7])) {
                        int i9 = i7 + 1;
                        if (i9 < strArr.length) {
                            str7 = str10;
                            if (strArr[i9].charAt(0) != '-') {
                                i7 = i9;
                                printWriter5 = printWriter6;
                                str11 = strArr[i9];
                            }
                        } else {
                            str7 = str10;
                        }
                        System.err.println(XSLMessages.createMessage("ER_MISSING_ARG_FOR_OPTION", new Object[]{"-IN"}));
                        printWriter5 = printWriter6;
                    } else {
                        str7 = str10;
                        if ("-MEDIA".equalsIgnoreCase(strArr[i7])) {
                            int i10 = i7 + 1;
                            if (i10 < strArr.length) {
                                i7 = i10;
                                printWriter5 = printWriter6;
                                str10 = strArr[i10];
                            } else {
                                System.err.println(XSLMessages.createMessage("ER_MISSING_ARG_FOR_OPTION", new Object[]{"-MEDIA"}));
                            }
                        } else if ("-OUT".equalsIgnoreCase(strArr[i7])) {
                            int i11 = i7 + 1;
                            if (i11 < strArr.length) {
                                c4 = 0;
                                if (strArr[i11].charAt(0) != '-') {
                                    i7 = i11;
                                    printWriter5 = printWriter6;
                                    str14 = strArr[i11];
                                }
                            } else {
                                c4 = 0;
                            }
                            PrintStream printStream = System.err;
                            Object[] objArr = new Object[1];
                            objArr[c4] = "-OUT";
                            printStream.println(XSLMessages.createMessage("ER_MISSING_ARG_FOR_OPTION", objArr));
                        } else if ("-XSL".equalsIgnoreCase(strArr[i7])) {
                            int i12 = i7 + 1;
                            if (i12 < strArr.length) {
                                c3 = 0;
                                if (strArr[i12].charAt(0) != '-') {
                                    i7 = i12;
                                    printWriter5 = printWriter6;
                                    str12 = strArr[i12];
                                }
                            } else {
                                c3 = 0;
                            }
                            PrintStream printStream2 = System.err;
                            Object[] objArr2 = new Object[1];
                            objArr2[c3] = "-XSL";
                            printStream2.println(XSLMessages.createMessage("ER_MISSING_ARG_FOR_OPTION", objArr2));
                        } else if ("-FLAVOR".equalsIgnoreCase(strArr[i7])) {
                            int i13 = i7 + 1;
                            if (i13 < strArr.length) {
                                i7 = i13;
                                printWriter5 = printWriter6;
                                str16 = strArr[i13];
                            } else {
                                System.err.println(XSLMessages.createMessage("ER_MISSING_ARG_FOR_OPTION", new Object[]{"-FLAVOR"}));
                            }
                        } else if ("-PARAM".equalsIgnoreCase(strArr[i7])) {
                            if (i7 + 2 < strArr.length) {
                                int i14 = i7 + 1;
                                vector.addElement(strArr[i14]);
                                i = 1;
                                i7 = i14 + 1;
                                vector.addElement(strArr[i7]);
                                printWriter5 = printWriter6;
                                str10 = str7;
                                i7 += i;
                                str = str6;
                                str9 = str5;
                                printWriter6 = printWriter5;
                            } else {
                                System.err.println(XSLMessages.createMessage("ER_MISSING_ARG_FOR_OPTION", new Object[]{"-PARAM"}));
                            }
                        } else if (!"-E".equalsIgnoreCase(strArr[i7])) {
                            if ("-V".equalsIgnoreCase(strArr[i7])) {
                                printWriter6.println(resourceBundle.getString("version") + Version.getVersion() + ", " + resourceBundle.getString("version2"));
                            } else if (!"-Q".equalsIgnoreCase(strArr[i7])) {
                                if ("-DIAG".equalsIgnoreCase(strArr[i7])) {
                                    printWriter5 = printWriter6;
                                    str10 = str7;
                                    i = 1;
                                    z3 = true;
                                } else {
                                    if ("-XML".equalsIgnoreCase(strArr[i7])) {
                                        str8 = "xml";
                                    } else if ("-TEXT".equalsIgnoreCase(strArr[i7])) {
                                        str8 = "text";
                                    } else if ("-HTML".equalsIgnoreCase(strArr[i7])) {
                                        str8 = "html";
                                    } else if ("-EDUMP".equalsIgnoreCase(strArr[i7])) {
                                        int i15 = i7 + 1;
                                        if (i15 >= strArr.length || strArr[i15].charAt(0) == '-') {
                                            printWriter5 = printWriter6;
                                        } else {
                                            i7 = i15;
                                            printWriter5 = printWriter6;
                                            str13 = strArr[i15];
                                        }
                                        str10 = str7;
                                        i = 1;
                                        z4 = true;
                                    } else if ("-URIRESOLVER".equalsIgnoreCase(strArr[i7])) {
                                        int i16 = i7 + 1;
                                        if (i16 < strArr.length) {
                                            try {
                                                try {
                                                    URIResolver uRIResolver2 = (URIResolver) ObjectFactory.newInstance(strArr[i16], true);
                                                    try {
                                                        transformerFactory.setURIResolver(uRIResolver2);
                                                        i7 = i16;
                                                        printWriter5 = printWriter6;
                                                        uRIResolver = uRIResolver2;
                                                    } catch (ConfigurationError unused) {
                                                        uRIResolver = uRIResolver2;
                                                        i5 = 1;
                                                        Object[] objArr3 = new Object[i5];
                                                        objArr3[0] = "-URIResolver";
                                                        String createMessage2 = XSLMessages.createMessage("ER_CLASS_NOT_FOUND_FOR_OPTION", objArr3);
                                                        System.err.println(createMessage2);
                                                        doExit(createMessage2);
                                                        i7 = i16;
                                                        printWriter5 = printWriter6;
                                                        i = i5;
                                                        str10 = str7;
                                                        i7 += i;
                                                        str = str6;
                                                        str9 = str5;
                                                        printWriter6 = printWriter5;
                                                    }
                                                } catch (ConfigurationError unused2) {
                                                    i5 = 1;
                                                    Object[] objArr32 = new Object[i5];
                                                    objArr32[0] = "-URIResolver";
                                                    String createMessage22 = XSLMessages.createMessage("ER_CLASS_NOT_FOUND_FOR_OPTION", objArr32);
                                                    System.err.println(createMessage22);
                                                    doExit(createMessage22);
                                                    i7 = i16;
                                                    printWriter5 = printWriter6;
                                                    i = i5;
                                                    str10 = str7;
                                                    i7 += i;
                                                    str = str6;
                                                    str9 = str5;
                                                    printWriter6 = printWriter5;
                                                }
                                            } catch (ConfigurationError unused3) {
                                                i5 = 1;
                                                Object[] objArr322 = new Object[i5];
                                                objArr322[0] = "-URIResolver";
                                                String createMessage222 = XSLMessages.createMessage("ER_CLASS_NOT_FOUND_FOR_OPTION", objArr322);
                                                System.err.println(createMessage222);
                                                doExit(createMessage222);
                                                i7 = i16;
                                                printWriter5 = printWriter6;
                                                i = i5;
                                                str10 = str7;
                                                i7 += i;
                                                str = str6;
                                                str9 = str5;
                                                printWriter6 = printWriter5;
                                            }
                                        } else {
                                            String createMessage3 = XSLMessages.createMessage("ER_MISSING_ARG_FOR_OPTION", new Object[]{"-URIResolver"});
                                            System.err.println(createMessage3);
                                            doExit(createMessage3);
                                        }
                                    } else {
                                        if ("-ENTITYRESOLVER".equalsIgnoreCase(strArr[i7])) {
                                            i3 = i7 + 1;
                                            if (i3 < strArr.length) {
                                                try {
                                                    i4 = 1;
                                                    try {
                                                        i7 = i3;
                                                        printWriter5 = printWriter6;
                                                        entityResolver = (EntityResolver) ObjectFactory.newInstance(strArr[i3], true);
                                                    } catch (ConfigurationError unused4) {
                                                        Object[] objArr4 = new Object[i4];
                                                        objArr4[0] = "-EntityResolver";
                                                        String createMessage4 = XSLMessages.createMessage("ER_CLASS_NOT_FOUND_FOR_OPTION", objArr4);
                                                        System.err.println(createMessage4);
                                                        doExit(createMessage4);
                                                        i7 = i3;
                                                        printWriter5 = printWriter6;
                                                        i = i4;
                                                        str10 = str7;
                                                        i7 += i;
                                                        str = str6;
                                                        str9 = str5;
                                                        printWriter6 = printWriter5;
                                                    }
                                                } catch (ConfigurationError unused5) {
                                                    i4 = 1;
                                                    Object[] objArr42 = new Object[i4];
                                                    objArr42[0] = "-EntityResolver";
                                                    String createMessage42 = XSLMessages.createMessage("ER_CLASS_NOT_FOUND_FOR_OPTION", objArr42);
                                                    System.err.println(createMessage42);
                                                    doExit(createMessage42);
                                                    i7 = i3;
                                                    printWriter5 = printWriter6;
                                                    i = i4;
                                                    str10 = str7;
                                                    i7 += i;
                                                    str = str6;
                                                    str9 = str5;
                                                    printWriter6 = printWriter5;
                                                }
                                            } else {
                                                String createMessage5 = XSLMessages.createMessage("ER_MISSING_ARG_FOR_OPTION", new Object[]{"-EntityResolver"});
                                                System.err.println(createMessage5);
                                                doExit(createMessage5);
                                            }
                                        } else if ("-CONTENTHANDLER".equalsIgnoreCase(strArr[i7])) {
                                            i3 = i7 + 1;
                                            if (i3 < strArr.length) {
                                                try {
                                                    i4 = 1;
                                                    try {
                                                        i7 = i3;
                                                        printWriter5 = printWriter6;
                                                        contentHandler = (ContentHandler) ObjectFactory.newInstance(strArr[i3], true);
                                                    } catch (ConfigurationError unused6) {
                                                        Object[] objArr5 = new Object[i4];
                                                        objArr5[0] = "-ContentHandler";
                                                        String createMessage6 = XSLMessages.createMessage("ER_CLASS_NOT_FOUND_FOR_OPTION", objArr5);
                                                        System.err.println(createMessage6);
                                                        doExit(createMessage6);
                                                        i7 = i3;
                                                        printWriter5 = printWriter6;
                                                        i = i4;
                                                        str10 = str7;
                                                        i7 += i;
                                                        str = str6;
                                                        str9 = str5;
                                                        printWriter6 = printWriter5;
                                                    }
                                                } catch (ConfigurationError unused7) {
                                                    i4 = 1;
                                                    Object[] objArr52 = new Object[i4];
                                                    objArr52[0] = "-ContentHandler";
                                                    String createMessage62 = XSLMessages.createMessage("ER_CLASS_NOT_FOUND_FOR_OPTION", objArr52);
                                                    System.err.println(createMessage62);
                                                    doExit(createMessage62);
                                                    i7 = i3;
                                                    printWriter5 = printWriter6;
                                                    i = i4;
                                                    str10 = str7;
                                                    i7 += i;
                                                    str = str6;
                                                    str9 = str5;
                                                    printWriter6 = printWriter5;
                                                }
                                            } else {
                                                String createMessage7 = XSLMessages.createMessage("ER_MISSING_ARG_FOR_OPTION", new Object[]{"-ContentHandler"});
                                                System.err.println(createMessage7);
                                                doExit(createMessage7);
                                            }
                                        } else if ("-XO".equalsIgnoreCase(strArr[i7])) {
                                            int i17 = i7 + 1;
                                            if (i17 >= strArr.length || strArr[i17].charAt(0) == '-') {
                                                transformerFactory.setAttribute(TransformerFactoryImpl.GENERATE_TRANSLET, "true");
                                            } else {
                                                transformerFactory.setAttribute(TransformerFactoryImpl.GENERATE_TRANSLET, "true");
                                                transformerFactory.setAttribute(TransformerFactoryImpl.TRANSLET_NAME, strArr[i17]);
                                                i7 = i17;
                                                printWriter5 = printWriter6;
                                            }
                                        } else {
                                            printWriter5 = printWriter6;
                                            if ("-XD".equalsIgnoreCase(strArr[i7])) {
                                                i2 = i7 + 1;
                                                if (i2 < strArr.length) {
                                                    c2 = 0;
                                                    if (strArr[i2].charAt(0) != '-') {
                                                        transformerFactory.setAttribute(TransformerFactoryImpl.DESTINATION_DIRECTORY, strArr[i2]);
                                                    }
                                                } else {
                                                    c2 = 0;
                                                }
                                                PrintStream printStream3 = System.err;
                                                Object[] objArr6 = new Object[1];
                                                objArr6[c2] = "-XD";
                                                printStream3.println(XSLMessages.createMessage("ER_MISSING_ARG_FOR_OPTION", objArr6));
                                            } else if ("-XJ".equalsIgnoreCase(strArr[i7])) {
                                                i2 = i7 + 1;
                                                if (i2 >= strArr.length || strArr[i2].charAt(0) == '-') {
                                                    System.err.println(XSLMessages.createMessage("ER_MISSING_ARG_FOR_OPTION", new Object[]{"-XJ"}));
                                                } else {
                                                    transformerFactory.setAttribute(TransformerFactoryImpl.GENERATE_TRANSLET, "true");
                                                    transformerFactory.setAttribute(TransformerFactoryImpl.JAR_NAME, strArr[i2]);
                                                }
                                            } else if ("-XP".equalsIgnoreCase(strArr[i7])) {
                                                i2 = i7 + 1;
                                                if (i2 < strArr.length) {
                                                    c = 0;
                                                    if (strArr[i2].charAt(0) != '-') {
                                                        transformerFactory.setAttribute(TransformerFactoryImpl.PACKAGE_NAME, strArr[i2]);
                                                    }
                                                } else {
                                                    c = 0;
                                                }
                                                PrintStream printStream4 = System.err;
                                                Object[] objArr7 = new Object[1];
                                                objArr7[c] = "-XP";
                                                printStream4.println(XSLMessages.createMessage("ER_MISSING_ARG_FOR_OPTION", objArr7));
                                            } else if ("-XN".equalsIgnoreCase(strArr[i7])) {
                                                transformerFactory.setAttribute(TransformerFactoryImpl.ENABLE_INLINING, "true");
                                            } else if ("-XX".equalsIgnoreCase(strArr[i7])) {
                                                transformerFactory.setAttribute("debug", "true");
                                            } else if ("-XT".equalsIgnoreCase(strArr[i7])) {
                                                transformerFactory.setAttribute(TransformerFactoryImpl.AUTO_TRANSLET, "true");
                                            } else {
                                                if ("-SECURE".equalsIgnoreCase(strArr[i7])) {
                                                    i = 1;
                                                    try {
                                                        transformerFactory.setFeature(Constants.FEATURE_SECURE_PROCESSING, true);
                                                    } catch (TransformerConfigurationException unused8) {
                                                    }
                                                    z2 = true;
                                                } else {
                                                    i = 1;
                                                    System.err.println(XSLMessages.createMessage("ER_INVALID_OPTION", new Object[]{strArr[i7]}));
                                                }
                                                str10 = str7;
                                            }
                                            i7 = i2;
                                        }
                                        i = i4;
                                        str10 = str7;
                                    }
                                    str15 = str8;
                                    printWriter5 = printWriter6;
                                }
                                i7 += i;
                                str = str6;
                                str9 = str5;
                                printWriter6 = printWriter5;
                            }
                        }
                        printWriter5 = printWriter6;
                    }
                    str10 = str7;
                }
                i = 1;
                i7 += i;
                str = str6;
                str9 = str5;
                printWriter6 = printWriter5;
            }
            i = 1;
            str10 = str7;
            i7 += i;
            str = str6;
            str9 = str5;
            printWriter6 = printWriter5;
        }
        if (str11 == null && str12 == null) {
            String string = resourceBundle.getString("xslProc_no_input");
            System.err.println(string);
            doExit(string);
        }
        try {
            long currentTimeMillis = System.currentTimeMillis();
            if (str13 != null) {
                try {
                    printWriter3 = new PrintWriter(new FileWriter(str13));
                } catch (Throwable th2) {
                    th = th2;
                    str2 = str13;
                    printWriter2 = printWriter6;
                    printWriter = printWriter2;
                }
            } else {
                printWriter3 = printWriter6;
            }
            if (str12 != null) {
                try {
                    if (str16.equals("d2d")) {
                        DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
                        try {
                            newInstance.setNamespaceAware(true);
                            if (z2) {
                                try {
                                    newInstance.setFeature(Constants.FEATURE_SECURE_PROCESSING, true);
                                } catch (ParserConfigurationException unused9) {
                                }
                            }
                            templates = transformerFactory.newTemplates(new DOMSource(newInstance.newDocumentBuilder().parse(new InputSource(str12)), str12));
                        } catch (Throwable th3) {
                            th = th3;
                            printWriter2 = printWriter3;
                            z = true;
                            str2 = str13;
                            printWriter = printWriter6;
                            while (th instanceof WrappedRuntimeException) {
                            }
                            z = z4;
                            printWriter.println();
                            if (z) {
                            }
                            if (str2 != null) {
                            }
                            doExit(th.getMessage());
                            if (str2 == null) {
                            }
                        }
                    } else {
                        templates = transformerFactory.newTemplates(new StreamSource(str12));
                    }
                    str3 = str14;
                } catch (Throwable th4) {
                    th = th4;
                    printWriter2 = printWriter3;
                    str2 = str13;
                    printWriter = printWriter6;
                    z = true;
                    while (th instanceof WrappedRuntimeException) {
                    }
                    z = z4;
                    printWriter.println();
                    if (z) {
                    }
                    if (str2 != null) {
                    }
                    doExit(th.getMessage());
                    if (str2 == null) {
                    }
                }
            } else {
                str3 = str14;
                templates = null;
            }
            if (str3 != null) {
                streamResult = new StreamResult(new FileOutputStream(str3));
                streamResult.setSystemId(str3);
            } else {
                try {
                    streamResult = new StreamResult(System.out);
                } catch (Throwable th5) {
                    th = th5;
                    printWriter4 = printWriter3;
                    str2 = str13;
                    printWriter = printWriter6;
                    z = true;
                    printWriter2 = printWriter4;
                    while (th instanceof WrappedRuntimeException) {
                    }
                    z = z4;
                    printWriter.println();
                    if (z) {
                    }
                    if (str2 != null) {
                    }
                    doExit(th.getMessage());
                    if (str2 == null) {
                    }
                }
            }
            SAXTransformerFactory sAXTransformerFactory = (SAXTransformerFactory) transformerFactory;
            printWriter4 = printWriter3;
            if (templates == null) {
                try {
                    str2 = str13;
                    try {
                        Source associatedStylesheet = sAXTransformerFactory.getAssociatedStylesheet(new StreamSource(str11), str10, (String) null, (String) null);
                        if (associatedStylesheet != null) {
                            templates = transformerFactory.newTemplates(associatedStylesheet);
                        } else if (str10 != null) {
                            Object[] objArr8 = new Object[2];
                            objArr8[0] = str11;
                            try {
                                objArr8[1] = str10;
                                throw new TransformerException(XSLMessages.createMessage("ER_NO_STYLESHEET_IN_MEDIA", objArr8));
                            } catch (Throwable th6) {
                                th = th6;
                                printWriter2 = printWriter4;
                                z = true;
                                printWriter = printWriter6;
                                while (th instanceof WrappedRuntimeException) {
                                }
                                z = z4;
                                printWriter.println();
                                if (z) {
                                }
                                if (str2 != null) {
                                }
                                doExit(th.getMessage());
                                if (str2 == null) {
                                }
                            }
                        } else {
                            try {
                                Object[] objArr9 = new Object[1];
                                objArr9[0] = str11;
                                throw new TransformerException(XSLMessages.createMessage("ER_NO_STYLESHEET_PI", objArr9));
                            } catch (Throwable th7) {
                                th = th7;
                                printWriter2 = printWriter4;
                                z = true;
                                printWriter = printWriter6;
                                while (th instanceof WrappedRuntimeException) {
                                }
                                z = z4;
                                printWriter.println();
                                if (z) {
                                }
                                if (str2 != null) {
                                }
                                doExit(th.getMessage());
                                if (str2 == null) {
                                }
                            }
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        printWriter2 = printWriter4;
                        printWriter = printWriter6;
                        z = true;
                        while (th instanceof WrappedRuntimeException) {
                        }
                        z = z4;
                        printWriter.println();
                        if (z) {
                        }
                        if (str2 != null) {
                        }
                        doExit(th.getMessage());
                        if (str2 == null) {
                        }
                    }
                } catch (Throwable th9) {
                    th = th9;
                    str2 = str13;
                    printWriter2 = printWriter4;
                    printWriter = printWriter6;
                    z = true;
                    while (th instanceof WrappedRuntimeException) {
                    }
                    z = z4;
                    printWriter.println();
                    if (z) {
                    }
                    if (str2 != null) {
                    }
                    doExit(th.getMessage());
                    if (str2 == null) {
                    }
                }
            } else {
                str2 = str13;
            }
            if (templates != null) {
                Transformer newTransformer = str16.equals("th") ? null : templates.newTransformer();
                newTransformer.setErrorListener(new DefaultErrorHandler());
                if (str15 != null) {
                    newTransformer.setOutputProperty(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_OUTPUT_METHOD, str15);
                }
                int i18 = 0;
                for (int size = vector.size(); i18 < size; size = size) {
                    newTransformer.setParameter((String) vector.elementAt(i18), (String) vector.elementAt(i18 + 1));
                    i18 += 2;
                    str12 = str12;
                }
                str4 = str12;
                if (uRIResolver != null) {
                    newTransformer.setURIResolver(uRIResolver);
                }
                if (str11 == null) {
                    newTransformer.transform(new StreamSource(new StringReader("<?xml version=\"1.0\"?> <doc/>")), streamResult);
                } else if (str16.equals("d2d")) {
                    DocumentBuilderFactory newInstance2 = DocumentBuilderFactory.newInstance();
                    newInstance2.setCoalescing(true);
                    newInstance2.setNamespaceAware(true);
                    if (z2) {
                        try {
                            newInstance2.setFeature(Constants.FEATURE_SECURE_PROCESSING, true);
                        } catch (ParserConfigurationException unused10) {
                        }
                    }
                    DocumentBuilder newDocumentBuilder = newInstance2.newDocumentBuilder();
                    if (entityResolver != null) {
                        newDocumentBuilder.setEntityResolver(entityResolver);
                    }
                    Document parse = newDocumentBuilder.parse(new InputSource(str11));
                    DocumentFragment createDocumentFragment = newDocumentBuilder.newDocument().createDocumentFragment();
                    newTransformer.transform(new DOMSource(parse, str11), new DOMResult(createDocumentFragment));
                    Transformer newTransformer2 = sAXTransformerFactory.newTransformer();
                    newTransformer2.setErrorListener(new DefaultErrorHandler());
                    newTransformer2.setOutputProperties(templates.getOutputProperties());
                    if (contentHandler != null) {
                        newTransformer2.transform(new DOMSource(createDocumentFragment), new SAXResult(contentHandler));
                    } else {
                        newTransformer2.transform(new DOMSource(createDocumentFragment), streamResult);
                    }
                } else if (str16.equals("th")) {
                    for (int i19 = 0; i19 < 1; i19++) {
                        try {
                            SAXParserFactory newInstance3 = SAXParserFactory.newInstance();
                            newInstance3.setNamespaceAware(true);
                            if (z2) {
                                try {
                                    newInstance3.setFeature(Constants.FEATURE_SECURE_PROCESSING, true);
                                } catch (ParserConfigurationException e2) {
                                    throw new SAXException(e2);
                                } catch (FactoryConfigurationError e3) {
                                    throw new SAXException(e3.toString());
                                } catch (AbstractMethodError | NoSuchMethodError unused11) {
                                }
                            }
                            XMLReader xMLReader2 = newInstance3.newSAXParser().getXMLReader();
                            if (xMLReader2 == null) {
                                xMLReader2 = XMLReaderFactory.createXMLReader();
                            }
                            ErrorHandler newTransformerHandler = sAXTransformerFactory.newTransformerHandler(templates);
                            xMLReader2.setContentHandler(newTransformerHandler);
                            xMLReader2.setDTDHandler(newTransformerHandler);
                            if (newTransformerHandler instanceof ErrorHandler) {
                                xMLReader2.setErrorHandler(newTransformerHandler);
                            }
                            try {
                                xMLReader2.setProperty("http://xml.org/sax/properties/lexical-handler", newTransformerHandler);
                            } catch (SAXNotRecognizedException | SAXNotSupportedException unused12) {
                            }
                            try {
                                xMLReader2.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
                                newTransformerHandler.setResult(streamResult);
                                xMLReader2.parse(new InputSource(str11));
                            } catch (Throwable th10) {
                                th = th10;
                                printWriter2 = printWriter4;
                                z = true;
                            }
                        } catch (Throwable th11) {
                            th = th11;
                            printWriter2 = printWriter4;
                            z = true;
                            printWriter = printWriter6;
                            while (th instanceof WrappedRuntimeException) {
                                th = ((WrappedRuntimeException) th).getException();
                            }
                            if (!(th instanceof NullPointerException) && !(th instanceof ClassCastException)) {
                                z = z4;
                            }
                            printWriter.println();
                            if (z) {
                                th.printStackTrace(printWriter2);
                            } else {
                                DefaultErrorHandler.printLocation(printWriter, th);
                                printWriter.println(XSLMessages.createMessage("ER_XSLT_ERROR", null) + " (" + th.getClass().getName() + "): " + th.getMessage());
                            }
                            if (str2 != null) {
                                printWriter2.close();
                            }
                            doExit(th.getMessage());
                            if (str2 == null) {
                            }
                        }
                    }
                } else if (entityResolver != null) {
                    try {
                        SAXParserFactory newInstance4 = SAXParserFactory.newInstance();
                        try {
                            newInstance4.setNamespaceAware(true);
                            if (z2) {
                                try {
                                    newInstance4.setFeature(Constants.FEATURE_SECURE_PROCESSING, true);
                                } catch (SAXException unused13) {
                                }
                            }
                            try {
                                xMLReader = newInstance4.newSAXParser().getXMLReader();
                            } catch (AbstractMethodError | NoSuchMethodError unused14) {
                                xMLReader = null;
                            }
                            if (xMLReader == null) {
                                xMLReader = XMLReaderFactory.createXMLReader();
                            }
                            xMLReader.setEntityResolver(entityResolver);
                            if (contentHandler != null) {
                                newTransformer.transform(new SAXSource(xMLReader, new InputSource(str11)), new SAXResult(contentHandler));
                            } else {
                                newTransformer.transform(new SAXSource(xMLReader, new InputSource(str11)), streamResult);
                            }
                        } catch (Throwable th12) {
                            th = th12;
                            printWriter2 = printWriter4;
                            z = true;
                            printWriter = printWriter6;
                            while (th instanceof WrappedRuntimeException) {
                            }
                            z = z4;
                            printWriter.println();
                            if (z) {
                            }
                            if (str2 != null) {
                            }
                            doExit(th.getMessage());
                            if (str2 == null) {
                            }
                        }
                    } catch (ParserConfigurationException e4) {
                        throw new SAXException(e4);
                    } catch (FactoryConfigurationError e5) {
                        throw new SAXException(e5.toString());
                    }
                } else if (contentHandler != null) {
                    newTransformer.transform(new StreamSource(str11), new SAXResult(contentHandler));
                } else {
                    newTransformer.transform(new StreamSource(str11), streamResult);
                }
                printWriter = printWriter6;
            } else {
                str4 = str12;
                try {
                    String createMessage8 = XSLMessages.createMessage(str9, null);
                    printWriter = printWriter6;
                    try {
                        printWriter.println(createMessage8);
                        doExit(createMessage8);
                    } catch (Throwable th13) {
                        th = th13;
                        z = true;
                        printWriter2 = printWriter4;
                        while (th instanceof WrappedRuntimeException) {
                        }
                        z = z4;
                        printWriter.println();
                        if (z) {
                        }
                        if (str2 != null) {
                        }
                        doExit(th.getMessage());
                        if (str2 == null) {
                        }
                    }
                } catch (Throwable th14) {
                    th = th14;
                    printWriter = printWriter6;
                    z = true;
                    printWriter2 = printWriter4;
                    while (th instanceof WrappedRuntimeException) {
                    }
                    z = z4;
                    printWriter.println();
                    if (z) {
                    }
                    if (str2 != null) {
                    }
                    doExit(th.getMessage());
                    if (str2 == null) {
                    }
                }
            }
            if (str3 != null) {
                try {
                    OutputStream outputStream = streamResult.getOutputStream();
                    Writer writer = streamResult.getWriter();
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException unused15) {
                        }
                    }
                    if (writer != null) {
                        writer.close();
                    }
                } catch (Throwable th15) {
                    th = th15;
                    printWriter2 = printWriter4;
                    z = true;
                    while (th instanceof WrappedRuntimeException) {
                    }
                    z = z4;
                    printWriter.println();
                    if (z) {
                    }
                    if (str2 != null) {
                    }
                    doExit(th.getMessage());
                    if (str2 == null) {
                    }
                }
            }
            long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
            if (z3) {
                Object[] objArr10 = new Object[3];
                objArr10[0] = str11;
                z = true;
                try {
                    objArr10[1] = str4;
                    objArr10[2] = new Long(currentTimeMillis2);
                    String createMessage9 = XSLMessages.createMessage("diagTiming", objArr10);
                    printWriter.println('\n');
                    printWriter.println(createMessage9);
                } catch (Throwable th16) {
                    th = th16;
                }
            }
            printWriter2 = printWriter4;
        } catch (Throwable th17) {
            th = th17;
            str2 = str13;
            printWriter = printWriter6;
            z = true;
            printWriter2 = printWriter;
            while (th instanceof WrappedRuntimeException) {
            }
            z = z4;
            printWriter.println();
            if (z) {
            }
            if (str2 != null) {
            }
            doExit(th.getMessage());
            if (str2 == null) {
            }
        }
        if (str2 == null) {
            printWriter2.close();
        }
    }

    static void doExit(String str) {
        throw new RuntimeException(str);
    }

    private static void waitForReturnKey(ResourceBundle resourceBundle) {
        System.out.println(resourceBundle.getString("xslProc_return_to_continue"));
        do {
            try {
            } catch (IOException unused) {
                return;
            }
        } while (System.in.read() != 10);
    }

    private static void printInvalidXSLTCOption(String str) {
        System.err.println(XSLMessages.createMessage("xslProc_invalid_xsltc_option", new Object[]{str}));
    }

    private static void printInvalidXalanOption(String str) {
        System.err.println(XSLMessages.createMessage("xslProc_invalid_xalan_option", new Object[]{str}));
    }
}
