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
package org.entando.entando.aps.system.services.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.entando.entando.aps.system.services.api.model.ApiMethod;
import org.entando.entando.aps.system.services.api.model.ApiService;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.common.AbstractDAO;
import com.agiletec.aps.util.ApsProperties;

/**
 * @author E.Santoboni
 */
public class ApiCatalogDAO extends AbstractDAO implements IApiCatalogDAO {
	
	@Override
	public void loadApiStatus(Map<String, ApiMethod> methods) {
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet res = null;
		List<String> invalidMethods = new ArrayList<String>();
		try {
			conn = this.getConnection();
			conn.setAutoCommit(false);
			stat = conn.prepareStatement(LOAD_API_STATUS);
			//"SELECT method, isactive FROM apicatalog_status";
			res = stat.executeQuery();
			while (res.next()) {
				String methodName = res.getString(1);
				ApiMethod method = methods.get(methodName);
				if (null == method) {
					invalidMethods.add(methodName);
				} else {
					boolean isActive = (res.getInt("isactive") == 1);
					method.setActive(isActive);
				}
			}
			for (int i = 0; i < invalidMethods.size(); i++) {
				this.removeApiStatus(invalidMethods.get(i), conn);
			}
			conn.commit();
		} catch (Throwable t) {
			this.executeRollback(conn);
			processDaoException(t, "Error while loading api status ", "loadApiStatus");
		} finally {
			closeDaoResources(res, stat, conn);
		}
	}
	
