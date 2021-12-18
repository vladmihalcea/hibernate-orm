/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.internal.entities.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.Supplier;

import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.internal.entities.PropertyData;
import org.hibernate.envers.internal.tools.ReflectionTools;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.service.ServiceRegistry;

/**
 * A base class for all entity mapper implementations.
 *
 * @author Chris Cranford
 */
public abstract class AbstractMapper {

	/**
	 * Perform an action in a privileged block.
	 *
	 * @param block the lambda to executed in privileged.
	 * @param <T> the return type
	 * @return the result of the privileged call, may be {@literal null}
	 */
	protected <T> T doPrivileged(Supplier<T> block) {
		if ( System.getSecurityManager() != null ) {
			return AccessController.doPrivileged( (PrivilegedAction<T>) block::get );
		}
		else {
			return block.get();
		}
	}

	/**
	 * Get a value from the specified object.
	 *
	 * @param propertyData the property data, should not be {@literal null}
	 * @param object the object for which the value should be read, should not be {@literal null}
	 * @param serviceRegistry the service registry, should not be {@literal null}
	 * @param <T> the return type
	 * @return the value read from the object, may be {@literal null}
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getValueFromObject(PropertyData propertyData, Object object, ServiceRegistry serviceRegistry) {
		return doPrivileged( () -> {
			final Getter getter = ReflectionTools.getGetter( object.getClass(), propertyData, serviceRegistry );
			return (T) getter.get( object );
		} );
	}

	/**
	 * Get a value from the specified object.
	 *
	 * @param propertyName the property name, should not be {@literal null}
	 * @param accessType the property access type, should not be {@literal null}
	 * @param object the object for hwich the value should be read, should not be {@literal null}
	 * @param serviceRegistry the service registry, should not be {@literal null}
	 * @param <T> the return type
	 * @return the value read from the object, may be {@literal null}
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getValueFromObject(String propertyName, String accessType, Object object, ServiceRegistry serviceRegistry) {
		return doPrivileged( () -> {
			final Getter getter = ReflectionTools.getGetter( object.getClass(), propertyName, accessType, serviceRegistry );
			return (T) getter.get( object );
		} );
	}

	/**
	 * Set the specified value on the object.
	 *
	 * @param propertyData the property data, should not be {@literal null}
	 * @param object the object for which the value should be set, should not be {@literal null}
	 * @param value the value ot be set, may be {@literal null}
	 * @param serviceRegistry the service registry, should not be {@literal null}
	 */
	protected void setValueOnObject(PropertyData propertyData, Object object, Object value, ServiceRegistry serviceRegistry) {
		doPrivileged( () -> {
			final Setter setter = ReflectionTools.getSetter(object.getClass(), propertyData, serviceRegistry );
			setter.set( object, value );
			return null;
		} );
	}

	/**
	 * Gets the value from the source object and sets the value in the destination object.
	 *
	 * @param propertyData the property data, should not be {@literal null}
	 * @param source the source object, should not be {@literal null}
	 * @param destination the destination object, should not be {@literal null}
	 * @param serviceRegistry the service registry, should not be {@literal null}
	 */
	protected void getAndSetValue(PropertyData propertyData, Object source, Object destination, ServiceRegistry serviceRegistry) {
		doPrivileged( () -> {
			final Getter getter = ReflectionTools.getGetter( source.getClass(), propertyData, serviceRegistry );
			final Setter setter = ReflectionTools.getSetter( destination.getClass(), propertyData, serviceRegistry );
			setter.set( destination, getter.get( source ) );
			return null;
		} );
	}

	/**
	 * Creates a new object based on the specified class with the given constructor arguments.
	 *
	 * @param clazz the class, must not be {@literal null}
	 * @param args the variadic constructor arguments, may be omitted.
	 * @param <T> the return class type
	 * @return a new instance of the class
	 */
	protected <T> T newObjectInstance(Class<T> clazz, Object... args) {
		return doPrivileged( () -> {
			try {
				final Constructor<T> constructor = ReflectHelper.getDefaultConstructor( clazz );
				if ( constructor == null ) {
					throw new AuditException( "Failed to locate default constructor for class: " + clazz.getName() );
				}
				return constructor.newInstance( args );
			}
			catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new AuditException( e );
			}
		} );
	}
}
