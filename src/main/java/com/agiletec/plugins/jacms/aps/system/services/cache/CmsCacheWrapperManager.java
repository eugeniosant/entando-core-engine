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
package com.agiletec.plugins.jacms.aps.system.services.cache;

import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.RequestContext;
import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.common.AbstractService;
import com.agiletec.aps.system.common.entity.event.EntityTypesChangingEvent;
import com.agiletec.aps.system.common.entity.event.EntityTypesChangingObserver;
import com.agiletec.aps.system.common.entity.model.IApsEntity;
import com.agiletec.aps.system.services.page.IPage;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.content.widget.IContentListTagBean;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.ContentModel;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.event.ContentModelChangedEvent;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.event.ContentModelChangedObserver;
import com.agiletec.plugins.jacms.aps.system.services.resource.ResourceUtilizer;
import com.agiletec.plugins.jacms.aps.system.services.resource.event.ResourceChangedEvent;
import com.agiletec.plugins.jacms.aps.system.services.resource.event.ResourceChangedObserver;
import com.agiletec.plugins.jacms.aps.system.services.resource.model.ResourceInterface;

import org.entando.entando.aps.system.services.cache.ICacheInfoManager;

/**
 * Cache Wrapper Manager for plugin jacms
 * @author E.Santoboni
 */
public class CmsCacheWrapperManager extends AbstractService 
		implements ICmsCacheWrapperManager, /*PublicContentChangedObserver, */ContentModelChangedObserver, EntityTypesChangingObserver, ResourceChangedObserver {
	
	@Override
	public void init() throws Exception {
		ApsSystemUtils.getLogger().debug(this.getClass().getName() + ": initialized");
	}
	/*
	@Override
	public void updateFromPublicContentChanged(PublicContentChangedEvent event) {
		try {
			Content content = event.getContent();
			Logger log = ApsSystemUtils.getLogger();
			if (log.isLoggable(Level.FINEST)) {
				log.info("Notified public content update : type " + content.getId());
			}
			this.releaseRelatedItems(content);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "updateFromPublicContentChanged", 
					"Error notifing event " + PublicContentChangedEvent.class.getName());
		}
	}
	*/
	@Override
	public void updateFromContentModelChanged(ContentModelChangedEvent event) {
		try {
			ContentModel model = event.getContentModel();
			Logger log = ApsSystemUtils.getLogger();
			log.info("Notified content model update : type " + model.getId());
			String cacheGroupKey = JacmsSystemConstants.CONTENT_MODEL_CACHE_GROUP_PREFIX + model.getId();
			this.getCacheInfoManager().flushGroup(cacheGroupKey);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "updateFromContentModelChanged", 
					"Error notifing event " + ContentModelChangedEvent.class.getName());
		}
	}
	
	@Override
	public void updateFromEntityTypesChanging(EntityTypesChangingEvent event) {
		try {
			String entityManagerName = event.getEntityManagerName();
			if (!entityManagerName.equals(JacmsSystemConstants.CONTENT_MANAGER)) return;
			if (event.getOperationCode() == EntityTypesChangingEvent.INSERT_OPERATION_CODE) return;
			IApsEntity oldEntityType = event.getOldEntityType();
			Logger log = ApsSystemUtils.getLogger();
			log.info("Notified content type modify : type " + oldEntityType.getTypeCode());
			String typeGroupKey = JacmsSystemConstants.CONTENT_TYPE_CACHE_GROUP_PREFIX + oldEntityType.getTypeCode();
			this.getCacheInfoManager().flushGroup(typeGroupKey);
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "updateFromEntityTypesChanging", 
					"Error notifing event " + EntityTypesChangingEvent.class.getName());
		}
	}
	
	@Override
	public void updateFromResourceChanged(ResourceChangedEvent event) {
		try {
			ResourceInterface resource = event.getResource();
			if (null == resource) {
				return;
			}
			List<String> utilizers = ((ResourceUtilizer) this.getContentManager()).getResourceUtilizers(resource.getId());
			for (int i = 0; i < utilizers.size(); i++) {
				String contentId = utilizers.get(i);
				Content content = this.getContentManager().loadContent(contentId, true);
				if (null != content) {
					this.releaseRelatedItems(content);
				}
			}
		} catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "updateFromResourceChanged", 
					"Error notifing event " + ResourceChangedEvent.class.getName());
		}
	}
	
	protected void releaseRelatedItems(Content content) {
		this.getCacheInfoManager().flushGroup(JacmsSystemConstants.CONTENT_CACHE_GROUP_PREFIX + content.getId());
		this.getCacheInfoManager().flushGroup(JacmsSystemConstants.CONTENTS_ID_CACHE_GROUP_PREFIX + content.getTypeCode());
		this.getCacheInfoManager().flushEntry(JacmsSystemConstants.CONTENT_CACHE_PREFIX + content.getId());
	}
	
	public static String getContentCacheGroupsCsv(String contentId) {
		StringBuilder builder = new StringBuilder();
		if (null != contentId) {
			String typeCode = contentId.substring(0, 3);
			String contentCacheGroupId = JacmsSystemConstants.CONTENT_CACHE_GROUP_PREFIX + contentId;
			String typeCacheGroupId = JacmsSystemConstants.CONTENT_TYPE_CACHE_GROUP_PREFIX + typeCode;
			builder.append(contentCacheGroupId).append(",").append(typeCacheGroupId);
		}
		return builder.toString();
	}
	
	public static String getContentListCacheGroupsCsv(IContentListTagBean bean, RequestContext reqCtx) {
		StringBuilder builder = new StringBuilder();
		IPage page = (IPage) reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_PAGE);
		String pageCacheGroupName = SystemConstants.PAGES_CACHE_GROUP_PREFIX + page.getCode();
		String contentTypeCacheGroupName = JacmsSystemConstants.CONTENTS_ID_CACHE_GROUP_PREFIX + bean.getContentType();
		builder.append(pageCacheGroupName).append(",").append(contentTypeCacheGroupName);
		return builder.toString();
	}
	
	public static String getContentCacheGroupsToEvictCsv(String contentId, String typeCode) {
		StringBuilder builder = new StringBuilder();
		String contentsIdCacheGroupId = JacmsSystemConstants.CONTENTS_ID_CACHE_GROUP_PREFIX + typeCode;
		builder.append(contentsIdCacheGroupId);
		if (null != contentId) {
			String contentCacheGroupId = JacmsSystemConstants.CONTENT_CACHE_GROUP_PREFIX + contentId;
			builder.append(",").append(contentCacheGroupId);
		}
		return builder.toString();
	}
	
	protected IContentManager getContentManager() {
		return _contentManager;
	}
	public void setContentManager(IContentManager contentManager) {
		this._contentManager = contentManager;
	}
	
	protected ICacheInfoManager getCacheInfoManager() {
		return _cacheInfoManager;
	}
	public void setCacheInfoManager(ICacheInfoManager cacheInfoManager) {
		this._cacheInfoManager = cacheInfoManager;
	}
	
	private IContentManager _contentManager;
	private ICacheInfoManager _cacheInfoManager;
	
}