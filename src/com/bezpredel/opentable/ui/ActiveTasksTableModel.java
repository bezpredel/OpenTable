package com.bezpredel.opentable.ui;

import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;

/**
* Date: 7/30/12
* Time: 7:00 PM
*/
public class ActiveTasksTableModel extends AbstractTableModel implements Observer {
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat timeFormatGMT = new SimpleDateFormat("HH:mm:ss");
    {
        timeFormatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    private final List<ActiveTask> tasks = new ArrayList<ActiveTask>();
    private static final String[] columnNames = new String[]{"Name", "Time", "Last Check", "Next Check", ""};
    private final javax.swing.Timer timer;

    ActiveTasksTableModel() {
        this.timer = new javax.swing.Timer(
                500,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!tasks.isEmpty()) {
                            fireTableRowsUpdated(0, tasks.size() - 1);
                        }
                    }
                }
        );
        timer.start();
    }


    public List<ActiveTask> getTasks() {
        return tasks;
    }

    public ActiveTask addActiveTask(Restaurant restaurant, int hour) {
        ActiveTask task = new ActiveTask(restaurant, hour);
        if(tasks.contains(task)) {
            return null;
        }
        tasks.add(task);
        fireTableRowsInserted(tasks.size() - 1, tasks.size() - 1);
        task.addObserver(this);
        return task;
    }

    public void removeTask(ActiveTask task) {
        int ind = tasks.indexOf(task);
        if(ind >= 0) {
            tasks.remove(ind);
            fireTableRowsDeleted(ind, ind);
        }
    }

    @Override
    public int getRowCount() {
        return tasks.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex==3 || columnIndex==4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ActiveTask task = tasks.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return task.restaurant.getName();
            case 1:
                return (task.hour < 10 ? "0" : "") + task.hour + ":00";
            case 2:
                Date lastCheckTime = task.getLastCheckTime();
                return lastCheckTime==null ? "" : timeFormat.format(lastCheckTime);
            case 3:
                if(task.isDead()) {
                    return "DEAD";
                }
                Date nextCheckTime = task.getNextCheckTime();
                if(nextCheckTime ==null) {
                    return "N/A";
                } else {
                    long t = nextCheckTime.getTime() - System.currentTimeMillis();
                    if(t <= 0) {
                        return "(now)";
                    } else {
                        return timeFormatGMT.format(new Date(t));
                    }
                }
            case 4:
                return "X";
        }
        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        ActiveTask task = (ActiveTask) o;
        int ind = tasks.indexOf(task);
        if(ind >= 0) {
            fireTableRowsUpdated(ind, ind);
        }
    }
}
