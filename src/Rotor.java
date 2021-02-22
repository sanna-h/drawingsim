import java.awt.*;
import java.awt.geom.Ellipse2D;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

class Rotor {

    public Point location;
    public Rotor baseRotor;
    public double radius;
    public double speed;
    public double gearing;
    public double startTheta;
    public double theta;

    public Rotor(Point location, double radius, double speed, double gearing, double startTheta) {
        this.location = location;
        this.radius = radius;
        this.speed = speed;
        this.gearing = gearing;
        this.startTheta = startTheta;
    }

    public Rotor(Rotor baseRotor, double radius, double speed, double gearing, double startTheta) {
        this.baseRotor = baseRotor;
        this.radius = radius;
        this.speed = speed;
        this.gearing = gearing;
        this.startTheta = startTheta;
    }

    private Point getCenter() {
        if(baseRotor != null)
            return baseRotor.getConnectingPoint();
        else
            return location;
    }

    public void draw(Graphics2D g2) {
        Point center = getCenter();
        g2.setColor(Color.blue);
        Shape theCircle = new Ellipse2D.Double(center.x - radius, center.y - radius, 2.0 * radius, 2.0 * radius);
        g2.draw(theCircle);

        g2.setColor(Color.red);
        if (baseRotor != null)
            baseRotor.getConnectingPoint().draw(g2);
    }

    private double getThetaSum() {
        return startTheta + theta + (baseRotor != null ? baseRotor.getThetaSum() : 0.0);

    }

    public Point getConnectingPoint() {
        Point center = getCenter();
        double thetaSum = getThetaSum();
        return new Point(center.x + cos(thetaSum) * radius, center.y + sin(thetaSum) * radius);
    }

}
