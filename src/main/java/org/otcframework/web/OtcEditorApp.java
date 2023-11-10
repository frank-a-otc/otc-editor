/**
* Copyright (c) otcframework.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcframework.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class OtcEditorApp extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(OtcEditorApp.class, args);
	}
}
