package com.bezpredel.opentable.ui;

import com.bezpredel.opentable.Mailer;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SMTPSettingsDialog extends JPanel {
    private final JTextField host = new JTextField();
    private final JTextField port = new JTextField();
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JTextField recipients = new JTextField();
    private final JButton test1 = new JButton("Test Connection");
    private final JButton test2 = new JButton("Send Test");


    private SMTPSettingsDialog(Defaults defaults) {
        super(new GridBagLayout());
        setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(BevelBorder.LOWERED),
                        "SMTP Settings"
                )
        );
        test1.setMargin(new Insets(0, 2, 0, 2));
        test2.setMargin(new Insets(0, 2, 0, 2));
        port.setPreferredSize(new Dimension(24, port.getPreferredSize().height));

        GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1,0,1,0), 0, 0 );
        c.gridx = 0;
        c.gridy = 0;

        c.gridwidth = 6;
        add(new JLabel("SMTP host:port"), c);
        c.gridwidth = 2;
        c.gridy++;
        c.weightx = 1;
        add(host, c);
        c.weightx = 0;
        c.gridwidth = 1;
        c.gridx++;
        add(new JLabel(" :"), c);
        c.gridx++;
        add(port, c);


        c.gridy++;
        c.gridx = 0;
        add(new JLabel("Username (email)"), c);
        c.gridx++;
        c.gridwidth = 5;
        add(username, c);
        c.gridwidth = 1;
        c.gridy++;
        c.gridx = 0;
        add(new JLabel("Password"), c);
        c.gridx++;
        c.gridwidth = 5;
        add(password, c);
        c.gridwidth = 6;

        c.gridy++;
        c.gridx = 0;
        this.add(new JLabel("Recipients (comma-separated)"), c);
        c.gridy++;
        c.gridx = 0;
        this.add(recipients, c);

        c.gridy++;
        c.gridx = 0;
        add(test1, c);
        c.gridy++;
        add(test2, c);

        host.setText( defaults.getDefaultMailHost() );
        port.setText( defaults.getDefaultMailPort() + "" );
        username.setText( defaults.getDefaultMailUser() );
        password.setText( defaults.getDefaultMailPassword() );
        recipients.setText( defaults.getDefaultMailRecipients() );

        test1.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doTest(false);
                    }
                }
        );

        test2.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doTest(true);
                    }
                }
        );
    }

    private void doTest(boolean send) {
        Mailer mailer = new Mailer(host.getText(), port.getText(), username.getText(), password.getText(), username.getText(), recipients.getText());
        try {
            if(send) {
                mailer.sendMail("Test email from OpenTable pinger", "Test email from OpenTable pinger");
            } else {
                mailer.test();
            }
            JOptionPane.showMessageDialog(this, "Successful");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to connect");
        }
    }


    public static Mailer getMailerFromSettingsDialog(Component parentComponent, Defaults defaults) {
        SMTPSettingsDialog panel = new SMTPSettingsDialog(defaults);

        JOptionPane pane = new JOptionPane(
                panel,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                UIManager.getIcon("OptionPane.questionIcon"),
                new String[]{"Save", "Cancel"},
                "SMTP Settings"
        );

        JDialog dialog = pane.createDialog(parentComponent, "SMTP Settings");
        dialog.setName("SMTP Settings");
        dialog.setVisible(true);
        Object o = pane.getValue();
        if (o==null || !o.equals("Save")) {
            return null;
        } else {
            try {
                String host = panel.host.getText();
                String port = (panel.port.getText());
                String username = panel.username.getText();
                String password = panel.password.getText();
                String recipients = panel.recipients.getText();

                defaults.setDefaultMailHost(host);
                defaults.setDefaultMailPort(port);
                defaults.setDefaultMailUser(username);
                defaults.setDefaultMailPassword(password);
                defaults.setDefaultMailRecipients(recipients);

                return new Mailer(host, port, username, password, username, recipients);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static Mailer getMailerFromDefaults(Defaults defaults) {
        return new Mailer(
                defaults.getDefaultMailHost(),
                defaults.getDefaultMailPort(),
                defaults.getDefaultMailUser(),
                defaults.getDefaultMailPassword(),
                defaults.getDefaultMailUser(),
                defaults.getDefaultMailRecipients()
        );
    }
}
