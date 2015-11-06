package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import play.db.jpa.JPA;
import play.db.jpa.Transactional;

/**
 * 
 */
@Entity
public class SystemInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	public Date serverStartTime;

	public Date serverRunTime;

	public String androidVersion = "1";

	public String iosVersion = "1";

	private static SystemInfo systemInfo;

	public SystemInfo() {
	}

	public static SystemInfo getInfo() {
		if (systemInfo != null) {
			return systemInfo;
		}
		Query q = JPA.em().createQuery("SELECT s FROM SystemInfo s");
		q.setMaxResults(1);
		systemInfo = (SystemInfo) q.getSingleResult();
		return systemInfo;
	}

	public static void recordServerStartTime() {
		SystemInfo info = getInfo();
		if (info != null) {
			info.serverStartTime = new Date();
			info.save();
		}
	}

	@Transactional
	public static void recordServerRunTime() {
		SystemInfo info = getInfo();
		if (info != null) {
			info.serverRunTime = new Date();
			info.save();
		}
	}

	@Transactional
	public void save() {
		JPA.em().persist(this);
		JPA.em().flush();     
	}

	@Transactional
	public void delete() {
		JPA.em().remove(this);
	}

	@Transactional
	public void merge() {
		JPA.em().merge(this);
	}

	@Transactional
	public void refresh() {
		JPA.em().refresh(this);
	}
}
