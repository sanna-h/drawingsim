
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import static java.lang.Math.*;

public class BasicEx extends JComponent {

    double maxTheta = 100 * 2 * PI;
    Machine machine = new Machine();

    BufferedImage canvas = new BufferedImage(700, 700, BufferedImage.TYPE_INT_ARGB);

    public BasicEx() {
        Timer timer = new Timer(1000, new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                //crankedDegrees += 10;
                repaint();
            }
        });
        timer.start();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(0.7f));

        long startTime = System.currentTimeMillis();
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        for (int crankedDegrees = 0; crankedDegrees < 100 * 360; crankedDegrees += 2) {
            double theta = crankedDegrees * 2 * PI / 360;
            machine.setRotation(theta);

            Point p = machine.getPenPositionOnCanvas();
            if (crankedDegrees == 0) {
                path.moveTo(p.x, p.y);
            } else {
                path.lineTo(p.x, p.y);
            }
        }
        g2.draw(path);
        g2.setStroke(new BasicStroke(4.0f));
        long executionTime = System.currentTimeMillis() - startTime;
        machine.draw(g2);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Ritmaskinssimulator");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new GridLayout(1, 1));
        frame.getContentPane().add(new BasicEx());
        //frame.getContentPane().add(new JButton("Uppdatera"));
        frame.pack();
        frame.setSize(new Dimension(700, 700));
        frame.setVisible(true);
    }
}