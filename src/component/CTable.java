package component;

import hibernate.HibernateManager;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;

import utils.Entity;
import utils.TableUpdater;
import model.GenericTableCellRenderer;
import model.GenericTableModel;

public class CTable extends JTable implements Updateable {

	private static final long serialVersionUID = 1L;
	private Class<?> clazz;

	private GenericTableModel tableModel;
	private Entity entity;
	private String columnName;
	private TablePanel tablePanel;
	private boolean updateable;
	private Comparator<Object> comparator;

	public CTable(String title, Class<?> clazz) {
		this(title, clazz, true, true);
	}

	public CTable(String title, Class<?> clazz, boolean updatable) {
		this(title, clazz, updatable, true);
	}

	/**
	 * @param title
	 */
	public CTable(String title, Class<?> clazz, boolean updateable, boolean autoResizableColumns) {
		this.updateable = updateable;
		this.clazz = clazz;
		if (updateable)
			TableUpdater.getInstance().addTable(clazz, this);
		initTable();
		if (autoResizableColumns)
			initAutoResizable();
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setComparator(Comparator<Object> comparator) {
		this.comparator = comparator;
	}

	private void initAutoResizable() {
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				resizeColumnWidth();
			}
		});
	}

	public void resizeColumnWidth() {
		TableColumnModel columnModel = getColumnModel();
		int rowCount = getRowCount();
		for (int column = 0; column < getColumnCount(); column++) {
			int width = 50; // Min width
			for (int row = 0; row < rowCount; row++) {
				int rowConverted = convertRowIndexToView(row);
				TableCellRenderer renderer = getCellRenderer(rowConverted, column);
				Component comp = prepareRenderer(renderer, rowConverted, column);
				width = Math.max(comp.getPreferredSize().width + 10, width);
			}

			Object headerValue = getColumnModel().getColumn(column).getHeaderValue();
			Component tableCellRendererComponent = getTableHeader().getDefaultRenderer()
					.getTableCellRendererComponent(this, headerValue, false, false, -1, column);

			int columnHeaderWidth = tableCellRendererComponent.getPreferredSize().width + 10;

			width = Math.max(width, columnHeaderWidth);
			columnModel.getColumn(column).setPreferredWidth(width);
		}
	}

	public void setUpdateable(boolean updateable) {
		this.updateable = updateable;
		if (updateable)
			TableUpdater.getInstance().addTable(clazz, this);
		else {
			TableUpdater.getInstance().removeTable(clazz, this);
		}
	}

	public List<?> getObjects() {
		return tableModel.getObjects();
	}

	public void setEntityAndColumnName(Entity entity, String columnName) {
		this.entity = entity;
		this.columnName = columnName;
	}

	public void setClazz(Class<?> clazz) {
		Class<?> old = this.clazz;
		this.clazz = clazz;
		setEntityAndColumnName(null, null);
		if (updateable)
			TableUpdater.getInstance().changeTablesClass(old, clazz, this);
		GenericTableModel model = (GenericTableModel) getModel();
		model.setClazz(clazz);
	}

	private void initTable() {
		tableModel = new GenericTableModel(clazz);
		setModel(tableModel);
		TableColumnModel columnModel = getColumnModel();
		int columnCount = columnModel.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			columnModel.getColumn(i).setCellRenderer(new GenericTableCellRenderer());
		}
	}

	public Object getEditedObject(TableModelEvent evt) {
		return tableModel.getEditedObject(evt);
	}

	public Object getLastEditedValue() {
		return tableModel.getLastEditedValue();
	}

	public Object getPostEditValue() {
		return tableModel.getPostEditValue();
	}

	@Override
	public boolean isEntityComponent() {
		return !(entity == null && columnName == null);
	}

	@Override
	public Component prepareEditor(TableCellEditor editor, int row, int column) {
		Component c = super.prepareEditor(editor, row, column);
		if (c instanceof JTextComponent) {
			((JTextComponent) c).selectAll();
		}
		return c;
	}

	@Override
	public void refresh() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (tablePanel != null)
					tablePanel.showLoading(true);

				System.out.print("TABLE:" + clazz.getSimpleName() + " >> ");
				checkout: {
					if (isEntityComponent()) {
						if (entity == null) {
							setObjects(new LinkedList<>());
							break checkout;
						}
						List<?> entitysList = HibernateManager.getInstance().getEntitysList(entity,
								columnName);
						setObjects(entitysList);
					} else {

						List<?> list = HibernateManager.getInstance().get(clazz);
						setObjects(list);
					}
				}
				clearSelection();

				if (tablePanel != null)
					tablePanel.showLoading(false);
			}
		});
	}

	@Override
	public int getRowCount() {
		int min = Math.min(super.getRowCount(), tableModel.getRowCount());
		return min;
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		Component prepareRenderer = super.prepareRenderer(renderer, row, column);
		if (tableModel.getTooltips().get(column)) {
			JLabel label = (JLabel) prepareRenderer;
			label.setToolTipText(label.getText());
		}
		return prepareRenderer;
	}

	@Override
	public void refresh(List<?> objects) {
		System.out.print("TABLE:" + clazz.getSimpleName() + " >> ");
		setObjects(objects);
	}

	public Object getSelectedObject() {
		if (getSelectedRows().length > 1)
			return null;
		int selectedRow = getSelectedRow();
		if (selectedRow < 0) {
			return null;
		}
		int rowIndex = convertRowIndexToModel(selectedRow);
		return tableModel.getValueAt(rowIndex, GenericTableModel.OBJECT_COLUMN);
	}

	public List<Object> getSelectedObjects() {
		int[] selectedRows = getSelectedRows();
		List<Object> selectedObjects = new ArrayList<>(selectedRows.length);
		for (int i : selectedRows) {
			int rowIndex = convertRowIndexToModel(i);
			Object o = tableModel.getValueAt(rowIndex, GenericTableModel.OBJECT_COLUMN);
			selectedObjects.add(o);
		}
		return selectedObjects;
	}

	@Override
	public void setObjects(List<?> objects) {
		if (comparator != null) {
			Collections.sort(objects, comparator);
		}
		tableModel.setObjects(objects);
	}

	@Override
	public void save(Entity entity) {
		tableModel.save(entity);
	}

	@Override
	public void update(Entity entity) {
		tableModel.update(entity);
	}

	@Override
	public void delete(Entity entity) {
		tableModel.delete(entity);
	}

	@Override
	public void saveAll(List<Entity> objects) {
		tableModel.saveAll(objects);
	}

	protected void setTablePanel(TablePanel tablePanel) {
		this.tablePanel = tablePanel;
	}

	@Override
	public void updateAll(List<Entity> objects) {
		tableModel.updateAll(objects);
	}
}
