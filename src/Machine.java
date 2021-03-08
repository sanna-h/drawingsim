import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.geom.GeneralPath;

import static java.lang.Math.*;

@Getter
@Setter
public class Machine {

    double canvasWidth = 210;
    double canvasHeight = 297;
    double canvasSpeed = 0.01;

    double towerASpeed = 2; // -0.5;
    double towerADistance = 301;
    double towerARadius = 60;
    double towerAStartAngle = 0;

    double towerBSpeed = 2.03; //1;
    double towerBDistance = 225;
    double towerBRadius = 45;
    double towerBStartAngle = 0;

    double towerABDistance = 180;
    double towerCSpeed = 0.5; //-1.5;
    double towerCRadius = 0;
    double towerCStartAngle = 0;

    double rodABaseLength = 225;
    double rodAExtensionLength = 112.5;
    double rodBLength = 225;

    double machineTurns = 100;
    double scale = 1;
    double penWidth = 0.7;
    boolean viewMachine = false;

    double canvasGearing = 1;
    double towerAGearing = 1;
    double towerBGearing = 1;
    double towerCGearing = 1;

    transient Rotor canvas;
    transient Rotor towerA;
    transient Rotor towerB;
    transient Rotor towerC;
    transient Line rodA;
    transient Line rodB;

    public Machine() {
        reset();
    }

    public void reset() {
        canvas = new Rotor(new Point(0, 0), 0, canvasSpeed, canvasGearing, 0);

        double towerABTheta = getThetaFromLegs(towerADistance, towerBDistance, towerABDistance);
        Point towerALocation = new Point(canvas.location.x, canvas.location.y + towerADistance);
        towerALocation.Rotate(canvas.location, -towerABTheta / 2);
        Point towerBLocation = new Point(canvas.location.x, canvas.location.y + towerBDistance);
        towerBLocation.Rotate(canvas.location, towerABTheta / 2);

        towerA = new Rotor(towerALocation, towerARadius, towerASpeed, towerAGearing, Math.toRadians(towerAStartAngle));
        towerB = new Rotor(towerBLocation, towerBRadius, towerBSpeed, towerBGearing, Math.toRadians(towerBStartAngle));
        towerC = new Rotor(towerB, towerCRadius, towerCSpeed, towerCGearing, Math.toRadians(towerCStartAngle));
    }

    public void setRotation(double theta) {
        canvas.theta = theta * canvas.speed * canvas.gearing;
        towerA.theta = theta * towerA.speed * towerA.gearing;
        towerB.theta = theta * towerB.speed * towerB.gearing;
        towerC.theta = theta * towerC.speed * towerC.gearing;
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
        canvas.location.draw(g2);
        rodA.draw(g2);
        rodB.draw(g2);
        g2.setColor(Color.GRAY);
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        path.moveTo(canvas.location.x - canvasWidth / 2, canvas.location.y - canvasHeight / 2);
        path.lineTo(canvas.location.x + canvasWidth / 2, canvas.location.y - canvasHeight / 2);
        path.lineTo(canvas.location.x + canvasWidth / 2, canvas.location.y + canvasHeight / 2);
        path.lineTo(canvas.location.x - canvasWidth / 2, canvas.location.y + canvasHeight / 2);
        path.closePath();
        g2.draw(path);
    }

}
