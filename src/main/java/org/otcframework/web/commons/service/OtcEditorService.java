/**
* Copyright (c) otcframework.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcframework.web.commons.service;

import java.util.List;
import java.util.Set;

import org.otcframework.common.OtcConstants;
import org.otcframework.common.dto.otc.OtcFileDto;
import org.otcframework.web.commons.dto.ClassMetadataDto;


public interface OtcEditorService {
	
	Set<String> findTypeNamesInPackage(String basePackage);
	
	OtcFileDto createOtcFileDto(String otcInstructions, boolean reverse);
	
	List<ClassMetadataDto> createTree(String clsName, OtcConstants.TARGET_SOURCE targetSource);

	String createYaml(OtcFileDto otcFileDto);
	
}
