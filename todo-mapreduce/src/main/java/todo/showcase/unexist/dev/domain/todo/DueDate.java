/**
 * @package Showcase-Hadoop-CDC-Quarkus
 *
 * @file DueDate class
 * @copyright 2023-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package todo.showcase.unexist.dev.domain.todo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.unexist.showcase.todo.infrastructure.serde.DateDeserializer;
import dev.unexist.showcase.todo.infrastructure.serializer.DateSerializer;

import java.time.LocalDate;
import java.util.Objects;

public class DueDate {
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    @JsonSerialize(using = DateSerializer.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDate start;

    @JsonSerialize(using = DateSerializer.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDate due;

    /**
     * Get start date
     *
     * @return Start date
     **/

    public LocalDate getStart() {
        return start;
    }

    /**
     * Set start date
     *
     * @param  start  Date to set
     **/

    public void setStart(LocalDate start) {
        this.start = start;
    }

    /**
     * Get due date
     *
     * @return Due date
     **/

    public LocalDate getDue() {
        return due;
    }

    /**
     * Set due date
     *
     * @param  due  Date to set
     **/

    public void setDue(LocalDate due) {
        this.due = due;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DueDate dueDate = (DueDate) o;

        return Objects.equals(start, dueDate.start) && Objects.equals(due, dueDate.due);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, due);
    }
}
