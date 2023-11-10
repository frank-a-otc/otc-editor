/**
* Copyright (c) otcframework.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcframework.web.commons.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

import org.otcframework.common.OtcConstants;
import org.otcframework.common.OtcConstants.TARGET_SOURCE;
import org.otcframework.common.config.OtcConfig;
import org.otcframework.common.util.CommonUtils;
import org.otcframework.common.util.OtcUtils;
import org.otcframework.common.util.PackagesFilterUtil;
import org.otcframework.web.commons.dto.ClassMetadataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtcEditorUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OtcEditorUtil.class);
	private static final String otcLibLocation = OtcConfig.getOtcLibLocation();
	private static final FileFilter jarFileFilter = CommonUtils.createFilenameFilter(".jar");
	private static Set<String> jarFilesLoaded;
	private static final int CYCLIC_DEPENDENCY_DEPTH = 2;
	private static final String SOURCE_ROOT = "source-root";
	private static final String TARGET_ROOT = "target-root";

	public static Set<String> findTypeNamesInPackage(String basePackage) {
		File otcLibDirectory = new File(otcLibLocation);
		Set<String> lstClassNames = fetchFileNamesRecursive(otcLibDirectory, jarFileFilter, null, basePackage);
	    return lstClassNames;
	}
	
	private static Set<String> fetchFileNamesRecursive(File directory, FileFilter fileFilter, Set<String> clzNames,
			String basePackage) {
        if (!PackagesFilterUtil.isFilteredPackage(basePackage)) {
        	return null;
        }
		File[] files = directory.listFiles(fileFilter);
		if (files == null) {
			return null;
		}
		for (File file : files) {
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
						String pkgName;
						if (clzName.contains(".")) {
							pkgName = clzName.substring(0, clzName.lastIndexOf("."));
						} else {
							pkgName = "";
						}
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
		            		OtcUtils.loadClass(clzName);
		            	}
					}
				} catch (Exception ex) {
					LOGGER.warn("", ex);
				} finally {
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
	
	public static List<ClassMetadataDto> createMembersHierarchy(String clsName, TARGET_SOURCE targetSource) {
		Class<?> clz = null;
		try {
			clz = OtcUtils.loadClass(clsName);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			throw ex;
		}
		return createClassMetadataDtos(clz, targetSource);		
	}
	
	private static List<ClassMetadataDto> createClassMetadataDtos(Class<?> clz, TARGET_SOURCE targetSource) {
		List<ClassMetadataDto> membersClassMetadataDtos = createClassMetadataDtos(null, clz, null,
				null, CYCLIC_DEPENDENCY_DEPTH, null, targetSource);
		String txt = clz.getName().concat(" - (right-click for context-menu)");
		ClassMetadataDto classMetadataDto = ClassMetadataDto.newBuilder()
			.addId(TARGET_SOURCE.TARGET == targetSource ? TARGET_ROOT : SOURCE_ROOT)
			.addText(txt)
			.addChildren(membersClassMetadataDtos)
			.build();
		List<ClassMetadataDto> classMetadataDtos = new ArrayList<>();
		classMetadataDtos.add(classMetadataDto);
		return classMetadataDtos;
	}

	private static List<ClassMetadataDto> createClassMetadataDtos(Class<?> parentClz, Class<?> clz,
			String displayId, Map<Class<?>, List<ClassMetadataDto>> mapRegistry, int cyclicDependencyDepthCount,
			Set<String> compiledOtcChains, TARGET_SOURCE targetSource) {
		if (!PackagesFilterUtil.isFilteredPackage(clz)) {
			return null;
		}
		if (mapRegistry != null) {
			if (mapRegistry.containsKey(clz)) {
				if (cyclicDependencyDepthCount < CYCLIC_DEPENDENCY_DEPTH) {
					return mapRegistry.get(clz);
				}
				if (clz == parentClz) {
					if (cyclicDependencyDepthCount == CYCLIC_DEPENDENCY_DEPTH) {
						return null;
					}
					cyclicDependencyDepthCount++;
				}
			}
		} else {
			mapRegistry = new HashMap<>();
			compiledOtcChains = new HashSet<String>();
		}
		List<ClassMetadataDto> classMetadataDtos = new ArrayList<>();
		mapRegistry.put(clz, classMetadataDtos);
		List<ClassMetadataDto> membersClassMetadataDtos = createClassMetadataDtosRecursive(clz,
				mapRegistry, cyclicDependencyDepthCount, compiledOtcChains, displayId, targetSource);
		if (membersClassMetadataDtos != null) {
			classMetadataDtos.addAll(membersClassMetadataDtos);
		}
		return classMetadataDtos; 
	}
	
	private static List<ClassMetadataDto> createClassMetadataDtosRecursive(Class<?> clz,
			Map<Class<?>, List<ClassMetadataDto>> mapRegistry, int recursiveChildrenDepthCount, 
			Set<String> compiledIds, String displayId, TARGET_SOURCE targetSource) {
		if (!PackagesFilterUtil.isFilteredPackage(clz)) {
			return null;
		}
		List<ClassMetadataDto> classMetadataDtos = null;
		Map<String, Field> fields = fetchfields(clz, null);
		if (fields == null || fields.size() == 0) {
			return null;
		}
		boolean isParentEnum = clz.isEnum();
		for (Entry<String, Field> entry : fields.entrySet()) {
			Field field = entry.getValue();
			Class<?> fieldType = field.getType();
			String propName = entry.getKey();
			boolean isEnum = fieldType.isEnum();
			if ((isEnum && isParentEnum) || (isParentEnum && propName.endsWith("$VALUES"))) {
				continue;
			}
			classMetadataDtos = createUiNode(field, clz, mapRegistry, recursiveChildrenDepthCount,
					compiledIds, displayId, classMetadataDtos, targetSource);
		}
		
		return classMetadataDtos;
	}
	
	private static List<ClassMetadataDto> createUiNode(Field field, Class<?> clz,
			Map<Class<?>, List<ClassMetadataDto>> mapRegistry, int recursiveChildrenDepthCount, 
			Set<String> compiledIds, String displayId, List<ClassMetadataDto> classMetadataDtos, TARGET_SOURCE targetSource) {
		Class<?> fieldType = field.getType();
		Type genericType = field.getGenericType();
		String propName = field.getName();
		StringBuilder otcChain = new StringBuilder();
		if (displayId != null) {
			otcChain.append(displayId).append(".");
		}
		otcChain.append(propName);
		if (Map.class.isAssignableFrom(fieldType)) {
			otcChain.append(OtcConstants.MAP_REF);
		} else if (Collection.class.isAssignableFrom(fieldType) || fieldType.isArray()) {
			otcChain.append(OtcConstants.ARR_REF);
		}
		String otcId = otcChain.toString();
		if (compiledIds.contains(otcId)) {
			return classMetadataDtos;
		}
		compiledIds.add(otcId);
		StringBuilder txtBuilder = new StringBuilder();
		txtBuilder.append(propName);
		Class<?> keyType = null;
		Class<?> valueType = null;
		boolean isMap = false;
		ClassMetadataDto.Builder builder = ClassMetadataDto.newBuilder()
				.addId(otcId);
		Class<?> childType = null;
		StringBuilder partialTxtBuilder = new StringBuilder();
		if (Map.class.isAssignableFrom(fieldType)) {
			isMap = true;
			txtBuilder.append(" : ").append(fieldType.getName()).append(OtcConstants.MAP_REF);
			builder.addText(txtBuilder.toString());
			ParameterizedType parameterizedType = (ParameterizedType) genericType;
			keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
			valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
			String keyRef = otcId + OtcConstants.MAP_KEY_REF;
			String displayText = OtcConstants.MAP_KEY_REF;
			displayText += " : ";
			if (keyType.isEnum()) {
				displayText += "*ENUM: ";
			}
			displayText += keyType.getName(); // + ")";
			ClassMetadataDto.Builder keyClassMetadataDtoBuilder = ClassMetadataDto.newBuilder()
					.addId(keyRef)
					.addText(displayText);
			if (PackagesFilterUtil.isFilteredPackage(keyType)) {
				keyClassMetadataDtoBuilder.addChildren(createClassMetadataDtos(null, keyType, keyRef,
						mapRegistry, recursiveChildrenDepthCount, compiledIds, targetSource));
			}
			builder.addChild(keyClassMetadataDtoBuilder.build());
			String valueRef = otcId + OtcConstants.MAP_VALUE_REF;
			displayText = OtcConstants.MAP_VALUE_REF;
			displayText += " : ";
			if (valueType.isEnum()) {
				displayText += " *ENUM: ";
			}
			displayText += valueType.getName(); // + ")";
			ClassMetadataDto.Builder valueClassMetadataDtoBuilder = ClassMetadataDto.newBuilder()
					.addId(valueRef)
					.addText(displayText);
			if (PackagesFilterUtil.isFilteredPackage(valueType)) {
				valueClassMetadataDtoBuilder.addChildren(createClassMetadataDtos(null, valueType, valueRef,
						mapRegistry, recursiveChildrenDepthCount, compiledIds, targetSource));
			}
			builder.addChild(valueClassMetadataDtoBuilder.build());
		} else if (fieldType.isArray()) {
			txtBuilder.append("[]");
			genericType = fieldType.getComponentType();
			childType = (Class<?>) genericType;
			partialTxtBuilder.append(childType.getName()); //.append(")");
		} else if (Collection.class.isAssignableFrom(fieldType)) {
			genericType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
			if (genericType instanceof ParameterizedType) {
				genericType = Object.class;
			}
			childType = (Class<?>) genericType;
			partialTxtBuilder.append(fieldType.getName()).append("<").append(childType.getName()).append(">");
//				.append(")");
		} else {
			childType = fieldType;
			genericType = null;
			partialTxtBuilder.append(fieldType.getTypeName()); //.append(")");
		}
		if (!isMap) {
			txtBuilder.append(" : ");
			if (childType.isEnum()) {
				txtBuilder.append(" *ENUM: ");
			}
			txtBuilder.append(partialTxtBuilder);
			builder.addText(txtBuilder.toString());
			if (PackagesFilterUtil.isFilteredPackage(childType)) {
				builder.addChildren(createClassMetadataDtos(clz, childType, otcId, mapRegistry,
						recursiveChildrenDepthCount, compiledIds, targetSource));
			}
		}
		if (!field.getDeclaringClass().isEnum()) {
			String getter;
			try {
				OtcEditorReflectionUtil.findGetterName(field, otcId);
			} catch (NoSuchMethodException e) {
				// exception already logged by OtcEditorReflectionUtil
				getter = OtcEditorReflectionUtil.findNonJavaBeanGetterName(field);
				if (getter == null) {
					builder.addIsGetterHelperRequired(true);
				} else {
					builder.addIsGetterRequired(true);
					builder.addGetter(getter);
				}
			}

			if (TARGET_SOURCE.TARGET == targetSource) {
				String setter;
				try {
					OtcEditorReflectionUtil.findSetterName(field, otcId);
				} catch (NoSuchMethodException e) {
					// exception already logged by OtcEditorReflectionUtil
					setter = OtcEditorReflectionUtil.findNonJavaBeanSetterName(field);
					if (setter == null) {
						builder.addIsSetterHelperRequired(true);
					} else {
						builder.addIsSetterRequired(true);
						builder.addSetter(setter);
					}
				}
			}
		}
		ClassMetadataDto classMetadataDto = builder.build();
		if (classMetadataDtos == null) {
			classMetadataDtos = new ArrayList<>();
		}
		classMetadataDtos.add(classMetadataDto);
		return classMetadataDtos;
	}

	private static Map<String, Field> fetchfields(Class<?> clz, Map<String, Field> fields) {
		if (!PackagesFilterUtil.isFilteredPackage(clz)) {
			return fields;
		}
		Field[] fieldArr;
		try {
			fieldArr = clz.getDeclaredFields();
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
			return fields;
		}
		for (Field field : fieldArr) {
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
