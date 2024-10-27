/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sam.raagatech.ogp.samcrm.contacts;

import com.raagatech.bean.InquiryBean;
import com.raagatech.bean.SliderImageBean;
import com.raagatech.bean.UserDataBean;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;

/**
 *
 * @author sarve
 */
@Service
public class UserDataSource extends DatabaseConnection implements UserDataInterface {

    protected final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

    @Override
    public int getSamcrmUserStatus(String mobileNo, String password, String ipAddress) throws Exception {
        //0 not available or 8 not individual, 1 not active, 2 not vendor, 
        //4 not loggedin, 5 not on promotion, 6 not from same device, 7 mobile or password not correct
        int samcrmUserStatus = 0;
        Connection connection;
        Statement statement;
        try {
            connection = createConnection();
            statement = connection.createStatement();
            String queryCheckLogin = "SELECT su.is_active, su.is_loggedin, su.is_vendor, su.is_on_promotion, su.ip_address "
                    + "FROM samcrm_users su join samcrm_individual si on su.mobile = si.mobile "
                    + "WHERE su.mobile = " + mobileNo + " and si.password = '" + password + "' and su.ip_address = '" + ipAddress + "' "
                    + "GROUP BY su.is_active, su.is_loggedin, su.is_vendor, su.is_on_promotion, su.ip_address";
            ResultSet result = statement.executeQuery(queryCheckLogin);
            while (result.next()) {
                if (result.getInt("is_active") == 0) {
                    samcrmUserStatus = 1;
                } else if (result.getInt("is_loggedin") == 0) {
                    samcrmUserStatus = 4;
                } else if (result.getInt("is_vendor") == 0) {
                    samcrmUserStatus = 2;
                } else if (result.getInt("is_on_promotion") == 0) {
                    samcrmUserStatus = 5;
                }
            }

            if (samcrmUserStatus == 0) {
                queryCheckLogin = "SELECT individual_id FROM samcrm_individual WHERE mobile = " + mobileNo + " AND password is null";
                result = statement.executeQuery(queryCheckLogin);
                while (result.next()) {
                    if (result.getInt("individual_id") > 0) {
                        samcrmUserStatus = 8;
                    }
                }
            }

            if (samcrmUserStatus == 0) {
                queryCheckLogin = "SELECT individual_id FROM samcrm_individual WHERE mobile = " + mobileNo + " and password = '" + password + "'";
                result = statement.executeQuery(queryCheckLogin);
                while (result.next()) {
                    if (result.getInt("individual_id") > 0) {
                        samcrmUserStatus = 6;
                    }
                }
            }

            if (samcrmUserStatus == 0) {
                queryCheckLogin = "SELECT individual_id FROM samcrm_individual WHERE mobile = " + mobileNo + " OR password = '" + password + "'";
                result = statement.executeQuery(queryCheckLogin);
                while (result.next()) {
                    if (result.getInt("individual_id") > 0) {
                        samcrmUserStatus = 7;
                    }
                }
            }
            result.close();
            connection.close();
        } catch (SQLException sqle) {
            Logger.getAnonymousLogger().log(Level.SEVERE, this.getClass().getName(), sqle);
        }
        return samcrmUserStatus;
    }

