package com.centit.framework.system.dao.jdbcimpl;

import com.centit.framework.core.dao.CodeBook;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import com.centit.framework.jdbc.dao.DatabaseOptUtils;
import com.centit.framework.system.dao.OptDataScopeDao;
import com.centit.framework.system.po.OptDataScope;
import com.centit.support.algorithm.CollectionsOpt;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository("optDataScopeDao")
public class OptDataScopeDaoImpl extends BaseDaoImpl<OptDataScope, String> implements OptDataScopeDao {

    public Map<String, String> getFilterField() {
        if (filterField == null) {
            filterField = new HashMap<>();
            filterField.put("optId", CodeBook.EQUAL_HQL_ID);
            filterField.put("optScopeCode", CodeBook.EQUAL_HQL_ID);
            filterField.put("scopeName", CodeBook.LIKE_HQL_ID);
        }
        return filterField;
    }

    @Transactional
    public List<OptDataScope> getDataScopeByOptID(String sOptID) {
        return this.listObjectsByProperty("optId", sOptID);
    }

    @Transactional
    public int getOptDataScopeSumByOptID(String sOptID) {

        return this.pageCount(
                CollectionsOpt.createHashMap("optId", sOptID));
    }


    @Transactional
    public void deleteDataScopeOfOptID(String sOptID) {
        this.deleteObjectsByProperties(
                CollectionsOpt.createHashMap("optId", sOptID));
    }


    @Transactional
    public String getNextOptCode() {
        Long nextValue = DatabaseOptUtils.getSequenceNextValue(this, "S_OPTDEFCODE");
        return nextValue==null?"":String.valueOf(nextValue);
    }


    @Transactional
    public List<String> listDataFiltersByIds(Collection<String> scopeCodes) {
        List<OptDataScope> objs    = this.listObjectsByFilter(
                "WHERE OPT_SCOPE_CODE in (:optScopeCode)",
                CollectionsOpt.createHashMap("optScopeCode", scopeCodes));
        if(objs==null)
            return null;
        List<String> filters = new ArrayList<>();
        for(OptDataScope scope:objs)
            filters.add(scope.getFilterCondition());
        return filters;
    }

    @Override
    public void updateOptDataScope(OptDataScope optDataScope){
        super.updateObject(optDataScope);
    }

    @Override
    public void saveNewOPtDataScope(OptDataScope optDataScope){
        super.saveNewObject(optDataScope);
    }

}
