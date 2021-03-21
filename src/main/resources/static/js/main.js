String.prototype.replaceAt=function(index, character) {
    return this.substr(0, index) + character + this.substr(index+character.length);
};

var pageUrl = "";

$(document).ready(function() {
	$("#otclInstructions").val('');
});


let anchorsMap = new Map();
let sourceMap = new Map();
let targetMap = new Map();

var copyCounter = 0;
var executeCounter = 0;
var selectMsg = "Select...";
var isSrcTree = true;

const CONSTANTS = {
	ANCHOR: "anchor",
	SOURCE_ROOT: "source-root",
	TARGET_ROOT: "target-root",
	GETTER: "getter",
	SETTER: "setter",
	GETTER_HELPER: "getterHelper",
	SETTER_HELPER: "setterHelper",
	CONCRETE_TYPE: "concreteType",
	CMD_COPY: "Copy",
	CMD_EXECUTE: "Execute"
};
Object.freeze(CONSTANTS);

var command = CONSTANTS.CMD_COPY;

var headerTemplate = 
	"metadata:\r\n" +
	"  objectTypes:\r\n" +
	"    source: <<sourceType>>\r\n" +
	"    target: <<targetType>>\r\n" +
	"  entryClassName: <<entryClassName>>\r\n" +
	"  helper: <<helperClassName>>\r\n" +
	"otclCommands:\r\n";

var copyScriptTemplate =
	"- copy:\r\n" +
	"    id: <<id>>\r\n" +
	"    from:\r\n" +
	"      <<from>>\r\n" +
	"      <<fromOverrides>>\r\n" +
	"    to:\r\n" +
	"      <<to>>\r\n" +
	"      <<toOverrides>>\r\n" +
	"    factoryClassName: <<factoryclass>>\r\n";

var executeScriptTemplate =
	"- execute:\r\n" +
	"    id: <<id>>\r\n" +
	"    otclConverter: <<otclConverter>>\r\n" +
	"    otclModule:\r\n" +
	"      otclNamespace: <<otclNamespace>>\r\n" +
	"    executionOrder:\r\n" +
	"    - otclModule\r\n" +
	"    - otclConverter\r\n" +
	"    source:\r\n" +
	"      <<from>>\r\n" +
	"      <<fromOverrides>>\r\n" +
	"    target:\r\n" +
	"      <<to>>\r\n" +
	"      <<toOverrides>>\r\n" +
	"    factoryClassName: <<factoryclass>>";

var otclChainTemplate = 
	"otclChain: <<otclChain>>";

var valuesTemplate = 
	"values:\r\n" +
	"      - <<values>>";

var overridesTemplate = 
	"      overrides:\r\n" +
	"      - tokenPath: <<tokenPath>>\r\n";

var concreteTypeTemplate = 
	"        concreteType: <<concreteType>>\r\n";

var setterTemplate = 
	"        setter: <<setter>>\r\n";

var getterTemplate = 
	"        getter: <<getter>>\r\n";

var setterHelperTemplate =
	"        setterHelper: <<setterHelper>>\r\n";

var getterHelperTemplate =
	"        getterHelper: <<getterHelper>>\r\n";

var idPlaceholder = "<<id>>";
var sourceTypePlaceholder = "<<sourceType>>";
var targetTypePlaceholder = "<<targetType>>";
var fromPlaceholder = "<<from>>";
var valuesPlaceholder = "<<values>>";

var toPlaceholder = "<<to>>";
var otclChainPlaceholder = "<<otclChain>>";
var tokenPathPlaceholder = "<<tokenPath>>";
var fromOverridesPlaceholder = "      <<fromOverrides>>\r\n";
var toOverridesPlaceholder = "      <<toOverrides>>\r\n";

var entryClassNamePlaceholder = "<<entryClassName>>";
var helperClassNamePlaceholder = "<<helperClassName>>";
var getterPlaceholder = "<<getter>>";
var setterPlaceholder = "<<setter>>";
var getterHelperPlaceholder = "<<getterHelper>>";
var setterHelperPlaceholder = "<<setterHelper>>";
var dateFormatPlaceholder = "<<dateFormat>>";
var concreteTypePlaceholder = "<<concreteType>>";
var factoryclassPlaceholder = "<<factoryclass>>";
var otclConverterPlaceholder = "<<otclConverter>>";
var otclNamespacePlaceholder = "<<otclNamespace>>";

