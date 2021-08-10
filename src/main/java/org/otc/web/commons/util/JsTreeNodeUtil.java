/**
* Copyright (c) otcframework.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otc.web.commons.util;

import java.util.ArrayList;
import java.util.List;

import org.otc.web.commons.dto.ClassMetadataDto;
import org.otc.web.commons.dto.JstreeNodeInfo;

public class JsTreeNodeUtil {

	public static List<JstreeNodeInfo> convert(List<ClassMetadataDto> lstClassInfo) {
		if (lstClassInfo == null) {
			return null;
		}
		List<JstreeNodeInfo> lstJsTreeInfo = new ArrayList<>();
		for (ClassMetadataDto clsInfo : lstClassInfo) {
			JstreeNodeInfo.Builder builder = JstreeNodeInfo.newBuilder();
			builder.addId(clsInfo.id)
				.addText(clsInfo.text)
				.addChildren(convert(clsInfo.children));
			JstreeNodeInfo jstreeNodeInfo = builder.build();
			lstJsTreeInfo.add(jstreeNodeInfo);
		}
		return lstJsTreeInfo;
	}
}
