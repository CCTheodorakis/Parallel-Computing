public class BHTree {
    private static final double THETA = 0.5;

    private Body body;
    private Quadrant quad;
    private double mass;
    private double cx, cy;
    private BHTree nw, ne, sw, se;

    public BHTree(Quadrant quad) {
        this.quad = quad;
        this.body = null;
        this.mass = 0;
        this.nw = null;
        this.ne = null;
        this.sw = null;
        this.se = null;
    }

    /*insert body to tree*/
    public void insert(Body b) {
        /*If node emtpy*/
        if (body == null) {
            body = b;
            mass = b.mass;
            cx = b.x;
            cy = b.y;
            return;
        }
        if (!isExternal()) {
            double totalMass = mass + b.mass;
            cx = (cx * mass + b.x * b.mass) / totalMass;
            cy = (cy * mass + b.y * b.mass) / totalMass;
            mass = totalMass;

            insertToChild(b);
            return;
        }
        Body existingBody = this.body;
        this.body = null;
        double totalMass = mass + b.mass;
        cx = (cx * mass + b.x * b.mass) / totalMass;
        cy = (cy * mass + b.y * b.mass) / totalMass;
        mass = totalMass;

        insertToChild(existingBody);
        insertToChild(b);
    }

    private void insertToChild(Body b) {
        if (quad.nw().contains(b)) {
            if (nw == null) nw = new BHTree(quad.nw());
            nw.insert(b);
        } else if (quad.ne().contains(b)) {
            if (ne == null) ne = new BHTree(quad.ne());
            ne.insert(b);
        } else if (quad.sw().contains(b)) {
            if (sw == null) sw = new BHTree(quad.sw());
            sw.insert(b);
        } else if (quad.se().contains(b)) {
            if (se == null) se = new BHTree(quad.se());
            se.insert(b);
        }
    }

    /*If node is leaf*/
    public boolean isExternal() {
        return nw == null && ne == null && sw == null && se == null;
    }

    public void calculateForce(Body b) {
        if (body == null) return;

        if (isExternal()) {
            if (body != b) {
                b.addForce(body);
            }
            return;
        }

        double d = Math.sqrt((b.x - cx)*(b.x - cx) + (b.y - cy)*(b.y - cy));
        double s = quad.length;

        if (s/d < THETA) {
            Body cm = new Body(cx, cy, 0, 0, mass, "CM");
            b.addForce(cm);
        } else {
            if (nw != null) nw.calculateForce(b);
            if (ne != null) ne.calculateForce(b);
            if (sw != null) sw.calculateForce(b);
            if (se != null) se.calculateForce(b);
        }
    }
}