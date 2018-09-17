package org.apache.xalan.transformer;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.w3c.dom.Node;

public class MsgMgr {
    private TransformerImpl m_transformer;

    public MsgMgr(TransformerImpl transformer) {
        this.m_transformer = transformer;
    }

    public void message(SourceLocator srcLctr, String msg, boolean terminate) throws TransformerException {
        ErrorListener errHandler = this.m_transformer.getErrorListener();
        if (errHandler != null) {
            errHandler.warning(new TransformerException(msg, srcLctr));
        } else if (terminate) {
            throw new TransformerException(msg, srcLctr);
        } else {
            System.out.println(msg);
        }
    }

    public void warn(SourceLocator srcLctr, String msg) throws TransformerException {
        warn(srcLctr, null, null, msg, null);
    }

    public void warn(SourceLocator srcLctr, String msg, Object[] args) throws TransformerException {
        warn(srcLctr, null, null, msg, args);
    }

    public void warn(SourceLocator srcLctr, Node styleNode, Node sourceNode, String msg) throws TransformerException {
        warn(srcLctr, styleNode, sourceNode, msg, null);
    }

    public void warn(SourceLocator srcLctr, Node styleNode, Node sourceNode, String msg, Object[] args) throws TransformerException {
        String formattedMsg = XSLMessages.createWarning(msg, args);
        ErrorListener errHandler = this.m_transformer.getErrorListener();
        if (errHandler != null) {
            errHandler.warning(new TransformerException(formattedMsg, srcLctr));
        } else {
            System.out.println(formattedMsg);
        }
    }

    public void error(SourceLocator srcLctr, String msg) throws TransformerException {
        error(srcLctr, null, null, msg, null);
    }

    public void error(SourceLocator srcLctr, String msg, Object[] args) throws TransformerException {
        error(srcLctr, null, null, msg, args);
    }

    public void error(SourceLocator srcLctr, String msg, Exception e) throws TransformerException {
        error(srcLctr, msg, null, e);
    }

    public void error(SourceLocator srcLctr, String msg, Object[] args, Exception e) throws TransformerException {
        String formattedMsg = XSLMessages.createMessage(msg, args);
        ErrorListener errHandler = this.m_transformer.getErrorListener();
        if (errHandler != null) {
            errHandler.fatalError(new TransformerException(formattedMsg, srcLctr));
            return;
        }
        throw new TransformerException(formattedMsg, srcLctr);
    }

    public void error(SourceLocator srcLctr, Node styleNode, Node sourceNode, String msg) throws TransformerException {
        error(srcLctr, styleNode, sourceNode, msg, null);
    }

    public void error(SourceLocator srcLctr, Node styleNode, Node sourceNode, String msg, Object[] args) throws TransformerException {
        String formattedMsg = XSLMessages.createMessage(msg, args);
        ErrorListener errHandler = this.m_transformer.getErrorListener();
        if (errHandler != null) {
            errHandler.fatalError(new TransformerException(formattedMsg, srcLctr));
            return;
        }
        throw new TransformerException(formattedMsg, srcLctr);
    }
}
