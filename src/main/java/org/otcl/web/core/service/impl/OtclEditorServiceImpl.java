/**
* Copyright (c) otclfoundation.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcl.web.core.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.otcl.web.commons.dto.ClassMetadataDto;
import org.otcl.web.commons.exception.OtclEditorException;
import org.otcl.web.commons.service.OtclEditorService;
import org.otcl.web.commons.util.OtclEditorUtil;
import org.otcl2.common.OtclConstants.TARGET_SOURCE;
import org.otcl2.common.dto.otcl.OtclFileDto;
import org.otcl2.common.dto.otcl.OtclFileDto.Execute;
import org.otcl2.common.dto.otcl.OtclFileDto.OtclCommand;
import org.otcl2.common.dto.otcl.OverrideDto;
import org.otcl2.common.dto.otcl.SourceDto;
import org.otcl2.common.dto.otcl.TargetDto;
import org.otcl2.common.util.OtclUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

@Service
public class OtclEditorServiceImpl implements OtclEditorService {

//	private static final Logger LOGGER = LoggerFactory.getLogger(OtclEditorServiceImpl.class);
	
	private static ObjectMapper objectMapper;

	public OtclEditorServiceImpl() {
		YAMLFactory yamlFactory = new YAMLFactory();
		yamlFactory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
		objectMapper = new ObjectMapper(yamlFactory);
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
	}
	
	@Override
	public List<ClassMetadataDto> createMembersHierarchy(String clsName, TARGET_SOURCE targetSource) {
		if (StringUtils.isEmpty(clsName)) {
			throw new OtclEditorException("", "Class-name cannot be empty!");
		}
		return OtclEditorUtil.createMembersHierarchy(clsName, targetSource);
	}

	@Override
	public Set<String> findTypeNamesInPackage(String basePackage) {
		Set<String> fileNames = OtclEditorUtil.findTypeNamesInPackage(basePackage);
		return fileNames;
	}

	@Override
	public OtclFileDto createOtclFileDto(String otclInstructions, boolean flip) {
		OtclFileDto otclFileDto = null;
		try {
			otclFileDto = objectMapper.readValue(otclInstructions, OtclFileDto.class);
		} catch (JsonProcessingException e) {
			throw new OtclEditorException("", e);
		}
		String targetCls = otclFileDto.metadata.objectTypes.target;
		String sourceCls = otclFileDto.metadata.objectTypes.source;
		if (flip) {
			// create new OtclFileDto and flip values
			OtclFileDto reverseOtclFileDto = new OtclFileDto();
			reverseOtclFileDto.metadata = new OtclFileDto.Metadata();
			reverseOtclFileDto.metadata.entryClassName = otclFileDto.metadata.entryClassName;
			reverseOtclFileDto.metadata.helper = otclFileDto.metadata.helper;
			reverseOtclFileDto.metadata.objectTypes = new OtclFileDto.Metadata.ObjectTypes(); 
			reverseOtclFileDto.metadata.objectTypes.source = otclFileDto.metadata.objectTypes.target;
			reverseOtclFileDto.metadata.objectTypes.target = otclFileDto.metadata.objectTypes.source;
			reverseOtclFileDto.otclCommands = new ArrayList<>();
			for (OtclFileDto.OtclCommand otclScript : otclFileDto.otclCommands) {
				OtclCommand flippedOtclScript = new OtclCommand();
				reverseOtclFileDto.otclCommands.add(flippedOtclScript);
				if (otclScript.copy != null) {
					flippedOtclScript.copy = new OtclFileDto.Copy();
					flippedOtclScript.copy.id = otclScript.copy.id;
					if (otclScript.copy.from.otclChain != null) {
						flippedOtclScript.copy.to = new TargetDto();
						flippedOtclScript.copy.to.otclChain = otclScript.copy.from.otclChain;
						flippedOtclScript.copy.from = new OtclFileDto.Copy.Source();
						flippedOtclScript.copy.from.otclChain = otclScript.copy.to.otclChain;
						if (otclScript.copy.from.overrides != null) {
							if (flippedOtclScript.copy.to.overrides == null) {
								flippedOtclScript.copy.to.overrides = new ArrayList<>();
							}
							for (OverrideDto sourceOverride : otclScript.copy.from.overrides) {
								TargetDto.Override override = new TargetDto.Override();
								flippedOtclScript.copy.to.overrides.add(override);
								override.tokenPath = sourceOverride.tokenPath;
								override.getter = sourceOverride.getter;
								override.getterHelper = sourceOverride.getterHelper;
							}
						} 
						if (otclScript.copy.to.overrides != null) {
							if (flippedOtclScript.copy.from.overrides == null) {
								flippedOtclScript.copy.from.overrides = new ArrayList<>();
							}
							for (TargetDto.Override targetOverride : otclScript.copy.to.overrides) {
								OverrideDto override = new OverrideDto();
								flippedOtclScript.copy.from.overrides.add(override);
								override.tokenPath = targetOverride.tokenPath;
								override.getter = targetOverride.getter;
								override.getterHelper = targetOverride.getterHelper;	
							}
						}
					} else if (otclScript.execute != null) {
						flippedOtclScript.execute = new Execute();
						flippedOtclScript.execute.id = otclScript.execute.id;
						flippedOtclScript.execute.target = new TargetDto();
						flippedOtclScript.execute.target.otclChain = otclScript.execute.source.otclChain;
						flippedOtclScript.execute.source = new SourceDto();
						flippedOtclScript.execute.source.otclChain = otclScript.execute.target.otclChain;
						if (otclScript.execute.source.overrides != null) {
							if (flippedOtclScript.execute.target.overrides == null) {
								flippedOtclScript.execute.target.overrides = new ArrayList<>();
							}
							for (OverrideDto sourceOverride : otclScript.execute.source.overrides) {
								TargetDto.Override override = new TargetDto.Override();
								flippedOtclScript.execute.target.overrides.add(override);
								override.tokenPath = sourceOverride.tokenPath;
								override.getter = sourceOverride.getter;
								override.getterHelper = sourceOverride.getterHelper;
							}
						} 
						if (otclScript.execute.target.overrides != null) {
							if (flippedOtclScript.execute.source.overrides == null) {
								flippedOtclScript.execute.source.overrides = new ArrayList<>();
							}
							for (TargetDto.Override targetOverride : otclScript.execute.target.overrides) {
								OverrideDto override = new OverrideDto();
								flippedOtclScript.execute.source.overrides.add(override);
								override.tokenPath = targetOverride.tokenPath;
								override.getter = targetOverride.getter;
								override.getterHelper = targetOverride.getterHelper;	
							}
						}
					}
				}
			}
			otclFileDto = reverseOtclFileDto;
		} else {
			otclFileDto.fileName = OtclUtils.createOtclFileName(sourceCls, targetCls);
		}
		return otclFileDto;
	}
	
	@Override
	public String createYaml(OtclFileDto otclFileDto) {
		StringWriter stringWriter = new StringWriter();
		try {
			objectMapper.writeValue(stringWriter, otclFileDto);
		} catch (IOException e) {
			throw new OtclEditorException("", e);
		}
		return stringWriter.toString();
	}
	
}
