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

package com.liferay.knowledgebase.util;

import com.liferay.knowledgebase.model.KBArticle;
import com.liferay.knowledgebase.model.KBComment;
import com.liferay.knowledgebase.model.KBCommentConstants;
import com.liferay.knowledgebase.model.KBTemplate;
import com.liferay.knowledgebase.util.comparator.KBArticleCreateDateComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleModifiedDateComparator;
import com.liferay.knowledgebase.util.comparator.KBArticlePriorityComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleStatusComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleTitleComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleUserNameComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleVersionComparator;
import com.liferay.knowledgebase.util.comparator.KBArticleViewCountComparator;
import com.liferay.knowledgebase.util.comparator.KBCommentCreateDateComparator;
import com.liferay.knowledgebase.util.comparator.KBCommentModifiedDateComparator;
import com.liferay.knowledgebase.util.comparator.KBTemplateCreateDateComparator;
import com.liferay.knowledgebase.util.comparator.KBTemplateModifiedDateComparator;
import com.liferay.knowledgebase.util.comparator.KBTemplateTitleComparator;
import com.liferay.knowledgebase.util.comparator.KBTemplateUserNameComparator;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.FriendlyURLNormalizerUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UniqueList;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.ModelHintsUtil;
import com.liferay.portal.util.PortalUtil;

import java.io.InputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Peter Shin
 * @author Brian Wing Shun Chan
 */
public class KnowledgeBaseUtil {

	public static OrderByComparator<KBArticle> getKBArticleOrderByComparator(
		String orderByCol, String orderByType) {

		if (Validator.isNull(orderByCol) || Validator.isNull(orderByType)) {
			return null;
		}

		boolean ascending = false;

		if (orderByType.equals("asc")) {
			ascending = true;
		}

		if (orderByCol.equals("create-date")) {
			return new KBArticleCreateDateComparator(ascending);
		}
		else if (orderByCol.equals("modified-date")) {
			return new KBArticleModifiedDateComparator(ascending);
		}
		else if (orderByCol.equals("priority")) {
			return new KBArticlePriorityComparator(ascending);
		}
		else if (orderByCol.equals("status")) {
			return new KBArticleStatusComparator(ascending);
		}
		else if (orderByCol.equals("title")) {
			return new KBArticleTitleComparator(ascending);
		}
		else if (orderByCol.equals("user-name")) {
			return new KBArticleUserNameComparator(ascending);
		}
		else if (orderByCol.equals("version")) {
			return new KBArticleVersionComparator(ascending);
		}
		else if (orderByCol.equals("view-count")) {
			return new KBArticleViewCountComparator(ascending);
		}

		return null;
	}

	public static Sort[] getKBArticleSorts(
		String orderByCol, String orderByType) {

		if (Validator.isNull(orderByCol) || Validator.isNull(orderByType)) {
			return SortFactoryUtil.getDefaultSorts();
		}

		boolean reverse = true;

		if (orderByType.equals("asc")) {
			reverse = false;
		}

		if (orderByCol.equals("create-date")) {
			String fieldName = Field.CREATE_DATE;

			return new Sort[] {
				SortFactoryUtil.create(fieldName, Sort.LONG_TYPE, reverse),
				SortFactoryUtil.create(null, Sort.SCORE_TYPE, false)
			};
		}
		else if (orderByCol.equals("modified-date")) {
			String fieldName = Field.MODIFIED_DATE;

			return new Sort[] {
				SortFactoryUtil.create(fieldName, Sort.LONG_TYPE, reverse),
				SortFactoryUtil.create(null, Sort.SCORE_TYPE, false)
			};
		}
		else if (orderByCol.equals("score")) {
			String fieldName = null;

			return new Sort[] {
				SortFactoryUtil.create(fieldName, Sort.SCORE_TYPE, !reverse),
				SortFactoryUtil.create(
					Field.MODIFIED_DATE, Sort.LONG_TYPE, true)
			};
		}
		else if (orderByCol.equals("title")) {
			String fieldName = "titleKeyword";

			return new Sort[] {
				SortFactoryUtil.create(fieldName, Sort.STRING_TYPE, reverse),
				SortFactoryUtil.create(null, Sort.SCORE_TYPE, false)
			};
		}
		else if (orderByCol.equals("user-name")) {
			String fieldName = Field.USER_NAME;

			return new Sort[] {
				SortFactoryUtil.create(fieldName, Sort.STRING_TYPE, reverse),
				SortFactoryUtil.create(null, Sort.SCORE_TYPE, false)
			};
		}

		return SortFactoryUtil.getDefaultSorts();
	}

	public static String getKBArticleURL(
		long plid, long resourcePrimKey, int status, String portalURL,
		boolean maximized) {

		StringBundler sb = new StringBundler(11);

		sb.append(portalURL);
		sb.append(PortalUtil.getPathMain());
		sb.append("/portal/knowledge_base/find_kb_article");
		sb.append(StringPool.QUESTION);
		sb.append("plid");
		sb.append(StringPool.EQUAL);
		sb.append(String.valueOf(plid));
		sb.append(StringPool.AMPERSAND);
		sb.append("resourcePrimKey");
		sb.append(StringPool.EQUAL);
		sb.append(String.valueOf(resourcePrimKey));

		String url = sb.toString();

		if (status != WorkflowConstants.STATUS_APPROVED) {
			url = url.concat(StringPool.AMPERSAND).concat("status").concat(
				StringPool.EQUAL).concat(String.valueOf(status));
		}

		if (maximized) {
			url = url.concat(StringPool.AMPERSAND).concat("maximized").concat(
				StringPool.EQUAL).concat(String.valueOf(maximized));
		}

		return url;
	}

