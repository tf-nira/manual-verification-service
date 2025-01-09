package in.tf.nira.manual.verification;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static final Logger logger = LoggerFactory.getLogger(ManualVerificationApplication.class);

	public static void main(String[] args) {
		logJavaVersion();
		logBouncyCastleVersion();
		ConfigurableApplicationContext configurableApplcnConetxt = SpringApplication.run(ManualVerificationApplication.class, args);
        Listener listener = configurableApplcnConetxt.getBean(Listener.class);
        listener.runVerificationQueue();
	}
	
	public static void logJavaVersion() {
		String javaVersion = System.getProperty("java.version");
		logger.info("Java Version: {}", javaVersion);
	}

	public static void logBouncyCastleVersion() {
		Security.addProvider(new BouncyCastleProvider());
		BouncyCastleProvider provider = new BouncyCastleProvider(); 
		Double bcVersion = provider.getVersion();
		logger.info("Bouncy Castle Version: {}", bcVersion);
	}

}
