package app.modus;

import java.lang.annotation.*;

/** used to distinguish a app.modus that can be instantiated without an instantiation exception or error. */
@Target( {ElementType.TYPE} )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
public @interface ModusMetatagRunStatus {
    boolean value() default false;
}
