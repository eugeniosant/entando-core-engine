/*
*
* Copyright 2013 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
* This file is part of Entando Enterprise Edition software.
* You can redistribute it and/or modify it
* under the terms of the Entando's EULA
* 
* See the file License for the specific language governing permissions   
* and limitations under the License
* 
* 
* 
* Copyright 2013 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
*/
package org.entando.entando.aps.system.services.guifragment;

import java.io.Serializable;

/**
 * @author E.Santoboni
 */
public class GuiFragment implements Serializable {
	
	public String getCode() {
		return _code;
	}
	public void setCode(String code) {
		this._code = code;
	}
	
	@Deprecated
	public String getWidgetCode() {
		return this.getWidgetTypeCode();
	}
	@Deprecated
	public void setWidgetCode(String widgetCode) {
		this.setWidgetTypeCode(widgetCode);
	}
	
	public String getWidgetTypeCode() {
		return _widgetTypeCode;
	}
	public void setWidgetTypeCode(String widgetTypeCode) {
		this._widgetTypeCode = widgetTypeCode;
	}
	
	public String getPluginCode() {
		return _pluginCode;
	}
	public void setPluginCode(String pluginCode) {
		this._pluginCode = pluginCode;
	}
	
	public String getGui() {
		return _gui;
	}
	public void setGui(String gui) {
		this._gui = gui;
	}
	
	public String getDefaultGui() {
		return _defaultGui;
	}
	public void setDefaultGui(String defaultGui) {
		this._defaultGui = defaultGui;
	}
	
	public boolean isLocked() {
		return _locked;
	}
	public void setLocked(boolean locked) {
		this._locked = locked;
	}
	
	private String _code;
	private String _widgetTypeCode;
	private String _pluginCode;
	private String _gui;
	private String _defaultGui;
	private boolean _locked;
	
}
