package com.ruoyi.common.core.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 工具类（若依标准能力封装）。
 *
 * 【重要加固】所有方法都经过 safe() 统一包装：
 * Redis 未启动 / 连接超时 / 序列化异常时，不再向上抛出异常炸掉业务，
 * 而是降级为「无缓存」语义（读返回安全默认值、写静默跳过），并只打印**一次**告警。
 *
 * 目的：本项目后端当前没有登录鉴权体系，Redis 仅作为后续接入（验证码/Token/字典缓存）
 * 的预留能力。若裸调用 redisTemplate，一旦 Redis 没起来，调用点会全线报错，
 * 排查成本极高。降级后系统退化为「不使用缓存」，功能不受影响。
 *
 * 注意：降级只应发生在 Redis 确实不可用时。正常（Redis 可用）情况下行为与原生完全一致，
 * 不会因为包装而损失任何错误信息（每次失败都记录 debug 级日志）。
 */
@Component
public class RedisCache
{
    private static final Logger log = LoggerFactory.getLogger(RedisCache.class);

    @Autowired
    public RedisTemplate<String, Object> redisTemplate;

    /** 是否已经打印过"Redis 不可用"告警，避免每调用一次刷一屏日志 */
    private volatile boolean warned = false;

    /**
     * 统一降级入口。
     *
     * @param task     真正的 Redis 操作
     * @param fallback Redis 不可用时返回的兜底值
     */
    private <T> T safe(Supplier<T> task, T fallback)
    {
        try
        {
            return task.get();
        }
        catch (Exception e)
        {
            degrade(e);
            return fallback;
        }
    }

    /** 无返回值的操作 */
    private void safeRun(Runnable task)
    {
        try
        {
            task.run();
        }
        catch (Exception e)
        {
            degrade(e);
        }
    }

    private void degrade(Exception e)
    {
        // 首次失败打 WARN 便于定位；之后只保留 DEBUG，避免日志爆炸
        if (!warned)
        {
            warned = true;
            log.warn("Redis 不可用，已自动降级为「不使用缓存」模式。请确认 Redis 是否已启动。"
                    + "错误：{}", e.getMessage());
        }
        else
        {
            log.debug("Redis 操作失败（已降级）：{}", e.getMessage());
        }
    }

    /** ============================== key 相关 ============================== */

    public Boolean hasKey(String key)
    {
        return safe(() -> redisTemplate.hasKey(key), Boolean.FALSE);
    }

    public Boolean deleteObject(String key)
    {
        return safe(() -> redisTemplate.delete(key), Boolean.FALSE);
    }

    public Long deleteObject(Collection<String> keys)
    {
        return safe(() -> redisTemplate.delete(keys), 0L);
    }

    public Boolean expire(String key, long timeout, TimeUnit unit)
    {
        return safe(() -> redisTemplate.expire(key, timeout, unit), Boolean.FALSE);
    }

    public Long getExpire(String key)
    {
        return safe(() -> redisTemplate.getExpire(key), -2L);
    }

    /** ============================== String 相关 ============================== */

    public <T> void setCacheObject(String key, T value)
    {
        safeRun(() -> redisTemplate.opsForValue().set(key, value));
    }

    public <T> void setCacheObject(String key, T value, long timeout, TimeUnit unit)
    {
        safeRun(() -> redisTemplate.opsForValue().set(key, value, timeout, unit));
    }

    public <T> T getCacheObject(String key, Class<T> clazz)
    {
        return safe(() -> {
            ValueOperations<String, Object> operation = redisTemplate.opsForValue();
            Object value = operation.get(key);
            return value == null ? null : clazz.cast(value);
        }, null);
    }

    public Object getCacheRaw(String key)
    {
        return safe(() -> redisTemplate.opsForValue().get(key), null);
    }

    public Long incr(String key, long delta)
    {
        return safe(() -> redisTemplate.opsForValue().increment(key, delta), null);
    }

    public Double incr(String key, double delta)
    {
        return safe(() -> redisTemplate.opsForValue().increment(key, delta), null);
    }

    public Long decr(String key, long delta)
    {
        return safe(() -> redisTemplate.opsForValue().decrement(key, delta), null);
    }

    /** ============================== Hash 相关 ============================== */

    public <T> void setCacheMap(String key, Map<String, T> dataMap)
    {
        if (dataMap != null)
        {
            safeRun(() -> redisTemplate.opsForHash().putAll(key, dataMap));
        }
    }

