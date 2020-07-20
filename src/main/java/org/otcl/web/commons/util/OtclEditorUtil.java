/**
* Copyright (c) otcl2.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcl.web.commons.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.otcl.common.OtclConstants;
import org.otcl.common.config.OtclConfig;
import org.otcl.common.util.CommonUtils;
import org.otcl.common.util.IncludePackagesUtil;
import org.otcl.web.commons.dto.ClassMetadataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtclEditorUtil {
	
	private static Logger LOGGER = LoggerFactory.getLogger(OtclEditorUtil.class);
	private static String otclLibLocation = OtclConfig.getOtclLibLocation();
	private static FileFilter jarFileFilter = CommonUtils.createFilenameFilter(".jar");
	private static URLClassLoader clzLoader;
	private static Set<String> jarFilesLoaded;
	private static int recursionDepth = 2;

	public static Set<String> findTypeNamesInPackage(String basePackage) {
		File otclLibDirectory = new File(otclLibLocation);
		if (clzLoader == null) {
			clzLoader = CommonUtils.loadURLClassLoader(otclLibLocation);
		}
		Set<String> lstClassNames = fetchFileNamesRecursive(otclLibDirectory, jarFileFilter, null, basePackage);
	    return lstClassNames;
	}
	
	private static Set<String> fetchFileNamesRecursive(File directory, FileFilter fileFilter, Set<String> clzNames,
			String basePackage) {
        if (!IncludePackagesUtil.shouldIncludePackage(basePackage)) {
        	return null;
        }
		for (File file : directory.listFiles(fileFilter)) {
			if (file.isDirectory()) {
				if (clzNames == null) {
					clzNames = fetchFileNamesRecursive(file, fileFilter, clzNames, basePackage);
				} else {
					clzNames.addAll(fetchFileNamesRecursive(file, fileFilter, clzNames, basePackage));
				}
			} else {
				JarFile jarFile = null;
				try {
					jarFile = new JarFile(file);
					Enumeration<JarEntry> jarEntries = jarFile.entries();
	            	String fileName = file.getAbsolutePath();
	            	boolean isClzFileAlreadyLoaded = false;
					if (jarFilesLoaded != null && jarFilesLoaded.contains(fileName)) {
						isClzFileAlreadyLoaded = true;
					} else {
						if (jarFilesLoaded == null) {
							jarFilesLoaded = new HashSet<>();
						}
						jarFilesLoaded.add(fileName);
					}
					for (JarEntry jarEntry = null; jarEntries.hasMoreElements()
				            && ((jarEntry = jarEntries.nextElement()) != null);) {
				        String clzName = jarEntry.getName();
				        if (!clzName.endsWith(".class")) {
				        	continue;
				        }
				        clzName = clzName.substring(0, clzName.lastIndexOf(".")).replace('/', '.');
				        String pkgName = clzName.substring(0, clzName.lastIndexOf("."));
			            if (!pkgName.equals(basePackage)) {
			            	continue;
			            }
			            if (clzNames != null && clzNames.contains(clzName)) {
			            	continue;
			            }
						if (clzNames == null) {
							clzNames = new TreeSet<>();
						}
		            	clzNames.add(clzName);
		            	if (!isClzFileAlreadyLoaded) {
		            		clzLoader.loadClass(clzName);
		            	}
					}
				} catch (IOException | ClassNotFoundException ex) {
					LOGGER.warn("", ex);
				} finally {
					try {
						clzLoader.close();
					} catch (IOException e) {
						LOGGER.warn("Problem closing ClassLoader!", e);
					}
					try {
						jarFile.close();
					} catch (IOException e) {
						LOGGER.warn("Problem closing Jarfile!", e);
					}
				}
			}
		}
		return clzNames;
	}
	
	public static List<ClassMetadataDto> createMembersHierarchy(String clsName) {
		if (clzLoader == null) {
			clzLoader = CommonUtils.loadURLClassLoader(otclLibLocation);
		}
		Class<?> clz = null;
		try {
			clz = clzLoader.loadClass(clsName);
		} catch (ClassNotFoundException ex) {
			LOGGER.warn(ex.getMessage());
		} finally {
			try {
				clzLoader.close();
			} catch (IOException e) {
				LOGGER.warn(e.getMessage());
			}
		}
		return createClassMetadataDtos(clz);		
	}
	
	private static List<ClassMetadataDto> createClassMetadataDtos(Class<?> clz) {
		List<ClassMetadataDto> membersClassMetadataDtos = createClassMetadataDtos(null, clz, null, null, null, recursionDepth, null);
		String txt = clz.getName();
		ClassMetadataDto classMetadataDto = ClassMetadataDto.newBuilder()
			.addId("otcl-root")
			.addText(txt)
			.addChildren(membersClassMetadataDtos)
			.build();
		List<ClassMetadataDto> classMetadataDtos = new ArrayList<>();
		classMetadataDtos.add(classMetadataDto);
		return classMetadataDtos;
	}

	private static List<ClassMetadataDto> createClassMetadataDtos(Class<?> parentClz, Class<?> clz, String displayId, 
			String mapPrefix, Map<Class<?>, List<ClassMetadataDto>> mapRegistry, int recursiveChildrenDepthCount, 
			Set<String> compiledOtclChains) {
		if (!IncludePackagesUtil.shouldIncludePackage(clz)) {
			return null;
		}
		if (mapRegistry != null) {
			if (mapRegistry.containsKey(clz)) {
				if (recursiveChildrenDepthCount < recursionDepth) {
					return mapRegistry.get(clz);
				}
				if (clz == parentClz) {
					recursiveChildrenDepthCount++;
				}
			}
		} else {
			mapRegistry = new HashMap<>();
			compiledOtclChains = new HashSet<String>();
		}
		List<ClassMetadataDto> classMetadataDtos = new ArrayList<>();
		mapRegistry.put(clz, classMetadataDtos);
		List<ClassMetadataDto> membersClassMetadataDtos = createClassMetadataDtosRecursive(clz, clz.isEnum(), 
				mapPrefix, mapRegistry, recursiveChildrenDepthCount, compiledOtclChains, displayId);
		classMetadataDtos.addAll(membersClassMetadataDtos);
		return classMetadataDtos; 
	}
	
	private static List<ClassMetadataDto> createClassMetadataDtosRecursive(Class<?> clz, boolean isParentEnum, 
			String mapPrefix, Map<Class<?>, List<ClassMetadataDto>> mapRegistry, int recursiveChildrenDepthCount, 
			Set<String> compiledIds, String displayId) {
		if (!IncludePackagesUtil.shouldIncludePackage(clz)) {
			return null;
		}
		Map<String, Field> fields = fetchfields(clz, null);
		if (fields == null || fields.size() == 0) {
			return null;
		}
		List<ClassMetadataDto> classMetadataDtos = null;
		for (Entry<String, Field> entry : fields.entrySet()) {
			Field field = entry.getValue();
			Class<?> fieldType = field.getType();
			Type genericType = field.getGenericType();
			String propName = entry.getKey();
			boolean isEnum = fieldType.isEnum();
			if ((isEnum && isParentEnum) || (isParentEnum && propName.endsWith("$VALUES"))) {
				continue;
			}
			StringBuilder txtBuilder = new StringBuilder();
			txtBuilder.append(propName);
			if (isEnum) {
				txtBuilder.append(" (*ENUM: ");
			} else {
				txtBuilder.append(" (");
			}
			boolean isMap = false;
			if (Map.class.isAssignableFrom(fieldType)) {
				isMap = true;
			} else if (fieldType == genericType) {
				genericType = null;
				txtBuilder.append(fieldType.getTypeName().replace("[]", ""));
			}
			Class<?> keyType = null;
			Class<?> valueType = null;
			if (isMap) {
				ParameterizedType parameterizedType = (ParameterizedType) genericType;
				keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
				valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
				txtBuilder.append(fieldType.getName()).append("<K,V>");
			} else if (genericType instanceof ParameterizedType) {
				genericType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
				if (genericType instanceof ParameterizedType) {
					genericType = Object.class;
				}
				txtBuilder.append(fieldType.getName()).append("<").append(((Class<?>)genericType).getName()).append(">");
			} else if (fieldType.isArray()) {
				txtBuilder.append("[]");
			}
			txtBuilder.append(")");			
			StringBuilder otclChain = new StringBuilder();
			if (displayId == null) {
				otclChain.append(propName);
			} else {
				otclChain.append(displayId).append(".").append(propName);
			}
			if (isMap) {
				otclChain.append(OtclConstants.MAP_REF);
			} else if (fieldType.isArray() || Collection.class.isAssignableFrom(fieldType)) {
				otclChain.append(OtclConstants.ARR_REF);
			}
			String otclId = otclChain.toString();
			if (compiledIds.contains(otclId)) {
				continue;
			}
			compiledIds.add(otclId);
			ClassMetadataDto.Builder builder = ClassMetadataDto.newBuilder()
				.addId(otclId)
				.addText(txtBuilder.toString());
			if (isMap) {
				String keyRef = otclId + OtclConstants.MAP_KEY_REF;
				String displayText = OtclConstants.MAP_KEY_REF + " (".concat(keyType.getName()).concat(")"); 
				ClassMetadataDto.Builder keyClassMetadataDtoBuilder = ClassMetadataDto.newBuilder()
						.addId(keyRef)
						.addText(displayText);
				if (IncludePackagesUtil.shouldIncludePackage(keyType)) {
					keyClassMetadataDtoBuilder.addChildren(createClassMetadataDtos(null, keyType, keyRef, 
							OtclConstants.MAP_KEY_REF, mapRegistry, recursiveChildrenDepthCount, compiledIds));
				}
				builder.addChild(keyClassMetadataDtoBuilder.build());
				String valueRef = otclId + OtclConstants.MAP_VALUE_REF;
				displayText = OtclConstants.MAP_VALUE_REF + " (".concat(valueType.getName()).concat(")");
				ClassMetadataDto.Builder valueClassMetadataDtoBuilder = ClassMetadataDto.newBuilder()
						.addId(valueRef)
						.addText(displayText);
				if (IncludePackagesUtil.shouldIncludePackage(valueType)) {
					valueClassMetadataDtoBuilder.addChildren(createClassMetadataDtos(null, valueType, valueRef, 
							OtclConstants.MAP_VALUE_REF, mapRegistry, recursiveChildrenDepthCount, compiledIds));
				}
				builder.addChild(valueClassMetadataDtoBuilder.build());
			} else {
				Class<?> typeOrGenericType = (genericType == null ? fieldType : (Class<?>) genericType);
				if (IncludePackagesUtil.shouldIncludePackage(typeOrGenericType)) {
					builder.addChildren(createClassMetadataDtos(clz, typeOrGenericType, otclId, null, mapRegistry, 
							recursiveChildrenDepthCount, compiledIds));
				}
			}
			ClassMetadataDto classMetadataDto = builder.build();
			if (classMetadataDtos == null) {
				classMetadataDtos = new ArrayList<>();
			}
			classMetadataDtos.add(classMetadataDto);
		}
		return classMetadataDtos;
	}
	
	private static Map<String, Field> fetchfields(Class<?> clz, Map<String, Field> fields) {
		if (!IncludePackagesUtil.shouldIncludePackage(clz)) {
			return fields;
		}
		for (Field field : clz.getDeclaredFields()) {
			if (fields == null) {
				fields = new LinkedHashMap<>();
			}
			String fieldName = field.getName();
			if (!fields.containsKey(fieldName)) {
				fields.put(fieldName, field);
			}
		}
		Class<?> superClz = clz.getSuperclass();
		Map<String, Field> superClzFields = fetchfields(superClz, fields);
		if (superClzFields != null) {
			if (fields == null) {
				fields = superClzFields;
			} else {
				fields.putAll(superClzFields);
			}
		}
		return fields;
	}
} 
