package de.herrmanno.jdbcorm.tables;

import java.util.Arrays;

public class JoinTable {

	StaticFieldProxy fp1;
	StaticFieldProxy fp2;

	public JoinTable(StaticFieldProxy fp) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		fp1 = fp;
		fp2 = EntityHelper.getFieldByName(fp.getReferenceClass(), fp.getReferenceFieldName());
	}
	
	public String getTableName() {
		StaticFieldProxy fp_1 = sort(fp1, fp2)[0];
		StaticFieldProxy fp_2 = sort(fp1, fp2)[1];

		final String className = 
		 	fp_1.getDeclaringClass().getSimpleName() + "_" + 
		 	fp_1.getName() + "_" + 
		 	fp_2.getDeclaringClass().getSimpleName() + "_" +
		 	fp_2.getName();
        return className;
	}
	
	public String getColumnName(StaticFieldProxy fp) {
		return fp.getDeclaringClass().getSimpleName() + "_" + fp.getName();
	}
	
	public StaticFieldProxy[] getFieldProxies() {
		return sort(fp1, fp2);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof JoinTable))
			return false;
		else if(this == null && obj != null || this != null && obj == null)
			return false;
		else 
			return this.getTableName().equals(((JoinTable) obj).getTableName()); 
	}
	
	@Override
	public int hashCode() {
		return 17 * getTableName().hashCode();
	}
	
	private StaticFieldProxy[] sort(StaticFieldProxy fp1, StaticFieldProxy fp2) {
		StaticFieldProxy[] fpArr = new StaticFieldProxy[]{fp1, fp2};
		Arrays.sort(fpArr, (f1, f2) -> f1.getName().compareTo(f2.getName()));
		return fpArr;
	}

	public StaticFieldProxy getOtherFp(ObjectFieldProxy fp) {
		if(fp == fp1)
			return fp2;
		else 
			return fp1;
	}
}
