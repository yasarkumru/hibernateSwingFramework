package component;

import hibernate.HibernateManager;

import java.util.List;

import javax.swing.JComboBox;

import utils.Entity;
import utils.TableUpdater;
import model.GenericComboBoxModel;

public class CComboBox<E> extends JComboBox<E> implements Updateable {

	private static final long serialVersionUID = 1L;
	private Class<?> clazz;
	private Entity entity;
	private String columnName;
	private GenericComboBoxModel<E> model;

	public CComboBox(Class<?> clazz) {
		model = new GenericComboBoxModel<>();
		this.setModel(model);
		this.clazz = clazz;
		if (clazz != null)
			TableUpdater.getInstance().addTable(clazz, this);
		AutoCompletion.enable(this);
	}

	public void setEntityAndColumnName(Entity entity, String columnName) {
		this.entity = entity;
		this.columnName = columnName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setObjects(List<?> objects) {
		model.setObjects((List<E>) objects);

	}

	@Override
	public boolean isEntityComponent() {
		return entity != null;
	}

	public Object getSelectedObject() {
		Object selectedItem = getSelectedItem();
		return selectedItem;
	}

	@Override
	public void refresh() {
		System.out.print("COMBOBOX:" + clazz.getSimpleName() + ">>");
		if (isEntityComponent()) {
			List<?> entitysList = HibernateManager.getInstance().getEntitysList(entity, columnName);
			setObjects(entitysList);

		} else {
			List<?> list = HibernateManager.getInstance().get(clazz);
			setObjects(list);

		}

	}

	@Override
	public void refresh(List<?> objects) {
		System.out.print("COMBOBOX:" + clazz.getSimpleName() + ">>");
		setObjects(objects);
	}

	@Override
	public void save(Entity entity) {
		model.save(entity);
	}

	@Override
	public void update(Entity entity) {
		model.update(entity);
	}

	@Override
	public void delete(Entity entity) {
		model.delete(entity);
	}

	/**
	 * combobox'a batch update yapılmayacaktır.
	 */
	@Override
	@Deprecated
	public void saveAll(List<Entity> objects) {
		for (Object object : objects) {
			save((Entity) object);
		}
	}

	/**
	 * combobox'a batch update yapılmayacaktır.
	 */
	@Override
	@Deprecated
	public void updateAll(List<Entity> objects) {
		for (Entity entity : objects) {
			model.update(entity);
		}
	}

	@Override
	public Object getSelectedItem() {
		Object selectedItem = super.getSelectedItem();
		return selectedItem;
	}
}
