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

import org.otcl.web.commons.dto.ClassMetadataDto;
import org.otcl.web.commons.dto.JstreeNodeInfo;
import org.otcl.web.commons.service.OtclEditorService;
import org.otcl.web.commons.util.JsTreeNodeUtil;
import org.otcl2.common.OtclConstants.TARGET_SOURCE;
import org.otcl2.common.config.OtclConfig;
import org.otcl2.common.dto.OtclFileDto;
import org.otcl2.common.util.OtclUtils;
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
	public static final String URL_FETCH_SOURCE_JSTREE_HIERARCHY ="/fetchSourceJsTreeData";
	public static final String URL_FETCH_TARGET_JSTREE_HIERARCHY ="/fetchTargetJsTreeData";
	public static final String URL_FETCH_JSTREE_HIERARCHY ="/fetchJsTreeData";
	public static final String URL_CREATE_OTCLFILE ="/createOtclFile";
	public static final String URL_FLIP_OTCL ="/flipOtcl";
	
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
	
	@GetMapping(URL_FETCH_SOURCE_JSTREE_HIERARCHY)
	public @ResponseBody Map<String, List<JstreeNodeInfo>> fetchSourceJsTree(@RequestParam(name = "srcClsName")
			String srcClsName) {
		Map<String, List<JstreeNodeInfo>> mapFields = new HashMap<>();
		if (!StringUtils.isEmpty(srcClsName)) { 
			List<ClassMetadataDto> lstSrcFields = otclEditorService.createMembersHierarchy(srcClsName,
					TARGET_SOURCE.SOURCE);
			mapFields.put("sourceFieldNames", JsTreeNodeUtil.convert(lstSrcFields));
		}
		return mapFields;
	}

	@GetMapping(URL_FETCH_TARGET_JSTREE_HIERARCHY)
	public @ResponseBody Map<String, List<JstreeNodeInfo>> fetchTargetJsTree(@RequestParam(name = "targetClsName")
			String targetClsName) {
		Map<String, List<JstreeNodeInfo>> mapFields = new HashMap<>();
		if (!StringUtils.isEmpty(targetClsName)) { 
			List<ClassMetadataDto> lstTargetFields = otclEditorService.createMembersHierarchy(targetClsName,
					TARGET_SOURCE.TARGET);
			mapFields.put("targetFieldNames", JsTreeNodeUtil.convert(lstTargetFields));
		}
		return mapFields;
	}

	@GetMapping(URL_FETCH_JSTREE_HIERARCHY)
	public @ResponseBody Map<String, List<JstreeNodeInfo>> fetchJsTree(@RequestParam(name = "srcClsName")
			String srcClsName, @RequestParam(name = "targetClsName") String targetClsName) {
		Map<String, List<JstreeNodeInfo>> mapFields = null;
		if (srcClsName != null) {
			mapFields = fetchSourceJsTree(srcClsName);
		}
		if (targetClsName != null) {
			if (mapFields == null) {
				mapFields = fetchTargetJsTree(targetClsName);
			} else {
				mapFields.putAll(fetchTargetJsTree(targetClsName));
			}
		}
		return mapFields;
	}

	@PostMapping(value=URL_CREATE_OTCLFILE)
	public void createMapper(@RequestParam(name = "otclInstructions") String otclInstructions, HttpServletResponse response) {
		response.setContentType("text/plain");
	    ServletOutputStream out;
		try {
			OtclFileDto otclFileDto = otclEditorService.createOtclFileDto(otclInstructions, false);
			String sourceClsName  = otclFileDto.metadata.objectTypes.source; 
			String targetClsName = otclFileDto.metadata.objectTypes.target;
			String otclFileName = OtclUtils.createOtclFileName(sourceClsName, targetClsName);
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

	@PostMapping(value=URL_FLIP_OTCL)
	public @ResponseBody String flipOtcl(@RequestParam(name = "otclInstructions") String otclInstructions) {
		OtclFileDto otclFileDto = otclEditorService.createOtclFileDto(otclInstructions, true);
		otclInstructions = otclEditorService.createYaml(otclFileDto);
		return otclInstructions;
	}

}
