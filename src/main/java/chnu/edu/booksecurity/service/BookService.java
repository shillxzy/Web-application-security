package chnu.edu.booksecurity.service;

import chnu.edu.booksecurity.model.Book;
import chnu.edu.booksecurity.repository.BookRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    @PostConstruct
    void init() {
        bookRepository.deleteAll();
        bookRepository.saveAll(List.of(
                new Book("Kobzar", "Taras Shevchenko", 1840),
                new Book("Clean Code", "Robert C. Martin", 2008),
                new Book("Effective Java", "Joshua Bloch", 2018)
        ));
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBook(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    public Book createBook(Book book) {
        book.setId(null);
        return bookRepository.save(book);
    }

    public Book updateBook(Book book) {
        Book existing = bookRepository.findById(book.getId()).orElse(null);
        if (existing != null) {
            existing.setTitle(book.getTitle());
            existing.setAuthor(book.getAuthor());
            existing.setPublishedYear(book.getPublishedYear());
            return bookRepository.save(existing);
        }
        return null;
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
}
