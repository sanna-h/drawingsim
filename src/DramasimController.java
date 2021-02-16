import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import lombok.AllArgsConstructor;

import javax.naming.PartialResultException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
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

class FocusTextField extends JTextField {
    {
        addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                FocusTextField.this.select(0, getText().length());
            }

            @Override
            public void focusLost(FocusEvent e) {
                FocusTextField.this.select(0, 0);
            }
        });
    }
}

public class DramasimController extends JPanel {

    Machine model;
    DramasimView view;
    ArrayList<ModelProperty> modelProperties = new ArrayList<>();
    String group;

    JCheckBox viewMachine = new JCheckBox("Show machine");

    public DramasimController(Machine model, DramasimView view) {

        this.model = model;
        this.view = view;

        group = "Setup";
        addProperty("Duration", model::getMachineTurns, model::setMachineTurns);
        addProperty("Canvas speed", model::getCanvasSpeed, model::setCanvasSpeed);
        addProperty("Canvas width", model::getCanvasWidth, model::setCanvasWidth);
        addProperty("Canvas height", model::getCanvasHeight, model::setCanvasHeight);
        addProperty("Scale", model::getScale, model::setScale);

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
        drawButton.addActionListener(e -> updateAndRedraw());
        add(drawButton);

        JButton saveButton = new JButton("Save file");
        saveButton.addActionListener(e -> saveToFile());
        add(saveButton);

        JButton loadButton = new JButton("Open file");
        loadButton.addActionListener(e -> loadFile());
        add(loadButton);

    }

    private void updateAndRedraw() {
        for (ModelProperty mp : modelProperties) {
            NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
            String text = mp.textField.getText();
            try {
                Number value = nf.parse(text.replace('-', new DecimalFormatSymbols().getMinusSign()));
                double doubleValue = value.doubleValue();
                mp.setter.accept(doubleValue);
            } catch (ParseException ex) {
                mp.textField.setBackground(Color.PINK);
            }
        }
        model.viewMachine = viewMachine.isSelected();
        model.reset();
        view.redraw();
    }

    private void saveToFile() {
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
                JOptionPane.showMessageDialog(null, "File saved.", "Save", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Failed to save file: " + ex.getMessage(), "Save", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to load");

        int userSelection = fileChooser.showOpenDialog(view.getParent());

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            String jsonString;
            try {
                jsonString = Files.readString(fileToLoad.toPath(), Charset.defaultCharset());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Failed to load file: " + ex.getMessage(), "Load", JOptionPane.ERROR_MESSAGE);
                return;
            }

            InstanceCreator<Machine> creator = type -> model;
            Gson gson = new GsonBuilder().registerTypeAdapter(Machine.class, creator).create();
            gson.fromJson(jsonString, Machine.class);
            for(ModelProperty mp : modelProperties) {
                NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
                mp.textField.setText(nf.format(mp.getter.getAsDouble()));
            }
            updateAndRedraw();
        }

    }

    private void addProperty(String name, DoubleSupplier getter, DoubleConsumer setter) {
        modelProperties.add(new ModelProperty(group, name, getter, setter, new FocusTextField()));
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
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        textField.setText(nf.format(value));
        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setBackground(new Color(205,205,205));
        textField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    updateAndRedraw();
                    if(e.getSource() instanceof FocusTextField)
                        ((FocusTextField) e.getSource()).selectAll();
                }

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        panel.add(textField);
        return panel;
    }

}
