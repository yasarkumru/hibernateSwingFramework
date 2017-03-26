package hibernate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionException;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import utils.Entity;
import utils.TableUpdater;
import utils.Utils;

public class HibernateManager {

	private static HibernateManager instance;
	private SessionFactory sessionFactory;
	private Configuration configuration;
	private String systemName;
	private static final String SYSTEM_NAME_PROPERTY = "system_name";
	private static final String SYSTEM_FIELD_NAME = "systemName";
	private Session permanentSession;
	private boolean isPermanentSession;

	/**
	 * Bu servis sinifi singleton pattern kullanmaktadir. buna binaen multiple instance'i
	 * olusturulamaz.
	 * 
	 * @return
	 */
	public static HibernateManager getInstance() {
		if (instance == null)
			instance = new HibernateManager();
		return instance;
	}

	private HibernateManager() {
	}

	private void parseSystemName() {
		systemName = configuration.getProperty(SYSTEM_NAME_PROPERTY);

	}

	public synchronized void startPermanentSession() {
		isPermanentSession = true;
		TableUpdater.getInstance().refreshLaterUpdates();
		permanentSession = sessionFactory.openSession();
	}

	public synchronized void endPermanentSession() {
		isPermanentSession = false;
		permanentSession.close();
		permanentSession = null;
		TableUpdater.getInstance().updateLaterUpdates();
	}

