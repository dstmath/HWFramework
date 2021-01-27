package ohos.com.sun.org.apache.xalan.internal.xsltc.cmdline;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import ohos.com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOptsException;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.XSLTC;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.jdk.xml.internal.JdkXmlFeatures;

public final class Compile {
    private static int VERSION_DELTA = 0;
    private static int VERSION_MAJOR = 1;
    private static int VERSION_MINOR = 4;
    private static boolean _allowExit = true;

    public static void printUsage() {
        String str;
        PrintStream printStream = System.err;
        StringBuilder sb = new StringBuilder();
        sb.append("XSLTC version ");
        sb.append(VERSION_MAJOR);
        sb.append(".");
        sb.append(VERSION_MINOR);
        if (VERSION_DELTA > 0) {
            str = "." + VERSION_DELTA;
        } else {
            str = "";
        }
        sb.append(str);
        sb.append("\n");
        sb.append(new ErrorMsg(ErrorMsg.COMPILE_USAGE_STR));
        printStream.println(sb.toString());
        if (_allowExit) {
            System.exit(-1);
        }
    }

    public static void main(String[] strArr) {
        boolean z;
        URL url;
        try {
            GetOpt getOpt = new GetOpt(strArr, "o:d:j:p:uxhsinv");
            if (strArr.length < 1) {
                printUsage();
            }
            XSLTC xsltc = new XSLTC(new JdkXmlFeatures(false));
            xsltc.init();
            boolean z2 = false;
            boolean z3 = false;
            boolean z4 = false;
            while (true) {
                int nextOption = getOpt.getNextOption();
                if (nextOption == -1) {
                    if (z2) {
                        if (!z3) {
                            System.err.println(new ErrorMsg(ErrorMsg.COMPILE_STDIN_ERR));
                            if (_allowExit) {
                                System.exit(-1);
                            }
                        }
                        z = xsltc.compile(System.in, xsltc.getClassName());
                    } else {
                        String[] cmdArgs = getOpt.getCmdArgs();
                        Vector vector = new Vector();
                        for (String str : cmdArgs) {
                            if (z4) {
                                url = new URL(str);
                            } else {
                                url = new File(str).toURI().toURL();
                            }
                            vector.addElement(url);
                        }
                        z = xsltc.compile(vector);
                    }
                    if (z) {
                        xsltc.printWarnings();
                        if (xsltc.getJarFileName() != null) {
                            xsltc.outputToJar();
                        }
                        if (_allowExit) {
                            System.exit(0);
                            return;
                        }
                        return;
                    }
                    xsltc.printWarnings();
                    xsltc.printErrors();
                    if (_allowExit) {
                        System.exit(-1);
                        return;
                    }
                    return;
                } else if (nextOption == 100) {
                    xsltc.setDestDirectory(getOpt.getOptionArg());
                } else if (nextOption == 115) {
                    _allowExit = false;
                } else if (nextOption == 117) {
                    z4 = true;
                } else if (nextOption == 120) {
                    xsltc.setDebug(true);
                } else if (nextOption == 105) {
                    z2 = true;
                } else if (nextOption != 106) {
                    switch (nextOption) {
                        case 110:
                            xsltc.setTemplateInlining(true);
                            continue;
                        case 111:
                            xsltc.setClassName(getOpt.getOptionArg());
                            z3 = true;
                            continue;
                        case 112:
                            xsltc.setPackageName(getOpt.getOptionArg());
                            continue;
                        default:
                            printUsage();
                            continue;
                    }
                } else {
                    xsltc.setJarFileName(getOpt.getOptionArg());
                }
            }
        } catch (GetOptsException e) {
            System.err.println(e);
            printUsage();
        } catch (Exception e2) {
            e2.printStackTrace();
            if (_allowExit) {
                System.exit(-1);
            }
        }
    }
}
