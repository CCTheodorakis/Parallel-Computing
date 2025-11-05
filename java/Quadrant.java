public class Quadrant {
    public double x, y;  /*Centrer*/
    public double length;

    public Quadrant(double x, double y, double length) {
        this.x = x;
        this.y = y;
        this.length = length;
    }

    public boolean contains(Body b) {
        double halfLength = length / 2.0;
        return (b.x >= x - halfLength && b.x <= x + halfLength &&
                b.y >= y - halfLength && b.y <= y + halfLength);
    }

    public Quadrant nw() { return new Quadrant(x - length/4, y + length/4, length/2); }
    public Quadrant ne() { return new Quadrant(x + length/4, y + length/4, length/2); }
    public Quadrant sw() { return new Quadrant(x - length/4, y - length/4, length/2); }
    public Quadrant se() { return new Quadrant(x + length/4, y - length/4, length/2); }
}