package pt.psoft.g1.psoftg1.bookmanagement.services;


import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.util.List;

/**
 *
 */
public interface BookService {
    Book create(CreateBookRequest request, String isbn);
    Book create(BookViewAMQP bookViewAMQP);
    Book save(Book book);
    Book findByIsbn(String isbn);
    Book update(UpdateBookRequest request, String currentVersion);
    Book update(BookViewAMQP bookViewAMQP);
}
