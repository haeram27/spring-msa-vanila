package com.example.springwebex.service;

import org.springframework.stereotype.Service;

import com.example.springwebex.model.restreq.BasicReqDto;
import com.example.springwebex.model.restresp.ResponseJsonDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PostEchoService {

    public ResponseJsonDto<?> echo(BasicReqDto request) {
        ResponseJsonDto<BasicReqDto> response = new ResponseJsonDto<>();
        response.setResponse(request);

        return response;
    }

}
