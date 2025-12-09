CREATE TABLE Formation (
                           id SERIAL PRIMARY KEY,
                           name VARCHAR(100) NOT NULL
);

-- 2. Table Student
CREATE TABLE Student (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         moyenne FLOAT NOT NULL,
                         formation_id INTEGER,
                         FOREIGN KEY (formation_id) REFERENCES Formation(id),
                         CONSTRAINT chk_moyenne CHECK (moyenne >= 0 AND moyenne <= 20)
);

-- Données de test (On ne spécifie PAS l'ID, PostgreSQL le gère)
INSERT INTO Formation (name) VALUES ('Informatique'), ('Gestion');