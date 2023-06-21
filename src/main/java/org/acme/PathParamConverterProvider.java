package org.acme;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.jboss.logging.Logger;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PathParamConverterProvider implements ParamConverterProvider {

    public static final Logger log = Logger.getLogger(PathParamConverterProvider.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        log.infof("getConverter(%s, %s, %s)", rawType.getName(), genericType.getTypeName(), annotations.length);

        if (rawType == java.nio.file.Path.class) {
            log.info("Path -> Looks like its assignable: " + rawType.getName());

            return (ParamConverter<T>) new ParamConverter<java.nio.file.Path>() {

                @Override
                public java.nio.file.Path fromString(String value) {
                    try {
                        java.nio.file.Path path = java.nio.file.Path.of(value);

                        return path;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    return null;
                }

                @Override
                public String toString(java.nio.file.Path value) {

                    return value.toString();
                }

            };
        }

        if (rawType == PathWrapper.class) {
            log.info("PathWrapper -> Looks like its assignable: " + rawType.getName());
            return (ParamConverter<T>) new ParamConverter<PathWrapper>() {

                @Override
                public PathWrapper fromString(String value) {
                    try {
                        PathWrapper path = new PathWrapper(java.nio.file.Path.of(value));

                        return path;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    return null;
                }

                @Override
                public String toString(PathWrapper value) {

                    return value.getPath().toString();
                }

            };
        }

        log.error("No ParamConverter for " + rawType.getName());
        return null;
    }

}
