package com.dalomao.dubbo.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class RedisApi {

    private static JedisPool pool;

    private static Properties prop = null;

    private static JedisPoolConfig config = null;

    static {
        InputStream in = RedisApi.class.getClassLoader()
                .getResourceAsStream("redis.properties");

        prop = new Properties();
        try {
            prop.load(in);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        config = new JedisPoolConfig();
        config.setMaxTotal(Integer.valueOf(prop.getProperty("MAX_TOTAL")));
        config.setMaxIdle(Integer.valueOf(prop.getProperty("MAX_IDLE")));
        config.setMaxWaitMillis(Integer.valueOf(prop.getProperty("MAX_WAIT_MILLIS")));
        config.setTestOnBorrow(Boolean.valueOf(prop.getProperty("TEST_ON_BORROW")));
        config.setTestOnReturn(Boolean.valueOf(prop.getProperty("TEST_ON_RETURN")));
        config.setTestWhileIdle(Boolean.valueOf(prop.getProperty("TEST_WHILE_IDLE")));
    }

    public static void createJedisPool(String address) {
        pool = new JedisPool(config, address.split(":")[0],
                Integer.valueOf(address.split(":")[1]), 100000);
    }

    /**
     * 创建redis连接池
     */
    public static JedisPool getPool() {

        if (pool == null) {

            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(Integer.valueOf(prop.getProperty("MAX_TOTAL")));
            config.setMaxIdle(Integer.valueOf(prop.getProperty("MAX_IDLE")));
            config.setMaxWaitMillis(Integer.valueOf(prop.getProperty("MAX_WAIT_MILLIS")));
            config.setTestOnBorrow(Boolean.valueOf(prop.getProperty("TEST_ON_BORROW")));
            config.setTestOnReturn(Boolean.valueOf(prop.getProperty("TEST_ON_RETURN")));
            config.setTestWhileIdle(Boolean.valueOf(prop.getProperty("TEST_WHILE_IDLE")));
            pool = new JedisPool(config, prop.getProperty("REDIS_IP"),
                    Integer.valueOf(prop.getProperty("REDIS_PORT")));
        }

        return pool;
    }

    public static void returnResource(JedisPool pool, Jedis redis) {
        if (redis != null) {
            pool.returnResource(redis);
        }
    }

    /**
     * 订阅
     * @param channel
     * @param msg
     */
    public static void publish(String channel, String msg) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.publish(channel, msg);
        }
        catch (Exception e) {

        }
        finally {
            returnResource(pool, jedis);
        }
    }

    /**
     * 发布
     * @param channel
     * @param ps
     */
    public static void subsribe(String channel, JedisPubSub ps) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.subscribe(ps, channel);
        }
        catch (Exception e) {

        }
        finally {
            returnResource(pool, jedis);
        }
    }

    public static Long hdel(String key, String key1) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.hdel(key, key1);
        }
        catch (Exception e) {

        }
        finally {
            returnResource(pool, jedis);
        }
        return null;
    }

    /**
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        Jedis jedis = null;
        String value = null;
        try {
            jedis = pool.getResource();
            value = jedis.get(key);
        }
        catch (Exception e) {

        }
        finally {
            returnResource(pool, jedis);
        }
        return value;
    }

    public static boolean exists(String key) {
        Jedis jedis = null;
        boolean value = false;
        try {
            jedis = pool.getResource();
            value = jedis.exists(key);
        }
        catch (Exception e) {

        }
        finally {
            returnResource(pool, jedis);
        }
        return value;
    }

    /**
     *
     */
    public static String set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.set(key, value);
        }
        catch (Exception e) {

            return "0";
        }
        finally {
            returnResource(pool, jedis);
        }
    }

    public static String set(String key, String value, int expire) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.expire(key, expire);
            return jedis.set(key, value);
        }
        catch (Exception e) {

            return "0";
        }
        finally {
            returnResource(pool, jedis);
        }
    }

    public static Long del(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.del(key);
        }
        catch (Exception e) {
            return null;
        }
        finally {
            returnResource(pool, jedis);
        }
    }

    /**
     * @Description 操作list类型数据的
     * @param @param key
     * @param @param strings  list.add(object)
     * @param @return 参数
     * @return Long 返回类型
     * @throws
     */

    public static Long lpush(String key, String... strings) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lpush(key, strings);
        }
        catch (Exception e) {

            return 0L;
        }
        finally {
            returnResource(pool, jedis);
        }
    }

    public static List<String> lrange(String key) {
        Jedis jedis = null;
        String value = null;
        try {
            jedis = pool.getResource();
            return jedis.lrange(key, 0, -1);
        }
        catch (Exception e) {
            return null;
        }
        finally {
            returnResource(pool, jedis);
        }
    }

    public static String lset(String key, long index, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lset(key, index, value);
        }
        catch (Exception e) {
            return null;
        }
        finally {
            returnResource(pool, jedis);
        }
    }

    public static String hmset(String key, Map map) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.hmset(key, map);
        }
        catch (Exception e) {

            return "0";
        }
        finally {
            returnResource(pool, jedis);
        }
    }

    public static List<String> hmget(String key, String... strings) {
        Jedis jedis = null;
        String value = null;
        try {
            jedis = pool.getResource();
            return jedis.hmget(key, strings);
        }
        catch (Exception e) {

        }
        finally {
            returnResource(pool, jedis);
        }
        return null;
    }


}
