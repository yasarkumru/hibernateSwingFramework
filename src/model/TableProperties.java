package model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface TableProperties {
	
	public String name() default "";
	public boolean showable() default true;
	public int columnOrder() default 0;
	public boolean editable() default false;
	public boolean showTooltip() default false;

}
