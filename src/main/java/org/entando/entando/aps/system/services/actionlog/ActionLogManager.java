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
package org.entando.entando.aps.system.services.actionlog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.common.AbstractService;
import com.agiletec.aps.system.exception.ApsSystemException;
import com.agiletec.aps.system.services.authorization.IApsAuthority;
import com.agiletec.aps.system.services.group.Group;
import com.agiletec.aps.system.services.group.IGroupManager;
import com.agiletec.aps.system.services.keygenerator.IKeyGeneratorManager;
import com.agiletec.aps.system.services.user.UserDetails;

import org.entando.entando.aps.system.services.actionlog.model.ActionLogRecord;
import org.entando.entando.aps.system.services.actionlog.model.IActionLogRecordSearchBean;
import org.entando.entando.aps.system.services.cache.ICacheInfoManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 * @author E.Santoboni - S.Puddu
 */
public class ActionLogManager extends AbstractService implements IActionLogManager {
	
	@Override
	public void init() throws Exception {
		ApsSystemUtils.getLogger().config(this.getClass().getName() + ": ready");
	}
	
	@Override
	public void addActionRecord(ActionLogRecord actionRecord) throws ApsSystemException {
		try {
			int key = this.getKeyGeneratorManager().getUniqueKeyCurrentValue();
			actionRecord.setId(key);
			actionRecord.setActionDate(new Date());
			this.getActionLogDAO().addActionRecord(actionRecord);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "addActionRecord");
			throw new ApsSystemException("Error adding an jpactionlogger record", t);
		}
	}
	
	@Override
	@CacheEvict(value = ICacheInfoManager.CACHE_NAME, key = "'ActionLogRecord_'.concat(#id)")
	public void deleteActionRecord(int id) throws ApsSystemException {
		try {
			this.getActionLogDAO().deleteActionRecord(id);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "deleteActionRecord");
			throw new ApsSystemException("Error deleting the jpactionlogger record: " + id, t);
		}
	}
	
	@Override
	public List<Integer> getActionRecords(IActionLogRecordSearchBean searchBean) throws ApsSystemException {
		List<Integer> records = new ArrayList<Integer>();
		try {
			records = this.getActionLogDAO().getActionRecords(searchBean);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "getActionRecords");
			throw new ApsSystemException("Error loading actionlogger records", t);
		}
		return records;
	}
	
	@Override
	@Cacheable(value = ICacheInfoManager.CACHE_NAME, key = "'ActionLogRecord_'.concat(#id)")
	public ActionLogRecord getActionRecord(int id) throws ApsSystemException {
		ActionLogRecord record = null;
		try {
			record = this.getActionLogDAO().getActionRecord(id);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "getActionRecords");
			throw new ApsSystemException("Error loading actionlogger record with id: " + id, t);
		}
		return record;
	}
	
	@Override
	public List<Integer> getActivityStream(List<String> userGroupCodes) throws ApsSystemException {
		List<Integer> recordIds = null;
		try {
			recordIds = this.getActionLogDAO().getActivityStream(userGroupCodes);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "getActivityStream");
			throw new ApsSystemException("Error loading activity stream records", t);
		}
		return recordIds;
	}
	
	@Override
	public List<Integer> getActivityStream(UserDetails loggedUser) throws ApsSystemException {
		List<String> userGroupCodes = this.extractUserGroupCodes(loggedUser);
		return this.getActivityStream(userGroupCodes);
	}
	
	private List<String> extractUserGroupCodes(UserDetails loggedUser) {
		List<String> codes = new ArrayList<String>();
		IApsAuthority[] autorities = loggedUser.getAuthorities();
		if (null != autorities) {
			for (int i = 0; i < autorities.length; i++) {
				IApsAuthority autority = autorities[i];
				if (autority instanceof Group) {
					codes.add(autority.getAuthority());
				}
			}
		}
		if (!codes.contains(Group.FREE_GROUP_NAME)) {
			codes.add(Group.FREE_GROUP_NAME);
		}
		return codes;
	}
	
	protected IActionLogDAO getActionLogDAO() {
		return _actionLogDAO;
	}
	public void setActionLogDAO(IActionLogDAO actionLogDAO) {
		this._actionLogDAO = actionLogDAO;
	}
	
	protected IKeyGeneratorManager getKeyGeneratorManager() {
		return _keyGeneratorManager;
	}
	public void setKeyGeneratorManager(IKeyGeneratorManager keyGeneratorManager) {
		this._keyGeneratorManager = keyGeneratorManager;
	}
	
	protected IGroupManager getGroupManager() {
		return _groupManager;
	}
	public void setGroupManager(IGroupManager groupManager) {
		this._groupManager = groupManager;
	}
	
	private IActionLogDAO _actionLogDAO;
	private IKeyGeneratorManager _keyGeneratorManager;
	private IGroupManager _groupManager;
	
}