    @Override
    public int addUpdateSamcrmUser(String name, String email, String mobile, String zipCode, String password, String ipAddress,
            String vendorCategoryId, String vendorSubtypeId, String vendorTitle, String vendorRegistrationNo, String individual_id,
            String vendorDescription, String pushMessage, String emailCampaignText, String bulkSmsText, String address) throws Exception {
        int records = 0;
        Connection connection;
        Statement statement;
        try {

            if ((individual_id != null && !individual_id.isEmpty()) || getSamcrmUserStatus(mobile, password, ipAddress) == 8) {
                connection = createConnection();
                statement = connection.createStatement();
                String queryUpdateUser = "UPDATE samcrm_individual SET email = '" + email + "' ";
                if (name != null) {
                    queryUpdateUser += ", name = '" + name + "' ";
                }
                if (zipCode != null) {
                    queryUpdateUser += ", zipcode = '" + zipCode + "' ";
                }
                if (password != null) {
                    queryUpdateUser += ", password = '" + password + "' ";
                }
                if (address != null) {
                    queryUpdateUser += ", address = '" + address + "' ";
                }
                queryUpdateUser += " WHERE mobile = " + mobile + " OR individual_id = " + individual_id;
                records = statement.executeUpdate(queryUpdateUser);

                if (records == 1 && vendorCategoryId != null && !vendorCategoryId.isEmpty()
                        && vendorSubtypeId != null && !vendorSubtypeId.isEmpty()) {
                    int vendor = 0;
                    String queryVendorId = "SELECT id FROM samcrm_vendor_category_subtype WHERE mobile = " + mobile;
// commented to prevent multiple vendor_group_id for now.
//                            + " AND vendor_category_id = " + vendorCategoryId
//                            + " AND vendor_subtype_id = " + vendorSubtypeId;
                    ResultSet result = statement.executeQuery(queryVendorId);
                    while (result.next()) {
                        vendor = result.getInt("id");
                        break;
                    }
                    result.close();
                    if (vendor > 0) {
                        statement = connection.createStatement();
                        String queryUpdateVendorStatus = "UPDATE samcrm_vendor_category_subtype set "
                                + "vendor_title ='" + vendorTitle + "', vendor_registration_no = '" + vendorRegistrationNo + "'";
                        if (vendorDescription != null) {
                            queryUpdateVendorStatus += ", description = '" + vendorDescription + "'";
                        }
                        if (pushMessage != null) {
                            queryUpdateVendorStatus += ", push_message = '" + pushMessage + "'";
                        }
                        if (emailCampaignText != null) {
                            queryUpdateVendorStatus += ", email_campaign_text = '" + emailCampaignText + "'";
                        }
                        if (bulkSmsText != null) {
                            queryUpdateVendorStatus += ", bulk_sms_text = '" + bulkSmsText + "'";
                        }
                        queryUpdateVendorStatus += " WHERE id = " + vendor;
                        records = statement.executeUpdate(queryUpdateVendorStatus);
                    } else {
                        statement = connection.createStatement();
                        String queryInsertVendorStatus = "INSERT into samcrm_vendor_category_subtype (mobile, vendor_category_id, vendor_subtype_id, vendor_title, vendor_registration_no, "
                                + "description, push_message, email_campaign_text, bulk_sms_text) "
                                + "VALUES (" + mobile + ", " + Integer.parseInt(vendorCategoryId) + ", " + Integer.parseInt(vendorSubtypeId)
                                + ", '" + vendorTitle + "', '" + vendorRegistrationNo + "', "
                                + " '" + vendorDescription + "', '" + pushMessage + "', '" + emailCampaignText + "', '" + bulkSmsText + "')";
                        records = statement.executeUpdate(queryInsertVendorStatus);

                        queryVendorId = "SELECT id FROM samcrm_vendor_category_subtype WHERE mobile = " + mobile;
                        result = statement.executeQuery(queryVendorId);
                        while (result.next()) {
                            vendor = result.getInt("id");
                        }
                        result.close();

                        statement = connection.createStatement();
                        String queryUpdateUserStatus = "UPDATE samcrm_users SET is_vendor = " + vendor + " WHERE mobile = " + mobile;
                        records = statement.executeUpdate(queryUpdateUserStatus);
                    }
                }

            } else {
                int key = generateNextPrimaryKey("samcrm_individual", "individual_id");
                connection = createConnection();
                statement = connection.createStatement();
                String queryInsertUser = "INSERT into samcrm_individual (individual_id, name, email, mobile, zipcode, password, country_code, creation_date, address) "
                        + "VALUES (" + key + ", '" + name + "', '" + email + "', " + mobile + ", '" + zipCode + "', '" + password + "', 091, '" + FORMATTER.format(new Date()) + "', '" + address + "')";
                records = statement.executeUpdate(queryInsertUser);

                if (records == 1) {
                    int vendor = 0;
                    if (vendorCategoryId != null && !vendorCategoryId.isEmpty()
                            && vendorSubtypeId != null && !vendorSubtypeId.isEmpty()) {
                        statement = connection.createStatement();
                        String queryInsertVendorStatus = "INSERT into samcrm_vendor_category_subtype (mobile, vendor_category_id, vendor_subtype_id, vendor_title, vendor_registration_no, "
                                + "description, push_message, email_campaign_text, bulk_sms_text) "
                                + "VALUES (" + mobile + ", " + Integer.parseInt(vendorCategoryId) + ", " + Integer.parseInt(vendorSubtypeId)
                                + "'" + vendorTitle + "', '" + vendorRegistrationNo + "', "
                                + "'" + vendorDescription + "', '" + pushMessage + "', '" + emailCampaignText + "', '" + bulkSmsText + "')";
                        records = statement.executeUpdate(queryInsertVendorStatus);

                        String queryVendorId = "SELECT id FROM samcrm_vendor_category_subtype WHERE mobile = " + mobile;
                        try ( ResultSet result = statement.executeQuery(queryVendorId)) {
                            while (result.next()) {
                                vendor = result.getInt("id");
                            }
                        }
                    }
                    statement = connection.createStatement();
                    String queryInsertUserStatus = "INSERT into samcrm_users (mobile, is_active, is_vendor, is_loggedin, is_on_promotion, ip_address) "
                            + "VALUES (" + mobile + ", 1, " + vendor + ", 1, 1,'" + ipAddress + "')";
                    records = statement.executeUpdate(queryInsertUserStatus);

                    if (vendor == 1) {
                        statement = connection.createStatement();
                        String insertQuery = "insert into samcrm_creditpoint(vendor_id, credit_date, credit_reward_points, coupon_code, unit) "
                                + "values(" + key + ", '" + FORMATTER.format(new Date()) + "', 10, 'V" + key + "C0O0%10', '%')";
                        records = statement.executeUpdate(insertQuery);
                    }
                }
            }
            connection.close();
        } catch (SQLException sqle) {
            Logger.getAnonymousLogger().log(Level.SEVERE, this.getClass().getName(), sqle);
        }
        return records;
    }

