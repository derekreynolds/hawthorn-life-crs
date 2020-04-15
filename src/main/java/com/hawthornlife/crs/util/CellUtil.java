package com.hawthornlife.crs.util;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;

public class CellUtil {

	
	public static Optional<String> getValue(final Cell cell) {
		
		String value = cell.getStringCellValue();
		
		if(StringUtils.isBlank(value))
			value = null;
			
		return Optional.ofNullable(value);	
	}
	
	public static String convertOecdType(final String type) {
		
		String seperator = "_";
		
		if(type.indexOf(seperator) == 3)
			return type;
		
		StringBuilder convertedType = new StringBuilder(type.substring(0, 4));
		convertedType.append(seperator);
		convertedType.append(type.substring(4, type.length()));
		
		return convertedType.toString();
	}
	
	public static String convertCrsType(final String type) {
		
		String seperator = "_";
		
		if(type.indexOf(seperator) == 2)
			return type;
		
		StringBuilder convertedType = new StringBuilder(type.substring(0, 3));
		convertedType.append(seperator);
		convertedType.append(type.substring(3, type.length()));
		
		return convertedType.toString();
	}
	
}
