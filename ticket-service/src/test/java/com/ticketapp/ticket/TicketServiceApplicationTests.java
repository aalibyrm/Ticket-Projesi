package com.ticketapp.ticket;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Tam context testi PostgreSQL + Kafka + Zeebe + Keycloak gerektiriyor.
// Gerçek testler: SlaCalculatorTest, TicketServiceTest, CommentServiceTest,
//                 TicketControllerTest, TicketRepositoryIntegrationTest
@Disabled("Tam altyapı (DB, Kafka, Zeebe) gerektiriyor - CI/CD ortamında etkinleştirin")
@SpringBootTest
class TicketServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
