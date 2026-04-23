package com.library.app.service;

import com.library.app.dao.BookCopyDAO;
import com.library.app.dao.BookDAO;
import com.library.app.dao.LoanDAO;
import com.library.app.model.Book;
import com.library.app.model.BookCatalogItem;
import com.library.app.util.ValidationUtil;

import java.util.List;

public class BookService {
    private final BookDAO bookDAO = new BookDAO();
    private final BookCopyDAO bookCopyDAO = new BookCopyDAO();
    private final LoanDAO loanDAO = new LoanDAO();

    public Book addBook(String isbn, String title, String author, String publisher,
                        int publicationYear, String category, String shelfCode, String coverUrl, int totalCopies) {
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
        book.setCoverUrl(coverUrl == null ? "" : coverUrl.trim());
        bookDAO.save(book);
        bookCopyDAO.saveCopies(book.getId(), totalCopies);
        return book;
    }

    public List<BookCatalogItem> searchCatalog(String keyword) {
        return bookDAO.searchCatalog(keyword == null ? "" : keyword.trim());
    }

    public void updateBook(long bookId, String isbn, String title, String author, String publisher,
                           int publicationYear, String category, String shelfCode, String coverUrl) {
        if (bookId <= 0) {
            throw new IllegalArgumentException("Data buku tidak valid.");
        }
        ValidationUtil.requireNotBlank(isbn, "ISBN wajib diisi.");
        ValidationUtil.requireNotBlank(title, "Judul buku wajib diisi.");
        ValidationUtil.requireNotBlank(author, "Penulis wajib diisi.");
        ValidationUtil.requirePublicationYear(publicationYear);

        Book book = new Book();
        book.setId(bookId);
        book.setIsbn(isbn.trim());
        book.setTitle(title.trim());
        book.setAuthor(author.trim());
        book.setPublisher(publisher == null ? "" : publisher.trim());
        book.setPublicationYear(publicationYear);
        book.setCategory(category == null ? "" : category.trim());
        book.setShelfCode(shelfCode == null ? "" : shelfCode.trim());
        book.setCoverUrl(coverUrl == null ? "" : coverUrl.trim());
        bookDAO.update(book);
    }

    public void deleteBook(long bookId) {
        if (bookId <= 0) {
            throw new IllegalArgumentException("Data buku tidak valid.");
        }
        if (loanDAO.hasLoanHistoryByBookId(bookId)) {
            throw new IllegalArgumentException("Buku tidak dapat dihapus karena sudah memiliki riwayat peminjaman.");
        }

        bookCopyDAO.deleteByBookId(bookId);
        bookDAO.deleteById(bookId);
    }
}
