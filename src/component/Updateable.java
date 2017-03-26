package component;

import java.util.List;

import utils.Entity;

public interface Updateable {
	
	public void refresh();
	public void refresh(List<?> objects);
	public void setObjects(List<?> objects);
	public boolean isEntityComponent();
	public void save(Entity entity);
	public void update(Entity entity);
	public void delete(Entity entity);
	public void saveAll(List<Entity> objects);
	public void updateAll(List<Entity> objects);

}
