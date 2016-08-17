/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.gradle.util;

import java.util.Arrays;

import org.gradle.util.CollectionUtils;

/**
 * @author Andrea Di Giorgi
 */
public class StringUtil {

	public static String capitalize(String s) {
		char firstChar = s.charAt(0);

		if (Character.isLowerCase(firstChar)) {
			s = Character.toUpperCase(firstChar) + s.substring(1);
		}

		return s;
	}

	/**
	 * @deprecated As of 1.1.0
	 */
	@Deprecated
	public static String merge(String[] array, String separator) {
		return CollectionUtils.join(separator, array);
	}

	public static String[] prepend(String[] array, String prefix) {
		if (ArrayUtil.isEmpty(array) || Validator.isNull(prefix)) {
			return array;
		}

		String[] newArray = new String[array.length];

		for (int i = 0; i < array.length; i++) {
			newArray[i] = prefix + array[i];
		}

		return newArray;
	}

	public static String repeat(char c, int length) {
		char[] chars = new char[length];

		Arrays.fill(chars, c);

		return new String(chars);
	}

}