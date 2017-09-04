package com.example.demo;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.TransportClientFactoryBean;

import java.net.InetSocketAddress;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	// Set your credentials here
	private static final String CLUSTER_NAME = "ELASTIC_CO_CLUSTER_LONG_ID";
	private static final String USERNAME = "elastic";
	private static final String PASSWORD = "PASSWORD";

	private static final String CLUSTER_URL = CLUSTER_NAME + ".us-east-1.aws.found.io";
	private static final String CREDENTIALS = USERNAME + ":" + PASSWORD;

    @Bean
	public ApplicationRunner runner(TransportClient client, CustomerRepository repository) {


	    return (args) -> {
            ClusterHealthResponse response = client.admin().cluster().prepareHealth().get();
            System.out.println("response = " + response.getClusterName());

            repository.deleteAll();
            repository.save(new Customer("Alice", "Smith"));
            repository.save(new Customer("Bob", "Smith"));
            System.out.println("Customers found with findAll():");
            System.out.println("-------------------------------");
            for (Customer customer : repository.findAll()) {
                System.out.println(customer);
            }
            System.out.println();

        };
    }

	@Bean
	public TransportClient elasticsearchSecuredClient() throws Exception {
        // Based on https://github.com/elastic/found-shield-example/blob/master/src/main/java/org/elasticsearch/cloud/transport/example/TransportExample.java
        Settings settings = Settings.builder()
                .put("client.transport.nodes_sampler_interval", "5s")
                .put("client.transport.sniff", false)
                .put("transport.tcp.compress", true)
                .put("cluster.name", CLUSTER_NAME)
                .put("xpack.security.transport.ssl.enabled", true)
                .put("request.headers.X-Found-Cluster", CLUSTER_NAME)
                .put("xpack.security.user", CREDENTIALS)
                .build();

        return new PreBuiltXPackTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(CLUSTER_URL, 9343)));
	}

}