    @Override
    public int updateSamcrmUserStatus(String mobile, String ipAddress, int userStatus) throws Exception {
        int records = 0;
        Connection connection;
        try {
            connection = createConnection();
            Statement statement = connection.createStatement();
            String queryInsertUserStatus = "UPDATE samcrm_users set is_active = 1, is_loggedin = 1 WHERE ip_address = '" + ipAddress + "' "
                    + " and mobile = " + mobile;
            records = statement.executeUpdate(queryInsertUserStatus);

            if (records == 0 && userStatus == 6) {
                int vendor;
                try ( ResultSet rs = statement.executeQuery("SELECT is_vendor from samcrm_users WHERE mobile = " + mobile)) {
                    vendor = 0;
                    while (rs.next()) {
                        vendor = rs.getInt("is_vendor");
                        break;
                    }
                }
                queryInsertUserStatus = "INSERT into samcrm_users (mobile, is_active, is_vendor, is_loggedin, is_on_promotion, ip_address) "
                        + "VALUES (" + mobile + ", 1, " + vendor + ", 1, 1,'" + ipAddress + "')";
                records = statement.executeUpdate(queryInsertUserStatus);
            }
            connection.close();
        } catch (SQLException sqle) {
            Logger.getAnonymousLogger().log(Level.SEVERE, this.getClass().getName(), sqle);
        }
        return records;
    }

