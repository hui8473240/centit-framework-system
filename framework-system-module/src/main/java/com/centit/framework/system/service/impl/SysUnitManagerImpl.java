package com.centit.framework.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.centit.framework.components.CodeRepositoryCache;
import com.centit.framework.core.dao.QueryParameterPrepare;
import com.centit.framework.system.dao.UnitInfoDao;
import com.centit.framework.system.dao.UserUnitDao;
import com.centit.framework.system.po.UnitInfo;
import com.centit.framework.system.po.UserInfo;
import com.centit.framework.system.service.SysUnitManager;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.database.utils.PageDesc;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;

@Service("sysUnitManager")
@Transactional
public class SysUnitManagerImpl implements SysUnitManager {

    public static Logger logger = LoggerFactory.getLogger(SysUnitManagerImpl.class);
    @Resource
    @NotNull
    private UserUnitDao userUnitDao;

    @Resource
    @NotNull
    protected UnitInfoDao unitInfoDao;

    @Override
    public List<UnitInfo> listObjectsAsSort(Map<String, Object> searchColumn) {
        List<UnitInfo> listObjects = unitInfoDao.listObjects(searchColumn);
        Iterator<UnitInfo> unitInfos = listObjects.iterator();

        while (unitInfos.hasNext()) {
            UnitInfo unitInfo = unitInfos.next();
            if (StringBaseOpt.isNvl(unitInfo.getParentUnit()) || "0".equals(unitInfo.getParentUnit())) {
                continue;
            }
            for (UnitInfo opt : listObjects) {
                if (opt.getUnitCode().equals(unitInfo.getParentUnit())) {
                    opt.getSubUnits().add(unitInfo);
                    break;
                }
            }
        }
        // 获取顶级的父级菜单
        List<UnitInfo> parentUnit = new ArrayList<>();
        for (UnitInfo unitInfo : listObjects) {
            if (StringBaseOpt.isNvl(unitInfo.getParentUnit()) || "0".equals(unitInfo.getParentUnit())) {
                parentUnit.add(unitInfo);
            }
        }
        return parentUnit;
    }

    /**
     * 查找对象，如果没有新建一个空对象，并附一个默认的编码
     * @param  object UnitInfo
     * @return UnitInfo
     */
    public UnitInfo getObject(UnitInfo object) {
        UnitInfo newObj = unitInfoDao.getObjectById(object.getUnitCode());
        if (newObj == null) {
            newObj = object;
            newObj.setUnitCode(unitInfoDao.getNextKey());
            newObj.setIsValid("T");
        }
        return newObj;
    }

    @Override
    @Transactional
    public List<UserInfo> getUnitUsers(String unitCode) {
        return unitInfoDao.listUnitUsers(unitCode);
    }

    @Override
    public UnitInfo getUnitByName(String name) {
        return unitInfoDao.getUnitByName(name);
    }

    @Override
    @Transactional
    public void changeStatus(String unitCode, String isValid) {
        List<UnitInfo> allSubUnits = listAllSubUnits(unitCode);
        for (UnitInfo subUnit : allSubUnits) {
            subUnit.setIsValid(isValid);
            unitInfoDao.updateUnit(subUnit);
        }
        CodeRepositoryCache.evictCache("UnitInfo");
    }


    @Override
    @Transactional
    public void deleteUnitInfo(UnitInfo unitinfo){
        String oldUnitPath = unitinfo.getUnitPath();
        List<UnitInfo> subUnits = unitInfoDao.listSubUnitsByUnitPaht(oldUnitPath);
        //子机构
        for(UnitInfo ui : subUnits){
            ui.setParentUnit(unitinfo.getParentUnit());
            ui.setUnitPath(unitinfo.getUnitPath());
            unitInfoDao.updateUnit(ui);
        }

        unitInfoDao.deleteObjectById(unitinfo.getUnitCode());
        CodeRepositoryCache.evictCache("UnitInfo");
    }

