package net.yankus.visor

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Visor { 
    public Class filters() default { match_all { } };
    public Class settings() default { node { local = true } };
    public String index();
    public boolean remote() default false;

}