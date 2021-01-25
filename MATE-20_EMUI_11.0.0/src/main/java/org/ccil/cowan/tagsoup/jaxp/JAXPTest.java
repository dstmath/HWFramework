package org.ccil.cowan.tagsoup.jaxp;

import java.io.File;
import java.io.PrintStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import org.w3c.dom.Document;
import org.xml.sax.helpers.DefaultHandler;

public class JAXPTest {
    public static void main(String[] args) throws Exception {
        new JAXPTest().test(args);
    }

    private void test(String[] args) throws Exception {
        if (args.length != 1) {
            PrintStream printStream = System.err;
            printStream.println("Usage: java " + getClass() + " [input-file]");
            System.exit(1);
        }
        File f = new File(args[0]);
        System.setProperty("javax.xml.parsers.SAXParserFactory", "org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl");
        SAXParserFactory spf = SAXParserFactory.newInstance();
        PrintStream printStream2 = System.out;
        printStream2.println("Ok, SAX factory JAXP creates is: " + spf);
        System.out.println("Let's parse...");
        spf.newSAXParser().parse(f, new DefaultHandler());
        System.out.println("Done. And then DOM build:");
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
        PrintStream printStream3 = System.out;
        printStream3.println("Succesfully built DOM tree from '" + f + "', -> " + doc);
    }
}
