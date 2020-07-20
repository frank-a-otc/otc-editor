<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>OTCL Editor</title>
	<link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
	<script type="text/javascript" src="http://code.jquery.com/jquery-1.12.4.min.js"></script>
	<script type="text/javascript" src="http://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>

	<script src="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/jstree.min.js"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/themes/default/style.min.css" />
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/main.css">

</head>

<body>
	<div id="header">
		OTCL Editor
	</div>
	<form id="otclEditor" action="createOtclFile" accept-charset="utf-8" method="post">
    	<input id="reverseOtclFile" name="reverseOtclFile" type="hidden" value="false">
		<div class="box columnHdr">
			<div class="leftDiv">
				<label><u><b>To (Target)</b></u></label>
			</div>
			<div class="rightDiv">
				<label><u><b>From (Source)</b></u></label>
			</div>
		</div>
		<div class="box">
			<div class="leftDiv">
				<label class="pkgNType">Package:</label>
				<input id="targetPkgName" type="text" value="org.otcl.airlines.athena.dto" class="inpt"/>
				<input id='fetchTarget' type='button' value="Fetch Types"  class="btn"/>
			</div>
			<div class="rightDiv">
				<label class="pkgNType">Package:</label>
				<input id="srcPkgName" type="text" value="org.otcl.airlines.kronos.dto" class="inpt"/>
				<input id='fetchSrc' type='button' value="Fetch Types"  class="btn"/>
			</div>
		</div>
		
		<div class="box">
			<div class="leftDiv">
				<label class="pkgNType">Types:</label>
				<select type="select" id="targetClsNames" name="targetClsNames" class="inptSingle">
			 	</select>
			</div>
 			<div class="rightDiv">
				<label class="pkgNType">Types:</label>
				<select type="select" id="srcClsNames" name="srcClsNames" class="inptSingle"> 
				</select>
			</div>
		</div>
		<div class="box">
			<div class="leftDiv">
				<label class="otclScript">Extensions:</label>
				<select type="select" id="otclScriptType"  class="otclScriptInpt">
					<option value="" selected="selected">Select template...</option>
					<option value="setValue">setValue</option>
					<option value="executeOtclConverter">executeOtclConverter</option>
					<option value="executeOtclModule">executeOtclModule</option>
			 	</select>
			</div>
			<div class="rightDiv">
				<label class="otclScript">OTCL Converter:</label>
				<select type="select" id="converters" disabled="disabled"  class="otclScriptInpt"></select>
			</div>	
		</div>
		<div class="box">
			<textarea id="otclInstructions" name="otclInstructions" class="txtArea"></textarea>
		</div>
		<div class="box">
			<div class="actions">
				<input id="displayTree" type="button" value="Show Object Trees" class="actionBtn" >
			</div>
			<div class="actions">
				<input id="createScript" type="button" value="Create OTCL Script" class="actionBtn" >
			</div>
			<div class="actions">
				<input id="reset" type="button" value="Reset OTCL Script" class="actionBtn" >
			</div>
			<div class="actions">
				<input id="createOtclFile" type="button" value="Create OTCL file" class="actionBtn" />
			</div>
			<div class="actions">
				<input id="createFlippedFile" type="button" value="Create swapped file" class="actionBtn" />
			</div>
		</div>
		<div class="box">
			<div id="targetTree" class="leftTree">&nbsp;
			</div>
			<div id="srcTree" class="rightTree">&nbsp;
			</div>
		</div>
	</form>

	<div id="typesNotSelected" title="Target/Source types" style="display: none">
	  <p>Cannot proceed - please select both Target and Source types.</p>
	</div>
	<div id="nothingToSave" title="Nothing to Save" style="display: none">
	  <p>Cannot proceed - Nothing to save!</p>
	</div>
	<div id="rootOtclChain" title="Root OTCL-Chain" style="display: none">
	  <p>Root OTCL-chain on either side cannot be selected to create OTCL-expression.</p>
	</div>
	<div id="sourceOtclChain" title="Source OTCL-Chain" style="display: none">
	  <p>Source OTCL-chain not selected.</p>
	</div>
	<div id="targetOtclChain" title="Target OTCL-Chain" style="display: none">
	  <p>Target OTCL-chain not selected.</p>
	</div>
	<div id="setValue" title="OTCL-Script::setValue" style="display: none">
	  <p>Caution! Change '&lt;value-placeholder&gt;' in OTCL-script to desired value.</p>
	</div>
	<div id="executeOtclConverter" title="OTCL-Script::executeOtclConverter" style="display: none">
	  <p>Warning! You have not selected a OTCL Converter! Replace '&lt;otclConverter-placeholder&gt;' with correct value.</p>
	</div>
	<div id="executeOtclModule" title="OTCL-Script::executeOtclModule" style="display: none">
	  <p>Caution! Provide value for the '&lt;otclNamespace-placeholder(optional)&gt;' or remove entire line.</p>
	</div>
	<div id="singleSideCollectionOnly" title="OTCL-Script::Collections on one side Only" style="display: none">
	  <p>For executeOtclConverter and executeOtclModule Otcl-scripts collections are allowed on any one side only.</p>
	</div>
	<div id="mapkeyOnLeaf" title="OTCL Chain::Mapkey on leaf" style="display: none">
	  <p>OTCL-chain with Map-key token as leaf cannot have a map-key.</p>
	</div>
	<div id="multipleAnchors" title="OTCL Chain::Multiple Anchors" style="display: none">
	  <p>OTCL-chain with multiple Anchors (^) invalid.</p>
	  <p id="otclChain" />
	</div>
</body>

	<script type="text/javascript" src="<%=request.getContextPath()%>/js/main.js"></script>
</html>