package com.bezpredel.opentable.ui;

import com.bezpredel.opentable.Mailer;
import com.sun.org.apache.bcel.internal.generic.NEW;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DelaySettingsDialog extends JPanel {
    private final JTextField label = new JTextField();
    private final JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 5);

    private DelaySettingsDialog(Defaults defaults) {
        super(new GridBagLayout());
        setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(BevelBorder.LOWERED),
                        "Delay Between Requests"
                )
        );


        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.ipadx = 64;
        add(label, c);
        c.ipadx = 250;
        c.gridx = 1;
        c.weightx = 1;
        add(slider, c);
        c.ipadx = 0;
        c.gridx = 2;
        c.weightx = 0;
        add(new JLabel("Seconds"), c);

        int defaultDelay = defaults.getDefaultDelay();
        slider.setValue(translateSecondsToCoef(defaultDelay));
        label.setText(Integer.toString(defaultDelay));

        slider.addChangeListener(
            new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int seconds = translateCoefToSeconds(slider.getValue());
                    label.setText(Integer.toString(seconds));
                }
            }
        );
        label.getDocument().addDocumentListener(
            new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    update();
                }

                private void update() {
                    try {
                        int i = translateSecondsToCoef(Integer.parseInt(label.getText()));
                        slider.setValue(i);
                    } catch (Exception ex) {

                    }
                }
            }
        );
    }


    private static final double coef = Math.pow(3600*4 - 59, 1.0/100);

    private int translateCoefToSeconds(int val) {
        return (int)(59 + Math.pow(coef, val));
    }

    private int translateSecondsToCoef(int val) {
        return (int)(Math.log(val - 59)/Math.log(coef));
    }


    public static int getDelayFromSettingsDialog(Component parentComponent, Defaults defaults) {
        DelaySettingsDialog panel = new DelaySettingsDialog(defaults);

        JOptionPane pane = new JOptionPane(
                panel,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                UIManager.getIcon("OptionPane.questionIcon"),
                new String[]{"Save", "Cancel"},
                "Request Delay Settings"
        );

        JDialog dialog = pane.createDialog(parentComponent, "Request Delay Settings");
        dialog.setName("Request Delay Settings");
        dialog.setVisible(true);
        Object o = pane.getValue();
        if (o!=null && o.equals("Save")) {
            try {
                int i = Integer.parseInt(panel.label.getText());

                defaults.setDefaultDelay(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaults.getDefaultDelay();
    }
}
