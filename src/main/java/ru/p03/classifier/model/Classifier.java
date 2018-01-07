/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.p03.classifier.model;

import java.io.Serializable;

/**
 *
 * @author timofeevan
 */
public abstract class Classifier implements Serializable {

    private static final long serialVersionUID = 1L;

    public Classifier() {
    }

    public abstract Long getId();

    public abstract Integer getIsDeleted();

    public abstract void setIsDeleted(Integer isDeleted);

}
