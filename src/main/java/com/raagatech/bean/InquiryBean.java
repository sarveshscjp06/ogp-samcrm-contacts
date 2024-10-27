/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.raagatech.bean;

import java.util.Date;
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
public class InquiryBean {

    private Integer inquiry_id;
    private String firstname;
    private Date inquiry_date;
    private Integer inspiration_id;
    private String email;
    private Long mobile;
    private Integer level_id;
    private String address_line1;
    private String followup_details;
    private Integer inquirystatus_id;
    private String label_text;
    private String label_color;

    private String nationality;
    private String father_name;
    private String mother_name;
    private Date date_of_birth;
    private Long telephone;
    private String photo;
    private char gender;
    private String inspiration;
    private String comfortability;
    private String primaryskill;
}
