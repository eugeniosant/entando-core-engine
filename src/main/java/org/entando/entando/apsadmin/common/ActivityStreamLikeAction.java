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
package org.entando.entando.apsadmin.common;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.apsadmin.system.BaseAction;

import org.entando.entando.aps.system.services.actionlog.IActionLogManager;

/**
 * @author E.Santoboni
 */
public class ActivityStreamLikeAction extends BaseAction {
	
	public String likeActivity() {
		return this.editLikeActivity(true);
	}
	
	public String unlikeActivity() {
		return this.editLikeActivity(false);
	}
	
	public String editLikeActivity(boolean add) {
		try {
			if (null == this.getRecordId()) {
				ApsSystemUtils.getLogger().error(this + " - Null record id");
				return SUCCESS;
			}
			UserDetails user = super.getCurrentUser();
			this.getActionLogManager().editActionLikeRecord(this.getRecordId(), user.getUsername(), add);
		} catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "editLikeActivity", "Error on like/unlike activity");
            return FAILURE;
        }
		return SUCCESS;
	}
	
	public Integer getRecordId() {
		return _recordId;
	}
	public void setRecordId(Integer recordId) {
		this._recordId = recordId;
	}
	
	protected IActionLogManager getActionLogManager() {
		return _actionLogManager;
	}
	public void setActionLogManager(IActionLogManager actionLogManager) {
		this._actionLogManager = actionLogManager;
	}
	
	private Integer _recordId;
	
	private IActionLogManager _actionLogManager;
	
}