package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.extensions.ExtensionNamespaceSupport;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.Constants;
import org.apache.xpath.VariableStack;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ElemExsltFunction extends ElemTemplate {
    static final long serialVersionUID = 272154954793534771L;

    public int getXSLToken() {
        return 88;
    }

    public String getNodeName() {
        return Constants.EXSLT_ELEMNAME_FUNCTION_STRING;
    }

    public void execute(TransformerImpl transformer, XObject[] args) throws TransformerException {
        VariableStack vars = transformer.getXPathContext().getVarStack();
        int thisFrame = vars.getStackFrame();
        int nextFrame = vars.link(this.m_frameSize);
        if (this.m_inArgsSize < args.length) {
            throw new TransformerException("function called with too many args");
        }
        if (this.m_inArgsSize > 0) {
            vars.clearLocalSlots(0, this.m_inArgsSize);
            if (args.length > 0) {
                vars.setStackFrame(thisFrame);
                NodeList children = getChildNodes();
                for (int i = 0; i < args.length; i++) {
                    Node child = children.item(i);
                    if (children.item(i) instanceof ElemParam) {
                        vars.setLocalVariable(((ElemParam) children.item(i)).getIndex(), args[i], nextFrame);
                    }
                }
                vars.setStackFrame(nextFrame);
            }
        }
        vars.setStackFrame(nextFrame);
        transformer.executeChildTemplates((ElemTemplateElement) this, true);
        vars.unlink(thisFrame);
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        String namespace = getName().getNamespace();
        String handlerClass = sroot.getExtensionHandlerClass();
        sroot.getExtensionNamespacesManager().registerExtension(new ExtensionNamespaceSupport(namespace, handlerClass, new Object[]{namespace, sroot}));
        if (!namespace.equals(Constants.S_EXSLT_FUNCTIONS_URL)) {
            sroot.getExtensionNamespacesManager().registerExtension(new ExtensionNamespaceSupport(Constants.S_EXSLT_FUNCTIONS_URL, handlerClass, new Object[]{Constants.S_EXSLT_FUNCTIONS_URL, sroot}));
        }
    }
}
