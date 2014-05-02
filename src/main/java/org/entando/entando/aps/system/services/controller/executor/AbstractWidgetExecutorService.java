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
package org.entando.entando.aps.system.services.controller.executor;

import com.agiletec.aps.system.RequestContext;
import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.common.FieldSearchFilter;
import com.agiletec.aps.system.exception.ApsSystemException;
import com.agiletec.aps.system.services.authorization.IAuthorizationManager;
import com.agiletec.aps.system.services.group.Group;
import com.agiletec.aps.system.services.page.IPage;
import com.agiletec.aps.system.services.page.Widget;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.aps.tags.util.IFrameDecoratorContainer;
import com.agiletec.aps.util.ApsWebApplicationUtils;

import freemarker.template.Template;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanComparator;
import org.entando.entando.aps.system.services.guifragment.GuiFragment;
import org.entando.entando.aps.system.services.guifragment.IGuiFragmentManager;
import org.entando.entando.aps.system.services.widgettype.WidgetType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.context.WebApplicationContext;

/**
 * @author E.Santoboni
 */
public abstract class AbstractWidgetExecutorService {
	
	private static final Logger _logger = LoggerFactory.getLogger(AbstractWidgetExecutorService.class);
	
	protected void buildWidgetsOutput(RequestContext reqCtx, IPage page, String[] widgetOutput) throws ApsSystemException {
		try {
			List<IFrameDecoratorContainer> decorators = this.extractDecorators(reqCtx);
			Widget[] widgets = page.getWidgets();
			for (int frame = 0; frame < widgets.length; frame++) {
				reqCtx.addExtraParam(SystemConstants.EXTRAPAR_CURRENT_FRAME, new Integer(frame));
				Widget widget = widgets[frame];
				widgetOutput[frame] = this.buildWidgetOutput(reqCtx, widget, decorators);
			}
		} catch (Throwable t) {
			String msg = "Error detected during widget preprocessing";
			_logger.error(msg, t);
			throw new ApsSystemException(msg, t);
		}
	}
	
	protected String buildWidgetOutput(RequestContext reqCtx, 
			Widget widget, List<IFrameDecoratorContainer> decorators) throws ApsSystemException {
		StringBuilder buffer = new StringBuilder();
		try {
			if (null != widget && this.isUserAllowed(reqCtx, widget)) {
				reqCtx.addExtraParam(SystemConstants.EXTRAPAR_CURRENT_WIDGET, widget);
			} else {
				reqCtx.removeExtraParam(SystemConstants.EXTRAPAR_CURRENT_WIDGET);
			}
			buffer.append(this.extractDecoratorsOutput(reqCtx, widget, decorators, false, true));
			if (null != widget && this.isUserAllowed(reqCtx, widget)) {
				String widgetOutput = this.extractWidgetOutput(reqCtx, widget.getType());
				//String widgetJspPath = widget.getType().getJspPath();
				buffer.append(this.extractDecoratorsOutput(reqCtx, widget, decorators, true, true));
				//buffer.append(this.extractJspOutput(reqCtx, widgetJspPath));
				buffer.append(widgetOutput);
				buffer.append(this.extractDecoratorsOutput(reqCtx, widget, decorators, true, false));
			}
			buffer.append(this.extractDecoratorsOutput(reqCtx, widget, decorators, false, false));
		} catch (Throwable t) {
			String msg = "Error creating widget output";
			_logger.error(msg, t);
			throw new RuntimeException(msg, t);
		}
		return buffer.toString();
	}
	
	protected String extractWidgetOutput(RequestContext reqCtx, WidgetType type) throws ApsSystemException {
		try {
			String widgetTypeCode = type.getCode();
			FieldSearchFilter filter = new FieldSearchFilter("widgettypecode", widgetTypeCode, false);
			FieldSearchFilter[] filters = {filter};
			IGuiFragmentManager guiFragmentManager = 
					(IGuiFragmentManager) ApsWebApplicationUtils.getBean(SystemConstants.GUI_FRAGMENT_MANAGER, reqCtx.getRequest());
			List<String> codes = guiFragmentManager.searchGuiFragments(filters);
			if (null != codes && !codes.isEmpty()) {
				String code = codes.get(0);
				GuiFragment guiFragment = guiFragmentManager.getGuiFragment(code);
				ExecutorBeanContainer ebc = (ExecutorBeanContainer) reqCtx.getExtraParam(SystemConstants.EXTRAPAR_EXECUTOR_BEAN_CONTAINER);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				Writer out = new OutputStreamWriter(baos);
				Template template = new Template(type.getCode(), new StringReader(guiFragment.getGui()), ebc.getConfiguration());
				template.process(ebc.getTemplateModel(), out);
				out.flush();
				return baos.toString();
			} else {
				String widgetJspPath = type.getJspPath();
				return this.extractJspWidgetOutput(widgetTypeCode, reqCtx, widgetJspPath);
			}
		} catch (Throwable t) {
			String msg = "Error creating widget output";
			_logger.error(msg, t);
			throw new ApsSystemException(msg, t);
		}
	}
	
