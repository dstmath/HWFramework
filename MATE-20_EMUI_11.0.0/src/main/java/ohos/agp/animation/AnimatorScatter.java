package ohos.agp.animation;

import java.io.IOException;
import java.util.HashMap;
import ohos.aafwk.utils.log.LogDomain;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.Node;
import ohos.global.resource.solidxml.SolidXml;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AnimatorScatter {
    private static final HashMap<Context, AnimatorScatter> ANIMATOR_SCATTER_CACHE = new HashMap<>();
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_AnimatorScatter");
    private final Context mContext;
    private final ResourceManager mResourceManager;

    private AnimatorScatter(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mResourceManager = this.mContext.getResourceManager();
            return;
        }
        throw new AnimatorScatterException("context is null");
    }

    public static AnimatorScatter getInstance(Context context) {
        AnimatorScatter animatorScatter;
        synchronized (ANIMATOR_SCATTER_CACHE) {
            animatorScatter = ANIMATOR_SCATTER_CACHE.get(context);
            if (animatorScatter == null) {
                animatorScatter = new AnimatorScatter(context);
                ANIMATOR_SCATTER_CACHE.put(context, animatorScatter);
            }
        }
        return animatorScatter;
    }

    public Animator parse(int i) {
        HiLog.debug(TAG, "enter parse Animator", new Object[0]);
        ResourceManager resourceManager = this.mResourceManager;
        if (resourceManager != null) {
            try {
                SolidXml solidXml = resourceManager.getSolidXml(i);
                HiLog.debug(TAG, "load drawable xml", new Object[0]);
                return parseSolidXml(solidXml);
            } catch (NotExistException unused) {
                throw new AnimatorScatterException("Can't open solid xml: file not exist: " + i);
            } catch (IOException unused2) {
                throw new AnimatorScatterException("Can't open solid xml: io exception: " + i);
            } catch (WrongTypeException unused3) {
                throw new AnimatorScatterException("Can't open solid xml: wrong type: " + i);
            }
        } else {
            throw new AnimatorScatterException("AnimatorScatter should init Context first.");
        }
    }

    private Animator parseSolidXml(SolidXml solidXml) {
        if (solidXml != null) {
            Node root = solidXml.getRoot();
            if (root != null) {
                String name = root.getName();
                HiLog.debug(TAG, "parseSolidXml: %{public}s", new Object[]{name});
                if (name == null || name.length() == 0) {
                    throw new AnimatorScatterException("Solid XML root node has no name!");
                }
                Animator parseFromTag = parseFromTag(name);
                if (parseFromTag != null) {
                    parseFromTag.parse(root, this.mResourceManager);
                }
                return parseFromTag;
            }
            throw new AnimatorScatterException("Solid XML has no root node!");
        }
        throw new AnimatorScatterException("Can't open solid XML!");
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0050  */
    private Animator parseFromTag(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode != -795202841) {
            if (hashCode != -423011167) {
                if (hashCode == 1664652764 && str.equals("animatorProperty")) {
                    c = 2;
                    if (c != 0) {
                        return new AnimatorValue();
                    }
                    if (c == 1 || c == 2) {
                        return new AnimatorProperty();
                    }
                    HiLog.debug(TAG, "do not support this tag: %{public}s", new Object[]{str});
                    return null;
                }
            } else if (str.equals("viewPropertyAnimator")) {
                c = 1;
                if (c != 0) {
                }
            }
        } else if (str.equals("animator")) {
            c = 0;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }
}