var helperParam = "helper:";
var getterHelperParam = "getterHelper:";
var setterHelperParam = "setterHelper:";


function getClassNames(url, eleName) {
	$.ajax({
        url : url,
    	type:"GET",
        contentType:"application/json;charset=utf-8",
        success : function(data) {
        	if (data) {
				var selectHtml = "<option value=''>".concat(selectMsg, "</option>");
	        	$.each(data, function(i, item){
	                selectHtml = selectHtml.concat("<option>", item, "</option>");
	            });
	        	$('#'+eleName).html(selectHtml);
        	}
        }
    });	
}

$("#fetchSrc").click(function() {
	var srcPkgName = $('#srcPkgName').val();
	if (!srcPkgName) {
		return;
	}
	var url = pageUrl.concat('showTypes').concat('?pkgName=').concat(srcPkgName);
	getClassNames(url, 'srcClsNames');
});

$("#fetchTarget").click(function() {
	var targetPkgName = $('#targetPkgName').val();
	if (!targetPkgName) {
		return;
	}
	var url = pageUrl.concat('showTypes').concat('?pkgName=').concat(targetPkgName);
	getClassNames(url, 'targetClsNames');
});

$("#showTree").click(function() {
	$("#otclInstructions").val('');
	var srcClsName = $('#srcClsNames').val();
	var targetClsName = $('#targetClsNames').val();
	var url;
	if (srcClsName != null && srcClsName.trim() != '' && targetClsName != null && targetClsName.trim() != '') {
		url = pageUrl.concat('fetchJsTreeData');
	} else if (srcClsName != null && srcClsName.trim() != '') {
		url = pageUrl.concat('fetchSourceJsTreeData');
	} else if (targetClsName != null && targetClsName.trim() != '') {
		url = pageUrl.concat('fetchTargetJsTreeData');
	}
	var didSucceed = fetchAndPopulateJstree(url);
	if (!didSucceed) {
		return;
	}
    $('#srcTree').on('loaded.jstree', function() {
      	 $("#srcTree").jstree("open_node", $("#" + CONSTANTS.SOURCE_ROOT));
    });

    $('#targetTree').on('loaded.jstree', function() {
   	 $("#targetTree").jstree("open_node", $("#" + CONSTANTS.TARGET_ROOT));
   });
});

var commandItems = {
    "copy": {
    	name: "Copy", 
        type: 'radio', 
        callback: function() {
        	command = CONSTANTS.CMD_COPY;
            return true;
        }
    },
    "execute": {
    	name: "Execute", 
        type: 'radio',
        callback: function() {
        	command = CONSTANTS.CMD_EXECUTE;
            return true;
        }
   	}
};

$(function() {
    $.contextMenuCommon({
        selector: '.context-menu-one', 
        autoHide: true,
        callback: function(key, options) {
            var m = "clicked: " + key;
            window.console && console.log(m) || alert(m); 
        },
        items: commandItems
    });
 
});

var sourceOverrideItems = {
    "none": {
    	name: "None", 
        type: 'radio', 
        callback: function(key, options) {
    		sourceMap.delete(CONSTANTS.GETTER);
        	return false;
        }
    },
    "getterItem": {
    	name: "getter", 
        type: 'radio', 
        callback: function(key, options) {
    		sourceMap.set(CONSTANTS.GETTER, getterTemplate);
        	return false;
        }
    },
    'getterHelperItem' : {
    	name: "getterHelper", 
        type: 'radio', 
        callback: function(key, options) {
    		sourceMap.set(CONSTANTS.GETTER, getterHelperTemplate);
        	return false;
        }
    }
};

