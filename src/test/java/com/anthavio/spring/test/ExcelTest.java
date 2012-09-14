package com.anthavio.spring.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.testng.annotations.Test;

import com.anthavio.poi.ExcelBuilder;


/**
 * @author vanek
 *
 */
public class ExcelTest {

	@Test
	public void test() throws FileNotFoundException {
		ExcelBuilder builder = new ExcelBuilder("Uživatelé");

		builder.createHeader(0, "Jméno", "Příjmení", "Datum", "Číslo", "Příznak");
		builder.setDateFormat("d.m.yyyy h:mm:ss");
		builder.addRow("Axlomatickojko", "Rosenkranc", new Date(), 98765432123456789l, true);
		builder.setDateFormat("d.m.yyyy");
		builder.addRow("Alex", "Rozdopoložovanitičesky", new Date(), 123456789, false);
		builder.addRow();
		builder.addCell("Saša");
		builder.addCell("Maša");
		builder.addCell(new Date());
		builder.addCell(666);
		builder.addCell(true);

		builder.autosizeColumns();
		builder.write(new File("target", "!xxx1.xls"));
	}

	@Test
	public void testSimplest() throws FileNotFoundException {
		ExcelBuilder builder = new ExcelBuilder();
		builder.addRow("Vole", "Hele", 1.1, new Date());
		builder.addRow("Žožo", "Čůčů", 2, new Date());

		builder.createHeader(5, "xxx", "yyy", "zzz");
		builder.addRow("Vole", "Hele", 1.1, new Date());

		builder.setDateFormat("d.m.yyyy h:mm:ss");

		builder.addRow("Žožo", "Čůčů", 2, new Date());

		builder.autosizeColumns();
		builder.write(new File("target", "!xxx2.xls"));
	}

	@Test
	public void testTemplate() throws IOException {
		InputStream stream = getClass().getClassLoader().getResourceAsStream("template.xls");
		ExcelBuilder builder = new ExcelBuilder(stream);
		builder.createRow(3, "Žožo", "Čůčů", 2, new Date());
		builder.createRow(3, "Žožox", "Čůčůx", 3, new Date());
		builder.addRow("Žožo", "Čůčů", 2, new Date());
		builder.write(new File("target", "!xxx3.xls"));
	}
}