	@Override
	public void saveApiStatus(ApiMethod method) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = this.getConnection();
			conn.setAutoCommit(false);
			this.removeApiStatus(method.getMethodName(), conn);
			stat = conn.prepareStatement(ADD_API_STATUS);
			//INSERT INTO apicatalog_status(method, isactive) VALUES ( ? , ? )
			int isActive = (method.isActive()) ? 1 : 0;
			stat.setString(1, method.getMethodName());
			stat.setInt(2, isActive);
			stat.executeUpdate();
			conn.commit();
		} catch (Throwable t) {
			this.executeRollback(conn);
			processDaoException(t, "Error while saving api status", "saveApiStatus");
		} finally {
			closeDaoResources(null, stat, conn);
		}
	}
	
	protected void removeApiStatus(String method, Connection conn) {
		PreparedStatement stat = null;
		try {
			stat = conn.prepareStatement(REMOVE_API_STATUS);
			stat.setString(1, method);
			stat.executeUpdate();
		} catch (Throwable t) {
			processDaoException(t, "Error deleting method status : method '" + method + "'" , "removeApiStatus");
		} finally {
			closeDaoResources(null, stat);
		}
	}
	
	@Override
	public Map<String, ApiService> loadServices(Map<String, ApiMethod> methods) {
		Map<String, ApiService> services = new HashMap<String, ApiService>();
		Connection conn = null;
		Statement stat = null;
		ResultSet res = null;
		List<String> invalidServices = new ArrayList<String>();
		try {
			conn = this.getConnection();
			stat = conn.createStatement();
			res = stat.executeQuery(LOAD_SERVICES);
			//servicekey, parentapi, description, parameters, 
			//tag, freeparameters, isactive, ispublic
			while (res.next()) {
				this.buildService(methods, services, invalidServices, res);
			}
		} catch (Throwable t) {
			processDaoException(t, "Error while loading groups", "loadGroups");
		} finally {
			closeDaoResources(res, stat, conn);
		}
		return services;
	}
	
	private void buildService(Map<String, ApiMethod> methods, 
			Map<String, ApiService> services, List<String> invalidServices, ResultSet res) {
		String key = null;
		try {
			key = res.getString(1);
			String parentCode = res.getString(2);
			ApiMethod masterMethod = methods.get(parentCode);
			if (null != masterMethod) {
				ApsProperties description = new ApsProperties();
				description.loadFromXml(res.getString(3));
				ApsProperties parameters = new ApsProperties();
				parameters.loadFromXml(res.getString(4));
				String tag = res.getString(5);
				String[] freeParameters = null;
				String freeParamString = res.getString(6);
				if (null != freeParamString && freeParamString.trim().length() > 0) {
					ServiceExtraConfigDOM dom = new ServiceExtraConfigDOM(freeParamString);
					freeParameters = dom.extractFreeParameters();
				}
				boolean isActive = (1 == res.getInt(7)) ? true : false;
				boolean isPublic = (1 == res.getInt(8)) ? true : false;
                                boolean isMyEntando = (1 == res.getInt(9)) ? true : false;
				ApiService apiService = new ApiService(key, description, masterMethod, 
                                        parameters, freeParameters, tag, isPublic, isActive, isMyEntando);
				services.put(key, apiService);
			} else {
				invalidServices.add(key);
			}
		} catch (Throwable t) {
			ApsSystemUtils.getLogger().severe("Error building service - key '" + key + "'");
		}
	}
	
	@Override
	public void addService(ApiService service) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = this.getConnection();
			conn.setAutoCommit(false);
			stat = conn.prepareStatement(ADD_SERVICE);
			//servicekey, parentapi, description, parameters, tag, freeparameters, isactive, ispublic
			stat.setString(1, service.getKey());
			stat.setString(2, service.getMaster().getMethodName());
			stat.setString(3, service.getDescription().toXml());
			stat.setString(4, service.getParameters().toXml());
			stat.setString(5, service.getTag());
			if (null == service.getFreeParameters() || service.getFreeParameters().length == 0) {
				stat.setNull(6, Types.VARCHAR);
			} else {
				ServiceExtraConfigDOM dom = new ServiceExtraConfigDOM();
				stat.setString(6, dom.extractXml(service.getFreeParameters()));
			}
			int isActive = (service.isActive()) ? 1 : 0;
			stat.setInt(7, isActive);
			int isPublic = (service.isPublicService()) ? 1 : 0;
			stat.setInt(8, isPublic);
                        int isMyEntando = (service.isMyEntando()) ? 1 : 0;
			stat.setInt(9, isMyEntando);
			stat.executeUpdate();
			conn.commit();
		} catch (Throwable t) {
			this.executeRollback(conn);
			processDaoException(t, "Error while adding a service", "addService");
		} finally {
			closeDaoResources(null, stat, conn);
		}
	}
	
	@Override
	public void updateService(ApiService service) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = this.getConnection();
			conn.setAutoCommit(false);
			stat = conn.prepareStatement(UPDATE_SERVICE);
			//SET parentapi = ? , description = ? , parameters = ? , tag = ? , freeparameters = ? , isactive = ? , ispublic = ? WHERE servicekey = ? ";
			stat.setString(1, service.getMaster().getMethodName());
			stat.setString(2, service.getDescription().toXml());
			stat.setString(3, service.getParameters().toXml());
			stat.setString(4, service.getTag());
			if (null == service.getFreeParameters() || service.getFreeParameters().length == 0) {
				stat.setNull(5, Types.VARCHAR);
			} else {
				ServiceExtraConfigDOM dom = new ServiceExtraConfigDOM();
				stat.setString(5, dom.extractXml(service.getFreeParameters()));
			}
			int isActive = (service.isActive()) ? 1 : 0;
			stat.setInt(6, isActive);
			int isPublic = (service.isPublicService()) ? 1 : 0;
			stat.setInt(7, isPublic);
                        int isMyEntando = (service.isMyEntando()) ? 1 : 0;
			stat.setInt(8, isMyEntando);
			stat.setString(9, service.getKey());
			stat.executeUpdate();
			conn.commit();
		} catch (Throwable t) {
			this.executeRollback(conn);
			processDaoException(t, "Error while updating a service", "updateService");
		} finally {
			closeDaoResources(null, stat, conn);
		}
	}
	
	@Override
	public void deleteService(String key) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = this.getConnection();
			conn.setAutoCommit(false);
			stat = conn.prepareStatement(DELETE_SERVICE);
			stat.setString(1, key);
			stat.executeUpdate();
			conn.commit();
		} catch (Throwable t) {
			this.executeRollback(conn);
			processDaoException(t, "Error while deleting a service", "deleteService");
		} finally {
			closeDaoResources(null, stat, conn);
		}
	}
	
	private static final String LOAD_API_STATUS = 
			"SELECT method, isactive FROM apicatalog_status";
	
	private static final String ADD_API_STATUS = 
			"INSERT INTO apicatalog_status(method, isactive) VALUES ( ? , ? )";
	
	private static final String REMOVE_API_STATUS = 
			"DELETE FROM apicatalog_status WHERE method = ?";
	
	private static final String LOAD_SERVICES = 
			"SELECT servicekey, parentapi, description, parameters, tag, " +
			"freeparameters, isactive, ispublic, myentando FROM apicatalog_services";
	
	private static final String ADD_SERVICE = 
			"INSERT INTO apicatalog_services(servicekey, parentapi, " +
			"description, parameters, tag, freeparameters, isactive, ispublic, myentando) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? ) ";
	
	private static final String UPDATE_SERVICE = 
			"UPDATE apicatalog_services SET parentapi = ? , description = ? , parameters = ? , tag = ? , freeparameters = ? , isactive = ? , ispublic = ? , myentando = ? WHERE servicekey = ? ";
	
	private static final String DELETE_SERVICE = 
			"DELETE FROM apicatalog_services WHERE servicekey = ? ";
	
}
