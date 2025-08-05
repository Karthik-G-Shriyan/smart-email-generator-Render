package com.unicksbyte.smart_email_generator.entity;


import lombok.Data;

@Data
public class EmailRequest {

    private String emailContent;

    private String tone;
}
