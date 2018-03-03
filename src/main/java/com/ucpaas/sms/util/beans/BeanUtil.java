package com.ucpaas.sms.util.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class BeanUtil {

	private static Logger logger = LoggerFactory.getLogger(BeanUtil.class);

	/**
	 * 修改Spring的BeanUtils增加日志功能，且只从source复制到（覆盖到）target上,一般用于编辑功能
	 * @param source
	 * @param target
	 * @throws BeansException
	 */
	public static void copyProperties(Object source, Object target) throws BeansException {
		copyProperties(source, target, (String[]) null);
	}

	/**
	 * 修改Spring的BeanUtils增加日志功能，且只从source复制到（覆盖到）target上,一般用于编辑功能
	 * 
	 * @param source
	 * @param target
	 * @param ignoreProperties
	 * @throws BeansException
	 */
	private static void copyProperties(Object source, Object target, String... ignoreProperties) throws BeansException {

		Assert.notNull(source, "Source must not be null");
		Assert.notNull(target, "Target must not be null");

		Class<?> actualEditable = target.getClass();
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : null);

		for (PropertyDescriptor targetPd : targetPds) {
			Method writeMethod = targetPd.getWriteMethod();
			if (writeMethod != null && (ignoreList == null || !ignoreList.contains(targetPd.getName()))) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
				if (sourcePd != null) {
					Method readMethod = sourcePd.getReadMethod();
					if (readMethod != null && ClassUtils.isAssignable(writeMethod.getParameterTypes()[0],
							readMethod.getReturnType())) {
						try {
							if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
								readMethod.setAccessible(true);
							}
							Object newValue = readMethod.invoke(source);
							if(newValue==null)
								continue;
							if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
								writeMethod.setAccessible(true);
							}
							Object oldValue = targetPd.getReadMethod().invoke(target);
//							logger.debug("修改对象{}的{}值,从{}修改为{}",actualEditable.getSimpleName(),targetPd.getName(),JacksonUtil.toJSON(oldValue),JacksonUtil.toJSON(newValue));
							writeMethod.invoke(target, newValue);
						} catch (Throwable ex) {
							throw new FatalBeanException(
									"Could not copy property '" + targetPd.getName() + "' from source to target", ex);
						}
					}
				}
			}
		}
	}

	/**
	 * Retrieve the JavaBeans {@code PropertyDescriptor}s of a given class.
	 * 
	 * @param clazz
	 *            the Class to retrieve the PropertyDescriptors for
	 * @return an array of {@code PropertyDescriptors} for the given class
	 * @throws BeansException
	 *             if PropertyDescriptor look fails
	 */
	public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws BeansException {
		CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
		return cr.getPropertyDescriptors();
	}

	/**
	 * Retrieve the JavaBeans {@code PropertyDescriptors} for the given
	 * property.
	 * 
	 * @param clazz
	 *            the Class to retrieve the PropertyDescriptor for
	 * @param propertyName
	 *            the name of the property
	 * @return the corresponding PropertyDescriptor, or {@code null} if none
	 * @throws BeansException
	 *             if PropertyDescriptor lookup fails
	 */
	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) throws BeansException {

		CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
		return cr.getPropertyDescriptor(propertyName);
	}
}
