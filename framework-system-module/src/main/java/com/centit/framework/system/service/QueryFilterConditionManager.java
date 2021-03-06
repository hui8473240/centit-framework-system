package com.centit.framework.system.service;

import com.alibaba.fastjson.JSONArray;
import com.centit.framework.system.po.QueryFilterCondition;
import com.centit.support.database.utils.PageDesc;

import java.util.List;
import java.util.Map;

/**
 * QueryFilterCondition  Service.
 * create by scaffold 2016-03-01
 * @author codefan@sina.com
 * 系统内置查询方式null
*/

public interface QueryFilterConditionManager
{

    public List<QueryFilterCondition> listObjects(Map<String, Object> filterMap, PageDesc pageDesc);

    public QueryFilterCondition getObjectById(Long filterNo);

    public void mergeObject(QueryFilterCondition userQueryFilter);

    public void deleteObjectById(Long filterNo);

    public Long saveNewObject(QueryFilterCondition userQueryFilter);

    public JSONArray listQueryFilterConditionsAsJson(
            String[] fields,
            Map<String, Object> filterMap, PageDesc pageDesc);
}
