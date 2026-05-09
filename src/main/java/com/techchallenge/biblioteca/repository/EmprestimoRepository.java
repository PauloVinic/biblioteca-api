package com.techchallenge.biblioteca.repository;

import com.techchallenge.biblioteca.entity.Emprestimo;
import jakarta.persistence.LockModeType;
import com.techchallenge.biblioteca.enums.StatusEmprestimo;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    boolean existsByLivro_Id(Long livroId);

    boolean existsByUsuario_Id(Long usuarioId);

    boolean existsByLivro_IdAndStatus(Long livroId, StatusEmprestimo status);

    boolean existsByLivro_IdAndStatusAndIdNot(Long livroId, StatusEmprestimo status, Long emprestimoId);

    @EntityGraph(attributePaths = {"livro", "usuario"})
    Page<Emprestimo> findByStatus(StatusEmprestimo status, Pageable pageable);

    @Query("""
            select e
            from Emprestimo e
            where e.livro.id = :livroId
              and e.status = com.techchallenge.biblioteca.enums.StatusEmprestimo.EMPRESTADO
            order by e.dataEmprestimo desc
            """)
    List<Emprestimo> findEmprestimosAtivosByLivroId(@Param("livroId") Long livroId);

    @Query("""
            select e
            from Emprestimo e
            where e.usuario.id = :usuarioId
              and e.status = com.techchallenge.biblioteca.enums.StatusEmprestimo.EMPRESTADO
            order by e.dataPrevistaDevolucao asc
            """)
    List<Emprestimo> findEmprestimosAtivosByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("""
            select l.id, l.titulo, l.autor, l.isbn, count(e.id)
            from Emprestimo e
            join e.livro l
            group by l.id, l.titulo, l.autor, l.isbn
            order by count(e.id) desc
            """)
    List<Object[]> findLivrosMaisEmprestadosComQuantidade(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e
            from Emprestimo e
            join fetch e.livro
            join fetch e.usuario
            where e.id = :id
            """)
    java.util.Optional<Emprestimo> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select e
            from Emprestimo e
            join fetch e.livro
            join fetch e.usuario
            where e.status = com.techchallenge.biblioteca.enums.StatusEmprestimo.EMPRESTADO
            order by e.dataPrevistaDevolucao asc
            """)
    List<Emprestimo> findEmprestimosAtivosComPrevisaoDevolucao();
}
