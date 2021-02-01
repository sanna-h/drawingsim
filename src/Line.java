import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Line {
    public Point start;
    public Point end;

    public Line(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public void draw(Graphics2D g2) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        path.moveTo(start.x, start.y);
        path.lineTo(end.x, end.y);
        g2.draw(path);
        start.draw(g2);
        end.draw(g2);
    }

    public double getLength() {
        return sqrt(pow(end.x - start.x ,2) + pow(end.y - start.y, 2));
    }

    public void setLength(double newLength) {
        double factor = newLength / getLength();
        Point newEnd = new Point(start.x + (end.x - start.x) * factor, start.y + (end.y-start.y) * factor);
        end = newEnd;
    }

    public void rotate(double theta) {
        end.Rotate(start, theta);
    }

}
