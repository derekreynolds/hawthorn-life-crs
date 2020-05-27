package com.hawthornlife.crs.transformer;

import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;

import com.hawthornlife.crs.util.CellUtil;
import static com.hawthornlife.crs.util.CommonElementUtil.*;
import com.hawthornlife.crs.xml.AccountHolderType;
import com.hawthornlife.crs.xml.AcctNumberTypeEnumType;
import com.hawthornlife.crs.xml.AddressFixType;
import com.hawthornlife.crs.xml.AddressType;
import com.hawthornlife.crs.xml.CRSOECD;
import com.hawthornlife.crs.xml.CorrectableAccountReportType;
import com.hawthornlife.crs.xml.CountryCodeType;
import com.hawthornlife.crs.xml.CrsAcctHolderTypeEnumType;
import com.hawthornlife.crs.xml.CrsBodyType;
import com.hawthornlife.crs.xml.CrsPaymentTypeEnumType;
import com.hawthornlife.crs.xml.CurrCodeType;
import com.hawthornlife.crs.xml.DocSpecType;
import com.hawthornlife.crs.xml.FIAccountNumberType;
import com.hawthornlife.crs.xml.MonAmntType;
import com.hawthornlife.crs.xml.NameOrganisationType;
import com.hawthornlife.crs.xml.NamePersonType;
import com.hawthornlife.crs.xml.OECDDocTypeIndicEnumType;
import com.hawthornlife.crs.xml.OECDLegalAddressTypeEnumType;
import com.hawthornlife.crs.xml.OECDNameTypeEnumType;
import com.hawthornlife.crs.xml.OrganisationINType;
import com.hawthornlife.crs.xml.OrganisationPartyType;
import com.hawthornlife.crs.xml.PaymentType;
import com.hawthornlife.crs.xml.PersonPartyType;
import com.hawthornlife.crs.xml.PersonPartyType.BirthInfo;
import com.hawthornlife.crs.xml.PersonPartyType.BirthInfo.CountryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AccountHolderTransformer implements Transformer {
	
    private static Logger log = LoggerFactory.getLogger(AccountHolderTransformer.class);
    
    private final CRSOECD crsOecd;

    private final ControllingPersonTransformer controllingPersonTransformer;


    public AccountHolderTransformer(final ControllingPersonTransformer controllingPersonTransformer, final CRSOECD crsOecd) {
        this.controllingPersonTransformer = controllingPersonTransformer;
        this.crsOecd = crsOecd;
    }

    @Override
    public void transform(final Row row) throws Exception {
        
        log.info("Entering with {}", row.getRowNum());

        CrsBodyType crsBody = crsOecd.getCrsBody().get(0);

        CrsBodyType.ReportingGroup reportingGroup;

        if(crsBody.getReportingGroup().isEmpty()) {
                reportingGroup = objectFactory.createCrsBodyTypeReportingGroup();
                crsBody.getReportingGroup().add(reportingGroup);
        } else {
                reportingGroup = crsBody.getReportingGroup().get(0);
        }

        CorrectableAccountReportType correctableAccountReport = objectFactory.createCorrectableAccountReportType();

        reportingGroup.getAccountReport().add(correctableAccountReport);

        correctableAccountReport.setDocSpec(createDocSpec(row));
        correctableAccountReport.setAccountNumber(createAccountNumber(row));		

        correctableAccountReport.setAccountHolder(createAccountHolder(row));

        correctableAccountReport.setAccountBalance(createAccountBalance(row));		

        CellUtil.getValue(row.getCell(AccountHolderConstants.DIVIDENDS_AMOUNT)).ifPresent(amount -> 
                correctableAccountReport.getPayment().add(createPayment(CrsPaymentTypeEnumType.CRS_501, amount, row.getCell(AccountHolderConstants.DIVIDENDS_CURRENCY).getStringCellValue()))
        );

        CellUtil.getValue(row.getCell(AccountHolderConstants.INTEREST_AMOUNT)).ifPresent(amount -> 
                correctableAccountReport.getPayment().add(createPayment(CrsPaymentTypeEnumType.CRS_502, amount, row.getCell(AccountHolderConstants.INTEREST_CURRENCY).getStringCellValue()))
        );

        CellUtil.getValue(row.getCell(AccountHolderConstants.GROSS_PROCEEDS_REDEMPTIONS_AMOUNT)).ifPresent(amount -> 
                correctableAccountReport.getPayment().add(createPayment(CrsPaymentTypeEnumType.CRS_503, amount, row.getCell(AccountHolderConstants.GROSS_PROCEEDS_REDEMPTIONS_CURRENCY).getStringCellValue()))
        );

        CellUtil.getValue(row.getCell(AccountHolderConstants.OTHER_AMOUNT)).ifPresent(amount -> 
                correctableAccountReport.getPayment().add(createPayment(CrsPaymentTypeEnumType.CRS_504, amount, row.getCell(AccountHolderConstants.OTHER_CURRENCY).getStringCellValue()))
        );

        if(row.getCell(AccountHolderConstants.CATEGORY_OF_ACCOUNT_HOLDER).getStringCellValue().equalsIgnoreCase("Organisation") ) {
                this.controllingPersonTransformer.init(correctableAccountReport);
                this.controllingPersonTransformer.transform(row);
        }

    }

    private DocSpecType createDocSpec(final Row row) {
        
        log.info("Entering with {}", row.getRowNum());

        DocSpecType docSpec = objectFactory.createDocSpecType();

        docSpec.setDocTypeIndic(OECDDocTypeIndicEnumType.OECD_1);

        docSpec.setDocRefId(row.getCell(AccountHolderConstants.ACCOUNT_NAME).getStringCellValue() + "-" + row.getRowNum());

        return docSpec;
    }

    private FIAccountNumberType createAccountNumber(final Row row) {
        
        log.info("Entering with {}", row.getRowNum());

        FIAccountNumberType accountNumber = objectFactory.createFIAccountNumberType();

        accountNumber.setAcctNumberType(AcctNumberTypeEnumType.OECD_605);
        accountNumber.setValue(row.getCell(AccountHolderConstants.ACCOUNT_NAME).getStringCellValue());

        accountNumber.setUndocumentedAccount(toBoolean(row.getCell(AccountHolderConstants.UNDOCUMENTED_ACCOUNT).getStringCellValue()));
        accountNumber.setClosedAccount(toBoolean(row.getCell(AccountHolderConstants.CLOSED_ACCOUNT).getStringCellValue()));
        accountNumber.setDormantAccount(toBoolean(row.getCell(AccountHolderConstants.DORMANT_ACCOUNT).getStringCellValue()));		

        return accountNumber;
    }

    private AccountHolderType createAccountHolder(final Row row) throws Exception {
        
        log.info("Entering with {}", row.getRowNum());

        AccountHolderType accountHolder = objectFactory.createAccountHolderType();

        if(row.getCell(AccountHolderConstants.CATEGORY_OF_ACCOUNT_HOLDER).getStringCellValue().equalsIgnoreCase("Individual")) {			
                accountHolder.setIndividual(createIndividual(row));
        } else {
                accountHolder.setAcctHolderType(CrsAcctHolderTypeEnumType.CRS_101);
                accountHolder.setOrganisation(createOrganisation(row));			
        }


        return accountHolder;
    }

    private PersonPartyType createIndividual(final Row row) throws Exception {
        
        log.info("Entering with {}", row.getRowNum());

        PersonPartyType personParty = objectFactory.createPersonPartyType();

        personParty.getName().add(createName(row));

        addResCountryCode(personParty.getResCountryCode(), row);

        CellUtil.getValue(row.getCell(AccountHolderConstants.TIN)).ifPresent(tin ->
                personParty.getTIN().add(createTin(tin, row.getCell(AccountHolderConstants.TIN_ISSUED_BY).getStringCellValue()))
        );
        CellUtil.getValue(row.getCell(AccountHolderConstants.TIN2)).ifPresent(tin ->
                personParty.getTIN().add(createTin(tin, row.getCell(AccountHolderConstants.TIN_ISSUED_BY2).getStringCellValue()))
        );
        CellUtil.getValue(row.getCell(AccountHolderConstants.TIN3)).ifPresent(tin ->
                personParty.getTIN().add(createTin(tin, row.getCell(AccountHolderConstants.TIN_ISSUED_BY3).getStringCellValue()))
        );
        CellUtil.getValue(row.getCell(AccountHolderConstants.TIN4)).ifPresent(tin ->
                personParty.getTIN().add(createTin(tin, row.getCell(AccountHolderConstants.TIN_ISSUED_BY4).getStringCellValue()))
        );

        personParty.getAddress().add(createAddress(row));
        personParty.setBirthInfo(createBirthInfo(row));

        return personParty;
    }

    private void addResCountryCode(final List<CountryCodeType> countryCodes, final Row row) {
        
        log.info("Entering with {}", row.getRowNum());
        
        CellUtil.getValue(row.getCell(AccountHolderConstants.RES_COUNTRY_CODE)).ifPresent(value -> 
                countryCodes.add(CountryCodeType.valueOf(value))
        );

        CellUtil.getValue(row.getCell(AccountHolderConstants.RES_COUNTRY_CODE2)).ifPresent(value -> 
                countryCodes.add(CountryCodeType.valueOf(value))
        );

        CellUtil.getValue(row.getCell(AccountHolderConstants.RES_COUNTRY_CODE3)).ifPresent(value -> 
                countryCodes.add(CountryCodeType.valueOf(value))
        );

        CellUtil.getValue(row.getCell(AccountHolderConstants.RES_COUNTRY_CODE4)).ifPresent(value -> 
                countryCodes.add(CountryCodeType.valueOf(value))
        );
    }

    private NamePersonType createName(final Row row) {
        
        log.info("Entering with {}", row.getRowNum());

        NamePersonType namePerson = objectFactory.createNamePersonType();

        namePerson.setFirstName(createFirstName(row));
        namePerson.setLastName(createLastName(row));

        CellUtil.getValue(row.getCell(AccountHolderConstants.NAME_PERSON_TYPE)).ifPresent(name ->
                namePerson.setNameType(OECDNameTypeEnumType.valueOf(name))
        );

        return namePerson;
    }


    private NamePersonType.FirstName createFirstName(final Row row) {
        
        log.info("Entering with {}", row.getRowNum());

        NamePersonType.FirstName firstName = objectFactory.createNamePersonTypeFirstName();

        firstName.setValue(row.getCell(AccountHolderConstants.FIRST_NAME).getStringCellValue());

        return firstName;
    }

    private NamePersonType.LastName createLastName(final Row row) {
        
        log.info("Entering with {}", row.getRowNum());

        NamePersonType.LastName lastName = objectFactory.createNamePersonTypeLastName();

        lastName.setValue(row.getCell(AccountHolderConstants.LAST_NAME).getStringCellValue());

        return lastName;
    }


    private OrganisationPartyType createOrganisation(final Row row) {
        
        log.info("Entering with {}", row.getRowNum());

        OrganisationPartyType organisationParty = objectFactory.createOrganisationPartyType();		

        organisationParty.getName().add(createNameOrganisation(row));
        addResCountryCode(organisationParty.getResCountryCode(), row);

        if(!StringUtils.isBlank(row.getCell(AccountHolderConstants.TIN).getStringCellValue())) {
                organisationParty.getIN().add(createOrganisationINType(row));
        }
        organisationParty.getAddress().add(createAddress(row));

        return organisationParty;
    }

    private OrganisationINType createOrganisationINType(final Row row) {
        
        log.info("Entering with {}", row.getRowNum());

        OrganisationINType organisationINType = objectFactory.createOrganisationINType();		

        organisationINType.setValue(row.getCell(AccountHolderConstants.TIN).getStringCellValue());
        organisationINType.setIssuedBy(CountryCodeType.valueOf(row.getCell(AccountHolderConstants.TIN_ISSUED_BY).getStringCellValue()));		
        organisationINType.setINType(row.getCell(AccountHolderConstants.TIN_TYPE).getStringCellValue());

        return organisationINType;

    }

    private NameOrganisationType createNameOrganisation(final Row row) {
        
        log.info("Entering with {}", row.getRowNum());

        NameOrganisationType nameOrganisation = objectFactory.createNameOrganisationType();

        nameOrganisation.setNameType(OECDNameTypeEnumType.OECD_207);
        nameOrganisation.setValue(row.getCell(AccountHolderConstants.ENTITY_NAME).getStringCellValue());

        return nameOrganisation;

    }


    private AddressType createAddress(final Row row) {
        
        log.info("Entering with {}", row.getRowNum());

        AddressType address = objectFactory.createAddressType();
        AddressFixType addressFix = objectFactory.createAddressFixType();

        addressFix.setStreet(row.getCell(AccountHolderConstants.STREET).getStringCellValue());
        addressFix.setBuildingIdentifier(row.getCell(AccountHolderConstants.BUILDING_IDENTIFIER).getStringCellValue());
        addressFix.setSuiteIdentifier(row.getCell(AccountHolderConstants.SUITE_IDENTIFIER).getStringCellValue());
        addressFix.setFloorIdentifier(row.getCell(AccountHolderConstants.FLOOR_IDENTIFIER).getStringCellValue());
        addressFix.setDistrictName(row.getCell(AccountHolderConstants.DISTRICT_NAME).getStringCellValue());
        addressFix.setPOB(row.getCell(AccountHolderConstants.POB).getStringCellValue());
        addressFix.setPostCode(row.getCell(AccountHolderConstants.POST_CODE).getStringCellValue());
        addressFix.setCity(row.getCell(AccountHolderConstants.CITY).getStringCellValue());			

        address.setLegalAddressType(OECDLegalAddressTypeEnumType.valueOf(CellUtil.convertOecdType(row.getCell(AccountHolderConstants.ADDRESS_TYPE).getStringCellValue())));
        address.getContent().add(objectFactory.createAddressTypeCountryCode(CountryCodeType.valueOf(row.getCell(AccountHolderConstants.COUNTRY_CODE).getStringCellValue())));
        address.getContent().add(objectFactory.createAddressTypeAddressFix(addressFix));

        return address;
    }

    private BirthInfo createBirthInfo(final Row row) throws Exception {
        
        log.info("Entering with {}", row.getRowNum());

        BirthInfo birth = objectFactory.createPersonPartyTypeBirthInfo();

        String date = row.getCell(AccountHolderConstants.BIRTH_DATE).getStringCellValue();

        XMLGregorianCalendar birthDate = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(new GregorianCalendar(Integer.valueOf(date.substring(0, 4)), 
                                                Integer.valueOf(date.substring(5, 7)) - 1, 
                                                Integer.valueOf(date.substring(8, 10))));
        birth.setBirthDate(birthDate);
        birth.setCity(row.getCell(AccountHolderConstants.BIRTH_CITY).getStringCellValue());

        CellUtil.getValue(row.getCell(AccountHolderConstants.BIRTH_COUNTRY_CODE)).ifPresent(value -> {
                CountryInfo countryInfo = objectFactory.createPersonPartyTypeBirthInfoCountryInfo();
                countryInfo.setCountryCode(CountryCodeType.valueOf(value));
                birth.setCountryInfo(countryInfo);
        });		

        return birth;
    }

    private MonAmntType createAccountBalance(final Row row) {
        
        log.info("Entering with {}", row.getRowNum());

        MonAmntType monAmnt = objectFactory.createMonAmntType();

        monAmnt.setValue(new BigDecimal(row.getCell(AccountHolderConstants.ACCOUNT_BALANCE).getStringCellValue()));
        monAmnt.setCurrCode(CurrCodeType.valueOf(row.getCell(AccountHolderConstants.ACCOUNT_CURRENCY).getStringCellValue()));

        return monAmnt;
    }

    private PaymentType createPayment(final CrsPaymentTypeEnumType crsPaymentType, final String amount, final String currency) {

        log.info("Entering with {}, {}, {}", crsPaymentType, amount, currency);
        
        MonAmntType monAmnt = objectFactory.createMonAmntType();

        monAmnt.setValue(new BigDecimal(amount));
        monAmnt.setCurrCode(CurrCodeType.valueOf(currency));

        PaymentType payment = objectFactory.createPaymentType();

        payment.setPaymentAmnt(monAmnt);
        payment.setType(crsPaymentType);

        return payment;
    }


    private boolean toBoolean(final String value) {
        
        boolean result = false;

        if(value.equalsIgnoreCase("yes"))
                result = true;

        return result;
    }

}
