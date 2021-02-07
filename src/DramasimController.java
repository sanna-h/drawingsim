import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

@AllArgsConstructor
class ModelProperty {
    String name;
    DoubleSupplier getter;
    DoubleConsumer setter;
    JTextField textField;
}

public class DramasimController extends JPanel {

    Machine model;
    DramasimView view;
    ArrayList<ModelProperty> modelProperties = new ArrayList<ModelProperty>();

    JCheckBox viewMachine = new JCheckBox("Show machine");

    public DramasimController(Machine model, DramasimView view) {

        addProperty("Duration", view::getMachineTurns, view::setMachineTurns);
        addProperty("Canvas speed", model::getCanvasSpeed, model::setCanvasSpeed);

        addProperty("Distance canvas - rotor A", model::getTowerADistance, model::setTowerADistance);
        addProperty("Distance canvas - rotor B", model::getTowerBDistance, model::setTowerBDistance);
        addProperty("Distance rotor A - B", model::getTowerABDistance, model::setTowerABDistance);
        addProperty("Rod A base length", model::getRodABaseLength, model::setRodABaseLength);
        addProperty("Rod A extension length", model::getRodAExtensionLength, model::setRodAExtensionLength);
        addProperty("Rod B length", model::getRodBLength, model::setRodBLength);

        addProperty("Rotor A radius", model::getTowerARadius, model::setTowerARadius);
        addProperty("Rotor B radius", model::getTowerBRadius, model::setTowerBRadius);
        addProperty("Rotor C radius", model::getTowerCRadius, model::setTowerCRadius);
        addProperty("Rotor A start angle", model::getTowerAStartTheta, model::setTowerAStartTheta);
        addProperty("Rotor B start angle", model::getTowerBStartTheta, model::setTowerBStartTheta);
        addProperty("Rotor C start angle", model::getTowerCStartTheta, model::setTowerCStartTheta);

        addProperty("Rotor A speed", model::getTowerASpeed, model::setTowerASpeed);
        addProperty("Rotor B speed", model::getTowerBSpeed, model::setTowerBSpeed);
        addProperty("Rotor C speed", model::getTowerCSpeed, model::setTowerCSpeed);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(Box.createVerticalGlue());
        for(ModelProperty mp : modelProperties) {
            add(createField(mp.name, mp.textField, mp.getter.getAsDouble()));
        }

        add(Box.createRigidArea(new Dimension(0,10)));
        add(viewMachine);
        add(Box.createRigidArea(new Dimension(0,10)));
        JButton drawButton = new JButton("Redraw");
        drawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ModelProperty mp : modelProperties) {
                    mp.setter.accept(Double.parseDouble(mp.textField.getText()));
                }
                view.viewMachine = viewMachine.isSelected();
                model.reset();
                view.redraw();
            }
        });
        add(drawButton);
    }

    private void addProperty(String name, DoubleSupplier getter, DoubleConsumer setter) {
        modelProperties.add(new ModelProperty(name, getter, setter, new JTextField()));
    }

    private JPanel createField(String labelText, JTextField textField, double value) {
        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 10));
        panel.add(label);
        textField.setText(Double.toString(value));
        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setBackground(Color.LIGHT_GRAY);
        panel.add(textField);
        return panel;
    }

}
