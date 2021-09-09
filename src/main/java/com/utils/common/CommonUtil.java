package com.utils.common;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.LoggerFactory;

import com.bizentro.unimes.common.message.CustomedFields;
import com.bizentro.unimes.common.message.Message;
import com.bizentro.unimes.common.message.MessageSet;
import com.bizentro.unimes.common.message.RuleMessage;
import com.bizentro.unimes.common.util.MesException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utils.bean.BeanUtils;
import com.utils.bean.BeanUtilsBean;

public class CommonUtil {

//	private static final String TOKEN = ",";
	private static final Integer MODIFY = Integer.valueOf(1);
	public static final String RULEMESSAGETYPE_RULEMESSAGE = "RULEMESSAGE";
	public static final String RULEMESSAGETYPE_DOCUMENT = "DOCUMENT";
	public static final String RULEMESSAGETYPE_STRING = "STRING";
	static int sNumber = 0; // 구동 시 숫자를 기억

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : getSeqNumber
	  * 2.Author    : user
	  * 3.CreateDate: 2021. 6. 25오전 10:34:37
	  * 4.UpdateDate:
	  * 5.Comment   :
	 * </PRE>
	 */
	private static int getSeqNumber() {
		sNumber++; // 숫자를 1씩 증가

		// 최대 숫자를 999999로 설정 그 이상일 경우 다시 1로 돌아감
		if (sNumber > 999999) {
			sNumber = 1;
		}

		return sNumber;
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : isEmpty
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:29:28
	  * 4.UpdateDate:
	  * 5.Comment   : String 이 empty (null or length 가 0) 인지를 체크<br>
	  *  			  empty 이면 true, 아니면 false 리턴
	 * </PRE>
	 */
	public static boolean isEmpty(String s) {
		return (s == null) || (s.length() == 0);
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : isEmpty
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:29:55
	  * 4.UpdateDate:
	  * 5.Comment   : String 이 empty (null or length 가 0) 인지를 체크
	  * 			  empty 가 아니면 true, 아니면(Empty 면) false 리턴
	 * </PRE>
	 */
	public static boolean isEmpty(Integer i) {
		return i == null;
	}

	public static boolean isEmpty(Float f) {
		return f == null;
	}

	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}

	public static boolean isNotEmpty(Integer i) {
		return !isEmpty(i);
	}

	public static boolean isNotEmpty(Float f) {
		return !isEmpty(f);
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : isCollectionEmpty
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:31:47
	  * 4.UpdateDate:
	  * 5.Comment   : Collection 객체 (List, Map 등)가 empty(null 또는 size 가 0) 이면 true, 아니면 false 리턴
	 * </PRE>
	 */
	public static boolean isCollectionEmpty(Object obj) {
		if (obj == null)
			return true;

		if ((obj instanceof List)) {
			if (((List<?>) obj).size() == 0)
				return true;
		} else if ((obj instanceof Map)) {
			if (((Map<?, ?>) obj).isEmpty())
				return true;
		}

		return false;
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : isCollectionNotEmpty
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:32:25
	  * 4.UpdateDate:
	  * 5.Comment   : Collection 객체 (List, Map 등)가 empty(null 또는 size 가 0) 이면 false, 아니면 true 리턴
	 * </PRE>
	 */
	public static boolean isCollectionNotEmpty(Object obj) {
		return !isCollectionEmpty(obj);
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : isNumeric
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:51:57
	  * 4.UpdateDate:
	  * 5.Comment   : String 이 유효한 숫자인지를 확인
	  *               String 이 empty 이거나 빈 문자열인경우 false 리턴
	  *               유효한 숫자가 아닌 경우 false 리턴
	 * </PRE>
	 */
	public static boolean isNumeric(String str) {
		if (str == null)
			return false;

		int sz = str.length();

		if (sz == 0)
			return false;

		for (int i = 0; i < sz; i++) {
			if (!Character.isDigit(str.charAt(i)))
				return false;
		}
		return true;
	}

	public static boolean isNumericWithRegex(String str) {
		return Pattern.matches("[0-9]+", str);
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : string2Integer
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:41:02
	  * 4.UpdateDate:
	  * 5.Comment   : String 을 Integer 로 변환 
	  *               null 이면 null 리턴
	 * </PRE>
	 */
	public static Integer string2Integer(String s) {
		return string2Integer(s, null);
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : string2Float
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:41:22
	  * 4.UpdateDate:
	  * 5.Comment   : String 을 Float 으로 변환 
	  *               null 이면 null 리턴
	 * </PRE>
	 */
	public static Float string2Float(String s) {
		return string2Float(s, null);
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : string2Boolean
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:41:59
	  * 4.UpdateDate:
	  * 5.Comment   : String 을 Bolean 으로 변환
	  *               null 이면 null 리턴
	 * </PRE>
	 */
	public static Boolean string2Boolean(String s) {
		return string2Boolean(s, null);
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : string2Integer
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:42:21
	  * 4.UpdateDate:
	  * 5.Comment   : String 을 Integer 로 변환
	  *               empty 면 넘어온 기본값으로 설정
	 * </PRE>
	 */
	public static Integer string2Integer(String s, Integer di) {
		return Integer.valueOf(isEmpty(s) ? di.intValue() : Integer.parseInt(s));
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : string2Float
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:42:48
	  * 4.UpdateDate:
	  * 5.Comment   : String 을 Float 으로 변환
	  *               empty 면 넘어온 기본값으로 설정
	 * </PRE>
	 */
	public static Float string2Float(String s, Float df) {
		return Float.valueOf(isEmpty(s) ? df.floatValue() : Float.parseFloat(s));
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : string2Boolean
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:43:15
	  * 4.UpdateDate:
	  * 5.Comment   : String 을 Boolean 으로 변환
	  *               "TRUE", "true", "T" : true
	  *               "FALSE", "false", "F" : false 위의 경우가 아닌 경우 null
	  *               만약 위의 값이 아니면 넘어온 기본 Boolean 값 리턴
	 * 
	 * </PRE>
	 */
	public static Boolean string2Boolean(String s, Boolean db) {
		if (isEmpty(s))
			return db;

		s = s.toUpperCase();

		if (("Y".equals(s)) || ("T".equals(s)) || ("TRUE".equals(s))) {
			return Boolean.valueOf(true);
		}
		if (("N".equals(s)) || ("F".equals(s)) || ("FALSE".equals(s))) {
			return Boolean.valueOf(false);
		}

		return db;
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : string2Date
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:44:03
	  * 4.UpdateDate:
	  * 5.Comment   : String 을 SimpleDateFormat String 에 맞게 String 으로 리턴
	  *               String 이 empty 이면 null 리턴
	  *               format String 이 존재하지 않으면 에러 발생
	  *               Simple DateFormat 형식에 맞지 않는 것은 일반 exception exception으로 처리
	 * </PRE>
	 */
	public static Date string2Date(String s, String fs) throws Exception {
		if (isEmpty(s))
			return null;

		if (isEmpty(fs)) {
			throw new MesException("NotFound", "Conversion Date Format String");
		}

		SimpleDateFormat sdf = new SimpleDateFormat(fs);

		return sdf.parse(s);
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : integer2String
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:47:05
	  * 4.UpdateDate:
	  * 5.Comment   : Integer 값을 String 으로 변환, null 이면 "" 리턴
	 * </PRE>
	 */
	public static String integer2String(Integer i) {
		return i == null ? "" : i.toString();
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : float2String
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:47:24
	  * 4.UpdateDate:
	  * 5.Comment   : Float 값을 String 으로 변환,  null 이면 "" 리턴
	 * </PRE>
	 */
	public static String float2String(Float f) {
		return f == null ? "" : f.toString();
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : float2Float
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:48:31
	  * 4.UpdateDate:
	  * 5.Comment   : 처음 인자값이 0이면 두번째 인자값으로 변환
	 * </PRE>
	 */
	public static float float2Float(float i, float defaultValue) throws Exception {
		return i == 0.0f ? defaultValue : i;
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : boolean2String
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:48:55
	  * 4.UpdateDate:
	  * 5.Comment   : Boolean 값을 String 으로 변환 , null 이면 "" 리턴
	 * </PRE>
	 */
	public static String boolean2String(Boolean b) {
		return b == null ? "" : b.toString();
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : long2String
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:49:18
	  * 4.UpdateDate:
	  * 5.Comment   : Long 값을 String 으로 변환, null 이면 "" 리턴
	 * </PRE>
	 */
	public static String long2String(Long l) throws Exception {
		return l == null ? "" : l.toString();
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : date2String
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:49:36
	  * 4.UpdateDate:
	  * 5.Comment   : Date 를 SimpleDateFormat String 에 맞게 String 으로 리턴
	  *               Date 가 null 이면 null 리턴
	  *               format String 이 존재하지 않으면 에러 발생
	 * </PRE>
	 */
	public static String date2String(Date d, String fs) throws Exception {
		if (d == null)
			return null;

		if (isEmpty(fs)) {
			// [{0}] (이)가 존재하지 않습니다.
			throw new MesException("NotFound", "Conversion Date Format String");
		}

		SimpleDateFormat sdf = new SimpleDateFormat(fs);

		return sdf.format(d);
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : empty2String
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:46:22
	  * 4.UpdateDate:
	  * 5.Comment   : String 값이 Empty 이면 넘어온 기본값으로 변환, empty 가 아니면 넘어온 값 그대로 리턴
	 * </PRE>
	 */
	public static String empty2String(String s, String ds) {
		return isEmpty(s) ? ds : s;
	}

	public static Float empty2Float(Float f, Float df) {
		return (f == null) || (f.floatValue() == 0.0F) ? df : f;
	}

	public static Integer empty2Integer(Integer i, Integer di) {
		return (i == null) || (i.intValue() == 0) ? di : i;
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : null2Integer
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:45:11
	  * 4.UpdateDate:
	  * 5.Comment   : Integer 값이 null 이면 넘어온 기본값으로 변환 , null 이 아니면 넘어온 값 그대로 리턴
	 * </PRE>
	 */
	public static Integer null2Integer(Integer i, Integer di) {
		return i == null ? di : i;
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : null2Float
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:45:28
	  * 4.UpdateDate:
	  * 5.Comment   : Float 값이 null 이면 넘어온 기본값으로 변환, null 이 아니면 넘어온 값 그대로 리턴
	 * </PRE>
	 */
	public static Float null2Float(Float f, Float df) {
		return f == null ? df : f;
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : null2Boolean
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:45:49
	  * 4.UpdateDate:
	  * 5.Comment   : Boolean 값이 null 이면 넘어온 기본값으로 변환, null 이 아니면 넘어온 값 그대로 리턴
	 * </PRE>
	 */
	public static Boolean null2Boolean(Boolean b, Boolean db) {
		return b == null ? db : b;
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : null2Empty
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:46:46
	  * 4.UpdateDate:
	  * 5.Comment   : String 값이 null 이면 "" 리턴
	 * </PRE>
	 */
	public static String null2Empty(String s) {
		return s == null ? "" : s;
	}

	public static BigDecimal float2BigDecimal(Float f) {
		if (f == null)
			return new BigDecimal(0);

		return new BigDecimal(f.toString());
	}

	public static BigDecimal integer2BigDecimal(Integer i) {
		if (i == null)
			return new BigDecimal(0);

		return new BigDecimal(i.intValue());
	}

	public static void addList2List(List<Message> targetList, List<Message> sourceList) {
		for (Message message : sourceList) {
			targetList.add(message);
		}
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : checkMandatory
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:30:51
	  * 4.UpdateDate:
	  * 5.Comment   : 필수 입력 항목 체크<br> 
	  * 			  넘어온 obj 가 empty 이면 fieldName 은 필수 인데 없다는 MesException 리턴<br>
	  * 			 String 인 경우는 length 가 0 이면 값이 없다고 판단함<br>
	 * </PRE>
	 */
	public static void checkMandatory(Object value, String fieldName) throws Exception {
		if (value == null) {
			throw new MesException("EmptyMandatory", fieldName);
		}

		if ((value instanceof String)) {
			if (((String) value).length() == 0) {
				throw new MesException("EmptyMandatory", fieldName);
			}
		}
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : getField
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:36:50
	  * 4.UpdateDate:
	  * 5.Comment   : 특정 object 의 field 의 값을 가져온다. 가져올때 해당 object 의 getter 함수를 사용한다.
	 * </PRE>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object getField(Object obj, Field field) throws Exception {
		Method getterMethod = null;
		Object tempValue = null;
		Object newValue = null;

		// field 의 generic string 을 가져온다.
		String genericTypeStr = field.toGenericString();

		if ((genericTypeStr.indexOf("static") != -1) || (genericTypeStr.indexOf("final") != -1)) {
			return null;
		}

		// getter method 를 가져온다.
		try {
			getterMethod = obj.getClass().getMethod(getMethodName(field.getName(), "get"), new Class[0]);

			tempValue = getterMethod.invoke(obj, new Object[0]);
		} catch (Exception e) {
		}

		// 값이 없으면 tempValue에 class를 설정함
		if (tempValue != null) {
			if (genericTypeStr.indexOf("private java.lang.String") != -1) {
				newValue = new String((String) tempValue);
			} else if (genericTypeStr.indexOf("private java.lang.Integer") != -1) {
				newValue = new Integer(((Integer) tempValue).intValue());
			} else if (genericTypeStr.indexOf("private java.lang.Float") != -1) {
				newValue = new Float(((Float) tempValue).floatValue());
			} else if (genericTypeStr.indexOf("private java.lang.Double") != -1) {
				newValue = new Double(((Double) tempValue).floatValue());
			} else if (genericTypeStr.indexOf("private java.util.Date") != -1) {
				newValue = new Date(((Date) tempValue).getTime());
			} else if (genericTypeStr.indexOf("private java.lang.Boolean") != -1) {
				newValue = new Boolean(((Boolean) tempValue).booleanValue());
			} else if (genericTypeStr.indexOf("private java.lang.Long") != -1) {
				newValue = new Long(((Long) tempValue).longValue());
			} else if (genericTypeStr.indexOf("private java.sql.Timestamp") != -1) {
				newValue = new Timestamp(((Timestamp) tempValue).getTime());
				((Timestamp) newValue).setNanos(((Timestamp) tempValue).getNanos());
			} else if ((genericTypeStr.indexOf("private java.util.List") != -1)
					|| (genericTypeStr.indexOf("private java.util.ArrayList") != -1)) {
				newValue = new ArrayList<Object>();

				for (int i = 0; i < ((List<?>) tempValue).size(); i++) {
					Object newObj = Class.forName(((List<?>) tempValue).get(i).getClass().getName()).newInstance();
					if ("java.lang.String".equals(newObj.getClass().getName())) {
						newObj = ((List<?>) tempValue).get(i);
					} else {
						copyValue(newObj, ((List<?>) tempValue).get(i));
					}
					((List) newValue).add(newObj);
				}
			} else if ((genericTypeStr.indexOf("private java.util.Map") != -1)
					|| (genericTypeStr.indexOf("private java.util.HashMap") != -1)) {
				newValue = new HashMap<Object, Object>();

				Iterator<?> list = ((Map<?, ?>) tempValue).keySet().iterator();

				while (list.hasNext()) {
					Object name = list.next();
					Object value = ((Map<?, ?>) tempValue).get(name);

					Object _name = Class.forName(name.getClass().getName()).newInstance();

					Object _value = null;
					try {
						_value = Class.forName(value.getClass().getName()).newInstance();
					} catch (Exception e) {
					}

					if ("java.lang.String".equals(_name.getClass().getName())) {
						_name = name;
					} else {
						copyValue(_name, name);
					}

					if (_value != null) {
						if ("java.lang.String".equals(_value.getClass().getName())) {
							_value = value;
						} else {
							copyValue(_value, value);
						}
					}
					((Map) newValue).put(_name, _value);
				}

			} else {
				try {
					newValue = Class.forName(tempValue.getClass().getName()).newInstance();
					copyValue(newValue, tempValue);
				} catch (Exception e) {
				}
			}

		}

		return newValue;
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : copyValue
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:34:06
	  * 4.UpdateDate:
	  * 5.Comment   : 원본 객체를 대상 객체로 복사
	 * </PRE>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void copyValue(Object targetObj, Object sourceObj) throws Exception {
		// 원본 객체와 대상 객체가 존재하는 지 확인한다.
		// 둘다 필수로 존재해야 하기 때문에 checkMandatory를 수행한다.
		if (targetObj == null) {
			throw new MesException("EmptyMandatory", "Target Object");
		}
		if (sourceObj == null) {
			throw new MesException("EmptyMandatory", "Source Object");
		}

		Class<? extends Object> target = targetObj.getClass();

		String targetName = target.getName();

		if (("java.util.List".equals(targetName)) || ("java.util.ArrayList".equals(targetName))) {
			for (int i = 0; i < ((List<?>) sourceObj).size(); i++) {
				Object newObj = null;
				try {
					newObj = Class.forName(((List<?>) sourceObj).get(i).getClass().getName()).newInstance();
				} catch (Exception e) {
				}

				if (newObj != null) {
					if ("java.lang.String".equals(newObj.getClass().getName())) {
						newObj = ((List<?>) sourceObj).get(i);
					} else {
						copyValue(newObj, ((List<?>) sourceObj).get(i));
					}
				}
				((List) targetObj).add(newObj);
			}
		} else if (("com.bizentro.unimes.common.message.CustomedFields".equals(targetName))
				|| ("java.util.Map".equals(targetName)) || ("java.util.HashMap".equals(targetName))) {
			Iterator<?> list = ((Map<?, ?>) sourceObj).keySet().iterator();

			while (list.hasNext()) {
				Object name = list.next();
				Object value = ((Map<?, ?>) sourceObj).get(name);

				Object _name = Class.forName(name.getClass().getName()).newInstance();
				Object _value = null;
				try {
					_value = Class.forName(value.getClass().getName()).newInstance();
				} catch (Exception e) {
				}

				if ("java.lang.String".equals(_name.getClass().getName())) {
					_name = name;
				} else {
					copyValue(_name, name);
				}

				if (_value != null) {
					if ("java.lang.String".equals(_value.getClass().getName())) {
						_value = value;
					} else {
						copyValue(_value, value);
					}
				}
				((Map) targetObj).put(_name, _value);
			}

		} else {
			// 대상 객체의 선언되어 있는 필드에 값을 설정 한다.
			for (Field field : targetObj.getClass().getDeclaredFields()) {
				setField(targetObj, field, getField(sourceObj, field));
			}

			for (Field field : targetObj.getClass().getSuperclass().getDeclaredFields()) {
				setField(targetObj, field, getField(sourceObj, field));
			}
		}
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : fieldExist
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:32:59
	  * 4.UpdateDate:
	  * 5.Comment   : 해당 객체에 주어진 필드가 존재하는 여부를 true/false 로 리턴한다.
	 * </PRE>
	 */
	public static boolean fieldExist(Object obj, String name) {
		// 대상 객체의 선언되어 있는 필드에 값을 설정 한다.
		for (Field field : obj.getClass().getDeclaredFields()) {
			if (field.getName().equalsIgnoreCase(name))
				return true;

		}

		// 대상 객체의 슈퍼클래스의 필드에 값을 설정 한다.
		// 이 이유는 모든 hibernate 객체에서는 Message 를 상속받기 때문이다.
		Class<?> source = obj.getClass().getSuperclass();

		Field[] fields = obj.getClass().getSuperclass().getDeclaredFields();
		while (true) {
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase(name))
					return true;
			}

			Class<?> parentClass = source.getSuperclass();

			if ((parentClass == null) || (parentClass.getSuperclass() == null))
				break;
			fields = parentClass.getDeclaredFields();
			source = parentClass.getSuperclass();
		}

		return false;
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : getValue
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:40:02
	  * 4.UpdateDate:
	  * 5.Comment   : object(message, messageset) 에서 name 의 값을 찾아 리턴한다
	 * </PRE>
	 */
	public static Object getValue(Object obj, String name) throws Exception {
		// 값을 가져올 object 는 필수 항목임
		checkMandatory(obj, "Getting Object");

		Object value = null;
		try {
			value = BeanUtils.getValue(obj, name);
		} catch (Exception e) {
		}

		return value;
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : getMethodName
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:38:52
	  * 4.UpdateDate:
	  * 5.Comment   : getter / setter 메소드 명을 가져온다.
	  *               methodType + field String 앞글자 대문자로 변경
	 * </PRE>
	 */
	public static String getMethodName(String field, String methodType) {
		return methodType + field.substring(0, 1).toUpperCase() + field.substring(1);
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : setField
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:38:10
	  * 4.UpdateDate:
	  * 5.Comment   : 특정 object 의 field 의 값을 넘어온 value 로 설정한다. 설정할때 해당 object 의 setter 함수를 사용한다.
	 * </PRE>
	 */
	private static void setField(Object obj, Field field, Object value) throws Exception {
		// setting 할 object 는 필수항목임
		checkMandatory(obj, "Setting Object");

		Method setterMethod = null;
		try {
			Method[] methods = obj.getClass().getMethods();

			if (value == null) {
				return;
			}

			for (Method method : methods) {
				if (getMethodName(field.getName(), "set").equals(method.getName())) {
					setterMethod = method;
					break;
				}

			}

			if ((value instanceof Class)) {
				setterMethod.invoke(obj, new Object[] { null });
			} else {
				try {
					setterMethod.invoke(obj, new Object[] { value });
				} catch (Exception e) {
					if (setterMethod != null)
						System.out.println(setterMethod.getName());
					throw e;
				}
			}

		} catch (Exception e) {
			LoggerFactory.getLogger("CORE_TRACE").error(e.getMessage(), e);
		}
	}

	private static void setFieldWithNull(Object obj, Field field, Object value) throws Exception {
		checkMandatory(obj, "Setting Object");

		String tempFieldName = field.getName();

		if (("serialVersionUID".equalsIgnoreCase(tempFieldName)) || ("id".equalsIgnoreCase(tempFieldName))
				|| ("createUser".equalsIgnoreCase(tempFieldName)) || ("createTime".equalsIgnoreCase(tempFieldName)))
			return;

		Method setterMethod = null;
		try {
			Method[] methods = obj.getClass().getMethods();

			for (Method method : methods) {
				if (getMethodName(field.getName(), "set").equals(method.getName())) {
					setterMethod = method;
					break;
				}

			}

			if ((value instanceof Class)) {
				setterMethod.invoke(obj, new Object[] { null });
			} else {
				setterMethod.invoke(obj, new Object[] { value });
			}
		} catch (Exception e) {
			LoggerFactory.getLogger("CORE_TRACE").error(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : setValue
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:39:30
	  * 4.UpdateDate:
	  * 5.Comment   : object 의 name(fieldName) 값에 value 를 설정한다.
	 * </PRE>
	 */
	public static void setValue(Object obj, String fieldName, Object value) throws Exception {
		try {
			// 대상 객체의 슈퍼클래스의 필드에 값을 설정한다
			Class<?> target = obj.getClass().getSuperclass();
			Field[] fields = obj.getClass().getSuperclass().getDeclaredFields();
			while (true) {
				for (Field field : fields) {
					if (field.getName().equalsIgnoreCase(fieldName)) {
						setField(obj, field, value);

						return;
					}
				}

				Class<?> parentClass = target.getSuperclass();
				if ((parentClass == null) || (parentClass.getSuperclass() == null))
					break;
				fields = parentClass.getDeclaredFields();
				target = parentClass.getSuperclass();
			}

			for (Field field : obj.getClass().getDeclaredFields()) {
				if (field.getName().equalsIgnoreCase(fieldName)) {
					setField(obj, field, value);

					return;
				}
			}
		} catch (Exception e) {
			LoggerFactory.getLogger("CORE_TRACE").error(e.getMessage(), e);
		}
	}

	private static void setValueWithNull(Object obj, String fieldName, Object value) throws Exception {
		try {
			Class<?> target = obj.getClass().getSuperclass();
			Field[] fields = obj.getClass().getSuperclass().getDeclaredFields();
			while (true) {
				for (Field field : fields) {
					if ((field.getName().equalsIgnoreCase(fieldName)) && (isOverrideEnableField(fieldName, value))) {
						setFieldWithNull(obj, field, value);

						return;
					}
				}

				Class<?> parentClass = target.getSuperclass();
				if ((parentClass == null) || (parentClass.getSuperclass() == null))
					break;
				fields = parentClass.getDeclaredFields();
				target = parentClass.getSuperclass();
			}

			for (Field field : obj.getClass().getDeclaredFields()) {
				if (field.getName().equalsIgnoreCase(fieldName)) {
					setFieldWithNull(obj, field, value);
					break;
				}

			}

		} catch (Exception e) {
			LoggerFactory.getLogger("CORE_TRACE").error(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : setOverrideMessage
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:57:14
	  * 4.UpdateDate:
	  * 5.Comment   : Message 에 overideMessage가 존재하면,
	  *               해당 key 값을 가지고 value 값을 Message field 에 설정한다.
	 * </PRE>
	 */
	public static void setOverrideMessage(Message msg) throws Exception {
		// 값이 존재하면
		if (msg.getOverridesCount() > 0) {
			CustomedFields overrides = msg.getOverrides();

			Iterator<?> list = overrides.keySet().iterator();

			// 존재하는 갯수 만큼 설정 함
			while (list.hasNext()) {
				String fieldName = (String) list.next();

				Field field = getClassField(msg, fieldName);

				// 존재 하지 않는 필드인 경우 에러.
				if (field == null) {
					throw new MesException("InvalidValue", new String[] { msg.getClass().getSimpleName(), fieldName });
				}

				// String 값을 실제 필드의 class 타입으로 변환.
				Object obj = string2Generic(field, (String) overrides.get(fieldName));

				setValueWithNull(msg, fieldName, obj);
			}
		}
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : getClassField
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:55:44
	  * 4.UpdateDate:
	  * 5.Comment   : 객체내에 필드값을 가져온다.
	 * </PRE>
	 */
	public static Field getClassField(Object obj, String name) {
		// 대상 객체의 선언되어 있는 필드에 값을 설정 한다.
		for (Field field : obj.getClass().getDeclaredFields()) {
			if (field.getName().equalsIgnoreCase(name))
				return field;

		}

		// 대상 객체의 슈퍼클래스의 필드에 값을 설정 한다.
		// 이 이유는 모든 hibernate 객체에서는 Message 를 상속받기 때문이다.
		Class<?> source = obj.getClass().getSuperclass();

		Field[] fields = obj.getClass().getSuperclass().getDeclaredFields();
		while (true) {
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase(name))
					return field;
			}

			Class<?> parentClass = source.getSuperclass();

			if ((parentClass == null) || (parentClass.getSuperclass() == null))
				break;
			fields = parentClass.getDeclaredFields();
			source = parentClass.getSuperclass();
		}

		return null;
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : updateList
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 3:59:20
	  * 4.UpdateDate:
	  * 5.Comment   : field 와 fieldName이 매칭되는 필드값을 넘어온 값으로 교체한다. (list 전체)
	 * </PRE>
	 */
	public static void updateList(List<Message> list, String fieldName, Object value) throws Exception {
		// list의 Message 타입은 하나밖에 없으므로, 첫번째 값을 tempMsg로 가져온다.
		Object tempMsg = list.get(0);

		Field updateField = null;

		// 본인의 클래스에서 매칭되는 필드를 찾는다.
		for (Field field : tempMsg.getClass().getDeclaredFields()) {
			if (field.getName().equalsIgnoreCase(fieldName)) {
				updateField = field;
				break;
			}
		}

		// update Field 가 null 이면
		if (updateField == null) {
			// 슈퍼클래스에서 필드를 한번 더 찾는다.
			for (Field field : tempMsg.getClass().getSuperclass().getDeclaredFields()) {
				if (field.getName().toUpperCase().equals(fieldName.toUpperCase())) {
					updateField = field;
					break;
				}
			}
		}

		// updateField가 존재하면, message list 의 field 값을 교체한다.
		if (updateField != null) {
			for (Message msg : list) {
				CommonUtil.setField(msg, updateField, value);
			}
		}
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : setDefaultValues
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 4. 29오후 4:02:42
	  * 4.UpdateDate:
	  * 5.Comment   : default value 를 설정한다
	 * </PRE>
	 */
	public static void setDefaultValues(String apiName, MessageSet msgSet) throws Exception {
		for (Message msg : msgSet.getList()) {
			msg.setExecuteService(CommonUtil.empty2String(msg.getExecuteService(), apiName));
			msg.setOriginalExecuteService(apiName);

			for (Field field : msgSet.getClass().getDeclaredFields()) {
				if (!fieldExist(msg, field.getName()))
					continue;

				if (CommonUtil.getValue(msg, field.getName()) == null) {
					CommonUtil.setField(msg, field, CommonUtil.getValue(msgSet, field.getName()));
				}
			}
		}
	}

	private static Object string2Generic(Field field, String value) throws Exception {
		Type type = field.getGenericType();

		if (isEmpty(value)) {
			return null;
		}
		if ("class java.lang.String".equals(type.toString())) {
			return value;
		}
		if ("class java.lang.Integer".equals(type.toString())) {
			return new Integer(value);
		}
		if ("class java.lang.Float".equals(type.toString())) {
			return new Float(value);
		}
		if ("class java.util.Date".equals(type.toString())) {
			return string2Date(value, "yyyyMMddHHmmss");
		}
		if ("class java.lang.Boolean".equals(type.toString())) {
			return new Boolean(value);
		}
		if ("class java.lang.Long".equals(type.toString())) {
			return new Long(value);
		}
		if ("class java.lang.Double".equals(type.toString())) {
			return new Double(value);
		}

		return null;
	}

	public static void makeOverrideSet(Message msg, String fields) throws Exception {
		if (isEmpty(fields)) {
			throw new MesException("ExceptionFieldNameIsEmpty", new String[0]);
		}

		if (fields.equals("*")) {
			fields = "";
		}

		List<Message> msgList = new ArrayList<Message>();
		msgList.add(msg);

		makeOverrideSet(msgList, fields, false);
	}

	public static void makeOverrideSet(List<Message> msgList, String fields) throws Exception {
		if (isEmpty(fields)) {
			throw new MesException("ExceptionFieldNameIsEmpty", new String[0]);
		}

		if (fields.equals("*")) {
			fields = "";
		}

		makeOverrideSet(msgList, fields, false);
	}

	public static void makeWithoutOverrideSet(Message msg, String fields) throws Exception {
		if (isEmpty(fields)) {
			throw new MesException("ExceptionFieldNameIsEmpty", new String[0]);
		}

		List<Message> msgList = new ArrayList<Message>();
		msgList.add(msg);

		makeOverrideSet(msgList, fields, true);
	}

	public static void makeWithoutOverrideSet(List<Message> msgList, String fields) throws Exception {
		if (isEmpty(fields)) {
			throw new MesException("ExceptionFieldNameIsEmpty", new String[0]);
		}

		makeOverrideSet(msgList, fields, true);
	}

	private static void makeOverrideSet(List<Message> msgList, String fields, boolean isWithout) throws Exception {
//		String requestTypeFieldName = "requestType";
		String[] arrFieldNames = null;
		String fieldsInMsg = "";
		String fieldsInVO = "";

		if (isCollectionNotEmpty(msgList)) {
			for (Message msgObj : msgList) {
				String className = msgObj.getClass().getName();
				Class<?> msgClass = Class.forName(className);
				Method getRequestTypeMethod = msgClass.getMethod(getMethodName("requestType", "get"), new Class[0]);

				Integer requestType = (Integer) getRequestTypeMethod.invoke(msgObj, new Object[0]);

				if (MODIFY.equals(requestType)) {
					fieldsInMsg = getArrFieldNames(msgObj.getClass().getSuperclass(), fields, isWithout);
					fieldsInVO = getArrFieldNames(msgObj, fields, isWithout);
					arrFieldNames = (fieldsInMsg + "," + fieldsInVO).split(",");
					makeOverrideSet(msgObj, arrFieldNames);
				}
			}
		}
	}

	private static void makeOverrideSet(Message msgObj, String[] arrFieldNames) throws Exception {
		if (arrFieldNames != null) {
			for (String fieldName : arrFieldNames) {
				fieldName = fieldName.trim();

				if ((fieldName.length() > 0) && (!isIgnoreField(fieldName))) {
					String getValiableMethodName = getMethodName(fieldName, "get");
					Method getValueMethod = msgObj.getClass().getMethod(getValiableMethodName, new Class[0]);
					Object value = getValueMethod.invoke(msgObj, new Object[0]);

					if (isOverrideEnableField(fieldName, value)) {
						if (value != null) {
							if ((value instanceof Date)) {
								msgObj.setOverride(fieldName, date2String((Date) value, "yyyyMMddhhmmss"));
							} else {
								msgObj.setOverride(fieldName, value.toString());
							}
						} else {
							msgObj.setOverride(fieldName, null);
						}
					}
				}
			}
		}
	}

	public static boolean isIgnoreField(String field) {
//		String neverOverrideFields = "id,originalActivity";
//		String noGetterFields = "logger";
		boolean bResult = false;

		if ("id,originalActivity".indexOf(field) > -1) {
			bResult = true;
		} else if ("logger".indexOf(field) > -1) {
			bResult = true;
		}

		return bResult;
	}

	private static boolean isOverrideEnableField(String fields, Object value) {
//		String overrideFieldsWithoutNull = "createUser,createTime,updateTime,updateUser,histFlag";
		boolean bResult = true;

		if (("createUser,createTime,updateTime,updateUser,histFlag".indexOf(fields) > -1) && (value == null)) {
			bResult = false;
		}

		return bResult;
	}

	private static String getArrFieldNames(Message sourceObj, String fieldes, boolean isWithout) throws MesException {
		return getArrFieldNames(sourceObj.getClass(), fieldes, isWithout);
	}

	private static String getArrFieldNames(Class<?> sourceClass, String fields, boolean isWithout) throws MesException {
		String resultVariableNames = null;
		Field[] arrFields = null;
		String genericTypeStr = null;

		if (isNotEmpty(fields)) {
			if (isWithout) {
				resultVariableNames = "";
				arrFields = sourceClass.getDeclaredFields();

				for (Field field : arrFields) {
					genericTypeStr = field.toGenericString();

					if ((fields.indexOf(field.getName()) == -1) && (genericTypeStr.indexOf("static") == -1)
							&& (genericTypeStr.indexOf("final") == -1)) {
						resultVariableNames = resultVariableNames + field.getName() + ",";
					}

				}

				resultVariableNames.substring(0, resultVariableNames.length() - ",".length());
			} else {
				resultVariableNames = fields;
			}
		} else {
			if (isWithout) {
				throw new MesException("ExceptionAllNameWithoutNames", new String[0]);
			}

			resultVariableNames = "";
			arrFields = sourceClass.getDeclaredFields();

			for (Field field : arrFields) {
				genericTypeStr = field.toGenericString();

				if ((genericTypeStr.indexOf("static") == -1) && (genericTypeStr.indexOf("final") == -1)) {
					resultVariableNames = resultVariableNames + field.getName() + ",";
				}

			}

			resultVariableNames.substring(0, resultVariableNames.length() - ",".length());
		}

		return resultVariableNames;
	}

	private static String getIndent(int j) {
		String str = "";

		for (int i = 0; i < j; i++) {
			str = str + "\t";
		}

		return str;
	}

	private static String getListStr(List<?> list, String header, String indent, int j) {
		if (list == null)
			return "";

		String str = "";

		if ((list != null) && (list.size() > 0)) {
			str = str + "\n" + indent + "\t<" + header + ">";

			for (int i = 0; i < list.size(); i++) {
				String className = list.get(i).getClass().getSimpleName();
				className = className.substring(0, 1).toUpperCase() + className.substring(1);

				if (("String".equals(className)) && (isNotEmpty((String) list.get(i)))) {
					str = str + "\n" + indent + "\t\t<" + className + ">" + list.get(i) + "\n" + indent + "\t\t</"
							+ className + ">";
				} else {
					str = str + "\n" + indent + "\t\t<" + className + ">" + makeXml(list.get(i), j + 2) + "\n" + indent
							+ "\t\t</" + className + ">";
				}
			}

			str = str + "\n" + indent + "\t</" + header + ">";
		}

		return str;
	}

	private static String getMapStr(Map<?, ?> map, String header, String indent, int j) {
		if (map == null)
			return "";

		String str = "";
		Iterator<?> iterator = map.keySet().iterator();

		if (iterator == null)
			return "";

		int i = 0;
		while (iterator.hasNext()) {
			if (i == 0)
				str = str + "\n" + indent + "\t<" + header + ">";

			String name = (String) iterator.next();
			Object value = map.get(name);

			name = name.substring(0, 1).toUpperCase() + name.substring(1);

			if ((value instanceof List)) {
				str = str + "\n" + indent + "\t\t<" + name + ">";
				List<?> list = (List<?>) map.get(name);
				str = str
						+ getListStr(list, "List", new StringBuilder().append(indent).append("\t\t").toString(), j + 2);
				str = str + "\n" + indent + "\t\t</" + name + ">";
			} else if ((value instanceof Map)) {
				str = str + "\n" + indent + "\t\t<" + name + ">";
				Map<?, ?> in_map = (Map<?, ?>) value;
				str = str + getMapStr(in_map, "Map", new StringBuilder().append(indent).append("\t").toString(), j);
				str = str + "\n" + indent + "\t\t</" + name + ">";
			} else if ((value != null) && (isNotEmpty(value.toString()))) {
				str = str + "\n" + indent + "\t\t<" + name + ">";
				str = str + "\n" + indent + "\t\t\t" + value;
				str = str + "\n" + indent + "\t\t</" + name + ">";
			}

			i++;
		}

		if (i > 0)
			str = str + "\n" + indent + "\t</" + header + ">";

		return str;
	}

	private static boolean isLogged(String header) {
		if ((!"CustomMessageCount".equals(header)) && (!"OverridesCount".equals(header))
				&& (!"PropertiesCount".equals(header)) && (!"TagsCount".equals(header))
				&& (!"PropertiesCount".equals(header)) && (!"AttributesCount".equals(header))) {
			return true;
		}

		return false;
	}

	private static String getMemberStr(Object obj, String header, String indent) {
		return "\n" + indent + "\t<" + header + ">" + obj + "</" + header + ">";
	}

	private static String getTagName(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	private static String getXml(Object obj, String indent, int j) {
		String str = "";

		BeanUtilsBean beanUtilsBean = BeanUtilsBean.getInstance();

		if ((obj instanceof DynaBean)) {
			DynaProperty[] origDescriptors = ((DynaBean) obj).getDynaClass().getDynaProperties();
			for (int i = 0; i < origDescriptors.length; i++) {
				String name = origDescriptors[i].getName();

				if (beanUtilsBean.getPropertyUtils().isReadable(obj, name)) {
					try {
						Object member = ((DynaBean) obj).get(name);
						String header = getTagName(name);

						if (member != null) {
							if ((member instanceof List)) {
								str = str + getListStr((List<?>) member, header, indent, j);
							} else if ((member instanceof Map)) {
								str = str + getMapStr((Map<?, ?>) member, header, indent, j);
							} else if ((isNotEmpty(member.toString())) && (isLogged(header))) {
								str = str + getMemberStr(member, header, indent);
							}
						}
					} catch (Exception e) {
					}
				}
			}

		} else if ((obj instanceof Map)) {
			Iterator<?> entries = ((Map<?, ?>) obj).entrySet().iterator();
			while (entries.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry entry = (Map.Entry) entries.next();
				String name = (String) entry.getKey();
				String header = getTagName(name);

				if ((entry.getValue() instanceof String)) {
					str = str + getMemberStr(entry.getValue(), header, indent);
				} else if ((entry.getValue() instanceof Integer)) {
					str = str + getMemberStr(entry.getValue(), header, indent);
				} else if ((entry.getValue() instanceof Float)) {
					str = str + getMemberStr(entry.getValue(), header, indent);
				} else if ((entry.getValue() instanceof Timestamp)) {
					str = str + getMemberStr(entry.getValue(), header, indent);
				} else {
					str = str + getMapStr((Map<?, ?>) entry.getValue(), header, indent, j);
				}
			}
		} else {
			PropertyDescriptor[] origDescriptors = beanUtilsBean.getPropertyUtils().getPropertyDescriptors(obj);
			for (int i = 0; i < origDescriptors.length; i++) {
				String name = origDescriptors[i].getName();
				if (!"class".equals(name)) {
					if (beanUtilsBean.getPropertyUtils().isReadable(obj, name)) {
						try {
							Object member = beanUtilsBean.getPropertyUtils().getSimpleProperty(obj, name);
							String header = getTagName(name);

							if (member != null) {
								if ((member instanceof List)) {
									str = str + getListStr((List<?>) member, header, indent, j);
								} else if ((member instanceof Map)) {
									str = str + getMapStr((Map<?, ?>) member, header, indent, j);
								} else if ((isNotEmpty(member.toString())) && (isLogged(header))) {
									str = str + getMemberStr(member, header, indent);
								}
							}
						} catch (Exception e) {
						}
					}
				}
			}
		}

		return str;
	}

	public static String makeXml(Object obj, int j) {
		String str = "";
		String indent = getIndent(j);
		try {
			str = str + getXml(obj, indent, j);
		} catch (Exception e) {
			LoggerFactory.getLogger("CORE_TRACE").error(e.getMessage(), e);
		}

		return str;
	}

	public static RuleMessage toRuleMessage(byte[] message) throws Exception {
		ByteArrayInputStream b = new ByteArrayInputStream(message);

		ObjectInputStream reader = new ObjectInputStream(b);

		return (RuleMessage) reader.readObject();
	}

	public static byte[] toBytes(Object object) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(object);

		return baos.toByteArray();
	}

	public static Document string2Document(String str) throws Exception {
		return new SAXBuilder().build(new StringReader(str));
	}

	public static String document2String(Document document) throws Exception {
		return new XMLOutputter().outputString(document);
	}

	public static String documentToStringForDocumentLog(Document document) throws Exception {
		if (document == null)
			throw new NullPointerException("Document is null.");

		XMLOutputter out = new XMLOutputter();
		Writer writer = new StringWriter();
		Format format = Format.getPrettyFormat();
		format = format.setEncoding("UTF-8");
		format.setLineSeparator("\n");
		format.setIndent("\t");
		out.setFormat(format);
		out.output(document, writer);

		return writer.toString();
	}

	public static List<Object> sort(List<?> list, String sortField) throws Exception {
		if (isCollectionEmpty(list))
			return null;

		SortedMap<Object, Object> map = new TreeMap<Object, Object>();

		String className = list.get(0).getClass().getName();

		String simpleClassName = list.get(0).getClass().getSimpleName();

		Class<?> c = Class.forName(className);

		Method m = c.getMethod(sortField, new Class[0]);

		Method txnCodeMethod = c.getMethod("getTxnCode", new Class[0]);

		Method lastEventTime = c.getMethod("getLastEventTime", new Class[0]);

		for (Iterator<?> i$ = list.iterator(); i$.hasNext();) {
			Object obj = i$.next();

			String tempTxnCode = "Z";
			try {
				Object result = m.invoke(obj, new Object[0]);

				Object txnCode = txnCodeMethod.invoke(obj, new Object[0]);

				if (txnCode == null) {
					tempTxnCode = "Z";
				} else {
					tempTxnCode = txnCode + "";
				}

				result = simpleClassName + "\t" + result + "\t" + lastEventTime + "\t" + tempTxnCode;

				map.put(result, obj);
			} catch (InvocationTargetException e) {
				throw new MesException("InvalidMethodName", sortField);
			} catch (Exception e) {
				LoggerFactory.getLogger("CORE_TRACE").error(e.getMessage(), e);
			}

		}

		List<Object> returnList = new ArrayList<Object>();

		Set<Object> set = map.keySet();

		Iterator<Object> itor = set.iterator();

		while (itor.hasNext()) {
			Object Key = itor.next();

			returnList.add(map.get(Key));
		}

		return returnList;
	}

	/**
	 * TimeStamp를 String으로 반환한다.
	 * 
	 * @return
	 */
	public static String timeStamp2String() {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		String sDate = simpleDateFormat.format(new Date());

		return sDate;
	}

	public static String makeRandomTableSysID(String sKeyName) {

		// Max 6자리의 난수를 발생한다.
		int minValue = 1;
		int maxValue = 999999;

		String sTableSysID = "";
		String sCurrentTime = timeStamp2String(); // 현재 시간을 가져온다.

		// 난수 발생
		int iRand = (int) ((Math.random() * (maxValue - minValue)) + minValue);

		if (isEmpty(sKeyName))
			sTableSysID = "SYS";
		else
			sTableSysID = sKeyName;

		sTableSysID += sCurrentTime + String.format("%6s", iRand);

		return sTableSysID;
	}

	/**
	 * 
	 * <PRE>
	  * 1.Method Name : makeTableSysID
	  * 2.Author    : ktbiz
	  * 3.CreateDate: 2021. 6. 9오후 3:32:11
	  * 4.UpdateDate:
	  * 5.Comment   : Table Sys ID를 생성하여 String으로 반환한다.
	 * </PRE>
	 */
	public static String makeTableSysID(String sKeyName) {
		String sTableSysID = "";
		String sCurrentTime = timeStamp2String(); // 현재 시간을 가져온다.

		if (isEmpty(sKeyName))
			sTableSysID = "SYS";
		else
			sTableSysID = sKeyName;

		sTableSysID += sCurrentTime + getSeqNumber();
		return sTableSysID;
	}

	public static String makeTableSysID() {
		return makeTableSysID(null);
	}

	public static List<String> String2ListJson(String list) throws JsonMappingException, JsonProcessingException {

		ObjectMapper objectMapper = new ObjectMapper();
		List<String> pp3 = objectMapper.readValue(list, java.util.ArrayList.class);
		List<String> json = new ArrayList<String>();
		for (int i = 0; i < pp3.size(); i++) {
			json.add(objectMapper.writeValueAsString(pp3.get(i)));
		}

		return json;
	}

}
