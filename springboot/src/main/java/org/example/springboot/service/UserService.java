package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.*;
import org.example.springboot.enumClass.AccountStatus;
import org.example.springboot.mapper.*;
import org.example.springboot.util.JwtTokenUtils;
import org.example.springboot.util.MenusUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private MenuMapper menuMapper;
    @Value("${user.defaultPassword}")
    private String DEFAULT_PWD;

    @Resource
    private OwnerMapper ownerMapper;

    @Resource
    private HousekeeperMapper houseKeeperMapper;

@Resource
private MaintenanceStaffMapper maintenanceStaffMapper;

    @Resource
    private PasswordEncoder bCryptPasswordEncoder;

    public User getByPhone(String phone){
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getPhone, phone);
        return userMapper.selectOne(queryWrapper);
    }

    public Result<?> login(User user){
        User compare = getByUsername(user.getUsername());
        if(compare==null)return Result.error("-1","用户不存在");
        if(compare.getStatus().equals(AccountStatus.PENDING_REVIEW.getValue()))return Result.error("-1","账号正在审核");
        if(compare.getStatus().equals(AccountStatus.REVIEW_FAILED.getValue()))return Result.error("-2","账号审核不通过，请修改个人信息",compare);
        if(compare != null && bCryptPasswordEncoder.matches(user.getPassword(), compare.getPassword())){
            List<Menu> roleMenuList = menuMapper.selectList(null);
            String token = JwtTokenUtils.genToken(String.valueOf(compare.getId()), compare.getPassword());
            compare.setMenuList(MenusUtils.allocMenus(roleMenuList,compare.getRole()));
            compare.setToken(token);
            return Result.success(compare);
        }
        return Result.error("-1","用户名或密码错误",null);

    }
    public List<User> getUserByRole(String role) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getRole, role);
        return userMapper.selectList(queryWrapper);
    }
    public int createUser(User user) {
        if (userMapper.selectOne(new QueryWrapper<User>().eq("username", user.getUsername())) != null) {
            return -1;
        }
        if(userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, user.getPhone()))!=null){
            return -2;
        }
        user.setPassword(StringUtils.isNotBlank(user.getPassword()) ? user.getPassword() : DEFAULT_PWD);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRole(StringUtils.isNotBlank(user.getRole()) ? user.getRole() : "USER");
        return userMapper.insert(user);
    }
    public Result<?> deleteUserById(int id) {
        String relation = checkRelation(id);
        if(relation!=null){
            return Result.error("-1",relation);
        }   
        return userMapper.deleteById(id) > 0 ? Result.success() : Result.error("-1","删除失败") ;
    }
    public boolean updateUser(Long id, User user) {
        // 检查要更新的用户是否存在
        User existUser = userMapper.selectById(id);
        if (existUser == null) {
            return false;
        }
        
        // 检查用户名是否已被其他用户使用
        if (StringUtils.isNotBlank(user.getUsername())) {
            User userByUsername = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
                    .ne(User::getId, id)  // 排除当前用户
            );
            if (userByUsername != null) {
                return false;
            }
        }
        
        // 检查手机号是否已被其他用户使用
        if (StringUtils.isNotBlank(user.getPhone())) {
            User userByPhone = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, user.getPhone())
                    .ne(User::getId, id)  // 排除当前用户
            );
            if (userByPhone != null) {
                return false;
            }
        }
        
        user.setId(id);
        // 避免前端未传密码时把库里的密码置空
        if (StringUtils.isBlank(user.getPassword())) {
            user.setPassword(existUser.getPassword());
        }
        return userMapper.updateById(user) > 0;
    }

    /** 仅更新用户头像，确保写入数据库 */
    public boolean updateAvatar(Long id, String avatar) {
        if (id == null || StringUtils.isBlank(avatar)) {
            return false;
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return false;
        }
        user.setAvatar(avatar);
        return userMapper.updateById(user) > 0;
    }
    public User getByUsername(String  username){
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername,username));
    }
    public boolean forgetPassword(String phone, String newPassword) {
        User oldUser = userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));
        if (oldUser != null) {
            oldUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
            return userMapper.updateById(oldUser) > 0;
        }
        return false;
    }

    public boolean updatePassword(int id, UserPasswordUpdate userPasswordUpdate) {
        User oldUser = userMapper.selectById(id);
        if (oldUser != null &&bCryptPasswordEncoder.matches(userPasswordUpdate.getOldPassword(),oldUser.getPassword())) {
            oldUser.setPassword(bCryptPasswordEncoder.encode(userPasswordUpdate.getNewPassword()));
            return userMapper.updateById(oldUser) > 0;
        }

        return false;
    }
    public Result<?> deleteBatch(List<Integer> ids) {
        for(Integer id:ids){
            String relation = checkRelation(id);
            if(relation!=null){
                return Result.error("-1",relation);
            }
        }
        return userMapper.deleteByIds(ids) > 0 ? Result.success() : Result.error("-1","删除失败") ;
    }

    public List<User> getAllUsers() {
        return userMapper.selectList(new QueryWrapper<>());
    }
    public User getUserById(int id) {
        return userMapper.selectById(id);
    }
    public Page<User> getUsersByPage(String username, String sex, String name, String role, String currentRole, Integer currentPage, Integer size) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        if (StringUtils.isNotBlank(sex)) {
            queryWrapper.eq(User::getSex, sex);
        }
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(User::getName, name);
        }
        if (StringUtils.isNotBlank(role)) {
            queryWrapper.eq(User::getRole, role);
        }
        Page<User> page = new Page<>(currentPage, size);
        return userMapper.selectPage(page, queryWrapper);
    }
    public List<User> getUsersByRole(User.Role role) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getRole, role.name());
        return userMapper.selectList(queryWrapper);
    }
    public boolean resetPassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(bCryptPasswordEncoder.encode(newPassword));
            return userMapper.updateById(user) > 0;
        }
        return false;
    }
    public List<User> searchUsers(String keyword) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(User::getUsername, keyword)
                .or()
                .like(User::getPhone, keyword)
                .or()
                .like(User::getName, keyword);
        return userMapper.selectList(queryWrapper);
    }

    public String checkRelation(int userId) {

        User user = userMapper.selectById(userId);
        //检查是否为业主
        if(user.getRole().equals("OWNER")){
            List<Owner> owners = ownerMapper.selectList(new LambdaQueryWrapper<Owner>().eq(Owner::getUserId, userId));
            if(owners!=null && !owners.isEmpty()){
                return "存在关联业主";
            }
        }
        //检查是否为家政人员
        if(user.getRole().equals("KEEPER")){
            List<Housekeeper> housekeepers = houseKeeperMapper.selectList(new LambdaQueryWrapper<Housekeeper>().eq(Housekeeper::getUserId, userId));
            if(housekeepers!=null&&!housekeepers.isEmpty()){
                return "存在关联家政人员";
            }
        }
        //检查是否为后勤人员
        if(user.getRole().equals("MAINTENANCE")){
            List<MaintenanceStaff> maintenanceStaffs = maintenanceStaffMapper.selectList(new LambdaQueryWrapper<MaintenanceStaff>().eq(MaintenanceStaff::getUserId, userId));
            if(maintenanceStaffs!=null&&!maintenanceStaffs.isEmpty()){
                return "存在关联后勤人员";
            }
        }
    return null;
    }
    
}
