package com.bezpredel.opentable.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Date: 7/30/12
 * Time: 8:28 PM
 */
public class AvailabilityPanel extends JPanel{
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEEE, d MMMMM yyyy");
    private final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    public AvailabilityPanel() {
        super(new BorderLayout());
        setBackground(Color.WHITE);
    }

    public void updateAvailability(List<Date> dates) {
        this.removeAll();
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = gbc.HORIZONTAL;
        gbc.anchor = gbc.NORTHWEST;
        gbc.insets = new Insets(1, 2, 2, 2);
        Insets insets = new Insets(0, 1, 0, 1);
        Font font = new Font("SansSerif", Font.BOLD, 14);
        Border underline = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK);
        String dateLabel = "";
        JPanel currentPanel = null;

        if(dates.isEmpty()) {
            JLabel l = new JLabel("No availability");
            l.setForeground(Color.RED);
            l.setFont(font);
            l.setBorder(underline);
            p.add(l, gbc);
        } else {
            gbc.gridy = 0;
            gbc.gridx = 0;
            gbc.weightx=0;

            for (Date date : dates) {
                String dl = DATE_FORMAT.format(date);
                if(!dl.equals(dateLabel)) {
                    dateLabel = dl;
                    JLabel l = new JLabel(dateLabel);
                    l.setFont(font);
                    l.setBorder(underline);
                    p.add(l, gbc);
                    gbc.gridy++;
                    gbc.gridx=0;

                    currentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    currentPanel.setBackground(Color.WHITE);
                    p.add(currentPanel, gbc);


                    gbc.gridy++;
                }

                JButton b = new JButton(TIME_FORMAT.format(date));
                b.setMargin(insets);
                currentPanel.add(b);
            }
        }

        gbc.weighty = 1;
        p.add(new JLabel(""), gbc);


        this.add(p, BorderLayout.WEST);
        this.invalidate();
        this.revalidate();
        getParent().invalidate();
        getParent().repaint();
    }
}
