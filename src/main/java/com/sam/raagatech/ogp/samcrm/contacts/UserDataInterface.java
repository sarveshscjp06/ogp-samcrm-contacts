/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.sam.raagatech.ogp.samcrm.contacts;

import com.raagatech.bean.InquiryBean;
import com.raagatech.bean.SliderImageBean;
import com.raagatech.bean.UserDataBean;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 *
 * @author sarve
 * The services of this sub-project is about Contact / Customer Application under SAMCRM project.
 */
public interface UserDataInterface {

    public boolean checkLogin(String userName, long password) throws Exception;

    public boolean insertUser(String username, String password, String email, long mobileNo) throws Exception;

    public boolean insertInquiry(String inquiryname, int inspirationid, String email, long mobileNo,
            int levelid, String address, String followupDetails,
            String nationality, String fname, String mname, String dob, long telOther, String image,
            String gender, String inspiration, String comfortability, String primaryskill) throws Exception;

    public LinkedHashMap<Integer, String> selectLevel() throws Exception;

    public LinkedHashMap<Integer, String> selectInspiration() throws Exception;

    public ArrayList<InquiryBean> listInquiry() throws Exception;

    public boolean updateInquiry(int inquiry_id, String inquiryname, int inspirationid, String email, long mobileNo,
            int levelid, String address, String followupDetails, String nationality, String fname, String mname, String dob, long telOther, String image,
            String gender, String inspiration, String comfortability, String primaryskill) throws Exception;

    public LinkedHashMap<Integer, String> selectInquiryStatus() throws Exception;

    public boolean updateFollowup(int inquiry_id, int inquirystatus_id, String followupDetails) throws Exception;

    public InquiryBean getInquiryById(int inquiryId) throws Exception;

    public ArrayList<SliderImageBean> listSliderImage();

    public int selectInquiry(String email, long mobileNo) throws Exception;

    public InquiryBean getInquiryDetails(String email, long mobileNo) throws Exception;

    public int getSamcrmUserStatus(String mobileNo, String password, String ipAddress) throws Exception;

    public int addUpdateSamcrmUser(String name, String email, String mobile, String zipCode, String password, String ipAddress,
            String vendorCategoryId, String vendorSubtypeId, String vendorTitle, String vendorRegistrationNo, String individual_id,
            String vendorDescription, String pushMessage, String emailCampaignText, String bulkSmsText, String address) throws Exception;

    public int updateSamcrmUserStatus(String mobile, String ipAddress, int userStatus) throws Exception;

    public LinkedHashMap<String, String> checkSamcrmLogin(String mobileNo, String ipAddress, String individualId) throws Exception;

    public int logoutSamcrmUser(String mobileNo, String ipAddress) throws Exception;

    public int deactivateCustomer(int vendorId, int cusomerId, int mobileNo, int isVendor) throws Exception;

    public UserDataBean selectUserData(long user_id) throws Exception;
}
