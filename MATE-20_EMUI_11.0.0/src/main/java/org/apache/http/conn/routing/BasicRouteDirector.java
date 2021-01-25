package org.apache.http.conn.routing;

@Deprecated
public class BasicRouteDirector implements HttpRouteDirector {
    @Override // org.apache.http.conn.routing.HttpRouteDirector
    public int nextStep(RouteInfo plan, RouteInfo fact) {
        if (plan == null) {
            throw new IllegalArgumentException("Planned route may not be null.");
        } else if (fact == null || fact.getHopCount() < 1) {
            return firstStep(plan);
        } else {
            if (plan.getHopCount() > 1) {
                return proxiedStep(plan, fact);
            }
            return directStep(plan, fact);
        }
    }

    /* access modifiers changed from: protected */
    public int firstStep(RouteInfo plan) {
        if (plan.getHopCount() > 1) {
            return 2;
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public int directStep(RouteInfo plan, RouteInfo fact) {
        if (fact.getHopCount() > 1 || !plan.getTargetHost().equals(fact.getTargetHost()) || plan.isSecure() != fact.isSecure()) {
            return -1;
        }
        if (plan.getLocalAddress() == null || plan.getLocalAddress().equals(fact.getLocalAddress())) {
            return 0;
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public int proxiedStep(RouteInfo plan, RouteInfo fact) {
        int phc;
        int fhc;
        if (fact.getHopCount() <= 1 || !plan.getTargetHost().equals(fact.getTargetHost()) || (phc = plan.getHopCount()) < (fhc = fact.getHopCount())) {
            return -1;
        }
        for (int i = 0; i < fhc - 1; i++) {
            if (!plan.getHopTarget(i).equals(fact.getHopTarget(i))) {
                return -1;
            }
        }
        if (phc > fhc) {
            return 4;
        }
        if ((fact.isTunnelled() && !plan.isTunnelled()) || (fact.isLayered() && !plan.isLayered())) {
            return -1;
        }
        if (plan.isTunnelled() && !fact.isTunnelled()) {
            return 3;
        }
        if (plan.isLayered() && !fact.isLayered()) {
            return 5;
        }
        if (plan.isSecure() != fact.isSecure()) {
            return -1;
        }
        return 0;
    }
}
