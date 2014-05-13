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
package org.entando.entando.aps.system.services.guifragment.api;

import com.agiletec.aps.system.common.FieldSearchFilter;
import com.agiletec.aps.system.exception.ApsSystemException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import org.entando.entando.aps.system.services.api.IApiErrorCodes;
import org.entando.entando.aps.system.services.api.model.ApiException;
import org.entando.entando.aps.system.services.guifragment.GuiFragment;
import org.entando.entando.aps.system.services.guifragment.GuiFragmentUtilizer;
import org.entando.entando.aps.system.services.guifragment.IGuiFragmentManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * @author E.Santoboni
 */
public class ApiGuiFragmentInterface implements BeanFactoryAware {
	
	private static final Logger _logger = LoggerFactory.getLogger(ApiGuiFragmentInterface.class);
	
	public List<String> getGuiFragments(Properties properties) throws Throwable {
		List<String> list = null;
		try {
			String code = properties.getProperty("code");
			String widgettypecode = properties.getProperty("widgettypecode");
			String plugincode = properties.getProperty("plugincode");
			FieldSearchFilter[] filters = new FieldSearchFilter[0];
			FieldSearchFilter codeFilterToAdd = null;
			if (StringUtils.isNotBlank(code)) {
				codeFilterToAdd = new FieldSearchFilter("code", code, true);
			} else {
				codeFilterToAdd = new FieldSearchFilter("code");
			}
			codeFilterToAdd.setOrder(FieldSearchFilter.Order.ASC);
			filters = this.addFilter(filters, codeFilterToAdd);
			if (StringUtils.isNotBlank(widgettypecode)) {
				FieldSearchFilter filterToAdd = new FieldSearchFilter("widgettypecode", widgettypecode, true);
				filters = this.addFilter(filters, filterToAdd);
			}
			if (StringUtils.isNotBlank(plugincode)) {
				FieldSearchFilter filterToAdd = new FieldSearchFilter("plugincode", plugincode, true);
				filters = this.addFilter(filters, filterToAdd);
			}
			list = this.getGuiFragmentManager().searchGuiFragments(filters);
		} catch (Throwable t) {
			_logger.error("Error extracting list of fragments", t);
			throw t;
		}
		return list;
	}
	
	protected FieldSearchFilter[] addFilter(FieldSearchFilter[] filters, FieldSearchFilter filterToAdd) {
		int len = filters.length;
		FieldSearchFilter[] newFilters = new FieldSearchFilter[len + 1];
		for(int i=0; i < len; i++){
			newFilters[i] = filters[i];
		}
		newFilters[len] = filterToAdd;
		return newFilters;
	}
	
    public JAXBGuiFragment getGuiFragment(Properties properties) throws ApiException, Throwable {
        String code = properties.getProperty("code");
		JAXBGuiFragment jaxbGuiFragment = null;
		try {
			GuiFragment guiFragment = this.getGuiFragmentManager().getGuiFragment(code);
			if (null == guiFragment) {
				throw new ApiException(IApiErrorCodes.API_VALIDATION_ERROR, "GuiFragment with id '" + code + "' does not exist", Response.Status.CONFLICT);
			}
			jaxbGuiFragment = new JAXBGuiFragment(guiFragment);
		} catch (ApiException ae) {
			throw ae;
		} catch (Throwable t) {
			_logger.error("Error creating jaxb object of fragment - code '{}'", code, t);
			throw t;
		}
        return jaxbGuiFragment;
    }
	
    public void addGuiFragment(JAXBGuiFragment jaxbGuiFragment) throws ApiException, Throwable {
		try {
			if (null != this.getGuiFragmentManager().getGuiFragment(jaxbGuiFragment.getCode())) {
				throw new ApiException(IApiErrorCodes.API_VALIDATION_ERROR, "GuiFragment with code " + jaxbGuiFragment.getCode() + " already exists", Response.Status.CONFLICT);
			}
			GuiFragment guiFragment = jaxbGuiFragment.getGuiFragment();
			if (StringUtils.isBlank(guiFragment.getCurrentGui())) {
				throw new ApiException(IApiErrorCodes.API_VALIDATION_ERROR, "one between A and B must be valued", Response.Status.CONFLICT);
			}
			this.getGuiFragmentManager().addGuiFragment(guiFragment);
		} catch (ApiException ae) {
			throw ae;
		} catch (Throwable t) {
			_logger.error("Error adding new fragment", t);
			throw t;
		}
    }
	
