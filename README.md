# OTCL editor

       Fix - Hashtable does not allow null value.
       Fix - check for concrete types of collections instead of just List.isAssignableFrom(...)
       Fix, while assigning date, if it is assignable, then dont call teh date-converter.
       Fix - enum of primitive types.
       Fix - otcleditor - right-click to anchor.
       Fix - compilation happening even if .dep and .class files are present
	    Fix - requried in compileOtcl(File directory, String otclNamespace)
	    		in code - if (!CommonUtils.isEmpty(compilationReport.otclNamespace)) {
					depFileName = compilationReport.otclNamespace + "." + depFileName;
					// this needs a fix
				}
	    
	    
	   Web-site - provide link to add user feedback about OTCL 
       
       Check override of "concreteType:" for normal tokens.
       
		Don't mandate source-type if whole otcl has only  'from: values'
		
		“${OTCL_HOME}\lib” - test with only .class files. 
		
		Change deployment logic to ".dep" files from “${OTCL_HOME}\bin” 
		
		Fix - GetSetTemplate and SetterTemplate has issues with date
		
		Testcases - getterHelper
		
		For collections, additionally check if the type is of interface type -
			 Ex: whether interface List or Concrete-type ArrayLilst
		
		https://programminghints.com/2017/05/still-using-java-util-date-dont/
		
		Activities :
		- 
		Google analytics
		
		Twitter, Facebook, Linkedin, Instagram, Whatsapp, 
		Slack,
		
		Industry awards for OTCL
		
		Press releases in magazines
		
		Fix date format in PropertyConverterUtil using LocalDate.....
		OTCL-script - executeOtclModule and executeOtclConvertor - collection on any one side is fine.
		OTCL-script - anchors not allowed.
		
		executeOtclConverter - changes in js file to allow collections on any one side.
		
		FAQ - with scenarios where developers can make mistakes.
		Documentation - what all collections does it support
		
		Documentation - Elastic trees (Anchors)
		Documentation - Elastic trees imbalances
		Documentation - Clarity regarding pcdId used to retrieve from targetPCD and parentPCd which can 
				be the same.
		
		Test on Java 9, 10 and 11.
		Test from Command line.
		
		Compiler - check for array or collection should be based on field type rather than the notation.
		
		Multiple anchor validation

		Validation - map otcl-token which is leaf and <K> type should not have a map-key
		
		Test for arrays, Enums and Maps, method calls, generics.
		
		Validation OTCL command for maps - atleast one key first.
		
		Test setter param-type with different field-type.
		
		Test async mode.
		Test createInstance - pojo with private constructor 
		Test enum .
		Test with primitives
		
		Remove public modifier in classes where required.
	
		Web - Syntax checker in editor web
		Web - remove dependency on common.
		
		Documentation - Understanding logs
		Documentation - Spring integration
		Documentation - Otcl imbalances with anchors.
		
		Documentation - what all collection-types allow null values and null key.
		
		UI - pop-menu to mark anchor on / off.
		UI - create reverse - remove anchors, replace ::set with ::get, remove generics
		UI - FAQ
		UI - sign-in
		Documentation - UI browser compatibility
		
		Documentation - requirements of the POJO.
		Documentation - "When you look at the code, do not review the code-flow in isolation.
		 		But look at it with consideration that it has to work in co-ordination with several 
		 		individual pieces

		Get ready for questions on - https://www.javacodegeeks.com/2013/10/java-object-to-object-mapper.html#:~:text=Lorentz%3A%20Lorentz%20is%20a%20generic,an%20object%20of%20another%20type.&text=Spring%20framework%3A%20Spring%20has%20an,transform%20Objects%20to%2Ffrom%20Strings.
		
		https://creativecommons.org/licenses/by-nc-nd/3.0/legalcode
		
		OtclEngineImpl.
		if (!otclCompilerReportDisabled) {
				//TODO -- below needs a fix
		
		target can be interface - so add OTCL-CONFIG and provide concrete type
		
		execueSetvalue - make it in line.
		executeModule - any one side can have collections
		executeConverter - any one side can have collections or source can be null.
		
		AbstractOtclConverter.executeModule 
		
		OTCL generic and other notations - warning message when each line overrides the earlier setting.
		
		
		Add JAXBElements object creation feature.
		
		Failfast - also when 0 successful.
		
		Disclaimer in the license.
		
		- Paypal
		- AWS
		- 
		
		Clean up DTOs and code.