	public static OrderByComparator<KBComment> getKBCommentOrderByComparator(
		String orderByCol, String orderByType) {

		if (Validator.isNull(orderByCol) || Validator.isNull(orderByType)) {
			return null;
		}

		boolean ascending = false;

		if (orderByType.equals("asc")) {
			ascending = true;
		}

		if (orderByCol.equals("create-date")) {
			return new KBCommentCreateDateComparator(ascending);
		}
		else if (orderByCol.equals("modified-date")) {
			return new KBCommentModifiedDateComparator(ascending);
		}

		return null;
	}

	public static OrderByComparator<KBTemplate> getKBTemplateOrderByComparator(
		String orderByCol, String orderByType) {

		if (Validator.isNull(orderByCol) || Validator.isNull(orderByType)) {
			return null;
		}

		boolean ascending = false;

		if (orderByType.equals("asc")) {
			ascending = true;
		}

		if (orderByCol.equals("create-date")) {
			return new KBTemplateCreateDateComparator(ascending);
		}
		else if (orderByCol.equals("modified-date")) {
			return new KBTemplateModifiedDateComparator(ascending);
		}
		else if (orderByCol.equals("title")) {
			return new KBTemplateTitleComparator(ascending);
		}
		else if (orderByCol.equals("user-name")) {
			return new KBTemplateUserNameComparator(ascending);
		}

		return null;
	}

	public static String getMimeType(byte[] bytes, String fileName) {
		InputStream inputStream = new UnsyncByteArrayInputStream(bytes);

		try {
			return MimeTypesUtil.getContentType(inputStream, fileName);
		}
		finally {
			StreamUtil.cleanUp(inputStream);
		}
	}

	public static final int getNextStatus(int status) {
		if (status == KBCommentConstants.STATUS_IN_PROGRESS) {
			return KBCommentConstants.STATUS_RESOLVED;
		}
		else if (status == KBCommentConstants.STATUS_PENDING) {
			return KBCommentConstants.STATUS_IN_PROGRESS;
		}
		else {
			return KBCommentConstants.STATUS_NONE;
		}
	}

	public static Long[][] getParams(Long[] params) {
		if (ArrayUtil.isEmpty(params)) {
			return null;
		}

		if (params.length <= _SQL_DATA_MAX_PARAMETERS) {
			return new Long[][] {new Long[0], params};
		}

		return new Long[][] {
			ArrayUtil.subset(params, _SQL_DATA_MAX_PARAMETERS, params.length),
			ArrayUtil.subset(params, 0, _SQL_DATA_MAX_PARAMETERS)
		};
	}

	public static final int getPreviousStatus(int status) {
		if (status == KBCommentConstants.STATUS_IN_PROGRESS) {
			return KBCommentConstants.STATUS_PENDING;
		}
		else if (status == KBCommentConstants.STATUS_RESOLVED) {
			return KBCommentConstants.STATUS_IN_PROGRESS;
		}
		else {
			return KBCommentConstants.STATUS_NONE;
		}
	}

	public static final String getStatusTransitionLabel(int status) {
		if (status == KBCommentConstants.STATUS_IN_PROGRESS) {
			return "move-to-in-progress";
		}
		else if (status == KBCommentConstants.STATUS_PENDING) {
			return "move-to-pending";
		}
		else if (status == KBCommentConstants.STATUS_RESOLVED) {
			return "resolve";
		}
		else {
			throw new IllegalArgumentException(
				String.format("Invalid feedback status %s", status));
		}
	}

	public static String getUrlTitle(long id, String title) {
		if (title == null) {
			return String.valueOf(id);
		}

		title = StringUtil.toLowerCase(title.trim());

		if (Validator.isNull(title) || Validator.isNumber(title) ||
			title.equals("rss")) {

			title = String.valueOf(id);
		}
		else {
			title = FriendlyURLNormalizerUtil.normalize(
				title, _friendlyURLPattern);
		}

		return ModelHintsUtil.trimString(
			KBArticle.class.getName(), "urlTitle", title);
	}

	public static String[] parseKeywords(String values) {
		List<String> keywords = new UniqueList<String>();

		StringBundler sb = new StringBundler();

		for (char c : values.toCharArray()) {
			if (Character.isWhitespace(c)) {
				if (sb.length() > 0) {
					keywords.add(sb.toString());

					sb = new StringBundler();
				}
			}
			else if (Character.isLetterOrDigit(c)) {
				sb.append(c);
			}
			else {
				return new String[] {values};
			}
		}

		if (sb.length() > 0) {
			keywords.add(sb.toString());
		}

		return StringUtil.split(StringUtil.merge(keywords));
	}

	public static List<KBArticle> sort(
		long[] resourcePrimKeys, List<KBArticle> kbArticles) {

		Map<Long, KBArticle> map = new HashMap<Long, KBArticle>();

		for (KBArticle kbArticle : kbArticles) {
			map.put(kbArticle.getResourcePrimKey(), kbArticle);
		}

		kbArticles.clear();

		for (long resourcePrimKey : resourcePrimKeys) {
			if (map.containsKey(resourcePrimKey)) {
				kbArticles.add(map.get(resourcePrimKey));
			}
		}

		return kbArticles;
	}

	public static String trimLeadingSlash(String s) {
		if (Validator.isNull(s)) {
			return s;
		}

		int x = 0;

		for (char c : s.toCharArray()) {
			if ((c != CharPool.BACK_SLASH) && (c != CharPool.FORWARD_SLASH)) {
				break;
			}

			x = x + 1;
		}

		return s.substring(x, s.length());
	}

	private static final int _SQL_DATA_MAX_PARAMETERS = GetterUtil.getInteger(
		PropsUtil.get(PropsKeys.SQL_DATA_MAX_PARAMETERS));

	private static Pattern _friendlyURLPattern = Pattern.compile("[^a-z0-9_-]");

}