package com.ticket.god_of_ticketing.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

	private static final String REDIS_HOST_PREFIX = "redis://";

	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		// Docker로 띄운 Redis에 접속 (localhost:6379)
		config.useSingleServer().setAddress(REDIS_HOST_PREFIX + "192.168.99.100:6379");

		return Redisson.create(config);
	}
}