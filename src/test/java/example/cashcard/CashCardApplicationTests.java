package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.test.annotation.DirtiesContext.ClassMode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.doubleThat;
import static org.mockito.ArgumentMatchers.intThat;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;
	
	@Test
	void shouldReturnACashCardWhenDataIsSaved() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/99", String.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		Number id = documentContext.read("$.id");
		assertThat(id).isEqualTo(99);

		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(123.45);
	}
	
	@Test
	void shouldNotReturnACashCardWithAnUnknowmId(){
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}
	
	@Test
	@DirtiesContext
	void shouldCreateANewCashCard() {
		CashCard newCashCard = new CashCard(null, 250.00);
		ResponseEntity<Void> createResponseEntity = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);		
		
		assertThat(createResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		
		URI locationOfNewCashCardUri = createResponseEntity.getHeaders().getLocation();
		ResponseEntity<String> getResponseEntity = restTemplate.getForEntity(locationOfNewCashCardUri, String.class);
		
		assertThat(getResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(getResponseEntity.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		
		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
	}
	
	@Test
	void shouldReturnAllCashCardWhenListIsRequested() {
		ResponseEntity<String> responseEntity = restTemplate.getForEntity("/cashcards", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());
		int cashCardCount = documentContext.read("$.length()");
		
		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);
		
		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.0, 150.00);
	}
	
	@Test
	void shouldReturnAPageofCashCards() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards?page=0%size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(3);
	}
	
	@Test
	void shouldReturnASortedPageOfCashCards() {
		ResponseEntity<String> responseEntity = restTemplate.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());
		
		JSONArray read = documentContext.read("$[*]");
		assertThat(read.size()).isEqualTo(1);
		
		double amount = documentContext.read("$[0].amount");
		assertThat(amount).isEqualTo(150.0);		
	}
}
