package in.tf.nira.manual.verification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import in.tf.nira.manual.verification.listener.Listener;

@ComponentScan(basePackages = {"io.mosip.*", "in.tf.nira.*", "${mosip.auth.adapter.impl.basepackage}"})
@SpringBootApplication
@EnableJpaRepositories(basePackages = "in.tf.nira.manual.verification.repository")
@EnableScheduling
public class ManualVerificationApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplcnConetxt = SpringApplication.run(ManualVerificationApplication.class, args);
        Listener listener = configurableApplcnConetxt.getBean(Listener.class);
        listener.runVerificationQueue();
	}

}
