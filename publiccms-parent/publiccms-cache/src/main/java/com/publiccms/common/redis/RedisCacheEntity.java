package com.publiccms.common.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.publiccms.common.cache.CacheEntity;
import com.publiccms.common.constants.Constants;
import com.publiccms.common.redis.serializer.Serializer;
import com.publiccms.common.redis.serializer.StringSerializer;
import com.publiccms.common.redis.serializer.ValueSerializer;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.RedisUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 *
 * RedisCacheEntity
 *
 * @param <K>
 * @param <V>
 *
 */
public class RedisCacheEntity<K, V> implements CacheEntity<K, V>, java.io.Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JedisPool jedisPool;
    private String region;
    private static final StringSerializer stringSerializer = new StringSerializer();
    private final Serializer<V> valueSerializer = new ValueSerializer<>();

    public static final String CACHE_PREFIX = "cms2025::";

    @Override
    public List<V> put(K key, V value) {
        Jedis jedis = jedisPool.getResource();
        jedis.set(getKey(key), valueSerializer.serialize(value));
        jedis.close();
        return null;
    }

    @Override
    public void put(K key, V value, Long expiryInSeconds) {
        Jedis jedis = jedisPool.getResource();
        if (null == expiryInSeconds) {
            jedis.set(getKey(key), valueSerializer.serialize(value));
        } else {
            jedis.setex(getKey(key), expiryInSeconds, valueSerializer.serialize(value));
        }
        jedis.close();
    }

    @Override
    public V get(K key) {
        Jedis jedis = jedisPool.getResource();
        V value = valueSerializer.deserialize(jedis.get(getKey(key)));
        jedis.close();
        return value;
    }

    @Override
    public V remove(K key) {
        Jedis jedis = jedisPool.getResource();
        byte[] byteKey = getKey(key);
        V value = valueSerializer.deserialize(jedis.get(byteKey));
        jedis.del(byteKey);
        jedis.close();
        return value;
    }

    @Override
    public List<V> clear(boolean recycling) {
        if (recycling) {
            List<V> list = new ArrayList<>();
            Jedis jedis = jedisPool.getResource();
            Set<String> keyList = jedis.keys(CommonUtils.joinString(region, Constants.COLON, "*"));
            keyList.forEach(k -> {
                byte[] byteKey = stringSerializer.serialize(k);
                V value = valueSerializer.deserialize(jedis.get(byteKey));
                if (0 < jedis.del(k)) {
                    list.add(value);
                }
            });
            jedis.close();
            return list;
        } else {
            Jedis jedis = jedisPool.getResource();
            Set<String> keyList = jedis.keys(CommonUtils.joinString(region, Constants.COLON, "*"));
            keyList.forEach(k -> {
                jedis.del(k);
            });
            jedis.close();
            return null;
        }
    }

    @Override
    public boolean contains(K key) {
        Jedis jedis = jedisPool.getResource();
        boolean exits = jedis.exists(getKey(key));
        jedis.close();
        return exits;
    }

    private byte[] getKey(K key) {
        return stringSerializer.serialize(CommonUtils.joinString(CACHE_PREFIX, region, Constants.COLON, key));
    }

    @Override
    public RedisCacheEntity<K, V> init(String region, Properties properties) {
        return init(region, RedisUtils.createOrGetJedisPool(properties));
    }

    public RedisCacheEntity<K, V> init(String region, JedisPool pool) {
        this.region = region;
        this.jedisPool = pool;
        return this;
    }

    public String getRegion() {
        return region;
    }

}