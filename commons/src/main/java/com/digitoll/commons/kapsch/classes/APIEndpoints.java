package com.digitoll.commons.kapsch.classes;

public enum APIEndpoints {
	
	AUTHENITCATE("auth"),
	VIGNETTE_INVENTORY("evstore/inventory"),
	REGISTER_VIGNETTE("evstore/evignettes"),
    ACTIVATE_VIGNETTE_START("evstore/evignettes"),
    ACTIVATE_VIGNETTE_END("activate"),
	VIGNETTE_INFO_START("evstore/evignettes"),
	VIGNETTE_INFO_END("info"),
	ACTIVATE_VIGNETTE_BATCH("evstore/evignettes/batch_activate"),
    PERIOD_SALES("evstore/evsales"),

	AUTHENITCATE_C9("auth"),
	DAILY_SALES_C9("evstore/aggregatedsales"),
	VIGNETTE_INVENTORY_C9("evstore/inventory"),
	PERIOD_SALES_C9("evstore/evsales"),
	VIGNETTE_STATES_C9("evstore/regdata/states"),
	VIGNETTE_SEARCH_C9("evstore/search");


	private String url;

	APIEndpoints(String url) {
		this.url = url;
	}

	public String getURL() {
		return this.url;
	}
}
