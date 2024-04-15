package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;
import java.net.URI;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/cashcards")
class CashCardController {
    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
       this.cashCardRepository = cashCardRepository;
    }
    
    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId) {
    	Optional<CashCard> cashCardOptional = cashCardRepository.findById(requestedId);
    	
    	if(cashCardOptional.isPresent()) {
    		return ResponseEntity.ok(cashCardOptional.get());
    	}else{
    		return ResponseEntity.notFound().build();
    	}
   }
    
    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb){
    	CashCard saveCashCard = cashCardRepository.save(newCashCardRequest);
    	
    	URI localtionOfNewCashCardBuilder = ucb
    			.path("cashcards/{id}")
    			.buildAndExpand(saveCashCard.id())
    			.toUri();
    	
    	return ResponseEntity.created(localtionOfNewCashCardBuilder).build();
    }
    
    @GetMapping
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable){
        Page<CashCard> page = cashCardRepository.findAll(
            PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
            )
        );
        return ResponseEntity.ok(page.getContent());
    }    
}