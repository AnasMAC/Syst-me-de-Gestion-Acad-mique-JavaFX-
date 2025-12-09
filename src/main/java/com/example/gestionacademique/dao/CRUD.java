package com.example.gestionacademique.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CRUD <T,PK>{
    public void create(T objet) throws SQLException;
    public void update(T objet) throws SQLException;
    public Boolean delete(PK id) throws SQLException;
    public Optional<T> findById(PK id) throws SQLException;
    public List<T> findAll() throws SQLException;
}
