package de.herrmanno.jdbcorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

import de.herrmanno.jdbcorm.constants.CascadeType;
import de.herrmanno.jdbcorm.constants.Constraint;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.FIELD)
public @interface ForeignKey {
	String RefColumn() default "id";
	CascadeType OnUpdate() default CascadeType.NO_ACTION;
	CascadeType OnDelete() default CascadeType.NO_ACTION;
}
