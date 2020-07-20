/**
* Copyright (c) otcl2.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcl.web.core.service.impl;

import java.util.List;
import java.util.Set;

import org.otcl.common.OtclConstants;
import org.otcl.common.engine.compiler.dto.OtclFileDto;
import org.otcl.common.util.OtclUtils;
import org.otcl.web.commons.dto.ClassMetadataDto;
import org.otcl.web.commons.exception.OtclEditorException;
import org.otcl.web.commons.service.OtclEditorService;
import org.otcl.web.commons.util.OtclEditorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Service
public class OtclEditorServiceImpl implements OtclEditorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OtclEditorServiceImpl.class);
		
	@Override
	public List<ClassMetadataDto> createMembersHierarchy(String clsName) {
		if (StringUtils.isEmpty(clsName)) {
			throw new OtclEditorException("", "Class-name cannot be empty!");
		}
		return OtclEditorUtil.createMembersHierarchy(clsName);
	}

	@Override
	public Set<String> findTypeNamesInPackage(String basePackage) {
		Set<String> fileNames = OtclEditorUtil.findTypeNamesInPackage(basePackage);
		return fileNames;
	}

	@Override
	public OtclFileDto createOtclFileDto(String targetCls, String sourceCls, String otclInstructions, boolean reverse) {
		Yaml yaml = new Yaml(new Constructor(OtclFileDto.class));
		OtclFileDto otclFileDto = yaml.load(otclInstructions);
		if (reverse) {
			String targetType = otclFileDto.metadata.labels.targetType;
			otclFileDto.metadata.labels.targetType = otclFileDto.metadata.labels.sourceType;
			otclFileDto.metadata.labels.sourceType = targetType;
			for (OtclFileDto.OtclScript script : otclFileDto.otclScripts) {
				String targetOtclChain = script.targetChainProfile.otclChain;
				script.targetChainProfile.otclChain = reverseSanitizedOtclChain(script.sourceChainProfile.otclChain);
				script.sourceChainProfile.otclChain= reverseSanitizedOtclChain(targetOtclChain);
			}
			otclFileDto.fileName = OtclUtils.createOtclFileName(sourceCls, targetCls);
		} else {
			otclFileDto.fileName = OtclUtils.createOtclFileName(targetCls, sourceCls);
		}
		return otclFileDto;
	}

	private static String reverseSanitizedOtclChain(String otclChain) {
		if (otclChain.startsWith("{")) {
			return null;
		}
		if (otclChain.contains(OtclConstants.ANCHOR)) {
			otclChain = otclChain.replace(OtclConstants.ANCHOR, "");
		}
		if (otclChain.contains("::get")) {
			otclChain = otclChain.replace("::get", "::set");
		} else if (otclChain.contains("::set")) {
			otclChain = otclChain.replace("::set", "::get");
		}
		// -- remove generics
		int idxCollection = otclChain.indexOf("[<");
		if (idxCollection < 0) {
			idxCollection = otclChain.indexOf("[^<");
		}
		if (idxCollection > 0) {
			// remove map-key is present.
			int idxMapKey = otclChain.indexOf("'", idxCollection);
			if (idxMapKey < 0) {
				idxMapKey = otclChain.indexOf("\"", idxCollection);
				if (idxMapKey > 0) {
					int idxEndMapKey = otclChain.indexOf("\"", idxCollection);
					otclChain = otclChain.replace(otclChain.substring(idxMapKey, idxEndMapKey + 1), "K");
				}
			} else {
				int idxEndMapKey = otclChain.indexOf("'", idxMapKey + 1);
				otclChain = otclChain.replace(otclChain.substring(idxMapKey, idxEndMapKey + 1), "K");
			}
		}
		int idx = otclChain.indexOf("<");
		if (idx > 0 && idx < idxCollection) {
			// in case if generics defined before map notation
			int idxEnd = otclChain.indexOf(">", idx);
			otclChain = otclChain.replace(otclChain.substring(idx, idxEnd + 1), "");
		}
		// in case the key / value has generics defined
		if (idxCollection > 0) {
			idxCollection = otclChain.indexOf(OtclConstants.MAP_KEY_REF);
			if (idxCollection < 0) {
				idxCollection = otclChain.indexOf(OtclConstants.MAP_VALUE_REF);
			}
			int idxMapEnd = 0;
			if (idxCollection > 0) {
				idxMapEnd = otclChain.indexOf("]", idx);
			}
			idx = otclChain.indexOf("<", idxMapEnd);
			if (idxCollection > 0 && idx < idxCollection) {
				int idxEnd = otclChain.indexOf(">", idx);
				otclChain = otclChain.replace(otclChain.substring(idx, idxEnd + 1), "");
			}
		}
		return otclChain;
	}
}
