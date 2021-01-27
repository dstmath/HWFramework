package ohos.com.sun.org.apache.xerces.internal.impl.dv;

import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;

public class XSFacets {
    public XSObjectList enumAnnotations;
    public Vector enumNSDecls;
    public Vector enumeration;
    public int fractionDigits;
    public XSAnnotation fractionDigitsAnnotation;
    public int length;
    public XSAnnotation lengthAnnotation;
    public String maxExclusive;
    public XSAnnotation maxExclusiveAnnotation;
    public String maxInclusive;
    public XSAnnotation maxInclusiveAnnotation;
    public int maxLength;
    public XSAnnotation maxLengthAnnotation;
    public String minExclusive;
    public XSAnnotation minExclusiveAnnotation;
    public String minInclusive;
    public XSAnnotation minInclusiveAnnotation;
    public int minLength;
    public XSAnnotation minLengthAnnotation;
    public String pattern;
    public XSObjectListImpl patternAnnotations;
    public int totalDigits;
    public XSAnnotation totalDigitsAnnotation;
    public short whiteSpace;
    public XSAnnotation whiteSpaceAnnotation;

    public void reset() {
        this.lengthAnnotation = null;
        this.minLengthAnnotation = null;
        this.maxLengthAnnotation = null;
        this.whiteSpaceAnnotation = null;
        this.totalDigitsAnnotation = null;
        this.fractionDigitsAnnotation = null;
        this.patternAnnotations = null;
        this.enumAnnotations = null;
        this.maxInclusiveAnnotation = null;
        this.maxExclusiveAnnotation = null;
        this.minInclusiveAnnotation = null;
        this.minExclusiveAnnotation = null;
    }
}
