package com.tegra.spec.service;

import com.tegra.spec.model.Spec;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {

    public Spec parseExcel(InputStream excelStream) throws Exception {
        try (Workbook wb = new XSSFWorkbook(excelStream)) {

            Sheet sheet = wb.getSheetAt(0);

            Spec spec = new Spec();

            spec.customer = getString(sheet, 2, 0);
            spec.style = getString(sheet, 3, 0);
            spec.season = getString(sheet, 4, 0);
            spec.pattern = getString(sheet, 5, 0);
            spec.po = getString(sheet, 6, 0);
            spec.sample_type = getString(sheet, 7, 0);
            spec.date = getString(sheet, 8, 0);
            spec.requested_by = getString(sheet, 9, 0);

            spec.colorway_number = getString(sheet, 11, 0);
            spec.colorway_name = getString(sheet, 11, 2);

            spec.dimensions = getString(sheet, 12, 0);

            List<String> inks = new ArrayList<>();
            int start = 19;
            for (int i = 0; i < 20; i++) {
                String v = getString(sheet, start + i, 0);
                if (v == null || v.isBlank()) break;
                inks.add(v);
            }

            spec.ink_sequence = inks;

            spec.ink_type = "PLASTISOL";
            spec.temp = "320°F";
            spec.time = "45 sec";

            return spec;
        }
    }

    private String getString(Sheet sheet, int r, int c) {
        Row row = sheet.getRow(r);
        if (row == null) return "";
        Cell cell = row.getCell(c);
        if (cell == null) return "";
        return cell.toString();
    }
}
