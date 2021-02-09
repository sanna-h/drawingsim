import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

@AllArgsConstructor
class ModelProperty {
    String group;
    String name;
    DoubleSupplier getter;
    DoubleConsumer setter;
    JTextField textField;
}

public class DramasimController extends JPanel {

    Machine model;
    DramasimView view;
    ArrayList<ModelProperty> modelProperties = new ArrayList<ModelProperty>();
    String group;

    JCheckBox viewMachine = new JCheckBox("Show machine");

    public DramasimController(Machine model, DramasimView view) {

        group = "Setup";
        addProperty("Duration", view::getMachineTurns, view::setMachineTurns);
        addProperty("Canvas speed", model::getCanvasSpeed, model::setCanvasSpeed);
        addProperty("Scale", view::getScale, view::setScale);

        group = "Measurements";
        addProperty("Canvas - rotor A", model::getTowerADistance, model::setTowerADistance);
        addProperty("Canvas - rotor B", model::getTowerBDistance, model::setTowerBDistance);
        addProperty("Rotor A - rotor B", model::getTowerABDistance, model::setTowerABDistance);
        addProperty("Rod A base", model::getRodABaseLength, model::setRodABaseLength);
        addProperty("Rod A extension", model::getRodAExtensionLength, model::setRodAExtensionLength);
        addProperty("Rod B", model::getRodBLength, model::setRodBLength);

        group = "Rotor A";
        addProperty("Radius", model::getTowerARadius, model::setTowerARadius);
        addProperty("Start angle", model::getTowerAStartAngle, model::setTowerAStartAngle);
        addProperty("Speed", model::getTowerASpeed, model::setTowerASpeed);

        group = "Rotor B";
        addProperty("Radius", model::getTowerBRadius, model::setTowerBRadius);
        addProperty("Start angle", model::getTowerBStartAngle, model::setTowerBStartAngle);
        addProperty("Speed", model::getTowerBSpeed, model::setTowerBSpeed);

        group = "Rotor C";
        addProperty("Radius", model::getTowerCRadius, model::setTowerCRadius);
        addProperty("Start angle", model::getTowerCStartAngle, model::setTowerCStartAngle);
        addProperty("Speed", model::getTowerCSpeed, model::setTowerCSpeed);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(Box.createVerticalGlue());
        for(ModelProperty mp : modelProperties) {
            if (!mp.group.equals(group)) {
                add(Box.createRigidArea(new Dimension(0,2)));
                add(createGroup(mp.group));
                add(Box.createRigidArea(new Dimension(0,2)));
                group = mp.group;
            }
            add(createField(mp.name, mp.textField, mp.getter.getAsDouble()));
            add(Box.createRigidArea(new Dimension(0,2)));
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
        JButton saveButton = new JButton("Save as...");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(model);
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Specify a file to save");

                int userSelection = fileChooser.showSaveDialog(view.getParent());

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave));
                        writer.write(json);
                        writer.close();
                    } catch (IOException ex) {

                    }
                }
            }
        });
        add(saveButton);
    }

    private void addProperty(String name, DoubleSupplier getter, DoubleConsumer setter) {
        modelProperties.add(new ModelProperty(group, name, getter, setter, new JTextField()));
    }

    private JPanel createGroup(String name) {
        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setLayout(new GridLayout(1, 2));
        JLabel label = new JLabel(name, SwingConstants.LEFT);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(110, 0)));
        return panel;
    }

    private JPanel createField(String labelText, JTextField textField, double value) {
        var panel = new JPanel();
        //panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setLayout(new GridLayout(1, 2));
        JLabel label = new JLabel("    " + labelText + "  ");
        label.setFont(new Font("SansSerif", Font.PLAIN, 10));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(label);
        textField.setText(Double.toString(value));
        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setBackground(new Color(205,205,205));
        panel.add(textField);
        return panel;
    }

}
