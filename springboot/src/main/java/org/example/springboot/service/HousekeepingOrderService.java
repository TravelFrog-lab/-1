package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.example.springboot.common.Result;
import org.example.springboot.dto.RepairEvaluationRequest;
import org.example.springboot.entity.*;
import org.example.springboot.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class HousekeepingOrderService {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(HousekeepingOrderService.class);
    
    @Autowired
    private HousekeepingOrderMapper housekeepingOrderMapper;
    
    @Autowired
    private OwnerMapper ownerMapper;
    
    @Autowired
    private HousekeeperMapper housekeeperMapper;
    
    @Autowired
    private HousekeepingServiceMapper housekeepingServiceMapper;
    
    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private HousekeeperServiceRelationMapper housekeeperServiceMapper;

    private Housekeeper getHousekeeperByUserId(Long housekeeperUserId) {
        if (housekeeperUserId == null) {
            return null;
        }
        List<Housekeeper> housekeepers = housekeeperMapper.selectList(
                new LambdaQueryWrapper<Housekeeper>().eq(Housekeeper::getUserId, housekeeperUserId));
        if (housekeepers == null || housekeepers.isEmpty()) {
            return null;
        }
        return housekeepers.get(0);
    }
    
    /**
     * 分页查询订单
     */
    public Result<IPage<HousekeepingOrder>> page(Integer pageNum, Integer pageSize, String orderNo, String status, Long ownerId, Long housekeeperId, Long housekeeperUserId, String ownerTab, String housekeeperTab) {
        LOGGER.info("分页查询订单: pageNum={}, pageSize={}, orderNo={}, status={}, ownerId={}, housekeeperId={}, ownerTab={}, housekeeperTab={}",
                pageNum, pageSize, orderNo, status, ownerId, housekeeperId, ownerTab, housekeeperTab);

        LambdaQueryWrapper<HousekeepingOrder> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            queryWrapper.like(HousekeepingOrder::getOrderNo, orderNo);
        }
        if (ownerId != null) {
            queryWrapper.eq(HousekeepingOrder::getOwnerId, ownerId);
        }
        /**
         * 业主端与报修一致：pending / progress / to_evaluate / done。
         * 管理员端也可只传 ownerTab（不传 ownerId）按同类语义筛全站订单。
         * 有 ownerTab 时优先于 status（避免与 Tab 语义冲突）。
         */
        if (StringUtils.hasText(ownerTab)) {
            switch (ownerTab.trim()) {
                case "pending":
                    queryWrapper.and(w -> w.eq(HousekeepingOrder::getStatus, "PENDING")
                            .or().eq(HousekeepingOrder::getStatus, "CANCELLED"));
                    break;
                case "progress":
                    // 处理中：已接单 / 服务中（含未付款；家政接单后业主应在此看到订单）
                    queryWrapper.in(HousekeepingOrder::getStatus, Arrays.asList("IN_PROGRESS", "CONFIRMED"));
                    break;
                case "to_evaluate":
                    queryWrapper.eq(HousekeepingOrder::getStatus, "COMPLETED")
                            .isNull(HousekeepingOrder::getEvaluationRating);
                    break;
                case "done":
                    queryWrapper.eq(HousekeepingOrder::getStatus, "COMPLETED")
                            .isNotNull(HousekeepingOrder::getEvaluationRating);
                    break;
                default:
                    if (StringUtils.hasText(status)) {
                        queryWrapper.eq(HousekeepingOrder::getStatus, status);
                    }
                    break;
            }
        } else if (housekeeperUserId != null && StringUtils.hasText(housekeeperTab)) {
            /** 家政人员端与业主端 Tab 一致：pending / progress / to_evaluate / done */
            switch (housekeeperTab.trim()) {
                case "pending":
                    queryWrapper.and(w -> w.eq(HousekeepingOrder::getStatus, "PENDING")
                            .or().eq(HousekeepingOrder::getStatus, "CANCELLED"));
                    break;
                case "progress":
                    queryWrapper.in(HousekeepingOrder::getStatus, Arrays.asList("IN_PROGRESS", "CONFIRMED"));
                    break;
                case "to_evaluate":
                    queryWrapper.eq(HousekeepingOrder::getStatus, "COMPLETED")
                            .isNull(HousekeepingOrder::getEvaluationRating);
                    break;
                case "done":
                    queryWrapper.eq(HousekeepingOrder::getStatus, "COMPLETED")
                            .isNotNull(HousekeepingOrder::getEvaluationRating);
                    break;
                default:
                    if (StringUtils.hasText(status)) {
                        queryWrapper.eq(HousekeepingOrder::getStatus, status);
                    }
                    break;
            }
        } else if (StringUtils.hasText(status)) {
            queryWrapper.eq(HousekeepingOrder::getStatus, status);
        }
        if (housekeeperId != null) {

            queryWrapper.eq(HousekeepingOrder::getHousekeeperId, housekeeperId);
        }
        if (housekeeperUserId != null) {
            List<Housekeeper> housekeepers = housekeeperMapper.selectList(
                    new LambdaQueryWrapper<Housekeeper>().eq(Housekeeper::getUserId, housekeeperUserId));
            LOGGER.info("housekeepers for userId {}: {}", housekeeperUserId, housekeepers);
            if (housekeepers == null || housekeepers.isEmpty()) {
                Page<HousekeepingOrder> emptyPage = new Page<>(pageNum, pageSize);
                emptyPage.setRecords(java.util.Collections.emptyList());
                emptyPage.setTotal(0);
                return Result.success(emptyPage);
            }
            queryWrapper.eq(HousekeepingOrder::getHousekeeperId, housekeepers.get(0).getId());
        }
        
        queryWrapper.orderByDesc(HousekeepingOrder::getCreatedAt);
        
        Page<HousekeepingOrder> page = new Page<>(pageNum, pageSize);
        IPage<HousekeepingOrder> orderPage = housekeepingOrderMapper.selectPage(page, queryWrapper);
        
        // 填充关联信息
        orderPage.getRecords().forEach(this::fillOrderInfo);
        
        return Result.success(orderPage);
    }
    
    /**
     * 根据ID查询订单
     */
    public Result<HousekeepingOrder> getById(Long id) {
        LOGGER.info("根据ID查询订单: id={}", id);
        
        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 填充关联信息
        fillOrderInfo(order);
        
        return Result.success(order);
    }
    
    /**
     * 创建订单
     */
    public Result<?> create(HousekeepingOrder order) {
        LOGGER.info("创建订单: {}", order);
        
        // 检查业主是否存在
        Owner owner = ownerMapper.selectById(order.getOwnerId());
        if (owner == null) {
            return Result.error("-1", "业主不存在");
        }
        
        // 检查家政人员是否存在
        Housekeeper housekeeper = housekeeperMapper.selectById(order.getHousekeeperId());
        if (housekeeper == null) {
            return Result.error("-1", "家政人员不存在");
        }
        
        // 检查服务项目是否存在
        HousekeepingService service = housekeepingServiceMapper.selectById(order.getServiceId());
        if (service == null) {
            return Result.error("-1", "服务项目不存在");
        }
        

        
        // 检查家政人员是否提供该服务
        LambdaQueryWrapper<HousekeeperServiceRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeeperServiceRelation::getHousekeeperId, order.getHousekeeperId())
                .eq(HousekeeperServiceRelation::getServiceId, order.getServiceId());
        HousekeeperServiceRelation housekeeperService = housekeeperServiceMapper.selectOne(queryWrapper);
        if (housekeeperService == null) {
            return Result.error("-1", "该家政人员不提供此服务");
        }
        
        // 生成订单编号
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        
        // 设置订单金额
        BigDecimal amount = calculateAmount(housekeeperService.getPrice(), order.getServiceDuration(), service.getUnit());
        order.setAmount(amount);
        
        // 设置订单状态
        order.setStatus("PENDING");
        order.setPaymentStatus("UNPAID");
        
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        
        housekeepingOrderMapper.insert(order);
        fillOrderInfo(order);
        return Result.success(order);
    }
    
    /**
     * 更新订单
     */
    public Result<?> update(HousekeepingOrder order) {
        LOGGER.info("更新订单: {}", order);
        
        // 检查订单是否存在
        HousekeepingOrder existOrder = housekeepingOrderMapper.selectById(order.getId());
        if (existOrder == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 检查订单状态
        if ("COMPLETED".equals(existOrder.getStatus()) || "CANCELLED".equals(existOrder.getStatus())) {
            return Result.error("-1", "已完成或已取消的订单不能修改");
        }
        
        // 如果修改了服务项目或家政人员，需要重新计算金额
        if (!existOrder.getServiceId().equals(order.getServiceId()) || 
                !existOrder.getHousekeeperId().equals(order.getHousekeeperId()) ||
                !existOrder.getServiceDuration().equals(order.getServiceDuration())) {
            
            // 检查家政人员是否提供该服务
            LambdaQueryWrapper<HousekeeperServiceRelation> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(HousekeeperServiceRelation::getHousekeeperId, order.getHousekeeperId())
                    .eq(HousekeeperServiceRelation::getServiceId, order.getServiceId());
            HousekeeperServiceRelation housekeeperService = housekeeperServiceMapper.selectOne(queryWrapper);
            if (housekeeperService == null) {
                return Result.error("-1", "该家政人员不提供此服务");
            }
            
            // 获取服务项目信息
            HousekeepingService service = housekeepingServiceMapper.selectById(order.getServiceId());
            if (service == null) {
                return Result.error("-1", "服务项目不存在");
            }
            
            // 重新计算金额
            BigDecimal amount = calculateAmount(housekeeperService.getPrice(), order.getServiceDuration(), service.getUnit());
            order.setAmount(amount);
        }
        
        // 更新时间
        order.setUpdatedAt(LocalDateTime.now());
        
        housekeepingOrderMapper.updateById(order);
        return Result.success();
    }
    
    /**
     * 取消订单
     */
    public Result<?> cancel(Long id) {
        LOGGER.info("取消订单: id={}", id);
        
        // 检查订单是否存在
        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 检查订单状态
        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            return Result.error("-1", "已完成或已取消的订单不能取消");
        }

        // 已支付不可直接取消（退款需线下/后台处理）
        if ("PAID".equals(order.getPaymentStatus())) {
            return Result.error("-1", "已支付订单不可取消，如需退款请联系物业");
        }
        
        // 更新订单状态
        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        
        housekeepingOrderMapper.updateById(order);
        return Result.success();
    }
    
    /**
     * 确认订单
     */
    public Result<?> confirm(Long id, Long housekeeperUserId) {
        LOGGER.info("确认订单: id={}", id);
        
        // 检查订单是否存在
        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 检查订单状态
        if (!"PENDING".equals(order.getStatus())) {
            return Result.error("-1", "只有待接单状态的订单可以确认");
        }

        // 家政人员接单时，必须是分配给自己的订单
        if (housekeeperUserId != null) {
            Housekeeper housekeeper = getHousekeeperByUserId(housekeeperUserId);
            if (housekeeper == null) {
                return Result.error("-1", "家政人员不存在");
            }
            if (!order.getHousekeeperId().equals(housekeeper.getId())) {
                return Result.error("-1", "只能接单分配给自己的订单");
            }
        }
        
        // 接单后为「已接单」，业主支付后再进入进行中（与支付宝回调、进度展示一致）
        order.setStatus("CONFIRMED");
        order.setUpdatedAt(LocalDateTime.now());

        housekeepingOrderMapper.updateById(order);
        return Result.success();
    }

    /**
     * 拒接订单（家政人员）
     */
    public Result<?> reject(Long id, Long housekeeperUserId, String reason) {
        LOGGER.info("拒接订单: id={}, housekeeperUserId={}", id, housekeeperUserId);

        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            return Result.error("-1", "只有待接单状态的订单可以拒接");
        }
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error("-1", "请填写拒接原因");
        }
        if (reason.length() > 200) {
            return Result.error("-1", "拒接原因不能超过200字");
        }

        Housekeeper housekeeper = getHousekeeperByUserId(housekeeperUserId);
        if (housekeeper == null) {
            return Result.error("-1", "家政人员不存在");
        }
        if (!order.getHousekeeperId().equals(housekeeper.getId())) {
            return Result.error("-1", "只能拒接分配给自己的订单");
        }

        // 使用已存在状态：拒接后记为已取消，并把原因写入备注
        order.setStatus("CANCELLED");
        order.setRemark("拒接原因：" + reason.trim());
        order.setUpdatedAt(LocalDateTime.now());
        housekeepingOrderMapper.updateById(order);
        return Result.success();
    }
    
    /**
     * 开始服务
     */
    public Result<?> start(Long id) {
        LOGGER.info("开始服务: id={}", id);
        
        // 检查订单是否存在
        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 检查订单状态
        if (!"IN_PROGRESS".equals(order.getStatus())) {
            return Result.error("-1", "只有进行中的订单可以开始服务");
        }
        
        // 检查支付状态
        if (!"PAID".equals(order.getPaymentStatus())) {
            return Result.error("-1", "订单未支付，不能开始服务");
        }
        
        // 已是进行中，仅更新时间（兼容旧前端可能仍调用该接口）
        order.setUpdatedAt(LocalDateTime.now());
        
        housekeepingOrderMapper.updateById(order);
        return Result.success();
    }
    
    /**
     * 完成服务
     */
    public Result<?> complete(Long id) {
        LOGGER.info("完成服务: id={}", id);
        
        // 检查订单是否存在
        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        // 检查订单状态（兼容历史已接单状态）
        if (!"IN_PROGRESS".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            return Result.error("-1", "只有进行中或已接单的订单可以完成");
        }
        
        // 更新订单状态
        order.setStatus("COMPLETED");
        order.setUpdatedAt(LocalDateTime.now());
        
        housekeepingOrderMapper.updateById(order);
        return Result.success();
    }
    
    /**
     * 支付订单
     */
    public Result<?> pay(Long id) {
        LOGGER.info("支付订单: id={}", id);
        
        // 检查订单是否存在
        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        if ("CANCELLED".equals(order.getStatus())) {
            return Result.error("-1", "当前订单状态不可支付");
        }
        // 已完工但未支付：允许在「待评价」列表中补付（状态保持 COMPLETED）
        if ("COMPLETED".equals(order.getStatus())) {
            if (order.getEvaluationRating() != null) {
                return Result.error("-1", "订单已评价，不可支付");
            }
            if (!"UNPAID".equals(order.getPaymentStatus())) {
                return Result.error("-1", "当前订单状态不可支付");
            }
            order.setPaymentStatus("PAID");
            order.setPayTime(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            housekeepingOrderMapper.updateById(order);
            return Result.success();
        }
        if (!"CONFIRMED".equals(order.getStatus()) && !"IN_PROGRESS".equals(order.getStatus())) {
            return Result.error("-1", "请等待服务人员接单后再支付");
        }

        // 检查支付状态
        if ("PAID".equals(order.getPaymentStatus()) || "REFUNDED".equals(order.getPaymentStatus())) {
            return Result.error("-1", "订单已支付或已退款");
        }

        order.setPaymentStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        // 已接单且付款成功后进入「进行中」，与业主端进度条「服务中」一致
        if ("CONFIRMED".equals(order.getStatus())) {
            order.setStatus("IN_PROGRESS");
        }

        housekeepingOrderMapper.updateById(order);
        return Result.success();
    }
    
    /**
     * 检查订单支付状态
     */
    public Result<?> checkPayStatus(String orderNo) {
        LOGGER.info("检查订单支付状态: orderNo={}", orderNo);
        
        // 查询订单
        LambdaQueryWrapper<HousekeepingOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeepingOrder::getOrderNo, orderNo);
        HousekeepingOrder order = housekeepingOrderMapper.selectOne(queryWrapper);
        
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        
        return Result.success(order.getPaymentStatus());
    }

    /**
     * 业主评价已完成订单（1-5 星 + 文字，仅一次；与报修一致）
     */
    @Transactional
    public Result<?> submitOwnerEvaluation(Long id, Long ownerId, RepairEvaluationRequest body) {
        if (body == null) {
            return Result.error("-1", "请求体不能为空");
        }
        HousekeepingOrder order = housekeepingOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1", "订单不存在");
        }
        if (ownerId == null || !ownerId.equals(order.getOwnerId())) {
            return Result.error("-1", "只能评价自己的订单");
        }
        if (!"COMPLETED".equals(order.getStatus())) {
            return Result.error("-1", "仅已完成订单可评价");
        }
        if (order.getEvaluationRating() != null) {
            return Result.error("-1", "您已评价过");
        }
        Integer rating = body.getEvaluationRating();
        if (rating == null || rating < 1 || rating > 5) {
            return Result.error("-1", "请选择1-5星评分");
        }
        String text = body.getEvaluation();
        if (text == null || text.trim().isEmpty()) {
            return Result.error("-1", "请填写评价内容");
        }
        if (text.trim().length() > 500) {
            return Result.error("-1", "评价内容最多500个字符");
        }
        order.setEvaluationRating(rating);
        order.setEvaluation(text.trim());
        order.setUpdatedAt(LocalDateTime.now());
        if (housekeepingOrderMapper.updateById(order) <= 0) {
            return Result.error("-1", "评价提交失败");
        }
        persistHousekeeperRatingFromOrders(order.getHousekeeperId());
        return Result.success();
    }

    /**
     * 根据已完成且已评价订单计算家政员平均星级（1 位小数）；无评价记录时返回 null。
     */
    public BigDecimal computeAverageRatingForHousekeeper(Long housekeeperId) {
        if (housekeeperId == null) {
            return null;
        }
        LambdaQueryWrapper<HousekeepingOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HousekeepingOrder::getHousekeeperId, housekeeperId)
                .eq(HousekeepingOrder::getStatus, "COMPLETED")
                .isNotNull(HousekeepingOrder::getEvaluationRating);
        List<HousekeepingOrder> list = housekeepingOrderMapper.selectList(queryWrapper);
        if (list == null || list.isEmpty()) {
            return null;
        }
        int sum = 0;
        for (HousekeepingOrder o : list) {
            sum += o.getEvaluationRating();
        }
        return BigDecimal.valueOf((double) sum / list.size()).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 将平均星级写回 housekeeper 表（与列表接口展示一致）
     */
    public void persistHousekeeperRatingFromOrders(Long housekeeperId) {
        if (housekeeperId == null) {
            return;
        }
        BigDecimal avg = computeAverageRatingForHousekeeper(housekeeperId);
        Housekeeper h = housekeeperMapper.selectById(housekeeperId);
        if (h == null) {
            return;
        }
        h.setRating(avg);
        h.setUpdatedAt(LocalDateTime.now());
        housekeeperMapper.updateById(h);
    }

    /**
     * 填充订单关联信息
     */
    private void fillOrderInfo(HousekeepingOrder order) {
        // 填充业主信息
        Owner owner = ownerMapper.selectById(order.getOwnerId());
        if(owner!=null&&owner.getUserId()!=null){
            User user = userMapper.selectById(owner.getUserId());
            owner.setUser(user);
        }
        order.setOwner(owner);
        
        // 填充家政人员信息（评分按已完成订单评价均分）
        Housekeeper housekeeper = housekeeperMapper.selectById(order.getHousekeeperId());
        if (housekeeper != null) {
            housekeeper.setRating(computeAverageRatingForHousekeeper(housekeeper.getId()));
        }
        order.setHousekeeper(housekeeper);
        
        // 填充服务项目信息
        HousekeepingService service = housekeepingServiceMapper.selectById(order.getServiceId());
        order.setService(service);

        // 数据自洽：已支付却仍为待接单时，自动更正为进行中（兼容历史/异常数据）
        if ("PAID".equals(order.getPaymentStatus()) && "PENDING".equals(order.getStatus())) {
            order.setStatus("IN_PROGRESS");
            order.setUpdatedAt(LocalDateTime.now());
            housekeepingOrderMapper.updateById(order);
            LOGGER.warn("订单 {} 已支付仍为待接单，已自动更正为进行中", order.getOrderNo());
        }
    }
    
    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        // 生成格式: 年月日时分秒 + 4位随机数
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String dateTime = LocalDateTime.now().format(formatter);
        
        // 生成4位随机数
        Random random = new Random();
        int randomNum = random.nextInt(10000);
        String randomStr = String.format("%04d", randomNum);
        
        return "HK" + dateTime + randomStr;
    }
    
    /**
     * 计算订单金额
     */
    private BigDecimal calculateAmount(BigDecimal price, Integer duration, String unit) {
        if ("小时".equals(unit)) {
            // 按小时计费，不足1小时按1小时计算
            int hours = (duration + 59) / 60;  // 向上取整
            return price.multiply(new BigDecimal(hours));
        } else if ("次".equals(unit)) {
            // 按次计费，直接返回价格
            return price;
        } else if ("平米".equals(unit)) {
            // 按平米计费，需要房屋面积信息，这里简化处理
            return price;
        } else {
            // 其他计费方式，默认返回价格
            return price;
        }
    }
} 