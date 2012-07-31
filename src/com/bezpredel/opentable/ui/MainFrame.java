package com.bezpredel.opentable.ui;

import com.bezpredel.opentable.Mailer;
import com.bezpredel.opentable.RequestSender;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;


public class MainFrame extends JFrame {
    private final TaskRunner taskRunner;
    private final JComboBox restaurants;
    private final ActiveTasksTableModel activeTasksTableModel;
    private final JTable currentlyWatchedTable;
    private final JComboBox times;
    private final JButton addButton;
    private final Logger logger;
    private final Defaults defaults;
    private final AvailabilityPanel availabilityPanel;

    private ActiveTask selectedTask;

    public MainFrame(RequestSender.RestaurantList list, TaskRunner taskRunner, Logger logger, Defaults defaults, AvailabilityPanel availabilityPanel) {
        super("OpenTable checker");
        this.taskRunner = taskRunner;
        this.defaults = defaults;
        this.logger = logger;
        this.availabilityPanel = availabilityPanel;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        restaurants = initRestaurantsList(list, defaults);
        times = initTimes(defaults);
        activeTasksTableModel = new ActiveTasksTableModel();
        currentlyWatchedTable = initActiveTable(activeTasksTableModel);
        addButton = initAddButton(restaurants, times, activeTasksTableModel);

        JPanel top = new JPanel(new BorderLayout());
        JPanel top_left = new JPanel(new BorderLayout());
        top_left.add(times, BorderLayout.WEST);
        top_left.add(addButton, BorderLayout.EAST);
        top.add(top_left, BorderLayout.EAST);
        top.add(restaurants, BorderLayout.CENTER);

        JPanel left = new JPanel(new BorderLayout());
        JSplitPane left_bottom = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(currentlyWatchedTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
                logger
        );
        left_bottom.setDividerLocation(200);

        left.add(top, BorderLayout.NORTH);
        left.add(left_bottom, BorderLayout.CENTER);

        JSplitPane split2 = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                left,
                new JScrollPane(this.availabilityPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        );

        this.getContentPane().add(split2);

        JMenuBar menuBar = initMenu();
        this.setJMenuBar(menuBar);


        this.setSize(800, 500);
    }

    private JMenuBar initMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem smtpSettingsItem = new JMenuItem("SMTP Settings");
        JMenuItem delaySettingsItem = new JMenuItem("Delay Settings");

