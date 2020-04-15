package com.hawthornlife.crs.util;

import com.hawthornlife.crs.xml.CountryCodeType;
import com.hawthornlife.crs.xml.ObjectFactory;
import com.hawthornlife.crs.xml.TINType;

public class CommonElementUtil {
	
	public static ObjectFactory objectFactory = new ObjectFactory();
	

	public static TINType createTin(final String tin, final String tinIssuedBy) {
		
		TINType tinType = objectFactory.createTINType();
		
		tinType.setValue(tin);
		tinType.setIssuedBy(CountryCodeType.valueOf(tinIssuedBy));
		
		return tinType;
	}
	
}
