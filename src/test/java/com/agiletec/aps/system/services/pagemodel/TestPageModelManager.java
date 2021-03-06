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
package com.agiletec.aps.system.services.pagemodel;

import java.util.ArrayList;
import java.util.List;

import org.entando.entando.aps.system.services.widgettype.WidgetType;

import com.agiletec.aps.BaseTestCase;
import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.exception.ApsSystemException;
import com.agiletec.aps.system.services.page.Widget;
import com.agiletec.aps.system.services.pagemodel.IPageModelManager;
import com.agiletec.aps.system.services.pagemodel.PageModel;
import com.agiletec.aps.util.ApsProperties;

/**
 * @author M.Diana
 */
public class TestPageModelManager extends BaseTestCase {
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        this.init();
    }
    
    public void testGetPageModel() throws ApsSystemException {
		PageModel pageModel  = this._pageModelManager.getPageModel("home");
		String code = pageModel.getCode();
		String descr = pageModel.getDescr();
		assertEquals(code, "home");
		assertEquals(descr, "Modello home page");
		Widget[] showlets = pageModel.getDefaultWidget();
		for (int i = 0; i < showlets.length; i++) {
			Widget widget = showlets[i]; 
			assertEquals(widget, null);
		}
		String[] frames = pageModel.getFrames();
		assertEquals(frames[0], "Box sinistra alto");
		int mainFrame = pageModel.getMainFrame();
		assertEquals(mainFrame, 2);
	}
	
	public void testGetPageModels() throws ApsSystemException {
		List<PageModel> pageModels = new ArrayList<PageModel>(this._pageModelManager.getPageModels());
		assertEquals(3, pageModels.size());
		for (int i = 0; i < pageModels.size(); i++) {
			PageModel pageModel = pageModels.get(i);
			String code = pageModel.getCode();
			boolean isNotNull = (code != null);
			assertEquals(isNotNull, true);
			if (code.equals("home")) {
				assertEquals("Modello home page", pageModel.getDescr());				
			} else if(code.equals("service")){
				assertEquals("Modello pagine di servizio", pageModel.getDescr());
			}
		}
	} 	
	
	public void testGetModel() throws Throwable {
		PageModel model = this._pageModelManager.getPageModel("internal");
		assertNotNull(model);
		assertEquals(9, model.getFrames().length);
		Widget[] defaultWidgets = model.getDefaultWidget();
		assertEquals(model.getFrames().length, defaultWidgets.length);
		for (int i = 0; i < defaultWidgets.length; i++) {
			Widget widget = defaultWidgets[i];
			if (i==3) {
				assertNotNull(widget);
				WidgetType type = widget.getType();
				assertEquals("leftmenu", type.getCode());
				assertEquals(1, type.getTypeParameters().size());
				assertNull(type.getConfig());
				ApsProperties config = widget.getConfig();
				assertEquals(1, config.size());
				assertEquals("code(homepage).subtree(1)", config.getProperty("navSpec"));
			} else {
				assertNull(widget);
			}
		}
	}
	
	private void init() throws Exception {
    	try {
    		this._pageModelManager = (IPageModelManager) this.getService(SystemConstants.PAGE_MODEL_MANAGER);
    	} catch (Throwable t) {
    		throw new Exception(t);
        }
    }
    
    private IPageModelManager _pageModelManager = null;
    
}
