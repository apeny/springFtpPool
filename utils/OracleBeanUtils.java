package org.jiuzhou.policebd_inf.utils;

import java.lang.reflect.Field;

import org.jiuzhou.policebd_inf.dsj.domain.DSJCheliangdangan.Cheliangdangan;

public class OracleBeanUtils {
	public static void main(String[] args) throws ClassNotFoundException {
		Cheliangdangan cd = new Cheliangdangan();
		converStringNULLToEmpty(cd, Cheliangdangan.class);
	}

	/**
	 * 把指定对象的string属性，null转换成""
	 * 
	 * @param source
	 * @param target
	 * @param classz
	 * @throws ClassNotFoundException
	 */
	public static void converStringNULLToEmpty(Object target, Class classz) throws ClassNotFoundException {
		Field[] fields = classz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				if ("java.lang.String".equals(field.getType().getName())) {
					try {
						field.setAccessible(true);
						if (field.get(target) == null) {
							field.set(target, "");
						}
					} catch (Exception e) {

					}
				}
			}
		}

	}

}