    @Value("${framework.unitinfo.id.generator:}")
    protected String userIdFormat;

    @Override
    @Transactional
    public String saveNewUnitInfo(UnitInfo unitinfo){
        String unitCode = unitInfoDao.getNextKey();

        if(StringUtils.isBlank(userIdFormat)){
            unitCode = StringBaseOpt.midPad(unitCode,6,"D",'0');
        }else{
          //{"prefix":"U","length":8,"pad":"0"}
            JSONObject idFormat = (JSONObject) JSON.parse(userIdFormat);
            if(idFormat!=null) {
                unitCode = StringBaseOpt.midPad(unitCode,
                    NumberBaseOpt.castObjectToInteger(idFormat.get("length"), 1),
                    idFormat.getString("prefix"),
                    idFormat.getString("pad"));
            }
        }

        unitinfo.setUnitCode(unitCode);
        UnitInfo parentUnit = unitInfoDao.getObjectById(unitinfo.getParentUnit());

        if (parentUnit == null) {
          unitinfo.setUnitPath("/" + unitinfo.getUnitCode() + "/");
        } else {
          unitinfo.setUnitPath(parentUnit.getUnitPath() + "/" + unitinfo.getUnitCode() + "/");
        }

        unitInfoDao.saveNewObject(unitinfo);
        CodeRepositoryCache.evictCache("UnitInfo");
        return unitinfo.getUnitCode();
    }

    @Override
    @Transactional
    public boolean isUniqueName(UnitInfo unitInfo){
        UnitInfo dbUnitInfo = unitInfoDao.getPeerUnitByName(
                unitInfo.getUnitName(), unitInfo.getParentUnit(), unitInfo.getUnitCode());
        return dbUnitInfo == null ? true : false;
    }

    @Override
    @Transactional
    public boolean isUniqueOrder(UnitInfo unitInfo){
        Integer exists = unitInfoDao.isExistsUnitByParentAndOrder(
            unitInfo.getParentUnit(), unitInfo.getUnitOrder());
        return exists == null || exists < 1;
    }

    @Override
    @Transactional
    public void updateUnitInfo(UnitInfo unitinfo){
        UnitInfo dbUnitInfo = unitInfoDao.getObjectById(unitinfo.getUnitCode());
        String oldUnitPath = dbUnitInfo.getUnitPath();

        dbUnitInfo.copyNotNullProperty(unitinfo);

        if(!StringUtils.equals(dbUnitInfo.getParentUnit(), unitinfo.getParentUnit())){
            UnitInfo parentUnit = unitInfoDao.getObjectById(unitinfo.getParentUnit());
            if(parentUnit==null) {
                unitinfo.setUnitPath("/"+unitinfo.getUnitCode()+"/");
            }
            else {
                unitinfo.setUnitPath(parentUnit.getUnitPath() + "/" + unitinfo.getUnitCode() + "/");
            }
            List<UnitInfo> subUnits = unitInfoDao.listSubUnitsByUnitPaht(oldUnitPath);
            int noupl = oldUnitPath.length();
            for(UnitInfo ui : subUnits){
                ui.setParentUnit(unitinfo.getUnitPath()+ ui.getUnitPath().substring(noupl));
                unitInfoDao.updateUnit(ui);
            }
        }
        unitInfoDao.updateUnit(dbUnitInfo);
        CodeRepositoryCache.evictCache("UnitInfo");
    }

