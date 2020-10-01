String.prototype.replaceAt=function(index, character) {
    return this.substr(0, index) + character + this.substr(index+character.length);
};

var pageUrl = "";

$(document).ready(function() {
	var url = pageUrl.concat('showConverters');
	getClassNames(url, 'converters');
	$("#otclInstructions").val('');
});


let map = new Map();
let sourceMap = new Map();
let targetMap = new Map();

//var isInitialized = false;

var copyCounter = 0;
var executeCounter = 0;
var selectMsg = "Select...";

const CONSTANTS = {
	ANCHOR: "anchor",
	SOURCE_ROOT: "source-root",
	TARGET_ROOT: "target-root",
	GETTER: "getter",
	SETTER: "setter",
	ACTIVATE_GETTER: "activateGetter",
	ACTIVATE_SETTER: "activateSetter",
	CONCRETE_TYPE: "concreteType",
	DATE_FORMAT: "dateFormat",
	CMD_COPY: "Copy",
	CMD_EXECUTE: "Execute"
};
Object.freeze(CONSTANTS);

var headerTemplate = 
	"metadata:\r\n" +
	"  mainClassName: <<mainClassName>>\r\n" +
	"  helper: <<helperClassName>>\r\n" +
	"  objectTypes:\r\n" +
	"    source: <<source-type>>\r\n" +
	"    target: <<target-type>>\r\n" +
	"otclScripts:\r\n";

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
	"overrides:\r\n" +
	"      - tokenPath: <<tokenPath>>\r\n";

var concreteTypeTemplate = 
	"        concreteType: <<concreteType>>\r\n";

var dateFormatTemplate = 
	"        dateFormat: <<dateFormat>>\r\n";

var setterTemplate = 
	"        setter: <<setter>>\r\n";

var getterTemplate = 
	"        getter: <<getter>>\r\n";

var activateSetterInHelperTemplate =
	"        activateSetterInHelper: true\r\n";

var activateGetterInHelperTemplate =
	"        activateGetterInHelper: true\r\n";

var idPlaceholder = "<<id>>";
var sourceTypePlaceholder = "<<source-type>>";
var targetTypePlaceholder = "<<target-type>>";
var fromPlaceholder = "<<from>>";
var toPlaceholder = "<<to>>";
var otclChainPlaceholder = "<<otclChain>>";
var tokenPathPlaceholder = "<<tokenPath>>";
var fromOverridesPlaceholder = "      <<fromOverrides>>\r\n";
var toOverridesPlaceholder = "      <<toOverrides>>\r\n";

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
	if (srcClsName != null && targetClsName != null) {
		url = pageUrl.concat('fetchJsTreeData');
	} else if (srcClsName != null) {
		url = pageUrl.concat('fetchSourceJsTreeData');
	} else {
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
            console.log('clicked', this);
            return true;
        }
    },
    "execute": {
    	name: "Execute", 
        type: 'radio',
        callback: function() {
            console.log('clicked', this);
            return true;
        }
   	}
};

var sourceOverrideItems = {
    "getterItem": {
    	name: "getter", 
        type: 'checkbox', 
        action : function (menuItem) {
        	alert (menuItem.item.checked);
        	if (menuItem.item.icon == false) {
        		sourceMap.set(CONSTANTS.GETTER, getterTemplate);
        	} else {
        		sourceMap.delete(CONSTANTS.GETTER);
        	}
        }
    },
    'activateGetterInHelperItem' : {
    	name: "activateGetterInHelperItem", 
        type: 'checkbox', 
        action : function (menuItem) { 
        	if (menuItem.item.checked == false) {
        		sourceMap.set(CONSTANTS.ACTIVATE_GETTER, activateGetterInHelperTemplate);
        	} else {
        		sourceMap.delete(CONSTANTS.ACTIVATE_GETTER);
        	}
        }
    },
    'dateFormatItem' : {
    	name: "dateFormatItem", 
        type: 'checkbox', 
        action : function (menuItem) { 
        	if (menuItem.item.icon == false) {
        		sourceMap.set(CONSTANTS.DATE_FORMAT, dateFormatTemplate);
        	} else {
        		sourceMap.delete(CONSTANTS.DATE_FORMAT);
        	}
        }
    }
};

