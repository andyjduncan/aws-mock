package com.tlswe.awsmock.ec2.model;

public class InstanceTag {

	private final String name;

	private final String value;

	public InstanceTag(String name, String value) {

		this.name = name;
		this.value = value;
	}

	public String getKey() {

		return name;
	}

	public String getValue() {

		return value;
	}
}
