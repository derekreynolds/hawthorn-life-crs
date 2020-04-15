package com.hawthornlife.crs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.hawthornlife.crs.transformer.ControllingPersonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SpreadsheetReader {
    
    private static Logger log = LoggerFactory.getLogger(SpreadsheetReader.class);
    
    private static int STARTING_ROW = 8;
	
    private Workbook workbook;

    private Sheet financialInstituteSheet;

    private Sheet accountHolderSheet;

    private Sheet controllingPersonsSheet; 


    public SpreadsheetReader(final String fileName) throws EncryptedDocumentException, IOException {		
        workbook = WorkbookFactory.create(new File(fileName));

        financialInstituteSheet = workbook.getSheet("FI");
        accountHolderSheet = workbook.getSheet("Account Holder");
        controllingPersonsSheet = workbook.getSheet("Controlling Persons");

    }

    public Row getFinancialInstituteRow() {
            return financialInstituteSheet.getRow(STARTING_ROW);		
    }

    public Row getAccountHolderRow(int index) {
        return accountHolderSheet.getRow(index + STARTING_ROW);
    }

    public List<Row> getControllingPersonRows(final String accountNumber) {
        
        log.info("Entering with {}", accountNumber);

        List<Row> rows = new ArrayList<>();

        for(int i = 0;; i++) {
            
            log.info("Controlling person row {}", i);
            
            Row row = controllingPersonsSheet.getRow(STARTING_ROW + i);

            if((row == null) || StringUtils.isBlank(row.getCell(ControllingPersonConstant.ACCOUNT_NAME).getStringCellValue()))
                break;

            if(accountNumber.equalsIgnoreCase(row.getCell(ControllingPersonConstant.ACCOUNT_NAME).getStringCellValue())) {
                log.info("Controlling person row {} added", i);
                rows.add(row);
            }
        }

        return rows;

    }

	
}
