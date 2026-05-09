package com.techchallenge.biblioteca.repository;

import com.techchallenge.biblioteca.entity.Livro;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    Optional<Livro> findByIsbnIgnoreCase(String isbn);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select l
            from Livro l
            where l.id = :id
            """)
    Optional<Livro> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select l
            from Livro l
            where (trim(coalesce(:titulo, '')) = '' or lower(l.titulo) like lower(concat('%', trim(:titulo), '%')))
              and (trim(coalesce(:autor, '')) = '' or lower(l.autor) like lower(concat('%', trim(:autor), '%')))
              and (trim(coalesce(:isbn, '')) = '' or lower(l.isbn) like lower(concat('%', trim(:isbn), '%')))
            """)
    Page<Livro> buscarComFiltros(
            @Param("titulo") String titulo,
            @Param("autor") String autor,
            @Param("isbn") String isbn,
            Pageable pageable
    );
}