    @Override
    @Transactional
    public List<UnitInfo> listAllSubObjectsAsSort(String primaryUnit) {
        List<UnitInfo> listObjects = listAllSubUnits(primaryUnit);
        Iterator<UnitInfo> unitInfos = listObjects.iterator();
        while (unitInfos.hasNext()) {
            UnitInfo unitInfo = unitInfos.next();
            if (StringBaseOpt.isNvl(unitInfo.getParentUnit()) || "0".equals(unitInfo.getParentUnit())) {
                continue;
            }
            for (UnitInfo opt : listObjects) {
                if (opt.getUnitCode().equals(unitInfo.getParentUnit())) {
                    opt.getSubUnits().add(unitInfo);

                    break;
                }
            }
        }
        List<UnitInfo> parentUnit = new ArrayList<>();
        // 获取顶级的父级菜单
        for (UnitInfo unitInfo : listObjects) {
            if (StringBaseOpt.isNvl(unitInfo.getParentUnit()) ||primaryUnit.equals(unitInfo.getUnitCode())) {
                parentUnit.add(unitInfo);
            }
        }
        return parentUnit;
    }

    @Override
    @Transactional
    public List<UnitInfo> listObjects(Map<String, Object> filterMap) {
        return unitInfoDao.listObjects(filterMap);
    }

    @Override
    @Transactional
    public List<UnitInfo> listObjects(Map<String, Object> filterMap, PageDesc pageDesc) {
        return unitInfoDao.pageQuery(
            QueryParameterPrepare.makeMybatisOrderByParam(
                QueryParameterPrepare.prepPageParams(filterMap,pageDesc,unitInfoDao.pageCount(filterMap)),UnitInfo.class));
    }

    @Override
    @Transactional
    public UnitInfo getObjectById(String unitCode) {
        UnitInfo unitInfo = unitInfoDao.getObjectById(unitCode);
        return unitInfo;
    }

    @Override
    @Transactional
    public void checkState(List<UnitInfo> listObjects) {
       // List<String> objs = (List<String>) DatabaseOptUtils.findObjectsBySql(baseDao, "select distinct t.parentunit from f_unitinfo t ");
        List<String> objs = unitInfoDao.getAllParentUnit();
        if(objs!=null && objs.size()>0){
            for (UnitInfo u : listObjects){
                if(objs.contains(u.getUnitCode())) {
                  u.setState("closed");
                } else {
                  u.setState("open");
                }
            }
        }else{
            for (UnitInfo u : listObjects){
                u.setState("open");
            }
        }
    }

    @Override
    @Transactional
    public List<UnitInfo> listValidSubUnit(String unitCode){
        Map<String, Object> map = new HashMap<>();
        map.put("parentUnit", unitCode);
        map.put("isValid", "T");
        return unitInfoDao.listObjects(map);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitInfo> listAllSubUnits(String unitCode) {

        UnitInfo unitInfo = unitInfoDao.getObjectById(unitCode);
        return  unitInfoDao.listSubUnitsByUnitPaht(unitInfo != null ? unitInfo.getUnitPath() : null);
    }

    /**
     * 查询启用状态的下级机构
     * @param unitCode 当前机构代码
     */
    @Override
    @Transactional(readOnly = true)
    public List<UnitInfo> listValidSubUnits(String unitCode){
        Map<String, Object> filterMap = new HashMap<>(4);
        UnitInfo unitInfo = unitInfoDao.getObjectById(unitCode);
        filterMap.put("unitPath", unitInfo.getUnitPath());
        filterMap.put("isValid", "T");
        return unitInfoDao.listObjects(filterMap);
    }

    @Override
    public boolean isDepNoUnique(String depNo, String unitCode) {
        Map<String, Object> filterMap = new HashMap<>(2);
        filterMap.put("depNo", depNo);
        List<UnitInfo> list = unitInfoDao.listObjects(filterMap);
        return list == null || list.size() == 0 || (unitCode != null && unitCode.equals(list.get(0).getUnitCode()));
    }

    @Override
    public boolean isUnitWordUnique(String unitWord, String unitCode) {
        Map<String, Object> filterMap = new HashMap<>(2);
        filterMap.put("unitWord", unitWord);
        List<UnitInfo> list = unitInfoDao.listObjects(filterMap);
        return list == null || list.size() == 0 || (unitCode != null && unitCode.equals(list.get(0).getUnitCode()));
    }
}
