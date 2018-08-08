package org.jiuzhou.policebd_inf.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringRegex {

	public static void main(String[] args) {
		System.out.println(isPlateNumber("WJ1234"));
	}

	/**
	 * 验证手机号码
	 * 
	 * @param mobiles
	 * @return
	 */
	public static boolean isMobileNO(String mobiles) {
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(17[0-9])|(18[0,5-9]))\\d{8}$");
			Matcher m = regex.matcher(mobiles);
			flag = m.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 验证邮箱地址是否正确
	 * 
	 * @param email
	 * @return
	 */
	public static boolean checkEmail(String email) {
		boolean flag = false;
		try {
			String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(email);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 校验身份证
	 * 
	 * @param idCard
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isIDCard(String idCard) {
		// 51012319980729403X
		// String REGEX_ID_CARD = "(^\\d{18}$)|(^\\d{15}$)";
		// return Pattern.matches(REGEX_ID_CARD, idCard);
		if (idCard == null) {
			return false;
		}
		String tmp = idCard.trim();
		if (tmp.length() == 18 || tmp.length() == 15) {
			return true;
		}
		return false;
	}

	/**
	 * 校验车牌号
	 * 
	 * @param shopSign
	 * @return
	 */
	public static boolean isPlateNumber(String shopSign) {
		// 车牌号格式验证
		String vehicleNoStyle = "^[\u4e00-\u9fa5]{1}[A-Z0-9]{6}$";
		Pattern pattern = Pattern.compile(vehicleNoStyle);
		Matcher matcher = pattern.matcher(shopSign.trim());
		return matcher.matches();
	}
}
