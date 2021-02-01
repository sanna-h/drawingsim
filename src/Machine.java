import java.awt.*;

import static java.lang.Math.*;

public class Machine {

    double canvasSpeed = 0.01;

    double towerASpeed = 2; // -0.5;
    double towerADistance = 301;
    double towerARadius = 60;
    double towerAStartTheta = 0;

    double towerBSpeed = 2.03; //1;
    double towerBDistance = 225;
    double towerBRadius = 45;
    double towerBStartTheta = 0;

    double towerABDistance = 180;
    double towerCSpeed = 0.5; //-1.5;
    double towerCRadius = 0;
    double towerCStartTheta = 0;

    double rodABaseLength = 225;
    double rodAExtensionLength = 112.5;
    double rodBLength = 225;

    Rotor canvas;
    Rotor towerA;
    Rotor towerB;
    Rotor towerC;
    Line rodA;
    Line rodB;

    public Machine() {
        canvas = new Rotor(new Point(300, 300), 0, canvasSpeed, 0);

        double towerABTheta = getThetaFromLegs(towerADistance, towerBDistance, towerABDistance);
        Point towerALocation = new Point(canvas.location.x, canvas.location.y + towerADistance);
        towerALocation.Rotate(canvas.location, -towerABTheta / 2);
        Point towerBLocation = new Point(canvas.location.x, canvas.location.y + towerBDistance);
        towerBLocation.Rotate(canvas.location, towerABTheta / 2);

        towerA = new Rotor(towerALocation, towerARadius, towerASpeed, towerAStartTheta);
        towerB = new Rotor(towerBLocation, towerBRadius, towerBSpeed, towerBStartTheta);
        towerC = new Rotor(towerB, towerCRadius, towerCSpeed, towerCStartTheta);
    }

    public void setRotation(double theta) {
        canvas.theta = theta * canvas.speed;
        towerA.theta = theta * towerA.speed;
        towerB.theta = theta * towerB.speed;
        towerC.theta = theta * towerC.speed;
        moveRods();
    }

    private void moveRods() {
        Line base = new Line(towerA.getConnectingPoint(), towerC.getConnectingPoint());
        double baseLength = base.getLength();
        double thetaA = getThetaFromLegs(baseLength, rodABaseLength, rodBLength);
        rodA = base;
        rodA.rotate(thetaA);
        rodA.setLength(rodABaseLength);
        rodB = new Line(towerC.getConnectingPoint(), rodA.end);
        rodA.setLength(rodABaseLength + rodAExtensionLength);
    }

    private double getThetaFromLegs(double legA, double legB, double oppositeLeg) {
        return acos((pow(legA, 2) + pow(legB, 2) - pow(oppositeLeg, 2)) / (2 * legA * legB));
    }

    public Point getPenPositionOnCanvas() {
        Point p = new Point(rodA.end.x, rodA.end.y);
        p.Rotate(canvas.location, canvas.theta);
        return p;
    }

    public void draw(Graphics2D g2) {
        towerA.draw(g2);
        towerB.draw(g2);
        towerC.draw(g2);
        rodA.draw(g2);
        rodB.draw(g2);
    }

}
