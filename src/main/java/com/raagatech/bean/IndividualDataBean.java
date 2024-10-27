/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.raagatech.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author sarve
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class IndividualDataBean {

    private String name;
    private String mobile;
    private String email;
    private String zipCode;
    private String countryCode;
    private String address;
    private String creationDate;
    private String profilePic;
    private String profileColor;
    private String dateOfBirth;
    private String marraigeAinversary;
    private String occupation;
    private String spouse;
    private String chiled1;
    private String child2;
}
