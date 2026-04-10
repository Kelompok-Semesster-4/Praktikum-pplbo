package com.library.app.service;

import com.library.app.dao.BookCopyDAO;
import com.library.app.dao.BookDAO;
import com.library.app.model.Book;
import com.library.app.model.BookCatalogItem;
import com.library.app.util.ValidationUtil;

import java.util.List;

public class BookService {
    private final BookDAO bookDAO = new BookDAO();
    private final BookCopyDAO bookCopyDAO = new BookCopyDAO();

    public Book addBook(String isbn, String title, String author, String publisher,
                        int publicationYear, String category, String shelfCode, int totalCopies) {
        ValidationUtil.requireNotBlank(isbn, "ISBN wajib diisi.");
        ValidationUtil.requireNotBlank(title, "Judul buku wajib diisi.");
        ValidationUtil.requireNotBlank(author, "Penulis wajib diisi.");
        ValidationUtil.requirePublicationYear(publicationYear);
        ValidationUtil.requirePositive(totalCopies, "Jumlah eksemplar harus lebih dari 0.");

        Book book = new Book();
        book.setIsbn(isbn.trim());
        book.setTitle(title.trim());
        book.setAuthor(author.trim());
        book.setPublisher(publisher == null ? "" : publisher.trim());
        book.setPublicationYear(publicationYear);
        book.setCategory(category == null ? "" : category.trim());
        book.setShelfCode(shelfCode == null ? "" : shelfCode.trim());
        bookDAO.save(book);
        bookCopyDAO.saveCopies(book.getId(), totalCopies);
        return book;
    }

    public List<BookCatalogItem> searchCatalog(String keyword) {
        return bookDAO.searchCatalog(keyword == null ? "" : keyword.trim());
    }
}