var targetContextMenuItems = {
    "noSetter": {
    	name: "None", 
        type: 'radio', 
        radio: "set",
        callback: function(key, options) {
        	targetMap.delete(CONSTANTS.SETTER);
        	return false;
        }
    },
    'setterItem' : {
    	name: "setter", 
        type: 'radio', 
        radio: "set",
        callback: function(key, options) {
        	targetMap.set(CONSTANTS.SETTER, setterTemplate);
        	return false;
        }
    },
    'setterHelperItem' : {
    	name: "setterHelper", 
        type: 'radio', 
        radio: "set",
        callback: function(key, options) {
        	targetMap.set(CONSTANTS.SETTER, setterHelperTemplate);
        	return false;
        }
    },
    separator1: "-----",
    "noGetter": {
    	name: "None", 
        type: 'radio', 
        radio: "get",
        callback: function(key, options) {
        	targetMap.delete(CONSTANTS.GETTER);
        	return false;
        }
    },
    'getterItem' : {
    	name: "getter", 
        type: 'radio', 
        radio: "get",
        callback: function(key, options) {
        	targetMap.set(CONSTANTS.GETTER, getterTemplate);
        	return false;
        }
    },
    'getterHelperItem' : {
    	name: "getterHelper", 
        type: 'radio', 
        radio: "get",
        callback: function(key, options) {
        	targetMap.set(CONSTANTS.GETTER, getterHelperTemplate);
        	return false;
        }
    },
    separator2: "-----",
    'concreteTypeItem' : {
    	name: "concreteType", 
        type: 'checkbox', 
        callback: function(key, options) {
        	if (targetMap.has(CONSTANTS.CONCRETE_TYPE)) {
        		targetMap.delete(CONSTANTS.CONCRETE_TYPE);
        	} else {
        		targetMap.set(CONSTANTS.CONCRETE_TYPE, concreteTypeTemplate);
        	}
        	return false;
        }
    }
};

function jstreeContextmenu(node) {
	var otclChain = node.id;
	if (CONSTANTS.SOURCE_ROOT == otclChain) {
	    $.contextMenuCommon({
	        selector: '#srcTree', 
	        autoHide: true,
	        className: 'data-title',
	        callback: function(key, options) {
	            var m = "clicked: " + key;
//	            window.console && console.log(m) || alert(m); 
	        },
	        items: sourceOverrideItems
	    });
	} else {
		$.contextMenu('destroy', "#targetTree");
		var menuItems;
		var className = 'data-title';
		if (CONSTANTS.TARGET_ROOT == otclChain) {
			menuItems = targetContextMenuItems; 
		} else {
			if (!otclChain.endsWith("]")) {
				return;
	        }
			var isChecked = false;
			if (anchorsMap.has(otclChain)) {
				isChecked = true;
			}
			var anchorItems = {
			   	anchor: {
			   		name: "Anchor", 
			        type: 'checkbox', 
			        selected: isChecked,
			        callback: function(key, opt) { 
			        	if (anchorsMap.has(otclChain)) {
			        		anchorsMap.delete(otclChain);
			        	} else if (otclChain.endsWith("]")) {
			        		var subKey;
			        		for (const [k, v] of anchorsMap.entries()) {
			        	   		if (otclChain.startsWith(k) || k.startsWith(otclChain)) {
			        	   			subKey = k;
			        	   			break;
			        	   		}
			        	   	};
			        	   	if (subKey) {
			        	   		anchorsMap.delete(subKey);
			        	   	}
//			        	   	console.log(node.text);
//			        	   	$("#" + node.id + " > a").addClass('selectedNodeColor');
				        	var lastIndexOf = otclChain.lastIndexOf("[");
				        	var anchoredOtclChain = otclChain.substring(0, lastIndexOf + 1) + "^" + otclChain.substring(lastIndexOf + 1);
				        	anchorsMap.set(otclChain, anchoredOtclChain);
				        }
					}
				}
			};
			menuItems = anchorItems;
			className = '';
		}
		$.contextMenuCommon({
	        selector: '#targetTree', 
	        autoHide: true,
	        className: className,
	        callback: function(key, options) {
	            var m = "clicked: " + key;
//	            window.console && console.log(m) || alert(m); 
	        },
	        items: menuItems
	    });
	}
};

