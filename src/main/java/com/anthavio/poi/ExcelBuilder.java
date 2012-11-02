package com.anthavio.poi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.UnhandledException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelBuilder {

	private static class DirectByteArrayOutputStream extends ByteArrayOutputStream {
		// ByteArrayOutputStream.toByteArray() creates copy of internal byte buffer
		public byte[] getBytes() {
			return buf;
		}
	}

	protected final Logger log = LoggerFactory.getLogger(ExcelBuilder.class);

	private static final int MAX_LIST_NAME_LENGTH = 30;

	protected HSSFWorkbook workBook;

	protected Locale locale;

	protected CreationHelper helper;

	protected HSSFCellStyle headerStyle;

	protected HSSFCellStyle cellStyle;

	protected HSSFCellStyle dateStyle;

	private final String dateFormat = "d.m.yyyy"; // d.m.yyyy h:mm:ss

	public ExcelBuilder() {
		this(null, null);
	}

	public ExcelBuilder(String sheetName) {
		this(sheetName, null);
	}

	public ExcelBuilder(String sheetName, Locale locale) {
		workBook = new HSSFWorkbook();
		helper = workBook.getCreationHelper();
		workBook.createSheet(getSheetName(sheetName));
		if (locale == null) {
			this.locale = Locale.getDefault();
		} else {
			this.locale = locale;
		}
		setDateFormat(dateFormat);
	}

	public ExcelBuilder(InputStream input) throws IOException {
		workBook = new HSSFWorkbook(input);
		helper = workBook.getCreationHelper();
		workBook.setActiveSheet(0);
		setDateFormat(dateFormat);
	}

	/**
	 * @return underlaying HSSFWorkbook
	 */
	public HSSFWorkbook getWorkBook() {
		return this.workBook;
	}

	protected String getSheetName(String sheetName) {
		if (sheetName == null) {
			return "List";
		}
		// max size of sheetName
		if (sheetName.length() > MAX_LIST_NAME_LENGTH) {
			StringBuilder sb = new StringBuilder();
			sheetName = sheetName.substring(0, MAX_LIST_NAME_LENGTH - 3);
			sheetName = sheetName + "..";
			sb.append(sheetName);
			sheetName = sb.toString();
		}
		return sheetName;
	}

	/**
	 * @return InputStream of WorkBook internal byte[] content
	 */
	public InputStream getInputStream() {
		DirectByteArrayOutputStream baos = new DirectByteArrayOutputStream();
		write(baos);
		return new ByteArrayInputStream(baos.getBytes());
	}

	/**
	 * Writes WorkBook internal byte[] content and closes stream
	 */
	public void write(OutputStream os) {
		try {
			workBook.write(os);
			os.flush();
			os.close();
		} catch (IOException iox) {
			throw new UnhandledException(iox);
		}
	}

	/**
	 * Writes WorkBook internal byte[] content into file
	 */
	public void write(File file) {
		try {
			write(new FileOutputStream(file));
		} catch (FileNotFoundException fnfx) {
			throw new UnhandledException(fnfx);
		}
	}

	/**
	 * Create row on specified rowIdx and populates it with header styled names
	 */
	public HSSFRow createHeader(int rowIdx, String... names) {
		HSSFRow row = getCurrentSheet().getRow(rowIdx);
		if (row == null) {
			row = createRow(rowIdx);
		}
		for (int i = 0; i < names.length; i++) {
			createHeaderCell(row, i, names[i]);
		}
		autosizeColumns(row);
		return row;
	}

	/**
	 * Populates specified row with header styled names
	 */
	public void createHeader(HSSFRow row, String... names) {
		for (int i = 0; i < names.length; i++) {
			createHeaderCell(row, i, names[i]);
		}
	}

	public HSSFCell createHeaderCell(HSSFRow row, int colIdx, String text) {
		HSSFCell cell = row.createCell(colIdx);
		cell.setCellValue(createTextValue(text));
		cell.setCellStyle(getHeaderCellStyle());
		return cell;
	}

	public HSSFCellStyle getHeaderCellStyle() {
		if (headerStyle == null) {
			// default header style
			headerStyle = workBook.createCellStyle();
			HSSFFont font = workBook.createFont();
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			headerStyle.setFont(font);
			headerStyle.setBorderBottom((short) 1);
		}
		return headerStyle;
	}

	public void setHeaderStyle(HSSFCellStyle style) {
		this.headerStyle = style;
	}

	/*
	 * private HSSFCellStyle getInfoCellStyle() { HSSFCellStyle style =
	 * workBook.createCellStyle(); HSSFFont font = workBook.createFont();
	 * font.setItalic(true); style.setFont(font); return style; }
	 */

	public HSSFCellStyle getCellStyle() {
		if (cellStyle == null) {
			// default cell style
			cellStyle = workBook.createCellStyle();
		}
		return cellStyle;
	}

	public void setCellStyle(HSSFCellStyle style) {
		this.cellStyle = style;
	}

	/**
	 * @param dateFormat
	 *          for example d.m.yyyy or d.m.yyyy h:mm:ss
	 */
	public void setDateFormat(String dateFormat) {
		dateStyle = workBook.createCellStyle();
		// copy style data from default cell style
		dateStyle.cloneStyleFrom(getCellStyle());
		// and add date formating
		dateStyle.setDataFormat(helper.createDataFormat().getFormat(dateFormat));
	}

	public String getDateFormat() {
		return dateFormat;
	}

	private CellStyle getDateCellStyle() {
		return dateStyle;
	}

	private HSSFRichTextString createTextValue(String value) {
		return new HSSFRichTextString(value);
	}

	/**
	 * Create new Cell on specified row and colIdx with value
	 */
	public HSSFCell createCell(HSSFRow row, int colIdx, Object value) {
		HSSFCell cell = row.createCell(colIdx);
		cell.setCellStyle(getCellStyle());
		if (value == null) {
			return cell;
		} else if (value instanceof Number) {
			cell.setCellValue(((Number) value).doubleValue());
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else if (value instanceof Date) {
			cell.setCellValue((Date) value);
			cell.setCellStyle(getDateCellStyle());
		} else if (value instanceof String) {
			cell.setCellValue(createTextValue((String) value));
		} else {
			cell.setCellValue(createTextValue(value.toString()));
		}

		return cell;
	}

	/**
	 * Append new Cell to current Row
	 */
	public HSSFCell addCell(Object value) {
		return addCell(getCurrentRow(), value);
	}

	/**
	 * Append new Cell to specified Row
	 */
	public HSSFCell addCell(HSSFRow row, Object value) {
		short lastCellNum = row.getLastCellNum();
		return createCell(row, lastCellNum < 0 ? 0 : lastCellNum, value);
	}

	/**
	 * Create new Row on specified row number
	 */
	public HSSFRow createRow(int rowNum) {
		return getCurrentSheet().createRow(rowNum);
	}

	/**
	 * Create new Row on specified row number with values
	 */
	public HSSFRow createRow(int rowNum, Object... values) {
		HSSFRow row = getCurrentSheet().createRow(rowNum);
		for (int i = 0; i < values.length; i++) {
			addCell(row, values[i]);
		}
		return row;
	}

	/**
	 * Append new Row after last row of current Sheet
	 */
	public HSSFRow addRow() {
		int lastRowNum = getCurrentSheet().getLastRowNum();
		return createRow(lastRowNum < 0 ? 0 : lastRowNum + 1);
	}

	/**
	 * Append new Row after last row of Sheet with values
	 */
	public HSSFRow addRow(Object... values) {
		HSSFRow row = addRow();
		for (int i = 0; i < values.length; i++) {
			addCell(row, values[i]);
		}
		return row;
	}

	/**
	 * Adjust size of columns that any value can fit info It visits all values in
	 * all rows so it is quite processing heavy...
	 */
	public HSSFRow autosizeColumns() {
		HSSFRow row = getCurrentRow();
		autosizeColumns(row);
		return row;
	}

	public void autosizeColumns(HSSFRow row) {
		for (int i = 0; i < row.getLastCellNum(); i++) {
			getCurrentSheet().autoSizeColumn(i);
		}
	}

	private HSSFSheet getCurrentSheet() {
		return workBook.getSheetAt(workBook.getActiveSheetIndex());
	}

	private HSSFRow getCurrentRow() {
		int lastRowNum = getCurrentSheet().getLastRowNum();
		if (lastRowNum == -1) {
			return null;
		} else {
			return getCurrentSheet().getRow(lastRowNum);
		}
	}

}
