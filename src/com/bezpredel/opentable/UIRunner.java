package com.bezpredel.opentable;

import com.bezpredel.opentable.ui.*;

import javax.swing.*;

/**
 * Date: 7/30/12
 * Time: 4:17 PM
 */
public class UIRunner {
    static {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            startup();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                }
        );
    }

    private static void startup() throws Exception {
        //21790 Babbo Ristorante
        //211 11Madison
        Defaults defaults = new Defaults();
        RequestSender requestSender = new RequestSender();


        Logger logger = new Logger();
        AvailabilityPanel availabilityPanel = new AvailabilityPanel();

        TaskRunner taskRunner = new TaskRunner(
                requestSender,
                null,
                logger
        );
        taskRunner.setDelay(defaults.getDefaultDelay());

        MainFrame mainFrame = new MainFrame(requestSender, taskRunner, logger, defaults, availabilityPanel);
        mainFrame.setVisible(true);

        if (!defaults.hasHostAndPort()) {
            mainFrame.showSMTPSettings();
        } else {
            taskRunner.setMailer(
                    SMTPSettingsDialog.getMailerFromDefaults(defaults)
            );
        }
    }


}
