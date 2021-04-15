/**
* Copyright (c) otclfoundation.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcl.web.commons.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public final class JstreeNodeInfo {

	@JsonInclude(Include.NON_NULL) 
	public String id;
	@JsonInclude(Include.NON_NULL)
	public String text;
	@JsonInclude(Include.NON_NULL)
	public List<JstreeNodeInfo> children;
	@JsonInclude(Include.NON_NULL)
	public String toolTip;
	
	private JstreeNodeInfo(Builder builder) {
		id = builder.id;
		text = builder.text;
		children = builder.children;
		toolTip = builder.toolTip;
	}
	
	public static Builder newBuilder() {
		return new Builder();
	}
	
	@Override
	public String toString() {
		return "JstreeNodeInfo [id=" + id + ", text=" + text + ", children=" + children + ", toolTip=" + toolTip + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((toolTip == null) ? 0 : toolTip.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JstreeNodeInfo other = (JstreeNodeInfo) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (toolTip == null) {
			if (other.toolTip != null)
				return false;
		} else if (!toolTip.equals(other.toolTip))
			return false;
		return true;
	}

	public static class Builder {
		private String id;
		private String text;
		private List<JstreeNodeInfo> children;
		private String toolTip;

		public Builder addId(String id) {
			this.id = id;
			return this;
		}

		public Builder addText(String text) {
			this.text = text;
			return this;
		}

		public Builder addChildren(List<JstreeNodeInfo> children) {
			this.children = children;
			return this;
		}

		public Builder addToolTip(String toolTip) {
			this.toolTip = toolTip;
			return this;
		}
		
		public JstreeNodeInfo build() {
			return new JstreeNodeInfo(this);
		}
	}
}