    @Override
    public LinkedHashMap<String, String> checkSamcrmLogin(String mobileNo, String ipAddress, String individualId) throws Exception {
        LinkedHashMap<String, String> individualData = new LinkedHashMap<>();
        Connection connection;
        Statement statement;
        try {
            connection = createConnection();
            statement = connection.createStatement();
            String queryCheckLogin = "SELECT si.individual_id, si.name, si.mobile, si.email, si.zipcode, si.profile_pic, si.profile_color, si.password, si.address, "
                    + "su.is_loggedin, su.is_vendor, svcs.vendor_category_id, svcs.vendor_subtype_id, svcs.vendor_registration_no, svcs.description, "
                    + "svcs.vendor_title, svcs.push_message, svcs.email_campaign_text, svcs.bulk_sms_text, svcs.id, "
                    + "soo.order_id, count(item_id) as cart_count "
                    + "from samcrm_individual si inner join samcrm_users su on su.mobile = si.mobile ";

            if (individualId != null && !individualId.isEmpty()) {
                queryCheckLogin = queryCheckLogin + " and si.individual_id = " + individualId;
            } else {
                queryCheckLogin = queryCheckLogin + " and su.mobile = " + mobileNo;
            }

            queryCheckLogin = queryCheckLogin + " left join samcrm_vendor_category_subtype svcs on svcs.mobile = si.mobile "
                    + "left join samcrm_obm_orders soo on soo.status = 'open' AND soo.customer_id = si.individual_id "
                    + "left join samcrm_obm_items soi on soi.order_id = soo.order_id where " //"su.ip_address = '" + ipAddress + "' and "
                    + "su.is_loggedin = 1 ";
            if (mobileNo != null && !mobileNo.isEmpty()) {
                queryCheckLogin = queryCheckLogin + " and su.mobile = " + mobileNo;
            }
            queryCheckLogin = queryCheckLogin + " group by si.mobile, su.is_vendor, svcs.vendor_category_id, svcs.vendor_subtype_id, svcs.vendor_registration_no, soo.order_id";
            try ( ResultSet result = statement.executeQuery(queryCheckLogin)) {
                while (result.next()) {
                    individualData.put("individual_id", result.getString("individual_id"));
                    String name = result.getString("name");
                    name = name.replaceAll("\\s+", "_");
                    individualData.put("name", name);
                    individualData.put("mobile", result.getString("mobile"));
                    individualData.put("email", result.getString("email"));
                    individualData.put("zipCode", result.getString("zipcode"));
                    individualData.put("profilePic", result.getString("profile_pic"));
                    individualData.put("profileColor", result.getString("profile_color"));
                    individualData.put("password", result.getString("password"));
                    individualData.put("address", result.getString("address"));
                    individualData.put("isVendor", result.getString("is_vendor"));
                    individualData.put("vendorTitle", result.getString("vendor_title"));
                    individualData.put("vendorRegistrationNo", result.getString("vendor_registration_no"));
                    individualData.put("vendorDescription", result.getString("description"));
                    individualData.put("pushMessage", result.getString("push_message"));
                    individualData.put("emailCampaignText", result.getString("email_campaign_text"));
                    individualData.put("bulkSmsText", result.getString("bulk_sms_text"));
                    individualData.put("vendorCategoryId", result.getString("vendor_category_id"));
                    individualData.put("vendorSubtypeId", result.getString("vendor_subtype_id"));
                    individualData.put("vendor_group_id", result.getString("id"));
                    individualData.put("order_id", result.getString("order_id"));
                    break;
                }
            }
            connection.close();
        } catch (SQLException sqle) {
            Logger.getAnonymousLogger().log(Level.SEVERE, this.getClass().getName(), sqle);
        }
        return individualData;
    }

    @Override
    public int logoutSamcrmUser(String mobile, String ipAddress) throws Exception {
        int records = 0;
        Connection connection;
        try {
            connection = createConnection();
            Statement statement = connection.createStatement();
            String queryInsertUserStatus = "UPDATE samcrm_users set is_loggedin = 0"//, ip_address = '" + ipAddress + "' "
                    + " WHERE mobile = " + mobile;
            records = statement.executeUpdate(queryInsertUserStatus);
            connection.close();
        } catch (SQLException sqle) {
            Logger.getAnonymousLogger().log(Level.SEVERE, this.getClass().getName(), sqle);
        }
        return records;
    }

    @Override
    public int deactivateCustomer(int vendorId, int cusomerId, int mobileNo, int isVendor) throws Exception {
        int records = 0;
        Connection connection;
        try {
            connection = createConnection();
            Statement statement = connection.createStatement();
            String queryInsertUserStatus = "UPDATE samcrm_users set is_active = 0 AND is_loggedin = 0 WHERE is_vendor = " + isVendor + " AND mobile = " + mobileNo;
            records = statement.executeUpdate(queryInsertUserStatus);
            connection.close();
        } catch (SQLException sqle) {
            Logger.getAnonymousLogger().log(Level.SEVERE, this.getClass().getName(), sqle);
        }
        return records;
    }

