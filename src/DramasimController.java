import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DramasimController extends JPanel {

    Machine model;
    DramasimView view;

    JTextField machineTurns = new JTextField();
    JTextField towerASpeed = new JTextField();
    JTextField towerBSpeed = new JTextField();
    JCheckBox viewMachine = new JCheckBox("Show machine");

    public DramasimController(Machine model, DramasimView view) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(Box.createVerticalGlue());
        add(createField("Duration", machineTurns, view.machineTurns));
        add(createField("Rotor A speed", towerASpeed, model.towerASpeed));
        add(createField("Rotor B speed", towerBSpeed, model.towerBSpeed));
        add(Box.createRigidArea(new Dimension(0,10)));
        add(viewMachine);
        add(Box.createRigidArea(new Dimension(0,10)));
        JButton drawButton = new JButton("Redraw");
        drawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.machineTurns = Double.parseDouble(machineTurns.getText());
                model.towerASpeed = Double.parseDouble(towerASpeed.getText());
                model.towerBSpeed = Double.parseDouble(towerBSpeed.getText());
                view.viewMachine = viewMachine.isSelected();
                model.reset();
                view.redraw();
            }
        });
        add(drawButton);
    }

    private JPanel createField(String labelText, JTextField textField, double value) {
        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(new JLabel(labelText));
        textField.setText(Double.toString(value));
        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setBackground(Color.LIGHT_GRAY);
        panel.add(textField);
        return panel;
    }

}
