package model;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import utils.Entity;

public class GenericTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	public static final int OBJECT_COLUMN = -1;
	private List<?> objects;
	private List<String> columnNames;
	private List<Class<?>> columnTypes;
	private List<Method> methods;
	private List<Boolean> editables;
	private List<Boolean> tooltips;
	private Class<?> clazz;
	private Object lastEditedValue;
	private Object postEditValue;

	public GenericTableModel(Class<?> clazz) {
		this.clazz = clazz;
		init();
	}

	public List<?> getObjects() {
		return objects;
	}

	private void refreshLists() {
		objects = new ArrayList<>();
		columnNames = new ArrayList<>();
		columnTypes = new ArrayList<>();
		methods = new ArrayList<>();
		editables = new ArrayList<>();
		tooltips = new ArrayList<>();
	}

	public List<Boolean> getTooltips() {
		return tooltips;
	}

	public Object getLastEditedValue() {
		return lastEditedValue;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
		init();
	}

	@Override
	public int getRowCount() {
		return objects.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.size();
	}

	@Override
	public String getColumnName(int column) {
		return columnNames.get(column);
	}

	@Override
	public synchronized Object getValueAt(int rowIndex, int columnIndex) {

		Object value = objects.get(rowIndex);
		if (columnIndex == OBJECT_COLUMN)
			return value;

		try {
			value = methods.get(columnIndex).invoke(value);
		} catch (Exception e) {
			e.printStackTrace();
			return value;
		}

		return value;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		String columnType = columnTypes.get(columnIndex).getName();
		switch (columnType) {
		case "double":
			return Double.class;
		case "int":
			return Integer.class;
		case "boolean":
			return Boolean.class;
		case "char":
			return Character.class;
		default:
			return columnTypes.get(columnIndex);
		}
	}

	public void setObjects(List<?> objects) {
		this.objects = objects;
		fireTableDataChanged();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return editables.get(columnIndex);
	}
	
	public Object getPostEditValue() {
		return postEditValue;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Object object = objects.get(rowIndex);
		Method getMethod = methods.get(columnIndex);
		try {
			lastEditedValue = getMethod.invoke(object);
			postEditValue = aValue;
			Method setMethod = clazz.getMethod("set"
					+ getMethod.getName().substring(3),
					columnTypes.get(columnIndex));
			setMethod.invoke(object, aValue);
		} catch (NoSuchMethodException e) {
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	public Object getEditedObject(TableModelEvent evt) {
		if (evt.getFirstRow() != evt.getLastRow())
			return null;
		return objects.get(evt.getFirstRow());
	}

	public void init() {
		refreshLists();
		initForClass(clazz);
		fireTableStructureChanged();
	}

	private void initForClass(Class<?> clazz) {
		Method[] methodz = clazz.getMethods();
		List<Method> sortedMethods = sortMethods(methodz);

		for (Method method : sortedMethods) {
			AnnotatedElement annotatedMethod = method;
			if (method.getReturnType().equals(Void.TYPE))
				continue;
			if (method.getReturnType().equals(List.class))
				continue;
			if (checkForObjectMethod(method))
				continue;
			String columnName;
			boolean editable = false;
			boolean toolTipText = false;
			if (method.isAnnotationPresent(TableProperties.class)) {
				if (!annotatedMethod.getAnnotation(TableProperties.class)
						.showable())
					continue;

				editable = method.getAnnotation(TableProperties.class)
						.editable();
				toolTipText = method.getAnnotation(TableProperties.class)
						.showTooltip();
				String annotationName = method.getAnnotation(
						TableProperties.class).name();
				columnName = annotationName.equals("") ? method.getName()
						.replaceAll("get", "") : annotationName;
			} else {
				columnName = method.getName().replaceAll("get", "");
			}
			editables.add(editable);
			columnNames.add(columnName);
			columnTypes.add(method.getReturnType());
			methods.add(method);
			tooltips.add(toolTipText);

		}
	}

	private List<Method> sortMethods(Method[] unsortedMethods) {
		List<Method> ms = new ArrayList<>();
		for (Method method : unsortedMethods) {
			ms.add(method);
		}
		Collections.sort(ms, new ColumnComparator());
		return ms;
	}

	private static boolean checkForObjectMethod(Method method) {
		switch (method.getName()) {
		case "equals":
		case "toString":
		case "hashCode":
		case "getClass":
			return true;
		}
		return false;

	}

	private class ColumnComparator implements Comparator<Method> {

		@Override
		public int compare(Method arg0, Method arg1) {
			AnnotatedElement m1 = arg0;
			AnnotatedElement m2 = arg1;
			boolean m1Present = m1.isAnnotationPresent(TableProperties.class);
			boolean m2Present = m2.isAnnotationPresent(TableProperties.class);
			if (m1Present && m2Present) {
				return m1.getAnnotation(TableProperties.class).columnOrder()
						- m2.getAnnotation(TableProperties.class).columnOrder();
			}

			if (m1Present)
				return 1;
			if (m2Present)
				return -1;

			return 0;
		}

	}

	@SuppressWarnings("unchecked")
	public void save(Entity entity) {
		List<Entity> obs = (List<Entity>) objects;
		obs.add(entity);
		fireTableDataChanged();
	}

	@SuppressWarnings("unchecked")
	public void update(Entity entity) {
		List<Entity> entities = (List<Entity>) objects;
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if (e.getId() == entity.getId()) {
				entities.set(i, entity);
				break;
			}
		}
		fireTableDataChanged();
	}

	public void delete(Entity entity) {
		objects.remove(entity);
		fireTableDataChanged();
	}

	@SuppressWarnings("unchecked")
	public void saveAll(List<Entity> objects2) {
		List<Entity> obs = (List<Entity>) objects;
		obs.addAll(objects2);
		fireTableDataChanged();
	}

	public void updateAll(List<Entity> objects2) {
		for (Entity entity : objects2) {
			update(entity);
		}
	}

}
