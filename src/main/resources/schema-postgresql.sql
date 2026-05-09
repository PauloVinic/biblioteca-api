ALTER TABLE livros ADD COLUMN IF NOT EXISTS version BIGINT;

UPDATE livros
SET version = 0
WHERE version IS NULL;

ALTER TABLE livros ALTER COLUMN version SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_emprestimos_livro_ativo
ON emprestimos (livro_id)
WHERE status = 'EMPRESTADO';
