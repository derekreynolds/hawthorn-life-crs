package com.hawthornlife.crs.transformer;

import org.apache.poi.ss.usermodel.Row;

public interface Transformer {

	void transform(Row row) throws Exception;

}