        smtpSettingsItem.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showSMTPSettings();
                }
            }
        );

        delaySettingsItem.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showDelaySettings();
                }
            }
        );
        fileMenu.add(smtpSettingsItem);
        fileMenu.add(delaySettingsItem);
        menuBar.add(fileMenu);
        return menuBar;
    }

    private void showDelaySettings() {
        int delay = DelaySettingsDialog.getDelayFromSettingsDialog(this, defaults);
        taskRunner.setDelay(delay);
    }

    public void showSMTPSettings() {
        Mailer mailer = SMTPSettingsDialog.getMailerFromSettingsDialog(this, defaults);
        if(mailer!=null) {
            taskRunner.setMailer(mailer);
        }
    }

    private JComboBox initTimes(final Defaults defaults) {
        final JComboBox b = new JComboBox();
        Integer defaultTime = defaults.getDefaultTime();
        for (int i = 0; i < 24; i++) {
            String l = (i < 10 ? "0" : "") + i + ":00";
            b.addItem(l);
        }
        b.setSelectedIndex(defaultTime);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String s = b.getSelectedItem().toString();
                    int i = Integer.parseInt(s.substring(0, s.indexOf(":")));
                    defaults.setDefaultTime(i);
                } catch (NumberFormatException ee) {
                    // do nothing
                }
            }
        });
        return b;
    }

    private JButton initAddButton(final JComboBox restaurants, final JComboBox times, final ActiveTasksTableModel model) {
        JButton b = new JButton("Add");
        b.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Restaurant item = (Restaurant) restaurants.getSelectedItem();
                        String timeStr = (String) times.getSelectedItem();
                        int time = Integer.parseInt(timeStr.substring(0, timeStr.indexOf(":")));
                        if (item != null) {
                            ActiveTask addedItem = model.addActiveTask(item, time);
                            taskRunner.runTask(addedItem);

                            addedItem.addObserver(
                                    new Observer() {
                                        @Override
                                        public void update(Observable o, Object arg) {
                                            updateAvailabilityPanel(o);
                                        }
                                    }
                            );
                        }
                    }
                }
        );
        return b;
    }

    private JTable initActiveTable(final ActiveTasksTableModel model) {
        final JTable table = new JTable(model);
        TableColumnModel columnModel = table.getColumnModel();
        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            private final Color SELECTED_GREEN = new Color(0, 150, 0);
            private final Color UNSELECTED_GREEN = new Color(200, 255, 200);

            private final Color SELECTED_RED = new Color(0, 150, 0);
            private final Color UNSELECTED_RED = new Color(255, 200, 200);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                ActiveTask task = model.getTasks().get(row);
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (task.isDead()) {
                    c.setBackground(isSelected ? SELECTED_RED : UNSELECTED_RED);
                } else if (task.hasAny()) {
                    c.setBackground(isSelected ? SELECTED_GREEN : UNSELECTED_GREEN);
                } else {
                    c.setBackground(isSelected ? Color.BLUE : Color.WHITE);
                }

                return c;
            }
        });

        columnModel.getColumn(3).setCellEditor(
                new ButtonCellEditor(
                        model,
                        new ButtonCellEditor.Listener() {
                            @Override
                            public void buttonPressed(ActiveTask task) {
                                taskRunner.runTask(task);
                            }
                        }
                )
        );
        columnModel.getColumn(3).setCellRenderer(new ButtonCellEditor(model, null));

        columnModel.getColumn(4).setCellEditor(
                new ButtonCellEditor(
                        model,
                        new ButtonCellEditor.Listener() {
                            @Override
                            public void buttonPressed(ActiveTask task) {
                                taskRunner.cancelTask(task);
                                model.removeTask(task);
                            }
                        }
                )
        );
        columnModel.getColumn(4).setCellRenderer(new ButtonCellEditor(model, null));

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        columnModel.getColumn(1).setMaxWidth(48);
        columnModel.getColumn(2).setMaxWidth(64);
        columnModel.getColumn(3).setMaxWidth(80);
        columnModel.getColumn(4).setMaxWidth(24);


        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        if(!e.getValueIsAdjusting()) {
                            itemSelected(table.getSelectionModel().getMinSelectionIndex());
                        }
                    }
                }
        );

        return table;
    }


    private void itemSelected(int index) {
        if(index == -1) {
            selectedTask = null;
        } else {
            selectedTask = activeTasksTableModel.getTasks().get(index);
        }

        updateAvailabilityPanel(selectedTask);
    }

    private void updateAvailabilityPanel(Object source) {
        if(source==selectedTask) {
            if(selectedTask==null) {
                availabilityPanel.updateAvailability(Collections.<Date>emptyList());
            } else {
                availabilityPanel.updateAvailability(selectedTask.getLastResult());
            }
        }
    }

    private JComboBox initRestaurantsList(RequestSender.RestaurantList list, final Defaults defaults) {
        final JComboBox c = new JComboBox();
        Integer def = defaults.getDefaultRestaurant();

        for (Map.Entry<String, Integer> entry : list.getAll().entrySet()) {
            c.addItem(new Restaurant(entry.getKey(), entry.getValue()));

            if (def != null && def.equals(entry.getValue())) {
                c.setSelectedIndex(c.getModel().getSize() - 1);
            }
        }

        c.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Restaurant r = (Restaurant) c.getSelectedItem();
                if (r != null) {
                    defaults.setDefaultRestaurant(r.getId());
                }
            }
        });
        return c;
    }
}