var targetContextMenuItems = {
    'getterItem' : {
    	name: "getter", 
        type: 'checkbox', 
        action : function (menuItem) { 
        	if (menuItem.item.icon == false) {
        		targetMap.set(CONSTANTS.GETTER, getterTemplate);
        	} else {
        		targetMap.delete(CONSTANTS.GETTER);
        	}
        }
    },
    'activateGetterInHelperItem' : {
    	name: "activateGetterInHelper", 
        type: 'checkbox', 
        action : function (menuItem) { 
        	if (menuItem.item.icon == false) {
        		targetMap.set(CONSTANTS.ACTIVATE_GETTER, activateGetterInHelperTemplate);
        	} else {
        		targetMap.delete(CONSTANTS.ACTIVATE_GETTER);
        	}
        }
    },
    'setterItem' : {
    	name: "setter", 
        type: 'checkbox', 
        action : function (menuItem) { 
        	if (menuItem.item.icon == false) {
        		targetMap.set(CONSTANTS.SETTER, setterTemplate);
        	} else {
        		targetMap.delete(CONSTANTS.SETTER);
        	}
        }
    },
    'activateSetterInHelperItem' : {
    	name: "activateSetterInHelper", 
        type: 'checkbox', 
        action : function (menuItem) { 
        	if (menuItem.item.icon == false) {
        		targetMap.set(CONSTANTS.ACTIVATE_SETTER, activateSetterInHelperTemplate);
        	} else {
        		targetMap.delete(CONSTANTS.ACTIVATE_SETTER);
        	}
        }
    },
    'concreteTypeItem' : {
    	name: "concreteTypeItem", 
        type: 'checkbox', 
        action : function (menuItem) { 
        	if (menuItem.item.icon == false) {
        		targetMap.set(CONSTANTS.CONCRETE_TYPE, concreteTypeTemplate);
        	} else {
        		targetMap.delete(CONSTANTS.CONCRETE_TYPE);
        	}
        }
    }
//   	"anchor": {
//   		name: "Anchor", 
//        type: 'checkbox', 
//        action: function (menuItem) { 
//        	if (map.has(otclChain)) {
//        		map.delete(otclChain);
//        		menuItem.item.icon = false;
//        	} else if (otclChain.includes("]")) {
//            	menuItem.item.icon = 'images/tick.png';
//	        	var lastIndexOf = otclChain.lastIndexOf("[<");
//	        	if (lastIndexOf < 0) {
//	        		lastIndexOf = otclChain.lastIndexOf("[");
//	        	}
//	        	var sourceOtclChain = otclChain.substring(0, lastIndexOf + 1);
//	        	if (otclChain.charAt(lastIndexOf + 1) == "*") {
//	        		targetOtclChain = otclChain.substring(lastIndexOf + 2);
//	            } else {
//	        		targetOtclChain = otclChain.substring(lastIndexOf + 1);
//	            }
//	        	var anchoredOtclChain = sourceOtclChain + "^" + targetOtclChain;
//	        	map.set(otclChain, anchoredOtclChain);
//	        }
//		}
//   	}
};

var anchorItems = {
   	"anchor": {
   		name: "Anchor", 
        type: 'checkbox', 
        action: function (menuItem) { 
        	if (map.has(otclChain)) {
        		map.delete(otclChain);
        		menuItem.item.icon = false;
        	} else if (otclChain.includes("]")) {
            	menuItem.item.icon = 'images/tick.png';
	        	var lastIndexOf = otclChain.lastIndexOf("[<");
	        	if (lastIndexOf < 0) {
	        		lastIndexOf = otclChain.lastIndexOf("[");
	        	}
	        	var sourceOtclChain = otclChain.substring(0, lastIndexOf + 1);
	        	if (otclChain.charAt(lastIndexOf + 1) == "*") {
	        		targetOtclChain = otclChain.substring(lastIndexOf + 2);
	            } else {
	        		targetOtclChain = otclChain.substring(lastIndexOf + 1);
	            }
	        	var anchoredOtclChain = sourceOtclChain + "^" + targetOtclChain;
	        	map.set(otclChain, anchoredOtclChain);
	        }
		}
   	}
};