    @Override
    public UserDataBean selectUserData(long user_id) throws Exception {

        UserDataBean userData = null;
        Connection connection;
        try {
            connection = createConnection();
            Statement statement = connection.createStatement();
            String selectQuery = "SELECT si.individual_id, si.name, si.email, si.mobile, si.zipcode, si.address, si.creation_date, si.country_code, si.date_of_birth "
                    + "FROM samcrm_individual si join samcrm_users su on si.mobile = su.mobile su.is_active = 1 where si.individual_id = " + user_id;
            try ( ResultSet result = statement.executeQuery(selectQuery)) {
                while (result.next()) {
                    userData = new UserDataBean();
                    userData.setIndividual_id(result.getString("individual_id"));
                    userData.setName(result.getString("name"));
                    userData.setEmail(result.getString("email"));
                    userData.setMobile(result.getString("mobile"));
                    userData.setZipcode(result.getString("zipcode"));
                    userData.setAddress(result.getString("address"));
                    userData.setCreation_date(result.getString("creation_date"));
                    userData.setCountry_code(result.getString("country_code"));
                    userData.setDob(result.getString("date_of_birth"));
                }
            }
            connection.close();
        } catch (SQLException sqle) {
        }
        return userData;
    }

    @Override
    public boolean checkLogin(String userName, long password) throws Exception {
        boolean isUserAvalable = Boolean.FALSE;
        Connection connection;
        Statement statement;
        ResultSet result;
        try {

            connection = createConnection();
            statement = connection.createStatement();
            String queryCheckLogin = "SELECT * FROM users WHERE email = '" + userName + "' "
                    + "AND mobile = " + password;
            result = statement.executeQuery(queryCheckLogin);
            while (result.next()) {
                isUserAvalable = Boolean.TRUE;
                break;
            }
            result.close();
            connection.close();
        } catch (SQLException sqle) {
        }
        return isUserAvalable;
    }

    @Override
    public boolean insertUser(String username, String password, String email, long mobileNo) throws Exception {
        boolean insertStatus = Boolean.FALSE;
        Connection connection;
        try {

            connection = createConnection();
            Statement statement = connection.createStatement();
            String queryInsertUser = "INSERT into users (username, password, creation_date, email, country_code, mobile) "
                    + "VALUES ('" + username + "','" + password + "','" + FORMATTER.format(new Date()) + "', '" + email + "', 091, " + mobileNo + ")";
            int records = statement.executeUpdate(queryInsertUser);
            if (records > 0) {
                insertStatus = Boolean.TRUE;
            }
            connection.close();
        } catch (SQLException sqle) {
        }
        return insertStatus;
    }

    @Override
    public boolean insertInquiry(String inquiryname, int inspirationid, String email, long mobileNo, int levelid, String address, String followupDetails, String nationality, String fname, String mname, String dob, long telOther, String image, String gender, String inspiration, String comfortability, String primaryskill) throws Exception {
        boolean insertStatus = Boolean.FALSE;
        Connection connection;
        ResultSet rs;
        try {

            connection = createConnection();
            Statement statement = connection.createStatement();
            String queryInsertInquiry = "INSERT into inquiry (firstname, inspiration_id, inquiry_date, email, mobile"
                    + ", level_id, address_line1, nationality, father_name, mother_name, date_of_birth, telephone, photo, gender, inspiration, comfortability, primaryskill) "
                    + "VALUES ('" + inquiryname + "'," + inspirationid + ",'" + FORMATTER.format(new Date()) + "', '" + email + "', " + mobileNo + ","
                    + levelid + ", '" + address + "', '" + nationality + "', '" + fname + "', '" + mname + "', '" + FORMATTER.format(new SimpleDateFormat("dd/MM/yyyy").parse(dob)) + "', " + telOther + ", '" + image + "', '"
                    + gender + "', '" + inspiration + "', '" + comfortability + "', '" + primaryskill + "')";
            int records = statement.executeUpdate(queryInsertInquiry);
            if (records > 0) {
                statement = connection.createStatement();
                rs = statement.executeQuery("SELECT MAX(inquiry_id) from inquiry");
                int inquiry_id = 0;
                while (rs.next()) {
                    inquiry_id = rs.getInt(1);
                }
                rs.close();
                statement = connection.createStatement();
                String queryInsertFollowupDetails = "INSERT into followupdetails (inquiry_id, inquirystatus_id, followup_details, followup_date) "
                        + "VALUES (" + inquiry_id + ", 1, '" + followupDetails + "','" + FORMATTER.format(new Date()) + "')";
                records = statement.executeUpdate(queryInsertFollowupDetails);
                if (records > 0) {
                    insertStatus = Boolean.TRUE;
                }
            }
            connection.close();
        } catch (SQLException sqle) {
        }
        return insertStatus;
    }

