package genericUI.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface UiProperties {
	
	public String name() default "";
	public boolean showable() default true;
	public int columnOrder() default 0;
	public boolean mandatory() default false;
	public boolean timeIncluded() default true;
}
