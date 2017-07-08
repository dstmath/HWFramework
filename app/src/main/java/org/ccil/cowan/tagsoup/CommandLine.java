package org.ccil.cowan.tagsoup;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.ims.AuthorizationHeaderIms;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

public class CommandLine {
    static Hashtable options;
    private static String theOutputEncoding;
    private static Parser theParser;
    private static HTMLSchema theSchema;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.ccil.cowan.tagsoup.CommandLine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.ccil.cowan.tagsoup.CommandLine.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.ccil.cowan.tagsoup.CommandLine.<clinit>():void");
    }

    public static void main(String[] argv) throws IOException, SAXException {
        int optind = getopts(options, argv);
        if (hasOption(options, "--help")) {
            doHelp();
        } else if (hasOption(options, "--version")) {
            System.err.println("TagSoup version 1.2");
        } else {
            if (argv.length == optind) {
                process("", System.out);
            } else if (hasOption(options, "--files")) {
                for (i = optind; i < argv.length; i++) {
                    String dst;
                    String src = argv[i];
                    int j = src.lastIndexOf(46);
                    if (j == -1) {
                        dst = src + ".xhtml";
                    } else if (src.endsWith(".xhtml")) {
                        dst = src + "_";
                    } else {
                        dst = src.substring(0, j) + ".xhtml";
                    }
                    System.err.println("src: " + src + " dst: " + dst);
                    process(src, new FileOutputStream(dst));
                }
            } else {
                for (i = optind; i < argv.length; i++) {
                    System.err.println("src: " + argv[i]);
                    process(argv[i], System.out);
                }
            }
        }
    }

    private static void doHelp() {
        System.err.print("usage: java -jar tagsoup-*.jar ");
        System.err.print(" [ ");
        boolean first = true;
        Enumeration e = options.keys();
        while (e.hasMoreElements()) {
            if (!first) {
                System.err.print("| ");
            }
            first = false;
            String key = (String) e.nextElement();
            System.err.print(key);
            if (key.endsWith(Separators.EQUALS)) {
                System.err.print(Separators.QUESTION);
            }
            System.err.print(Separators.SP);
        }
        System.err.println("]*");
    }

    private static void process(String src, OutputStream os) throws IOException, SAXException {
        XMLReader r;
        Writer w;
        if (hasOption(options, "--reuse")) {
            if (theParser == null) {
                theParser = new Parser();
            }
            r = theParser;
        } else {
            r = new Parser();
        }
        theSchema = new HTMLSchema();
        r.setProperty(Parser.schemaProperty, theSchema);
        if (hasOption(options, "--nocdata")) {
            r.setFeature(Parser.CDATAElementsFeature, false);
        }
        if (hasOption(options, "--nons") || hasOption(options, "--html")) {
            r.setFeature(Parser.namespacesFeature, false);
        }
        if (hasOption(options, "--nobogons")) {
            r.setFeature(Parser.ignoreBogonsFeature, true);
        }
        if (hasOption(options, "--any")) {
            r.setFeature(Parser.bogonsEmptyFeature, false);
        } else if (hasOption(options, "--emptybogons")) {
            r.setFeature(Parser.bogonsEmptyFeature, true);
        }
        if (hasOption(options, "--norootbogons")) {
            r.setFeature(Parser.rootBogonsFeature, false);
        }
        if (hasOption(options, "--nodefaults")) {
            r.setFeature(Parser.defaultAttributesFeature, false);
        }
        if (hasOption(options, "--nocolons")) {
            r.setFeature(Parser.translateColonsFeature, true);
        }
        if (hasOption(options, "--norestart")) {
            r.setFeature(Parser.restartElementsFeature, false);
        }
        if (hasOption(options, "--ignorable")) {
            r.setFeature(Parser.ignorableWhitespaceFeature, true);
        }
        if (hasOption(options, "--pyxin")) {
            r.setProperty(Parser.scannerProperty, new PYXScanner());
        }
        if (theOutputEncoding == null) {
            w = new OutputStreamWriter(os);
        } else {
            w = new OutputStreamWriter(os, theOutputEncoding);
        }
        ContentHandler h = chooseContentHandler(w);
        r.setContentHandler(h);
        if (hasOption(options, "--lexical") && (h instanceof LexicalHandler)) {
            r.setProperty(Parser.lexicalHandlerProperty, h);
        }
        InputSource s = new InputSource();
        if (src != "") {
            s.setSystemId(src);
        } else {
            s.setByteStream(System.in);
        }
        if (hasOption(options, "--encoding=")) {
            String encoding = (String) options.get("--encoding=");
            if (encoding != null) {
                s.setEncoding(encoding);
            }
        }
        r.parse(s);
    }

    private static ContentHandler chooseContentHandler(Writer w) {
        if (hasOption(options, "--pyx")) {
            return new PYXWriter(w);
        }
        XMLWriter x = new XMLWriter(w);
        if (hasOption(options, "--html")) {
            x.setOutputProperty(XMLWriter.METHOD, "html");
            x.setOutputProperty(XMLWriter.OMIT_XML_DECLARATION, AuthorizationHeaderIms.YES);
        }
        if (hasOption(options, "--method=")) {
            String method = (String) options.get("--method=");
            if (method != null) {
                x.setOutputProperty(XMLWriter.METHOD, method);
            }
        }
        if (hasOption(options, "--doctype-public=")) {
            String doctype_public = (String) options.get("--doctype-public=");
            if (doctype_public != null) {
                x.setOutputProperty(XMLWriter.DOCTYPE_PUBLIC, doctype_public);
            }
        }
        if (hasOption(options, "--doctype-system=")) {
            String doctype_system = (String) options.get("--doctype-system=");
            if (doctype_system != null) {
                x.setOutputProperty(XMLWriter.DOCTYPE_SYSTEM, doctype_system);
            }
        }
        if (hasOption(options, "--output-encoding=")) {
            theOutputEncoding = (String) options.get("--output-encoding=");
            if (theOutputEncoding != null) {
                x.setOutputProperty(XMLWriter.ENCODING, theOutputEncoding);
            }
        }
        if (hasOption(options, "--omit-xml-declaration")) {
            x.setOutputProperty(XMLWriter.OMIT_XML_DECLARATION, AuthorizationHeaderIms.YES);
        }
        x.setPrefix(theSchema.getURI(), "");
        return x;
    }

    private static int getopts(Hashtable options, String[] argv) {
        int optind = 0;
        while (optind < argv.length) {
            String arg = argv[optind];
            Object obj = null;
            if (arg.charAt(0) != '-') {
                break;
            }
            int eqsign = arg.indexOf(61);
            if (eqsign != -1) {
                obj = arg.substring(eqsign + 1, arg.length());
                arg = arg.substring(0, eqsign + 1);
            }
            if (!options.containsKey(arg)) {
                System.err.print("Unknown option ");
                System.err.println(arg);
                System.exit(1);
            } else if (obj == null) {
                options.put(arg, Boolean.TRUE);
            } else {
                options.put(arg, obj);
            }
            optind++;
        }
        return optind;
    }

    private static boolean hasOption(Hashtable options, String option) {
        if (!Boolean.getBoolean(option) && options.get(option) == Boolean.FALSE) {
            return false;
        }
        return true;
    }
}
