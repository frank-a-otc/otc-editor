/**
* Copyright (c) otcl2.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcl.web.commons.service;

import java.util.List;
import java.util.Set;

import org.otcl.common.engine.compiler.dto.OtclFileDto;
import org.otcl.web.commons.dto.ClassMetadataDto;

public interface OtclEditorService {
	
	Set<String> findTypeNamesInPackage(String basePackage);
	
	OtclFileDto createOtclFileDto(String targetCls, String sourceCls, String otclInstructions, boolean reverse);
	
	List<ClassMetadataDto> createMembersHierarchy(String clsName);
	
}
