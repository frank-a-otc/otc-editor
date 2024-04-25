/**
 * Copyright (c) otcframework.org
 *
 * @author  Franklin J Abel
 * @version 1.0
 * @since   2020-06-08
 *
 * This file is part of the OTC framework.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.otcframework.web.core.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.otcframework.common.OtcConstants.TARGET_SOURCE;
import org.otcframework.common.dto.otc.OtcFileDto;
import org.otcframework.common.dto.otc.OtcFileDto.Execute;
import org.otcframework.common.dto.otc.OtcFileDto.OtcsCommand;
import org.otcframework.common.dto.otc.OverrideDto;
import org.otcframework.common.dto.otc.SourceDto;
import org.otcframework.common.dto.otc.TargetDto;
import org.otcframework.common.util.OtcUtils;
import org.otcframework.web.commons.dto.ClassMetadataDto;
import org.otcframework.web.commons.exception.OtcEditorException;
import org.otcframework.web.commons.service.OtcEditorService;
import org.otcframework.web.commons.util.OtcEditorUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class OtcEditorServiceImpl implements OtcEditorService {

	private static ObjectMapper objectMapper;

	public OtcEditorServiceImpl() {
		YAMLFactory yamlFactory = new YAMLFactory();
		yamlFactory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
		objectMapper = new ObjectMapper(yamlFactory);
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
	}
	
	@Override
	public List<ClassMetadataDto> createTree(String clsName, TARGET_SOURCE targetOrSource) {
		if (StringUtils.isEmpty(clsName)) {
			throw new OtcEditorException("", "Class-name cannot be empty!");
		}
		return OtcEditorUtil.createMembersHierarchy(clsName, targetOrSource);
	}

	@Override
	public Set<String> findTypeNamesInPackage(String basePackage) {
		return OtcEditorUtil.findTypeNamesInPackage(basePackage);
	}

	@Override
	public OtcFileDto createOtcFileDto(String otcInstructions, boolean flip) {
		OtcFileDto otcFileDto = null;
		try {
			otcFileDto = objectMapper.readValue(otcInstructions, OtcFileDto.class);
		} catch (JsonProcessingException e) {
			throw new OtcEditorException("", e);
		}
		String targetCls = otcFileDto.metadata.objectTypes.target;
		String sourceCls = otcFileDto.metadata.objectTypes.source;
		if (flip) {
			// create new OtcFileDto and flip values
			OtcFileDto reverseOtcFileDto = new OtcFileDto();
			reverseOtcFileDto.metadata = new OtcFileDto.Metadata();
			reverseOtcFileDto.metadata.entryClassName = otcFileDto.metadata.entryClassName;
			reverseOtcFileDto.metadata.helper = otcFileDto.metadata.helper;
			reverseOtcFileDto.metadata.objectTypes = new OtcFileDto.Metadata.ObjectTypes(); 
			reverseOtcFileDto.metadata.objectTypes.source = otcFileDto.metadata.objectTypes.target;
			reverseOtcFileDto.metadata.objectTypes.target = otcFileDto.metadata.objectTypes.source;
			reverseOtcFileDto.commands = new ArrayList<>();
			for (OtcsCommand otcScript : otcFileDto.commands) {
				OtcsCommand flippedOtcScript = new OtcsCommand();
				reverseOtcFileDto.commands.add(flippedOtcScript);
				if (otcScript.copy != null) {
					flippedOtcScript.copy = new OtcFileDto.Copy();
					flippedOtcScript.copy.id = otcScript.copy.id;
					if (otcScript.copy.from.objectPath != null) {
						flippedOtcScript.copy.to = new TargetDto();
						flippedOtcScript.copy.to.objectPath = otcScript.copy.from.objectPath;
						flippedOtcScript.copy.from = new OtcFileDto.Copy.Source();
						flippedOtcScript.copy.from.objectPath = otcScript.copy.to.objectPath;
						if (otcScript.copy.from.overrides != null) {
							if (flippedOtcScript.copy.to.overrides == null) {
								flippedOtcScript.copy.to.overrides = new ArrayList<>();
							}
							for (OverrideDto sourceOverride : otcScript.copy.from.overrides) {
								TargetDto.Override override = new TargetDto.Override();
								flippedOtcScript.copy.to.overrides.add(override);
								override.tokenPath = sourceOverride.tokenPath;
								override.getter = sourceOverride.getter;
								override.getterHelper = sourceOverride.getterHelper;
							}
						} 
						if (otcScript.copy.to.overrides != null) {
							if (flippedOtcScript.copy.from.overrides == null) {
								flippedOtcScript.copy.from.overrides = new ArrayList<>();
							}
							for (TargetDto.Override targetOverride : otcScript.copy.to.overrides) {
								OverrideDto override = new OverrideDto();
								flippedOtcScript.copy.from.overrides.add(override);
								override.tokenPath = targetOverride.tokenPath;
								override.getter = targetOverride.getter;
								override.getterHelper = targetOverride.getterHelper;	
							}
						}
					} else if (otcScript.execute != null) {
						flippedOtcScript.execute = new Execute();
						flippedOtcScript.execute.id = otcScript.execute.id;
						flippedOtcScript.execute.target = new TargetDto();
						flippedOtcScript.execute.target.objectPath = otcScript.execute.source.objectPath;
						flippedOtcScript.execute.source = new SourceDto();
						flippedOtcScript.execute.source.objectPath = otcScript.execute.target.objectPath;
						if (otcScript.execute.source.overrides != null) {
							if (flippedOtcScript.execute.target.overrides == null) {
								flippedOtcScript.execute.target.overrides = new ArrayList<>();
							}
							for (OverrideDto sourceOverride : otcScript.execute.source.overrides) {
								TargetDto.Override override = new TargetDto.Override();
								flippedOtcScript.execute.target.overrides.add(override);
								override.tokenPath = sourceOverride.tokenPath;
								override.getter = sourceOverride.getter;
								override.getterHelper = sourceOverride.getterHelper;
							}
						} 
						if (otcScript.execute.target.overrides != null) {
							if (flippedOtcScript.execute.source.overrides == null) {
								flippedOtcScript.execute.source.overrides = new ArrayList<>();
							}
							for (TargetDto.Override targetOverride : otcScript.execute.target.overrides) {
								OverrideDto override = new OverrideDto();
								flippedOtcScript.execute.source.overrides.add(override);
								override.tokenPath = targetOverride.tokenPath;
								override.getter = targetOverride.getter;
								override.getterHelper = targetOverride.getterHelper;	
							}
						}
					}
				}
			}
			otcFileDto = reverseOtcFileDto;
		} else {
			otcFileDto.fileName = OtcUtils.createOtcFileName(sourceCls, targetCls);
		}
		return otcFileDto;
	}
	
	@Override
	public String createYaml(OtcFileDto otcFileDto) {
		StringWriter stringWriter = new StringWriter();
		try {
			objectMapper.writeValue(stringWriter, otcFileDto);
		} catch (IOException e) {
			throw new OtcEditorException("", e);
		}
		return stringWriter.toString();
	}
	
}
