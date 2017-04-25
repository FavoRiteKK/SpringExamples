package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Favo
 *         Created on 24/04/2017.
 */
@RestController
@RequestMapping("/bookmarks")
class BookmarkRestController {

    private static final Logger logger = LoggerFactory.getLogger(BookmarkRestController.class);

	private final BookmarkRepository bookmarkRepository;

	private final AccountRepository accountRepository;

	@Autowired
	BookmarkRestController(BookmarkRepository bookmarkRepository,
						   AccountRepository accountRepository) {
		this.bookmarkRepository = bookmarkRepository;
		this.accountRepository = accountRepository;
	}

	@RequestMapping(method = RequestMethod.GET)
	Resources<BookmarkResource> readBookmarks(Principal principal) {

        logger.info("Inside BookmarkRestController.readBookmarks()");

		this.validateUser(principal);

		List<BookmarkResource> bookmarkResourceList = bookmarkRepository
			.findByAccountUsername(principal.getName()).stream()
			.map(BookmarkResource::new)
			.collect(Collectors.toList());

		return new Resources<>(bookmarkResourceList);
	}

	@RequestMapping(method = RequestMethod.POST)
	ResponseEntity<?> add(Principal principal, @RequestBody Bookmark input) {

        logger.info("Inside BookmarkRestController.add()");

		this.validateUser(principal);

		return accountRepository
				.findByUsername(principal.getName())
				.map(account -> {
					Bookmark bookmark = bookmarkRepository.save(
						new Bookmark(account, input.uri, input.description));

					Link forOneBookmark = new BookmarkResource(bookmark).getLink(Link.REL_SELF);

					return ResponseEntity.created(URI
						.create(forOneBookmark.getHref()))
						.build();
				})
				.orElse(ResponseEntity.noContent().build());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{bookmarkId}")
	BookmarkResource readBookmark(Principal principal, @PathVariable Long bookmarkId) {

        logger.info("Inside BookmarkRestController.readBookmark()");

		this.validateUser(principal);
		return new BookmarkResource(
			this.bookmarkRepository.findOne(bookmarkId));
	}

	private void validateUser(Principal principal) {

        logger.info("Inside BookmarkRestController.validateUser()");

        String userId = principal.getName();
		this.accountRepository
			.findByUsername(userId)
			.orElseThrow(
				() -> new UserNotFoundException(userId));
	}
}