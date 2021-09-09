package com.utils.bean;

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ContextClassLoaderLocal;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.commons.beanutils.expression.Resolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanUtilsBean {
	private static final ContextClassLoaderLocal BEANS_BY_CLASSLOADER = new ContextClassLoaderLocal() {
		protected Object initialValue() {
			return new BeanUtilsBean();
		}
	};

	private Logger log = LoggerFactory.getLogger(BeanUtils.class);
	private ConvertUtilsBean convertUtilsBean;
	private PropertyUtilsBean propertyUtilsBean;
	private static final Method INIT_CAUSE_METHOD = getInitCauseMethod();

	public static BeanUtilsBean getInstance() {
		return (BeanUtilsBean) BEANS_BY_CLASSLOADER.get();
	}

	public static void setInstance(BeanUtilsBean newInstance) {
		BEANS_BY_CLASSLOADER.set(newInstance);
	}

	public BeanUtilsBean() {
		this(new ConvertUtilsBean(), new PropertyUtilsBean());
	}

	public BeanUtilsBean(ConvertUtilsBean convertUtilsBean) {
		this(convertUtilsBean, new PropertyUtilsBean());
	}

	public BeanUtilsBean(ConvertUtilsBean convertUtilsBean, PropertyUtilsBean propertyUtilsBean) {
		this.convertUtilsBean = convertUtilsBean;
		this.propertyUtilsBean = propertyUtilsBean;
	}

	public Object cloneBean(Object bean)
			throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		if (this.log.isDebugEnabled()) {
			this.log.debug("Cloning bean: " + bean.getClass().getName());
		}
		Object newBean = null;
		if ((bean instanceof DynaBean)) {
			newBean = ((DynaBean) bean).getDynaClass().newInstance();
		} else {
			newBean = bean.getClass().newInstance();
		}
		getPropertyUtils().copyProperties(newBean, bean);
		return newBean;
	}

	public void copyProperties(Object dest, Object orig) throws IllegalAccessException, InvocationTargetException {
		if (dest == null) {
			throw new IllegalArgumentException("No destination bean specified");
		}
		if (orig == null) {
			throw new IllegalArgumentException("No origin bean specified");
		}
		if (this.log.isDebugEnabled()) {
			this.log.debug("BeanUtils.copyProperties(" + dest + ", " + orig + ")");
		}

		if ((orig instanceof DynaBean)) {
			DynaProperty[] origDescriptors = ((DynaBean) orig).getDynaClass().getDynaProperties();
			for (int i = 0; i < origDescriptors.length; i++) {
				String name = origDescriptors[i].getName();

				if ((getPropertyUtils().isReadable(orig, name)) && (getPropertyUtils().isWriteable(dest, name))) {
					Object value = ((DynaBean) orig).get(name);
					copyProperty(dest, name, value);
				}
			}
		} else if ((orig instanceof Map)) {
			Iterator entries = ((Map) orig).entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry entry = (Map.Entry) entries.next();
				String name = (String) entry.getKey();
				if (getPropertyUtils().isWriteable(dest, name)) {
					copyProperty(dest, name, entry.getValue());
				}
			}
		} else {
			PropertyDescriptor[] origDescriptors = getPropertyUtils().getPropertyDescriptors(orig);
			for (int i = 0; i < origDescriptors.length; i++) {
				String name = origDescriptors[i].getName();
				if (!"class".equals(name)) {
					if ((getPropertyUtils().isReadable(orig, name)) && (getPropertyUtils().isWriteable(dest, name))) {
						try {
							Object value = getPropertyUtils().getSimpleProperty(orig, name);
							copyProperty(dest, name, value);
						} catch (NoSuchMethodException e) {
						}
					}
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void copyNotNullProperties(Object dest, Object orig)
			throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
		if (dest == null) {
			throw new IllegalArgumentException("No destination bean specified");
		}
		if (orig == null) {
			throw new IllegalArgumentException("No origin bean specified");
		}
		if (this.log.isDebugEnabled()) {
			this.log.debug("BeanUtils.copyProperties(" + dest + ", " + orig + ")");
		}

		if ((orig instanceof DynaBean)) {
			DynaProperty[] origDescriptors = ((DynaBean) orig).getDynaClass().getDynaProperties();
			for (int i = 0; i < origDescriptors.length; i++) {
				String name = origDescriptors[i].getName();

				if ((getPropertyUtils().isReadable(orig, name)) && (getPropertyUtils().isWriteable(dest, name))) {
					Object value = ((DynaBean) orig).get(name);

					if (value != null)
						copyProperty(dest, name, value);
				}
			}
		} else if ((orig instanceof Map)) {
			Iterator entries = ((Map) orig).entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry entry = (Map.Entry) entries.next();
				String name = (String) entry.getKey();
				if (getPropertyUtils().isWriteable(dest, name)) {
					if (entry.getValue() != null)
						copyProperty(dest, name, entry.getValue());
				}
			}
		} else if ((orig instanceof List)) {
			for (int i = 0; i < ((List) orig).size(); i++) {
				Object newObj = null;
				try {
					newObj = Class.forName(((List) orig).get(i).getClass().getName()).newInstance();
				} catch (Exception e) {
				}

				if (newObj != null) {
					if ("java.lang.String".equals(newObj.getClass().getName())) {
						newObj = ((List) orig).get(i);
					} else {
						copyNotNullProperties(newObj, ((List) orig).get(i));
					}
				}
				((List) dest).add(newObj);
			}

		} else {
			PropertyDescriptor[] origDescriptors = getPropertyUtils().getPropertyDescriptors(orig);
			for (int i = 0; i < origDescriptors.length; i++) {
				String name = origDescriptors[i].getName();
				if (!"class".equals(name)) {
					if ((getPropertyUtils().isReadable(orig, name)) && (getPropertyUtils().isWriteable(dest, name))) {
						try {
							Object value = getPropertyUtils().getSimpleProperty(orig, name);
							if (value != null)
								copyProperty(dest, name, value);
						} catch (NoSuchMethodException e) {
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void copyProperty(Object bean, String name, Object value)
			throws IllegalAccessException, InvocationTargetException {
		if (this.log.isTraceEnabled()) {
			StringBuffer sb = new StringBuffer("  copyProperty(");
			sb.append(bean);
			sb.append(", ");
			sb.append(name);
			sb.append(", ");
			if (value == null) {
				sb.append("<NULL>");
			} else if ((value instanceof String)) {
				sb.append((String) value);
			} else if ((value instanceof String[])) {
				String[] values = (String[]) value;
				sb.append('[');
				for (int i = 0; i < values.length; i++) {
					if (i > 0) {
						sb.append(',');
					}
					sb.append(values[i]);
				}
				sb.append(']');
			} else {
				sb.append(value.toString());
			}
			sb.append(')');
			this.log.trace(sb.toString());
		}

		Object target = bean;
		Resolver resolver = getPropertyUtils().getResolver();
		while (resolver.hasNested(name)) {
			try {
				target = getPropertyUtils().getProperty(target, resolver.next(name));
				name = resolver.remove(name);
			} catch (NoSuchMethodException e) {
				return;
			}
		}
		if (this.log.isTraceEnabled()) {
			this.log.trace("    Target bean = " + target);
			this.log.trace("    Target name = " + name);
		}

		String propName = resolver.getProperty(name);
		Class type = null;
		int index = resolver.getIndex(name);
		String key = resolver.getKey(name);

		if ((target instanceof DynaBean)) {
			DynaClass dynaClass = ((DynaBean) target).getDynaClass();
			DynaProperty dynaProperty = dynaClass.getDynaProperty(propName);
			if (dynaProperty == null) {
				return;
			}
			type = dynaProperty.getType();
		} else {
			PropertyDescriptor descriptor = null;
			try {
				descriptor = getPropertyUtils().getPropertyDescriptor(target, name);
				if (descriptor == null) {
					return;
				}
			} catch (NoSuchMethodException e) {
				return;
			}
			type = descriptor.getPropertyType();
			if (type == null) {
				if (this.log.isTraceEnabled()) {
					this.log.trace("    target type for property '" + propName + "' is null, so skipping ths setter");
				}
				return;
			}
		}
		if (this.log.isTraceEnabled()) {
			this.log.trace("    target propName=" + propName + ", type=" + type + ", index=" + index + ", key=" + key);
		}

		if (index >= 0) {
			value = convert(value, type.getComponentType());
			try {
				getPropertyUtils().setIndexedProperty(target, propName, index, value);
			} catch (NoSuchMethodException e) {
				throw new InvocationTargetException(e, "Cannot set " + propName);
			}
		} else if (key != null) {
			try {
				getPropertyUtils().setMappedProperty(target, propName, key, value);
			} catch (NoSuchMethodException e) {
				throw new InvocationTargetException(e, "Cannot set " + propName);
			}
		} else {
			value = convert(value, type);
			try {
				getPropertyUtils().setSimpleProperty(target, propName, value);
			} catch (NoSuchMethodException e) {
				throw new InvocationTargetException(e, "Cannot set " + propName);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map describe(Object bean) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			return new HashMap();
		}

		if (this.log.isDebugEnabled()) {
			this.log.debug("Describing bean: " + bean.getClass().getName());
		}

		Map description = new HashMap();
		if ((bean instanceof DynaBean)) {
			DynaProperty[] descriptors = ((DynaBean) bean).getDynaClass().getDynaProperties();
			for (int i = 0; i < descriptors.length; i++) {
				String name = descriptors[i].getName();
				description.put(name, getProperty(bean, name));
			}
		} else {
			PropertyDescriptor[] descriptors = getPropertyUtils().getPropertyDescriptors(bean);
			Class clazz = bean.getClass();
			for (int i = 0; i < descriptors.length; i++) {
				String name = descriptors[i].getName();
				if (getPropertyUtils().getReadMethod(clazz, descriptors[i]) != null) {
					description.put(name, getProperty(bean, name));
				}
			}
		}
		return description;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String[] getArrayProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object value = getPropertyUtils().getProperty(bean, name);
		if (value == null) {
			return null;
		}
		if ((value instanceof Collection)) {
			ArrayList values = new ArrayList();
			Iterator items = ((Collection) value).iterator();
			while (items.hasNext()) {
				Object item = items.next();
				if (item == null) {
					values.add((String) null);
				} else {
					values.add(getConvertUtils().convert(item));
				}
			}
			return (String[]) values.toArray(new String[values.size()]);
		}
		if (value.getClass().isArray()) {
			int n = Array.getLength(value);
			String[] results = new String[n];
			for (int i = 0; i < n; i++) {
				Object item = Array.get(value, i);
				if (item == null) {
					results[i] = null;
				} else {
					results[i] = getConvertUtils().convert(item);
				}
			}
			return results;
		}

		String[] results = new String[1];
		results[0] = getConvertUtils().convert(value);
		return results;
	}

	public String getIndexedProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object value = getPropertyUtils().getIndexedProperty(bean, name);
		return getConvertUtils().convert(value);
	}

	public String getIndexedProperty(Object bean, String name, int index)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object value = getPropertyUtils().getIndexedProperty(bean, name, index);
		return getConvertUtils().convert(value);
	}

	public String getMappedProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object value = getPropertyUtils().getMappedProperty(bean, name);
		return getConvertUtils().convert(value);
	}

	public String getMappedProperty(Object bean, String name, String key)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object value = getPropertyUtils().getMappedProperty(bean, name, key);
		return getConvertUtils().convert(value);
	}

	public String getNestedProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object value = getPropertyUtils().getNestedProperty(bean, name);
		return getConvertUtils().convert(value);
	}

	public Object getNestedValue(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return getPropertyUtils().getNestedProperty(bean, name);
	}

	public String getProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return getNestedProperty(bean, name);
	}

	public String getSimpleProperty(Object bean, String name)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object value = getPropertyUtils().getSimpleProperty(bean, name);
		return getConvertUtils().convert(value);
	}

	@SuppressWarnings("rawtypes")
	public void populate(Object bean, Map properties) throws IllegalAccessException, InvocationTargetException {
		if ((bean == null) || (properties == null)) {
			return;
		}
		if (this.log.isDebugEnabled()) {
			this.log.debug("BeanUtils.populate(" + bean + ", " + properties + ")");
		}

		Iterator entries = properties.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			String name = (String) entry.getKey();
			if (name != null) {
				setProperty(bean, name, entry.getValue());
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void setProperty(Object bean, String name, Object value)
			throws IllegalAccessException, InvocationTargetException {
		if (this.log.isTraceEnabled()) {
			StringBuffer sb = new StringBuffer("  setProperty(");
			sb.append(bean);
			sb.append(", ");
			sb.append(name);
			sb.append(", ");
			if (value == null) {
				sb.append("<NULL>");
			} else if ((value instanceof String)) {
				sb.append((String) value);
			} else if ((value instanceof String[])) {
				String[] values = (String[]) value;
				sb.append('[');
				for (int i = 0; i < values.length; i++) {
					if (i > 0) {
						sb.append(',');
					}
					sb.append(values[i]);
				}
				sb.append(']');
			} else {
				sb.append(value.toString());
			}
			sb.append(')');
			this.log.trace(sb.toString());
		}

		Object target = bean;
		Resolver resolver = getPropertyUtils().getResolver();
		while (resolver.hasNested(name)) {
			try {
				target = getPropertyUtils().getProperty(target, resolver.next(name));
				name = resolver.remove(name);
			} catch (NoSuchMethodException e) {
				return;
			}
		}
		if (this.log.isTraceEnabled()) {
			this.log.trace("    Target bean = " + target);
			this.log.trace("    Target name = " + name);
		}

		String propName = resolver.getProperty(name);
		Class type = null;
		int index = resolver.getIndex(name);
		String key = resolver.getKey(name);

		if ((target instanceof DynaBean)) {
			DynaClass dynaClass = ((DynaBean) target).getDynaClass();
			DynaProperty dynaProperty = dynaClass.getDynaProperty(propName);
			if (dynaProperty == null) {
				return;
			}
			type = dynaProperty.getType();
		} else if ((target instanceof Map)) {
			type = Object.class;
		} else if ((target.getClass().isArray()) && (index >= 0)) {
			type = Array.get(target, index).getClass();
		} else {
			PropertyDescriptor descriptor = null;
			try {
				descriptor = getPropertyUtils().getPropertyDescriptor(target, name);
				if (descriptor == null) {
					return;
				}
			} catch (NoSuchMethodException e) {
				return;
			}
			if ((descriptor instanceof MappedPropertyDescriptor)) {
				if (((MappedPropertyDescriptor) descriptor).getMappedWriteMethod() == null) {
					if (this.log.isDebugEnabled()) {
						this.log.debug("Skipping read-only property");
					}
					return;
				}
				type = ((MappedPropertyDescriptor) descriptor).getMappedPropertyType();
			} else if ((index >= 0) && ((descriptor instanceof IndexedPropertyDescriptor))) {
				if (((IndexedPropertyDescriptor) descriptor).getIndexedWriteMethod() == null) {
					if (this.log.isDebugEnabled()) {
						this.log.debug("Skipping read-only property");
					}
					return;
				}
				type = ((IndexedPropertyDescriptor) descriptor).getIndexedPropertyType();
			} else if (key != null) {
				if (descriptor.getReadMethod() == null) {
					if (this.log.isDebugEnabled()) {
						this.log.debug("Skipping read-only property");
					}
					return;
				}
				type = value == null ? Object.class : value.getClass();
			} else {
				if (descriptor.getWriteMethod() == null) {
					if (this.log.isDebugEnabled()) {
						this.log.debug("Skipping read-only property");
					}
					return;
				}
				type = descriptor.getPropertyType();
			}

		}

		Object newValue = null;
		if ((type.isArray()) && (index < 0)) {
			if (value == null) {
				String[] values = new String[1];
				values[0] = null;
				newValue = getConvertUtils().convert(values, type);
			} else if ((value instanceof String)) {
				newValue = getConvertUtils().convert(value, type);
			} else if ((value instanceof String[])) {
				newValue = getConvertUtils().convert((String[]) value, type);
			} else {
				newValue = convert(value, type);
			}
		} else if (type.isArray()) {
			if (((value instanceof String)) || (value == null)) {
				newValue = getConvertUtils().convert((String) value, type.getComponentType());
			} else if ((value instanceof String[])) {
				newValue = getConvertUtils().convert(((String[]) (String[]) value)[0], type.getComponentType());
			} else {
				newValue = convert(value, type.getComponentType());
			}

		} else if ((value instanceof String)) {
			newValue = getConvertUtils().convert((String) value, type);
		} else if ((value instanceof String[])) {
			newValue = getConvertUtils().convert(((String[]) (String[]) value)[0], type);
		} else {
			newValue = convert(value, type);
		}

		try {
			getPropertyUtils().setProperty(target, name, newValue);
		} catch (NoSuchMethodException e) {
			throw new InvocationTargetException(e, "Cannot set " + propName);
		}
	}

	public ConvertUtilsBean getConvertUtils() {
		return this.convertUtilsBean;
	}

	public PropertyUtilsBean getPropertyUtils() {
		return this.propertyUtilsBean;
	}

	public boolean initCause(Throwable throwable, Throwable cause) {
		if ((INIT_CAUSE_METHOD != null) && (cause != null)) {
			try {
				INIT_CAUSE_METHOD.invoke(throwable, new Object[] { cause });
				return true;
			} catch (Throwable e) {
				return false;
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	protected Object convert(Object value, Class type) {
		Converter converter = getConvertUtils().lookup(type);
		if (converter != null) {
			this.log.trace("        USING CONVERTER " + converter);
			return converter.convert(type, value);
		}

		return value;
	}

	@SuppressWarnings("rawtypes")
	private static Method getInitCauseMethod() {
		try {
			Class[] paramsClasses = { Throwable.class };
			return Throwable.class.getMethod("initCause", paramsClasses);
		} catch (NoSuchMethodException e) {
			Logger log = LoggerFactory.getLogger(BeanUtils.class);
			if (log.isWarnEnabled()) {
				log.warn("Throwable does not have initCause() method in JDK 1.3");
			}
			return null;
		} catch (Throwable e) {
			Logger log = LoggerFactory.getLogger(BeanUtils.class);
			if (log.isWarnEnabled()) {
				log.warn("Error getting the Throwable initCause() method", e);
			}
		}
		return null;
	}
}