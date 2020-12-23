<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>OTCL Editor</title>
	
	<link rel="icon" href="./images/otcl-logo.png">
	
	<link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
	<script type="text/javascript" src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
	<script type="text/javascript" src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
	
	<!-- from https://anseki.github.io/jquery-contextmenu-common/ -->	
	<link href="jquery/dist/fixed/jquery.contextMenu.min.css" rel="stylesheet">
	<link href="jquery/dist/jquery.contextMenuCommon.min.css" rel="stylesheet">
	
	<script src="jquery/dist/jquery-ui-position.min.js"></script>
	<script src="jquery/dist/fixed/jquery.contextMenu.min.js"></script>
	<script src="jquery/dist/jquery.contextMenuCommon.min.js"></script>
	
	<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/jstree.min.js"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/themes/default/style.min.css" />
	
	<link rel="stylesheet" type="text/css" href="css/main.css" />

</head>

<body>
	<div id="header">
		<img class="logo" src="./images/otcl-logo.png">OTCL Script Editor
	</div>

	<form id="otclEditorForm" action="createOtclFile" accept-charset="utf-8" method="post">
		<div class="box columnHdr">
			<div class="sourceDiv">
				<label><u><b>From</b></u></label>
			</div>
			<div class="targetDiv">
				<label><u><b>To</b></u></label>
			</div>
		</div>
		<div class="box">
			<div class="sourceDiv">
				<label class="pkgNType">Pkg/NS:</label>
				<input id="srcPkgName" type="text" value="com.kronos.airlines.dto" class="inpt"/>
				<input id='fetchSrc' type='button' value="Fetch Types"  class="btn"/>
			</div>
			<div class="targetDiv">
				<label class="pkgNType">Pkg/NS:</label>
				<input id="targetPkgName" type="text" value="com.athena.airlines.dto" class="inpt"/>
				<input id='fetchTarget' type='button' value="Fetch Types"  class="btn"/>
			</div>
		</div>
		
		<div class="box">
 			<div class="sourceDiv">
				<label class="pkgNType">Types:</label>
				<select type="select" id="srcClsNames" name="srcClsNames" class="inptSingle"> 
				</select>
			</div>
			<div class="targetDiv">
				<label class="pkgNType">Types:</label>
				<select type="select" id="targetClsNames" name="targetClsNames" class="inptSingle">
			 	</select>
			</div>
		</div>
		<div class="box" id="divTextArea">
			<textarea id="otclInstructions" name="otclInstructions" class="txtArea context-menu-one"
					placeholder="Right-click inside this text-area to select command-type."></textarea>
		</div>
		<div class="box">
			<div class="actions">
				<input id="showTree" type="button" value="Show Tree" class="actionBtn" >
			</div>

			<div class="actions">
				<input id="addScript" type="button" value="Add Script" class="actionBtn" >
			</div>
			<div class="actions">
				<input id="createOtclFile" type="button" value="Create file" class="actionBtn" />
			</div>
			<div class="actions">
				<input id="flipOtcl" type="button" value="Flip OTCL" class="actionBtn" />
			</div>
			<div class="actions">
				<input id="reset" type="button" value="Clear" class="actionBtn" >
			</div>
		</div>
		<div class="box">
			<div id="srcTree" class="sourceTree">&nbsp;
			</div>
			<div id="targetTree" class="targetTree">&nbsp;
			</div>
		</div>
	</form>

<!--  error messages -->
	<div id="typesNotSelected" title="Target/Source types" style="display: none">
	  <p>Cannot proceed - please select both Target and Source types.</p>
	</div>
	<div id="nothingToSave" title="Nothing to Save" style="display: none">
	  <p>Cannot proceed - Nothing to save!</p>
	</div>
	<div id="rootOtclChain" title="Root OTCL-Chain" style="display: none">
	  <p>Root-node on any side cannot be selected to create OTCL-Script block!</p>
	</div>
	<div id="targetOtclChain" title="Target OTCL-Chain" style="display: none">
	  <p>Target OTCL-chain not selected!</p>
	</div>

	<div id="singleSideCollectionOnly" title="OTCL-Script::Collections on one side Only" style="display: none">
	  <p>Otcl-scripts collections are allowed on any one of executeOtclConverter and executeOtclModule only!</p>
	</div>
	<div id="multipleAnchors" title="OTCL Chain::Multiple Anchors" style="display: none">
	  <p>OTCL-chain with multiple Anchors (^) is not valid!</p>
	  <p id="otclChain" />
	</div>

	<div id="sourceType" title="Source-Type" style="display: none">
	  <p>Please define value for placeholder &lt;&lt;sourceType&gt;&gt; under 'metadata: objectTypes: source:'.</p>
	</div>
	<div id="targetType" title="Target-Type" style="display: none">
	  <p>Please define value for placeholder &lt;&lt;targetType&gt;&gt; under 'metadata: objectTypes: target:'.</p>
	</div>
	<div id="mainClassName" title="Main-Class Name" style="display: none">
	  <p>Please define value for placeholder &lt;&lt;mainClassName&gt;&gt; under 'copy: or metadata: mainClassName:'.</p>
	</div>
	<div id="helperClassName" title="Helper-Class Name" style="display: none">
	  <p>Please define value for placeholder or remove line for 'helper: &lt;&lt;helperClassName&gt;&gt;' parameter under metadata:' or remove line.</p>
	</div>
	<div id="factoryclass" title="Factory-Class Name" style="display: none">
	  <p>Please define value for placeholder &lt;&lt;factoryclass&gt;&gt; under 'copy: or execute: factoryClassName:' or remove line.</p>
	</div>
	<div id="getter" title="Getter-Name" style="display: none">
	  <p>Please define value for placeholder &lt;&lt;getter&gt;&gt; under 'copy: or execute: getter:' or remove line.</p>
	</div>
	<div id="setter" title="Setter-Name" style="display: none">
	  <p>Please define value for placeholder &lt;&lt;setter&gt;&gt; under 'copy: or execute: setter:' or remove line.</p>
	</div>
	<div id="getterHelper" title="GetterHelper-Name" style="display: none">
	  <p>Please define value for placeholder &lt;&lt;getterHelper&gt;&gt; under 'copy: or execute: getterHelper:' or remove line.</p>
	</div>
	<div id="setterHelper" title="SetterHelper-Name" style="display: none">
	  <p>Please define value for placeholder &lt;&lt;setterHelper&gt;&gt; under 'copy: or execute: setterHelper:' or remove line.</p>
	</div>
	<div id="helperClassNotDefined" title="HelperClass-Name requried" style="display: none">
	  <p>'helper:' parameter and value expected under 'metadata:' section due to presence of 'getterHelper:' / 'setterHelper:' in script-block(s).</p>
	</div>

	<div id="concreteType" title="Concrete-Type" style="display: none">
	  <p>Please define value for placeholder &lt;&lt;concreteType&gt;&gt; under 'copy:' or 'execute:' - 'concreteType:' or remove line.</p>
	</div>
	<div id="otclConverter" title="OTCL-Converter" style="display: none">
	  <p>Please define value for placeholder &lt;&lt;otclConverter&gt;&gt; under 'execute: otclConverter:' or remove line.</p>
	</div>
	<div id="otclNamespace" title="OTCL-Namespace" style="display: none">
	  <p>Please define value for placeholder &lt;&lt;otclNamespace&gt;&gt; under 'execute: otclNamespace:' or remove line.</p>
	</div>
	<div id="infoLoss" title="Some info can be lost" style="display: none">
	  <p>Some information can be lost.</p>
	</div>

</body>

	<script type="text/javascript" src="js/main.js"></script>


</html>