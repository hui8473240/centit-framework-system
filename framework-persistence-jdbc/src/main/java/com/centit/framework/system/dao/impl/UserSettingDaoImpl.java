package com.centit.framework.system.dao.impl;

import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.core.dao.CodeBook;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import com.centit.framework.system.dao.UserSettingDao;
import com.centit.framework.system.po.UserSetting;
import com.centit.framework.system.po.UserSettingId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("userSettingDao")
public class UserSettingDaoImpl extends BaseDaoImpl<UserSetting, UserSettingId> implements UserSettingDao {

    public static final Logger logger = LoggerFactory.getLogger(UserSettingDaoImpl.class);

    public Map<String, String> getFilterField() {
        if (filterField == null) {
            filterField = new HashMap<String, String>();

            filterField.put(CodeRepositoryUtil.USER_CODE, "cid.userCode = :userCode");

            filterField.put("paramCode", "cid.paramCode = :paramCode");

            filterField.put("paramValue", CodeBook.LIKE_HQL_ID);

            filterField.put("paramClass", CodeBook.LIKE_HQL_ID);

            filterField.put("paramName", CodeBook.LIKE_HQL_ID);

            filterField.put("createDate", CodeBook.LIKE_HQL_ID);

        }
        return filterField;
    }
    
    @Transactional
    public List<UserSetting> getUserSettingsByCode(String userCode) {
        return listObjects("From UserSetting where cid.userCode=?",userCode);
    }
    
    @Transactional
    public List<UserSetting> getUserSettings(String userCode,String optID) {
        return listObjects("From UserSetting where cid.userCode=? and optId= ?",
                new Object[]{userCode,optID});
    }
    
    @Transactional
    public void saveUserSetting(UserSetting us){
    	super.mergeObject(us);
    }
}