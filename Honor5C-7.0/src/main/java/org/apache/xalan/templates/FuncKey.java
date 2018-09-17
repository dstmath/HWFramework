package org.apache.xalan.templates;

import java.util.Hashtable;
import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.KeyManager;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.UnionPathIterator;
import org.apache.xpath.functions.Function2Args;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;

public class FuncKey extends Function2Args {
    private static Boolean ISTRUE = null;
    static final long serialVersionUID = 9089293100115347340L;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xalan.templates.FuncKey.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xalan.templates.FuncKey.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xalan.templates.FuncKey.<clinit>():void");
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        QName keyname;
        XObject arg;
        TransformerImpl transformer = (TransformerImpl) xctxt.getOwnerObject();
        int context = xctxt.getCurrentNode();
        int docContext = xctxt.getDTM(context).getDocumentRoot(context);
        if (-1 == docContext) {
            keyname = new QName(getArg0().execute(xctxt).str(), xctxt.getNamespaceContext());
            arg = getArg1().execute(xctxt);
        } else {
            keyname = new QName(getArg0().execute(xctxt).str(), xctxt.getNamespaceContext());
            arg = getArg1().execute(xctxt);
        }
        boolean argIsNodeSetDTM = 4 == arg.getType();
        KeyManager kmgr = transformer.getKeyManager();
        if (argIsNodeSetDTM) {
            XNodeSet ns = (XNodeSet) arg;
            ns.setShouldCacheNodes(true);
            if (ns.getLength() <= 1) {
                argIsNodeSetDTM = false;
            }
        }
        if (argIsNodeSetDTM) {
            Hashtable usedrefs = null;
            DTMIterator ni = arg.iter();
            UnionPathIterator upi = new UnionPathIterator();
            upi.exprSetParent(this);
            while (true) {
                int pos = ni.nextNode();
                if (-1 != pos) {
                    XMLString ref = xctxt.getDTM(pos).getStringValue(pos);
                    if (ref != null) {
                        if (usedrefs == null) {
                            usedrefs = new Hashtable();
                        }
                        if (usedrefs.get(ref) == null) {
                            usedrefs.put(ref, ISTRUE);
                            XNodeSet nl = kmgr.getNodeSetDTMByKey(xctxt, docContext, keyname, ref, xctxt.getNamespaceContext());
                            nl.setRoot(xctxt.getCurrentNode(), xctxt);
                            upi.addIterator(nl);
                        }
                    }
                } else {
                    upi.setRoot(xctxt.getCurrentNode(), xctxt);
                    return new XNodeSet((DTMIterator) upi);
                }
            }
        }
        XNodeSet nodes = kmgr.getNodeSetDTMByKey(xctxt, docContext, keyname, arg.xstr(), xctxt.getNamespaceContext());
        nodes.setRoot(xctxt.getCurrentNode(), xctxt);
        return nodes;
    }
}
