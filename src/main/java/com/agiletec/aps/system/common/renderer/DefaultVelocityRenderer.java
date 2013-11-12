/*
*
* Copyright 2013 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
* This file is part of Entando software.
* Entando is a free software;
* You can redistribute it and/or modify it
* under the terms of the GNU General Public License (GPL) as published by the Free Software Foundation; version 2.
* 
* See the file License for the specific language governing permissions   
* and limitations under the License
* 
* 
* 
* Copyright 2013 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
*/
package com.agiletec.aps.system.common.renderer;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.common.AbstractService;
import com.agiletec.aps.system.exception.ApsSystemException;

import java.io.StringWriter;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

import org.slf4j.Logger;

/**
 * Entities rendering service.
 * @author M.Diana - W.Ambu - E.Santoboni
 */
public class DefaultVelocityRenderer extends AbstractService implements LogChute, IVelocityRenderer {
	
	@Override
	public void init() throws Exception {
		try {
			Velocity.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, this);
			Velocity.init();
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "init");
			throw new ApsSystemException("Error initializing the VelocityEngine", t);
		}
		ApsSystemUtils.getLogger().debug(this.getName() + ": initialized");
	}
	
	@Override
	public String render(Object object, String velocityTemplate) {
		String renderedObject = null;
		try {
			Context velocityContext = new VelocityContext();
			velocityContext.put(this.getWrapperContextName(), object);
			StringWriter stringWriter = new StringWriter();
			boolean isEvaluated = Velocity.evaluate(velocityContext, stringWriter, "render", velocityTemplate);
			if (!isEvaluated) {
				throw new ApsSystemException("Rendering error");
			}
			stringWriter.flush();
			renderedObject = stringWriter.toString();
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "render", "Rendering error");
			renderedObject = "";
		}
		return renderedObject;
	}
	
	@Override
	public void init(RuntimeServices rs) {
		//non fa nulla
	}
	
	@Override
	public boolean isLevelEnabled(int level) {
		return true;
	}
	
	@Override
	public void log(int level, String message) {
		this.log(level, message, null);
	}
	
	@Override
	public void log(int level, String message, Throwable t) {
		Logger logger = ApsSystemUtils.getLogger();
		if (t == null) {
			switch (level) {
				case TRACE_ID:
					logger.trace(message);
					break;
				case DEBUG_ID:
					logger.debug(message);
					break;
				case INFO_ID:
					logger.info(message);
					break;
				case WARN_ID:
					logger.warn(message);
					break;
				case ERROR_ID:
					logger.error(message);
					break;
				default:
					logger.info(message);
					break;
			}
		} else {
			logger.error(message, t);
		}
	}
	
	protected String getWrapperContextName() {
		if (null == this._wrapperContextName) {
			return DEFAULT_WRAPPER_CTX_NAME;
		}
		return _wrapperContextName;
	}
	
	public void setWrapperContextName(String wrapperContextName) {
		this._wrapperContextName = wrapperContextName;
	}
	
	private String _wrapperContextName;
	
	protected static final String DEFAULT_WRAPPER_CTX_NAME = "object";
	
}