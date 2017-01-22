package com.spbsu.datastream.example.bl.inverted_index;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Author: Artem
 * Date: 18.01.2017
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value=WordPage.class, name="wordpage"),
        @JsonSubTypes.Type(value=WordIndex.class, name="index")
})
public interface WordContainer {
  String word();
}
