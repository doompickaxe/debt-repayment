package com.example.debtrepayment;

import com.example.debtrepayment.api.StatusApi;
import com.example.debtrepayment.api.model.Status;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.OffsetDateTime;

public class StatusResource implements StatusApi {

    @ConfigProperty(name = "quarkus.application.version")
    String version;

    @Override
    public Status getStatus() {
        return new Status()
                .status(Status.StatusEnum.UP)
                .version(version)
                .timestamp(OffsetDateTime.now());
    }
}
