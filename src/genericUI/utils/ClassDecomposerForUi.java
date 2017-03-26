package genericUI.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;

import utils.Entity;
import component.AutoCompletion;
import component.CComboBox;
import component.CDateChooser;
import component.CTextField;

public class ClassDecomposerForUi {

	private static ClassDecomposerForUi instance;

	public static ClassDecomposerForUi getInstance() {
		if (instance == null)
			instance = new ClassDecomposerForUi();
		return instance;
	}

	private ClassDecomposerForUi() {
	}
	
	public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
	    fields.addAll(0,Arrays.asList(type.getDeclaredFields()));

	    if (type.getSuperclass() != null) {
	        fields = getAllFields(fields, type.getSuperclass());
	    }

	    return fields;
	}

	public static List<ClassVariable> getClassVariables(Class<? extends Entity> entityClass) {
		List<Field> allFields = new ArrayList<>();
		List<Field> declaredFields = getAllFields(allFields, entityClass);
		List<ClassVariable> classVariables = new ArrayList<>();
		for (Field field : declaredFields) {
			if (field.getName().equals("id"))
				continue;
			Method getMethod = null;
			Method setMethod = null;
			String name = field.getName();
			int columnOrder = 0;
			boolean mandatory = false;
			boolean timeIncluded = true;
			try {
				getMethod = entityClass.getMethod(getGetter(field));
				setMethod = entityClass.getMethod(getSetter(field), field.getType());
				if (getMethod.isAnnotationPresent(UiProperties.class)) {
					if (!getMethod.getAnnotation(UiProperties.class).showable())
						continue;
					name = getMethod.getAnnotation(UiProperties.class).name();
					columnOrder = getMethod.getAnnotation(UiProperties.class).columnOrder();
					mandatory = getMethod.getAnnotation(UiProperties.class).mandatory();
					timeIncluded = getMethod.getAnnotation(UiProperties.class).timeIncluded();
				}
			} catch (Exception e) {
				System.err.println("get Method is not found for the field " + field.getName());
				continue;
			}

			ClassVariable classVariable = getInstance().new ClassVariable();
			classVariable.setClassName(name);
			classVariable.setTimeIncluded(timeIncluded);
			classVariable.setClassType(field.getType());
			classVariable.setClassOrder(columnOrder);
			classVariable.setGetMethod(getMethod);
			classVariable.setSetMethod(setMethod);
			classVariable.setMandatory(mandatory);

			classVariables.add(classVariable);

		}

		Collections.sort(classVariables, new Comparator<ClassVariable>() {

			@Override
			public int compare(ClassVariable o1, ClassVariable o2) {
				return o1.getClassOrder() - o2.getClassOrder();
			}
		});
		return classVariables;
	}

	private static String getGetter(Field field) {
		return "get" + field.getName().substring(0, 1).toUpperCase(Locale.ENGLISH)
				+ field.getName().substring(1);
	}

	private static String getSetter(Field field) {
		return "set" + field.getName().substring(0, 1).toUpperCase(Locale.ENGLISH)
				+ field.getName().substring(1);
	}

	public class ClassVariable {

		private String className;
		private Class<?> classType;
		private int classOrder;
		private JComponent component;
		private Method getMethod;
		private Method setMethod;
		private boolean mandatory;
		private boolean timeIncluded;

		public void setValue(Object value) {
			if (Entity.class.isAssignableFrom(classType)) {
				CComboBox<?> comboBox = (CComboBox<?>) component;
				comboBox.setSelectedItem(value);
				System.out.println(value+ "here");
			} else if (Date.class.isAssignableFrom(classType)) {
				CDateChooser dateChooser = (CDateChooser) component;
				dateChooser.setDate((Date) value);
			} else {
				CTextField textField = (CTextField) component;
				textField.setValue(value);
			}
		}

		public boolean getTimeIncluded() {
			return timeIncluded;
		}

		public void setTimeIncluded(boolean timeIncluded) {
			this.timeIncluded = timeIncluded;
		}

		public boolean isMandatory() {
			return mandatory;
		}

		public void setMandatory(boolean mandatory) {
			this.mandatory = mandatory;
		}

		public Method getSetMethod() {
			return setMethod;
		}

		public void setSetMethod(Method setMethod) {
			this.setMethod = setMethod;
		}

		public Method getGetMethod() {
			return getMethod;
		}

		public void setGetMethod(Method getMethod) {
			this.getMethod = getMethod;
		}

		public JComponent getComponent() {
			return component;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public Class<?> getClassType() {
			return classType;
		}

		public void setClassType(Class<?> classType) {
			this.classType = classType;

			if (Entity.class.isAssignableFrom(classType)) {
				CComboBox<?> comboBox = new CComboBox<>(classType);
				comboBox.refresh();
				AutoCompletion.enable(comboBox);
				component = comboBox;
			} else if (Date.class.isAssignableFrom(classType)) {
				CDateChooser dateChooser = new CDateChooser(null, getTimeIncluded(), true);
				component = dateChooser;
			} else {
				CTextField textField = new CTextField();
				if (classType.isAssignableFrom(int.class)
						|| classType.isAssignableFrom(double.class)
						|| classType.isAssignableFrom(long.class)) {
					textField.setAsNumberTextField();
				}
				component = textField;
			}
		}

		public int getClassOrder() {
			return classOrder;
		}

		public void setClassOrder(int classOrder) {
			this.classOrder = classOrder;
		}

	}

}
