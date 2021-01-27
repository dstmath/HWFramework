package ohos.agp.components.element;

import java.io.IOException;
import ohos.aafwk.utils.log.LogDomain;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.Node;
import ohos.global.resource.solidxml.SolidXml;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ElementScatter {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_ElementScatter");
    private static final String TITLE_ANIMATED_STATE_ELEMENT = "animated-state-container";
    private static final String TITLE_FRAME_ANIMATION_ELEMENT = "animation-list";
    private static final String TITLE_SHAPE_ELEMENT = "shape";
    private static final String TITLE_STATE_ELEMENT = "state-container";
    private static final String TITLE_VECTOR_ELEMENT = "vector";
    private final Context mContext;
    private ResourceManager mResourceManager;

    public ElementScatter(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mResourceManager = this.mContext.getResourceManager();
            return;
        }
        throw new ElementScatterException("context is null");
    }

    public static ElementScatter getInstance(Context context) {
        if (context != null) {
            return new ElementScatter(context);
        }
        throw new ElementScatterException("get context is null");
    }

    public Element parse(int i) {
        ResourceManager resourceManager = this.mResourceManager;
        if (resourceManager != null) {
            try {
                SolidXml solidXml = resourceManager.getSolidXml(i);
                HiLog.debug(TAG, "load element xml", new Object[0]);
                return parseSolidXml(solidXml);
            } catch (NotExistException unused) {
                throw new ElementScatterException("Can't open solid xml: file not exist: " + i);
            } catch (IOException unused2) {
                throw new ElementScatterException("Can't open solid xml: io exception: " + i);
            } catch (WrongTypeException unused3) {
                throw new ElementScatterException("Can't open solid xml: wrong type: " + i);
            }
        } else {
            throw new ElementScatterException("ElementScatter should init Context first.");
        }
    }

    private Element parseSolidXml(SolidXml solidXml) {
        if (solidXml != null) {
            Node root = solidXml.getRoot();
            if (root != null) {
                String name = root.getName();
                HiLog.debug(TAG, "parseSolidXml: %{public}s", new Object[]{name});
                if (name == null || name.length() == 0) {
                    throw new ElementScatterException("Solid XML root node has no name!");
                }
                Element parseFromTag = parseFromTag(name);
                if (parseFromTag != null) {
                    parseFromTag.parseXMLNode(this.mContext, root);
                }
                return parseFromTag;
            }
            throw new ElementScatterException("Solid XML has no root node!");
        }
        throw new ElementScatterException("Can't open solid XML!");
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private Element parseFromTag(String str) {
        char c;
        switch (str.hashCode()) {
            case -1493546681:
                if (str.equals(TITLE_FRAME_ANIMATION_ELEMENT)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1127369189:
                if (str.equals(TITLE_ANIMATED_STATE_ELEMENT)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -820387517:
                if (str.equals(TITLE_VECTOR_ELEMENT)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 109399969:
                if (str.equals(TITLE_SHAPE_ELEMENT)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 685322821:
                if (str.equals(TITLE_STATE_ELEMENT)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return new StateElement();
        }
        if (c == 1) {
            return new AnimatedStateElement();
        }
        if (c == 2) {
            return new FrameAnimationElement();
        }
        if (c == 3) {
            return new ShapeElement();
        }
        if (c == 4) {
            return new VectorElement();
        }
        HiLog.debug(TAG, "do not support this tag: %{public}s", new Object[]{str});
        return null;
    }

    /* access modifiers changed from: package-private */
    public Node getRootNodeFromXmlId(int i) {
        try {
            HiLog.debug(TAG, "load element xml", new Object[0]);
            SolidXml solidXml = this.mResourceManager.getSolidXml(i);
            Node node = null;
            if (solidXml != null) {
                node = solidXml.getRoot();
            }
            if (node != null) {
                return node;
            }
            throw new ElementScatterException("xml root node is null");
        } catch (NotExistException unused) {
            throw new ElementScatterException("Can't open solid xml: file not exist: " + i);
        } catch (IOException unused2) {
            throw new ElementScatterException("Can't open solid xml: io exception: " + i);
        } catch (WrongTypeException unused3) {
            throw new ElementScatterException("Can't open solid xml: wrong type: " + i);
        }
    }
}
