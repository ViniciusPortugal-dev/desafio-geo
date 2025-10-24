DROP TABLE IF EXISTS pedido CASCADE;
DROP TABLE IF EXISTS entregador CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

CREATE TABLE IF NOT EXISTS usuario (
                                       id BIGSERIAL PRIMARY KEY,
                                       external_id VARCHAR(50) NOT NULL UNIQUE,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS entregador (
                                          id BIGSERIAL PRIMARY KEY,
                                          nome VARCHAR(120) NOT NULL,
    telefone VARCHAR(40) NOT NULL
    );

CREATE TABLE IF NOT EXISTS pedido (
                                      id BIGSERIAL PRIMARY KEY,
                                      external_id VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255) NOT NULL,
    valor NUMERIC(14,2) NOT NULL,
    id_usuario BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    id_entregador BIGINT NOT NULL REFERENCES entregador(id) ON DELETE CASCADE
    );

-- Índices
CREATE INDEX IF NOT EXISTS idx_usuario_external_id    ON usuario(external_id);
CREATE INDEX IF NOT EXISTS idx_pedido_external_id     ON pedido(external_id);
