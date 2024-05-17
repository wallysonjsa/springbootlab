package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.net.URI;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;
import java.security.Principal;

@RestController
@RequestMapping("/cashcards")
class CashCardController {
    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
       this.cashCardRepository = cashCardRepository;
    }
    
    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
    	CashCard cashCard = findCashCard(requestedId, principal);

    	if(cashCard != null) {
    		return ResponseEntity.ok(cashCard);
    	}else{
    		return ResponseEntity.notFound().build();
    	}
   }
    
    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal){
    	CashCard cashCardWithOwnerCard = new CashCard(
    			null, newCashCardRequest.amount(), principal.getName()
    			);
    	
    	CashCard saveCashCard = cashCardRepository.save(cashCardWithOwnerCard);
    	
    	URI localtionOfNewCashCardBuilder = ucb
    			.path("cashcards/{id}")
    			.buildAndExpand(saveCashCard.id())
    			.toUri();
    	
    	return ResponseEntity.created(localtionOfNewCashCardBuilder).build();
    }
    
    @GetMapping
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal){
        Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(),
            PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
            )
        );
        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal){
    	CashCard cashCard = findCashCard(requestedId, principal);

		if(cashCard!=null){
			CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());
			cashCardRepository.save(updatedCashCard);
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
    }

	private CashCard findCashCard(Long requestedId, Principal principal){
		return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
	}

	@DeleteMapping("/{id}")
	private ResponseEntity<Void> deleteCashCard(@PathVariable Long id){
		return ResponseEntity.noContent().build();
	}
    
}