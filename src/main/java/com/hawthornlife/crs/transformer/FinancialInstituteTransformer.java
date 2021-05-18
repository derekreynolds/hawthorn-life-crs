package com.hawthornlife.crs.transformer;

import static com.hawthornlife.crs.util.CommonElementUtil.objectFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.poi.ss.usermodel.Row;

import com.hawthornlife.crs.util.CellUtil;
import com.hawthornlife.crs.xml.AddressFixType;
import com.hawthornlife.crs.xml.AddressType;
import com.hawthornlife.crs.xml.CRSOECD;
import com.hawthornlife.crs.xml.CorrectableOrganisationPartyType;
import com.hawthornlife.crs.xml.CountryCodeType;
import com.hawthornlife.crs.xml.CrsBodyType;
import com.hawthornlife.crs.xml.CrsMessageTypeIndicEnumType;
import com.hawthornlife.crs.xml.DocSpecType;
import com.hawthornlife.crs.xml.MessageSpecType;
import com.hawthornlife.crs.xml.MessageTypeEnumType;
import com.hawthornlife.crs.xml.NameOrganisationType;
import com.hawthornlife.crs.xml.OECDDocTypeIndicEnumType;
import com.hawthornlife.crs.xml.OECDLegalAddressTypeEnumType;
import com.hawthornlife.crs.xml.OECDNameTypeEnumType;
import com.hawthornlife.crs.xml.OrganisationINType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FinancialInstituteTransformer implements Transformer {
    
    private static Logger log = LoggerFactory.getLogger(FinancialInstituteTransformer.class);    
	
    private final CRSOECD crsOecd;

    private final Calendar now = GregorianCalendar.getInstance();


    public FinancialInstituteTransformer(final CRSOECD crsOecd) {
        this.crsOecd = crsOecd;
    }

    @Override
    public void transform(final Row row) throws Exception {
        
        log.info("Entering with {}", row.getRowNum());
        
        this.generateFinancialInstitute(row);
        this.generateMessageSpec(row);
    }

    private void generateMessageSpec(final Row row) throws NumberFormatException, DatatypeConfigurationException {

        log.info("Entering with {}", row.getRowNum());
        
        MessageSpecType messageSpecType = objectFactory.createMessageSpecType();
        crsOecd.setMessageSpec(messageSpecType);

        messageSpecType.setSendingCompanyIN(row.getCell(FinancialInstituteConstant.SENDING_COMPANY_ID_NUMBER).getStringCellValue());
        messageSpecType.setMessageRefId(getMessageRefId(row));
        messageSpecType.setTimestamp(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)this.now));
        messageSpecType.setMessageType(MessageTypeEnumType.CRS);
        messageSpecType.setReceivingCountry(CountryCodeType.valueOf(row.getCell(FinancialInstituteConstant.RECEIVING_COUNTRY).getStringCellValue()));
        messageSpecType.setTransmittingCountry(CountryCodeType.valueOf(row.getCell(FinancialInstituteConstant.TRANSMITTING_COUNTRY).getStringCellValue()));
        messageSpecType.setMessageTypeIndic(CrsMessageTypeIndicEnumType.CRS_701);

        XMLGregorianCalendar reportingPeriod = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(new GregorianCalendar(Integer.valueOf(row.getCell(FinancialInstituteConstant.REPORTING_YEAR).getStringCellValue()), 11, 31));
        messageSpecType.setReportingPeriod(reportingPeriod);

        CellUtil.getValue(row.getCell(FinancialInstituteConstant.CONTACT)).ifPresent(value ->
            messageSpecType.setContact(value)
        );	

    }


    private void generateFinancialInstitute(final Row row) {

        log.info("Entering with {}", row.getRowNum());
        
        CrsBodyType crsBodyType = objectFactory.createCrsBodyType();
        crsOecd.getCrsBody().add(crsBodyType);

        CorrectableOrganisationPartyType reportingFinancialInstituation = objectFactory.createCorrectableOrganisationPartyType();
        crsBodyType.setReportingFI(reportingFinancialInstituation);

        reportingFinancialInstituation.setDocSpec(createDocSpec(row));		

        reportingFinancialInstituation.getResCountryCode().add(CountryCodeType.valueOf(row.getCell(FinancialInstituteConstant.RES_COUNTRY_CODE).getStringCellValue()));

        OrganisationINType organisationIn = objectFactory.createOrganisationINType();

        //organisationIn.setINType(row.getCell(FinancialInstituteConstant.ID_NUMBER_TYPE).getStringCellValue());
        organisationIn.setIssuedBy(CountryCodeType.valueOf(row.getCell(FinancialInstituteConstant.ID_NUMBER_ISSUED_BY).getStringCellValue()));
        organisationIn.setINType("TIN");
        organisationIn.setValue(row.getCell(FinancialInstituteConstant.ID_NUMBER).getStringCellValue());

        reportingFinancialInstituation.getIN().add(organisationIn);

        NameOrganisationType nameOrganisation = objectFactory.createNameOrganisationType();
        nameOrganisation.setNameType(OECDNameTypeEnumType.OECD_207);
        nameOrganisation.setValue(row.getCell(FinancialInstituteConstant.FINANCIAL_INSTITUTION_NAME).getStringCellValue());
        reportingFinancialInstituation.getName().add(nameOrganisation);

        reportingFinancialInstituation.getAddress().add(createAddress(row));


    }

    private DocSpecType createDocSpec(final Row row) {

        log.info("Entering with {}", row.getRowNum());
        
        DocSpecType docSpec = objectFactory.createDocSpecType();

        docSpec.setDocTypeIndic(OECDDocTypeIndicEnumType.OECD_1);
        docSpec.setDocRefId(getDocRefId(row));

        return docSpec;
    }

    private String getMessageRefId(final Row row) {

        log.info("Entering with {}", row.getRowNum());
        
        StringBuilder messageRefId = new StringBuilder(row.getCell(FinancialInstituteConstant.TRANSMITTING_COUNTRY).getStringCellValue());

        messageRefId.append(row.getCell(FinancialInstituteConstant.REPORTING_YEAR).getStringCellValue());	
        messageRefId.append(row.getCell(FinancialInstituteConstant.RECEIVING_COUNTRY).getStringCellValue());
        messageRefId.append(row.getCell(FinancialInstituteConstant.ID_NUMBER).getStringCellValue());
        messageRefId.append(this.now.getTime().getTime());

        return messageRefId.toString();
    }

    private String getDocRefId(final Row row) {

        log.info("Entering with {}", row.getRowNum());
        
        StringBuilder docRefId = new StringBuilder(row.getCell(FinancialInstituteConstant.TRANSMITTING_COUNTRY).getStringCellValue());

        docRefId.append(row.getCell(FinancialInstituteConstant.RECEIVING_COUNTRY).getStringCellValue());
        docRefId.append(this.now.getTime().getTime());

        return docRefId.toString();
    }

    private AddressType createAddress(final Row row) {

        log.info("Entering with {}", row.getRowNum());
        
        AddressType address = objectFactory.createAddressType();
        AddressFixType addressFix = objectFactory.createAddressFixType();
     
        CellUtil.getValue(row.getCell(FinancialInstituteConstant.STREET)).ifPresent(value ->
            addressFix.setStreet(value)
        );
        CellUtil.getValue(row.getCell(FinancialInstituteConstant.BUILDING_IDENTIFIER)).ifPresent(value ->
            addressFix.setBuildingIdentifier(value)
        );
        CellUtil.getValue(row.getCell(FinancialInstituteConstant.SUITE_IDENTIFIER)).ifPresent(value ->
            addressFix.setSuiteIdentifier(value)
        );
        CellUtil.getValue(row.getCell(FinancialInstituteConstant.FLOOR_IDENTIFIER)).ifPresent(value ->
            addressFix.setFloorIdentifier(value)
        );
        CellUtil.getValue(row.getCell(FinancialInstituteConstant.DISTRICT_NAME)).ifPresent(value ->
            addressFix.setDistrictName(value)
        );
        CellUtil.getValue(row.getCell(FinancialInstituteConstant.POB)).ifPresent(value ->
            addressFix.setPOB(value)
        );
        CellUtil.getValue(row.getCell(FinancialInstituteConstant.POST_CODE)).ifPresent(value ->
            addressFix.setPostCode(value)
        );
        CellUtil.getValue(row.getCell(FinancialInstituteConstant.CITY)).ifPresent(value ->
            addressFix.setCity(value)
        );

        CellUtil.getValue(row.getCell(FinancialInstituteConstant.ADDRESS_TYPE)).ifPresent(value ->
            address.setLegalAddressType(OECDLegalAddressTypeEnumType.valueOf(CellUtil.convertOecdType(value)))
        );
        CellUtil.getValue(row.getCell(FinancialInstituteConstant.COUNTRY_CODE)).ifPresent(value ->
            address.getContent().add(objectFactory.createAddressTypeCountryCode(CountryCodeType.valueOf(value)))
        );
        
        address.getContent().add(objectFactory.createAddressTypeAddressFix(addressFix));
                
        return address;
    }

}
