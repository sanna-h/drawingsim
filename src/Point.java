import java.awt.*;
import java.awt.geom.Ellipse2D;

import static java.lang.Math.*;
import static java.lang.Math.pow;

class Point {
    public double x;
    public double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    void Rotate(Point origo, double theta) {
        double rx = cos(theta) * (x-origo.x) - sin(theta) * (y-origo.y) + origo.x;
        double ry = sin(theta) * (x-origo.x) + cos(theta) * (y-origo.y) + origo.y;
        x = rx;
        y = ry;
    }

    void Move(Point offset) {
        x += offset.x;
        y += offset.y;
    }

    double GetDistanceFrom(Point other) {
        return (sqrt(pow(x-other.x, 2) + pow(y-other.y, 2)));
    }

    void draw(Graphics2D g2) {
        Shape dot = new Ellipse2D.Double(x - 2, y - 2, 4, 4);
        g2.draw(dot);
    }
}
