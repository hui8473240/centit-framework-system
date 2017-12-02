define(function(require) {
  var Config = require('config');
  var RoleController = require('modules/sys/roleinfo/ctrl/roleinfo');
  var Cache = require('core/cache');

	return RoleController.extend(function() {

	  // 部门角色只查询主部门下的角色
    var loginUser = Cache.get('loginuser');
    this.queryRoleUrl = Config.ContextPath + "system/roleinfo/unit/" + loginUser.primaryUnit;

    this.validateRoleNameWhenAdd = Config.ContextPath + 'system/roleinfo/isunitroleunique/' + loginUser.primaryUnit + '/{{roleName}}';

    this.validateRoleNameWhenEdit = Config.ContextPath + 'system/roleinfo/isunitroleunique/' + loginUser.primaryUnit + '/{{roleName}}/{{roleCode}}';

    // 新增部门角色的roleType必须为 D
    this.doDeptRoleAdd = function(ctrl) {
      ctrl.panel.find('.roleType').remove();
      ctrl.object.roleType = 'D';
    }

	});

});