function fetchAndPopulateJstree(url) {
	$('#srcTree').jstree("destroy").empty();
	$('#targetTree').jstree("destroy").empty();
	var srcClsName = $('#srcClsNames').val();
	var targetClsName = $('#targetClsNames').val();
	if (srcClsName != null && srcClsName != selectMsg) {
		url = url.concat('?srcClsName=').concat(srcClsName);
	}
	if (targetClsName != null) {
		if (srcClsName == null) {
			url = url.concat('?targetClsName=').concat(targetClsName);
		} else {
			url = url.concat('&targetClsName=').concat(targetClsName);
		}
	}
	$.jstree.defaults.core.force_text = true;
    $.ajax({
        url : url,
        dataType:"json",
        success : function(response) {
        	if (srcClsName != null) {
        		$('#srcTree').jstree({
	        		'core': {
	        			'data': response.sourceFieldNames
	        		},
	        		"plugins": ["contextmenu"],
	        		"contextmenu": {
	        			"items": jstreeContextmenu
	        		}
	        	});
        		isSrcTree = true;
        	} else {
        		isSrcTree = false;
        	}
        	if (targetClsName != null) {
        		$('#targetTree').jstree({
	        		'core': {
	        			'data': response.targetFieldNames
	        		},
	        		"plugins": ["contextmenu", "themes", "ui", "types"],
	        		"contextmenu": {
	        			"items": jstreeContextmenu
	        		}
	        	});
        	}
        },
        error : function(xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });
   	return true;
}

$("#addScript").click(function( event ) {
    var targetNode = $('#targetTree').jstree(true).get_selected(true);
   	var targetOtclChain;
   	if (targetNode[0]) {
   		if (targetNode[0].id == CONSTANTS.TARGET_ROOT) {
   			showMsg($("#rootOtclChain"));
   			return;
   		}
   		targetOtclChain = targetNode[0].id;
   	}
   	if (!targetOtclChain) {
		showMsg($("#targetOtclChain"));
		return;
	}
	for (const [key, value] of anchorsMap.entries()) {
   		if (targetOtclChain.startsWith(key)) {
   			targetOtclChain = targetOtclChain.replace(key, value);
   			break;
   		}
   	};   	
	var isValid = isAnchorsValid(targetOtclChain);
	if (!isValid) {
		return;
	}
   	var otclInstructions = $("#otclInstructions");
   	var otclInstructionsValue = otclInstructions.val();
   	
   	if (otclInstructionsValue.trim() == "") {
   		copyCounter = 0;
   		executeCounter = 0;
   	}
   	var scriptBlock = null;
	if (CONSTANTS.CMD_COPY == command) {
		copyCounter++;
		var scriptId = "CP" + copyCounter;
		scriptBlock = copyScriptTemplate.replace(idPlaceholder, scriptId);
		scriptBlock = scriptBlock.replace(factoryclassPlaceholder, scriptId);
	} else {
		executeCounter++;
		var scriptId = "XE" + executeCounter;
		scriptBlock = executeScriptTemplate.replace(idPlaceholder, scriptId);
		scriptBlock = scriptBlock.replace(factoryclassPlaceholder, scriptId);
	}
    var srcNode;
	var sourceOtclChain;
    if (isSrcTree) {
    	srcNode = $('#srcTree').jstree(true).get_selected(true);
    }
   	if (srcNode && srcNode[0]) {
   		if (srcNode[0].id == CONSTANTS.SOURCE_ROOT) {
   			showMsg($("#rootOtclChain"));
   			return;
   		}
	  	sourceOtclChain = srcNode[0].id;
   	}
	if (sourceOtclChain == null) {
		scriptBlock = scriptBlock.replace(fromPlaceholder, valuesTemplate);
	} else {
		if (CONSTANTS.CMD_EXECUTE == command) {
	    	var isValid = isValidCollections(targetOtclChain, sourceOtclChain);
	   		if (!isValid) {
	   			return;
	   		}
		}
		var from = otclChainTemplate.replace(otclChainPlaceholder, sourceOtclChain);
		scriptBlock = scriptBlock.replace(fromPlaceholder, from);
	}
	if (sourceMap.has(CONSTANTS.GETTER)) {
		var overrides = overridesTemplate.replace(tokenPathPlaceholder, sourceOtclChain);
		if (sourceOtclChain != null) {
			if (sourceMap.has(CONSTANTS.GETTER)) {
				overrides += sourceMap.get(CONSTANTS.GETTER);
			}
		}
		scriptBlock = scriptBlock.replace(fromOverridesPlaceholder, overrides);
	} else {
		scriptBlock = scriptBlock.replace(fromOverridesPlaceholder, "");
	}
	// ---- to
	var to = otclChainTemplate.replace(otclChainPlaceholder, targetOtclChain);
	scriptBlock = scriptBlock.replace(toPlaceholder, to);
	
	if (targetMap.has(CONSTANTS.GETTER) || targetMap.has(CONSTANTS.GETTER_HELPER) ||
			targetMap.has(CONSTANTS.SETTER) || targetMap.has(CONSTANTS.SETTER_HELPER) ||
			targetMap.has(CONSTANTS.CONCRETE_TYPE)) {
		var overrides = overridesTemplate.replace(tokenPathPlaceholder, targetOtclChain.replace('^', ''));
		if (targetMap.has(CONSTANTS.GETTER)) {
			overrides += targetMap.get(CONSTANTS.GETTER);
		}
		if (targetMap.has(CONSTANTS.SETTER)) {
			overrides += targetMap.get(CONSTANTS.SETTER);
		}	
		if (targetMap.has(CONSTANTS.CONCRETE_TYPE)) {
			overrides += concreteTypeTemplate;
		}
		scriptBlock = scriptBlock.replace(toOverridesPlaceholder, overrides);
	} else {
		scriptBlock = scriptBlock.replace(toOverridesPlaceholder, "");
	}

   	if (otclInstructionsValue.trim() == "") {
   		var sourceType = $("#srcClsNames").val();
   		var targetType = $("#targetClsNames").val();
   		var entryClsName = "";
    	var lastIndexOf = 0;
    	if (sourceType) {
			if (sourceType.includes(".")) {
	    		lastIndexOf = sourceType.lastIndexOf(".");
	    		entryClsName = sourceType.substring(lastIndexOf + 1);
	    	} else {
	    		entryClsName = sourceType;
	    	}
		}
    	entryClsName = entryClsName.concat("To");
    	if (targetType.includes(".")) {
    		lastIndexOf = targetType.lastIndexOf(".");
    		entryClsName = entryClsName.concat(targetType.substring(lastIndexOf + 1));
    	} else {
    		entryClsName = entryClsName.concat(targetType);
    	}
   		var header = headerTemplate.replace(sourceTypePlaceholder, sourceType)
							.replace(targetTypePlaceholder, targetType)
							.replace(entryClassNamePlaceholder, entryClsName);
   		otclInstructionsValue = header;
   		otclInstructions.val(header);
   	} else {
   		otclInstructionsValue = otclInstructionsValue.concat('\n\n');
   	}
   	otclInstructionsValue = otclInstructionsValue.concat(scriptBlock);
   	otclInstructions.val(otclInstructionsValue);
    if (otclInstructions.length) {
    	otclInstructions.scrollTop(otclInstructions[0].scrollHeight - otclInstructions.height());
    }
   	$('#converters').val("");
   	$('#otclScriptType').val("");
});

function isValidCollections(targetOtclChain, sourceOtclChain) {
   	var idxTarget = targetOtclChain.indexOf("[");
   	var idxSource = sourceOtclChain.indexOf("[");
	if (idxTarget > 1 && idxSource > 1) {
		showMsg($("#singleSideCollectionOnly"));
    	return false;
	}
	return true;
}

function isAnchorsValid(otclChain) {
	var count = (otclChain.match(/^/g) || []).length;
	if (count > 1) {
		$("#otclChain").val(" in " + otclChain);
		showMsg($("#multipleAnchors"));
    	return false;		
	}
	return true;
}

$("#reset").click(function() {
	$("#otclInstructions").val('');
	copyCounter = 0;
	executeCounter = 0;
});

$("#otclEditorForm").submit(function(event) {
   	var otclInstructions = $("#otclInstructions");
   	var otclInstructionsValue = otclInstructions.val();

   	if (otclInstructionsValue.includes(helperClassNamePlaceholder)) {
		showMsg($("#helperClassName"));
		return false;
	}
   	if (otclInstructionsValue.includes(valuesPlaceholder)) {
		showMsg($("#values"));
		return false;
	}
   	if (otclInstructionsValue.includes(getterPlaceholder)) {
		showMsg($("#getter"));
		return false;
	}
   	if (otclInstructionsValue.includes(setterPlaceholder)) {
		showMsg($("#setter"));
		return false;
	}
   	if (otclInstructionsValue.includes(getterHelperPlaceholder)) {
		showMsg($("#getterHelper"));
		return false;
	}
   	if (otclInstructionsValue.includes(getterHelperParam) || otclInstructionsValue.includes(setterHelperParam)) {
   		if (!otclInstructionsValue.includes(helperParam)) {
   			showMsg($("#helperClassNotDefined"));
   			return false;
   		}
	}
   	if (otclInstructionsValue.includes(setterHelperPlaceholder)) {
		showMsg($("#setterHelper"));
		return false;
	}
   	if (otclInstructionsValue.includes(concreteTypePlaceholder)) {
		showMsg($("#concreteType"));
		return false;
	}
   	if (otclInstructionsValue.includes(otclConverterPlaceholder)) {
		showMsg($("#otclConverter"));
		return false;
	}
   	if (otclInstructionsValue.includes(otclNamespacePlaceholder)) {
		showMsg($("#otclNamespace"));
		return false;
	}
});

$("#createOtclFile").click(function() {
	var targetClsName = $('#targetClsNames').val();
	if (!targetClsName) {
		showMsg($("#typesNotSelected"));
   		return false;
	}
	var otclInstructions = $("#otclInstructions").val();
	if (!otclInstructions) {
		showMsg($("#nothingToSave"));
		return ;
	}
	$("#otclEditorForm").submit();
});

$("#flipOtcl").click(function() {
	var otcl = $("#otclInstructions").val();
	if (!otcl) {
		showMsg($("#nothingToSave"));
		return;
	}
	var srcClsName = $('#srcClsNames').val();
	if (!srcClsName) {
		showMsg($("#typesNotSelected"));
   		return false;
	}
	var targetClsName = $('#targetClsNames').val();
	if (!targetClsName) {
		showMsg($("#typesNotSelected"));
   		return false;
	}
	$("#infoLoss").show();
	$("#infoLoss").dialog({
		resizable: false,
	    modal: true,
		buttons: {
	        Ok: function() {
	          $( this ).dialog( "close" );
	      		var url = pageUrl.concat('flipOtcl');
		    	var entryClzName = $('#srcPkgName').val();
		    	var newEntryClzName = targetClsName + "To" + srcClsName;
		    	otcl = otcl.replace(entryClzName, newEntryClzName);
	    		$.post( url, { otclInstructions: otcl }).done(function( data ) {
	      			$("#otclInstructions").val(data);
	    	  	});
		    	var srcPkgName = $('#srcPkgName').val();
		    	var targetPkgName = $('#targetPkgName').val();
		    	$('#srcPkgName').val(targetPkgName);
		    	$('#targetPkgName').val(srcPkgName);
	
		    	var $srcClsNames = $("#srcClsNames > option").clone();
		    	var $targetClsNames = $("#targetClsNames > option").clone();
		    	$('#srcClsNames').empty();
		    	$('#targetClsNames').empty();
		    	$('#srcClsNames').append($targetClsNames);
		    	$('#targetClsNames').append($srcClsNames);
		    	
		    	$('#srcTree').jstree("destroy").empty();
		    	$('#targetTree').jstree("destroy").empty();
	        },
			Cancel: function() {
				return;
			}
        }
	});
});

function showMsg(msgElement) {
	msgElement.show();
	msgElement.dialog({
		resizable: false,
	    modal: true,
		buttons: {
	        Ok: function() {
	          $( this ).dialog( "close" );
	        }
        }
	});
	return;
}
