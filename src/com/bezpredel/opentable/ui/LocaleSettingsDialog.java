package com.bezpredel.opentable.ui;

import com.bezpredel.opentable.RequestSender;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LocaleSettingsDialog extends JPanel {
    private final JList regions;
    private final JList locales;
    private final RequestSender requestSender;
    private String regionName;

    private LocaleSettingsDialog(Defaults defaults, RequestSender requestSender) {
        super(new BorderLayout());
        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEtchedBorder(BevelBorder.LOWERED),
                        BorderFactory.createEmptyBorder(2,2,2,2)
                )
        );

        this.requestSender = requestSender;

        ListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) value;
                return super.getListCellRendererComponent(list, entry.getKey(), index, isSelected, cellHasFocus);
            }
        };

        regions = new JList(new DefaultListModel());
        regions.setCellRenderer( renderer );
        regions.setPrototypeCellValue(Collections.singletonMap("state can be this wide", null).entrySet().iterator().next());
        regions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        locales = new JList(new DefaultListModel());
        locales.setCellRenderer( renderer );
        locales.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(new JLabel("Regions"), BorderLayout.NORTH);
        p1.add(new JScrollPane(regions, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(new JLabel("Locales"), BorderLayout.NORTH);
        p2.add(new JScrollPane(locales, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        add(p1, BorderLayout.WEST);
        add(p2, BorderLayout.EAST);

        regions.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        if(regions.getSelectedIndex() >= 0) {
                            Map.Entry<String, String> entry = (Map.Entry<String, String>) regions.getSelectedValue();
                            requestLocales(entry.getKey(), entry.getValue());
                        } else {
                            regionName = null;
                        }
                    }
                }
        );

        requestRegions(requestSender);
    }

    private void requestLocales(final String name, final String url) {
        locales.setListData(new Vector());

        SwingWorker<Map<String, String>, Object> worker = new SwingWorker<Map<String, String>, Object>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                return requestSender.getAreasForLocale(name, url);
            }
        };

        worker.execute();


        try {
            Map<String, String> values = worker.get();
            regionName = name;
            locales.setListData(values.entrySet().toArray(new Object[values.size()]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestRegions(final RequestSender requestSender) {
        SwingWorker<Map<String, String>, Object> worker = new SwingWorker<Map<String, String>, Object>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                return requestSender.requestLocales();
            }
        };
        worker.execute();

        try {
            Map<String, String> values = worker.get();
            regions.setListData(values.entrySet().toArray(new Object[values.size()]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAnythingSelected() {
        return regionName!=null && locales.getSelectedIndex() >= 0;
    }

    private String getLocaleName() {
        return ((Map.Entry<String, String>)locales.getSelectedValue()).getKey() + ", " + regionName;
    }

    private String getLocaleURL() {
        return ((Map.Entry<String, String>)locales.getSelectedValue()).getValue();
    }


    public static boolean showDialog(Component parentComponent, Defaults defaults, RequestSender requestSender) {
        LocaleSettingsDialog panel = new LocaleSettingsDialog(defaults, requestSender);

        JOptionPane pane = new JOptionPane(
                panel,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                UIManager.getIcon("OptionPane.questionIcon"),
                new String[]{"Save", "Cancel"},
                "Select Locale"
        );

        JDialog dialog = pane.createDialog(parentComponent, "Select Locale");
        dialog.setName("Select Locale");
        dialog.setVisible(true);
        Object o = pane.getValue();
        if (o!=null && o.equals("Save")) {
            if(panel.isAnythingSelected()) {
                defaults.setLocale(
                        panel.getLocaleName(),
                        panel.getLocaleURL()
                );
                return true;
            }
        }
        return false;
    }
}
