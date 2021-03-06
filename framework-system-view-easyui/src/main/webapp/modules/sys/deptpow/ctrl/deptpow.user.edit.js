define(function(require) {
	var Config = require('config');
	var Core = require('core/core');
	
	var Page = require('core/page');
	
	// 编辑机构用户
	var DeptPowUserEdit = Page.extend(function() {
		var _self = this;
		
		// @override
		this.load = function(panel, data) {
			
			// 保存原始信息
			this.oldData = data;
			
			var form = panel.find('form');
			Core.ajax(Config.ContextPath+'system/userunit/'+data.unitCode+'/'+data.userCode, {
				data: {
					userStation: data.userStation,
	                userRank: data.userRank
				},
				method: 'get'
			}).then(function(data) {
				_self.data = data;
				
				form.form('disableValidation')
					.form('load', data)
					.form('readonly', 'userCode')
					.form('focus');
			});
		};
		
		// @override
		this.submit = function(panel, data, closeCallback) {
			var form = panel.find('form');
			
			form.form('enableValidation');
			var isValid = form.form('validate');
			
			if (isValid) {
				// 原始职位和岗位
				data.oldUserStation = this.oldData.userStation;
				data.oldUserRank = this.oldData.userRank;
			
				form.form('ajax', {
					url: Config.ContextPath+'system/userunit/'+data.unitCode+'/'+data.userCode,
					method: 'put',
					data: data
				}).then(closeCallback);
			}
			
			return false;
		};
		
		// @override
		this.onClose = function(table) {
			table.datagrid('reload');
		};
	});
	
	return DeptPowUserEdit;
});