    public void updateGuiFragment(JAXBGuiFragment jaxbGuiFragment) throws ApiException, Throwable {
		try {
			if (null == this.getGuiFragmentManager().getGuiFragment(jaxbGuiFragment.getCode())) {
				throw new ApiException(IApiErrorCodes.API_VALIDATION_ERROR, "GuiFragment with code '" + jaxbGuiFragment.getCode() + "' does not exist", Response.Status.CONFLICT);
			}
			GuiFragment guiFragment = jaxbGuiFragment.getGuiFragment();
			if (StringUtils.isBlank(guiFragment.getCurrentGui())) {
				throw new ApiException(IApiErrorCodes.API_VALIDATION_ERROR, "one between A and B must be valued", Response.Status.CONFLICT);
			}
			this.getGuiFragmentManager().updateGuiFragment(guiFragment);
		} catch (ApiException ae) {
			throw ae;
		} catch (Throwable t) {
			_logger.error("Error updating fragment", t);
			throw t;
		}
    }
	
    public void deleteGuiFragment(Properties properties) throws ApiException, Throwable {
        String code = properties.getProperty("code");
		try {
			GuiFragment guiFragment = this.getGuiFragmentManager().getGuiFragment(code);
			if (null == guiFragment) {
				throw new ApiException(IApiErrorCodes.API_VALIDATION_ERROR, "GuiFragment with code '" + code + "' does not exist", Response.Status.CONFLICT);
			}
			if (guiFragment.isLocked()) {
				throw new ApiException(IApiErrorCodes.API_VALIDATION_ERROR, "GuiFragment with code '" + code + "' are locked", Response.Status.CONFLICT);
			}
			Map<String, List<Object>> references = new HashMap<String, List<Object>>();
			ListableBeanFactory factory = (ListableBeanFactory) this.getBeanFactory();
			String[] defNames = factory.getBeanNamesForType(GuiFragmentUtilizer.class);
			for (int i=0; i < defNames.length; i++) {
				Object service = null;
				try {
					service = this.getBeanFactory().getBean(defNames[i]);
				} catch (Throwable t) {
					_logger.error("error extracting bean with name '{}'", defNames[i], t);
					throw new ApsSystemException("error extracting bean with name '" + defNames[i] + "'", t);
				}
				if (service != null) {
					GuiFragmentUtilizer guiFragUtilizer = (GuiFragmentUtilizer) service;
					List<Object> utilizers = guiFragUtilizer.getGuiFragmentUtilizers(code);
					if (utilizers != null && !utilizers.isEmpty()) {
						references.put(guiFragUtilizer.getName(), utilizers);
					}
				}
			}
			if (!references.isEmpty()) {
				throw new ApiException(IApiErrorCodes.API_VALIDATION_ERROR, "GuiFragment with code " + code + " has references with other object", Response.Status.CONFLICT);
			}
			this.getGuiFragmentManager().deleteGuiFragment(code);
		} catch (ApiException ae) {
			throw ae;
		} catch (Throwable t) {
			_logger.error("Error deleting fragment throw api", t);
			throw t;
		}
    }
	
	protected BeanFactory getBeanFactory() {
		return _beanFactory;
	}
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this._beanFactory = beanFactory;
	}
	
	protected IGuiFragmentManager getGuiFragmentManager() {
		return _guiFragmentManager;
	}
	public void setGuiFragmentManager(IGuiFragmentManager guiFragmentManager) {
		this._guiFragmentManager = guiFragmentManager;
	}
	
	private BeanFactory _beanFactory;
	private IGuiFragmentManager _guiFragmentManager;
	
}