	protected List<IFrameDecoratorContainer> extractDecorators(RequestContext reqCtx) throws ApsSystemException {
		HttpServletRequest request = reqCtx.getRequest();
		WebApplicationContext wac = ApsWebApplicationUtils.getWebApplicationContext(request);
		List<IFrameDecoratorContainer> containters = new ArrayList<IFrameDecoratorContainer>();
		try {
			String[] beanNames = wac.getBeanNamesForType(IFrameDecoratorContainer.class);
			for (int i = 0; i < beanNames.length; i++) {
				IFrameDecoratorContainer container = (IFrameDecoratorContainer) wac.getBean(beanNames[i]);
				containters.add(container);
			}
			BeanComparator comparator = new BeanComparator("order");
			Collections.sort(containters, comparator);
		} catch (Throwable t) {
			_logger.error("Error extracting widget decorators", t);
			throw new ApsSystemException("Error extracting widget decorators", t);
		}
		return containters;
	}
	
	protected String extractDecoratorsOutput(RequestContext reqCtx, Widget widget, 
			List<IFrameDecoratorContainer> decorators, boolean isWidgetDecorator, boolean includeHeader) throws Throwable {
		if (null == decorators || decorators.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < decorators.size(); i++) {
			IFrameDecoratorContainer decoratorContainer = (includeHeader)
					? decorators.get(i)
					: decorators.get(decorators.size() - i - 1);
			if ((isWidgetDecorator != decoratorContainer.isShowletDecorator()) 
					|| !decoratorContainer.needsDecoration(widget, reqCtx)) {
				continue;
			}
			String path = (includeHeader) ? decoratorContainer.getHeaderPath() : decoratorContainer.getFooterPath();
			if (null != path && path.trim().length() > 0) {
				String output = this.extractJspOutput(reqCtx, path);
				builder.append(output);
			}
		}
		return builder.toString();
	}
	
	protected boolean isUserAllowed(RequestContext reqCtx, Widget widget) /*throws Throwable */{
		if (null == widget) {
			return false;
		}
		String widgetTypeGroup = widget.getType().getMainGroup();
		try {
			if (null == widgetTypeGroup || widgetTypeGroup.equals(Group.FREE_GROUP_NAME)) {
				return true;
			}
			IAuthorizationManager authorizationManager = (IAuthorizationManager) ApsWebApplicationUtils.getBean(SystemConstants.AUTHORIZATION_SERVICE, reqCtx.getRequest());
			UserDetails currentUser = (UserDetails) reqCtx.getRequest().getSession().getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER);
			return authorizationManager.isAuthOnGroup(currentUser, widgetTypeGroup);
		} catch (Throwable t) {
			_logger.error("Error checking user authorities", t);
		}
		return false;
	}
	
	protected String extractJspWidgetOutput(String widgetTypeCode, RequestContext reqCtx, String jspPath) throws Throwable {
		try {
			return this.extractJspOutput(reqCtx, jspPath);
		} catch (IOException e) {
			_logger.error("The widget '{}' is unavailable. Expected jsp path '{}'", widgetTypeCode, jspPath, e);
			return "The widget '" + widgetTypeCode + "' is unavailable";
		} catch (Throwable t) {
			_logger.error("Error extracting jsp output", t);
			throw t;
		}
	}
	
	protected String extractJspOutput(RequestContext reqCtx, String jspPath) throws ServletException, IOException {
		HttpServletRequest request = reqCtx.getRequest();
		HttpServletResponse response = reqCtx.getResponse();
		BufferedHttpResponseWrapper wrapper = new BufferedHttpResponseWrapper(response);
		ServletContext context = request.getSession().getServletContext();
		String url = response.encodeRedirectURL(jspPath);
		RequestDispatcher dispatcher = context.getRequestDispatcher(url);
		dispatcher.include(request, wrapper);
		return wrapper.getOutput();
	}
	
}