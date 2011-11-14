/*
*
* Copyright 2005 AgileTec s.r.l. (http://www.agiletec.it) All rights reserved.
*
* This file is part of jAPS software.
* jAPS is a free software; 
* you can redistribute it and/or modify it
* under the terms of the GNU General Public License (GPL) as published by the Free Software Foundation; version 2.
* 
* See the file License for the specific language governing permissions   
* and limitations under the License
* 
* 
* 
* Copyright 2005 AgileTec s.r.l. (http://www.agiletec.it) All rights reserved.
*
*/
package org.entando.entando.aps.system.services.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.entando.entando.aps.system.services.api.ApiMethodsDefDOM;
import org.jdom.Element;


/**
 * @author E.Santoboni
 */
public class ApiMethod implements Serializable {
	
	protected ApiMethod() {}
	
	public ApiMethod(Element element) {
		this.setMethodName(element.getAttributeValue(ApiMethodsDefDOM.METHOD_ATTRIBUTE_NAME));
		this.setActive(Boolean.parseBoolean(element.getAttributeValue(ApiMethodsDefDOM.ACTIVE_ATTRIBUTE_NAME)));
		this.setCanSpawnOthers(Boolean.parseBoolean(element.getAttributeValue(ApiMethodsDefDOM.CAN_SPAWN_OTHER_ATTRIBUTE_NAME)));
		Element sourceElement = element.getChild(ApiMethodsDefDOM.SOURCE_ELEMENT_NAME);
		this.setSource(sourceElement.getText());
		this.setPluginCode(sourceElement.getAttributeValue(ApiMethodsDefDOM.PLUGIN_CODE_ATTRIBUTE_NAME));
		this.setDescription(element.getChildText(ApiMethodsDefDOM.DESCRIPTION_ELEMENT_NAME));
		Element springBeanElement = element.getChild(ApiMethodsDefDOM.SPRING_BEAN_ELEMENT_NAME);
		this.setSpringBean(springBeanElement.getAttributeValue(ApiMethodsDefDOM.SPRING_BEAN_NAME_ATTRIBUTE_NAME));
		this.setSpringBeanMethod(springBeanElement.getAttributeValue(ApiMethodsDefDOM.SPRING_BEAN_METHOD_ATTRIBUTE_NAME));
		this.setResponseClassName(element.getChildText(ApiMethodsDefDOM.RESPONSE_CLASS_ELEMENT_NAME));
		Element parametersElement = element.getChild(ApiMethodsDefDOM.PARAMETERS_ELEMENT_NAME);
		if (null != parametersElement) {
			List<Element> parametersElements = parametersElement.getChildren(ApiMethodsDefDOM.PARAMETER_ELEMENT_NAME);
			for (int i = 0; i < parametersElements.size(); i++) {
				Element parameterElement = parametersElements.get(i);
				ApiMethodParameter parameter = new ApiMethodParameter(parameterElement);
				if (null == this.getParameters()) {
					this.setParameters(new ArrayList<ApiMethodParameter>());
				}
				this.getParameters().add(parameter);
			}
		}
		Element relatedShowletElement = element.getChild(ApiMethodsDefDOM.RELATED_SHOWLET_ELEMENT_NAME);
		if (null != relatedShowletElement) {
			this.setRelatedShowlet(new ApiMethodRelatedShowlet(relatedShowletElement));
		}
	}
	
	@Override
	public ApiMethod clone() {
		ApiMethod clone = new ApiMethod();
		clone.setActive(this.isActive());
		clone.setDescription(this.getDescription());
		clone.setMethodName(this.getMethodName());
		if (null != this.getParameters()) {
			List<ApiMethodParameter> clonedParameters = new ArrayList<ApiMethodParameter>();
			for (int i = 0; i < this.getParameters().size(); i++) {
				ApiMethodParameter clonedParameter = this.getParameters().get(i).clone();
				clonedParameters.add(clonedParameter);
			}
			clone.setParameters(clonedParameters);
		}
		clone.setPluginCode(this.getPluginCode());
		clone.setResponseClassName(this.getResponseClassName());
		clone.setSource(this.getSource());
		clone.setSpringBean(this.getSpringBean());
		clone.setSpringBeanMethod(this.getSpringBeanMethod());
		clone.setCanSpawnOthers(this.isCanSpawnOthers());
		if (null != this.getRelatedShowlet()) {
			clone.setRelatedShowlet(this.getRelatedShowlet().clone());
		}
		return clone;
	}
	
	public String getSource() {
		return _source;
	}
	protected void setSource(String source) {
		this._source = source;
	}
	
	public String getPluginCode() {
		return _pluginCode;
	}
	protected void setPluginCode(String pluginCode) {
		this._pluginCode = pluginCode;
	}
	
	public String getMethodName() {
		return _methodName;
	}
	protected void setMethodName(String methodName) {
		this._methodName = methodName;
	}
	
	public String getDescription() {
		return _description;
	}
	protected void setDescription(String description) {
		this._description = description;
	}
	
	public boolean isActive() {
		return _active;
	}
	public void setActive(boolean active) {
		this._active = active;
	}
	
	public boolean isCanSpawnOthers() {
		return _canSpawnOthers;
	}
	protected void setCanSpawnOthers(boolean canSpawnOthers) {
		this._canSpawnOthers = canSpawnOthers;
	}
	
	public String getSpringBean() {
		return _springBean;
	}
	protected void setSpringBean(String springBean) {
		this._springBean = springBean;
	}
	
	public String getSpringBeanMethod() {
		return _springBeanMethod;
	}
	protected void setSpringBeanMethod(String springBeanMethod) {
		this._springBeanMethod = springBeanMethod;
	}
	
	public String getResponseClassName() {
		return _responseClassName;
	}
	protected void setResponseClassName(String responseClassName) {
		this._responseClassName = responseClassName;
	}
	
	public List<ApiMethodParameter> getParameters() {
		return _parameters;
	}
	protected void setParameters(List<ApiMethodParameter> parameters) {
		this._parameters = parameters;
	}
	public ApiMethodParameter getParameter(String key) {
		if (null == key || key.trim().length() == 0 || this._parameters == null) return null;
		for (int i = 0; i < this._parameters.size(); i++) {
			ApiMethodParameter parameter = this._parameters.get(i);
			if (parameter.getKey().equals(key)) return parameter;
		}
		return null;
	}
	
	public ApiMethodRelatedShowlet getRelatedShowlet() {
		return _relatedShowlet;
	}
	protected void setRelatedShowlet(ApiMethodRelatedShowlet relatedShowlet) {
		this._relatedShowlet = relatedShowlet;
	}
	
	private String _source;
	private String _pluginCode;
	private String _methodName;
	private String _description;
	private boolean _active;
	private boolean _canSpawnOthers;
	private String _springBean;
	private String _springBeanMethod;
	private String _responseClassName;
	private List<ApiMethodParameter> _parameters;
	
	private ApiMethodRelatedShowlet _relatedShowlet;
	
}