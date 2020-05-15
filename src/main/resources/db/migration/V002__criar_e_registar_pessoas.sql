CREATE TABLE pessoa (
  codigo BIGINT NOT NULL AUTO_INCREMENT,
  nome VARCHAR(50) NOT NULL,
  ativo TINYINT NULL,
  logradouro VARCHAR(80) NULL,
  numero VARCHAR(4) NULL,
  complemento VARCHAR(50) NULL,
  bairro VARCHAR(45) NULL,
  cep VARCHAR(45) NULL,
  cidade VARCHAR(45) NULL,
  estado VARCHAR(45) NULL,
  
  PRIMARY KEY (codigo));
  
  
  INSERT INTO pessoa (nome,ativo,logradouro,numero,complemento,bairro,cep,cidade,estado) VALUES ("ana", 1,"logradouro","200","complemento", "bairro", "cep", "cidade", "estado");
  INSERT INTO pessoa (nome,ativo,logradouro,numero,complemento,bairro,cep,cidade,estado) VALUES ("Clebão", 1,"logradouro","200","complemento", "bairro", "cep", "cidade", "estado");
  INSERT INTO pessoa (nome,ativo,logradouro,numero,complemento,bairro,cep,cidade,estado) VALUES ("Bibita", 1,"logradouro","200","complemento", "bairro", "cep", "cidade", "estado");
  INSERT INTO pessoa (nome,ativo,logradouro,numero,complemento,bairro,cep,cidade,estado) VALUES ("Carla Rafaela", 1,"logradouro","200","complemento", "bairro", "cep", "cidade", "estado");
  INSERT INTO pessoa (nome,ativo,logradouro,numero,complemento,bairro,cep,cidade,estado) VALUES ("Maze", 1,"logradouro","200","complemento", "bairro", "cep", "cidade", "estado");
  INSERT INTO pessoa (nome,ativo,logradouro,numero,complemento,bairro,cep,cidade,estado) VALUES ("Ana das Carlas", 1,"logradouro","200","complemento", "bairro", "cep", "cidade", "estado");
  