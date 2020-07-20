/**
* Copyright (c) otcl2.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcl.web.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.otcl.common.config.OtclConfig;
import org.otcl.common.engine.compiler.dto.OtclFileDto;
import org.otcl.common.engine.compiler.dto.OtclFileDto.ExecuteOtclConverter;
import org.otcl.common.engine.compiler.dto.OtclFileDto.ExecuteOtclModule;
import org.otcl.common.engine.compiler.dto.OtclFileDto.Metadata;
import org.otcl.common.engine.compiler.dto.OtclFileDto.OtclScript;
import org.otcl.common.engine.compiler.dto.OtclFileDto.SetValue;
import org.otcl.common.util.OtclUtils;
import org.otcl.web.commons.dto.ClassMetadataDto;
import org.otcl.web.commons.dto.JstreeNodeInfo;
import org.otcl.web.commons.service.OtclEditorService;
import org.otcl.web.commons.util.JsTreeNodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class OtclEditorController {

	private static Logger LOGGER = LoggerFactory.getLogger(OtclEditorController.class);
	
	@Autowired
	private OtclEditorService otclEditorService;

	public static final String URL_MAIN ="/main";
	public static final String URL_SHOW_TYPES ="/showTypes";
	public static final String URL_SHOWCONVERTERS ="/showConverters";
	public static final String URL_FETCH_JSTREE_HIERARCHY ="/fetchJsTreeData";
	public static final String URL_CREATE_OTCLFILE ="/createOtclFile";
		
	@GetMapping("/")
	public String showMapper() {
		return URL_MAIN;
	}

	@GetMapping(value=URL_SHOW_TYPES, produces={"application/json;charset=UTF-8"})
	public @ResponseBody Set<String> getClassNames(@RequestParam(name = "pkgName") String pkgName) {
		Set<String> lstClsName = otclEditorService.findTypeNamesInPackage(pkgName);
		return lstClsName;
	}

	@GetMapping(value=URL_SHOWCONVERTERS, produces={"application/json;charset=UTF-8"})
	public @ResponseBody List<String> getConvertors() {
		return OtclConfig.getOtclConverters();
	}
	
	@GetMapping(URL_FETCH_JSTREE_HIERARCHY)
	public @ResponseBody Map<String, List<JstreeNodeInfo>> fetchJsTree(@RequestParam(name = "srcClsName") String srcClsName,
			@RequestParam(name = "targetClsName") String targetClsName) {
		Map<String, List<JstreeNodeInfo>> mapFields = new HashMap<>();
		List<ClassMetadataDto> lstSrcFields = null;
		if (!StringUtils.isEmpty(srcClsName)) { 
			lstSrcFields = otclEditorService.createMembersHierarchy(srcClsName);
			mapFields.put("sourceFieldNames", JsTreeNodeUtil.convert(lstSrcFields));
		}
		List<ClassMetadataDto> lstTargetFields = null;
		if (!StringUtils.isEmpty(targetClsName)) { 
			lstTargetFields = otclEditorService.createMembersHierarchy(targetClsName);
			mapFields.put("targetFieldNames", JsTreeNodeUtil.convert(lstTargetFields));
		}
		return mapFields;
	}

	@PostMapping(value=URL_CREATE_OTCLFILE)
	public void createMapper(@RequestParam String targetClsNames, @RequestParam String srcClsNames, 
			@RequestParam String otclInstructions, @RequestParam boolean reverseOtclFile, HttpServletResponse response) {
		String otclFileName = null;
		response.setContentType("text/plain");
	    ServletOutputStream out;
		try {
			if (reverseOtclFile) {
				OtclFileDto otclFileDto = otclEditorService.createOtclFileDto(targetClsNames, srcClsNames, 
						otclInstructions, reverseOtclFile);
				otclInstructions = createYaml(otclFileDto);
				otclFileName = otclFileDto.fileName;
			} else {
				otclFileName = OtclUtils.createOtclFileName(targetClsNames, srcClsNames);
			}
		    response.setHeader("Content-Disposition","attachment;filename=" + otclFileName);
			out = response.getOutputStream();
		    out.println(otclInstructions);
		    out.flush();
		    out.close();
		} catch (IOException e) {
			LOGGER.error("", e);
		}
        return;
	}
	
	private String createYaml(OtclFileDto otclFileDto) {
		// ---- issue with Yaml's dump() method - it does not retain order of properties 
		//      and hence this utility method.
		StringBuilder yamlBuilder = new StringBuilder();
		OtclFileDto.Metadata metadata = otclFileDto.metadata;
		if (metadata != null) {
			Metadata.Labels labels = metadata.labels;
			yamlBuilder.append("metadata:\n");
			yamlBuilder.append("  labels:\n");
			if (labels != null) {
				yamlBuilder.append("    targetType: ").append(labels.targetType == null ? "" : labels.targetType);
				yamlBuilder.append("\n");
				yamlBuilder.append("    sourceType: ").append(labels.sourceType == null ? "" : labels.sourceType);
				yamlBuilder.append("\n");
				yamlBuilder.append("  objectFactory: ").append(metadata.objectFactory == null ? "" : metadata.objectFactory);
				yamlBuilder.append("\n");
			}
			yamlBuilder.append("scripts:");
			yamlBuilder.append("\n");
			for (OtclScript script : otclFileDto.otclScripts) {
				yamlBuilder.append("  - targetOtclChain: ").append(script.targetChainProfile.otclChain == null ? "" : 
					script.targetChainProfile.otclChain);
				yamlBuilder.append("\n");
				if (script.sourceChainProfile.otclChain != null) {
					yamlBuilder.append("    sourceOtclChain: ").append(script.sourceChainProfile.otclChain);
					yamlBuilder.append("\n");
				}
				SetValue setValue = script.setValue;
				if (setValue != null) {
					yamlBuilder.append("    setValue: ");
					yamlBuilder.append("\n");
					if (setValue.values != null) {
						yamlBuilder.append("      values: ").append(setValue.values);
						yamlBuilder.append("\n");
					}
					if (setValue.value != null) {
						yamlBuilder.append("      value: ").append(setValue.value);
						yamlBuilder.append("\n");
					}
				}
				ExecuteOtclConverter executeOtclConverter = script.executeOtclConverter;
				if (executeOtclConverter != null) {
					yamlBuilder.append("    executeOtclConverter: ");
					yamlBuilder.append("\n");
					if (executeOtclConverter.otclConverter != null) {
						yamlBuilder.append("      otclConverter: ").append(executeOtclConverter.otclConverter);
						yamlBuilder.append("\n");
					}					
				}
				ExecuteOtclModule executeOtclModule = script.executeOtclModule;
				if (executeOtclModule != null) {
					yamlBuilder.append("    executeOtclModule: ");
					yamlBuilder.append("\n");
					if (executeOtclModule.otclNamespace != null) {
						yamlBuilder.append("      otclNamespace: ").append(executeOtclModule.otclNamespace);
						yamlBuilder.append("\n");
					}					
				}
				yamlBuilder.append("\n");
			}
		}
		return yamlBuilder.toString();
	}
}