let contextMenuItems = JSON.parse(JSON.stringify(targetContextMenuItems));

function jstreeContextmenu(node) {
	var otclChain = node.id;
//	alert (otclChain);
	if (CONSTANTS.SOURCE_ROOT == otclChain) {
	    $.contextMenuCommon({
	        selector: '#srcTree', 
	        autoHide: true,
	        callback: function(key, options) {
	            var m = "clicked: " + key;
	            window.console && console.log(m) || alert(m); 
	        },
	        items: sourceOverrideItems
	    });
	} else {
		var menuItems = [];
		if (CONSTANTS.TARGET_ROOT == otclChain) {
			menuItems = targetContextMenuItems; 
		} else {
			menuItems = anchorItems;
		}
	    $.contextMenuCommon({
	        selector: '#targetTree', 
	        autoHide: true,
	        items: menuItems
//	        build: function($triggerElement, e) {
//	            return {
//	                callback: function(){},
//	                items: {
//	                    menuItem: {name: "My on demand menu item"}
//	                }
//	        }
//	    callback: function(key, options) {
//	            var m = "clicked: " + key;
//	            window.console && console.log(m) || alert(m); 
//	            
//	    		contextMenuItems = JSON.parse(JSON.stringify(targetContextMenuItems));
//	    		if (CONSTANTS.TARGET_ROOT == otclChain) {
//	    			delete contextMenuItems.anchor;
//	    		} else {
//	    			delete contextMenuItems.getterItem;
//	    			delete contextMenuItems.activateGetterInHelperItem;
//	    			delete contextMenuItems.setterItem;
//	    			delete contextMenuItems.activateSetterInHelperItem;
//	    			delete contextMenuItems.concreteTypeItem;
//	    		}
//	        },
//	        items: contextMenuItems
	    });
	}
};

//$(function() {
//    $.contextMenuCommon({
//        selector: '#otclInstructions',  //'.context-menu-one', 
//        autoHide: true,
//        callback: function(key, options) {
//            var m = "clicked: " + key;
//            window.console && console.log(m) || alert(m); 
//        },
//        items: commandItems
//    });
//
//    $('.context-menu-one').on('click', function(e){
//        console.log('clicked', this);
//    })    
//});


