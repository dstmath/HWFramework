package ohos.ai.cv.docrefine;

import ohos.ai.cv.common.ParamKey;

public class DocRefineParamKey extends ParamKey {
    public static final String DOCREFINE_DETECT = "docrefine_detect";
    public static final String DOCREFINE_FUNC = "key_docrefine_func";
    public static final int DOCREFINE_FUNC_DETECT = 0;
    public static final int DOCREFINE_FUNC_REFINE = 1;
    public static final String DOCREFINE_IN_COORD = "docrefine_in_coord";
    public static final String DOCREFINE_REFINE = "docrefine_refine";

    private DocRefineParamKey() {
    }
}
