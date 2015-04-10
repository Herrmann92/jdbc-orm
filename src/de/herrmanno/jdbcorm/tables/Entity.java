package de.herrmanno.jdbcorm.tables;

import static de.herrmanno.jdbcorm.constants.Constraint.AUTO_INCREMENT;
import static de.herrmanno.jdbcorm.constants.Constraint.NOT_NULL;
import static de.herrmanno.jdbcorm.constants.Constraint.PRIMARY_KEY;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import de.herrmanno.jdbcorm.annotations.Field;


public abstract class Entity {
	
	boolean isNew = true;
	boolean isSaving = false;
	
	@Field({PRIMARY_KEY, AUTO_INCREMENT, NOT_NULL})
	private long id = -1;
	
	@Field
	private Date createdAt = null;
	
	@Field
	private Date updatedAt = null;
	
	
	public Entity() {
		try {
			for(ObjectFieldProxy refField : EntityHelper.getListReferencedFields(this)) {
				refField.setValue(ChildList.createEmpty(refField.getReferenceClass()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public long getId() {
		return this.id;
	}
	
	void setId(long id) {
		this.id = id;
	}
	
	public Date getCreateAt() {
		return this.createdAt;
	}
	
	public Date getUpdatedAt() {
		return this.updatedAt;
	}
	
	public boolean validate() {
		return true;
	}
	
	public void beforeLoad() {};
	
	public void afterLoad() {};
	
	public void beforeSave() {
		Date now = new Date();
		if(isNew) {
			createdAt = now;
		}
		
		updatedAt = now;
	};
	
	public void afterSave() {};
	
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		
		List<ObjectFieldProxy> fields;
		try {
			fields = EntityHelper.getFields(this);
			StringBuilder sb = new StringBuilder();
			for(ObjectFieldProxy fp : fields) {
				sb.append(fp.name);
				sb.append(" - ");
				if(fp.getIsReference()) {
					Collection<Entity> refE = (Collection<Entity>) fp.getValue();
					String value = refE != null ? EntityHelper.getTableName((Class<? extends Entity>) fp.getReferenceClass()) + " (" + refE.size() + ")" : "NULL";
					sb.append(fp.value != null ? value : "NULL");
				} else if(Entity.class.isAssignableFrom(fp.getType())) {
					Entity refE = (Entity) fp.getValue();
					String value = refE != null ? EntityHelper.getTableName(refE.getClass()) + " - " + refE.getId() : "NULL";
					sb.append(fp.value != null ? value : "NULL");
				} else {
					sb.append(fp.value != null ? fp.value : "NULL");
				}
				sb.append("\t [");
				for(int c = 0; c < fp.annotation.length; c++) {
					sb.append(fp.annotation[c].toString());
					if(c + 1 < fp.annotation.length)
						sb.append(", ");
				}
				sb.append("]");
				sb.append("\n");
			}
			return sb.toString();
			
		} catch (Exception e1) {
			e1.printStackTrace();
			return "An error occured while invoking 'toString'";
		}
		
	}
	
}
