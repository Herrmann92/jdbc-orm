package de.herrmanno.jdbcorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.herrmanno.jdbcorm.constants.CascadeType;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.FIELD)
public @interface ForeignKey {
	String RefColumn() default "id";
	CascadeType OnUpdate() default CascadeType.CASCADE;
	CascadeType OnDelete() default CascadeType.CASCADE;
}