	public boolean isPermanentSession() {
		return isPermanentSession;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	/**
	 * custom hql kullanimi icin session acar. bunun harici bir kullanimi yok
	 * 
	 * @return
	 */
	public synchronized Session openSession() {
		if (permanentSession == null || !permanentSession.isOpen())
			return sessionFactory.openSession();
		return permanentSession;
	}

	public synchronized void closeSession(Session session) {
		if (session == permanentSession)
			return;

		session.close();
	}

	/**
	 * hibernate'in kullanacagi config'i file olarak alan method
	 * 
	 * @param configFile
	 */
	public synchronized void setConfigFile(File configFile) {
		Configuration config = new Configuration();

		config.configure(configFile);
		this.configuration = config;
		sessionFactory = config.buildSessionFactory();
		parseSystemName();
	}

	public synchronized void setConfigFile(InputStream inputStream) {

		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document parse = db.parse(inputStream);

			Configuration config = new Configuration();

			config.configure(parse);
			config.setProperty("hibernate.current_session_context_class", "thread");
			this.configuration = config;
			sessionFactory = config.buildSessionFactory();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		parseSystemName();

	}

	public synchronized Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * object'i ilgili tabloya kaydeder. (persistancy of the object)
	 * 
	 * @param o
	 * @return
	 */
	public synchronized boolean save(Entity o) {
		Session session = null;
		try {
			session = openSession();
			Transaction transaction = session.beginTransaction();

			session.save(o);

			transaction.commit();
		} catch (Exception e) {
			showExceptionMessage(e);
			return false;
		} finally {

			closeSession(session);
		}
		TableUpdater.getInstance().save(o);
		return true;
	}

	public synchronized boolean saveAll(List<Entity> entities) {
		if (entities == null || entities.size() == 0)
			return true;
		Session session = null;
		try {
			session = openSession();
			Transaction transaction = session.beginTransaction();

			int count = 0;
			for (Entity entity : entities) {
				session.save(entity);
				count++;
				if (count % 32 == 0) {
					session.flush();
					session.clear();
				}
			}

			transaction.commit();
		} catch (Exception e) {
			showExceptionMessage(e);
			return false;
		} finally {

			closeSession(session);
		}
		TableUpdater.getInstance().saveAll(entities);
		return true;
	}

	public synchronized boolean deleteAll(List<Entity> entities) {
		if (entities == null || entities.size() == 0)
			return true;
		Session session = null;
		try {
			session = openSession();
			Transaction transaction = session.beginTransaction();

			int count = 0;
			for (Entity entity : entities) {
				session.delete(entity);
				count++;
				if (count % 32 == 0) {
					session.flush();
					session.clear();
				}
			}

			transaction.commit();
		} catch (Exception e) {
			showExceptionMessage(e);
			return false;
		} finally {

			closeSession(session);
		}
		TableUpdater.getInstance().saveAll(entities);
		return true;
	}

	public synchronized boolean clearTable(Class<?> clazz) {
		Session session = null;
		try {
			session = openSession();
			Transaction transaction = session.beginTransaction();

			String hql = "delete from " + clazz.getSimpleName();
			Query createQuery = session.createQuery(hql);
			createQuery.executeUpdate();

			transaction.commit();
		} catch (Exception e) {
			showExceptionMessage(e);
			return false;
		} finally {

			closeSession(session);
		}
		TableUpdater.getInstance().updateTable(clazz);
		return true;
	}

	@SuppressWarnings("unchecked")
	public synchronized boolean saveAllEntityObjects(Entity entity, String fieldName,
			List<Entity> entities) {
		if (entities.size() == 0)
			return true;
		Session session = null;
		try {
			session = openSession();
			Transaction transaction = session.beginTransaction();

			Entity object = (Entity) session.get(entity.getClass(), entity.getId());

			List<Entity> list = (List<Entity>) entity.getClass().getMethod(getGetMethod(fieldName))
					.invoke(object);

			for (Entity entity2 : entities) {
				list.add(entity2);
			}

			session.update(object);

			transaction.commit();
		} catch (Exception e) {
			showExceptionMessage(e);
			return false;
		} finally {

			closeSession(session);
		}

		TableUpdater.getInstance().updateTable(entities.get(0).getClass());
		return true;

	}

	/**
	 * verilen entity'nin belirtilen fieldname'inde bulunan listeye, object'i ekler.
	 * 
	 * @param entity
	 * @param fieldName
	 * @param subEntity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean saveEntityObject(Entity entity, String fieldName, Entity subEntity) {
		Session session = null;
		try {
			session = openSession();
			Transaction transaction = session.beginTransaction();

			Entity object = (Entity) session.get(entity.getClass(), entity.getId());
			List<Entity> list = (List<Entity>) entity.getClass().getMethod(getGetMethod(fieldName))
					.invoke(object);

			list.add(subEntity);

			session.update(object);
			transaction.commit();
		} catch (Exception e) {
			showExceptionMessage(e);
			return false;
		} finally {

			closeSession(session);
		}

		TableUpdater.getInstance().save(subEntity);
		return true;
	}

	/**
	 * verilen object'i update eder.
	 * 
	 * @param o
	 * @return
	 */
	public synchronized boolean update(Entity o) {
		Session session = null;
		try {
			session = openSession();
			Transaction transaction = session.beginTransaction();

			session.update(o);

			transaction.commit();
		} catch (Exception e) {
			showExceptionMessage(e);
			return false;
		} finally {

			closeSession(session);
		}

		TableUpdater.getInstance().update(o);
		return true;
	}

	public synchronized boolean updateAll(List<Entity> entities) {
		if (entities == null || entities.size() == 0)
			return true;
		Session session = null;
		try {
			session = openSession();
			Transaction transaction = session.beginTransaction();

			int count = 0;
			for (Entity entity : entities) {
				session.update(entity);
				count++;
				if (count % 32 == 0) {
					session.flush();
					session.clear();
				}
			}

			transaction.commit();
		} catch (Exception e) {
			showExceptionMessage(e);
			return false;
		} finally {
			closeSession(session);
		}
		TableUpdater.getInstance().updateAll(entities);
		return true;
	}

	/**
	 * verilen object'i siler. bir entity'nin alt objesi dahi olsa silinen veri orasi
	 * ilede iliskilendirilir.
	 * 
	 * @param o
	 * @return
	 */
	public synchronized boolean delete(Entity o) {
		Session session = null;
		try {
			session = openSession();
			Transaction transaction = session.beginTransaction();

			session.delete(o);
			transaction.commit();
		} catch (Exception e) {
			showExceptionMessage(e);
			return false;
		} finally {

			closeSession(session);
		}

		TableUpdater.getInstance().delete(o);
		return true;
	}

	/**
	 * verilen entity'nin belirtilen fieldname'inde bulunan listeye, object'i ekler.
	 * 
	 * @param entity
	 * @param fieldName
	 * @param subEntity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean deleteEntityObject(Entity entity, String fieldName, Entity subEntity) {
		Session session = null;
		try {
			session = openSession();
			Transaction transaction = session.beginTransaction();

			Object object = session.get(entity.getClass(), entity.getId());
			List<Entity> list = (List<Entity>) entity.getClass().getMethod(getGetMethod(fieldName))
					.invoke(object);

			for (Object object2 : list) {
				Entity en = (Entity) object2;
				if (en.getId() == subEntity.getId()) {
					list.remove(en);
					break;
				}
			}
			session.update(object);
			transaction.commit();
		} catch (Exception e) {
			showExceptionMessage(e);
			return false;
		} finally {

			closeSession(session);
		}

		TableUpdater.getInstance().delete(subEntity);
		return true;
	}

	public synchronized List<?> distinctSelectField(Class<?> c, String fieldName) {
		Session session = null;
		List<?> list = null;
		try {
			session = openSession();
			session.beginTransaction();
			Criteria criteria = session.createCriteria(c);
			criteria.setProjection(Projections.distinct(Projections.property(fieldName)));
			list = criteria.list();
		} catch (HibernateException e) {
			showExceptionMessage(e);
			return null;
		} finally {

			closeSession(session);
		}
		return list;
	}

	/**
	 * verilen sinifin butun object'lerini liste olarak doner
	 * 
	 * @param c
	 * @return
	 */
	public synchronized List<?> get(Class<?> c) {
		return get(c, 0, Integer.MAX_VALUE);
	}

	public synchronized List<?> get(Class<?> c, int start, int offset) {
		Session session = null;
		List<?> list = null;
		try {
			session = openSession();
			session.beginTransaction();

			Criteria criteria = session.createCriteria(c);
			if (start != 0 && offset != Integer.MAX_VALUE) {
				criteria.setFirstResult(start);
				criteria.setMaxResults(offset);
			}

			list = criteria.list();
		} catch (HibernateException e) {
			showExceptionMessage(e);
			return null;
		} finally {

			closeSession(session);
		}
		return list;
	}

	/**
	 * verilen bir entity'nin belirtilen fieldname'indeki listeyi doner.
	 * 
	 * @param e
	 * @param fieldName
	 * @return
	 */
	public synchronized List<?> getEntitysList(Entity e, String fieldName) {
		Session session = openSession();
		session.beginTransaction();

		Object object = session.get(e.getClass(), e.getId());

		List<?> list = null;
		try {
			list = (List<?>) e.getClass().getMethod(getGetMethod(fieldName)).invoke(object);
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		}

		list.size();

		closeSession(session);

		return list;
	}

	public synchronized Object getObjectWithUniqueColumn(Class<?> clazz, String fieldName,
			Object value) {
		return getObjectWithUniqueColumn(clazz, fieldName, value, true);
	}

	/**
	 * verilen sinifa ait tabloda belirtilen field'da verilen value degeri olan unique
	 * object'i doner
	 * 
	 * @param clazz
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public synchronized Object getObjectWithUniqueColumn(Class<?> clazz, String fieldName,
			Object value, boolean systemNameCheck) {
		Session session = openSession();
		session.beginTransaction();

		Criteria criteria = session.createCriteria(clazz, fieldName);
		criteria.add(Restrictions.eq(fieldName, value));
		if (systemNameCheck)
			criteria.add(Restrictions.eq(SYSTEM_FIELD_NAME, systemName));
		Object uniqueResult = criteria.uniqueResult();

		closeSession(session);
		return uniqueResult;

	}

	public synchronized List<?> getWithCriteria(Class<?> clazz, String fieldName, Object value) {
		return getWithCriteria(clazz, fieldName, value, true);
	}

	/**
	 * verilen sinifa ait tabloda belirtilen field'da verilen value degeri olan object
	 * listesini doner
	 * 
	 * @param clazz
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public synchronized List<?> getWithCriteria(Class<?> clazz, String fieldName, Object value,
			boolean systemNameCheck) {
		Session session = openSession();
		session.beginTransaction();

		Criteria criteria = session.createCriteria(clazz, fieldName);
		criteria.add(Restrictions.eq(fieldName, value).ignoreCase());
		if (systemNameCheck)
			criteria.add(Restrictions.eq(SYSTEM_FIELD_NAME, systemName));
		List<?> list = criteria.list();

		closeSession(session);
		return list;
	}

	private synchronized static void showExceptionMessage(Exception e) {
		System.err.println(e.getMessage());
		e.printStackTrace();
		if (e instanceof ConstraintViolationException) {
			if (e.getMessage().contains("insert"))
				Utils.showErrorMessage("Aynı girdi daha önceden girilmiş");
			else {
				Utils.showErrorMessage("Bu girdinin silinmesi diğer verilere zarar vereceğinden kısıtlanmıştır.");
			}
		} else if (e instanceof SessionException) {
			Utils.showErrorMessage("Veritabanı bağlantı hatası.(session)");
		} else if (e instanceof TransactionException) {
			Utils.showErrorMessage("Veritabanı bağlantı hatası.(transaction)");
		} else if (e instanceof JDBCException) {
			Utils.showErrorMessage("Veritabanı bağlantı hatası.(jdbc))");
		} else {
			Utils.showErrorMessage("Beklenmeyen bir hata oluştu. Tekrarı halinde servis çağırın!");
		}
	}

	private synchronized static String getGetMethod(String s) {
		return "get" + s.substring(0, 1).toUpperCase(Locale.ENGLISH) + s.substring(1);
	}
}
