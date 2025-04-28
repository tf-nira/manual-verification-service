package in.tf.nira.manual.verification;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import in.tf.nira.manual.verification.listener.Listener;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleAPIManager;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleManager;
import io.mosip.registration.processor.core.abstractverticle.StageHealthCheckHandler;
import io.mosip.registration.processor.core.eventbus.KafkaMosipEventBus;
import io.mosip.registration.processor.core.eventbus.VertxMosipEventBus;
import io.mosip.registration.processor.rest.client.config.RestConfigBean;

@ComponentScan(basePackages = { "in.tf.nira.*", "${mosip.auth.adapter.impl.basepackage}","io.mosip.registration.processor.rest.client.*",
		"io.mosip.registration.processor.core.token.*",
		"io.mosip.registration.processor.core.config"},
excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, value =  {RestConfigBean.class,KafkaMosipEventBus.class,VertxMosipEventBus.class
		,StageHealthCheckHandler.class,MosipVerticleManager.class,MosipVerticleAPIManager.class,MosipEventBus.class}))
						
@SpringBootApplication
@EnableJpaRepositories(basePackages = "in.tf.nira.manual.verification.repository")
@EnableScheduling
public class ManualVerificationApplication {

	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		ConfigurableApplicationContext configurableApplcnConetxt = SpringApplication.run(ManualVerificationApplication.class, args);
        Listener listener = configurableApplcnConetxt.getBean(Listener.class);
        listener.runVerificationQueue();
	}

}
