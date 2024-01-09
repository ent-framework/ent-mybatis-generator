/*
 * Copyright (c) 2024. Licensed under the Apache License, Version 2.0.
 */

package net.entframework.mybatis.apt;

import java.util.List;

public class Utils {

	public static int length(final CharSequence cs) {
		return cs == null ? 0 : cs.length();
	}

	public static String uncapitalize(final String str) {
		final int strLen = length(str);
		if (strLen == 0) {
			return str;
		}

		final int firstCodePoint = str.codePointAt(0);
		final int newCodePoint = Character.toLowerCase(firstCodePoint);
		if (firstCodePoint == newCodePoint) {
			// already capitalized
			return str;
		}

		final int[] newCodePoints = new int[strLen]; // cannot be longer than the char
														// array
		int outOffset = 0;
		newCodePoints[outOffset++] = newCodePoint; // copy the first code point
		for (int inOffset = Character.charCount(firstCodePoint); inOffset < strLen;) {
			final int codePoint = str.codePointAt(inOffset);
			newCodePoints[outOffset++] = codePoint; // copy the remaining ones
			inOffset += Character.charCount(codePoint);
		}
		return new String(newCodePoints, 0, outOffset);
	}

	public static String join(List<String> list, String separator) {
		if (list == null || list.isEmpty())
			return "";
		StringBuilder stringBuilder = new StringBuilder();
		for (int k = 0; k < list.size(); k++) {
			stringBuilder.append(list.get(k));
			if (k != list.size() - 1) {
				stringBuilder.append(separator);
			}
		}
		return stringBuilder.toString();
	}

}
