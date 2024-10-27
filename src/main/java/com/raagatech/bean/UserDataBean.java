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
public class UserDataBean {

    private String individual_id;
    private String name;
    private String email;
    private String mobile;
    private String zipcode;
    private String address;
    private String creation_date;
    private String country_code;
    private String dob;
    private String vendorTitle;
    private String vendorCategoryName;
    private String vendorCategoryDescription;
    private String vendorSubtypeName;
    private String vendorSubtypeDescription;
    private String vendorGroupId;
    private String vendorRegistrationNo;
    private String vendorLogoName;
    private String vendorLogoUrl;
    private String message;
    private String comment;
}
