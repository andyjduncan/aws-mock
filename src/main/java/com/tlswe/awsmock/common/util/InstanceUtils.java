package com.tlswe.awsmock.common.util;

import com.tlswe.awsmock.common.exception.AwsMockException;
import com.tlswe.awsmock.ec2.model.AbstractMockEc2Instance;
import com.tlswe.awsmock.ec2.model.AbstractMockEc2Instance.InstanceType;
import com.tlswe.awsmock.ec2.model.InstanceEvent;
import com.tlswe.awsmock.ec2.model.InstanceTag;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

public class InstanceUtils {


	/**
	 * Class for all mock ec2 instances, which should extend {@link AbstractMockEc2Instance}.
	 */
	private static String MOCK_EC2_INSTANCE_CLASS_NAME = PropertiesUtils
			.getProperty(Constants.PROP_NAME_EC2_INSTANCE_CLASS);


	public static Set<AbstractMockEc2Instance> predefinedInstances() {

		InputStream inputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("instances.yml");

		Yaml yaml = new Yaml();


		Constructor<? extends AbstractMockEc2Instance> constructor;

		try {
			// clazzOfMockEc2Instance = (Class<? extends MockEc2Instance>) Class.forName(MOCK_EC2_INSTANCE_CLASS_NAME);

			Class<? extends AbstractMockEc2Instance> clazzOfMockEc2Instance = (Class.forName(MOCK_EC2_INSTANCE_CLASS_NAME)
					.asSubclass(AbstractMockEc2Instance.class));

			constructor = clazzOfMockEc2Instance.getConstructor(String.class);

		} catch ( ClassNotFoundException e ) {
			throw new AwsMockException("badly configured class '" + MOCK_EC2_INSTANCE_CLASS_NAME + "' not found", e);
		} catch ( NoSuchMethodException e ) {
			throw new AwsMockException(e);
		}

		Map<String, Map<String, Map<String, List<Map<String, Object>>>>> instancesRoot = (Map) yaml.load(inputStream);
		if (instancesRoot == null) {
			return emptySet();
		}

		Map<String, Map<String, List<Map<String, Object>>>> predefined = instancesRoot.get("predefined");
		if (predefined == null) {
			return emptySet();
		}

		Map<String, List<Map<String, Object>>> mock = predefined.get("mock");
		if (mock == null) {
			return emptySet();
		}

		List<Map<String, Object>> instances = mock.get("instances");
		if (instances == null) {
			return emptySet();
		}

		return instances
				.stream()
				.map((map) -> {
					AbstractMockEc2Instance instance = null;
					try {
						instance = constructor.newInstance((String) map.get("instance-id"));
					} catch ( InstantiationException | IllegalAccessException | InvocationTargetException e ) {
						throw new AwsMockException(e);
					}

					String newImageID = (String) map.get("image-id");
					if ( newImageID != null )
						instance.setImageId(newImageID);

					InstanceType instanceType = InstanceType.getByName((String) map.get("instance-type"));
					if ( instanceType != null )
						instance.setInstanceType(instanceType);

					List<String> securityGroups = (List<String>) map.get("security-groups");

					if ( securityGroups != null )
						instance.setSecurityGroups(new HashSet<>(securityGroups));

					List<Map<String, String>> events = (List<Map<String, String>>) map.get("events");

					if ( events != null ) {
						instance.setEvents(events.stream()
								.map((event) -> new InstanceEvent(
										event.get("code"),
										event.get("description"),
										event.get("notAfter"),
										event.get("notBefore")))
								.collect(toCollection(LinkedHashSet::new)));
					}

					List<Map<String, String>> tags = (List<Map<String, String>>) map.get("tags");

					if ( tags != null ) {
						instance.setTags(tags.stream()
										.map((tag) -> new InstanceTag(
												tag.get("key"),
												tag.get("value")
										))
								.collect(toCollection(LinkedHashSet::new)));
					}

					String instanceState = (String) map.get("state");

					if ( "running".equals(instanceState) ) {
						instance.start();
					}

					instance.initializeInternalTimer();

					return instance;
				}).collect(toSet());
	}
}
