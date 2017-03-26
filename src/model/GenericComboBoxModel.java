package model;

import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

import utils.Entity;

public class GenericComboBoxModel<E> extends AbstractListModel<E> implements MutableComboBoxModel<E>{
	
	private static final long serialVersionUID = 1L;
	private List<E> objects = new LinkedList<>();
	private Object selectedItem;

	public GenericComboBoxModel() {
	}

	public void setObjects(List<E> objects){
		this.objects = objects;
		fireContentsChanged();
	}
	

	@Override
	public int getSize() {
		return objects.size();
	}

	@Override
	public E getElementAt(int index) {
		return objects.get(index);
	}

	@Override
	public void setSelectedItem(Object anItem) {
		selectedItem = anItem;
		fireContentsChanged();
	}

	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}

	@Override
	public void addElement(E item) {
		objects.add(item);
		fireContentsChanged();
	}

	private void fireContentsChanged() {
		fireContentsChanged(this, 0, getSize());
	}

	@Override
	public void removeElement(Object obj) {
		objects.remove(obj);
		fireContentsChanged();
	}

	@Override
	public void insertElementAt(E item, int index) {
		objects.add(index, item);
		fireContentsChanged();
	}

	@Override
	public void removeElementAt(int index) {
		objects.remove(index);
		fireContentsChanged();
	}

	@SuppressWarnings("unchecked")
	public void save(Entity entity) {
		addElement((E) entity);
	}

	@SuppressWarnings("unchecked")
	public void update(Entity entity) {
		List<Entity> entities = (List<Entity>) objects;
		for (int i = 0; i < entities.size(); i++) {
			Entity e =  entities.get(i);
			if(e.getId() == entity.getId()){
				entities.set(i, entity) ;
				break;
			}
		}
		fireContentsChanged();
	}
	

	@SuppressWarnings("unchecked")
	public void delete(Entity entity) {
		List<Entity> entities = (List<Entity>) objects;
		for (int i = 0; i < entities.size(); i++) {
			Entity e =  entities.get(i);
			if(e.getId() == entity.getId()){
				entities.remove(i) ;
				break;
			}
		}
		fireContentsChanged();
		setSelectedItem(null);
	}
}
