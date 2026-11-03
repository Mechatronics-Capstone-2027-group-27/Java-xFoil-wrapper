package matz.xfoil;

public class XfoilOut {
    private final double alpha;
    private final double cl;
    private final double cm;
    private final double cd;
    private final double cdf;
    private final double cdp;

    public XfoilOut(double alpha, double cl, double cm,
                    double cd, double cdf, double cdp) {
        this.alpha = alpha;
        this.cl = cl;
        this.cm = cm;
        this.cd = cd;
        this.cdf = cdf;
        this.cdp = cdp;
    }

    public double alpha() {
        return alpha;
    }

    public double cl() {
        return cl;
    }

    public double cm() {
        return cm;
    }

    public double cd() {
        return cd;
    }

    public double cdf() {
        return cdf;
    }

    public double cdp() {
        return cdp;
    }
}
