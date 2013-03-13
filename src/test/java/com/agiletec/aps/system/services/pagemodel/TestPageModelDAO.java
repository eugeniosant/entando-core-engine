/*
*
* Copyright 2013 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
* This file is part of Entando software.
* Entando is a free software; 
* you can redistribute it and/or modify it
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

import java.util.Map;

import javax.sql.DataSource;

import com.agiletec.aps.BaseTestCase;

import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.services.pagemodel.PageModel;
import com.agiletec.aps.system.services.pagemodel.PageModelDAO;
import com.agiletec.aps.system.services.showlettype.IShowletTypeManager;

/**
 * @version 1.0
 * @author M.Diana
 */
public class TestPageModelDAO extends BaseTestCase {
	
    public void testLoadModels() throws Throwable {
    	DataSource dataSource = (DataSource) this.getApplicationContext().getBean("portDataSource");
    	IShowletTypeManager showletTypeManager = 
         	(IShowletTypeManager) this.getService(SystemConstants.SHOWLET_TYPE_MANAGER);
		PageModelDAO pageModelDAO = new PageModelDAO();
		pageModelDAO.setDataSource(dataSource);
		pageModelDAO.setShowletTypeManager(showletTypeManager);
		Map<String, PageModel> models = null;
        try {
            models = pageModelDAO.loadModels();
        } catch (Throwable t) {
            throw t;
        }
        assertEquals(models.containsKey("home"), true);
        assertEquals(models.containsKey("service"), true);
	}  
    	
}
