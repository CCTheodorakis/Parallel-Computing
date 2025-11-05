import java.util.Locale;

public class Body {
    public double x, y;
    public double vx, vy;
    public double mass;
    public String name;
    public double fx, fy;

    public Body(double x, double y, double vx, double vy, double mass, String name) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.mass = mass;
        this.name = name;
        this.fx = 0;
        this.fy = 0;
    }

    public double distanceTo(Body other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        return Math.sqrt(dx*dx + dy*dy);
    }

    public void addForce(Body other) {
        final double G = 6.67e-11;
        double r = this.distanceTo(other);
        if (r == 0) return;
        double F = G * this.mass * other.mass / (r*r);
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        this.fx += F * dx / r;
        this.fy += F * dy / r;
    }

    /*Update pos and speed after dt step*/
    public void update(double dt) {
        double ax = fx / mass;
        double ay = fy / mass;
        vx += ax * dt;
        vy += ay * dt;
        x += vx * dt;
        y += vy * dt;
        fx = 0;
        fy = 0;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%.6f %.6f %.6f %.6f %.6e %s", x, y, vx, vy, mass, name);
    }

}