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
package org.entando.entando.aps.system.init.model.servdb;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.entando.entando.aps.system.init.IDatabaseManager;
import org.entando.entando.aps.system.init.model.ExtendedColumnDefinition;

/**
 * @author E.Santoboni
 */
@DatabaseTable(tableName = UserProfileAttributeRole.TABLE_NAME)
public class UserProfileAttributeRole implements ExtendedColumnDefinition {
	
	public UserProfileAttributeRole() {}
	
	@DatabaseField(foreign = true, columnName = "username", 
			width = 40, 
			canBeNull = false, index = true)
	private UserProfile _username;
	
	@DatabaseField(columnName = "attrname", 
			dataType = DataType.STRING, 
			width = 30, 
			canBeNull = false, index = true)
	private String _attributeName;
	
	@DatabaseField(columnName = "rolename", 
			dataType = DataType.STRING, 
			width = 50, 
			canBeNull = false, index = true)
	private String _roleName;
	
	@Override
	public String[] extensions(IDatabaseManager.DatabaseType type) {
		String tableName = TABLE_NAME;
		String profileTableName = UserProfile.TABLE_NAME;
		if (IDatabaseManager.DatabaseType.MYSQL.equals(type)) {
			tableName = "`" + tableName + "`";
			profileTableName = "`" + UserProfile.TABLE_NAME + "`";
		}
		return new String[]{"ALTER TABLE " + tableName + " " 
				+ "ADD CONSTRAINT " + TABLE_NAME + "_fkey FOREIGN KEY (username) "
				+ "REFERENCES " + profileTableName + " (username)"};
	}
	
	public static final String TABLE_NAME = "authuserprofileattrroles";
	
}