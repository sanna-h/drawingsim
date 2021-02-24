import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import lombok.AllArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    FocusTextField valueField;
    FocusTextField deltaField;
}

class FocusTextField extends JTextField {
    static Color backgroundColor = new Color(205,205,205);

    {
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                FocusTextField.this.select(0, FocusTextField.this.getText().length());
            }

            @Override
            public void focusLost(FocusEvent e) {
                FocusTextField.this.select(0, 0);
            }
        });
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(backgroundColor);
    }
    public void setErrorState(boolean error) {
        setBackground(error ? Color.PINK : new Color(205,205,205));
    }
}

public class DramasimController extends JPanel  implements KeyListener {

    Machine model;
    DramasimView view;
    ArrayList<ModelProperty> modelProperties = new ArrayList<>();
    String group;
    Locale currentLocale;
    NumberFormat nf;

    JCheckBox viewMachine = new JCheckBox("Show machine");

    public DramasimController(Machine model, DramasimView view) {

        currentLocale = Locale.getDefault();
        nf = NumberFormat.getInstance(currentLocale);
        nf.setMaximumFractionDigits(13);

        this.model = model;
        this.view = view;

        group = "Setup";
        addProperty("Duration", model::getMachineTurns, model::setMachineTurns);
        addProperty("Canvas speed", model::getCanvasSpeed, model::setCanvasSpeed);
        addProperty("Canvas gearing", model::getCanvasGearing, model::setCanvasGearing);
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
        addProperty("Gearing", model::getTowerAGearing, model::setTowerAGearing);

        group = "Rotor B";
        addProperty("Radius", model::getTowerBRadius, model::setTowerBRadius);
        addProperty("Start angle", model::getTowerBStartAngle, model::setTowerBStartAngle);
        addProperty("Speed", model::getTowerBSpeed, model::setTowerBSpeed);
        addProperty("Gearing", model::getTowerBGearing, model::setTowerBGearing);

        group = "Rotor C";
        addProperty("Radius", model::getTowerCRadius, model::setTowerCRadius);
        addProperty("Start angle", model::getTowerCStartAngle, model::setTowerCStartAngle);
        addProperty("Speed", model::getTowerCSpeed, model::setTowerCSpeed);
        addProperty("Gearing", model::getTowerCGearing, model::setTowerCGearing);
        showPropertyValues();

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(Box.createVerticalGlue());
        for(ModelProperty mp : modelProperties) {
            if (!mp.group.equals(group)) {
                add(Box.createRigidArea(new Dimension(0,2)));
                add(createGroup(mp.group));
                add(Box.createRigidArea(new Dimension(0,2)));
                group = mp.group;
            }
            add(createField(mp.name, mp.valueField, mp.deltaField));
            add(Box.createRigidArea(new Dimension(0,2)));
        }

        add(Box.createRigidArea(new Dimension(0,10)));
        add(viewMachine);
        add(Box.createRigidArea(new Dimension(0,10)));

        JButton drawButton = new JButton("Redraw");
        drawButton.addActionListener(e -> updateAndRedraw());
        add(drawButton);

        JButton addDeltaButton = new JButton("Add delta");
        addDeltaButton.addActionListener(e -> applyDelta(1));
        add(addDeltaButton);

        JButton subtractDeltaButton = new JButton("Subtract delta");
        subtractDeltaButton.addActionListener(e -> applyDelta(-1));
        add(subtractDeltaButton);

        JButton saveButton = new JButton("Save file");
        saveButton.addActionListener(e -> saveToFile());
        add(saveButton);

        JButton loadButton = new JButton("Open file");
        loadButton.addActionListener(e -> loadFile());
        add(loadButton);

    }

    private void updateAndRedraw() {
        for (ModelProperty mp : modelProperties) {
            String text = mp.valueField.getText();
            Double value = parseDouble(text);
            if (value != null)
                mp.setter.accept(value);
            mp.valueField.setErrorState(value == null);
        }
        model.viewMachine = viewMachine.isSelected();
        model.reset();
        view.redraw();
    }

    private void applyDelta(double sign) {
        for (ModelProperty mp : modelProperties) {
            String text = mp.deltaField.getText();
            if (!text.isBlank()) {
                Double delta = parseDouble(text);
                if (delta != null)
                    mp.setter.accept(mp.getter.getAsDouble() + delta * sign);
                mp.deltaField.setErrorState(delta == null);
            }
        }
        showPropertyValues();
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
            showPropertyValues();
            updateAndRedraw();
        }
    }

    private Double parseDouble(String text) {
        try {
            Number value = nf.parse(text.replace('-', new DecimalFormatSymbols(currentLocale).getMinusSign()));
            return value.doubleValue();
        } catch (ParseException ex) {
            return null;
        }
    }

    private void showPropertyValues() {
        for(ModelProperty mp : modelProperties) {
            mp.valueField.setText(nf.format(mp.getter.getAsDouble()));
        }
    }

    private void addProperty(String name, DoubleSupplier getter, DoubleConsumer setter) {
        modelProperties.add(new ModelProperty(group, name, getter, setter, new FocusTextField(), new FocusTextField()));
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

    private JPanel createField(String labelText, JTextField valueField, JTextField deltaField) {
        var panel = new JPanel();
        //panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setLayout(new GridLayout(1, 2));
        JLabel label = new JLabel("    " + labelText + "  ");
        label.setFont(new Font("SansSerif", Font.PLAIN, 10));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(label);

        var valuePanel = new JPanel();
        var layout = new GridLayout(1, 2);
        layout.setHgap(3);
        valuePanel.setLayout(layout);
        valuePanel.add(valueField);
        valueField.addKeyListener(this);
        valuePanel.add(deltaField);
        panel.add(valuePanel);
        return panel;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
            updateAndRedraw();
            if(e.getSource() instanceof FocusTextField)
                ((FocusTextField) e.getSource()).selectAll();
        }

    }

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

}
