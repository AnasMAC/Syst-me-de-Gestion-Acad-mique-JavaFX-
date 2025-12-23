-- Nettoyage préalable (optionnel, pour redémarrer à zéro)
DROP TABLE IF EXISTS Student_Cours CASCADE;
DROP TABLE IF EXISTS Formation_Cours CASCADE;
DROP TABLE IF EXISTS DossierAdministratif CASCADE;
DROP TABLE IF EXISTS Student CASCADE;
DROP TABLE IF EXISTS Cours CASCADE;
DROP TABLE IF EXISTS Formation CASCADE;

-- 1. Table Formation (Filière)
CREATE TABLE Formation (
                           id SERIAL PRIMARY KEY,
                           name VARCHAR(100) NOT NULL UNIQUE -- Règle 2: Code/Nom filière unique
);

-- 2. Table Student (Élève)
CREATE TABLE Student (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         moyenne FLOAT NOT NULL,
                         formation_id INTEGER NOT NULL, -- Obligatoire pour la logique métier
                         FOREIGN KEY (formation_id) REFERENCES Formation(id) ON DELETE RESTRICT, -- Règle 5: Suppression interdite si contient élèves
                         CONSTRAINT chk_moyenne CHECK (moyenne >= 0 AND moyenne <= 20)
);

-- 3. Table DossierAdministratif
CREATE TABLE DossierAdministratif (
                                      id SERIAL PRIMARY KEY,
                                      numero_inscription VARCHAR(50) NOT NULL UNIQUE, -- Règle 1: Matricule unique
                                      date_creation DATE DEFAULT CURRENT_DATE,
                                      student_id INTEGER NOT NULL UNIQUE, -- Règle 4: Un seul dossier par élève (One-to-One)
                                      FOREIGN KEY (student_id) REFERENCES Student(id) ON DELETE CASCADE
);

-- 4. Table Cours
CREATE TABLE Cours (
                       id SERIAL PRIMARY KEY,
                       code VARCHAR(50) NOT NULL UNIQUE, -- Règle 2: Code cours unique
                       intitule VARCHAR(150) NOT NULL
);

-- 5. Association Filière - Cours (Quels cours sont dispos pour quelle filière)
CREATE TABLE Formation_Cours (
                                 formation_id INTEGER NOT NULL,
                                 cours_id INTEGER NOT NULL,
                                 PRIMARY KEY (formation_id, cours_id),
                                 FOREIGN KEY (formation_id) REFERENCES Formation(id) ON DELETE CASCADE,
                                 FOREIGN KEY (cours_id) REFERENCES Cours(id) ON DELETE CASCADE
);

-- 6. Association Élève - Cours (Inscriptions aux cours)
CREATE TABLE Student_Cours (
                               student_id INTEGER NOT NULL,
                               cours_id INTEGER NOT NULL,
                               date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (student_id, cours_id),
                               FOREIGN KEY (student_id) REFERENCES Student(id) ON DELETE CASCADE,
                               FOREIGN KEY (cours_id) REFERENCES Cours(id) ON DELETE CASCADE
);

-- Données de test
INSERT INTO Formation (name) VALUES ('Informatique'), ('Gestion');