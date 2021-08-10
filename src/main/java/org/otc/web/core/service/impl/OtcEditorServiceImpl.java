/**
* Copyright (c) otcframework.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otc.web.core.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.otc.web.commons.dto.ClassMetadataDto;
import org.otc.web.commons.exception.OtcEditorException;
import org.otc.web.commons.service.OtcEditorService;
import org.otc.web.commons.util.OtcEditorUtil;
import org.otcframework.common.OtcConstants.TARGET_SOURCE;
import org.otcframework.common.dto.otc.OtcFileDto;
import org.otcframework.common.dto.otc.OtcFileDto.Execute;
import org.otcframework.common.dto.otc.OtcFileDto.OtcCommands;
import org.otcframework.common.dto.otc.OverrideDto;
import org.otcframework.common.dto.otc.SourceDto;
import org.otcframework.common.dto.otc.TargetDto;
import org.otcframework.common.util.OtcUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

@Service
public class OtcEditorServiceImpl implements OtcEditorService {

//	private static final Logger LOGGER = LoggerFactory.getLogger(OtcEditorServiceImpl.class);
	
	private static ObjectMapper objectMapper;

	public OtcEditorServiceImpl() {
		YAMLFactory yamlFactory = new YAMLFactory();
		yamlFactory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
		objectMapper = new ObjectMapper(yamlFactory);
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
	}
	
	@Override
	public List<ClassMetadataDto> createMembersHierarchy(String clsName, TARGET_SOURCE targetSource) {
		if (StringUtils.isEmpty(clsName)) {
			throw new OtcEditorException("", "Class-name cannot be empty!");
		}
		return OtcEditorUtil.createMembersHierarchy(clsName, targetSource);
	}

	@Override
	public Set<String> findTypeNamesInPackage(String basePackage) {
		Set<String> fileNames = OtcEditorUtil.findTypeNamesInPackage(basePackage);
		return fileNames;
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
			reverseOtcFileDto.otcCommands = new ArrayList<>();
			for (OtcCommands otcScript : otcFileDto.otcCommands) {
				OtcCommands flippedOtcScript = new OtcCommands();
				reverseOtcFileDto.otcCommands.add(flippedOtcScript);
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
