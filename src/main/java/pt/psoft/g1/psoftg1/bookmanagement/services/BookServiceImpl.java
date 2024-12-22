package pt.psoft.g1.psoftg1.bookmanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
//import pt.psoft.g1.psoftg1.authormanagement.model.Author;
//import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
//import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
//import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@PropertySource({"classpath:config/library.properties"})
public class BookServiceImpl implements BookService {

	private final BookRepository bookRepository;

	@Override
	public Book create(CreateBookRequest request, String isbn) {

		if(bookRepository.findByIsbn(isbn).isPresent()){
			throw new ConflictException("Book with ISBN " + isbn + " already exists");
		}


		MultipartFile photo = request.getPhoto();
		String photoURI = request.getPhotoURI();
		if(photo == null && photoURI != null || photo != null && photoURI == null) {
			request.setPhoto(null);
			request.setPhotoURI(null);
		}


		Book newBook = new Book(isbn, request.getTitle(), request.getDescription(), /*genre, authors,*/ photoURI);

        return bookRepository.save(newBook);
	}


	@Override
	public Book update(UpdateBookRequest request, String currentVersion) {

        var book = findByIsbn(request.getIsbn());


		MultipartFile photo = request.getPhoto();
		String photoURI = request.getPhotoURI();
		if(photo == null && photoURI != null || photo != null && photoURI == null) {
			request.setPhoto(null);
			request.setPhotoURI(null);
		}

        book.applyPatch(Long.parseLong(currentVersion), request);

		bookRepository.save(book);


		return book;
	}

	@Override
	public Book save(Book book) {
		return this.bookRepository.save(book);
	}



	public Book findByIsbn(String isbn) {
		return this.bookRepository.findByIsbn(isbn)
				.orElseThrow(() -> new NotFoundException(Book.class, isbn));
	}


}
