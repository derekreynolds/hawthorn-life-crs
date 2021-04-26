package com.hawthornlife.crs;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;

import static com.hawthornlife.crs.util.CommonElementUtil.*;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.hawthornlife.crs.transformer.AccountHolderConstants;
import com.hawthornlife.crs.transformer.AccountHolderTransformer;
import com.hawthornlife.crs.transformer.ControllingPersonTransformer;
import com.hawthornlife.crs.transformer.FinancialInstituteTransformer;
import com.hawthornlife.crs.transformer.Transformer;
import com.hawthornlife.crs.xml.CRSOECD;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.SneakyThrows;

public class XmlGenerator {
    
        private static Logger log = LoggerFactory.getLogger(XmlGenerator.class);
	
	private static final String CRS_VERSION = "1.0";
		
	private final CRSOECD crsOecd = objectFactory.createCRSOECD();
	
	private final Transformer financialInstituteTransformer;
	
	private final Transformer accountHolderTransformer;
	
	private final SpreadsheetReader spreadsheetReader;
        
        private final String file;
	
	
	public XmlGenerator(final String file) throws Exception {		
            crsOecd.setVersion(CRS_VERSION);
            this.file = file;
            this.spreadsheetReader = new SpreadsheetReader(file);
            this.financialInstituteTransformer = new FinancialInstituteTransformer(crsOecd);
            this.accountHolderTransformer = new AccountHolderTransformer(spreadsheetReader, new ControllingPersonTransformer(spreadsheetReader), crsOecd);
	}
	
	
	public void generate() throws Exception {
		
            log.info("Entering");
            
            this.financialInstituteTransformer.transform(spreadsheetReader.getFinancialInstituteRow());

            transformAccountHolder();
		
            writeFile();
		
	}
        
        @SneakyThrows
        private void writeFile() {
            
            log.info("Entering");
            
            String outputFile = this.file.substring(0, this.file.lastIndexOf(".")) + ".xml";
            
            log.info("Output file: " + outputFile);
            
            JAXBContext jaxbContext = JAXBContext.newInstance(CRSOECD.class);

            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            File file = new File(outputFile);

            jaxbMarshaller.marshal(crsOecd, file); 
            
        }
	
	
	private void transformAccountHolder() throws Exception {		
		
            for(int i = 0;; i++) {

                log.info("Processing account holder row: " + i);
                
                Row row = this.spreadsheetReader.getAccountHolderRow(i);

                if((row == null) || StringUtils.isBlank(row.getCell(AccountHolderConstants.ACCOUNT_NAME)
                                .getStringCellValue()))
                    break;

                this.accountHolderTransformer.transform(row);
            }
		
	}
	
	
	
	
}
