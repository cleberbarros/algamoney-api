CREATE TABLE categoria (
  codigo bigint NOT NULL AUTO_INCREMENT,
  nome varchar(50) NOT NULL,
  PRIMARY KEY (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

INSERT INTO categoria(nome) values ('Lazer');
INSERT INTO categoria(nome) values ('Alimentação');
INSERT INTO categoria(nome) values ('Supermercado');
INSERT INTO categoria(nome) values ('Outros');