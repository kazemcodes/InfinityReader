package ireader.domain.usecases.local.book_usecases

import ireader.domain.models.entities.Book
import ireader.domain.data.repository.BookRepository
import kotlinx.coroutines.flow.Flow


/**
 * return a book from id
 */

class SubscribeBookById(private val bookRepository: BookRepository) {
    operator fun invoke(id: Long): Flow<Book?> {
        return bookRepository.subscribeBookById(id = id)
    }
}

class FindBookById(private val bookRepository: BookRepository) {
    suspend operator fun invoke(id: Long?): Book? {
        if (id == null) return null
        return bookRepository.findBookById(id = id)
    }
}


class FindDuplicateBook(private val bookRepository: BookRepository) {
    suspend operator fun invoke(title:String, source: Long): Book? {
       return bookRepository.findDuplicateBook(title,source)
    }
}
