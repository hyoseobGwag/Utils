package com.utils.bean;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.WrapDynaBean;
import org.apache.commons.beanutils.expression.DefaultResolver;
import org.apache.commons.beanutils.expression.Resolver;
import org.apache.commons.collections.FastHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyUtilsBean {
	private Resolver resolver = new DefaultResolver();

	private WeakFastHashMap descriptorsCache = null;
	private WeakFastHashMap mappedDescriptorsCache = null;
  @SuppressWarnings("rawtypes")
private static final Class[] EMPTY_CLASS_PARAMETERS = new Class[0];
  @SuppressWarnings("rawtypes")
private static final Class[] LIST_CLASS_PARAMETER = { List.class };

	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	private Logger log = LoggerFactory.getLogger(PropertyUtils.class);

	protected static PropertyUtilsBean getInstance() {
		return BeanUtilsBean.getInstance().getPropertyUtils();
	}

	public PropertyUtilsBean() {
		this.descriptorsCache = new WeakFastHashMap();
		this.descriptorsCache.setFast(true);
		this.mappedDescriptorsCache = new WeakFastHashMap();
		this.mappedDescriptorsCache.setFast(true);
	}

	public Resolver getResolver() {
		return this.resolver;
	}

	public void setResolver(Resolver resolver) {
		if (resolver == null)
			this.resolver = new DefaultResolver();
		else
			this.resolver = resolver;
	}

	public void clearDescriptors() {
		this.descriptorsCache.clear();
		this.mappedDescriptorsCache.clear();
		Introspector.flushCaches();
	}

	@SuppressWarnings("rawtypes")
	public void copyProperties(Object dest, Object orig)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (dest == null) {
			throw new IllegalArgumentException("No destination bean specified");
		}

		if (orig == null) {
			throw new IllegalArgumentException("No origin bean specified");
		}

		if ((orig instanceof DynaBean)) {
			DynaProperty[] origDescriptors = ((DynaBean) orig).getDynaClass().getDynaProperties();

			for (int i = 0; i < origDescriptors.length; i++) {
				String name = origDescriptors[i].getName();
				if ((isReadable(orig, name)) && (isWriteable(dest, name))) {
					try {
						Object value = ((DynaBean) orig).get(name);
						if ((dest instanceof DynaBean))
							((DynaBean) dest).set(name, value);
						else
							setSimpleProperty(dest, name, value);
					} catch (NoSuchMethodException e) {
						if (this.log.isDebugEnabled())
							this.log.debug("Error writing to '" + name + "' on class '" + dest.getClass() + "'", e);
					}
				}
			}
		} else if ((orig instanceof Map)) {
			Iterator entries = ((Map) orig).entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry entry = (Map.Entry) entries.next();
				String name = (String) entry.getKey();
				if (isWriteable(dest, name))
					try {
						if ((dest instanceof DynaBean))
							((DynaBean) dest).set(name, entry.getValue());
						else
							setSimpleProperty(dest, name, entry.getValue());
					} catch (NoSuchMethodException e) {
						if (this.log.isDebugEnabled())
							this.log.debug("Error writing to '" + name + "' on class '" + dest.getClass() + "'", e);
					}
			}
		} else {
			PropertyDescriptor[] origDescriptors = getPropertyDescriptors(orig);

			for (int i = 0; i < origDescriptors.length; i++) {
				String name = origDescriptors[i].getName();
				if ((isReadable(orig, name)) && (isWriteable(dest, name)))
					try {
						Object value = getSimpleProperty(orig, name);
						if ((dest instanceof DynaBean))
							((DynaBean) dest).set(name, value);
						else
							setSimpleProperty(dest, name, value);
					} catch (NoSuchMethodException e) {
						if (this.log.isDebugEnabled())
							this.log.debug("Error writing to '" + name + "' on class '" + dest.getClass() + "'", e);
					}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map describe(Object bean) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		Map description = new HashMap();
		if ((bean instanceof DynaBean)) {
			DynaProperty[] descriptors = ((DynaBean) bean).getDynaClass().getDynaProperties();

			for (int i = 0; i < descriptors.length; i++) {
				String name = descriptors[i].getName();
				description.put(name, getProperty(bean, name));
			}
		} else {
			PropertyDescriptor[] descriptors = getPropertyDescriptors(bean);

			for (int i = 0; i < descriptors.length; i++) {
				String name = descriptors[i].getName();
				if (descriptors[i].getReadMethod() != null) {
					description.put(name, getProperty(bean, name));
				}
			}
		}
		return description;
	}

	public Object getIndexedProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		int index = -1;
		try {
			index = this.resolver.getIndex(name);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid indexed property '" + name + "' on bean class '"
					+ bean.getClass() + "' " + e.getMessage());
		}

		if (index < 0) {
			throw new IllegalArgumentException(
					"Invalid indexed property '" + name + "' on bean class '" + bean.getClass() + "'");
		}

		name = this.resolver.getProperty(name);

		return getIndexedProperty(bean, name, index);
	}

	@SuppressWarnings("rawtypes")
	public Object getIndexedProperty(Object bean, String name, int index)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if ((name == null) || (name.length() == 0)) {
			if (bean.getClass().isArray())
				return Array.get(bean, index);
			if ((bean instanceof List)) {
				return ((List) bean).get(index);
			}
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		if ((bean instanceof DynaBean)) {
			DynaProperty descriptor = ((DynaBean) bean).getDynaClass().getDynaProperty(name);

			if (descriptor == null) {
				throw new NoSuchMethodException(
						"Unknown property '" + name + "' on bean class '" + bean.getClass() + "'");
			}

			return ((DynaBean) bean).get(name, index);
		}

		PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);

		if (descriptor == null) {
			throw new NoSuchMethodException("Unknown property '" + name + "' on bean class '" + bean.getClass() + "'");
		}

		if ((descriptor instanceof IndexedPropertyDescriptor)) {
			Method readMethod = ((IndexedPropertyDescriptor) descriptor).getIndexedReadMethod();

			readMethod = MethodUtils.getAccessibleMethod(bean.getClass(), readMethod);
			if (readMethod != null) {
				Object[] subscript = new Object[1];
				subscript[0] = new Integer(index);
				try {
					return invokeMethod(readMethod, bean, subscript);
				} catch (InvocationTargetException e) {
					if ((e.getTargetException() instanceof IndexOutOfBoundsException)) {
						throw ((IndexOutOfBoundsException) e.getTargetException());
					}

					throw e;
				}

			}

		}

		Method readMethod = getReadMethod(bean.getClass(), descriptor);
		if (readMethod == null) {
			throw new NoSuchMethodException(
					"Property '" + name + "' has no " + "getter method on bean class '" + bean.getClass() + "'");
		}

		Object value = invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);
		if (!value.getClass().isArray()) {
			if (!(value instanceof List)) {
				throw new IllegalArgumentException(
						"Property '" + name + "' is not indexed on bean class '" + bean.getClass() + "'");
			}

			return ((List) value).get(index);
		}

		try {
			return Array.get(value, index);
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		throw new ArrayIndexOutOfBoundsException(
				"Index: " + index + ", Size: " + Array.getLength(value) + " for property '" + name + "'");
	}

	public Object getMappedProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		String key = null;
		try {
			key = this.resolver.getKey(name);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"Invalid mapped property '" + name + "' on bean class '" + bean.getClass() + "' " + e.getMessage());
		}

		if (key == null) {
			throw new IllegalArgumentException(
					"Invalid mapped property '" + name + "' on bean class '" + bean.getClass() + "'");
		}

		name = this.resolver.getProperty(name);

		return getMappedProperty(bean, name, key);
	}

	@SuppressWarnings("rawtypes")
	public Object getMappedProperty(Object bean, String name, String key)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		if (key == null) {
			throw new IllegalArgumentException(
					"No key specified for property '" + name + "' on bean class " + bean.getClass() + "'");
		}

		if ((bean instanceof DynaBean)) {
			DynaProperty descriptor = ((DynaBean) bean).getDynaClass().getDynaProperty(name);

			if (descriptor == null) {
				throw new NoSuchMethodException(
						"Unknown property '" + name + "'+ on bean class '" + bean.getClass() + "'");
			}

			return ((DynaBean) bean).get(name, key);
		}

		Object result = null;

		PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
		if (descriptor == null) {
			throw new NoSuchMethodException("Unknown property '" + name + "'+ on bean class '" + bean.getClass() + "'");
		}

		if ((descriptor instanceof MappedPropertyDescriptor)) {
			Method readMethod = ((MappedPropertyDescriptor) descriptor).getMappedReadMethod();

			readMethod = MethodUtils.getAccessibleMethod(bean.getClass(), readMethod);
			if (readMethod != null) {
				Object[] keyArray = new Object[1];
				keyArray[0] = key;
				result = invokeMethod(readMethod, bean, keyArray);
			} else {
				throw new NoSuchMethodException(
						"Property '" + name + "' has no mapped getter method on bean class '" + bean.getClass() + "'");
			}

		} else {
			Method readMethod = getReadMethod(bean.getClass(), descriptor);
			if (readMethod != null) {
				Object invokeResult = invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);

				if ((invokeResult instanceof Map))
					result = ((Map) invokeResult).get(key);
			} else {
				throw new NoSuchMethodException(
						"Property '" + name + "' has no mapped getter method on bean class '" + bean.getClass() + "'");
			}

		}

		return result;
	}

	@SuppressWarnings("rawtypes")
	public FastHashMap getMappedPropertyDescriptors(Class beanClass) {
		if (beanClass == null) {
			return null;
		}

		return (FastHashMap) this.mappedDescriptorsCache.get(beanClass);
	}

	public FastHashMap getMappedPropertyDescriptors(Object bean) {
		if (bean == null) {
			return null;
		}
		return getMappedPropertyDescriptors(bean.getClass());
	}

	@SuppressWarnings("rawtypes")
	public Object getNestedProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		while (this.resolver.hasNested(name)) {
			String next = this.resolver.next(name);
			Object nestedBean = null;
			if ((bean instanceof Map))
				nestedBean = getPropertyOfMapBean((Map) bean, next);
			else if (this.resolver.isMapped(next))
				nestedBean = getMappedProperty(bean, next);
			else if (this.resolver.isIndexed(next))
				nestedBean = getIndexedProperty(bean, next);
			else {
				nestedBean = getSimpleProperty(bean, next);
			}
			if (nestedBean == null) {
				throw new NestedNullException(
						"Null property value for '" + name + "' on bean class '" + bean.getClass() + "'");
			}

			bean = nestedBean;
			name = this.resolver.remove(name);
		}

		if ((bean instanceof Map))
			bean = getPropertyOfMapBean((Map) bean, name);
		else if (this.resolver.isMapped(name))
			bean = getMappedProperty(bean, name);
		else if (this.resolver.isIndexed(name))
			bean = getIndexedProperty(bean, name);
		else {
			bean = getSimpleProperty(bean, name);
		}
		return bean;
	}

	@SuppressWarnings("rawtypes")
	protected Object getPropertyOfMapBean(Map bean, String propertyName)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (this.resolver.isMapped(propertyName)) {
			String name = this.resolver.getProperty(propertyName);
			if ((name == null) || (name.length() == 0)) {
				propertyName = this.resolver.getKey(propertyName);
			}
		}

		if ((this.resolver.isIndexed(propertyName)) || (this.resolver.isMapped(propertyName))) {
			throw new IllegalArgumentException(
					"Indexed or mapped properties are not supported on objects of type Map: " + propertyName);
		}

		return bean.get(propertyName);
	}

	public Object getProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return getNestedProperty(bean, name);
	}

	public PropertyDescriptor getPropertyDescriptor(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		while (this.resolver.hasNested(name)) {
			String next = this.resolver.next(name);
			Object nestedBean = getProperty(bean, next);
			if (nestedBean == null) {
				throw new NestedNullException(
						"Null property value for '" + next + "' on bean class '" + bean.getClass() + "'");
			}

			bean = nestedBean;
			name = this.resolver.remove(name);
		}

		name = this.resolver.getProperty(name);

		if (name == null) {
			return null;
		}

		PropertyDescriptor[] descriptors = getPropertyDescriptors(bean);
		if (descriptors != null) {
			for (int i = 0; i < descriptors.length; i++) {
				if (name.equals(descriptors[i].getName())) {
					return descriptors[i];
				}
			}
		}

		PropertyDescriptor result = null;
		FastHashMap mappedDescriptors = getMappedPropertyDescriptors(bean);

		if (mappedDescriptors == null) {
			mappedDescriptors = new FastHashMap();
			mappedDescriptors.setFast(true);
			this.mappedDescriptorsCache.put(bean.getClass(), mappedDescriptors);
		}
		result = (PropertyDescriptor) mappedDescriptors.get(name);
		if (result == null) {
			try {
				result = new MappedPropertyDescriptor(name, bean.getClass());
			} catch (IntrospectionException ie) {
			}

			if (result != null) {
				mappedDescriptors.put(name, result);
			}
		}

		return result;
	}

	@SuppressWarnings("rawtypes")
	public PropertyDescriptor[] getPropertyDescriptors(Class beanClass) {
		if (beanClass == null) {
			throw new IllegalArgumentException("No bean class specified");
		}

		PropertyDescriptor[] descriptors = null;
		descriptors = (PropertyDescriptor[]) this.descriptorsCache.get(beanClass);

		if (descriptors != null) {
			return descriptors;
		}

		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(beanClass);
		} catch (IntrospectionException e) {
			return new PropertyDescriptor[0];
		}
		descriptors = beanInfo.getPropertyDescriptors();
		if (descriptors == null) {
			descriptors = new PropertyDescriptor[0];
		}

		for (int i = 0; i < descriptors.length; i++) {
			if ((descriptors[i] instanceof IndexedPropertyDescriptor)) {
				IndexedPropertyDescriptor descriptor = (IndexedPropertyDescriptor) descriptors[i];
				String propName = descriptor.getName().substring(0, 1).toUpperCase()
						+ descriptor.getName().substring(1);

				if (descriptor.getReadMethod() == null) {
					String methodName = "get" + propName;

					Method readMethod = MethodUtils.getMatchingAccessibleMethod(beanClass, methodName,
							EMPTY_CLASS_PARAMETERS);

					if (readMethod != null) {
						try {
							descriptor.setReadMethod(readMethod);
						} catch (Exception e) {
							this.log.error("Error setting indexed property read method", e);
						}
					}
				}
				if (descriptor.getWriteMethod() == null) {
					String methodName = "set" + propName;

					Method writeMethod = MethodUtils.getMatchingAccessibleMethod(beanClass, methodName,
							LIST_CLASS_PARAMETER);

					if (writeMethod == null) {
						Method[] methods = beanClass.getMethods();
						for (int j = 0; j < methods.length; j++) {
							if (methods[j].getName().equals(methodName)) {
								Class[] parameterTypes = methods[j].getParameterTypes();
								if ((parameterTypes.length == 1) && (List.class.isAssignableFrom(parameterTypes[0]))) {
									writeMethod = methods[j];
									break;
								}
							}
						}
					}
					if (writeMethod != null) {
						try {
							descriptor.setWriteMethod(writeMethod);
						} catch (Exception e) {
							this.log.error("Error setting indexed property write method", e);
						}
					}
				}
			}

		}

		this.descriptorsCache.put(beanClass, descriptors);
		return descriptors;
	}

	public PropertyDescriptor[] getPropertyDescriptors(Object bean) {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		return getPropertyDescriptors(bean.getClass());
	}

	public Class getPropertyEditorClass(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);

		if (descriptor != null) {
			return descriptor.getPropertyEditorClass();
		}
		return null;
	}

	public Class getPropertyType(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		while (this.resolver.hasNested(name)) {
			String next = this.resolver.next(name);
			Object nestedBean = getProperty(bean, next);
			if (nestedBean == null) {
				throw new NestedNullException(
						"Null property value for '" + next + "' on bean class '" + bean.getClass() + "'");
			}

			bean = nestedBean;
			name = this.resolver.remove(name);
		}

		name = this.resolver.getProperty(name);

		if ((bean instanceof DynaBean)) {
			DynaProperty descriptor = ((DynaBean) bean).getDynaClass().getDynaProperty(name);

			if (descriptor == null) {
				return null;
			}
			Class type = descriptor.getType();
			if (type == null)
				return null;
			if (type.isArray()) {
				return type.getComponentType();
			}
			return type;
		}

		PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);

		if (descriptor == null)
			return null;
		if ((descriptor instanceof IndexedPropertyDescriptor)) {
			return ((IndexedPropertyDescriptor) descriptor).getIndexedPropertyType();
		}
		if ((descriptor instanceof MappedPropertyDescriptor)) {
			return ((MappedPropertyDescriptor) descriptor).getMappedPropertyType();
		}

		return descriptor.getPropertyType();
	}

	public Method getReadMethod(PropertyDescriptor descriptor) {
		return MethodUtils.getAccessibleMethod(descriptor.getReadMethod());
	}

	Method getReadMethod(Class clazz, PropertyDescriptor descriptor) {
		return MethodUtils.getAccessibleMethod(clazz, descriptor.getReadMethod());
	}

	public Object getSimpleProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		if (this.resolver.hasNested(name)) {
			throw new IllegalArgumentException("Nested property names are not allowed: Property '" + name
					+ "' on bean class '" + bean.getClass() + "'");
		}

		if (this.resolver.isIndexed(name)) {
			throw new IllegalArgumentException("Indexed property names are not allowed: Property '" + name
					+ "' on bean class '" + bean.getClass() + "'");
		}

		if (this.resolver.isMapped(name)) {
			throw new IllegalArgumentException("Mapped property names are not allowed: Property '" + name
					+ "' on bean class '" + bean.getClass() + "'");
		}

		if ((bean instanceof DynaBean)) {
			DynaProperty descriptor = ((DynaBean) bean).getDynaClass().getDynaProperty(name);

			if (descriptor == null) {
				throw new NoSuchMethodException(
						"Unknown property '" + name + "' on dynaclass '" + ((DynaBean) bean).getDynaClass() + "'");
			}

			return ((DynaBean) bean).get(name);
		}

		PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);

		if (descriptor == null) {
			throw new NoSuchMethodException("Unknown property '" + name + "' on class '" + bean.getClass() + "'");
		}

		Method readMethod = getReadMethod(bean.getClass(), descriptor);
		if (readMethod == null) {
			throw new NoSuchMethodException(
					"Property '" + name + "' has no getter method in class '" + bean.getClass() + "'");
		}

		Object value = invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);
		return value;
	}

	public Method getWriteMethod(PropertyDescriptor descriptor) {
		return MethodUtils.getAccessibleMethod(descriptor.getWriteMethod());
	}

	Method getWriteMethod(Class clazz, PropertyDescriptor descriptor) {
		return MethodUtils.getAccessibleMethod(clazz, descriptor.getWriteMethod());
	}

	public boolean isReadable(Object bean, String name) {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		while (this.resolver.hasNested(name)) {
			String next = this.resolver.next(name);
			Object nestedBean = null;
			try {
				nestedBean = getProperty(bean, next);
			} catch (IllegalAccessException e) {
				return false;
			} catch (InvocationTargetException e) {
				return false;
			} catch (NoSuchMethodException e) {
				return false;
			}
			if (nestedBean == null) {
				throw new NestedNullException(
						"Null property value for '" + next + "' on bean class '" + bean.getClass() + "'");
			}

			bean = nestedBean;
			name = this.resolver.remove(name);
		}

		name = this.resolver.getProperty(name);

		if ((bean instanceof WrapDynaBean)) {
			bean = ((WrapDynaBean) bean).getInstance();
		}

		if ((bean instanceof DynaBean)) {
			return ((DynaBean) bean).getDynaClass().getDynaProperty(name) != null;
		}
		try {
			PropertyDescriptor desc = getPropertyDescriptor(bean, name);

			if (desc != null) {
				Method readMethod = getReadMethod(bean.getClass(), desc);
				if (readMethod == null) {
					if ((desc instanceof IndexedPropertyDescriptor))
						readMethod = ((IndexedPropertyDescriptor) desc).getIndexedReadMethod();
					else if ((desc instanceof MappedPropertyDescriptor)) {
						readMethod = ((MappedPropertyDescriptor) desc).getMappedReadMethod();
					}
					readMethod = MethodUtils.getAccessibleMethod(bean.getClass(), readMethod);
				}
				return readMethod != null;
			}
			return false;
		} catch (IllegalAccessException e) {
			return false;
		} catch (InvocationTargetException e) {
			return false;
		} catch (NoSuchMethodException e) {
		}
		return false;
	}

	public boolean isWriteable(Object bean, String name) {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		while (this.resolver.hasNested(name)) {
			String next = this.resolver.next(name);
			Object nestedBean = null;
			try {
				nestedBean = getProperty(bean, next);
			} catch (IllegalAccessException e) {
				return false;
			} catch (InvocationTargetException e) {
				return false;
			} catch (NoSuchMethodException e) {
				return false;
			}
			if (nestedBean == null) {
				throw new NestedNullException(
						"Null property value for '" + next + "' on bean class '" + bean.getClass() + "'");
			}

			bean = nestedBean;
			name = this.resolver.remove(name);
		}

		name = this.resolver.getProperty(name);

		if ((bean instanceof WrapDynaBean)) {
			bean = ((WrapDynaBean) bean).getInstance();
		}

		if ((bean instanceof DynaBean)) {
			return ((DynaBean) bean).getDynaClass().getDynaProperty(name) != null;
		}
		try {
			PropertyDescriptor desc = getPropertyDescriptor(bean, name);

			if (desc != null) {
				Method writeMethod = getWriteMethod(bean.getClass(), desc);
				if (writeMethod == null) {
					if ((desc instanceof IndexedPropertyDescriptor))
						writeMethod = ((IndexedPropertyDescriptor) desc).getIndexedWriteMethod();
					else if ((desc instanceof MappedPropertyDescriptor)) {
						writeMethod = ((MappedPropertyDescriptor) desc).getMappedWriteMethod();
					}
					writeMethod = MethodUtils.getAccessibleMethod(bean.getClass(), writeMethod);
				}
				return writeMethod != null;
			}
			return false;
		} catch (IllegalAccessException e) {
			return false;
		} catch (InvocationTargetException e) {
			return false;
		} catch (NoSuchMethodException e) {
		}
		return false;
	}

	public void setIndexedProperty(Object bean, String name, Object value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		int index = -1;
		try {
			index = this.resolver.getIndex(name);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"Invalid indexed property '" + name + "' on bean class '" + bean.getClass() + "'");
		}

		if (index < 0) {
			throw new IllegalArgumentException(
					"Invalid indexed property '" + name + "' on bean class '" + bean.getClass() + "'");
		}

		name = this.resolver.getProperty(name);

		setIndexedProperty(bean, name, index, value);
	}

	public void setIndexedProperty(Object bean, String name, int index, Object value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if ((name == null) || (name.length() == 0)) {
			if (bean.getClass().isArray()) {
				Array.set(bean, index, value);
				return;
			}
			if ((bean instanceof List)) {
				((List) bean).set(index, value);
				return;
			}
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		if ((bean instanceof DynaBean)) {
			DynaProperty descriptor = ((DynaBean) bean).getDynaClass().getDynaProperty(name);

			if (descriptor == null) {
				throw new NoSuchMethodException(
						"Unknown property '" + name + "' on bean class '" + bean.getClass() + "'");
			}

			((DynaBean) bean).set(name, index, value);
			return;
		}

		PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);

		if (descriptor == null) {
			throw new NoSuchMethodException("Unknown property '" + name + "' on bean class '" + bean.getClass() + "'");
		}

		if ((descriptor instanceof IndexedPropertyDescriptor)) {
			Method writeMethod = ((IndexedPropertyDescriptor) descriptor).getIndexedWriteMethod();

			writeMethod = MethodUtils.getAccessibleMethod(bean.getClass(), writeMethod);
			if (writeMethod != null) {
				Object[] subscript = new Object[2];
				subscript[0] = new Integer(index);
				subscript[1] = value;
				try {
					if (this.log.isTraceEnabled()) {
						String valueClassName = value == null ? "<null>" : value.getClass().getName();

						this.log.trace("setSimpleProperty: Invoking method " + writeMethod + " with index=" + index
								+ ", value=" + value + " (class " + valueClassName + ")");
					}

					invokeMethod(writeMethod, bean, subscript);
				} catch (InvocationTargetException e) {
					if ((e.getTargetException() instanceof IndexOutOfBoundsException)) {
						throw ((IndexOutOfBoundsException) e.getTargetException());
					}

					throw e;
				}

				return;
			}

		}

		Method readMethod = getReadMethod(bean.getClass(), descriptor);
		if (readMethod == null) {
			throw new NoSuchMethodException(
					"Property '" + name + "' has no getter method on bean class '" + bean.getClass() + "'");
		}

		Object array = invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);
		if (!array.getClass().isArray()) {
			if ((array instanceof List)) {
				((List) array).set(index, value);
			} else
				throw new IllegalArgumentException(
						"Property '" + name + "' is not indexed on bean class '" + bean.getClass() + "'");

		} else {
			Array.set(array, index, value);
		}
	}

	public void setMappedProperty(Object bean, String name, Object value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		String key = null;
		try {
			key = this.resolver.getKey(name);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"Invalid mapped property '" + name + "' on bean class '" + bean.getClass() + "'");
		}

		if (key == null) {
			throw new IllegalArgumentException(
					"Invalid mapped property '" + name + "' on bean class '" + bean.getClass() + "'");
		}

		name = this.resolver.getProperty(name);

		setMappedProperty(bean, name, key, value);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setMappedProperty(Object bean, String name, String key, Object value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		if (key == null) {
			throw new IllegalArgumentException(
					"No key specified for property '" + name + "' on bean class '" + bean.getClass() + "'");
		}

		if ((bean instanceof DynaBean)) {
			DynaProperty descriptor = ((DynaBean) bean).getDynaClass().getDynaProperty(name);

			if (descriptor == null) {
				throw new NoSuchMethodException(
						"Unknown property '" + name + "' on bean class '" + bean.getClass() + "'");
			}

			((DynaBean) bean).set(name, key, value);
			return;
		}

		PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);

		if (descriptor == null) {
			throw new NoSuchMethodException("Unknown property '" + name + "' on bean class '" + bean.getClass() + "'");
		}

		if ((descriptor instanceof MappedPropertyDescriptor)) {
			Method mappedWriteMethod = ((MappedPropertyDescriptor) descriptor).getMappedWriteMethod();

			mappedWriteMethod = MethodUtils.getAccessibleMethod(bean.getClass(), mappedWriteMethod);
			if (mappedWriteMethod != null) {
				Object[] params = new Object[2];
				params[0] = key;
				params[1] = value;
				if (this.log.isTraceEnabled()) {
					String valueClassName = value == null ? "<null>" : value.getClass().getName();

					this.log.trace("setSimpleProperty: Invoking method " + mappedWriteMethod + " with key=" + key
							+ ", value=" + value + " (class " + valueClassName + ")");
				}

				invokeMethod(mappedWriteMethod, bean, params);
			} else {
				throw new NoSuchMethodException("Property '" + name + "' has no mapped setter method"
						+ "on bean class '" + bean.getClass() + "'");
			}

		} else {
			Method readMethod = getReadMethod(bean.getClass(), descriptor);
			if (readMethod != null) {
				Object invokeResult = invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);

				if ((invokeResult instanceof Map))
					((Map) invokeResult).put(key, value);
			} else {
				throw new NoSuchMethodException(
						"Property '" + name + "' has no mapped getter method on bean class '" + bean.getClass() + "'");
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void setNestedProperty(Object bean, String name, Object value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		while (this.resolver.hasNested(name)) {
			String next = this.resolver.next(name);
			Object nestedBean = null;
			if ((bean instanceof Map))
				nestedBean = getPropertyOfMapBean((Map) bean, next);
			else if (this.resolver.isMapped(next))
				nestedBean = getMappedProperty(bean, next);
			else if (this.resolver.isIndexed(next))
				nestedBean = getIndexedProperty(bean, next);
			else {
				nestedBean = getSimpleProperty(bean, next);
			}
			if (nestedBean == null) {
				throw new NestedNullException(
						"Null property value for '" + name + "' on bean class '" + bean.getClass() + "'");
			}

			bean = nestedBean;
			name = this.resolver.remove(name);
		}

		if ((bean instanceof Map))
			setPropertyOfMapBean((Map) bean, name, value);
		else if (this.resolver.isMapped(name))
			setMappedProperty(bean, name, value);
		else if (this.resolver.isIndexed(name))
			setIndexedProperty(bean, name, value);
		else
			setSimpleProperty(bean, name, value);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void setPropertyOfMapBean(Map bean, String propertyName, Object value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (this.resolver.isMapped(propertyName)) {
			String name = this.resolver.getProperty(propertyName);
			if ((name == null) || (name.length() == 0)) {
				propertyName = this.resolver.getKey(propertyName);
			}
		}

		if ((this.resolver.isIndexed(propertyName)) || (this.resolver.isMapped(propertyName))) {
			throw new IllegalArgumentException(
					"Indexed or mapped properties are not supported on objects of type Map: " + propertyName);
		}

		bean.put(propertyName, value);
	}

	public void setProperty(Object bean, String name, Object value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		setNestedProperty(bean, name, value);
	}

	public void setSimpleProperty(Object bean, String name, Object value)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (name == null) {
			throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
		}

		if (this.resolver.hasNested(name)) {
			throw new IllegalArgumentException("Nested property names are not allowed: Property '" + name
					+ "' on bean class '" + bean.getClass() + "'");
		}

		if (this.resolver.isIndexed(name)) {
			throw new IllegalArgumentException("Indexed property names are not allowed: Property '" + name
					+ "' on bean class '" + bean.getClass() + "'");
		}

		if (this.resolver.isMapped(name)) {
			throw new IllegalArgumentException("Mapped property names are not allowed: Property '" + name
					+ "' on bean class '" + bean.getClass() + "'");
		}

		if ((bean instanceof DynaBean)) {
			DynaProperty descriptor = ((DynaBean) bean).getDynaClass().getDynaProperty(name);

			if (descriptor == null) {
				throw new NoSuchMethodException(
						"Unknown property '" + name + "' on dynaclass '" + ((DynaBean) bean).getDynaClass() + "'");
			}

			((DynaBean) bean).set(name, value);
			return;
		}

		PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);

		if (descriptor == null) {
			throw new NoSuchMethodException("Unknown property '" + name + "' on class '" + bean.getClass() + "'");
		}

		Method writeMethod = getWriteMethod(bean.getClass(), descriptor);
		if (writeMethod == null) {
			throw new NoSuchMethodException(
					"Property '" + name + "' has no setter method in class '" + bean.getClass() + "'");
		}

		Object[] values = new Object[1];
		values[0] = value;
		if (this.log.isTraceEnabled()) {
			String valueClassName = value == null ? "<null>" : value.getClass().getName();

			this.log.trace("setSimpleProperty: Invoking method " + writeMethod + " with value " + value + " (class "
					+ valueClassName + ")");
		}

		invokeMethod(writeMethod, bean, values);
	}

	@SuppressWarnings("rawtypes")
	private Object invokeMethod(Method method, Object bean, Object[] values)
			throws IllegalAccessException, InvocationTargetException {
		if (bean == null) {
			throw new IllegalArgumentException(
					"No bean specified - this should have been checked before reaching this method");
		}

		try {
			return method.invoke(bean, values);
		} catch (NullPointerException cause) {
			String valueString = "";
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					if (i > 0) {
						valueString = valueString + ", ";
					}
					if (values[i] == null)
						valueString = valueString + "<null>";
					else {
						valueString = valueString + values[i].getClass().getName();
					}
				}
			}
			String expectedString = "";
			Class[] parTypes = method.getParameterTypes();
			if (parTypes != null) {
				for (int i = 0; i < parTypes.length; i++) {
					if (i > 0) {
						expectedString = expectedString + ", ";
					}
					expectedString = expectedString + parTypes[i].getName();
				}
			}
			IllegalArgumentException e = new IllegalArgumentException("Cannot invoke "
					+ method.getDeclaringClass().getName() + "." + method.getName() + " on bean class '"
					+ bean.getClass() + "' - " + cause.getMessage() + " - had objects of type \"" + valueString
					+ "\" but expected signature \"" + expectedString + "\"");

			if (!BeanUtils.initCause(e, cause)) {
				this.log.error("Method invocation failed", cause);
			}
			throw e;
		} catch (IllegalArgumentException cause) {
			String valueString = "";
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					if (i > 0) {
						valueString = valueString + ", ";
					}
					if (values[i] == null)
						valueString = valueString + "<null>";
					else {
						valueString = valueString + values[i].getClass().getName();
					}
				}
			}
			String expectedString = "";
			Class[] parTypes = method.getParameterTypes();
			if (parTypes != null) {
				for (int i = 0; i < parTypes.length; i++) {
					if (i > 0) {
						expectedString = expectedString + ", ";
					}
					expectedString = expectedString + parTypes[i].getName();
				}
			}
			IllegalArgumentException e = new IllegalArgumentException("Cannot invoke "
					+ method.getDeclaringClass().getName() + "." + method.getName() + " on bean class '"
					+ bean.getClass() + "' - " + cause.getMessage() + " - had objects of type \"" + valueString
					+ "\" but expected signature \"" + expectedString + "\"");

			if (!BeanUtils.initCause(e, cause)) {
				this.log.error("Method invocation failed", cause);
			}
			throw e;
		}
	}
}