package com.atguigu.gmall.seckill.task;

import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.seckill.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.seckill.util.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 将数据库中秒杀商品的数据提前预热到redis中去的定时任务
 */
@Component
public class AddSeckillGoodsFronDbToRedisTask {
    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 将数据库中秒杀商品的数据提前预热到redis中去
     * cron: 秒 分 时 日 月 周 年(省略)
     *       *:任意时间
     *       ?: 忽略这个时间
     *      a.直接写数字, 在指定的时间执行
     *      b.逗号分割
     *      c.区间
     *      d.间隔
     *   fixedDelay: 每5秒执行一次,受方法的执行时间影响--->上一次执行完多久以后执行下一次(每次都只有一个任务在跑)
     *   fixedRate: 每5秒执行一次,不受方法的执行时间影响--->上一次执行开始多久以后执行下一次(可能出现两或多个个任务一起跑)
     *   initialDelay:只影响第一次什么时候执行
     *
     *  @Scheduled注解允许你定义一个方法，该方法会按照指定的计划（schedule）周期性地执行。
     *  这是一个注解，用于标记一个方法作为Spring的定时任务。被标记的方法将由Spring的定时任务框架（通常是基于ScheduledExecutorService）来调度执行。
     *  cron = "1/20 * * * * *" 这是@Scheduled注解的一个属性，用于指定定时任务的CRON表达式。
     *  CRON表达式由六个字段组成，分别表示秒、分钟、小时、日、月、星期。每个字段可以包含特定的值或范围，以及一些特殊字符（如*表示所有可能的值，/表示增量等）。
     *  在这个例子中，CRON表达式的值为"1/20 * * * * *"，这意味着：
     *  1/20：秒字段的值从0开始，每隔20秒执行一次。例如，0秒、20秒、40秒、60秒等。
     *  *：分钟字段的值是所有可能的分钟（0-59）。
     *  *：小时字段的值是所有可能的小时（0-23）。
     *  *：日字段的值是所有可能的日期（1-31）。
     *  *：月字段的值是所有可能的月份（1-12）。
     *  *：星期字段的值是所有可能的星期（0-7，其中0和7都表示星期日）。
     */
    @Scheduled(cron = "1/20 * * * * *")
    public void addSeckillGoodsFronDbToRedis() {
        //计算当前系统时间所在的时间段,以及后面4个时间段
        List<Date> dateMenus = DateUtil.getDateMenus();
        //循环查询每个时间段的商品数据
        dateMenus.stream().forEach(date -> {
            //计算当前这个时间段的开始时间: 2022-11-14 10:00:00
            String startTime = DateUtil.data2str(date, DateUtil.PATTERN_YYYY_MM_DDHHMM);
            //计算当前这个时间段的截止时间: 2022-11-14 12:00:00
            Date endTimes = DateUtil.addDateHour(date, 2);
            //计算商品数据应该在redis中的有效时间
            long liveTime = endTimes.getTime() - System.currentTimeMillis();
            //计算当前这个时间段的截止时间: 2022-11-14 12:00:00
            String endTime = DateUtil.data2str(DateUtil.addDateHour(date, 2), DateUtil.PATTERN_YYYY_MM_DDHHMM);
            //计算redis中存储时间段商品的key: 2022111410
            String key = DateUtil.data2str(date, DateUtil.PATTERN_YYYYMMDDHH);
            //拼接查询商品的条件
            LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
            //商品必须是审核通过的 "1"
            wrapper.eq(SeckillGoods::getStatus, "1");
            //商品必须在活动时间以内 startTime<=start_time
            wrapper.ge(SeckillGoods::getStartTime, startTime);
            //商品必须在活动时间以内 end_time<=endTime
            wrapper.le(SeckillGoods::getEndTime, endTime);
            //剩余库存必须大于0
            wrapper.gt(SeckillGoods::getStockCount, 0);
            //redis中没有的商品数据
            Set keys = redisTemplate.opsForHash().keys(key);
            if(keys != null && keys.size() > 0){
                wrapper.notIn(SeckillGoods::getId, keys);
            }
            //查询
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(wrapper);
            //遍历将商品数据写入redis中去
            seckillGoodsList.stream().forEach(seckillGoods -> {
                //将商品数据写入redis: TODO----1.商品活动到期需要清理掉 2.商品活动到期需要将剩余库存同步到数据库
                redisTemplate.opsForHash().put(key, seckillGoods.getId() + "", seckillGoods);
                // 构建一个商品库存剩余个数的长度的队列
                Integer stockCount = seckillGoods.getStockCount();
                // 构建好商品库存剩余个数的数组，并且数组的每一个位置都存储一个商品的id
                String[] ids = getIds(stockCount, seckillGoods.getId() + "");
                // 下单是否能够进行的依据
                redisTemplate.opsForList().leftPushAll("Seckill_Goods_Stock_Queue_" + seckillGoods.getId(), ids);
                redisTemplate.expire("Seckill_Goods_Stock_Queue_" + seckillGoods.getId(), liveTime, TimeUnit.MILLISECONDS);
                // 构建一个商品库存的自增值: 记录商品的剩余库存！
                redisTemplate.opsForHash().increment("Seckill_Goods_Increment_" + key, seckillGoods.getId() + "", stockCount);
            });
            //设置商品相关key的过期时间
            setSeckillGoodsKeysRedisExpire(key, liveTime);
        });
    }

    /**
     * 设置商品数据过期和设置商品库存自增值数据过期
     * @param key
     * @param liveTime
     */
    private void setSeckillGoodsKeysRedisExpire(String key, long liveTime) {
        //保证每个时间段的数据只设置一次过期时间
        Long result = redisTemplate.opsForHash().increment("Seckill_Goods_Expire_Times", key, 1);
        if (result > 1) {
            return ;
        }
        // 设置商品数据过期
        redisTemplate.expire(key, liveTime, TimeUnit.MILLISECONDS);
        //该什么时候进行库存同步
        rabbitTemplate.convertAndSend("seckill_goods_nomal_exchange",
                "seckill.goods.dead",
                key,
                (message -> {
                    //获取消息的属性
                    MessageProperties messageProperties = message.getMessageProperties();
                    //设置过期时间: 活动结束半小时后收到商品库存同步消息
                    //messageProperties.setExpiration((liveTime + 1800000) + "");
                    messageProperties.setExpiration(20000 + "");
                    //返回
                    return message;
                }));
    }

    /**
     * 构建商品库存数组
     * @param stockCount
     * @param goodsId
     * @return
     */
    private String[] getIds(Integer stockCount, String goodsId) {
        // 声明一个库存长度的数组
        String[] ids = new String[stockCount];
        // 为每个元素赋值
        for (int i = 0; i < stockCount; i++) {
            ids[i] = goodsId;
        }
        // 返回
        return ids;
    }
}
