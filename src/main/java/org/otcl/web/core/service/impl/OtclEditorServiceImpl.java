/**
* Copyright (c) otcl2.org
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
import org.otcl2.common.dto.OtclFileDto;
import org.otcl2.common.dto.OtclFileDto.Copy;
import org.otcl2.common.dto.OtclFileDto.Copy.Source;
import org.otcl2.common.dto.OtclFileDto.Execute;
import org.otcl2.common.dto.OtclFileDto.Metadata;
import org.otcl2.common.dto.OtclFileDto.Metadata.ObjectTypes;
import org.otcl2.common.dto.OtclFileDto.OtclScript;
import org.otcl2.common.dto.OtclFileDto.Target;
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
			reverseOtclFileDto.metadata = new Metadata();
			reverseOtclFileDto.metadata.mainClassName = otclFileDto.metadata.mainClassName;
			reverseOtclFileDto.metadata.helper = otclFileDto.metadata.helper;
			reverseOtclFileDto.metadata.objectTypes = new ObjectTypes(); 
			reverseOtclFileDto.metadata.objectTypes.source = otclFileDto.metadata.objectTypes.target;
			reverseOtclFileDto.metadata.objectTypes.target = otclFileDto.metadata.objectTypes.source;
			reverseOtclFileDto.otclScripts = new ArrayList<>();
			for (OtclScript otclScript : otclFileDto.otclScripts) {
				OtclScript reverseOtclScript = new OtclScript();
				reverseOtclFileDto.otclScripts.add(reverseOtclScript);
				if (otclScript.copy != null) {
					reverseOtclScript.copy = new Copy();
					reverseOtclScript.copy.id = otclScript.copy.id;
					if (otclScript.copy.from.otclChain != null) {
						reverseOtclScript.copy.to = new Target();
						reverseOtclScript.copy.to.otclChain = otclScript.copy.from.otclChain;
						reverseOtclScript.copy.from = new Source();
						reverseOtclScript.copy.from.otclChain = otclScript.copy.to.otclChain;
						if (otclScript.copy.from.overrides != null) {
							if (reverseOtclScript.copy.to.overrides == null) {
								reverseOtclScript.copy.to.overrides = new ArrayList<>();
							}
							for (OtclFileDto.Override sourceOverride : otclScript.copy.from.overrides) {
								Target.Override override = new Target.Override();
								reverseOtclScript.copy.to.overrides.add(override);
								override.tokenPath = sourceOverride.tokenPath;
								override.getter = sourceOverride.getter;
								override.getterHelper = sourceOverride.getterHelper;
							}
						} 
						if (otclScript.copy.to.overrides != null) {
							if (reverseOtclScript.copy.from.overrides == null) {
								reverseOtclScript.copy.from.overrides = new ArrayList<>();
							}
							for (Target.Override targetOverride : otclScript.copy.to.overrides) {
								OtclFileDto.Override override = new OtclFileDto.Override();
								reverseOtclScript.copy.from.overrides.add(override);
								override.tokenPath = targetOverride.tokenPath;
								override.getter = targetOverride.getter;
								override.getterHelper = targetOverride.getterHelper;	
							}
						}
					} else if (otclScript.execute != null) {
						reverseOtclScript.execute = new Execute();
						reverseOtclScript.execute.id = otclScript.execute.id;
						reverseOtclScript.execute.target = new Target();
						reverseOtclScript.execute.target.otclChain = otclScript.execute.source.otclChain;
						reverseOtclScript.execute.source = new Execute.Source();
						reverseOtclScript.execute.source.otclChain = otclScript.execute.target.otclChain;
						if (otclScript.execute.source.overrides != null) {
							if (reverseOtclScript.execute.target.overrides == null) {
								reverseOtclScript.execute.target.overrides = new ArrayList<>();
							}
							for (OtclFileDto.Override sourceOverride : otclScript.execute.source.overrides) {
								Target.Override override = new Target.Override();
								reverseOtclScript.execute.target.overrides.add(override);
								override.tokenPath = sourceOverride.tokenPath;
								override.getter = sourceOverride.getter;
								override.getterHelper = sourceOverride.getterHelper;
							}
						} 
						if (otclScript.execute.target.overrides != null) {
							if (reverseOtclScript.execute.source.overrides == null) {
								reverseOtclScript.execute.source.overrides = new ArrayList<>();
							}
							for (Target.Override targetOverride : otclScript.execute.target.overrides) {
								OtclFileDto.Override override = new OtclFileDto.Override();
								reverseOtclScript.execute.source.overrides.add(override);
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
			otclFileDto.fileName = OtclUtils.createOtclFileName(targetCls, sourceCls);
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
