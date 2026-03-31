package com.example.springwebex.model.pagenation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL) // serialize
@JsonIgnoreProperties(ignoreUnknown = true) // deserialize
public class Server {
    public Server (
        String hostName,
        String ip,
        String managerId,
        String managerName,
        String developerId,
        String developerName,
        String serviceName
    ) {
        this.hostName = hostName;
        this.ip = ip;
        this.managerId = managerId;
        this.managerName = managerName;
        this.developerId = developerId;
        this.developerName = developerName;
        this.serviceName = serviceName;
    }

    String hostName;
    String ip;
    String managerId;
    String managerName;
    String developerId;
    String developerName;
    String serviceName;
}
