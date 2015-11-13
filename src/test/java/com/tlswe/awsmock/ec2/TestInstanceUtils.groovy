package com.tlswe.awsmock.ec2

import com.tlswe.awsmock.common.util.InstanceUtils
import com.tlswe.awsmock.ec2.model.AbstractMockEc2Instance
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static com.tlswe.awsmock.ec2.model.AbstractMockEc2Instance.InstanceState.RUNNING
import static com.tlswe.awsmock.ec2.model.AbstractMockEc2Instance.InstanceType.T1_MICRO

class TestInstanceUtils extends Specification {

	void 'loads a predefined instance from a config file'() {
		given:
		def instances = InstanceUtils.predefinedInstances()

		when:
		def instance = instances.find { it.instanceID == 'i-test' }

		then:
		instance.instanceID == 'i-test'
		instance.imageId == 'ami-123456'
		instance.instanceType == T1_MICRO
		instance.securityGroups == [ 'sg-123456' ] as Set
	}

	void 'starts a predefined instance marked as running'() {
		given:
		def conditions = new PollingConditions(timeout: 2)
		def instances = InstanceUtils.predefinedInstances()

		when:
		def instance = instances.find { it.instanceID == 'i-running' }

		then:
		conditions.eventually {
			assert instance.instanceState == RUNNING
		}
	}

	void 'uses a custom class if specified'() {
		given:
		def originalClass = InstanceUtils.MOCK_EC2_INSTANCE_CLASS_NAME
		InstanceUtils.MOCK_EC2_INSTANCE_CLASS_NAME = CustomInstanceClass.name

		when:
		def instances = InstanceUtils.predefinedInstances()
		InstanceUtils.MOCK_EC2_INSTANCE_CLASS_NAME = originalClass

		then:
		instances.every { it instanceof CustomInstanceClass }
	}

	void 'adds events for an instance'() {
		given:
		def instances = InstanceUtils.predefinedInstances()

		when:
		def instance = instances.find { it.instanceID == 'i-events' }

		then:
		def event = instance.events.first()
		event.code == 'instance-stop'
		event.description == 'The instance is running on degraded hardware'
		event.notAfter == '2015-11-06T13:30:20'
		event.notBefore == '2015-11-04T13:30:20'
	}
}

class CustomInstanceClass extends AbstractMockEc2Instance {

	CustomInstanceClass(String instanceID) {
		super(instanceID)
	}

	@Override
	void onStarted() {

	}

	@Override
	void onBooted() {

	}

	@Override
	void onStopping() {

	}

	@Override
	void onStopped() {

	}

	@Override
	void onTerminating() {

	}

	@Override
	void onTerminated() {

	}

	@Override
	void onInternalTimer() {

	}
}