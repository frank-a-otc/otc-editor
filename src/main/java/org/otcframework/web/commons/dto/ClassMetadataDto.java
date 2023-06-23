/**
* Copyright (c) otcframework.org
*
* @author  Franklin Abel
* @version 1.0
* @since   2020-06-08 
*/
package org.otcframework.web.commons.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public final class ClassMetadataDto {

	@JsonInclude(Include.NON_NULL) 
	public String id;
	@JsonInclude(Include.NON_NULL) 
	public List<ClassMetadataDto> children;
	@JsonInclude(Include.NON_NULL)
	public String text;
	@JsonInclude(Include.NON_NULL)
	public Boolean isSetterRequired;
	@JsonInclude(Include.NON_NULL)
	public String setter;
	@JsonInclude(Include.NON_NULL)
	public Boolean isSetterHelperRequired;
	@JsonInclude(Include.NON_NULL)
	public Boolean isGetterRequired;
	@JsonInclude(Include.NON_NULL)
	public String getter;
	@JsonInclude(Include.NON_NULL)
	public Boolean isGetterHelperRequired;

	private ClassMetadataDto(Builder builder) {
		id = builder.id;
		children = builder.children;
		text = builder.text;
		isGetterRequired = builder.isGetterRequired;
		getter = builder.getter;
		isGetterHelperRequired = builder.isGetterHelperRequired;
		isSetterRequired = builder.isSetterRequired;
		setter = builder.setter;
		isSetterHelperRequired = builder.isSetterHelperRequired;
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
		result = prime * result + ((setter == null) ? 0 : setter.hashCode());
		result = prime * result + ((isSetterRequired == null) ? 0 : isSetterRequired.hashCode());
		result = prime * result + ((isSetterHelperRequired == null) ? 0 : isSetterHelperRequired.hashCode());
		result = prime * result + ((getter == null) ? 0 : getter.hashCode());
		result = prime * result + ((isGetterRequired == null) ? 0 : isGetterRequired.hashCode());
		result = prime * result + ((isGetterHelperRequired == null) ? 0 : isGetterHelperRequired.hashCode());
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
		public boolean isSetterRequired;
		private String setter;
		public boolean isSetterHelperRequired;
		public boolean isGetterRequired;
		private String getter;
		public boolean isGetterHelperRequired;

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

		public Builder addIsSetterRequired(boolean isSetterRequired) {
			this.isSetterRequired = isSetterRequired;
			return this;
		}

		public Builder addSetter(String setter) {
			this.setter = setter;
			return this;
		}

		public Builder addIsSetterHelperRequired(boolean isSetterHelperRequired) {
			this.isSetterHelperRequired = isSetterHelperRequired;
			return this;
		}

		public Builder addIsGetterRequired(boolean isGetterRequired) {
			this.isGetterRequired = isGetterRequired;
			return this;
		}

		public Builder addGetter(String getter) {
			this.getter = getter;
			return this;
		}

		public Builder addIsGetterHelperRequired(boolean isGetterHelperRequired) {
			this.isGetterHelperRequired = isGetterHelperRequired;
			return this;
		}

		public ClassMetadataDto build() {
			return new ClassMetadataDto(this);
		}

	}
}
