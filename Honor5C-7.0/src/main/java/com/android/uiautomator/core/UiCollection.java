package com.android.uiautomator.core;

@Deprecated
public class UiCollection extends UiObject {
    public UiCollection(UiSelector selector) {
        super(selector);
    }

    public UiObject getChildByDescription(UiSelector childPattern, String text) throws UiObjectNotFoundException {
        Tracer.trace(childPattern, text);
        if (text != null) {
            int count = getChildCount(childPattern);
            for (int x = 0; x < count; x++) {
                UiObject row = getChildByInstance(childPattern, x);
                String nodeDesc = row.getContentDescription();
                if ((nodeDesc != null && nodeDesc.contains(text)) || row.getChild(new UiSelector().descriptionContains(text)).exists()) {
                    return row;
                }
            }
        }
        throw new UiObjectNotFoundException("for description= \"" + text + "\"");
    }

    public UiObject getChildByInstance(UiSelector childPattern, int instance) throws UiObjectNotFoundException {
        Tracer.trace(childPattern, Integer.valueOf(instance));
        return new UiObject(UiSelector.patternBuilder(getSelector(), UiSelector.patternBuilder(childPattern).instance(instance)));
    }

    public UiObject getChildByText(UiSelector childPattern, String text) throws UiObjectNotFoundException {
        Tracer.trace(childPattern, text);
        if (text != null) {
            int count = getChildCount(childPattern);
            for (int x = 0; x < count; x++) {
                UiObject row = getChildByInstance(childPattern, x);
                if (text.equals(row.getText()) || row.getChild(new UiSelector().text(text)).exists()) {
                    return row;
                }
            }
        }
        throw new UiObjectNotFoundException("for text= \"" + text + "\"");
    }

    public int getChildCount(UiSelector childPattern) {
        Tracer.trace(childPattern);
        return getQueryController().getPatternCount(UiSelector.patternBuilder(getSelector(), UiSelector.patternBuilder(childPattern)));
    }
}
