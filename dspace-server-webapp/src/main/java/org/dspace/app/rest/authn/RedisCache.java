package org.dspace.app.rest.authn;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisCache<V> {

    private static final Logger log = LoggerFactory.getLogger(RedisCache.class);
    private static volatile RedisCache instance;

    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private RedisCache() {
        try {
            ConfigurationService configService =
                    DSpaceServicesFactory.getInstance().getConfigurationService();

            String host = configService.getProperty("dspace.redis.host", "localhost");
            int port = configService.getIntProperty("dspace.redis.port", 6379);
            String password = configService.getProperty("dspace.redis.password");

            int maxTotal = configService.getIntProperty("dspace.redis.pool.maxTotal", 20);
            int maxIdle = configService.getIntProperty("dspace.redis.pool.maxIdle", 10);
            int minIdle = configService.getIntProperty("dspace.redis.pool.minIdle", 2);

            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(maxTotal);
            poolConfig.setMaxIdle(maxIdle);
            poolConfig.setMinIdle(minIdle);

            if (password == null || password.isBlank()) {
                jedisPool = new JedisPool(poolConfig, host, port);
            } else {
                jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
            }

            log.info("RedisCache initialized [host={}, port={}]", host, port);

        } catch (Exception e) {
            log.error("Failed to initialize RedisCache", e);
            throw new RuntimeException("Redis initialization failed", e);
        }
    }

    public static RedisCache getInstance() {
        if (instance == null) {
            synchronized (RedisCache.class) {
                if (instance == null) {
                    instance = new RedisCache();
                }
            }
        }
        return instance;
    }

    public void put(String key, V value, int ttlSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = objectMapper.writeValueAsString(value);
            jedis.setex(key, ttlSeconds, json);
            log.debug("Redis PUT | key={} | ttl={}s", key, ttlSeconds);
        } catch (Exception e) {
            log.error("Redis PUT failed | key={}", key, e);
            throw new RuntimeException("Redis put failed", e);
        }
    }

    public V get(String key, Class<V> clazz) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Redis GET failed | key={}", key, e);
            throw new RuntimeException("Redis get failed", e);
        }
    }

    public void remove(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        } catch (Exception e) {
            log.error("Redis DEL failed | key={}", key, e);
        }
    }
}