    public <T> Map<String, T> getCacheMap(String key)
    {
        return safe(() -> {
            Map<String, T> result = new HashMap<>();
            Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
            for (Map.Entry<Object, Object> entry : map.entrySet())
            {
                result.put(String.valueOf(entry.getKey()), (T) entry.getValue());
            }
            return result;
        }, new HashMap<>());
    }

    public <T> void setCacheMapValue(String key, String hKey, T value)
    {
        safeRun(() -> redisTemplate.opsForHash().put(key, hKey, value));
    }

    public <T> T getCacheMapValue(String key, String hKey, Class<T> clazz)
    {
        return safe(() -> {
            HashOperations<String, String, Object> operation = redisTemplate.opsForHash();
            Object value = operation.get(key, hKey);
            return value == null ? null : clazz.cast(value);
        }, null);
    }

    /** ============================== List 相关 ============================== */

    public <T> long setCacheList(String key, List<T> dataList)
    {
        return safe(() -> {
            Long count = redisTemplate.opsForList().rightPushAll(key, (List<Object>) (List<?>) dataList);
            return count == null ? 0L : count;
        }, 0L);
    }

    public <T> List<T> getCacheList(String key, Class<T> clazz)
    {
        return safe(() -> {
            List<Object> list = redisTemplate.opsForList().range(key, 0, -1);
            List<T> result = new ArrayList<>();
            if (list != null)
            {
                for (Object o : list)
                {
                    result.add(clazz.cast(o));
                }
            }
            return result;
        }, new ArrayList<>());
    }

    /**
     * ============================== 鉴权会话（严格，不降级） ==============================
     * 登录 token 会话是业务核心，Redis 不可用时必须显式失败（而非静默当作无会话），
     * 否则会出现「登录返回 token，但后续请求全部 401」的诡异现象，极难排查。
     * 因此这里直接操作 redisTemplate，异常向上抛出。
     */
    public <T> void setTokenObject(String key, T value, long timeout, TimeUnit unit)
    {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public <T> T getTokenObject(String key, Class<T> clazz)
    {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : clazz.cast(value);
    }

    public Boolean deleteTokenObject(String key)
    {
        return redisTemplate.delete(key);
    }

    /** ============================== Set 相关 ============================== */

    public <T> void setCacheSet(String key, Set<T> dataSet)
    {
        safeRun(() -> {
            SetOperations<String, Object> operation = redisTemplate.opsForSet();
            for (T t : dataSet)
            {
                operation.add(key, t);
            }
        });
    }

    public <T> Set<T> getCacheSet(String key, Class<T> clazz)
    {
        return safe(() -> {
            Set<Object> set = redisTemplate.opsForSet().members(key);
            Set<T> result = new HashSet<>();
            if (set != null)
            {
                for (Object o : set)
                {
                    result.add(clazz.cast(o));
                }
            }
            return result;
        }, new HashSet<>());
    }

    /** ============================== 分布式锁 ============================== */

    /**
     * 抢占分布式锁（SET key value NX EX），仅在 key 不存在时写入成功。
     *
     * <p><b>降级语义与缓存类操作相反，务必注意</b>：
     * 缓存读不到时返回兜底值是安全的（数据仍在库里）；但锁操作若把「Redis 不可用」
     * 当成「抢锁失败」返回 false，会导致 Redis 一挂业务就被完全阻断。
     * 因此这里 Redis 异常时返回 <b>true（放行）</b>，把可用性放在防重之前——
     * 防重的最后一道防线仍是数据库的原子库存扣减与发药记录的幂等判断。
     *
     * @param key     锁键
     * @param value   锁值（建议带上请求标识，便于排查）
     * @param timeout 自动释放时间
     * @param unit    时间单位
     * @return true 表示抢到锁（或 Redis 不可用已降级放行）；false 表示锁已被他人持有
     */
    public boolean tryLock(String key, String value, long timeout, TimeUnit unit)
    {
        try
        {
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
            // Redis 正常响应但返回 null 属异常边界，按「放行」处理，避免误伤业务
            return ok == null || ok;
        }
        catch (Exception e)
        {
            degrade(e);
            return true;
        }
    }

    /**
     * 释放分布式锁。仅在需要提前释放时调用；不调用也会按 TTL 自动过期。
     */
    public void unlock(String key)
    {
        safeRun(() -> redisTemplate.delete(key));
    }
}
