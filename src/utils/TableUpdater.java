package utils;

import hibernate.HibernateManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import component.Updateable;

public class TableUpdater {

	private static TableUpdater instance;
	private Map<Class<?>, List<Updateable>> updateables = new HashMap<>();
	private Set<Updateable> laterUpdates;

	private TableUpdater() {
	}

	public static TableUpdater getInstance() {
		if (instance == null)
			instance = new TableUpdater();
		return instance;
	}

	public void addTable(Class<?> clazz, Updateable component) {
		boolean containsKey = updateables.containsKey(clazz);
		if (containsKey) {
			List<Updateable> list = updateables.get(clazz);
			addComponent(list, component);
			return;
		}
		updateables.put(clazz, new LinkedList<Updateable>());
		addTable(clazz, component);
	}

	private static void addComponent(List<Updateable> list, Updateable component) {
		for (Updateable updateable : list) {
			if (updateable == component)
				return;
		}
		list.add(component);
	}

	public void removeTable(Class<?> clazz, Updateable component) {
		List<Updateable> list = updateables.get(clazz);
		list.remove(component);
	}

	public void changeTablesClass(Class<?> from, Class<?> to, Updateable table) {
		List<Updateable> list = updateables.get(from);
		list.remove(table);
		addTable(to, table);
	}

	public void refreshLaterUpdates() {
		laterUpdates = new HashSet<>();
	}

	public void updateLaterUpdates() {
		System.out.println(laterUpdates);
		for (Updateable updateable : laterUpdates) {
			updateable.refresh();
		}
	}

	public void save(final Entity entity) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				List<Updateable> components = updateables.get(entity.getClass());
				if (components == null)
					return;

				for (Updateable component : components) {
					if (component.isEntityComponent()) {
						if (HibernateManager.getInstance().isPermanentSession()) {
							laterUpdates.add(component);
							continue;
						}
						component.refresh();
						continue;
					}
					component.save(entity);
				}
			}
		});
	}

	public void update(final Entity entity) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				List<Updateable> components = updateables.get(entity.getClass());
				if (components == null)
					return;

				for (Updateable component : components) {
					if (component.isEntityComponent()) {
						if (HibernateManager.getInstance().isPermanentSession()) {
							laterUpdates.add(component);
							continue;
						}
						component.refresh();
						continue;
					}
					component.update(entity);
				}
			}
		});
	}

	public void delete(final Entity entity) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				List<Updateable> components = updateables.get(entity.getClass());
				if (components == null)
					return;

				for (Updateable component : components) {
					if (component.isEntityComponent()) {
						if (HibernateManager.getInstance().isPermanentSession()) {
							laterUpdates.add(component);
							continue;
						}
						component.refresh();
						continue;
					}
					component.delete(entity);
				}
			}
		});

	}

	public void saveAll(final List<Entity> objects) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (objects == null || objects.size() == 0)
					return;
				List<Updateable> components = updateables.get(objects.get(0)
						.getClass());
				if (components == null)
					return;

				for (Updateable component : components) {
					if (component.isEntityComponent()) {
						if (HibernateManager.getInstance().isPermanentSession()) {
							laterUpdates.add(component);
							continue;
						}
						component.refresh();
						continue;
					}
					component.saveAll(objects);
				}
			}
		});
	}

	public void updateTable(Class<?> clazz) {
		List<Updateable> components = updateables.get(clazz);
		if (components == null)
			return;

		for (Updateable component : components) {
			if (component == null)
				continue;
			component.refresh();
		}
	}

	public void updateAll(final List<Entity> objects) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (objects == null || objects.size() == 0)
					return;
				List<Updateable> components = updateables.get(objects.get(0)
						.getClass());
				if (components == null)
					return;

				for (Updateable component : components) {
					if (component.isEntityComponent()) {
						if (HibernateManager.getInstance().isPermanentSession()) {
							laterUpdates.add(component);
							continue;
						}
						component.refresh();
						continue;
					}
					component.updateAll(objects);
				}
			}
		});
	}

}
