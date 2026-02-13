package com.blog.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

@Configuration
@EnableTransactionManagement
public class AppConfig {
	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager(
				"Post.getAll",
				"Post.findById",
				"Post.count",
				"Comment.findByPostId",
				"PostTags.findAll",
				"PostTags.findByPostId",
				"PostTags.count"
		);
	}
}
