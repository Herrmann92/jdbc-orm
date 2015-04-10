package de.herrmanno.jdbcorm.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class ChildList<T extends Entity> extends ArrayList<T>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5134454257966099877L;
	
	
	private Collection<T> initialValues = null;;
	
	private ChildList(Collection<T> initList) {
		super();
		this.addAll(initList);
		this.initialValues = new ArrayList<T>(initList);
	}
	
	static <T extends Entity> ChildList<T> create(Collection<T> list) {
		return new ChildList<T>(list);
	}
	
	static <T extends Entity> ChildList<T> createEmpty(Class<T> clazz) {
		return new ChildList<T>(new ArrayList<T>());
	}
	
	List<T> getRemoved() {
		List<T> list = new ArrayList<T>();
		
		for(T t : initialValues) {
			if(!this.contains(t))
				list.add(t);
		}
		
		return list;
	}
	
	List<T> getAdded() {
		List<T> list = new ArrayList<T>();
		
		for(T t : this) {
			if(!this.initialValues.contains(t))
				list.add(t);
		}
		
		return list;
	} 

}
