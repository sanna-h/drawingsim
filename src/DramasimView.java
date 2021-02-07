
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import static java.lang.Math.*;

@Getter
@Setter
public class DramasimView extends JPanel {

    double machineTurns = 100;
    boolean viewMachine = false;
    Machine machine;

    public DramasimView(Machine model) {
        machine = model;
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
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;

        g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(0.7f));

        long startTime = System.currentTimeMillis();
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        for (int crankedDegrees = 0; crankedDegrees < machineTurns * 360; crankedDegrees += 2) {
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
        if (viewMachine)
            machine.draw(g2);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DraMaSim");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Machine model = new Machine();
        DramasimView view = new DramasimView(model);
        frame.add(view, BorderLayout.CENTER);

        DramasimController controller = new DramasimController(model, view);

        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new FlowLayout());
        eastPanel.add(controller);
        frame.add(eastPanel, BorderLayout.EAST);

        frame.pack();
        frame.setSize(new Dimension(900, 700));
        frame.setVisible(true);
    }

    void redraw() {
        repaint();
    }
}