function fetchAndPopulateJstree(url) {
	$('#srcTree').jstree("destroy").empty();
	$('#targetTree').jstree("destroy").empty();
	var srcClsName = $('#srcClsNames').val();
	var targetClsName = $('#targetClsNames').val();
	if (srcClsName != null) {
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
        	}
        	if (targetClsName != null) {
        		$('#targetTree').jstree({
	        		'core': {
	        			'data': response.targetFieldNames
	        		},
	        		"plugins": ["contextmenu"],
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

function areTypesSelected() {
	var srcClsName = $('#srcClsNames').val();
	var targetClsName = $('#targetClsNames').val();
	if (!srcClsName || !targetClsName) {
        $("#typesNotSelected").show();
       	$("#typesNotSelected").dialog({resizable: false}).dialog("open");
   		return false;
	}
	return true;
}

$("#addScript").click(function( event ) {
    var targetNode = $('#targetTree').jstree(true).get_selected(true);
   	var targetOtclChain;
   	if (targetNode[0]) {
   		if (targetNode[0].id == CONSTANTS.TARGET_ROOT) {
        	$("#rootOtclChain").show();
        	$("#rootOtclChain").dialog({resizable: false}).dialog("open");
   			return;
   		}
   		targetOtclChain = targetNode[0].id;
   	}
   	if (!targetOtclChain) {
        $( "#targetOtclChain").show();
        $( "#targetOtclChain").dialog({resizable: false}).dialog("open");
		return;
	}
	var isValid = isAnchorsValid(targetOtclChain);
	if (!isValid) {
		return;
	}
	
    var srcNode = $('#srcTree').jstree(true).get_selected(true);
	var sourceOtclChain;
   	if (srcNode[0]) {
   		if (srcNode[0].id == CONSTANTS.SOURCE_ROOT) {
        	$("#rootOtclChain").show();
        	$("#rootOtclChain").dialog({resizable: false}).dialog("open");
        	return;
   		}
	  	sourceOtclChain = srcNode[0].id;
   	}

   	var command = $('input[name="command"]:checked').val();
   	var scriptBlock = null;
	if (CONSTANTS.CMD_COPY == command) {
		copyCounter++;
		scriptBlock = copyScriptTemplate.replace(idPlaceholder, "CPY" + copyCounter);
	} else {
		executeCounter++;
		scriptBlock = executeScriptTemplate.replace(idPlaceholder, "EXE" + executeCounter);
	}
	if (sourceOtclChain == null) {
		scriptBlock = scriptBlock.replace(fromPlaceholder, valuesTemplate);
	} else {
    	var isValid = isValidCollections(targetOtclChain, sourceOtclChain);
   		if (!isValid) {
   			return;
   		}
		var from = otclChainTemplate.replace(otclChainPlaceholder, sourceOtclChain);
		scriptBlock = scriptBlock.replace(fromPlaceholder, from);
	}
	if (sourceMap.has(CONSTANTS.GETTER) || sourceMap.has(CONSTANTS.ACTIVATE_GETTER) ||
			sourceMap.has(CONSTANTS.DATE_FORMAT)) {
		var overrides = overridesTemplate.replace(tokenPathPlaceholder, sourceOtclChain);
		if (sourceOtclChain != null) {
			if (sourceMap.has(CONSTANTS.GETTER)) {
				overrides += getterTemplate;
			}
			if (sourceMap.has(CONSTANTS.ACTIVATE_GETTER)) {
				overrides += activateGetterInHelperTemplate;
			}
		}
		if (sourceMap.has(CONSTANTS.DATE_FORMAT)) {
			overrides += dateFormatTemplate;
		}
		scriptBlock = scriptBlock.replace(fromOverridesPlaceholder, overrides);
	} else {
		scriptBlock = scriptBlock.replace(fromOverridesPlaceholder, "");
	}
	// to
	var to = otclChainTemplate.replace(otclChainPlaceholder, targetOtclChain);
	scriptBlock = scriptBlock.replace(toPlaceholder, to);
	
	if (targetMap.has(CONSTANTS.GETTER) || targetMap.has(CONSTANTS.ACTIVATE_GETTER) ||
			targetMap.has(CONSTANTS.SETTER) || targetMap.has(CONSTANTS.ACTIVATE_SETTER) ||
			targetMap.has(CONSTANTS.CONCRETE_TYPE)) {
		var overrides = overridesTemplate.replace(tokenPathPlaceholder, targetOtclChain);
		if (targetMap.has(CONSTANTS.GETTER)) {
			overrides += getterTemplate;
		}
		if (targetMap.has(CONSTANTS.SETTER)) {
			overrides += setterTemplate;
		}
		if (targetMap.has(CONSTANTS.ACTIVATE_GETTER)) {
			overrides += activateGetterInHelperTemplate;
		}
		if (targetMap.has(CONSTANTS.ACTIVATE_SETTER)) {
			overrides += activateSetterInHelperTemplate;
		}		
		if (targetMap.has(CONSTANTS.CONCRETE_TYPE)) {
			overrides += concreteTypeTemplate;
		}
		scriptBlock = scriptBlock.replace(toOverridesPlaceholder, overrides);
	} else {
		scriptBlock = scriptBlock.replace(toOverridesPlaceholder, "");
	}

   	var otclInstructions = $("#otclInstructions");
   	var otclInstructionsValue = otclInstructions.val();
   	
   	if (otclInstructionsValue.trim() == "") {
//   		isInitialized = true;
   		var sourceType = $("#srcClsNames").val();
   		var targetType = $("#targetClsNames").val();
   		var header = headerTemplate.replace(sourceTypePlaceholder, sourceType).replace(targetTypePlaceholder, targetType);
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
    	$("#singleSideCollectionOnly").show();
    	$("#singleSideCollectionOnly").dialog({resizable: false}).dialog("open");
    	return false;
	}
	return true;
}

function isAnchorsValid(otclChain) {
	var count = (otclChain.match(/^/g) || []).length;
	if (count > 1) {
		$("#otclChain").val(" in " + otclChain);
    	$("#multipleAnchors").show();
    	$("#multipleAnchors").dialog({resizable: false}).dialog("open");
    	return false;		
	}
	return true;
}


$("#reset").click(function() {
	$("#otclInstructions").val('');
	copyCounter = 0;
	executeCounter = 0;
//	isInitialized = false;
});

$("#otclEditor").submit(function(event) {
	var isTypesSelected = areTypesSelected();
	if (!isTypesSelected) {
		return;
	}
	$('<input />').attr('type', 'text')
    	.attr('name', 'reverseOtclFile')
    	.attr('value', $('reverseOtclFile').val());
	
   	var otclInstructions = $("#otclInstructions");
   	var otclInstructionsValue = otclInstructions.val();
   	otclInstructionsValue = otclInstructionsValue.replace("<targetType-placeholder>", $('#targetClsNames').val())
   		.replace("<sourceType-placeholder>", $('#srcClsNames').val());
   	otclInstructions.val(otclInstructionsValue);
});

$("#createOtclFile").click(function() {
	var isTypesSelected = areTypesSelected();
	if (!isTypesSelected) {
		return;
	}
	var otclInstructions = $("#otclInstructions").val();
	if (!otclInstructions) {
		$("#nothingToSave").show();
   		$("#nothingToSave").dialog({resizable: false}).dialog("open");
		return;
	}
	$("#otclEditor").submit();
});

$("#flipOtcl").click(function() {
	var isTypesSelected = areTypesSelected();
	if (!isTypesSelected) {
		return;
	}
	var otclInstructions = $("#otclInstructions").val();
	if (!otclInstructions) {
		$("#nothingToSave").show();
   		$("#nothingToSave").dialog({resizable: false}).dialog("open");
		return;
	}
	$("#reverseOtclFile").val("true");
	$("#otclEditor").submit();
	$("#reverseOtclFile").val("false");
});

// ----- TextArea context menu

//$("#divTextArea").bind("contextmenu", function (event) {
//    
//    // Avoid the real one
//    event.preventDefault();
//    
//    // Show contextmenu
//    $(".custom-menu").finish().toggle(100).
//    
//    // In the right position (the mouse)
//    css({
//        top: event.pageY + "px",
//        left: event.pageX + "px"
//    });
//});
//
//
//// If the document is clicked somewhere
//$("#divTextArea").bind("mousedown", function (e) {
//    
//    // If the clicked element is not the menu
//    if (!$(e.target).parents(".custom-menu").length > 0) {
//        
//        // Hide it
//        $(".custom-menu").hide(100);
//    }
//});
//
//
//// If the menu element is clicked
//$(".custom-menu li").click(function(){
//    
//    // This is the triggered action name
//    switch($(this).attr("data-action")) {
//        
//        // A case for each action. Your actions here
//        case "first": alert("first"); break;
//        case "second": alert("second"); break;
//    }
//  
//    // Hide it AFTER the action was triggered
//    $(".custom-menu").hide(100);
//  });
