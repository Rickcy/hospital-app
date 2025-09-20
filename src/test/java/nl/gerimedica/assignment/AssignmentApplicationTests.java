package nl.gerimedica.assignment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AssignmentApplicationTests {
	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		restTemplate = new RestTemplate();
	}

	@Test
	void testSuccess() {
		String url = "http://localhost:8080/api/appointments-by-reason?keyword=Checkup";

		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

		String body = response.getBody();

		assertTrue(body.contains("\"reason\" : \"SomeNonExistentField\""));
	}
}
