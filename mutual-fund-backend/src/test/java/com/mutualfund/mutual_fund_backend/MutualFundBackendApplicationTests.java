package com.mutualfund.mutual_fund_backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MutualFundBackendApplicationTests {

	@Autowired
	private DataSource dataSource;

	@Test
	void contextLoads() {
	}

	@Test
	void serviceAndNewtonClientCanBeCreatedWithoutTrouble() {
		MutualFundService service = new MutualFundService();
		assertNotNull(service);
		NewtonStockBetaClient client = new NewtonStockBetaClient();
		assertNotNull(client);
	}

	/**
	 * Verifies that the database configured in application.properties is reachable.
	 * Only runs when RUN_DB_TESTS=true (e.g. RUN_DB_TESTS=true ./mvnw test).
	 * Skip when DB is not available (e.g. local dev without VPN to AWS RDS).
	 */
	@Test
	@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
	void databaseConnectionSucceeds() {
		assertNotNull(dataSource, "DataSource should be configured");
		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		Integer result = jdbc.queryForObject("SELECT 1", Integer.class);
		assertNotNull(result);
		assert result == 1;
	}
}
