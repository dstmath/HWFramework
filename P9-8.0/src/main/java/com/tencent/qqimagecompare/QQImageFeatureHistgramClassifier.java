package com.tencent.qqimagecompare;

import java.util.ArrayList;
import java.util.Iterator;

public class QQImageFeatureHistgramClassifier {
    private int mThreshold = 80;
    private ArrayList<QQImageFeatureHSV> nE = new ArrayList();
    private IClassifyCallback nF;
    private Object nG;
    private int nH;

    public interface IClassifyCallback {
        void OnStep(int i, Object obj);
    }

    public void addFeature(QQImageFeatureHSV qQImageFeatureHSV) {
        this.nE.add(qQImageFeatureHSV);
        this.nH = this.nE.size();
    }

    public ArrayList<ArrayList<QQImageFeatureHSV>> classify() {
        ArrayList<ArrayList<QQImageFeatureHSV>> arrayList = new ArrayList();
        if (this.nE.size() > 0) {
            while (true) {
                int size = this.nE.size();
                ArrayList arrayList2;
                if (size != 1) {
                    if (size <= 1) {
                        break;
                    }
                    arrayList2 = new ArrayList();
                    QQImageFeatureHSV qQImageFeatureHSV = (QQImageFeatureHSV) this.nE.remove(0);
                    arrayList2.add(qQImageFeatureHSV);
                    Iterator it = this.nE.iterator();
                    while (it.hasNext()) {
                        QQImageFeatureHSV qQImageFeatureHSV2 = (QQImageFeatureHSV) it.next();
                        if (qQImageFeatureHSV.compare(qQImageFeatureHSV2) >= this.mThreshold) {
                            arrayList2.add(qQImageFeatureHSV2);
                            it.remove();
                        }
                    }
                    arrayList.add(arrayList2);
                    if (this.nF != null) {
                        this.nF.OnStep(((this.nH - this.nE.size()) * 100) / this.nH, this.nG);
                    }
                } else {
                    arrayList2 = new ArrayList();
                    arrayList2.add(this.nE.remove(0));
                    arrayList.add(arrayList2);
                    if (this.nF != null) {
                        this.nF.OnStep(((this.nH - this.nE.size()) * 100) / this.nH, this.nG);
                    }
                }
            }
        }
        return arrayList;
    }

    public void setClassifyCallback(IClassifyCallback iClassifyCallback, Object obj) {
        this.nF = iClassifyCallback;
        this.nG = obj;
    }

    public void setThreshold(int i) {
        this.mThreshold = i;
    }
}
