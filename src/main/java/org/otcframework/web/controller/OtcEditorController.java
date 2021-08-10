/**
* Copyright (c) otcframework.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcframework.web.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.otcframework.common.OtcConstants.TARGET_SOURCE;
import org.otcframework.common.dto.otc.OtcFileDto;
import org.otcframework.common.util.OtcUtils;
import org.otcframework.web.commons.dto.ClassMetadataDto;
import org.otcframework.web.commons.dto.JstreeNodeInfo;
import org.otcframework.web.commons.service.OtcEditorService;
import org.otcframework.web.commons.util.JsTreeNodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ComponentScan(basePackages= {"org.otc.web"})
public class OtcEditorController {

	private static Logger LOGGER = LoggerFactory.getLogger(OtcEditorController.class);
	
	@Autowired
	private OtcEditorService otcEditorService;

	public static final String URL_SHOW_TYPES ="/showTypes";
	public static final String URL_FETCH_SOURCE_JSTREE_HIERARCHY ="/fetchSourceJsTreeData";
	public static final String URL_FETCH_TARGET_JSTREE_HIERARCHY ="/fetchTargetJsTreeData";
	public static final String URL_FETCH_JSTREE_HIERARCHY ="/fetchJsTreeData";
	public static final String URL_CREATE_OTCFILE ="/createOtcFile";
	public static final String URL_FLIP_OTC ="/flipOtc";

	@GetMapping(value=URL_SHOW_TYPES, produces={"application/json;charset=UTF-8"})
	public @ResponseBody Set<String> getClassNames(@RequestParam(name = "pkgName") String pkgName) {
		Set<String> lstClsName = otcEditorService.findTypeNamesInPackage(pkgName);
		return lstClsName;
	}

	@GetMapping(value=URL_FETCH_SOURCE_JSTREE_HIERARCHY)
	public @ResponseBody Map<String, List<JstreeNodeInfo>> fetchSourceJsTree(@RequestParam(name = "srcClsName")
			String srcClsName) {
		Map<String, List<JstreeNodeInfo>> mapFields = new HashMap<>();
		if (!StringUtils.isEmpty(srcClsName)) { 
			List<ClassMetadataDto> lstSrcFields = otcEditorService.createMembersHierarchy(srcClsName,
					TARGET_SOURCE.SOURCE);
			mapFields.put("sourceFieldNames", JsTreeNodeUtil.convert(lstSrcFields));
		}
		return mapFields;
	}

	@GetMapping(value=URL_FETCH_TARGET_JSTREE_HIERARCHY)
	public @ResponseBody Map<String, List<JstreeNodeInfo>> fetchTargetJsTree(@RequestParam(name = "targetClsName")
			String targetClsName) {
		Map<String, List<JstreeNodeInfo>> mapFields = new HashMap<>();
		if (!StringUtils.isEmpty(targetClsName)) { 
			List<ClassMetadataDto> lstTargetFields = otcEditorService.createMembersHierarchy(targetClsName,
					TARGET_SOURCE.TARGET);
			mapFields.put("targetFieldNames", JsTreeNodeUtil.convert(lstTargetFields));
		}
		return mapFields;
	}

	@GetMapping(value=URL_FETCH_JSTREE_HIERARCHY)
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

	@PostMapping(value=URL_CREATE_OTCFILE)
	public void createMapper(@RequestParam(name = "otcInstructions") String otcInstructions, HttpServletResponse response) {
		response.setContentType("text/plain");
	    ServletOutputStream out;
		try {
			OtcFileDto otcFileDto = otcEditorService.createOtcFileDto(otcInstructions, false);
			String sourceClsName  = otcFileDto.metadata.objectTypes.source; 
			String targetClsName = otcFileDto.metadata.objectTypes.target;
			String otcFileName = OtcUtils.createOtcFileName(sourceClsName, targetClsName);
		    response.setHeader("Content-Disposition","attachment;filename=" + otcFileName);
			out = response.getOutputStream();
		    out.println(otcInstructions);
		    out.flush();
		    out.close();
		} catch (IOException e) {
			LOGGER.error("", e);
		}
        return;
	}

	@PostMapping(value=URL_FLIP_OTC)
	public @ResponseBody String flipOtc(@RequestParam(name = "otcInstructions") String otcInstructions) {
		OtcFileDto otcFileDto = otcEditorService.createOtcFileDto(otcInstructions, true);
		otcInstructions = otcEditorService.createYaml(otcFileDto);
		return otcInstructions;
	}

}