    @Override
    public LinkedHashMap<Integer, String> selectLevel() throws Exception {
        LinkedHashMap<Integer, String> levelMap = new LinkedHashMap<>();
        Connection connection;
        ResultSet result;
        try {
            connection = createConnection();
            Statement statement = connection.createStatement();
            String querySelectLevel = "SELECT level_id, levelname FROM level order by level_id";
            result = statement.executeQuery(querySelectLevel);
            while (result.next()) {
                int key = result.getInt("level_id");
                String value = result.getString("levelname");
                levelMap.put(key, value);
            }
            result.close();
            connection.close();
        } catch (SQLException sqle) {
        }
        return levelMap;
    }

    @Override
    public LinkedHashMap<Integer, String> selectInspiration() throws Exception {
        LinkedHashMap<Integer, String> inspirationMap = new LinkedHashMap<>();
        Connection connection;
        ResultSet result;
        try {
            connection = createConnection();
            Statement statement = connection.createStatement();
            String querySelectInspiration = "SELECT inspiration_id, inspirationname FROM inspiration";
            result = statement.executeQuery(querySelectInspiration);
            while (result.next()) {
                int key = result.getInt("inspiration_id");
                String value = result.getString("inspirationname");
                inspirationMap.put(key, value);
            }
            result.close();
            connection.close();
        } catch (SQLException sqle) {
        }
        return inspirationMap;
    }

    @Override
    public ArrayList<InquiryBean> listInquiry() throws Exception {
        ArrayList<InquiryBean> inquiryList = new ArrayList<>();
        Connection connection;
        ResultSet result;
        try {

            connection = createConnection();
            Statement statement = connection.createStatement();
            String querySelectInquiry = "select i.inquiry_id, i.firstname, i.inquiry_date, i.email, i.mobile, i.address_line1, "
                    + "i.inspiration_id, i.level_id, f.followup_details, f.inquirystatus_id, "
                    + "s.label_text, s.label_color from inquiry i join followupdetails f on i.inquiry_id = f.inquiry_id "
                    + "join inquirystatusmaster s on f.inquirystatus_id = s.inquirystatus_id "
                    + "where f.followup_id = (select max(d.followup_id) from followupdetails d where d.inquiry_id = i.inquiry_id)";
            result = statement.executeQuery(querySelectInquiry);
            while (result.next()) {
                InquiryBean inquiryBean = new InquiryBean();
                inquiryBean.setInquiry_id(result.getInt("inquiry_id"));
                inquiryBean.setFirstname(result.getString("firstname"));
                inquiryBean.setInquiry_date(result.getDate("inquiry_date"));
                inquiryBean.setEmail(result.getString("email"));
                inquiryBean.setMobile(result.getLong("mobile"));
                inquiryBean.setAddress_line1(result.getString("address_line1"));
                inquiryBean.setInspiration_id(result.getInt("inspiration_id"));
                inquiryBean.setLevel_id(result.getInt("level_id"));
                inquiryBean.setFollowup_details(result.getString("followup_details"));
                inquiryBean.setInquirystatus_id(result.getInt("inquirystatus_id"));
                inquiryBean.setLabel_text(result.getString("label_text"));
                inquiryBean.setLabel_color(result.getString("label_color"));
                inquiryList.add(inquiryBean);
                //inquiryList.add(result.getString("email")+"<x>"+result.getString("mobile"));
            }
            result.close();
            connection.close();
        } catch (SQLException sqle) {
        }
        return inquiryList;
    }

