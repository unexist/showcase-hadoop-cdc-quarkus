/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file Todo class and aggregate root
 * @copyright 2020-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.domain.todo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "Todos")
@NamedQueries({
        @NamedQuery(name = Todo.FIND_ALL, query = "SELECT t FROM Todo t"),
        @NamedQuery(name = Todo.FIND_BY_ID, query = "SELECT t FROM Todo t WHERE t.id = :id")
})
public class Todo extends TodoBase {
    public static final String FIND_ALL = "Todo.findAll";
    public static final String FIND_BY_ID = "Todo.findById";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    /**
     * Constructor
     **/

    public Todo() {
    }

    /**
     * Constructor
     *
     * @param  base  Base entry
     **/

    public Todo(final TodoBase base) {
        this.update(base);
    }

    /**
     * Update values from base
     *
     * @param  base  Todo base class
     **/

    public void update(final TodoBase base) {
        this.setTitle(base.getTitle());
        this.setDescription(base.getDescription());
        this.setDone(base.getDone());

        if (null != base.getDueDate()) {
            this.setDueDate(base.getDueDate());
        }
    }

    /**
     * Get id of entry
     *
     * @return Id of the entry
     **/

    public int getId() {
        return id;
    }

    /**
     * Set id of entry
     *
     * @param  id  Id of the entry
     **/

    public void setId(int id) {
        this.id = id;
    }
}
