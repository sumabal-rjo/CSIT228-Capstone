package com.example.capstone.database;

import java.util.List;


public interface GenericDAO<T> {

    boolean add(T item);
    boolean update(T item);
    boolean delete(int id);
    List<T> getAll();
    T getById(int id);
}