    @Override
    public boolean updateInquiry(int inquiry_id, String inquiryname, int inspirationid, String email, long mobileNo, int levelid, String address, String followupDetails, String nationality, String fname, String mname, String dob, long telOther, String image, String gender, String inspiration, String comfortability, String primaryskill) throws Exception {
        boolean updateStatus = Boolean.FALSE;
        Connection connection;
        ResultSet rs;
        try {
            connection = createConnection();
            Statement statement = connection.createStatement();
            String queryUpdateInquiry = "UPDATE inquiry SET firstname = '" + inquiryname + "', email = '" + email + "', mobile = " + mobileNo
                    + ", address_line1 =  '" + address + "', inspiration_id = " + inspirationid + " , level_id = " + levelid + " where inquiry_id = " + inquiry_id;
            int records = statement.executeUpdate(queryUpdateInquiry);
            if (records > 0) {
                statement = connection.createStatement();
                rs = statement.executeQuery("SELECT MAX(followup_id) from followupdetails where inquiry_id = " + inquiry_id);
                int followup_id = 0;
                while (rs.next()) {
                    followup_id = rs.getInt(1);
                }
                rs.close();
                if (followup_id > 0 && followupDetails != null && !followupDetails.isEmpty()) {
                    statement = connection.createStatement();
                    String queryUpdateFollowupDetails = "UPDATE followupdetails SET followup_details = '" + followupDetails + "' "
                            + " where followup_id = " + followup_id;
                    statement.executeUpdate(queryUpdateFollowupDetails);
                    updateStatus = Boolean.TRUE;
                } else {
                    updateStatus = updateFollowup(inquiry_id, 1, followupDetails);
                }
            }
            connection.close();
        } catch (SQLException sqle) {
        }
        return updateStatus;
    }

    @Override
    public LinkedHashMap<Integer, String> selectInquiryStatus() throws Exception {
        LinkedHashMap<Integer, String> inquiryStatusMap = new LinkedHashMap<>();
        Connection connection;
        ResultSet result;
        try {
            connection = createConnection();
            Statement statement = connection.createStatement();
            String querySelectLevel = "SELECT inquirystatus_id, label_text FROM inquirystatusmaster order by inquirystatus_id";
            result = statement.executeQuery(querySelectLevel);
            while (result.next()) {
                int key = result.getInt("inquirystatus_id");
                String value = result.getString("label_text");
                inquiryStatusMap.put(key, value);
            }
            result.close();
            connection.close();
        } catch (SQLException sqle) {
        }
        return inquiryStatusMap;
    }

