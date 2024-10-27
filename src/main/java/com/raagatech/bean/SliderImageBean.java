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
public class SliderImageBean {

    private String id;
    private String title;
    private String description;
    private String image_url;
}
