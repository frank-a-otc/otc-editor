/**
* Copyright (c) otclfoundation.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcl.web.commons.service;

import java.util.List;
import java.util.Set;

import org.otcl.web.commons.dto.ClassMetadataDto;
import org.otcl2.common.OtclConstants;
import org.otcl2.common.dto.otcl.OtclFileDto;


public interface OtclEditorService {
	
	Set<String> findTypeNamesInPackage(String basePackage);
	
	OtclFileDto createOtclFileDto(String otclInstructions, boolean reverse);
	
	List<ClassMetadataDto> createMembersHierarchy(String clsName, OtclConstants.TARGET_SOURCE targetSource);

	String createYaml(OtclFileDto otclFileDto);
	
}
