package org.apache.http.conn.routing;

@Deprecated
public class BasicRouteDirector implements HttpRouteDirector {
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

    protected int firstStep(RouteInfo plan) {
        if (plan.getHopCount() > 1) {
            return 2;
        }
        return 1;
    }

    protected int directStep(RouteInfo plan, RouteInfo fact) {
        if (fact.getHopCount() > 1 || !plan.getTargetHost().equals(fact.getTargetHost()) || plan.isSecure() != fact.isSecure()) {
            return -1;
        }
        if (plan.getLocalAddress() == null || (plan.getLocalAddress().equals(fact.getLocalAddress()) ^ 1) == 0) {
            return 0;
        }
        return -1;
    }

    protected int proxiedStep(RouteInfo plan, RouteInfo fact) {
        if (fact.getHopCount() <= 1 || !plan.getTargetHost().equals(fact.getTargetHost())) {
            return -1;
        }
        int phc = plan.getHopCount();
        int fhc = fact.getHopCount();
        if (phc < fhc) {
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
        if ((fact.isTunnelled() && (plan.isTunnelled() ^ 1) != 0) || (fact.isLayered() && (plan.isLayered() ^ 1) != 0)) {
            return -1;
        }
        if (plan.isTunnelled() && (fact.isTunnelled() ^ 1) != 0) {
            return 3;
        }
        if (plan.isLayered() && (fact.isLayered() ^ 1) != 0) {
            return 5;
        }
        if (plan.isSecure() != fact.isSecure()) {
            return -1;
        }
        return 0;
    }
}
