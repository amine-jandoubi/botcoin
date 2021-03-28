package com.github.sbouclier.result;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result wrapper with last id
 *
 * @param <T> result response type
 * @author Stéphane Bouclier
 */
public class ResultWithLastId<T> extends Result<T> {
    private Long last = 0L;

    public Long getLast() {
        return last;
    }

    public void setLast(Long lastId) {
        this.last = lastId;
    }
}
