package com.tlswe.awsmock.ec2.model;

public class InstanceEvent {

	private final String code;

	private final String description;

	private final String notAfter;

	private final String notBefore;

	public InstanceEvent(String code, String description, String notAfter, String notBefore) {

		this.code = code;
		this.description = description;
		this.notAfter = notAfter;
		this.notBefore = notBefore;
	}

	public String getCode() {

		return code;
	}

	public String getDescription() {

		return description;
	}

	public String getNotAfter() {

		return notAfter;
	}

	public String getNotBefore() {

		return notBefore;
	}
}
