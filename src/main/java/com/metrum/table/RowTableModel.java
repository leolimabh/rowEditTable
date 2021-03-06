/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metrum.table;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

/**
 * Classe que implementa um modelo para tabela genérico.
 *
 * @author leandro.lima
 * @param <T>
 */
public class RowTableModel<T extends RowModel> extends AbstractTableModel {

    private final LinkedList<ColumnContext> columns;
    private LinkedList<T> model = new LinkedList<>();
    private final Class<T> rowType;

    public RowTableModel(Class<T> rowType, List<ColumnContext> columns) {
        this.columns = new LinkedList<>(columns);
        this.rowType = rowType;
    }

    public List<T> getModel() {
        return model;
    }

    public void setModel(List<T> model) {
        this.model = new LinkedList<>(model);
        fireTableChanged(new TableModelEvent(this));
    }

    public void clear() {
        this.model.clear();
        fireTableChanged(new TableModelEvent(this));
    }

    @Override
    public int getRowCount() {
        return model.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columns.get(columnIndex).isAutoIncremented())
            return rowIndex + 1;
        else
            return model.get(rowIndex).getValueAt(columnIndex);
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns.get(columnIndex).getName();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columns.get(columnIndex).getType();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columns.get(columnIndex).isEditable();
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        final T row = model.get(rowIndex);
        row.setValueAt(columnIndex, value);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void removeRows(int firstRowIndex, int lastRowIndex) {
        if (firstRowIndex > lastRowIndex)
            throw new IllegalArgumentException("LastRowIndex " + lastRowIndex
                    + " must be greater or equal to " + firstRowIndex);

        if (model.size() <= lastRowIndex)
            throw new StackOverflowError("The lastRowIndex " + lastRowIndex
                    + " must be less than row count " + model.size());

        for (int i = lastRowIndex; i >= firstRowIndex; i--)
            model.remove(i);
        fireTableRowsDeleted(firstRowIndex, lastRowIndex);
    }

    public List<T> getRows(int firstRowIndex, int lastRowIndex) {
        if (firstRowIndex > lastRowIndex)
            throw new IllegalArgumentException("LastRowIndex " + lastRowIndex
                    + " must be greater or equal to " + firstRowIndex);
        if (model.size() < lastRowIndex)
            throw new StackOverflowError("The lastRowIndex " + lastRowIndex
                    + " must be less or equal to row count " + model.size());

        final LinkedList<T> rows = new LinkedList<>();
        for (T element : model.subList(firstRowIndex, lastRowIndex))
            rows.add(element);

        return rows;
    }

    public T getRow(int rowIndex) {
        if (model.size() < rowIndex)
            throw new StackOverflowError("The lastRowIndex " + rowIndex
                    + " must be less or equal to row count " + model.size());

        return model.get(rowIndex);
    }

    public void addRow() {
        addRow(model.size());
    }

    private void addRow(int rowIndex) {
        try {
            addRow(rowIndex, rowType.newInstance());
        } catch (IllegalAccessException | InstantiationException ex) {

        }
    }

    private void addRow(int rowIndex, T row) {
        model.add(rowIndex, row);
        fireTableRowsInserted(rowIndex, rowIndex);
    }

    public void insertRowsAt(int rowIndex, int quantity) {
        if (rowIndex > model.size())
            throw new StackOverflowError("The rowIndex " + rowIndex
                    + " must be less or equal to row count " + model.size());

        for (int i = 0; i < quantity; i++)
            addRow(i + rowIndex);
    }

    public void insertRowsAt(int rowIndex, List<T> rows) {
        if (rowIndex > model.size())
            throw new StackOverflowError("The rowIndex " + rowIndex
                    + " must be less or equal to row count " + model.size());

        for (int i = 0; i < rows.size(); i++)
            addRow(rowIndex + i, rows.get(i));
    }

    public void insertRowsAt(int rowIndex, String rows) {
        StringTokenizer rowTokenizer = new StringTokenizer(rows, "\n");
        final LinkedList<T> list = new LinkedList<>();
        while (rowTokenizer.hasMoreTokens())
            try {
                T element = (T) rowType.newInstance().fromString(rowTokenizer.nextToken());
                list.add(element);
            } catch (IllegalAccessException | InstantiationException ex) {

            }

        insertRowsAt(rowIndex, list);
    }
    public void update(){
        fireTableChanged(new TableModelEvent(this));
    }

}
