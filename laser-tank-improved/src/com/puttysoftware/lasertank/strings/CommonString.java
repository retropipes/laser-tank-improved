package com.puttysoftware.lasertank.strings;

public enum CommonString {
    LOGO_VERSION_PREFIX("V"), EMPTY(""), SPACE(" "), UNDERSCORE("_"), NOTL_PERIOD("."),
    NOTL_IMAGE_EXTENSION_PNG(".png"), ZERO("0"), BETA_SHORT("b"), COLON(":"), CLOSE_PARENTHESES(")"),
    OPEN_PARENTHESES("("), SPACE_DASH_SPACE(" - ");
    String internalValue;

    CommonString(final String v) {
	this.internalValue = v;
    }

    String getValue() {
	return this.internalValue;
    }
}