    @Override
    public boolean updateFollowup(int inquiry_id, int inquirystatus_id, String followupDetails) throws Exception {
        boolean updateStatus = Boolean.FALSE;
        Connection connection;
        try {
            connection = createConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT into followupdetails (inquiry_id, inquirystatus_id, followup_details, followup_date) "
                    + "VALUES (" + inquiry_id + ", " + inquirystatus_id + ", '" + followupDetails + "','" + FORMATTER.format(new Date()) + "')");
            updateStatus = Boolean.TRUE;
            connection.close();
        } catch (SQLException sqle) {
        }
        return updateStatus;
    }

    @Override
    public InquiryBean getInquiryById(int inquiryId) throws Exception {
        InquiryBean inquiryBean = null;
        Connection connection;
        ResultSet result;
        try {

            connection = createConnection();
            Statement statement = connection.createStatement();
            String querySelectInquiry = "select i.firstname, i.inquiry_date, i.email, i.mobile from inquiry i where i.inquiry_id = " + inquiryId;
            result = statement.executeQuery(querySelectInquiry);
            while (result.next()) {
                inquiryBean = new InquiryBean();
                inquiryBean.setFirstname(result.getString("firstname"));
                inquiryBean.setInquiry_date(result.getDate("inquiry_date"));
                inquiryBean.setEmail(result.getString("email"));
                inquiryBean.setMobile(result.getLong("mobile"));
            }
            result.close();
            connection.close();
        } catch (SQLException sqle) {
        }
        return inquiryBean;
    }

    @Override
    public ArrayList<SliderImageBean> listSliderImage() {
        ArrayList<SliderImageBean> sliderImageList = new ArrayList<>();
        int imageId = 1;
        do {
            SliderImageBean bean = new SliderImageBean();
            bean.setId(String.valueOf(imageId));
            bean.setTitle("SliderImage " + imageId);
            bean.setDescription("This is Slider Image example!");
            if (imageId == 1) {
                bean.setImage_url("http:\\/\\/localhost:8888\\/images\\/stories\\/facebookIcon.png");
            }
            if (imageId == 2) {
                bean.setImage_url("http:\\/\\/localhost:8888\\/images\\/stories\\/facebookIcon.png");
            }
            if (imageId == 3) {
                bean.setImage_url("http:\\/\\/localhost:8888\\/images\\/stories\\/facebookIcon.png");
            }
            sliderImageList.add(bean);
            imageId++;
        } while (imageId < 4);
        return sliderImageList;
    }

    @Override
    public int selectInquiry(String email, long mobileNo) throws Exception {
        int inquiry_id = 0;
        Connection connection;
        Statement statement;
        ResultSet result;
        try {
            connection = createConnection();
            statement = connection.createStatement();
            String querySelectInquiry = "select inquiry_id from inquiry WHERE email = '" + email + "' AND mobile = " + mobileNo;
            result = statement.executeQuery(querySelectInquiry);
            while (result.next()) {
                inquiry_id = result.getInt("inquiry_id");
            }
            result.close();
            statement.close();
            connection.close();
        } catch (SQLException sqle) {
        }
        return inquiry_id;
    }

    @Override
    public InquiryBean getInquiryDetails(String email, long mobileNo) throws Exception {
        Connection connection = null;
        ResultSet result = null;
        InquiryBean inquiryBean = null;
        try {
            connection = createConnection();
            Statement statement = connection.createStatement();
            String querySelectInquiry = "select i.inquiry_id, i.firstname, i.inquiry_date, i.email, i.mobile, i.address_line1, "
                    + "i.inspiration_id, i.level_id, i.nationality, i.father_name, i.mother_name, i.date_of_birth, i.telephone, i.photo, i.gender, i.inspiration, i.comfortability, i.primaryskill, "
                    + "f.followup_details, f.inquirystatus_id, "
                    + "s.label_text, s.label_color from inquiry i join followupdetails f on i.inquiry_id = f.inquiry_id "
                    + "join inquirystatusmaster s on f.inquirystatus_id = s.inquirystatus_id "
                    + "where f.followup_id = (select max(d.followup_id) from followupdetails d where d.inquiry_id = i.inquiry_id)"
                    + " AND i.email = '" + email + "' AND i.mobile = " + mobileNo;
            result = statement.executeQuery(querySelectInquiry);
            while (result.next()) {
                inquiryBean = new InquiryBean();
                inquiryBean.setInquiry_id(result.getInt("inquiry_id"));
                inquiryBean.setFirstname(result.getString("firstname"));
                inquiryBean.setInquiry_date(result.getDate("inquiry_date"));
                inquiryBean.setEmail(result.getString("email"));
                inquiryBean.setMobile(result.getLong("mobile"));
                inquiryBean.setAddress_line1(result.getString("address_line1"));
                inquiryBean.setInspiration_id(result.getInt("inspiration_id"));
                inquiryBean.setLevel_id(result.getInt("level_id"));
                inquiryBean.setFollowup_details(result.getString("followup_details"));
                inquiryBean.setInquirystatus_id(result.getInt("inquirystatus_id"));
                inquiryBean.setLabel_text(result.getString("label_text"));
                inquiryBean.setLabel_color(result.getString("label_color"));

                inquiryBean.setNationality(result.getString("nationality"));
                inquiryBean.setFather_name(result.getString("father_name"));
                inquiryBean.setMother_name(result.getString("mother_name"));
                inquiryBean.setDate_of_birth(result.getDate("date_of_birth"));
                inquiryBean.setTelephone(result.getLong("telephone"));
                if (result.getBlob("photo") != null) {
                    inquiryBean.setPhoto(result.getBlob("photo").toString());
                }
                if (result.getBlob("gender") != null) {
                    inquiryBean.setGender(result.getString("gender").charAt(0));
                }
                inquiryBean.setInspiration(result.getString("inspiration"));
                inquiryBean.setComfortability(result.getString("comfortability"));
                inquiryBean.setPrimaryskill(result.getString("primaryskill"));
                break;
            }

        } catch (SQLException sqle) {
        } finally {
            if (result != null) {
                result.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return inquiryBean;
    }

}
