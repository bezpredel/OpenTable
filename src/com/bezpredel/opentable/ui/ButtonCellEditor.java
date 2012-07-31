package com.bezpredel.opentable.ui;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
* Date: 7/30/12
* Time: 7:00 PM
*/
public class ButtonCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, ActionListener {
    private final JButton delegate;
    private final ActiveTasksTableModel model;
    private final Listener listener;
    private ActiveTask currentTask;

    ButtonCellEditor(ActiveTasksTableModel model, Listener listener) {
        this.model = model;
        this.listener = listener;
        delegate = new JButton();
        delegate.setMargin(new Insets(0, 0, 0, 0));
        delegate.addActionListener(this);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        ActiveTask task = model.getTasks().get(row);
        delegate.setText(value.toString());
        currentTask = task;
        return delegate;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        delegate.setText(value.toString());
        return delegate;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        fireEditingCanceled();
        listener.buttonPressed(currentTask);
    }

    public interface Listener {
        void buttonPressed(ActiveTask task);
    }
}
