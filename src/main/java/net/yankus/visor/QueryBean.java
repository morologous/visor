package net.yankus.visor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QueryBean { 
	public Class settings();
	public String index();
	public Class filters();
	public Class returnType();
}
