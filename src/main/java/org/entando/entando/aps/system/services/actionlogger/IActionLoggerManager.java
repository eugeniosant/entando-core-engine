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
package org.entando.entando.aps.system.services.actionlogger;

import java.util.List;

import com.agiletec.aps.system.exception.ApsSystemException;
import org.entando.entando.aps.system.services.actionlogger.model.ActionRecord;
import org.entando.entando.aps.system.services.actionlogger.model.IActionRecordSearchBean;

/**
 * Interface for the service that manages the {@link ActionRecord}
 *
 */
public interface IActionLoggerManager {
	
	/**
	 * Load a list of {@link ActionRecord} codes that match the search criteria rapresented by the searchBean
	 * @param searchBean object containing the search criteria
	 * @return a list of codes
	 * @throws ApsSystemException if an error occurs
	 */
	public List<Integer> getActionRecords(IActionRecordSearchBean searchBean) throws ApsSystemException;
	
	/**
	 * Save a new {@link ActionRecord}
	 * @param actionRecord
	 * @throws ApsSystemException
	 */
	public void addActionRecord(ActionRecord actionRecord) throws ApsSystemException;
	
	/**
	 * Load a {@link ActionRecord}
	 * @param id the code of the record to load
	 * @return an {@link ActionRecord} 
	 * @throws ApsSystemException if an error occurs
	 */
	public ActionRecord getActionRecord(int id) throws ApsSystemException;
	
	/**
	 * Delete a {@link ActionRecord}
	 * @param id the code of the record to delete
	 * @throws ApsSystemException if an error occurs
	 */
	public void deleteActionRecord(int id) throws ApsSystemException;
	
}