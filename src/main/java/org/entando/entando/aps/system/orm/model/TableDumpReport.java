/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.entando.entando.aps.system.orm.model;

import org.jdom.Element;

/**
 * @author E.Santoboni
 */
public class TableDumpReport {
	
	public TableDumpReport(String tableName) {
		this.setTableName(tableName);
	}
	
	protected TableDumpReport(Element element) {
		String tableName = element.getAttributeValue(NAME_ATTRIBUTE);
		this.setTableName(tableName);
		String rowsString = element.getAttributeValue(ROWS_ATTRIBUTE);
		this.setRows(Integer.parseInt(rowsString));
		String requiredTimeString = element.getAttributeValue(REQUIRED_TIME_ATTRIBUTE);
		this.setRequiredTime(Integer.parseInt(requiredTimeString));
	}
	
	public long getRequiredTime() {
		return _requiredTime;
	}
	public void setRequiredTime(long requiredTime) {
		this._requiredTime = requiredTime;
	}
	
	public int getRows() {
		return _rows;
	}
	public void setRows(int rows) {
		this._rows = rows;
	}
	
	public String getTableName() {
		return _tableName;
	}
	protected void setTableName(String tableName) {
		this._tableName = tableName;
	}
	
	protected Element toJdomElement() {
		Element element = new Element(TABLE_ELEMENT);
		element.setAttribute(NAME_ATTRIBUTE, this.getTableName());
		element.setAttribute(REQUIRED_TIME_ATTRIBUTE, String.valueOf(this.getRequiredTime()));
		element.setAttribute(ROWS_ATTRIBUTE, String.valueOf(this.getRows()));
		return element;
	}
	
	private String _tableName;
	private int _rows;
	private long _requiredTime;
	
	private static final String TABLE_ELEMENT = "table";
	private static final String NAME_ATTRIBUTE = "name";
	private static final String REQUIRED_TIME_ATTRIBUTE = "requiredTime";
	private static final String ROWS_ATTRIBUTE = "rows";
	
}
