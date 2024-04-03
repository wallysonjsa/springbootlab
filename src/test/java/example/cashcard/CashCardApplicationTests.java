package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
}
