package com.hawthornlife.crs.transformer;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.poi.ss.usermodel.Row;

import static com.hawthornlife.crs.util.CommonElementUtil.*;
import com.hawthornlife.crs.SpreadsheetReader;
import com.hawthornlife.crs.util.CellUtil;
import com.hawthornlife.crs.xml.AddressFixType;
import com.hawthornlife.crs.xml.AddressType;
import com.hawthornlife.crs.xml.ControllingPersonType;
import com.hawthornlife.crs.xml.CorrectableAccountReportType;
import com.hawthornlife.crs.xml.CountryCodeType;
import com.hawthornlife.crs.xml.CrsCtrlgPersonTypeEnumType;
import com.hawthornlife.crs.xml.NamePersonType;
import com.hawthornlife.crs.xml.OECDLegalAddressTypeEnumType;
import com.hawthornlife.crs.xml.NamePersonType.FirstName;
import com.hawthornlife.crs.xml.NamePersonType.LastName;
import com.hawthornlife.crs.xml.PersonPartyType.BirthInfo;
import com.hawthornlife.crs.xml.PersonPartyType.BirthInfo.CountryInfo;
import com.hawthornlife.crs.xml.OECDNameTypeEnumType;
import com.hawthornlife.crs.xml.PersonPartyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllingPersonTransformer implements Transformer {
    
    private static Logger log = LoggerFactory.getLogger(ControllingPersonTransformer.class);
	
    private final SpreadsheetReader spreadsheetReader;

    private CorrectableAccountReportType correctableAccountReport;


    public ControllingPersonTransformer(final SpreadsheetReader spreadsheetReader) {
        this.spreadsheetReader = spreadsheetReader;		
    }

    public void init(final CorrectableAccountReportType correctableAccountReport) {
        this.correctableAccountReport = correctableAccountReport;
    }

    @Override
    public void transform(Row row) throws Exception {
        
        log.info("Entering with {}", row.getRowNum());

        String accountNumber = row.getCell(AccountHolderConstants.ACCOUNT_NAME).getStringCellValue();

        correctableAccountReport.getControllingPerson().addAll(createControllingPersons(accountNumber));

    }

    private List<ControllingPersonType> createControllingPersons(final String accountNumber) throws Exception {
        
        log.info("Entering with {}", accountNumber);

        List<ControllingPersonType> controllingPersons = new ArrayList<>();

        List<Row> rows = this.spreadsheetReader.getControllingPersonRows(accountNumber);

        for (Row row : rows) {
                controllingPersons.add(createControllingPerson(row));
        }		

        return controllingPersons;

    }

    private ControllingPersonType createControllingPerson(final Row row) throws Exception {
        
        log.info("Entering with {}", row.getRowNum());

        ControllingPersonType controllingPerson = objectFactory.createControllingPersonType();

        controllingPerson.setCtrlgPersonType(
                        CrsCtrlgPersonTypeEnumType.valueOf(CellUtil.convertCrsType(row.getCell(
                                        ControllingPersonConstant.CONTROLLING_PERSON_TYPE).getStringCellValue())));

        controllingPerson.setIndividual(createPersonParty(row));

        return controllingPerson;
    }

    private PersonPartyType createPersonParty(final Row row) throws Exception {
        
        log.info("Entering with {}", row.getRowNum());

        PersonPartyType personParty = objectFactory.createPersonPartyType();

        personParty.getName().add(createNamePerson(row));
        addResCountryCode(personParty.getResCountryCode(), row);

        CellUtil.getValue(row.getCell(ControllingPersonConstant.TIN)).ifPresent(tin ->
                personParty.getTIN().add(createTin(tin, row.getCell(ControllingPersonConstant.TIN_ISSUED_BY).getStringCellValue()))
        );
        CellUtil.getValue(row.getCell(ControllingPersonConstant.TIN2)).ifPresent(tin ->
                personParty.getTIN().add(createTin(tin, row.getCell(ControllingPersonConstant.TIN_ISSUED_BY2).getStringCellValue()))
        );
        CellUtil.getValue(row.getCell(ControllingPersonConstant.TIN3)).ifPresent(tin ->
                personParty.getTIN().add(createTin(tin, row.getCell(ControllingPersonConstant.TIN_ISSUED_BY3).getStringCellValue()))
        );
        CellUtil.getValue(row.getCell(ControllingPersonConstant.TIN4)).ifPresent(tin ->
                personParty.getTIN().add(createTin(tin, row.getCell(ControllingPersonConstant.TIN_ISSUED_BY4).getStringCellValue()))
        );

        personParty.getAddress().add(createAddress(row));

        personParty.setBirthInfo(createBirthInfo(row));

            return personParty;

    }

    private NamePersonType createNamePerson(final Row row) {
        
        log.info("Entering with {}", row.getRowNum());

        NamePersonType namePerson = objectFactory.createNamePersonType();
        FirstName firstName = objectFactory.createNamePersonTypeFirstName();
        LastName lastName = objectFactory.createNamePersonTypeLastName();

        firstName.setValue(row.getCell(ControllingPersonConstant.FIRST_NAME).getStringCellValue());
        lastName.setValue(row.getCell(ControllingPersonConstant.LAST_NAME).getStringCellValue());

        CellUtil.getValue(row.getCell(ControllingPersonConstant.NAME_PERSON_TYPE)).ifPresent(value -> {
                namePerson.setNameType(OECDNameTypeEnumType.valueOf(value));
        });

        namePerson.setFirstName(firstName);
        namePerson.setLastName(lastName);

        return namePerson;
    }

    private void addResCountryCode(final List<CountryCodeType> countryCodes, final Row row) {
        
        log.info("Entering with {}", row.getRowNum());

        CellUtil.getValue(row.getCell(ControllingPersonConstant.RES_COUNTRY_CODE)).ifPresent(value -> 
                countryCodes.add(CountryCodeType.valueOf(value))
        );

        CellUtil.getValue(row.getCell(ControllingPersonConstant.RES_COUNTRY_CODE2)).ifPresent(value -> 
                countryCodes.add(CountryCodeType.valueOf(value))
        );

        CellUtil.getValue(row.getCell(ControllingPersonConstant.RES_COUNTRY_CODE3)).ifPresent(value -> 
                countryCodes.add(CountryCodeType.valueOf(value))
        );

        CellUtil.getValue(row.getCell(ControllingPersonConstant.RES_COUNTRY_CODE4)).ifPresent(value -> 
                countryCodes.add(CountryCodeType.valueOf(value))
        );
    }

    private AddressType createAddress(final Row row) {
        
        log.info("Entering with {}", row.getRowNum());

        AddressType address = objectFactory.createAddressType();
        AddressFixType addressFix = objectFactory.createAddressFixType();

        addressFix.setStreet(row.getCell(ControllingPersonConstant.STREET).getStringCellValue());
        addressFix.setBuildingIdentifier(row.getCell(ControllingPersonConstant.BUILDING_IDENTIFIER).getStringCellValue());
        addressFix.setSuiteIdentifier(row.getCell(ControllingPersonConstant.SUITE_IDENTIFIER).getStringCellValue());
        addressFix.setFloorIdentifier(row.getCell(ControllingPersonConstant.FLOOR_IDENTIFIER).getStringCellValue());
        addressFix.setDistrictName(row.getCell(ControllingPersonConstant.DISTRICT_NAME).getStringCellValue());
        addressFix.setPOB(row.getCell(ControllingPersonConstant.POB).getStringCellValue());
        addressFix.setPostCode(row.getCell(ControllingPersonConstant.POST_CODE).getStringCellValue());
        addressFix.setCity(row.getCell(ControllingPersonConstant.CITY).getStringCellValue());

        address.setLegalAddressType(OECDLegalAddressTypeEnumType.valueOf(CellUtil.convertOecdType(row.getCell(ControllingPersonConstant.ADDRESS_TYPE).getStringCellValue())));
        address.getContent().add(objectFactory.createAddressTypeCountryCode(CountryCodeType.valueOf(row.getCell(ControllingPersonConstant.COUNTRY_CODE).getStringCellValue())));
        address.getContent().add(objectFactory.createAddressTypeAddressFix(addressFix));

        return address;
    }

    private BirthInfo createBirthInfo(final Row row) throws Exception {
        
        log.info("Entering with {}", row.getRowNum());

        BirthInfo birth = objectFactory.createPersonPartyTypeBirthInfo();

        String date = row.getCell(ControllingPersonConstant.BIRTH_DATE).getStringCellValue();

        XMLGregorianCalendar birthDate = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(new GregorianCalendar(Integer.valueOf(date.substring(0, 4)), 
                                                Integer.valueOf(date.substring(5, 7)) - 1, 
                                                Integer.valueOf(date.substring(8, 10))));
        birth.setBirthDate(birthDate);
        birth.setCity(row.getCell(ControllingPersonConstant.BIRTH_CITY).getStringCellValue());

        CellUtil.getValue(row.getCell(ControllingPersonConstant.BIRTH_COUNTRY_CODE)).ifPresent(value -> {
                CountryInfo countryInfo = objectFactory.createPersonPartyTypeBirthInfoCountryInfo();
                countryInfo.setCountryCode(CountryCodeType.valueOf(value));
                birth.setCountryInfo(countryInfo);
        });		

        return birth;
    }
	
}
