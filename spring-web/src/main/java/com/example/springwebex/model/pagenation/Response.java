package com.example.springwebex.model.pagenation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)  // serialize
@JsonIgnoreProperties(ignoreUnknown = true) // deserialize
public class Response {
    ResponseHeader header;
    ResponseBody body;
}