package com.svg.base;

/**
 * Thrown by the parser if a problem is found in the SVG file.
 * 
 * @author Mohammed Rampurawala
 */

public class SVGParseException extends Exception {
	public SVGParseException(String msg) {
		super(msg);
	}

	public SVGParseException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public SVGParseException(Throwable cause) {
		super(cause);
	}
}
