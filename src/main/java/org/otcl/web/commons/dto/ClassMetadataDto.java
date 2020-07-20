/**
* Copyright (c) otcl2.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcl.web.commons.dto;

import java.util.ArrayList;
import java.util.List;

public final class ClassMetadataDto {

	public String id;
	public List<ClassMetadataDto> children;
	public String text;
	
	private ClassMetadataDto(Builder builder) {
		id = builder.id;
		children = builder.children;
		text = builder.text;
	}
	
	public static Builder newBuilder() {
		return new Builder();
	}
	
	@Override
	public String toString() {
		return "ClassInfoDto [id=" + id + ", text=" + text + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		ClassMetadataDto other = (ClassMetadataDto) obj;
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
		return true;
	}

	public static class Builder {
		private String id;
		private List<ClassMetadataDto> children;
		private String text;

		public Builder addId(String id) {
			this.id = id;
			return this;
		}

		public Builder addChildren(List<ClassMetadataDto> children) {
			if (this.children == null) {
				this.children = children;
			} else {
				this.children.addAll(children);
			}
			return this;
		}

		public Builder addChild(ClassMetadataDto classMetadataDto) {
			if (children == null) {
				children = new ArrayList<>();
			}
			children.add(classMetadataDto);
			return this;
		}

		public Builder addText(String text) {
			this.text = text;
			return this;
		}

		public ClassMetadataDto build() {
			return new ClassMetadataDto(this);
		}

	}
}
